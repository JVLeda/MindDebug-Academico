package dao;

import excecao.RegraDeNegocioException;
import model.Curso;
import model.Estudante;
import model.Materia;
import model.Monitor;
import model.Periodo;
import model.Professor;
import model.Usuario;
import util.Validador;

import java.text.SimpleDateFormat;
import java.util.ArrayList;

/**
 * Teste de integracao de CursoDao, PeriodoDao, MateriaDao e ParticipanteDao.
 * Precisa do MySQL ativo. Usa o curso de sigla TESTE7 e usuarios com login TESTE7_,
 * apagados no fim mesmo se o teste falhar.
 */
public class EstruturaAcademicaDaoIntegracaoTest {
    private static final String SIGLA = "TESTE7";
    private static final String[] LOGINS = {"TESTE7_prof1", "TESTE7_prof2", "TESTE7_aluno", "TESTE7_monitor"};

    private static CursoDao cursoDao = new CursoDao();
    private static PeriodoDao periodoDao = new PeriodoDao();
    private static MateriaDao materiaDao = new MateriaDao();
    private static ParticipanteDao participanteDao = new ParticipanteDao();
    private static UsuarioDao usuarioDao = new UsuarioDao();

    private static Professor professor1;
    private static Professor professor2;
    private static Estudante aluno;
    private static Monitor monitor;

    public static void main(String[] args) {
        new BancoDao().carregar(); // garante que as tabelas existem
        limpar();
        try {
            criarUsuarios();
            Curso curso = cursoECursoRepetido();
            Periodo periodo = periodos(curso);
            Materia materia = materias(curso, periodo);
            trocaDeProfessor(periodo, materia);
            enturmacao(materia);
            exclusoes(curso, periodo, materia);
        } finally {
            limpar();
        }
        verificar(cursoDao.buscarPorSigla(SIGLA) == null, "curso de teste apagado no fim");
    }

    private static void criarUsuarios() {
        professor1 = new Professor("Professor Um", "TESTE7_prof1", "teste7_prof1@teste.local", "1234");
        professor2 = new Professor("Professor Dois", "TESTE7_prof2", "teste7_prof2@teste.local", "1234");
        aluno = new Estudante("Aluno de Teste", "teste7_aluno@teste.local", "TESTE7_aluno", "1234", null);
        monitor = new Monitor("Monitor de Teste", "TESTE7_monitor", "teste7_monitor@teste.local", "1234", "Bolsista");
        usuarioDao.inserir(professor1);
        usuarioDao.inserir(professor2);
        usuarioDao.inserir(aluno);
        usuarioDao.inserir(monitor);
    }

    private static Curso cursoECursoRepetido() {
        Curso curso = new Curso("Curso de Teste", SIGLA, "Teste", 3000, "Coordenacao de Teste");
        cursoDao.inserir(curso);
        verificar(curso.getId() > 0, "curso recebe o id gerado pelo banco");

        Curso lido = cursoDao.buscarPorSigla(SIGLA);
        verificar(lido != null && lido.getId() == curso.getId(), "curso encontrado pela sigla");
        verificar("Curso de Teste".equals(lido.getNome()) && lido.getCargaHoraria() == 3000
                && lido.getNivel() == curso.getNivel(), "dados do curso lidos do banco");

        Curso repetido = new Curso("Outro curso", SIGLA, "Teste", 100, "Outra coordenacao");
        verificar(recusou(repetido), "sigla repetida recusada");
        verificar(repetido.getId() == 0, "curso recusado fica sem id");
        return curso;
    }

    private static boolean recusou(Curso curso) {
        try {
            cursoDao.inserir(curso);
            return false;
        } catch (RegraDeNegocioException esperado) {
            return true;
        }
    }

    private static Periodo periodos(Curso curso) {
        Validador validador = new Validador();
        Periodo primeiro = new Periodo(1, "Primeiro periodo de teste");
        primeiro.setAno(2026);
        primeiro.setSemestre(1);
        primeiro.definirDatas(validador.data("02/03/2026", "Inicio"), validador.data("10/07/2026", "Fim"));
        periodoDao.inserir(curso, primeiro);
        Periodo segundo = new Periodo(2, "Segundo periodo de teste");
        periodoDao.inserir(curso, segundo);
        verificar(primeiro.getId() > 0 && segundo.getId() > primeiro.getId(), "periodos recebem ids gerados");

        boolean recusou = false;
        try {
            periodoDao.inserir(curso, new Periodo(1, "Periodo repetido"));
        } catch (RegraDeNegocioException esperado) {
            recusou = true;
        }
        verificar(recusou, "numero de periodo repetido no curso recusado");

        ArrayList<Periodo> lidos = periodoDao.listarDoCurso(curso);
        verificar(lidos.size() == 2, "dois periodos no curso: " + lidos.size());
        Periodo lido = lidos.get(0);
        SimpleDateFormat formato = new SimpleDateFormat("dd/MM/yyyy");
        verificar(lido.getNumero() == 1 && "2026.1".equals(lido.getAnoSemestre()), "numero, ano e semestre lidos");
        verificar("02/03/2026".equals(formato.format(lido.getDataInicio()))
                && "10/07/2026".equals(formato.format(lido.getDataFim())), "datas do periodo lidas");
        verificar(lidos.get(1).getDataInicio() == null && lidos.get(1).getAno() == 0, "periodo sem datas fica sem datas");
        return primeiro;
    }

    private static Materia materias(Curso curso, Periodo periodo) {
        Materia materia = new Materia("Materia de Teste", professor1);
        materia.setCodigo("TESTE7-01");
        materia.setCargaHoraria(60);
        materiaDao.inserir(periodo, materia);
        verificar(materia.getId() > 0, "materia recebe o id gerado");

        Materia nomeRepetido = new Materia("Materia de Teste", null);
        verificar(recusou(periodo, nomeRepetido), "materia com nome repetido no periodo recusada");

        Materia codigoRepetido = new Materia("Outra materia", null);
        codigoRepetido.setCodigo("TESTE7-01");
        verificar(recusou(periodo, codigoRepetido), "codigo de materia repetido recusado");

        Periodo segundo = periodoDao.listarDoCurso(curso).get(1);
        materiaDao.inserir(segundo, new Materia("Materia de Teste", null));
        verificar(materiaDao.listarDoPeriodo(segundo).size() == 1, "o mesmo nome e aceito em outro periodo");

        ArrayList<Materia> lidas = materiaDao.listarDoPeriodo(periodo);
        verificar(lidas.size() == 1, "uma materia no periodo");
        Materia lida = lidas.get(0);
        verificar("TESTE7-01".equals(lida.getCodigo()) && lida.getCargaHoraria() == 60, "codigo e carga lidos");
        verificar(lida.getProfessor() != null && lida.getProfessor().getId() == professor1.getId(),
                "professor da materia lido do banco");
        return materia;
    }

    private static boolean recusou(Periodo periodo, Materia materia) {
        try {
            materiaDao.inserir(periodo, materia);
            return false;
        } catch (RegraDeNegocioException esperado) {
            return materia.getId() == 0;
        }
    }

    private static void trocaDeProfessor(Periodo periodo, Materia materia) {
        materiaDao.definirProfessor(materia, professor2);
        verificar(materia.getProfessor() == professor2, "professor novo no objeto");
        verificar(professorLido(periodo) == professor2.getId(), "so o professor novo fica no banco");

        materiaDao.definirProfessor(materia, null);
        verificar(materia.getProfessor() == null && professorLido(periodo) == 0, "materia sem professor");

        materiaDao.definirProfessor(materia, professor1);
        verificar(professorLido(periodo) == professor1.getId(), "professor designado de novo");
    }

    private static int professorLido(Periodo periodo) {
        Materia lida = materiaDao.listarDoPeriodo(periodo).get(0);
        if (lida.getProfessor() == null) {
            return 0;
        }
        return lida.getProfessor().getId();
    }

    private static void enturmacao(Materia materia) {
        participanteDao.adicionar(materia, aluno);
        participanteDao.adicionar(materia, monitor);

        boolean recusou = false;
        try {
            participanteDao.adicionar(materia, aluno);
        } catch (RegraDeNegocioException esperado) {
            recusou = true;
        }
        verificar(recusou, "o mesmo aluno nao entra duas vezes na materia");

        ArrayList<Usuario> participantes = participanteDao.listarDaMateria(materia);
        verificar(participantes.size() == 2, "dois participantes: " + participantes.size());
        verificar(participantes.get(0).getId() == aluno.getId() && participantes.get(1).getId() == monitor.getId(),
                "aluno e monitor enturmados");

        participanteDao.remover(materia, monitor);
        participantes = participanteDao.listarDaMateria(materia);
        verificar(participantes.size() == 1 && participantes.get(0).getId() == aluno.getId(), "monitor retirado");
    }

    private static void exclusoes(Curso curso, Periodo periodo, Materia materia) {
        boolean recusou = false;
        try {
            cursoDao.remover(curso);
        } catch (RegraDeNegocioException esperado) {
            recusou = true;
        }
        verificar(recusou, "curso com materias nao e excluido");
        verificar(cursoDao.buscarPorSigla(SIGLA) != null && periodoDao.listarDoCurso(curso).size() == 2,
                "curso e periodos continuam no banco");

        recusou = false;
        try {
            periodoDao.remover(periodo);
        } catch (RegraDeNegocioException esperado) {
            recusou = true;
        }
        verificar(recusou, "periodo com materias nao e excluido");

        materiaDao.remover(materia);
        verificar(materiaDao.listarDoPeriodo(periodo).isEmpty(), "materia excluida, junto com os participantes");
        Periodo segundo = periodoDao.listarDoCurso(curso).get(1);
        materiaDao.remover(materiaDao.listarDoPeriodo(segundo).get(0));

        cursoDao.remover(curso);
        verificar(cursoDao.buscarPorSigla(SIGLA) == null, "curso sem materias e excluido");
        verificar(periodoDao.listarDoCurso(curso).isEmpty(), "periodos vazios excluidos junto com o curso");
    }

    private static void limpar() {
        Curso curso = cursoDao.buscarPorSigla(SIGLA);
        if (curso != null) {
            for (Periodo periodo : periodoDao.listarDoCurso(curso)) {
                for (Materia materia : materiaDao.listarDoPeriodo(periodo)) {
                    materiaDao.remover(materia);
                }
            }
            cursoDao.remover(curso);
        }
        for (String login : LOGINS) {
            Usuario usuario = usuarioDao.buscarPorLogin(login);
            if (usuario != null) {
                usuarioDao.remover(usuario);
            }
        }
    }

    private static void verificar(boolean condicao, String descricao) {
        if (!condicao) {
            throw new AssertionError("FALHOU: " + descricao);
        }
    }
}
