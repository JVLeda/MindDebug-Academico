package model;

/**
 * Perfis de usuario do sistema. Cada subclasse de Usuario informa o seu.
 * O codigo e o mesmo id da tabela perfis do banco.
 */
public enum Perfil {
    ADMINISTRADOR(1, "Administrador"),
    PROFESSOR(2, "Professor"),
    MONITOR(3, "Monitor"),
    ALUNO(4, "Aluno");

    private int codigo;
    private String nome;

    Perfil(int codigo, String nome) {
        this.codigo = codigo;
        this.nome = nome;
    }

    public int getCodigo() {
        return codigo;
    }

    public String getNome() {
        return nome;
    }

    public static Perfil buscarPorCodigo(int codigo) {
        for (Perfil perfil : values()) {
            if (perfil.codigo == codigo) {
                return perfil;
            }
        }
        return null;
    }
}
