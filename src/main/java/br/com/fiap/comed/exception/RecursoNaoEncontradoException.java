package br.com.fiap.comed.exception;

public class RecursoNaoEncontradoException extends RuntimeException {

    public RecursoNaoEncontradoException(String recurso, String id) {
        super(recurso + " não encontrado(a): " + id);
    }
}
