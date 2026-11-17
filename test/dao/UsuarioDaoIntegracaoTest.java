package dao;

import excecao.MindDebugException;
import excecao.RegraDeNegocioException;
import model.Curso;
import model.Estudante;
import model.Monitor;
import model.Perfil;
import model.Professor;
import model.Usuario;

import java.util.ArrayList;

/**
 * Teste de integracao: precisa do MySQL ativo e grava no banco configurado em util.Conexao.
 * Os usuarios do teste tem login comecando com TESTE_ e sao apagados no fim, mesmo se o teste falhar.
 */
public class UsuarioDaoIntegracaoTest {
    private static final String[] LOGINS_DE_TESTE = {"TESTE_prof", "TESTE_monitor", "TESTE_aluno", "TESTE_falha"};

    public static void main(String[] args) {
        BancoDados banco = new BancoDao().carregar(); // garante que as tabelas existem
        Curso curso = banco.getCursos().isEmpty() ? null : banco.getCursos().get(0);
        UsuarioDao dao = new UsuarioDao();

        apagarUsuariosDeTeste(dao);
        try {
            int totalAntes = dao.listar().size();
            inserirEBuscar(dao, curso);
            bloquearDadosRepetidos(dao);
            atualizarEDesativar(dao);
            listarPorPerfil(dao);
            naoGravarMetadeQuandoFalha(dao);
            verificar(dao.listar().size() == totalAntes + 3, "so os tres usuarios de teste foram gravados");
        } finally {
            apagarUsuariosDeTeste(dao);
        }
        verificar(dao.buscarPorLogin("TESTE_prof") == null, "usuarios de teste apagados no fim");
    }

    private static void inserirEBuscar(UsuarioDao dao, Curso curso) {
        Professor professor = new Professor("Professor de Teste", "TESTE_prof", "teste_prof@teste.local", "1234");
        professor.setMatricula("TESTE_SIAPE1");
        professor.setTitulacao("Mestre");
        professor.setAreaAtuacao("Computacao");
        dao.inserir(professor);
        verificar(professor.getId() > 0, "o id gerado pelo banco volta para o objeto");

        Usuario lido = dao.buscarPorLogin("TESTE_prof");
        verificar(lido != null, "professor encontrado pelo login");
        verificar(lido != professor, "a busca monta um objeto novo a partir do banco");
        verificar(lido.getId() == professor.getId(), "mesmo id depois de ler");
        verificar(lido.getPerfil() == Perfil.PROFESSOR, "perfil lido do banco");
        verificar("teste_prof@teste.local".equals(lido.getEmail()), "email lido do banco");
        verificar("TESTE_SIAPE1".equals(lido.getMatricula()), "matricula SIAPE lida do banco");
        verificar(lido.isAtivo() && !lido.isTrocaDeSenhaObrigatoria(), "valores padrao de ativo e troca de senha");
        ArrayList<Professor> professores = new ArrayList<>();
        lido.incluirEmProfessores(professores);
        verificar(professores.size() == 1 && "Mestre".equals(professores.get(0).getTitulacao())
                && "Computacao".equals(professores.get(0).getAreaAtuacao()), "titulacao e area lidas de professores");

        Monitor monitor = new Monitor("Monitor de Teste", "TESTE_monitor", "teste_monitor@teste.local", "1234", "Bolsista");
        dao.inserir(monitor);
        Usuario monitorLido = dao.buscarPorLogin("teste_monitor");
        verificar(monitorLido != null, "login encontrado sem diferenciar maiusculas");
        verificar("Bolsista".equals(monitorLido.getTipoMonitor()), "tipo lido de monitores");

        Estudante aluno = new Estudante("Aluno de Teste", "teste_aluno@teste.local", "TESTE_aluno", "1234", curso);
        dao.inserir(aluno);
        Usuario alunoLido = dao.buscarPorLogin("TESTE_aluno");
        verificar(alunoLido.getPerfil() == Perfil.ALUNO, "aluno gravado com o perfil certo");
        verificar("TESTE_aluno".equals(alunoLido.getMatricula()), "matricula do aluno lida do banco");
        if (curso == null) {
            verificar(alunoLido.getCurso() == null, "aluno sem curso");
        } else {
            verificar(alunoLido.getCurso() != null && alunoLido.getCurso().getId() == curso.getId()
                    && curso.getNome().equals(alunoLido.getCurso().getNome()), "curso lido de estudantes");
        }
    }

    private static void bloquearDadosRepetidos(UsuarioDao dao) {
        int antes = dao.listar().size();

        Professor loginRepetido = new Professor("Outro", "TESTE_prof", "teste_outro@teste.local", "1234");
        verificar(recusouPorRegra(dao, loginRepetido, "Login ja cadastrado."), "login repetido recusado");

        Professor emailRepetido = new Professor("Outro", "TESTE_outro", "teste_prof@teste.local", "1234");
        verificar(recusouPorRegra(dao, emailRepetido, "Email ja cadastrado."), "email repetido recusado");

        verificar(dao.listar().size() == antes, "nada gravado depois das recusas");
        verificar(loginRepetido.getId() == 0 && emailRepetido.getId() == 0, "objetos recusados ficam sem id");
    }

    private static boolean recusouPorRegra(UsuarioDao dao, Usuario usuario, String mensagem) {
        try {
            dao.inserir(usuario);
            return false;
        } catch (RegraDeNegocioException esperado) {
            return mensagem.equals(esperado.getMessage());
        }
    }

    private static void atualizarEDesativar(UsuarioDao dao) {
        Usuario professor = dao.buscarPorLogin("TESTE_prof");
        professor.setNome("Professor de Teste Atualizado");
        professor.redefinirSenha("4321");
        dao.atualizar(professor);

        Usuario lido = dao.buscarPorLogin("TESTE_prof");
        verificar("Professor de Teste Atualizado".equals(lido.getNome()), "nome atualizado");
        verificar(lido.isTrocaDeSenhaObrigatoria(), "troca de senha obrigatoria gravada");
        verificar(lido.autenticar("TESTE_prof", "4321"), "senha nova gravada");

        Usuario monitor = dao.buscarPorLogin("TESTE_monitor");
        monitor.setEmail("teste_prof@teste.local");
        boolean recusou = false;
        try {
            dao.atualizar(monitor);
        } catch (RegraDeNegocioException esperado) {
            recusou = true;
        }
        verificar(recusou, "atualizar para um email de outro usuario e recusado");
        verificar("teste_monitor@teste.local".equals(dao.buscarPorLogin("TESTE_monitor").getEmail()),
                "email do monitor continua o antigo");

        dao.desativar(lido);
        Usuario desativado = dao.buscarPorLogin("TESTE_prof");
        verificar(!desativado.isAtivo(), "desativado no banco");
        verificar(!desativado.autenticar("TESTE_prof", "4321"), "usuario desativado nao entra");
    }

    private static void listarPorPerfil(UsuarioDao dao) {
        ArrayList<Usuario> monitores = dao.listarPorPerfil(Perfil.MONITOR);
        boolean achou = false;
        for (Usuario usuario : monitores) {
            verificar(usuario.getPerfil() == Perfil.MONITOR, "a lista de monitores so tem monitores");
            if ("TESTE_monitor".equals(usuario.getLogin())) {
                achou = true;
            }
        }
        verificar(achou, "monitor de teste na lista por perfil");
        verificar(dao.listarPorPerfil(Perfil.ADMINISTRADOR).size() >= 1, "administrador listado pelo perfil");
    }

    private static void naoGravarMetadeQuandoFalha(UsuarioDao dao) {
        // O curso nao existe no banco: a linha de usuarios entra, a de estudantes e recusada pela chave estrangeira.
        Curso cursoInexistente = new Curso("Curso inexistente", "XX", "Teste", 100, "Ninguem");
        cursoInexistente.setId(999999);
        Estudante aluno = new Estudante("Aluno com falha", "teste_falha@teste.local", "TESTE_falha", "1234",
                cursoInexistente);

        boolean falhou = false;
        try {
            dao.inserir(aluno);
        } catch (MindDebugException esperado) {
            falhou = true;
        }
        verificar(falhou, "inserir aluno com curso inexistente falha");
        verificar(dao.buscarPorLogin("TESTE_falha") == null, "a linha de usuarios foi desfeita junto");
        verificar(aluno.getId() == 0, "o objeto nao recebe id quando a transacao falha");
    }

    private static void apagarUsuariosDeTeste(UsuarioDao dao) {
        for (String login : LOGINS_DE_TESTE) {
            Usuario usuario = dao.buscarPorLogin(login);
            if (usuario != null) {
                dao.remover(usuario);
            }
        }
    }

    private static void verificar(boolean condicao, String descricao) {
        if (!condicao) {
            throw new AssertionError("FALHOU: " + descricao);
        }
    }
}
