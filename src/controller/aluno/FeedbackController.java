package controller.aluno;

import controller.ControllerBase;
import dao.BancoDados;
import dao.FeedbackDao;
import excecao.AcessoNegadoException;
import model.Aula;
import model.Feedback;
import model.Usuario;

/** Feedback de entendimento da aula, modulo do Aluno. */
public class FeedbackController extends ControllerBase {
    private FeedbackDao feedbackDao = new FeedbackDao();

    public FeedbackController(BancoDados banco) {
        super(banco);
    }

    /** Se o aluno ja avaliou a aula, a nota e o comentario sao trocados. */
    public void registrarFeedback(Usuario usuarioLogado, Aula aula, int nota, String comentario) {
        if (usuarioLogado == null || !usuarioLogado.podeRegistrarFeedback()) {
            throw new AcessoNegadoException("Somente aluno registra feedback.");
        }
        Feedback feedback = usuarioLogado.criarFeedback(validador.nota(nota), comentario);
        feedbackDao.registrar(aula, feedback);
        aula.registrarFeedback(feedback);
    }
}
