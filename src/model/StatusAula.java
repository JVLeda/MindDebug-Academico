package model;

/**
 * Situacao de uma aula, calculada pela media dos feedbacks
 * comparada com a media esperada pelo professor.
 */
public enum StatusAula {
    SEM_DADOS("Sem dados"),
    CRITICO("Critico"),
    REGULAR("Regular"),
    SATISFATORIO("Satisfatorio");

    private String nome;

    StatusAula(String nome) {
        this.nome = nome;
    }

    public String getNome() {
        return nome;
    }
}
