package com.studyhub.controllers;

import tools.jackson.databind.ObjectMapper;
import com.studyhub.models.*;
import com.studyhub.repositories.*;
import com.studyhub.security.PromozioneModeratori;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class TestPromozioneModeratori {

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
    private PasswordEncoder passwordEncoder;

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

    private RuoloUtente ruoloDi(String username) {
        return utenteRepository.findByUsername(username).orElseThrow().getRuolo();
    }

    @Test
    public void testPromozione_soloGliUtentiElencati() {
        creaUtente("futuroModeratore");
        creaUtente("restaUtente");

        // Spazi, voci vuote e username inesistenti non devono bloccare l'avvio
        new PromozioneModeratori(utenteRepository, List.of(" futuroModeratore ", "", "nonEsiste")).run(null);

        assertEquals(RuoloUtente.MODERATORE, ruoloDi("futuroModeratore"));
        assertEquals(RuoloUtente.UTENTE, ruoloDi("restaUtente"));
    }

    @Test
    public void testPromozione_ilRuoloValeAlLoginSuccessivo() throws Exception {
        creaUtente("nuovoModeratore");
        new PromozioneModeratori(utenteRepository, List.of("nuovoModeratore")).run(null);

        MockHttpSession sessione = (MockHttpSession) mockMvc.perform(post("/api/auth/login").with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("username", "nuovoModeratore", "password", PASSWORD))))
                .andExpect(status().isOk())
                .andReturn().getRequest().getSession(false);

        mockMvc.perform(get("/api/moderatore/segnalazioni").session(sessione))
                .andExpect(status().isOk());
    }
}
