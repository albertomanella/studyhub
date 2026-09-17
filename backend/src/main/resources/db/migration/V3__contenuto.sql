create table etichetta (
    id serial not null,
    nome varchar(32) not null unique,
    primary key (id)
);
create table contenuto (
    id serial not null,
    tipo varchar(31) not null,              -- colonna discriminante, gestita da Hibernate
    utente_id integer not null,
    esame_id integer not null,
    data_creazione timestamp,
    titolo varchar(64),                     -- nullable: solo Documento
    descrizione varchar(512),               -- nullable: solo Documento
    nome_file varchar(255),                 -- nullable: solo Documento
    corpo varchar(512),                     -- nullable: solo Consiglio
    etichetta_id integer,                   -- nullable: solo Consiglio
    primary key (id)
);

alter table if exists contenuto add constraint fk_contenuto_utente foreign key (utente_id) references utente on delete cascade;
alter table if exists contenuto add constraint fk_contenuto_esame foreign key (esame_id) references esame on delete cascade;
alter table if exists contenuto add constraint fk_contenuto_etichetta foreign key (etichetta_id) references etichetta;