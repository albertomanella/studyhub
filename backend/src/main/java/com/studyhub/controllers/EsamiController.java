package com.studyhub.controllers;

import com.studyhub.dto.EsameDTO;
import com.studyhub.models.Esame;
import com.studyhub.repositories.EsameRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/esami")
public class EsamiController {

    private final EsameRepository esameRepository;

    public EsamiController(EsameRepository esameRepository) {
        this.esameRepository = esameRepository;
    }

    @GetMapping
    public List<EsameDTO> getEsami() {
        List<Esame> esami = esameRepository.findAll();
        List<EsameDTO> dto = esami.stream().map(esame -> new EsameDTO(
                esame.getId(),
                esame.getNome(),
                esame.getAnno(),
                esame.getCorso().getNomeCorso())).collect(Collectors.toList());
        return dto;
    }
}
