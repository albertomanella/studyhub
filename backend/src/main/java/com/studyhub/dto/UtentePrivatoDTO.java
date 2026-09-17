package com.studyhub.dto;

import java.util.List;

public class UtentePrivatoDTO extends UtentePubblicoDTO {
    private String ruolo;
    private int numAvvertimenti;
    private String dataUltimoAvvertimento;

    public UtentePrivatoDTO(Integer id, String username, String nome, String cognome, String biografia,
            String immagineProfilo, String email, List<CorsoConAteneoDTO> corsi, String ruolo, int numAvvertimenti,
            String dataUltimoAvvertimento) {
        super(id, username, nome, cognome, biografia, immagineProfilo, email, corsi);
        this.ruolo = ruolo;
        this.numAvvertimenti = numAvvertimenti;
        this.dataUltimoAvvertimento = dataUltimoAvvertimento;
    }

    public String getRuolo() {
        return ruolo;
    }

    public int getNumAvvertimenti() {
        return numAvvertimenti;
    }

    public String getDataUltimoAvvertimento() {
        return dataUltimoAvvertimento;
    }

}