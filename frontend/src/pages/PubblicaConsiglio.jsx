import { useState, useEffect } from 'react'
import { useNavigate, useSearchParams } from 'react-router-dom'
import Navbar from '../components/Navbar'
import { getEsami, getEtichette, creaConsiglio } from '../services/api'

export default function PubblicaConsiglio() {
  const navigate = useNavigate()
  const [params] = useSearchParams()

  const [esami, setEsami] = useState([])
  const [etichette, setEtichette] = useState([])
  const [esameId, setEsameId] = useState(params.get('esame_id') || '')
  const [etichettaId, setEtichettaId] = useState('')
  const [corpo, setCorpo] = useState('')
  const [errore, setErrore] = useState('')

  useEffect(() => {
    getEsami().then(data => Array.isArray(data) && setEsami(data)).catch(() => {})
    getEtichette().then(data => {
      if (!Array.isArray(data)) return
      setEtichette(data)
      if (data.length && !etichettaId) setEtichettaId(String(data[0].id))
    }).catch(() => {})
  }, [])

  async function handleSubmit(e) {
    e.preventDefault()
    if (!esameId) { setErrore('Seleziona un esame'); return }
    if (!etichettaId) { setErrore('Seleziona una categoria'); return }
    if (!corpo.trim()) { setErrore('Il testo è obbligatorio'); return }
    setErrore('')
    try {
      const res = await creaConsiglio({
        corpo, esameId: parseInt(esameId), etichettaId: parseInt(etichettaId)
      })
      if (res.errore) { setErrore(res.errore); return }
      navigate(`/esame/${esameId}`)
    } catch (err) {
      setErrore(err.message)
    }
  }

  return (
    <>
      <Navbar />
      <div className="page">
        <div className="container">
          <form className="form-wrap" style={{ margin: '16px auto' }} onSubmit={handleSubmit}>
            <div className="form-title">Pubblica Consiglio</div>
            {errore && <div className="form-error" style={{ textAlign: 'center', marginBottom: 10 }}>{errore}</div>}

            <div className="form-group">
              <label className="form-label">Esame</label>
              <select className="form-input" value={esameId} onChange={e => setEsameId(e.target.value)}>
                <option value="">-- seleziona --</option>
                {esami.map(ex => <option key={ex.id} value={ex.id}>{ex.nome}</option>)}
              </select>
            </div>

            <div className="form-group">
              <label className="form-label">Categoria</label>
              <select className="form-input" value={etichettaId} onChange={e => setEtichettaId(e.target.value)}>
                {etichette.map(et => <option key={et.id} value={et.id}>{et.nome}</option>)}
              </select>
            </div>

            <div className="form-group">
              <label className="form-label">Testo del consiglio (max 512 caratteri)</label>
              <textarea className="form-input" maxLength={512} value={corpo}
                onChange={e => setCorpo(e.target.value)} rows={6} required />
              <div className="char-count">{512 - corpo.length} rimasti</div>
            </div>

            <button className="btn btn-primary btn-block" type="submit">Pubblica</button>
          </form>
        </div>
      </div>
    </>
  )
}
