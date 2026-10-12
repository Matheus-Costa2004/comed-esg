package br.com.fiap.comed.repository;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;

import br.com.fiap.comed.model.Consulta;
import br.com.fiap.comed.model.Modalidade;
import br.com.fiap.comed.model.StatusConsulta;

public interface ConsultaRepository extends MongoRepository<Consulta, String> {

    List<Consulta> findByModalidadeAndStatus(Modalidade modalidade, StatusConsulta status);
}
