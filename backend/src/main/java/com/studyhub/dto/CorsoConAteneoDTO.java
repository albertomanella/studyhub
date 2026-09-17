package com.studyhub.dto;

public class CorsoConAteneoDTO {
    private String nomeCorso;
    private String nomeAteneo;

    public String getNomeCorso() {
        return nomeCorso;
    }

    public void setNomeCorso(String nomeCorso) {
        this.nomeCorso = nomeCorso;
    }

    public String getNomeAteneo() {
        return nomeAteneo;
    }

    public void setNomeAteneo(String nomeAteneo) {
        this.nomeAteneo = nomeAteneo;
    }

    public CorsoConAteneoDTO(String nomeCorso, String nomeAteneo) {
        this.nomeCorso = nomeCorso;
        this.nomeAteneo = nomeAteneo;
    }

}
