package com.studyhub.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import com.studyhub.models.Utente;

public interface UtenteRepository extends JpaRepository<Utente, Integer> {
    Boolean existsByUsername(String username);

    Boolean existsByProfiloEmail(String email);

    Optional<Utente> findByUsername(String username);
}