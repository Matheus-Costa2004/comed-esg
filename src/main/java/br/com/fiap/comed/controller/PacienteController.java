package br.com.fiap.comed.controller;

import java.net.URI;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import br.com.fiap.comed.model.Paciente;
import br.com.fiap.comed.service.PacienteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/pacientes")
@Tag(name = "Pacientes", description = "Pilar Social: perfil clínico e vulnerabilidade social")
public class PacienteController {

    private final PacienteService service;

    public PacienteController(PacienteService service) {
        this.service = service;
    }

    @GetMapping
    public List<Paciente> listar() {
        return service.listar();
    }

    @GetMapping("/vulneraveis")
    @Operation(summary = "Pacientes em situação de vulnerabilidade social")
    public List<Paciente> vulneraveis() {
        return service.vulneraveis();
    }

    @GetMapping("/{id}")
    public Paciente buscar(@PathVariable String id) {
        return service.buscar(id);
    }

    @PostMapping
    public ResponseEntity<Paciente> criar(@Valid @RequestBody Paciente paciente) {
        Paciente criado = service.criar(paciente);
        return ResponseEntity.created(localDe(criado.getId())).body(criado);
    }

    @PutMapping("/{id}")
    public Paciente atualizar(@PathVariable String id, @Valid @RequestBody Paciente paciente) {
        return service.atualizar(id, paciente);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Exclui o paciente e registra exclusao_dados na auditoria LGPD")
    public ResponseEntity<Void> excluir(@PathVariable String id,
            @RequestParam(defaultValue = "sistema") String solicitante) {
        service.excluir(id, solicitante);
        return ResponseEntity.noContent().build();
    }

    static URI localDe(String id) {
        return ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(id).toUri();
    }
}
