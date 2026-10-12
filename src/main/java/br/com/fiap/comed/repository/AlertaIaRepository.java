package br.com.fiap.comed.repository;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;

import br.com.fiap.comed.model.AlertaIa;
import br.com.fiap.comed.model.NivelRisco;

public interface AlertaIaRepository extends MongoRepository<AlertaIa, String> {

    List<AlertaIa> findByNivelRiscoAndRevisadoPorMedicoFalse(NivelRisco nivelRisco);

    long countByNivelRiscoAndRevisadoPorMedicoFalse(NivelRisco nivelRisco);
}
