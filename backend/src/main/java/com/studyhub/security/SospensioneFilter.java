package com.studyhub.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.studyhub.models.Sospensione;
import com.studyhub.repositories.SospensioneRepository;
import com.studyhub.repositories.UtenteRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;

// Un utente sospeso mentre ha una sessione aperta viene disconnesso alla
// richiesta successiva: stessa regola del login, dove un utente sospeso non
// può entrare. Vale anche per le sospensioni automatiche dopo il terzo
// avvertimento. Costa una query per richiesta autenticata.
//
// Non è un @Component: Spring Boot registrerebbe ogni Filter dichiarato come
// bean anche come filtro servlet, e verrebbe eseguito due volte.
class SospensioneFilter extends OncePerRequestFilter {

    // Il frontend riconosce questo codice per tornare al login mostrando il messaggio
    static final String CODICE_ACCOUNT_SOSPESO = "ACCOUNT_SOSPESO";

    private final UtenteRepository utenteRepository;
    private final SospensioneRepository sospensioneRepository;
    private final ObjectMapper objectMapper;

    SospensioneFilter(UtenteRepository utenteRepository, SospensioneRepository sospensioneRepository,
            ObjectMapper objectMapper) {
        this.utenteRepository = utenteRepository;
        this.sospensioneRepository = sospensioneRepository;
        this.objectMapper = objectMapper;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication instanceof AnonymousAuthenticationToken) {
            filterChain.doFilter(request, response);
            return;
        }

        Optional<Sospensione> sospensione = utenteRepository.findByUsername(authentication.getName())
                .flatMap(sospensioneRepository::findByUtente)
                .filter(Sospensione::isAttiva);
        if (sospensione.isEmpty()) {
            filterChain.doFilter(request, response);
            return;
        }

        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        SecurityContextHolder.clearContext();

        response.setStatus(HttpStatus.FORBIDDEN.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        objectMapper.writeValue(response.getWriter(), Map.of(
                "errore", "Account sospeso fino al " + sospensione.get().getDataFine(),
                "codice", CODICE_ACCOUNT_SOSPESO));
    }
}
