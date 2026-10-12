package br.com.fiap.comed.service;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import br.com.fiap.comed.exception.RecursoNaoEncontradoException;
import br.com.fiap.comed.model.EventoAuditoria;
import br.com.fiap.comed.model.TipoEventoAuditoria;
import br.com.fiap.comed.repository.EventoAuditoriaRepository;

/** Trilha LGPD append-only: só existe inserção e consulta. */
@Service
public class AuditoriaService {

    private static final Sort MAIS_RECENTES_PRIMEIRO = Sort.by(Sort.Direction.DESC, "dataHora");

    private final EventoAuditoriaRepository repository;

    public AuditoriaService(EventoAuditoriaRepository repository) {
        this.repository = repository;
    }

    public List<EventoAuditoria> listar() {
        return repository.findAll(MAIS_RECENTES_PRIMEIRO);
    }

    public EventoAuditoria buscar(String id) {
        return repository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Evento de auditoria", id));
    }

    public List<EventoAuditoria> porPaciente(String nome) {
        return repository.findByPacienteOrderByDataHoraDesc(nome);
    }

    /** O id e o horário são sempre definidos pelo servidor, nunca pelo cliente. */
    public EventoAuditoria registrar(EventoAuditoria evento) {
        evento.setId(null);
        evento.setDataHora(Instant.now());
        return repository.save(evento);
    }

    public EventoAuditoria registrar(TipoEventoAuditoria tipo, String usuario, String paciente, String descricao,
            Map<String, Object> detalhes) {
        return registrar(new EventoAuditoria(tipo, usuario, paciente, descricao, detalhes));
    }
}
