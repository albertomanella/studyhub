import { useState } from 'react'
import { useNavigate, Link, Navigate } from 'react-router-dom'
import { login } from '../services/api'
import { useAuth } from '../auth/AuthContext'

export default function Login() {
  const navigate = useNavigate()
  const { utente, setUtente, avviso, setAvviso } = useAuth()
  const [username, setUsername] = useState('')
  const [password, setPassword] = useState('')
  const [errore, setErrore] = useState('')

  async function handleSubmit(e) {
    e.preventDefault()
    setErrore('')
    setAvviso('')
    try {
      const res = await login(username, password)
      if (res.errore) { setErrore(res.errore); return }
      setUtente(res)
      navigate('/home')
    } catch (err) {
      setErrore(err.message)
    }
  }

  if (utente) return <Navigate to="/home" replace />

  return (
    <div style={{ background: '#f0f0f0', minHeight: '100vh', paddingTop: 40 }}>
      <form className="form-wrap" onSubmit={handleSubmit}>
        <div className="form-title">StudyHub — Accedi</div>
        {(errore || avviso) && (
          <div className="form-error" style={{ textAlign: 'center', marginBottom: 10 }}>{errore || avviso}</div>
        )}
        <div className="form-group">
          <label className="form-label">Username</label>
          <input className="form-input" value={username} onChange={e => setUsername(e.target.value)} required autoFocus />
        </div>
        <div className="form-group">
          <label className="form-label">Password</label>
          <input className="form-input" type="password" value={password} onChange={e => setPassword(e.target.value)} required />
        </div>
        <button className="btn btn-primary btn-block" type="submit">Accedi</button>
        <div className="form-link">
          Non hai un account? <Link to="/register">Registrati</Link>
        </div>
      </form>
    </div>
  )
}
