package dao;

import excecao.PersistenciaException;
import excecao.RegraDeNegocioException;
import model.Materia;
import model.Periodo;
import model.Professor;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Acesso a tabela materias. O professor da materia fica na coluna professor_id,
 * entao cada materia tem no maximo um professor.
 */
public class MateriaDao extends AcessoBanco {
    private static final String CONSULTA =
            "SELECT id, periodo_id, nome, codigo, carga_horaria, professor_id FROM materias ";

    // ------------------------------------------------------------------ consultas

    /** Materias do periodo, cada uma com o seu professor. Os participantes ficam com o ParticipanteDao. */
    public ArrayList<Materia> listarDoPeriodo(Periodo periodo) {
        HashMap<Integer, Professor> professores = new UsuarioDao().professoresPorId();
        Connection conexao = abrirConexao();
        try {
            PreparedStatement consulta = conexao.prepareStatement(CONSULTA + "WHERE periodo_id = ? ORDER BY id");
            consulta.setInt(1, periodo.getId());
            ResultSet resultado = consulta.executeQuery();
            ArrayList<Materia> materias = new ArrayList<>();
            while (resultado.next()) {
                materias.add(criarMateria(resultado, professores));
            }
            resultado.close();
            consulta.close();
            return materias;
        } catch (SQLException erro) {
            throw new PersistenciaException("Erro ao consultar materias: " + erro.getMessage());
        } finally {
            fechar(conexao);
        }
    }

    /** Usado pelo BancoDao: coloca cada materia dentro do seu periodo. */
    HashMap<Integer, Materia> listar(Connection conexao, HashMap<Integer, Periodo> periodos,
            HashMap<Integer, Professor> professores) throws SQLException {
        HashMap<Integer, Materia> mapa = new HashMap<>();
        PreparedStatement consulta = conexao.prepareStatement(CONSULTA + "ORDER BY id");
        ResultSet resultado = consulta.executeQuery();
        while (resultado.next()) {
            Periodo periodo = periodos.get(resultado.getInt("periodo_id"));
            if (periodo != null) {
                Materia materia = criarMateria(resultado, professores);
                periodo.adicionarMateria(materia);
                mapa.put(materia.getId(), materia);
            }
        }
        resultado.close();
        consulta.close();
        return mapa;
    }

    private Materia criarMateria(ResultSet resultado, HashMap<Integer, Professor> professores) throws SQLException {
        Professor professor = professores.get(resultado.getInt("professor_id"));
        Materia materia = new Materia(resultado.getString("nome"), professor);
        materia.setId(resultado.getInt("id"));
        materia.setCodigo(textoOuVazio(resultado.getString("codigo")));
        materia.setCargaHoraria(resultado.getInt("carga_horaria"));
        return materia;
    }

    // ------------------------------------------------------------------ gravacao

    /** Cadastra a materia e, na mesma transacao, o chat geral e o chat de equipe dela. */
    public void inserir(Periodo periodo, Materia materia) {
        Connection conexao = abrirConexao();
        boolean gravou = false;
        try {
            conexao.setAutoCommit(false);
            verificarDadosLivres(conexao, periodo, materia);

            PreparedStatement comando = conexao.prepareStatement(
                    "INSERT INTO materias (periodo_id, nome, codigo, carga_horaria, professor_id) VALUES (?, ?, ?, ?, ?)");
            comando.setInt(1, periodo.getId());
            preencherMateria(comando, 2, materia);
            comando.executeUpdate();
            comando.close();
            int idGerado = lerUltimoId(conexao, "materias");

            comando = conexao.prepareStatement("INSERT INTO chats (tipo, materia_id) VALUES (?, ?)");
            String[] tipos = {"GERAL", "EQUIPE"};
            for (String tipo : tipos) {
                comando.setString(1, tipo);
                comando.setInt(2, idGerado);
                comando.executeUpdate();
            }
            comando.close();

            conexao.commit();
            gravou = true;
            materia.setId(idGerado);
        } catch (SQLException erro) {
            throw new PersistenciaException("Erro ao cadastrar a materia. Nada foi gravado. Detalhe: "
                    + erro.getMessage());
        } finally {
            if (!gravou) {
                desfazer(conexao);
            }
            fechar(conexao);
        }
    }

    /**
     * Usado pelo BancoDao, dentro da transacao dele, para regravar a materia com o id que ja tem.
     * Os chats sao regravados pelo proprio BancoDao.
     */
    void inserirComId(Connection conexao, Periodo periodo, Materia materia) throws SQLException {
        PreparedStatement comando = conexao.prepareStatement(
                "INSERT INTO materias (id, periodo_id, nome, codigo, carga_horaria, professor_id) VALUES (?, ?, ?, ?, ?, ?)");
        comando.setInt(1, materia.getId());
        comando.setInt(2, periodo.getId());
        preencherMateria(comando, 3, materia);
        comando.executeUpdate();
        comando.close();
    }

    public void atualizar(Periodo periodo, Materia materia) {
        Connection conexao = abrirConexao();
        try {
            verificarDadosLivres(conexao, periodo, materia);
            PreparedStatement comando = conexao.prepareStatement(
                    "UPDATE materias SET nome = ?, codigo = ?, carga_horaria = ? WHERE id = ?");
            comando.setString(1, materia.getNome());
            definirTextoOuNulo(comando, 2, materia.getCodigo());
            definirInteiroOuNulo(comando, 3, materia.getCargaHoraria());
            comando.setInt(4, materia.getId());
            int alteradas = comando.executeUpdate();
            comando.close();
            if (alteradas == 0) {
                throw new RegraDeNegocioException("Materia nao encontrada no banco.");
            }
        } catch (SQLException erro) {
            throw new PersistenciaException("Erro ao atualizar a materia: " + erro.getMessage());
        } finally {
            fechar(conexao);
        }
    }

    /** Troca o professor da materia. Com professor nulo, a materia fica sem professor. */
    public void definirProfessor(Materia materia, Professor professor) {
        Connection conexao = abrirConexao();
        try {
            PreparedStatement comando = conexao.prepareStatement("UPDATE materias SET professor_id = ? WHERE id = ?");
            definirIdOuNulo(comando, 1, professor);
            comando.setInt(2, materia.getId());
            int alteradas = comando.executeUpdate();
            comando.close();
            if (alteradas == 0) {
                throw new RegraDeNegocioException("Materia nao encontrada no banco.");
            }
            materia.setProfessor(professor);
        } catch (SQLException erro) {
            throw new PersistenciaException("Erro ao definir o professor da materia: " + erro.getMessage());
        } finally {
            fechar(conexao);
        }
    }

    /**
     * Apaga a materia, os participantes e os chats dela numa transacao.
     * Se a materia ja tiver aulas, materiais, mensagens ou outros registros, o banco recusa e nada muda.
     */
    public void remover(Materia materia) {
        Connection conexao = abrirConexao();
        boolean gravou = false;
        try {
            conexao.setAutoCommit(false);
            String[] comandos = {
                "DELETE FROM participantes WHERE materia_id = ?",
                "DELETE FROM chats WHERE materia_id = ?",
                "DELETE FROM materias WHERE id = ?"
            };
            for (String sql : comandos) {
                PreparedStatement comando = conexao.prepareStatement(sql);
                comando.setInt(1, materia.getId());
                comando.executeUpdate();
                comando.close();
            }
            conexao.commit();
            gravou = true;
        } catch (SQLIntegrityConstraintViolationException erro) {
            throw new RegraDeNegocioException("A materia tem aulas, materiais, mensagens ou outros registros "
                    + "e nao pode ser excluida.");
        } catch (SQLException erro) {
            throw new PersistenciaException("Erro ao excluir a materia: " + erro.getMessage());
        } finally {
            if (!gravou) {
                desfazer(conexao);
            }
            fechar(conexao);
        }
    }

    // ------------------------------------------------------------------ apoio

    /** Preenche nome, codigo, carga horaria e professor a partir da posicao dada. */
    private void preencherMateria(PreparedStatement comando, int inicio, Materia materia) throws SQLException {
        comando.setString(inicio, materia.getNome());
        definirTextoOuNulo(comando, inicio + 1, materia.getCodigo());
        definirInteiroOuNulo(comando, inicio + 2, materia.getCargaHoraria());
        definirIdOuNulo(comando, inicio + 3, materia.getProfessor());
    }

    /** O nome e unico dentro do periodo; o codigo, quando informado, e unico no sistema. */
    private void verificarDadosLivres(Connection conexao, Periodo periodo, Materia materia) throws SQLException {
        PreparedStatement consulta = conexao.prepareStatement(
                "SELECT COUNT(*) FROM materias WHERE periodo_id = ? AND nome = ? AND id <> ?");
        consulta.setInt(1, periodo.getId());
        consulta.setString(2, materia.getNome());
        consulta.setInt(3, materia.getId());
        if (contar(consulta) > 0) {
            throw new RegraDeNegocioException("Ja existe uma materia com este nome no periodo.");
        }

        String codigo = materia.getCodigo();
        if (codigo != null && !codigo.trim().isEmpty()) {
            consulta = conexao.prepareStatement("SELECT COUNT(*) FROM materias WHERE codigo = ? AND id <> ?");
            consulta.setString(1, codigo);
            consulta.setInt(2, materia.getId());
            if (contar(consulta) > 0) {
                throw new RegraDeNegocioException("Ja existe uma materia com este codigo.");
            }
        }
    }
}
