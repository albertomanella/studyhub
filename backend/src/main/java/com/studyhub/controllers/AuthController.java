package com.studyhub.controllers;

import com.studyhub.dto.CorsoConAteneoDTO;
import com.studyhub.dto.RegistrationBodyDTO;
import com.studyhub.dto.UtentePrivatoDTO;
import com.studyhub.models.CorsoDiStudio;
import com.studyhub.models.RuoloUtente;
import com.studyhub.models.Utente;
import com.studyhub.repositories.CorsoDiStudioRepository;
import com.studyhub.repositories.UtenteRepository;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UtenteRepository utenteRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final CorsoDiStudioRepository corsiRepository;

    public AuthController(UtenteRepository utenteRepository, BCryptPasswordEncoder passwordEncoder,
            CorsoDiStudioRepository corsiRepository) {
        this.utenteRepository = utenteRepository;
        this.passwordEncoder = passwordEncoder;
        this.corsiRepository = corsiRepository;
    }

    private ResponseEntity<?> responseBuilder(Utente u) {
        List<CorsoDiStudio> corsi = u.getCorsi();
        List<CorsoConAteneoDTO> dtoCorsi = corsi.stream().map(corsoConAteneo -> new CorsoConAteneoDTO(
                corsoConAteneo.getNomeCorso(),
                corsoConAteneo.getAteneo().getNomeAteneo())).collect(Collectors.toList());
        Optional<LocalDate> dataUltimoAvvertimento = Optional
                .ofNullable(u.getProfilo().getDataUltimoAvvertimento());
        String dataFormatted = null;
        if (dataUltimoAvvertimento.isPresent()) {
            dataFormatted = dataUltimoAvvertimento.get().toString();
        }
        UtentePrivatoDTO dtoUtente = new UtentePrivatoDTO(u.getId(), u.getUsername(),
                u.getProfilo().getNome(), u.getProfilo().getCognome(), u.getProfilo().getBiografia(),
                u.getProfilo().getImmagineProfilo(), u.getProfilo().getEmail(), dtoCorsi,
                u.getRuolo().name(),
                u.getProfilo().getNumAvvertimenti(), dataFormatted);
        return ResponseEntity.ok(dtoUtente);
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegistrationBodyDTO body) {
        String username = body.getUsername();
        String password = body.getPassword();
        String nome = body.getNome();
        String cognome = body.getCognome();
        String email = body.getEmail();

        if (body.getIdCorsi() == null || body.getIdCorsi().isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("errore", "Non è stato inserito almeno un corso di studio"));
        }
        List<CorsoDiStudio> corsi = corsiRepository.findAllById(body.getIdCorsi());

        boolean existsUsername = utenteRepository.existsByUsername(username);
        if (existsUsername) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("errore", "Username già esistente"));
        }

        boolean existsEmail = utenteRepository.existsByProfiloEmail(email);
        if (existsEmail) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("errore", "Email già registrata"));
        }

        String hashedPassword = passwordEncoder.encode(password);

        Utente u = new Utente(username, hashedPassword, RuoloUtente.UTENTE, nome, cognome, email, corsi);
        utenteRepository.save(u);

        return responseBuilder(u);

    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> body) {
        String username = body.get("username");
        String password = body.get("password");

        return utenteRepository.findByUsername(username)
                .filter(u -> passwordEncoder.matches(password, u.getPassword()))
                .<ResponseEntity<?>>map(u -> {
                    return responseBuilder(u);
                })
                .orElse(ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("errore", "Credenziali non valide")));
    }
}
