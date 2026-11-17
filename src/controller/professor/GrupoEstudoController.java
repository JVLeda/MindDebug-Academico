package controller.professor;

import controller.ControllerBase;
import dao.BancoDados;
import dao.GrupoEstudoDao;
import excecao.DadoInvalidoException;
import excecao.RegraDeNegocioException;
import model.Aula;
import model.Estudante;
import model.GrupoEstudo;
import model.Materia;
import model.Monitor;
import model.Usuario;

/** Grupos de estudo da disciplina, com alunos e monitores, modulo do Professor. */
public class GrupoEstudoController extends ControllerBase {
    private GrupoEstudoDao grupoEstudoDao = new GrupoEstudoDao();

    public GrupoEstudoController(BancoDados banco) {
        super(banco);
    }

    public GrupoEstudo criarGrupoEstudo(Usuario usuarioLogado, Materia materia, String nome, Aula aula, Monitor monitor) {
        exigirProfessorDaMateria(usuarioLogado, materia, "criar grupo de estudo");
        if (aula != null && !materia.getAulas().contains(aula)) {
            throw new RegraDeNegocioException("A aula escolhida nao pertence a esta materia.");
        }
        if (monitor != null && !materia.getMonitores().contains(monitor)) {
            throw new RegraDeNegocioException("O monitor escolhido nao esta cadastrado nesta materia.");
        }

        String nomeValidado = validador.texto(nome, "Nome do grupo");
        for (GrupoEstudo grupoExistente : materia.getGrupos()) {
            if (grupoExistente.getNome().equalsIgnoreCase(nomeValidado)) {
                throw new RegraDeNegocioException("Ja existe um grupo com este nome.");
            }
        }

        GrupoEstudo grupo = new GrupoEstudo(nomeValidado, aula, monitor);
        grupoEstudoDao.inserir(materia, grupo);
        materia.adicionarGrupo(grupo);
        return grupo;
    }

    public void adicionarAlunoAoGrupo(Usuario usuarioLogado, Materia materia, GrupoEstudo grupo, Estudante estudante) {
        exigirProfessorDaMateria(usuarioLogado, materia, "adicionar aluno ao grupo");
        validarGrupoEEstudante(materia, grupo, estudante);
        grupoEstudoDao.adicionarAluno(grupo, estudante);
        grupo.adicionarEstudante(estudante);
    }

    public void removerAlunoDoGrupo(Usuario usuarioLogado, Materia materia, GrupoEstudo grupo, Estudante estudante) {
        exigirProfessorDaMateria(usuarioLogado, materia, "remover aluno do grupo");
        validarGrupoEEstudante(materia, grupo, estudante);
        grupoEstudoDao.removerAluno(grupo, estudante);
        grupo.removerEstudante(estudante);
    }

    public void adicionarMonitorAoGrupo(Usuario usuarioLogado, Materia materia, GrupoEstudo grupo, Monitor monitor) {
        exigirProfessorDaMateria(usuarioLogado, materia, "adicionar monitor ao grupo");
        validarGrupoEMonitor(materia, grupo, monitor);
        grupoEstudoDao.adicionarMonitor(grupo, monitor);
        grupo.adicionarMonitor(monitor);
    }

    public void removerMonitorDoGrupo(Usuario usuarioLogado, Materia materia, GrupoEstudo grupo, Monitor monitor) {
        exigirProfessorDaMateria(usuarioLogado, materia, "remover monitor do grupo");
        validarGrupoEMonitor(materia, grupo, monitor);
        grupoEstudoDao.removerMonitor(grupo, monitor);
        grupo.removerMonitor(monitor);
    }

    public void removerGrupoEstudo(Usuario usuarioLogado, Materia materia, GrupoEstudo grupo) {
        exigirProfessorDaMateria(usuarioLogado, materia, "remover grupo de estudo");
        if (grupo == null || !materia.getGrupos().contains(grupo)) {
            throw new DadoInvalidoException("Selecione um grupo.");
        }
        grupoEstudoDao.remover(grupo);
        materia.removerGrupo(grupo);
    }

    private void validarGrupoEEstudante(Materia materia, GrupoEstudo grupo, Estudante estudante) {
        if (grupo == null) {
            throw new DadoInvalidoException("Selecione um grupo.");
        }
        if (estudante == null) {
            throw new DadoInvalidoException("Selecione um aluno.");
        }
        if (!materia.getGrupos().contains(grupo)) {
            throw new RegraDeNegocioException("O grupo nao pertence a esta materia.");
        }
        if (!materia.getEstudantes().contains(estudante)) {
            throw new RegraDeNegocioException("O aluno nao esta matriculado nesta materia.");
        }
    }

    private void validarGrupoEMonitor(Materia materia, GrupoEstudo grupo, Monitor monitor) {
        if (grupo == null) {
            throw new DadoInvalidoException("Selecione um grupo.");
        }
        if (monitor == null) {
            throw new DadoInvalidoException("Selecione um monitor.");
        }
        if (!materia.getGrupos().contains(grupo)) {
            throw new RegraDeNegocioException("O grupo nao pertence a esta materia.");
        }
        if (!materia.getMonitores().contains(monitor)) {
            throw new RegraDeNegocioException("O monitor nao esta cadastrado nesta materia.");
        }
    }
}
