package com.studyhub.repositories;

import com.studyhub.models.Sospensione;
import com.studyhub.models.Utente;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SospensioneRepository extends JpaRepository<Sospensione, Integer> {
    Optional<Sospensione> findByUtente(Utente utente);
}
