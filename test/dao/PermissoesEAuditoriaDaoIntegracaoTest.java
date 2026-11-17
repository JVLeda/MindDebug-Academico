package dao;

import excecao.MindDebugException;
import model.Estudante;
import model.LogAuditoria;
import model.MatrizPermissoes;
import model.Perfil;
import model.Permissao;
import model.Usuario;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

/**
 * Teste de integracao de PerfilDao, PermissaoDao e LogAuditoriaDao. Precisa do MySQL ativo.
 * A matriz de permissoes do banco e lida no inicio e regravada igual no fim.
 * O usuario TESTE10_aluno e os registros de log dele sao apagados no fim.
 */
public class PermissoesEAuditoriaDaoIntegracaoTest {
    private static final String LOGIN = "TESTE10_aluno";

    private static PerfilDao perfilDao = new PerfilDao();
    private static PermissaoDao permissaoDao = new PermissaoDao();
    private static LogAuditoriaDao logDao = new LogAuditoriaDao();
    private static UsuarioDao usuarioDao = new UsuarioDao();

    /** Matriz com defeito: repete uma permissao do aluno, o que a chave primaria da tabela recusa. */
    private static class MatrizComPermissaoRepetida extends MatrizPermissoes {
        @Override
        public ArrayList<Permissao> listar(Perfil perfil) {
            ArrayList<Permissao> permissoes = super.listar(perfil);
            if (perfil == Perfil.ALUNO) {
                permissoes.add(Permissao.REGISTRAR_FEEDBACK);
            }
            return permissoes;
        }
    }

    public static void main(String[] args) {
        new BancoDao().carregar(); // garante que as tabelas e os dados fixos existem
        MatrizPermissoes original = permissaoDao.carregarMatriz();
        limpar();
        try {
            perfis();
            matrizPadraoEAlteracoes();
            restaurarRegravaTudoOuNada();
            auditoria();
        } finally {
            limpar();
            permissaoDao.salvarMatriz(original);
        }
        verificar(mesmasPermissoes(permissaoDao.carregarMatriz(), original), "matriz original regravada no fim");
    }

    private static void perfis() {
        ArrayList<Perfil> perfis = perfilDao.listar();
        verificar(perfis.size() == 4 && perfis.get(0) == Perfil.ADMINISTRADOR && perfis.get(3) == Perfil.ALUNO,
                "os quatro perfis lidos do banco");

        HashMap<Perfil, Integer> antes = perfilDao.contarUsuariosPorPerfil();
        verificar(antes.size() == 4, "todos os perfis aparecem na contagem, mesmo sem usuarios");
        usuarioDao.inserir(new Estudante("Aluno de Teste", "teste10_aluno@teste.local", LOGIN, "1234", null));
        HashMap<Perfil, Integer> depois = perfilDao.contarUsuariosPorPerfil();
        verificar(depois.get(Perfil.ALUNO) == antes.get(Perfil.ALUNO) + 1
                && depois.get(Perfil.PROFESSOR).equals(antes.get(Perfil.PROFESSOR)), "contagem com GROUP BY por perfil");
    }

    private static void matrizPadraoEAlteracoes() {
        permissaoDao.restaurarPadrao();
        MatrizPermissoes lida = permissaoDao.carregarMatriz();
        verificar(mesmasPermissoes(lida, new MatrizPermissoes()), "matriz padrao gravada e lida");
        verificar(total(lida) == 12, "12 permissoes no padrao: " + total(lida));

        permissaoDao.revogar(Perfil.PROFESSOR, Permissao.MODERAR_CONTEUDO);
        permissaoDao.conceder(Perfil.MONITOR, Permissao.RESPONDER_QUIZ);
        permissaoDao.conceder(Perfil.MONITOR, Permissao.RESPONDER_QUIZ);
        lida = permissaoDao.carregarMatriz();
        verificar(!lida.permite(Perfil.PROFESSOR, Permissao.MODERAR_CONTEUDO), "permissao revogada");
        verificar(lida.permite(Perfil.MONITOR, Permissao.RESPONDER_QUIZ), "permissao concedida");
        verificar(total(lida) == 12, "conceder duas vezes nao repete a permissao: " + total(lida));
    }

    private static void restaurarRegravaTudoOuNada() {
        MatrizPermissoes antes = permissaoDao.carregarMatriz();
        boolean falhou = false;
        try {
            permissaoDao.salvarMatriz(new MatrizComPermissaoRepetida());
        } catch (MindDebugException esperado) {
            falhou = true;
        }
        verificar(falhou, "matriz com permissao repetida e recusada pelo banco");
        verificar(mesmasPermissoes(permissaoDao.carregarMatriz(), antes),
                "depois da falha, a matriz anterior continua inteira (nem apagada, nem pela metade)");

        permissaoDao.restaurarPadrao();
        verificar(mesmasPermissoes(permissaoDao.carregarMatriz(), new MatrizPermissoes()), "padrao restaurado");
    }

    private static void auditoria() {
        Usuario aluno = usuarioDao.buscarPorLogin(LOGIN);
        LogAuditoria log = new LogAuditoria(aluno, "Teste de auditoria", "Registro criado pelo teste");
        logDao.registrar(log);
        verificar(log.getId() > 0, "registro de log recebe o id gerado");

        ArrayList<LogAuditoria> doUsuario = logDao.listarDoUsuario(aluno);
        verificar(doUsuario.size() == 1, "um registro do usuario");
        LogAuditoria lido = doUsuario.get(0);
        SimpleDateFormat formato = new SimpleDateFormat("dd/MM/yyyy");
        verificar(lido.getUsuario().getId() == aluno.getId(), "o log grava o usuario");
        verificar("Teste de auditoria".equals(lido.getAcao()) && "Registro criado pelo teste".equals(lido.getDetalhes()),
                "o log grava a acao e os detalhes");
        verificar(formato.format(new Date()).equals(formato.format(lido.getData())) && log.getHora().equals(lido.getHora()),
                "o log grava a data e a hora");

        LogAuditoria segundo = new LogAuditoria(aluno, "Segunda acao", "");
        logDao.registrar(segundo);
        verificar(logDao.listar().get(0).getId() == segundo.getId(), "a listagem comeca pelo registro mais novo");
    }

    private static void limpar() {
        Usuario aluno = usuarioDao.buscarPorLogin(LOGIN);
        if (aluno != null) {
            logDao.removerDoUsuario(aluno);
            usuarioDao.remover(aluno);
        }
    }

    private static boolean mesmasPermissoes(MatrizPermissoes uma, MatrizPermissoes outra) {
        for (Perfil perfil : Perfil.values()) {
            for (Permissao permissao : Permissao.values()) {
                if (uma.permite(perfil, permissao) != outra.permite(perfil, permissao)) {
                    return false;
                }
            }
        }
        return true;
    }

    private static int total(MatrizPermissoes matriz) {
        int quantidade = 0;
        for (Perfil perfil : Perfil.values()) {
            quantidade += matriz.listar(perfil).size();
        }
        return quantidade;
    }

    private static void verificar(boolean condicao, String descricao) {
        if (!condicao) {
            throw new AssertionError("FALHOU: " + descricao);
        }
    }
}
