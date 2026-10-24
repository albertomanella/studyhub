import { createContext, useContext, useEffect, useState } from 'react'
import { getMe, logout as apiLogout, setOnUnauthorized } from '../services/api'

const AuthContext = createContext(null)

// Chi è l'utente lo decide il backend tramite la sessione: all'avvio il
// frontend lo chiede a /api/auth/me invece di leggerlo dal browser.
export function AuthProvider({ children }) {
  const [utente, setUtente] = useState(null)
  const [caricamento, setCaricamento] = useState(true)
  // Messaggio da mostrare nella pagina di login (es. account sospeso)
  const [avviso, setAvviso] = useState('')

  useEffect(() => {
    // Residui della versione precedente, che salvava username e ruolo nel browser
    localStorage.removeItem('username')
    localStorage.removeItem('ruolo')

    setOnUnauthorized(messaggio => {
      setUtente(null)
      if (messaggio) setAvviso(messaggio)
    })
    getMe()
      .then(res => setUtente(res.errore ? null : res))
      .catch(() => setUtente(null))
      .finally(() => setCaricamento(false))
  }, [])

  async function logout() {
    try {
      await apiLogout()
    } catch {
      // Backend non raggiungibile: la sessione scadrà da sola
    }
    setUtente(null)
  }

  return (
    <AuthContext.Provider value={{ utente, setUtente, logout, caricamento, avviso, setAvviso }}>
      {children}
    </AuthContext.Provider>
  )
}

export function useAuth() {
  return useContext(AuthContext)
}
