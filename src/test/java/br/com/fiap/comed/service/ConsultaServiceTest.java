package br.com.fiap.comed.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import br.com.fiap.comed.exception.RecursoNaoEncontradoException;
import br.com.fiap.comed.model.Consulta;
import br.com.fiap.comed.model.Modalidade;
import br.com.fiap.comed.model.StatusConsulta;
import br.com.fiap.comed.repository.ConsultaRepository;

@ExtendWith(MockitoExtension.class)
class ConsultaServiceTest {

    private static final double FATOR_PADRAO = 0.13;

    @Mock
    private ConsultaRepository repository;

    private ConsultaService service;

    @BeforeEach
    void setUp() {
        service = new ConsultaService(repository, FATOR_PADRAO);
    }

    @Test
    void co2EvitadoEhDistanciaIdaEVoltaVezesFator() {
        // 12 km x 2 x 0.13 = 3.12 kg (Ana Luiza no seed: 3.1 kg)
        assertThat(service.calcularCo2EvitadoKg(12)).isEqualTo(3.12);
        assertThat(service.calcularCo2EvitadoKg(0)).isZero();
    }

    @Test
    void fatorDeCo2EhConfiguravel() {
        ConsultaService outroFator = new ConsultaService(repository, 0.2);

        assertThat(outroFator.calcularCo2EvitadoKg(10)).isEqualTo(4.0);
    }

    @Test
    void criarTelemedicinaSemCo2CalculaAPartirDaDistancia() {
        when(repository.save(any(Consulta.class))).thenAnswer(i -> i.getArgument(0));

        Consulta salva = service.criar(consulta(Modalidade.telemedicina, 15.0, null));

        assertThat(salva.getCo2EvitadoKg()).isEqualTo(3.9);
    }

    @Test
    void criarTelemedicinaComCo2InformadoNaoRecalcula() {
        when(repository.save(any(Consulta.class))).thenAnswer(i -> i.getArgument(0));

        Consulta salva = service.criar(consulta(Modalidade.telemedicina, 15.0, 4.0));

        assertThat(salva.getCo2EvitadoKg()).isEqualTo(4.0);
    }

    @Test
    void criarTelemedicinaSemDistanciaNaoCalcula() {
        when(repository.save(any(Consulta.class))).thenAnswer(i -> i.getArgument(0));

        assertThat(service.criar(consulta(Modalidade.telemedicina, null, null)).getCo2EvitadoKg()).isNull();
    }

    @Test
    void criarPresencialNaoCalculaCo2() {
        when(repository.save(any(Consulta.class))).thenAnswer(i -> i.getArgument(0));
        Consulta presencial = consulta(Modalidade.presencial, 8.0, null);
        presencial.setClinica("Clínica São Lucas");

        Consulta salva = service.criar(presencial);

        assertThat(salva.getCo2EvitadoKg()).isNull();
        assertThat(salva.getId()).isNull();
    }

    @Test
    void atualizarTambemCalculaCo2() {
        when(repository.findById("c1")).thenReturn(Optional.of(consulta(Modalidade.presencial, null, null)));
        when(repository.save(any(Consulta.class))).thenAnswer(i -> i.getArgument(0));

        Consulta salva = service.atualizar("c1", consulta(Modalidade.telemedicina, 7.0, null));

        assertThat(salva.getId()).isEqualTo("c1");
        assertThat(salva.getCo2EvitadoKg()).isEqualTo(1.82);
    }

    @Test
    void atualizarInexistenteNaoSalva() {
        when(repository.findById("x")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.atualizar("x", consulta(Modalidade.telemedicina, 7.0, null)))
                .isInstanceOf(RecursoNaoEncontradoException.class);
        verify(repository, never()).save(any());
    }

    @Test
    void excluirRemoveAConsulta() {
        Consulta existente = consulta(Modalidade.presencial, null, null);
        when(repository.findById("c1")).thenReturn(Optional.of(existente));

        service.excluir("c1");

        verify(repository).delete(existente);
    }

    static Consulta consulta(Modalidade modalidade, Double distanciaKm, Double co2Kg) {
        Consulta c = new Consulta();
        c.setPaciente("Ana Luiza Fonseca");
        c.setMedico("Dra. Juliana Reis");
        c.setModalidade(modalidade);
        c.setStatus(StatusConsulta.concluida);
        c.setDataHora(LocalDateTime.of(2026, 9, 2, 9, 0));
        c.setDistanciaEvitadaKm(distanciaKm);
        c.setCo2EvitadoKg(co2Kg);
        return c;
    }
}
