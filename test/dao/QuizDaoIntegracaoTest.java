package dao;

import excecao.MindDebugException;
import excecao.RegraDeNegocioException;
import model.AlternativaQuiz;
import model.Aula;
import model.Curso;
import model.Estudante;
import model.Materia;
import model.PerguntaQuiz;
import model.Periodo;
import model.Quiz;
import model.RespostaQuiz;
import model.TentativaQuiz;
import model.Usuario;

/**
 * Teste de integracao do QuizDao. Precisa do MySQL ativo. Usa o curso de sigla TESTE9
 * e o aluno TESTE9_aluno, apagados no fim mesmo se o teste falhar.
 */
public class QuizDaoIntegracaoTest {
    private static final String SIGLA = "TESTE9";
    private static final String LOGIN_ALUNO = "TESTE9_aluno";

    private static UsuarioDao usuarioDao = new UsuarioDao();
    private static CursoDao cursoDao = new CursoDao();
    private static PeriodoDao periodoDao = new PeriodoDao();
    private static MateriaDao materiaDao = new MateriaDao();
    private static AulaDao aulaDao = new AulaDao();
    private static QuizDao quizDao = new QuizDao();

    public static void main(String[] args) {
        new BancoDao().carregar(); // garante que as tabelas existem
        limpar();
        try {
            Estudante aluno = new Estudante("Aluno de Teste", "teste9_aluno@teste.local", LOGIN_ALUNO, "1234", null);
            usuarioDao.inserir(aluno);
            Aula aula = criarAula();

            Quiz quiz = criarQuiz(aula);
            perguntas(quiz);
            perguntaIncompletaNaoEntra(quiz);
            perguntaComFalhaNaoFicaPelaMetade(aula, quiz);
            tentativa(aula, quiz, aluno);

            quizDao.remover(quiz);
            verificar(quizDao.buscarDaAula(aulaDao.listarDaMateria(materiaDaAula()).get(0)) == null,
                    "quiz removido com perguntas, alternativas, tentativas e respostas");
        } finally {
            limpar();
        }
        verificar(cursoDao.buscarPorSigla(SIGLA) == null, "dados de teste apagados no fim");
    }

    private static Aula criarAula() {
        Curso curso = new Curso("Curso de Teste", SIGLA, "Teste", 3000, "Coordenacao de Teste");
        cursoDao.inserir(curso);
        Periodo periodo = new Periodo(1, "Periodo de teste");
        periodoDao.inserir(curso, periodo);
        Materia materia = new Materia("Materia de Teste", null);
        materiaDao.inserir(periodo, materia);
        Aula aula = new Aula("Aula de Teste", "05/03/2026", "Aula com quiz");
        aulaDao.inserir(materia, aula);
        return aula;
    }

    private static Materia materiaDaAula() {
        Curso curso = cursoDao.buscarPorSigla(SIGLA);
        Periodo periodo = periodoDao.listarDoCurso(curso).get(0);
        return materiaDao.listarDoPeriodo(periodo).get(0);
    }

    private static Quiz criarQuiz(Aula aula) {
        Quiz quiz = new Quiz("Quiz de Teste", "Revisao da aula");
        quiz.setAula(aula);
        quizDao.inserir(aula, quiz);
        verificar(quiz.getId() > 0, "quiz recebe o id gerado");

        boolean recusou = false;
        try {
            quizDao.inserir(aula, new Quiz("Outro quiz", ""));
        } catch (RegraDeNegocioException esperado) {
            recusou = true;
        }
        verificar(recusou, "a aula so pode ter um quiz");
        return quiz;
    }

    private static PerguntaQuiz novaPergunta(String enunciado, int indiceCorreta) {
        PerguntaQuiz pergunta = new PerguntaQuiz(enunciado, "Facil");
        for (int i = 0; i < 4; i++) {
            pergunta.adicionarAlternativa(new AlternativaQuiz("Alternativa " + (i + 1), "Explicacao " + (i + 1),
                    i == indiceCorreta));
        }
        return pergunta;
    }

    private static void perguntas(Quiz quiz) {
        PerguntaQuiz primeira = novaPergunta("Primeira pergunta", 1);
        quizDao.adicionarPergunta(quiz, primeira);
        quiz.adicionarPergunta(primeira);
        PerguntaQuiz segunda = novaPergunta("Segunda pergunta", 3);
        quizDao.adicionarPergunta(quiz, segunda);
        quiz.adicionarPergunta(segunda);

        verificar(primeira.getId() > 0 && segunda.getId() > primeira.getId(), "perguntas recebem ids gerados");
        for (AlternativaQuiz alternativa : primeira.getAlternativas()) {
            verificar(alternativa.getId() > 0, "cada alternativa recebe o id gerado");
        }

        Quiz lido = quizDao.buscarDaAula(quiz.getAula());
        verificar(lido != null && lido.getId() == quiz.getId() && "Quiz de Teste".equals(lido.getTitulo()),
                "quiz lido pela aula");
        verificar(lido.getPerguntas().size() == 2, "duas perguntas lidas");
        PerguntaQuiz lida = lido.getPerguntas().get(0);
        verificar("Primeira pergunta".equals(lida.getEnunciado()) && lida.getAlternativas().size() == 4,
                "pergunta lida com as quatro alternativas");
        verificar(lida.getAlternativaCorreta().getId() == primeira.getAlternativas().get(1).getId()
                && "Explicacao 2".equals(lida.getAlternativaCorreta().getExplicacao()), "alternativa correta e explicacao lidas");
        verificar(lido.getPerguntas().get(1).getAlternativaCorreta().getId() == segunda.getAlternativas().get(3).getId(),
                "alternativa correta da segunda pergunta lida");
    }

    private static void perguntaIncompletaNaoEntra(Quiz quiz) {
        PerguntaQuiz incompleta = new PerguntaQuiz("Pergunta com tres alternativas", "Facil");
        for (int i = 0; i < 3; i++) {
            incompleta.adicionarAlternativa(new AlternativaQuiz("Alternativa " + i, "", i == 0));
        }
        boolean recusou = false;
        try {
            quizDao.adicionarPergunta(quiz, incompleta);
        } catch (RegraDeNegocioException esperado) {
            recusou = true;
        }
        verificar(recusou && incompleta.getId() == 0, "pergunta sem quatro alternativas recusada");
        verificar(quizDao.buscarDaAula(quiz.getAula()).getPerguntas().size() == 2, "nada gravado da pergunta incompleta");
    }

    /** A quarta alternativa tem texto nulo (NOT NULL no banco): a pergunta e as tres primeiras sao desfeitas. */
    private static void perguntaComFalhaNaoFicaPelaMetade(Aula aula, Quiz quiz) {
        PerguntaQuiz comFalha = new PerguntaQuiz("Pergunta que falha no meio", "Media");
        for (int i = 0; i < 3; i++) {
            comFalha.adicionarAlternativa(new AlternativaQuiz("Alternativa " + i, "", i == 0));
        }
        comFalha.adicionarAlternativa(new AlternativaQuiz(null, "", false));

        boolean falhou = false;
        try {
            quizDao.adicionarPergunta(quiz, comFalha);
        } catch (MindDebugException esperado) {
            falhou = true;
        }
        verificar(falhou, "alternativa sem texto faz a gravacao falhar");
        verificar(comFalha.getId() == 0 && comFalha.getAlternativas().get(0).getId() == 0,
                "objetos nao recebem id quando a transacao falha");
        verificar(quizDao.buscarDaAula(aula).getPerguntas().size() == 2,
                "a pergunta e as alternativas ja inseridas foram desfeitas");
    }

    private static void tentativa(Aula aula, Quiz quiz, Estudante aluno) {
        PerguntaQuiz primeira = quiz.getPerguntas().get(0);
        PerguntaQuiz segunda = quiz.getPerguntas().get(1);
        TentativaQuiz tentativa = new TentativaQuiz(aluno, quiz);
        tentativa.adicionarResposta(new RespostaQuiz(primeira, primeira.getAlternativaCorreta()));
        tentativa.adicionarResposta(new RespostaQuiz(segunda, segunda.getAlternativas().get(0)));
        tentativa.calcularAcertos();
        quizDao.registrarTentativa(tentativa);
        verificar(tentativa.getId() > 0 && tentativa.getRespostas().get(1).getId() > 0,
                "tentativa e respostas recebem ids gerados");

        Quiz lido = quizDao.buscarDaAula(aula);
        verificar(lido.getTentativas().size() == 1, "uma tentativa lida");
        TentativaQuiz lida = lido.getTentativas().get(0);
        verificar(lida.getEstudante().getId() == aluno.getId(), "tentativa ligada ao aluno");
        verificar(lida.getAcertos() == 1 && lida.getTotalPerguntas() == 2, "um acerto em duas perguntas");
        verificar(tentativa.getDataTentativa().equals(lida.getDataTentativa()), "data e hora da tentativa lidas");
        verificar(lida.getRespostas().size() == 2 && lida.getRespostas().get(0).isAcertou()
                && !lida.getRespostas().get(1).isAcertou(), "respostas lidas com acerto e erro");
        verificar(lida.getRespostas().get(1).getAlternativa().getId() == segunda.getAlternativas().get(0).getId(),
                "alternativa escolhida lida");
    }

    private static void limpar() {
        Curso curso = cursoDao.buscarPorSigla(SIGLA);
        if (curso != null) {
            for (Periodo periodo : periodoDao.listarDoCurso(curso)) {
                for (Materia materia : materiaDao.listarDoPeriodo(periodo)) {
                    for (Aula aula : aulaDao.listarDaMateria(materia)) {
                        Quiz quiz = quizDao.buscarDaAula(aula);
                        if (quiz != null) {
                            quizDao.remover(quiz);
                        }
                        aulaDao.remover(aula);
                    }
                    materiaDao.remover(materia);
                }
            }
            cursoDao.remover(curso);
        }
        Usuario aluno = usuarioDao.buscarPorLogin(LOGIN_ALUNO);
        if (aluno != null) {
            usuarioDao.remover(aluno);
        }
    }

    private static void verificar(boolean condicao, String descricao) {
        if (!condicao) {
            throw new AssertionError("FALHOU: " + descricao);
        }
    }
}
