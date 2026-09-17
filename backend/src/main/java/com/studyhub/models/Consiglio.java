package com.studyhub.models;

import jakarta.persistence.*;

@Entity
@DiscriminatorValue("consiglio")
public class Consiglio extends Contenuto {

    @Column(length = 512)
    private String corpo;

    @ManyToOne(optional = false)
    @JoinColumn(name = "etichetta_id", nullable = false)
    private Etichetta etichetta;

    public Consiglio() {
    }

    public Consiglio(Utente utente, Esame esame, String corpo, Etichetta etichetta) {
        super(utente, esame);
        this.corpo = corpo;
        this.etichetta = etichetta;
    }

    public String getCorpo() {
        return corpo;
    }

    public Etichetta getEtichetta() {
        return etichetta;
    }
}