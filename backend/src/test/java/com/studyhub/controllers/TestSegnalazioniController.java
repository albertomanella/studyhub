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

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// Stesso impianto dei test unitari proposti nel documento di progettazione
// originale (Piano del collaudo, pag. 69-70 e 96-97): un setUp() che costruisce
// a mano Ateneo -> CorsoDiStudio -> Esame -> Utente, poi le operazioni da
// verificare. Le richieste passano dall'intera catena HTTP e di sicurezza
// (MockMvc), autenticate come un utente specifico; ogni test è avvolto in una
// transazione annullata a fine esecuzione (@Transactional).
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class TestSegnalazioniController {

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
    private SegnalazioneRepository segnalazioneRepository;
    @Autowired
    private SospensioneRepository sospensioneRepository;
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

        esame = new Esame("SistemiInformativi", 2, corso);
        esameRepository.save(esame);

        etichetta = etichettaRepository.save(new Etichetta("test-etichetta"));
    }

    private Utente creaUtente(String username, RuoloUtente ruolo) {
        Utente u = new Utente(username, passwordEncoder.encode("Password!23"), ruolo,
                "Nome", "Cognome", username + "@studio.unibo.it", List.of(corso));
        return utenteRepository.save(u);
    }

    private Contenuto creaConsiglioDi(Utente autore) {
        Consiglio c = new Consiglio(autore, esame, "Contenuto di prova", etichetta);
        return contenutoRepository.save(c);
    }

    private ResultActions segnala(Utente segnalante, Contenuto contenuto) throws Exception {
        return mockMvc.perform(post("/api/segnalazioni")
                .with(user(segnalante.getUsername()).roles(segnalante.getRuolo().name())).with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of(
                        "idContenuto", contenuto.getId().toString(),
                        "motivazione", "Contenuto inadeguato"))));
    }

    private ResultActions sospendi(Utente moderatore, Utente utente, int giorni) throws Exception {
        return mockMvc.perform(post("/api/moderatore/sospensioni")
                .with(user(moderatore.getUsername()).roles("MODERATORE")).with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of(
                        "usernameUtente", utente.getUsername(),
                        "motivazione", "Comportamento scorretto",
                        "giorni", giorni))));
    }

    private void accogliSegnalazione(Utente segnalante, Contenuto contenuto, Utente moderatore) throws Exception {
        segnala(segnalante, contenuto).andExpect(status().isCreated());

        Segnalazione segnalazione = segnalazioneRepository.findAll().stream()
                .filter(s -> s.getUtente().getId().equals(segnalante.getId())
                        && s.getContenuto().getId().equals(contenuto.getId()))
                .findFirst().orElseThrow();

        mockMvc.perform(patch("/api/moderatore/segnalazioni/{id}", segnalazione.getId())
                .with(user(moderatore.getUsername()).roles("MODERATORE")).with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("esito", "accolta"))))
                .andExpect(status().isOk());
    }

    @Test
    public void testSegnalazioneDuplicata_ritorna409() throws Exception {
        Utente autore = creaUtente("autore1", RuoloUtente.UTENTE);
        Utente segnalante = creaUtente("segnalante1", RuoloUtente.UTENTE);
        Contenuto contenuto = creaConsiglioDi(autore);

        segnala(segnalante, contenuto).andExpect(status().isCreated());
        segnala(segnalante, contenuto).andExpect(status().isConflict());

        assertEquals(1, segnalazioneRepository.count());
    }

    @Test
    public void testSegnalazione_daModeratore_ritorna403() throws Exception {
        Utente autore = creaUtente("autore2", RuoloUtente.UTENTE);
        Utente moderatore = creaUtente("modSegnala", RuoloUtente.MODERATORE);

        segnala(moderatore, creaConsiglioDi(autore)).andExpect(status().isForbidden());
    }

    @Test
    public void testEndpointModeratore_conRuoloUtente_ritorna403() throws Exception {
        Utente utente = creaUtente("utenteCurioso", RuoloUtente.UTENTE);
        Utente bersaglio = creaUtente("bersaglio", RuoloUtente.UTENTE);

        mockMvc.perform(get("/api/moderatore/segnalazioni")
                .with(user(utente.getUsername()).roles("UTENTE")))
                .andExpect(status().isForbidden());

        // Un utente normale non può sospendere nessuno, qualunque cosa mandi nel corpo
        mockMvc.perform(post("/api/moderatore/sospensioni")
                .with(user(utente.getUsername()).roles("UTENTE")).with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of(
                        "usernameUtente", bersaglio.getUsername(), "motivazione", "x", "giorni", 30))))
                .andExpect(status().isForbidden());
        assertTrue(sospensioneRepository.findByUtente(bersaglio).isEmpty());
    }

    @Test
    public void testSospensioneAutomatica_scattaAlTerzoAvvertimento_nonPrima() throws Exception {
        Utente moderatore = creaUtente("mod1", RuoloUtente.MODERATORE);
        Utente autore = creaUtente("autoreRischio", RuoloUtente.UTENTE);

        // Due segnalazioni accolte: dopo la seconda l'autore ha 2 avvertimenti,
        // R21F non prevede ancora nessuna sospensione.
        for (int i = 0; i < 2; i++) {
            Utente segnalante = creaUtente("segnalanteA" + i, RuoloUtente.UTENTE);
            Contenuto contenuto = creaConsiglioDi(autore);
            accogliSegnalazione(segnalante, contenuto, moderatore);
        }

        assertEquals(2, utenteRepository.findById(autore.getId()).orElseThrow()
                .getProfilo().getNumAvvertimenti());
        assertTrue(sospensioneRepository.findByUtente(autore).isEmpty());

        // Terza segnalazione accolta: terzo avvertimento, R21F impone la
        // sospensione per 1 settimana da qui in poi.
        Utente terzoSegnalante = creaUtente("segnalanteA2", RuoloUtente.UTENTE);
        Contenuto terzoContenuto = creaConsiglioDi(autore);
        accogliSegnalazione(terzoSegnalante, terzoContenuto, moderatore);

        Sospensione sospensione = sospensioneRepository.findByUtente(autore).orElseThrow();
        assertTrue(sospensione.isAttiva());
        assertEquals(LocalDate.now().plusDays(7), sospensione.getDataFine());
    }

    @Test
    public void testSospensioneAutomatica_siRinnovaAncheAlQuartoAvvertimento() throws Exception {
        // R21F: "ogni nuovo avvertimento, a partire dal terzo" -- non solo i
        // multipli di 3. Il quarto avvertimento deve rinnovare la sospensione,
        // non lasciarla invariata.
        Utente moderatore = creaUtente("mod2", RuoloUtente.MODERATORE);
        Utente autore = creaUtente("autoreRecidivo", RuoloUtente.UTENTE);

        for (int i = 0; i < 3; i++) {
            Utente segnalante = creaUtente("segnalanteB" + i, RuoloUtente.UTENTE);
            Contenuto contenuto = creaConsiglioDi(autore);
            accogliSegnalazione(segnalante, contenuto, moderatore);
        }
        Sospensione dopoTerzo = sospensioneRepository.findByUtente(autore).orElseThrow();
        LocalDate finePrimaSospensione = dopoTerzo.getDataFine();

        // Simula il passare di qualche giorno prima del quarto avvertimento,
        // così da poter distinguere la data di rinnovo da quella originale.
        dopoTerzo.rinnova(dopoTerzo.getMotivazione(), finePrimaSospensione.minusDays(2));
        sospensioneRepository.save(dopoTerzo);

        Utente quartoSegnalante = creaUtente("segnalanteB3", RuoloUtente.UTENTE);
        Contenuto quartoContenuto = creaConsiglioDi(autore);
        accogliSegnalazione(quartoSegnalante, quartoContenuto, moderatore);

        Sospensione dopoQuarto = sospensioneRepository.findByUtente(autore).orElseThrow();
        assertEquals(1, sospensioneRepository.count());
        assertEquals(LocalDate.now().plusDays(7), dopoQuarto.getDataFine());
        assertTrue(dopoQuarto.getMotivazione().contains("4"));
    }

    @Test
    public void testSospensioneManuale_suUtenteGiaSospeso_ritorna409() throws Exception {
        Utente moderatore = creaUtente("mod3", RuoloUtente.MODERATORE);
        Utente utente = creaUtente("utenteSospeso", RuoloUtente.UTENTE);
        sospensioneRepository.save(new Sospensione(utente, "Sospensione precedente", LocalDate.now().plusDays(3)));

        sospendi(moderatore, utente, 5).andExpect(status().isConflict());

        assertEquals(1, sospensioneRepository.count());
    }

    @Test
    public void testSospensioneManuale_suSospensioneScaduta_rinnovaStessaRiga() throws Exception {
        Utente moderatore = creaUtente("mod4", RuoloUtente.MODERATORE);
        Utente utente = creaUtente("utenteRecidivo", RuoloUtente.UTENTE);
        sospensioneRepository.save(new Sospensione(utente, "Sospensione scaduta", LocalDate.now().minusDays(1)));

        sospendi(moderatore, utente, 5).andExpect(status().isOk());

        // Il vincolo di unicità su utente_id impedirebbe comunque una seconda
        // riga: qui verifichiamo che il comportamento sia il rinnovo voluto,
        // non solo che non sia esplosa un'eccezione di violazione del vincolo.
        assertEquals(1, sospensioneRepository.count());
        assertEquals(LocalDate.now().plusDays(5),
                sospensioneRepository.findByUtente(utente).orElseThrow().getDataFine());
    }
}
