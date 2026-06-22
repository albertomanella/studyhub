package com.unishare.controllers;

import com.unishare.models.Contenuto;
import com.unishare.storage.InMemoryStore;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/contenuti")
public class ContenutiController {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

    private Map<String, Object> arricchisci(Contenuto c, String username) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", c.getId());
        m.put("tipo", c.getTipo());
        m.put("titolo", c.getTitolo());
        m.put("corpo", c.getCorpo());
        m.put("etichetta", c.getEtichetta());
        m.put("nomeFile", c.getNomeFile());
        m.put("utente", c.getUtente());
        m.put("esameId", c.getEsameId());
        m.put("dataCreazione", c.getDataCreazione());
        m.put("mediaStelle", InMemoryStore.mediaStelle(c.getId()));
        m.put("numValutazioni", InMemoryStore.numValutazioni(c.getId()));
        if (username != null && !username.isBlank()) {
            m.put("valutazioneCorrente", InMemoryStore.valutazioneUtente(username, c.getId()));
        }
        return m;
    }

    @GetMapping
    public List<Map<String, Object>> getContenuti(
            @RequestParam("esame_id") int esameId,
            @RequestParam(value = "username", required = false) String username) {
        return InMemoryStore.contenuti.stream()
                .filter(c -> c.getEsameId() == esameId)
                .map(c -> arricchisci(c, username))
                .collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    public Map<String, Object> getContenuto(@PathVariable int id,
            @RequestParam(value = "username", required = false) String username) {
        return InMemoryStore.contenuti.stream()
                .filter(c -> c.getId() == id)
                .findFirst()
                .map(c -> arricchisci(c, username))
                .orElse(Map.of("errore", "Contenuto non trovato"));
    }

    @PostMapping
    public Map<String, Object> creaContenuto(@RequestBody Map<String, Object> body) {
        String tipo = (String) body.get("tipo");
        String titolo = (String) body.get("titolo");
        String corpo = (String) body.get("corpo");
        String etichetta = (String) body.get("etichetta");
        String nomeFile = (String) body.get("nomeFile");
        String utente = (String) body.get("username");
        int esameId = (Integer) body.get("esameId");
        String data = LocalDateTime.now().format(FMT);

        Contenuto c = new Contenuto(InMemoryStore.nextId(), tipo, titolo, corpo,
                etichetta, nomeFile, utente, esameId, data);
        InMemoryStore.contenuti.add(c);
        return arricchisci(c, utente);
    }
}
