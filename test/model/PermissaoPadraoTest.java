package model;

import java.util.ArrayList;

public class PermissaoPadraoTest {
    public static void main(String[] args) {
        testarPadraoDeCadaPerfil();
        testarPadraoIgualAsRegrasDoCodigo();
        testarConcederERevogar();
        testarRestaurarPadrao();
        testarListaNaoExpoeAMatriz();
        testarDescricaoDasPermissoes();
    }

    private static void testarPadraoDeCadaPerfil() {
        MatrizPermissoes matriz = new MatrizPermissoes();

        verificar(matriz.listar(Perfil.ADMINISTRADOR).size() == 3, "Administrador nasce com tres permissoes.");
        verificar(matriz.permite(Perfil.ADMINISTRADOR, Permissao.GERENCIAR_ESTRUTURA),
                "Administrador gerencia a estrutura academica.");
        verificar(!matriz.permite(Perfil.ADMINISTRADOR, Permissao.MODERAR_CONTEUDO),
                "Administrador nao apaga conteudo pedagogico de outros usuarios.");
        verificar(!matriz.permite(Perfil.ADMINISTRADOR, Permissao.REGISTRAR_FEEDBACK),
                "Administrador nao registra feedback de aula.");
        verificar(!matriz.permite(Perfil.ADMINISTRADOR, Permissao.GERENCIAR_MATERIA),
                "Administrador nao cria aulas, quiz nem grupos de estudo.");

        verificar(matriz.listar(Perfil.PROFESSOR).size() == 4, "Professor nasce com quatro permissoes.");
        verificar(matriz.permite(Perfil.PROFESSOR, Permissao.GERENCIAR_MATERIA), "Professor gerencia a materia.");
        verificar(!matriz.permite(Perfil.PROFESSOR, Permissao.GERENCIAR_ESTRUTURA),
                "Professor nao gerencia a estrutura academica.");

        verificar(matriz.listar(Perfil.MONITOR).size() == 2, "Monitor nasce com duas permissoes.");
        verificar(matriz.permite(Perfil.MONITOR, Permissao.ATENDER_MONITORIA), "Monitor atende monitoria.");
        verificar(matriz.permite(Perfil.MONITOR, Permissao.MODERAR_CONTEUDO), "Monitor modera conteudo.");

        verificar(matriz.listar(Perfil.ALUNO).size() == 3, "Aluno nasce com tres permissoes.");
        verificar(matriz.permite(Perfil.ALUNO, Permissao.REGISTRAR_FEEDBACK), "Aluno registra feedback.");
        verificar(!matriz.permite(Perfil.ALUNO, Permissao.ATENDER_MONITORIA), "Aluno nao atende monitoria.");
    }

    private static void testarPadraoIgualAsRegrasDoCodigo() {
        MatrizPermissoes matriz = new MatrizPermissoes();
        Professor professor = new Professor("Professor", "prof", "prof@teste.edu", "segredo1");
        Materia materia = new Materia("Banco de Dados", professor);
        Estudante outroAluno = new Estudante("Outro aluno", "outro@teste.edu", "2026002", "segredo1", null);
        Usuario[] usuarios = {
            new Administrador("Administrador", "admin", "admin@teste.edu", "segredo1"),
            professor,
            new Monitor("Monitor", "monitor", "monitor@teste.edu", "segredo1", "Monitor"),
            new Estudante("Aluno", "aluno@teste.edu", "2026001", "segredo1", null)
        };

        for (Usuario usuario : usuarios) {
            Perfil perfil = usuario.getPerfil();
            String quem = " Perfil: " + perfil.getNome() + ".";

            verificar(matriz.permite(perfil, Permissao.GERENCIAR_ESTRUTURA) == usuario.podeGerenciarEstrutura(),
                    "Padrao de GERENCIAR_ESTRUTURA difere da regra do codigo." + quem);
            verificar(matriz.permite(perfil, Permissao.CADASTRAR_MONITOR) == usuario.podeCadastrarMonitor(),
                    "Padrao de CADASTRAR_MONITOR difere da regra do codigo." + quem);
            verificar(matriz.permite(perfil, Permissao.GERENCIAR_MATERIA) == usuario.podeGerenciarMateria(materia),
                    "Padrao de GERENCIAR_MATERIA difere da regra do codigo." + quem);
            verificar(matriz.permite(perfil, Permissao.ATENDER_MONITORIA) == usuario.podeAtenderMonitoria(),
                    "Padrao de ATENDER_MONITORIA difere da regra do codigo." + quem);
            verificar(matriz.permite(perfil, Permissao.MODERAR_CONTEUDO) == usuario.podeApagarConteudoDe(outroAluno),
                    "Padrao de MODERAR_CONTEUDO difere da regra do codigo." + quem);
            verificar(matriz.permite(perfil, Permissao.REGISTRAR_FEEDBACK) == usuario.podeRegistrarFeedback(),
                    "Padrao de REGISTRAR_FEEDBACK difere da regra do codigo." + quem);
            verificar(matriz.permite(perfil, Permissao.SOLICITAR_MONITORIA) == usuario.podeSolicitarMonitoria(),
                    "Padrao de SOLICITAR_MONITORIA difere da regra do codigo." + quem);
            verificar(matriz.permite(perfil, Permissao.RESPONDER_QUIZ) == usuario.podeResponderQuiz(),
                    "Padrao de RESPONDER_QUIZ difere da regra do codigo." + quem);
        }
    }

    private static void testarConcederERevogar() {
        MatrizPermissoes matriz = new MatrizPermissoes();

        matriz.revogar(Perfil.MONITOR, Permissao.MODERAR_CONTEUDO);
        verificar(!matriz.permite(Perfil.MONITOR, Permissao.MODERAR_CONTEUDO), "Permissao revogada deixa de valer.");
        verificar(matriz.permite(Perfil.PROFESSOR, Permissao.MODERAR_CONTEUDO),
                "Revogar de um perfil nao muda os outros perfis.");

        matriz.conceder(Perfil.MONITOR, Permissao.MODERAR_CONTEUDO);
        verificar(matriz.permite(Perfil.MONITOR, Permissao.MODERAR_CONTEUDO), "Permissao concedida passa a valer.");

        int quantidade = matriz.listar(Perfil.MONITOR).size();
        matriz.conceder(Perfil.MONITOR, Permissao.MODERAR_CONTEUDO);
        verificar(matriz.listar(Perfil.MONITOR).size() == quantidade, "Conceder duas vezes nao duplica a permissao.");

        matriz.revogar(Perfil.ALUNO, Permissao.GERENCIAR_ESTRUTURA);
        verificar(matriz.listar(Perfil.ALUNO).size() == 3, "Revogar o que o perfil nao tem nao muda nada.");
    }

    private static void testarRestaurarPadrao() {
        MatrizPermissoes padrao = new MatrizPermissoes();
        MatrizPermissoes alterada = new MatrizPermissoes();

        alterada.revogar(Perfil.PROFESSOR, Permissao.CADASTRAR_MONITOR);
        alterada.conceder(Perfil.MONITOR, Permissao.CADASTRAR_MONITOR);
        alterada.revogar(Perfil.ALUNO, Permissao.RESPONDER_QUIZ);
        alterada.restaurarPadrao();

        for (Perfil perfil : Perfil.values()) {
            for (Permissao permissao : Permissao.values()) {
                verificar(alterada.permite(perfil, permissao) == padrao.permite(perfil, permissao),
                        "Restaurar o padrao deve desfazer as alteracoes. Perfil: " + perfil.getNome()
                                + ", permissao: " + permissao.getDescricao());
            }
        }
    }

    private static void testarListaNaoExpoeAMatriz() {
        MatrizPermissoes matriz = new MatrizPermissoes();
        ArrayList<Permissao> doAluno = matriz.listar(Perfil.ALUNO);

        doAluno.add(Permissao.GERENCIAR_ESTRUTURA);
        doAluno.remove(Permissao.REGISTRAR_FEEDBACK);

        verificar(!matriz.permite(Perfil.ALUNO, Permissao.GERENCIAR_ESTRUTURA),
                "Mexer na lista devolvida nao concede permissao.");
        verificar(matriz.permite(Perfil.ALUNO, Permissao.REGISTRAR_FEEDBACK),
                "Mexer na lista devolvida nao revoga permissao.");
    }

    private static void testarDescricaoDasPermissoes() {
        Permissao[] permissoes = Permissao.values();
        verificar(permissoes.length == 8, "O sistema tem oito permissoes.");

        for (int i = 0; i < permissoes.length; i++) {
            Permissao permissao = permissoes[i];
            verificar(permissao.getModulo().length() > 0, "Toda permissao pertence a um modulo.");
            verificar(permissao.getAcao().length() > 0, "Toda permissao libera uma acao.");
            verificar(permissao.getDescricao().length() > 0, "Toda permissao tem descricao.");

            for (int j = i + 1; j < permissoes.length; j++) {
                boolean repetida = permissao.getModulo().equals(permissoes[j].getModulo())
                        && permissao.getAcao().equals(permissoes[j].getAcao());
                verificar(!repetida, "Modulo e acao nao podem se repetir: " + permissao.getModulo()
                        + " / " + permissao.getAcao());
            }
        }
    }

    private static void verificar(boolean condicao, String mensagem) {
        if (!condicao) {
            throw new AssertionError(mensagem);
        }
    }
}
