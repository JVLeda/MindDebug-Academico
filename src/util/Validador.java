package util;

public class Validador {
    public String texto(String valor, String campo) {
        if (valor == null || valor.trim().isEmpty()) {
            throw new RuntimeException(campo + " e obrigatorio.");
        }
        return valor.trim();
    }

    public String email(String valor) {
        valor = texto(valor, "Email").toLowerCase();
        if (!valor.contains("@") || !valor.contains(".")) {
            throw new RuntimeException("Email invalido.");
        }
        return valor;
    }

    public String senha(String valor) {
        valor = texto(valor, "Senha");
        if (valor.length() < 4) {
            throw new RuntimeException("Senha deve ter pelo menos 4 caracteres.");
        }
        return valor;
    }

    public int nota(int valor) {
        if (valor < 0 || valor > 10) {
            throw new RuntimeException("Nota deve estar entre 0 e 10.");
        }
        return valor;
    }
}
