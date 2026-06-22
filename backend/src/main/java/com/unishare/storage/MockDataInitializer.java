package com.unishare.storage;

import com.unishare.models.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Component
public class MockDataInitializer implements CommandLineRunner {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

    @Override
    public void run(String... args) {
        // Utenti
        InMemoryStore.utenti.add(new Utente(InMemoryStore.nextId(), "mario", "mario123!", "Mario", "Rossi"));
        InMemoryStore.utenti.add(new Utente(InMemoryStore.nextId(), "giulia", "giulia123!", "Giulia", "Bianchi"));

        // Esami
        String[] nomiEsami = {
            "Analisi Matematica", "Algebra Lineare", "Fisica", "Programmazione",
            "Algoritmi e Strutture Dati", "Basi di Dati", "Reti di Calcolatori",
            "Sistemi Operativi", "Ingegneria del Software", "Architettura degli Elaboratori"
        };
        for (String nome : nomiEsami) {
            InMemoryStore.esami.add(new Esame(InMemoryStore.nextId(), nome));
        }

        // ID esami: 3=Analisi Matematica, 4=Algebra Lineare, 5=Fisica, 6=Programmazione, ecc.
        // (nextId parte da 1, 2 utenti => id 3..12 per esami)
        int esameAnalisiId = 3;
        int esameAlgebraId = 4;
        int esameFisicaId = 5;
        int esameProgId = 6;

        String data1 = LocalDateTime.now().minusDays(10).format(FMT);
        String data2 = LocalDateTime.now().minusDays(5).format(FMT);
        String data3 = LocalDateTime.now().minusDays(3).format(FMT);
        String data4 = LocalDateTime.now().minusDays(1).format(FMT);

        // Documenti
        int docId1 = InMemoryStore.nextId(); // 13
        InMemoryStore.contenuti.add(new Contenuto(docId1, "documento",
                "Appunti completi Analisi Matematica",
                "Appunti dettagliati su limiti, derivate, integrali e serie numeriche. Coprono l'intero programma del corso con esempi svolti e dimostrazioni.",
                null, "appunti_analisi.pdf", "mario", esameAnalisiId, data1));

        int docId2 = InMemoryStore.nextId(); // 14
        InMemoryStore.contenuti.add(new Contenuto(docId2, "documento",
                "Esercizi svolti Programmazione",
                "Raccolta di esercizi svolti in Java: strutture dati, ricorsione, algoritmi di ordinamento e gestione delle eccezioni.",
                null, "esercizi_prog.pdf", "giulia", esameProgId, data2));

        // Consigli
        int consiglioId1 = InMemoryStore.nextId(); // 15
        InMemoryStore.contenuti.add(new Contenuto(consiglioId1, "consiglio",
                null,
                "Il professore di Algebra Lineare fa spesso domande sulle trasformazioni lineari all'orale. Studiate bene il nucleo e l'immagine con esempi pratici!",
                "Professore", null, "giulia", esameAlgebraId, data3));

        int consiglioId2 = InMemoryStore.nextId(); // 16
        InMemoryStore.contenuti.add(new Contenuto(consiglioId2, "consiglio",
                null,
                "Per Fisica conviene guardare le prove degli anni precedenti: il 70% degli esercizi ricalca gli stessi schemi. Trovate tutto sul portale studenti.",
                "Esame", null, "mario", esameFisicaId, data4));

        // Valutazioni
        // giulia valuta il documento di mario (Analisi): 5 stelle
        InMemoryStore.valutazioni.add(new Valutazione(InMemoryStore.nextId(), 5, "giulia", docId1));
        // mario valuta il documento di giulia (Programmazione): 4 stelle
        InMemoryStore.valutazioni.add(new Valutazione(InMemoryStore.nextId(), 4, "mario", docId2));
        // mario valuta il consiglio di giulia (Algebra): 4 stelle
        InMemoryStore.valutazioni.add(new Valutazione(InMemoryStore.nextId(), 4, "mario", consiglioId1));
        // giulia valuta il consiglio di mario (Fisica): 5 stelle
        InMemoryStore.valutazioni.add(new Valutazione(InMemoryStore.nextId(), 5, "giulia", consiglioId2));

        System.out.println("MockDataInitializer: dati caricati in memoria.");
    }
}
