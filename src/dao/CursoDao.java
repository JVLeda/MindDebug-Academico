package dao;

import excecao.PersistenciaException;
import excecao.RegraDeNegocioException;
import model.Curso;
import model.NivelCurso;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.util.ArrayList;

/**
 * Acesso a tabela cursos. As consultas devolvem so os dados do curso;
 * os periodos ficam com o PeriodoDao.
 */
public class CursoDao extends AcessoBanco {
    private static final String CONSULTA =
            "SELECT id, nome, sigla, nivel, area, carga_horaria, coordenador FROM cursos ";

    // ------------------------------------------------------------------ consultas

    public ArrayList<Curso> listar() {
        Connection conexao = abrirConexao();
        try {
            return listar(conexao);
        } catch (SQLException erro) {
            throw new PersistenciaException("Erro ao consultar cursos: " + erro.getMessage());
        } finally {
            fechar(conexao);
        }
    }

    public Curso buscarPorSigla(String sigla) {
        Connection conexao = abrirConexao();
        try {
            PreparedStatement consulta = conexao.prepareStatement(CONSULTA + "WHERE sigla = ?");
            consulta.setString(1, sigla);
            ArrayList<Curso> encontrados = lerCursos(consulta);
            consulta.close();
            if (encontrados.isEmpty()) {
                return null;
            }
            return encontrados.get(0);
        } catch (SQLException erro) {
            throw new PersistenciaException("Erro ao consultar o curso: " + erro.getMessage());
        } finally {
            fechar(conexao);
        }
    }

    /** Usado pelo BancoDao ao carregar tudo. */
    ArrayList<Curso> listar(Connection conexao) throws SQLException {
        PreparedStatement consulta = conexao.prepareStatement(CONSULTA + "ORDER BY id");
        ArrayList<Curso> cursos = lerCursos(consulta);
        consulta.close();
        return cursos;
    }

    private ArrayList<Curso> lerCursos(PreparedStatement consulta) throws SQLException {
        ArrayList<Curso> cursos = new ArrayList<>();
        ResultSet resultado = consulta.executeQuery();
        while (resultado.next()) {
            Curso curso = new Curso(
                    resultado.getString("nome"),
                    resultado.getString("sigla"),
                    resultado.getString("area"),
                    resultado.getInt("carga_horaria"),
                    resultado.getString("coordenador")
            );
            curso.setId(resultado.getInt("id"));
            curso.setNivel(NivelCurso.buscarPorNome(resultado.getString("nivel")));
            cursos.add(curso);
        }
        resultado.close();
        return cursos;
    }

    // ------------------------------------------------------------------ gravacao

    public void inserir(Curso curso) {
        Connection conexao = abrirConexao();
        boolean gravou = false;
        try {
            conexao.setAutoCommit(false);
            verificarSiglaLivre(conexao, curso);

            PreparedStatement comando = conexao.prepareStatement(
                    "INSERT INTO cursos (nome, sigla, nivel, area, carga_horaria, coordenador) VALUES (?, ?, ?, ?, ?, ?)");
            preencherCurso(comando, 1, curso);
            comando.executeUpdate();
            comando.close();
            int idGerado = lerUltimoId(conexao, "cursos");

            conexao.commit();
            gravou = true;
            curso.setId(idGerado);
        } catch (SQLException erro) {
            throw new PersistenciaException("Erro ao cadastrar o curso. Nada foi gravado. Detalhe: " + erro.getMessage());
        } finally {
            if (!gravou) {
                desfazer(conexao);
            }
            fechar(conexao);
        }
    }

    /** Usado pelo BancoDao, dentro da transacao dele, para regravar o curso com o id que ja tem. */
    void inserirComId(Connection conexao, Curso curso) throws SQLException {
        PreparedStatement comando = conexao.prepareStatement(
                "INSERT INTO cursos (id, nome, sigla, nivel, area, carga_horaria, coordenador) VALUES (?, ?, ?, ?, ?, ?, ?)");
        comando.setInt(1, curso.getId());
        preencherCurso(comando, 2, curso);
        comando.executeUpdate();
        comando.close();
    }

    public void atualizar(Curso curso) {
        Connection conexao = abrirConexao();
        try {
            verificarSiglaLivre(conexao, curso);
            PreparedStatement comando = conexao.prepareStatement(
                    "UPDATE cursos SET nome = ?, sigla = ?, nivel = ?, area = ?, carga_horaria = ?, coordenador = ? "
                            + "WHERE id = ?");
            preencherCurso(comando, 1, curso);
            comando.setInt(7, curso.getId());
            int alteradas = comando.executeUpdate();
            comando.close();
            if (alteradas == 0) {
                throw new RegraDeNegocioException("Curso nao encontrado no banco.");
            }
        } catch (SQLException erro) {
            throw new PersistenciaException("Erro ao atualizar o curso: " + erro.getMessage());
        } finally {
            fechar(conexao);
        }
    }

    /**
     * Curso com materias ou com alunos nao pode ser excluido.
     * Os periodos vazios do curso sao apagados junto, na mesma transacao.
     */
    public void remover(Curso curso) {
        Connection conexao = abrirConexao();
        boolean gravou = false;
        try {
            conexao.setAutoCommit(false);
            int materias = contarPorId(conexao,
                    "SELECT COUNT(*) FROM materias m JOIN periodos p ON p.id = m.periodo_id WHERE p.curso_id = ?",
                    curso.getId());
            if (materias > 0) {
                throw new RegraDeNegocioException("O curso tem materias cadastradas e nao pode ser excluido.");
            }
            int alunos = contarPorId(conexao, "SELECT COUNT(*) FROM estudantes WHERE curso_id = ?", curso.getId());
            if (alunos > 0) {
                throw new RegraDeNegocioException("O curso tem alunos cadastrados e nao pode ser excluido.");
            }

            PreparedStatement comando = conexao.prepareStatement("DELETE FROM periodos WHERE curso_id = ?");
            comando.setInt(1, curso.getId());
            comando.executeUpdate();
            comando.close();

            comando = conexao.prepareStatement("DELETE FROM cursos WHERE id = ?");
            comando.setInt(1, curso.getId());
            comando.executeUpdate();
            comando.close();

            conexao.commit();
            gravou = true;
        } catch (SQLIntegrityConstraintViolationException erro) {
            throw new RegraDeNegocioException("O curso tem registros ligados a ele e nao pode ser excluido.");
        } catch (SQLException erro) {
            throw new PersistenciaException("Erro ao excluir o curso: " + erro.getMessage());
        } finally {
            if (!gravou) {
                desfazer(conexao);
            }
            fechar(conexao);
        }
    }

    // ------------------------------------------------------------------ apoio

    private void preencherCurso(PreparedStatement comando, int inicio, Curso curso) throws SQLException {
        comando.setString(inicio, curso.getNome());
        comando.setString(inicio + 1, curso.getSigla());
        comando.setString(inicio + 2, curso.getNivel().getNome());
        comando.setString(inicio + 3, curso.getArea());
        comando.setInt(inicio + 4, curso.getCargaHoraria());
        comando.setString(inicio + 5, curso.getCoordenador());
    }

    private void verificarSiglaLivre(Connection conexao, Curso curso) throws SQLException {
        PreparedStatement consulta = conexao.prepareStatement(
                "SELECT COUNT(*) FROM cursos WHERE sigla = ? AND id <> ?");
        consulta.setString(1, curso.getSigla());
        consulta.setInt(2, curso.getId());
        if (contar(consulta) > 0) {
            throw new RegraDeNegocioException("Ja existe um curso com esta sigla.");
        }
    }
}
