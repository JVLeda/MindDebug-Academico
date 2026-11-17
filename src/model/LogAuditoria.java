package model;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Registro de uma acao administrativa: quem fez, quando e o que foi feito.
 */
public class LogAuditoria extends Entidade {
    private Usuario usuario;
    private Date data;
    private String hora;
    private String acao;
    private String detalhes;

    public LogAuditoria(Usuario usuario, String acao, String detalhes) {
        Date agora = new Date();
        this.usuario = usuario;
        this.data = agora;
        this.hora = new SimpleDateFormat("HH:mm").format(agora);
        this.acao = acao;
        this.detalhes = detalhes;
    }

    public LogAuditoria(Usuario usuario, Date data, String hora, String acao, String detalhes) {
        this.usuario = usuario;
        this.data = data;
        this.hora = hora;
        this.acao = acao;
        this.detalhes = detalhes;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public Date getData() {
        return data;
    }

    public String getHora() {
        return hora;
    }

    public String getAcao() {
        return acao;
    }

    public String getDetalhes() {
        return detalhes;
    }

    @Override
    public String toString() {
        String dia = new SimpleDateFormat("dd/MM/yyyy").format(data);
        return "[" + dia + " " + hora + "] " + usuario.getNome() + ": " + acao + " - " + detalhes;
    }
}
