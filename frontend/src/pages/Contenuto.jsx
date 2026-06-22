import { useState, useEffect } from 'react'
import { useParams, useNavigate } from 'react-router-dom'
import Navbar from '../components/Navbar'
import Stars from '../components/Stars'
import { getContenuto, valuta } from '../services/api'

const BASE = 'http://localhost:8081/api'

function formatData(str) {
  if (!str) return ''
  return new Date(str).toLocaleString('it-IT')
}

export default function Contenuto() {
  const { id } = useParams()
  const navigate = useNavigate()
  const username = localStorage.getItem('username')
  const [contenuto, setContenuto] = useState(null)
  const [errore, setErrore] = useState('')
  const [ratingMsg, setRatingMsg] = useState('')

  useEffect(() => {
    getContenuto(id, username)
      .then(data => {
        if (data.errore) { setErrore(data.errore); return }
        setContenuto(data)
      })
      .catch(err => setErrore(err.message))
  }, [id])

  async function handleRate(stelle) {
    setRatingMsg('')
    try {
      const res = await valuta(username, parseInt(id), stelle)
      if (res.errore) { setRatingMsg(res.errore); return }
      setContenuto(prev => ({
        ...prev,
        mediaStelle: res.nuovaMedia,
        numValutazioni: res.numValutazioni,
        valutazioneCorrente: stelle
      }))
    } catch (err) {
      setRatingMsg(err.message)
    }
  }

  function handleDownload() {
    const a = document.createElement('a')
    a.href = `${BASE}/contenuti/${id}/file`
    a.download = contenuto.nomeFile || 'file'
    a.click()
  }

  if (errore) return (
    <>
      <Navbar />
      <div className="page"><div className="container"><div className="error-banner">{errore}</div></div></div>
    </>
  )

  if (!contenuto) return (
    <>
      <Navbar />
      <div className="page"><div className="container"><p>Caricamento...</p></div></div>
    </>
  )

  const sonoAutore = contenuto.utente === username
  const stelleAttuali = sonoAutore ? 0 : (contenuto.valutazioneCorrente > 0 ? contenuto.valutazioneCorrente : 0)

  return (
    <>
      <Navbar />
      <div className="page">
        <div className="container">
          <button className="btn" onClick={() => navigate(-1)} style={{ marginBottom: 12 }}>← Indietro</button>

          <div className="detail-card">
            {contenuto.tipo === 'consiglio' && contenuto.etichetta && (
              <span className={`badge badge-${contenuto.etichetta}`} style={{ marginBottom: 10, display: 'inline-block' }}>
                {contenuto.etichetta}
              </span>
            )}

            {contenuto.tipo === 'documento' && (
              <h2 style={{ fontSize: 17, fontWeight: 'bold', color: '#1a5276', marginBottom: 8 }}>
                {contenuto.titolo}
              </h2>
            )}

            <div className="detail-meta">
              <span>Autore: <strong>@{contenuto.utente}</strong></span>
              <span>{formatData(contenuto.dataCreazione)}</span>
              <span style={{ textTransform: 'capitalize', color: '#888' }}>[{contenuto.tipo}]</span>
            </div>

            <div className="detail-body">{contenuto.corpo}</div>

            {contenuto.tipo === 'documento' && (
              <div style={{ marginBottom: 16 }}>
                {contenuto.hasFile ? (
                  <button className="btn btn-primary" onClick={handleDownload}>
                    ⬇ Scarica — {contenuto.nomeFile}
                  </button>
                ) : (
                  <span style={{ fontSize: 12, color: '#999' }}>
                    {contenuto.nomeFile ? `File indicato: ${contenuto.nomeFile} (non caricato)` : 'Nessun file allegato'}
                  </span>
                )}
              </div>
            )}

            <div className="rating-section">
              <div className="rating-label">
                {sonoAutore ? 'Valutazione ricevuta' : 'Dai un voto'}
              </div>
              <Stars value={stelleAttuali} onRate={sonoAutore ? null : handleRate} disabled={sonoAutore} />
              {sonoAutore && <div className="disabled-note">Non puoi valutare i tuoi contenuti.</div>}
              {ratingMsg && <div className="form-error" style={{ marginTop: 4 }}>{ratingMsg}</div>}
              <div className="rating-info">
                {contenuto.numValutazioni === 0
                  ? 'Ancora nessun voto.'
                  : `Media: ${contenuto.mediaStelle.toFixed(1)}/5 — ${contenuto.numValutazioni} vot${contenuto.numValutazioni === 1 ? 'o' : 'i'}`
                }
              </div>
            </div>
          </div>
        </div>
      </div>
    </>
  )
}
