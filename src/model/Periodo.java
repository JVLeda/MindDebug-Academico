package model;

import java.util.ArrayList;

public class Periodo extends Entidade {
    private int numero;
    private String nome;
    private ArrayList<Materia> materias = new ArrayList<>();

    public Periodo(int numero, String nome) {
        this.numero = numero;
        this.nome = nome;
    }

    public void adicionarMateria(Materia materia) {
        materias.add(materia);
    }

    public void removerMateria(Materia materia) {
        materias.remove(materia);
    }

    public int getNumero() {
        return numero;
    }

    public String getNome() {
        return nome;
    }

    public ArrayList<Materia> getMaterias() {
        return materias;
    }

    @Override
    public String toString() {
        return numero + "o Periodo";
    }
}
