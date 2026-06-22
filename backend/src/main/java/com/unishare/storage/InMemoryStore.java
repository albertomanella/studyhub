package com.unishare.storage;

import com.unishare.models.*;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.OptionalDouble;

@Component
public class InMemoryStore {

    public static List<Utente> utenti = new ArrayList<>();
    public static List<Esame> esami = new ArrayList<>();
    public static List<Contenuto> contenuti = new ArrayList<>();
    public static List<Valutazione> valutazioni = new ArrayList<>();
    public static int nextId = 1;

    public static synchronized int nextId() {
        return nextId++;
    }

    public static double mediaStelle(int contenutoId) {
        OptionalDouble media = valutazioni.stream()
                .filter(v -> v.getContenutoId() == contenutoId)
                .mapToInt(Valutazione::getStelle)
                .average();
        return media.isPresent() ? Math.round(media.getAsDouble() * 10.0) / 10.0 : 0.0;
    }

    public static int numValutazioni(int contenutoId) {
        return (int) valutazioni.stream()
                .filter(v -> v.getContenutoId() == contenutoId)
                .count();
    }

    public static int valutazioneUtente(String username, int contenutoId) {
        return valutazioni.stream()
                .filter(v -> v.getUsername().equals(username) && v.getContenutoId() == contenutoId)
                .mapToInt(Valutazione::getStelle)
                .findFirst()
                .orElse(-1);
    }
}
