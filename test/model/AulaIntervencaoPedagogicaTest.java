package model;

public class AulaIntervencaoPedagogicaTest {
    public static void main(String[] args) {
        Curso curso = new Curso("Engenharia", "EC", "Exatas", 3600, "Coordenacao");
        Estudante aluno1 = new Estudante("Aluno 1", "a1@teste.edu", "2026001", "1234", curso);
        Estudante aluno2 = new Estudante("Aluno 2", "a2@teste.edu", "2026002", "1234", curso);

        Aula semFeedback = new Aula("Aula sem retorno", "01/01/2026", "Introducao");
        verificar("Aguardar feedback da turma".equals(semFeedback.getAcaoPedagogica()),
                "Aula sem feedback deve pedir retorno da turma");

        Aula aulaCritica = new Aula("Heranca", "02/01/2026", "Heranca e polimorfismo");
        aulaCritica.registrarFeedback(new Feedback(aluno1, 3, "Dificil"));
        aulaCritica.registrarFeedback(new Feedback(aluno2, 4, "Preciso revisar"));
        verificar("Planejar acao de reforco".equals(aulaCritica.getAcaoPedagogica()),
                "Aula critica deve recomendar uma acao de reforco");

        Aula aulaComDominio = new Aula("Classes", "03/01/2026", "Objetos e classes");
        aulaComDominio.registrarFeedback(new Feedback(aluno1, 8, "Entendi"));
        aulaComDominio.registrarFeedback(new Feedback(aluno2, 9, "Entendi"));
        verificar("Manter acompanhamento".equals(aulaComDominio.getAcaoPedagogica()),
                "Aula com dominio deve manter acompanhamento");

        verificar("50%".equals(aulaCritica.getParticipacaoDaTurma(4)),
                "Participacao deve usar feedbacks enviados sobre o total de estudantes");
    }

    private static void verificar(boolean condicao, String mensagem) {
        if (!condicao) {
            throw new AssertionError(mensagem);
        }
    }
}
