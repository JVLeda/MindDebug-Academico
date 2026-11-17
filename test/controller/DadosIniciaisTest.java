package controller;

import dao.BancoDados;
import model.Curso;
import model.Materia;
import model.Periodo;
import model.Usuario;

import java.util.ArrayList;

/** Confere os dados iniciais montados em memoria, sem usar o banco. */
public class DadosIniciaisTest {
    public static void main(String[] args) {
        BancoDados banco = new BancoDados();
        new DadosIniciais().montar(banco);

        verificar(banco.getUsuarios().size() == 4, "um usuario de cada perfil");
        String[] logins = {"admin", "prof", "monitor", "2026001"};
        for (String login : logins) {
            verificar(banco.buscarUsuarioPorLogin(login) != null, "usuario " + login);
        }
        verificar(banco.getCursos().size() == 1, "um curso");

        Curso curso = banco.getCursos().get(0);
        verificar("EC".equals(curso.getSigla()), "curso de Engenharia da Computacao");
        verificar(curso.getPeriodos().size() == 10, "dez periodos");

        int materias = 0;
        Materia demonstracao = null;
        for (Periodo periodo : curso.getPeriodos()) {
            materias += periodo.getMaterias().size();
            for (Materia materia : periodo.getMaterias()) {
                if ("Algoritmos e Programacao".equals(materia.getNome())) {
                    demonstracao = materia;
                }
            }
        }
        verificar(materias == 56, "56 materias na grade: " + materias);
        verificar(demonstracao != null, "materia de demonstracao");
        verificar(demonstracao.getAulas().size() == 3, "tres aulas");
        verificar(demonstracao.getAulas().get(0).getFeedbacks().size() == 1
                && demonstracao.getAulas().get(1).getFeedbacks().size() == 1, "dois feedbacks");
        verificar(demonstracao.getMateriais().size() == 2, "dois materiais");
        verificar(demonstracao.getChatGeral().getMensagens().size() == 1
                && demonstracao.getChatEquipe().getMensagens().size() == 1, "uma mensagem em cada chat");
        verificar(demonstracao.getEstudantes().size() == 1 && demonstracao.getMonitores().size() == 1,
                "um aluno e um monitor na materia");

        ArrayList<Integer> ids = new ArrayList<>();
        for (Usuario usuario : banco.getUsuarios()) {
            verificar(usuario.getId() > 0 && !ids.contains(usuario.getId()), "id unico para " + usuario.getLogin());
            ids.add(usuario.getId());
        }
    }

    private static void verificar(boolean condicao, String descricao) {
        if (!condicao) {
            throw new AssertionError("FALHOU: " + descricao);
        }
    }
}
