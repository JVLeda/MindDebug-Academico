package controller.comum;

import controller.ControllerBase;
import dao.BancoDados;
import dao.ChatDao;
import excecao.AcessoNegadoException;
import excecao.DadoInvalidoException;
import excecao.RegraDeNegocioException;
import model.Aula;
import model.Chat;
import model.Curso;
import model.Materia;
import model.Mensagem;
import model.Periodo;
import model.Usuario;

import java.util.ArrayList;

/** Mensagens nos chats da materia (geral e equipe) e da aula, usados por todos os atores. */
public class ChatController extends ControllerBase {
    private ChatDao chatDao = new ChatDao();

    public ChatController(BancoDados banco) {
        super(banco);
    }

    public void enviarMensagem(Chat chat, Usuario autor, String texto) {
        Mensagem mensagem = new Mensagem(autor, validador.texto(texto, "Mensagem"));
        gravarMensagem(chat, mensagem);
        chat.adicionarMensagem(mensagem);
    }

    public void apagarMensagem(Chat chat, Usuario usuarioLogado, Mensagem mensagem) {
        if (mensagem == null) {
            throw new DadoInvalidoException("Selecione uma mensagem.");
        }
        if (!usuarioLogado.podeApagarConteudoDe(mensagem.getAutor())) {
            throw new AcessoNegadoException("Sem permissao para apagar esta mensagem.");
        }
        chatDao.removerMensagem(mensagem);
        chat.apagar(mensagem);
    }

    /** O chat nao sabe a quem pertence; procura a materia ou a aula dona dele para gravar no chat certo. */
    private void gravarMensagem(Chat chat, Mensagem mensagem) {
        for (Materia materia : listarTodasAsMaterias()) {
            if (materia.getChatGeral() == chat) {
                chatDao.enviarNoChatGeral(materia, mensagem);
                return;
            }
            if (materia.getChatEquipe() == chat) {
                chatDao.enviarNoChatEquipe(materia, mensagem);
                return;
            }
            for (Aula aula : materia.getAulas()) {
                if (aula.getChat() == chat) {
                    chatDao.enviarNoChatDaAula(aula, mensagem);
                    return;
                }
            }
        }
        throw new RegraDeNegocioException("Chat nao encontrado.");
    }

    private ArrayList<Materia> listarTodasAsMaterias() {
        ArrayList<Materia> materias = new ArrayList<>();
        for (Curso curso : banco.getCursos()) {
            for (Periodo periodo : curso.getPeriodos()) {
                materias.addAll(periodo.getMaterias());
            }
        }
        return materias;
    }
}
