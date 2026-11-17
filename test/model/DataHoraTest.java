package model;

import java.text.ParseException;
import java.text.SimpleDateFormat;

public class DataHoraTest {
    public static void main(String[] args) throws ParseException {
        testarHoraDaMensagem();
        testarDataDaTentativa();
    }

    private static void testarHoraDaMensagem() throws ParseException {
        Professor autor = new Professor("Professor", "prof", "prof@teste.edu", "1234");
        Mensagem mensagem = new Mensagem(autor, "Bom dia");
        String hora = mensagem.getHora();

        verificar(hora.length() == 5, "A hora da mensagem deve ter o formato HH:mm. Atual: " + hora);
        confirmarFormato("HH:mm", hora);
        verificar(("[" + hora + "] Professor: Bom dia").equals(mensagem.toString()),
                "O texto da mensagem deve mostrar hora, autor e conteudo.");
    }

    private static void testarDataDaTentativa() throws ParseException {
        Quiz quiz = new Quiz("Revisao", "Conceitos");
        Estudante estudante = new Estudante("Aluno", "aluno@teste.edu", "2026001", "1234", null);
        TentativaQuiz tentativa = new TentativaQuiz(estudante, quiz);
        String data = tentativa.getDataTentativa();

        verificar(data.length() == 16, "A data da tentativa deve ter o formato dd/MM/yyyy HH:mm. Atual: " + data);
        confirmarFormato("dd/MM/yyyy HH:mm", data);
    }

    private static void confirmarFormato(String formato, String texto) throws ParseException {
        SimpleDateFormat leitor = new SimpleDateFormat(formato);
        leitor.setLenient(false);
        leitor.parse(texto);
    }

    private static void verificar(boolean condicao, String mensagem) {
        if (!condicao) {
            throw new AssertionError(mensagem);
        }
    }
}
