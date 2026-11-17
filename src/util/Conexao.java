package util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Fonte unica da configuracao de acesso ao MySQL.
 * Os valores sao os mesmos do compose.yaml e servem apenas
 * para o ambiente local de desenvolvimento.
 */
public class Conexao {
    public static final String HOST = "localhost";
    public static final String PORTA = "3306";
    public static final String BANCO = "minddebug";
    public static final String USUARIO = "minddebug";
    public static final String SENHA = "minddebug";

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(getUrl(), USUARIO, SENHA);
    }

    public static String getUrl() {
        return "jdbc:mysql://" + descricao();
    }

    /** Destino da conexao sem expor a senha. Ex.: localhost:3306/minddebug */
    public static String descricao() {
        return HOST + ":" + PORTA + "/" + BANCO;
    }
}
