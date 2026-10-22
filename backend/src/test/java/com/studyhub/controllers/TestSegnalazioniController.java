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

// Stesso impianto dei test unitari proposti nel documento di progettazione
// originale (Piano del collaudo, pag. 69-70 e 96-97): un setUp() che costruisce
// a mano Ateneo -> CorsoDiStudio -> Esame -> Utente, e chiamate dirette ai
// metodi del controller, con verifica del risultato. L'unica differenza reale
// è che qui non esiste un DbMock: il controller è quello vero, autowired da
// Spring, sul database Postgres di sviluppo; ogni test è avvolto in una
// transazione che viene sempre annullata (@Transactional), quindi non lascia
// dati residui, come farebbe invece un DbMock ricreato ad ogni @BeforeEach.
@SpringBootTest
@Transactional
public class TestSegnalazioniController {

    @Autowired
    private SegnalazioniController segnalazioniController;
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
    private BCryptPasswordEncoder passwordEncoder;

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

    private Segnalazione accogliSegnalazione(Utente segnalante, Contenuto contenuto, Utente moderatore) {
        Map<String, String> segnalazioneBody = Map.of(
                "idContenuto", contenuto.getId().toString(),
                "usernameSegnalante", segnalante.getUsername(),
                "motivazione", "Contenuto inadeguato");
        segnalazioniController.segnala(segnalazioneBody);

        Segnalazione segnalazione = segnalazioneRepository.findAll().stream()
                .filter(s -> s.getUtente().getId().equals(segnalante.getId())
                        && s.getContenuto().getId().equals(contenuto.getId()))
                .findFirst().orElseThrow();

        Map<String, String> gestioneBody = Map.of(
                "usernameModeratore", moderatore.getUsername(),
                "esito", "accolta");
        segnalazioniController.gestisciSegnalazione(segnalazione.getId(), gestioneBody);
        return segnalazione;
    }

    @Test
    public void testSegnalazioneDuplicata_ritorna409() {
        Utente autore = creaUtente("autore1", RuoloUtente.UTENTE);
        Utente segnalante = creaUtente("segnalante1", RuoloUtente.UTENTE);
        Contenuto contenuto = creaConsiglioDi(autore);

        Map<String, String> body = Map.of(
                "idContenuto", contenuto.getId().toString(),
                "usernameSegnalante", segnalante.getUsername(),
                "motivazione", "Contenuto inadeguato");

        ResponseEntity<Map<String, Object>> prima = segnalazioniController.segnala(body);
        assertEquals(HttpStatus.CREATED, prima.getStatusCode());

        ResponseEntity<Map<String, Object>> seconda = segnalazioniController.segnala(body);
        assertEquals(HttpStatus.CONFLICT, seconda.getStatusCode());

        assertEquals(1, segnalazioneRepository.count());
    }

    @Test
    public void testSospensioneAutomatica_scattaAlTerzoAvvertimento_nonPrima() {
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
    public void testSospensioneAutomatica_siRinnovaAncheAlQuartoAvvertimento() {
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
    public void testSospensioneManuale_suUtenteGiaSospeso_ritorna409() {
        Utente moderatore = creaUtente("mod3", RuoloUtente.MODERATORE);
        Utente utente = creaUtente("utenteSospeso", RuoloUtente.UTENTE);
        sospensioneRepository.save(new Sospensione(utente, "Sospensione precedente", LocalDate.now().plusDays(3)));

        Map<String, Object> body = Map.of(
                "usernameModeratore", moderatore.getUsername(),
                "usernameUtente", utente.getUsername(),
                "motivazione", "Comportamento scorretto",
                "giorni", 5);

        ResponseEntity<Map<String, Object>> risposta = segnalazioniController.sospendiUtente(body);

        assertEquals(HttpStatus.CONFLICT, risposta.getStatusCode());
        assertEquals(1, sospensioneRepository.count());
    }

    @Test
    public void testSospensioneManuale_suSospensioneScaduta_rinnovaStessaRiga() {
        Utente moderatore = creaUtente("mod4", RuoloUtente.MODERATORE);
        Utente utente = creaUtente("utenteRecidivo", RuoloUtente.UTENTE);
        sospensioneRepository.save(new Sospensione(utente, "Sospensione scaduta", LocalDate.now().minusDays(1)));

        Map<String, Object> body = Map.of(
                "usernameModeratore", moderatore.getUsername(),
                "usernameUtente", utente.getUsername(),
                "motivazione", "Nuova infrazione",
                "giorni", 5);

        ResponseEntity<Map<String, Object>> risposta = segnalazioniController.sospendiUtente(body);

        assertEquals(HttpStatus.OK, risposta.getStatusCode());
        // Il vincolo di unicità su utente_id impedirebbe comunque una seconda
        // riga: qui verifichiamo che il comportamento sia il rinnovo voluto,
        // non solo che non sia esplosa un'eccezione di violazione del vincolo.
        assertEquals(1, sospensioneRepository.count());
        assertEquals(LocalDate.now().plusDays(5),
                sospensioneRepository.findByUtente(utente).orElseThrow().getDataFine());
    }
}
