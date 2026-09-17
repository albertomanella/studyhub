package com.studyhub.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.studyhub.models.Contenuto;
import com.studyhub.models.Esame;
import com.studyhub.models.Utente;

public interface ContenutoRepository extends JpaRepository<Contenuto, Integer> {
    List<Contenuto> findByEsame(Esame esame);

    List<Contenuto> findByUtente(Utente utente);
}