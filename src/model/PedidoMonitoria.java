package model;

public class PedidoMonitoria extends Entidade {
    private Estudante estudante;
    private Aula aula;
    private Monitor monitor;
    private String duvida;
    private String data;
    private String horario;
    private String status = "Solicitado";

    public PedidoMonitoria(Estudante estudante, Aula aula, Monitor monitor, String duvida) {
        this(estudante, aula, monitor, duvida, "", "");
    }

    public PedidoMonitoria(Estudante estudante, Aula aula, Monitor monitor, String duvida, String data, String horario) {
        this.estudante = estudante;
        this.aula = aula;
        this.monitor = monitor;
        this.duvida = duvida;
        this.data = data;
        this.horario = horario;
    }

    public void aceitar(Monitor monitor) {
        if (monitor != null) {
            this.monitor = monitor;
        }
        status = "Aceito";
    }

    public void recusar() {
        status = "Recusado";
    }

    public Estudante getEstudante() {
        return estudante;
    }

    public Aula getAula() {
        return aula;
    }

    public Monitor getMonitor() {
        return monitor;
    }

    public String getDuvida() {
        return duvida;
    }

    public String getStatus() {
        return status;
    }

    public String getData() {
        return data;
    }

    public String getHorario() {
        return horario;
    }

    @Override
    public String toString() {
        String nomeAula = aula == null ? "Geral" : aula.getTitulo();
        String nomeMonitor = monitor == null ? "Sem monitor" : monitor.getNome();
        return estudante.getNome() + " | " + nomeAula + " | " + nomeMonitor + " | " + data + " " + horario + " | " + status + " | " + duvida;
    }
}
