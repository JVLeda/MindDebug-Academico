package controller;

import dao.BancoDados;
import excecao.AcessoNegadoException;
import excecao.DadoInvalidoException;
import excecao.RegraDeNegocioException;
import model.Aula;
import model.Materia;
import model.Usuario;
import util.Validador;

/**
 * Base dos controllers por ator. Guarda os dados carregados do banco e as verificacoes
 * usadas por mais de um modulo. Cada controller filho recebe as acoes das telas de um modulo,
 * confere permissoes e regras, grava pelo DAO e so depois muda os objetos em memoria.
 */
public abstract class ControllerBase {
    protected BancoDados banco;
    protected Validador validador = new Validador();

    protected ControllerBase(BancoDados banco) {
        this.banco = banco;
    }

    protected void exigirGestaoDaEstrutura(Usuario usuario, String acao) {
        if (usuario == null || !usuario.podeGerenciarEstrutura()) {
            negarAcesso(acao);
        }
    }

    /** Aulas, quiz e grupos de estudo: so o professor da materia (RN01J). */
    protected void exigirProfessorDaMateria(Usuario usuario, Materia materia, String acao) {
        exigirDados(usuario, materia);
        if (!usuario.podeGerenciarMateria(materia)) {
            negarAcesso(acao);
        }
    }

    /** Monitores da materia: o professor da materia ou o administrador, que cuida da enturmacao (RF05J). */
    protected void exigirProfessorDaMateriaOuAdministrador(Usuario usuario, Materia materia, String acao) {
        exigirDados(usuario, materia);
        if (!usuario.podeGerenciarMateria(materia) && !usuario.podeGerenciarEstrutura()) {
            negarAcesso(acao);
        }
    }

    private void exigirDados(Usuario usuario, Materia materia) {
        if (usuario == null || materia == null) {
            throw new DadoInvalidoException("Dados invalidos.");
        }
    }

    protected void negarAcesso(String acao) {
        throw new AcessoNegadoException("Sem permissao para " + acao + ".");
    }

    protected void validarAulaDaMateria(Materia materia, Aula aula) {
        if (materia == null || aula == null || !materia.getAulas().contains(aula)) {
            throw new RegraDeNegocioException("A aula nao pertence a esta materia.");
        }
    }
}
