package com.studyhub.dto;

public class ContenutoDTO {
    private Integer id;
    private String tipo; // "documento" o "consiglio" — utile al frontend per sapere come renderizzare
    private String autore; // username, non l'intero oggetto Utente
    private String dataCreazione;
    // campi solo-Documento (null se è un Consiglio)
    private String titolo;
    private String descrizione;
    private String nomeFile;
    // campi solo-Consiglio (null se è un Documento)
    private String corpo;
    private String etichetta; // nome, non l'intero oggetto Etichetta
    private final double mediaStelle;
    private final int numValutazioni;

    public ContenutoDTO(Integer id, String tipo, String autore, String dataCreazione,
            String titolo, String descrizione, String nomeFile,
            String corpo, String etichetta,
            double mediaStelle, int numValutazioni) {
        this.id = id;
        this.tipo = tipo;
        this.autore = autore;
        this.dataCreazione = dataCreazione;
        this.titolo = titolo;
        this.descrizione = descrizione;
        this.nomeFile = nomeFile;
        this.corpo = corpo;
        this.etichetta = etichetta;
        this.mediaStelle = mediaStelle;
        this.numValutazioni = numValutazioni;
    }

    public Integer getId() {
        return id;
    }

    public String getTipo() {
        return tipo;
    }

    public String getAutore() {
        return autore;
    }

    public String getDataCreazione() {
        return dataCreazione;
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

    public String getCorpo() {
        return corpo;
    }

    public String getEtichetta() {
        return etichetta;
    }

    public double getMediaStelle() {
        return mediaStelle;
    }

    public int getNumValutazioni() {
        return numValutazioni;
    }
}