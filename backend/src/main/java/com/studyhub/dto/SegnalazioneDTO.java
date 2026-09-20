package com.studyhub.dto;

public class SegnalazioneDTO {
    private final Integer id;
    private final String motivazione;
    private final String usernameSegnalante;
    private final Integer idContenuto;
    private final String stato;
    private final String dataSegnalazione;

    public SegnalazioneDTO(Integer id, String motivazione, String usernameSegnalante, Integer idContenuto,
            String stato, String dataSegnalazione) {
        this.id = id;
        this.motivazione = motivazione;
        this.usernameSegnalante = usernameSegnalante;
        this.idContenuto = idContenuto;
        this.stato = stato;
        this.dataSegnalazione = dataSegnalazione;
    }

    public Integer getId() {
        return id;
    }

    public String getMotivazione() {
        return motivazione;
    }

    public String getUsernameSegnalante() {
        return usernameSegnalante;
    }

    public Integer getIdContenuto() {
        return idContenuto;
    }

    public String getStato() {
        return stato;
    }

    public String getDataSegnalazione() {
        return dataSegnalazione;
    }
}
