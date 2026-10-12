package br.com.fiap.comed.service;

import java.util.List;

import org.springframework.stereotype.Service;

import br.com.fiap.comed.exception.RecursoNaoEncontradoException;
import br.com.fiap.comed.model.Medico;
import br.com.fiap.comed.model.StatusCompliance;
import br.com.fiap.comed.repository.MedicoRepository;

@Service
public class MedicoService {

    private final MedicoRepository repository;

    public MedicoService(MedicoRepository repository) {
        this.repository = repository;
    }

    public List<Medico> listar() {
        return repository.findAll();
    }

    public Medico buscar(String id) {
        return repository.findById(id).orElseThrow(() -> new RecursoNaoEncontradoException("Médico", id));
    }

    /** Médicos com status diferente de regular (pendente_renovacao ou suspenso). */
    public List<Medico> pendenciasCompliance() {
        return repository.findByStatusComplianceNot(StatusCompliance.regular);
    }

    public Medico criar(Medico medico) {
        medico.setId(null);
        return repository.save(medico);
    }

    public Medico atualizar(String id, Medico dados) {
        buscar(id);
        dados.setId(id);
        return repository.save(dados);
    }

    public void excluir(String id) {
        repository.delete(buscar(id));
    }
}
