package model;

import java.util.ArrayList;

public class GrupoEstudo extends Entidade {
    private String nome;
    private Aula aula;
    private ArrayList<Monitor> monitores = new ArrayList<>();
    private ArrayList<Estudante> estudantes = new ArrayList<>();

    public GrupoEstudo(String nome, Aula aula, Monitor monitor) {
        this.nome = nome;
        this.aula = aula;
        adicionarMonitor(monitor);
    }

    public String getNome() {
        return nome;
    }

    public Aula getAula() {
        return aula;
    }

    public ArrayList<Monitor> getMonitores() {
        return monitores;
    }

    public ArrayList<Estudante> getEstudantes() {
        return estudantes;
    }

    public void adicionarEstudante(Estudante estudante) {
        if (estudante != null && !estudantes.contains(estudante)) {
            estudantes.add(estudante);
        }
    }

    public void removerEstudante(Estudante estudante) {
        estudantes.remove(estudante);
    }

    public void adicionarMonitor(Monitor monitor) {
        if (monitor != null && !monitores.contains(monitor)) {
            monitores.add(monitor);
        }
    }

    public void removerMonitor(Monitor monitor) {
        monitores.remove(monitor);
    }

    @Override
    public String toString() {
        String nomeAula = aula == null ? "Sem aula" : aula.getTitulo();
        return nome + " | " + nomeAula + " | Monitores: " + monitores.size() + " | Alunos: " + estudantes.size();
    }
}
