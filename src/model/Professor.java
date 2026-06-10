package model;

public class Professor extends Usuario implements Moderador {
    public Professor(String nome, String login, String email, String senha) {
        super(nome, login, email, senha);
    }

    @Override
    public int getNivel() {
        return 3;
    }

    @Override
    public String getCargo() {
        return "Docente";
    }

    @Override
    public boolean podeModerar() {
        return true;
    }
}
