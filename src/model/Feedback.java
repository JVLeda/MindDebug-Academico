package model;

public class Feedback extends Entidade {
    private Estudante estudante;
    private int nota;
    private String comentario;

    public Feedback(Estudante estudante, int nota, String comentario) {
        this.estudante = estudante;
        this.nota = nota;
        this.comentario = comentario;
    }

    public Estudante getEstudante() {
        return estudante;
    }

    public int getNota() {
        return nota;
    }

    public void setNota(int nota) {
        this.nota = nota;
    }

    public String getComentario() {
        return comentario;
    }

    public void setComentario(String comentario) {
        this.comentario = comentario;
    }
}
