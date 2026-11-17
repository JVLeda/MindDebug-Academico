package excecao;

/**
 * Excecao base do MindDebug.
 * Toda falha prevista pelo sistema usa uma subclasse desta classe,
 * para que a tela consiga tratar todas do mesmo jeito.
 */
public class MindDebugException extends RuntimeException {
    public MindDebugException(String mensagem) {
        super(mensagem);
    }
}
