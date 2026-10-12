package br.com.fiap.comed.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import br.com.fiap.comed.exception.RecursoNaoEncontradoException;
import br.com.fiap.comed.model.Consulta;
import br.com.fiap.comed.model.Modalidade;
import br.com.fiap.comed.model.StatusConsulta;
import br.com.fiap.comed.service.ConsultaService;

@WebMvcTest(ConsultaController.class)
class ConsultaControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private ConsultaService service;

    @Test
    void criarTeleconsultaResponde201ComCo2Calculado() throws Exception {
        Consulta criada = new Consulta();
        criada.setId("c1");
        criada.setModalidade(Modalidade.telemedicina);
        criada.setStatus(StatusConsulta.concluida);
        criada.setDataHora(LocalDateTime.of(2026, 9, 2, 9, 0));
        criada.setDistanciaEvitadaKm(12.0);
        criada.setCo2EvitadoKg(3.12);
        when(service.criar(any(Consulta.class))).thenReturn(criada);

        mvc.perform(post("/api/consultas").contentType(MediaType.APPLICATION_JSON).content("""
                        {
                          "paciente": "Ana Luiza Fonseca",
                          "medico": "Dra. Juliana Reis",
                          "modalidade": "telemedicina",
                          "status": "concluida",
                          "dataHora": "2026-09-02T09:00:00",
                          "distanciaEvitadaKm": 12
                        }
                        """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.co2EvitadoKg").value(3.12))
                .andExpect(jsonPath("$.clinica").doesNotExist());
    }

    @Test
    void criarSemCamposObrigatoriosResponde400() throws Exception {
        mvc.perform(post("/api/consultas").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"paciente\": \"Ana\", \"distanciaEvitadaKm\": -3}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erros.medico").exists())
                .andExpect(jsonPath("$.erros.modalidade").exists())
                .andExpect(jsonPath("$.erros.distanciaEvitadaKm").exists());
    }

    @Test
    void buscarInexistenteResponde404() throws Exception {
        when(service.buscar("x")).thenThrow(new RecursoNaoEncontradoException("Consulta", "x"));

        mvc.perform(get("/api/consultas/x")).andExpect(status().isNotFound());
    }
}
