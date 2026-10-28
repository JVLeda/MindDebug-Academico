package model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Quiz extends Entidade {
    private String titulo;
    private String descricao;
    private Aula aula;
    private ArrayList<PerguntaQuiz> perguntas = new ArrayList<>();
    private ArrayList<TentativaQuiz> tentativas = new ArrayList<>();

    public Quiz(String titulo, String descricao) {
        this.titulo = titulo;
        this.descricao = descricao;
    }

    public void adicionarPergunta(PerguntaQuiz pergunta) {
        if (pergunta == null) {
            throw new RuntimeException("Pergunta do quiz obrigatoria");
        }
        if (!pergunta.temQuatroAlternativas() || pergunta.getAlternativaCorreta() == null) {
            throw new RuntimeException("Pergunta deve possuir quatro alternativas e uma correta");
        }
        perguntas.add(pergunta);
    }

    public void adicionarTentativa(TentativaQuiz tentativa) {
        if (tentativa == null) {
            throw new RuntimeException("Tentativa do quiz obrigatoria");
        }
        if (tentativa.getQuiz() != this) {
            throw new RuntimeException("Tentativa nao pertence ao quiz");
        }
        tentativas.add(tentativa);
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public Aula getAula() {
        return aula;
    }

    public void setAula(Aula aula) {
        this.aula = aula;
    }

    public List<PerguntaQuiz> getPerguntas() {
        return Collections.unmodifiableList(perguntas);
    }

    public boolean estaProntoParaResponder() {
        return !perguntas.isEmpty();
    }

    public List<TentativaQuiz> getTentativas() {
        return Collections.unmodifiableList(tentativas);
    }
}
