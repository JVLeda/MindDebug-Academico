package dao;

import controller.administrador.EstruturaAcademicaController;
import controller.administrador.UsuarioController;
import controller.comum.AutenticacaoController;
import excecao.AcessoNegadoException;
import excecao.RegraDeNegocioException;
import model.Curso;
import model.Materia;
import model.Monitor;
import model.Periodo;
import model.Professor;
import model.Usuario;

/**
 * Testes de aceite da fase 1, no banco real: e-mail repetido bloqueado, curso com materias nao exclui,
 * um professor por materia, troca de senha obrigatoria (RN02J) e usuario desativado sem acesso.
 * Cada cadastro e conferido depois de carregar o banco de novo, como se o programa fosse fechado e reaberto.
 * Usa o curso de sigla TESTE13 e usuarios com login TESTE13_, apagados no fim.
 */
public class AceiteFase1IntegracaoTest {
    private static final String SIGLA = "TESTE13";
    private static final String[] LOGINS = {"TESTE13_prof1", "TESTE13_prof2", "TESTE13_monitor"};

    public static void main(String[] args) {
        limpar();
        try {
            BancoDados banco = new BancoDao().carregar();
            Usuario admin = banco.buscarUsuarioPorLogin("admin");
            UsuarioController usuarios = new UsuarioController(banco);
            EstruturaAcademicaController estrutura = new EstruturaAcademicaController(banco);
            AutenticacaoController autenticacao = new AutenticacaoController(banco);

            Professor professor1 = usuarios.cadastrarProfessor(admin, "Professor Um", "TESTE13_prof1",
                    "teste13_prof1@teste.local", "1234");
            Professor professor2 = usuarios.cadastrarProfessor(admin, "Professor Dois", "TESTE13_prof2",
                    "teste13_prof2@teste.local", "1234");
            verificar(professor1.isTrocaDeSenhaObrigatoria(), "conta criada pelo administrador exige troca de senha");

            emailRepetidoBloqueado(usuarios, admin);

            Curso curso = estrutura.criarCurso(admin, "Curso de Aceite", SIGLA, "Teste", 3000, "Coordenacao");
            Periodo comMateria = estrutura.criarPeriodo(admin, curso, 1, "Periodo com materia");
            Periodo vazio = estrutura.criarPeriodo(admin, curso, 2, "Periodo vazio");
            Materia materia = estrutura.criarMateria(admin, comMateria, "Materia de Aceite", professor1);
            cursoComMateriasNaoExclui(estrutura, admin, curso);
            estrutura.removerPeriodo(admin, curso, vazio);

            estrutura.designarProfessorNaMateria(admin, materia, professor2);
            verificar(materia.getProfessor() == professor2, "o professor novo substitui o anterior");

            verificar(autenticacao.fazerLogin("TESTE13_prof1", "1234") == professor1, "primeiro acesso com a senha inicial");
            autenticacao.alterarMinhaSenha(professor1, "4321");
            autenticacao.alterarMinhaSenha(professor2, "8765");
            usuarios.redefinirSenhaDeUsuario(admin, professor2, "5678");

            Monitor monitor = usuarios.cadastrarMonitor(admin, "Monitor de Aceite", "TESTE13_monitor",
                    "teste13_monitor@teste.local", "1234", "Bolsista");
            usuarios.desativarUsuario(admin, monitor);

            conferirDepoisDeReabrir(professor1, professor2);
        } finally {
            limpar();
        }
        verificar(new CursoDao().buscarPorSigla(SIGLA) == null, "dados de teste apagados no fim");
    }

    private static void emailRepetidoBloqueado(UsuarioController usuarios, Usuario admin) {
        boolean recusou = false;
        try {
            usuarios.cadastrarMonitor(admin, "Monitor Repetido", "TESTE13_monitor", "TESTE13_PROF1@teste.local",
                    "1234", "Bolsista");
        } catch (RegraDeNegocioException esperado) {
            recusou = true;
        }
        verificar(recusou, "controller bloqueia e-mail repetido, sem diferenciar maiusculas");

        recusou = false;
        try {
            new UsuarioDao().inserir(new Monitor("Monitor Repetido", "TESTE13_monitor", "teste13_prof1@teste.local",
                    "1234", "Bolsista"));
        } catch (RegraDeNegocioException esperado) {
            recusou = true;
        }
        verificar(recusou, "banco bloqueia e-mail repetido mesmo sem passar pelo controller");
        verificar(new UsuarioDao().buscarPorLogin("TESTE13_monitor") == null, "nenhum usuario gravado com e-mail repetido");
    }

    private static void cursoComMateriasNaoExclui(EstruturaAcademicaController estrutura, Usuario admin, Curso curso) {
        boolean recusou = false;
        try {
            estrutura.removerCurso(admin, curso);
        } catch (RegraDeNegocioException esperado) {
            recusou = true;
        }
        verificar(recusou, "controller recusa excluir curso com materias");

        recusou = false;
        try {
            new CursoDao().remover(curso);
        } catch (RegraDeNegocioException esperado) {
            recusou = true;
        }
        verificar(recusou, "banco recusa excluir curso com materias mesmo sem passar pelo controller");
    }

    private static void conferirDepoisDeReabrir(Professor professor1, Professor professor2) {
        BancoDados banco = new BancoDao().carregar();
        Curso curso = null;
        for (Curso candidato : banco.getCursos()) {
            if (SIGLA.equals(candidato.getSigla())) {
                curso = candidato;
            }
        }
        verificar(curso != null, "curso continua gravado depois de reabrir");
        verificar(curso.getPeriodos().size() == 1, "periodo vazio removido, periodo com materia mantido");
        Materia materia = curso.getPeriodos().get(0).getMaterias().get(0);
        verificar(materia.getProfessor() != null && materia.getProfessor().getId() == professor2.getId(),
                "a materia tem um unico professor, o designado por ultimo");

        Usuario reaberto1 = banco.buscarUsuarioPorLogin("TESTE13_prof1");
        verificar(reaberto1.autenticar("TESTE13_prof1", "4321") && !reaberto1.isTrocaDeSenhaObrigatoria(),
                "senha trocada no primeiro acesso e gravada");
        Usuario reaberto2 = banco.buscarUsuarioPorLogin("TESTE13_prof2");
        verificar(reaberto2.autenticar("TESTE13_prof2", "5678") && reaberto2.isTrocaDeSenhaObrigatoria(),
                "senha redefinida pelo administrador exige nova troca");

        Usuario monitor = banco.buscarUsuarioPorLogin("TESTE13_monitor");
        verificar(monitor != null && !monitor.isAtivo(), "monitor desativado continua gravado");
        boolean negou = false;
        try {
            new AutenticacaoController(banco).fazerLogin("TESTE13_monitor", "1234");
        } catch (AcessoNegadoException esperado) {
            negou = true;
        }
        verificar(negou, "usuario desativado nao entra no sistema");
    }

    private static void limpar() {
        CursoDao cursoDao = new CursoDao();
        UsuarioDao usuarioDao = new UsuarioDao();
        Curso curso = cursoDao.buscarPorSigla(SIGLA);
        if (curso != null) {
            for (Periodo periodo : new PeriodoDao().listarDoCurso(curso)) {
                for (Materia materia : new MateriaDao().listarDoPeriodo(periodo)) {
                    new MateriaDao().remover(materia);
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
