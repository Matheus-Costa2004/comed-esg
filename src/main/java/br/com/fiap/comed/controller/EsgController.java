package br.com.fiap.comed.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.fiap.comed.dto.IndicadoresEsg;
import br.com.fiap.comed.service.IndicadoresEsgService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/esg")
@Tag(name = "Indicadores ESG")
public class EsgController {

    private final IndicadoresEsgService service;

    public EsgController(IndicadoresEsgService service) {
        this.service = service;
    }

    @GetMapping("/indicadores")
    @Operation(summary = "Indicadores ambientais, sociais e de governança consolidados")
    public IndicadoresEsg indicadores() {
        return service.calcular();
    }
}
