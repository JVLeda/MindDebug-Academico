package model;

public abstract class Usuario extends Entidade {
    private String nome;
    private String login;
    private String email;
    private String senha;

    public Usuario(String nome, String login, String email, String senha) {
        this.nome = nome;
        this.login = login;
        this.email = email;
        this.senha = senha;
    }

    public abstract int getNivel();

    public abstract String getCargo();

    public boolean autenticar(String login, String senha) {
        return this.login.equalsIgnoreCase(login) && this.senha.equals(senha);
    }

    public boolean podeApagarConteudoDe(Usuario autor) {
        return this == autor || (getNivel() >= 2 && getNivel() < 4);
    }

    public void trocarSenha(String novaSenha) {
        this.senha = novaSenha;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getSenha() {
        return senha;
    }

    public void setSenha(String senha) {
        this.senha = senha;
    }

    @Override
    public String toString() {
        return nome + " (" + getCargo() + ")";
    }
}
