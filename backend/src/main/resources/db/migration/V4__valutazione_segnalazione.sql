create table valutazione (
    id serial not null,
    stelle integer not null,
    utente_id integer not null,
    contenuto_id integer not null,
    primary key (id)
);

create table segnalazione (
    id serial not null,
    motivazione varchar(256) not null,
    utente_id integer not null,
    contenuto_id integer not null,
    stato varchar(255) not null,
    data_segnalazione timestamp not null,
    primary key (id)
);

alter table if exists valutazione
    add constraint fk_valutazione_utente foreign key (utente_id) references utente on delete cascade;
alter table if exists valutazione
    add constraint fk_valutazione_contenuto foreign key (contenuto_id) references contenuto on delete cascade;

alter table if exists segnalazione
    add constraint fk_segnalazione_utente foreign key (utente_id) references utente on delete cascade;
alter table if exists segnalazione
    add constraint fk_segnalazione_contenuto foreign key (contenuto_id) references contenuto on delete cascade;

alter table if exists valutazione
    add constraint uq_valutazione_utente_contenuto unique (utente_id, contenuto_id);
alter table if exists segnalazione
    add constraint uq_segnalazione_utente_contenuto unique (utente_id, contenuto_id);