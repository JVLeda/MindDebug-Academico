package controller.professor;

import controller.ControllerBase;
import dao.BancoDados;
import dao.ParticipanteDao;
import excecao.DadoInvalidoException;
import model.Materia;
import model.Monitor;
import model.Usuario;

/** Monitores que atendem a disciplina, modulo do Professor. */
public class MonitoresDaMateriaController extends ControllerBase {
    private ParticipanteDao participanteDao = new ParticipanteDao();

    public MonitoresDaMateriaController(BancoDados banco) {
        super(banco);
    }

    public void adicionarMonitorNaMateria(Usuario usuarioLogado, Materia materia, Monitor monitor) {
        exigirProfessorDaMateriaOuAdministrador(usuarioLogado, materia, "adicionar monitor");
        if (monitor == null || materia.getMonitores().contains(monitor)) {
            return;
        }
        participanteDao.adicionar(materia, monitor);
        materia.adicionarParticipante(monitor);
    }

    public void removerMonitorDaMateria(Usuario usuarioLogado, Materia materia, Monitor monitor) {
        exigirProfessorDaMateriaOuAdministrador(usuarioLogado, materia, "remover monitor");
        if (monitor == null) {
            throw new DadoInvalidoException("Escolha um monitor.");
        }
        participanteDao.remover(materia, monitor);
        materia.removerParticipante(monitor);
    }
}
