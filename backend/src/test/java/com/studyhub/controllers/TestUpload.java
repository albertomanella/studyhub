package com.studyhub.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.studyhub.models.*;
import com.studyhub.repositories.*;
import com.studyhub.storage.FileStorage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

// I file di questi test vanno in una cartella separata, mai in quella reale
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@TestPropertySource(properties = "app.upload.dir=target/test-uploads")
public class TestUpload {

    private static final byte[] PDF = "%PDF-1.4 contenuto di prova".getBytes(StandardCharsets.US_ASCII);
    // Un eseguibile Windows inizia con "MZ" e contiene byte a zero
    private static final byte[] ESEGUIBILE = { 'M', 'Z', (byte) 0x90, 0, 3, 0, 0, 0 };

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private FileStorage fileStorage;
    @Autowired
    private UtenteRepository utenteRepository;
    @Autowired
    private AteneoRepository ateneoRepository;
    @Autowired
    private CorsoDiStudioRepository corsoDiStudioRepository;
    @Autowired
    private EsameRepository esameRepository;
    @Autowired
    private ContenutoRepository contenutoRepository;
    @Autowired
    private SegnalazioneRepository segnalazioneRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;

    private CorsoDiStudio corso;
    private Esame esame;

    @BeforeEach
    public void setUp() {
        Ateneo ateneo = ateneoRepository.save(new Ateneo("UniBo"));
        corso = new CorsoDiStudio("IngegneriaInformatica", ateneo);
        corsoDiStudioRepository.save(corso);
        esame = esameRepository.save(new Esame("SistemiInformativi", 2, corso));
        creaUtente("autore", RuoloUtente.UTENTE);
    }

    private void creaUtente(String username, RuoloUtente ruolo) {
        utenteRepository.save(new Utente(username, passwordEncoder.encode("Password!23"), ruolo,
                "Nome", "Cognome", username + "@studio.unibo.it", List.of(corso)));
    }

    private ResultActions carica(String nomeFile, byte[] contenuto) throws Exception {
        return mockMvc.perform(multipart("/api/contenuti/documento")
                .file(new MockMultipartFile("file", nomeFile, "application/octet-stream", contenuto))
                .param("titolo", "Appunti")
                .param("descrizione", "")
                .param("esameId", esame.getId().toString())
                .with(user("autore").roles("UTENTE")).with(csrf()));
    }

    private int idDi(MvcResult risultato) throws Exception {
        return objectMapper.readTree(risultato.getResponse().getContentAsString()).get("id").asInt();
    }

    private Documento documento(int id) {
        return (Documento) contenutoRepository.findById(id).orElseThrow();
    }

    private long fileNellaCartella() throws Exception {
        try (Stream<Path> file = Files.list(fileStorage.cartella())) {
            return file.count();
        }
    }

    @Test
    public void testPdf_salvatoConNomeGenerato() throws Exception {
        MvcResult risultato = carica("Appunti Analisi.pdf", PDF)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nomeFile").value("Appunti Analisi.pdf"))
                .andReturn();

        String nomeSuDisco = documento(idDi(risultato)).getNomeFile();
        assertTrue(nomeSuDisco.matches("[0-9a-f-]{36}"), "nome su disco: " + nomeSuDisco);
        assertArrayEquals(PDF, Files.readAllBytes(fileStorage.cartella().resolve(nomeSuDisco)));
    }

    @Test
    public void testEseguibileRinominatoInPdf_rifiutato() throws Exception {
        long prima = fileNellaCartella();

        carica("appunti.pdf", ESEGUIBILE)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errore").value(containsString("non corrisponde")));

        assertEquals(prima, fileNellaCartella());
    }

    @Test
    public void testFileDiCodice_ammessoInQualsiasiLinguaggio() throws Exception {
        // Nessuna lista di linguaggi: basta che sia testo, anche senza estensione
        carica("main.zig", "const std = @import(\"std\");".getBytes(StandardCharsets.UTF_8))
                .andExpect(status().isOk());
        carica("Makefile", "all:\n\tgcc main.c\n".getBytes(StandardCharsets.UTF_8))
                .andExpect(status().isOk());
    }

    @Test
    public void testFileBinarioSconosciuto_rifiutato() throws Exception {
        carica("programma.exe", ESEGUIBILE).andExpect(status().isBadRequest());
        carica("dati.bin", new byte[] { 1, 2, 0, 4 }).andExpect(status().isBadRequest());
    }

    @Test
    public void testFileVuoto_rifiutato() throws Exception {
        carica("vuoto.txt", new byte[0])
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errore").value(containsString("vuoto")));
    }

    @Test
    public void testNomeConPercorso_restaNellaCartella() throws Exception {
        MvcResult risultato = carica("../../segreto.txt", "testo".getBytes(StandardCharsets.UTF_8))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nomeFile").value("segreto.txt"))
                .andReturn();

        String nomeSuDisco = documento(idDi(risultato)).getNomeFile();
        assertTrue(Files.exists(fileStorage.cartella().resolve(nomeSuDisco)));
    }

    @Test
    public void testDownload_contenutoENomeOriginale() throws Exception {
        byte[] testo = "Appunti della lezione".getBytes(StandardCharsets.UTF_8);
        int id = idDi(carica("Lezione è 1.txt", testo).andExpect(status().isOk()).andReturn());

        mockMvc.perform(get("/api/contenuti/{id}/file", id).with(user("autore").roles("UTENTE")))
                .andExpect(status().isOk())
                .andExpect(content().bytes(testo))
                .andExpect(header().string("Content-Disposition", containsString("attachment")))
                .andExpect(header().string("Content-Disposition",
                        containsString("filename*=UTF-8''Lezione%20%C3%A8%201.txt")))
                .andExpect(header().string("X-Content-Type-Options", "nosniff"));
    }

    @Test
    public void testEliminazione_rimuoveIlFileDalDisco() throws Exception {
        int id = idDi(carica("da-eliminare.pdf", PDF).andExpect(status().isOk()).andReturn());
        Path file = fileStorage.cartella().resolve(documento(id).getNomeFile());
        assertTrue(Files.exists(file));

        mockMvc.perform(delete("/api/contenuti/{id}", id).with(user("autore").roles("UTENTE")).with(csrf()))
                .andExpect(status().isOk());

        assertFalse(Files.exists(file));
    }

    @Test
    public void testSegnalazioneAccolta_rimuoveIlFileDalDisco() throws Exception {
        creaUtente("segnalante", RuoloUtente.UTENTE);
        creaUtente("moderatore", RuoloUtente.MODERATORE);
        int id = idDi(carica("segnalato.pdf", PDF).andExpect(status().isOk()).andReturn());
        Path file = fileStorage.cartella().resolve(documento(id).getNomeFile());

        mockMvc.perform(post("/api/segnalazioni").with(user("segnalante").roles("UTENTE")).with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("idContenuto", String.valueOf(id), "motivazione", "x"))))
                .andExpect(status().isCreated());
        Integer idSegnalazione = segnalazioneRepository.findAll().stream()
                .filter(s -> s.getContenuto().getId().equals(id))
                .findFirst().orElseThrow().getId();

        mockMvc.perform(patch("/api/moderatore/segnalazioni/{id}", idSegnalazione)
                .with(user("moderatore").roles("MODERATORE")).with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("esito", "accolta"))))
                .andExpect(status().isOk());

        assertFalse(Files.exists(file));
    }
}
