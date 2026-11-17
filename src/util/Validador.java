package util;

import excecao.DadoInvalidoException;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Validador {
    public String texto(String valor, String campo) {
        if (valor == null || valor.trim().isEmpty()) {
            throw new DadoInvalidoException(campo + " e obrigatorio.");
        }
        return valor.trim();
    }

    public String email(String valor) {
        valor = texto(valor, "Email").toLowerCase();
        if (!valor.contains("@") || !valor.contains(".")) {
            throw new DadoInvalidoException("Email invalido.");
        }
        return valor;
    }

    public String senha(String valor) {
        valor = texto(valor, "Senha");
        if (valor.length() < 4) {
            throw new DadoInvalidoException("Senha deve ter pelo menos 4 caracteres.");
        }
        return valor;
    }

    public int nota(int valor) {
        if (valor < 0 || valor > 10) {
            throw new DadoInvalidoException("Nota deve estar entre 0 e 10.");
        }
        return valor;
    }

    public int positivo(int valor, String campo) {
        if (valor <= 0) {
            throw new DadoInvalidoException(campo + " deve ser maior que zero.");
        }
        return valor;
    }

    public int semestre(int valor) {
        if (valor != 1 && valor != 2) {
            throw new DadoInvalidoException("Semestre deve ser 1 ou 2.");
        }
        return valor;
    }

    public double mediaEsperada(double valor) {
        if (valor < 0 || valor > 10) {
            throw new DadoInvalidoException("Media esperada deve estar entre 0 e 10.");
        }
        return valor;
    }

    public String textoDeData(String valor, String campo) {
        data(valor, campo);
        return valor.trim();
    }

    public String hora(String valor, String campo) {
        valor = texto(valor, campo);
        boolean valida = valor.length() == 5 && valor.charAt(2) == ':'
                && Character.isDigit(valor.charAt(0)) && Character.isDigit(valor.charAt(1))
                && Character.isDigit(valor.charAt(3)) && Character.isDigit(valor.charAt(4));
        if (valida) {
            int horas = Integer.parseInt(valor.substring(0, 2));
            int minutos = Integer.parseInt(valor.substring(3, 5));
            valida = horas <= 23 && minutos <= 59;
        }
        if (!valida) {
            throw new DadoInvalidoException(campo + " deve estar no formato HH:mm.");
        }
        return valor;
    }

    public Date data(String valor, String campo) {
        valor = texto(valor, campo);
        String mensagem = campo + " deve estar no formato dd/mm/aaaa.";
        SimpleDateFormat formato = new SimpleDateFormat("dd/MM/yyyy");
        formato.setLenient(false);
        try {
            Date data = formato.parse(valor);
            if (!formato.format(data).equals(valor)) {
                throw new DadoInvalidoException(mensagem);
            }
            return data;
        } catch (ParseException erro) {
            throw new DadoInvalidoException(mensagem);
        }
    }
}
