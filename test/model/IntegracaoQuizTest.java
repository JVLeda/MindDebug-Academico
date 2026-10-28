package model;

public class IntegracaoQuizTest {
    public static void main(String[] args) {
        Quiz quiz = new Quiz("Revisao de classes", "Verificacao de conceitos");
        PerguntaQuiz pergunta = new PerguntaQuiz("O que representa uma classe?", "Facil");
        AlternativaQuiz alternativaIncorreta = new AlternativaQuiz("Um objeto criado", "Objeto e uma instancia.", false);
        AlternativaQuiz alternativaCorreta = new AlternativaQuiz("Um molde para objetos", "Correto.", true);
        pergunta.adicionarAlternativa(alternativaIncorreta);
        pergunta.adicionarAlternativa(alternativaCorreta);
        pergunta.adicionarAlternativa(new AlternativaQuiz("Um metodo", "Metodo define comportamento.", false));
        pergunta.adicionarAlternativa(new AlternativaQuiz("Um atributo", "Atributo representa estado.", false));
        quiz.adicionarPergunta(pergunta);

        Estudante estudante = new Estudante("Aluno", "aluno@minddebug.edu.br", "2026001", "1234", null);
        TentativaQuiz tentativa = new TentativaQuiz(estudante, quiz);
        tentativa.adicionarResposta(new RespostaQuiz(pergunta, alternativaCorreta));

        assertEquals(1, tentativa.calcularAcertos(), "A resposta correta deve ser contabilizada.");
        assertEquals(100.0, tentativa.getPercentualAcertos(), "O percentual deve refletir o total de perguntas.");

        assertThrows(() -> tentativa.adicionarResposta(new RespostaQuiz(pergunta, alternativaIncorreta)),
                "A mesma pergunta nao pode ser respondida duas vezes.");
    }

    private static void assertEquals(Object esperado, Object atual, String mensagem) {
        if (!esperado.equals(atual)) {
            throw new AssertionError(mensagem + " Esperado: " + esperado + ", atual: " + atual);
        }
    }

    private static void assertThrows(Runnable acao, String mensagem) {
        try {
            acao.run();
            throw new AssertionError(mensagem);
        } catch (RuntimeException esperado) {
            // A excecao confirma a protecao contra respostas duplicadas.
        }
    }
}
