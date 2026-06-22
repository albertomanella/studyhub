# UniShare — StudyHub Prototype

Applicazione web per la condivisione di materiale universitario.

## Stack
- **Frontend**: React 18 + Vite (porta 5173)
- **Backend**: Java 21 + Spring Boot 3 (porta 8080)
- **Persistenza**: dati in memoria RAM (si resettano al riavvio del backend)

## Avvio

### Backend
```bash
cd backend
mvn spring-boot:run
```

### Frontend
```bash
cd frontend
npm install
npm run dev
```

Apri http://localhost:5173

## Utenti precaricati
| Username | Password   |
|----------|------------|
| mario    | mario123!  |
| giulia   | giulia123! |
