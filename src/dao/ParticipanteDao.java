package dao;

import excecao.PersistenciaException;
import excecao.RegraDeNegocioException;
import model.Materia;
import model.Usuario;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Acesso a tabela participantes: os alunos e monitores enturmados em cada materia.
 * O professor nao entra aqui; ele fica em materias.professor_id.
 */
public class ParticipanteDao extends AcessoBanco {

    public ArrayList<Usuario> listarDaMateria(Materia materia) {
        return new UsuarioDao().listarParticipantesDaMateria(materia.getId());
    }

    /** Usado pelo BancoDao: liga cada participante a sua materia. */
    void listar(Connection conexao, HashMap<Integer, Materia> materias, HashMap<Integer, Usuario> usuarios)
            throws SQLException {
        PreparedStatement consulta = conexao.prepareStatement("SELECT materia_id, usuario_id FROM participantes");
        ResultSet resultado = consulta.executeQuery();
        while (resultado.next()) {
            Materia materia = materias.get(resultado.getInt("materia_id"));
            Usuario usuario = usuarios.get(resultado.getInt("usuario_id"));
            if (materia != null && usuario != null) {
                materia.adicionarParticipante(usuario);
            }
        }
        resultado.close();
        consulta.close();
    }

    /** Enturma o usuario na materia. O mesmo usuario nao entra duas vezes. */
    public void adicionar(Materia materia, Usuario usuario) {
        Connection conexao = abrirConexao();
        try {
            PreparedStatement consulta = conexao.prepareStatement(
                    "SELECT COUNT(*) FROM participantes WHERE materia_id = ? AND usuario_id = ?");
            consulta.setInt(1, materia.getId());
            consulta.setInt(2, usuario.getId());
            if (contar(consulta) > 0) {
                throw new RegraDeNegocioException("O usuario ja participa desta materia.");
            }
            inserir(conexao, materia, usuario);
        } catch (SQLException erro) {
            throw new PersistenciaException("Erro ao enturmar o usuario: " + erro.getMessage());
        } finally {
            fechar(conexao);
        }
    }

    /** Tambem usado pelo BancoDao, dentro da transacao dele. */
    void inserir(Connection conexao, Materia materia, Usuario usuario) throws SQLException {
        PreparedStatement comando = conexao.prepareStatement(
                "INSERT INTO participantes (materia_id, usuario_id) VALUES (?, ?)");
        comando.setInt(1, materia.getId());
        comando.setInt(2, usuario.getId());
        comando.executeUpdate();
        comando.close();
    }

    public void remover(Materia materia, Usuario usuario) {
        Connection conexao = abrirConexao();
        try {
            PreparedStatement comando = conexao.prepareStatement(
                    "DELETE FROM participantes WHERE materia_id = ? AND usuario_id = ?");
            comando.setInt(1, materia.getId());
            comando.setInt(2, usuario.getId());
            comando.executeUpdate();
            comando.close();
        } catch (SQLException erro) {
            throw new PersistenciaException("Erro ao retirar o usuario da materia: " + erro.getMessage());
        } finally {
            fechar(conexao);
        }
    }
}
