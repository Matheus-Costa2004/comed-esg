package br.com.fiap.comed.controller;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import br.com.fiap.comed.dto.IndicadoresEsg;
import br.com.fiap.comed.service.IndicadoresEsgService;

@WebMvcTest(EsgController.class)
class EsgControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private IndicadoresEsgService service;

    @Test
    void indicadoresRespondeOsTresPilares() throws Exception {
        when(service.calcular()).thenReturn(new IndicadoresEsg(
                new IndicadoresEsg.Ambiental(4, 11.6, 43.0),
                new IndicadoresEsg.Social(10, 7, 70.0),
                new IndicadoresEsg.Governanca(2, 2, 5),
                Instant.parse("2026-10-12T12:00:00Z")));

        mvc.perform(get("/api/esg/indicadores"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ambiental.consultasTelemedicinaConcluidas").value(4))
                .andExpect(jsonPath("$.ambiental.co2EvitadoKg").value(11.6))
                .andExpect(jsonPath("$.social.percentualVulneraveis").value(70.0))
                .andExpect(jsonPath("$.governanca.alertasAltoRiscoPendentes").value(2))
                .andExpect(jsonPath("$.geradoEm").value("2026-10-12T12:00:00Z"));
    }
}
