package com.studyhub.models;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "ateneo")
public class Ateneo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private String nomeAteneo;
    @OneToMany(mappedBy = "ateneo", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private List<CorsoDiStudio> corsi = new ArrayList<>();

    public Ateneo(String nomeAteneo) {
        this.nomeAteneo = nomeAteneo;
    }

    public Ateneo() {
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getNomeAteneo() {
        return nomeAteneo;
    }

    public void setNomeAteneo(String nomeAteneo) {
        this.nomeAteneo = nomeAteneo;
    }

    public List<CorsoDiStudio> getCorsi() {
        return corsi;
    }

    public void addCorso(CorsoDiStudio corso) {
        this.corsi.add(corso);
        corso.setAteneo(this);
    }

    public void removeCorso(CorsoDiStudio corso) {
        this.corsi.remove(corso);
        corso.setAteneo(null);
    }

    public void setCorsi(List<CorsoDiStudio> corsi) {
        List<CorsoDiStudio> tmp = new ArrayList<>(this.corsi);

        for (CorsoDiStudio c : tmp) {
            this.removeCorso(c);
        }

        for (CorsoDiStudio c : corsi) {
            this.addCorso(c);
        }
    }

}
