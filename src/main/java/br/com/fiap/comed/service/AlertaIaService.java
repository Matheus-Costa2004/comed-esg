package br.com.fiap.comed.service;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import br.com.fiap.comed.exception.ConflitoException;
import br.com.fiap.comed.exception.RecursoNaoEncontradoException;
import br.com.fiap.comed.model.AlertaIa;
import br.com.fiap.comed.model.NivelRisco;
import br.com.fiap.comed.model.TipoEventoAuditoria;
import br.com.fiap.comed.repository.AlertaIaRepository;

@Service
public class AlertaIaService {

    private final AlertaIaRepository repository;
    private final AuditoriaService auditoria;

    public AlertaIaService(AlertaIaRepository repository, AuditoriaService auditoria) {
        this.repository = repository;
        this.auditoria = auditoria;
    }

    public List<AlertaIa> listar() {
        return repository.findAll();
    }

    public AlertaIa buscar(String id) {
        return repository.findById(id).orElseThrow(() -> new RecursoNaoEncontradoException("Alerta", id));
    }

    /** Alertas de risco alto que ainda aguardam um médico. */
    public List<AlertaIa> pendentes() {
        return repository.findByNivelRiscoAndRevisadoPorMedicoFalse(NivelRisco.alto);
    }

    /** Todo alerta novo começa sem revisão: só o PATCH de revisão muda isso. */
    public AlertaIa criar(AlertaIa alerta) {
        alerta.setId(null);
        alerta.setDataGeracao(Instant.now());
        alerta.setRevisadoPorMedico(false);
        alerta.setMedicoRevisor(null);
        alerta.setDataRevisao(null);
        return repository.save(alerta);
    }

    /** Atualiza o conteúdo do alerta, preservando a data de geração e a revisão. */
    public AlertaIa atualizar(String id, AlertaIa dados) {
        AlertaIa atual = buscar(id);
        dados.setId(id);
        dados.setDataGeracao(atual.getDataGeracao());
        dados.setRevisadoPorMedico(atual.isRevisadoPorMedico());
        dados.setMedicoRevisor(atual.getMedicoRevisor());
        dados.setDataRevisao(atual.getDataRevisao());
        return repository.save(dados);
    }

    public void excluir(String id) {
        repository.delete(buscar(id));
    }

    /** Humano-no-loop: o médico confirma o alerta e a revisão vai para a trilha de auditoria. */
    public AlertaIa revisar(String id, String medico) {
        AlertaIa alerta = buscar(id);
        if (alerta.isRevisadoPorMedico()) {
            throw new ConflitoException("Alerta " + id + " já foi revisado por " + alerta.getMedicoRevisor());
        }
        alerta.setRevisadoPorMedico(true);
        alerta.setMedicoRevisor(medico);
        alerta.setDataRevisao(Instant.now());
        AlertaIa revisado = repository.save(alerta);

        auditoria.registrar(TipoEventoAuditoria.revisao_alerta_ia, medico, alerta.getPaciente(),
                "Alerta de IA revisado pelo médico",
                Map.of("alertaId", id, "tipo", alerta.getTipo().name(), "nivelRisco", alerta.getNivelRisco().name()));
        return revisado;
    }
}
