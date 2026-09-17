package com.studyhub.models;

import jakarta.persistence.*;

@Entity
@DiscriminatorValue("documento")
public class Documento extends Contenuto {

    @Column(length = 64)
    private String titolo;

    @Column(length = 512)
    private String descrizione;

    private String nomeFile;

    public Documento() {
    }

    public Documento(Utente utente, Esame esame, String titolo, String descrizione, String nomeFile) {
        super(utente, esame);
        this.titolo = titolo;
        this.descrizione = descrizione;
        this.nomeFile = nomeFile;
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

}
