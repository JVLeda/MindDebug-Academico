package util;

public class ValidadorTest {
    public static void main(String[] args) {
        Validador validador = new Validador();

        assertEquals(0, validador.nota(0), "O limite inferior deve ser aceito.");
        assertEquals(10, validador.nota(10), "O limite superior deve ser aceito.");
        assertThrows(() -> validador.nota(-1), "Nota abaixo de zero deve ser rejeitada.");
        assertThrows(() -> validador.nota(11), "Nota acima de dez deve ser rejeitada.");
    }

    private static void assertEquals(int esperado, int atual, String mensagem) {
        if (esperado != atual) {
            throw new AssertionError(mensagem);
        }
    }

    private static void assertThrows(Runnable acao, String mensagem) {
        try {
            acao.run();
            throw new AssertionError(mensagem);
        } catch (RuntimeException esperado) {
            // A excecao confirma a fronteira definida pela regra de negocio.
        }
    }
}
