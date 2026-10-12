package br.com.fiap.comed.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import br.com.fiap.comed.exception.RecursoNaoEncontradoException;
import br.com.fiap.comed.model.Consulta;
import br.com.fiap.comed.model.Modalidade;
import br.com.fiap.comed.repository.ConsultaRepository;

@Service
public class ConsultaService {

    private final ConsultaRepository repository;
    private final double fatorCo2KgPorKm;

    public ConsultaService(ConsultaRepository repository,
            @Value("${comed.esg.fator-co2-kg-por-km:0.13}") double fatorCo2KgPorKm) {
        this.repository = repository;
        this.fatorCo2KgPorKm = fatorCo2KgPorKm;
    }

    public List<Consulta> listar() {
        return repository.findAll();
    }

    public Consulta buscar(String id) {
        return repository.findById(id).orElseThrow(() -> new RecursoNaoEncontradoException("Consulta", id));
    }

    public Consulta criar(Consulta consulta) {
        consulta.setId(null);
        preencherCo2Evitado(consulta);
        return repository.save(consulta);
    }

    public Consulta atualizar(String id, Consulta dados) {
        buscar(id);
        dados.setId(id);
        preencherCo2Evitado(dados);
        return repository.save(dados);
    }

    public void excluir(String id) {
        repository.delete(buscar(id));
    }

    /** CO2 que o paciente deixaria de emitir: distância x 2 (ida e volta) x fator, em kg. */
    public double calcularCo2EvitadoKg(double distanciaKm) {
        return arredondar(distanciaKm * 2 * fatorCo2KgPorKm);
    }

    private void preencherCo2Evitado(Consulta consulta) {
        if (consulta.getModalidade() == Modalidade.telemedicina
                && consulta.getDistanciaEvitadaKm() != null
                && consulta.getCo2EvitadoKg() == null) {
            consulta.setCo2EvitadoKg(calcularCo2EvitadoKg(consulta.getDistanciaEvitadaKm()));
        }
    }

    private static double arredondar(double valor) {
        return Math.round(valor * 100.0) / 100.0;
    }
}
