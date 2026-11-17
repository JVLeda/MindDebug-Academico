package dao;

public class DiagnosticoBancoTest {
    public static void main(String[] args) {
        String mensagem = AcessoBanco.mensagemDeConexaoIndisponivel("Connection refused");

        assertContains(mensagem, "MySQL", "A mensagem deve identificar o servico necessario.");
        assertContains(mensagem, util.Conexao.descricao(), "A mensagem deve indicar o endereco configurado em Conexao.");
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
