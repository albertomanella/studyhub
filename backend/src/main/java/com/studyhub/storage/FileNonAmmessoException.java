package com.studyhub.storage;

// Il file caricato non rispetta le regole di UploadValidator; il messaggio è
// pensato per essere mostrato all'utente.
public class FileNonAmmessoException extends RuntimeException {

    public FileNonAmmessoException(String messaggio) {
        super(messaggio);
    }
}
