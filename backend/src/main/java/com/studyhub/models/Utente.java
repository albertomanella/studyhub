package com.studyhub.models;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.*;

@Entity
@Table(name = "utente")
public class Utente {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private String username;
    private String password;
    @Embedded
    private Profilo profilo;
    @Enumerated(EnumType.STRING)
    private RuoloUtente ruolo;

    @ManyToMany
    @JoinTable(name = "utente_corso_di_studio", joinColumns = @JoinColumn(name = "utente_id"), inverseJoinColumns = @JoinColumn(name = "corso_id"))
    private List<CorsoDiStudio> corsi = new ArrayList<>();

    public Utente() {
    }

    public Utente(String username, String password, RuoloUtente ruolo, String nome, String cognome, String email,
            List<CorsoDiStudio> corsi) {
        this.username = username;
        this.password = password;
        this.ruolo = ruolo;
        this.profilo = new Profilo(nome, cognome, email);
        this.setCorsi(corsi);
    }

    public Integer getId() {
        return id;
    }

    public void setCorsi(List<CorsoDiStudio> corsi) {
        List<CorsoDiStudio> tmp = new ArrayList<>(this.corsi);
        for (CorsoDiStudio c : tmp) {
            this.removeCorso(c);
        }
        for (CorsoDiStudio c : corsi) {
            this.addCorso(c);
        }
    }

    public void addCorso(CorsoDiStudio corso) {
        corsi.add(corso);
        corso.addUtente(this);
    }

    public void removeCorso(CorsoDiStudio corso) {
        corsi.remove(corso);
        corso.removeUtente(this);
    }

    public List<CorsoDiStudio> getCorsi() {
        return corsi;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Profilo getProfilo() {
        return this.profilo;
    }

    public void setProfilo(Profilo profilo) {
        this.profilo = profilo;
    }

    public RuoloUtente getRuolo() {
        return ruolo;
    }

    public void setRuolo(RuoloUtente ruolo) {
        this.ruolo = ruolo;
    }
}