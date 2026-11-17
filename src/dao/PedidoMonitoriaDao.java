package dao;

import excecao.PersistenciaException;
import excecao.RegraDeNegocioException;
import model.Aula;
import model.Estudante;
import model.Materia;
import model.Monitor;
import model.PedidoMonitoria;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;

/** Acesso a tabela pedidos_monitoria. O status e Solicitado, Aceito ou Recusado (CHECK da tabela). */
public class PedidoMonitoriaDao extends AcessoBanco {
    private static final String CONSULTA =
            "SELECT id, materia_id, estudante_id, aula_id, monitor_id, duvida, status, data_monitoria, horario "
                    + "FROM pedidos_monitoria ";

    // ------------------------------------------------------------------ consultas

    public ArrayList<PedidoMonitoria> listarDaMateria(Materia materia) {
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
            ArrayList<PedidoMonitoria> pedidos = new ArrayList<>();
            while (resultado.next()) {
                Estudante estudante = estudantes.get(resultado.getInt("estudante_id"));
                pedidos.add(criarPedido(resultado, estudante, aulas, monitores));
            }
            resultado.close();
            consulta.close();
            return pedidos;
        } catch (SQLException erro) {
            throw new PersistenciaException("Erro ao consultar pedidos de monitoria: " + erro.getMessage());
        } finally {
            fechar(conexao);
        }
    }

    /** Usado pelo BancoDao: coloca cada pedido dentro da sua materia. */
    void listar(Connection conexao, HashMap<Integer, Materia> materias, HashMap<Integer, Aula> aulas,
            HashMap<Integer, Estudante> estudantes, HashMap<Integer, Monitor> monitores) throws SQLException {
        PreparedStatement consulta = conexao.prepareStatement(CONSULTA + "ORDER BY id");
        ResultSet resultado = consulta.executeQuery();
        while (resultado.next()) {
            Materia materia = materias.get(resultado.getInt("materia_id"));
            Estudante estudante = estudantes.get(resultado.getInt("estudante_id"));
            if (materia != null && estudante != null) {
                materia.adicionarPedidoMonitoria(criarPedido(resultado, estudante, aulas, monitores));
            }
        }
        resultado.close();
        consulta.close();
    }

    private PedidoMonitoria criarPedido(ResultSet resultado, Estudante estudante, HashMap<Integer, Aula> aulas,
            HashMap<Integer, Monitor> monitores) throws SQLException {
        Monitor monitor = monitores.get(resultado.getInt("monitor_id"));
        PedidoMonitoria pedido = new PedidoMonitoria(
                estudante,
                aulas.get(resultado.getInt("aula_id")),
                monitor,
                resultado.getString("duvida"),
                textoDaData(resultado.getDate("data_monitoria")),
                textoOuVazio(resultado.getString("horario"))
        );
        pedido.setId(resultado.getInt("id"));

        String status = resultado.getString("status");
        if ("Aceito".equals(status)) {
            pedido.aceitar(monitor);
        } else if ("Recusado".equals(status)) {
            pedido.recusar();
        }
        return pedido;
    }

    // ------------------------------------------------------------------ gravacao

    public void inserir(Materia materia, PedidoMonitoria pedido) {
        Connection conexao = abrirConexao();
        boolean gravou = false;
        try {
            conexao.setAutoCommit(false);
            PreparedStatement comando = conexao.prepareStatement(
                    "INSERT INTO pedidos_monitoria (materia_id, estudante_id, aula_id, monitor_id, duvida, status, "
                            + "data_monitoria, horario) VALUES (?, ?, ?, ?, ?, ?, ?, ?)");
            comando.setInt(1, materia.getId());
            preencherPedido(comando, 2, pedido);
            comando.executeUpdate();
            comando.close();
            int idGerado = lerUltimoId(conexao, "pedidos_monitoria");

            conexao.commit();
            gravou = true;
            pedido.setId(idGerado);
        } catch (SQLException erro) {
            throw new PersistenciaException("Erro ao registrar o pedido de monitoria. Nada foi gravado. Detalhe: "
                    + erro.getMessage());
        } finally {
            if (!gravou) {
                desfazer(conexao);
            }
            fechar(conexao);
        }
    }

    /** Usado pelo BancoDao, dentro da transacao dele. */
    void inserirComId(Connection conexao, Materia materia, PedidoMonitoria pedido) throws SQLException {
        PreparedStatement comando = conexao.prepareStatement(
                "INSERT INTO pedidos_monitoria (id, materia_id, estudante_id, aula_id, monitor_id, duvida, status, "
                        + "data_monitoria, horario) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)");
        comando.setInt(1, pedido.getId());
        comando.setInt(2, materia.getId());
        preencherPedido(comando, 3, pedido);
        comando.executeUpdate();
        comando.close();
    }

    /**
     * Grava o novo status e o monitor do pedido. O controller chama antes de mudar o objeto,
     * para que a memoria so mude se o banco aceitar.
     */
    public void atualizarStatus(PedidoMonitoria pedido, String status, Monitor monitor) {
        Connection conexao = abrirConexao();
        try {
            PreparedStatement comando = conexao.prepareStatement(
                    "UPDATE pedidos_monitoria SET status = ?, monitor_id = ? WHERE id = ?");
            comando.setString(1, status);
            definirIdOuNulo(comando, 2, monitor);
            comando.setInt(3, pedido.getId());
            int alteradas = comando.executeUpdate();
            comando.close();
            if (alteradas == 0) {
                throw new RegraDeNegocioException("Pedido de monitoria nao encontrado no banco.");
            }
        } catch (SQLException erro) {
            throw new PersistenciaException("Erro ao atualizar o pedido de monitoria: " + erro.getMessage());
        } finally {
            fechar(conexao);
        }
    }

    /**
     * Fica restrito ao pacote dao, sem uso pelas telas: o sistema nao tem a opcao de cancelar pedido.
     * Serve para os testes de integracao limparem o que criaram.
     */
    void remover(PedidoMonitoria pedido) {
        Connection conexao = abrirConexao();
        try {
            PreparedStatement comando = conexao.prepareStatement("DELETE FROM pedidos_monitoria WHERE id = ?");
            comando.setInt(1, pedido.getId());
            comando.executeUpdate();
            comando.close();
        } catch (SQLException erro) {
            throw new PersistenciaException("Erro ao apagar: " + erro.getMessage());
        } finally {
            fechar(conexao);
        }
    }

    /** Preenche estudante, aula, monitor, duvida, status, data e horario a partir da posicao dada. */
    private void preencherPedido(PreparedStatement comando, int inicio, PedidoMonitoria pedido) throws SQLException {
        comando.setInt(inicio, pedido.getEstudante().getId());
        definirIdOuNulo(comando, inicio + 1, pedido.getAula());
        definirIdOuNulo(comando, inicio + 2, pedido.getMonitor());
        comando.setString(inicio + 3, pedido.getDuvida());
        comando.setString(inicio + 4, pedido.getStatus());
        definirData(comando, inicio + 5, converterTextoEmData(pedido.getData()));
        definirTextoOuNulo(comando, inicio + 6, pedido.getHorario());
    }
}
