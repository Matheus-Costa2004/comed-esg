package br.com.fiap.comed.config;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Component;

import br.com.fiap.comed.model.AlertaIa;
import br.com.fiap.comed.model.Consulta;
import br.com.fiap.comed.model.EventoAuditoria;
import br.com.fiap.comed.model.Medico;
import br.com.fiap.comed.model.Modalidade;
import br.com.fiap.comed.model.NivelRisco;
import br.com.fiap.comed.model.Paciente;
import br.com.fiap.comed.model.StatusCompliance;
import br.com.fiap.comed.model.StatusConsulta;
import br.com.fiap.comed.model.TipoAlerta;
import br.com.fiap.comed.model.TipoEventoAuditoria;
import br.com.fiap.comed.repository.AlertaIaRepository;
import br.com.fiap.comed.repository.ConsultaRepository;
import br.com.fiap.comed.repository.EventoAuditoriaRepository;
import br.com.fiap.comed.repository.MedicoRepository;
import br.com.fiap.comed.repository.PacienteRepository;

/** Dados de exemplo (SEED_ENABLED); só insere em collection vazia, então pode rodar a cada inicialização. */
@Component
@ConditionalOnProperty(name = "comed.seed.enabled", havingValue = "true")
public class SeedDados implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(SeedDados.class);
    private static final String MODELO_IA = "comed-copiloto-v2";

    private final PacienteRepository pacientes;
    private final MedicoRepository medicos;
    private final ConsultaRepository consultas;
    private final AlertaIaRepository alertas;
    private final EventoAuditoriaRepository auditoria;

    public SeedDados(PacienteRepository pacientes, MedicoRepository medicos, ConsultaRepository consultas,
            AlertaIaRepository alertas, EventoAuditoriaRepository auditoria) {
        this.pacientes = pacientes;
        this.medicos = medicos;
        this.consultas = consultas;
        this.alertas = alertas;
        this.auditoria = auditoria;
    }

    @Override
    public void run(String... args) {
        popular("pacientes", pacientes, this::pacientes);
        popular("medicos", medicos, this::medicos);
        popular("consultas", consultas, this::consultas);
        popular("alertas_ia", alertas, this::alertas);
        popular("auditoria_compliance", auditoria, this::eventosAuditoria);
    }

    private <T> void popular(String collection, MongoRepository<T, String> repository,
            Supplier<List<T>> dados) {
        if (repository.count() > 0) {
            log.info("Seed: collection {} já tem dados, nada inserido", collection);
            return;
        }
        List<T> inseridos = repository.saveAll(dados.get());
        log.info("Seed: {} documentos inseridos em {}", inseridos.size(), collection);
    }

    private List<Paciente> pacientes() {
        return List.of(
                paciente("Ana Luiza Fonseca", 52, List.of("Hipertensão arterial sistêmica", "Diabetes tipo 2"),
                        List.of("Losartana 50mg", "Metformina 850mg"), List.of(),
                        mapa("hba1c", 7.8, "historicoGlicemia", List.of(160, 172, 190, 178))),
                paciente("Carlos Mendes", 67, List.of("Cardiopatia"),
                        List.of("Warfarina 5mg", "Ibuprofeno 600mg"), List.of("baixa renda", "idoso"),
                        mapa("anticoagulado", true, "fracaoEjecao", 0.45)),
                paciente("Luísa Santos", 29, List.of("Pré-natal"),
                        List.of("Sulfato ferroso", "Ácido fólico"), List.of("baixa renda", "gestante sem rede de apoio"),
                        mapa("semanaGestacional", 28, "gestacaoDeRisco", false)),
                paciente("João Ferreira", 71, List.of("Hipertensão grave (estágio 3)"),
                        List.of("Anlodipino 10mg", "Hidroclorotiazida 25mg"), List.of("idoso", "baixa renda"),
                        mapa("pressaoArterial", "178/102", "estagioHipertensao", 3)),
                paciente("Helena Duarte", 60, List.of("Diabetes tipo 1"),
                        List.of("Insulina glargina", "Insulina asparte"), List.of("acesso limitado a insumos"),
                        mapa("hba1c", 8.4, "historicoHba1c", List.of(6.9, 7.3, 7.8, 8.4))),
                paciente("Ricardo Alves", 34, List.of("Ansiedade (suporte psicológico)"),
                        List.of("Sertralina 50mg"), List.of("desemprego recente"),
                        mapa("escalaGad7", 14, "acompanhamentoPsicologico", true)),
                paciente("Antônio Souza", 81, List.of("Hipertensão", "Artrose"),
                        List.of("Losartana 25mg", "Paracetamol 750mg"), List.of("idoso morando sozinho"),
                        mapa("moraSozinho", true,
                                "cuidador", mapa("nome", "Marta Souza", "parentesco", "filha", "visitasPorSemana", 2))),
                paciente("Fernanda Lima", 26, List.of("Acompanhamento de saúde geral"),
                        List.of(), List.of("refugiada", "barreira linguística"),
                        mapa("idiomaPreferido", "espanhol", "paisOrigem", "Venezuela",
                                "encaminhadaPor", "ONG parceira")),
                paciente("Beatriz Torres", 45, List.of("Hipotireoidismo"),
                        List.of("Levotiroxina 50mcg"), List.of(),
                        mapa("tsh", 2.1)),
                paciente("Marcos Oliveira", 38, List.of("Asma"),
                        List.of("Budesonida + formoterol"), List.of(),
                        mapa("usoBroncodilatadorPorSemana", 2)));
    }

    private static Paciente paciente(String nome, int idade, List<String> condicoes, List<String> medicamentos,
            List<String> fatoresVulnerabilidade, Map<String, Object> dadosClinicos) {
        Paciente p = new Paciente();
        p.setNome(nome);
        p.setIdade(idade);
        p.setCondicoes(condicoes);
        p.setMedicamentos(medicamentos);
        p.setVulnerabilidadeSocial(!fatoresVulnerabilidade.isEmpty());
        p.setFatoresVulnerabilidade(fatoresVulnerabilidade);
        p.setDadosClinicos(dadosClinicos);
        p.setDataCadastro(Instant.parse("2026-08-01T12:00:00Z"));
        return p;
    }

    private List<Medico> medicos() {
        return List.of(
                medico("Dr. Rafael Costa", "58432-SP", "Clínica Geral", StatusCompliance.regular, null,
                        true, 24, LocalDate.of(2027, 3, 10)),
                medico("Dra. Renata Xavier", "60211-SP", "Cardiologia", StatusCompliance.pendente_renovacao,
                        "Treinamento LGPD não concluído; CRM a renovar", false, 15, LocalDate.of(2026, 9, 15)),
                medico("Dr. Fábio Andrade", "28855-SP", "Psiquiatria", StatusCompliance.suspenso,
                        "Suspenso por auditoria interna", true, 0, LocalDate.of(2027, 1, 20)),
                medico("Dra. Juliana Reis", "44190-SP", "Endocrinologia", StatusCompliance.regular, null,
                        true, 21, LocalDate.of(2027, 6, 30)),
                medico("Dra. Camila Ferraz", "51823-SP", "Ginecologia e Obstetrícia", StatusCompliance.regular, null,
                        true, 18, LocalDate.of(2027, 11, 5)));
    }

    private static Medico medico(String nome, String crm, String especialidade, StatusCompliance status,
            String motivoStatus, boolean lgpd, int pacientesAtivos, LocalDate expiracaoCrm) {
        Medico m = new Medico();
        m.setNome(nome);
        m.setCrm(crm);
        m.setEspecialidade(especialidade);
        m.setStatusCompliance(status);
        m.setMotivoStatus(motivoStatus);
        m.setTreinamentoLgpdConcluido(lgpd);
        m.setPacientesAtivos(pacientesAtivos);
        m.setDataExpiracaoCrm(expiracaoCrm);
        return m;
    }

    private List<Consulta> consultas() {
        return List.of(
                telemedicina("Ana Luiza Fonseca", "Dra. Juliana Reis", "2026-09-02T09:00", StatusConsulta.concluida,
                        "Ajuste de metformina", 12.0, 3.1),
                telemedicina("Beatriz Torres", "Dr. Rafael Costa", "2026-09-05T10:30", StatusConsulta.concluida,
                        "Revisão de exames de tireoide", 9.0, 2.4),
                telemedicina("Helena Duarte", "Dra. Juliana Reis", "2026-09-10T14:00", StatusConsulta.concluida,
                        "Controle glicêmico", 15.0, 4.0),
                telemedicina("Ricardo Alves", "Dr. Rafael Costa", "2026-09-12T16:00", StatusConsulta.concluida,
                        "Acompanhamento de ansiedade", 7.0, 2.1),
                presencial("Carlos Mendes", "Dra. Renata Xavier", "2026-09-08T08:30", StatusConsulta.concluida,
                        "Revisão da anticoagulação", "Clínica São Lucas", 8.0),
                presencial("Luísa Santos", "Dra. Camila Ferraz", "2026-09-15T11:00", StatusConsulta.concluida,
                        "Pré-natal, semana 28", "UBS Vila Mariana", 5.0),
                telemedicina("Antônio Souza", "Dr. Rafael Costa", "2026-10-20T15:00", StatusConsulta.agendada,
                        "Retorno de pressão arterial", 11.0, null));
    }

    private static Consulta telemedicina(String paciente, String medico, String dataHora, StatusConsulta status,
            String motivo, Double distanciaKm, Double co2Kg) {
        Consulta c = consulta(paciente, medico, Modalidade.telemedicina, dataHora, status, motivo);
        c.setDistanciaEvitadaKm(distanciaKm);
        c.setCo2EvitadoKg(co2Kg);
        return c;
    }

    private static Consulta presencial(String paciente, String medico, String dataHora, StatusConsulta status,
            String motivo, String clinica, double deslocamentoKm) {
        Consulta c = consulta(paciente, medico, Modalidade.presencial, dataHora, status, motivo);
        c.setClinica(clinica);
        c.setDeslocamentoKmPaciente(deslocamentoKm);
        return c;
    }

    private static Consulta consulta(String paciente, String medico, Modalidade modalidade, String dataHora,
            StatusConsulta status, String motivo) {
        Consulta c = new Consulta();
        c.setPaciente(paciente);
        c.setMedico(medico);
        c.setModalidade(modalidade);
        c.setDataHora(LocalDateTime.parse(dataHora));
        c.setStatus(status);
        c.setMotivo(motivo);
        return c;
    }

    private List<AlertaIa> alertas() {
        return List.of(
                alerta("Carlos Mendes", TipoAlerta.interacao_medicamentosa, NivelRisco.alto,
                        "Interação entre Warfarina e Ibuprofeno: risco aumentado de sangramento",
                        mapa("medicamentos", List.of("Warfarina", "Ibuprofeno"),
                                "recomendacao", "Substituir ibuprofeno por paracetamol"),
                        "2026-09-08T08:10:00Z", "Dra. Renata Xavier", "2026-09-08T08:45:00Z"),
                alerta("Ana Luiza Fonseca", TipoAlerta.hipotese_diagnostica, NivelRisco.medio,
                        "Hipótese diagnóstica: descompensação glicêmica",
                        mapa("hipotese", "Descompensação glicêmica", "probabilidade", 0.87,
                                "evidencias", List.of("HbA1c 7.8%", "Glicemia média acima de 170 mg/dL")),
                        "2026-09-01T18:00:00Z", null, null),
                alerta("Helena Duarte", TipoAlerta.tendencia_hba1c, NivelRisco.alto,
                        "HbA1c em alta contínua nas últimas 4 medições",
                        mapa("historicoHba1c", List.of(6.9, 7.3, 7.8, 8.4), "tendencia", "alta"),
                        "2026-09-10T13:30:00Z", null, null),
                alerta("João Ferreira", TipoAlerta.pa_critica, NivelRisco.alto,
                        "Pressão arterial crítica: 178/102 mmHg",
                        mapa("sistolica", 178, "diastolica", 102, "estagio", 3),
                        "2026-10-01T07:15:00Z", null, null),
                alerta("Ricardo Alves", TipoAlerta.risco_psicossocial, NivelRisco.medio,
                        "Escala GAD-7 com ansiedade moderada",
                        mapa("escalaGad7", 14, "recomendacao", "Manter psicoterapia semanal"),
                        "2026-09-12T15:00:00Z", "Dr. Rafael Costa", "2026-09-12T16:30:00Z"),
                alerta("Antônio Souza", TipoAlerta.risco_psicossocial, NivelRisco.medio,
                        "Idoso que mora sozinho com sinais de isolamento social",
                        mapa("moraSozinho", true, "ultimaVisitaCuidadorDias", 6),
                        "2026-09-20T10:00:00Z", null, null),
                alerta("Luísa Santos", TipoAlerta.pa_critica, NivelRisco.alto,
                        "PA 142/91 na semana 28: rastrear pré-eclâmpsia",
                        mapa("sistolica", 142, "diastolica", 91, "semanaGestacional", 28),
                        "2026-09-15T10:40:00Z", "Dra. Camila Ferraz", "2026-09-15T11:20:00Z"),
                alerta("Fernanda Lima", TipoAlerta.hipotese_diagnostica, NivelRisco.baixo,
                        "Hipótese diagnóstica: anemia ferropriva",
                        mapa("hipotese", "Anemia ferropriva", "probabilidade", 0.62),
                        "2026-09-25T09:00:00Z", null, null));
    }

    private static AlertaIa alerta(String paciente, TipoAlerta tipo, NivelRisco risco, String descricao,
            Map<String, Object> detalhes, String geradoEm, String revisor, String revisadoEm) {
        AlertaIa a = new AlertaIa();
        a.setPaciente(paciente);
        a.setTipo(tipo);
        a.setNivelRisco(risco);
        a.setDescricao(descricao);
        a.setDetalhes(detalhes);
        a.setModeloIa(MODELO_IA);
        a.setDataGeracao(Instant.parse(geradoEm));
        a.setRevisadoPorMedico(revisor != null);
        a.setMedicoRevisor(revisor);
        a.setDataRevisao(revisadoEm == null ? null : Instant.parse(revisadoEm));
        return a;
    }

    private List<EventoAuditoria> eventosAuditoria() {
        return List.of(
                evento(TipoEventoAuditoria.acesso_dados, "Dr. Rafael Costa", "Carlos Mendes",
                        "Acesso ao prontuário para revisão de medicamentos",
                        mapa("recurso", "prontuario", "finalidade", "assistencia"), "2026-09-08T08:20:00Z"),
                evento(TipoEventoAuditoria.revisao_alerta_ia, "Dra. Renata Xavier", "Carlos Mendes",
                        "Alerta de IA revisado pelo médico",
                        mapa("tipo", "interacao_medicamentosa", "nivelRisco", "alto"), "2026-09-08T08:45:00Z"),
                evento(TipoEventoAuditoria.acesso_negado, "Dr. Fábio Andrade", "Helena Duarte",
                        "Tentativa de acesso negada",
                        mapa("motivo", "token expirado"), "2026-09-18T22:05:00Z"),
                evento(TipoEventoAuditoria.exclusao_dados, "Ricardo Alves", "Ricardo Alves",
                        "Exclusão de dados solicitada pelo titular",
                        mapa("canal", "LGPD", "situacao", "solicitada"), "2026-09-22T14:00:00Z"),
                evento(TipoEventoAuditoria.consentimento, "Ana Luiza Fonseca", "Ana Luiza Fonseca",
                        "Aceite do termo de consentimento LGPD",
                        mapa("versaoTermo", "3.1"), "2026-08-01T12:05:00Z"));
    }

    private static EventoAuditoria evento(TipoEventoAuditoria tipo, String usuario, String paciente,
            String descricao, Map<String, Object> detalhes, String dataHora) {
        EventoAuditoria e = new EventoAuditoria(tipo, usuario, paciente, descricao, detalhes);
        e.setDataHora(Instant.parse(dataHora));
        return e;
    }

    /** Map ordenado a partir de pares chave/valor (Map.of não preserva a ordem). */
    private static Map<String, Object> mapa(Object... chavesEValores) {
        Map<String, Object> mapa = new LinkedHashMap<>();
        for (int i = 0; i < chavesEValores.length; i += 2) {
            mapa.put((String) chavesEValores[i], chavesEValores[i + 1]);
        }
        return mapa;
    }
}
