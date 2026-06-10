package util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

public class Conexao {
    private static final String URL_SERVIDOR = "jdbc:mysql://127.0.0.1:3306/?useSSL=false&serverTimezone=America/Sao_Paulo";
    private static final String URL_BANCO = "jdbc:mysql://127.0.0.1:3306/minddebug?useSSL=false&serverTimezone=America/Sao_Paulo";
    private static final String USUARIO = "root";
    private static final String SENHA = "Leda123";

    public static Connection abrirServidor() {
        try {
            return DriverManager.getConnection(URL_SERVIDOR, USUARIO, SENHA);
        } catch (Exception erro) {
            throw new RuntimeException("Nao foi possivel conectar ao servidor MySQL: " + erro.getMessage());
        }
    }

    public static Connection abrirBanco() {
        criarBancoSeNaoExistir();
        try {
            return DriverManager.getConnection(URL_BANCO, USUARIO, SENHA);
        } catch (Exception erro) {
            throw new RuntimeException("Nao foi possivel conectar ao banco minddebug: " + erro.getMessage());
        }
    }

    public static void criarBancoSeNaoExistir() {
        try {
            Connection conexao = abrirServidor();
            Statement comando = conexao.createStatement();
            comando.executeUpdate("CREATE DATABASE IF NOT EXISTS minddebug");
            comando.close();
            conexao.close();
        } catch (Exception erro) {
            throw new RuntimeException("Nao foi possivel criar o banco minddebug: " + erro.getMessage());
        }
    }
}
