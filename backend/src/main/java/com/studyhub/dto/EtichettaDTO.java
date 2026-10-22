package com.studyhub.dto;

public class EtichettaDTO {
    private final Integer id;
    private final String nome;

    public EtichettaDTO(Integer id, String nome) {
        this.id = id;
        this.nome = nome;
    }

    public Integer getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }
}
