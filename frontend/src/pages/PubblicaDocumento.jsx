import { useState, useEffect } from 'react'
import { useNavigate, useSearchParams } from 'react-router-dom'
import Navbar from '../components/Navbar'
import { getEsami, creaDocumento } from '../services/api'

export default function PubblicaDocumento() {
  const navigate = useNavigate()
  const [params] = useSearchParams()

  const [esami, setEsami] = useState([])
  const [esameId, setEsameId] = useState(params.get('esame_id') || '')
  const [titolo, setTitolo] = useState('')
  const [corpo, setCorpo] = useState('')
  const [file, setFile] = useState(null)
  const [errore, setErrore] = useState('')
  const [caricamento, setCaricamento] = useState(false)

  useEffect(() => {
    getEsami().then(data => Array.isArray(data) && setEsami(data)).catch(() => {})
  }, [])

  async function handleSubmit(e) {
    e.preventDefault()
    if (!esameId) { setErrore('Seleziona un esame'); return }
    if (!titolo.trim()) { setErrore('Il titolo è obbligatorio'); return }
    if (!file) { setErrore('Devi allegare un file'); return }
    setErrore('')
    setCaricamento(true)
    try {
      const fd = new FormData()
      fd.append('titolo', titolo)
      fd.append('descrizione', corpo)
      fd.append('esameId', esameId)
      fd.append('file', file)

      const res = await creaDocumento(fd)
      if (res.errore) { setErrore(res.errore); return }

      navigate(`/esame/${esameId}`)
    } catch (err) {
      setErrore(err.message)
    } finally {
      setCaricamento(false)
    }
  }

  return (
    <>
      <Navbar />
      <div className="page">
        <div className="container">
          <form className="form-wrap" style={{ margin: '16px auto' }} onSubmit={handleSubmit}>
            <div className="form-title">Pubblica Documento</div>
            {errore && <div className="form-error" style={{ textAlign: 'center', marginBottom: 10 }}>{errore}</div>}

            <div className="form-group">
              <label className="form-label">Esame</label>
              <select className="form-input" value={esameId} onChange={e => setEsameId(e.target.value)}>
                <option value="">-- seleziona --</option>
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
              <label className="form-label">Allega file</label>
              <div className="file-input-wrap">
                <span>Scegli un file da allegare</span>
                <input type="file" onChange={e => setFile(e.target.files[0] || null)} />
                <div className="char-count">
                  PDF, Word, Excel, PowerPoint, JPG, PNG, ZIP o file di testo e codice (max 20 MB)
                </div>
                {file && <div style={{ marginTop: 4, color: '#1a5276' }}>File selezionato: {file.name}</div>}
              </div>
            </div>

            <button className="btn btn-primary btn-block" type="submit" disabled={caricamento}>
              {caricamento ? 'Pubblicazione...' : 'Pubblica'}
            </button>
          </form>
        </div>
      </div>
    </>
  )
}
