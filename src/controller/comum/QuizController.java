package controller.comum;

import controller.ControllerBase;
import dao.BancoDados;
import dao.QuizDao;
import excecao.AcessoNegadoException;
import excecao.DadoInvalidoException;
import excecao.RegraDeNegocioException;
import model.AlternativaQuiz;
import model.Aula;
import model.Materia;
import model.PerguntaQuiz;
import model.Quiz;
import model.RespostaQuiz;
import model.TentativaQuiz;
import model.Usuario;

/**
 * Quiz da aula. O professor da materia monta o quiz e as perguntas; o aluno da materia responde.
 * A tentativa fica so em memoria enquanto o aluno responde e e gravada ao finalizar.
 */
public class QuizController extends ControllerBase {
    private QuizDao quizDao = new QuizDao();

    public QuizController(BancoDados banco) {
        super(banco);
    }

    public Quiz criarQuiz(Usuario usuarioLogado, Materia materia, Aula aula, String titulo, String descricao) {
        exigirProfessorDaMateria(usuarioLogado, materia, "criar quiz");
        validarAulaDaMateria(materia, aula);
        if (aula.getQuiz() != null) {
            throw new RegraDeNegocioException("Esta aula ja possui quiz.");
        }

        Quiz quiz = new Quiz(validador.texto(titulo, "Titulo do quiz"), descricao);
        quizDao.inserir(aula, quiz);
        quiz.setAula(aula);
        aula.setQuiz(quiz);
        return quiz;
    }

    public PerguntaQuiz adicionarPerguntaAoQuiz(Usuario usuarioLogado, Materia materia, Quiz quiz,
            String enunciado, String dificuldade, String[] textosAlternativas,
            String[] explicacoes, int indiceCorreta) {
        exigirProfessorDaMateria(usuarioLogado, materia, "adicionar pergunta ao quiz");
        validarQuizDaMateria(materia, quiz);
        if (textosAlternativas == null || explicacoes == null || textosAlternativas.length != 4
                || explicacoes.length != 4 || indiceCorreta < 0 || indiceCorreta > 3) {
            throw new DadoInvalidoException("Informe quatro alternativas e uma resposta correta.");
        }

        PerguntaQuiz pergunta = new PerguntaQuiz(
                validador.texto(enunciado, "Enunciado"),
                validador.texto(dificuldade, "Dificuldade")
        );
        for (int i = 0; i < 4; i++) {
            AlternativaQuiz alternativa = new AlternativaQuiz(
                    validador.texto(textosAlternativas[i], "Alternativa"),
                    explicacoes[i],
                    i == indiceCorreta
            );
            pergunta.adicionarAlternativa(alternativa);
        }

        quizDao.adicionarPergunta(quiz, pergunta);
        quiz.adicionarPergunta(pergunta);
        return pergunta;
    }

    public void removerQuiz(Usuario usuarioLogado, Materia materia, Aula aula) {
        exigirProfessorDaMateria(usuarioLogado, materia, "remover quiz");
        validarAulaDaMateria(materia, aula);
        if (aula.getQuiz() == null) {
            throw new RegraDeNegocioException("A aula nao possui quiz.");
        }
        quizDao.remover(aula.getQuiz());
        aula.setQuiz(null);
    }

    public TentativaQuiz iniciarTentativaQuiz(Usuario usuarioLogado, Materia materia, Quiz quiz) {
        validarEstudanteDoQuiz(usuarioLogado, materia, quiz);
        if (!quiz.estaProntoParaResponder()) {
            throw new RegraDeNegocioException("O quiz ainda nao possui perguntas.");
        }
        return usuarioLogado.iniciarTentativaQuiz(quiz);
    }

    public void registrarRespostaQuiz(Usuario usuarioLogado, TentativaQuiz tentativa,
            PerguntaQuiz pergunta, AlternativaQuiz alternativa) {
        if (tentativa == null || tentativa.getEstudante() != usuarioLogado) {
            throw new AcessoNegadoException("Somente o estudante da tentativa pode responder.");
        }
        tentativa.adicionarResposta(new RespostaQuiz(pergunta, alternativa));
    }

    public void finalizarTentativaQuiz(Usuario usuarioLogado, TentativaQuiz tentativa) {
        if (tentativa == null || tentativa.getEstudante() != usuarioLogado) {
            throw new AcessoNegadoException("Somente o estudante da tentativa pode finalizar.");
        }
        if (tentativa.getQuiz().getTentativas().contains(tentativa)) {
            return;
        }
        if (tentativa.getRespostas().size() != tentativa.getQuiz().getPerguntas().size()) {
            throw new RegraDeNegocioException("Responda todas as perguntas antes de finalizar.");
        }
        tentativa.calcularAcertos();
        quizDao.registrarTentativa(tentativa);
        tentativa.getQuiz().adicionarTentativa(tentativa);
    }

    private void validarQuizDaMateria(Materia materia, Quiz quiz) {
        if (quiz == null || quiz.getAula() == null) {
            throw new DadoInvalidoException("Quiz invalido.");
        }
        validarAulaDaMateria(materia, quiz.getAula());
        if (quiz.getAula().getQuiz() != quiz) {
            throw new DadoInvalidoException("Quiz invalido.");
        }
    }

    private void validarEstudanteDoQuiz(Usuario usuarioLogado, Materia materia, Quiz quiz) {
        if (usuarioLogado == null || !usuarioLogado.podeResponderQuiz()) {
            throw new AcessoNegadoException("Somente estudante responde quiz.");
        }
        validarQuizDaMateria(materia, quiz);
        if (!materia.getEstudantes().contains(usuarioLogado)) {
            throw new RegraDeNegocioException("Estudante nao participa desta materia.");
        }
    }
}
