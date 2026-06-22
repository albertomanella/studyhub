import { useState, useEffect } from 'react'
import { useParams, useNavigate } from 'react-router-dom'
import Navbar from '../components/Navbar'
import Stars from '../components/Stars'
import { getContenuto, valuta } from '../services/api'

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

  function carica() {
    getContenuto(id, username)
      .then(data => {
        if (data.errore) { setErrore(data.errore); return }
        setContenuto(data)
      })
      .catch(err => setErrore(err.message))
  }

  useEffect(() => { carica() }, [id])

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

  if (errore) {
    return (
      <>
        <Navbar />
        <div className="page"><div className="container"><div className="error-banner">{errore}</div></div></div>
      </>
    )
  }

  if (!contenuto) return (
    <>
      <Navbar />
      <div className="page"><div className="container"><p>Caricamento...</p></div></div>
    </>
  )

  const sonoAutore = contenuto.utente === username
  const statoStelle = sonoAutore ? 0 : (contenuto.valutazioneCorrente > 0 ? contenuto.valutazioneCorrente : 0)

  return (
    <>
      <Navbar />
      <div className="page">
        <div className="container">
          <button
            className="btn-secondary"
            onClick={() => navigate(-1)}
            style={{ marginBottom: 16 }}
          >
            ← Torna indietro
          </button>

          <div className="detail-card">
            {contenuto.tipo === 'consiglio' && contenuto.etichetta && (
              <span className={`badge badge-${contenuto.etichetta}`} style={{ marginBottom: 12, display: 'inline-block' }}>
                {contenuto.etichetta}
              </span>
            )}

            {contenuto.tipo === 'documento' && (
              <h2 style={{ fontSize: 22, fontWeight: 700, color: '#1a3a5c', marginBottom: 8 }}>
                {contenuto.titolo}
              </h2>
            )}

            <div className="detail-meta">
              <span>Autore: <strong>@{contenuto.utente}</strong></span>
              <span>{formatData(contenuto.dataCreazione)}</span>
              <span style={{ textTransform: 'capitalize' }}>{contenuto.tipo}</span>
            </div>

            <div className="detail-body">{contenuto.corpo}</div>

            {contenuto.tipo === 'documento' && contenuto.nomeFile && (
              <button
                className="btn-primary"
                style={{ marginBottom: 16 }}
                onClick={() => alert(`Download simulato: ${contenuto.nomeFile}`)}
              >
                ⬇ Scarica — {contenuto.nomeFile}
              </button>
            )}

            <div className="rating-section">
              <div className="rating-label">
                {sonoAutore ? 'Valutazione' : 'Valuta questo contenuto'}
              </div>

              <Stars
                value={statoStelle}
                onRate={sonoAutore ? null : handleRate}
                disabled={sonoAutore}
              />

              {sonoAutore && (
                <div className="disabled-note">Non puoi valutare i tuoi contenuti.</div>
              )}

              {ratingMsg && <div className="form-error" style={{ marginTop: 6 }}>{ratingMsg}</div>}

              <div className="rating-info">
                {contenuto.numValutazioni === 0
                  ? 'Nessuna valutazione ancora.'
                  : `Media: ★ ${contenuto.mediaStelle.toFixed(1)} su ${contenuto.numValutazioni} vot${contenuto.numValutazioni === 1 ? 'o' : 'i'}`
                }
              </div>
            </div>
          </div>
        </div>
      </div>
    </>
  )
}
