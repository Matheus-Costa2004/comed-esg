package br.com.fiap.comed.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.fiap.comed.model.EventoAuditoria;
import br.com.fiap.comed.service.AuditoriaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

/** Só GET e POST: a trilha de auditoria não pode ser alterada nem apagada pela API. */
@RestController
@RequestMapping("/api/auditoria")
@Tag(name = "Auditoria LGPD", description = "Pilar Governança: trilha append-only")
public class AuditoriaController {

    private final AuditoriaService service;

    public AuditoriaController(AuditoriaService service) {
        this.service = service;
    }

    @GetMapping
    @Operation(summary = "Eventos de auditoria, do mais recente para o mais antigo")
    public List<EventoAuditoria> listar() {
        return service.listar();
    }

    @GetMapping("/{id}")
    public EventoAuditoria buscar(@PathVariable String id) {
        return service.buscar(id);
    }

    @GetMapping("/paciente/{nome}")
    @Operation(summary = "Eventos de auditoria de um titular de dados")
    public List<EventoAuditoria> porPaciente(@PathVariable String nome) {
        return service.porPaciente(nome);
    }

    @PostMapping
    public ResponseEntity<EventoAuditoria> registrar(@Valid @RequestBody EventoAuditoria evento) {
        EventoAuditoria registrado = service.registrar(evento);
        return ResponseEntity.created(PacienteController.localDe(registrado.getId())).body(registrado);
    }
}
