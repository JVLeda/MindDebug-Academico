package model;

import java.util.ArrayList;

public class Aula extends Entidade {
    private String titulo;
    private String data;
    private String objetivo;
    private Quiz quiz;
    private ArrayList<Feedback> feedbacks = new ArrayList<>();
    private Chat chat = new Chat("Chat da aula");

    public Aula(String titulo, String data, String objetivo) {
        this.titulo = titulo;
        this.data = data;
        this.objetivo = objetivo;
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

    public String getStatus() {
        if (feedbacks.isEmpty()) {
            return "Sem Respostas";
        }
        if (calcularMedia() < 4) {
            return "Critico";
        }
        if (calcularMedia() < 7) {
            return "Regular";
        }
        return "Dominio total";
    }

    public String getAcaoPedagogica() {
        if (feedbacks.isEmpty()) {
            return "Aguardar feedback da turma";
        }
        if (calcularMedia() < 7) {
            return "Elaborar quiz de reforco";
        }
        return "Manter acompanhamento";
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

    public String getObjetivo() {
        return objetivo;
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
