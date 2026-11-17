package model;

import excecao.DadoInvalidoException;
import util.Validador;

/**
 * Horario em que um monitor atende uma materia.
 * As horas ficam no formato HH:mm, por exemplo 14:00.
 */
public class HorarioMonitor extends Entidade {
    private Materia materia;
    private DiaSemana dia;
    private String horaInicio;
    private String horaFim;

    public HorarioMonitor(Materia materia, DiaSemana dia, String horaInicio, String horaFim) {
        if (materia == null) {
            throw new DadoInvalidoException("Materia do horario obrigatoria.");
        }
        if (dia == null) {
            throw new DadoInvalidoException("Dia da semana obrigatorio.");
        }
        Validador validador = new Validador();
        String inicio = validador.hora(horaInicio, "Hora de inicio");
        String fim = validador.hora(horaFim, "Hora de termino");
        if (fim.compareTo(inicio) <= 0) {
            throw new DadoInvalidoException("O horario deve terminar depois de comecar.");
        }
        this.materia = materia;
        this.dia = dia;
        this.horaInicio = inicio;
        this.horaFim = fim;
    }

    public boolean conflitaCom(HorarioMonitor outro) {
        return dia == outro.dia
                && horaInicio.compareTo(outro.horaFim) < 0
                && outro.horaInicio.compareTo(horaFim) < 0;
    }

    public Materia getMateria() {
        return materia;
    }

    public DiaSemana getDia() {
        return dia;
    }

    public String getHoraInicio() {
        return horaInicio;
    }

    public String getHoraFim() {
        return horaFim;
    }

    @Override
    public String toString() {
        return dia.getNome() + " " + horaInicio + " - " + horaFim + " | " + materia.getNome();
    }
}
