create table ateneo (id serial not null, nome_ateneo varchar(255), primary key (id));
create table corso_di_studio (ateneo_id integer not null, id serial not null, nome_corso varchar(255), primary key (id));
create table esame (anno integer not null, corso_id integer not null, id serial not null, nome varchar(255), primary key (id));
alter table if exists corso_di_studio add constraint fk_corso_ateneo foreign key (ateneo_id) references ateneo on delete cascade;
alter table if exists esame add constraint fk_esame_corso foreign key (corso_id) references corso_di_studio on delete cascade;