package br.com.fiap.comed.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import br.com.fiap.comed.dto.IndicadoresEsg;
import br.com.fiap.comed.model.Consulta;
import br.com.fiap.comed.model.Modalidade;
import br.com.fiap.comed.model.NivelRisco;
import br.com.fiap.comed.model.StatusCompliance;
import br.com.fiap.comed.model.StatusConsulta;
import br.com.fiap.comed.repository.AlertaIaRepository;
import br.com.fiap.comed.repository.ConsultaRepository;
import br.com.fiap.comed.repository.EventoAuditoriaRepository;
import br.com.fiap.comed.repository.MedicoRepository;
import br.com.fiap.comed.repository.PacienteRepository;

@ExtendWith(MockitoExtension.class)
class IndicadoresEsgServiceTest {

    @Mock
    private ConsultaRepository consultas;
    @Mock
    private PacienteRepository pacientes;
    @Mock
    private AlertaIaRepository alertas;
    @Mock
    private MedicoRepository medicos;
    @Mock
    private EventoAuditoriaRepository auditoria;

    @InjectMocks
    private IndicadoresEsgService service;

    @Test
    void consolidaOsTresPilaresComOsDadosDoSeed() {
        when(consultas.findByModalidadeAndStatus(Modalidade.telemedicina, StatusConsulta.concluida)).thenReturn(List.of(
                tele(12.0, 3.1), tele(9.0, 2.4), tele(15.0, 4.0), tele(7.0, 2.1)));
        when(pacientes.count()).thenReturn(10L);
        when(pacientes.countByVulnerabilidadeSocialTrue()).thenReturn(7L);
        when(alertas.countByNivelRiscoAndRevisadoPorMedicoFalse(NivelRisco.alto)).thenReturn(2L);
        when(medicos.countByStatusComplianceNot(StatusCompliance.regular)).thenReturn(2L);
        when(auditoria.count()).thenReturn(5L);

        IndicadoresEsg indicadores = service.calcular();

        assertThat(indicadores.ambiental()).isEqualTo(new IndicadoresEsg.Ambiental(4, 11.6, 43.0));
        assertThat(indicadores.social()).isEqualTo(new IndicadoresEsg.Social(10, 7, 70.0));
        assertThat(indicadores.governanca()).isEqualTo(new IndicadoresEsg.Governanca(2, 2, 5));
        assertThat(indicadores.geradoEm()).isNotNull();
    }

    @Test
    void consultaSemCo2OuDistanciaNaoQuebraASoma() {
        when(consultas.findByModalidadeAndStatus(Modalidade.telemedicina, StatusConsulta.concluida))
                .thenReturn(List.of(tele(10.0, null), tele(null, 1.5)));

        IndicadoresEsg.Ambiental ambiental = service.calcular().ambiental();

        assertThat(ambiental.consultasTelemedicinaConcluidas()).isEqualTo(2);
        assertThat(ambiental.co2EvitadoKg()).isEqualTo(1.5);
        assertThat(ambiental.kmEvitados()).isEqualTo(10.0);
    }

    @Test
    void semPacientesOPercentualEhZero() {
        when(consultas.findByModalidadeAndStatus(Modalidade.telemedicina, StatusConsulta.concluida))
                .thenReturn(List.of());
        when(pacientes.count()).thenReturn(0L);

        IndicadoresEsg indicadores = service.calcular();

        assertThat(indicadores.social().percentualVulneraveis()).isZero();
        assertThat(indicadores.ambiental().co2EvitadoKg()).isZero();
    }

    @Test
    void percentualArredondadoComDuasCasas() {
        when(consultas.findByModalidadeAndStatus(Modalidade.telemedicina, StatusConsulta.concluida))
                .thenReturn(List.of());
        when(pacientes.count()).thenReturn(3L);
        when(pacientes.countByVulnerabilidadeSocialTrue()).thenReturn(1L);

        assertThat(service.calcular().social().percentualVulneraveis()).isEqualTo(33.33);
    }

    private static Consulta tele(Double km, Double co2) {
        Consulta c = new Consulta();
        c.setModalidade(Modalidade.telemedicina);
        c.setStatus(StatusConsulta.concluida);
        c.setDistanciaEvitadaKm(km);
        c.setCo2EvitadoKg(co2);
        return c;
    }
}
