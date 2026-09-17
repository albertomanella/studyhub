package com.studyhub.models;

import jakarta.persistence.*;

@Entity
@Table(name = "etichetta")
public class Etichetta {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @Column(length = 32, unique = true)
    private String nome;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public Etichetta(String nome) {
        this.nome = nome;
    }

    public Etichetta() {
    }

}