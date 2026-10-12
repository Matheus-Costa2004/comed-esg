package br.com.fiap.comed;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import br.com.fiap.comed.config.SeedDados;
import br.com.fiap.comed.dto.IndicadoresEsg;
import br.com.fiap.comed.repository.AlertaIaRepository;
import br.com.fiap.comed.repository.ConsultaRepository;
import br.com.fiap.comed.repository.EventoAuditoriaRepository;
import br.com.fiap.comed.repository.MedicoRepository;
import br.com.fiap.comed.repository.PacienteRepository;
import br.com.fiap.comed.service.IndicadoresEsgService;

/** O seed roda na inicialização, num database novo, e não duplica dados se rodar de novo. */
@SpringBootTest(properties = "comed.seed.enabled=true")
class SeedDadosIT {

    private static final String DATABASE = "comed_it_seed_" + UUID.randomUUID().toString().substring(0, 8);

    @DynamicPropertySource
    static void database(DynamicPropertyRegistry registry) {
        registry.add("spring.data.mongodb.database", () -> DATABASE);
    }

    @Autowired
    private SeedDados seed;
    @Autowired
    private IndicadoresEsgService indicadores;
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

    @AfterAll
    static void apagarDatabase(@Autowired MongoTemplate mongo) {
        mongo.getDb().drop();
    }

    @Test
    void seedPopulaAsCincoCollectionsUmaVezSo() {
        assertContagens();

        seed.run();

        assertContagens();
    }

    @Test
    void indicadoresDoSeedBatemComAAtividadeAnterior() {
        IndicadoresEsg esg = indicadores.calcular();

        assertThat(esg.ambiental()).isEqualTo(new IndicadoresEsg.Ambiental(4, 11.6, 43.0));
        assertThat(esg.social()).isEqualTo(new IndicadoresEsg.Social(10, 7, 70.0));
        assertThat(esg.governanca()).isEqualTo(new IndicadoresEsg.Governanca(2, 2, 5));
    }

    private void assertContagens() {
        assertThat(pacientes.count()).isEqualTo(10);
        assertThat(medicos.count()).isEqualTo(5);
        assertThat(consultas.count()).isEqualTo(7);
        assertThat(alertas.count()).isEqualTo(8);
        assertThat(auditoria.count()).isEqualTo(5);
    }
}
