package br.com.fiap.comed.repository;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;

import br.com.fiap.comed.model.Paciente;

public interface PacienteRepository extends MongoRepository<Paciente, String> {

    List<Paciente> findByVulnerabilidadeSocialTrue();

    long countByVulnerabilidadeSocialTrue();
}
