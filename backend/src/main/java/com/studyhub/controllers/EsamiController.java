package com.studyhub.controllers;

import com.studyhub.models.Esame;
import com.studyhub.storage.InMemoryStore;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/esami")
public class EsamiController {

    @GetMapping
    public List<Esame> getEsami() {
        return InMemoryStore.esami;
    }
}
