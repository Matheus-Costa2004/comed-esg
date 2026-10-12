package br.com.fiap.comed.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;

import br.com.fiap.comed.exception.RecursoNaoEncontradoException;
import br.com.fiap.comed.model.EventoAuditoria;
import br.com.fiap.comed.model.TipoEventoAuditoria;
import br.com.fiap.comed.repository.EventoAuditoriaRepository;

@ExtendWith(MockitoExtension.class)
class AuditoriaServiceTest {

    @Mock
    private EventoAuditoriaRepository repository;

    @InjectMocks
    private AuditoriaService service;

    @Test
    void registrarDefineIdEHorarioNoServidor() {
        EventoAuditoria evento = new EventoAuditoria(TipoEventoAuditoria.consentimento, "Ana Luiza Fonseca",
                "Ana Luiza Fonseca", "Aceite do termo", Map.of("versaoTermo", "3.1"));
        evento.setId("id-forjado");
        evento.setDataHora(Instant.parse("2000-01-01T00:00:00Z"));
        when(repository.save(any(EventoAuditoria.class))).thenAnswer(i -> i.getArgument(0));

        Instant antes = Instant.now();
        EventoAuditoria salvo = service.registrar(evento);

        assertThat(salvo.getId()).isNull();
        assertThat(salvo.getDataHora()).isAfterOrEqualTo(antes);
    }

    @Test
    void registrarPorCamposMontaOEvento() {
        when(repository.save(any(EventoAuditoria.class))).thenAnswer(i -> i.getArgument(0));

        service.registrar(TipoEventoAuditoria.acesso_negado, "Dr. Fábio Andrade", "Helena Duarte",
                "Tentativa de acesso negada", Map.of("motivo", "token expirado"));

        ArgumentCaptor<EventoAuditoria> captor = ArgumentCaptor.forClass(EventoAuditoria.class);
        verify(repository).save(captor.capture());
        EventoAuditoria salvo = captor.getValue();
        assertThat(salvo.getTipoEvento()).isEqualTo(TipoEventoAuditoria.acesso_negado);
        assertThat(salvo.getUsuario()).isEqualTo("Dr. Fábio Andrade");
        assertThat(salvo.getPaciente()).isEqualTo("Helena Duarte");
        assertThat(salvo.getDetalhes()).containsEntry("motivo", "token expirado");
    }

    @Test
    void listarOrdenaDoMaisRecenteParaOMaisAntigo() {
        when(repository.findAll(any(Sort.class))).thenReturn(List.of());

        service.listar();

        verify(repository).findAll(Sort.by(Sort.Direction.DESC, "dataHora"));
    }

    @Test
    void porPacienteFiltraPeloTitular() {
        EventoAuditoria evento = new EventoAuditoria();
        when(repository.findByPacienteOrderByDataHoraDesc("Carlos Mendes")).thenReturn(List.of(evento));

        assertThat(service.porPaciente("Carlos Mendes")).containsExactly(evento);
    }

    @Test
    void buscarInexistenteLancaRecursoNaoEncontrado() {
        when(repository.findById("x")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.buscar("x")).isInstanceOf(RecursoNaoEncontradoException.class);
    }
}
