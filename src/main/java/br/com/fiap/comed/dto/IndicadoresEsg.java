package br.com.fiap.comed.dto;

import java.time.Instant;

/** Painel ESG: um bloco por pilar. */
public record IndicadoresEsg(Ambiental ambiental, Social social, Governanca governanca, Instant geradoEm) {

    public record Ambiental(long consultasTelemedicinaConcluidas, double co2EvitadoKg, double kmEvitados) {
    }

    public record Social(long totalPacientes, long pacientesVulneraveis, double percentualVulneraveis) {
    }

    public record Governanca(long alertasAltoRiscoPendentes, long medicosComPendencia, long eventosAuditoria) {
    }
}
