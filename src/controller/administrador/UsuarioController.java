package controller.administrador;

import controller.ControllerBase;
import dao.BancoDados;
import dao.UsuarioDao;
import excecao.DadoInvalidoException;
import excecao.RegraDeNegocioException;
import model.Curso;
import model.Estudante;
import model.Monitor;
import model.Professor;
import model.Usuario;

import java.util.ArrayList;

/**
 * Cadastro de usuarios, modulo do Administrador. O aluno tambem se cadastra pela tela de entrada
 * e o professor cadastra monitores; as permissoes de cada caso ficam nas subclasses de Usuario.
 * Conta criada por outra pessoa exige troca de senha no primeiro acesso (RN02J).
 */
public class UsuarioController extends ControllerBase {
    private UsuarioDao usuarioDao = new UsuarioDao();

    public UsuarioController(BancoDados banco) {
        super(banco);
    }

    public ArrayList<Usuario> listarUsuarios() {
        return banco.getUsuarios();
    }

    public ArrayList<Professor> listarProfessores() {
        ArrayList<Professor> professores = new ArrayList<>();
        for (Usuario usuario : banco.getUsuarios()) {
            usuario.incluirEmProfessores(professores);
        }
        return professores;
    }

    public ArrayList<Monitor> listarMonitores() {
        ArrayList<Monitor> monitores = new ArrayList<>();
        for (Usuario usuario : banco.getUsuarios()) {
            usuario.incluirEmMonitores(monitores);
        }
        return monitores;
    }

    /** Cadastro feito pelo proprio aluno na tela de entrada; a senha ja e dele. */
    public Estudante cadastrarEstudante(String nome, String email, String matricula, String senha, Curso curso) {
        Estudante estudante = montarEstudante(nome, email, matricula, senha, curso);
        gravar(estudante);
        return estudante;
    }

    public Estudante cadastrarEstudantePeloAdministrador(Usuario usuarioLogado, String nome, String email,
            String matricula, String senha, Curso curso) {
        exigirGestaoDaEstrutura(usuarioLogado, "cadastrar aluno");
        Estudante estudante = montarEstudante(nome, email, matricula, senha, curso);
        estudante.setTrocaDeSenhaObrigatoria(true);
        gravar(estudante);
        return estudante;
    }

    public Professor cadastrarProfessor(Usuario usuarioLogado, String nome, String login, String email, String senha) {
        exigirGestaoDaEstrutura(usuarioLogado, "cadastrar professor");
        validarLoginLivre(login);
        validarEmailLivre(email);
        Professor professor = new Professor(
                validador.texto(nome, "Nome"),
                validador.texto(login, "Login"),
                validador.email(email),
                validador.senha(senha)
        );
        professor.setTrocaDeSenhaObrigatoria(true);
        gravar(professor);
        return professor;
    }

    public Monitor cadastrarMonitor(Usuario usuarioLogado, String nome, String login, String email, String senha, String tipo) {
        if (usuarioLogado == null || !usuarioLogado.podeCadastrarMonitor()) {
            negarAcesso("cadastrar monitor");
        }
        validarLoginLivre(login);
        validarEmailLivre(email);
        Monitor monitor = new Monitor(
                validador.texto(nome, "Nome"),
                validador.texto(login, "Login"),
                validador.email(email),
                validador.senha(senha),
                validador.texto(tipo, "Tipo")
        );
        monitor.setTrocaDeSenhaObrigatoria(true);
        gravar(monitor);
        return monitor;
    }

    public void cadastrarUsuarioPeloCargo(Usuario usuarioLogado, String nome, String login, String senha, String cargo) {
        if ("Docente".equals(cargo)) {
            cadastrarProfessor(usuarioLogado, nome, login, login + "@minddebug.edu.br", senha);
        } else if ("Monitor".equals(cargo)) {
            cadastrarMonitor(usuarioLogado, nome, login, login + "@minddebug.edu.br", senha, "Monitor");
        } else {
            Curso curso = banco.getCursos().isEmpty() ? null : banco.getCursos().get(0);
            cadastrarEstudantePeloAdministrador(usuarioLogado, nome, login + "@aluno.edu.br", login, senha, curso);
        }
    }

    /** Usuario com feedbacks, mensagens ou matriculas nao e apagado; o banco recusa e avisa. */
    public void removerUsuario(Usuario usuarioLogado, Usuario usuario) {
        exigirGestaoDaEstrutura(usuarioLogado, "remover usuario");
        validarOutroUsuario(usuarioLogado, usuario);
        usuarioDao.remover(usuario);
        banco.removerUsuario(usuario);
    }

    /** O usuario desativado continua no banco com o historico dele, mas nao entra mais no sistema. */
    public void desativarUsuario(Usuario usuarioLogado, Usuario usuario) {
        exigirGestaoDaEstrutura(usuarioLogado, "desativar usuario");
        validarOutroUsuario(usuarioLogado, usuario);
        usuarioDao.desativar(usuario);
        usuario.setAtivo(false);
    }

    /** A senha redefinida e provisoria: o usuario troca no proximo acesso. */
    public void redefinirSenhaDeUsuario(Usuario usuarioLogado, Usuario usuario, String novaSenha) {
        exigirGestaoDaEstrutura(usuarioLogado, "redefinir senha");
        if (usuario == null) {
            throw new DadoInvalidoException("Selecione um usuario.");
        }
        String senha = validador.senha(novaSenha);
        usuarioDao.atualizarSenha(usuario, senha, true);
        usuario.redefinirSenha(senha);
    }

    private Estudante montarEstudante(String nome, String email, String matricula, String senha, Curso curso) {
        validarLoginLivre(matricula);
        validarEmailLivre(email);
        return new Estudante(
                validador.texto(nome, "Nome"),
                validador.email(email),
                validador.texto(matricula, "Matricula"),
                validador.senha(senha),
                curso
        );
    }

    private void gravar(Usuario usuario) {
        usuarioDao.inserir(usuario);
        banco.adicionarUsuario(usuario);
    }

    private void validarOutroUsuario(Usuario usuarioLogado, Usuario usuario) {
        if (usuario == null) {
            throw new DadoInvalidoException("Selecione um usuario.");
        }
        if (usuario.ehOMesmoUsuario(usuarioLogado)) {
            throw new RegraDeNegocioException("Voce nao pode remover nem desativar a propria conta.");
        }
    }

    private void validarEmailLivre(String email) {
        if (email == null) {
            return;
        }
        for (Usuario usuario : banco.getUsuarios()) {
            if (usuario.getEmail().equalsIgnoreCase(email.trim())) {
                throw new RegraDeNegocioException("Email ja cadastrado.");
            }
        }
    }

    private void validarLoginLivre(String login) {
        if (banco.buscarUsuarioPorLogin(login) != null) {
            throw new RegraDeNegocioException("Login ja cadastrado.");
        }
    }
}
