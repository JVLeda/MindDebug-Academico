package model;

import excecao.DadoInvalidoException;
import excecao.RegraDeNegocioException;

public class HorarioMonitorTest {
    public static void main(String[] args) {
        testarHorarioValido();
        testarLimitesDaHora();
        testarHorarioInvalido();
        testarConflitoDeHorarios();
        testarHorariosPorMateria();
    }

    private static void testarHorarioValido() {
        Materia materia = novaMateria("Banco de Dados");
        HorarioMonitor horario = new HorarioMonitor(materia, DiaSemana.SEGUNDA, "14:00", "16:00");

        verificar(horario.getMateria() == materia, "O horario guarda a materia.");
        verificar(horario.getDia() == DiaSemana.SEGUNDA, "O horario guarda o dia da semana.");
        verificar("14:00".equals(horario.getHoraInicio()), "O horario guarda a hora de inicio.");
        verificar("16:00".equals(horario.getHoraFim()), "O horario guarda a hora de termino.");
        verificar("Segunda 14:00 - 16:00 | Banco de Dados".equals(horario.toString()),
                "O texto do horario mostra dia, horas e materia.");
    }

    private static void testarLimitesDaHora() {
        Materia materia = novaMateria("Banco de Dados");

        HorarioMonitor diaInteiro = new HorarioMonitor(materia, DiaSemana.SABADO, "00:00", "23:59");
        verificar("00:00".equals(diaInteiro.getHoraInicio()), "00:00 e a menor hora aceita.");
        verificar("23:59".equals(diaInteiro.getHoraFim()), "23:59 e a maior hora aceita.");

        verificar(foiRejeitado(materia, DiaSemana.SEGUNDA, "23:00", "24:00"), "24:00 nao existe.");
        verificar(foiRejeitado(materia, DiaSemana.SEGUNDA, "12:00", "12:60"), "Minuto 60 nao existe.");
    }

    private static void testarHorarioInvalido() {
        Materia materia = novaMateria("Banco de Dados");

        verificar(foiRejeitado(materia, DiaSemana.SEGUNDA, "14:00", "14:00"),
                "Horario que termina na mesma hora em que comeca deve ser rejeitado.");
        verificar(foiRejeitado(materia, DiaSemana.SEGUNDA, "16:00", "14:00"),
                "Horario que termina antes de comecar deve ser rejeitado.");
        verificar(foiRejeitado(materia, DiaSemana.SEGUNDA, "9:00", "10:00"), "Hora sem zero a esquerda e rejeitada.");
        verificar(foiRejeitado(materia, DiaSemana.SEGUNDA, "ab:cd", "10:00"), "Hora com letras e rejeitada.");
        verificar(foiRejeitado(materia, DiaSemana.SEGUNDA, null, "10:00"), "Hora vazia e rejeitada.");
        verificar(foiRejeitado(null, DiaSemana.SEGUNDA, "14:00", "16:00"), "Horario sem materia e rejeitado.");
        verificar(foiRejeitado(materia, null, "14:00", "16:00"), "Horario sem dia da semana e rejeitado.");
    }

    private static boolean foiRejeitado(Materia materia, DiaSemana dia, String inicio, String fim) {
        try {
            new HorarioMonitor(materia, dia, inicio, fim);
            return false;
        } catch (DadoInvalidoException esperado) {
            return true;
        }
    }

    private static void testarConflitoDeHorarios() {
        Materia bancoDeDados = novaMateria("Banco de Dados");
        Materia engenharia = novaMateria("Engenharia de Software");
        Monitor monitor = new Monitor("Monitor", "monitor", "monitor@teste.edu", "segredo1", "Monitor");

        monitor.adicionarHorario(new HorarioMonitor(bancoDeDados, DiaSemana.SEGUNDA, "14:00", "16:00"));

        verificar(conflitou(monitor, new HorarioMonitor(bancoDeDados, DiaSemana.SEGUNDA, "15:00", "17:00")),
                "Horario que comeca dentro de outro no mesmo dia deve ser recusado.");
        verificar(conflitou(monitor, new HorarioMonitor(engenharia, DiaSemana.SEGUNDA, "13:00", "14:01")),
                "O conflito vale mesmo quando a materia e outra.");
        verificar(conflitou(monitor, new HorarioMonitor(bancoDeDados, DiaSemana.SEGUNDA, "14:00", "16:00")),
                "Horario repetido deve ser recusado.");
        verificar(monitor.getHorarios().size() == 1, "Horario recusado nao entra na lista.");

        monitor.adicionarHorario(new HorarioMonitor(bancoDeDados, DiaSemana.SEGUNDA, "16:00", "18:00"));
        monitor.adicionarHorario(new HorarioMonitor(bancoDeDados, DiaSemana.SEGUNDA, "12:00", "14:00"));
        verificar(monitor.getHorarios().size() == 3, "Horarios encostados, sem sobreposicao, sao aceitos.");

        monitor.adicionarHorario(new HorarioMonitor(bancoDeDados, DiaSemana.TERCA, "14:00", "16:00"));
        verificar(monitor.getHorarios().size() == 4, "A mesma hora em outro dia e aceita.");

        boolean rejeitou = false;
        try {
            monitor.adicionarHorario(null);
        } catch (DadoInvalidoException esperado) {
            rejeitou = true;
        }
        verificar(rejeitou, "Horario nulo e rejeitado.");
    }

    private static boolean conflitou(Monitor monitor, HorarioMonitor horario) {
        try {
            monitor.adicionarHorario(horario);
            return false;
        } catch (RegraDeNegocioException esperado) {
            return true;
        }
    }

    private static void testarHorariosPorMateria() {
        Materia bancoDeDados = novaMateria("Banco de Dados");
        Materia engenharia = novaMateria("Engenharia de Software");
        Monitor monitor = new Monitor("Monitor", "monitor", "monitor@teste.edu", "segredo1", "Monitor");
        HorarioMonitor segunda = new HorarioMonitor(bancoDeDados, DiaSemana.SEGUNDA, "14:00", "16:00");

        monitor.adicionarHorario(segunda);
        monitor.adicionarHorario(new HorarioMonitor(bancoDeDados, DiaSemana.QUARTA, "14:00", "16:00"));
        monitor.adicionarHorario(new HorarioMonitor(engenharia, DiaSemana.SEXTA, "10:00", "12:00"));

        verificar(monitor.getHorariosDa(bancoDeDados).size() == 2, "O monitor ve os horarios de uma materia.");
        verificar(monitor.getHorariosDa(engenharia).size() == 1, "Os horarios de outra materia ficam separados.");

        monitor.removerHorario(segunda);
        verificar(monitor.getHorariosDa(bancoDeDados).size() == 1, "Horario removido sai da lista.");
        monitor.adicionarHorario(new HorarioMonitor(engenharia, DiaSemana.SEGUNDA, "15:00", "17:00"));
        verificar(monitor.getHorarios().size() == 3, "Depois de removido, o horario fica livre para outro.");
    }

    private static Materia novaMateria(String nome) {
        return new Materia(nome, new Professor("Professor", "prof", "prof@teste.edu", "segredo1"));
    }

    private static void verificar(boolean condicao, String mensagem) {
        if (!condicao) {
            throw new AssertionError(mensagem);
        }
    }
}
