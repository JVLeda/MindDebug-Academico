package dao;

import controller.MindDebugController;
import excecao.RegraDeNegocioException;
import model.Aula;
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
 * Teste de integracao da persistencia por operacao: faz as acoes pelo controller, como as telas,
 * carrega o banco de novo e confere se tudo foi gravado. Precisa do MySQL com os dados iniciais.
 * Usa o curso de sigla TESTE11 e usuarios com login TESTE11_, apagados no fim.
 */
public class PersistenciaIntegracaoTest {
    private static final String SIGLA = "TESTE11";
    private static final String[] LOGINS = {"TESTE11_aluno", "TESTE11_monitor"};

    public static void main(String[] args) {
        limpar();
        try {
            BancoDados banco = new BancoDao().carregar();
            MindDebugController controller = new MindDebugController(banco);
            Usuario admin = banco.buscarUsuarioPorLogin("admin");
            Professor professor = controller.listarProfessores().get(0);

            Curso curso = controller.criarCurso(admin, "Curso de Persistencia", SIGLA, "Teste", 3000, "Coordenacao");
            Periodo periodo = controller.criarPeriodo(admin, curso, 1, "Periodo de teste");
            Materia materia = controller.criarMateria(admin, periodo, "Materia Principal", professor);
            Materia semProfessor = controller.criarMateria(admin, periodo, "Materia Sem Professor", professor);
            Materia removida = controller.criarMateria(admin, periodo, "Materia Removida", professor);
            controller.removerProfessorDaMateria(admin, semProfessor);
            controller.removerMateria(admin, periodo, removida);

            Estudante aluno = controller.cadastrarEstudante("Aluno de Teste", "teste11_aluno@teste.local",
                    "TESTE11_aluno", "1234", curso);
            Monitor monitor = controller.cadastrarMonitor(professor, "Monitor de Teste", "TESTE11_monitor",
                    "teste11_monitor@teste.local", "1234", "Bolsista");
            controller.adicionarMonitorNaMateria(professor, materia, monitor);
            controller.entrarNaMateria(aluno, materia);
            controller.alterarMinhaSenha(aluno, "4321");

            Aula aula = controller.criarAula(professor, materia, "Aula Gravada", "10/03/2026");
            Aula aulaRemovida = controller.criarAula(professor, materia, "Aula Removida", "11/03/2026");
            controller.removerAula(professor, materia, aulaRemovida);
            controller.registrarFeedback(aluno, aula, 7, "Bom");
            controller.registrarFeedback(aluno, aula, 9, "Revisei e melhorou");

            controller.adicionarMaterial(materia, professor, "Slides", "Link", "https://exemplo.local/slides");
            controller.enviarMensagem(materia.getChatGeral(), aluno, "Mensagem no chat geral");
            controller.enviarMensagem(aula.getChat(), aluno, "Mensagem no chat da aula");
            controller.enviarMensagem(materia.getChatEquipe(), professor, "Mensagem que sera apagada");
            Mensagem apagada = materia.getChatEquipe().getMensagens().get(0);
            controller.apagarMensagem(materia.getChatEquipe(), professor, apagada);

            GrupoEstudo grupo = controller.criarGrupoEstudo(professor, materia, "Grupo Gravado", aula, monitor);
            controller.adicionarAlunoAoGrupo(professor, materia, grupo, aluno);
            PedidoMonitoria pedido = controller.solicitarMonitoria(aluno, materia, aula, monitor, "Duvida gravada",
                    "12/03/2026", "15:30");
            controller.aceitarMonitoria(monitor, pedido, null);

            Quiz quiz = controller.criarQuiz(professor, materia, aula, "Quiz Gravado", "Revisao");
            String[] textos = {"A", "B", "C", "D"};
            String[] explicacoes = {"Nao", "Nao", "Sim", "Nao"};
            PerguntaQuiz pergunta = controller.adicionarPerguntaAoQuiz(professor, materia, quiz, "Pergunta gravada",
                    "Facil", textos, explicacoes, 2);
            TentativaQuiz tentativa = controller.iniciarTentativaQuiz(aluno, materia, quiz);
            controller.registrarRespostaQuiz(aluno, tentativa, pergunta, pergunta.getAlternativas().get(2));
            controller.finalizarTentativaQuiz(aluno, tentativa);

            recusasNaoMudamAMemoria(controller, admin, periodo, materia, aluno, banco);
            conferirDepoisDeRecarregar(aula, aluno, monitor, professor);
        } finally {
            limpar();
        }
        verificar(new CursoDao().buscarPorSigla(SIGLA) == null, "dados de teste apagados no fim");
    }

    /** Quando o banco recusa a operacao, os objetos em memoria continuam como estavam. */
    private static void recusasNaoMudamAMemoria(MindDebugController controller, Usuario admin, Periodo periodo,
            Materia materia, Estudante aluno, BancoDados banco) {
        boolean recusou = false;
        try {
            controller.removerMateria(admin, periodo, materia);
        } catch (RegraDeNegocioException esperado) {
            recusou = true;
        }
        verificar(recusou && periodo.getMaterias().contains(materia), "materia com aulas nao sai do banco nem da memoria");

        recusou = false;
        try {
            controller.removerUsuario(admin, aluno);
        } catch (RegraDeNegocioException esperado) {
            recusou = true;
        }
        verificar(recusou && banco.getUsuarios().contains(aluno), "aluno com registros nao sai do banco nem da memoria");
    }

    private static void conferirDepoisDeRecarregar(Aula aulaOriginal, Estudante alunoOriginal, Monitor monitorOriginal,
            Professor professor) {
        BancoDados banco = new BancoDao().carregar();
        Curso curso = null;
        for (Curso candidato : banco.getCursos()) {
            if (SIGLA.equals(candidato.getSigla())) {
                curso = candidato;
            }
        }
        verificar(curso != null && curso.getPeriodos().size() == 1, "curso e periodo gravados");
        Periodo periodo = curso.getPeriodos().get(0);
        verificar(periodo.getMaterias().size() == 2, "duas materias; a removida nao volta: " + periodo.getMaterias().size());
        Materia materia = periodo.getMaterias().get(0);
        Materia semProfessor = periodo.getMaterias().get(1);
        verificar("Materia Principal".equals(materia.getNome()) && materia.getProfessor().getId() == professor.getId(),
                "materia com o professor");
        verificar(semProfessor.getProfessor() == null, "professor removido da outra materia");

        Usuario aluno = banco.buscarUsuarioPorLogin("TESTE11_aluno");
        verificar(aluno != null && aluno.getId() == alunoOriginal.getId(), "aluno gravado com o mesmo id");
        verificar(aluno.autenticar("TESTE11_aluno", "4321"), "senha nova gravada");
        verificar(materia.getEstudantes().size() == 1 && materia.getEstudantes().get(0).getId() == aluno.getId(),
                "aluno entrou na materia");
        verificar(materia.getMonitores().size() == 1 && materia.getMonitores().get(0).getId() == monitorOriginal.getId(),
                "monitor adicionado a materia");

        verificar(materia.getAulas().size() == 1, "a aula removida nao volta");
        Aula aula = materia.getAulas().get(0);
        verificar(aula.getId() == aulaOriginal.getId() && "10/03/2026".equals(aula.getData()), "aula gravada");
        verificar(aula.getFeedbacks().size() == 1 && aula.getFeedbacks().get(0).getNota() == 9,
                "um feedback do aluno, com a nota trocada");
        verificar(materia.getMateriais().size() == 1, "material gravado");
        verificar(materia.getChatGeral().getMensagens().size() == 1 && materia.getChatEquipe().getMensagens().isEmpty()
                && aula.getChat().getMensagens().size() == 1, "mensagens gravadas e a apagada fora");

        verificar(materia.getGrupos().size() == 1 && materia.getGrupos().get(0).getEstudantes().size() == 1
                && materia.getGrupos().get(0).getMonitores().size() == 1, "grupo com aluno e monitor");
        PedidoMonitoria pedido = materia.getPedidosMonitoria().get(0);
        verificar("Aceito".equals(pedido.getStatus()) && pedido.getMonitor().getId() == monitorOriginal.getId()
                && "15:30".equals(pedido.getHorario()), "pedido aceito pelo monitor");

        Quiz quiz = aula.getQuiz();
        verificar(quiz != null && quiz.getPerguntas().size() == 1, "quiz com a pergunta");
        verificar(quiz.getTentativas().size() == 1 && quiz.getTentativas().get(0).getAcertos() == 1,
                "tentativa finalizada gravada");
    }

    private static void limpar() {
        CursoDao cursoDao = new CursoDao();
        UsuarioDao usuarioDao = new UsuarioDao();
        Curso curso = cursoDao.buscarPorSigla(SIGLA);
        if (curso != null) {
            for (Periodo periodo : new PeriodoDao().listarDoCurso(curso)) {
                for (Materia materia : new MateriaDao().listarDoPeriodo(periodo)) {
                    limparMateria(materia);
                }
            }
        }
        for (String login : LOGINS) {
            Usuario usuario = usuarioDao.buscarPorLogin(login);
            if (usuario != null) {
                usuarioDao.remover(usuario);
            }
        }
        if (curso != null) {
            cursoDao.remover(curso);
        }
    }

    private static void limparMateria(Materia materia) {
        GrupoEstudoDao grupoDao = new GrupoEstudoDao();
        PedidoMonitoriaDao pedidoDao = new PedidoMonitoriaDao();
        MaterialDao materialDao = new MaterialDao();
        ChatDao chatDao = new ChatDao();
        AulaDao aulaDao = new AulaDao();
        QuizDao quizDao = new QuizDao();
        for (GrupoEstudo grupo : grupoDao.listarDaMateria(materia)) {
            grupoDao.remover(grupo);
        }
        for (PedidoMonitoria pedido : pedidoDao.listarDaMateria(materia)) {
            pedidoDao.remover(pedido);
        }
        for (Material material : materialDao.listarDaMateria(materia)) {
            materialDao.remover(material);
        }
        apagarMensagens(chatDao, chatDao.listarDoChatGeral(materia));
        apagarMensagens(chatDao, chatDao.listarDoChatEquipe(materia));
        for (Aula aula : aulaDao.listarDaMateria(materia)) {
            Quiz quiz = quizDao.buscarDaAula(aula);
            if (quiz != null) {
                quizDao.remover(quiz);
            }
            apagarMensagens(chatDao, chatDao.listarDoChatDaAula(aula));
            new FeedbackDao().removerDaAula(aula);
            aulaDao.remover(aula);
        }
        new MateriaDao().remover(materia);
    }

    private static void apagarMensagens(ChatDao chatDao, ArrayList<Mensagem> mensagens) {
        for (Mensagem mensagem : mensagens) {
            chatDao.removerMensagem(mensagem);
        }
    }

    private static void verificar(boolean condicao, String descricao) {
        if (!condicao) {
            throw new AssertionError("FALHOU: " + descricao);
        }
    }
}
