package br.com.fiap.comed.model;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/** Pilar Governança: todo alerta nasce sem revisão; a IA sugere, o médico decide. */
@Document("alertas_ia")
public class AlertaIa {

    @Id
    @Schema(accessMode = Schema.AccessMode.READ_ONLY)
    private String id;

    @NotBlank
    private String paciente;

    @NotNull
    private TipoAlerta tipo;

    @NotBlank
    private String descricao;

    @NotNull
    private NivelRisco nivelRisco;

    @Schema(description = "Dados específicos do tipo de alerta", example = "{\"hipotese\": \"Descompensação glicêmica\", \"probabilidade\": 0.87}")
    private Map<String, Object> detalhes = new LinkedHashMap<>();

    private String modeloIa;

    private boolean revisadoPorMedico;

    @Schema(accessMode = Schema.AccessMode.READ_ONLY)
    private String medicoRevisor;

    @Schema(accessMode = Schema.AccessMode.READ_ONLY)
    private Instant dataRevisao;

    @Schema(accessMode = Schema.AccessMode.READ_ONLY)
    private Instant dataGeracao;

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

    public TipoAlerta getTipo() {
        return tipo;
    }

    public void setTipo(TipoAlerta tipo) {
        this.tipo = tipo;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public NivelRisco getNivelRisco() {
        return nivelRisco;
    }

    public void setNivelRisco(NivelRisco nivelRisco) {
        this.nivelRisco = nivelRisco;
    }

    public Map<String, Object> getDetalhes() {
        return detalhes;
    }

    public void setDetalhes(Map<String, Object> detalhes) {
        this.detalhes = detalhes;
    }

    public String getModeloIa() {
        return modeloIa;
    }

    public void setModeloIa(String modeloIa) {
        this.modeloIa = modeloIa;
    }

    public boolean isRevisadoPorMedico() {
        return revisadoPorMedico;
    }

    public void setRevisadoPorMedico(boolean revisadoPorMedico) {
        this.revisadoPorMedico = revisadoPorMedico;
    }

    public String getMedicoRevisor() {
        return medicoRevisor;
    }

    public void setMedicoRevisor(String medicoRevisor) {
        this.medicoRevisor = medicoRevisor;
    }

    public Instant getDataRevisao() {
        return dataRevisao;
    }

    public void setDataRevisao(Instant dataRevisao) {
        this.dataRevisao = dataRevisao;
    }

    public Instant getDataGeracao() {
        return dataGeracao;
    }

    public void setDataGeracao(Instant dataGeracao) {
        this.dataGeracao = dataGeracao;
    }
}
