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
    getEsami().then(setEsami).catch(err => setErrore(err.message))
  }, [])

  const filtrati = esami.filter(e => e.nome.toLowerCase().includes(cerca.toLowerCase()))

  const perAnno = {
    1: filtrati.filter(e => e.anno === 1),
    2: filtrati.filter(e => e.anno === 2),
    3: filtrati.filter(e => e.anno === 3),
  }

  const labelAnno = { 1: '1° Anno', 2: '2° Anno', 3: '3° Anno' }

  return (
    <>
      <Navbar extra={
        <button className="btn btn-ghost" onClick={() => navigate('/pubblica')}>+ Pubblica</button>
      } />
      <div className="page">
        <div className="container">
          <div className="top-bar">
            <div className="page-title" style={{ marginBottom: 0 }}>Ingegneria Informatica</div>
          </div>
          {errore && <div className="error-banner">{errore}</div>}
          <input
            className="search-bar"
            placeholder="Cerca esame..."
            value={cerca}
            onChange={e => setCerca(e.target.value)}
          />
          {[1, 2, 3].map(anno => (
            perAnno[anno].length > 0 && (
              <div key={anno}>
                <div className="section-title">{labelAnno[anno]}</div>
                <div className="grid">
                  {perAnno[anno].map(esame => (
                    <div key={esame.id} className="card" onClick={() => navigate(`/esame/${esame.id}`)}>
                      <div className="card-title">{esame.nome}</div>
                    </div>
                  ))}
                </div>
              </div>
            )
          ))}
        </div>
      </div>
    </>
  )
}
