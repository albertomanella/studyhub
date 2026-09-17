package com.studyhub.models;

import java.time.LocalDateTime;

import jakarta.persistence.*;

@Entity
@Table(name = "contenuto")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "tipo", discriminatorType = DiscriminatorType.STRING)
public abstract class Contenuto {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "utente_id", nullable = false)
    private Utente utente;

    @ManyToOne(optional = false)
    @JoinColumn(name = "esame_id", nullable = false)
    private Esame esame;

    private LocalDateTime dataCreazione;

    protected Contenuto() {
    }

    protected Contenuto(Utente utente, Esame esame) {
        this.utente = utente;
        this.esame = esame;
        this.dataCreazione = LocalDateTime.now();
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Utente getUtente() {
        return utente;
    }

    public Esame getEsame() {
        return esame;
    }

    public LocalDateTime getDataCreazione() {
        return dataCreazione;
    }

}