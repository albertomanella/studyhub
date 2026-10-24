package com.studyhub.controllers;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

// I cookie impostati da Spring Security non sono osservabili in modo affidabile
// con MockMvc: questo test avvia il server su una porta casuale e fa una vera
// richiesta HTTP, come il browser.
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class TestCookieHttp {

    @LocalServerPort
    private int port;

    @Test
    public void testRichiestaAnonima_riceveCookieCsrfLeggibileENessunaSessione() throws Exception {
        HttpResponse<Void> risposta = HttpClient.newHttpClient().send(
                HttpRequest.newBuilder(URI.create("http://127.0.0.1:" + port + "/api/auth/me")).build(),
                HttpResponse.BodyHandlers.discarding());

        assertEquals(401, risposta.statusCode());
        List<String> cookie = risposta.headers().allValues("Set-Cookie");
        // Il frontend deve poter leggere il token CSRF già prima del login...
        assertTrue(cookie.stream().anyMatch(c -> c.startsWith("XSRF-TOKEN=") && !c.contains("HttpOnly")));
        // ...ma una sessione nasce solo al login
        assertTrue(cookie.stream().noneMatch(c -> c.startsWith("JSESSIONID=")));
    }
}
