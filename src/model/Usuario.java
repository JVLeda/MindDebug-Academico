package model;

import excecao.AcessoNegadoException;

import java.util.ArrayList;

public abstract class Usuario extends Entidade {
    private String nome;
    private String login;
    private String email;
    private String senha;
    private String matricula;
    private boolean ativo = true;
    private boolean trocaDeSenhaObrigatoria = false;

    public Usuario(String nome, String login, String email, String senha) {
        this.nome = nome;
        this.login = login;
        this.email = email;
        this.senha = senha;
    }

    public abstract int getNivel();

    public abstract String getCargo();

    public abstract Perfil getPerfil();

    public boolean autenticar(String login, String senha) {
        return ativo && this.login.equalsIgnoreCase(login) && this.senha.equals(senha);
    }

    public boolean ehOMesmoUsuario(Usuario outro) {
        if (outro == null) {
            return false;
        }
        return this == outro || (getId() != 0 && getId() == outro.getId());
    }

    // Permissoes. O padrao e negar; cada subclasse sobrescreve o que o seu perfil pode fazer.

    public boolean podeGerenciarEstrutura() {
        return false;
    }

    public boolean podeCadastrarMonitor() {
        return false;
    }

    public boolean podeGerenciarMateria(Materia materia) {
        return false;
    }

    public boolean podeAtenderMonitoria() {
        return false;
    }

    public boolean podeRecusarPedido(PedidoMonitoria pedido) {
        return podeAtenderMonitoria();
    }

    public boolean podeRegistrarFeedback() {
        return false;
    }

    public boolean podeSolicitarMonitoria() {
        return false;
    }

    public boolean podeResponderQuiz() {
        return false;
    }

    public boolean podeApagarConteudoDe(Usuario autor) {
        return this == autor;
    }

    // Acoes que dependem do perfil. O padrao e recusar; a subclasse que pode fazer sobrescreve.

    public void registrarFeedback(Aula aula, int nota, String comentario) {
        throw new AcessoNegadoException("Somente aluno registra feedback.");
    }

    /** Cria o feedback deste usuario sem registrar na aula (o controller grava antes de registrar). */
    public Feedback criarFeedback(int nota, String comentario) {
        throw new AcessoNegadoException("Somente aluno registra feedback.");
    }

    public PedidoMonitoria criarPedidoMonitoria(Aula aula, Monitor monitor, String duvida, String data, String horario) {
        throw new AcessoNegadoException("Somente aluno solicita monitoria.");
    }

    public TentativaQuiz iniciarTentativaQuiz(Quiz quiz) {
        throw new AcessoNegadoException("Somente estudante responde quiz.");
    }

    public Monitor escolherMonitorDoAtendimento(Monitor monitorInformado) {
        return monitorInformado;
    }

    public ArrayList<Curso> filtrarCursosVisiveis(ArrayList<Curso> cursos) {
        return cursos;
    }

    // Cada subclasse entra somente na lista do proprio perfil.

    public void incluirEmEstudantes(ArrayList<Estudante> estudantes) {
    }

    public void incluirEmMonitores(ArrayList<Monitor> monitores) {
    }

    public void incluirEmProfessores(ArrayList<Professor> professores) {
    }

    // Dados que so existem em alguns perfis. Quem nao tem devolve null.

    public Curso getCurso() {
        return null;
    }

    public String getTipoMonitor() {
        return null;
    }

    public void trocarSenha(String novaSenha) {
        this.senha = novaSenha;
        this.trocaDeSenhaObrigatoria = false;
    }

    public void redefinirSenha(String senhaProvisoria) {
        this.senha = senhaProvisoria;
        this.trocaDeSenhaObrigatoria = true;
    }

    public boolean isTrocaDeSenhaObrigatoria() {
        return trocaDeSenhaObrigatoria;
    }

    public void setTrocaDeSenhaObrigatoria(boolean trocaDeSenhaObrigatoria) {
        this.trocaDeSenhaObrigatoria = trocaDeSenhaObrigatoria;
    }

    public boolean isAtivo() {
        return ativo;
    }

    public void setAtivo(boolean ativo) {
        this.ativo = ativo;
    }

    public String getMatricula() {
        return matricula;
    }

    public void setMatricula(String matricula) {
        this.matricula = matricula;
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
