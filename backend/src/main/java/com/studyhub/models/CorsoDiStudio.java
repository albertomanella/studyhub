package com.studyhub.models;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "corso_di_studio")
public class CorsoDiStudio {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String nomeCorso;

    @ManyToOne(optional = false)
    @JoinColumn(name = "ateneo_id", nullable = false)
    private Ateneo ateneo;

    @OneToMany(mappedBy = "corso", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private List<Esame> esami = new ArrayList<>();

    public Integer getId() {
        return id;
    }

    public String getNomeCorso() {
        return nomeCorso;
    }

    public Ateneo getAteneo() {
        return ateneo;
    }

    public List<Esame> getEsami() {
        return esami;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public void setNomeCorso(String nomeCorso) {
        this.nomeCorso = nomeCorso;
    }

    public void setAteneo(Ateneo ateneo) {
        this.ateneo = ateneo;
    }

    public void setEsami(List<Esame> esami) {
        List<Esame> tmp = new ArrayList<>(this.esami);
        for (Esame e : tmp) {
            this.removeEsame(e);
        }

        for (Esame e : esami) {
            this.addEsame(e);
        }
    }

    public void addEsame(Esame esame) {
        this.esami.add(esame);
        esame.setCorso(this);
    }

    public void removeEsame(Esame esame) {
        this.esami.remove(esame);
        esame.setCorso(null);
    }

    public CorsoDiStudio() {
    }

    public CorsoDiStudio(String nomeCorso, Ateneo ateneo) {
        this.nomeCorso = nomeCorso;
        this.ateneo = ateneo;
    }

}