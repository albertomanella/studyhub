package com.studyhub.controllers;

import com.studyhub.models.*;
import com.studyhub.repositories.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
public class TestAuthController {

    @Autowired
    private AuthController authController;
    @Autowired
    private UtenteRepository utenteRepository;
    @Autowired
    private AteneoRepository ateneoRepository;
    @Autowired
    private CorsoDiStudioRepository corsoDiStudioRepository;
    @Autowired
    private SospensioneRepository sospensioneRepository;
    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    private static final String PASSWORD = "Password!23";

    private CorsoDiStudio corso;

    @BeforeEach
    public void setUp() {
        Ateneo ateneo = ateneoRepository.save(new Ateneo("UniBo"));
        corso = new CorsoDiStudio("IngegneriaInformatica", ateneo);
        corsoDiStudioRepository.save(corso);
    }

    private Utente creaUtente(String username) {
        Utente u = new Utente(username, passwordEncoder.encode(PASSWORD), RuoloUtente.UTENTE,
                "Nome", "Cognome", username + "@studio.unibo.it", List.of(corso));
        return utenteRepository.save(u);
    }

    @Test
    public void testLogin_utenteNonSospeso_consentito() {
        creaUtente("utenteAttivo");

        ResponseEntity<?> risposta = authController.login(Map.of("username", "utenteAttivo", "password", PASSWORD));

        assertEquals(HttpStatus.OK, risposta.getStatusCode());
    }

    @Test
    public void testLogin_utenteConSospensioneAttiva_bloccato() {
        Utente utente = creaUtente("utenteSospeso");
        sospensioneRepository.save(new Sospensione(utente, "Sospensione automatica", LocalDate.now().plusDays(4)));

        ResponseEntity<?> risposta = authController.login(Map.of("username", "utenteSospeso", "password", PASSWORD));

        assertEquals(HttpStatus.FORBIDDEN, risposta.getStatusCode());
        @SuppressWarnings("unchecked")
        Map<String, Object> body = (Map<String, Object>) risposta.getBody();
        assertTrue(body.get("errore").toString().toLowerCase().contains("sospeso"));
    }

    @Test
    public void testLogin_utenteConSospensioneScaduta_consentito() {
        // La sospensione è "stato corrente", non storico: una volta scaduta non
        // deve più bloccare l'accesso, anche se la riga resta a DB in attesa di
        // un'eventuale prossima sospensione.
        Utente utente = creaUtente("utenteExSospeso");
        sospensioneRepository.save(new Sospensione(utente, "Sospensione ormai scaduta", LocalDate.now().minusDays(1)));

        ResponseEntity<?> risposta = authController.login(Map.of("username", "utenteExSospeso", "password", PASSWORD));

        assertEquals(HttpStatus.OK, risposta.getStatusCode());
    }
}
