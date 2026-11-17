package controller.professor;

import controller.ControllerBase;
import dao.AulaDao;
import dao.BancoDados;
import model.Aula;
import model.Materia;
import model.Usuario;

/** Aulas da disciplina, modulo do Professor. */
public class AulaController extends ControllerBase {
    private AulaDao aulaDao = new AulaDao();

    public AulaController(BancoDados banco) {
        super(banco);
    }

    public Aula criarAula(Usuario usuarioLogado, Materia materia, String titulo, String data) {
        exigirProfessorDaMateria(usuarioLogado, materia, "criar aula");
        Aula aula = new Aula(validador.texto(titulo, "Titulo da aula"), validador.textoDeData(data, "Data da aula"), "");
        aulaDao.inserir(materia, aula);
        materia.adicionarAula(aula);
        return aula;
    }

    /** Aula com feedbacks, mensagens ou quiz nao e apagada; o banco recusa e avisa. */
    public void removerAula(Usuario usuarioLogado, Materia materia, Aula aula) {
        exigirProfessorDaMateria(usuarioLogado, materia, "remover aula");
        validarAulaDaMateria(materia, aula);
        aulaDao.remover(aula);
        materia.getAulas().remove(aula);
    }
}
