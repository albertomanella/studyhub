package com.studyhub.services;

import com.studyhub.models.Contenuto;
import com.studyhub.models.Documento;
import com.studyhub.repositories.ContenutoRepository;
import com.studyhub.repositories.SegnalazioneRepository;
import com.studyhub.repositories.ValutazioneRepository;
import com.studyhub.storage.FileStorage;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

// Eliminare un contenuto vuol dire anche eliminare le sue valutazioni, le sue
// segnalazioni e il suo file: succede sia quando lo elimina l'autore, sia
// quando un moderatore accoglie una segnalazione.
@Service
public class ContenutoService {

    private final ContenutoRepository contenutoRepository;
    private final ValutazioneRepository valutazioneRepository;
    private final SegnalazioneRepository segnalazioneRepository;
    private final FileStorage fileStorage;
    private final TransactionTemplate transazione;

    public ContenutoService(ContenutoRepository contenutoRepository, ValutazioneRepository valutazioneRepository,
            SegnalazioneRepository segnalazioneRepository, FileStorage fileStorage,
            PlatformTransactionManager transactionManager) {
        this.contenutoRepository = contenutoRepository;
        this.valutazioneRepository = valutazioneRepository;
        this.segnalazioneRepository = segnalazioneRepository;
        this.fileStorage = fileStorage;
        this.transazione = new TransactionTemplate(transactionManager);
    }

    // Prima il database, in un'unica transazione; poi il disco. Se il database
    // fallisce il file resta al suo posto; se fallisce il file, il contenuto è
    // comunque sparito e resta solo un file orfano.
    //
    // Valutazioni e segnalazioni si eliminano esplicitamente anche se il
    // database le cancellerebbe da solo (ON DELETE CASCADE): così Hibernate non
    // tiene in memoria oggetti che puntano a un contenuto che non esiste più.
    public void elimina(Contenuto contenuto) {
        transazione.executeWithoutResult(stato -> {
            valutazioneRepository.deleteByContenuto(contenuto);
            segnalazioneRepository.deleteByContenuto(contenuto);
            contenutoRepository.delete(contenuto);
        });
        if (contenuto instanceof Documento documento) {
            fileStorage.elimina(documento.getNomeFile());
        }
    }
}
