package controller.aluno;

import controller.ControllerBase;
import dao.BancoDados;
import dao.ParticipanteDao;
import excecao.DadoInvalidoException;
import model.Estudante;
import model.Materia;
import model.Monitor;
import model.Usuario;

import java.util.ArrayList;

/** Entrada do aluno (e do monitor) numa disciplina, modulo do Aluno. */
public class MatriculaController extends ControllerBase {
    private ParticipanteDao participanteDao = new ParticipanteDao();

    public MatriculaController(BancoDados banco) {
        super(banco);
    }

    /**
     * Ao abrir uma disciplina, o aluno ou monitor passa a participar dela.
     * Administrador e professor nao entram na tabela de participantes.
     */
    public void entrarNaMateria(Usuario usuarioLogado, Materia materia) {
        if (usuarioLogado == null || materia == null) {
            throw new DadoInvalidoException("Escolha uma disciplina.");
        }
        if (participaDaMateria(materia, usuarioLogado)) {
            return;
        }
        if (gravaComoParticipante(usuarioLogado)) {
            participanteDao.adicionar(materia, usuarioLogado);
        }
        materia.adicionarParticipante(usuarioLogado);
    }

    /** Aluno e monitor sao gravados em participantes; administrador e professor, nao. */
    private boolean gravaComoParticipante(Usuario usuario) {
        ArrayList<Estudante> estudantes = new ArrayList<>();
        ArrayList<Monitor> monitores = new ArrayList<>();
        usuario.incluirEmEstudantes(estudantes);
        usuario.incluirEmMonitores(monitores);
        return !estudantes.isEmpty() || !monitores.isEmpty();
    }

    private boolean participaDaMateria(Materia materia, Usuario usuario) {
        for (Estudante estudante : materia.getEstudantes()) {
            if (estudante == usuario) {
                return true;
            }
        }
        for (Monitor monitor : materia.getMonitores()) {
            if (monitor == usuario) {
                return true;
            }
        }
        return false;
    }
}
