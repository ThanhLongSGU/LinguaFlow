
import { useEffect, useState } from 'react'
import './App.css'

const API_URL = 'http://localhost:8080/api/tourist-sites'

function App() {
  const [sites, setSites] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')

  useEffect(() => {
    fetch(API_URL)
      .then((response) => {
        if (!response.ok) {
          throw new Error('Không thể tải danh sách địa điểm.')
        }
        return response.json()
      })
      .then((data) => {
        setSites(Array.isArray(data) ? data : [])
        setError('')
      })
      .catch((err) => {
        console.error(err)
        setError('Không thể kết nối đến backend. Em hãy kiểm tra Spring Boot.')
      })
      .finally(() => setLoading(false))
  }, [])

  const formatPrice = (price) => {
    if (price == null) return 'Chưa cập nhật'
    if (Number(price) === 0) return 'Miễn phí'
    return `${Number(price).toLocaleString('vi-VN')} VNĐ`
  }

  return (
    <div className="app">
      <aside className="sidebar">
        <div className="brand">
          <span className="brand-icon">✦</span>
          <div>
            <strong>Saigon Guide</strong>
            <small>TOURISM MANAGEMENT</small>
          </div>
        </div>

        <p className="menu-label">QUẢN LÝ</p>
        <button className="menu-item active">▦ Địa điểm du lịch</button>
        <button className="menu-item" disabled>▧ Danh mục</button>
        <button className="menu-item" disabled>▦ Mã QR</button>

        <div className="sidebar-bottom">
          <span className="status-dot" />
          <span>Hệ thống quản lý du lịch</span>
        </div>
      </aside>

      <main className="main">
        <header className="topbar">
          <div>
            <p className="breadcrumb">Quản lý / Địa điểm</p>
            <h1>Địa điểm du lịch</h1>
          </div>
          <div className="admin">
            <span className="admin-avatar">A</span>
            <div>
              <strong>Quản trị viên</strong>
              <small>Administrator</small>
            </div>
          </div>
        </header>

        <section className="welcome">
          <div>
            <p className="eyebrow">SAIGON TOURISM</p>
            <h2>Khám phá thành phố của bạn.</h2>
            <p>Quản lý thông tin các địa điểm du lịch tại TP. Hồ Chí Minh.</p>
          </div>
          <div className="welcome-icon">🏙️</div>
        </section>

        <section className="stats">
          <article className="stat-card">
            <span className="stat-icon blue">⌖</span>
            <div>
              <p>Tổng địa điểm</p>
              <h3>{loading ? '...' : sites.length}</h3>
            </div>
          </article>
          <article className="stat-card">
            <span className="stat-icon green">✓</span>
            <div>
              <p>Đang hoạt động</p>
              <h3>
                {loading
                  ? '...'
                  : sites.filter((site) => site.isActive).length}
              </h3>
            </div>
          </article>
          <article className="stat-card">
            <span className="stat-icon orange">₫</span>
            <div>
              <p>Địa điểm miễn phí</p>
              <h3>
                {loading
                  ? '...'
                  : sites.filter(
                      (site) => Number(site.referenceTicketPrice) === 0,
                    ).length}
              </h3>
            </div>
          </article>
        </section>

        <section className="content-card">
          <div className="table-heading">
            <div>
              <h2>Danh sách địa điểm</h2>
              <p>Dữ liệu được tải từ hệ thống backend.</p>
            </div>
            <button
              className="refresh-button"
              onClick={() => window.location.reload()}
            >
              ↻ Làm mới
            </button>
          </div>

          {loading && <p className="message">Đang tải dữ liệu...</p>}

          {error && <p className="message error">{error}</p>}

          {!loading && !error && sites.length === 0 && (
            <p className="message">Chưa có địa điểm du lịch nào.</p>
          )}

          {!loading && !error && sites.length > 0 && (
            <div className="table-wrapper">
              <table>
                <thead>
                  <tr>
                    <th>ĐỊA ĐIỂM</th>
                    <th>ĐỊA CHỈ</th>
                    <th>GIÁ VÉ THAM KHẢO</th>
                    <th>TRẠNG THÁI</th>
                  </tr>
                </thead>
                <tbody>
                  {sites.map((site) => (
                    <tr key={site.siteId}>
                      <td>
                        <div className="site-name">
                          <span className="site-avatar">⌖</span>
                          <div>
                            <strong>{site.name}</strong>
                            <small>Mã địa điểm: #{site.siteId}</small>
                          </div>
                        </div>
                      </td>
                      <td>{site.address || 'Chưa cập nhật'}</td>
                      <td>{formatPrice(site.referenceTicketPrice)}</td>
                      <td>
                        <span
                          className={`badge ${
                            site.isActive ? 'enabled' : 'disabled'
                          }`}
                        >
                          {site.isActive ? 'Đang hoạt động' : 'Đã tắt'}
                        </span>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
        </section>

        <footer>
          Hệ thống thuyết minh tự động đa ngôn ngữ · TP. Hồ Chí Minh
        </footer>
      </main>
    </div>
  )
}

export default App
