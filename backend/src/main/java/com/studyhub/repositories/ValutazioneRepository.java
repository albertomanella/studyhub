package com.studyhub.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.studyhub.models.Contenuto;
import com.studyhub.models.Utente;
import com.studyhub.models.Valutazione;

public interface ValutazioneRepository extends JpaRepository<Valutazione, Integer> {

    // Sostituisce il vecchio filtro manuale su InMemoryStore.valutazioni
    Optional<Valutazione> findByUtenteAndContenuto(Utente utente, Contenuto contenuto);

    // Sostituisce InMemoryStore.mediaStelle(contenutoId)
    // Ritorna null (non 0.0) se non ci sono valutazioni: nessuna riga da mediare
    @Query("select avg(v.stelle) from Valutazione v where v.contenuto = :contenuto")
    Double mediaStelle(@Param("contenuto") Contenuto contenuto);

    // Sostituisce InMemoryStore.numValutazioni(contenutoId)
    long countByContenuto(Contenuto contenuto);
}
