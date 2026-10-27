package com.studyhub.storage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

// Gestisce i file caricati su disco, nella cartella app.upload.dir (UPLOAD_DIR).
// Ogni file è salvato con un nome generato (UUID): il nome scelto dall'utente
// non arriva mai al filesystem, quindi niente percorsi come "../", caratteri
// strani o collisioni tra utenti.
@Component
public class FileStorage {

    private static final Logger log = LoggerFactory.getLogger(FileStorage.class);
    private static final int LUNGHEZZA_MASSIMA_NOME = 255;

    private final Path cartella;

    public FileStorage(@Value("${app.upload.dir}") String cartella) throws IOException {
        this.cartella = Path.of(cartella).toAbsolutePath().normalize();
        Files.createDirectories(this.cartella);
        log.info("Cartella dei file caricati: {}", this.cartella);
    }

    // Tomcat ha già scritto il file in una cartella temporanea mentre arrivava:
    // transferTo lo sposta (o lo copia a blocchi) senza caricarlo in memoria.
    public String salva(MultipartFile file) throws IOException {
        String nome = UUID.randomUUID().toString();
        file.transferTo(percorso(nome));
        return nome;
    }

    // Restituisce il file da inviare a blocchi, senza leggerlo tutto in memoria
    public Resource apri(String nome) {
        Path percorso = percorso(nome);
        return Files.isRegularFile(percorso) ? new FileSystemResource(percorso) : null;
    }

    // Se la cancellazione fallisce il contenuto è già stato rimosso dal
    // database: resta un file orfano, innocuo, che viene segnalato nei log.
    public void elimina(String nome) {
        try {
            Files.deleteIfExists(percorso(nome));
        } catch (IOException | RuntimeException e) {
            log.warn("Impossibile eliminare il file {}: {}", nome, e.getMessage());
        }
    }

    public Path cartella() {
        return cartella;
    }

    // Nome da mostrare all'utente: senza eventuali cartelle (alcuni browser
    // inviano il percorso completo) e di lunghezza limitata.
    public static String nomeOriginale(String dalClient) {
        if (dalClient == null) {
            return "file";
        }
        String nome = dalClient.substring(Math.max(dalClient.lastIndexOf('/'), dalClient.lastIndexOf('\\')) + 1)
                .strip();
        if (nome.isEmpty()) {
            return "file";
        }
        return nome.length() > LUNGHEZZA_MASSIMA_NOME ? nome.substring(nome.length() - LUNGHEZZA_MASSIMA_NOME) : nome;
    }

    // Ulteriore protezione: il percorso finale deve restare dentro la cartella
    private Path percorso(String nome) {
        Path percorso = cartella.resolve(nome).normalize();
        if (!percorso.getParent().equals(cartella)) {
            throw new IllegalArgumentException("Nome di file non valido");
        }
        return percorso;
    }
}
