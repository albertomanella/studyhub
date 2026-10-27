-- Il file su disco ha ora un nome generato (UUID); il nome scelto dall'utente
-- si conserva a parte e serve solo per mostrarlo e per il download.
alter table contenuto add column nome_originale varchar(255);

-- Righe esistenti: il vecchio formato era "<uuid>_<nome originale>". Il file
-- resta su disco con il suo nome, che continua a valere come nome di
-- archiviazione; qui si ricava solo il nome da mostrare.
update contenuto
set nome_originale = case
        when nome_file ~ '^[0-9a-f-]{36}_' then substring(nome_file from 38)
        else nome_file
    end
where nome_file is not null;
