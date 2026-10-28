package model;

public class QuizDisponibilidadeTest {
    public static void main(String[] args) {
        Quiz quiz = new Quiz("Revisao de vetores", "Conceitos basicos");

        assertFalse(quiz.estaProntoParaResponder(), "Quiz sem perguntas nao pode ser respondido.");

        PerguntaQuiz pergunta = new PerguntaQuiz("Qual e o indice inicial?", "Facil");
        pergunta.adicionarAlternativa(new AlternativaQuiz("1", "Indice incorreto.", false));
        pergunta.adicionarAlternativa(new AlternativaQuiz("0", "Correto.", true));
        pergunta.adicionarAlternativa(new AlternativaQuiz("-1", "Indice incorreto.", false));
        pergunta.adicionarAlternativa(new AlternativaQuiz("Depende", "Indice incorreto.", false));
        quiz.adicionarPergunta(pergunta);

        assertTrue(quiz.estaProntoParaResponder(), "Quiz com pergunta valida deve estar pronto para responder.");
    }

    private static void assertTrue(boolean condicao, String mensagem) {
        if (!condicao) {
            throw new AssertionError(mensagem);
        }
    }

    private static void assertFalse(boolean condicao, String mensagem) {
        assertTrue(!condicao, mensagem);
    }
}
