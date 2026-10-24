import { useNavigate } from 'react-router-dom'
import Navbar from '../components/Navbar'
import { useAuth } from '../auth/AuthContext'

export default function Home() {
  const navigate = useNavigate()
  const { utente } = useAuth()

  return (
    <>
      <Navbar />
      <div className="page">
        <div className="container">
          <div className="page-title">I miei corsi</div>
          <div className="course-card" onClick={() => navigate('/corso')}>
            <div className="course-card-name">Ingegneria Informatica</div>
            <div className="course-card-uni">Università di Bologna (UniBo)</div>
          </div>

          {utente.ruolo === 'MODERATORE' && (
            <div style={{ marginTop: 32 }}>
              <div className="page-title">Area Moderatore</div>
              <div className="course-card" onClick={() => navigate('/moderatore/segnalazioni')}>
                <div className="course-card-name">Segnalazioni in attesa</div>
                <div className="course-card-uni">Visualizza e monitora le segnalazioni degli utenti</div>
              </div>
            </div>
          )}
        </div>
      </div>
    </>
  )
}
