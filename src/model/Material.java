package model;

public class Material extends Entidade {
    private String titulo;
    private String tipo;
    private String referencia;
    private Usuario autor;

    public Material(String titulo, String tipo, String referencia, Usuario autor) {
        this.titulo = titulo;
        this.tipo = tipo;
        this.referencia = referencia;
        this.autor = autor;
    }

    public Usuario getAutor() {
        return autor;
    }

    public String getTitulo() {
        return titulo;
    }

    public String getTipo() {
        return tipo;
    }

    public String getReferencia() {
        return referencia;
    }

    @Override
    public String toString() {
        return tipo + ": " + titulo + " - " + referencia;
    }
}
