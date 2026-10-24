package com.studyhub.controllers;

import com.studyhub.models.Contenuto;
import com.studyhub.models.Utente;
import com.studyhub.models.Valutazione;
import com.studyhub.repositories.ContenutoRepository;
import com.studyhub.repositories.ValutazioneRepository;
import com.studyhub.security.UtenteCorrente;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/valutazioni")
public class ValutazioniController {

    private final UtenteCorrente utenteCorrente;
    private final ContenutoRepository contenutoRepository;
    private final ValutazioneRepository valutazioneRepository;

    public ValutazioniController(UtenteCorrente utenteCorrente,
            ContenutoRepository contenutoRepository,
            ValutazioneRepository valutazioneRepository) {
        this.utenteCorrente = utenteCorrente;
        this.contenutoRepository = contenutoRepository;
        this.valutazioneRepository = valutazioneRepository;
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> valuta(@RequestBody Map<String, Object> body,
            Authentication authentication) {
        int contenutoId = (Integer) body.get("contenutoId");
        int stelle = (Integer) body.get("stelle");

        Utente utente = utenteCorrente.da(authentication);
        Optional<Contenuto> contenutoOpt = contenutoRepository.findById(contenutoId);
        if (contenutoOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("errore", "Il contenuto non esiste"));
        }

        Contenuto contenuto = contenutoOpt.get();

        // Controlla che l'utente non valuti il proprio contenuto
        if (contenuto.getUtente().getId().equals(utente.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("errore", "Non puoi valutare i tuoi contenuti"));
        }

        try {
            // Sovrascrive se già esiste, altrimenti crea
            Optional<Valutazione> esistente = valutazioneRepository.findByUtenteAndContenuto(utente, contenuto);
            if (esistente.isPresent()) {
                esistente.get().setStelle(stelle);
                valutazioneRepository.save(esistente.get());
            } else {
                valutazioneRepository.save(new Valutazione(stelle, utente, contenuto));
            }
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("errore", e.getMessage()));
        }

        Double media = valutazioneRepository.mediaStelle(contenuto);
        long numValutazioni = valutazioneRepository.countByContenuto(contenuto);

        return ResponseEntity.ok(Map.of(
                "successo", true,
                "nuovaMedia", media != null ? media : 0.0,
                "numValutazioni", numValutazioni));
    }
}
