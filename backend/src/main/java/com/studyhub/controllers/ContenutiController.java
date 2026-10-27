package com.studyhub.controllers;

import com.studyhub.dto.ContenutoDTO;
import com.studyhub.models.*;
import com.studyhub.repositories.*;
import com.studyhub.security.UtenteCorrente;
import com.studyhub.services.ContenutoService;
import com.studyhub.storage.FileNonAmmessoException;
import com.studyhub.storage.FileStorage;
import com.studyhub.storage.UploadValidator;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/contenuti")
public class ContenutiController {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

    private final ContenutoRepository contenutoRepository;
    private final EsameRepository esameRepository;
    private final UtenteCorrente utenteCorrente;
    private final EtichettaRepository etichettaRepository;
    private final ValutazioneRepository valutazioneRepository;
    private final ContenutoService contenutoService;
    private final FileStorage fileStorage;
    private final UploadValidator uploadValidator;

    public ContenutiController(ContenutoRepository contenutoRepository, EsameRepository esameRepository,
            UtenteCorrente utenteCorrente, EtichettaRepository etichettaRepository,
            ValutazioneRepository valutazioneRepository, ContenutoService contenutoService,
            FileStorage fileStorage, UploadValidator uploadValidator) {
        this.contenutoRepository = contenutoRepository;
        this.esameRepository = esameRepository;
        this.utenteCorrente = utenteCorrente;
        this.etichettaRepository = etichettaRepository;
        this.valutazioneRepository = valutazioneRepository;
        this.contenutoService = contenutoService;
        this.fileStorage = fileStorage;
        this.uploadValidator = uploadValidator;
    }

    private ContenutoDTO toDTO(Contenuto c) {
        String tipo;
        String titolo = null, descrizione = null, nomeFile = null;
        String corpo = null, etichetta = null;

        if (c instanceof Documento d) {
            tipo = "documento";
            titolo = d.getTitolo();
            descrizione = d.getDescrizione();
            // Il nome su disco resta interno al backend
            nomeFile = d.getNomeOriginale();
        } else if (c instanceof Consiglio con) {
            tipo = "consiglio";
            corpo = con.getCorpo();
            etichetta = con.getEtichetta().getNome();
        } else {
            tipo = "sconosciuto";
        }

        Double media = valutazioneRepository.mediaStelle(c);
        int numValutazioni = ((int) valutazioneRepository.countByContenuto(c));

        return new ContenutoDTO(
                c.getId(),
                tipo,
                c.getUtente().getUsername(),
                c.getDataCreazione().format(FMT),
                titolo, descrizione, nomeFile,
                corpo, etichetta,
                media != null ? media : 0.0,
                numValutazioni);
    }

    @GetMapping
    public ResponseEntity<?> getContenuti(@RequestParam("esame_id") int esameId) {
        return esameRepository.findById(esameId)
                .map(esame -> {
                    List<ContenutoDTO> dto = contenutoRepository.findByEsame(esame).stream()
                            .map(this::toDTO)
                            .collect(Collectors.toList());
                    return ResponseEntity.ok(dto);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getContenuto(@PathVariable int id) {
        return contenutoRepository.findById(id)
                .map(c -> ResponseEntity.ok(toDTO(c)))
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/documento")
    public ResponseEntity<?> creaDocumento(
            @RequestParam("titolo") String titolo,
            @RequestParam("descrizione") String descrizione,
            @RequestParam("esameId") int esameId,
            @RequestParam("file") MultipartFile file,
            Authentication authentication) throws IOException {

        Utente utente = utenteCorrente.da(authentication);
        Esame esame = esameRepository.findById(esameId).orElse(null);
        if (esame == null) {
            return ResponseEntity.badRequest().body(Map.of("errore", "Esame non trovato"));
        }

        String nomeOriginale = FileStorage.nomeOriginale(file.getOriginalFilename());
        try (InputStream contenuto = file.getInputStream()) {
            uploadValidator.verifica(nomeOriginale, contenuto);
        } catch (FileNonAmmessoException e) {
            return ResponseEntity.badRequest().body(Map.of("errore", e.getMessage()));
        }

        String nomeSalvato = fileStorage.salva(file);
        Documento doc = new Documento(utente, esame, titolo, descrizione, nomeSalvato, nomeOriginale);
        try {
            contenutoRepository.save(doc);
        } catch (RuntimeException e) {
            // Senza la riga nel database il file non sarebbe raggiungibile
            fileStorage.elimina(nomeSalvato);
            throw e;
        }
        return ResponseEntity.ok(toDTO(doc));
    }

    @PostMapping("/consiglio")
    public ResponseEntity<?> creaConsiglio(@RequestBody Map<String, Object> body, Authentication authentication) {
        String corpo = (String) body.get("corpo");
        int esameId = (Integer) body.get("esameId");
        int etichettaId = (Integer) body.get("etichettaId");

        Utente utente = utenteCorrente.da(authentication);
        Esame esame = esameRepository.findById(esameId).orElse(null);
        Etichetta etichetta = etichettaRepository.findById(etichettaId).orElse(null);
        if (esame == null || etichetta == null) {
            return ResponseEntity.badRequest().body(Map.of("errore", "Esame o etichetta non trovati"));
        }

        Consiglio consiglio = new Consiglio(utente, esame, corpo, etichetta);
        contenutoRepository.save(consiglio);
        return ResponseEntity.ok(toDTO(consiglio));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminaContenuto(@PathVariable int id, Authentication authentication) {
        Contenuto c = contenutoRepository.findById(id).orElse(null);
        if (c == null) {
            return ResponseEntity.notFound().build();
        }
        Utente utente = utenteCorrente.da(authentication);
        if (!c.getUtente().getId().equals(utente.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("errore", "Puoi eliminare solo i tuoi contenuti"));
        }
        contenutoService.elimina(c);
        return ResponseEntity.ok(Map.of("successo", true));
    }

    // Il file viene inviato a blocchi direttamente dal disco (Resource), senza
    // caricarlo in memoria. Sempre come download: il browser non lo mostra né lo
    // esegue, e Spring Security aggiunge "X-Content-Type-Options: nosniff".
    @GetMapping("/{id}/file")
    public ResponseEntity<Resource> downloadFile(@PathVariable int id) {
        Contenuto c = contenutoRepository.findById(id).orElse(null);
        if (!(c instanceof Documento doc)) {
            return ResponseEntity.notFound().build();
        }
        Resource file = fileStorage.apri(doc.getNomeFile());
        if (file == null) {
            return ResponseEntity.notFound().build();
        }
        ContentDisposition disposizione = ContentDisposition.attachment()
                .filename(doc.getNomeOriginale(), StandardCharsets.UTF_8)
                .build();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, disposizione.toString())
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(file);
    }
}
