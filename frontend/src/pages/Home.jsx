import { useNavigate } from 'react-router-dom'
import Navbar from '../components/Navbar'

export default function Home() {
  const navigate = useNavigate()

  return (
    <>
      <Navbar />
      <div className="page">
        <div className="container">
          <h1 className="page-title">I tuoi corsi</h1>
          <div className="home-course-card">
            <div className="course-card-inner" onClick={() => navigate('/corso')}>
              <div className="course-card-name">Ingegneria Informatica</div>
              <div className="course-card-uni">Università di Bologna (UniBo)</div>
            </div>
          </div>
        </div>
      </div>
    </>
  )
}
