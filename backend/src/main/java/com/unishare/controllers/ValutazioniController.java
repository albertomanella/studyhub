package com.unishare.controllers;

import com.unishare.models.Valutazione;
import com.unishare.storage.InMemoryStore;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/valutazioni")
public class ValutazioniController {

    @PostMapping
    public Map<String, Object> valuta(@RequestBody Map<String, Object> body) {
        String username = (String) body.get("username");
        int contenutoId = (Integer) body.get("contenutoId");
        int stelle = (Integer) body.get("stelle");

        // Controlla che l'utente non valuti il proprio contenuto
        Optional<String> autore = InMemoryStore.contenuti.stream()
                .filter(c -> c.getId() == contenutoId)
                .map(c -> c.getUtente())
                .findFirst();
        if (autore.isPresent() && autore.get().equals(username)) {
            return Map.of("errore", "Non puoi valutare i tuoi contenuti");
        }

        // Sovrascrive se già esiste
        Optional<Valutazione> esistente = InMemoryStore.valutazioni.stream()
                .filter(v -> v.getUsername().equals(username) && v.getContenutoId() == contenutoId)
                .findFirst();
        if (esistente.isPresent()) {
            esistente.get().setStelle(stelle);
        } else {
            InMemoryStore.valutazioni.add(new Valutazione(InMemoryStore.nextId(), stelle, username, contenutoId));
        }

        return Map.of(
                "successo", true,
                "nuovaMedia", InMemoryStore.mediaStelle(contenutoId),
                "numValutazioni", InMemoryStore.numValutazioni(contenutoId)
        );
    }
}
