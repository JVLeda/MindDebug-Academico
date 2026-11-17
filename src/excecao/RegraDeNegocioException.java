package excecao;

/**
 * Os dados estao preenchidos, mas a operacao quebra uma regra do sistema.
 * Exemplo: cadastrar um login que ja existe.
 */
public class RegraDeNegocioException extends MindDebugException {
    public RegraDeNegocioException(String mensagem) {
        super(mensagem);
    }
}
