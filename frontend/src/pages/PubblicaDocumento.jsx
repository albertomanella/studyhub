import { useState, useEffect } from 'react'
import { useNavigate, useSearchParams } from 'react-router-dom'
import Navbar from '../components/Navbar'
import { getEsami, creaContenuto } from '../services/api'

export default function PubblicaDocumento() {
  const navigate = useNavigate()
  const [params] = useSearchParams()
  const username = localStorage.getItem('username')

  const [esami, setEsami] = useState([])
  const [esameId, setEsameId] = useState(params.get('esame_id') || '')
  const [titolo, setTitolo] = useState('')
  const [corpo, setCorpo] = useState('')
  const [nomeFile, setNomeFile] = useState('')
  const [errore, setErrore] = useState('')

  useEffect(() => {
    getEsami().then(setEsami).catch(() => {})
  }, [])

  async function handleSubmit(e) {
    e.preventDefault()
    if (!esameId) { setErrore('Seleziona un esame'); return }
    if (!titolo.trim()) { setErrore('Il titolo è obbligatorio'); return }
    setErrore('')
    try {
      await creaContenuto({ tipo: 'documento', titolo, corpo, nomeFile, username, esameId: parseInt(esameId) })
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
          <form className="form-centered" style={{ margin: '20px auto' }} onSubmit={handleSubmit}>
            <div className="form-title">Pubblica Documento</div>
            {errore && <div className="form-error" style={{ textAlign: 'center', marginBottom: 12 }}>{errore}</div>}

            <div className="form-group">
              <label className="form-label">Esame</label>
              <select className="form-input" value={esameId} onChange={e => setEsameId(e.target.value)}>
                <option value="">— Seleziona esame —</option>
                {esami.map(ex => <option key={ex.id} value={ex.id}>{ex.nome}</option>)}
              </select>
            </div>

            <div className="form-group">
              <label className="form-label">Titolo (max 64 caratteri)</label>
              <input className="form-input" maxLength={64} value={titolo}
                onChange={e => setTitolo(e.target.value)} required />
            </div>

            <div className="form-group">
              <label className="form-label">Descrizione (max 512 caratteri)</label>
              <textarea className="form-input" maxLength={512} value={corpo}
                onChange={e => setCorpo(e.target.value)} rows={5} />
              <div className="char-count">{512 - corpo.length} caratteri rimanenti</div>
            </div>

            <div className="form-group">
              <label className="form-label">Nome file (es. appunti_analisi.pdf)</label>
              <input className="form-input" value={nomeFile}
                onChange={e => setNomeFile(e.target.value)}
                placeholder="nome_file.pdf" />
            </div>

            <button className="btn-primary btn-block" type="submit">Pubblica</button>
          </form>
        </div>
      </div>
    </>
  )
}
