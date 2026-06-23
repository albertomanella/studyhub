import { useNavigate, useSearchParams } from 'react-router-dom'
import Navbar from '../components/Navbar'

export default function Pubblica() {
  const navigate = useNavigate()
  const [params] = useSearchParams()
  const esameId = params.get('esame_id') || ''

  function vai(tipo) {
    const qs = esameId ? `?esame_id=${esameId}` : ''
    navigate(`/pubblica/${tipo}${qs}`)
  }

  return (
    <>
      <Navbar />
      <div className="page">
        <div className="container">
          <div className="page-title">Pubblica contenuto</div>
          <div className="publish-choice">
            <button className="publish-choice-btn" onClick={() => vai('documento')}>
              Documento
            </button>
            <button className="publish-choice-btn" onClick={() => vai('consiglio')}>
              Consiglio
            </button>
          </div>
        </div>
      </div>
    </>
  )
}
