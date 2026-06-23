package com.unishare.controllers;

import com.unishare.models.Segnalazione;
import com.unishare.storage.InMemoryStore;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
public class SegnalazioniController {

    @PostMapping("/api/segnalazioni")
    public ResponseEntity<Map<String, Object>> segnala(@RequestBody Map<String, String> body) {
        String idContenuto = body.get("idContenuto");
        String usernameSegnalante = body.get("usernameSegnalante");
        String motivazione = body.get("motivazione");

        // Verifica ruolo utente
        Optional<com.unishare.models.Utente> utente = InMemoryStore.utenti.stream()
                .filter(u -> u.getUsername().equals(usernameSegnalante))
                .findFirst();
        if (utente.isEmpty() || !"utente".equals(utente.get().getRuolo())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("errore", "Accesso negato: solo gli utenti possono segnalare"));
        }

        // Verifica segnalazione duplicata
        boolean giaSegnalato = InMemoryStore.segnalazioni.stream()
                .anyMatch(s -> s.getIdContenuto().equals(idContenuto)
                        && s.getUsernameSegnalante().equals(usernameSegnalante));
        if (giaSegnalato) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("errore", "Contenuto già segnalato da questo utente"));
        }

        Segnalazione s = new Segnalazione(idContenuto, usernameSegnalante, motivazione);
        InMemoryStore.segnalazioni.add(s);
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("successo", true));
    }

    @GetMapping("/api/moderatore/segnalazioni")
    public ResponseEntity<?> listaSegnalazioni(@RequestParam String username) {
        Optional<com.unishare.models.Utente> utente = InMemoryStore.utenti.stream()
                .filter(u -> u.getUsername().equals(username))
                .findFirst();
        if (utente.isEmpty() || !"moderatore".equals(utente.get().getRuolo())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("errore", "Accesso negato: solo i moderatori possono visualizzare le segnalazioni"));
        }

        List<Segnalazione> attesa = InMemoryStore.segnalazioni.stream()
                .filter(s -> "attesa".equals(s.getStato()))
                .toList();
        return ResponseEntity.ok(attesa);
    }
}
