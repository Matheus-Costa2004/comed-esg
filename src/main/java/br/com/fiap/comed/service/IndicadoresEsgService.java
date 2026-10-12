package br.com.fiap.comed.service;

import java.time.Instant;
import java.util.List;
import java.util.Objects;

import org.springframework.stereotype.Service;

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

@Service
public class IndicadoresEsgService {

    private final ConsultaRepository consultas;
    private final PacienteRepository pacientes;
    private final AlertaIaRepository alertas;
    private final MedicoRepository medicos;
    private final EventoAuditoriaRepository auditoria;

    public IndicadoresEsgService(ConsultaRepository consultas, PacienteRepository pacientes,
            AlertaIaRepository alertas, MedicoRepository medicos, EventoAuditoriaRepository auditoria) {
        this.consultas = consultas;
        this.pacientes = pacientes;
        this.alertas = alertas;
        this.medicos = medicos;
        this.auditoria = auditoria;
    }

    public IndicadoresEsg calcular() {
        return new IndicadoresEsg(ambiental(), social(), governanca(), Instant.now());
    }

    private IndicadoresEsg.Ambiental ambiental() {
        List<Consulta> teleconcluidas = consultas.findByModalidadeAndStatus(Modalidade.telemedicina,
                StatusConsulta.concluida);
        double co2 = teleconcluidas.stream().map(Consulta::getCo2EvitadoKg).filter(Objects::nonNull)
                .mapToDouble(Double::doubleValue).sum();
        double km = teleconcluidas.stream().map(Consulta::getDistanciaEvitadaKm).filter(Objects::nonNull)
                .mapToDouble(Double::doubleValue).sum();
        return new IndicadoresEsg.Ambiental(teleconcluidas.size(), arredondar(co2), arredondar(km));
    }

    private IndicadoresEsg.Social social() {
        long total = pacientes.count();
        long vulneraveis = pacientes.countByVulnerabilidadeSocialTrue();
        double percentual = total == 0 ? 0 : arredondar(vulneraveis * 100.0 / total);
        return new IndicadoresEsg.Social(total, vulneraveis, percentual);
    }

    private IndicadoresEsg.Governanca governanca() {
        return new IndicadoresEsg.Governanca(
                alertas.countByNivelRiscoAndRevisadoPorMedicoFalse(NivelRisco.alto),
                medicos.countByStatusComplianceNot(StatusCompliance.regular),
                auditoria.count());
    }

    private static double arredondar(double valor) {
        return Math.round(valor * 100.0) / 100.0;
    }
}
