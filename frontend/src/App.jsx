import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom'
import { AuthProvider, useAuth } from './auth/AuthContext'
import Login from './pages/Login'
import Register from './pages/Register'
import Home from './pages/Home'
import Corso from './pages/Corso'
import Esame from './pages/Esame'
import Contenuto from './pages/Contenuto'
import Pubblica from './pages/Pubblica'
import PubblicaDocumento from './pages/PubblicaDocumento'
import PubblicaConsiglio from './pages/PubblicaConsiglio'
import ElencoSegnalazioni from './pages/ElencoSegnalazioni'

function PrivateRoute({ children }) {
  const { utente, caricamento } = useAuth()
  if (caricamento) return null
  return utente ? children : <Navigate to="/" replace />
}

export default function App() {
  return (
    <AuthProvider>
      <BrowserRouter>
        <Routes>
          <Route path="/" element={<Login />} />
          <Route path="/register" element={<Register />} />
          <Route path="/home" element={<PrivateRoute><Home /></PrivateRoute>} />
          <Route path="/corso" element={<PrivateRoute><Corso /></PrivateRoute>} />
          <Route path="/esame/:id" element={<PrivateRoute><Esame /></PrivateRoute>} />
          <Route path="/contenuto/:id" element={<PrivateRoute><Contenuto /></PrivateRoute>} />
          <Route path="/pubblica" element={<PrivateRoute><Pubblica /></PrivateRoute>} />
          <Route path="/pubblica/documento" element={<PrivateRoute><PubblicaDocumento /></PrivateRoute>} />
          <Route path="/pubblica/consiglio" element={<PrivateRoute><PubblicaConsiglio /></PrivateRoute>} />
          <Route path="/moderatore/segnalazioni" element={<PrivateRoute><ElencoSegnalazioni /></PrivateRoute>} />
          <Route path="*" element={<Navigate to="/" replace />} />
        </Routes>
      </BrowserRouter>
    </AuthProvider>
  )
}
