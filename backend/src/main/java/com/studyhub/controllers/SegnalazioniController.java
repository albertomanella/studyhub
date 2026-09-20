package com.studyhub.controllers;

import com.studyhub.dto.SegnalazioneDTO;
import com.studyhub.models.Contenuto;
import com.studyhub.models.RuoloUtente;
import com.studyhub.models.Segnalazione;
import com.studyhub.models.StatoSegnalazione;
import com.studyhub.models.Utente;
import com.studyhub.repositories.ContenutoRepository;
import com.studyhub.repositories.SegnalazioneRepository;
import com.studyhub.repositories.UtenteRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
public class SegnalazioniController {

        private final UtenteRepository utenteRepository;
        private final ContenutoRepository contenutoRepository;
        private final SegnalazioneRepository segnalazioneRepository;

        public SegnalazioniController(UtenteRepository utenteRepository, ContenutoRepository contenutoRepository,
                        SegnalazioneRepository segnalazioneRepository) {
                this.utenteRepository = utenteRepository;
                this.contenutoRepository = contenutoRepository;
                this.segnalazioneRepository = segnalazioneRepository;
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
                        contenutoRepository.delete(contenuto);
                        segnalazione.setStato(StatoSegnalazione.ACCOLTA);
                } else if ("respinta".equalsIgnoreCase(esito)) {
                        segnalazione.setStato(StatoSegnalazione.RESPINTA);
                } else {
                        return ResponseEntity.badRequest()
                                        .body(Map.of("errore", "Esito non valido: usare 'accolta' o 'respinta'"));
                }

                segnalazioneRepository.save(segnalazione);
                return ResponseEntity.ok(Map.of("successo", true, "stato", segnalazione.getStato().name()));
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