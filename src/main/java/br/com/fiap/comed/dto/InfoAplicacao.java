package br.com.fiap.comed.dto;

import java.time.Instant;

public record InfoAplicacao(String aplicacao, String versao, String ambiente, Instant timestamp) {
}
