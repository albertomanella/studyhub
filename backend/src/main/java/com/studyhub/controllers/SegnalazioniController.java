package com.studyhub.controllers;

import com.studyhub.dto.SegnalazioneDTO;
import com.studyhub.models.Contenuto;
import com.studyhub.models.RuoloUtente;
import com.studyhub.models.Segnalazione;
import com.studyhub.models.Sospensione;
import com.studyhub.models.StatoSegnalazione;
import com.studyhub.models.Utente;
import com.studyhub.repositories.ContenutoRepository;
import com.studyhub.repositories.SegnalazioneRepository;
import com.studyhub.repositories.SospensioneRepository;
import com.studyhub.repositories.UtenteRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
public class SegnalazioniController {

        private static final int SOGLIA_AVVERTIMENTI_SOSPENSIONE = 3;
        private static final long DURATA_SOSPENSIONE_AUTOMATICA_GIORNI = 7;

        private final UtenteRepository utenteRepository;
        private final ContenutoRepository contenutoRepository;
        private final SegnalazioneRepository segnalazioneRepository;
        private final SospensioneRepository sospensioneRepository;

        public SegnalazioniController(UtenteRepository utenteRepository, ContenutoRepository contenutoRepository,
                        SegnalazioneRepository segnalazioneRepository, SospensioneRepository sospensioneRepository) {
                this.utenteRepository = utenteRepository;
                this.contenutoRepository = contenutoRepository;
                this.segnalazioneRepository = segnalazioneRepository;
                this.sospensioneRepository = sospensioneRepository;
        }

        @PostMapping("/api/segnalazioni")
        public ResponseEntity<Map<String, Object>> segnala(@RequestBody Map<String, String> body) {
                Integer idContenuto = Integer.valueOf(body.get("idContenuto"));
                String usernameSegnalante = body.get("usernameSegnalante");
                String motivazione = body.get("motivazione");

                // Verifica ruolo utente
                Optional<Utente> utenteOpt = utenteRepository.findByUsername(usernameSegnalante);
                if (utenteOpt.isEmpty() || !RuoloUtente.UTENTE.equals(utenteOpt.get().getRuolo())) {
                        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                                        .body(Map.of("errore", "Accesso negato: solo gli utenti possono segnalare"));
                }
                Utente utente = utenteOpt.get();

                // Verifica che l'utente non stia segnalando un proprio contenuto e che il
                // contenuto esista
                if (!contenutoRepository.findById(idContenuto).isPresent()) {
                        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                                        .body(Map.of("errore", "Il contenuto non esiste"));

                }

                Contenuto contenuto = contenutoRepository.findById(idContenuto).get();

                if (contenutoRepository.findById(idContenuto).get().getUtente().getUsername()
                                .equals(usernameSegnalante)) {
                        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                                        .body(Map.of("errore", "Non puoi segnalare un tuo contenuto"));
                }

                if (segnalazioneRepository.existsByUtenteAndContenuto(utente, contenuto)) {
                        return ResponseEntity.status(HttpStatus.CONFLICT)
                                        .body(Map.of("errore", "Contenuto già segnalato da questo utente"));
                }

                segnalazioneRepository.save(new Segnalazione(motivazione, utente, contenuto));
                return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("successo", true));
        }

        @GetMapping("/api/moderatore/segnalazioni")
        public ResponseEntity<?> listaSegnalazioni(@RequestParam String username) {
                Optional<com.studyhub.models.Utente> utente = utenteRepository.findByUsername(username);
                if (utente.isEmpty() || !RuoloUtente.MODERATORE.equals(utente.get().getRuolo())) {
                        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                                        .body(Map.of("errore",
                                                        "Accesso negato: solo i moderatori possono visualizzare le segnalazioni"));
                }

                List<SegnalazioneDTO> attesa = segnalazioneRepository.findByStato(StatoSegnalazione.ATTESA).stream()
                                .map(this::toDTO)
                                .collect(Collectors.toList());
                return ResponseEntity.ok(attesa);
        }

        @PatchMapping("/api/moderatore/segnalazioni/{id}")
        public ResponseEntity<Map<String, Object>> gestisciSegnalazione(@PathVariable Integer id,
                        @RequestBody Map<String, String> body) {
                String usernameModeratore = body.get("usernameModeratore");
                String esito = body.get("esito"); // "accolta" oppure "respinta"

                if (verificaModeratore(usernameModeratore).isEmpty()) {
                        return erroreAccessoModeratore("gestire le segnalazioni");
                }

                Optional<Segnalazione> segnalazioneOpt = segnalazioneRepository.findById(id);
                if (segnalazioneOpt.isEmpty()) {
                        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                                        .body(Map.of("errore", "Segnalazione non trovata"));
                }
                Segnalazione segnalazione = segnalazioneOpt.get();

                if (segnalazione.getStato() != StatoSegnalazione.ATTESA) {
                        return ResponseEntity.status(HttpStatus.CONFLICT)
                                        .body(Map.of("errore", "Segnalazione già gestita"));
                }

                if ("accolta".equalsIgnoreCase(esito)) {
                        // Ordine importante: leggere l'autore prima di cancellare il contenuto
                        Contenuto contenuto = segnalazione.getContenuto();
                        Utente autore = contenuto.getUtente();
                        autore.getProfilo().addAvvertimento();
                        utenteRepository.save(autore);
                        sospendiSeSogliaRaggiunta(autore);
                        segnalazione.setStato(StatoSegnalazione.ACCOLTA);
                        String statoFinale = segnalazione.getStato().name();
                        // fk_segnalazione_contenuto è ON DELETE CASCADE: cancellare il contenuto
                        // elimina a cascata questa riga (e le eventuali altre segnalazioni ancora
                        // in attesa sullo stesso contenuto, che non hanno più nulla da moderare).
                        // Per questo la risposta si costruisce PRIMA della delete, senza più
                        // richiamare segnalazioneRepository.save(): la riga sarebbe già sparita.
                        contenutoRepository.delete(contenuto);
                        return ResponseEntity.ok(Map.of("successo", true, "stato", statoFinale));
                } else if ("respinta".equalsIgnoreCase(esito)) {
                        segnalazione.setStato(StatoSegnalazione.RESPINTA);
                        segnalazioneRepository.save(segnalazione);
                        return ResponseEntity.ok(Map.of("successo", true, "stato", segnalazione.getStato().name()));
                } else {
                        return ResponseEntity.badRequest()
                                        .body(Map.of("errore", "Esito non valido: usare 'accolta' o 'respinta'"));
                }
        }

        @PostMapping("/api/moderatore/sospensioni")
        public ResponseEntity<Map<String, Object>> sospendiUtente(@RequestBody Map<String, Object> body) {
                String usernameModeratore = (String) body.get("usernameModeratore");
                String usernameUtente = (String) body.get("usernameUtente");
                String motivazione = (String) body.get("motivazione");
                Integer giorni = (Integer) body.get("giorni");

                if (verificaModeratore(usernameModeratore).isEmpty()) {
                        return erroreAccessoModeratore("sospendere gli utenti");
                }

                Optional<Utente> utenteOpt = utenteRepository.findByUsername(usernameUtente);
                if (utenteOpt.isEmpty()) {
                        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                                        .body(Map.of("errore", "Utente non trovato"));
                }
                if (giorni == null || giorni <= 0) {
                        return ResponseEntity.badRequest()
                                        .body(Map.of("errore", "Numero di giorni di sospensione non valido"));
                }

                Utente utente = utenteOpt.get();
                Optional<Sospensione> esistente = sospensioneRepository.findByUtente(utente);
                if (esistente.isPresent() && esistente.get().isAttiva()) {
                        return ResponseEntity.status(HttpStatus.CONFLICT)
                                        .body(Map.of("errore",
                                                        "L'utente risulta già sospeso fino al "
                                                                        + esistente.get().getDataFine()));
                }

                LocalDate dataFine = LocalDate.now().plusDays(giorni);
                if (esistente.isPresent()) {
                        esistente.get().rinnova(motivazione, dataFine);
                        sospensioneRepository.save(esistente.get());
                } else {
                        sospensioneRepository.save(new Sospensione(utente, motivazione, dataFine));
                }

                return ResponseEntity.ok(Map.of("successo", true, "dataFine", dataFine.toString()));
        }

        // R21F: "Ogni nuovo avvertimento, a partire dal terzo, comporta la
        // sospensione dell'utente per 1 settimana" — non solo il 3°, il 6°, ecc.:
        // OGNI avvertimento dal 3° in poi (3°, 4°, 5°...) riapplica la sospensione,
        // rinnovando la finestra di una settimana da oggi. A differenza della
        // sospensione manuale (SospensioneUtente), qui non c'è uno scenario
        // "utente già sospeso" da rispettare: il requisito non prevede eccezioni.
        private void sospendiSeSogliaRaggiunta(Utente autore) {
                int numAvvertimenti = autore.getProfilo().getNumAvvertimenti();
                if (numAvvertimenti < SOGLIA_AVVERTIMENTI_SOSPENSIONE) {
                        return;
                }

                Optional<Sospensione> esistente = sospensioneRepository.findByUtente(autore);
                LocalDate dataFine = LocalDate.now().plusDays(DURATA_SOSPENSIONE_AUTOMATICA_GIORNI);
                String motivo = "Sospensione automatica per accumulo di avvertimenti (" + numAvvertimenti + ")";
                if (esistente.isPresent()) {
                        esistente.get().rinnova(motivo, dataFine);
                        sospensioneRepository.save(esistente.get());
                } else {
                        sospensioneRepository.save(new Sospensione(autore, motivo, dataFine));
                }
        }

        private Optional<Utente> verificaModeratore(String username) {
                return utenteRepository.findByUsername(username)
                                .filter(u -> RuoloUtente.MODERATORE.equals(u.getRuolo()));
        }

        private ResponseEntity<Map<String, Object>> erroreAccessoModeratore(String azione) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                                .body(Map.of("errore", "Accesso negato: solo i moderatori possono " + azione));
        }

        private SegnalazioneDTO toDTO(Segnalazione s) {
                return new SegnalazioneDTO(
                                s.getId(),
                                s.getMotivazione(),
                                s.getUtente().getUsername(),
                                s.getContenuto().getId(),
                                s.getStato().name(),
                                s.getDataSegnalazione().toString());
        }
}