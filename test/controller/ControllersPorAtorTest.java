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
import excecao.AcessoNegadoException;
import excecao.DadoInvalidoException;
import excecao.RegraDeNegocioException;
import model.Aula;
import model.Chat;
import model.Curso;
import model.Materia;
import model.Material;
import model.Mensagem;
import model.Monitor;
import model.Periodo;
import model.Professor;
import model.Quiz;
import model.Usuario;

/**
 * Confere as permissoes e regras de cada controller por ator, sem usar o banco:
 * todas as recusas testadas acontecem antes de qualquer chamada ao DAO.
 * Os ids montados em memoria sao afastados dos ids do banco real: se uma regra deixar de recusar
 * e a acao chegar ao DAO, nenhuma linha de verdade e alterada.
 */
public class ControllersPorAtorTest {
    private static BancoDados banco;
    private static Usuario admin;
    private static Professor professor;
    private static Monitor monitor;
    private static Usuario aluno;
    private static Materia materia;
    private static Aula aula;

    public static void main(String[] args) {
        banco = new BancoDados();
        new DadosIniciais().montar(banco);
        afastarIdsDoBanco(banco);
        admin = banco.buscarUsuarioPorLogin("admin");
        professor = new UsuarioController(banco).listarProfessores().get(0);
        monitor = new UsuarioController(banco).listarMonitores().get(0);
        aluno = banco.buscarUsuarioPorLogin("2026001");
        materia = banco.getCursos().get(0).getPeriodos().get(0).getMaterias().get(0);
        aula = materia.getAulas().get(0);

        testarAutenticacao();
        testarUsuarios();
        testarEstruturaAcademica();
        testarModuloDoProfessor();
        testarModuloDoAluno();
        testarModuloDoMonitor();
        testarCompartilhados();
        testarRegrasNovas();
    }

    private static void testarAutenticacao() {
        AutenticacaoController autenticacao = new AutenticacaoController(banco);
        verificar(autenticacao.fazerLogin("prof", "1234") == professor, "login com senha certa");
        boolean negou = false;
        try {
            autenticacao.fazerLogin("prof", "errada");
        } catch (AcessoNegadoException esperado) {
            negou = true;
        }
        verificar(negou, "login com senha errada e recusado");
    }

    private static void testarUsuarios() {
        UsuarioController usuarios = new UsuarioController(banco);
        verificar(usuarios.listarProfessores().size() == 1 && usuarios.listarMonitores().size() == 1,
                "listas por perfil");
        verificar(negouCadastroDeProfessor(usuarios, professor), "professor nao cadastra professor");
        verificar(negouCadastroDeProfessor(usuarios, aluno), "aluno nao cadastra professor");
        verificar(negouCadastroDeMonitor(usuarios, monitor), "monitor nao cadastra monitor");
        verificar(negouCadastroDeMonitor(usuarios, aluno), "aluno nao cadastra monitor");

        boolean negou = false;
        try {
            usuarios.removerUsuario(professor, aluno);
        } catch (AcessoNegadoException esperado) {
            negou = true;
        }
        verificar(negou, "professor nao remove usuario");

        boolean recusou = false;
        try {
            usuarios.cadastrarEstudante("Outro", "outro@aluno.edu.br", "2026001", "1234", null);
        } catch (RegraDeNegocioException esperado) {
            recusou = true;
        }
        verificar(recusou, "matricula repetida e recusada");

        recusou = false;
        try {
            usuarios.cadastrarProfessor(admin, "Outro", "outroprof", "PROF@minddebug.edu.br", "1234");
        } catch (RegraDeNegocioException esperado) {
            recusou = true;
        }
        verificar(recusou, "email repetido e recusado, sem diferenciar maiusculas");
    }

    private static void testarEstruturaAcademica() {
        EstruturaAcademicaController estrutura = new EstruturaAcademicaController(banco);
        Curso curso = estrutura.listarCursos().get(0);
        Periodo periodo = curso.getPeriodos().get(0);

        verificar(negouCriarCurso(estrutura, professor), "professor nao cria curso");
        verificar(negouCriarCurso(estrutura, aluno), "aluno nao cria curso");
        verificar(negouCriarCurso(estrutura, monitor), "monitor nao cria curso");

        boolean negou = false;
        try {
            estrutura.removerMateria(professor, periodo, materia);
        } catch (AcessoNegadoException esperado) {
            negou = true;
        }
        verificar(negou, "professor nao remove disciplina");

        boolean recusou = false;
        try {
            estrutura.criarCurso(admin, "Repetido", "ec", "Area", 100, "Coordenador");
        } catch (RegraDeNegocioException esperado) {
            recusou = true;
        }
        verificar(recusou, "sigla repetida e recusada");

        recusou = false;
        try {
            estrutura.criarPeriodo(admin, curso, 1, "Repetido");
        } catch (RegraDeNegocioException esperado) {
            recusou = true;
        }
        verificar(recusou, "periodo repetido e recusado");

        recusou = false;
        try {
            estrutura.criarMateria(admin, periodo, "algoritmos e programacao", null);
        } catch (DadoInvalidoException esperado) {
            recusou = true;
        }
        verificar(recusou, "disciplina sem professor e recusada");
    }

    private static void testarModuloDoProfessor() {
        AulaController aulas = new AulaController(banco);
        GrupoEstudoController grupos = new GrupoEstudoController(banco);
        Professor outroProfessor = new Professor("Outro Professor", "outro", "outro@minddebug.edu.br", "1234");

        verificar(negouCriarAula(aulas, outroProfessor), "professor de outra disciplina nao cria aula");
        verificar(negouCriarAula(aulas, aluno), "aluno nao cria aula");
        verificar(negouCriarAula(aulas, monitor), "monitor nao cria aula");

        boolean negou = false;
        try {
            grupos.criarGrupoEstudo(monitor, materia, "Grupo", aula, null);
        } catch (AcessoNegadoException esperado) {
            negou = true;
        }
        verificar(negou, "monitor nao cria grupo de estudo");

        verificar(administradorNaoMexeNaMateria(aulas, grupos), "RN01J: administrador nao cria nem remove aula, grupo e quiz");
        verificar(administradorCuidaDosMonitoresDaMateria(), "administrador continua cuidando dos monitores da materia");

        boolean recusou = false;
        try {
            aulas.removerAula(professor, materia, new Aula("Fora", "01/04/2026", ""));
        } catch (RegraDeNegocioException esperado) {
            recusou = true;
        }
        verificar(recusou, "aula de fora da disciplina nao e removida");
    }

    private static void testarModuloDoAluno() {
        FeedbackController feedbacks = new FeedbackController(banco);
        boolean negou = false;
        try {
            feedbacks.registrarFeedback(professor, aula, 8, "Nao pode");
        } catch (AcessoNegadoException esperado) {
            negou = true;
        }
        verificar(negou, "professor nao registra feedback");

        int participantes = materia.getEstudantes().size();
        new MatriculaController(banco).entrarNaMateria(aluno, materia);
        verificar(materia.getEstudantes().size() == participantes, "aluno que ja participa nao entra de novo");
    }

    private static void testarModuloDoMonitor() {
        SolicitacaoController solicitacoes = new SolicitacaoController(banco);
        boolean negou = false;
        try {
            solicitacoes.solicitarMonitoria(professor, materia, aula, monitor, "Duvida", "02/04/2026", "14:00");
        } catch (AcessoNegadoException esperado) {
            negou = true;
        }
        verificar(negou, "professor nao solicita monitoria");

        negou = false;
        try {
            solicitacoes.aceitarMonitoria(aluno, null, null);
        } catch (AcessoNegadoException esperado) {
            negou = true;
        }
        verificar(negou, "aluno nao aceita monitoria");
    }

    private static void testarCompartilhados() {
        QuizController quizzes = new QuizController(banco);
        boolean negou = false;
        try {
            quizzes.criarQuiz(aluno, materia, aula, "Quiz", "");
        } catch (AcessoNegadoException esperado) {
            negou = true;
        }
        verificar(negou, "aluno nao cria quiz");

        ChatController chats = new ChatController(banco);
        Mensagem doProfessor = materia.getChatGeral().getMensagens().get(0);
        negou = false;
        try {
            chats.apagarMensagem(materia.getChatGeral(), aluno, doProfessor);
        } catch (AcessoNegadoException esperado) {
            negou = true;
        }
        verificar(negou && materia.getChatGeral().getMensagens().contains(doProfessor),
                "aluno nao apaga mensagem do professor");
    }

    /** Regras do passo 13 que o controller confere antes de chegar ao banco. */
    private static void testarRegrasNovas() {
        UsuarioController usuarios = new UsuarioController(banco);
        EstruturaAcademicaController estrutura = new EstruturaAcademicaController(banco);
        Curso curso = estrutura.listarCursos().get(0);

        boolean recusou = false;
        try {
            usuarios.removerUsuario(admin, admin);
        } catch (RegraDeNegocioException esperado) {
            recusou = "Voce nao pode remover nem desativar a propria conta.".equals(esperado.getMessage());
        }
        verificar(recusou && banco.getUsuarios().contains(admin), "administrador nao remove a propria conta");

        recusou = false;
        try {
            usuarios.desativarUsuario(admin, admin);
        } catch (RegraDeNegocioException esperado) {
            recusou = "Voce nao pode remover nem desativar a propria conta.".equals(esperado.getMessage());
        }
        verificar(recusou && admin.isAtivo(), "administrador nao desativa a propria conta");

        boolean negou = false;
        try {
            usuarios.desativarUsuario(professor, aluno);
        } catch (AcessoNegadoException esperado) {
            negou = true;
        }
        verificar(negou && aluno.isAtivo(), "professor nao desativa usuario");

        recusou = false;
        try {
            estrutura.removerCurso(admin, curso);
        } catch (RegraDeNegocioException esperado) {
            recusou = true;
        }
        verificar(recusou && banco.getCursos().contains(curso), "curso com materias nao e excluido");

        recusou = false;
        try {
            estrutura.removerPeriodo(admin, curso, curso.getPeriodos().get(0));
        } catch (RegraDeNegocioException esperado) {
            recusou = true;
        }
        verificar(recusou, "periodo com materias nao e excluido");

        negou = false;
        try {
            estrutura.removerCurso(professor, curso);
        } catch (AcessoNegadoException esperado) {
            negou = true;
        }
        verificar(negou, "professor nao remove curso");

        recusou = false;
        try {
            new AutenticacaoController(banco).alterarMinhaSenha(aluno, "1234");
        } catch (RegraDeNegocioException esperado) {
            recusou = "A nova senha deve ser diferente da atual.".equals(esperado.getMessage());
        }
        verificar(recusou, "nova senha igual a atual e recusada");

        verificar(administradorNaoMexeNoConteudoPedagogico(), "RN01J: administrador nao altera conteudo de professor e aluno");
        verificar(usuarioDesativadoNaoEntra(), "usuario desativado nao entra no sistema");
    }

    /** RN01J: o administrador nao registra nem troca feedback e nao apaga material ou mensagem de outros. */
    private static boolean administradorNaoMexeNoConteudoPedagogico() {
        boolean negouFeedback = false;
        try {
            new FeedbackController(banco).registrarFeedback(admin, aula, 2, "Troca de nota");
        } catch (AcessoNegadoException esperado) {
            negouFeedback = true;
        }
        boolean feedbackIgual = aula.getFeedbacks().size() == 1 && aula.getFeedbacks().get(0).getNota() == 8;

        Material material = materia.getMateriais().get(0);
        boolean negouMaterial = false;
        try {
            new MaterialController(banco).removerMaterial(materia, admin, material);
        } catch (AcessoNegadoException esperado) {
            negouMaterial = true;
        }

        Mensagem mensagem = materia.getChatGeral().getMensagens().get(0);
        boolean negouMensagem = false;
        try {
            new ChatController(banco).apagarMensagem(materia.getChatGeral(), admin, mensagem);
        } catch (AcessoNegadoException esperado) {
            negouMensagem = true;
        }
        return negouFeedback && feedbackIgual && negouMaterial && materia.getMateriais().contains(material)
                && negouMensagem && materia.getChatGeral().getMensagens().contains(mensagem);
    }

    /** RN01J: aulas, quiz e grupos de estudo sao do professor da materia; o administrador e recusado antes do banco. */
    private static boolean administradorNaoMexeNaMateria(AulaController aulas, GrupoEstudoController grupos) {
        int aulasAntes = materia.getAulas().size();
        int gruposAntes = materia.getGrupos().size();
        Quiz quizAntes = aula.getQuiz();
        boolean negouCriarAula = negouCriarAula(aulas, admin);

        boolean negouRemoverAula = false;
        try {
            aulas.removerAula(admin, materia, aula);
        } catch (AcessoNegadoException esperado) {
            negouRemoverAula = true;
        }

        boolean negouGrupo = false;
        try {
            grupos.criarGrupoEstudo(admin, materia, "Grupo do administrador", aula, null);
        } catch (AcessoNegadoException esperado) {
            negouGrupo = true;
        }

        boolean negouQuiz = false;
        try {
            new QuizController(banco).criarQuiz(admin, materia, aula, "Quiz do administrador", "");
        } catch (AcessoNegadoException esperado) {
            negouQuiz = true;
        }
        return negouCriarAula && negouRemoverAula && negouGrupo && negouQuiz
                && materia.getAulas().size() == aulasAntes && materia.getGrupos().size() == gruposAntes
                && aula.getQuiz() == quizAntes;
    }

    /**
     * Enturmacao (RF05J): o administrador pode vincular monitores a materia. O monitor ja participa,
     * entao a acao passa pela permissao e termina sem chegar ao banco; o aluno e recusado.
     */
    private static boolean administradorCuidaDosMonitoresDaMateria() {
        MonitoresDaMateriaController monitores = new MonitoresDaMateriaController(banco);
        if (!materia.getMonitores().contains(monitor)) {
            return false;
        }
        monitores.adicionarMonitorNaMateria(admin, materia, monitor);
        boolean negouAluno = false;
        try {
            monitores.adicionarMonitorNaMateria(aluno, materia, monitor);
        } catch (AcessoNegadoException esperado) {
            negouAluno = true;
        }
        return negouAluno && materia.getMonitores().contains(monitor);
    }

    private static boolean usuarioDesativadoNaoEntra() {
        BancoDados outroBanco = new BancoDados();
        new DadosIniciais().montar(outroBanco);
        afastarIdsDoBanco(outroBanco);
        outroBanco.buscarUsuarioPorLogin("2026001").setAtivo(false);
        try {
            new AutenticacaoController(outroBanco).fazerLogin("2026001", "1234");
            return false;
        } catch (AcessoNegadoException esperado) {
            return true;
        }
    }

    private static void afastarIdsDoBanco(BancoDados dados) {
        int distancia = 900000;
        for (Usuario usuario : dados.getUsuarios()) {
            usuario.setId(usuario.getId() + distancia);
        }
        for (Curso curso : dados.getCursos()) {
            curso.setId(curso.getId() + distancia);
            for (Periodo periodo : curso.getPeriodos()) {
                periodo.setId(periodo.getId() + distancia);
                for (Materia materiaDoPeriodo : periodo.getMaterias()) {
                    materiaDoPeriodo.setId(materiaDoPeriodo.getId() + distancia);
                    for (Aula aulaDaMateria : materiaDoPeriodo.getAulas()) {
                        aulaDaMateria.setId(aulaDaMateria.getId() + distancia);
                    }
                    for (Material materialDaMateria : materiaDoPeriodo.getMateriais()) {
                        materialDaMateria.setId(materialDaMateria.getId() + distancia);
                    }
                    afastarIdsDasMensagens(materiaDoPeriodo.getChatGeral(), distancia);
                    afastarIdsDasMensagens(materiaDoPeriodo.getChatEquipe(), distancia);
                }
            }
        }
    }

    private static void afastarIdsDasMensagens(Chat chat, int distancia) {
        for (Mensagem mensagem : chat.getMensagens()) {
            mensagem.setId(mensagem.getId() + distancia);
        }
    }

    private static boolean negouCadastroDeProfessor(UsuarioController usuarios, Usuario usuarioLogado) {
        try {
            usuarios.cadastrarProfessor(usuarioLogado, "Novo", "novoprof", "novo@minddebug.edu.br", "1234");
            return false;
        } catch (AcessoNegadoException esperado) {
            return true;
        }
    }

    private static boolean negouCadastroDeMonitor(UsuarioController usuarios, Usuario usuarioLogado) {
        try {
            usuarios.cadastrarMonitor(usuarioLogado, "Novo", "novomon", "novomon@minddebug.edu.br", "1234", "Bolsista");
            return false;
        } catch (AcessoNegadoException esperado) {
            return true;
        }
    }

    private static boolean negouCriarCurso(EstruturaAcademicaController estrutura, Usuario usuarioLogado) {
        try {
            estrutura.criarCurso(usuarioLogado, "Novo", "NOVO", "Area", 100, "Coordenador");
            return false;
        } catch (AcessoNegadoException esperado) {
            return true;
        }
    }

    private static boolean negouCriarAula(AulaController aulas, Usuario usuarioLogado) {
        try {
            aulas.criarAula(usuarioLogado, materia, "Nova aula", "01/04/2026");
            return false;
        } catch (AcessoNegadoException esperado) {
            return true;
        }
    }

    private static void verificar(boolean condicao, String descricao) {
        if (!condicao) {
            throw new AssertionError("FALHOU: " + descricao);
        }
    }
}
