package br.com.fiap.comed.model;

import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

/** Pilar Social: campos comuns tipados; o que depende da condição clínica vai em dadosClinicos. */
@Document("pacientes")
public class Paciente {

    @Id
    @Schema(accessMode = Schema.AccessMode.READ_ONLY)
    private String id;

    @NotBlank
    @Indexed
    private String nome;

    @NotNull
    @Min(0)
    @Max(130)
    private Integer idade;

    @NotEmpty
    private List<String> condicoes = new ArrayList<>();

    private List<String> medicamentos = new ArrayList<>();

    @Indexed
    private boolean vulnerabilidadeSocial;

    private List<String> fatoresVulnerabilidade = new ArrayList<>();

    @Schema(description = "Campos que variam conforme a condição", example = "{\"hba1c\": 7.8, \"historicoGlicemia\": [160, 172, 190, 178]}")
    private Map<String, Object> dadosClinicos = new LinkedHashMap<>();

    @Schema(accessMode = Schema.AccessMode.READ_ONLY)
    private Instant dataCadastro;

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

    public Integer getIdade() {
        return idade;
    }

    public void setIdade(Integer idade) {
        this.idade = idade;
    }

    public List<String> getCondicoes() {
        return condicoes;
    }

    public void setCondicoes(List<String> condicoes) {
        this.condicoes = condicoes;
    }

    public List<String> getMedicamentos() {
        return medicamentos;
    }

    public void setMedicamentos(List<String> medicamentos) {
        this.medicamentos = medicamentos;
    }

    public boolean isVulnerabilidadeSocial() {
        return vulnerabilidadeSocial;
    }

    public void setVulnerabilidadeSocial(boolean vulnerabilidadeSocial) {
        this.vulnerabilidadeSocial = vulnerabilidadeSocial;
    }

    public List<String> getFatoresVulnerabilidade() {
        return fatoresVulnerabilidade;
    }

    public void setFatoresVulnerabilidade(List<String> fatoresVulnerabilidade) {
        this.fatoresVulnerabilidade = fatoresVulnerabilidade;
    }

    public Map<String, Object> getDadosClinicos() {
        return dadosClinicos;
    }

    public void setDadosClinicos(Map<String, Object> dadosClinicos) {
        this.dadosClinicos = dadosClinicos;
    }

    public Instant getDataCadastro() {
        return dataCadastro;
    }

    public void setDataCadastro(Instant dataCadastro) {
        this.dataCadastro = dataCadastro;
    }
}
