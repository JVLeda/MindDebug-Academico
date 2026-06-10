package model;

import java.util.ArrayList;

public class Curso extends Entidade {
    private String nome;
    private String sigla;
    private String area;
    private int cargaHoraria;
    private String coordenador;
    private ArrayList<Periodo> periodos = new ArrayList<>();

    public Curso(String nome, String sigla, String area, int cargaHoraria, String coordenador) {
        this.nome = nome;
        this.sigla = sigla;
        this.area = area;
        this.cargaHoraria = cargaHoraria;
        this.coordenador = coordenador;
    }

    public void adicionarPeriodo(Periodo periodo) {
        periodos.add(periodo);
    }

    public void removerPeriodo(Periodo periodo) {
        periodos.remove(periodo);
    }

    public ArrayList<Periodo> getPeriodos() {
        return periodos;
    }

    public String getNome() {
        return nome;
    }

    public String getSigla() {
        return sigla;
    }

    public String getArea() {
        return area;
    }

    public int getCargaHoraria() {
        return cargaHoraria;
    }

    public String getCoordenador() {
        return coordenador;
    }

    @Override
    public String toString() {
        return sigla + " - " + nome;
    }
}
