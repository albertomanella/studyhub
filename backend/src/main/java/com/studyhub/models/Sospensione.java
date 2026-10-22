package com.studyhub.models;

import java.time.LocalDate;

import jakarta.persistence.*;

@Entity
@Table(name = "sospensione")
public class Sospensione {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    // Relazione 0..1:1 con Utente: unique=true sulla FK garantisce a livello DB
    // che un utente non possa avere più di una riga sospensione contemporaneamente.
    @OneToOne(optional = false)
    @JoinColumn(name = "utente_id", nullable = false, unique = true)
    private Utente utente;

    @Column(length = 256, nullable = false)
    private String motivazione;

    @Column(name = "data_inizio", nullable = false)
    private LocalDate dataInizio;

    @Column(name = "data_fine", nullable = false)
    private LocalDate dataFine;

    public Sospensione() {
    }

    public Sospensione(Utente utente, String motivazione, LocalDate dataFine) {
        this.utente = utente;
        this.motivazione = motivazione;
        this.dataInizio = LocalDate.now();
        this.dataFine = dataFine;
    }

    public boolean isAttiva() {
        return !LocalDate.now().isAfter(dataFine);
    }

    // Stato corrente, non storico: una sospensione scaduta viene rinnovata sulla
    // stessa riga invece di crearne una nuova (il vincolo di unicità su
    // utente_id non permetterebbe comunque una seconda riga per lo stesso utente).
    public void rinnova(String motivazione, LocalDate dataFine) {
        this.motivazione = motivazione;
        this.dataInizio = LocalDate.now();
        this.dataFine = dataFine;
    }

    public Integer getId() {
        return id;
    }

    public Utente getUtente() {
        return utente;
    }

    public String getMotivazione() {
        return motivazione;
    }

    public LocalDate getDataInizio() {
        return dataInizio;
    }

    public LocalDate getDataFine() {
        return dataFine;
    }
}
