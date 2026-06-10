package dao;

import model.Usuario;

import java.util.ArrayList;

public class UsuarioDao {
    private BancoDados banco;

    public UsuarioDao(BancoDados banco) {
        this.banco = banco;
    }

    public void cadastrar(Usuario usuario) {
        if (usuario.getId() == 0) {
            usuario.setId(banco.gerarId());
        }
        banco.getUsuarios().add(usuario);
    }

    public ArrayList<Usuario> listar() {
        return banco.getUsuarios();
    }

    public Usuario buscarPorLogin(String login) {
        for (Usuario usuario : banco.getUsuarios()) {
            if (usuario.getLogin().equalsIgnoreCase(login)) {
                return usuario;
            }
        }
        return null;
    }

    public void atualizar(Usuario usuarioAtualizado) {
        for (int i = 0; i < banco.getUsuarios().size(); i++) {
            Usuario usuario = banco.getUsuarios().get(i);
            if (usuario.getId() == usuarioAtualizado.getId()) {
                banco.getUsuarios().set(i, usuarioAtualizado);
                return;
            }
        }
    }

    public void remover(Usuario usuario) {
        banco.getUsuarios().remove(usuario);
    }
}
