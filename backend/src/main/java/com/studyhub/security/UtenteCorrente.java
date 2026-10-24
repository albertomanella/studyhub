package com.studyhub.security;

import com.studyhub.models.Utente;
import com.studyhub.repositories.UtenteRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

// L'identità dell'utente arriva sempre dalla sessione (Authentication),
// mai da un parametro inviato dal client.
@Component
public class UtenteCorrente {

    private final UtenteRepository utenteRepository;

    public UtenteCorrente(UtenteRepository utenteRepository) {
        this.utenteRepository = utenteRepository;
    }

    public Utente da(Authentication authentication) {
        // L'utente può essere stato cancellato mentre la sua sessione era aperta
        return utenteRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));
    }
}
