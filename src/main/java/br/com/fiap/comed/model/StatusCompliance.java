package br.com.fiap.comed.model;

// Minúsculas porque são os valores gravados no MongoDB e expostos no JSON.
public enum StatusCompliance {
    regular,
    pendente_renovacao,
    suspenso
}
