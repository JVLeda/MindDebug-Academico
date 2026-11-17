package model;

import excecao.DadoInvalidoException;
import excecao.RegraDeNegocioException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

public class TentativaQuiz extends Entidade {
    private Estudante estudante;
    private Quiz quiz;
    private String dataTentativa;
    private int acertos;
    private int totalPerguntas;
    private ArrayList<RespostaQuiz> respostas = new ArrayList<>();

    public TentativaQuiz(Estudante estudante, Quiz quiz) {
        if (estudante == null) {
            throw new DadoInvalidoException("Estudante obrigatorio");
        }
        if (quiz == null) {
            throw new DadoInvalidoException("Quiz obrigatorio");
        }
        this.estudante = estudante;
        this.quiz = quiz;
        this.dataTentativa = new SimpleDateFormat("dd/MM/yyyy HH:mm").format(new Date());
    }

    public static TentativaQuiz restaurar(Estudante estudante, Quiz quiz, int acertos, int totalPerguntas) {
        if (acertos < 0 || totalPerguntas < 0 || acertos > totalPerguntas) {
            throw new DadoInvalidoException("Resultado da tentativa invalido");
        }
        TentativaQuiz tentativa = new TentativaQuiz(estudante, quiz);
        tentativa.acertos = acertos;
        tentativa.totalPerguntas = totalPerguntas;
        return tentativa;
    }

    public void adicionarResposta(RespostaQuiz resposta) {
        if (resposta == null) {
            throw new DadoInvalidoException("Resposta obrigatoria");
        }
        if (!quiz.getPerguntas().contains(resposta.getPergunta())) {
            throw new RegraDeNegocioException("Pergunta nao pertence ao quiz");
        }
        for (RespostaQuiz respostaExistente : respostas) {
            if (respostaExistente.getPergunta() == resposta.getPergunta()) {
                throw new RegraDeNegocioException("Pergunta ja respondida");
            }
        }
        respostas.add(resposta);
    }

    public int calcularAcertos() {
        acertos = 0;
        for (RespostaQuiz resposta : respostas) {
            if (resposta.isAcertou()) {
                acertos++;
            }
        }
        totalPerguntas = quiz.getPerguntas().size();
        return acertos;
    }

    public double getPercentualAcertos() {
        if (totalPerguntas == 0) {
            return 0;
        }
        return acertos * 100.0 / totalPerguntas;
    }

    public Estudante getEstudante() {
        return estudante;
    }

    public Quiz getQuiz() {
        return quiz;
    }

    public String getDataTentativa() {
        return dataTentativa;
    }

    public void setDataTentativa(String dataTentativa) {
        this.dataTentativa = dataTentativa;
    }

    public int getAcertos() {
        return acertos;
    }

    public int getTotalPerguntas() {
        return totalPerguntas;
    }

    public List<RespostaQuiz> getRespostas() {
        return Collections.unmodifiableList(respostas);
    }
}
