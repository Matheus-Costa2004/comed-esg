package br.com.fiap.comed.model;

// Constantes em minúsculas porque são exatamente os valores gravados no MongoDB e expostos no JSON.
public enum TipoEventoAuditoria {
    acesso_dados,
    consentimento,
    exclusao_dados,
    acesso_negado,
    revisao_alerta_ia
}
