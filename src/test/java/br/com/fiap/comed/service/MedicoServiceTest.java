package br.com.fiap.comed.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import br.com.fiap.comed.exception.RecursoNaoEncontradoException;
import br.com.fiap.comed.model.Medico;
import br.com.fiap.comed.model.StatusCompliance;
import br.com.fiap.comed.repository.MedicoRepository;

@ExtendWith(MockitoExtension.class)
class MedicoServiceTest {

    @Mock
    private MedicoRepository repository;

    @InjectMocks
    private MedicoService service;

    @Test
    void criarIgnoraIdDoCliente() {
        Medico rafael = medico("id-do-cliente", "58432-SP", StatusCompliance.regular);
        when(repository.save(any(Medico.class))).thenAnswer(i -> i.getArgument(0));

        assertThat(service.criar(rafael).getId()).isNull();
    }

    @Test
    void pendenciasDeComplianceSaoOsStatusDiferentesDeRegular() {
        Medico renata = medico("m2", "60211-SP", StatusCompliance.pendente_renovacao);
        Medico fabio = medico("m3", "28855-SP", StatusCompliance.suspenso);
        when(repository.findByStatusComplianceNot(StatusCompliance.regular)).thenReturn(List.of(renata, fabio));

        assertThat(service.pendenciasCompliance()).containsExactly(renata, fabio);
    }

    @Test
    void atualizarMantemOIdDoPath() {
        when(repository.findById("m1")).thenReturn(Optional.of(medico("m1", "58432-SP", StatusCompliance.regular)));
        when(repository.save(any(Medico.class))).thenAnswer(i -> i.getArgument(0));

        Medico atualizado = service.atualizar("m1", medico(null, "58432-SP", StatusCompliance.suspenso));

        assertThat(atualizado.getId()).isEqualTo("m1");
        assertThat(atualizado.getStatusCompliance()).isEqualTo(StatusCompliance.suspenso);
    }

    @Test
    void excluirInexistenteLancaRecursoNaoEncontrado() {
        when(repository.findById("x")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.excluir("x")).isInstanceOf(RecursoNaoEncontradoException.class);
        verify(repository, never()).delete(any());
    }

    @Test
    void excluirRemoveOMedico() {
        Medico rafael = medico("m1", "58432-SP", StatusCompliance.regular);
        when(repository.findById("m1")).thenReturn(Optional.of(rafael));

        service.excluir("m1");

        verify(repository).delete(rafael);
    }

    static Medico medico(String id, String crm, StatusCompliance status) {
        Medico m = new Medico();
        m.setId(id);
        m.setNome("Dr. Teste");
        m.setCrm(crm);
        m.setEspecialidade("Clínica Geral");
        m.setStatusCompliance(status);
        return m;
    }
}
