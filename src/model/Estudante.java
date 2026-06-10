package model;

public class Estudante extends Usuario {
    private String matricula;
    private Curso curso;

    public Estudante(String nome, String email, String matricula, String senha, Curso curso) {
        super(nome, matricula, email, senha);
        this.matricula = matricula;
        this.curso = curso;
    }

    @Override
    public int getNivel() {
        return 1;
    }

    @Override
    public String getCargo() {
        return "Aluno";
    }

    public String getMatricula() {
        return matricula;
    }

    public Curso getCurso() {
        return curso;
    }
}
