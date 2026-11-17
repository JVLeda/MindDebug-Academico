package controller;

import controller.administrador.EstruturaAcademicaController;
import controller.administrador.UsuarioController;
import controller.aluno.FeedbackController;
import controller.aluno.MatriculaController;
import controller.comum.AutenticacaoController;
import controller.comum.ChatController;
import controller.comum.MaterialController;
import controller.comum.QuizController;
import controller.monitor.SolicitacaoController;
import controller.professor.AulaController;
import controller.professor.GrupoEstudoController;
import controller.professor.MonitoresDaMateriaController;
import dao.BancoDados;
import model.AlternativaQuiz;
import model.Aula;
import model.Chat;
import model.Curso;
import model.Estudante;
import model.GrupoEstudo;
import model.Materia;
import model.Material;
import model.Mensagem;
import model.Monitor;
import model.PedidoMonitoria;
import model.PerguntaQuiz;
import model.Periodo;
import model.Professor;
import model.Quiz;
import model.TentativaQuiz;
import model.Usuario;

import java.util.ArrayList;

/**
 * Fachada usada pelas telas Swing enquanto a interface JavaFX nao fica pronta.
 * Nao tem regra propria: cada metodo repassa a acao ao controller do ator responsavel,
 * nos pacotes controller.administrador, controller.professor, controller.aluno,
 * controller.monitor e controller.comum.
 */
public class MindDebugController {
    private AutenticacaoController autenticacao;
    private UsuarioController usuarios;
    private EstruturaAcademicaController estrutura;
    private AulaController aulas;
    private MonitoresDaMateriaController monitoresDaMateria;
    private GrupoEstudoController grupos;
    private FeedbackController feedbacks;
    private MatriculaController matriculas;
    private SolicitacaoController solicitacoes;
    private ChatController chats;
    private MaterialController materiais;
    private QuizController quizzes;

    public MindDebugController(BancoDados banco) {
        new DadosIniciais().gravarSeVazio(banco);
        autenticacao = new AutenticacaoController(banco);
        usuarios = new UsuarioController(banco);
        estrutura = new EstruturaAcademicaController(banco);
        aulas = new AulaController(banco);
        monitoresDaMateria = new MonitoresDaMateriaController(banco);
        grupos = new GrupoEstudoController(banco);
        feedbacks = new FeedbackController(banco);
        matriculas = new MatriculaController(banco);
        solicitacoes = new SolicitacaoController(banco);
        chats = new ChatController(banco);
        materiais = new MaterialController(banco);
        quizzes = new QuizController(banco);
    }

    // ------------------------------------------------------------------ comum: autenticacao

    public Usuario fazerLogin(String login, String senha) {
        return autenticacao.fazerLogin(login, senha);
    }

    public void alterarMinhaSenha(Usuario usuario, String novaSenha) {
        autenticacao.alterarMinhaSenha(usuario, novaSenha);
    }

    // ------------------------------------------------------------------ administrador: usuarios

    public ArrayList<Usuario> listarUsuarios() {
        return usuarios.listarUsuarios();
    }

    public ArrayList<Professor> listarProfessores() {
        return usuarios.listarProfessores();
    }

    public ArrayList<Monitor> listarMonitores() {
        return usuarios.listarMonitores();
    }

    public Estudante cadastrarEstudante(String nome, String email, String matricula, String senha, Curso curso) {
        return usuarios.cadastrarEstudante(nome, email, matricula, senha, curso);
    }

    public Professor cadastrarProfessor(Usuario usuarioLogado, String nome, String login, String email, String senha) {
        return usuarios.cadastrarProfessor(usuarioLogado, nome, login, email, senha);
    }

    public Monitor cadastrarMonitor(Usuario usuarioLogado, String nome, String login, String email, String senha, String tipo) {
        return usuarios.cadastrarMonitor(usuarioLogado, nome, login, email, senha, tipo);
    }

    public void cadastrarUsuarioPeloCargo(Usuario usuarioLogado, String nome, String login, String senha, String cargo) {
        usuarios.cadastrarUsuarioPeloCargo(usuarioLogado, nome, login, senha, cargo);
    }

    public void removerUsuario(Usuario usuarioLogado, Usuario usuario) {
        usuarios.removerUsuario(usuarioLogado, usuario);
    }

    public void desativarUsuario(Usuario usuarioLogado, Usuario usuario) {
        usuarios.desativarUsuario(usuarioLogado, usuario);
    }

    public void redefinirSenhaDeUsuario(Usuario usuarioLogado, Usuario usuario, String novaSenha) {
        usuarios.redefinirSenhaDeUsuario(usuarioLogado, usuario, novaSenha);
    }

    // ------------------------------------------------------------------ administrador: estrutura academica

    public ArrayList<Curso> listarCursos() {
        return estrutura.listarCursos();
    }

    public ArrayList<Curso> listarCursosVisiveis(Usuario usuarioLogado) {
        return estrutura.listarCursosVisiveis(usuarioLogado);
    }

    public Curso criarCurso(Usuario usuarioLogado, String nome, String sigla, String area, int cargaHoraria, String coordenador) {
        return estrutura.criarCurso(usuarioLogado, nome, sigla, area, cargaHoraria, coordenador);
    }

    public void removerCurso(Usuario usuarioLogado, Curso curso) {
        estrutura.removerCurso(usuarioLogado, curso);
    }

    public Periodo criarPeriodo(Usuario usuarioLogado, Curso curso, int numero, String nome) {
        return estrutura.criarPeriodo(usuarioLogado, curso, numero, nome);
    }

    public void removerPeriodo(Usuario usuarioLogado, Curso curso, Periodo periodo) {
        estrutura.removerPeriodo(usuarioLogado, curso, periodo);
    }

    public Materia criarMateria(Usuario usuarioLogado, Periodo periodo, String nome, Professor professor) {
        return estrutura.criarMateria(usuarioLogado, periodo, nome, professor);
    }

    public void removerMateria(Usuario usuarioLogado, Periodo periodo, Materia materia) {
        estrutura.removerMateria(usuarioLogado, periodo, materia);
    }

    public void designarProfessorNaMateria(Usuario usuarioLogado, Materia materia, Professor professor) {
        estrutura.designarProfessorNaMateria(usuarioLogado, materia, professor);
    }

    public void removerProfessorDaMateria(Usuario usuarioLogado, Materia materia) {
        estrutura.removerProfessorDaMateria(usuarioLogado, materia);
    }

    // ------------------------------------------------------------------ professor

    public Aula criarAula(Usuario usuarioLogado, Materia materia, String titulo, String data) {
        return aulas.criarAula(usuarioLogado, materia, titulo, data);
    }

    public void removerAula(Usuario usuarioLogado, Materia materia, Aula aula) {
        aulas.removerAula(usuarioLogado, materia, aula);
    }

    public void adicionarMonitorNaMateria(Usuario usuarioLogado, Materia materia, Monitor monitor) {
        monitoresDaMateria.adicionarMonitorNaMateria(usuarioLogado, materia, monitor);
    }

    public void removerMonitorDaMateria(Usuario usuarioLogado, Materia materia, Monitor monitor) {
        monitoresDaMateria.removerMonitorDaMateria(usuarioLogado, materia, monitor);
    }

    public GrupoEstudo criarGrupoEstudo(Usuario usuarioLogado, Materia materia, String nome, Aula aula, Monitor monitor) {
        return grupos.criarGrupoEstudo(usuarioLogado, materia, nome, aula, monitor);
    }

    public void adicionarAlunoAoGrupo(Usuario usuarioLogado, Materia materia, GrupoEstudo grupo, Estudante estudante) {
        grupos.adicionarAlunoAoGrupo(usuarioLogado, materia, grupo, estudante);
    }

    public void removerAlunoDoGrupo(Usuario usuarioLogado, Materia materia, GrupoEstudo grupo, Estudante estudante) {
        grupos.removerAlunoDoGrupo(usuarioLogado, materia, grupo, estudante);
    }

    public void adicionarMonitorAoGrupo(Usuario usuarioLogado, Materia materia, GrupoEstudo grupo, Monitor monitor) {
        grupos.adicionarMonitorAoGrupo(usuarioLogado, materia, grupo, monitor);
    }

    public void removerMonitorDoGrupo(Usuario usuarioLogado, Materia materia, GrupoEstudo grupo, Monitor monitor) {
        grupos.removerMonitorDoGrupo(usuarioLogado, materia, grupo, monitor);
    }

    public void removerGrupoEstudo(Usuario usuarioLogado, Materia materia, GrupoEstudo grupo) {
        grupos.removerGrupoEstudo(usuarioLogado, materia, grupo);
    }

    // ------------------------------------------------------------------ aluno

    public void registrarFeedback(Usuario usuarioLogado, Aula aula, int nota, String comentario) {
        feedbacks.registrarFeedback(usuarioLogado, aula, nota, comentario);
    }

    public void entrarNaMateria(Usuario usuarioLogado, Materia materia) {
        matriculas.entrarNaMateria(usuarioLogado, materia);
    }

    // ------------------------------------------------------------------ monitor

    public PedidoMonitoria solicitarMonitoria(Usuario usuarioLogado, Materia materia, Aula aula, Monitor monitor, String duvida, String data, String horario) {
        return solicitacoes.solicitarMonitoria(usuarioLogado, materia, aula, monitor, duvida, data, horario);
    }

    public void aceitarMonitoria(Usuario usuarioLogado, PedidoMonitoria pedido, Monitor monitor) {
        solicitacoes.aceitarMonitoria(usuarioLogado, pedido, monitor);
    }

    public void recusarMonitoria(Usuario usuarioLogado, PedidoMonitoria pedido) {
        solicitacoes.recusarMonitoria(usuarioLogado, pedido);
    }

    // ------------------------------------------------------------------ comum: chats, materiais e quiz

    public void enviarMensagem(Chat chat, Usuario autor, String texto) {
        chats.enviarMensagem(chat, autor, texto);
    }

    public void apagarMensagem(Chat chat, Usuario usuarioLogado, Mensagem mensagem) {
        chats.apagarMensagem(chat, usuarioLogado, mensagem);
    }

    public void adicionarMaterial(Materia materia, Usuario autor, String titulo, String tipo, String referencia) {
        materiais.adicionarMaterial(materia, autor, titulo, tipo, referencia);
    }

    public void removerMaterial(Materia materia, Usuario usuarioLogado, Material material) {
        materiais.removerMaterial(materia, usuarioLogado, material);
    }

    public Quiz criarQuiz(Usuario usuarioLogado, Materia materia, Aula aula, String titulo, String descricao) {
        return quizzes.criarQuiz(usuarioLogado, materia, aula, titulo, descricao);
    }

    public PerguntaQuiz adicionarPerguntaAoQuiz(Usuario usuarioLogado, Materia materia, Quiz quiz,
            String enunciado, String dificuldade, String[] textosAlternativas,
            String[] explicacoes, int indiceCorreta) {
        return quizzes.adicionarPerguntaAoQuiz(usuarioLogado, materia, quiz, enunciado, dificuldade,
                textosAlternativas, explicacoes, indiceCorreta);
    }

    public void removerQuiz(Usuario usuarioLogado, Materia materia, Aula aula) {
        quizzes.removerQuiz(usuarioLogado, materia, aula);
    }

    public TentativaQuiz iniciarTentativaQuiz(Usuario usuarioLogado, Materia materia, Quiz quiz) {
        return quizzes.iniciarTentativaQuiz(usuarioLogado, materia, quiz);
    }

    public void registrarRespostaQuiz(Usuario usuarioLogado, TentativaQuiz tentativa,
            PerguntaQuiz pergunta, AlternativaQuiz alternativa) {
        quizzes.registrarRespostaQuiz(usuarioLogado, tentativa, pergunta, alternativa);
    }

    public void finalizarTentativaQuiz(Usuario usuarioLogado, TentativaQuiz tentativa) {
        quizzes.finalizarTentativaQuiz(usuarioLogado, tentativa);
    }
}
