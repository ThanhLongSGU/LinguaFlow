import { useEffect, useMemo, useState } from 'react'
import { api } from './api.js'

function thisMonth() {
  const now = new Date()
  // Tên tháng mặc định theo giờ Việt Nam, thống nhất với Backend.
  const pieces = new Intl.DateTimeFormat('en-US', { timeZone: 'Asia/Ho_Chi_Minh', year: 'numeric', month: '2-digit' })
    .formatToParts(now)
  const year = pieces.find(p => p.type === 'year')?.value
  const month = pieces.find(p => p.type === 'month')?.value
  return `${year}-${month}`
}

export default function MonthlyVisitsChart({ admin }) {
  const [month, setMonth] = useState(thisMonth)
  const [data, setData] = useState(null)
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    let alive = true
    async function refresh() {
      try {
        const result = await api(`/admin/visits/monthly?month=${encodeURIComponent(month)}`, { admin })
        if (alive) { setData(result); setError(''); setLoading(false) }
      } catch (err) {
        if (alive) { setError(err.message); setLoading(false) }
      }
    }
    setData(null)
    setError('')
    setLoading(true)
    refresh()
    const timer = window.setInterval(refresh, 30000)
    return () => { alive = false; window.clearInterval(timer) }
  }, [month, admin])

  const max = useMemo(() => Math.max(1, ...(data?.days || []).map(d => d.visits)), [data])
  const monthLabel = month ? `${month.slice(5)}/${month.slice(0, 4)}` : ''

  return <section className="card monthly-visit-card" aria-label="Biểu đồ lượt ghé địa điểm theo ngày trong tháng">
    <div className="monthly-visit-heading">
      <div>
        <h2>Lượt tham quan trong tháng</h2>
        <p className="fine">Thống kê số lượt ghé địa điểm theo từng ngày (giờ Việt Nam).</p>
      </div>
      <label className="monthly-month-picker">Chọn tháng
        <input type="month" value={month} min="2020-01" max="2100-12"
          onChange={e => { if (e.target.value) setMonth(e.target.value) }}/>
      </label>
    </div>
    <div className="monthly-summary" aria-live="polite">
      <span>Tổng lượt ghé trong tháng {monthLabel}</span>
      <strong>{data ? data.totalVisits.toLocaleString('vi-VN') : '—'}</strong>
    </div>
    {loading && <p className="fine" role="status">Đang tải dữ liệu thống kê...</p>}
    {error && <p className="alert" role="alert">Chưa tải được biểu đồ: {error}</p>}
    {data && <>
      <div className="monthly-plot-scroll">
        <div className="monthly-plot" role="img" aria-label={`Số lượt ghé trong tháng ${monthLabel}, tổng ${data.totalVisits} lượt`}>
          {data.days.map(day => (
            <div className="monthly-day" key={day.date}
                 title={`Ngày ${day.date.slice(-2)}: ${day.visits} lượt ghé`}>
              <div className="monthly-bar-space">
                {day.visits > 0 && <span className="monthly-count">{day.visits}</span>}
                <div className="monthly-bar" style={{height: `${day.visits / max * 100}%`}} />
              </div>
              <span className="monthly-day-label">{Number(day.date.slice(-2))}</span>
            </div>
          ))}
        </div>
      </div>
      {data.totalVisits === 0 && <p className="fine">Tháng này chưa có lượt ghé được ghi nhận.</p>}
      <p className="fine">Một lượt ghé là một bản ghi trong <code>SiteVisits</code> (theo <code>EnteredAt</code>), bao gồm cả phiên mô phỏng nếu có. Đây không phải số du khách duy nhất.</p>
    </>}
  </section>
}
