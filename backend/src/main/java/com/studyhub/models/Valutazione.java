package com.studyhub.models;

import jakarta.persistence.*;

@Entity
@Table(name = "valutazione", uniqueConstraints = @UniqueConstraint(columnNames = { "utente_id", "contenuto_id" }))
public class Valutazione {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false)
    private int stelle;

    @ManyToOne(optional = false)
    @JoinColumn(name = "utente_id", nullable = false)
    private Utente utente;

    @ManyToOne(optional = false)
    @JoinColumn(name = "contenuto_id", nullable = false)
    private Contenuto contenuto;

    public Valutazione() {
    }

    public Valutazione(int stelle, Utente utente, Contenuto contenuto) {
        this.stelle = stelle;
        this.utente = utente;
        this.contenuto = contenuto;
    }

    public Integer getId() {
        return id;
    }

    public int getStelle() {
        return stelle;
    }

    public void setStelle(int stelle) {
        if (stelle < 1 || stelle > 5) {
            throw new IllegalArgumentException("La valutazione deve essere compresa tra 1 e 5 stelle");
        }
        this.stelle = stelle;
    }

    public Utente getUtente() {
        return utente;
    }

    public Contenuto getContenuto() {
        return contenuto;
    }
}
