package com.studyhub.storage;

import com.studyhub.models.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class MockDataInitializer implements CommandLineRunner {

    @Override
    public void run(String... args) {
        CorsoDiStudio corso = new CorsoDiStudio("Ingegneria", new Ateneo("Unibo"));

        // Esami — 1° anno
        String[][] primo = {
                { "Fondamenti di Informatica T-1" },
                { "Fondamenti di Informatica T-2" },
                { "Analisi Matematica T-1" },
                { "Analisi Matematica T-2" },
                { "Reti Logiche T" },
                { "Geometria e Algebra T" }
        };
        for (String[] e : primo)
            InMemoryStore.esami.add(new Esame(e[0], 1, corso));

        // Esami — 2° anno
        String[][] secondo = {
                { "Elettrotecnica T" },
                { "Sistemi Informativi T" },
                { "Sistemi Operativi T" },
                { "Calcolatori Elettronici T" },
                { "Fisica Generale T" },
                { "Economia Aziendale T" },
                { "Matematica Applicata T" },
                { "Fondamenti di Telecomunicazioni T" }
        };
        for (String[] e : secondo)
            InMemoryStore.esami.add(new Esame(e[0], 2, corso));

        // Esami — 3° anno
        String[][] terzo = {
                { "Reti di Calcolatori T" },
                { "Tecnologie Web T" },
                { "Elettronica T" },
                { "Controlli Automatici T" },
                { "Ingegneria del Software T" },
                { "Diritto dell'Informatica T" },
                { "Sicurezza Informatica T" },
                { "Progettazione Applicazioni Web T" },
                { "Amministrazione di Sistemi T" }
        };
        for (String[] e : terzo)
            InMemoryStore.esami.add(new Esame(e[0], 3, corso));

        System.out.println("MockDataInitializer: dati caricati in memoria.");
    }
}
