package model;

/**
 * Permissoes que o administrador pode ligar ou desligar para cada perfil.
 * Cada permissao pertence a um modulo do sistema e libera uma acao.
 * O codigo e o mesmo id da tabela permissoes do banco.
 */
public enum Permissao {
    GERENCIAR_ESTRUTURA(1, "Estrutura academica", "Gerenciar", "Cadastrar cursos, periodos, disciplinas e contas de usuario"),
    CADASTRAR_MONITOR(2, "Monitores", "Cadastrar", "Cadastrar monitores"),
    GERENCIAR_MATERIA(3, "Materia", "Gerenciar", "Criar aulas, grupos de estudo e quiz da materia"),
    ATENDER_MONITORIA(4, "Monitoria", "Atender", "Aceitar ou recusar pedidos de monitoria"),
    MODERAR_CONTEUDO(5, "Conteudo", "Moderar", "Apagar mensagens e materiais de outros usuarios"),
    REGISTRAR_FEEDBACK(6, "Feedback", "Registrar", "Registrar o entendimento de uma aula"),
    SOLICITAR_MONITORIA(7, "Monitoria", "Solicitar", "Pedir ajuda a um monitor"),
    RESPONDER_QUIZ(8, "Quiz", "Responder", "Responder o quiz de uma aula");

    private int codigo;
    private String modulo;
    private String acao;
    private String descricao;

    Permissao(int codigo, String modulo, String acao, String descricao) {
        this.codigo = codigo;
        this.modulo = modulo;
        this.acao = acao;
        this.descricao = descricao;
    }

    public int getCodigo() {
        return codigo;
    }

    public String getModulo() {
        return modulo;
    }

    public String getAcao() {
        return acao;
    }

    public String getDescricao() {
        return descricao;
    }

    public static Permissao buscarPorCodigo(int codigo) {
        for (Permissao permissao : values()) {
            if (permissao.codigo == codigo) {
                return permissao;
            }
        }
        return null;
    }
}
