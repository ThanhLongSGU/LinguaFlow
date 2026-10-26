import { useEffect, useRef, useState } from 'react'
import { QRCodeSVG } from 'qrcode.react'
import MapPanel from './MapPanel.jsx'
import { api } from './api.js'
import { identifySite } from './geo.js'

const SITE_EMPTY = { name:'', categoryId:1, address:'',description:'',imageUrl:'',
  latitude:10.7798, longitude:106.6990, radiusMeters:120, active:true }
const QR_DEMO = 'HOTEL-CENTER-001'
const languages = [{code:'vi',label:'Tiếng Việt'},{code:'en',label:'English'},{code:'ja',label:'日本語'}]

function Topbar({ admin = false }) {
  return <header className="topbar"><a className="brand" href="/"><span className="brand-mark">✦</span>
    Lingua<span>Flow</span><small>V2</small></a>
    <nav><a href="/">Trang chủ</a><a href="/start/HOTEL-CENTER-001">Khách tham quan</a>
      {!admin && <a href="/admin">Quản trị</a>}</nav></header>
}

function LandingPage() {
  return <><Topbar /><main className="wrap landing">
    <div className="hero-copy"><span className="eyebrow">NỀN TẢNG THUYẾT MINH DU LỊCH THÔNG MINH</span>
      <h1>Một lần quét QR.<br /><em>Cả hành trình khám phá.</em></h1>
      <p>LinguaFlow tự nhận diện địa điểm gần bạn bằng GPS, hiển thị thông tin và để bạn chủ động chọn nghe thuyết minh theo ngôn ngữ yêu thích.</p>
      <div className="actions"><a className="button primary" href={`/start/${QR_DEMO}`}>Bắt đầu hành trình →</a>
        <a className="button secondary" href={`/start/${QR_DEMO}?demo=1`}>Thử mô phỏng tại nhà</a></div>
      <p className="fine">Bản mẫu học tập. Chỉ bật GPS khi bạn đồng ý; dữ liệu vị trí chính xác không được gửi lên máy chủ.</p>
    </div>
    <div className="hero-panel"><div className="hero-card"><span className="pill">✧ Trải nghiệm khám phá</span>
      <h2>Khám phá Sài Gòn</h2><p>🗺️ Bản đồ địa điểm</p><p>📍 Tự nhận diện vị trí</p>
      <p>🎧 Thuyết minh đa ngôn ngữ</p><p>📊 Thống kê phiên gần thời gian thực</p>
      <div className="hero-big-icon">◎</div></div></div>
  </main><footer className="footer">LinguaFlow V2 · Java Spring Boot · React · SQL Server · OpenStreetMap</footer></>
}

function VisitorPage({ code }) {
  const demo = new URLSearchParams(window.location.search).get('demo') === '1'
  const storageKey = `linguaflow-v2-session-${code}-${demo?'demo':'real'}`
  const [startPoint,setStartPoint] = useState(null)
  const [sites,setSites] = useState([])
  const [session,setSession] = useState(() => sessionStorage.getItem(storageKey) || '')
  const [position,setPosition] = useState(null)
  const [currentSite,setCurrentSite] = useState(null)
  const [siteDetails,setSiteDetails] = useState(null)
  const [gpsState,setGpsState] = useState('Chưa bật GPS')
  const [status,setStatus] = useState('')
  const [lang,setLang] = useState('vi')
  const [running,setRunning] = useState(false)
  const [speaking,setSpeaking] = useState(false)
  const [follow,setFollow] = useState(true)
  const watcherRef = useRef(null)
  const currentRef = useRef(null)
  const candidateRef = useRef(null)
  const sitesRef = useRef([])
  const sessionRef = useRef(session)
  const audioRef = useRef(null)
  const latestFixRef = useRef(null)
  const beatRef = useRef(null)

  useEffect(() => { sessionRef.current=session },[session])
  useEffect(() => { sitesRef.current=sites },[sites])
  useEffect(() => {
    let alive=true
    Promise.all([api(`/public/start/${encodeURIComponent(code)}`),api('/public/sites')])
      .then(([start, list]) => { if(alive){setStartPoint(start);setSites(list)} })
      .catch(err => { if(alive)setStatus(err.message) })
    return () => {alive=false}
  },[code])

  async function sendHeartbeat(site = currentRef.current) {
    if (!sessionRef.current || document.visibilityState !== 'visible') return
    try {
      // If GPS sample is too old, no site is considered currently present.
      const gpsExpired = !demo && (!latestFixRef.current || Date.now()-latestFixRef.current > 65000)
      if (gpsExpired && currentRef.current) {
        currentRef.current=null; candidateRef.current=null
        setCurrentSite(null); window.speechSynthesis?.cancel(); setSpeaking(false)
      }
      await api('/public/journeys/heartbeat',{
        method:'POST',session:sessionRef.current,
        data:{siteId: gpsExpired ? null : (site?.siteId ?? null)}
      })
    } catch (err) {
      if (/expired|ended|gone|HTTP 410/i.test(err.message)) {
        sessionStorage.removeItem(storageKey)
        setSession(''); sessionRef.current=''; setStatus('Phiên đã hết hạn, vui lòng bắt đầu lại.')
      } else setStatus(`Không kết nối được backend: ${err.message}`)
    }
  }
  beatRef.current=sendHeartbeat

  useEffect(() => {
    if (!session) return
    const beat=()=>beatRef.current?.()
    beat()
    const interval=window.setInterval(beat,15000)
    const onVisible=()=>{if(document.visibilityState==='visible') beat()}
    document.addEventListener('visibilitychange',onVisible)
    return ()=>{clearInterval(interval);document.removeEventListener('visibilitychange',onVisible)}
  },[session])

  useEffect(() => {
    if (!currentSite) {setSiteDetails(null);return}
    let alive=true
    api(`/public/sites/${currentSite.siteId}`).then(x=>{if(alive)setSiteDetails(x)})
      .catch(err=>{if(alive)setStatus(err.message)})
    return ()=>{alive=false}
  },[currentSite?.siteId])

  function updateSite(site) {
    if (currentRef.current?.siteId === site?.siteId) return
    stopAudio()
    currentRef.current=site
    setCurrentSite(site)
    if (sessionRef.current) beatRef.current?.(site)
  }

  function startGPS() {
    if (!navigator.geolocation) {setGpsState('Trình duyệt không hỗ trợ GPS');return}
    if (watcherRef.current !== null) return
    setGpsState('Đang yêu cầu quyền vị trí...')
    watcherRef.current=navigator.geolocation.watchPosition(
      result=>{
        const p={latitude:result.coords.latitude,longitude:result.coords.longitude,
          accuracy:result.coords.accuracy}
        setPosition(p); latestFixRef.current=Date.now()
        if (p.accuracy>150) {setGpsState(`GPS chưa đủ chính xác (±${Math.round(p.accuracy)}m)`);return}
        setGpsState(`Đang cập nhật · độ chính xác ±${Math.round(p.accuracy)}m`)
        if (demo) return
        const next=identifySite(p,sitesRef.current,currentRef.current,candidateRef.current)
        candidateRef.current=next.candidate
        updateSite(next.site)
      }, err=>{
        setGpsState(err.code===1?'Bạn đã từ chối quyền GPS':
          err.code===2?'Không lấy được vị trí GPS':'GPS phản hồi quá lâu')
        stopGPS()
      }, {enableHighAccuracy:true,maximumAge:5000,timeout:18000}
    )
  }
  function stopGPS() {
    if(watcherRef.current !== null){navigator.geolocation.clearWatch(watcherRef.current);watcherRef.current=null}
    setRunning(false)
  }
  function stopAudio() {
    window.speechSynthesis?.cancel()
    if(audioRef.current){audioRef.current.pause();audioRef.current=null}
    setSpeaking(false)
  }
  useEffect(() => () => {
    if(watcherRef.current !== null) navigator.geolocation.clearWatch(watcherRef.current)
    window.speechSynthesis?.cancel()
    if(audioRef.current)audioRef.current.pause()
  },[])

  async function begin() {
    setRunning(true); setStatus('')
    try {
      const result=await api('/public/journeys/start',{method:'POST',data:{code,demo}})
      sessionStorage.setItem(storageKey,result.sessionId)
      sessionRef.current=result.sessionId;setSession(result.sessionId)
      setRunning(false)
    }catch(err){setRunning(false);setStatus(err.message)}
  }
  async function end() {
    try{await api('/public/journeys/end',{method:'POST',session:sessionRef.current})}
    catch(err){setStatus(`Đã đóng trang nhưng server phản hồi: ${err.message}`)}
    stopGPS();stopAudio();sessionStorage.removeItem(storageKey);setSession('');sessionRef.current=''
    updateSite(null);setPosition(null);latestFixRef.current=null
    setGpsState('Đã kết thúc hành trình')
  }
  function chooseDemo(site) {
    if(!demo) return
    latestFixRef.current=Date.now()
    candidateRef.current=null
    updateSite(site)
  }
  async function speak() {
    if(speaking){stopAudio();return}
    const narration=siteDetails?.narrations?.find(n=>n.language===lang) || siteDetails?.narrations?.[0]
    if(!narration){setStatus('Địa điểm chưa có bài thuyết minh.');return}
    setStatus('')
    if(narration.audioUrl){
      try{
        const url=new URL(narration.audioUrl,window.location.origin)
        if(url.protocol!=='https:' && url.protocol!=='http:') throw new Error('Invalid audio URL')
        const a=new Audio(url.href);audioRef.current=a;a.onended=()=>setSpeaking(false)
        await a.play();setSpeaking(true)
      }catch(err){setStatus(`Không phát được file audio: ${err.message}`)}
      return
    }
    if(!('speechSynthesis' in window)) {setStatus('Trình duyệt không hỗ trợ giọng đọc.');return}
    const utt=new SpeechSynthesisUtterance(narration.content)
    utt.lang=lang==='ja'?'ja-JP':lang==='en'?'en-US':'vi-VN';utt.rate=0.95
    utt.onend=()=>setSpeaking(false);utt.onerror=()=>setSpeaking(false)
    window.speechSynthesis.cancel();window.speechSynthesis.speak(utt);setSpeaking(true)
  }
  const narration=siteDetails?.narrations?.find(n=>n.language===lang) || siteDetails?.narrations?.[0]

  return <><Topbar/><main className="wrap visitor">
    <div className="page-heading"><div><span className="eyebrow">HÀNH TRÌNH THAM QUAN</span>
      <h1>Khám phá thành phố</h1><p>Điểm bắt đầu: <strong>{startPoint?.name || code}</strong></p></div>
      {session && <span className="tag green">● Phiên đã bắt đầu</span>}
    </div>
    {status && <div className="alert" role="alert">{status}</div>}
    {!session ? <div className="card center bigpad"><div className="round-icon">◈</div>
      <h2>Chỉ cần bắt đầu một lần</h2><p>Nhấn bắt đầu để tạo phiên tham quan. GPS chỉ được bật khi bạn nhấn cho phép.</p>
      {demo && <p className="tag amber">CHẾ ĐỘ MÔ PHỎNG — không dùng vị trí thật để xác nhận điểm đến</p>}
      <button className="button primary" onClick={begin} disabled={!startPoint || running}>{running?'Đang khởi tạo...':'Bắt đầu hành trình →'}</button></div>
    : <>
      <div className="visitor-grid">
        <div className="card map-card"><div className="card-header"><div><h2>Bản đồ khám phá</h2><span className="muted">{gpsState}</span></div>
          <label className="toggle"><input type="checkbox" checked={follow} onChange={e=>setFollow(e.target.checked)}/> Theo vị trí</label></div>
          <MapPanel sites={sites} position={position} currentSiteId={currentSite?.siteId} follow={follow} height={460}/>
          <div className="map-actions">
            <button className="button primary" onClick={startGPS} disabled={watcherRef.current!==null}>📍 Bật GPS</button>
            <button className="button secondary" onClick={()=>{stopGPS();setGpsState('Đã tạm dừng GPS');latestFixRef.current=null;updateSite(null); beatRef.current?.(null)}}>Tạm dừng GPS</button>
            <button className="button danger" onClick={end}>Kết thúc hành trình</button>
          </div><p className="fine">GPS chỉ hoạt động khi bạn cho phép và trình duyệt còn hoạt động. Không gửi tọa độ chính xác lên máy chủ.</p></div>
        <div className="sidebar"><div className="card"><span className="eyebrow">ĐỊA ĐIỂM HIỆN TẠI</span>
          {currentSite ? <><h2>{currentSite.name}</h2><p className="muted">{currentSite.address}</p>
            <p>{currentSite.description}</p><label className="field">Ngôn ngữ
              <select value={lang} onChange={e=>{stopAudio();setLang(e.target.value)}}>
                {languages.map(l=><option key={l.code} value={l.code}>{l.label}</option>)}
              </select></label>
            <div className="narration"><h3>{narration?.title || 'Chưa có nội dung'}</h3><p>{narration?.content || 'Chưa có bản dịch cho ngôn ngữ này.'}</p></div>
            <button className="button primary full" onClick={speak} disabled={!narration}>
              {speaking?'■ Dừng phát':'▶ Nghe thuyết minh'}</button>
            {!narration?.audioUrl && <p className="fine">Bản demo sử dụng giọng đọc của trình duyệt, tùy khả năng thiết bị.</p>}
          </> : <div className="empty-state"><div className="round-icon">◎</div>
            <h3>Chưa nhận diện địa điểm</h3><p>Bật GPS và đến gần một ghim trên bản đồ. Nếu đang ở nhà, bạn có thể dùng chế độ mô phỏng.</p></div>}</div>
          {demo && <div className="card"><span className="tag amber">MÔ PHỎNG</span><h3>Đặt điểm đến thử nghiệm</h3><p className="muted">Chỉ sử dụng cho bài demo, không chứng minh bạn đang ở địa điểm này.</p>
            <div className="site-buttons">{sites.map(s=><button className="site-button" key={s.siteId} onClick={()=>chooseDemo(s)}>{s.name} →</button>)}
              <button className="site-button" onClick={()=>chooseDemo(null)}>Rời khỏi tất cả địa điểm</button></div></div>}
        </div>
      </div>
      <div className="card"><h2>Địa điểm trong hành trình</h2><div className="site-cards">
        {sites.map(s=><div className="mini-site" key={s.siteId}><div className="mini-icon">⌖</div>
          <strong>{s.name}</strong><p>{s.address}</p>
          <span className="fine">Vùng nhận diện: {s.radiusMeters} m</span></div>)}</div></div>
    </>}
  </main></>
}

function AdminPage() {
  const [credentials,setCredentials] = useState(null)
  const [formLogin,setFormLogin] = useState({user:'admin',password:''})
  const [stats,setStats] = useState(null)
  const [sites,setSites] = useState([])
  const [categories,setCategories] = useState([])
  const [starts,setStarts] = useState([])
  const [error,setError] = useState('')
  const [notice,setNotice] = useState('')
  const [siteForm,setSiteForm] = useState({...SITE_EMPTY})
  const [editingId,setEditingId] = useState(null)
  const [startForm,setStartForm] = useState({name:'',address:''})
  const [narrationSite,setNarrationSite] = useState(null)
  const [narrationForm,setNarrationForm] = useState({title:'',vi:'',en:'',ja:''})
  const [tab,setTab] = useState('overview')
  const credsRef=useRef(credentials)
  useEffect(()=>{credsRef.current=credentials},[credentials])
  async function refresh(auth=credsRef.current) {
    const [a,b,c,d]=await Promise.all([
      api('/admin/stats',{admin:auth}),api('/admin/sites',{admin:auth}),
      api('/admin/categories',{admin:auth}),api('/admin/start-points',{admin:auth})
    ])
    setStats(a);setSites(b);setCategories(c);setStarts(d)
  }
  async function login(event) {
    event.preventDefault();setError('')
    try{await refresh(formLogin);setCredentials({...formLogin});setFormLogin(x=>({...x,password:''}))}
    catch(err){setError(`Đăng nhập thất bại: ${err.message}`)}
  }
  useEffect(()=>{
    if(!credentials)return
    const timer=setInterval(()=>refresh(credsRef.current).catch(e=>setError(e.message)),5000)
    return ()=>clearInterval(timer)
  },[credentials])
  async function mutate(run,success) {
    setError('');setNotice('')
    try{await run();await refresh();setNotice(success)}catch(err){setError(err.message)}
  }
  function editSite(site) {
    setEditingId(site.siteId);setSiteForm({...site});setTab('sites');window.scrollTo({top:0,behavior:'smooth'})
  }
  async function saveSite(e) {
    e.preventDefault()
    await mutate(()=>api(editingId?`/admin/sites/${editingId}`:'/admin/sites',{
      method:editingId?'PUT':'POST',admin:credentials,
      data:{...siteForm,categoryId:Number(siteForm.categoryId),latitude:Number(siteForm.latitude),
        longitude:Number(siteForm.longitude),radiusMeters:Number(siteForm.radiusMeters)}
    }),editingId?'Đã cập nhật địa điểm':'Đã tạo địa điểm')
    setEditingId(null);setSiteForm({...SITE_EMPTY})
  }
  async function openNarration(site) {
    setNarrationSite(site);setTab('narration');setError('')
    try{
      const res=await api(`/public/sites/${site.siteId}`)
      const list=res.narrations || []
      setNarrationForm({title:list[0]?.title || `Giới thiệu ${site.name}`,
        vi:list.find(x=>x.language==='vi')?.content || '',
        en:list.find(x=>x.language==='en')?.content || '',
        ja:list.find(x=>x.language==='ja')?.content || ''})
    } catch {setNarrationForm({title:`Giới thiệu ${site.name}`,vi:'',en:'',ja:''})}
  }
  if(!credentials)return <><Topbar admin/><main className="login-wrap"><form className="card login" onSubmit={login}>
    <span className="eyebrow">KHU VỰC QUẢN TRỊ</span><h1>Đăng nhập LinguaFlow</h1>
    <p className="muted">Dashboard tổng hợp, quản lý địa điểm và QR khởi hành.</p>
    {error&&<div className="alert">{error}</div>}
    <label className="field">Tài khoản<input value={formLogin.user} autoComplete="username" onChange={e=>setFormLogin({...formLogin,user:e.target.value})}/></label>
    <label className="field">Mật khẩu<input type="password" value={formLogin.password} autoComplete="current-password" onChange={e=>setFormLogin({...formLogin,password:e.target.value})}/></label>
    <button className="button primary full" type="submit">Đăng nhập →</button>
    <p className="fine">Tài khoản demo đặt bằng biến ADMIN_USER / ADMIN_PASSWORD tại backend. Khi truy cập từ xa phải sử dụng HTTPS.</p>
  </form></main></>

  return <><Topbar admin/><main className="wrap admin"><div className="page-heading"><div>
    <span className="eyebrow">LINGUAFLOW · QUẢN TRỊ</span><h1>Trung tâm điều hành</h1>
    <p>Thống kê theo phiên và địa điểm, không hiển thị vị trí riêng của du khách.</p></div>
    <button className="button secondary" onClick={()=>{setCredentials(null);setStats(null)}}>Đăng xuất</button></div>
    {error&&<div className="alert" role="alert">{error}</div>}
    {notice&&<div className="notice">{notice}</div>}
    <div className="tabs">{[
      ['overview','Tổng quan'],['sites','Địa điểm GPS'],['starts','Điểm khởi hành & QR'],['narration','Thuyết minh']
    ].map(([id,label])=><button key={id} className={tab===id?'tab active':'tab'} onClick={()=>setTab(id)}>{label}</button>)}</div>
    {tab==='overview'&&<>
      <div className="metric-grid"><Metric title="Phiên đã bắt đầu" number={stats?.totalStarted??'—'} icon="◈"/>
        <Metric title="Đang hoạt động · 45 giây" number={stats?.activeSessions??'—'} icon="◎"/>
        <Metric title="Tổng lượt ghé địa điểm" number={stats?.totalVisits??'—'} icon="⌖"/>
        <Metric title="Trong đó: phiên demo đang hoạt động" number={stats?.demoActiveSessions??'—'} icon="⚑"/></div>
      <div className="card"><div className="card-header"><div><h2>Bản đồ phiên hoạt động theo địa điểm</h2>
        <p className="fine">Tự động cập nhật mỗi 5 giây. Marker là tọa độ địa điểm cố định, không phải tọa độ khách.</p></div>
        <button className="button secondary small" onClick={()=>refresh().catch(e=>setError(e.message))}>Làm mới</button></div>
        <MapPanel sites={(stats?.sites||[]).map(s=>({...s,radiusMeters:100}))}
          counts={Object.fromEntries((stats?.sites||[]).map(s=>[s.siteId,s.activeSessions]))} height={400}/></div>
      <div className="card"><h2>Thống kê từng địa điểm</h2><div className="table-wrap"><table><thead><tr>
        <th>Địa điểm</th><th>Phiên đang hoạt động</th><th>Lượt ghé</th></tr></thead><tbody>
        {(stats?.sites||[]).map(s=><tr key={s.siteId}><td>{s.name}</td>
          <td><strong>{s.activeSessions}</strong></td><td>{s.totalVisits}</td></tr>)}</tbody></table></div></div>
    </>}
    {tab==='sites'&&<div className="admin-columns"><div className="card"><h2>{editingId?'Sửa địa điểm':'Thêm địa điểm mới'}</h2>
      <form className="stack" onSubmit={saveSite}>
        <label className="field">Tên địa điểm<input required value={siteForm.name} onChange={e=>setSiteForm({...siteForm,name:e.target.value})}/></label>
        <label className="field">Danh mục<select value={siteForm.categoryId} onChange={e=>setSiteForm({...siteForm,categoryId:e.target.value})}>
          {categories.map(c=><option key={c.categoryId} value={c.categoryId}>{c.name}</option>)}</select></label>
        <label className="field">Địa chỉ<input value={siteForm.address || ''} onChange={e=>setSiteForm({...siteForm,address:e.target.value})}/></label>
        <label className="field">Mô tả<textarea rows="3" value={siteForm.description || ''} onChange={e=>setSiteForm({...siteForm,description:e.target.value})}/></label>
        <div className="two-col"><label className="field">Vĩ độ<input type="number" step="any" required value={siteForm.latitude} onChange={e=>setSiteForm({...siteForm,latitude:e.target.value})}/></label>
          <label className="field">Kinh độ<input type="number" step="any" required value={siteForm.longitude} onChange={e=>setSiteForm({...siteForm,longitude:e.target.value})}/></label></div>
        <label className="field">Bán kính nhận diện (m)<input type="number" min="30" max="2000" required value={siteForm.radiusMeters} onChange={e=>setSiteForm({...siteForm,radiusMeters:e.target.value})}/></label>
        <label className="check"><input type="checkbox" checked={siteForm.active} onChange={e=>setSiteForm({...siteForm,active:e.target.checked})}/> Địa điểm hoạt động</label>
        <div className="actions"><button className="button primary" type="submit">{editingId?'Lưu thay đổi':'Tạo địa điểm'}</button>
          {editingId&&<button className="button secondary" type="button" onClick={()=>{setEditingId(null);setSiteForm({...SITE_EMPTY})}}>Hủy</button>}</div>
      </form></div><div className="card"><h2>Danh sách địa điểm ({sites.length})</h2>
      <div className="stack">{sites.map(s=><div className="admin-item" key={s.siteId}>
        <div><strong>{s.name}</strong><p className="fine">{s.latitude}, {s.longitude} · {s.radiusMeters}m
          {s.active?' · Đang bật':' · Đã tắt'}</p></div>
        <div className="actions"><button className="button secondary small" onClick={()=>editSite(s)}>Sửa</button>
          <button className="button secondary small" onClick={()=>openNarration(s)}>Thuyết minh</button>
          {s.active&&<button className="button danger small" onClick={()=>{
            if(window.confirm(`Tắt địa điểm ${s.name}?`))mutate(()=>api(`/admin/sites/${s.siteId}`,{method:'DELETE',admin:credentials}),'Đã tắt địa điểm')
          }}>Tắt</button>}</div></div>)}</div></div></div>}
    {tab==='starts'&&<div className="admin-columns"><div className="card"><h2>Tạo khách sạn / điểm khởi hành</h2>
      <p className="muted">Mỗi điểm bắt đầu được sinh một mã QR riêng. QR này chỉ dùng để mở một hành trình mới.</p>
      <form className="stack" onSubmit={e=>{e.preventDefault();mutate(()=>api('/admin/start-points',{method:'POST',admin:credentials,data:startForm}),'Đã tạo mã QR khởi hành');setStartForm({name:'',address:''})}}>
        <label className="field">Tên địa điểm bắt đầu<input required value={startForm.name} onChange={e=>setStartForm({...startForm,name:e.target.value})}/></label>
        <label className="field">Địa chỉ<input value={startForm.address} onChange={e=>setStartForm({...startForm,address:e.target.value})}/></label>
        <button className="button primary">Tạo điểm khởi hành & QR</button></form></div>
      <div className="card"><h2>QR Code đang có</h2><div className="start-list">
        {starts.map((s,i)=>{const url=`${window.location.origin}/start/${encodeURIComponent(s.code)}`;return <div key={i} className="qr-item">
          <QRCodeSVG value={url} size={130} level="M" includeMargin/>
          <div><strong>{s.name}</strong><p className="fine">{s.address}</p>
            <code>{s.code}</code><p className="break-url">{url}</p>
            <a href={url} target="_blank" rel="noreferrer">Mở trang QR ↗</a>
          </div></div>})}</div>
          <p className="fine">Khi mở trên điện thoại, đường dẫn QR phải dùng hostname HTTPS mà điện thoại truy cập được, không phải localhost.</p></div></div>}
    {tab==='narration'&&<div className="admin-columns"><div className="card"><h2>Chọn địa điểm chỉnh nội dung</h2>
      <div className="site-buttons">{sites.map(s=><button className="site-button" key={s.siteId} onClick={()=>openNarration(s)}>{s.name} →</button>)}</div></div>
      <div className="card"><h2>{narrationSite?`Nội dung: ${narrationSite.name}`:'Chọn một địa điểm'}</h2>
        {narrationSite&&<form className="stack" onSubmit={e=>{e.preventDefault();mutate(()=>api(`/admin/sites/${narrationSite.siteId}/narration`,{method:'PUT',admin:credentials,data:narrationForm}),'Đã lưu thuyết minh')}}>
          <label className="field">Tiêu đề<input required value={narrationForm.title} onChange={e=>setNarrationForm({...narrationForm,title:e.target.value})}/></label>
          {[['vi','Tiếng Việt · gốc'],['en','English'],['ja','日本語']].map(([k,l])=><label className="field" key={k}>{l}
            <textarea required={k==='vi'} rows="4" value={narrationForm[k]} onChange={e=>setNarrationForm({...narrationForm,[k]:e.target.value})}/></label>)}
          <button className="button primary">Lưu nội dung</button>
          <p className="fine">Phát audio demo qua Web Speech API; nếu thêm AudioUrl vào SQL, trang khách sẽ ưu tiên file âm thanh.</p>
        </form>}</div></div>}
  </main></>
}
function Metric({ title,number,icon }) {return <div className="card metric"><span className="metric-icon">{icon}</span>
  <p className="muted">{title}</p><div className="metric-number">{number}</div></div>}
export default function App() {
  const path=window.location.pathname
  if(path==='/admin')return <AdminPage/>
  const match=path.match(/^\/start\/([^/]+)\/?$/)
  if(match)return <VisitorPage code={decodeURIComponent(match[1])}/>
  return <LandingPage/>
}
