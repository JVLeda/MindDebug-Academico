package dao;

import excecao.PersistenciaException;
import model.Aula;
import model.Estudante;
import model.Feedback;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Acesso a tabela feedbacks. Cada aluno tem no maximo um feedback por aula:
 * se registrar de novo, a nota e o comentario sao trocados (como em Aula.registrarFeedback).
 * O estudante_id fica no banco; o anonimato e tratado na exibicao.
 */
public class FeedbackDao extends AcessoBanco {
    private static final String CONSULTA = "SELECT id, aula_id, estudante_id, nota, comentario FROM feedbacks ";

    // ------------------------------------------------------------------ consultas

    public ArrayList<Feedback> listarDaAula(Aula aula) {
        HashMap<Integer, Estudante> estudantes = new UsuarioDao().estudantesPorId();
        Connection conexao = abrirConexao();
        try {
            PreparedStatement consulta = conexao.prepareStatement(CONSULTA + "WHERE aula_id = ? ORDER BY id");
            consulta.setInt(1, aula.getId());
            ResultSet resultado = consulta.executeQuery();
            ArrayList<Feedback> feedbacks = new ArrayList<>();
            while (resultado.next()) {
                Estudante estudante = estudantes.get(resultado.getInt("estudante_id"));
                feedbacks.add(criarFeedback(resultado, estudante));
            }
            resultado.close();
            consulta.close();
            return feedbacks;
        } catch (SQLException erro) {
            throw new PersistenciaException("Erro ao consultar feedbacks: " + erro.getMessage());
        } finally {
            fechar(conexao);
        }
    }

    /** Media das notas da aula calculada pelo banco. Sem feedbacks, devolve 0, como Aula.calcularMedia. */
    public double calcularMedia(Aula aula) {
        Connection conexao = abrirConexao();
        try {
            PreparedStatement consulta = conexao.prepareStatement("SELECT AVG(nota) FROM feedbacks WHERE aula_id = ?");
            consulta.setInt(1, aula.getId());
            ResultSet resultado = consulta.executeQuery();
            resultado.next();
            double media = resultado.getDouble(1);
            resultado.close();
            consulta.close();
            return media;
        } catch (SQLException erro) {
            throw new PersistenciaException("Erro ao calcular a media da aula: " + erro.getMessage());
        } finally {
            fechar(conexao);
        }
    }

    /** Usado pelo BancoDao: coloca cada feedback dentro da sua aula. */
    void listar(Connection conexao, HashMap<Integer, Aula> aulas, HashMap<Integer, Estudante> estudantes)
            throws SQLException {
        PreparedStatement consulta = conexao.prepareStatement(CONSULTA + "ORDER BY id");
        ResultSet resultado = consulta.executeQuery();
        while (resultado.next()) {
            Aula aula = aulas.get(resultado.getInt("aula_id"));
            Estudante estudante = estudantes.get(resultado.getInt("estudante_id"));
            if (aula != null && estudante != null) {
                aula.registrarFeedback(criarFeedback(resultado, estudante));
            }
        }
        resultado.close();
        consulta.close();
    }

    private Feedback criarFeedback(ResultSet resultado, Estudante estudante) throws SQLException {
        Feedback feedback = new Feedback(estudante, resultado.getInt("nota"), resultado.getString("comentario"));
        feedback.setId(resultado.getInt("id"));
        return feedback;
    }

    // ------------------------------------------------------------------ gravacao

    /**
     * Grava o feedback do aluno na aula. Se ele ja tinha um, troca nota e comentario.
     * A nota precisa estar entre 0 e 10 (CHECK da tabela); fora disso, nada e gravado.
     */
    public void registrar(Aula aula, Feedback feedback) {
        Connection conexao = abrirConexao();
        boolean gravou = false;
        try {
            conexao.setAutoCommit(false);
            int idExistente = buscarIdDoFeedback(conexao, aula, feedback.getEstudante());
            if (idExistente > 0) {
                PreparedStatement comando = conexao.prepareStatement(
                        "UPDATE feedbacks SET nota = ?, comentario = ? WHERE id = ?");
                comando.setInt(1, feedback.getNota());
                comando.setString(2, feedback.getComentario());
                comando.setInt(3, idExistente);
                comando.executeUpdate();
                comando.close();
            } else {
                inserir(conexao, aula, feedback);
                idExistente = lerUltimoId(conexao, "feedbacks");
            }
            conexao.commit();
            gravou = true;
            feedback.setId(idExistente);
        } catch (SQLException erro) {
            throw new PersistenciaException("Erro ao registrar o feedback. Nada foi gravado. Detalhe: "
                    + erro.getMessage());
        } finally {
            if (!gravou) {
                desfazer(conexao);
            }
            fechar(conexao);
        }
    }

    /** Tambem usado pelo BancoDao, dentro da transacao dele. */
    void inserir(Connection conexao, Aula aula, Feedback feedback) throws SQLException {
        PreparedStatement comando = conexao.prepareStatement(
                "INSERT INTO feedbacks (aula_id, estudante_id, nota, comentario) VALUES (?, ?, ?, ?)");
        comando.setInt(1, aula.getId());
        comando.setInt(2, feedback.getEstudante().getId());
        comando.setInt(3, feedback.getNota());
        comando.setString(4, feedback.getComentario());
        comando.executeUpdate();
        comando.close();
    }

    /**
     * Fica restrito ao pacote dao, sem uso pelas telas: nenhum perfil apaga feedback.
     * Serve para os testes de integracao limparem o que criaram.
     */
    void removerDaAula(Aula aula) {
        Connection conexao = abrirConexao();
        try {
            PreparedStatement comando = conexao.prepareStatement("DELETE FROM feedbacks WHERE aula_id = ?");
            comando.setInt(1, aula.getId());
            comando.executeUpdate();
            comando.close();
        } catch (SQLException erro) {
            throw new PersistenciaException("Erro ao apagar: " + erro.getMessage());
        } finally {
            fechar(conexao);
        }
    }

    private int buscarIdDoFeedback(Connection conexao, Aula aula, Estudante estudante) throws SQLException {
        PreparedStatement consulta = conexao.prepareStatement(
                "SELECT id FROM feedbacks WHERE aula_id = ? AND estudante_id = ?");
        consulta.setInt(1, aula.getId());
        consulta.setInt(2, estudante.getId());
        ResultSet resultado = consulta.executeQuery();
        int id = 0;
        if (resultado.next()) {
            id = resultado.getInt("id");
        }
        resultado.close();
        consulta.close();
        return id;
    }
}
