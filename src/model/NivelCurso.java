package model;

public enum NivelCurso {
    GRADUACAO("Graduacao"),
    POS_GRADUACAO("Pos-graduacao");

    private String nome;

    NivelCurso(String nome) {
        this.nome = nome;
    }

    public String getNome() {
        return nome;
    }

    public static NivelCurso buscarPorNome(String nome) {
        for (NivelCurso nivel : values()) {
            if (nivel.nome.equals(nome)) {
                return nivel;
            }
        }
        return null;
    }
}
