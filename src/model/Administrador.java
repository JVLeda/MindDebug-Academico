package model;

/**
 * Administrador do sistema: cuida da estrutura academica, das contas e da enturmacao.
 * Nao gerencia o conteudo da materia (aulas, quiz e grupos de estudo), que e do professor (RN01J).
 */
public class Administrador extends Usuario implements Moderador {
    public Administrador(String nome, String login, String email, String senha) {
        super(nome, login, email, senha);
    }

    @Override
    public int getNivel() {
        return 4;
    }

    @Override
    public String getCargo() {
        return "Administrador do Sistema";
    }

    @Override
    public Perfil getPerfil() {
        return Perfil.ADMINISTRADOR;
    }

    @Override
    public boolean podeModerar() {
        return true;
    }

    @Override
    public boolean podeGerenciarEstrutura() {
        return true;
    }

    @Override
    public boolean podeCadastrarMonitor() {
        return true;
    }

    @Override
    public boolean podeAtenderMonitoria() {
        return true;
    }
}
