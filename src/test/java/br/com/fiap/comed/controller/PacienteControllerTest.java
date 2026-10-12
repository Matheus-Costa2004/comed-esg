package br.com.fiap.comed.controller;

import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import br.com.fiap.comed.exception.RecursoNaoEncontradoException;
import br.com.fiap.comed.model.Paciente;
import br.com.fiap.comed.service.PacienteService;

@WebMvcTest(PacienteController.class)
class PacienteControllerTest {

    private static final String ANA = """
            {
              "nome": "Ana Luiza Fonseca",
              "idade": 52,
              "condicoes": ["Hipertensão arterial sistêmica", "Diabetes tipo 2"],
              "vulnerabilidadeSocial": false,
              "dadosClinicos": {"hba1c": 7.8, "historicoGlicemia": [160, 172, 190, 178]}
            }
            """;

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private PacienteService service;

    @Test
    void listarResponde200() throws Exception {
        when(service.listar()).thenReturn(List.of(paciente("p1", "Ana Luiza Fonseca")));

        mvc.perform(get("/api/pacientes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].nome").value("Ana Luiza Fonseca"));
    }

    @Test
    void buscarExistenteResponde200ComDadosClinicosFlexiveis() throws Exception {
        Paciente ana = paciente("p1", "Ana Luiza Fonseca");
        ana.setDadosClinicos(Map.of("hba1c", 7.8));
        when(service.buscar("p1")).thenReturn(ana);

        mvc.perform(get("/api/pacientes/p1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("p1"))
                .andExpect(jsonPath("$.dadosClinicos.hba1c").value(7.8));
    }

    @Test
    void buscarInexistenteResponde404ComProblemDetail() throws Exception {
        when(service.buscar("x")).thenThrow(new RecursoNaoEncontradoException("Paciente", "x"));

        mvc.perform(get("/api/pacientes/x"))
                .andExpect(status().isNotFound())
                .andExpect(header().string("Content-Type", MediaType.APPLICATION_PROBLEM_JSON_VALUE))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.detail").value("Paciente não encontrado(a): x"));
    }

    @Test
    void vulneraveisResponde200() throws Exception {
        when(service.vulneraveis()).thenReturn(List.of(paciente("p8", "Fernanda Lima")));

        mvc.perform(get("/api/pacientes/vulneraveis"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nome").value("Fernanda Lima"));
    }

    @Test
    void criarValidoResponde201ComLocation() throws Exception {
        when(service.criar(any(Paciente.class))).thenReturn(paciente("novo-id", "Ana Luiza Fonseca"));

        mvc.perform(post("/api/pacientes").contentType(MediaType.APPLICATION_JSON).content(ANA))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", endsWith("/api/pacientes/novo-id")))
                .andExpect(jsonPath("$.id").value("novo-id"));
    }

    @Test
    void criarInvalidoResponde400ComOsCamposComErro() throws Exception {
        mvc.perform(post("/api/pacientes").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\": \"\", \"idade\": 200}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Dados inválidos"))
                .andExpect(jsonPath("$.erros.nome").exists())
                .andExpect(jsonPath("$.erros.idade").exists())
                .andExpect(jsonPath("$.erros.condicoes").exists());
        verify(service, never()).criar(any());
    }

    @Test
    void jsonMalformadoResponde400() throws Exception {
        mvc.perform(post("/api/pacientes").contentType(MediaType.APPLICATION_JSON).content("{nome:"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Corpo da requisição inválido"));
    }

    @Test
    void atualizarResponde200() throws Exception {
        when(service.atualizar(eq("p1"), any(Paciente.class))).thenReturn(paciente("p1", "Ana Luiza Fonseca"));

        mvc.perform(put("/api/pacientes/p1").contentType(MediaType.APPLICATION_JSON).content(ANA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("p1"));
    }

    @Test
    void atualizarInexistenteResponde404() throws Exception {
        when(service.atualizar(eq("x"), any(Paciente.class)))
                .thenThrow(new RecursoNaoEncontradoException("Paciente", "x"));

        mvc.perform(put("/api/pacientes/x").contentType(MediaType.APPLICATION_JSON).content(ANA))
                .andExpect(status().isNotFound());
    }

    @Test
    void excluirResponde204EUsaOSolicitanteInformado() throws Exception {
        mvc.perform(delete("/api/pacientes/p6").param("solicitante", "Ricardo Alves"))
                .andExpect(status().isNoContent());

        verify(service).excluir("p6", "Ricardo Alves");
    }

    @Test
    void excluirSemSolicitanteUsaSistema() throws Exception {
        mvc.perform(delete("/api/pacientes/p6")).andExpect(status().isNoContent());

        verify(service).excluir("p6", "sistema");
    }

    @Test
    void excluirInexistenteResponde404() throws Exception {
        doThrow(new RecursoNaoEncontradoException("Paciente", "x")).when(service).excluir("x", "sistema");

        mvc.perform(delete("/api/pacientes/x")).andExpect(status().isNotFound());
    }

    private static Paciente paciente(String id, String nome) {
        Paciente p = new Paciente();
        p.setId(id);
        p.setNome(nome);
        p.setIdade(52);
        p.setCondicoes(List.of("Diabetes tipo 2"));
        return p;
    }
}
