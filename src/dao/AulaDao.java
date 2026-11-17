package dao;

import excecao.PersistenciaException;
import excecao.RegraDeNegocioException;
import model.Aula;
import model.Materia;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.util.ArrayList;
import java.util.HashMap;

/** Acesso a tabela aulas. Cada aula tem o seu proprio chat, criado junto com ela. */
public class AulaDao extends AcessoBanco {
    private static final String CONSULTA =
            "SELECT id, materia_id, titulo, data_aula, descricao, media_esperada FROM aulas ";

    // ------------------------------------------------------------------ consultas

    /** Aulas da materia, sem feedbacks; os feedbacks ficam com o FeedbackDao. */
    public ArrayList<Aula> listarDaMateria(Materia materia) {
        Connection conexao = abrirConexao();
        try {
            PreparedStatement consulta = conexao.prepareStatement(CONSULTA + "WHERE materia_id = ? ORDER BY id");
            consulta.setInt(1, materia.getId());
            ResultSet resultado = consulta.executeQuery();
            ArrayList<Aula> aulas = new ArrayList<>();
            while (resultado.next()) {
                aulas.add(criarAula(resultado));
            }
            resultado.close();
            consulta.close();
            return aulas;
        } catch (SQLException erro) {
            throw new PersistenciaException("Erro ao consultar aulas: " + erro.getMessage());
        } finally {
            fechar(conexao);
        }
    }

    /** Usado pelo BancoDao: coloca cada aula dentro da sua materia. */
    HashMap<Integer, Aula> listar(Connection conexao, HashMap<Integer, Materia> materias) throws SQLException {
        HashMap<Integer, Aula> mapa = new HashMap<>();
        PreparedStatement consulta = conexao.prepareStatement(CONSULTA + "ORDER BY id");
        ResultSet resultado = consulta.executeQuery();
        while (resultado.next()) {
            Materia materia = materias.get(resultado.getInt("materia_id"));
            if (materia != null) {
                Aula aula = criarAula(resultado);
                materia.adicionarAula(aula);
                mapa.put(aula.getId(), aula);
            }
        }
        resultado.close();
        consulta.close();
        return mapa;
    }

    private Aula criarAula(ResultSet resultado) throws SQLException {
        Aula aula = new Aula(
                resultado.getString("titulo"),
                textoDaData(resultado.getDate("data_aula")),
                textoOuVazio(resultado.getString("descricao"))
        );
        aula.setId(resultado.getInt("id"));
        aula.setMediaEsperada(resultado.getDouble("media_esperada"));
        return aula;
    }

    // ------------------------------------------------------------------ gravacao

    /** Cadastra a aula e, na mesma transacao, o chat dela. */
    public void inserir(Materia materia, Aula aula) {
        Connection conexao = abrirConexao();
        boolean gravou = false;
        try {
            conexao.setAutoCommit(false);
            PreparedStatement comando = conexao.prepareStatement(
                    "INSERT INTO aulas (materia_id, titulo, data_aula, descricao, media_esperada) VALUES (?, ?, ?, ?, ?)");
            comando.setInt(1, materia.getId());
            preencherAula(comando, 2, aula);
            comando.executeUpdate();
            comando.close();
            int idGerado = lerUltimoId(conexao, "aulas");

            comando = conexao.prepareStatement("INSERT INTO chats (tipo, aula_id) VALUES ('AULA', ?)");
            comando.setInt(1, idGerado);
            comando.executeUpdate();
            comando.close();

            conexao.commit();
            gravou = true;
            aula.setId(idGerado);
        } catch (SQLException erro) {
            throw new PersistenciaException("Erro ao cadastrar a aula. Nada foi gravado. Detalhe: " + erro.getMessage());
        } finally {
            if (!gravou) {
                desfazer(conexao);
            }
            fechar(conexao);
        }
    }

    /** Usado pelo BancoDao, dentro da transacao dele. O chat e regravado pelo ChatDao. */
    void inserirComId(Connection conexao, Materia materia, Aula aula) throws SQLException {
        PreparedStatement comando = conexao.prepareStatement(
                "INSERT INTO aulas (id, materia_id, titulo, data_aula, descricao, media_esperada) VALUES (?, ?, ?, ?, ?, ?)");
        comando.setInt(1, aula.getId());
        comando.setInt(2, materia.getId());
        preencherAula(comando, 3, aula);
        comando.executeUpdate();
        comando.close();
    }

    public void atualizar(Aula aula) {
        Connection conexao = abrirConexao();
        try {
            PreparedStatement comando = conexao.prepareStatement(
                    "UPDATE aulas SET titulo = ?, data_aula = ?, descricao = ?, media_esperada = ? WHERE id = ?");
            preencherAula(comando, 1, aula);
            comando.setInt(5, aula.getId());
            int alteradas = comando.executeUpdate();
            comando.close();
            if (alteradas == 0) {
                throw new RegraDeNegocioException("Aula nao encontrada no banco.");
            }
        } catch (SQLException erro) {
            throw new PersistenciaException("Erro ao atualizar a aula: " + erro.getMessage());
        } finally {
            fechar(conexao);
        }
    }

    /** Apaga a aula e o chat dela. Aula com feedbacks, mensagens, quiz ou outros registros nao e apagada. */
    public void remover(Aula aula) {
        Connection conexao = abrirConexao();
        boolean gravou = false;
        try {
            conexao.setAutoCommit(false);
            String[] comandos = {"DELETE FROM chats WHERE aula_id = ?", "DELETE FROM aulas WHERE id = ?"};
            for (String sql : comandos) {
                PreparedStatement comando = conexao.prepareStatement(sql);
                comando.setInt(1, aula.getId());
                comando.executeUpdate();
                comando.close();
            }
            conexao.commit();
            gravou = true;
        } catch (SQLIntegrityConstraintViolationException erro) {
            throw new RegraDeNegocioException("A aula tem feedbacks, mensagens ou outros registros e nao pode ser excluida.");
        } catch (SQLException erro) {
            throw new PersistenciaException("Erro ao excluir a aula: " + erro.getMessage());
        } finally {
            if (!gravou) {
                desfazer(conexao);
            }
            fechar(conexao);
        }
    }

    /** Preenche titulo, data, descricao e media esperada a partir da posicao dada. */
    private void preencherAula(PreparedStatement comando, int inicio, Aula aula) throws SQLException {
        comando.setString(inicio, aula.getTitulo());
        definirData(comando, inicio + 1, converterTextoEmData(aula.getData()));
        definirTextoOuNulo(comando, inicio + 2, aula.getDescricao());
        comando.setDouble(inicio + 3, aula.getMediaEsperada());
    }
}
