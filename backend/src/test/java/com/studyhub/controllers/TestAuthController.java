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
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// Le richieste passano dall'intera catena di Spring Security (sessione, CSRF,
// regole di accesso) come quelle del browser; ogni test viene annullato a fine
// esecuzione (@Transactional) e non lascia dati nel database di sviluppo.
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class TestAuthController {

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
    private SospensioneRepository sospensioneRepository;
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

    private MvcResult login(String username, String password) throws Exception {
        return mockMvc.perform(post("/api/auth/login").with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("username", username, "password", password))))
                .andReturn();
    }

    private MockHttpSession sessione(MvcResult risultato) {
        return (MockHttpSession) risultato.getRequest().getSession(false);
    }

    @Test
    public void testLogin_utenteNonSospeso_creaSessione() throws Exception {
        creaUtente("utenteAttivo");

        MvcResult risultato = login("utenteAttivo", PASSWORD);

        assertEquals(200, risultato.getResponse().getStatus());
        MockHttpSession sessione = sessione(risultato);
        assertNotNull(sessione);
        mockMvc.perform(get("/api/auth/me").session(sessione))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("utenteAttivo"));
    }

    @Test
    public void testLogin_utenteConSospensioneAttiva_bloccato() throws Exception {
        Utente utente = creaUtente("utenteSospeso");
        sospensioneRepository.save(new Sospensione(utente, "Sospensione automatica", LocalDate.now().plusDays(4)));

        MvcResult risultato = login("utenteSospeso", PASSWORD);

        assertEquals(403, risultato.getResponse().getStatus());
        assertTrue(risultato.getResponse().getContentAsString().toLowerCase().contains("sospeso"));
        // Nessuna sessione autenticata per l'utente sospeso
        MockHttpSession sessione = sessione(risultato);
        if (sessione != null) {
            mockMvc.perform(get("/api/auth/me").session(sessione)).andExpect(status().isUnauthorized());
        }
    }

    @Test
    public void testLogin_utenteConSospensioneScaduta_consentito() throws Exception {
        // La sospensione è "stato corrente", non storico: una volta scaduta non
        // deve più bloccare l'accesso, anche se la riga resta a DB in attesa di
        // un'eventuale prossima sospensione.
        Utente utente = creaUtente("utenteExSospeso");
        sospensioneRepository.save(new Sospensione(utente, "Sospensione ormai scaduta", LocalDate.now().minusDays(1)));

        assertEquals(200, login("utenteExSospeso", PASSWORD).getResponse().getStatus());
    }

    @Test
    public void testLogin_credenzialiErrate_stessoErrorePerPasswordEUsername() throws Exception {
        creaUtente("utenteReale");

        MvcResult passwordErrata = login("utenteReale", "sbagliata");
        MvcResult usernameInesistente = login("nessuno", PASSWORD);

        assertEquals(401, passwordErrata.getResponse().getStatus());
        assertEquals(401, usernameInesistente.getResponse().getStatus());
        assertEquals(passwordErrata.getResponse().getContentAsString(),
                usernameInesistente.getResponse().getContentAsString());
    }

    @Test
    public void testLogin_senzaTokenCsrf_rifiutato() throws Exception {
        creaUtente("utenteCsrf");

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("username", "utenteCsrf", "password", PASSWORD))))
                .andExpect(status().isForbidden());
    }

    @Test
    public void testRichiestaSenzaSessione_ritorna401() throws Exception {
        mockMvc.perform(get("/api/esami")).andExpect(status().isUnauthorized());
        mockMvc.perform(get("/api/auth/me")).andExpect(status().isUnauthorized());
    }

    @Test
    public void testLogout_invalidaLaSessione() throws Exception {
        creaUtente("utenteLogout");
        MockHttpSession sessione = sessione(login("utenteLogout", PASSWORD));

        mockMvc.perform(post("/api/auth/logout").session(sessione).with(csrf()))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/auth/me").session(sessione))
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void testSospensioneDuranteLaSessione_disconnetteLUtente() throws Exception {
        // L'utente fa login, poi un moderatore lo sospende mentre la sessione è aperta
        Utente utente = creaUtente("sospesoDopoLogin");
        MockHttpSession sessione = sessione(login("sospesoDopoLogin", PASSWORD));
        sospensioneRepository.save(new Sospensione(utente, "Comportamento scorretto", LocalDate.now().plusDays(7)));

        mockMvc.perform(get("/api/esami").session(sessione))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.codice").value("ACCOUNT_SOSPESO"))
                .andExpect(jsonPath("$.errore").value(containsString("sospeso")));

        // La sessione è stata distrutta: la richiesta successiva non è più autenticata
        mockMvc.perform(get("/api/auth/me").session(sessione))
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void testSospensioneScaduta_nonInterrompeLaSessione() throws Exception {
        Utente utente = creaUtente("exSospesoInSessione");
        MockHttpSession sessione = sessione(login("exSospesoInSessione", PASSWORD));
        sospensioneRepository.save(new Sospensione(utente, "Sospensione scaduta", LocalDate.now().minusDays(1)));

        mockMvc.perform(get("/api/esami").session(sessione))
                .andExpect(status().isOk());
    }
}
