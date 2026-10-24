package com.studyhub.controllers;

import com.studyhub.dto.CorsoConAteneoDTO;
import com.studyhub.dto.RegistrationBodyDTO;
import com.studyhub.dto.UtentePrivatoDTO;
import com.studyhub.models.CorsoDiStudio;
import com.studyhub.models.RuoloUtente;
import com.studyhub.models.Sospensione;
import com.studyhub.models.Utente;
import com.studyhub.repositories.CorsoDiStudioRepository;
import com.studyhub.repositories.SospensioneRepository;
import com.studyhub.repositories.UtenteRepository;
import com.studyhub.security.UtenteCorrente;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.context.SecurityContextRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UtenteRepository utenteRepository;
    private final PasswordEncoder passwordEncoder;
    private final CorsoDiStudioRepository corsiRepository;
    private final SospensioneRepository sospensioneRepository;
    private final AuthenticationManager authenticationManager;
    private final SecurityContextRepository securityContextRepository;
    private final UtenteCorrente utenteCorrente;

    public AuthController(UtenteRepository utenteRepository, PasswordEncoder passwordEncoder,
            CorsoDiStudioRepository corsiRepository, SospensioneRepository sospensioneRepository,
            AuthenticationManager authenticationManager, SecurityContextRepository securityContextRepository,
            UtenteCorrente utenteCorrente) {
        this.utenteRepository = utenteRepository;
        this.passwordEncoder = passwordEncoder;
        this.corsiRepository = corsiRepository;
        this.sospensioneRepository = sospensioneRepository;
        this.authenticationManager = authenticationManager;
        this.securityContextRepository = securityContextRepository;
        this.utenteCorrente = utenteCorrente;
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
    public ResponseEntity<?> login(@RequestBody Map<String, String> body, HttpServletRequest request,
            HttpServletResponse response) {
        Authentication authentication;
        try {
            authentication = authenticationManager.authenticate(
                    UsernamePasswordAuthenticationToken.unauthenticated(body.get("username"), body.get("password")));
        } catch (AuthenticationException e) {
            // Stesso messaggio per username inesistente e password errata
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("errore", "Credenziali non valide"));
        }

        // La sospensione si controlla solo dopo la password: chi non la conosce
        // non scopre se un account è sospeso.
        Utente u = utenteCorrente.da(authentication);
        Optional<Sospensione> sospensione = sospensioneRepository.findByUtente(u)
                .filter(Sospensione::isAttiva);
        if (sospensione.isPresent()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("errore", "Account sospeso fino al " + sospensione.get().getDataFine()));
        }

        // Nuovo ID di sessione al login: un ID fissato in precedenza da un
        // attaccante non diventa valido (session fixation).
        if (request.getSession(false) != null) {
            request.changeSessionId();
        }
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);
        securityContextRepository.saveContext(context, request, response);

        return responseBuilder(u);
    }

    // Il frontend lo chiama all'avvio per sapere se esiste una sessione valida.
    // Il logout è gestito da Spring Security (POST /api/auth/logout).
    @GetMapping("/me")
    public ResponseEntity<?> me(Authentication authentication) {
        return responseBuilder(utenteCorrente.da(authentication));
    }
}
