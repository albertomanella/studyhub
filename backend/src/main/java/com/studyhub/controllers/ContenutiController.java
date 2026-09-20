package com.studyhub.controllers;

import com.studyhub.dto.ContenutoDTO;
import com.studyhub.models.*;
import com.studyhub.repositories.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/contenuti")
public class ContenutiController {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

    private final ContenutoRepository contenutoRepository;
    private final EsameRepository esameRepository;
    private final UtenteRepository utenteRepository;
    private final EtichettaRepository etichettaRepository;
    private final ValutazioneRepository valutazioneRepository;

    @org.springframework.beans.factory.annotation.Value("${app.upload.dir}")
    private String uploadDir;

    public ContenutiController(ContenutoRepository contenutoRepository, EsameRepository esameRepository,
            UtenteRepository utenteRepository, EtichettaRepository etichettaRepository,
            ValutazioneRepository valutazioneRepository) {
        this.contenutoRepository = contenutoRepository;
        this.esameRepository = esameRepository;
        this.utenteRepository = utenteRepository;
        this.etichettaRepository = etichettaRepository;
        this.valutazioneRepository = valutazioneRepository;
    }

    private ContenutoDTO toDTO(Contenuto c) {
        String tipo;
        String titolo = null, descrizione = null, nomeFile = null;
        String corpo = null, etichetta = null;

        if (c instanceof Documento d) {
            tipo = "documento";
            titolo = d.getTitolo();
            descrizione = d.getDescrizione();
            nomeFile = d.getNomeFile();
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
            @RequestParam("username") String username,
            @RequestParam("file") MultipartFile file) throws IOException {

        Esame esame = esameRepository.findById(esameId).orElse(null);
        Utente utente = utenteRepository.findByUsername(username).orElse(null);
        if (esame == null || utente == null) {
            return ResponseEntity.badRequest().body(Map.of("errore", "Esame o utente non trovato"));
        }

        String originale = file.getOriginalFilename() != null ? file.getOriginalFilename() : "file";
        String nomeSalvato = UUID.randomUUID() + "_" + originale;

        Path dir = Path.of(uploadDir);
        Files.createDirectories(dir);
        Files.write(dir.resolve(nomeSalvato), file.getBytes());

        Documento doc = new Documento(utente, esame, titolo, descrizione, nomeSalvato);
        contenutoRepository.save(doc);
        return ResponseEntity.ok(toDTO(doc));
    }

    @PostMapping("/consiglio")
    public ResponseEntity<?> creaConsiglio(@RequestBody Map<String, Object> body) {
        String corpo = (String) body.get("corpo");
        int esameId = (Integer) body.get("esameId");
        int etichettaId = (Integer) body.get("etichettaId");
        String username = (String) body.get("username");

        Esame esame = esameRepository.findById(esameId).orElse(null);
        Utente utente = utenteRepository.findByUsername(username).orElse(null);
        Etichetta etichetta = etichettaRepository.findById(etichettaId).orElse(null);
        if (esame == null || utente == null || etichetta == null) {
            return ResponseEntity.badRequest().body(Map.of("errore", "Esame, utente o etichetta non trovati"));
        }

        Consiglio consiglio = new Consiglio(utente, esame, corpo, etichetta);
        contenutoRepository.save(consiglio);
        return ResponseEntity.ok(toDTO(consiglio));
    }

    @GetMapping("/{id}/file")
    public ResponseEntity<byte[]> downloadFile(@PathVariable int id) throws IOException {
        Contenuto c = contenutoRepository.findById(id).orElse(null);
        if (!(c instanceof Documento doc)) {
            return ResponseEntity.notFound().build();
        }
        Path path = Path.of(uploadDir).resolve(doc.getNomeFile());
        if (!Files.exists(path)) {
            return ResponseEntity.notFound().build();
        }
        byte[] bytes = Files.readAllBytes(path);
        return ResponseEntity.ok()
                .header("Content-Disposition", "attachment; filename=\"" + doc.getNomeFile() + "\"")
                .header("Content-Type", "application/octet-stream")
                .body(bytes);
    }
}