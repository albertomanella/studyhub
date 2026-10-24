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
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class TestValutazioniController {

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
    private ValutazioneRepository valutazioneRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;

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

    private ResultActions valuta(Utente valutatore, Contenuto contenuto, int stelle) throws Exception {
        return mockMvc.perform(post("/api/valutazioni")
                .with(user(valutatore.getUsername()).roles("UTENTE")).with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("contenutoId", contenuto.getId(), "stelle", stelle))));
    }

    @Test
    public void testValutazione_ripetutaDalloStessoUtente_sovrascriveNonDuplica() throws Exception {
        // R10NF/ValutaContenuto (pag. 21): "Il sistema consente di sostituire la
        // valutazione precedente" -- non crea un secondo voto per la stessa coppia
        // utente-contenuto.
        Utente autore = creaUtente("autoreVal");
        Utente valutatore = creaUtente("valutatore");
        Etichetta etichetta = etichettaRepository.save(new Etichetta("test-etichetta"));
        Contenuto contenuto = contenutoRepository.save(
                new Consiglio(autore, esame, "Contenuto da valutare", etichetta));

        valuta(valutatore, contenuto, 2)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nuovaMedia").value(2.0));

        valuta(valutatore, contenuto, 5)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nuovaMedia").value(5.0));

        assertEquals(1, valutazioneRepository.countByContenuto(contenuto));
    }

    @Test
    public void testValutazione_fuoriRange_ritorna400() throws Exception {
        Utente autore = creaUtente("autoreVal2");
        Utente valutatore = creaUtente("valutatore2");
        Etichetta etichetta = etichettaRepository.save(new Etichetta("test-etichetta"));
        Contenuto contenuto = contenutoRepository.save(
                new Consiglio(autore, esame, "Contenuto da valutare", etichetta));

        valuta(valutatore, contenuto, 9).andExpect(status().isBadRequest());

        assertEquals(0, valutazioneRepository.countByContenuto(contenuto));
    }

    @Test
    public void testValutazione_propioContenuto_ritorna403() throws Exception {
        // R10NF: "L'utente non può valutare i propri contenuti"
        Utente autore = creaUtente("autoreVal3");
        Etichetta etichetta = etichettaRepository.save(new Etichetta("test-etichetta"));
        Contenuto contenuto = contenutoRepository.save(
                new Consiglio(autore, esame, "Contenuto proprio", etichetta));

        valuta(autore, contenuto, 4).andExpect(status().isForbidden());
    }
}
