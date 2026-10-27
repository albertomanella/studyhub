package com.studyhub.storage;

import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Locale;
import java.util.Map;

// Decide se un file caricato è ammesso guardando il contenuto, non solo il nome:
// - i formati di documento noti devono iniziare con la loro firma ("magic bytes"),
//   così un eseguibile rinominato in .pdf viene rifiutato;
// - qualunque altro file è ammesso solo se è testo (codice in qualsiasi
//   linguaggio, anche futuro, senza dover aggiornare una lista).
// Legge solo i primi 8000 byte: la memoria usata non dipende dalla dimensione
// del file.
@Component
public class UploadValidator {

    // Stessa soglia usata da git per decidere se un file è binario
    private static final int BYTE_CONTROLLATI = 8000;

    private static final byte[] PDF = { '%', 'P', 'D', 'F' };
    private static final byte[] PNG = { (byte) 0x89, 'P', 'N', 'G', 0x0D, 0x0A, 0x1A, 0x0A };
    private static final byte[] JPEG = { (byte) 0xFF, (byte) 0xD8, (byte) 0xFF };
    // docx, xlsx, pptx sono archivi zip
    private static final byte[] ZIP = { 'P', 'K', 0x03, 0x04 };
    // Formato Office precedente al 2007
    private static final byte[] OFFICE_OLE = { (byte) 0xD0, (byte) 0xCF, 0x11, (byte) 0xE0,
            (byte) 0xA1, (byte) 0xB1, 0x1A, (byte) 0xE1 };

    private static final Map<String, byte[]> FIRME = Map.ofEntries(
            Map.entry("pdf", PDF),
            Map.entry("png", PNG),
            Map.entry("jpg", JPEG),
            Map.entry("jpeg", JPEG),
            Map.entry("zip", ZIP),
            Map.entry("docx", ZIP),
            Map.entry("xlsx", ZIP),
            Map.entry("pptx", ZIP),
            Map.entry("doc", OFFICE_OLE),
            Map.entry("xls", OFFICE_OLE),
            Map.entry("ppt", OFFICE_OLE));

    public void verifica(String nomeOriginale, InputStream contenuto) throws IOException {
        byte[] inizio = contenuto.readNBytes(BYTE_CONTROLLATI);
        if (inizio.length == 0) {
            throw new FileNonAmmessoException("Il file è vuoto");
        }

        String estensione = estensione(nomeOriginale);
        byte[] firma = FIRME.get(estensione);
        if (firma != null) {
            if (!iniziaCon(inizio, firma)) {
                throw new FileNonAmmessoException(
                        "Il contenuto del file non corrisponde a un file ." + estensione);
            }
            return;
        }

        for (byte b : inizio) {
            if (b == 0) {
                throw new FileNonAmmessoException("Tipo di file non ammesso: sono accettati PDF, Word, Excel, "
                        + "PowerPoint, immagini JPG e PNG, archivi ZIP e file di testo o codice");
            }
        }
    }

    private static String estensione(String nome) {
        int punto = nome.lastIndexOf('.');
        return punto < 0 ? "" : nome.substring(punto + 1).toLowerCase(Locale.ROOT);
    }

    private static boolean iniziaCon(byte[] dati, byte[] prefisso) {
        return dati.length >= prefisso.length
                && Arrays.equals(dati, 0, prefisso.length, prefisso, 0, prefisso.length);
    }
}
