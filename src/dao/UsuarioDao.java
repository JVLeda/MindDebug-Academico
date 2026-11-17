package dao;

import excecao.PersistenciaException;
import excecao.RegraDeNegocioException;
import model.Administrador;
import model.Curso;
import model.Estudante;
import model.Monitor;
import model.NivelCurso;
import model.Perfil;
import model.Professor;
import model.Usuario;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Acesso a tabela usuarios e as tabelas de cada perfil (estudantes, professores e monitores).
 * Toda gravacao que mexe em mais de uma tabela acontece numa transacao:
 * ou tudo e gravado, ou nada muda no banco.
 */
public class UsuarioDao extends AcessoBanco {

    // Os dados do curso vem junto para montar o Estudante sem outra consulta.
    private static final String CONSULTA =
            "SELECT u.id, u.nome, u.login, u.email, u.senha, u.matricula, u.perfil_id, u.ativo, u.trocar_senha, "
                    + "e.curso_id, c.nome AS curso_nome, c.sigla AS curso_sigla, c.nivel AS curso_nivel, "
                    + "c.area AS curso_area, c.carga_horaria AS curso_carga_horaria, "
                    + "c.coordenador AS curso_coordenador, "
                    + "p.titulacao, p.area_atuacao, m.tipo "
                    + "FROM usuarios u "
                    + "LEFT JOIN estudantes e ON e.usuario_id = u.id "
                    + "LEFT JOIN cursos c ON c.id = e.curso_id "
                    + "LEFT JOIN professores p ON p.usuario_id = u.id "
                    + "LEFT JOIN monitores m ON m.usuario_id = u.id ";

    // ------------------------------------------------------------------ consultas

    public ArrayList<Usuario> listar() {
        return consultar(CONSULTA + "ORDER BY u.id", null, 0);
    }

    public ArrayList<Usuario> listarPorPerfil(Perfil perfil) {
        return consultar(CONSULTA + "WHERE u.perfil_id = ? ORDER BY u.id", null, perfil.getCodigo());
    }

    public Usuario buscarPorLogin(String login) {
        ArrayList<Usuario> encontrados = consultar(CONSULTA + "WHERE u.login = ?", login, 0);
        if (encontrados.isEmpty()) {
            return null;
        }
        return encontrados.get(0);
    }

    /** Roda a consulta com um parametro de texto, um numero maior que zero, ou nenhum dos dois. */
    private ArrayList<Usuario> consultar(String sql, String texto, int numero) {
        Connection conexao = abrirConexao();
        try {
            PreparedStatement consulta = conexao.prepareStatement(sql);
            if (texto != null) {
                consulta.setString(1, texto);
            } else if (numero > 0) {
                consulta.setInt(1, numero);
            }
            ArrayList<Usuario> usuarios = lerUsuarios(consulta, new HashMap<Integer, Curso>());
            consulta.close();
            return usuarios;
        } catch (SQLException erro) {
            throw new PersistenciaException("Erro ao consultar usuarios: " + erro.getMessage());
        } finally {
            fechar(conexao);
        }
    }

    /**
     * Usado pelo BancoDao ao carregar tudo: os estudantes recebem os mesmos objetos Curso
     * que o BancoDao ja leu.
     */
    ArrayList<Usuario> listar(Connection conexao, HashMap<Integer, Curso> cursos) throws SQLException {
        PreparedStatement consulta = conexao.prepareStatement(CONSULTA + "ORDER BY u.id");
        ArrayList<Usuario> usuarios = lerUsuarios(consulta, cursos);
        consulta.close();
        return usuarios;
    }

    private ArrayList<Usuario> lerUsuarios(PreparedStatement consulta, HashMap<Integer, Curso> cursos)
            throws SQLException {
        ArrayList<Usuario> usuarios = new ArrayList<>();
        ResultSet resultado = consulta.executeQuery();
        while (resultado.next()) {
            Usuario usuario = criarUsuario(resultado, cursos);
            usuario.setId(resultado.getInt("id"));
            usuario.setMatricula(resultado.getString("matricula"));
            usuario.setAtivo(resultado.getBoolean("ativo"));
            usuario.setTrocaDeSenhaObrigatoria(resultado.getBoolean("trocar_senha"));
            usuarios.add(usuario);
        }
        resultado.close();
        return usuarios;
    }

    private Usuario criarUsuario(ResultSet resultado, HashMap<Integer, Curso> cursos) throws SQLException {
        Perfil perfil = Perfil.buscarPorCodigo(resultado.getInt("perfil_id"));
        if (perfil == null) {
            throw new PersistenciaException("Usuario com perfil desconhecido: " + resultado.getInt("perfil_id"));
        }

        String nome = resultado.getString("nome");
        String login = resultado.getString("login");
        String email = resultado.getString("email");
        String senha = resultado.getString("senha");

        switch (perfil) {
            case ADMINISTRADOR:
                return new Administrador(nome, login, email, senha);
            case PROFESSOR:
                Professor professor = new Professor(nome, login, email, senha);
                professor.setTitulacao(textoOuVazio(resultado.getString("titulacao")));
                professor.setAreaAtuacao(textoOuVazio(resultado.getString("area_atuacao")));
                return professor;
            case MONITOR:
                return new Monitor(nome, login, email, senha, resultado.getString("tipo"));
            default:
                Curso curso = buscarCurso(resultado, cursos);
                return new Estudante(nome, email, resultado.getString("matricula"), senha, curso);
        }
    }

    /** Reaproveita o curso ja montado; dois estudantes do mesmo curso apontam para o mesmo objeto. */
    private Curso buscarCurso(ResultSet resultado, HashMap<Integer, Curso> cursos) throws SQLException {
        int cursoId = resultado.getInt("curso_id");
        if (cursoId == 0) {
            return null;
        }
        if (cursos.containsKey(cursoId)) {
            return cursos.get(cursoId);
        }
        Curso curso = new Curso(
                resultado.getString("curso_nome"),
                resultado.getString("curso_sigla"),
                resultado.getString("curso_area"),
                resultado.getInt("curso_carga_horaria"),
                resultado.getString("curso_coordenador")
        );
        curso.setId(cursoId);
        curso.setNivel(NivelCurso.buscarPorNome(resultado.getString("curso_nivel")));
        cursos.put(cursoId, curso);
        return curso;
    }

    public HashMap<Integer, Usuario> usuariosPorId() {
        HashMap<Integer, Usuario> mapa = new HashMap<>();
        for (Usuario usuario : listar()) {
            mapa.put(usuario.getId(), usuario);
        }
        return mapa;
    }

    public HashMap<Integer, Estudante> estudantesPorId() {
        ArrayList<Estudante> estudantes = new ArrayList<>();
        for (Usuario usuario : listarPorPerfil(Perfil.ALUNO)) {
            usuario.incluirEmEstudantes(estudantes);
        }
        HashMap<Integer, Estudante> mapa = new HashMap<>();
        for (Estudante estudante : estudantes) {
            mapa.put(estudante.getId(), estudante);
        }
        return mapa;
    }

    public HashMap<Integer, Monitor> monitoresPorId() {
        ArrayList<Monitor> monitores = new ArrayList<>();
        for (Usuario usuario : listarPorPerfil(Perfil.MONITOR)) {
            usuario.incluirEmMonitores(monitores);
        }
        HashMap<Integer, Monitor> mapa = new HashMap<>();
        for (Monitor monitor : monitores) {
            mapa.put(monitor.getId(), monitor);
        }
        return mapa;
    }

    public HashMap<Integer, Professor> professoresPorId() {
        ArrayList<Professor> professores = new ArrayList<>();
        for (Usuario usuario : listarPorPerfil(Perfil.PROFESSOR)) {
            usuario.incluirEmProfessores(professores);
        }
        HashMap<Integer, Professor> mapa = new HashMap<>();
        for (Professor professor : professores) {
            mapa.put(professor.getId(), professor);
        }
        return mapa;
    }

    /** Usado pelo ParticipanteDao: alunos e monitores ligados a materia. */
    ArrayList<Usuario> listarParticipantesDaMateria(int materiaId) {
        return consultar(CONSULTA + "JOIN participantes pa ON pa.usuario_id = u.id WHERE pa.materia_id = ? ORDER BY u.id",
                null, materiaId);
    }

    // ------------------------------------------------------------------ gravacao

    /** Cadastra o usuario e os dados do perfil dele. O id gerado pelo banco e colocado no objeto. */
    public void inserir(Usuario usuario) {
        Connection conexao = abrirConexao();
        boolean gravou = false;
        try {
            conexao.setAutoCommit(false);
            verificarDadosLivres(conexao, usuario, 0);

            PreparedStatement comando = conexao.prepareStatement(
                    "INSERT INTO usuarios (nome, login, email, senha, matricula, perfil_id, ativo, trocar_senha) "
                            + "VALUES (?, ?, ?, ?, ?, ?, ?, ?)");
            preencherUsuario(comando, 1, usuario);
            comando.executeUpdate();
            comando.close();

            int idGerado = lerUltimoId(conexao, "usuarios");
            inserirDadosDoPerfil(conexao, usuario, idGerado);

            conexao.commit();
            gravou = true;
            usuario.setId(idGerado);
        } catch (SQLException erro) {
            throw new PersistenciaException("Erro ao cadastrar o usuario. Nada foi gravado. Detalhe: "
                    + erro.getMessage());
        } finally {
            if (!gravou) {
                desfazer(conexao);
            }
            fechar(conexao);
        }
    }

    /**
     * Usado pelo BancoDao, dentro da transacao dele, para regravar um usuario com o id que ja tem.
     */
    void inserirComId(Connection conexao, Usuario usuario) throws SQLException {
        PreparedStatement comando = conexao.prepareStatement(
                "INSERT INTO usuarios (id, nome, login, email, senha, matricula, perfil_id, ativo, trocar_senha) "
                        + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)");
        comando.setInt(1, usuario.getId());
        preencherUsuario(comando, 2, usuario);
        comando.executeUpdate();
        comando.close();

        inserirDadosDoPerfil(conexao, usuario, usuario.getId());
    }

    public void atualizar(Usuario usuario) {
        Connection conexao = abrirConexao();
        boolean gravou = false;
        try {
            conexao.setAutoCommit(false);
            verificarDadosLivres(conexao, usuario, usuario.getId());

            PreparedStatement comando = conexao.prepareStatement(
                    "UPDATE usuarios SET nome = ?, login = ?, email = ?, senha = ?, matricula = ?, perfil_id = ?, "
                            + "ativo = ?, trocar_senha = ? WHERE id = ?");
            preencherUsuario(comando, 1, usuario);
            comando.setInt(9, usuario.getId());
            int alteradas = comando.executeUpdate();
            comando.close();
            if (alteradas == 0) {
                throw new RegraDeNegocioException("Usuario nao encontrado no banco.");
            }

            atualizarDadosDoPerfil(conexao, usuario);

            conexao.commit();
            gravou = true;
        } catch (SQLException erro) {
            throw new PersistenciaException("Erro ao atualizar o usuario. Nada foi alterado. Detalhe: "
                    + erro.getMessage());
        } finally {
            if (!gravou) {
                desfazer(conexao);
            }
            fechar(conexao);
        }
    }

    /** Grava a senha nova e se o usuario precisa troca-la no proximo acesso. */
    public void atualizarSenha(Usuario usuario, String senha, boolean trocaObrigatoria) {
        Connection conexao = abrirConexao();
        try {
            PreparedStatement comando = conexao.prepareStatement(
                    "UPDATE usuarios SET senha = ?, trocar_senha = ? WHERE id = ?");
            comando.setString(1, senha);
            comando.setBoolean(2, trocaObrigatoria);
            comando.setInt(3, usuario.getId());
            int alteradas = comando.executeUpdate();
            comando.close();
            if (alteradas == 0) {
                throw new RegraDeNegocioException("Usuario nao encontrado no banco.");
            }
        } catch (SQLException erro) {
            throw new PersistenciaException("Erro ao gravar a senha: " + erro.getMessage());
        } finally {
            fechar(conexao);
        }
    }

    /** O usuario continua no banco, com o historico dele, mas nao consegue mais entrar. */
    public void desativar(Usuario usuario) {
        Connection conexao = abrirConexao();
        try {
            PreparedStatement comando = conexao.prepareStatement("UPDATE usuarios SET ativo = FALSE WHERE id = ?");
            comando.setInt(1, usuario.getId());
            int alteradas = comando.executeUpdate();
            comando.close();
            if (alteradas == 0) {
                throw new RegraDeNegocioException("Usuario nao encontrado no banco.");
            }
            usuario.setAtivo(false);
        } catch (SQLException erro) {
            throw new PersistenciaException("Erro ao desativar o usuario: " + erro.getMessage());
        } finally {
            fechar(conexao);
        }
    }

    /** Apaga o usuario. Se ele tiver feedbacks, mensagens ou outros registros, o banco recusa. */
    public void remover(Usuario usuario) {
        Connection conexao = abrirConexao();
        boolean gravou = false;
        try {
            conexao.setAutoCommit(false);
            String[] tabelasDoPerfil = {"estudantes", "professores", "monitores"};
            for (String tabela : tabelasDoPerfil) {
                PreparedStatement comando = conexao.prepareStatement("DELETE FROM " + tabela + " WHERE usuario_id = ?");
                comando.setInt(1, usuario.getId());
                comando.executeUpdate();
                comando.close();
            }

            PreparedStatement comando = conexao.prepareStatement("DELETE FROM usuarios WHERE id = ?");
            comando.setInt(1, usuario.getId());
            comando.executeUpdate();
            comando.close();

            conexao.commit();
            gravou = true;
        } catch (SQLIntegrityConstraintViolationException erro) {
            throw new RegraDeNegocioException("O usuario tem registros ligados a ele e nao pode ser apagado. "
                    + "Desative o usuario.");
        } catch (SQLException erro) {
            throw new PersistenciaException("Erro ao remover o usuario: " + erro.getMessage());
        } finally {
            if (!gravou) {
                desfazer(conexao);
            }
            fechar(conexao);
        }
    }

    // ------------------------------------------------------------------ apoio

    /** Preenche nome, login, email, senha, matricula, perfil, ativo e trocar_senha a partir da posicao dada. */
    private void preencherUsuario(PreparedStatement comando, int inicio, Usuario usuario) throws SQLException {
        comando.setString(inicio, usuario.getNome());
        comando.setString(inicio + 1, usuario.getLogin());
        comando.setString(inicio + 2, usuario.getEmail());
        comando.setString(inicio + 3, usuario.getSenha());
        definirTextoOuNulo(comando, inicio + 4, usuario.getMatricula());
        comando.setInt(inicio + 5, usuario.getPerfil().getCodigo());
        comando.setBoolean(inicio + 6, usuario.isAtivo());
        comando.setBoolean(inicio + 7, usuario.isTrocaDeSenhaObrigatoria());
    }

    /**
     * Login, email e matricula sao UNIQUE no banco. A consulta antes da gravacao
     * permite mostrar uma mensagem clara. O id informado e ignorado (0 no cadastro).
     */
    private void verificarDadosLivres(Connection conexao, Usuario usuario, int idIgnorado) throws SQLException {
        if (existeOutro(conexao, "login", usuario.getLogin(), idIgnorado)) {
            throw new RegraDeNegocioException("Login ja cadastrado.");
        }
        if (existeOutro(conexao, "email", usuario.getEmail(), idIgnorado)) {
            throw new RegraDeNegocioException("Email ja cadastrado.");
        }
        if (usuario.getMatricula() != null && existeOutro(conexao, "matricula", usuario.getMatricula(), idIgnorado)) {
            throw new RegraDeNegocioException("Matricula ja cadastrada.");
        }
    }

    private boolean existeOutro(Connection conexao, String coluna, String valor, int idIgnorado) throws SQLException {
        PreparedStatement consulta = conexao.prepareStatement(
                "SELECT COUNT(*) FROM usuarios WHERE " + coluna + " = ? AND id <> ?");
        consulta.setString(1, valor);
        consulta.setInt(2, idIgnorado);
        return contar(consulta) > 0;
    }

    /** Cada usuario entra em uma lista so, a do seu perfil; o administrador nao tem tabela propria. */
    private void inserirDadosDoPerfil(Connection conexao, Usuario usuario, int id) throws SQLException {
        ArrayList<Estudante> estudantes = new ArrayList<>();
        ArrayList<Professor> professores = new ArrayList<>();
        ArrayList<Monitor> monitores = new ArrayList<>();
        usuario.incluirEmEstudantes(estudantes);
        usuario.incluirEmProfessores(professores);
        usuario.incluirEmMonitores(monitores);

        for (Estudante estudante : estudantes) {
            PreparedStatement comando = conexao.prepareStatement(
                    "INSERT INTO estudantes (usuario_id, curso_id) VALUES (?, ?)");
            comando.setInt(1, id);
            definirIdOuNulo(comando, 2, estudante.getCurso());
            comando.executeUpdate();
            comando.close();
        }
        for (Professor professor : professores) {
            PreparedStatement comando = conexao.prepareStatement(
                    "INSERT INTO professores (usuario_id, titulacao, area_atuacao) VALUES (?, ?, ?)");
            comando.setInt(1, id);
            definirTextoOuNulo(comando, 2, professor.getTitulacao());
            definirTextoOuNulo(comando, 3, professor.getAreaAtuacao());
            comando.executeUpdate();
            comando.close();
        }
        for (Monitor monitor : monitores) {
            PreparedStatement comando = conexao.prepareStatement(
                    "INSERT INTO monitores (usuario_id, tipo) VALUES (?, ?)");
            comando.setInt(1, id);
            comando.setString(2, monitor.getTipo());
            comando.executeUpdate();
            comando.close();
        }
    }

    private void atualizarDadosDoPerfil(Connection conexao, Usuario usuario) throws SQLException {
        ArrayList<Estudante> estudantes = new ArrayList<>();
        ArrayList<Professor> professores = new ArrayList<>();
        ArrayList<Monitor> monitores = new ArrayList<>();
        usuario.incluirEmEstudantes(estudantes);
        usuario.incluirEmProfessores(professores);
        usuario.incluirEmMonitores(monitores);

        for (Estudante estudante : estudantes) {
            PreparedStatement comando = conexao.prepareStatement(
                    "UPDATE estudantes SET curso_id = ? WHERE usuario_id = ?");
            definirIdOuNulo(comando, 1, estudante.getCurso());
            comando.setInt(2, estudante.getId());
            comando.executeUpdate();
            comando.close();
        }
        for (Professor professor : professores) {
            PreparedStatement comando = conexao.prepareStatement(
                    "UPDATE professores SET titulacao = ?, area_atuacao = ? WHERE usuario_id = ?");
            definirTextoOuNulo(comando, 1, professor.getTitulacao());
            definirTextoOuNulo(comando, 2, professor.getAreaAtuacao());
            comando.setInt(3, professor.getId());
            comando.executeUpdate();
            comando.close();
        }
        for (Monitor monitor : monitores) {
            PreparedStatement comando = conexao.prepareStatement(
                    "UPDATE monitores SET tipo = ? WHERE usuario_id = ?");
            comando.setString(1, monitor.getTipo());
            comando.setInt(2, monitor.getId());
            comando.executeUpdate();
            comando.close();
        }
    }
}
