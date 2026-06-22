import { useState, useEffect } from 'react'
import { useParams, useNavigate } from 'react-router-dom'
import Navbar from '../components/Navbar'
import Stars from '../components/Stars'
import { getEsami, getContenuti } from '../services/api'

function formatData(str) {
  if (!str) return ''
  return new Date(str).toLocaleDateString('it-IT')
}

function stelleInfo(c) {
  if (c.numValutazioni === 0) return 'nessun voto'
  return `${c.mediaStelle.toFixed(1)}/5 (${c.numValutazioni} vot${c.numValutazioni === 1 ? 'o' : 'i'})`
}

export default function Esame() {
  const { id } = useParams()
  const navigate = useNavigate()
  const username = localStorage.getItem('username')
  const [nomeEsame, setNomeEsame] = useState('')
  const [contenuti, setContenuti] = useState([])
  const [errore, setErrore] = useState('')

  useEffect(() => {
    getEsami().then(esami => {
      const e = esami.find(x => x.id === parseInt(id))
      if (e) setNomeEsame(e.nome)
    }).catch(() => {})

    getContenuti(id, username)
      .then(setContenuti)
      .catch(err => setErrore(err.message))
  }, [id])

  const documenti = contenuti.filter(c => c.tipo === 'documento')
  const consigli = contenuti.filter(c => c.tipo === 'consiglio')

  function CardDoc({ c }) {
    return (
      <div className="card" onClick={() => navigate(`/contenuto/${c.id}`)}>
        <div className="card-title">{c.titolo}</div>
        <div className="card-body">
          {(c.corpo || '').slice(0, 100)}{(c.corpo || '').length > 100 ? '...' : ''}
        </div>
        <div className="card-meta">
          <span>@{c.utente}</span>
          <span>{formatData(c.dataCreazione)}</span>
          {c.nomeFile && <span>📎 {c.nomeFile}</span>}
          <Stars value={Math.round(c.mediaStelle)} size="sm" />
          <span>{stelleInfo(c)}</span>
        </div>
      </div>
    )
  }

  function CardConsiglio({ c }) {
    return (
      <div className="card" onClick={() => navigate(`/contenuto/${c.id}`)}>
        <span className={`badge badge-${c.etichetta}`}>{c.etichetta}</span>
        <div className="card-body" style={{ marginTop: 8 }}>
          {(c.corpo || '').slice(0, 150)}{(c.corpo || '').length > 150 ? '...' : ''}
        </div>
        <div className="card-meta">
          <span>@{c.utente}</span>
          <span>{formatData(c.dataCreazione)}</span>
          <Stars value={Math.round(c.mediaStelle)} size="sm" />
          <span>{stelleInfo(c)}</span>
        </div>
      </div>
    )
  }

  return (
    <>
      <Navbar extra={
        <button className="btn btn-ghost" onClick={() => navigate(`/pubblica?esame_id=${id}`)}>
          + Pubblica
        </button>
      } />
      <div className="page">
        <div className="container">
          <div className="top-bar">
            <div className="page-title" style={{ marginBottom: 0 }}>{nomeEsame || `Esame #${id}`}</div>
            <button className="btn" onClick={() => navigate('/corso')}>← Torna agli esami</button>
          </div>
          {errore && <div className="error-banner">{errore}</div>}

          <div className="section-title">Documenti ({documenti.length})</div>
          {documenti.length === 0
            ? <div className="empty-msg">Nessun documento caricato.</div>
            : documenti.map(c => <CardDoc key={c.id} c={c} />)
          }

          <div className="section-title">Consigli ({consigli.length})</div>
          {consigli.length === 0
            ? <div className="empty-msg">Nessun consiglio ancora.</div>
            : consigli.map(c => <CardConsiglio key={c.id} c={c} />)
          }
        </div>
      </div>
    </>
  )
}
