package br.com.fiap.comed.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import br.com.fiap.comed.model.EventoAuditoria;
import br.com.fiap.comed.model.TipoEventoAuditoria;
import br.com.fiap.comed.service.AuditoriaService;

@WebMvcTest(AuditoriaController.class)
class AuditoriaControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private AuditoriaService service;

    @Test
    void porPacienteResponde200() throws Exception {
        when(service.porPaciente("Carlos Mendes")).thenReturn(List.of(evento()));

        mvc.perform(get("/api/auditoria/paciente/{nome}", "Carlos Mendes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].tipoEvento").value("acesso_dados"))
                .andExpect(jsonPath("$[0].paciente").value("Carlos Mendes"));
    }

    @Test
    void registrarResponde201() throws Exception {
        EventoAuditoria salvo = evento();
        salvo.setId("e1");
        when(service.registrar(any(EventoAuditoria.class))).thenReturn(salvo);

        mvc.perform(post("/api/auditoria").contentType(MediaType.APPLICATION_JSON).content("""
                        {
                          "tipoEvento": "acesso_dados",
                          "usuario": "Dr. Rafael Costa",
                          "paciente": "Carlos Mendes",
                          "descricao": "Acesso ao prontuário"
                        }
                        """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value("e1"));
    }

    @Test
    void registrarSemTipoResponde400() throws Exception {
        mvc.perform(post("/api/auditoria").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"usuario\": \"Dr. Rafael Costa\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erros.tipoEvento").exists())
                .andExpect(jsonPath("$.erros.descricao").exists());
    }

    @Test
    void trilhaEhAppendOnlySemPutNemDelete() throws Exception {
        mvc.perform(put("/api/auditoria/e1").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isMethodNotAllowed());
        mvc.perform(delete("/api/auditoria/e1"))
                .andExpect(status().isMethodNotAllowed());
    }

    private static EventoAuditoria evento() {
        EventoAuditoria e = new EventoAuditoria(TipoEventoAuditoria.acesso_dados, "Dr. Rafael Costa",
                "Carlos Mendes", "Acesso ao prontuário", Map.of());
        e.setDataHora(Instant.parse("2026-09-08T08:20:00Z"));
        return e;
    }
}
