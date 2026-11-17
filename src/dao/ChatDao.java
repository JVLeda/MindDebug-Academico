package dao;

import excecao.PersistenciaException;
import excecao.RegraDeNegocioException;
import model.Aula;
import model.Chat;
import model.Materia;
import model.Mensagem;
import model.Usuario;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Acesso as tabelas chats e mensagens. Cada materia tem um chat GERAL e um de EQUIPE;
 * cada aula tem um chat AULA. Os chats sao criados pelo MateriaDao e pelo AulaDao.
 */
public class ChatDao extends AcessoBanco {
    // Trechos de WHERE que escolhem o chat; o parametro e o id da materia ou da aula.
    private static final String CHAT_GERAL = "c.materia_id = ? AND c.tipo = 'GERAL'";
    private static final String CHAT_EQUIPE = "c.materia_id = ? AND c.tipo = 'EQUIPE'";
    private static final String CHAT_DA_AULA = "c.aula_id = ?";

    // ------------------------------------------------------------------ consultas

    public ArrayList<Mensagem> listarDoChatGeral(Materia materia) {
        return listarMensagens(CHAT_GERAL, materia.getId());
    }

    public ArrayList<Mensagem> listarDoChatEquipe(Materia materia) {
        return listarMensagens(CHAT_EQUIPE, materia.getId());
    }

    public ArrayList<Mensagem> listarDoChatDaAula(Aula aula) {
        return listarMensagens(CHAT_DA_AULA, aula.getId());
    }

    private ArrayList<Mensagem> listarMensagens(String filtro, int id) {
        HashMap<Integer, Usuario> usuarios = new UsuarioDao().usuariosPorId();
        Connection conexao = abrirConexao();
        try {
            PreparedStatement consulta = conexao.prepareStatement(
                    "SELECT m.id, m.autor_id, m.texto, m.data_envio, m.hora_envio "
                            + "FROM mensagens m JOIN chats c ON c.id = m.chat_id WHERE " + filtro + " ORDER BY m.id");
            consulta.setInt(1, id);
            ResultSet resultado = consulta.executeQuery();
            ArrayList<Mensagem> mensagens = new ArrayList<>();
            while (resultado.next()) {
                mensagens.add(criarMensagem(resultado, usuarios.get(resultado.getInt("autor_id"))));
            }
            resultado.close();
            consulta.close();
            return mensagens;
        } catch (SQLException erro) {
            throw new PersistenciaException("Erro ao consultar mensagens: " + erro.getMessage());
        } finally {
            fechar(conexao);
        }
    }

    /** Usado pelo BancoDao: devolve, pelo id do chat, o objeto Chat que ja existe na materia ou na aula. */
    HashMap<Integer, Chat> listarChats(Connection conexao, HashMap<Integer, Materia> materias,
            HashMap<Integer, Aula> aulas) throws SQLException {
        HashMap<Integer, Chat> mapa = new HashMap<>();
        PreparedStatement consulta = conexao.prepareStatement("SELECT id, tipo, materia_id, aula_id FROM chats");
        ResultSet resultado = consulta.executeQuery();
        while (resultado.next()) {
            String tipo = resultado.getString("tipo");
            Materia materia = materias.get(resultado.getInt("materia_id"));
            Aula aula = aulas.get(resultado.getInt("aula_id"));
            Chat chat = null;
            if ("GERAL".equals(tipo) && materia != null) {
                chat = materia.getChatGeral();
            } else if ("EQUIPE".equals(tipo) && materia != null) {
                chat = materia.getChatEquipe();
            } else if ("AULA".equals(tipo) && aula != null) {
                chat = aula.getChat();
            }
            if (chat != null) {
                mapa.put(resultado.getInt("id"), chat);
            }
        }
        resultado.close();
        consulta.close();
        return mapa;
    }

    /** Usado pelo BancoDao: coloca cada mensagem dentro do seu chat. */
    void listarMensagens(Connection conexao, HashMap<Integer, Chat> chats, HashMap<Integer, Usuario> usuarios)
            throws SQLException {
        PreparedStatement consulta = conexao.prepareStatement(
                "SELECT id, chat_id, autor_id, texto, data_envio, hora_envio FROM mensagens ORDER BY id");
        ResultSet resultado = consulta.executeQuery();
        while (resultado.next()) {
            Chat chat = chats.get(resultado.getInt("chat_id"));
            Usuario autor = usuarios.get(resultado.getInt("autor_id"));
            if (chat != null && autor != null) {
                chat.adicionarMensagem(criarMensagem(resultado, autor));
            }
        }
        resultado.close();
        consulta.close();
    }

    private Mensagem criarMensagem(ResultSet resultado, Usuario autor) throws SQLException {
        Mensagem mensagem = new Mensagem(
                autor,
                resultado.getString("texto"),
                resultado.getDate("data_envio"),
                resultado.getString("hora_envio")
        );
        mensagem.setId(resultado.getInt("id"));
        return mensagem;
    }

    // ------------------------------------------------------------------ gravacao

    public void enviarNoChatGeral(Materia materia, Mensagem mensagem) {
        enviar(CHAT_GERAL, materia.getId(), mensagem);
    }

    public void enviarNoChatEquipe(Materia materia, Mensagem mensagem) {
        enviar(CHAT_EQUIPE, materia.getId(), mensagem);
    }

    public void enviarNoChatDaAula(Aula aula, Mensagem mensagem) {
        enviar(CHAT_DA_AULA, aula.getId(), mensagem);
    }

    private void enviar(String filtro, int id, Mensagem mensagem) {
        Connection conexao = abrirConexao();
        boolean gravou = false;
        try {
            conexao.setAutoCommit(false);
            int chatId = buscarIdDoChat(conexao, filtro, id);
            inserirMensagem(conexao, chatId, mensagem, false);
            int idGerado = lerUltimoId(conexao, "mensagens");

            conexao.commit();
            gravou = true;
            mensagem.setId(idGerado);
        } catch (SQLException erro) {
            throw new PersistenciaException("Erro ao enviar a mensagem. Nada foi gravado. Detalhe: " + erro.getMessage());
        } finally {
            if (!gravou) {
                desfazer(conexao);
            }
            fechar(conexao);
        }
    }

    public void removerMensagem(Mensagem mensagem) {
        Connection conexao = abrirConexao();
        try {
            PreparedStatement comando = conexao.prepareStatement("DELETE FROM mensagens WHERE id = ?");
            comando.setInt(1, mensagem.getId());
            comando.executeUpdate();
            comando.close();
        } catch (SQLException erro) {
            throw new PersistenciaException("Erro ao apagar a mensagem: " + erro.getMessage());
        } finally {
            fechar(conexao);
        }
    }

    /**
     * Usado pelo BancoDao, dentro da transacao dele: regrava todos os chats, com ids em sequencia,
     * e as mensagens de cada um com o id que ja tem.
     */
    void inserirTodos(Connection conexao, ArrayList<Materia> materias) throws SQLException {
        int chatId = 1;
        for (Materia materia : materias) {
            inserirChat(conexao, chatId, "GERAL", materia.getId(), 0, materia.getChatGeral());
            chatId++;
            inserirChat(conexao, chatId, "EQUIPE", materia.getId(), 0, materia.getChatEquipe());
            chatId++;
            for (Aula aula : materia.getAulas()) {
                inserirChat(conexao, chatId, "AULA", 0, aula.getId(), aula.getChat());
                chatId++;
            }
        }
    }

    private void inserirChat(Connection conexao, int chatId, String tipo, int materiaId, int aulaId, Chat chat)
            throws SQLException {
        PreparedStatement comando = conexao.prepareStatement(
                "INSERT INTO chats (id, tipo, materia_id, aula_id) VALUES (?, ?, ?, ?)");
        comando.setInt(1, chatId);
        comando.setString(2, tipo);
        definirInteiroOuNulo(comando, 3, materiaId);
        definirInteiroOuNulo(comando, 4, aulaId);
        comando.executeUpdate();
        comando.close();

        for (Mensagem mensagem : chat.getMensagens()) {
            inserirMensagem(conexao, chatId, mensagem, true);
        }
    }

    /** Com comId verdadeiro, grava o id que a mensagem ja tem; senao, o banco gera o id. */
    private void inserirMensagem(Connection conexao, int chatId, Mensagem mensagem, boolean comId) throws SQLException {
        PreparedStatement comando;
        int posicao = 1;
        if (comId) {
            comando = conexao.prepareStatement(
                    "INSERT INTO mensagens (id, chat_id, autor_id, texto, data_envio, hora_envio) VALUES (?, ?, ?, ?, ?, ?)");
            comando.setInt(posicao, mensagem.getId());
            posicao++;
        } else {
            comando = conexao.prepareStatement(
                    "INSERT INTO mensagens (chat_id, autor_id, texto, data_envio, hora_envio) VALUES (?, ?, ?, ?, ?)");
        }
        comando.setInt(posicao, chatId);
        comando.setInt(posicao + 1, mensagem.getAutor().getId());
        comando.setString(posicao + 2, mensagem.getTexto());
        definirData(comando, posicao + 3, mensagem.getData());
        comando.setString(posicao + 4, mensagem.getHora());
        comando.executeUpdate();
        comando.close();
    }

    private int buscarIdDoChat(Connection conexao, String filtro, int id) throws SQLException {
        PreparedStatement consulta = conexao.prepareStatement("SELECT c.id FROM chats c WHERE " + filtro);
        consulta.setInt(1, id);
        ResultSet resultado = consulta.executeQuery();
        int chatId = 0;
        if (resultado.next()) {
            chatId = resultado.getInt("id");
        }
        resultado.close();
        consulta.close();
        if (chatId == 0) {
            throw new RegraDeNegocioException("Chat nao encontrado no banco.");
        }
        return chatId;
    }
}
