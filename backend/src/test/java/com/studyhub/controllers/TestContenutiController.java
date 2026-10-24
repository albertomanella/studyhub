package com.studyhub.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.studyhub.models.*;
import com.studyhub.repositories.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class TestContenutiController {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
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
    private PasswordEncoder passwordEncoder;

    private CorsoDiStudio corso;
    private Esame esame;
    private Etichetta etichetta;

    @BeforeEach
    public void setUp() {
        Ateneo ateneo = ateneoRepository.save(new Ateneo("UniBo"));
        corso = new CorsoDiStudio("IngegneriaInformatica", ateneo);
        corsoDiStudioRepository.save(corso);
        esame = esameRepository.save(new Esame("SistemiInformativi", 2, corso));
        etichetta = etichettaRepository.save(new Etichetta("test-etichetta"));
    }

    private Utente creaUtente(String username) {
        Utente u = new Utente(username, passwordEncoder.encode("Password!23"), RuoloUtente.UTENTE,
                "Nome", "Cognome", username + "@studio.unibo.it", List.of(corso));
        return utenteRepository.save(u);
    }

    @Test
    public void testCreaConsiglio_autoreEUtenteDellaSessione_nonQuelloNelCorpo() throws Exception {
        creaUtente("autoreVero");
        creaUtente("vittima");

        // Un client malevolo prova a pubblicare a nome di un altro utente
        String corpo = objectMapper.writeValueAsString(Map.of(
                "corpo", "Consiglio di prova",
                "esameId", esame.getId(),
                "etichettaId", etichetta.getId(),
                "username", "vittima"));

        mockMvc.perform(post("/api/contenuti/consiglio")
                .with(user("autoreVero").roles("UTENTE")).with(csrf())
                .contentType(MediaType.APPLICATION_JSON).content(corpo))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.autore").value("autoreVero"));
    }

    @Test
    public void testCreaConsiglio_senzaSessione_ritorna401() throws Exception {
        String corpo = objectMapper.writeValueAsString(Map.of(
                "corpo", "Consiglio anonimo", "esameId", esame.getId(), "etichettaId", etichetta.getId()));

        mockMvc.perform(post("/api/contenuti/consiglio").with(csrf())
                .contentType(MediaType.APPLICATION_JSON).content(corpo))
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void testEliminaContenuto_altrui_ritorna403() throws Exception {
        Utente autore = creaUtente("autoreContenuto");
        creaUtente("altroUtente");
        Contenuto contenuto = contenutoRepository.save(new Consiglio(autore, esame, "Da non cancellare", etichetta));

        mockMvc.perform(delete("/api/contenuti/{id}", contenuto.getId())
                .with(user("altroUtente").roles("UTENTE")).with(csrf()))
                .andExpect(status().isForbidden());

        assertTrue(contenutoRepository.findById(contenuto.getId()).isPresent());
    }

    @Test
    public void testEliminaContenuto_proprio_consentito() throws Exception {
        Utente autore = creaUtente("autoreElimina");
        Contenuto contenuto = contenutoRepository.save(new Consiglio(autore, esame, "Da cancellare", etichetta));

        mockMvc.perform(delete("/api/contenuti/{id}", contenuto.getId())
                .with(user("autoreElimina").roles("UTENTE")).with(csrf()))
                .andExpect(status().isOk());

        assertFalse(contenutoRepository.findById(contenuto.getId()).isPresent());
    }

    @Test
    public void testEliminaContenuto_senzaTokenCsrf_rifiutato() throws Exception {
        // Simula una richiesta partita da un altro sito con il cookie di sessione
        // dell'utente ma senza il token CSRF
        Utente autore = creaUtente("autoreCsrf");
        Contenuto contenuto = contenutoRepository.save(new Consiglio(autore, esame, "Protetto", etichetta));

        mockMvc.perform(delete("/api/contenuti/{id}", contenuto.getId())
                .with(user("autoreCsrf").roles("UTENTE")))
                .andExpect(status().isForbidden());

        assertTrue(contenutoRepository.findById(contenuto.getId()).isPresent());
    }
}
