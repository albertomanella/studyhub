# StudyHub

Applicazione web per la condivisione di materiale universitario.

## Stack
- **Frontend**: React 18 + Vite (porta 5173)
- **Backend**: Java 21 + Spring Boot 3 (porta 8081)
- **Persistenza**: PostgreSQL 16 (Docker), schema gestito con Flyway

## Avvio

### Configurazione
```bash
cp .env.example .env   # poi imposta DB_PASSWORD
```

### Database
```bash
docker compose up -d
```

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

Apri http://localhost:5173 e registra un nuovo utente.

## Moderatori

La registrazione crea solo utenti normali. Per rendere moderatore un utente
già registrato, aggiungi il suo username in `.env` (più nomi separati da
virgola) e riavvia il backend:

```bash
MODERATORS=mario,giulia
```

Il nuovo ruolo vale dal login successivo. Togliere un nome dalla lista non
revoca il ruolo.
