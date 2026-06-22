import { useState, useEffect } from 'react'
import { useNavigate } from 'react-router-dom'
import Navbar from '../components/Navbar'
import { getEsami } from '../services/api'

export default function Corso() {
  const navigate = useNavigate()
  const [esami, setEsami] = useState([])
  const [cerca, setCerca] = useState('')
  const [errore, setErrore] = useState('')

  useEffect(() => {
    getEsami()
      .then(setEsami)
      .catch(err => setErrore(err.message))
  }, [])

  const filtrati = esami.filter(e => e.nome.toLowerCase().includes(cerca.toLowerCase()))

  return (
    <>
      <Navbar extra={
        <button className="btn-ghost" onClick={() => navigate('/pubblica')}>Pubblica contenuto</button>
      } />
      <div className="page">
        <div className="container">
          <h1 className="page-title">Ingegneria Informatica</h1>
          {errore && <div className="error-banner">{errore}</div>}
          <input
            className="search-bar"
            placeholder="Cerca esame..."
            value={cerca}
            onChange={e => setCerca(e.target.value)}
          />
          <div className="grid">
            {filtrati.map(esame => (
              <div key={esame.id} className="card" onClick={() => navigate(`/esame/${esame.id}`)}>
                <div className="card-title">{esame.nome}</div>
              </div>
            ))}
          </div>
        </div>
      </div>
    </>
  )
}
