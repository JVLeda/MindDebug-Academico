package model;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class Mensagem extends Entidade {
    private Usuario autor;
    private String texto;
    private String hora;

    public Mensagem(Usuario autor, String texto) {
        this.autor = autor;
        this.texto = texto;
        this.hora = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm"));
    }

    public Usuario getAutor() {
        return autor;
    }

    public String getTexto() {
        return texto;
    }

    public String getHora() {
        return hora;
    }

    @Override
    public String toString() {
        return "[" + hora + "] " + autor.getNome() + ": " + texto;
    }
}
