package dao;

import excecao.PersistenciaException;
import model.Entidade;
import util.Conexao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Base dos DAOs: abre e fecha a conexao, desfaz transacoes e preenche
 * parametros que podem ficar nulos no banco.
 */
public abstract class AcessoBanco {
    protected static final String FORMATO_DATA = "dd/MM/yyyy";

    public static String mensagemDeConexaoIndisponivel(String detalhe) {
        return "Nao foi possivel conectar ao MySQL em " + Conexao.descricao() + ". "
                + "Confirme se o servico esta ativo e se o banco configurado existe. Detalhe: " + detalhe;
    }

    protected Connection abrirConexao() {
        try {
            return Conexao.getConnection();
        } catch (SQLException erro) {
            throw new PersistenciaException(mensagemDeConexaoIndisponivel(erro.getMessage()));
        }
    }

    protected void desfazer(Connection conexao) {
        try {
            conexao.rollback();
        } catch (SQLException erro) {
            // A conexao e fechada logo em seguida; o MySQL descarta o que nao foi confirmado.
        }
    }

    protected void fechar(Connection conexao) {
        try {
            conexao.close();
        } catch (SQLException erro) {
            // Nada a fazer: a conexao ja nao pode ser usada.
        }
    }

    protected void definirInteiroOuNulo(PreparedStatement comando, int posicao, int valor) throws SQLException {
        if (valor == 0) {
            comando.setNull(posicao, Types.INTEGER);
        } else {
            comando.setInt(posicao, valor);
        }
    }

    protected void definirTextoOuNulo(PreparedStatement comando, int posicao, String valor) throws SQLException {
        if (valor == null || valor.trim().isEmpty()) {
            comando.setNull(posicao, Types.VARCHAR);
        } else {
            comando.setString(posicao, valor);
        }
    }

    protected void definirIdOuNulo(PreparedStatement comando, int posicao, Entidade entidade) throws SQLException {
        if (entidade == null) {
            comando.setNull(posicao, Types.INTEGER);
        } else {
            comando.setInt(posicao, entidade.getId());
        }
    }

    protected void definirData(PreparedStatement comando, int posicao, Date data) throws SQLException {
        if (data == null) {
            comando.setNull(posicao, Types.DATE);
        } else {
            comando.setDate(posicao, new java.sql.Date(data.getTime()));
        }
    }

    /**
     * Le o id que o AUTO_INCREMENT acabou de gerar na tabela. A consulta roda na mesma transacao
     * e na mesma conexao do INSERT, entao o maior id e o da linha que acabou de entrar.
     */
    protected int lerUltimoId(Connection conexao, String tabela) throws SQLException {
        PreparedStatement consulta = conexao.prepareStatement("SELECT MAX(id) FROM " + tabela);
        ResultSet resultado = consulta.executeQuery();
        resultado.next();
        int id = resultado.getInt(1);
        resultado.close();
        consulta.close();
        return id;
    }

    /** Roda um SELECT COUNT(*) cujo unico parametro e um id. */
    protected int contarPorId(Connection conexao, String sql, int id) throws SQLException {
        PreparedStatement consulta = conexao.prepareStatement(sql);
        consulta.setInt(1, id);
        return contar(consulta);
    }

    /** Roda um SELECT COUNT(*) com os parametros ja preenchidos e fecha a consulta. */
    protected int contar(PreparedStatement consulta) throws SQLException {
        ResultSet resultado = consulta.executeQuery();
        resultado.next();
        int quantidade = resultado.getInt(1);
        resultado.close();
        consulta.close();
        return quantidade;
    }

    /** Converte a data digitada (dd/mm/aaaa) para gravar numa coluna DATE. Texto vazio vira nulo. */
    protected Date converterTextoEmData(String texto) {
        if (texto == null || texto.trim().isEmpty()) {
            return null;
        }
        SimpleDateFormat formato = new SimpleDateFormat(FORMATO_DATA);
        formato.setLenient(false);
        try {
            return formato.parse(texto.trim());
        } catch (ParseException erro) {
            throw new PersistenciaException("Data invalida: " + texto + ". Use o formato dd/mm/aaaa.");
        }
    }

    /** Converte a data lida do banco para o texto dd/mm/aaaa usado no modelo. */
    protected String textoDaData(Date data) {
        if (data == null) {
            return "";
        }
        return new SimpleDateFormat(FORMATO_DATA).format(data);
    }

    protected String textoOuVazio(String texto) {
        if (texto == null) {
            return "";
        }
        return texto;
    }
}
