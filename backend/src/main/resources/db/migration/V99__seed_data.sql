-- Dati di riferimento (atenei, corsi, esami, etichette): popolamento iniziale
-- statico, non gestibile a runtime (nessun pannello di amministrazione CRUD
-- nello scope di questa tesi). Sostituisce MockDataInitializer.

insert into ateneo (nome_ateneo) values ('Università di Bologna (UniBo)');

insert into corso_di_studio (nome_corso, ateneo_id)
select 'Ingegneria Informatica', id from ateneo where nome_ateneo = 'Università di Bologna (UniBo)';

insert into esame (nome, anno, corso_id)
select v.nome, v.anno, c.id
from (values
    ('Fondamenti di Informatica T-1', 1),
    ('Fondamenti di Informatica T-2', 1),
    ('Analisi Matematica T-1', 1),
    ('Analisi Matematica T-2', 1),
    ('Reti Logiche T', 1),
    ('Geometria e Algebra T', 1),
    ('Elettrotecnica T', 2),
    ('Sistemi Informativi T', 2),
    ('Sistemi Operativi T', 2),
    ('Calcolatori Elettronici T', 2),
    ('Fisica Generale T', 2),
    ('Economia Aziendale T', 2),
    ('Matematica Applicata T', 2),
    ('Fondamenti di Telecomunicazioni T', 2),
    ('Reti di Calcolatori T', 3),
    ('Tecnologie Web T', 3),
    ('Elettronica T', 3),
    ('Controlli Automatici T', 3),
    ('Ingegneria del Software T', 3),
    ('Diritto dell''Informatica T', 3),
    ('Sicurezza Informatica T', 3),
    ('Progettazione Applicazioni Web T', 3),
    ('Amministrazione di Sistemi T', 3)
) as v(nome, anno)
cross join (select id from corso_di_studio where nome_corso = 'Ingegneria Informatica') as c;

insert into etichetta (nome) values
    ('professore'),
    ('esame'),
    ('lezione'),
    ('teoria'),
    ('esercizio'),
    ('altro');
