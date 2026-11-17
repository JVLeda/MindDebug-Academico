package model;

import excecao.AcessoNegadoException;
import excecao.DadoInvalidoException;
import excecao.RegraDeNegocioException;

import java.util.ArrayList;

public class Monitor extends Usuario implements Moderador {
    private String tipo;
    private ArrayList<HorarioMonitor> horarios = new ArrayList<>();

    public Monitor(String nome, String login, String email, String senha, String tipo) {
        super(nome, login, email, senha);
        this.tipo = tipo;
    }

    @Override
    public int getNivel() {
        return 2;
    }

    @Override
    public String getCargo() {
        return "Monitor";
    }

    @Override
    public Perfil getPerfil() {
        return Perfil.MONITOR;
    }

    @Override
    public boolean podeModerar() {
        return true;
    }

    @Override
    public boolean podeAtenderMonitoria() {
        return true;
    }

    @Override
    public boolean podeRecusarPedido(PedidoMonitoria pedido) {
        return pedido.getMonitor() == null || pedido.getMonitor() == this;
    }

    @Override
    public boolean podeApagarConteudoDe(Usuario autor) {
        return true;
    }

    @Override
    public Monitor escolherMonitorDoAtendimento(Monitor monitorInformado) {
        if (monitorInformado != null && monitorInformado != this) {
            throw new AcessoNegadoException("Monitor so pode aceitar pedido para si mesmo.");
        }
        return this;
    }

    @Override
    public void incluirEmMonitores(ArrayList<Monitor> monitores) {
        monitores.add(this);
    }

    @Override
    public String getTipoMonitor() {
        return tipo;
    }

    public String getTipo() {
        return tipo;
    }

    public void adicionarHorario(HorarioMonitor novoHorario) {
        if (novoHorario == null) {
            throw new DadoInvalidoException("Horario obrigatorio.");
        }
        for (HorarioMonitor horario : horarios) {
            if (horario.conflitaCom(novoHorario)) {
                throw new RegraDeNegocioException("O monitor ja tem um horario nesse dia e nessa hora.");
            }
        }
        horarios.add(novoHorario);
    }

    public void removerHorario(HorarioMonitor horario) {
        horarios.remove(horario);
    }

    public ArrayList<HorarioMonitor> getHorarios() {
        return horarios;
    }

    public ArrayList<HorarioMonitor> getHorariosDa(Materia materia) {
        ArrayList<HorarioMonitor> daMateria = new ArrayList<>();
        for (HorarioMonitor horario : horarios) {
            if (horario.getMateria() == materia) {
                daMateria.add(horario);
            }
        }
        return daMateria;
    }
}
