import { Link, useNavigate } from 'react-router-dom'
import { useAuth } from '../auth/AuthContext'

export default function Navbar({ extra }) {
  const navigate = useNavigate()
  const { utente, logout } = useAuth()
  const username = utente?.username

  async function esci() {
    await logout()
    navigate('/')
  }

  return (
    <nav className="navbar">
      <Link to="/home" className="navbar-brand">StudyHub</Link>
      <div className="navbar-right">
        {extra}
        {username && (
          <>
            <span className="navbar-user">{username}</span>
            <button className="btn btn-ghost" onClick={esci}>Esci</button>
          </>
        )}
      </div>
    </nav>
  )
}
