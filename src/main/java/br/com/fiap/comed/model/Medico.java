package br.com.fiap.comed.model;

import java.time.LocalDate;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

/** Pilar Governança: credenciamento e compliance de quem usa o copiloto. */
@Document("medicos")
public class Medico {

    @Id
    @Schema(accessMode = Schema.AccessMode.READ_ONLY)
    private String id;

    @NotBlank
    private String nome;

    @NotBlank
    @Pattern(regexp = "\\d{4,7}-[A-Z]{2}", message = "deve seguir o formato 58432-SP")
    @Indexed(unique = true)
    @Schema(example = "58432-SP")
    private String crm;

    @NotBlank
    private String especialidade;

    @NotNull
    private StatusCompliance statusCompliance;

    private String motivoStatus;

    private boolean treinamentoLgpdConcluido;

    @Min(0)
    private int pacientesAtivos;

    private LocalDate dataExpiracaoCrm;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getCrm() {
        return crm;
    }

    public void setCrm(String crm) {
        this.crm = crm;
    }

    public String getEspecialidade() {
        return especialidade;
    }

    public void setEspecialidade(String especialidade) {
        this.especialidade = especialidade;
    }

    public StatusCompliance getStatusCompliance() {
        return statusCompliance;
    }

    public void setStatusCompliance(StatusCompliance statusCompliance) {
        this.statusCompliance = statusCompliance;
    }

    public String getMotivoStatus() {
        return motivoStatus;
    }

    public void setMotivoStatus(String motivoStatus) {
        this.motivoStatus = motivoStatus;
    }

    public boolean isTreinamentoLgpdConcluido() {
        return treinamentoLgpdConcluido;
    }

    public void setTreinamentoLgpdConcluido(boolean treinamentoLgpdConcluido) {
        this.treinamentoLgpdConcluido = treinamentoLgpdConcluido;
    }

    public int getPacientesAtivos() {
        return pacientesAtivos;
    }

    public void setPacientesAtivos(int pacientesAtivos) {
        this.pacientesAtivos = pacientesAtivos;
    }

    public LocalDate getDataExpiracaoCrm() {
        return dataExpiracaoCrm;
    }

    public void setDataExpiracaoCrm(LocalDate dataExpiracaoCrm) {
        this.dataExpiracaoCrm = dataExpiracaoCrm;
    }
}
