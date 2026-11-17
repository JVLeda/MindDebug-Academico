package dao;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

/**
 * Confere se src/MindDebugBD.sql tem exatamente o esquema de EsquemaBanco.
 * O script e formatado em varias linhas, entao a comparacao ignora espacos e quebras de linha.
 */
public class ScriptBancoTest {
    public static void main(String[] args) throws FileNotFoundException {
        String script = lerArquivo("src/MindDebugBD.sql");
        String scriptNormalizado = normalizar(script);

        verificar(script.toLowerCase().contains("create database " + util.Conexao.BANCO.toLowerCase() + ";"),
                "O script deve criar o banco da aplicacao.");
        verificar(script.toLowerCase().contains("use " + util.Conexao.BANCO.toLowerCase() + ";"),
                "O script deve selecionar o banco da aplicacao.");
        verificar(!script.toLowerCase().contains("if not exists"), "O script nao pode usar IF NOT EXISTS.");

        String[] nomes = EsquemaBanco.nomesDasTabelas();
        String[] comandos = EsquemaBanco.comandosDeCriacao();
        int anterior = -1;
        for (int i = 0; i < comandos.length; i++) {
            int posicao = scriptNormalizado.indexOf(normalizar(comandos[i]) + ";");
            verificar(posicao >= 0, "O script esta desatualizado na tabela " + nomes[i] + ".");
            verificar(posicao > anterior, "O script deve criar a tabela " + nomes[i] + " na mesma ordem do esquema.");
            anterior = posicao;
        }
        verificar(contar(script, "CREATE TABLE ") == comandos.length, "O script nao deve ter tabelas a mais.");

        String[] dados = EsquemaBanco.comandosDeDadosFixos();
        for (String comando : dados) {
            verificar(scriptNormalizado.contains(normalizar(comando) + ";"), "Falta no script: " + comando);
        }
        verificar(contar(script, "INSERT INTO ") == dados.length, "O script deve ter somente os dados fixos.");
    }

    private static String normalizar(String texto) {
        String resultado = "";
        boolean espacoPendente = false;
        for (int i = 0; i < texto.length(); i++) {
            char letra = texto.charAt(i);
            if (Character.isWhitespace(letra)) {
                espacoPendente = true;
            } else {
                boolean colado = letra == ')' || resultado.endsWith("(") || resultado.length() == 0;
                if (espacoPendente && !colado) {
                    resultado = resultado + " ";
                }
                resultado = resultado + letra;
                espacoPendente = false;
            }
        }
        return resultado;
    }

    private static int contar(String texto, String trecho) {
        int quantidade = 0;
        int posicao = texto.indexOf(trecho);
        while (posicao >= 0) {
            quantidade++;
            posicao = texto.indexOf(trecho, posicao + trecho.length());
        }
        return quantidade;
    }

    private static String lerArquivo(String caminho) throws FileNotFoundException {
        Scanner leitor = new Scanner(new File(caminho), "UTF-8");
        String texto = "";
        try {
            while (leitor.hasNextLine()) {
                String linha = leitor.nextLine();
                if (!linha.trim().startsWith("--")) {
                    texto = texto + linha + "\n";
                }
            }
        } finally {
            leitor.close();
        }
        return texto;
    }

    private static void verificar(boolean condicao, String mensagem) {
        if (!condicao) {
            throw new AssertionError(mensagem);
        }
    }
}
