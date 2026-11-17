package dao;

import excecao.PersistenciaException;
import model.LogAuditoria;
import model.Usuario;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Acesso a tabela log_auditoria: quem fez cada acao administrativa, quando e o que foi feito.
 * O log so recebe registros novos; nenhuma tela altera ou apaga um registro.
 */
public class LogAuditoriaDao extends AcessoBanco {
    private static final String CONSULTA =
            "SELECT id, usuario_id, data_registro, hora_registro, acao, detalhes FROM log_auditoria ";

    public void registrar(LogAuditoria log) {
        Connection conexao = abrirConexao();
        boolean gravou = false;
        try {
            conexao.setAutoCommit(false);
            PreparedStatement comando = conexao.prepareStatement(
                    "INSERT INTO log_auditoria (usuario_id, data_registro, hora_registro, acao, detalhes) "
                            + "VALUES (?, ?, ?, ?, ?)");
            comando.setInt(1, log.getUsuario().getId());
            definirData(comando, 2, log.getData());
            comando.setString(3, log.getHora());
            comando.setString(4, log.getAcao());
            definirTextoOuNulo(comando, 5, log.getDetalhes());
            comando.executeUpdate();
            comando.close();
            int idGerado = lerUltimoId(conexao, "log_auditoria");

            conexao.commit();
            gravou = true;
            log.setId(idGerado);
        } catch (SQLException erro) {
            throw new PersistenciaException("Erro ao gravar o log de auditoria: " + erro.getMessage());
        } finally {
            if (!gravou) {
                desfazer(conexao);
            }
            fechar(conexao);
        }
    }

    /** Todos os registros, do mais novo para o mais antigo. */
    public ArrayList<LogAuditoria> listar() {
        return consultar(CONSULTA + "ORDER BY id DESC", 0);
    }

    /** Registros de um usuario, do mais novo para o mais antigo. */
    public ArrayList<LogAuditoria> listarDoUsuario(Usuario usuario) {
        return consultar(CONSULTA + "WHERE usuario_id = ? ORDER BY id DESC", usuario.getId());
    }

    private ArrayList<LogAuditoria> consultar(String sql, int usuarioId) {
        HashMap<Integer, Usuario> usuarios = new UsuarioDao().usuariosPorId();
        Connection conexao = abrirConexao();
        try {
            PreparedStatement consulta = conexao.prepareStatement(sql);
            if (usuarioId > 0) {
                consulta.setInt(1, usuarioId);
            }
            ResultSet resultado = consulta.executeQuery();
            ArrayList<LogAuditoria> registros = new ArrayList<>();
            while (resultado.next()) {
                LogAuditoria log = new LogAuditoria(
                        usuarios.get(resultado.getInt("usuario_id")),
                        resultado.getDate("data_registro"),
                        resultado.getString("hora_registro"),
                        resultado.getString("acao"),
                        textoOuVazio(resultado.getString("detalhes"))
                );
                log.setId(resultado.getInt("id"));
                registros.add(log);
            }
            resultado.close();
            consulta.close();
            return registros;
        } catch (SQLException erro) {
            throw new PersistenciaException("Erro ao consultar o log de auditoria: " + erro.getMessage());
        } finally {
            fechar(conexao);
        }
    }

    /**
     * Fica restrito ao pacote dao, sem uso pelas telas: o log nao e apagado pelo sistema.
     * Serve para os testes de integracao limparem o que criaram.
     */
    void removerDoUsuario(Usuario usuario) {
        Connection conexao = abrirConexao();
        try {
            PreparedStatement comando = conexao.prepareStatement("DELETE FROM log_auditoria WHERE usuario_id = ?");
            comando.setInt(1, usuario.getId());
            comando.executeUpdate();
            comando.close();
        } catch (SQLException erro) {
            throw new PersistenciaException("Erro ao apagar: " + erro.getMessage());
        } finally {
            fechar(conexao);
        }
    }
}
