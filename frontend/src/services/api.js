const BASE = 'http://localhost:8081/api'

async function request(path, options = {}) {
  try {
    const isFormData = options.body instanceof FormData
    const res = await fetch(`${BASE}${path}`, {
      ...options,
      headers: isFormData ? options.headers : { 'Content-Type': 'application/json', ...options.headers }
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

export const getEtichette = () => request('/etichette')

export const getContenuti = (esameId) => request(`/contenuti?esame_id=${esameId}`)

export const getContenuto = (id) => request(`/contenuti/${id}`)

export const creaDocumento = (formData) =>
  request('/contenuti/documento', { method: 'POST', body: formData })

export const creaConsiglio = (data) =>
  request('/contenuti/consiglio', { method: 'POST', body: JSON.stringify(data) })

export const eliminaContenuto = (id, username) =>
  request(`/contenuti/${id}?username=${encodeURIComponent(username)}`, { method: 'DELETE' })

export const valuta = (username, contenutoId, stelle) =>
  request('/valutazioni', { method: 'POST', body: JSON.stringify({ username, contenutoId, stelle }) })

export const segnala = (idContenuto, usernameSegnalante, motivazione) =>
  request('/segnalazioni', { method: 'POST', body: JSON.stringify({ idContenuto, usernameSegnalante, motivazione }) })

export const getSegnalazioni = (username) =>
  request(`/moderatore/segnalazioni?username=${encodeURIComponent(username)}`)

export const gestisciSegnalazione = (id, usernameModeratore, esito) =>
  request(`/moderatore/segnalazioni/${id}`, { method: 'PATCH', body: JSON.stringify({ usernameModeratore, esito }) })
