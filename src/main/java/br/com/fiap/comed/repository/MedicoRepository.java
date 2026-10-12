package br.com.fiap.comed.repository;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;

import br.com.fiap.comed.model.Medico;
import br.com.fiap.comed.model.StatusCompliance;

public interface MedicoRepository extends MongoRepository<Medico, String> {

    List<Medico> findByStatusComplianceNot(StatusCompliance status);

    long countByStatusComplianceNot(StatusCompliance status);
}
