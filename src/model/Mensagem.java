package model;

import java.text.SimpleDateFormat;
import java.util.Date;

public class Mensagem extends Entidade {
    private Usuario autor;
    private String texto;
    private Date data;
    private String hora;

    public Mensagem(Usuario autor, String texto) {
        Date agora = new Date();
        this.autor = autor;
        this.texto = texto;
        this.data = agora;
        this.hora = new SimpleDateFormat("HH:mm").format(agora);
    }

    public Mensagem(Usuario autor, String texto, Date data, String hora) {
        this.autor = autor;
        this.texto = texto;
        this.data = data;
        this.hora = hora;
    }

    public Usuario getAutor() {
        return autor;
    }

    public String getTexto() {
        return texto;
    }

    public Date getData() {
        return data;
    }

    public String getHora() {
        return hora;
    }

    @Override
    public String toString() {
        return "[" + hora + "] " + autor.getNome() + ": " + texto;
    }
}
