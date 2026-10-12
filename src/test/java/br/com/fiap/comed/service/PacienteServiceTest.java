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

import br.com.fiap.comed.exception.RecursoNaoEncontradoException;
import br.com.fiap.comed.model.Paciente;
import br.com.fiap.comed.model.TipoEventoAuditoria;
import br.com.fiap.comed.repository.PacienteRepository;

@ExtendWith(MockitoExtension.class)
class PacienteServiceTest {

    @Mock
    private PacienteRepository repository;

    @Mock
    private AuditoriaService auditoria;

    @InjectMocks
    private PacienteService service;

    @Test
    void listarDevolveTodosOsPacientes() {
        when(repository.findAll()).thenReturn(List.of(paciente("p1", "Ana"), paciente("p2", "Carlos")));

        assertThat(service.listar()).extracting(Paciente::getNome).containsExactly("Ana", "Carlos");
    }

    @Test
    void buscarDevolveOPacienteExistente() {
        when(repository.findById("p1")).thenReturn(Optional.of(paciente("p1", "Ana")));

        assertThat(service.buscar("p1").getNome()).isEqualTo("Ana");
    }

    @Test
    void buscarInexistenteLancaRecursoNaoEncontrado() {
        when(repository.findById("x")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.buscar("x"))
                .isInstanceOf(RecursoNaoEncontradoException.class)
                .hasMessageContaining("x");
    }

    @Test
    void criarIgnoraIdDoClienteEDefineDataDeCadastro() {
        Paciente novo = paciente("id-do-cliente", "Ana");
        when(repository.save(any(Paciente.class))).thenAnswer(i -> i.getArgument(0));

        Paciente salvo = service.criar(novo);

        assertThat(salvo.getId()).isNull();
        assertThat(salvo.getDataCadastro()).isNotNull();
    }

    @Test
    void atualizarPreservaIdEDataDeCadastro() {
        Paciente atual = paciente("p1", "Ana");
        Instant cadastro = Instant.parse("2026-08-01T12:00:00Z");
        atual.setDataCadastro(cadastro);
        when(repository.findById("p1")).thenReturn(Optional.of(atual));
        when(repository.save(any(Paciente.class))).thenAnswer(i -> i.getArgument(0));

        Paciente salvo = service.atualizar("p1", paciente(null, "Ana Luiza"));

        assertThat(salvo.getId()).isEqualTo("p1");
        assertThat(salvo.getNome()).isEqualTo("Ana Luiza");
        assertThat(salvo.getDataCadastro()).isEqualTo(cadastro);
    }

    @Test
    void atualizarInexistenteNaoSalva() {
        when(repository.findById("x")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.atualizar("x", paciente(null, "Ana")))
                .isInstanceOf(RecursoNaoEncontradoException.class);
        verify(repository, never()).save(any());
    }

    @Test
    void excluirRegistraExclusaoDeDadosNaAuditoria() {
        Paciente carlos = paciente("p2", "Carlos Mendes");
        when(repository.findById("p2")).thenReturn(Optional.of(carlos));

        service.excluir("p2", "Dr. Rafael Costa");

        verify(repository).delete(carlos);
        verify(auditoria).registrar(TipoEventoAuditoria.exclusao_dados, "Dr. Rafael Costa", "Carlos Mendes",
                "Dados do paciente excluídos via API", Map.of("pacienteId", "p2"));
    }

    @Test
    void excluirInexistenteNaoRegistraAuditoria() {
        when(repository.findById("x")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.excluir("x", "sistema"))
                .isInstanceOf(RecursoNaoEncontradoException.class);
        verify(auditoria, never()).registrar(any(), anyString(), anyString(), anyString(), anyMap());
        verify(repository, never()).delete(any());
    }

    @Test
    void vulneraveisDelegaParaORepositorio() {
        Paciente fernanda = paciente("p8", "Fernanda Lima");
        fernanda.setVulnerabilidadeSocial(true);
        when(repository.findByVulnerabilidadeSocialTrue()).thenReturn(List.of(fernanda));

        assertThat(service.vulneraveis()).containsExactly(fernanda);
        verify(repository, never()).findAll();
    }

    static Paciente paciente(String id, String nome) {
        Paciente p = new Paciente();
        p.setId(id);
        p.setNome(nome);
        p.setIdade(50);
        p.setCondicoes(List.of("Hipertensão"));
        return p;
    }
}
