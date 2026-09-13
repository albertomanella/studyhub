package com.studyhub.models;

import jakarta.persistence.*;

@Entity
@Table(name = "esame")
public class Esame {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String nome;
    private int anno;

    @ManyToOne(optional = false)
    @JoinColumn(name = "corso_id", nullable = false)
    private CorsoDiStudio corso;

    public Esame() {
    }

    public Esame(String nome, int anno, CorsoDiStudio corso) {
        this.nome = nome;
        this.anno = anno;
        this.corso = corso;
    }

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

    public int getAnno() {
        return anno;
    }

    public void setAnno(int anno) {
        this.anno = anno;
    }

    public void setCorso(CorsoDiStudio corso) {
        this.corso = corso;
    }

    public CorsoDiStudio getCorso() {
        return this.corso;
    }
}
