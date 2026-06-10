package dao;

import model.Curso;
import model.Usuario;

import java.io.Serializable;
import java.util.ArrayList;

public class BancoDados implements Serializable {
    private ArrayList<Usuario> usuarios = new ArrayList<>();
    private ArrayList<Curso> cursos = new ArrayList<>();
    private int proximoId = 1;

    public int gerarId() {
        return proximoId++;
    }

    public void ajustarProximoId(int maiorId) {
        proximoId = maiorId + 1;
    }

    public ArrayList<Usuario> getUsuarios() {
        return usuarios;
    }

    public ArrayList<Curso> getCursos() {
        return cursos;
    }
}
