package controller;

import dao.BancoDao;
import dao.BancoDados;
import dao.CursoDao;
import dao.UsuarioDao;
import model.Aula;
import model.Chat;
import model.Curso;
import model.Dev;
import model.Estudante;
import model.Feedback;
import model.GrupoEstudo;
import model.Materia;
import model.Material;
import model.Mensagem;
import model.Monitor;
import model.PedidoMonitoria;
import model.Periodo;
import model.Professor;
import model.Usuario;
import util.Validador;

import java.util.ArrayList;

public class MindDebugController {
    private BancoDados banco;
    private BancoDao bancoDao;
    private UsuarioDao usuarioDao;
    private CursoDao cursoDao;
    private Validador validador = new Validador();

    public MindDebugController(BancoDados banco) {
        this.banco = banco;
        this.bancoDao = new BancoDao();
        this.usuarioDao = new UsuarioDao(banco);
        this.cursoDao = new CursoDao(banco);
        boolean bancoEstavaVazio = usuarioDao.listar().isEmpty();
        criarDadosIniciais();
        if (bancoEstavaVazio) {
            bancoDao.salvar(banco);
        }
    }

    public Usuario fazerLogin(String login, String senha) {
        Usuario usuario = usuarioDao.buscarPorLogin(login);
        if (usuario != null && usuario.autenticar(login, senha)) {
            return usuario;
        }
        throw new RuntimeException("Login ou senha invalidos.");
    }

    public void salvar() {
        bancoDao.salvar(banco);
    }

    public ArrayList<Usuario> listarUsuarios() {
        return usuarioDao.listar();
    }

    public ArrayList<Curso> listarCursos() {
        return cursoDao.listar();
    }

    public ArrayList<Professor> listarProfessores() {
        ArrayList<Professor> professores = new ArrayList<>();
        for (Usuario usuario : usuarioDao.listar()) {
            if (usuario instanceof Professor) {
                professores.add((Professor) usuario);
            }
        }
        return professores;
    }

    public ArrayList<Monitor> listarMonitores() {
        ArrayList<Monitor> monitores = new ArrayList<>();
        for (Usuario usuario : usuarioDao.listar()) {
            if (usuario instanceof Monitor) {
                monitores.add((Monitor) usuario);
            }
        }
        return monitores;
    }

    public Estudante cadastrarEstudante(String nome, String email, String matricula, String senha, Curso curso) {
        validarLoginLivre(matricula);
        Estudante estudante = new Estudante(
                validador.texto(nome, "Nome"),
                validador.email(email),
                validador.texto(matricula, "Matricula"),
                validador.senha(senha),
                curso
        );
        usuarioDao.cadastrar(estudante);
        return estudante;
    }

    public Professor cadastrarProfessor(Usuario usuarioLogado, String nome, String login, String email, String senha) {
        exigirNivel(usuarioLogado, 4, "cadastrar professor");
        validarLoginLivre(login);
        Professor professor = new Professor(
                validador.texto(nome, "Nome"),
                validador.texto(login, "Login"),
                validador.email(email),
                validador.senha(senha)
        );
        usuarioDao.cadastrar(professor);
        return professor;
    }

    public Monitor cadastrarMonitor(Usuario usuarioLogado, String nome, String login, String email, String senha, String tipo) {
        exigirNivel(usuarioLogado, 3, "cadastrar monitor");
        validarLoginLivre(login);
        Monitor monitor = new Monitor(
                validador.texto(nome, "Nome"),
                validador.texto(login, "Login"),
                validador.email(email),
                validador.senha(senha),
                validador.texto(tipo, "Tipo")
        );
        usuarioDao.cadastrar(monitor);
        return monitor;
    }

    public void cadastrarUsuarioPeloCargo(Usuario usuarioLogado, String nome, String login, String senha, String cargo) {
        if ("Docente".equals(cargo)) {
            cadastrarProfessor(usuarioLogado, nome, login, login + "@minddebug.edu.br", senha);
        } else if ("Monitor".equals(cargo)) {
            cadastrarMonitor(usuarioLogado, nome, login, login + "@minddebug.edu.br", senha, "Monitor");
        } else {
            Curso curso = cursoDao.listar().isEmpty() ? null : cursoDao.listar().get(0);
            cadastrarEstudante(nome, login + "@aluno.edu.br", login, senha, curso);
        }
    }

    public void removerUsuario(Usuario usuarioLogado, Usuario usuario) {
        exigirNivel(usuarioLogado, 4, "remover usuario");
        usuarioDao.remover(usuario);
    }

    public Curso criarCurso(Usuario usuarioLogado, String nome, String sigla, String area, int cargaHoraria, String coordenador) {
        exigirNivel(usuarioLogado, 4, "criar curso");
        Curso curso = new Curso(
                validador.texto(nome, "Nome"),
                validador.texto(sigla, "Sigla"),
                validador.texto(area, "Area"),
                cargaHoraria,
                validador.texto(coordenador, "Coordenador")
        );
        cursoDao.cadastrar(curso);
        return curso;
    }

    public Periodo criarPeriodo(Usuario usuarioLogado, Curso curso, int numero, String nome) {
        exigirNivel(usuarioLogado, 4, "criar periodo");
        if (curso == null) {
            throw new RuntimeException("Selecione um curso.");
        }
        Periodo periodo = new Periodo(numero, validador.texto(nome, "Nome do periodo"));
        periodo.setId(banco.gerarId());
        curso.adicionarPeriodo(periodo);
        return periodo;
    }

    public Materia criarMateria(Usuario usuarioLogado, Periodo periodo, String nome, Professor professor) {
        exigirNivel(usuarioLogado, 4, "criar disciplina");
        if (periodo == null) {
            throw new RuntimeException("Selecione um periodo.");
        }
        if (professor == null) {
            throw new RuntimeException("Selecione um professor.");
        }
        Materia materia = new Materia(validador.texto(nome, "Nome da disciplina"), professor);
        materia.setId(banco.gerarId());
        periodo.adicionarMateria(materia);
        return materia;
    }

    public Aula criarAula(Usuario usuarioLogado, Materia materia, String titulo, String data) {
        exigirProfessorDaMateriaOuDev(usuarioLogado, materia, "criar aula");
        Aula aula = new Aula(validador.texto(titulo, "Titulo da aula"), data, "");
        aula.setId(banco.gerarId());
        materia.adicionarAula(aula);
        return aula;
    }

    public void adicionarMonitorNaMateria(Usuario usuarioLogado, Materia materia, Monitor monitor) {
        exigirProfessorDaMateriaOuDev(usuarioLogado, materia, "adicionar monitor");
        materia.adicionarParticipante(monitor);
    }

    public void registrarFeedback(Usuario usuarioLogado, Aula aula, int nota, String comentario) {
        if (!(usuarioLogado instanceof Estudante)) {
            throw new RuntimeException("Somente aluno registra feedback.");
        }
        aula.registrarFeedback(new Feedback((Estudante) usuarioLogado, validador.nota(nota), comentario));
    }

    public void adicionarMaterial(Materia materia, Usuario autor, String titulo, String tipo, String referencia) {
        Material material = new Material(
                validador.texto(titulo, "Titulo"),
                validador.texto(tipo, "Tipo"),
                validador.texto(referencia, "Referencia"),
                autor
        );
        material.setId(banco.gerarId());
        materia.adicionarMaterial(material);
    }

    public void removerMaterial(Materia materia, Usuario usuarioLogado, Material material) {
        if (material == null) {
            throw new RuntimeException("Selecione um material.");
        }
        if (!usuarioLogado.podeApagarConteudoDe(material.getAutor())) {
            throw new RuntimeException("Sem permissao para apagar este material.");
        }
        materia.getMateriais().remove(material);
    }

    public void enviarMensagem(Chat chat, Usuario autor, String texto) {
        Mensagem mensagem = chat.enviar(autor, validador.texto(texto, "Mensagem"));
        mensagem.setId(banco.gerarId());
    }

    public void apagarMensagem(Chat chat, Usuario usuarioLogado, Mensagem mensagem) {
        if (mensagem == null) {
            throw new RuntimeException("Selecione uma mensagem.");
        }
        if (!usuarioLogado.podeApagarConteudoDe(mensagem.getAutor())) {
            throw new RuntimeException("Sem permissao para apagar esta mensagem.");
        }
        chat.apagar(mensagem);
    }

    public GrupoEstudo criarGrupoEstudo(Usuario usuarioLogado, Materia materia, String nome, Aula aula, Monitor monitor) {
        exigirProfessorDaMateriaOuDev(usuarioLogado, materia, "criar grupo de estudo");
        if (aula != null && !materia.getAulas().contains(aula)) {
            throw new RuntimeException("A aula escolhida nao pertence a esta materia.");
        }
        if (monitor != null && !materia.getMonitores().contains(monitor)) {
            throw new RuntimeException("O monitor escolhido nao esta cadastrado nesta materia.");
        }

        String nomeValidado = validador.texto(nome, "Nome do grupo");
        for (GrupoEstudo grupoExistente : materia.getGrupos()) {
            if (grupoExistente.getNome().equalsIgnoreCase(nomeValidado)) {
                throw new RuntimeException("Ja existe um grupo com este nome.");
            }
        }

        GrupoEstudo grupo = new GrupoEstudo(nomeValidado, aula, monitor);
        grupo.setId(banco.gerarId());
        materia.adicionarGrupo(grupo);
        return grupo;
    }

    public void adicionarAlunoAoGrupo(Usuario usuarioLogado, Materia materia, GrupoEstudo grupo, Estudante estudante) {
        exigirProfessorDaMateriaOuDev(usuarioLogado, materia, "adicionar aluno ao grupo");
        validarGrupoEEstudante(materia, grupo, estudante);
        grupo.adicionarEstudante(estudante);
    }

    public void removerAlunoDoGrupo(Usuario usuarioLogado, Materia materia, GrupoEstudo grupo, Estudante estudante) {
        exigirProfessorDaMateriaOuDev(usuarioLogado, materia, "remover aluno do grupo");
        validarGrupoEEstudante(materia, grupo, estudante);
        grupo.removerEstudante(estudante);
    }

    public void adicionarMonitorAoGrupo(Usuario usuarioLogado, Materia materia, GrupoEstudo grupo, Monitor monitor) {
        exigirProfessorDaMateriaOuDev(usuarioLogado, materia, "adicionar monitor ao grupo");
        validarGrupoEMonitor(materia, grupo, monitor);
        grupo.adicionarMonitor(monitor);
    }

    public void removerMonitorDoGrupo(Usuario usuarioLogado, Materia materia, GrupoEstudo grupo, Monitor monitor) {
        exigirProfessorDaMateriaOuDev(usuarioLogado, materia, "remover monitor do grupo");
        validarGrupoEMonitor(materia, grupo, monitor);
        grupo.removerMonitor(monitor);
    }

    public void removerGrupoEstudo(Usuario usuarioLogado, Materia materia, GrupoEstudo grupo) {
        exigirProfessorDaMateriaOuDev(usuarioLogado, materia, "remover grupo de estudo");
        if (grupo == null || !materia.getGrupos().contains(grupo)) {
            throw new RuntimeException("Selecione um grupo.");
        }
        materia.removerGrupo(grupo);
    }

    public PedidoMonitoria solicitarMonitoria(Usuario usuarioLogado, Materia materia, Aula aula, Monitor monitor, String duvida, String data, String horario) {
        if (!(usuarioLogado instanceof Estudante)) {
            throw new RuntimeException("Somente aluno solicita monitoria.");
        }
        if (materia == null) {
            throw new RuntimeException("Materia invalida.");
        }
        if (aula == null) {
            throw new RuntimeException("Selecione uma aula.");
        }
        if (monitor == null) {
            throw new RuntimeException("Selecione um monitor.");
        }
        if (!materia.getAulas().contains(aula)) {
            throw new RuntimeException("A aula escolhida nao pertence a esta materia.");
        }
        if (!materia.getMonitores().contains(monitor)) {
            throw new RuntimeException("O monitor escolhido nao esta cadastrado nesta materia.");
        }

        PedidoMonitoria pedido = new PedidoMonitoria(
                (Estudante) usuarioLogado,
                aula,
                monitor,
                validador.texto(duvida, "Duvida"),
                validador.texto(data, "Data"),
                validador.texto(horario, "Horario")
        );
        pedido.setId(banco.gerarId());
        materia.adicionarPedidoMonitoria(pedido);
        return pedido;
    }

    public void aceitarMonitoria(Usuario usuarioLogado, PedidoMonitoria pedido, Monitor monitor) {
        exigirNivel(usuarioLogado, 2, "aceitar monitoria");
        if (pedido == null) {
            throw new RuntimeException("Selecione um pedido.");
        }
        if ("Aceito".equals(pedido.getStatus())) {
            throw new RuntimeException("Este pedido ja foi aceito.");
        }
        if (monitor == null && usuarioLogado instanceof Monitor) {
            monitor = (Monitor) usuarioLogado;
        }
        if (monitor == null) {
            throw new RuntimeException("Selecione um monitor.");
        }
        if (usuarioLogado instanceof Monitor && usuarioLogado != monitor) {
            throw new RuntimeException("Monitor so pode aceitar pedido para si mesmo.");
        }
        pedido.aceitar(monitor);
    }

    public void recusarMonitoria(Usuario usuarioLogado, PedidoMonitoria pedido) {
        exigirNivel(usuarioLogado, 2, "recusar monitoria");
        if (pedido == null) {
            throw new RuntimeException("Selecione um pedido.");
        }
        if (usuarioLogado instanceof Monitor && pedido.getMonitor() != null && usuarioLogado != pedido.getMonitor()) {
            throw new RuntimeException("Monitor so pode recusar pedidos direcionados a ele.");
        }
        pedido.recusar();
    }

    public void redefinirSenhaDeUsuario(Usuario usuarioLogado, Usuario usuario, String novaSenha) {
        exigirNivel(usuarioLogado, 4, "redefinir senha");
        if (usuario == null) {
            throw new RuntimeException("Selecione um usuario.");
        }
        usuario.trocarSenha(validador.senha(novaSenha));
        usuarioDao.atualizar(usuario);
    }

    public void alterarMinhaSenha(Usuario usuario, String novaSenha) {
        if (usuario == null) {
            throw new RuntimeException("Usuario nao esta logado.");
        }
        usuario.trocarSenha(validador.senha(novaSenha));
        usuarioDao.atualizar(usuario);
    }

    private void validarLoginLivre(String login) {
        if (usuarioDao.buscarPorLogin(login) != null) {
            throw new RuntimeException("Login ja cadastrado.");
        }
    }

    private void exigirNivel(Usuario usuario, int nivel, String acao) {
        if (usuario == null || usuario.getNivel() < nivel) {
            throw new RuntimeException("Sem permissao para " + acao + ".");
        }
    }

    private void exigirProfessorDaMateriaOuDev(Usuario usuario, Materia materia, String acao) {
        if (usuario == null || materia == null) {
            throw new RuntimeException("Dados invalidos.");
        }
        boolean professorDaMateria = usuario == materia.getProfessor();
        boolean dev = usuario.getNivel() == 4;
        if (!professorDaMateria && !dev) {
            throw new RuntimeException("Sem permissao para " + acao + ".");
        }
    }

    private void validarGrupoEEstudante(Materia materia, GrupoEstudo grupo, Estudante estudante) {
        if (grupo == null) {
            throw new RuntimeException("Selecione um grupo.");
        }
        if (estudante == null) {
            throw new RuntimeException("Selecione um aluno.");
        }
        if (!materia.getGrupos().contains(grupo)) {
            throw new RuntimeException("O grupo nao pertence a esta materia.");
        }
        if (!materia.getEstudantes().contains(estudante)) {
            throw new RuntimeException("O aluno nao esta matriculado nesta materia.");
        }
    }

    private void validarGrupoEMonitor(Materia materia, GrupoEstudo grupo, Monitor monitor) {
        if (grupo == null) {
            throw new RuntimeException("Selecione um grupo.");
        }
        if (monitor == null) {
            throw new RuntimeException("Selecione um monitor.");
        }
        if (!materia.getGrupos().contains(grupo)) {
            throw new RuntimeException("O grupo nao pertence a esta materia.");
        }
        if (!materia.getMonitores().contains(monitor)) {
            throw new RuntimeException("O monitor nao esta cadastrado nesta materia.");
        }
    }

    private void criarDadosIniciais() {
        if (!usuarioDao.listar().isEmpty()) {
            return;
        }

        Dev dev = new Dev("Equipe Dev", "dev", "dev@minddebug.edu.br", "1234");
        Professor professor = new Professor("Professor 1", "prof", "prof@minddebug.edu.br", "1234");
        Monitor monitor = new Monitor("Monitor 1", "monitor", "monitor@minddebug.edu.br", "1234", "Monitor");

        usuarioDao.cadastrar(dev);
        usuarioDao.cadastrar(professor);
        usuarioDao.cadastrar(monitor);

        Curso curso = new Curso("Engenharia da Computacao", "EC", "Ciencias Exatas", 3600, "Coordenador 1");
        cursoDao.cadastrar(curso);

        Periodo periodo = new Periodo(1, "Primeiro Periodo");
        periodo.setId(banco.gerarId());
        curso.adicionarPeriodo(periodo);

        Materia materia = new Materia("Programacao Orientada a Objetos", professor);
        materia.setId(banco.gerarId());
        materia.adicionarParticipante(monitor);
        periodo.adicionarMateria(materia);

        Aula aula1 = new Aula("Aula 1", "16/03/2026", "Objetos e classes");
        Aula aula2 = new Aula("Aula 2", "23/03/2026", "Heranca e polimorfismo");
        Aula aula3 = new Aula("Aula 3", "30/03/2026", "Colecoes e excecoes");
        aula1.setId(banco.gerarId());
        aula2.setId(banco.gerarId());
        aula3.setId(banco.gerarId());
        materia.adicionarAula(aula1);
        materia.adicionarAula(aula2);
        materia.adicionarAula(aula3);

        Estudante aluno = new Estudante("Aluno 1", "aluno@aluno.edu.br", "2026001", "1234", curso);
        aluno.setId(banco.gerarId());
        usuarioDao.cadastrar(aluno);
        materia.adicionarParticipante(aluno);

        aula1.registrarFeedback(new Feedback(aluno, 8, "Entendi bem"));
        aula2.registrarFeedback(new Feedback(aluno, 5, "Preciso revisar"));

        Material material1 = new Material("Material1.pdf", "Arquivo", "Material1.pdf", professor);
        Material material2 = new Material("Video de apoio", "Link", "https://www.youtube.com/watch?v=dXZRgW-X2ls", professor);
        material1.setId(banco.gerarId());
        material2.setId(banco.gerarId());
        materia.adicionarMaterial(material1);
        materia.adicionarMaterial(material2);

        Mensagem mensagemGeral = materia.getChatGeral().enviar(professor, "Bem-vindos ao MindDebug.");
        Mensagem mensagemEquipe = materia.getChatEquipe().enviar(professor, "Monitores, acompanhem os feedbacks.");
        mensagemGeral.setId(banco.gerarId());
        mensagemEquipe.setId(banco.gerarId());
    }
}
