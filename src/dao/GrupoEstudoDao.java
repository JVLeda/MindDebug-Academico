package dao;

import excecao.PersistenciaException;
import excecao.RegraDeNegocioException;
import model.Aula;
import model.Estudante;
import model.GrupoEstudo;
import model.Materia;
import model.Monitor;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Acesso a grupos_estudo e as tabelas associativas grupos_estudo_alunos e grupos_estudo_monitores.
 * O nome do grupo e unico dentro da materia.
 */
public class GrupoEstudoDao extends AcessoBanco {
    private static final String CONSULTA = "SELECT id, materia_id, nome, aula_id FROM grupos_estudo ";

    // ------------------------------------------------------------------ consultas

    /** Grupos da materia com alunos e monitores. A aula do grupo vem do AulaDao. */
    public ArrayList<GrupoEstudo> listarDaMateria(Materia materia) {
        HashMap<Integer, Aula> aulas = new HashMap<>();
        for (Aula aula : new AulaDao().listarDaMateria(materia)) {
            aulas.put(aula.getId(), aula);
        }
        UsuarioDao usuarioDao = new UsuarioDao();
        HashMap<Integer, Estudante> estudantes = usuarioDao.estudantesPorId();
        HashMap<Integer, Monitor> monitores = usuarioDao.monitoresPorId();

        Connection conexao = abrirConexao();
        try {
            PreparedStatement consulta = conexao.prepareStatement(CONSULTA + "WHERE materia_id = ? ORDER BY id");
            consulta.setInt(1, materia.getId());
            ResultSet resultado = consulta.executeQuery();
            ArrayList<GrupoEstudo> grupos = new ArrayList<>();
            HashMap<Integer, GrupoEstudo> mapa = new HashMap<>();
            while (resultado.next()) {
                GrupoEstudo grupo = criarGrupo(resultado, aulas);
                grupos.add(grupo);
                mapa.put(grupo.getId(), grupo);
            }
            resultado.close();
            consulta.close();
            listarMembros(conexao, mapa, estudantes, monitores);
            return grupos;
        } catch (SQLException erro) {
            throw new PersistenciaException("Erro ao consultar grupos de estudo: " + erro.getMessage());
        } finally {
            fechar(conexao);
        }
    }

    /** Usado pelo BancoDao: coloca cada grupo, com os membros, dentro da sua materia. */
    void listar(Connection conexao, HashMap<Integer, Materia> materias, HashMap<Integer, Aula> aulas,
            HashMap<Integer, Estudante> estudantes, HashMap<Integer, Monitor> monitores) throws SQLException {
        HashMap<Integer, GrupoEstudo> mapa = new HashMap<>();
        PreparedStatement consulta = conexao.prepareStatement(CONSULTA + "ORDER BY id");
        ResultSet resultado = consulta.executeQuery();
        while (resultado.next()) {
            Materia materia = materias.get(resultado.getInt("materia_id"));
            if (materia != null) {
                GrupoEstudo grupo = criarGrupo(resultado, aulas);
                materia.adicionarGrupo(grupo);
                mapa.put(grupo.getId(), grupo);
            }
        }
        resultado.close();
        consulta.close();
        listarMembros(conexao, mapa, estudantes, monitores);
    }

    private GrupoEstudo criarGrupo(ResultSet resultado, HashMap<Integer, Aula> aulas) throws SQLException {
        GrupoEstudo grupo = new GrupoEstudo(resultado.getString("nome"), aulas.get(resultado.getInt("aula_id")), null);
        grupo.setId(resultado.getInt("id"));
        return grupo;
    }

    /** Le os alunos e os monitores e coloca cada um no grupo que esta no mapa. */
    private void listarMembros(Connection conexao, HashMap<Integer, GrupoEstudo> grupos,
            HashMap<Integer, Estudante> estudantes, HashMap<Integer, Monitor> monitores) throws SQLException {
        PreparedStatement consulta = conexao.prepareStatement(
                "SELECT grupo_id, estudante_id FROM grupos_estudo_alunos ORDER BY grupo_id, estudante_id");
        ResultSet resultado = consulta.executeQuery();
        while (resultado.next()) {
            GrupoEstudo grupo = grupos.get(resultado.getInt("grupo_id"));
            if (grupo != null) {
                grupo.adicionarEstudante(estudantes.get(resultado.getInt("estudante_id")));
            }
        }
        resultado.close();
        consulta.close();

        consulta = conexao.prepareStatement(
                "SELECT grupo_id, monitor_id FROM grupos_estudo_monitores ORDER BY grupo_id, monitor_id");
        resultado = consulta.executeQuery();
        while (resultado.next()) {
            GrupoEstudo grupo = grupos.get(resultado.getInt("grupo_id"));
            if (grupo != null) {
                grupo.adicionarMonitor(monitores.get(resultado.getInt("monitor_id")));
            }
        }
        resultado.close();
        consulta.close();
    }

    // ------------------------------------------------------------------ gravacao

    /** Cadastra o grupo e os membros que ele ja tem, numa transacao. */
    public void inserir(Materia materia, GrupoEstudo grupo) {
        Connection conexao = abrirConexao();
        boolean gravou = false;
        try {
            conexao.setAutoCommit(false);
            PreparedStatement consulta = conexao.prepareStatement(
                    "SELECT COUNT(*) FROM grupos_estudo WHERE materia_id = ? AND nome = ?");
            consulta.setInt(1, materia.getId());
            consulta.setString(2, grupo.getNome());
            if (contar(consulta) > 0) {
                throw new RegraDeNegocioException("Ja existe um grupo com este nome na materia.");
            }

            PreparedStatement comando = conexao.prepareStatement(
                    "INSERT INTO grupos_estudo (materia_id, nome, aula_id) VALUES (?, ?, ?)");
            comando.setInt(1, materia.getId());
            comando.setString(2, grupo.getNome());
            definirIdOuNulo(comando, 3, grupo.getAula());
            comando.executeUpdate();
            comando.close();
            int idGerado = lerUltimoId(conexao, "grupos_estudo");
            inserirMembros(conexao, idGerado, grupo);

            conexao.commit();
            gravou = true;
            grupo.setId(idGerado);
        } catch (SQLException erro) {
            throw new PersistenciaException("Erro ao criar o grupo de estudo. Nada foi gravado. Detalhe: "
                    + erro.getMessage());
        } finally {
            if (!gravou) {
                desfazer(conexao);
            }
            fechar(conexao);
        }
    }

    /** Usado pelo BancoDao, dentro da transacao dele. */
    void inserirComId(Connection conexao, Materia materia, GrupoEstudo grupo) throws SQLException {
        PreparedStatement comando = conexao.prepareStatement(
                "INSERT INTO grupos_estudo (id, materia_id, nome, aula_id) VALUES (?, ?, ?, ?)");
        comando.setInt(1, grupo.getId());
        comando.setInt(2, materia.getId());
        comando.setString(3, grupo.getNome());
        definirIdOuNulo(comando, 4, grupo.getAula());
        comando.executeUpdate();
        comando.close();
        inserirMembros(conexao, grupo.getId(), grupo);
    }

    public void adicionarAluno(GrupoEstudo grupo, Estudante estudante) {
        adicionarMembro("grupos_estudo_alunos", "estudante_id", grupo, estudante.getId(), "O aluno ja esta no grupo.");
    }

    public void adicionarMonitor(GrupoEstudo grupo, Monitor monitor) {
        adicionarMembro("grupos_estudo_monitores", "monitor_id", grupo, monitor.getId(), "O monitor ja esta no grupo.");
    }

    public void removerAluno(GrupoEstudo grupo, Estudante estudante) {
        removerMembro("grupos_estudo_alunos", "estudante_id", grupo, estudante.getId());
    }

    public void removerMonitor(GrupoEstudo grupo, Monitor monitor) {
        removerMembro("grupos_estudo_monitores", "monitor_id", grupo, monitor.getId());
    }

    /** Apaga os membros e o grupo numa transacao. */
    public void remover(GrupoEstudo grupo) {
        Connection conexao = abrirConexao();
        boolean gravou = false;
        try {
            conexao.setAutoCommit(false);
            String[] comandos = {
                "DELETE FROM grupos_estudo_alunos WHERE grupo_id = ?",
                "DELETE FROM grupos_estudo_monitores WHERE grupo_id = ?",
                "DELETE FROM grupos_estudo WHERE id = ?"
            };
            for (String sql : comandos) {
                PreparedStatement comando = conexao.prepareStatement(sql);
                comando.setInt(1, grupo.getId());
                comando.executeUpdate();
                comando.close();
            }
            conexao.commit();
            gravou = true;
        } catch (SQLException erro) {
            throw new PersistenciaException("Erro ao excluir o grupo de estudo: " + erro.getMessage());
        } finally {
            if (!gravou) {
                desfazer(conexao);
            }
            fechar(conexao);
        }
    }

    // ------------------------------------------------------------------ apoio

    private void inserirMembros(Connection conexao, int grupoId, GrupoEstudo grupo) throws SQLException {
        for (Estudante estudante : grupo.getEstudantes()) {
            inserirMembro(conexao, "grupos_estudo_alunos", "estudante_id", grupoId, estudante.getId());
        }
        for (Monitor monitor : grupo.getMonitores()) {
            inserirMembro(conexao, "grupos_estudo_monitores", "monitor_id", grupoId, monitor.getId());
        }
    }

    private void inserirMembro(Connection conexao, String tabela, String coluna, int grupoId, int membroId)
            throws SQLException {
        PreparedStatement comando = conexao.prepareStatement(
                "INSERT INTO " + tabela + " (grupo_id, " + coluna + ") VALUES (?, ?)");
        comando.setInt(1, grupoId);
        comando.setInt(2, membroId);
        comando.executeUpdate();
        comando.close();
    }

    private void adicionarMembro(String tabela, String coluna, GrupoEstudo grupo, int membroId, String mensagemRepetido) {
        Connection conexao = abrirConexao();
        try {
            PreparedStatement consulta = conexao.prepareStatement(
                    "SELECT COUNT(*) FROM " + tabela + " WHERE grupo_id = ? AND " + coluna + " = ?");
            consulta.setInt(1, grupo.getId());
            consulta.setInt(2, membroId);
            if (contar(consulta) > 0) {
                throw new RegraDeNegocioException(mensagemRepetido);
            }
            inserirMembro(conexao, tabela, coluna, grupo.getId(), membroId);
        } catch (SQLException erro) {
            throw new PersistenciaException("Erro ao incluir no grupo de estudo: " + erro.getMessage());
        } finally {
            fechar(conexao);
        }
    }

    private void removerMembro(String tabela, String coluna, GrupoEstudo grupo, int membroId) {
        Connection conexao = abrirConexao();
        try {
            PreparedStatement comando = conexao.prepareStatement(
                    "DELETE FROM " + tabela + " WHERE grupo_id = ? AND " + coluna + " = ?");
            comando.setInt(1, grupo.getId());
            comando.setInt(2, membroId);
            comando.executeUpdate();
            comando.close();
        } catch (SQLException erro) {
            throw new PersistenciaException("Erro ao retirar do grupo de estudo: " + erro.getMessage());
        } finally {
            fechar(conexao);
        }
    }
}
