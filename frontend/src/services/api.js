const BASE = 'http://localhost:8081/api'

async function request(path, options = {}) {
  try {
    const res = await fetch(`${BASE}${path}`, {
      headers: { 'Content-Type': 'application/json' },
      ...options
    })
    return await res.json()
  } catch {
    throw new Error('Backend non raggiungibile. Assicurati che il server sia avviato su localhost:8081.')
  }
}

export const login = (username, password) =>
  request('/auth/login', { method: 'POST', body: JSON.stringify({ username, password }) })

export const register = (data) =>
  request('/auth/register', { method: 'POST', body: JSON.stringify(data) })

export const getEsami = () => request('/esami')

export const getContenuti = (esameId, username) => {
  const qs = username ? `&username=${encodeURIComponent(username)}` : ''
  return request(`/contenuti?esame_id=${esameId}${qs}`)
}

export const getContenuto = (id, username) => {
  const qs = username ? `?username=${encodeURIComponent(username)}` : ''
  return request(`/contenuti/${id}${qs}`)
}

export const creaContenuto = (data) =>
  request('/contenuti', { method: 'POST', body: JSON.stringify(data) })

export const valuta = (username, contenutoId, stelle) =>
  request('/valutazioni', { method: 'POST', body: JSON.stringify({ username, contenutoId, stelle }) })

export const segnala = (idContenuto, usernameSegnalante, motivazione) =>
  request('/segnalazioni', { method: 'POST', body: JSON.stringify({ idContenuto, usernameSegnalante, motivazione }) })

export const getSegnalazioni = (username) =>
  request(`/moderatore/segnalazioni?username=${encodeURIComponent(username)}`)
