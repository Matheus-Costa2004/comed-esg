package br.com.fiap.comed;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import br.com.fiap.comed.repository.AlertaIaRepository;
import br.com.fiap.comed.repository.ConsultaRepository;
import br.com.fiap.comed.repository.EventoAuditoriaRepository;
import br.com.fiap.comed.repository.MedicoRepository;
import br.com.fiap.comed.repository.PacienteRepository;

/**
 * Sobe a aplicação inteira contra um MongoDB real (MONGODB_URI). Usa o database comed_it
 * para nunca tocar nos dados de desenvolvimento.
 */
@SpringBootTest(properties = { "spring.data.mongodb.database=comed_it", "comed.seed.enabled=false" })
@AutoConfigureMockMvc
class ComedApiIT {

    @Autowired
    private MockMvc mvc;
    @Autowired
    private ObjectMapper json;

    @Autowired
    private PacienteRepository pacientes;
    @Autowired
    private MedicoRepository medicos;
    @Autowired
    private ConsultaRepository consultas;
    @Autowired
    private AlertaIaRepository alertas;
    @Autowired
    private EventoAuditoriaRepository auditoria;

    @BeforeEach
    void limparCollections() {
        // deleteAll (e não drop) para preservar o índice único de CRM.
        pacientes.deleteAll();
        medicos.deleteAll();
        consultas.deleteAll();
        alertas.deleteAll();
        auditoria.deleteAll();
    }

    @Test
    void excluirPacienteGravaExclusaoDeDadosNaTrilha() throws Exception {
        String id = criar("/api/pacientes", """
                {"nome": "Carlos Mendes", "idade": 67, "condicoes": ["Cardiopatia"],
                 "vulnerabilidadeSocial": true, "dadosClinicos": {"anticoagulado": true}}
                """);

        mvc.perform(get("/api/pacientes/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.dadosClinicos.anticoagulado").value(true));

        mvc.perform(delete("/api/pacientes/" + id).param("solicitante", "Dr. Rafael Costa"))
                .andExpect(status().isNoContent());

        mvc.perform(get("/api/pacientes/" + id)).andExpect(status().isNotFound());
        mvc.perform(get("/api/auditoria/paciente/{nome}", "Carlos Mendes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].tipoEvento").value("exclusao_dados"))
                .andExpect(jsonPath("$[0].usuario").value("Dr. Rafael Costa"))
                .andExpect(jsonPath("$[0].detalhes.pacienteId").value(id));
    }

    @Test
    void teleconsultaPersisteCo2CalculadoEEntraNosIndicadores() throws Exception {
        String id = criar("/api/consultas", """
                {"paciente": "Ana Luiza Fonseca", "medico": "Dra. Juliana Reis", "modalidade": "telemedicina",
                 "status": "concluida", "dataHora": "2026-09-02T09:00:00", "distanciaEvitadaKm": 12}
                """);

        mvc.perform(get("/api/consultas/" + id))
                .andExpect(jsonPath("$.co2EvitadoKg").value(3.12));
        mvc.perform(get("/api/esg/indicadores"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ambiental.consultasTelemedicinaConcluidas").value(1))
                .andExpect(jsonPath("$.ambiental.co2EvitadoKg").value(3.12))
                .andExpect(jsonPath("$.ambiental.kmEvitados").value(12.0));
    }

    @Test
    void revisarAlertaTiraDosPendentesEGravaNaTrilha() throws Exception {
        String id = criar("/api/alertas", """
                {"paciente": "Helena Duarte", "tipo": "tendencia_hba1c", "nivelRisco": "alto",
                 "descricao": "HbA1c em alta", "detalhes": {"historicoHba1c": [6.9, 7.3, 7.8, 8.4]}}
                """);
        mvc.perform(get("/api/alertas/pendentes")).andExpect(jsonPath("$", hasSize(1)));

        mvc.perform(patch("/api/alertas/" + id + "/revisao").param("medico", "Dra. Juliana Reis"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.revisadoPorMedico").value(true));

        mvc.perform(get("/api/alertas/pendentes")).andExpect(jsonPath("$", hasSize(0)));
        mvc.perform(get("/api/auditoria/paciente/{nome}", "Helena Duarte"))
                .andExpect(jsonPath("$[0].tipoEvento").value("revisao_alerta_ia"))
                .andExpect(jsonPath("$[0].detalhes.alertaId").value(id));
        mvc.perform(patch("/api/alertas/" + id + "/revisao").param("medico", "Dr. Rafael Costa"))
                .andExpect(status().isConflict());
    }

    @Test
    void crmEhUnicoNoBanco() throws Exception {
        String rafael = """
                {"nome": "Dr. Rafael Costa", "crm": "58432-SP", "especialidade": "Clínica Geral",
                 "statusCompliance": "regular", "pacientesAtivos": 24}
                """;
        criar("/api/medicos", rafael);

        mvc.perform(post("/api/medicos").contentType(MediaType.APPLICATION_JSON).content(rafael))
                .andExpect(status().isConflict());
    }

    @Test
    void indicadoresSociaisEDeGovernanca() throws Exception {
        criar("/api/pacientes", """
                {"nome": "Fernanda Lima", "idade": 26, "condicoes": ["Saúde geral"], "vulnerabilidadeSocial": true,
                 "dadosClinicos": {"idiomaPreferido": "espanhol"}}
                """);
        criar("/api/pacientes", """
                {"nome": "Beatriz Torres", "idade": 45, "condicoes": ["Hipotireoidismo"]}
                """);
        criar("/api/medicos", """
                {"nome": "Dr. Fábio Andrade", "crm": "28855-SP", "especialidade": "Psiquiatria",
                 "statusCompliance": "suspenso"}
                """);

        mvc.perform(get("/api/pacientes/vulneraveis")).andExpect(jsonPath("$", hasSize(1)));
        mvc.perform(get("/api/medicos/pendencias-compliance")).andExpect(jsonPath("$", hasSize(1)));
        mvc.perform(get("/api/esg/indicadores"))
                .andExpect(jsonPath("$.social.totalPacientes").value(2))
                .andExpect(jsonPath("$.social.percentualVulneraveis").value(50.0))
                .andExpect(jsonPath("$.governanca.medicosComPendencia").value(1));
    }

    @Test
    void healthEstaUpComOMongo() throws Exception {
        mvc.perform(get("/actuator/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"))
                .andExpect(jsonPath("$.components.mongo.status").value("UP"));
    }

    private String criar(String url, String corpo) throws Exception {
        String resposta = mvc.perform(post(url).contentType(MediaType.APPLICATION_JSON).content(corpo))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        JsonNode node = json.readTree(resposta);
        return node.get("id").asText();
    }
}
