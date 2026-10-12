package br.com.fiap.comed.model;

import java.time.LocalDateTime;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

/**
 * Pilares Social e Ambiental. A telemedicina guarda o impacto evitado
 * ({@code distanciaEvitadaKm}, {@code co2EvitadoKg}); a presencial guarda
 * {@code clinica} e {@code deslocamentoKmPaciente}.
 */
@Document("consultas")
public class Consulta {

    @Id
    @Schema(accessMode = Schema.AccessMode.READ_ONLY)
    private String id;

    @NotBlank
    private String paciente;

    @NotBlank
    private String medico;

    @NotNull
    private Modalidade modalidade;

    @NotNull
    private StatusConsulta status;

    @NotNull
    @Schema(example = "2026-10-20T14:30:00")
    private LocalDateTime dataHora;

    private String motivo;

    @PositiveOrZero
    private Double distanciaEvitadaKm;

    @PositiveOrZero
    @Schema(description = "Se ausente em telemedicina, é calculado a partir de distanciaEvitadaKm")
    private Double co2EvitadoKg;

    private String clinica;

    @PositiveOrZero
    private Double deslocamentoKmPaciente;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPaciente() {
        return paciente;
    }

    public void setPaciente(String paciente) {
        this.paciente = paciente;
    }

    public String getMedico() {
        return medico;
    }

    public void setMedico(String medico) {
        this.medico = medico;
    }

    public Modalidade getModalidade() {
        return modalidade;
    }

    public void setModalidade(Modalidade modalidade) {
        this.modalidade = modalidade;
    }

    public StatusConsulta getStatus() {
        return status;
    }

    public void setStatus(StatusConsulta status) {
        this.status = status;
    }

    public LocalDateTime getDataHora() {
        return dataHora;
    }

    public void setDataHora(LocalDateTime dataHora) {
        this.dataHora = dataHora;
    }

    public String getMotivo() {
        return motivo;
    }

    public void setMotivo(String motivo) {
        this.motivo = motivo;
    }

    public Double getDistanciaEvitadaKm() {
        return distanciaEvitadaKm;
    }

    public void setDistanciaEvitadaKm(Double distanciaEvitadaKm) {
        this.distanciaEvitadaKm = distanciaEvitadaKm;
    }

    public Double getCo2EvitadoKg() {
        return co2EvitadoKg;
    }

    public void setCo2EvitadoKg(Double co2EvitadoKg) {
        this.co2EvitadoKg = co2EvitadoKg;
    }

    public String getClinica() {
        return clinica;
    }

    public void setClinica(String clinica) {
        this.clinica = clinica;
    }

    public Double getDeslocamentoKmPaciente() {
        return deslocamentoKmPaciente;
    }

    public void setDeslocamentoKmPaciente(Double deslocamentoKmPaciente) {
        this.deslocamentoKmPaciente = deslocamentoKmPaciente;
    }
}
