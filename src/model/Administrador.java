package model;

public class Administrador extends Usuario implements Moderador {
    public Administrador(String nome, String login, String email, String senha) {
        super(nome, login, email, senha);
    }

    @Override
    public int getNivel() {
        return 4;
    }

    @Override
    public String getCargo() {
        return "Administrador do Sistema";
    }

    @Override
    public boolean podeModerar() {
        return true;
    }
}
