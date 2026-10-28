package dao;

public class DiagnosticoBancoTest {
    public static void main(String[] args) {
        String mensagem = BancoDao.mensagemDeConexaoIndisponivel("Connection refused");

        assertContains(mensagem, "MySQL", "A mensagem deve identificar o servico necessario.");
        assertContains(mensagem, "localhost:3307", "A mensagem deve indicar o endereco configurado.");
        assertContains(mensagem, "Connection refused", "A mensagem deve preservar o detalhe tecnico.");
        assertFalse(mensagem.toLowerCase().contains("senha"), "A mensagem nao deve expor segredo.");
    }

    private static void assertContains(String texto, String trecho, String mensagem) {
        if (!texto.contains(trecho)) {
            throw new AssertionError(mensagem);
        }
    }

    private static void assertFalse(boolean condicao, String mensagem) {
        if (condicao) {
            throw new AssertionError(mensagem);
        }
    }
}
