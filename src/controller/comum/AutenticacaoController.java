package controller.comum;

import controller.ControllerBase;
import dao.BancoDados;
import dao.UsuarioDao;
import excecao.AcessoNegadoException;
import excecao.RegraDeNegocioException;
import model.Usuario;

/**
 * Entrada no sistema e troca da propria senha, comuns aos quatro atores.
 * Usuario desativado nao entra. Quem tem troca de senha obrigatoria (RN02J) precisa
 * trocar a senha antes de usar o sistema; a tela consulta isTrocaDeSenhaObrigatoria().
 */
public class AutenticacaoController extends ControllerBase {
    private UsuarioDao usuarioDao = new UsuarioDao();

    public AutenticacaoController(BancoDados banco) {
        super(banco);
    }

    public Usuario fazerLogin(String login, String senha) {
        Usuario usuario = banco.buscarUsuarioPorLogin(login);
        if (usuario != null && usuario.autenticar(login, senha)) {
            return usuario;
        }
        throw new AcessoNegadoException("Login ou senha invalidos.");
    }

    public void alterarMinhaSenha(Usuario usuario, String novaSenha) {
        if (usuario == null) {
            throw new AcessoNegadoException("Usuario nao esta logado.");
        }
        String senha = validador.senha(novaSenha);
        if (senha.equals(usuario.getSenha())) {
            throw new RegraDeNegocioException("A nova senha deve ser diferente da atual.");
        }
        usuarioDao.atualizarSenha(usuario, senha, false);
        usuario.trocarSenha(senha);
    }
}
