package model;

public class Dev extends Usuario implements Moderador {
    public Dev(String nome, String login, String email, String senha) {
        super(nome, login, email, senha);
    }

    @Override
    public int getNivel() {
        return 4;
    }

    @Override
    public String getCargo() {
        return "Dev";
    }

    @Override
    public boolean podeModerar() {
        return true;
    }
}
