package util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Conexao {
    private static final String URL_PADRAO = "jdbc:mysql://localhost:3306/";
    private static final String USUARIO = "root";
    private static final String SENHA = "Leda123";

    public static Connection getConnection() throws SQLException {
        String banco = System.getProperty("minddebug.banco", "minddebug");
        return DriverManager.getConnection(URL_PADRAO + banco, USUARIO, SENHA);
    }
}
