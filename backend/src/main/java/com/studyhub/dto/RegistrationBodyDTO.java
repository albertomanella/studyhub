package com.studyhub.dto;

import java.util.ArrayList;
import java.util.List;

public class RegistrationBodyDTO {
    private String username;
    private String password;
    private String nome;
    private String cognome;
    private String email;
    private List<Integer> idCorsi;

    public RegistrationBodyDTO(String nome, String username, String password, String cognome, String email,
            List<Integer> idCorsi) {
        this.nome = nome;
        this.username = username;
        this.password = password;
        this.cognome = cognome;
        this.email = email;
        this.idCorsi = new ArrayList<>(idCorsi);
    }

    public RegistrationBodyDTO() {

    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
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

    public List<Integer> getIdCorsi() {
        return idCorsi;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPassword(String password) {
        this.password = password;
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

    public void setIdCorsi(List<Integer> idCorsi) {
        this.idCorsi = idCorsi;
    }

}