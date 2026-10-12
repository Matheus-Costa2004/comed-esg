package br.com.fiap.comed.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import br.com.fiap.comed.exception.RecursoNaoEncontradoException;
import br.com.fiap.comed.model.Medico;
import br.com.fiap.comed.model.StatusCompliance;
import br.com.fiap.comed.service.MedicoService;

@WebMvcTest(MedicoController.class)
class MedicoControllerTest {

    private static final String RAFAEL = """
            {
              "nome": "Dr. Rafael Costa",
              "crm": "58432-SP",
              "especialidade": "Clínica Geral",
              "statusCompliance": "regular",
              "treinamentoLgpdConcluido": true,
              "pacientesAtivos": 24,
              "dataExpiracaoCrm": "2027-03-10"
            }
            """;

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private MedicoService service;

    @Test
    void pendenciasDeComplianceResponde200() throws Exception {
        Medico renata = new Medico();
        renata.setNome("Dra. Renata Xavier");
        renata.setStatusCompliance(StatusCompliance.pendente_renovacao);
        when(service.pendenciasCompliance()).thenReturn(List.of(renata));

        mvc.perform(get("/api/medicos/pendencias-compliance"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].statusCompliance").value("pendente_renovacao"));
    }

    @Test
    void criarValidoResponde201() throws Exception {
        Medico criado = new Medico();
        criado.setId("m1");
        criado.setCrm("58432-SP");
        when(service.criar(any(Medico.class))).thenReturn(criado);

        mvc.perform(post("/api/medicos").contentType(MediaType.APPLICATION_JSON).content(RAFAEL))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.crm").value("58432-SP"));
    }

    @Test
    void crmForaDoFormatoResponde400() throws Exception {
        mvc.perform(post("/api/medicos").contentType(MediaType.APPLICATION_JSON)
                        .content(RAFAEL.replace("58432-SP", "58432")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erros.crm").value("deve seguir o formato 58432-SP"));
    }

    @Test
    void statusDeComplianceDesconhecidoResponde400() throws Exception {
        mvc.perform(post("/api/medicos").contentType(MediaType.APPLICATION_JSON)
                        .content(RAFAEL.replace("\"regular\"", "\"inexistente\"")))
                .andExpect(status().isBadRequest());
    }

    @Test
    void crmDuplicadoResponde409() throws Exception {
        when(service.criar(any(Medico.class))).thenThrow(new DuplicateKeyException("E11000 duplicate key"));

        mvc.perform(post("/api/medicos").contentType(MediaType.APPLICATION_JSON).content(RAFAEL))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.title").value("Registro duplicado"));
    }

    @Test
    void atualizarInexistenteResponde404() throws Exception {
        when(service.atualizar(eq("x"), any(Medico.class))).thenThrow(new RecursoNaoEncontradoException("Médico", "x"));

        mvc.perform(put("/api/medicos/x").contentType(MediaType.APPLICATION_JSON).content(RAFAEL))
                .andExpect(status().isNotFound());
    }

    @Test
    void excluirResponde204() throws Exception {
        mvc.perform(delete("/api/medicos/m1")).andExpect(status().isNoContent());
    }
}
