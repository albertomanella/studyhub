package com.studyhub.models;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Contenuto {
    private int id;
    private String tipo;
    private String titolo;
    private String corpo;
    private String etichetta;
    private String nomeFile;
    private String utente;
    private int esameId;
    private String dataCreazione;

    public Contenuto() {}

    public Contenuto(int id, String tipo, String titolo, String corpo, String etichetta,
                     String nomeFile, String utente, int esameId, String dataCreazione) {
        this.id = id;
        this.tipo = tipo;
        this.titolo = titolo;
        this.corpo = corpo;
        this.etichetta = etichetta;
        this.nomeFile = nomeFile;
        this.utente = utente;
        this.esameId = esameId;
        this.dataCreazione = dataCreazione;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }
    public String getTitolo() { return titolo; }
    public void setTitolo(String titolo) { this.titolo = titolo; }
    public String getCorpo() { return corpo; }
    public void setCorpo(String corpo) { this.corpo = corpo; }
    public String getEtichetta() { return etichetta; }
    public void setEtichetta(String etichetta) { this.etichetta = etichetta; }
    public String getNomeFile() { return nomeFile; }
    public void setNomeFile(String nomeFile) { this.nomeFile = nomeFile; }
    public String getUtente() { return utente; }
    public void setUtente(String utente) { this.utente = utente; }
    public int getEsameId() { return esameId; }
    public void setEsameId(int esameId) { this.esameId = esameId; }
    public String getDataCreazione() { return dataCreazione; }
    public void setDataCreazione(String dataCreazione) { this.dataCreazione = dataCreazione; }
}
