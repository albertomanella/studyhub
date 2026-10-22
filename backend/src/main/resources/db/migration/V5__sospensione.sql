create table sospensione (
    id serial not null,
    utente_id integer not null,
    motivazione varchar(256) not null,
    data_inizio date not null,
    data_fine date not null,
    primary key (id)
);

alter table if exists sospensione
    add constraint fk_sospensione_utente foreign key (utente_id) references utente on delete cascade;
alter table if exists sospensione
    add constraint uq_sospensione_utente unique (utente_id);
