package com.studyhub.models;

import jakarta.persistence.*;

@Entity
@DiscriminatorValue("documento")
public class Documento extends Contenuto {

    @Column(length = 64)
    private String titolo;

    @Column(length = 512)
    private String descrizione;

    // Nome del file su disco, generato dal server (UUID)
    private String nomeFile;

    // Nome scelto dall'utente: solo per la visualizzazione e il download
    private String nomeOriginale;

    public Documento() {
    }

    public Documento(Utente utente, Esame esame, String titolo, String descrizione, String nomeFile,
            String nomeOriginale) {
        super(utente, esame);
        this.titolo = titolo;
        this.descrizione = descrizione;
        this.nomeFile = nomeFile;
        this.nomeOriginale = nomeOriginale;
    }

    public String getTitolo() {
        return titolo;
    }

    public String getDescrizione() {
        return descrizione;
    }

    public String getNomeFile() {
        return nomeFile;
    }

    public String getNomeOriginale() {
        return nomeOriginale;
    }

}
