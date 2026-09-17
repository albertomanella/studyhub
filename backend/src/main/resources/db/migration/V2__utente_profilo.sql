create table utente (
    id serial not null,
    username varchar(255) not null,
    password varchar(255) not null,
    nome varchar(255) not null,
    cognome varchar(255) not null,
    email varchar(255) not null,
    biografia varchar(255),
    immagine_profilo varchar(255),
    ruolo varchar(255) not null check (ruolo in ('UTENTE','MODERATORE')),
    num_avvertimenti integer not null,
    data_ultimo_avvertimento date,
    primary key (id),
    constraint uq_utente_email unique (email)
);
create table utente_corso_di_studio (corso_id integer not null, utente_id integer not null, primary key (corso_id, utente_id));
alter table if exists utente_corso_di_studio add constraint fk_utentecorso_corso foreign key (corso_id) references corso_di_studio on delete cascade;
alter table if exists utente_corso_di_studio add constraint fk_utentecorso_utente foreign key (utente_id) references utente on delete cascade;