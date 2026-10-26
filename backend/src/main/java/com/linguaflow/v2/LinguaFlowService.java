package com.linguaflow.v2;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.linguaflow.v2.ApiDtos.AdminStats;
import com.linguaflow.v2.ApiDtos.HeartbeatRequest;
import com.linguaflow.v2.ApiDtos.HeartbeatResponse;
import com.linguaflow.v2.ApiDtos.Narration;
import com.linguaflow.v2.ApiDtos.NarrationEditRequest;
import com.linguaflow.v2.ApiDtos.Site;
import com.linguaflow.v2.ApiDtos.SiteDetail;
import com.linguaflow.v2.ApiDtos.SiteEditRequest;
import com.linguaflow.v2.ApiDtos.SiteStats;
import com.linguaflow.v2.ApiDtos.StartJourneyRequest;
import com.linguaflow.v2.ApiDtos.StartJourneyResponse;
import com.linguaflow.v2.ApiDtos.StartPoint;
import com.linguaflow.v2.ApiDtos.StartPointEditRequest;

@Service
public class LinguaFlowService {
    private final JdbcTemplate db;
    /**
     * Khởi tạo Service, nhận JdbcTemplate do Spring cung cấp
     * để truy vấn và cập nhật cơ sở dữ liệu SQL Server.
     */
    public LinguaFlowService(JdbcTemplate db) { this.db = db; }

    /**
     * Tạo lỗi HTTP 404 (Not Found) khi không tìm thấy dữ liệu được yêu cầu,
     * ví dụ: địa điểm, mã QR hoặc phiên tham quan.
     */
    private static ResponseStatusException missing(String message) {
        return new ResponseStatusException(HttpStatus.NOT_FOUND, message);
    }
    /**
     * Tạo lỗi HTTP 400 (Bad Request) khi dữ liệu đầu vào không hợp lệ.
     */
    private static ResponseStatusException bad(String message) {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST, message);
    }

    private final org.springframework.jdbc.core.RowMapper<Site> siteMapper = (rs, n) ->
        new Site(rs.getInt("SiteId"), rs.getInt("CategoryId"), rs.getString("Name"), rs.getString("Address"),
            rs.getString("Description"), rs.getString("ImageUrl"),
            rs.getDouble("Latitude"), rs.getDouble("Longitude"),
            rs.getInt("GeofenceRadiusMeters"), rs.getBoolean("Status"));

    /**
     * Lấy danh sách địa điểm từ TouristSites.
     * includeInactive = true: lấy cả địa điểm đã tắt; false: chỉ lấy địa điểm đang hoạt động.
     */
    public List<Site> listSites(boolean includeInactive) {
        String sql = "SELECT SiteId,CategoryId,Name,Address,Description,ImageUrl,Latitude,Longitude,GeofenceRadiusMeters,Status " +
                "FROM dbo.TouristSites " + (includeInactive ? "" : "WHERE Status=1 ") + "ORDER BY Name";
        return db.query(sql, siteMapper);
    }

    /**
     * Lấy thông tin một địa điểm theo SiteId.
     * Có thể cho phép hoặc không cho phép lấy địa điểm đã tắt.
     * Nếu không tìm thấy địa điểm phù hợp thì trả lỗi HTTP 404.
     */
    public Site getSite(int id, boolean includeInactive) {
        List<Site> matches = db.query(
            "SELECT SiteId,CategoryId,Name,Address,Description,ImageUrl,Latitude,Longitude,GeofenceRadiusMeters,Status " +
            "FROM dbo.TouristSites WHERE SiteId=?" + (includeInactive ? "" : " AND Status=1"), siteMapper, id);
        if (matches.isEmpty()) throw missing("Tourist site not found");
        return matches.get(0);
    }

    /**
     * Lấy thông tin chi tiết một địa điểm kèm các bài thuyết minh.
     * Nội dung tiếng Việt lấy từ Narrations; các bản dịch lấy từ Translations.
     * Dùng để hiển thị thông tin và nội dung đọc thuyết minh cho du khách.
     */
    public SiteDetail getSiteDetail(int id) {
        Site site = getSite(id, false);
        List<Narration> narrations = new ArrayList<>();
        List<Map<String, Object>> original = db.queryForList(
            "SELECT TOP 1 NarrationId,Title,OriginalContent FROM dbo.Narrations " +
            "WHERE SiteId=? AND IsActive=1 ORDER BY NarrationId", id);
        if (!original.isEmpty()) {
            Map<String,Object> n = original.get(0);
            int narrationId = ((Number)n.get("NarrationId")).intValue();
            narrations.add(new Narration("vi", (String)n.get("Title"), (String)n.get("OriginalContent"), null));
            narrations.addAll(db.query("""
                SELECT l.Code, n.Title, t.Content, t.AudioUrl
                FROM dbo.Translations t
                JOIN dbo.Languages l ON t.LanguageId=l.LanguageId
                JOIN dbo.Narrations n ON t.NarrationId=n.NarrationId
                WHERE t.NarrationId=? AND l.IsActive=1
                ORDER BY l.Code
                """, (rs, row) -> new Narration(rs.getString("Code"), rs.getString("Title"),
                    rs.getString("Content"), rs.getString("AudioUrl")), narrationId));
        }
        return new SiteDetail(site,narrations);
    }

    /**
     * Lấy danh sách điểm bắt đầu hành trình và mã QR tương ứng.
     * Một mã QR được xem là hoạt động khi cả QR và điểm bắt đầu đều đang bật.
     */
    public List<StartPoint> listStartPoints() {
        return db.query("""
            SELECT s.StartPointId,s.Name,s.Address,q.QrCodeValue,q.IsActive AS QrActive,s.IsActive AS PointActive
            FROM dbo.StartPoints s JOIN dbo.QRCodes q ON q.StartPointId=s.StartPointId
            ORDER BY s.StartPointId, q.QrId
            """, (rs, row) -> new StartPoint(rs.getInt("StartPointId"), rs.getString("Name"),
                rs.getString("Address"),rs.getString("QrCodeValue"),
                rs.getBoolean("QrActive") && rs.getBoolean("PointActive")));
    }

    /**
     * Tìm điểm bắt đầu hành trình dựa trên chuỗi mã QR.
     * Kiểm tra mã QR không rỗng, không quá dài và còn hoạt động.
     * Trả lỗi nếu mã không hợp lệ, không tồn tại hoặc đã bị tắt.
     */
    public StartPoint findStartPointForCode(String code) {
        if (code == null || code.isBlank() || code.length() > 100) throw bad("Invalid QR code");
        List<StartPoint> result = db.query("""
            SELECT s.StartPointId,s.Name,s.Address,q.QrCodeValue
            FROM dbo.StartPoints s JOIN dbo.QRCodes q ON q.StartPointId=s.StartPointId
            WHERE q.QrCodeValue=? AND q.IsActive=1 AND s.IsActive=1
            """, (rs,row) -> new StartPoint(rs.getInt(1),rs.getString(2),rs.getString(3),rs.getString(4),true), code);
        if (result.isEmpty()) throw missing("QR is invalid or disabled");
        return result.get(0);
    }

    @Transactional
    /**
     * Bắt đầu hành trình sau khi xác thực mã QR.
     * Tạo SessionId, thêm một phiên vào VisitorSessions và ghi log vào QRScanLogs.
     * @Transactional giúp các thao tác ghi dữ liệu thành công hoặc hoàn tác cùng nhau.
     * Trả về thông tin phiên mới cho Frontend.
     */
    public StartJourneyResponse startJourney(StartJourneyRequest request) {
        StartPoint start = findStartPointForCode(request.code());
        int qrId = db.queryForObject(
                "SELECT QrId FROM dbo.QRCodes WHERE QrCodeValue=?", Integer.class, request.code());
        UUID session = UUID.randomUUID();
        db.update("""
            INSERT dbo.VisitorSessions(SessionId,StartQrId,ExpiresAt,IsDemo)
            VALUES (CONVERT(uniqueidentifier,?),?,DATEADD(HOUR,12,SYSUTCDATETIME()),?)
            """, session.toString(), qrId, request.demo());
        db.update("INSERT dbo.QRScanLogs(QrId) VALUES (?)", qrId);
        return new StartJourneyResponse(session,start.name(),Instant.now().toString());
    }

    @Transactional
    /**
     * Nhận tín hiệu heartbeat để duy trì phiên tham quan đang hoạt động.
     * Kiểm tra phiên còn hợp lệ và xác nhận SiteId nếu Frontend báo địa điểm.
     * Nếu địa điểm thay đổi: đóng lượt ghé cũ và tạo lượt ghé mới trong SiteVisits.
     * Luôn cập nhật LastSeenAt và CurrentSiteId của phiên hợp lệ.
     * Không nhận tọa độ GPS trực tiếp; chỉ xử lý SiteId do Frontend gửi.
     */
    public HeartbeatResponse heartbeat(UUID sessionId, HeartbeatRequest request) {
        // UPDLOCK serializes concurrent heartbeats for the same session.
        List<Integer> current = db.query("""
            SELECT CurrentSiteId FROM dbo.VisitorSessions WITH (UPDLOCK, ROWLOCK)
            WHERE SessionId=CONVERT(uniqueidentifier,?) AND EndedAt IS NULL
              AND ExpiresAt > SYSUTCDATETIME()
            """, (rs, row) -> (Integer)rs.getObject("CurrentSiteId"), sessionId.toString());
        if (current.isEmpty()) throw new ResponseStatusException(HttpStatus.GONE,
                "Journey ended or expired. Scan QR to start a new journey.");
        Integer previous = current.get(0);
        Integer target = request.siteId();
        if (target != null) {
            List<Integer> valid = db.query(
                "SELECT SiteId FROM dbo.TouristSites WHERE SiteId=? AND Status=1",
                (rs,row) -> rs.getInt(1),target);
            if (valid.isEmpty()) throw bad("Unknown or disabled tourist site");
        }
        boolean changed = !Objects.equals(previous,target);
        if (changed) {
            db.update("""
                UPDATE dbo.SiteVisits SET ExitedAt=SYSUTCDATETIME()
                WHERE SessionId=CONVERT(uniqueidentifier,?) AND ExitedAt IS NULL
                """, sessionId.toString());
            if (target != null) {
                db.update("""
                    INSERT dbo.SiteVisits(SessionId,SiteId)
                    VALUES (CONVERT(uniqueidentifier,?),?)
                    """, sessionId.toString(),target);
            }
        }
        db.update("""
            UPDATE dbo.VisitorSessions SET LastSeenAt=SYSUTCDATETIME(), CurrentSiteId=?,
                CurrentSiteChangedAt=CASE WHEN ?=1 THEN SYSUTCDATETIME() ELSE CurrentSiteChangedAt END
            WHERE SessionId=CONVERT(uniqueidentifier,?)
            """,target,changed ? 1:0,sessionId.toString());
        return new HeartbeatResponse(target,changed);
    }

    @Transactional
    /**
     * Kết thúc phiên tham quan bằng cách cập nhật EndedAt trong VisitorSessions.
     * Đồng thời ghi ExitedAt cho lượt ghé chưa kết thúc.
     * Không xóa phiên nên số "Phiên đã bắt đầu" vẫn được giữ để thống kê.
     */
    public void endJourney(UUID sessionId) {
        int count = db.update("""
            UPDATE dbo.VisitorSessions SET EndedAt=SYSUTCDATETIME(), CurrentSiteId=NULL
            WHERE SessionId=CONVERT(uniqueidentifier,?) AND EndedAt IS NULL
            """, sessionId.toString());
        if (count == 0) throw missing("Journey not found or already ended");
        db.update("UPDATE dbo.SiteVisits SET ExitedAt=SYSUTCDATETIME() " +
                "WHERE SessionId=CONVERT(uniqueidentifier,?) AND ExitedAt IS NULL",sessionId.toString());
    }

    /**
     * Tính toán số liệu hiển thị trên Dashboard quản trị:
     * tổng phiên đã bắt đầu, phiên còn hoạt động trong 45 giây gần nhất,
     * phiên demo đang hoạt động, tổng lượt ghé và thống kê theo từng địa điểm.
     * Dữ liệu được tổng hợp trực tiếp từ VisitorSessions và SiteVisits.
     */
    public AdminStats adminStats() {
        long started = db.queryForObject("SELECT COUNT_BIG(*) FROM dbo.VisitorSessions", Long.class);
        long active = db.queryForObject("""
            SELECT COUNT_BIG(*) FROM dbo.VisitorSessions
            WHERE EndedAt IS NULL AND ExpiresAt > SYSUTCDATETIME()
              AND LastSeenAt >= DATEADD(SECOND,-45,SYSUTCDATETIME())
            """, Long.class);
        long demoActive = db.queryForObject("""
            SELECT COUNT_BIG(*) FROM dbo.VisitorSessions
            WHERE EndedAt IS NULL AND ExpiresAt > SYSUTCDATETIME() AND IsDemo=1
              AND LastSeenAt >= DATEADD(SECOND,-45,SYSUTCDATETIME())
            """, Long.class);
        long visits = db.queryForObject("SELECT COUNT_BIG(*) FROM dbo.SiteVisits", Long.class);
        List<SiteStats> siteStats = db.query("""
            SELECT s.SiteId,s.Name,s.Latitude,s.Longitude,
              (SELECT COUNT_BIG(*) FROM dbo.VisitorSessions v
                WHERE v.CurrentSiteId=s.SiteId AND v.EndedAt IS NULL
                  AND v.ExpiresAt>SYSUTCDATETIME()
                  AND v.LastSeenAt>=DATEADD(SECOND,-45,SYSUTCDATETIME())) AS ActiveSessions,
              (SELECT COUNT_BIG(*) FROM dbo.SiteVisits sv WHERE sv.SiteId=s.SiteId) AS TotalVisits
            FROM dbo.TouristSites s WHERE s.Status=1 ORDER BY s.Name
            """, (rs,row) -> new SiteStats(rs.getInt("SiteId"),rs.getString("Name"),
                rs.getDouble("Latitude"),rs.getDouble("Longitude"),
                rs.getLong("ActiveSessions"),rs.getLong("TotalVisits")));
        return new AdminStats(started,active,visits,demoActive,siteStats);
    }

    /**
     * Lấy danh sách danh mục địa điểm từ Categories để hiển thị trên giao diện.
     */
    public List<Map<String,Object>> categories() {
        return db.queryForList("SELECT CategoryId AS categoryId,Name AS name FROM dbo.Categories ORDER BY Name");
    }

    @Transactional
    /**
     * Thêm mới hoặc cập nhật thông tin địa điểm du lịch.
     * id == null: INSERT địa điểm mới vào TouristSites.
     * id != null: UPDATE địa điểm đã tồn tại.
     * Kiểm tra CategoryId hợp lệ trước khi lưu và trả lại thông tin địa điểm.
     */
    public Site saveSite(Integer id, SiteEditRequest input) {
        int exists = db.queryForObject("SELECT COUNT(*) FROM dbo.Categories WHERE CategoryId=?", Integer.class, input.categoryId());
        if (exists==0) throw bad("Category does not exist");
        int siteId;
        if (id==null) {
            siteId = db.queryForObject("""
                INSERT dbo.TouristSites(CategoryId,Name,Description,Address,ImageUrl,Latitude,Longitude,GeofenceRadiusMeters,Status)
                OUTPUT INSERTED.SiteId VALUES (?,?,?,?,?,?,?,?,?)
                """,Integer.class,input.categoryId(),input.name(),input.description(),input.address(),
                input.imageUrl(),input.latitude(),input.longitude(),input.radiusMeters(),input.active());
        } else {
            int updated = db.update("""
                UPDATE dbo.TouristSites SET CategoryId=?,Name=?,Description=?,Address=?,ImageUrl=?,
                  Latitude=?,Longitude=?,GeofenceRadiusMeters=?,Status=? WHERE SiteId=?
                """,input.categoryId(),input.name(),input.description(),input.address(),input.imageUrl(),
                input.latitude(),input.longitude(),input.radiusMeters(),input.active(),id);
            if (updated==0) throw missing("Site not found");
            siteId=id;
        }
        return getSite(siteId,true);
    }

    @Transactional
    /**
     * Tắt địa điểm theo SiteId bằng cách cập nhật Status = 0 (soft delete).
     * Không xóa bản ghi để giữ thông tin và các dữ liệu liên quan.
     */
    public void disableSite(int id) {
        if (db.update("UPDATE dbo.TouristSites SET Status=0 WHERE SiteId=?",id)==0) throw missing("Site not found");
    }

    @Transactional
    /**
     * Tạo điểm bắt đầu hành trình trong StartPoints và sinh mã QR duy nhất.
     * Lưu mã QR vào QRCodes với khóa ngoại StartPointId liên kết điểm bắt đầu.
     * Trả về điểm bắt đầu cùng chuỗi mã QR mới cho giao diện quản trị.
     */
    public StartPoint createStartPoint(StartPointEditRequest req) {
        int id = db.queryForObject("""
            INSERT dbo.StartPoints(Name,Address,IsActive) OUTPUT INSERTED.StartPointId VALUES(?,?,1)
            """,Integer.class,req.name(),req.address());
        String code="START-"+UUID.randomUUID().toString().substring(0,12).toUpperCase(Locale.ROOT);
        db.update("INSERT dbo.QRCodes(StartPointId,QrCodeValue) VALUES (?,?)",id,code);
        return new StartPoint(id,req.name(),req.address(),code,true);
    }

    @Transactional
    /**
     * Thêm hoặc cập nhật bài thuyết minh cho một địa điểm.
     * Nội dung tiếng Việt lưu trong Narrations; bản tiếng Anh và Nhật
     * được thêm/cập nhật qua hàm upsertTranslation().
     */
    public void saveNarration(int siteId, NarrationEditRequest req) {
        getSite(siteId,true);
        List<Integer> found = db.query("SELECT TOP 1 NarrationId FROM dbo.Narrations WHERE SiteId=? ORDER BY NarrationId",
                (rs,row)->rs.getInt(1),siteId);
        int narrationId;
        if (found.isEmpty()) {
            narrationId=db.queryForObject("""
                INSERT dbo.Narrations(SiteId,Title,OriginalContent,IsActive)
                OUTPUT INSERTED.NarrationId VALUES(?,?,?,1)
                """,Integer.class,siteId,req.title(),req.vi());
        } else {
            narrationId=found.get(0);
            db.update("UPDATE dbo.Narrations SET Title=?,OriginalContent=?,IsActive=1 WHERE NarrationId=?",
                    req.title(),req.vi(),narrationId);
        }
        upsertTranslation(narrationId,"en",req.en());
        upsertTranslation(narrationId,"ja",req.ja());
    }

    /**
     * Thêm mới hoặc cập nhật nội dung bản dịch của một bài thuyết minh.
     * Nếu bản dịch theo NarrationId và ngôn ngữ đã có thì UPDATE; chưa có thì INSERT.
     * Bỏ qua nội dung rỗng và báo lỗi khi ngôn ngữ chưa tồn tại trong Languages.
     */
    private void upsertTranslation(int narrationId, String code, String content) {
        if (content==null || content.isBlank()) return;
        List<Integer> language = db.query("SELECT LanguageId FROM dbo.Languages WHERE Code=?",
                (rs,row)->rs.getInt(1),code);
        if (language.isEmpty()) throw bad("Missing language: "+code+". Run database/02_Seed.sql");
        int languageId=language.get(0);
        int updated=db.update("UPDATE dbo.Translations SET Content=? WHERE NarrationId=? AND LanguageId=?",
                content,narrationId,languageId);
        if (updated==0) db.update("INSERT dbo.Translations(NarrationId,LanguageId,Content) VALUES (?,?,?)",
                narrationId,languageId,content);
    }
}
