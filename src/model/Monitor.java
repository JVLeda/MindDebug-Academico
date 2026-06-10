package model;

public class Monitor extends Usuario implements Moderador {
    private String tipo;

    public Monitor(String nome, String login, String email, String senha, String tipo) {
        super(nome, login, email, senha);
        this.tipo = tipo;
    }

    @Override
    public int getNivel() {
        return 2;
    }

    @Override
    public String getCargo() {
        return "Monitor";
    }

    @Override
    public boolean podeModerar() {
        return true;
    }

    public String getTipo() {
        return tipo;
    }
}
