package dao;

public class EsquemaBancoTest {
    public static void main(String[] args) {
        String ddl = String.join("\n", EsquemaBanco.comandosDeCriacao()).toLowerCase();

        assertContains(ddl, "login varchar(50) not null unique", "Login deve ser unico.");
        assertContains(ddl, "unique (aula_id, estudante_id)", "Feedback deve ser unico por aluno e aula.");
        assertContains(ddl, "check (nota between 0 and 10)", "Nota deve respeitar a escala de 0 a 10.");
        assertContains(ddl, "primary key (materia_id, usuario_id)", "Participantes deve usar chave composta.");
        assertContains(ddl, "unique (tentativa_id, pergunta_id)", "Resposta deve ser unica por tentativa e pergunta.");
        assertContains(ddl, "foreign key (curso_id) references cursos(id)", "Usuario deve referenciar curso.");
    }

    private static void assertContains(String texto, String esperado, String mensagem) {
        if (!texto.contains(esperado)) {
            throw new AssertionError(mensagem + " Trecho ausente: " + esperado);
        }
    }
}
