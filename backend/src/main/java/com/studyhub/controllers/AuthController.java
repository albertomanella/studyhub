package com.studyhub.controllers;

import com.studyhub.models.Utente;
import com.studyhub.storage.InMemoryStore;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @PostMapping("/register")
    public Map<String, Object> register(@RequestBody Map<String, String> body) {
        String username = body.get("username");
        String password = body.get("password");
        String nome = body.get("nome");
        String cognome = body.get("cognome");

        boolean exists = InMemoryStore.utenti.stream()
                .anyMatch(u -> u.getUsername().equals(username));
        if (exists) {
            return Map.of("errore", "Username già esistente");
        }

        Utente u = new Utente(InMemoryStore.nextId(), username, password, nome, cognome, "utente");
        InMemoryStore.utenti.add(u);
        return Map.of("successo", true);
    }

    @PostMapping("/login")
    public Map<String, Object> login(@RequestBody Map<String, String> body) {
        String username = body.get("username");
        String password = body.get("password");

        return InMemoryStore.utenti.stream()
                .filter(u -> u.getUsername().equals(username) && u.getPassword().equals(password))
                .findFirst()
                .<Map<String, Object>>map(u -> {
                    Map<String, Object> r = new HashMap<>();
                    r.put("successo", true);
                    r.put("username", u.getUsername());
                    r.put("nome", u.getNome());
                    r.put("cognome", u.getCognome());
                    r.put("ruolo", u.getRuolo());
                    return r;
                })
                .orElse(Map.of("errore", "Credenziali non valide"));
    }
}
