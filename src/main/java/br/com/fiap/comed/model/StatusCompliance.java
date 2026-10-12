package br.com.fiap.comed.model;

// Constantes em minúsculas porque são exatamente os valores gravados no MongoDB e expostos no JSON.
public enum StatusCompliance {
    regular,
    pendente_renovacao,
    suspenso
}
