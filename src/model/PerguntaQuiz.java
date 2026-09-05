package model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PerguntaQuiz extends Entidade {
    private String enunciado;
    private String dificuldade;
    private ArrayList<AlternativaQuiz> alternativas = new ArrayList<>();

    public PerguntaQuiz(String enunciado, String dificuldade) {
        this.enunciado = enunciado;
        this.dificuldade = dificuldade;
    }

    public void adicionarAlternativa(AlternativaQuiz alternativa) {
        if (alternativa == null) {
            throw new RuntimeException("Alternativa obrigatoria");
        }
        if (alternativas.size() == 4) {
            throw new RuntimeException("A pergunta deve possuir exatamente quatro alternativas");
        }
        if (alternativa.isCorreta() && getAlternativaCorreta() != null) {
            throw new RuntimeException("A pergunta deve possuir somente uma alternativa correta");
        }
        alternativas.add(alternativa);
    }

    public AlternativaQuiz getAlternativaCorreta() {
        for (AlternativaQuiz alternativa : alternativas) {
            if (alternativa.isCorreta()) {
                return alternativa;
            }
        }
        return null;
    }

    public boolean temQuatroAlternativas() {
        return alternativas.size() == 4;
    }

    public String getEnunciado() {
        return enunciado;
    }

    public void setEnunciado(String enunciado) {
        this.enunciado = enunciado;
    }

    public String getDificuldade() {
        return dificuldade;
    }

    public void setDificuldade(String dificuldade) {
        this.dificuldade = dificuldade;
    }

    public List<AlternativaQuiz> getAlternativas() {
        return Collections.unmodifiableList(alternativas);
    }
}
