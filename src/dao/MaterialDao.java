package dao;

import excecao.PersistenciaException;
import model.Materia;
import model.Material;
import model.Usuario;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;

/** Acesso a tabela materiais: arquivos e links publicados na materia. */
public class MaterialDao extends AcessoBanco {
    private static final String CONSULTA = "SELECT id, materia_id, titulo, tipo, referencia, autor_id FROM materiais ";

    public ArrayList<Material> listarDaMateria(Materia materia) {
        HashMap<Integer, Usuario> usuarios = new UsuarioDao().usuariosPorId();
        Connection conexao = abrirConexao();
        try {
            PreparedStatement consulta = conexao.prepareStatement(CONSULTA + "WHERE materia_id = ? ORDER BY id");
            consulta.setInt(1, materia.getId());
            ResultSet resultado = consulta.executeQuery();
            ArrayList<Material> materiais = new ArrayList<>();
            while (resultado.next()) {
                materiais.add(criarMaterial(resultado, usuarios.get(resultado.getInt("autor_id"))));
            }
            resultado.close();
            consulta.close();
            return materiais;
        } catch (SQLException erro) {
            throw new PersistenciaException("Erro ao consultar materiais: " + erro.getMessage());
        } finally {
            fechar(conexao);
        }
    }

    /** Usado pelo BancoDao: coloca cada material dentro da sua materia. */
    void listar(Connection conexao, HashMap<Integer, Materia> materias, HashMap<Integer, Usuario> usuarios)
            throws SQLException {
        PreparedStatement consulta = conexao.prepareStatement(CONSULTA + "ORDER BY id");
        ResultSet resultado = consulta.executeQuery();
        while (resultado.next()) {
            Materia materia = materias.get(resultado.getInt("materia_id"));
            Usuario autor = usuarios.get(resultado.getInt("autor_id"));
            if (materia != null && autor != null) {
                materia.adicionarMaterial(criarMaterial(resultado, autor));
            }
        }
        resultado.close();
        consulta.close();
    }

    private Material criarMaterial(ResultSet resultado, Usuario autor) throws SQLException {
        Material material = new Material(
                resultado.getString("titulo"),
                resultado.getString("tipo"),
                resultado.getString("referencia"),
                autor
        );
        material.setId(resultado.getInt("id"));
        return material;
    }

    public void inserir(Materia materia, Material material) {
        Connection conexao = abrirConexao();
        boolean gravou = false;
        try {
            conexao.setAutoCommit(false);
            PreparedStatement comando = conexao.prepareStatement(
                    "INSERT INTO materiais (materia_id, titulo, tipo, referencia, autor_id) VALUES (?, ?, ?, ?, ?)");
            comando.setInt(1, materia.getId());
            preencherMaterial(comando, 2, material);
            comando.executeUpdate();
            comando.close();
            int idGerado = lerUltimoId(conexao, "materiais");

            conexao.commit();
            gravou = true;
            material.setId(idGerado);
        } catch (SQLException erro) {
            throw new PersistenciaException("Erro ao publicar o material. Nada foi gravado. Detalhe: "
                    + erro.getMessage());
        } finally {
            if (!gravou) {
                desfazer(conexao);
            }
            fechar(conexao);
        }
    }

    /** Usado pelo BancoDao, dentro da transacao dele. */
    void inserirComId(Connection conexao, Materia materia, Material material) throws SQLException {
        PreparedStatement comando = conexao.prepareStatement(
                "INSERT INTO materiais (id, materia_id, titulo, tipo, referencia, autor_id) VALUES (?, ?, ?, ?, ?, ?)");
        comando.setInt(1, material.getId());
        comando.setInt(2, materia.getId());
        preencherMaterial(comando, 3, material);
        comando.executeUpdate();
        comando.close();
    }

    public void remover(Material material) {
        Connection conexao = abrirConexao();
        try {
            PreparedStatement comando = conexao.prepareStatement("DELETE FROM materiais WHERE id = ?");
            comando.setInt(1, material.getId());
            comando.executeUpdate();
            comando.close();
        } catch (SQLException erro) {
            throw new PersistenciaException("Erro ao remover o material: " + erro.getMessage());
        } finally {
            fechar(conexao);
        }
    }

    /** Preenche titulo, tipo, referencia e autor a partir da posicao dada. */
    private void preencherMaterial(PreparedStatement comando, int inicio, Material material) throws SQLException {
        comando.setString(inicio, material.getTitulo());
        comando.setString(inicio + 1, material.getTipo());
        comando.setString(inicio + 2, material.getReferencia());
        comando.setInt(inicio + 3, material.getAutor().getId());
    }
}
