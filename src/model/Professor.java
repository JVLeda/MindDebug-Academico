package model;

import java.util.ArrayList;

/**
 * Professor. A matricula herdada de Usuario guarda a matricula SIAPE.
 */
public class Professor extends Usuario implements Moderador {
    private String titulacao = "";
    private String areaAtuacao = "";

    public Professor(String nome, String login, String email, String senha) {
        super(nome, login, email, senha);
    }

    @Override
    public int getNivel() {
        return 3;
    }

    @Override
    public String getCargo() {
        return "Docente";
    }

    @Override
    public Perfil getPerfil() {
        return Perfil.PROFESSOR;
    }

    @Override
    public boolean podeModerar() {
        return true;
    }

    @Override
    public boolean podeCadastrarMonitor() {
        return true;
    }

    @Override
    public boolean podeGerenciarMateria(Materia materia) {
        return materia != null && ehOMesmoUsuario(materia.getProfessor());
    }

    @Override
    public boolean podeAtenderMonitoria() {
        return true;
    }

    @Override
    public boolean podeApagarConteudoDe(Usuario autor) {
        return true;
    }

    @Override
    public void incluirEmProfessores(ArrayList<Professor> professores) {
        professores.add(this);
    }

    public String getTitulacao() {
        return titulacao;
    }

    public void setTitulacao(String titulacao) {
        this.titulacao = titulacao;
    }

    public String getAreaAtuacao() {
        return areaAtuacao;
    }

    public void setAreaAtuacao(String areaAtuacao) {
        this.areaAtuacao = areaAtuacao;
    }
}
