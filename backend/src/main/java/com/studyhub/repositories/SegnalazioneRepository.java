package com.studyhub.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.studyhub.models.Contenuto;
import com.studyhub.models.Segnalazione;
import com.studyhub.models.StatoSegnalazione;
import com.studyhub.models.Utente;

public interface SegnalazioneRepository extends JpaRepository<Segnalazione, Integer> {

    // Sostituisce il controllo manuale "giaSegnalato" su InMemoryStore.segnalazioni
    boolean existsByUtenteAndContenuto(Utente utente, Contenuto contenuto);

    // Sostituisce il filtro "attesa" nell'endpoint del moderatore
    List<Segnalazione> findByStato(StatoSegnalazione stato);

    void deleteByContenuto(Contenuto contenuto);
}
