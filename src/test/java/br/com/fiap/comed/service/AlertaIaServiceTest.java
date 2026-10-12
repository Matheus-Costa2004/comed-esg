package br.com.fiap.comed.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import br.com.fiap.comed.exception.ConflitoException;
import br.com.fiap.comed.exception.RecursoNaoEncontradoException;
import br.com.fiap.comed.model.AlertaIa;
import br.com.fiap.comed.model.NivelRisco;
import br.com.fiap.comed.model.TipoAlerta;
import br.com.fiap.comed.model.TipoEventoAuditoria;
import br.com.fiap.comed.repository.AlertaIaRepository;

@ExtendWith(MockitoExtension.class)
class AlertaIaServiceTest {

    @Mock
    private AlertaIaRepository repository;

    @Mock
    private AuditoriaService auditoria;

    @InjectMocks
    private AlertaIaService service;

    @Test
    void criarSempreComecaSemRevisao() {
        AlertaIa alerta = alerta("id-do-cliente");
        alerta.setRevisadoPorMedico(true);
        alerta.setMedicoRevisor("alguém");
        when(repository.save(any(AlertaIa.class))).thenAnswer(i -> i.getArgument(0));

        AlertaIa salvo = service.criar(alerta);

        assertThat(salvo.getId()).isNull();
        assertThat(salvo.isRevisadoPorMedico()).isFalse();
        assertThat(salvo.getMedicoRevisor()).isNull();
        assertThat(salvo.getDataGeracao()).isNotNull();
    }

    @Test
    void pendentesSaoOsDeRiscoAltoNaoRevisados() {
        AlertaIa helena = alerta("a3");
        when(repository.findByNivelRiscoAndRevisadoPorMedicoFalse(NivelRisco.alto)).thenReturn(List.of(helena));

        assertThat(service.pendentes()).containsExactly(helena);
    }

    @Test
    void revisarMarcaOAlertaERegistraAuditoria() {
        AlertaIa helena = alerta("a3");
        when(repository.findById("a3")).thenReturn(Optional.of(helena));
        when(repository.save(any(AlertaIa.class))).thenAnswer(i -> i.getArgument(0));

        AlertaIa revisado = service.revisar("a3", "Dra. Juliana Reis");

        assertThat(revisado.isRevisadoPorMedico()).isTrue();
        assertThat(revisado.getMedicoRevisor()).isEqualTo("Dra. Juliana Reis");
        assertThat(revisado.getDataRevisao()).isNotNull();
        verify(auditoria).registrar(TipoEventoAuditoria.revisao_alerta_ia, "Dra. Juliana Reis", "Helena Duarte",
                "Alerta de IA revisado pelo médico",
                Map.of("alertaId", "a3", "tipo", "tendencia_hba1c", "nivelRisco", "alto"));
    }

    @Test
    void revisarAlertaJaRevisadoEhConflitoESemNovaAuditoria() {
        AlertaIa carlos = alerta("a1");
        carlos.setRevisadoPorMedico(true);
        carlos.setMedicoRevisor("Dra. Renata Xavier");
        when(repository.findById("a1")).thenReturn(Optional.of(carlos));

        assertThatThrownBy(() -> service.revisar("a1", "Dr. Rafael Costa"))
                .isInstanceOf(ConflitoException.class)
                .hasMessageContaining("Dra. Renata Xavier");
        verify(repository, never()).save(any());
        verify(auditoria, never()).registrar(any(), anyString(), anyString(), anyString(), anyMap());
    }

    @Test
    void revisarInexistenteLancaRecursoNaoEncontrado() {
        when(repository.findById("x")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.revisar("x", "Dr. Rafael Costa"))
                .isInstanceOf(RecursoNaoEncontradoException.class);
    }

    @Test
    void atualizarNaoMexeNaRevisaoNemNaDataDeGeracao() {
        AlertaIa atual = alerta("a1");
        Instant geracao = Instant.parse("2026-09-08T08:10:00Z");
        atual.setDataGeracao(geracao);
        atual.setRevisadoPorMedico(true);
        atual.setMedicoRevisor("Dra. Renata Xavier");
        when(repository.findById("a1")).thenReturn(Optional.of(atual));
        when(repository.save(any(AlertaIa.class))).thenAnswer(i -> i.getArgument(0));

        AlertaIa dados = alerta(null);
        dados.setDescricao("Descrição corrigida");
        AlertaIa salvo = service.atualizar("a1", dados);

        assertThat(salvo.getId()).isEqualTo("a1");
        assertThat(salvo.getDescricao()).isEqualTo("Descrição corrigida");
        assertThat(salvo.getDataGeracao()).isEqualTo(geracao);
        assertThat(salvo.isRevisadoPorMedico()).isTrue();
        assertThat(salvo.getMedicoRevisor()).isEqualTo("Dra. Renata Xavier");
    }

    @Test
    void excluirRemoveOAlerta() {
        AlertaIa alerta = alerta("a1");
        when(repository.findById("a1")).thenReturn(Optional.of(alerta));

        service.excluir("a1");

        verify(repository).delete(alerta);
    }

    static AlertaIa alerta(String id) {
        AlertaIa a = new AlertaIa();
        a.setId(id);
        a.setPaciente("Helena Duarte");
        a.setTipo(TipoAlerta.tendencia_hba1c);
        a.setNivelRisco(NivelRisco.alto);
        a.setDescricao("HbA1c em alta");
        return a;
    }
}
