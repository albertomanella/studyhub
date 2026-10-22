package com.studyhub.controllers;

import com.studyhub.dto.EtichettaDTO;
import com.studyhub.repositories.EtichettaRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/etichette")
public class EtichetteController {

    private final EtichettaRepository etichettaRepository;

    public EtichetteController(EtichettaRepository etichettaRepository) {
        this.etichettaRepository = etichettaRepository;
    }

    @GetMapping
    public List<EtichettaDTO> getEtichette() {
        return etichettaRepository.findAll().stream()
                .map(e -> new EtichettaDTO(e.getId(), e.getNome()))
                .collect(Collectors.toList());
    }
}
