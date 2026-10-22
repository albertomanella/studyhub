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

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
public class TestValutazioniController {

    @Autowired
    private ValutazioniController valutazioniController;
    @Autowired
    private UtenteRepository utenteRepository;
    @Autowired
    private AteneoRepository ateneoRepository;
    @Autowired
    private CorsoDiStudioRepository corsoDiStudioRepository;
    @Autowired
    private EsameRepository esameRepository;
    @Autowired
    private EtichettaRepository etichettaRepository;
    @Autowired
    private ContenutoRepository contenutoRepository;
    @Autowired
    private ValutazioneRepository valutazioneRepository;
    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    private CorsoDiStudio corso;
    private Esame esame;

    @BeforeEach
    public void setUp() {
        Ateneo ateneo = ateneoRepository.save(new Ateneo("UniBo"));
        corso = new CorsoDiStudio("IngegneriaInformatica", ateneo);
        corsoDiStudioRepository.save(corso);

        esame = new Esame("SistemiInformativi", 2, corso);
        esameRepository.save(esame);
    }

    private Utente creaUtente(String username) {
        Utente u = new Utente(username, passwordEncoder.encode("Password!23"), RuoloUtente.UTENTE,
                "Nome", "Cognome", username + "@studio.unibo.it", List.of(corso));
        return utenteRepository.save(u);
    }

    @Test
    public void testValutazione_ripetutaDalloStessoUtente_sovrascriveNonDuplica() {
        // R10NF/ValutaContenuto (pag. 21): "Il sistema consente di sostituire la
        // valutazione precedente" -- non crea un secondo voto per la stessa coppia
        // utente-contenuto.
        Utente autore = creaUtente("autoreVal");
        Utente valutatore = creaUtente("valutatore");
        Etichetta etichetta = etichettaRepository.save(new Etichetta("test-etichetta"));
        Contenuto contenuto = contenutoRepository.save(
                new Consiglio(autore, esame, "Contenuto da valutare", etichetta));

        ResponseEntity<Map<String, Object>> prima = valutazioniController.valuta(
                Map.of("username", valutatore.getUsername(), "contenutoId", contenuto.getId(), "stelle", 2));
        assertEquals(HttpStatus.OK, prima.getStatusCode());
        assertEquals(2.0, (double) prima.getBody().get("nuovaMedia"));

        ResponseEntity<Map<String, Object>> seconda = valutazioniController.valuta(
                Map.of("username", valutatore.getUsername(), "contenutoId", contenuto.getId(), "stelle", 5));
        assertEquals(HttpStatus.OK, seconda.getStatusCode());
        assertEquals(5.0, (double) seconda.getBody().get("nuovaMedia"));

        assertEquals(1, valutazioneRepository.countByContenuto(contenuto));
    }

    @Test
    public void testValutazione_fuoriRange_ritorna400() {
        Utente autore = creaUtente("autoreVal2");
        Utente valutatore = creaUtente("valutatore2");
        Etichetta etichetta = etichettaRepository.save(new Etichetta("test-etichetta"));
        Contenuto contenuto = contenutoRepository.save(
                new Consiglio(autore, esame, "Contenuto da valutare", etichetta));

        ResponseEntity<Map<String, Object>> risposta = valutazioniController.valuta(
                Map.of("username", valutatore.getUsername(), "contenutoId", contenuto.getId(), "stelle", 9));

        assertEquals(HttpStatus.BAD_REQUEST, risposta.getStatusCode());
        assertEquals(0, valutazioneRepository.countByContenuto(contenuto));
    }

    @Test
    public void testValutazione_propioContenuto_ritorna403() {
        // R10NF: "L'utente non può valutare i propri contenuti"
        Utente autore = creaUtente("autoreVal3");
        Etichetta etichetta = etichettaRepository.save(new Etichetta("test-etichetta"));
        Contenuto contenuto = contenutoRepository.save(
                new Consiglio(autore, esame, "Contenuto proprio", etichetta));

        ResponseEntity<Map<String, Object>> risposta = valutazioniController.valuta(
                Map.of("username", autore.getUsername(), "contenutoId", contenuto.getId(), "stelle", 4));

        assertEquals(HttpStatus.FORBIDDEN, risposta.getStatusCode());
    }
}
