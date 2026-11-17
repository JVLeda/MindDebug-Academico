package dao;

import excecao.PersistenciaException;
import model.Perfil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Acesso a tabela perfis. Os perfis sao fixos (os mesmos do enum Perfil) e sao gravados
 * uma vez, quando as tabelas sao criadas; por isso aqui so ha consultas.
 */
public class PerfilDao extends AcessoBanco {

    public ArrayList<Perfil> listar() {
        Connection conexao = abrirConexao();
        try {
            PreparedStatement consulta = conexao.prepareStatement("SELECT id FROM perfis ORDER BY id");
            ResultSet resultado = consulta.executeQuery();
            ArrayList<Perfil> perfis = new ArrayList<>();
            while (resultado.next()) {
                Perfil perfil = Perfil.buscarPorCodigo(resultado.getInt("id"));
                if (perfil != null) {
                    perfis.add(perfil);
                }
            }
            resultado.close();
            consulta.close();
            return perfis;
        } catch (SQLException erro) {
            throw new PersistenciaException("Erro ao consultar perfis: " + erro.getMessage());
        } finally {
            fechar(conexao);
        }
    }

    /** Quantos usuarios cada perfil tem. Perfil sem usuarios aparece com zero (LEFT JOIN). */
    public HashMap<Perfil, Integer> contarUsuariosPorPerfil() {
        Connection conexao = abrirConexao();
        try {
            PreparedStatement consulta = conexao.prepareStatement(
                    "SELECT p.id, COUNT(u.id) AS quantidade FROM perfis p "
                            + "LEFT JOIN usuarios u ON u.perfil_id = p.id GROUP BY p.id ORDER BY p.id");
            ResultSet resultado = consulta.executeQuery();
            HashMap<Perfil, Integer> quantidades = new HashMap<>();
            while (resultado.next()) {
                Perfil perfil = Perfil.buscarPorCodigo(resultado.getInt("id"));
                if (perfil != null) {
                    quantidades.put(perfil, resultado.getInt("quantidade"));
                }
            }
            resultado.close();
            consulta.close();
            return quantidades;
        } catch (SQLException erro) {
            throw new PersistenciaException("Erro ao contar usuarios por perfil: " + erro.getMessage());
        } finally {
            fechar(conexao);
        }
    }
}
