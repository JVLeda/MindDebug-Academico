package excecao;

/**
 * Um dado informado esta vazio, nao foi selecionado ou esta fora do formato.
 * Exemplo: nota fora do intervalo de 0 a 10.
 */
public class DadoInvalidoException extends MindDebugException {
    public DadoInvalidoException(String mensagem) {
        super(mensagem);
    }
}
