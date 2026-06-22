import { useNavigate } from 'react-router-dom'
import Navbar from '../components/Navbar'

export default function Home() {
  const navigate = useNavigate()

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
        </div>
      </div>
    </>
  )
}
