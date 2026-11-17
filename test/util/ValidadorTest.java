package util;

import excecao.DadoInvalidoException;

import java.text.SimpleDateFormat;
import java.util.Date;

public class ValidadorTest {
    public static void main(String[] args) {
        testarNota();
        testarPositivo();
        testarSemestre();
        testarMediaEsperada();
        testarData();
    }

    private static void testarNota() {
        Validador validador = new Validador();

        assertEquals(0, validador.nota(0), "O limite inferior deve ser aceito.");
        assertEquals(10, validador.nota(10), "O limite superior deve ser aceito.");
        assertTrue(notaFoiRejeitada(validador, -1), "Nota abaixo de zero deve ser rejeitada.");
        assertTrue(notaFoiRejeitada(validador, 11), "Nota acima de dez deve ser rejeitada.");
    }

    private static boolean notaFoiRejeitada(Validador validador, int nota) {
        try {
            validador.nota(nota);
            return false;
        } catch (DadoInvalidoException esperado) {
            return true;
        }
    }

    private static void testarPositivo() {
        Validador validador = new Validador();

        assertEquals(1, validador.positivo(1, "Carga horaria"), "Um e o menor valor aceito.");
        assertEquals(60, validador.positivo(60, "Carga horaria"), "Valor positivo deve ser aceito.");
        assertTrue(positivoFoiRejeitado(validador, 0), "Zero deve ser rejeitado.");
        assertTrue(positivoFoiRejeitado(validador, -1), "Valor negativo deve ser rejeitado.");
    }

    private static boolean positivoFoiRejeitado(Validador validador, int valor) {
        try {
            validador.positivo(valor, "Carga horaria");
            return false;
        } catch (DadoInvalidoException esperado) {
            return true;
        }
    }

    private static void testarSemestre() {
        Validador validador = new Validador();

        assertEquals(1, validador.semestre(1), "Primeiro semestre deve ser aceito.");
        assertEquals(2, validador.semestre(2), "Segundo semestre deve ser aceito.");
        assertTrue(semestreFoiRejeitado(validador, 0), "Semestre zero deve ser rejeitado.");
        assertTrue(semestreFoiRejeitado(validador, 3), "Semestre tres deve ser rejeitado.");
    }

    private static boolean semestreFoiRejeitado(Validador validador, int semestre) {
        try {
            validador.semestre(semestre);
            return false;
        } catch (DadoInvalidoException esperado) {
            return true;
        }
    }

    private static void testarMediaEsperada() {
        Validador validador = new Validador();

        assertTrue(validador.mediaEsperada(0.0) == 0.0, "Zero e a menor media esperada aceita.");
        assertTrue(validador.mediaEsperada(10.0) == 10.0, "Dez e a maior media esperada aceita.");
        assertTrue(validador.mediaEsperada(7.5) == 7.5, "Media esperada com casa decimal deve ser aceita.");
        assertTrue(mediaEsperadaFoiRejeitada(validador, -0.1), "Media esperada abaixo de zero deve ser rejeitada.");
        assertTrue(mediaEsperadaFoiRejeitada(validador, 10.1), "Media esperada acima de dez deve ser rejeitada.");
    }

    private static boolean mediaEsperadaFoiRejeitada(Validador validador, double media) {
        try {
            validador.mediaEsperada(media);
            return false;
        } catch (DadoInvalidoException esperado) {
            return true;
        }
    }

    private static void testarData() {
        Validador validador = new Validador();
        SimpleDateFormat formato = new SimpleDateFormat("dd/MM/yyyy");

        Date data = validador.data("17/12/2026", "Data");
        assertTrue("17/12/2026".equals(formato.format(data)), "Data valida deve ser lida.");
        Date comEspacos = validador.data("  29/02/2028  ", "Data");
        assertTrue("29/02/2028".equals(formato.format(comEspacos)), "Espacos nas pontas sao ignorados.");

        assertTrue(dataFoiRejeitada(validador, "31/02/2026"), "Dia que nao existe deve ser rejeitado.");
        assertTrue(dataFoiRejeitada(validador, "29/02/2026"), "29 de fevereiro fora de ano bissexto deve ser rejeitado.");
        assertTrue(dataFoiRejeitada(validador, "17-12-2026"), "Separador errado deve ser rejeitado.");
        assertTrue(dataFoiRejeitada(validador, "1/2/2026"), "Dia e mes sem zero a esquerda devem ser rejeitados.");
        assertTrue(dataFoiRejeitada(validador, "17/12/26"), "Ano com dois digitos deve ser rejeitado.");
        assertTrue(dataFoiRejeitada(validador, "17/12/20a6"), "Ano com letra deve ser rejeitado.");
        assertTrue(dataFoiRejeitada(validador, "17/12/2026 10:00"), "Texto alem da data deve ser rejeitado.");
        assertTrue(dataFoiRejeitada(validador, ""), "Data vazia deve ser rejeitada.");
        assertTrue(dataFoiRejeitada(validador, null), "Data nula deve ser rejeitada.");
    }

    private static boolean dataFoiRejeitada(Validador validador, String texto) {
        try {
            validador.data(texto, "Data");
            return false;
        } catch (DadoInvalidoException esperado) {
            return true;
        }
    }

    private static void assertEquals(int esperado, int atual, String mensagem) {
        if (esperado != atual) {
            throw new AssertionError(mensagem);
        }
    }

    private static void assertTrue(boolean condicao, String mensagem) {
        if (!condicao) {
            throw new AssertionError(mensagem);
        }
    }
}
