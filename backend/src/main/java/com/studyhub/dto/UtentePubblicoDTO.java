package com.studyhub.dto;

import java.util.List;

public class UtentePubblicoDTO {
    private Integer id;
    private String username;
    private String nome;
    private String cognome;
    private String biografia;
    private String immagineProfilo;
    private String email;
    private List<CorsoConAteneoDTO> corsi;

    public Integer getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getNome() {
        return nome;
    }

    public String getCognome() {
        return cognome;
    }

    public String getBiografia() {
        return biografia;
    }

    public String getImmagineProfilo() {
        return immagineProfilo;
    }

    public String getEmail() {
        return email;
    }

    public List<CorsoConAteneoDTO> getCorsi() {
        return corsi;
    }

    public UtentePubblicoDTO(Integer id, String username, String nome, String cognome, String biografia,
            String immagineProfilo, String email, List<CorsoConAteneoDTO> corsi) {
        this.id = id;
        this.username = username;
        this.nome = nome;
        this.cognome = cognome;
        this.biografia = biografia;
        this.immagineProfilo = immagineProfilo;
        this.email = email;
        this.corsi = corsi;
    }

}
