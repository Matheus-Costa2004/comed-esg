package br.com.fiap.comed.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.fiap.comed.model.Medico;
import br.com.fiap.comed.service.MedicoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/medicos")
@Tag(name = "Médicos", description = "Pilar Governança: credenciamento e compliance")
public class MedicoController {

    private final MedicoService service;

    public MedicoController(MedicoService service) {
        this.service = service;
    }

    @GetMapping
    public List<Medico> listar() {
        return service.listar();
    }

    @GetMapping("/pendencias-compliance")
    @Operation(summary = "Médicos com status de compliance diferente de regular")
    public List<Medico> pendenciasCompliance() {
        return service.pendenciasCompliance();
    }

    @GetMapping("/{id}")
    public Medico buscar(@PathVariable String id) {
        return service.buscar(id);
    }

    @PostMapping
    public ResponseEntity<Medico> criar(@Valid @RequestBody Medico medico) {
        Medico criado = service.criar(medico);
        return ResponseEntity.created(PacienteController.localDe(criado.getId())).body(criado);
    }

    @PutMapping("/{id}")
    public Medico atualizar(@PathVariable String id, @Valid @RequestBody Medico medico) {
        return service.atualizar(id, medico);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable String id) {
        service.excluir(id);
        return ResponseEntity.noContent().build();
    }
}
