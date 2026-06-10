package dao;

import model.Curso;

import java.util.ArrayList;

public class CursoDao {
    private BancoDados banco;

    public CursoDao(BancoDados banco) {
        this.banco = banco;
    }

    public void cadastrar(Curso curso) {
        if (curso.getId() == 0) {
            curso.setId(banco.gerarId());
        }
        banco.getCursos().add(curso);
    }

    public ArrayList<Curso> listar() {
        return banco.getCursos();
    }

    public void atualizar(Curso cursoAtualizado) {
        for (int i = 0; i < banco.getCursos().size(); i++) {
            Curso curso = banco.getCursos().get(i);
            if (curso.getId() == cursoAtualizado.getId()) {
                banco.getCursos().set(i, cursoAtualizado);
                return;
            }
        }
    }

    public void remover(Curso curso) {
        banco.getCursos().remove(curso);
    }
}
