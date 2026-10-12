package br.com.fiap.comed.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import br.com.fiap.comed.model.AlertaIa;
import br.com.fiap.comed.service.AlertaIaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;

@RestController
@RequestMapping("/api/alertas")
@Tag(name = "Alertas de IA", description = "Pilar Governança: alertas da IA com revisão médica obrigatória")
public class AlertaIaController {

    private final AlertaIaService service;

    public AlertaIaController(AlertaIaService service) {
        this.service = service;
    }

    @GetMapping
    public List<AlertaIa> listar() {
        return service.listar();
    }

    @GetMapping("/pendentes")
    @Operation(summary = "Alertas de risco alto ainda não revisados por um médico")
    public List<AlertaIa> pendentes() {
        return service.pendentes();
    }

    @GetMapping("/{id}")
    public AlertaIa buscar(@PathVariable String id) {
        return service.buscar(id);
    }

    @PostMapping
    public ResponseEntity<AlertaIa> criar(@Valid @RequestBody AlertaIa alerta) {
        AlertaIa criado = service.criar(alerta);
        return ResponseEntity.created(PacienteController.localDe(criado.getId())).body(criado);
    }

    @PutMapping("/{id}")
    public AlertaIa atualizar(@PathVariable String id, @Valid @RequestBody AlertaIa alerta) {
        return service.atualizar(id, alerta);
    }

    @PatchMapping("/{id}/revisao")
    @Operation(summary = "Marca o alerta como revisado e registra revisao_alerta_ia na auditoria")
    public AlertaIa revisar(@PathVariable String id, @RequestParam @NotBlank String medico) {
        return service.revisar(id, medico);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable String id) {
        service.excluir(id);
        return ResponseEntity.noContent().build();
    }
}
