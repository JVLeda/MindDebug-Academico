package model;

import excecao.AcessoNegadoException;

import java.util.ArrayList;

public class PermissoesPorPerfilTest {
    public static void main(String[] args) {
        testarPermissoesGerais();
        testarGestaoDaMateria();
        testarAcoesDoEstudante();
        testarAcoesNegadasAosOutrosPerfis();
        testarAtendimentoDeMonitoria();
        testarApagarConteudo();
        testarListasPorPerfil();
        testarCursosVisiveis();
        testarDadosQueSoExistemEmAlgunsPerfis();
    }

    private static void testarPermissoesGerais() {
        Administrador administrador = novoAdministrador();
        Professor professor = novoProfessor("prof");
        Monitor monitor = novoMonitor("monitor");
        Estudante estudante = novoEstudante("2026001", null);

        verificar(administrador.podeGerenciarEstrutura(), "Administrador gerencia a estrutura academica.");
        verificar(!professor.podeGerenciarEstrutura(), "Professor nao gerencia a estrutura academica.");
        verificar(!monitor.podeGerenciarEstrutura(), "Monitor nao gerencia a estrutura academica.");
        verificar(!estudante.podeGerenciarEstrutura(), "Aluno nao gerencia a estrutura academica.");

        verificar(administrador.podeCadastrarMonitor(), "Administrador cadastra monitor.");
        verificar(professor.podeCadastrarMonitor(), "Professor cadastra monitor.");
        verificar(!monitor.podeCadastrarMonitor(), "Monitor nao cadastra monitor.");
        verificar(!estudante.podeCadastrarMonitor(), "Aluno nao cadastra monitor.");

        verificar(administrador.podeAtenderMonitoria(), "Administrador responde pedido de monitoria.");
        verificar(professor.podeAtenderMonitoria(), "Professor responde pedido de monitoria.");
        verificar(monitor.podeAtenderMonitoria(), "Monitor responde pedido de monitoria.");
        verificar(!estudante.podeAtenderMonitoria(), "Aluno nao responde pedido de monitoria.");

        verificar(estudante.podeRegistrarFeedback(), "Aluno registra feedback.");
        verificar(estudante.podeSolicitarMonitoria(), "Aluno solicita monitoria.");
        verificar(estudante.podeResponderQuiz(), "Aluno responde quiz.");
        verificar(!professor.podeRegistrarFeedback(), "Professor nao registra feedback.");
        verificar(!monitor.podeSolicitarMonitoria(), "Monitor nao solicita monitoria.");
        verificar(!administrador.podeResponderQuiz(), "Administrador nao responde quiz.");
    }

    private static void testarGestaoDaMateria() {
        Administrador administrador = novoAdministrador();
        Professor professorDaMateria = novoProfessor("prof1");
        Professor outroProfessor = novoProfessor("prof2");
        Monitor monitor = novoMonitor("monitor");
        Estudante estudante = novoEstudante("2026001", null);
        Materia materia = new Materia("Programacao Orientada a Objetos", professorDaMateria);

        verificar(professorDaMateria.podeGerenciarMateria(materia), "Professor gerencia a propria materia.");
        verificar(!outroProfessor.podeGerenciarMateria(materia), "Professor nao gerencia materia de outro professor.");
        verificar(!administrador.podeGerenciarMateria(materia), "Administrador nao gerencia aulas, quiz e grupos da materia.");
        verificar(!monitor.podeGerenciarMateria(materia), "Monitor nao gerencia materia.");
        verificar(!estudante.podeGerenciarMateria(materia), "Aluno nao gerencia materia.");
        verificar(!professorDaMateria.podeGerenciarMateria(null), "Sem materia nao ha o que gerenciar.");

        materia.removerProfessor();
        verificar(!professorDaMateria.podeGerenciarMateria(materia),
                "Professor nao gerencia materia que ficou sem professor designado.");
        verificar(!outroProfessor.podeGerenciarMateria(materia),
                "Nenhum professor gerencia materia sem professor designado.");
        verificar(!administrador.podeGerenciarMateria(materia),
                "Administrador tambem nao gerencia materia sem professor; ele designa um professor novo.");

        Professor professorSalvo = novoProfessor("prof3");
        professorSalvo.setId(7);
        Professor mesmoProfessorRecarregado = novoProfessor("prof3");
        mesmoProfessorRecarregado.setId(7);
        Materia materiaSalva = new Materia("Banco de Dados", professorSalvo);
        verificar(mesmoProfessorRecarregado.podeGerenciarMateria(materiaSalva),
                "O mesmo professor, recarregado do banco com o mesmo id, gerencia a materia.");
    }

    private static void testarAcoesDoEstudante() {
        Estudante estudante = novoEstudante("2026001", null);
        Monitor monitor = novoMonitor("monitor");
        Aula aula = new Aula("Aula 1", "01/01/2026", "Objetos e classes");

        estudante.registrarFeedback(aula, 8, "Entendi");
        verificar(aula.getFeedbacks().size() == 1, "O feedback do aluno deve entrar na aula.");
        verificar(aula.getFeedbacks().get(0).getEstudante() == estudante, "O feedback deve guardar o aluno que registrou.");
        verificar(aula.getFeedbacks().get(0).getNota() == 8, "O feedback deve guardar a nota informada.");

        estudante.registrarFeedback(aula, 5, "Preciso revisar");
        verificar(aula.getFeedbacks().size() == 1, "O mesmo aluno nao gera dois feedbacks na mesma aula.");
        verificar(aula.getFeedbacks().get(0).getNota() == 5, "O segundo registro atualiza a nota.");

        PedidoMonitoria pedido = estudante.criarPedidoMonitoria(aula, monitor, "Duvida", "02/01/2026", "14:00");
        verificar(pedido.getEstudante() == estudante, "O pedido deve guardar o aluno que solicitou.");
        verificar(pedido.getMonitor() == monitor, "O pedido deve guardar o monitor escolhido.");
        verificar("Solicitado".equals(pedido.getStatus()), "O pedido nasce com o status Solicitado.");

        Quiz quiz = new Quiz("Revisao", "Conceitos");
        TentativaQuiz tentativa = estudante.iniciarTentativaQuiz(quiz);
        verificar(tentativa.getEstudante() == estudante, "A tentativa deve guardar o aluno.");
        verificar(tentativa.getQuiz() == quiz, "A tentativa deve guardar o quiz.");
    }

    private static void testarAcoesNegadasAosOutrosPerfis() {
        Usuario[] semPermissao = {novoAdministrador(), novoProfessor("prof"), novoMonitor("monitor")};
        Aula aula = new Aula("Aula 1", "01/01/2026", "Objetos e classes");
        Quiz quiz = new Quiz("Revisao", "Conceitos");

        for (Usuario usuario : semPermissao) {
            verificar(feedbackFoiNegado(usuario, aula), usuario.getCargo() + " nao registra feedback.");
            verificar(pedidoFoiNegado(usuario, aula), usuario.getCargo() + " nao solicita monitoria.");
            verificar(tentativaFoiNegada(usuario, quiz), usuario.getCargo() + " nao responde quiz.");
        }
        verificar(aula.getFeedbacks().isEmpty(), "Nenhum feedback negado pode ficar gravado na aula.");
    }

    private static boolean feedbackFoiNegado(Usuario usuario, Aula aula) {
        try {
            usuario.registrarFeedback(aula, 8, "Entendi");
            return false;
        } catch (AcessoNegadoException esperado) {
            return true;
        }
    }

    private static boolean pedidoFoiNegado(Usuario usuario, Aula aula) {
        try {
            usuario.criarPedidoMonitoria(aula, null, "Duvida", "02/01/2026", "14:00");
            return false;
        } catch (AcessoNegadoException esperado) {
            return true;
        }
    }

    private static boolean tentativaFoiNegada(Usuario usuario, Quiz quiz) {
        try {
            usuario.iniciarTentativaQuiz(quiz);
            return false;
        } catch (AcessoNegadoException esperado) {
            return true;
        }
    }

    private static void testarAtendimentoDeMonitoria() {
        Professor professor = novoProfessor("prof");
        Monitor monitorA = novoMonitor("monitorA");
        Monitor monitorB = novoMonitor("monitorB");
        Estudante estudante = novoEstudante("2026001", null);
        Aula aula = new Aula("Aula 1", "01/01/2026", "Objetos e classes");

        verificar(professor.escolherMonitorDoAtendimento(monitorA) == monitorA,
                "Professor escolhe qual monitor atende o pedido.");
        verificar(professor.escolherMonitorDoAtendimento(null) == null,
                "Professor que nao escolhe monitor nao recebe um monitor por conta propria.");
        verificar(monitorA.escolherMonitorDoAtendimento(null) == monitorA,
                "Monitor que aceita sem escolher assume o pedido.");
        verificar(monitorA.escolherMonitorDoAtendimento(monitorA) == monitorA,
                "Monitor pode aceitar o pedido para si mesmo.");

        boolean bloqueou = false;
        try {
            monitorA.escolherMonitorDoAtendimento(monitorB);
        } catch (AcessoNegadoException esperado) {
            bloqueou = true;
        }
        verificar(bloqueou, "Monitor nao aceita pedido em nome de outro monitor.");

        PedidoMonitoria pedidoParaA = new PedidoMonitoria(estudante, aula, monitorA, "Duvida");
        verificar(monitorA.podeRecusarPedido(pedidoParaA), "Monitor recusa pedido direcionado a ele.");
        verificar(!monitorB.podeRecusarPedido(pedidoParaA), "Monitor nao recusa pedido direcionado a outro monitor.");
        verificar(professor.podeRecusarPedido(pedidoParaA), "Professor recusa qualquer pedido.");
        verificar(!estudante.podeRecusarPedido(pedidoParaA), "Aluno nao recusa pedido de monitoria.");

        PedidoMonitoria pedidoSemMonitor = new PedidoMonitoria(estudante, aula, null, "Duvida");
        verificar(monitorB.podeRecusarPedido(pedidoSemMonitor), "Monitor recusa pedido que ainda nao tem monitor.");
    }

    private static void testarApagarConteudo() {
        Administrador administrador = novoAdministrador();
        Professor professor = novoProfessor("prof");
        Monitor monitor = novoMonitor("monitor");
        Estudante estudante = novoEstudante("2026001", null);
        Estudante outroEstudante = novoEstudante("2026002", null);

        verificar(estudante.podeApagarConteudoDe(estudante), "Aluno apaga o proprio conteudo.");
        verificar(!estudante.podeApagarConteudoDe(outroEstudante), "Aluno nao apaga conteudo de outro aluno.");
        verificar(monitor.podeApagarConteudoDe(estudante), "Monitor modera o conteudo dos alunos.");
        verificar(professor.podeApagarConteudoDe(estudante), "Professor modera o conteudo dos alunos.");
        verificar(administrador.podeApagarConteudoDe(administrador), "Administrador apaga o proprio conteudo.");
        verificar(!administrador.podeApagarConteudoDe(estudante),
                "Administrador nao apaga conteudo pedagogico de outro usuario.");
    }

    private static void testarListasPorPerfil() {
        Administrador administrador = novoAdministrador();
        Professor professor = novoProfessor("prof");
        Monitor monitor = novoMonitor("monitor");
        Estudante estudante1 = novoEstudante("2026001", null);
        Estudante estudante2 = novoEstudante("2026002", null);

        Materia materia = new Materia("Programacao Orientada a Objetos", professor);
        materia.adicionarParticipante(estudante1);
        materia.adicionarParticipante(monitor);
        materia.adicionarParticipante(estudante2);

        ArrayList<Estudante> estudantes = materia.getEstudantes();
        verificar(estudantes.size() == 2, "A materia deve listar somente os dois alunos.");
        verificar(estudantes.contains(estudante1) && estudantes.contains(estudante2),
                "Os dois alunos matriculados devem aparecer na lista.");

        ArrayList<Monitor> monitores = materia.getMonitores();
        verificar(monitores.size() == 1 && monitores.get(0) == monitor,
                "A materia deve listar somente o monitor.");

        Usuario[] todos = {administrador, professor, monitor, estudante1, estudante2};
        ArrayList<Professor> professores = new ArrayList<>();
        for (Usuario usuario : todos) {
            usuario.incluirEmProfessores(professores);
        }
        verificar(professores.size() == 1 && professores.get(0) == professor,
                "Somente o professor entra na lista de professores.");
    }

    private static void testarCursosVisiveis() {
        Curso engenharia = new Curso("Engenharia da Computacao", "EC", "Exatas", 3600, "Coordenacao");
        Curso sistemas = new Curso("Sistemas de Informacao", "SI", "Exatas", 3000, "Coordenacao");
        ArrayList<Curso> todos = new ArrayList<>();
        todos.add(engenharia);
        todos.add(sistemas);

        ArrayList<Curso> doAluno = novoEstudante("2026001", sistemas).filtrarCursosVisiveis(todos);
        verificar(doAluno.size() == 1 && doAluno.get(0) == sistemas, "Aluno enxerga somente o proprio curso.");

        ArrayList<Curso> doAlunoSemCurso = novoEstudante("2026002", null).filtrarCursosVisiveis(todos);
        verificar(doAlunoSemCurso.isEmpty(), "Aluno sem curso nao enxerga curso nenhum.");

        verificar(novoProfessor("prof").filtrarCursosVisiveis(todos).size() == 2, "Professor enxerga todos os cursos.");
        verificar(novoAdministrador().filtrarCursosVisiveis(todos).size() == 2, "Administrador enxerga todos os cursos.");
        verificar(todos.size() == 2, "O filtro do aluno nao pode alterar a lista original.");
    }

    private static void testarDadosQueSoExistemEmAlgunsPerfis() {
        Curso curso = new Curso("Engenharia da Computacao", "EC", "Exatas", 3600, "Coordenacao");
        Estudante estudante = novoEstudante("2026001", curso);
        Monitor monitor = novoMonitor("monitor");
        Professor professor = novoProfessor("prof");

        verificar("2026001".equals(estudante.getMatricula()), "Aluno informa a propria matricula.");
        verificar(estudante.getCurso() == curso, "Aluno informa o proprio curso.");
        verificar("Monitor".equals(monitor.getTipoMonitor()), "Monitor informa o proprio tipo.");
        verificar(professor.getMatricula() == null, "Professor nao tem matricula de aluno.");
        verificar(professor.getCurso() == null, "Professor nao pertence a um curso.");
        verificar(professor.getTipoMonitor() == null, "Professor nao tem tipo de monitor.");
        verificar(estudante.getTipoMonitor() == null, "Aluno nao tem tipo de monitor.");
    }

    private static Administrador novoAdministrador() {
        return new Administrador("Administrador", "admin", "admin@teste.edu", "1234");
    }

    private static Professor novoProfessor(String login) {
        return new Professor("Professor " + login, login, login + "@teste.edu", "1234");
    }

    private static Monitor novoMonitor(String login) {
        return new Monitor("Monitor " + login, login, login + "@teste.edu", "1234", "Monitor");
    }

    private static Estudante novoEstudante(String matricula, Curso curso) {
        return new Estudante("Aluno " + matricula, matricula + "@teste.edu", matricula, "1234", curso);
    }

    private static void verificar(boolean condicao, String mensagem) {
        if (!condicao) {
            throw new AssertionError(mensagem);
        }
    }
}
