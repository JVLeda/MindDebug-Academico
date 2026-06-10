package model;

import java.util.ArrayList;

public class GrupoEstudo extends Entidade {
    private String nome;
    private Aula aula;
    private Monitor monitor;
    private ArrayList<Estudante> estudantes = new ArrayList<>();

    public GrupoEstudo(String nome, Aula aula, Monitor monitor) {
        this.nome = nome;
        this.aula = aula;
        this.monitor = monitor;
    }

    public void adicionarEstudante(Estudante estudante) {
        if (!estudantes.contains(estudante)) {
            estudantes.add(estudante);
        }
    }

    public String getNome() {
        return nome;
    }

    public Aula getAula() {
        return aula;
    }

    public Monitor getMonitor() {
        return monitor;
    }

    public ArrayList<Estudante> getEstudantes() {
        return estudantes;
    }

    @Override
    public String toString() {
        String nomeAula = aula == null ? "Sem aula" : aula.getTitulo();
        String nomeMonitor = monitor == null ? "Sem monitor" : monitor.getNome();
        return nome + " | " + nomeAula + " | " + nomeMonitor + " | Alunos: " + estudantes.size();
    }
}
