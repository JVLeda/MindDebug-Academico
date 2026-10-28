package model;

public class AdministradorTest {
    public static void main(String[] args) {
        Administrador administrador = new Administrador(
                "Administrador do Sistema", "admin", "admin@minddebug.edu.br", "1234"
        );

        assertEquals(4, administrador.getNivel(), "Administrador deve ter nivel 4.");
        assertEquals("Administrador do Sistema", administrador.getCargo(), "Cargo exibido incorreto.");
        assertTrue(administrador.podeModerar(), "Administrador deve poder moderar.");
        Estudante estudante = new Estudante("Aluno", "aluno@minddebug.edu.br", "2026001", "1234", null);
        assertTrue(!administrador.podeApagarConteudoDe(estudante),
                "Administrador nao deve apagar conteudo pedagogico de outro usuario.");
    }

    private static void assertEquals(Object esperado, Object atual, String mensagem) {
        if (!esperado.equals(atual)) {
            throw new AssertionError(mensagem + " Esperado: " + esperado + ", atual: " + atual);
        }
    }

    private static void assertTrue(boolean condicao, String mensagem) {
        if (!condicao) {
            throw new AssertionError(mensagem);
        }
    }
}
