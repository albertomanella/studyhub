package com.unishare.models;

import java.time.LocalDateTime;
import java.util.UUID;

public class Segnalazione {
    private String idSegnalazione;
    private String idContenuto;
    private String usernameSegnalante;
    private String motivazione;
    private String stato;
    private LocalDateTime dataSegnalazione;

    public Segnalazione() {}

    public Segnalazione(String idContenuto, String usernameSegnalante, String motivazione) {
        this.idSegnalazione = UUID.randomUUID().toString();
        this.idContenuto = idContenuto;
        this.usernameSegnalante = usernameSegnalante;
        this.motivazione = motivazione;
        this.stato = "attesa";
        this.dataSegnalazione = LocalDateTime.now();
    }

    public String getIdSegnalazione() { return idSegnalazione; }
    public void setIdSegnalazione(String idSegnalazione) { this.idSegnalazione = idSegnalazione; }
    public String getIdContenuto() { return idContenuto; }
    public void setIdContenuto(String idContenuto) { this.idContenuto = idContenuto; }
    public String getUsernameSegnalante() { return usernameSegnalante; }
    public void setUsernameSegnalante(String usernameSegnalante) { this.usernameSegnalante = usernameSegnalante; }
    public String getMotivazione() { return motivazione; }
    public void setMotivazione(String motivazione) { this.motivazione = motivazione; }
    public String getStato() { return stato; }
    public void setStato(String stato) { this.stato = stato; }
    public LocalDateTime getDataSegnalazione() { return dataSegnalazione; }
    public void setDataSegnalazione(LocalDateTime dataSegnalazione) { this.dataSegnalazione = dataSegnalazione; }
}
