package dao;

import excecao.PersistenciaException;
import excecao.RegraDeNegocioException;
import model.DiaSemana;
import model.HorarioMonitor;
import model.Materia;
import model.Monitor;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Acesso a tabela horarios_monitor. As horas ficam no formato HH:mm, entao comparar
 * os textos no SQL da o mesmo resultado que comparar as horas.
 */
public class HorarioMonitorDao extends AcessoBanco {

    /** Horarios do monitor. Cada horario traz a materia com id e nome. */
    public ArrayList<HorarioMonitor> listarDoMonitor(Monitor monitor) {
        Connection conexao = abrirConexao();
        try {
            PreparedStatement consulta = conexao.prepareStatement(
                    "SELECT h.id, h.dia, h.hora_inicio, h.hora_fim, m.id AS materia_id, m.nome AS materia_nome "
                            + "FROM horarios_monitor h JOIN materias m ON m.id = h.materia_id "
                            + "WHERE h.monitor_id = ? ORDER BY h.id");
            consulta.setInt(1, monitor.getId());
            ResultSet resultado = consulta.executeQuery();
            ArrayList<HorarioMonitor> horarios = new ArrayList<>();
            HashMap<Integer, Materia> materias = new HashMap<>();
            while (resultado.next()) {
                int materiaId = resultado.getInt("materia_id");
                if (!materias.containsKey(materiaId)) {
                    Materia materia = new Materia(resultado.getString("materia_nome"), null);
                    materia.setId(materiaId);
                    materias.put(materiaId, materia);
                }
                horarios.add(criarHorario(resultado, materias.get(materiaId)));
            }
            resultado.close();
            consulta.close();
            return horarios;
        } catch (SQLException erro) {
            throw new PersistenciaException("Erro ao consultar os horarios do monitor: " + erro.getMessage());
        } finally {
            fechar(conexao);
        }
    }

    /** Usado pelo BancoDao: coloca cada horario no seu monitor. */
    void listar(Connection conexao, HashMap<Integer, Materia> materias, HashMap<Integer, Monitor> monitores)
            throws SQLException {
        PreparedStatement consulta = conexao.prepareStatement(
                "SELECT id, monitor_id, materia_id, dia, hora_inicio, hora_fim FROM horarios_monitor ORDER BY id");
        ResultSet resultado = consulta.executeQuery();
        while (resultado.next()) {
            Monitor monitor = monitores.get(resultado.getInt("monitor_id"));
            Materia materia = materias.get(resultado.getInt("materia_id"));
            if (monitor != null && materia != null) {
                monitor.adicionarHorario(criarHorario(resultado, materia));
            }
        }
        resultado.close();
        consulta.close();
    }

    private HorarioMonitor criarHorario(ResultSet resultado, Materia materia) throws SQLException {
        HorarioMonitor horario = new HorarioMonitor(
                materia,
                DiaSemana.buscarPorNome(resultado.getString("dia")),
                resultado.getString("hora_inicio"),
                resultado.getString("hora_fim")
        );
        horario.setId(resultado.getInt("id"));
        return horario;
    }

    /** Cadastra o horario se ele nao se sobrepoe a outro horario do mesmo monitor no mesmo dia. */
    public void inserir(Monitor monitor, HorarioMonitor horario) {
        Connection conexao = abrirConexao();
        boolean gravou = false;
        try {
            conexao.setAutoCommit(false);
            PreparedStatement consulta = conexao.prepareStatement(
                    "SELECT COUNT(*) FROM horarios_monitor "
                            + "WHERE monitor_id = ? AND dia = ? AND hora_inicio < ? AND hora_fim > ?");
            consulta.setInt(1, monitor.getId());
            consulta.setString(2, horario.getDia().getNome());
            consulta.setString(3, horario.getHoraFim());
            consulta.setString(4, horario.getHoraInicio());
            if (contar(consulta) > 0) {
                throw new RegraDeNegocioException("O horario conflita com outro horario do monitor.");
            }

            inserir(conexao, monitor, horario);
            int idGerado = lerUltimoId(conexao, "horarios_monitor");

            conexao.commit();
            gravou = true;
            horario.setId(idGerado);
        } catch (SQLException erro) {
            throw new PersistenciaException("Erro ao cadastrar o horario. Nada foi gravado. Detalhe: " + erro.getMessage());
        } finally {
            if (!gravou) {
                desfazer(conexao);
            }
            fechar(conexao);
        }
    }

    /** Tambem usado pelo BancoDao, dentro da transacao dele. */
    void inserir(Connection conexao, Monitor monitor, HorarioMonitor horario) throws SQLException {
        PreparedStatement comando = conexao.prepareStatement(
                "INSERT INTO horarios_monitor (monitor_id, materia_id, dia, hora_inicio, hora_fim) VALUES (?, ?, ?, ?, ?)");
        comando.setInt(1, monitor.getId());
        comando.setInt(2, horario.getMateria().getId());
        comando.setString(3, horario.getDia().getNome());
        comando.setString(4, horario.getHoraInicio());
        comando.setString(5, horario.getHoraFim());
        comando.executeUpdate();
        comando.close();
    }

    public void remover(HorarioMonitor horario) {
        Connection conexao = abrirConexao();
        try {
            PreparedStatement comando = conexao.prepareStatement("DELETE FROM horarios_monitor WHERE id = ?");
            comando.setInt(1, horario.getId());
            comando.executeUpdate();
            comando.close();
        } catch (SQLException erro) {
            throw new PersistenciaException("Erro ao remover o horario: " + erro.getMessage());
        } finally {
            fechar(conexao);
        }
    }
}
