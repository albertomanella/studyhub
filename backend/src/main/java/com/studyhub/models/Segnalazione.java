package com.studyhub.models;

import java.time.LocalDateTime;
import jakarta.persistence.*;

@Entity
@Table(name = "segnalazione", uniqueConstraints = @UniqueConstraint(columnNames = { "utente_id", "contenuto_id" }))
public class Segnalazione {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(length = 256, nullable = false)
    private String motivazione;

    // L'utente segnalante
    @ManyToOne(optional = false)
    @JoinColumn(name = "utente_id", nullable = false)
    private Utente utente;

    @ManyToOne(optional = false)
    @JoinColumn(name = "contenuto_id", nullable = false)
    private Contenuto contenuto;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatoSegnalazione stato;

    @Column(name = "data_segnalazione", nullable = false, updatable = false)
    private LocalDateTime dataSegnalazione;

    public Segnalazione() {
    }

    public Segnalazione(String motivazione, Utente utente, Contenuto contenuto) {
        this.motivazione = motivazione;
        this.utente = utente;
        this.contenuto = contenuto;
        this.stato = StatoSegnalazione.ATTESA;
        this.dataSegnalazione = LocalDateTime.now();
    }

    public Integer getId() {
        return id;
    }

    public String getMotivazione() {
        return motivazione;
    }

    public Utente getUtente() {
        return utente;
    }

    public Contenuto getContenuto() {
        return contenuto;
    }

    public StatoSegnalazione getStato() {
        return stato;
    }

    public void setStato(StatoSegnalazione stato) {
        this.stato = stato;
    }

    public LocalDateTime getDataSegnalazione() {
        return dataSegnalazione;
    }
}
