package br.com.fiap.comed.service;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import br.com.fiap.comed.exception.RecursoNaoEncontradoException;
import br.com.fiap.comed.model.Paciente;
import br.com.fiap.comed.model.TipoEventoAuditoria;
import br.com.fiap.comed.repository.PacienteRepository;

@Service
public class PacienteService {

    private final PacienteRepository repository;
    private final AuditoriaService auditoria;

    public PacienteService(PacienteRepository repository, AuditoriaService auditoria) {
        this.repository = repository;
        this.auditoria = auditoria;
    }

    public List<Paciente> listar() {
        return repository.findAll();
    }

    public Paciente buscar(String id) {
        return repository.findById(id).orElseThrow(() -> new RecursoNaoEncontradoException("Paciente", id));
    }

    public List<Paciente> vulneraveis() {
        return repository.findByVulnerabilidadeSocialTrue();
    }

    public Paciente criar(Paciente paciente) {
        paciente.setId(null);
        paciente.setDataCadastro(Instant.now());
        return repository.save(paciente);
    }

    public Paciente atualizar(String id, Paciente dados) {
        Paciente atual = buscar(id);
        dados.setId(id);
        dados.setDataCadastro(atual.getDataCadastro());
        return repository.save(dados);
    }

    /** Exclusão de dados pessoais sempre deixa rastro na trilha LGPD. */
    public void excluir(String id, String solicitante) {
        Paciente paciente = buscar(id);
        repository.delete(paciente);
        auditoria.registrar(TipoEventoAuditoria.exclusao_dados, solicitante, paciente.getNome(),
                "Dados do paciente excluídos via API", Map.of("pacienteId", id));
    }
}
