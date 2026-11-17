package model;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Guarda quais permissoes cada perfil tem.
 * Nasce com o padrao do sistema; o administrador pode conceder, revogar
 * e voltar ao padrao.
 */
public class MatrizPermissoes {
    private HashMap<Perfil, ArrayList<Permissao>> permissoesPorPerfil = new HashMap<>();

    public MatrizPermissoes() {
        restaurarPadrao();
    }

    public void restaurarPadrao() {
        permissoesPorPerfil.clear();
        for (Perfil perfil : Perfil.values()) {
            permissoesPorPerfil.put(perfil, new ArrayList<Permissao>());
        }

        conceder(Perfil.ADMINISTRADOR, Permissao.GERENCIAR_ESTRUTURA);
        conceder(Perfil.ADMINISTRADOR, Permissao.CADASTRAR_MONITOR);
        conceder(Perfil.ADMINISTRADOR, Permissao.ATENDER_MONITORIA);

        conceder(Perfil.PROFESSOR, Permissao.CADASTRAR_MONITOR);
        conceder(Perfil.PROFESSOR, Permissao.GERENCIAR_MATERIA);
        conceder(Perfil.PROFESSOR, Permissao.ATENDER_MONITORIA);
        conceder(Perfil.PROFESSOR, Permissao.MODERAR_CONTEUDO);

        conceder(Perfil.MONITOR, Permissao.ATENDER_MONITORIA);
        conceder(Perfil.MONITOR, Permissao.MODERAR_CONTEUDO);

        conceder(Perfil.ALUNO, Permissao.REGISTRAR_FEEDBACK);
        conceder(Perfil.ALUNO, Permissao.SOLICITAR_MONITORIA);
        conceder(Perfil.ALUNO, Permissao.RESPONDER_QUIZ);
    }

    public void conceder(Perfil perfil, Permissao permissao) {
        ArrayList<Permissao> permissoes = permissoesPorPerfil.get(perfil);
        if (!permissoes.contains(permissao)) {
            permissoes.add(permissao);
        }
    }

    public void revogar(Perfil perfil, Permissao permissao) {
        permissoesPorPerfil.get(perfil).remove(permissao);
    }

    public boolean permite(Perfil perfil, Permissao permissao) {
        return permissoesPorPerfil.get(perfil).contains(permissao);
    }

    public ArrayList<Permissao> listar(Perfil perfil) {
        return new ArrayList<>(permissoesPorPerfil.get(perfil));
    }
}
