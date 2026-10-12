package br.com.fiap.comed.controller;

import java.time.Instant;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.fiap.comed.dto.InfoAplicacao;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

/** Identifica qual build está rodando em qual ambiente; o smoke test do pipeline confere a versão aqui. */
@RestController
@Tag(name = "Info")
public class InfoController {

    private final String aplicacao;
    private final String versao;
    private final String ambiente;

    public InfoController(@Value("${spring.application.name}") String aplicacao,
            @Value("${comed.versao}") String versao,
            @Value("${comed.ambiente}") String ambiente) {
        this.aplicacao = aplicacao;
        this.versao = versao;
        this.ambiente = ambiente;
    }

    @GetMapping("/api/info")
    @Operation(summary = "Nome, versão (SHA do commit) e ambiente da aplicação")
    public InfoAplicacao info() {
        return new InfoAplicacao(aplicacao, versao, ambiente, Instant.now());
    }
}
