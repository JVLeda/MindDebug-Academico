package util;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

public class ConexaoTest {
    public static void main(String[] args) throws FileNotFoundException {
        testarUrlUsaConfiguracao();
        testarDescricaoNaoExpoeSenha();
        testarComposeAlinhadoComConexao();
        testarSemUsuarioRoot();
    }

    private static void testarUrlUsaConfiguracao() {
        assertEquals("jdbc:mysql://localhost:3306/minddebug", Conexao.getUrl(), "A URL deve usar host, porta e banco da Conexao.");
    }

    private static void testarDescricaoNaoExpoeSenha() {
        assertEquals("localhost:3306/minddebug", Conexao.descricao(), "A descricao deve mostrar apenas host, porta e banco.");
    }

    private static void testarComposeAlinhadoComConexao() throws FileNotFoundException {
        String compose = lerArquivo("compose.yaml");
        assertContains(compose, "MYSQL_DATABASE: " + Conexao.BANCO, "compose.yaml deve criar o mesmo banco da Conexao.");
        assertContains(compose, "MYSQL_USER: " + Conexao.USUARIO, "compose.yaml deve criar o mesmo usuario da Conexao.");
        assertContains(compose, "MYSQL_PASSWORD: " + Conexao.SENHA, "compose.yaml deve usar a mesma senha da Conexao.");
        assertContains(compose, "\"" + Conexao.PORTA + ":3306\"", "compose.yaml deve publicar a mesma porta da Conexao.");
    }

    private static void testarSemUsuarioRoot() {
        assertFalse(Conexao.USUARIO.equals("root"), "A aplicacao nao deve usar o usuario root.");
    }

    private static String lerArquivo(String caminho) throws FileNotFoundException {
        Scanner leitor = new Scanner(new File(caminho), "UTF-8");
        String texto = "";
        try {
            while (leitor.hasNextLine()) {
                texto = texto + leitor.nextLine() + "\n";
            }
        } finally {
            leitor.close();
        }
        return texto;
    }

    private static void assertEquals(String esperado, String atual, String mensagem) {
        if (!esperado.equals(atual)) {
            throw new AssertionError(mensagem + " Esperado: " + esperado + ", atual: " + atual);
        }
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
