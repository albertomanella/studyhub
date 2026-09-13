package com.studyhub.dto;

public class EsameDTO {
    private Integer id;
    private String nome;
    private int anno;
    private String nomeCorso;

    public EsameDTO(Integer id, String nome, int anno, String nomeCorso) {
        this.id = id;
        this.nome = nome;
        this.anno = anno;
        this.nomeCorso = nomeCorso;
    }

    public Integer getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public int getAnno() {
        return anno;
    }

    public String getNomeCorso() {
        return nomeCorso;
    }

}
