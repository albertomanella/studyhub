package com.studyhub.controllers;

import com.studyhub.models.RuoloUtente;
import com.studyhub.models.Segnalazione;
import com.studyhub.repositories.ContenutoRepository;
import com.studyhub.repositories.UtenteRepository;
import com.studyhub.storage.InMemoryStore;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
public class SegnalazioniController {

        private final UtenteRepository utenteRepository;
        private final ContenutoRepository contenutoRepository;

        public SegnalazioniController(UtenteRepository utenteRepository, ContenutoRepository contenutoRepository) {
                this.utenteRepository = utenteRepository;
                this.contenutoRepository = contenutoRepository;
        }

        @PostMapping("/api/segnalazioni")
        public ResponseEntity<Map<String, Object>> segnala(@RequestBody Map<String, String> body) {
                Integer idContenuto = Integer.valueOf(body.get("idContenuto"));
                String usernameSegnalante = body.get("usernameSegnalante");
                String motivazione = body.get("motivazione");

                // Verifica ruolo utente
                Optional<com.studyhub.models.Utente> utente = utenteRepository.findByUsername(usernameSegnalante);
                if (utente.isEmpty() || !RuoloUtente.UTENTE.equals(utente.get().getRuolo())) {
                        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                                        .body(Map.of("errore", "Accesso negato: solo gli utenti possono segnalare"));
                }

                // Verifica che l'utente non stia segnalando un proprio contenuto e che il
                // contenuto esista
                if (!contenutoRepository.findById(idContenuto).isPresent()) {
                        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                                        .body(Map.of("errore", "Il contenuto non esiste"));

                }
                if (contenutoRepository.findById(idContenuto).get().getUtente().getUsername()
                                .equals(usernameSegnalante)) {
                        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                                        .body(Map.of("errore", "Non puoi segnalare un tuo contenuto"));
                }

                // Verifica segnalazione duplicata
                boolean giaSegnalato = InMemoryStore.segnalazioni.stream()
                                .anyMatch(s -> s.getIdContenuto().equals(String.valueOf(idContenuto))
                                                && s.getUsernameSegnalante().equals(usernameSegnalante));
                if (giaSegnalato) {
                        return ResponseEntity.status(HttpStatus.CONFLICT)
                                        .body(Map.of("errore", "Contenuto già segnalato da questo utente"));
                }

                Segnalazione s = new Segnalazione(String.valueOf(idContenuto), usernameSegnalante, motivazione);
                InMemoryStore.segnalazioni.add(s);
                return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("successo", true));
        }

        @GetMapping("/api/moderatore/segnalazioni")
        public ResponseEntity<?> listaSegnalazioni(@RequestParam String username) {
                Optional<com.studyhub.models.Utente> utente = utenteRepository.findByUsername(username);
                if (utente.isEmpty() || !RuoloUtente.MODERATORE.equals(utente.get().getRuolo())) {
                        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                                        .body(Map.of("errore",
                                                        "Accesso negato: solo i moderatori possono visualizzare le segnalazioni"));
                }

                List<Segnalazione> attesa = InMemoryStore.segnalazioni.stream()
                                .filter(s -> "attesa".equals(s.getStato()))
                                .toList();
                return ResponseEntity.ok(attesa);
        }
}