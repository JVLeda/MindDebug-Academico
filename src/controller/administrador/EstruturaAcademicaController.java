package controller.administrador;

import controller.ControllerBase;
import dao.BancoDados;
import dao.CursoDao;
import dao.MateriaDao;
import dao.PeriodoDao;
import excecao.DadoInvalidoException;
import excecao.RegraDeNegocioException;
import model.Curso;
import model.Materia;
import model.Periodo;
import model.Professor;
import model.Usuario;

import java.util.ArrayList;

/** Cursos, periodos, disciplinas e o professor de cada disciplina, modulo do Administrador. */
public class EstruturaAcademicaController extends ControllerBase {
    private CursoDao cursoDao = new CursoDao();
    private PeriodoDao periodoDao = new PeriodoDao();
    private MateriaDao materiaDao = new MateriaDao();

    public EstruturaAcademicaController(BancoDados banco) {
        super(banco);
    }

    public ArrayList<Curso> listarCursos() {
        return banco.getCursos();
    }

    public ArrayList<Curso> listarCursosVisiveis(Usuario usuarioLogado) {
        return usuarioLogado.filtrarCursosVisiveis(banco.getCursos());
    }

    public Curso criarCurso(Usuario usuarioLogado, String nome, String sigla, String area, int cargaHoraria, String coordenador) {
        exigirGestaoDaEstrutura(usuarioLogado, "criar curso");
        String siglaValidada = validador.texto(sigla, "Sigla");
        validarSiglaLivre(siglaValidada);
        Curso curso = new Curso(
                validador.texto(nome, "Nome"),
                siglaValidada,
                validador.texto(area, "Area"),
                validador.positivo(cargaHoraria, "Carga horaria"),
                validador.texto(coordenador, "Coordenador")
        );
        cursoDao.inserir(curso);
        banco.adicionarCurso(curso);
        return curso;
    }

    public Periodo criarPeriodo(Usuario usuarioLogado, Curso curso, int numero, String nome) {
        exigirGestaoDaEstrutura(usuarioLogado, "criar periodo");
        if (curso == null) {
            throw new DadoInvalidoException("Selecione um curso.");
        }
        validador.positivo(numero, "Numero do periodo");
        if (buscarPeriodo(curso, numero) != null) {
            throw new RegraDeNegocioException("Este curso ja tem o periodo " + numero + ".");
        }
        Periodo periodo = new Periodo(numero, validador.texto(nome, "Nome do periodo"));
        periodoDao.inserir(curso, periodo);
        curso.adicionarPeriodo(periodo);
        return periodo;
    }

    public Materia criarMateria(Usuario usuarioLogado, Periodo periodo, String nome, Professor professor) {
        exigirGestaoDaEstrutura(usuarioLogado, "criar disciplina");
        if (periodo == null) {
            throw new DadoInvalidoException("Selecione um periodo.");
        }
        if (professor == null) {
            throw new DadoInvalidoException("Selecione um professor.");
        }
        String nomeValidado = validador.texto(nome, "Nome da disciplina");
        if (buscarMateria(periodo, nomeValidado) != null) {
            throw new RegraDeNegocioException("Este periodo ja tem uma disciplina com este nome.");
        }
        Materia materia = new Materia(nomeValidado, professor);
        materiaDao.inserir(periodo, materia);
        periodo.adicionarMateria(materia);
        return materia;
    }

    /** Disciplina com aulas, materiais ou mensagens nao e apagada; o banco recusa e avisa. */
    public void removerMateria(Usuario usuarioLogado, Periodo periodo, Materia materia) {
        exigirGestaoDaEstrutura(usuarioLogado, "remover disciplina");
        if (periodo == null || materia == null || !periodo.getMaterias().contains(materia)) {
            throw new DadoInvalidoException("Escolha uma disciplina.");
        }
        materiaDao.remover(materia);
        periodo.removerMateria(materia);
    }

    public void designarProfessorNaMateria(Usuario usuarioLogado, Materia materia, Professor professor) {
        exigirGestaoDaEstrutura(usuarioLogado, "designar professor");
        if (materia == null) {
            throw new DadoInvalidoException("Selecione uma disciplina.");
        }
        if (professor == null) {
            throw new DadoInvalidoException("Selecione um professor.");
        }
        materiaDao.definirProfessor(materia, professor);
    }

    public void removerProfessorDaMateria(Usuario usuarioLogado, Materia materia) {
        exigirGestaoDaEstrutura(usuarioLogado, "remover professor");
        if (materia == null) {
            throw new DadoInvalidoException("Selecione uma disciplina.");
        }
        materiaDao.definirProfessor(materia, null);
    }

    /** Curso com materias ou com alunos nao e excluido; os periodos vazios saem junto. */
    public void removerCurso(Usuario usuarioLogado, Curso curso) {
        exigirGestaoDaEstrutura(usuarioLogado, "remover curso");
        if (curso == null) {
            throw new DadoInvalidoException("Selecione um curso.");
        }
        for (Periodo periodo : curso.getPeriodos()) {
            if (!periodo.getMaterias().isEmpty()) {
                throw new RegraDeNegocioException("O curso tem materias cadastradas e nao pode ser excluido.");
            }
        }
        cursoDao.remover(curso);
        banco.removerCurso(curso);
    }

    /** Periodo com materias nao e excluido. */
    public void removerPeriodo(Usuario usuarioLogado, Curso curso, Periodo periodo) {
        exigirGestaoDaEstrutura(usuarioLogado, "remover periodo");
        if (curso == null || periodo == null || !curso.getPeriodos().contains(periodo)) {
            throw new DadoInvalidoException("Escolha um periodo do curso.");
        }
        if (!periodo.getMaterias().isEmpty()) {
            throw new RegraDeNegocioException("O periodo tem materias cadastradas e nao pode ser excluido.");
        }
        periodoDao.remover(periodo);
        curso.removerPeriodo(periodo);
    }

    private void validarSiglaLivre(String sigla) {
        for (Curso curso : banco.getCursos()) {
            if (curso.getSigla().equalsIgnoreCase(sigla)) {
                throw new RegraDeNegocioException("Ja existe um curso com esta sigla.");
            }
        }
    }

    private Periodo buscarPeriodo(Curso curso, int numero) {
        for (Periodo periodo : curso.getPeriodos()) {
            if (periodo.getNumero() == numero) {
                return periodo;
            }
        }
        return null;
    }

    private Materia buscarMateria(Periodo periodo, String nomeMateria) {
        for (Materia materia : periodo.getMaterias()) {
            if (materia.getNome().equalsIgnoreCase(nomeMateria)) {
                return materia;
            }
        }
        return null;
    }
}
