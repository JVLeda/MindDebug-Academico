package model;

public enum DiaSemana {
    SEGUNDA("Segunda"),
    TERCA("Terca"),
    QUARTA("Quarta"),
    QUINTA("Quinta"),
    SEXTA("Sexta"),
    SABADO("Sabado");

    private String nome;

    DiaSemana(String nome) {
        this.nome = nome;
    }

    public String getNome() {
        return nome;
    }

    public static DiaSemana buscarPorNome(String nome) {
        for (DiaSemana dia : values()) {
            if (dia.nome.equals(nome)) {
                return dia;
            }
        }
        return null;
    }
}
