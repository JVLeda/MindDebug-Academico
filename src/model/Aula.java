package model;

import java.util.ArrayList;

public class Aula extends Entidade {
    public static final double MEDIA_ESPERADA_PADRAO = 7.0;
    public static final double FAIXA_REGULAR = 3.0;

    private String titulo;
    private String data;
    private String descricao;
    private double mediaEsperada = MEDIA_ESPERADA_PADRAO;
    private Quiz quiz;
    private ArrayList<Feedback> feedbacks = new ArrayList<>();
    private Chat chat = new Chat("Chat da aula");

    public Aula(String titulo, String data, String descricao) {
        this.titulo = titulo;
        this.data = data;
        this.descricao = descricao;
    }

    public void registrarFeedback(Feedback novoFeedback) {
        for (Feedback feedback : feedbacks) {
            if (feedback.getEstudante() == novoFeedback.getEstudante()) {
                feedback.setNota(novoFeedback.getNota());
                feedback.setComentario(novoFeedback.getComentario());
                return;
            }
        }
        feedbacks.add(novoFeedback);
    }

    public double calcularMedia() {
        if (feedbacks.isEmpty()) {
            return 0;
        }
        int soma = 0;
        for (Feedback feedback : feedbacks) {
            soma += feedback.getNota();
        }
        return soma / (double) feedbacks.size();
    }

    public StatusAula getStatusAula() {
        if (feedbacks.isEmpty()) {
            return StatusAula.SEM_DADOS;
        }
        double media = calcularMedia();
        if (media >= mediaEsperada) {
            return StatusAula.SATISFATORIO;
        }
        if (media >= mediaEsperada - FAIXA_REGULAR) {
            return StatusAula.REGULAR;
        }
        return StatusAula.CRITICO;
    }

    public String getStatus() {
        return getStatusAula().getNome();
    }

    public String getAcaoPedagogica() {
        if (feedbacks.isEmpty()) {
            return "Aguardar feedback da turma";
        }
        if (precisaAcaoDeReforco()) {
            return "Planejar acao de reforco";
        }
        return "Manter acompanhamento";
    }

    public boolean precisaAcaoDeReforco() {
        return !feedbacks.isEmpty() && calcularMedia() < mediaEsperada;
    }

    public String getParticipacaoDaTurma(int totalEstudantes) {
        if (totalEstudantes <= 0) {
            return "0%";
        }
        return Math.round((feedbacks.size() * 100.0) / totalEstudantes) + "%";
    }

    public int getQuantidadeTentativasQuiz() {
        if (quiz == null) {
            return 0;
        }
        return quiz.getTentativas().size();
    }

    public double calcularMediaAcertosQuiz() {
        if (getQuantidadeTentativasQuiz() == 0) {
            return 0;
        }
        double soma = 0;
        for (TentativaQuiz tentativa : quiz.getTentativas()) {
            soma += tentativa.getPercentualAcertos();
        }
        return soma / getQuantidadeTentativasQuiz();
    }

    public String getTitulo() {
        return titulo;
    }

    public String getData() {
        return data;
    }

    public String getDescricao() {
        return descricao;
    }

    public double getMediaEsperada() {
        return mediaEsperada;
    }

    public void setMediaEsperada(double mediaEsperada) {
        this.mediaEsperada = mediaEsperada;
    }

    public ArrayList<Feedback> getFeedbacks() {
        return feedbacks;
    }

    public Chat getChat() {
        return chat;
    }

    public Quiz getQuiz() {
        return quiz;
    }

    public void setQuiz(Quiz quiz) {
        this.quiz = quiz;
    }

    @Override
    public String toString() {
        return titulo;
    }
}
