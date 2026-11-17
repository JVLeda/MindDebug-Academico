package dao;

import model.Curso;
import model.Usuario;

import java.io.Serializable;
import java.util.ArrayList;

public class BancoDados implements Serializable {
    private ArrayList<Usuario> usuarios = new ArrayList<>();
    private ArrayList<Curso> cursos = new ArrayList<>();
    private int proximoId = 1;

    /** Ids dos objetos montados em memoria antes de ir para o banco (so os dados iniciais). */
    public int gerarId() {
        return proximoId++;
    }

    public ArrayList<Usuario> getUsuarios() {
        return usuarios;
    }

    // Operacoes so em memoria. Quem grava no banco e o controller, pelos DAOs, antes de chamar estes metodos.

    public void adicionarUsuario(Usuario usuario) {
        if (usuario.getId() == 0) {
            usuario.setId(gerarId());
        }
        usuarios.add(usuario);
    }

    public Usuario buscarUsuarioPorLogin(String login) {
        for (Usuario usuario : usuarios) {
            if (usuario.getLogin().equalsIgnoreCase(login)) {
                return usuario;
            }
        }
        return null;
    }

    public void removerUsuario(Usuario usuario) {
        usuarios.remove(usuario);
    }

    public void removerCurso(Curso curso) {
        cursos.remove(curso);
    }

    public void adicionarCurso(Curso curso) {
        if (curso.getId() == 0) {
            curso.setId(gerarId());
        }
        cursos.add(curso);
    }

    public ArrayList<Curso> getCursos() {
        return cursos;
    }
}
