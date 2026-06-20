package dao;

import model.Aula;
import model.Chat;
import model.Curso;
import model.Dev;
import model.Estudante;
import model.Feedback;
import model.GrupoEstudo;
import model.Materia;
import model.Material;
import model.Mensagem;
import model.Monitor;
import model.PedidoMonitoria;
import model.Periodo;
import model.Professor;
import model.Usuario;
import util.Conexao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.HashMap;

public class BancoDao {
    public BancoDados carregar() {
        criarTabelas();

        BancoDados banco = new BancoDados();
        HashMap<Integer, Curso> cursos = carregarCursos(banco);
        HashMap<Integer, Usuario> usuarios = carregarUsuarios(banco, cursos);
        HashMap<Integer, Periodo> periodos = carregarPeriodos(cursos);
        HashMap<Integer, Materia> materias = carregarMaterias(periodos, usuarios);
        HashMap<Integer, Aula> aulas = carregarAulas(materias);
        HashMap<Integer, GrupoEstudo> grupos = carregarGrupos(materias, aulas, usuarios);

        carregarParticipantes(materias, usuarios);
        carregarFeedbacks(aulas, usuarios);
        carregarMateriais(materias, usuarios);
        carregarMensagens(materias, aulas, usuarios);
        carregarAlunosDosGrupos(grupos, usuarios);
        carregarMonitoresDosGrupos(grupos, usuarios);
        carregarPedidosMonitoria(materias, aulas, usuarios);
        ajustarProximoId(banco);

        return banco;
    }

    public void salvar(BancoDados banco) {
        criarTabelas();
        limparTabelas();
        salvarCursos(banco);
        salvarUsuarios(banco);
        salvarPeriodos(banco);
        salvarMaterias(banco);
        salvarParticipantes(banco);
        salvarAulas(banco);
        salvarFeedbacks(banco);
        salvarMateriais(banco);
        salvarMensagens(banco);
        salvarGrupos(banco);
        salvarAlunosDosGrupos(banco);
        salvarMonitoresDosGrupos(banco);
        salvarPedidosMonitoria(banco);
    }

    private void criarTabelas() {
        try {
            Connection conexao = Conexao.getConnection();
            Statement comando = conexao.createStatement();

            comando.executeUpdate("CREATE TABLE IF NOT EXISTS cursos (id INT PRIMARY KEY, nome VARCHAR(100), sigla VARCHAR(20), area VARCHAR(100), carga_horaria INT, coordenador VARCHAR(100))");
            comando.executeUpdate("CREATE TABLE IF NOT EXISTS usuarios (id INT PRIMARY KEY, nome VARCHAR(100), login VARCHAR(50), email VARCHAR(100), senha VARCHAR(50), cargo VARCHAR(30), tipo_monitor VARCHAR(30), matricula VARCHAR(50), curso_id INT)");
            comando.executeUpdate("CREATE TABLE IF NOT EXISTS periodos (id INT PRIMARY KEY, curso_id INT, numero INT, nome VARCHAR(100))");
            comando.executeUpdate("CREATE TABLE IF NOT EXISTS materias (id INT PRIMARY KEY, periodo_id INT, nome VARCHAR(100), professor_id INT)");
            comando.executeUpdate("CREATE TABLE IF NOT EXISTS participantes (materia_id INT, usuario_id INT)");
            comando.executeUpdate("CREATE TABLE IF NOT EXISTS aulas (id INT PRIMARY KEY, materia_id INT, titulo VARCHAR(100), data_aula VARCHAR(20), objetivo VARCHAR(200))");
            comando.executeUpdate("CREATE TABLE IF NOT EXISTS feedbacks (id INT PRIMARY KEY AUTO_INCREMENT, aula_id INT, estudante_id INT, nota INT, comentario VARCHAR(200))");
            comando.executeUpdate("CREATE TABLE IF NOT EXISTS materiais (id INT PRIMARY KEY, materia_id INT, titulo VARCHAR(100), tipo VARCHAR(30), referencia VARCHAR(300), autor_id INT)");
            comando.executeUpdate("CREATE TABLE IF NOT EXISTS mensagens (id INT PRIMARY KEY, origem VARCHAR(20), origem_id INT, autor_id INT, texto VARCHAR(500))");
            comando.executeUpdate("CREATE TABLE IF NOT EXISTS grupos_estudo (id INT PRIMARY KEY, materia_id INT, nome VARCHAR(100), aula_id INT, monitor_id INT)");
            comando.executeUpdate("CREATE TABLE IF NOT EXISTS grupos_estudo_alunos (grupo_id INT, estudante_id INT)");
            comando.executeUpdate("CREATE TABLE IF NOT EXISTS grupos_estudo_monitores (grupo_id INT, monitor_id INT)");
            comando.executeUpdate("CREATE TABLE IF NOT EXISTS pedidos_monitoria (id INT PRIMARY KEY, materia_id INT, estudante_id INT, aula_id INT, monitor_id INT, duvida VARCHAR(300), status VARCHAR(30), data_monitoria VARCHAR(20), horario VARCHAR(10))");

            comando.close();
            conexao.close();
        } catch (Exception erro) {
            throw new RuntimeException("Erro ao criar tabelas: " + erro.getMessage());
        }
    }

    private void limparTabelas() {
        try {
            Connection conexao = Conexao.getConnection();
            Statement comando = conexao.createStatement();

            comando.executeUpdate("DELETE FROM grupos_estudo_alunos");
            comando.executeUpdate("DELETE FROM grupos_estudo_monitores");
            comando.executeUpdate("DELETE FROM pedidos_monitoria");
            comando.executeUpdate("DELETE FROM grupos_estudo");
            comando.executeUpdate("DELETE FROM mensagens");
            comando.executeUpdate("DELETE FROM materiais");
            comando.executeUpdate("DELETE FROM feedbacks");
            comando.executeUpdate("DELETE FROM aulas");
            comando.executeUpdate("DELETE FROM participantes");
            comando.executeUpdate("DELETE FROM materias");
            comando.executeUpdate("DELETE FROM periodos");
            comando.executeUpdate("DELETE FROM usuarios");
            comando.executeUpdate("DELETE FROM cursos");

            comando.close();
            conexao.close();
        } catch (Exception erro) {
            throw new RuntimeException("Erro ao limpar tabelas: " + erro.getMessage());
        }
    }

    private HashMap<Integer, Curso> carregarCursos(BancoDados banco) {
        HashMap<Integer, Curso> mapa = new HashMap<>();

        try {
            Connection conexao = Conexao.getConnection();
            PreparedStatement comando = conexao.prepareStatement("SELECT * FROM cursos");
            ResultSet resultado = comando.executeQuery();

            while (resultado.next()) {
                Curso curso = new Curso(
                        resultado.getString("nome"),
                        resultado.getString("sigla"),
                        resultado.getString("area"),
                        resultado.getInt("carga_horaria"),
                        resultado.getString("coordenador")
                );
                curso.setId(resultado.getInt("id"));
                banco.getCursos().add(curso);
                mapa.put(curso.getId(), curso);
            }

            resultado.close();
            comando.close();
            conexao.close();
        } catch (Exception erro) {
            throw new RuntimeException("Erro ao carregar cursos: " + erro.getMessage());
        }

        return mapa;
    }

    private HashMap<Integer, Usuario> carregarUsuarios(BancoDados banco, HashMap<Integer, Curso> cursos) {
        HashMap<Integer, Usuario> mapa = new HashMap<>();

        try {
            Connection conexao = Conexao.getConnection();
            PreparedStatement comando = conexao.prepareStatement("SELECT * FROM usuarios");
            ResultSet resultado = comando.executeQuery();

            while (resultado.next()) {
                String cargo = resultado.getString("cargo");
                Usuario usuario;

                if ("Dev".equals(cargo)) {
                    usuario = new Dev(
                            resultado.getString("nome"),
                            resultado.getString("login"),
                            resultado.getString("email"),
                            resultado.getString("senha")
                    );
                } else if ("Docente".equals(cargo)) {
                    usuario = new Professor(
                            resultado.getString("nome"),
                            resultado.getString("login"),
                            resultado.getString("email"),
                            resultado.getString("senha")
                    );
                } else if ("Monitor".equals(cargo)) {
                    usuario = new Monitor(
                            resultado.getString("nome"),
                            resultado.getString("login"),
                            resultado.getString("email"),
                            resultado.getString("senha"),
                            resultado.getString("tipo_monitor")
                    );
                } else {
                    Curso curso = cursos.get(resultado.getInt("curso_id"));
                    usuario = new Estudante(
                            resultado.getString("nome"),
                            resultado.getString("email"),
                            resultado.getString("matricula"),
                            resultado.getString("senha"),
                            curso
                    );
                }

                usuario.setId(resultado.getInt("id"));
                banco.getUsuarios().add(usuario);
                mapa.put(usuario.getId(), usuario);
            }

            resultado.close();
            comando.close();
            conexao.close();
        } catch (Exception erro) {
            throw new RuntimeException("Erro ao carregar usuarios: " + erro.getMessage());
        }

        return mapa;
    }

    private HashMap<Integer, Periodo> carregarPeriodos(HashMap<Integer, Curso> cursos) {
        HashMap<Integer, Periodo> mapa = new HashMap<>();

        try {
            Connection conexao = Conexao.getConnection();
            PreparedStatement comando = conexao.prepareStatement("SELECT * FROM periodos");
            ResultSet resultado = comando.executeQuery();

            while (resultado.next()) {
                Periodo periodo = new Periodo(resultado.getInt("numero"), resultado.getString("nome"));
                periodo.setId(resultado.getInt("id"));

                Curso curso = cursos.get(resultado.getInt("curso_id"));
                if (curso != null) {
                    curso.adicionarPeriodo(periodo);
                    mapa.put(periodo.getId(), periodo);
                }
            }

            resultado.close();
            comando.close();
            conexao.close();
        } catch (Exception erro) {
            throw new RuntimeException("Erro ao carregar periodos: " + erro.getMessage());
        }

        return mapa;
    }

    private HashMap<Integer, Materia> carregarMaterias(HashMap<Integer, Periodo> periodos, HashMap<Integer, Usuario> usuarios) {
        HashMap<Integer, Materia> mapa = new HashMap<>();

        try {
            Connection conexao = Conexao.getConnection();
            PreparedStatement comando = conexao.prepareStatement("SELECT * FROM materias");
            ResultSet resultado = comando.executeQuery();

            while (resultado.next()) {
                Usuario usuario = usuarios.get(resultado.getInt("professor_id"));
                Professor professor = usuario instanceof Professor ? (Professor) usuario : null;
                Materia materia = new Materia(resultado.getString("nome"), professor);
                materia.setId(resultado.getInt("id"));

                Periodo periodo = periodos.get(resultado.getInt("periodo_id"));
                if (periodo != null) {
                    periodo.adicionarMateria(materia);
                    mapa.put(materia.getId(), materia);
                }
            }

            resultado.close();
            comando.close();
            conexao.close();
        } catch (Exception erro) {
            throw new RuntimeException("Erro ao carregar materias: " + erro.getMessage());
        }

        return mapa;
    }

    private HashMap<Integer, Aula> carregarAulas(HashMap<Integer, Materia> materias) {
        HashMap<Integer, Aula> mapa = new HashMap<>();

        try {
            Connection conexao = Conexao.getConnection();
            PreparedStatement comando = conexao.prepareStatement("SELECT * FROM aulas");
            ResultSet resultado = comando.executeQuery();

            while (resultado.next()) {
                Aula aula = new Aula(
                        resultado.getString("titulo"),
                        resultado.getString("data_aula"),
                        resultado.getString("objetivo")
                );
                aula.setId(resultado.getInt("id"));

                Materia materia = materias.get(resultado.getInt("materia_id"));
                if (materia != null) {
                    materia.adicionarAula(aula);
                    mapa.put(aula.getId(), aula);
                }
            }

            resultado.close();
            comando.close();
            conexao.close();
        } catch (Exception erro) {
            throw new RuntimeException("Erro ao carregar aulas: " + erro.getMessage());
        }

        return mapa;
    }

    private HashMap<Integer, GrupoEstudo> carregarGrupos(HashMap<Integer, Materia> materias, HashMap<Integer, Aula> aulas, HashMap<Integer, Usuario> usuarios) {
        HashMap<Integer, GrupoEstudo> mapa = new HashMap<>();

        try {
            Connection conexao = Conexao.getConnection();
            PreparedStatement comando = conexao.prepareStatement("SELECT * FROM grupos_estudo");
            ResultSet resultado = comando.executeQuery();

            while (resultado.next()) {
                Materia materia = materias.get(resultado.getInt("materia_id"));
                if (materia == null) {
                    continue;
                }

                Aula aula = aulas.get(resultado.getInt("aula_id"));
                Usuario usuario = usuarios.get(resultado.getInt("monitor_id"));
                Monitor monitor = usuario instanceof Monitor ? (Monitor) usuario : null;

                GrupoEstudo grupo = new GrupoEstudo(resultado.getString("nome"), aula, monitor);
                grupo.setId(resultado.getInt("id"));
                materia.adicionarGrupo(grupo);
                mapa.put(grupo.getId(), grupo);
            }

            resultado.close();
            comando.close();
            conexao.close();
        } catch (Exception erro) {
            throw new RuntimeException("Erro ao carregar grupos de estudo: " + erro.getMessage());
        }

        return mapa;
    }

    private void carregarParticipantes(HashMap<Integer, Materia> materias, HashMap<Integer, Usuario> usuarios) {
        try {
            Connection conexao = Conexao.getConnection();
            PreparedStatement comando = conexao.prepareStatement("SELECT * FROM participantes");
            ResultSet resultado = comando.executeQuery();

            while (resultado.next()) {
                Materia materia = materias.get(resultado.getInt("materia_id"));
                Usuario usuario = usuarios.get(resultado.getInt("usuario_id"));
                if (materia != null && usuario != null) {
                    materia.adicionarParticipante(usuario);
                }
            }

            resultado.close();
            comando.close();
            conexao.close();
        } catch (Exception erro) {
            throw new RuntimeException("Erro ao carregar participantes: " + erro.getMessage());
        }
    }

    private void carregarFeedbacks(HashMap<Integer, Aula> aulas, HashMap<Integer, Usuario> usuarios) {
        try {
            Connection conexao = Conexao.getConnection();
            PreparedStatement comando = conexao.prepareStatement("SELECT * FROM feedbacks");
            ResultSet resultado = comando.executeQuery();

            while (resultado.next()) {
                Aula aula = aulas.get(resultado.getInt("aula_id"));
                Usuario usuario = usuarios.get(resultado.getInt("estudante_id"));
                if (aula != null && usuario instanceof Estudante) {
                    Feedback feedback = new Feedback(
                            (Estudante) usuario,
                            resultado.getInt("nota"),
                            resultado.getString("comentario")
                    );
                    feedback.setId(resultado.getInt("id"));
                    aula.registrarFeedback(feedback);
                }
            }

            resultado.close();
            comando.close();
            conexao.close();
        } catch (Exception erro) {
            throw new RuntimeException("Erro ao carregar feedbacks: " + erro.getMessage());
        }
    }

    private void carregarMateriais(HashMap<Integer, Materia> materias, HashMap<Integer, Usuario> usuarios) {
        try {
            Connection conexao = Conexao.getConnection();
            PreparedStatement comando = conexao.prepareStatement("SELECT * FROM materiais");
            ResultSet resultado = comando.executeQuery();

            while (resultado.next()) {
                Materia materia = materias.get(resultado.getInt("materia_id"));
                Usuario autor = usuarios.get(resultado.getInt("autor_id"));
                if (materia != null && autor != null) {
                    Material material = new Material(
                            resultado.getString("titulo"),
                            resultado.getString("tipo"),
                            resultado.getString("referencia"),
                            autor
                    );
                    material.setId(resultado.getInt("id"));
                    materia.adicionarMaterial(material);
                }
            }

            resultado.close();
            comando.close();
            conexao.close();
        } catch (Exception erro) {
            throw new RuntimeException("Erro ao carregar materiais: " + erro.getMessage());
        }
    }

    private void carregarMensagens(HashMap<Integer, Materia> materias, HashMap<Integer, Aula> aulas, HashMap<Integer, Usuario> usuarios) {
        try {
            Connection conexao = Conexao.getConnection();
            PreparedStatement comando = conexao.prepareStatement("SELECT * FROM mensagens");
            ResultSet resultado = comando.executeQuery();

            while (resultado.next()) {
                Usuario autor = usuarios.get(resultado.getInt("autor_id"));
                if (autor == null) {
                    continue;
                }

                String origem = resultado.getString("origem");
                int origemId = resultado.getInt("origem_id");
                String texto = resultado.getString("texto");
                Mensagem mensagem = null;

                if ("GERAL".equals(origem)) {
                    Materia materia = materias.get(origemId);
                    if (materia != null) {
                        mensagem = materia.getChatGeral().enviar(autor, texto);
                    }
                } else if ("EQUIPE".equals(origem)) {
                    Materia materia = materias.get(origemId);
                    if (materia != null) {
                        mensagem = materia.getChatEquipe().enviar(autor, texto);
                    }
                } else if ("AULA".equals(origem)) {
                    Aula aula = aulas.get(origemId);
                    if (aula != null) {
                        mensagem = aula.getChat().enviar(autor, texto);
                    }
                }

                if (mensagem != null) {
                    mensagem.setId(resultado.getInt("id"));
                }
            }

            resultado.close();
            comando.close();
            conexao.close();
        } catch (Exception erro) {
            throw new RuntimeException("Erro ao carregar mensagens: " + erro.getMessage());
        }
    }

    private void carregarAlunosDosGrupos(HashMap<Integer, GrupoEstudo> grupos, HashMap<Integer, Usuario> usuarios) {
        try {
            Connection conexao = Conexao.getConnection();
            PreparedStatement comando = conexao.prepareStatement("SELECT * FROM grupos_estudo_alunos");
            ResultSet resultado = comando.executeQuery();

            while (resultado.next()) {
                GrupoEstudo grupo = grupos.get(resultado.getInt("grupo_id"));
                Usuario usuario = usuarios.get(resultado.getInt("estudante_id"));
                if (grupo != null && usuario instanceof Estudante) {
                    grupo.adicionarEstudante((Estudante) usuario);
                }
            }

            resultado.close();
            comando.close();
            conexao.close();
        } catch (Exception erro) {
            throw new RuntimeException("Erro ao carregar alunos dos grupos: " + erro.getMessage());
        }
    }

    private void carregarMonitoresDosGrupos(HashMap<Integer, GrupoEstudo> grupos, HashMap<Integer, Usuario> usuarios) {
        try {
            Connection conexao = Conexao.getConnection();
            PreparedStatement comando = conexao.prepareStatement("SELECT * FROM grupos_estudo_monitores");
            ResultSet resultado = comando.executeQuery();

            while (resultado.next()) {
                GrupoEstudo grupo = grupos.get(resultado.getInt("grupo_id"));
                Usuario usuario = usuarios.get(resultado.getInt("monitor_id"));
                if (grupo != null && usuario instanceof Monitor) {
                    grupo.adicionarMonitor((Monitor) usuario);
                }
            }

            resultado.close();
            comando.close();
            conexao.close();
        } catch (Exception erro) {
            throw new RuntimeException("Erro ao carregar monitores dos grupos: " + erro.getMessage());
        }
    }

    private void carregarPedidosMonitoria(HashMap<Integer, Materia> materias, HashMap<Integer, Aula> aulas, HashMap<Integer, Usuario> usuarios) {
        try {
            Connection conexao = Conexao.getConnection();
            PreparedStatement comando = conexao.prepareStatement("SELECT * FROM pedidos_monitoria");
            ResultSet resultado = comando.executeQuery();

            while (resultado.next()) {
                Materia materia = materias.get(resultado.getInt("materia_id"));
                Usuario usuarioEstudante = usuarios.get(resultado.getInt("estudante_id"));
                Usuario usuarioMonitor = usuarios.get(resultado.getInt("monitor_id"));
                Aula aula = aulas.get(resultado.getInt("aula_id"));

                if (materia != null && usuarioEstudante instanceof Estudante) {
                    Monitor monitor = usuarioMonitor instanceof Monitor ? (Monitor) usuarioMonitor : null;
                    PedidoMonitoria pedido = new PedidoMonitoria(
                            (Estudante) usuarioEstudante,
                            aula,
                            monitor,
                            resultado.getString("duvida"),
                            resultado.getString("data_monitoria"),
                            resultado.getString("horario")
                    );
                    pedido.setId(resultado.getInt("id"));

                    String status = resultado.getString("status");
                    if ("Aceito".equals(status)) {
                        pedido.aceitar(monitor);
                    } else if ("Recusado".equals(status)) {
                        pedido.recusar();
                    }

                    materia.adicionarPedidoMonitoria(pedido);
                }
            }

            resultado.close();
            comando.close();
            conexao.close();
        } catch (Exception erro) {
            throw new RuntimeException("Erro ao carregar pedidos de monitoria: " + erro.getMessage());
        }
    }

    private void salvarCursos(BancoDados banco) {
        try {
            Connection conexao = Conexao.getConnection();
            PreparedStatement comando = conexao.prepareStatement(
                    "INSERT INTO cursos (id, nome, sigla, area, carga_horaria, coordenador) VALUES (?, ?, ?, ?, ?, ?)"
            );

            for (Curso curso : banco.getCursos()) {
                comando.setInt(1, curso.getId());
                comando.setString(2, curso.getNome());
                comando.setString(3, curso.getSigla());
                comando.setString(4, curso.getArea());
                comando.setInt(5, curso.getCargaHoraria());
                comando.setString(6, curso.getCoordenador());
                comando.executeUpdate();
            }

            comando.close();
            conexao.close();
        } catch (Exception erro) {
            throw new RuntimeException("Erro ao salvar cursos: " + erro.getMessage());
        }
    }

    private void salvarUsuarios(BancoDados banco) {
        try {
            Connection conexao = Conexao.getConnection();
            PreparedStatement comando = conexao.prepareStatement(
                    "INSERT INTO usuarios (id, nome, login, email, senha, cargo, tipo_monitor, matricula, curso_id) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)"
            );

            for (Usuario usuario : banco.getUsuarios()) {
                comando.setInt(1, usuario.getId());
                comando.setString(2, usuario.getNome());
                comando.setString(3, usuario.getLogin());
                comando.setString(4, usuario.getEmail());
                comando.setString(5, usuario.getSenha());
                comando.setString(6, usuario.getCargo());

                if (usuario instanceof Monitor) {
                    comando.setString(7, ((Monitor) usuario).getTipo());
                } else {
                    comando.setString(7, null);
                }

                if (usuario instanceof Estudante) {
                    Estudante estudante = (Estudante) usuario;
                    comando.setString(8, estudante.getMatricula());
                    if (estudante.getCurso() == null) {
                        comando.setObject(9, null);
                    } else {
                        comando.setInt(9, estudante.getCurso().getId());
                    }
                } else {
                    comando.setString(8, null);
                    comando.setObject(9, null);
                }

                comando.executeUpdate();
            }

            comando.close();
            conexao.close();
        } catch (Exception erro) {
            throw new RuntimeException("Erro ao salvar usuarios: " + erro.getMessage());
        }
    }

    private void salvarPeriodos(BancoDados banco) {
        try {
            Connection conexao = Conexao.getConnection();
            PreparedStatement comando = conexao.prepareStatement(
                    "INSERT INTO periodos (id, curso_id, numero, nome) VALUES (?, ?, ?, ?)"
            );

            for (Curso curso : banco.getCursos()) {
                for (Periodo periodo : curso.getPeriodos()) {
                    comando.setInt(1, periodo.getId());
                    comando.setInt(2, curso.getId());
                    comando.setInt(3, periodo.getNumero());
                    comando.setString(4, periodo.getNome());
                    comando.executeUpdate();
                }
            }

            comando.close();
            conexao.close();
        } catch (Exception erro) {
            throw new RuntimeException("Erro ao salvar periodos: " + erro.getMessage());
        }
    }

    private void salvarMaterias(BancoDados banco) {
        try {
            Connection conexao = Conexao.getConnection();
            PreparedStatement comando = conexao.prepareStatement(
                    "INSERT INTO materias (id, periodo_id, nome, professor_id) VALUES (?, ?, ?, ?)"
            );

            for (Curso curso : banco.getCursos()) {
                for (Periodo periodo : curso.getPeriodos()) {
                    for (Materia materia : periodo.getMaterias()) {
                        comando.setInt(1, materia.getId());
                        comando.setInt(2, periodo.getId());
                        comando.setString(3, materia.getNome());
                        if (materia.getProfessor() == null) {
                            comando.setObject(4, null);
                        } else {
                            comando.setInt(4, materia.getProfessor().getId());
                        }
                        comando.executeUpdate();
                    }
                }
            }

            comando.close();
            conexao.close();
        } catch (Exception erro) {
            throw new RuntimeException("Erro ao salvar materias: " + erro.getMessage());
        }
    }

    private void salvarParticipantes(BancoDados banco) {
        try {
            Connection conexao = Conexao.getConnection();
            PreparedStatement comando = conexao.prepareStatement(
                    "INSERT INTO participantes (materia_id, usuario_id) VALUES (?, ?)"
            );

            for (Curso curso : banco.getCursos()) {
                for (Periodo periodo : curso.getPeriodos()) {
                    for (Materia materia : periodo.getMaterias()) {
                        for (Estudante estudante : materia.getEstudantes()) {
                            comando.setInt(1, materia.getId());
                            comando.setInt(2, estudante.getId());
                            comando.executeUpdate();
                        }

                        for (Monitor monitor : materia.getMonitores()) {
                            comando.setInt(1, materia.getId());
                            comando.setInt(2, monitor.getId());
                            comando.executeUpdate();
                        }

                        if (materia.getProfessor() != null) {
                            comando.setInt(1, materia.getId());
                            comando.setInt(2, materia.getProfessor().getId());
                            comando.executeUpdate();
                        }
                    }
                }
            }

            comando.close();
            conexao.close();
        } catch (Exception erro) {
            throw new RuntimeException("Erro ao salvar participantes: " + erro.getMessage());
        }
    }

    private void salvarAulas(BancoDados banco) {
        try {
            Connection conexao = Conexao.getConnection();
            PreparedStatement comando = conexao.prepareStatement(
                    "INSERT INTO aulas (id, materia_id, titulo, data_aula, objetivo) VALUES (?, ?, ?, ?, ?)"
            );

            for (Curso curso : banco.getCursos()) {
                for (Periodo periodo : curso.getPeriodos()) {
                    for (Materia materia : periodo.getMaterias()) {
                        for (Aula aula : materia.getAulas()) {
                            comando.setInt(1, aula.getId());
                            comando.setInt(2, materia.getId());
                            comando.setString(3, aula.getTitulo());
                            comando.setString(4, aula.getData());
                            comando.setString(5, aula.getObjetivo());
                            comando.executeUpdate();
                        }
                    }
                }
            }

            comando.close();
            conexao.close();
        } catch (Exception erro) {
            throw new RuntimeException("Erro ao salvar aulas: " + erro.getMessage());
        }
    }

    private void salvarFeedbacks(BancoDados banco) {
        try {
            Connection conexao = Conexao.getConnection();
            PreparedStatement comando = conexao.prepareStatement(
                    "INSERT INTO feedbacks (aula_id, estudante_id, nota, comentario) VALUES (?, ?, ?, ?)"
            );

            for (Curso curso : banco.getCursos()) {
                for (Periodo periodo : curso.getPeriodos()) {
                    for (Materia materia : periodo.getMaterias()) {
                        for (Aula aula : materia.getAulas()) {
                            for (Feedback feedback : aula.getFeedbacks()) {
                                comando.setInt(1, aula.getId());
                                comando.setInt(2, feedback.getEstudante().getId());
                                comando.setInt(3, feedback.getNota());
                                comando.setString(4, feedback.getComentario());
                                comando.executeUpdate();
                            }
                        }
                    }
                }
            }

            comando.close();
            conexao.close();
        } catch (Exception erro) {
            throw new RuntimeException("Erro ao salvar feedbacks: " + erro.getMessage());
        }
    }

    private void salvarMateriais(BancoDados banco) {
        try {
            Connection conexao = Conexao.getConnection();
            PreparedStatement comando = conexao.prepareStatement(
                    "INSERT INTO materiais (id, materia_id, titulo, tipo, referencia, autor_id) VALUES (?, ?, ?, ?, ?, ?)"
            );

            for (Curso curso : banco.getCursos()) {
                for (Periodo periodo : curso.getPeriodos()) {
                    for (Materia materia : periodo.getMaterias()) {
                        for (Material material : materia.getMateriais()) {
                            comando.setInt(1, material.getId());
                            comando.setInt(2, materia.getId());
                            comando.setString(3, material.getTitulo());
                            comando.setString(4, material.getTipo());
                            comando.setString(5, material.getReferencia());
                            comando.setInt(6, material.getAutor().getId());
                            comando.executeUpdate();
                        }
                    }
                }
            }

            comando.close();
            conexao.close();
        } catch (Exception erro) {
            throw new RuntimeException("Erro ao salvar materiais: " + erro.getMessage());
        }
    }

    private void salvarMensagens(BancoDados banco) {
        try {
            Connection conexao = Conexao.getConnection();
            PreparedStatement comando = conexao.prepareStatement(
                    "INSERT INTO mensagens (id, origem, origem_id, autor_id, texto) VALUES (?, ?, ?, ?, ?)"
            );

            for (Curso curso : banco.getCursos()) {
                for (Periodo periodo : curso.getPeriodos()) {
                    for (Materia materia : periodo.getMaterias()) {
                        salvarMensagensDoChat(comando, materia.getChatGeral(), "GERAL", materia.getId());
                        salvarMensagensDoChat(comando, materia.getChatEquipe(), "EQUIPE", materia.getId());

                        for (Aula aula : materia.getAulas()) {
                            salvarMensagensDoChat(comando, aula.getChat(), "AULA", aula.getId());
                        }
                    }
                }
            }

            comando.close();
            conexao.close();
        } catch (Exception erro) {
            throw new RuntimeException("Erro ao salvar mensagens: " + erro.getMessage());
        }
    }

    private void salvarGrupos(BancoDados banco) {
        try {
            Connection conexao = Conexao.getConnection();
            PreparedStatement comando = conexao.prepareStatement(
                    "INSERT INTO grupos_estudo (id, materia_id, nome, aula_id, monitor_id) VALUES (?, ?, ?, ?, ?)"
            );

            for (Curso curso : banco.getCursos()) {
                for (Periodo periodo : curso.getPeriodos()) {
                    for (Materia materia : periodo.getMaterias()) {
                        for (GrupoEstudo grupo : materia.getGrupos()) {
                            comando.setInt(1, grupo.getId());
                            comando.setInt(2, materia.getId());
                            comando.setString(3, grupo.getNome());

                            if (grupo.getAula() == null) {
                                comando.setObject(4, null);
                            } else {
                                comando.setInt(4, grupo.getAula().getId());
                            }

                            if (grupo.getMonitores().isEmpty()) {
                                comando.setObject(5, null);
                            } else {
                                comando.setInt(5, grupo.getMonitores().get(0).getId());
                            }

                            comando.executeUpdate();
                        }
                    }
                }
            }

            comando.close();
            conexao.close();
        } catch (Exception erro) {
            throw new RuntimeException("Erro ao salvar grupos de estudo: " + erro.getMessage());
        }
    }

    private void salvarAlunosDosGrupos(BancoDados banco) {
        try {
            Connection conexao = Conexao.getConnection();
            PreparedStatement comando = conexao.prepareStatement(
                    "INSERT INTO grupos_estudo_alunos (grupo_id, estudante_id) VALUES (?, ?)"
            );

            for (Curso curso : banco.getCursos()) {
                for (Periodo periodo : curso.getPeriodos()) {
                    for (Materia materia : periodo.getMaterias()) {
                        for (GrupoEstudo grupo : materia.getGrupos()) {
                            for (Estudante estudante : grupo.getEstudantes()) {
                                comando.setInt(1, grupo.getId());
                                comando.setInt(2, estudante.getId());
                                comando.executeUpdate();
                            }
                        }
                    }
                }
            }

            comando.close();
            conexao.close();
        } catch (Exception erro) {
            throw new RuntimeException("Erro ao salvar alunos dos grupos: " + erro.getMessage());
        }
    }

    private void salvarMonitoresDosGrupos(BancoDados banco) {
        try {
            Connection conexao = Conexao.getConnection();
            PreparedStatement comando = conexao.prepareStatement(
                    "INSERT INTO grupos_estudo_monitores (grupo_id, monitor_id) VALUES (?, ?)"
            );

            for (Curso curso : banco.getCursos()) {
                for (Periodo periodo : curso.getPeriodos()) {
                    for (Materia materia : periodo.getMaterias()) {
                        for (GrupoEstudo grupo : materia.getGrupos()) {
                            for (Monitor monitor : grupo.getMonitores()) {
                                comando.setInt(1, grupo.getId());
                                comando.setInt(2, monitor.getId());
                                comando.executeUpdate();
                            }
                        }
                    }
                }
            }

            comando.close();
            conexao.close();
        } catch (Exception erro) {
            throw new RuntimeException("Erro ao salvar monitores dos grupos: " + erro.getMessage());
        }
    }

    private void salvarPedidosMonitoria(BancoDados banco) {
        try {
            Connection conexao = Conexao.getConnection();
            PreparedStatement comando = conexao.prepareStatement(
                    "INSERT INTO pedidos_monitoria (id, materia_id, estudante_id, aula_id, monitor_id, duvida, status, data_monitoria, horario) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)"
            );

            for (Curso curso : banco.getCursos()) {
                for (Periodo periodo : curso.getPeriodos()) {
                    for (Materia materia : periodo.getMaterias()) {
                        for (PedidoMonitoria pedido : materia.getPedidosMonitoria()) {
                            comando.setInt(1, pedido.getId());
                            comando.setInt(2, materia.getId());
                            comando.setInt(3, pedido.getEstudante().getId());

                            if (pedido.getAula() == null) {
                                comando.setObject(4, null);
                            } else {
                                comando.setInt(4, pedido.getAula().getId());
                            }

                            if (pedido.getMonitor() == null) {
                                comando.setObject(5, null);
                            } else {
                                comando.setInt(5, pedido.getMonitor().getId());
                            }

                            comando.setString(6, pedido.getDuvida());
                            comando.setString(7, pedido.getStatus());
                            comando.setString(8, pedido.getData());
                            comando.setString(9, pedido.getHorario());
                            comando.executeUpdate();
                        }
                    }
                }
            }

            comando.close();
            conexao.close();
        } catch (Exception erro) {
            throw new RuntimeException("Erro ao salvar pedidos de monitoria: " + erro.getMessage());
        }
    }

    private void salvarMensagensDoChat(PreparedStatement comando, Chat chat, String origem, int origemId) throws Exception {
        for (Mensagem mensagem : chat.getMensagens()) {
            comando.setInt(1, mensagem.getId());
            comando.setString(2, origem);
            comando.setInt(3, origemId);
            comando.setInt(4, mensagem.getAutor().getId());
            comando.setString(5, mensagem.getTexto());
            comando.executeUpdate();
        }
    }

    private void ajustarProximoId(BancoDados banco) {
        int maiorId = 0;

        for (Usuario usuario : banco.getUsuarios()) {
            if (usuario.getId() > maiorId) {
                maiorId = usuario.getId();
            }
        }

        for (Curso curso : banco.getCursos()) {
            if (curso.getId() > maiorId) {
                maiorId = curso.getId();
            }

            for (Periodo periodo : curso.getPeriodos()) {
                if (periodo.getId() > maiorId) {
                    maiorId = periodo.getId();
                }

                for (Materia materia : periodo.getMaterias()) {
                    if (materia.getId() > maiorId) {
                        maiorId = materia.getId();
                    }

                    for (Aula aula : materia.getAulas()) {
                        if (aula.getId() > maiorId) {
                            maiorId = aula.getId();
                        }
                    }

                    for (Material material : materia.getMateriais()) {
                        if (material.getId() > maiorId) {
                            maiorId = material.getId();
                        }
                    }

                    for (GrupoEstudo grupo : materia.getGrupos()) {
                        if (grupo.getId() > maiorId) {
                            maiorId = grupo.getId();
                        }
                    }

                    for (PedidoMonitoria pedido : materia.getPedidosMonitoria()) {
                        if (pedido.getId() > maiorId) {
                            maiorId = pedido.getId();
                        }
                    }
                }
            }
        }

        banco.ajustarProximoId(maiorId);
    }
}
