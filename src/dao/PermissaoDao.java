package dao;

import excecao.PersistenciaException;
import model.MatrizPermissoes;
import model.Perfil;
import model.Permissao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Acesso a matriz de permissoes (tabela perfis_permissoes). As permissoes em si sao fixas,
 * como os perfis; o que muda e quais permissoes cada perfil tem.
 */
public class PermissaoDao extends AcessoBanco {

    /** Monta a matriz com o que esta no banco. */
    public MatrizPermissoes carregarMatriz() {
        MatrizPermissoes matriz = new MatrizPermissoes();
        for (Perfil perfil : Perfil.values()) {
            for (Permissao permissao : matriz.listar(perfil)) {
                matriz.revogar(perfil, permissao);
            }
        }

        Connection conexao = abrirConexao();
        try {
            PreparedStatement consulta = conexao.prepareStatement(
                    "SELECT perfil_id, permissao_id FROM perfis_permissoes ORDER BY perfil_id, permissao_id");
            ResultSet resultado = consulta.executeQuery();
            while (resultado.next()) {
                Perfil perfil = Perfil.buscarPorCodigo(resultado.getInt("perfil_id"));
                Permissao permissao = Permissao.buscarPorCodigo(resultado.getInt("permissao_id"));
                if (perfil != null && permissao != null) {
                    matriz.conceder(perfil, permissao);
                }
            }
            resultado.close();
            consulta.close();
            return matriz;
        } catch (SQLException erro) {
            throw new PersistenciaException("Erro ao carregar as permissoes: " + erro.getMessage());
        } finally {
            fechar(conexao);
        }
    }

    public void conceder(Perfil perfil, Permissao permissao) {
        Connection conexao = abrirConexao();
        try {
            PreparedStatement consulta = conexao.prepareStatement(
                    "SELECT COUNT(*) FROM perfis_permissoes WHERE perfil_id = ? AND permissao_id = ?");
            consulta.setInt(1, perfil.getCodigo());
            consulta.setInt(2, permissao.getCodigo());
            if (contar(consulta) == 0) {
                inserir(conexao, perfil, permissao);
            }
        } catch (SQLException erro) {
            throw new PersistenciaException("Erro ao conceder a permissao: " + erro.getMessage());
        } finally {
            fechar(conexao);
        }
    }

    public void revogar(Perfil perfil, Permissao permissao) {
        Connection conexao = abrirConexao();
        try {
            PreparedStatement comando = conexao.prepareStatement(
                    "DELETE FROM perfis_permissoes WHERE perfil_id = ? AND permissao_id = ?");
            comando.setInt(1, perfil.getCodigo());
            comando.setInt(2, permissao.getCodigo());
            comando.executeUpdate();
            comando.close();
        } catch (SQLException erro) {
            throw new PersistenciaException("Erro ao revogar a permissao: " + erro.getMessage());
        } finally {
            fechar(conexao);
        }
    }

    /**
     * Regrava a matriz inteira numa transacao: apaga tudo e insere de novo.
     * Se algum INSERT falhar, a matriz antiga continua no banco.
     */
    public void salvarMatriz(MatrizPermissoes matriz) {
        Connection conexao = abrirConexao();
        boolean gravou = false;
        try {
            conexao.setAutoCommit(false);
            PreparedStatement comando = conexao.prepareStatement("DELETE FROM perfis_permissoes");
            comando.executeUpdate();
            comando.close();

            for (Perfil perfil : Perfil.values()) {
                for (Permissao permissao : matriz.listar(perfil)) {
                    inserir(conexao, perfil, permissao);
                }
            }
            conexao.commit();
            gravou = true;
        } catch (SQLException erro) {
            throw new PersistenciaException("Erro ao gravar as permissoes. Nada foi alterado. Detalhe: "
                    + erro.getMessage());
        } finally {
            if (!gravou) {
                desfazer(conexao);
            }
            fechar(conexao);
        }
    }

    /** Volta a matriz do banco ao padrao do sistema (o mesmo de new MatrizPermissoes()). */
    public void restaurarPadrao() {
        salvarMatriz(new MatrizPermissoes());
    }

    private void inserir(Connection conexao, Perfil perfil, Permissao permissao) throws SQLException {
        PreparedStatement comando = conexao.prepareStatement(
                "INSERT INTO perfis_permissoes (perfil_id, permissao_id) VALUES (?, ?)");
        comando.setInt(1, perfil.getCodigo());
        comando.setInt(2, permissao.getCodigo());
        comando.executeUpdate();
        comando.close();
    }
}
