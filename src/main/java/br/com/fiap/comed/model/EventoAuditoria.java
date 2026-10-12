package br.com.fiap.comed.model;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/** Pilar Governança: trilha LGPD append-only (a API não expõe update nem delete). */
@Document("auditoria_compliance")
public class EventoAuditoria {

    @Id
    @Schema(accessMode = Schema.AccessMode.READ_ONLY)
    private String id;

    @NotNull
    private TipoEventoAuditoria tipoEvento;

    @NotBlank
    @Schema(description = "Quem executou a ação", example = "Dr. Rafael Costa")
    private String usuario;

    @Indexed
    @Schema(description = "Titular dos dados, quando houver", example = "Carlos Mendes")
    private String paciente;

    @NotBlank
    private String descricao;

    private Map<String, Object> detalhes = new LinkedHashMap<>();

    @Schema(accessMode = Schema.AccessMode.READ_ONLY)
    private Instant dataHora;

    public EventoAuditoria() {
    }

    public EventoAuditoria(TipoEventoAuditoria tipoEvento, String usuario, String paciente, String descricao,
            Map<String, Object> detalhes) {
        this.tipoEvento = tipoEvento;
        this.usuario = usuario;
        this.paciente = paciente;
        this.descricao = descricao;
        this.detalhes = new LinkedHashMap<>(detalhes);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public TipoEventoAuditoria getTipoEvento() {
        return tipoEvento;
    }

    public void setTipoEvento(TipoEventoAuditoria tipoEvento) {
        this.tipoEvento = tipoEvento;
    }

    public String getUsuario() {
        return usuario;
    }

    public void setUsuario(String usuario) {
        this.usuario = usuario;
    }

    public String getPaciente() {
        return paciente;
    }

    public void setPaciente(String paciente) {
        this.paciente = paciente;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public Map<String, Object> getDetalhes() {
        return detalhes;
    }

    public void setDetalhes(Map<String, Object> detalhes) {
        this.detalhes = detalhes;
    }

    public Instant getDataHora() {
        return dataHora;
    }

    public void setDataHora(Instant dataHora) {
        this.dataHora = dataHora;
    }
}
