package model;

public class AulaStatusTest {
    public static void main(String[] args) {
        testarMediaEsperadaPadrao();
        testarStatusComMediaEsperadaPadrao();
        testarStatusComOutraMediaEsperada();
        testarMediaEsperadaNosExtremos();
        testarAlertaDeReforco();
        testarDescricao();
    }

    private static void testarMediaEsperadaPadrao() {
        Aula aula = new Aula("Aula 1", "01/01/2026", "Objetos e classes");
        verificar(aula.getMediaEsperada() == 7.0, "A media esperada padrao e 7.");
        verificar(aula.getMediaEsperada() == Aula.MEDIA_ESPERADA_PADRAO, "A media padrao fica numa constante.");

        aula.setMediaEsperada(8.5);
        verificar(aula.getMediaEsperada() == 8.5, "O professor define a media esperada da aula.");
    }

    private static void testarStatusComMediaEsperadaPadrao() {
        conferir(7.0, new int[]{}, StatusAula.SEM_DADOS, "Sem feedback.");
        conferir(7.0, new int[]{3}, StatusAula.CRITICO, "Media 3, esperada 7.");
        conferir(7.0, new int[]{3, 4}, StatusAula.CRITICO, "Media 3,5, esperada 7.");
        conferir(7.0, new int[]{4}, StatusAula.REGULAR, "Media 4, esperada 7: comeco da faixa regular.");
        conferir(7.0, new int[]{6, 7}, StatusAula.REGULAR, "Media 6,5, esperada 7.");
        conferir(7.0, new int[]{7}, StatusAula.SATISFATORIO, "Media 7, esperada 7: atingiu a media esperada.");
        conferir(7.0, new int[]{10}, StatusAula.SATISFATORIO, "Media 10, esperada 7.");

        verificar("Sem dados".equals(aulaComNotas(7.0, new int[]{}).getStatus()), "Rotulo de aula sem feedback.");
        verificar("Critico".equals(aulaComNotas(7.0, new int[]{3}).getStatus()), "Rotulo de aula critica.");
        verificar("Regular".equals(aulaComNotas(7.0, new int[]{5}).getStatus()), "Rotulo de aula regular.");
        verificar("Satisfatorio".equals(aulaComNotas(7.0, new int[]{8}).getStatus()), "Rotulo de aula satisfatoria.");
    }

    private static void testarStatusComOutraMediaEsperada() {
        conferir(8.0, new int[]{4}, StatusAula.CRITICO, "Media 4, esperada 8.");
        conferir(8.0, new int[]{5}, StatusAula.REGULAR, "Media 5, esperada 8: tres pontos abaixo ainda e regular.");
        conferir(8.0, new int[]{7}, StatusAula.REGULAR, "Media 7, esperada 8.");
        conferir(8.0, new int[]{8}, StatusAula.SATISFATORIO, "Media 8, esperada 8.");
    }

    private static void testarMediaEsperadaNosExtremos() {
        conferir(10.0, new int[]{6}, StatusAula.CRITICO, "Media 6, esperada 10.");
        conferir(10.0, new int[]{9}, StatusAula.REGULAR, "Media 9, esperada 10.");
        conferir(10.0, new int[]{10}, StatusAula.SATISFATORIO, "Media 10, esperada 10.");
        conferir(2.0, new int[]{0}, StatusAula.REGULAR, "Media 0, esperada 2: nao ha faixa critica abaixo de zero.");
        conferir(2.0, new int[]{2}, StatusAula.SATISFATORIO, "Media 2, esperada 2.");
        conferir(0.0, new int[]{0}, StatusAula.SATISFATORIO, "Media 0, esperada 0.");
    }

    private static void testarAlertaDeReforco() {
        Aula semFeedback = aulaComNotas(8.0, new int[]{});
        verificar(!semFeedback.precisaAcaoDeReforco(), "Aula sem feedback nao gera alerta de reforco.");
        verificar("Aguardar feedback da turma".equals(semFeedback.getAcaoPedagogica()),
                "Aula sem feedback pede o retorno da turma.");

        Aula abaixoDaEsperada = aulaComNotas(8.0, new int[]{7});
        verificar(abaixoDaEsperada.precisaAcaoDeReforco(),
                "Media 7 com esperada 8 gera alerta, mesmo sendo acima do antigo limite fixo.");
        verificar("Planejar acao de reforco".equals(abaixoDaEsperada.getAcaoPedagogica()),
                "Aula abaixo da media esperada recomenda reforco.");

        Aula naEsperada = aulaComNotas(6.0, new int[]{6});
        verificar(!naEsperada.precisaAcaoDeReforco(),
                "Media 6 com esperada 6 nao gera alerta, mesmo sendo abaixo do antigo limite fixo.");
        verificar("Manter acompanhamento".equals(naEsperada.getAcaoPedagogica()),
                "Aula que atingiu a media esperada so pede acompanhamento.");
    }

    private static void testarDescricao() {
        Aula aula = new Aula("Aula 1", "01/01/2026", "Objetos e classes");
        verificar("Objetos e classes".equals(aula.getDescricao()), "A aula guarda a descricao do conteudo.");
        verificar("Aula 1".equals(aula.toString()), "O texto da aula continua sendo o titulo.");
    }

    private static void conferir(double mediaEsperada, int[] notas, StatusAula esperado, String caso) {
        StatusAula atual = aulaComNotas(mediaEsperada, notas).getStatusAula();
        verificar(atual == esperado, caso + " Esperado: " + esperado.getNome() + ", atual: " + atual.getNome());
    }

    private static Aula aulaComNotas(double mediaEsperada, int[] notas) {
        Aula aula = new Aula("Aula", "01/01/2026", "Conteudo");
        aula.setMediaEsperada(mediaEsperada);
        for (int i = 0; i < notas.length; i++) {
            Estudante aluno = new Estudante("Aluno " + i, "aluno" + i + "@teste.edu", "20260" + i, "segredo1", null);
            aula.registrarFeedback(new Feedback(aluno, notas[i], ""));
        }
        return aula;
    }

    private static void verificar(boolean condicao, String mensagem) {
        if (!condicao) {
            throw new AssertionError(mensagem);
        }
    }
}
