package model;

public class RespostaQuiz extends Entidade {
    private PerguntaQuiz pergunta;
    private AlternativaQuiz alternativa;
    private boolean acertou;

    public RespostaQuiz(PerguntaQuiz pergunta, AlternativaQuiz alternativa) {
        validar(pergunta, alternativa);
        this.pergunta = pergunta;
        this.alternativa = alternativa;
        this.acertou = alternativa.isCorreta();
    }

    public static RespostaQuiz restaurar(PerguntaQuiz pergunta, AlternativaQuiz alternativa, boolean acertou) {
        validar(pergunta, alternativa);
        RespostaQuiz resposta = new RespostaQuiz(pergunta, alternativa);
        resposta.acertou = acertou;
        return resposta;
    }

    private static void validar(PerguntaQuiz pergunta, AlternativaQuiz alternativa) {
        if (pergunta == null || alternativa == null || !pergunta.getAlternativas().contains(alternativa)) {
            throw new RuntimeException("Alternativa nao pertence a pergunta");
        }
    }

    public PerguntaQuiz getPergunta() {
        return pergunta;
    }

    public AlternativaQuiz getAlternativa() {
        return alternativa;
    }

    public boolean isAcertou() {
        return acertou;
    }
}
