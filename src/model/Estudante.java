package model;

import java.util.ArrayList;

public class Estudante extends Usuario {
    private Curso curso;

    public Estudante(String nome, String email, String matricula, String senha, Curso curso) {
        super(nome, matricula, email, senha);
        setMatricula(matricula);
        this.curso = curso;
    }

    @Override
    public int getNivel() {
        return 1;
    }

    @Override
    public String getCargo() {
        return "Aluno";
    }

    @Override
    public Perfil getPerfil() {
        return Perfil.ALUNO;
    }

    @Override
    public boolean podeRegistrarFeedback() {
        return true;
    }

    @Override
    public boolean podeSolicitarMonitoria() {
        return true;
    }

    @Override
    public boolean podeResponderQuiz() {
        return true;
    }

    @Override
    public void registrarFeedback(Aula aula, int nota, String comentario) {
        aula.registrarFeedback(criarFeedback(nota, comentario));
    }

    @Override
    public Feedback criarFeedback(int nota, String comentario) {
        return new Feedback(this, nota, comentario);
    }

    @Override
    public PedidoMonitoria criarPedidoMonitoria(Aula aula, Monitor monitor, String duvida, String data, String horario) {
        return new PedidoMonitoria(this, aula, monitor, duvida, data, horario);
    }

    @Override
    public TentativaQuiz iniciarTentativaQuiz(Quiz quiz) {
        return new TentativaQuiz(this, quiz);
    }

    @Override
    public ArrayList<Curso> filtrarCursosVisiveis(ArrayList<Curso> cursos) {
        ArrayList<Curso> visiveis = new ArrayList<>();
        if (curso != null) {
            visiveis.add(curso);
        }
        return visiveis;
    }

    @Override
    public void incluirEmEstudantes(ArrayList<Estudante> estudantes) {
        estudantes.add(this);
    }

    @Override
    public Curso getCurso() {
        return curso;
    }
}
