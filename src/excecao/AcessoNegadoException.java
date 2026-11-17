package excecao;

/**
 * O usuario tentou fazer algo que o perfil dele nao permite,
 * ou tentou entrar com login ou senha errados.
 */
public class AcessoNegadoException extends MindDebugException {
    public AcessoNegadoException(String mensagem) {
        super(mensagem);
    }
}
