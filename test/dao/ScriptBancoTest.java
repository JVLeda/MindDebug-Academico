package dao;

import java.nio.file.Files;
import java.nio.file.Path;

public class ScriptBancoTest {
    public static void main(String[] args) throws Exception {
        String script = Files.readString(Path.of("src", "MindDebugBD.sql")).toLowerCase();

        assertContains(script, "create database if not exists projeto_faculdade", "O script deve usar o banco configurado pela aplicacao.");
        assertContains(script, "unique (aula_id, estudante_id)", "O script deve impedir feedback duplicado.");
        assertContains(script, "check (nota between 0 and 10)", "O script deve validar a escala de notas.");
        assertContains(script, "primary key (materia_id, usuario_id)", "O script deve definir a chave da associacao de participantes.");
        assertContains(script, "unique (tentativa_id, pergunta_id)", "O script deve impedir resposta duplicada.");
    }

    private static void assertContains(String texto, String trecho, String mensagem) {
        if (!texto.contains(trecho)) {
            throw new AssertionError(mensagem);
        }
    }
}
