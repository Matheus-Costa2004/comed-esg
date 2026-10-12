package br.com.fiap.comed.repository;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;

import br.com.fiap.comed.model.EventoAuditoria;

public interface EventoAuditoriaRepository extends MongoRepository<EventoAuditoria, String> {

    List<EventoAuditoria> findByPacienteOrderByDataHoraDesc(String paciente);
}
