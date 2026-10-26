export async function api(path, { method = 'GET', data, session, admin } = {}) {
  const headers = {}
  if (data !== undefined) headers['Content-Type'] = 'application/json'
  if (session) headers.Authorization = `Bearer ${session}`
  if (admin) {
    const bytes = new TextEncoder().encode(`${admin.user}:${admin.password}`)
    headers.Authorization = `Basic ${btoa(String.fromCharCode(...bytes))}`
  }
  const response = await fetch(`/api${path}`, {
    method, headers, body: data === undefined ? undefined : JSON.stringify(data)
  })
  if (!response.ok) {
    let detail = ''
    try {
      const json = await response.json()
      detail = json.message || json.detail || json.error || ''
    } catch { /* response may have no JSON */ }
    throw new Error(detail || `HTTP ${response.status}`)
  }
  if (response.status === 204) return null
  return response.json()
}
