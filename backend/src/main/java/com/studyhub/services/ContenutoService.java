package com.studyhub.services;

import com.studyhub.models.Contenuto;
import com.studyhub.models.Documento;
import com.studyhub.repositories.ContenutoRepository;
import com.studyhub.storage.FileStorage;
import org.springframework.stereotype.Service;

// Eliminare un contenuto vuol dire anche eliminare il suo file: succede sia
// quando lo elimina l'autore, sia quando un moderatore accoglie una segnalazione.
@Service
public class ContenutoService {

    private final ContenutoRepository contenutoRepository;
    private final FileStorage fileStorage;

    public ContenutoService(ContenutoRepository contenutoRepository, FileStorage fileStorage) {
        this.contenutoRepository = contenutoRepository;
        this.fileStorage = fileStorage;
    }

    // Prima il database, poi il disco: se la cancellazione nel database fallisce
    // il file resta al suo posto; se fallisce quella del file, il contenuto è
    // comunque sparito e resta solo un file orfano.
    public void elimina(Contenuto contenuto) {
        contenutoRepository.delete(contenuto);
        if (contenuto instanceof Documento documento) {
            fileStorage.elimina(documento.getNomeFile());
        }
    }
}
