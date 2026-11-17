package dao;

import excecao.PersistenciaException;
import excecao.RegraDeNegocioException;
import model.Curso;
import model.Periodo;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

/** Acesso a tabela periodos. Cada periodo pertence a um curso. */
public class PeriodoDao extends AcessoBanco {
    private static final String CONSULTA =
            "SELECT id, curso_id, numero, nome, ano, semestre, data_inicio, data_fim, ativo FROM periodos ";

    // ------------------------------------------------------------------ consultas

    /** Periodos do curso, em ordem de numero. As materias ficam com o MateriaDao. */
    public ArrayList<Periodo> listarDoCurso(Curso curso) {
        Connection conexao = abrirConexao();
        try {
            PreparedStatement consulta = conexao.prepareStatement(CONSULTA + "WHERE curso_id = ? ORDER BY numero");
            consulta.setInt(1, curso.getId());
            ResultSet resultado = consulta.executeQuery();
            ArrayList<Periodo> periodos = new ArrayList<>();
            while (resultado.next()) {
                periodos.add(criarPeriodo(resultado));
            }
            resultado.close();
            consulta.close();
            return periodos;
        } catch (SQLException erro) {
            throw new PersistenciaException("Erro ao consultar periodos: " + erro.getMessage());
        } finally {
            fechar(conexao);
        }
    }

    /** Usado pelo BancoDao: coloca cada periodo dentro do seu curso. */
    HashMap<Integer, Periodo> listar(Connection conexao, HashMap<Integer, Curso> cursos) throws SQLException {
        HashMap<Integer, Periodo> mapa = new HashMap<>();
        PreparedStatement consulta = conexao.prepareStatement(CONSULTA + "ORDER BY id");
        ResultSet resultado = consulta.executeQuery();
        while (resultado.next()) {
            Curso curso = cursos.get(resultado.getInt("curso_id"));
            if (curso != null) {
                Periodo periodo = criarPeriodo(resultado);
                curso.adicionarPeriodo(periodo);
                mapa.put(periodo.getId(), periodo);
            }
        }
        resultado.close();
        consulta.close();
        return mapa;
    }

    private Periodo criarPeriodo(ResultSet resultado) throws SQLException {
        Periodo periodo = new Periodo(resultado.getInt("numero"), resultado.getString("nome"));
        periodo.setId(resultado.getInt("id"));
        periodo.setAno(resultado.getInt("ano"));
        periodo.setSemestre(resultado.getInt("semestre"));
        periodo.setAtivo(resultado.getBoolean("ativo"));

        Date inicio = resultado.getDate("data_inicio");
        Date fim = resultado.getDate("data_fim");
        if (inicio != null && fim != null) {
            periodo.definirDatas(inicio, fim);
        }
        return periodo;
    }

    // ------------------------------------------------------------------ gravacao

    public void inserir(Curso curso, Periodo periodo) {
        Connection conexao = abrirConexao();
        boolean gravou = false;
        try {
            conexao.setAutoCommit(false);
            verificarNumeroLivre(conexao, curso, periodo);

            PreparedStatement comando = conexao.prepareStatement(
                    "INSERT INTO periodos (curso_id, numero, nome, ano, semestre, data_inicio, data_fim, ativo) "
                            + "VALUES (?, ?, ?, ?, ?, ?, ?, ?)");
            comando.setInt(1, curso.getId());
            preencherPeriodo(comando, 2, periodo);
            comando.executeUpdate();
            comando.close();
            int idGerado = lerUltimoId(conexao, "periodos");

            conexao.commit();
            gravou = true;
            periodo.setId(idGerado);
        } catch (SQLException erro) {
            throw new PersistenciaException("Erro ao cadastrar o periodo. Nada foi gravado. Detalhe: "
                    + erro.getMessage());
        } finally {
            if (!gravou) {
                desfazer(conexao);
            }
            fechar(conexao);
        }
    }

    /** Usado pelo BancoDao, dentro da transacao dele, para regravar o periodo com o id que ja tem. */
    void inserirComId(Connection conexao, Curso curso, Periodo periodo) throws SQLException {
        PreparedStatement comando = conexao.prepareStatement(
                "INSERT INTO periodos (id, curso_id, numero, nome, ano, semestre, data_inicio, data_fim, ativo) "
                        + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)");
        comando.setInt(1, periodo.getId());
        comando.setInt(2, curso.getId());
        preencherPeriodo(comando, 3, periodo);
        comando.executeUpdate();
        comando.close();
    }

    public void atualizar(Curso curso, Periodo periodo) {
        Connection conexao = abrirConexao();
        try {
            verificarNumeroLivre(conexao, curso, periodo);
            PreparedStatement comando = conexao.prepareStatement(
                    "UPDATE periodos SET numero = ?, nome = ?, ano = ?, semestre = ?, data_inicio = ?, data_fim = ?, "
                            + "ativo = ? WHERE id = ?");
            preencherPeriodo(comando, 1, periodo);
            comando.setInt(8, periodo.getId());
            int alteradas = comando.executeUpdate();
            comando.close();
            if (alteradas == 0) {
                throw new RegraDeNegocioException("Periodo nao encontrado no banco.");
            }
        } catch (SQLException erro) {
            throw new PersistenciaException("Erro ao atualizar o periodo: " + erro.getMessage());
        } finally {
            fechar(conexao);
        }
    }

    /** Periodo com materias nao pode ser excluido. */
    public void remover(Periodo periodo) {
        Connection conexao = abrirConexao();
        try {
            int materias = contarPorId(conexao, "SELECT COUNT(*) FROM materias WHERE periodo_id = ?", periodo.getId());
            if (materias > 0) {
                throw new RegraDeNegocioException("O periodo tem materias cadastradas e nao pode ser excluido.");
            }
            PreparedStatement comando = conexao.prepareStatement("DELETE FROM periodos WHERE id = ?");
            comando.setInt(1, periodo.getId());
            comando.executeUpdate();
            comando.close();
        } catch (SQLException erro) {
            throw new PersistenciaException("Erro ao excluir o periodo: " + erro.getMessage());
        } finally {
            fechar(conexao);
        }
    }

    // ------------------------------------------------------------------ apoio

    /** Preenche numero, nome, ano, semestre, data de inicio, data de fim e ativo a partir da posicao dada. */
    private void preencherPeriodo(PreparedStatement comando, int inicio, Periodo periodo) throws SQLException {
        comando.setInt(inicio, periodo.getNumero());
        comando.setString(inicio + 1, periodo.getNome());
        definirInteiroOuNulo(comando, inicio + 2, periodo.getAno());
        definirInteiroOuNulo(comando, inicio + 3, periodo.getSemestre());
        definirData(comando, inicio + 4, periodo.getDataInicio());
        definirData(comando, inicio + 5, periodo.getDataFim());
        comando.setBoolean(inicio + 6, periodo.isAtivo());
    }

    private void verificarNumeroLivre(Connection conexao, Curso curso, Periodo periodo) throws SQLException {
        PreparedStatement consulta = conexao.prepareStatement(
                "SELECT COUNT(*) FROM periodos WHERE curso_id = ? AND numero = ? AND id <> ?");
        consulta.setInt(1, curso.getId());
        consulta.setInt(2, periodo.getNumero());
        consulta.setInt(3, periodo.getId());
        if (contar(consulta) > 0) {
            throw new RegraDeNegocioException("Ja existe um periodo com este numero no curso.");
        }
    }
}
