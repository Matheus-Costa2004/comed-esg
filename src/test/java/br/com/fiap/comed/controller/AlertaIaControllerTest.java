package br.com.fiap.comed.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import br.com.fiap.comed.exception.ConflitoException;
import br.com.fiap.comed.exception.RecursoNaoEncontradoException;
import br.com.fiap.comed.model.AlertaIa;
import br.com.fiap.comed.model.NivelRisco;
import br.com.fiap.comed.model.TipoAlerta;
import br.com.fiap.comed.service.AlertaIaService;

@WebMvcTest(AlertaIaController.class)
class AlertaIaControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private AlertaIaService service;

    @Test
    void pendentesResponde200() throws Exception {
        when(service.pendentes()).thenReturn(List.of(alerta(false)));

        mvc.perform(get("/api/alertas/pendentes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nivelRisco").value("alto"))
                .andExpect(jsonPath("$[0].revisadoPorMedico").value(false));
    }

    @Test
    void revisarResponde200() throws Exception {
        AlertaIa revisado = alerta(true);
        revisado.setMedicoRevisor("Dra. Juliana Reis");
        when(service.revisar("a3", "Dra. Juliana Reis")).thenReturn(revisado);

        mvc.perform(patch("/api/alertas/a3/revisao").param("medico", "Dra. Juliana Reis"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.revisadoPorMedico").value(true))
                .andExpect(jsonPath("$.medicoRevisor").value("Dra. Juliana Reis"));
    }

    @Test
    void revisarSemMedicoResponde400() throws Exception {
        mvc.perform(patch("/api/alertas/a3/revisao")).andExpect(status().isBadRequest());
        verify(service, never()).revisar(any(), any());
    }

    @Test
    void revisarComMedicoEmBrancoResponde400() throws Exception {
        mvc.perform(patch("/api/alertas/a3/revisao").param("medico", " ")).andExpect(status().isBadRequest());
        verify(service, never()).revisar(any(), any());
    }

    @Test
    void revisarInexistenteResponde404() throws Exception {
        when(service.revisar("x", "Dr. Rafael Costa")).thenThrow(new RecursoNaoEncontradoException("Alerta", "x"));

        mvc.perform(patch("/api/alertas/x/revisao").param("medico", "Dr. Rafael Costa"))
                .andExpect(status().isNotFound());
    }

    @Test
    void revisarDuasVezesResponde409() throws Exception {
        when(service.revisar("a1", "Dr. Rafael Costa")).thenThrow(new ConflitoException("já revisado"));

        mvc.perform(patch("/api/alertas/a1/revisao").param("medico", "Dr. Rafael Costa"))
                .andExpect(status().isConflict());
    }

    @Test
    void criarComNivelDeRiscoInvalidoResponde400() throws Exception {
        mvc.perform(post("/api/alertas").contentType(MediaType.APPLICATION_JSON).content("""
                        {"paciente": "Helena Duarte", "tipo": "tendencia_hba1c", "descricao": "x", "nivelRisco": "altissimo"}
                        """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void criarValidoResponde201() throws Exception {
        AlertaIa criado = alerta(false);
        criado.setId("a9");
        when(service.criar(any(AlertaIa.class))).thenReturn(criado);

        mvc.perform(post("/api/alertas").contentType(MediaType.APPLICATION_JSON).content("""
                        {
                          "paciente": "Helena Duarte",
                          "tipo": "tendencia_hba1c",
                          "descricao": "HbA1c em alta",
                          "nivelRisco": "alto",
                          "detalhes": {"historicoHba1c": [6.9, 7.3, 7.8, 8.4]}
                        }
                        """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value("a9"));
    }

    private static AlertaIa alerta(boolean revisado) {
        AlertaIa a = new AlertaIa();
        a.setId("a3");
        a.setPaciente("Helena Duarte");
        a.setTipo(TipoAlerta.tendencia_hba1c);
        a.setNivelRisco(NivelRisco.alto);
        a.setDescricao("HbA1c em alta");
        a.setRevisadoPorMedico(revisado);
        return a;
    }
}
