package com.studyhub.models;

public class Valutazione {
    private int id;
    private int stelle;
    private String username;
    private int contenutoId;

    public Valutazione() {}

    public Valutazione(int id, int stelle, String username, int contenutoId) {
        this.id = id;
        this.stelle = stelle;
        this.username = username;
        this.contenutoId = contenutoId;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getStelle() { return stelle; }
    public void setStelle(int stelle) { this.stelle = stelle; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public int getContenutoId() { return contenutoId; }
    public void setContenutoId(int contenutoId) { this.contenutoId = contenutoId; }
}
