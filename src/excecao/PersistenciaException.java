package excecao;

/**
 * Falha ao ler ou gravar no banco de dados.
 */
public class PersistenciaException extends MindDebugException {
    public PersistenciaException(String mensagem) {
        super(mensagem);
    }
}
