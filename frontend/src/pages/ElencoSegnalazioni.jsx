import { useState, useEffect } from 'react'
import { useNavigate } from 'react-router-dom'
import Navbar from '../components/Navbar'
import { getSegnalazioni } from '../services/api'

function formatData(dt) {
  if (!dt) return ''
  return new Date(dt).toLocaleString('it-IT')
}

export default function ElencoSegnalazioni() {
  const navigate = useNavigate()
  const username = localStorage.getItem('username')
  const ruolo = localStorage.getItem('ruolo')
  const [segnalazioni, setSegnalazioni] = useState([])
  const [errore, setErrore] = useState('')

  useEffect(() => {
    if (ruolo !== 'moderatore') return
    getSegnalazioni(username)
      .then(data => {
        if (data.errore) { setErrore(data.errore); return }
        setSegnalazioni(data)
      })
      .catch(err => setErrore(err.message))
  }, [])

  if (ruolo !== 'moderatore') {
    return (
      <>
        <Navbar />
        <div className="page"><div className="container"><div className="error-banner">Accesso negato.</div></div></div>
      </>
    )
  }

  return (
    <>
      <Navbar />
      <div className="page">
        <div className="container">
          <button className="btn" onClick={() => navigate(-1)} style={{ marginBottom: 12 }}>← Indietro</button>
          <div className="page-title">Segnalazioni in attesa</div>

          {errore && <div className="error-banner">{errore}</div>}

          {!errore && segnalazioni.length === 0 && (
            <p style={{ color: '#666', marginTop: 16 }}>Nessuna segnalazione in attesa.</p>
          )}

          {segnalazioni.length > 0 && (
            <div style={{ overflowX: 'auto', marginTop: 16 }}>
              <table style={{ width: '100%', borderCollapse: 'collapse', fontSize: 14 }}>
                <thead>
                  <tr style={{ background: '#1a5276', color: '#fff' }}>
                    <th style={th}>ID Segnalazione</th>
                    <th style={th}>Username segnalante</th>
                    <th style={th}>ID Contenuto</th>
                    <th style={th}>Motivazione</th>
                    <th style={th}>Data</th>
                  </tr>
                </thead>
                <tbody>
                  {segnalazioni.map((s, i) => (
                    <tr key={s.idSegnalazione} style={{ background: i % 2 === 0 ? '#f9f9f9' : '#fff' }}>
                      <td style={td}><span style={{ fontFamily: 'monospace', fontSize: 12 }}>{s.idSegnalazione}</span></td>
                      <td style={td}>@{s.usernameSegnalante}</td>
                      <td style={td}>{s.idContenuto}</td>
                      <td style={td}>{s.motivazione}</td>
                      <td style={td}>{formatData(s.dataSegnalazione)}</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
        </div>
      </div>
    </>
  )
}

const th = { padding: '10px 12px', textAlign: 'left', fontWeight: 600 }
const td = { padding: '10px 12px', borderBottom: '1px solid #e0e0e0', verticalAlign: 'top' }
