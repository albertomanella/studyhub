// Percorso relativo: in sviluppo le richieste passano dal proxy di Vite
// (vite.config.js), quindi il cookie di sessione viaggia automaticamente.
const BASE = '/api'

// Chiamata quando il server risponde 401 (sessione assente o scaduta):
// AuthContext la usa per riportare l'utente al login.
let onUnauthorized = () => {}
export function setOnUnauthorized(fn) {
  onUnauthorized = fn
}

// Token CSRF: il backend lo invia nel cookie XSRF-TOKEN e si aspetta di
// ritrovarlo nell'header X-XSRF-TOKEN di ogni richiesta che modifica dati.
// Un altro sito non può leggere questo cookie, quindi non può falsificare
// l'header.
function csrfToken() {
  const match = document.cookie.match(/(?:^|;\s*)XSRF-TOKEN=([^;]*)/)
  return match ? decodeURIComponent(match[1]) : null
}

const MESSAGGI_ERRORE = {
  401: 'Sessione scaduta: effettua di nuovo il login.',
  403: 'Operazione non consentita.',
  404: 'Risorsa non trovata.',
  413: 'File troppo grande.'
}

// Restituisce il corpo JSON della risposta. In caso di errore restituisce
// { errore: '...' }, con il messaggio del backend se presente.
async function request(path, options = {}) {
  const method = (options.method || 'GET').toUpperCase()
  const headers = { ...options.headers }
  if (options.body !== undefined && !(options.body instanceof FormData)) {
    headers['Content-Type'] = 'application/json'
  }
  if (method !== 'GET') {
    const token = csrfToken()
    if (token) headers['X-XSRF-TOKEN'] = token
  }

  let res
  try {
    res = await fetch(`${BASE}${path}`, { ...options, method, headers })
  } catch {
    throw new Error('Backend non raggiungibile. Assicurati che il server sia avviato.')
  }

  if (res.status === 401) onUnauthorized()

  const isJson = (res.headers.get('content-type') || '').includes('application/json')
  const data = isJson ? await res.json() : null
  if (res.ok) return data ?? {}
  return { errore: data?.errore || MESSAGGI_ERRORE[res.status] || `Errore del server (${res.status}).` }
}

export const login = (username, password) =>
  request('/auth/login', { method: 'POST', body: JSON.stringify({ username, password }) })

export const logout = () => request('/auth/logout', { method: 'POST' })

export const getMe = () => request('/auth/me')

export const register = (data) =>
  request('/auth/register', { method: 'POST', body: JSON.stringify(data) })

export const getEsami = () => request('/esami')

export const getEtichette = () => request('/etichette')

export const getContenuti = (esameId) => request(`/contenuti?esame_id=${esameId}`)

export const getContenuto = (id) => request(`/contenuti/${id}`)

// Il download è un normale link: il browser invia da solo il cookie di sessione
export const fileUrl = (id) => `${BASE}/contenuti/${id}/file`

export const creaDocumento = (formData) =>
  request('/contenuti/documento', { method: 'POST', body: formData })

export const creaConsiglio = (data) =>
  request('/contenuti/consiglio', { method: 'POST', body: JSON.stringify(data) })

export const eliminaContenuto = (id) =>
  request(`/contenuti/${id}`, { method: 'DELETE' })

export const valuta = (contenutoId, stelle) =>
  request('/valutazioni', { method: 'POST', body: JSON.stringify({ contenutoId, stelle }) })

export const segnala = (idContenuto, motivazione) =>
  request('/segnalazioni', { method: 'POST', body: JSON.stringify({ idContenuto, motivazione }) })

export const getSegnalazioni = () => request('/moderatore/segnalazioni')

export const gestisciSegnalazione = (id, esito) =>
  request(`/moderatore/segnalazioni/${id}`, { method: 'PATCH', body: JSON.stringify({ esito }) })
