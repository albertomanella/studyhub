-- R5NF: l'unicità dello username era garantita solo dal controllo applicativo
-- (UtenteRepository.existsByUsername in AuthController), non da un vincolo DB,
-- a differenza dell'email (uq_utente_email, introdotto in V2). Sotto richieste
-- concorrenti il solo controllo applicativo non basta a evitare un duplicato.
alter table if exists utente
    add constraint uq_utente_username unique (username);
