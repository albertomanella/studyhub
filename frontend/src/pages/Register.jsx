import { useState } from 'react'
import { useNavigate, Link } from 'react-router-dom'
import { register } from '../services/api'

function validate(f) {
  const e = {}
  if (!f.nome.trim()) e.nome = 'Campo obbligatorio'
  if (!f.cognome.trim()) e.cognome = 'Campo obbligatorio'
  if (!f.username.trim()) e.username = 'Campo obbligatorio'
  if (!f.email.match(/^[^\s@]+@[^\s@]+\.[^\s@]+$/)) e.email = 'Email non valida'
  if (f.password.length < 8) e.password = 'Minimo 8 caratteri'
  else if (!/[!@#$%^&*()_+\-=[\]{};':"\\|,.<>/?]/.test(f.password))
    e.password = 'Deve contenere almeno un carattere speciale'
  return e
}

export default function Register() {
  const navigate = useNavigate()
  const [f, setF] = useState({ nome: '', cognome: '', username: '', email: '', password: '' })
  const [errs, setErrs] = useState({})
  const [apiErr, setApiErr] = useState('')

  const set = k => e => setF(prev => ({ ...prev, [k]: e.target.value }))

  async function handleSubmit(e) {
    e.preventDefault()
    const v = validate(f)
    setErrs(v)
    if (Object.keys(v).length) return
    setApiErr('')
    try {
      const res = await register({
        username: f.username, password: f.password, nome: f.nome, cognome: f.cognome,
        email: f.email, idCorsi: [1] // Ingegneria Informatica @ UniBo, unico corso seedato — form ha ateneo/corso fissi
      })
      if (res.errore) { setApiErr(res.errore); return }
      navigate('/')
    } catch (err) {
      setApiErr(err.message)
    }
  }

  function campo(label, key, type = 'text', disabled = false) {
    return (
      <div className="form-group">
        <label className="form-label">{label}</label>
        <input
          className={`form-input${errs[key] ? ' error' : ''}`}
          type={type}
          value={f[key] ?? ''}
          onChange={set(key)}
          disabled={disabled}
        />
        {errs[key] && <div className="form-error">{errs[key]}</div>}
      </div>
    )
  }

  return (
    <div style={{ background: '#f0f0f0', minHeight: '100vh', paddingTop: 20 }}>
      <form className="form-wrap" onSubmit={handleSubmit}>
        <div className="form-title">StudyHub — Registrazione</div>
        {apiErr && <div className="form-error" style={{ textAlign: 'center', marginBottom: 10 }}>{apiErr}</div>}
        {campo('Nome', 'nome')}
        {campo('Cognome', 'cognome')}
        {campo('Username', 'username')}
        {campo('Email', 'email', 'email')}
        {campo('Password', 'password', 'password')}
        <div className="form-group">
          <label className="form-label">Ateneo</label>
          <input className="form-input" value="Università di Bologna (UniBo)" disabled />
        </div>
        <div className="form-group">
          <label className="form-label">Corso di laurea</label>
          <input className="form-input" value="Ingegneria Informatica" disabled />
        </div>
        <button className="btn btn-primary btn-block" type="submit">Registrati</button>
        <div className="form-link">
          Hai già un account? <Link to="/">Accedi</Link>
        </div>
      </form>
    </div>
  )
}
