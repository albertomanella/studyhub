package com.studyhub.security;

import com.studyhub.models.RuoloUtente;
import com.studyhub.repositories.UtenteRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

// La registrazione crea solo utenti normali. All'avvio, gli utenti elencati in
// app.moderatori (variabile MODERATORS, separati da virgola) diventano
// moderatori: nessuna password nella configurazione, solo username di account
// già registrati. Il nuovo ruolo vale dal login successivo, perché il ruolo
// viene letto al momento del login. Togliere un nome dalla lista non revoca il
// ruolo.
@Component
public class PromozioneModeratori implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(PromozioneModeratori.class);

    private final UtenteRepository utenteRepository;
    private final List<String> usernames;

    public PromozioneModeratori(UtenteRepository utenteRepository,
            @Value("${app.moderatori:}") List<String> usernames) {
        this.utenteRepository = utenteRepository;
        this.usernames = usernames;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        for (String username : usernames) {
            String nome = username.trim();
            if (nome.isEmpty()) {
                continue;
            }
            utenteRepository.findByUsername(nome).ifPresentOrElse(utente -> {
                if (utente.getRuolo() != RuoloUtente.MODERATORE) {
                    utente.setRuolo(RuoloUtente.MODERATORE);
                    utenteRepository.save(utente);
                    log.info("Utente '{}' promosso a moderatore", nome);
                }
            }, () -> log.warn("MODERATORS: l'utente '{}' non esiste, registralo prima", nome));
        }
    }
}
