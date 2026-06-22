import { Link, useNavigate } from 'react-router-dom'

export default function Navbar({ extra }) {
  const navigate = useNavigate()
  const username = localStorage.getItem('username')

  function esci() {
    localStorage.removeItem('username')
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
