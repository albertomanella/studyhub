import { useState } from 'react'
import { useNavigate, Link } from 'react-router-dom'
import { login } from '../services/api'

export default function Login() {
  const navigate = useNavigate()
  const [username, setUsername] = useState('')
  const [password, setPassword] = useState('')
  const [errore, setErrore] = useState('')

  async function handleSubmit(e) {
    e.preventDefault()
    setErrore('')
    try {
      const res = await login(username, password)
      if (res.errore) { setErrore(res.errore); return }
      localStorage.setItem('username', res.username)
      navigate('/home')
    } catch (err) {
      setErrore(err.message)
    }
  }

  return (
    <div style={{ paddingTop: 60, minHeight: '100vh', background: '#fff' }}>
      <form className="form-centered" onSubmit={handleSubmit}>
        <div className="form-title">UniShare</div>
        {errore && <div className="form-error" style={{ textAlign: 'center', marginBottom: 12 }}>{errore}</div>}
        <div className="form-group">
          <label className="form-label">Username</label>
          <input className="form-input" value={username} onChange={e => setUsername(e.target.value)} required autoFocus />
        </div>
        <div className="form-group">
          <label className="form-label">Password</label>
          <input className="form-input" type="password" value={password} onChange={e => setPassword(e.target.value)} required />
        </div>
        <button className="btn-primary btn-block" type="submit">Accedi</button>
        <div className="form-link">
          Non hai un account? <Link to="/register">Registrati</Link>
        </div>
      </form>
    </div>
  )
}
