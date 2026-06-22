import { useState } from 'react'
import { useNavigate, Link } from 'react-router-dom'
import { register } from '../services/api'

function validate(fields) {
  const errs = {}
  if (!fields.nome.trim()) errs.nome = 'Il nome è obbligatorio'
  if (!fields.cognome.trim()) errs.cognome = 'Il cognome è obbligatorio'
  if (!fields.username.trim()) errs.username = 'Lo username è obbligatorio'
  if (!fields.email.match(/^[^\s@]+@[^\s@]+\.[^\s@]+$/)) errs.email = 'Email non valida'
  if (fields.password.length < 8) errs.password = 'Minimo 8 caratteri'
  else if (!/[!@#$%^&*()_+\-=[\]{};':"\\|,.<>/?]/.test(fields.password))
    errs.password = 'Deve contenere almeno un carattere speciale'
  return errs
}

export default function Register() {
  const navigate = useNavigate()
  const [fields, setFields] = useState({ nome: '', cognome: '', username: '', email: '', password: '' })
  const [errs, setErrs] = useState({})
  const [apiError, setApiError] = useState('')

  function set(k) { return e => setFields(f => ({ ...f, [k]: e.target.value })) }

  async function handleSubmit(e) {
    e.preventDefault()
    const v = validate(fields)
    setErrs(v)
    if (Object.keys(v).length) return
    setApiError('')
    try {
      const res = await register({ username: fields.username, password: fields.password, nome: fields.nome, cognome: fields.cognome })
      if (res.errore) { setApiError(res.errore); return }
      navigate('/')
    } catch (err) {
      setApiError(err.message)
    }
  }

  function field(label, key, type = 'text', disabled = false) {
    return (
      <div className="form-group">
        <label className="form-label">{label}</label>
        <input className={`form-input${errs[key] ? ' error' : ''}`} type={type}
          value={fields[key] ?? ''} onChange={set(key)} disabled={disabled} />
        {errs[key] && <div className="form-error">{errs[key]}</div>}
      </div>
    )
  }

  return (
    <div style={{ paddingTop: 40, minHeight: '100vh', background: '#fff' }}>
      <form className="form-centered" onSubmit={handleSubmit}>
        <div className="form-title">Registrazione</div>
        {apiError && <div className="form-error" style={{ textAlign: 'center', marginBottom: 12 }}>{apiError}</div>}
        {field('Nome', 'nome')}
        {field('Cognome', 'cognome')}
        {field('Username', 'username')}
        {field('Email', 'email', 'email')}
        {field('Password', 'password', 'password')}
        <div className="form-group">
          <label className="form-label">Ateneo</label>
          <input className="form-input" value="Università di Bologna (UniBo)" disabled />
        </div>
        <div className="form-group">
          <label className="form-label">Corso di laurea</label>
          <input className="form-input" value="Ingegneria Informatica" disabled />
        </div>
        <button className="btn-primary btn-block" type="submit">Registrati</button>
        <div className="form-link">
          Hai già un account? <Link to="/">Accedi</Link>
        </div>
      </form>
    </div>
  )
}
