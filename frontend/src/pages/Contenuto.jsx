import { useState, useEffect } from 'react'
import { useParams, useNavigate } from 'react-router-dom'
import Navbar from '../components/Navbar'
import Stars from '../components/Stars'
import { getContenuto, valuta, segnala, eliminaContenuto, fileUrl } from '../services/api'
import { useAuth } from '../auth/AuthContext'

function formatData(str) {
  if (!str) return ''
  return new Date(str).toLocaleString('it-IT')
}

export default function Contenuto() {
  const { id } = useParams()
  const navigate = useNavigate()
  const { utente } = useAuth()
  const [contenuto, setContenuto] = useState(null)
  const [errore, setErrore] = useState('')
  const [ratingMsg, setRatingMsg] = useState('')
  const [showSegnala, setShowSegnala] = useState(false)
  const [motivazione, setMotivazione] = useState('')
  const [segnalaMsg, setSegnalaMsg] = useState('')
  const [segnalaOk, setSegnalaOk] = useState(false)

  const [eliminando, setEliminando] = useState(false)

  useEffect(() => {
    getContenuto(id)
      .then(data => {
        if (data.errore) { setErrore(data.errore); return }
        setContenuto(data)
      })
      .catch(err => setErrore(err.message))
  }, [id])

  async function handleRate(stelle) {
    setRatingMsg('')
    try {
      const res = await valuta(parseInt(id), stelle)
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

  async function handleSegnala(e) {
    e.preventDefault()
    setSegnalaMsg('')
    try {
      const res = await segnala(String(id), motivazione)
      if (res.errore) { setSegnalaMsg(res.errore); return }
      setSegnalaOk(true)
      setSegnalaMsg('Segnalazione inviata con successo.')
      setMotivazione('')
    } catch (err) {
      setSegnalaMsg(err.message)
    }
  }

  function handleDownload() {
    const a = document.createElement('a')
    a.href = fileUrl(id)
    a.download = contenuto.nomeFile || 'file'
    a.click()
  }

  async function handleElimina() {
    if (!window.confirm('Eliminare definitivamente questo contenuto?')) return
    setEliminando(true)
    try {
      const res = await eliminaContenuto(id)
      if (res.errore) { setErrore(res.errore); return }
      navigate(-1)
    } catch (err) {
      setErrore(err.message)
    } finally {
      setEliminando(false)
    }
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

  const sonoAutore = contenuto.autore === utente.username
  const stelleAttuali = 0

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
              <span>Autore: <strong>@{contenuto.autore}</strong></span>
              <span>{formatData(contenuto.dataCreazione)}</span>
              <span style={{ textTransform: 'capitalize', color: '#888' }}>[{contenuto.tipo}]</span>
            </div>

            <div className="detail-body">{contenuto.corpo}</div>

            {contenuto.tipo === 'documento' && (
              <div style={{ marginBottom: 16 }}>
                {contenuto.nomeFile ? (
                  <button className="btn btn-primary" onClick={handleDownload}>
                    ⬇ Scarica — {contenuto.nomeFile}
                  </button>
                ) : (
                  <span style={{ fontSize: 12, color: '#999' }}>Nessun file allegato</span>
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

          {sonoAutore && (
            <div style={{ marginTop: 24 }}>
              <button
                className="btn"
                style={{ background: '#c0392b', color: '#fff', border: 'none' }}
                onClick={handleElimina}
                disabled={eliminando}
              >
                {eliminando ? 'Eliminazione...' : 'Elimina contenuto'}
              </button>
            </div>
          )}

          {!sonoAutore && utente.ruolo === 'UTENTE' && (
            <div style={{ marginTop: 24 }}>
              <button
                className="btn"
                style={{ background: '#c0392b', color: '#fff', border: 'none' }}
                onClick={() => { setShowSegnala(true); setSegnalaMsg(''); setSegnalaOk(false) }}
              >
                Segnala contenuto
              </button>
            </div>
          )}
        </div>
      </div>

      {showSegnala && (
        <div style={{
          position: 'fixed', inset: 0, background: 'rgba(0,0,0,0.45)',
          display: 'flex', alignItems: 'center', justifyContent: 'center', zIndex: 1000
        }}>
          <div style={{
            background: '#fff', borderRadius: 10, padding: 28, width: 380, maxWidth: '90vw', boxShadow: '0 4px 24px rgba(0,0,0,0.18)'
          }}>
            <div style={{ fontWeight: 700, fontSize: 17, marginBottom: 16 }}>Segnala contenuto</div>
            {segnalaMsg && (
              <div className={segnalaOk ? undefined : 'form-error'} style={{
                marginBottom: 12, color: segnalaOk ? '#1a7a2e' : undefined
              }}>
                {segnalaMsg}
              </div>
            )}
            {!segnalaOk && (
              <form onSubmit={handleSegnala}>
                <div className="form-group">
                  <label className="form-label">Motivazione *</label>
                  <textarea
                    className="form-input"
                    rows={4}
                    style={{ resize: 'vertical' }}
                    value={motivazione}
                    onChange={e => setMotivazione(e.target.value)}
                    required
                  />
                </div>
                <div style={{ display: 'flex', gap: 10, marginTop: 8 }}>
                  <button className="btn btn-primary" type="submit">Invia</button>
                  <button className="btn" type="button" onClick={() => setShowSegnala(false)}>Annulla</button>
                </div>
              </form>
            )}
            {segnalaOk && (
              <button className="btn" onClick={() => setShowSegnala(false)}>Chiudi</button>
            )}
          </div>
        </div>
      )}
    </>
  )
}
