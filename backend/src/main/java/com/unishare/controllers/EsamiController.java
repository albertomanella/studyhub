package com.unishare.controllers;

import com.unishare.models.Esame;
import com.unishare.storage.InMemoryStore;
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
