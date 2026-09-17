package com.studyhub.models;

import java.time.LocalDate;

import jakarta.persistence.*;

@Embeddable
public class Profilo {
    private String nome;
    private String cognome;
    @Column(unique = true)
    private String email;
    private String biografia;
    private String immagineProfilo;
    private int numAvvertimenti;
    private LocalDate dataUltimoAvvertimento;

    public Profilo() {
    }

    public Profilo(String nome, String cognome, String email) {
        this.nome = nome;
        this.cognome = cognome;
        this.email = email;
        this.numAvvertimenti = 0;
        this.dataUltimoAvvertimento = null;
    }

    public String getNome() {
        return nome;
    }

    public String getCognome() {
        return cognome;
    }

    public String getEmail() {
        return email;
    }

    public String getBiografia() {
        return biografia;
    }

    public String getImmagineProfilo() {
        return immagineProfilo;
    }

    public int getNumAvvertimenti() {
        return numAvvertimenti;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public void setCognome(String cognome) {
        this.cognome = cognome;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setBiografia(String bio) {
        this.biografia = bio;
    }

    public void setImmagineProfilo(String immagine) {
        this.immagineProfilo = immagine;
    }

    public void addAvvertimento() {
        this.numAvvertimenti++;
        this.dataUltimoAvvertimento = LocalDate.now();
    }

    public void azzeraAvvertimenti() {
        this.numAvvertimenti = 0;
        this.dataUltimoAvvertimento = null;
    }

    public LocalDate getDataUltimoAvvertimento() {
        return dataUltimoAvvertimento;
    }

}