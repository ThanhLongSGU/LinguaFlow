package com.linguaflow.linguaflow.config;

import com.linguaflow.linguaflow.entity.Category;
import com.linguaflow.linguaflow.entity.Language;
import com.linguaflow.linguaflow.entity.Narration;
import com.linguaflow.linguaflow.entity.PoiSyncMetadata;
import com.linguaflow.linguaflow.entity.QRCode;
import com.linguaflow.linguaflow.entity.Region;
import com.linguaflow.linguaflow.entity.TouristSite;
import com.linguaflow.linguaflow.entity.Translation;
import com.linguaflow.linguaflow.entity.User;
import com.linguaflow.linguaflow.repository.CategoryRepository;
import com.linguaflow.linguaflow.repository.LanguageRepository;
import com.linguaflow.linguaflow.repository.NarrationRepository;
import com.linguaflow.linguaflow.repository.PoiSyncMetadataRepository;
import com.linguaflow.linguaflow.repository.QRCodeRepository;
import com.linguaflow.linguaflow.repository.RegionRepository;
import com.linguaflow.linguaflow.repository.TouristSiteRepository;
import com.linguaflow.linguaflow.repository.TranslationRepository;
import com.linguaflow.linguaflow.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Sort;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@Order(2)
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final CategoryRepository categoryRepository;
    private final LanguageRepository languageRepository;
    private final NarrationRepository narrationRepository;
    private final PoiSyncMetadataRepository poiSyncMetadataRepository;
    private final QRCodeRepository qrCodeRepository;
    private final RegionRepository regionRepository;
    private final TouristSiteRepository touristSiteRepository;
    private final TranslationRepository translationRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public void run(String... args) {
        User admin = userRepository.findByUsername("admin").orElseThrow(
                () -> new IllegalStateException("Super Admin must be seeded before POI data"));

        if (poiSyncMetadataRepository.findById(1).isEmpty()) {
            poiSyncMetadataRepository.save(PoiSyncMetadata.builder()
                    .metadataId(1)
                    .dataVersion(1L)
                    .build());
        }

        if (touristSiteRepository.count() > 0) {
            ensureSampleQrCode();
            return;
        }

        Category category = categoryRepository.findAll().stream().findFirst()
                .orElseGet(() -> categoryRepository.save(Category.builder()
                        .name("Địa danh lịch sử")
                        .description("Các địa danh lịch sử và văn hóa tại TP. Hồ Chí Minh")
                        .build()));
        Region region = regionRepository.findAll().stream().findFirst()
                .orElseGet(() -> regionRepository.save(Region.builder()
                        .name("Trung tâm TP. Hồ Chí Minh")
                        .description("Khu vực trung tâm Thành phố Hồ Chí Minh")
                        .active(true)
                        .build()));

        Map<String, Language> languages = languageRepository.findAll().stream()
                .collect(Collectors.toMap(Language::getCode, language -> language));
        languages.put("vi-VN", findOrCreateLanguage(languages, "vi-VN", "Tiếng Việt"));
        languages.put("en-US", findOrCreateLanguage(languages, "en-US", "English (United States)"));

        List<SamplePoi> samples = List.of(
                new SamplePoi("Dinh Độc Lập", "Independence Palace",
                        "135 Nam Kỳ Khởi Nghĩa, Quận 1, TP. Hồ Chí Minh",
                        "Dinh Độc Lập là di tích lịch sử đặc biệt, gắn liền với những dấu mốc quan trọng của Việt Nam hiện đại.",
                        "The Independence Palace is a special historical monument associated with important milestones in modern Vietnam.",
                        10.776889, 106.695333),
                new SamplePoi("Nhà thờ Đức Bà Sài Gòn", "Saigon Notre-Dame Cathedral",
                        "Công trường Công xã Paris, Quận 1, TP. Hồ Chí Minh",
                        "Nhà thờ Đức Bà là công trình kiến trúc nổi bật giữa trung tâm Thành phố Hồ Chí Minh.",
                        "Saigon Notre-Dame Cathedral is a landmark architectural work in the heart of Ho Chi Minh City.",
                        10.779783, 106.699018),
                new SamplePoi("Bưu điện Trung tâm Sài Gòn", "Saigon Central Post Office",
                        "Số 2 Công trường Công xã Paris, Quận 1, TP. Hồ Chí Minh",
                        "Bưu điện Trung tâm Sài Gòn nổi bật với kiến trúc cổ điển và là một điểm tham quan quen thuộc.",
                        "Saigon Central Post Office is known for its classic architecture and is a popular visitor attraction.",
                        10.779944, 106.699009),
                new SamplePoi("Bảo tàng Chứng tích Chiến tranh", "War Remnants Museum",
                        "28 Võ Văn Tần, Quận 3, TP. Hồ Chí Minh",
                        "Bảo tàng Chứng tích Chiến tranh lưu giữ nhiều tư liệu về lịch sử và hậu quả của các cuộc chiến tranh.",
                        "The War Remnants Museum preserves historical documents about the impact of wars.",
                        10.779429, 106.692105));

        samples.forEach(sample -> seedPoi(sample, admin, category, region, languages));
        ensureSampleQrCode();
    }

    private void ensureSampleQrCode() {
        if (qrCodeRepository.existsByQrCodeValue("SAMPLE-QR-123")) {
            return;
        }

        TouristSite firstSite = touristSiteRepository.findAll(
                Sort.by(Sort.Direction.ASC, "siteId")).stream().findFirst()
                .orElseThrow(() -> new IllegalStateException("At least one POI is required for sample QR"));

        qrCodeRepository.save(QRCode.builder()
                .touristSite(firstSite)
                .qrCodeValue("SAMPLE-QR-123")
                .active(true)
                .build());
    }

    private Language findOrCreateLanguage(Map<String, Language> languages, String code, String name) {
        Language language = languages.get(code);
        if (language != null) {
            return language;
        }

        return languageRepository.save(Language.builder()
                .code(code)
                .name(name)
                .active(true)
                .build());
    }

    private void seedPoi(
            SamplePoi sample,
            User admin,
            Category category,
            Region region,
            Map<String, Language> languages) {
        TouristSite site = touristSiteRepository.save(TouristSite.builder()
                .name(sample.vietnameseName())
                .category(category)
                .region(region)
                .description(sample.vietnameseDescription())
                .address(sample.address())
                .city("TP. Hồ Chí Minh")
                .latitude(BigDecimal.valueOf(sample.latitude()))
                .longitude(BigDecimal.valueOf(sample.longitude()))
                .triggerRadius(40)
                .status(true)
                .createdBy(admin)
                .build());

        Narration narration = narrationRepository.save(Narration.builder()
                .touristSite(site)
                .title(sample.vietnameseName())
                .originalContent(sample.vietnameseDescription())
                .build());

        translationRepository.saveAll(List.of(
                Translation.builder()
                        .narration(narration)
                        .language(languages.get("vi-VN"))
                        .content(sample.vietnameseDescription())
                        .build(),
                Translation.builder()
                        .narration(narration)
                        .language(languages.get("en-US"))
                        .content(sample.englishDescription())
                        .build()));
    }

    private record SamplePoi(
            String vietnameseName,
            String englishName,
            String address,
            String vietnameseDescription,
            String englishDescription,
            double latitude,
            double longitude) {
    }
}
