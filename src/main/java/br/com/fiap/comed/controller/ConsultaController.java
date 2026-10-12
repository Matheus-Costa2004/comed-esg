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

import br.com.fiap.comed.model.Consulta;
import br.com.fiap.comed.service.ConsultaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/consultas")
@Tag(name = "Consultas", description = "Pilares Social e Ambiental: telemedicina e presencial")
public class ConsultaController {

    private final ConsultaService service;

    public ConsultaController(ConsultaService service) {
        this.service = service;
    }

    @GetMapping
    public List<Consulta> listar() {
        return service.listar();
    }

    @GetMapping("/{id}")
    public Consulta buscar(@PathVariable String id) {
        return service.buscar(id);
    }

    @PostMapping
    @Operation(summary = "Cria consulta; em telemedicina sem co2EvitadoKg, o CO2 é calculado pela distância")
    public ResponseEntity<Consulta> criar(@Valid @RequestBody Consulta consulta) {
        Consulta criada = service.criar(consulta);
        return ResponseEntity.created(PacienteController.localDe(criada.getId())).body(criada);
    }

    @PutMapping("/{id}")
    public Consulta atualizar(@PathVariable String id, @Valid @RequestBody Consulta consulta) {
        return service.atualizar(id, consulta);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable String id) {
        service.excluir(id);
        return ResponseEntity.noContent().build();
    }
}
