package model;

import java.util.ArrayList;

public class Materia extends Entidade {
    private String nome;
    private Professor professor;
    private ArrayList<Usuario> participantes = new ArrayList<>();
    private ArrayList<Aula> aulas = new ArrayList<>();
    private ArrayList<Material> materiais = new ArrayList<>();
    private ArrayList<GrupoEstudo> grupos = new ArrayList<>();
    private ArrayList<PedidoMonitoria> pedidosMonitoria = new ArrayList<>();
    private Chat chatGeral = new Chat("Chat geral");
    private Chat chatEquipe = new Chat("Chat de equipe");

    public Materia(String nome, Professor professor) {
        this.nome = nome;
        this.professor = professor;
        adicionarParticipante(professor);
    }

    public void adicionarParticipante(Usuario usuario) {
        if (usuario != null && !participantes.contains(usuario)) {
            participantes.add(usuario);
        }
    }

    public void removerParticipante(Usuario usuario) {
        if (usuario != professor) {
            participantes.remove(usuario);
        }
    }

    public void adicionarAula(Aula aula) {
        aulas.add(aula);
    }

    public void adicionarMaterial(Material material) {
        materiais.add(material);
    }

    public void adicionarGrupo(GrupoEstudo grupo) {
        grupos.add(grupo);
    }

    public void removerGrupo(GrupoEstudo grupo) {
        grupos.remove(grupo);
    }

    public void adicionarPedidoMonitoria(PedidoMonitoria pedido) {
        pedidosMonitoria.add(pedido);
    }

    public ArrayList<Estudante> getEstudantes() {
        ArrayList<Estudante> estudantes = new ArrayList<>();
        for (Usuario usuario : participantes) {
            if (usuario instanceof Estudante) {
                estudantes.add((Estudante) usuario);
            }
        }
        return estudantes;
    }

    public ArrayList<Monitor> getMonitores() {
        ArrayList<Monitor> monitores = new ArrayList<>();
        for (Usuario usuario : participantes) {
            if (usuario instanceof Monitor) {
                monitores.add((Monitor) usuario);
            }
        }
        return monitores;
    }

    public String getNome() {
        return nome;
    }

    public Professor getProfessor() {
        return professor;
    }

    public void setProfessor(Professor professor) {
        this.professor = professor;
        adicionarParticipante(professor);
    }

    public ArrayList<Aula> getAulas() {
        return aulas;
    }

    public ArrayList<Material> getMateriais() {
        return materiais;
    }

    public ArrayList<GrupoEstudo> getGrupos() {
        return grupos;
    }

    public ArrayList<PedidoMonitoria> getPedidosMonitoria() {
        return pedidosMonitoria;
    }

    public Chat getChatGeral() {
        return chatGeral;
    }

    public Chat getChatEquipe() {
        return chatEquipe;
    }

    @Override
    public String toString() {
        return nome;
    }
}
