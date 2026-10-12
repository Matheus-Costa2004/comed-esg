package br.com.fiap.comed.model;

// Constantes em minúsculas porque são exatamente os valores gravados no MongoDB e expostos no JSON.
public enum TipoAlerta {
    interacao_medicamentosa,
    pa_critica,
    hipotese_diagnostica,
    tendencia_hba1c,
    risco_psicossocial
}
