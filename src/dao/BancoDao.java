package dao;

import excecao.PersistenciaException;
import excecao.RegraDeNegocioException;
import model.Aula;
import model.Chat;
import model.Curso;
import model.Estudante;
import model.Feedback;
import model.GrupoEstudo;
import model.HorarioMonitor;
import model.Materia;
import model.Material;
import model.Monitor;
import model.PedidoMonitoria;
import model.Periodo;
import model.Professor;
import model.Usuario;
import util.Conexao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Cria as tabelas que faltam, carrega todos os dados do MindDebug ao abrir o sistema
 * e grava os dados iniciais na primeira vez. Depois disso, cada operacao e gravada
 * pelo DAO da tabela, chamado pelo controller.
 */
public class BancoDao extends AcessoBanco {

    private HashMap<Integer, Estudante> estudantesPorId = new HashMap<>();
    private HashMap<Integer, Monitor> monitoresPorId = new HashMap<>();
    private HashMap<Integer, Professor> professoresPorId = new HashMap<>();
    private UsuarioDao usuarioDao = new UsuarioDao();
    private CursoDao cursoDao = new CursoDao();
    private PeriodoDao periodoDao = new PeriodoDao();
    private MateriaDao materiaDao = new MateriaDao();
    private ParticipanteDao participanteDao = new ParticipanteDao();
    private AulaDao aulaDao = new AulaDao();
    private HorarioMonitorDao horarioMonitorDao = new HorarioMonitorDao();
    private FeedbackDao feedbackDao = new FeedbackDao();
    private MaterialDao materialDao = new MaterialDao();
    private ChatDao chatDao = new ChatDao();
    private GrupoEstudoDao grupoEstudoDao = new GrupoEstudoDao();
    private PedidoMonitoriaDao pedidoMonitoriaDao = new PedidoMonitoriaDao();
    private QuizDao quizDao = new QuizDao();

    public static String mensagemDeEsquemaAntigo() {
        return "O banco " + Conexao.descricao() + " ainda tem as tabelas da versao anterior do MindDebug. "
                + "Apague essas tabelas e abra o sistema de novo para criar as tabelas atuais "
                + "(veja docs/AMBIENTE_LOCAL.md).";
    }

    // ------------------------------------------------------------------ carregar

    public BancoDados carregar() {
        Connection conexao = abrirConexao();
        try {
            criarTabelas(conexao);

            BancoDados banco = new BancoDados();
            HashMap<Integer, Curso> cursos = carregarCursos(conexao, banco);
            HashMap<Integer, Usuario> usuarios = carregarUsuarios(conexao, banco, cursos);
            separarUsuariosPorPerfil(banco);
            HashMap<Integer, Periodo> periodos = carregarPeriodos(conexao, cursos);
            HashMap<Integer, Materia> materias = carregarMaterias(conexao, periodos);
            HashMap<Integer, Aula> aulas = aulaDao.listar(conexao, materias);

            carregarParticipantes(conexao, materias, usuarios);
            horarioMonitorDao.listar(conexao, materias, monitoresPorId);
            feedbackDao.listar(conexao, aulas, estudantesPorId);
            materialDao.listar(conexao, materias, usuarios);
            HashMap<Integer, Chat> chats = chatDao.listarChats(conexao, materias, aulas);
            chatDao.listarMensagens(conexao, chats, usuarios);
            grupoEstudoDao.listar(conexao, materias, aulas, estudantesPorId, monitoresPorId);
            pedidoMonitoriaDao.listar(conexao, materias, aulas, estudantesPorId, monitoresPorId);
            quizDao.listar(conexao, aulas, estudantesPorId);

            return banco;
        } catch (SQLException erro) {
            throw new PersistenciaException("Erro ao carregar os dados: " + erro.getMessage());
        } finally {
            fechar(conexao);
        }
    }

    private HashMap<Integer, Curso> carregarCursos(Connection conexao, BancoDados banco) throws SQLException {
        HashMap<Integer, Curso> mapa = new HashMap<>();
        for (Curso curso : cursoDao.listar(conexao)) {
            banco.getCursos().add(curso);
            mapa.put(curso.getId(), curso);
        }
        return mapa;
    }

    private HashMap<Integer, Usuario> carregarUsuarios(Connection conexao, BancoDados banco,
            HashMap<Integer, Curso> cursos) throws SQLException {
        HashMap<Integer, Usuario> mapa = new HashMap<>();
        for (Usuario usuario : usuarioDao.listar(conexao, cursos)) {
            banco.getUsuarios().add(usuario);
            mapa.put(usuario.getId(), usuario);
        }
        return mapa;
    }

    private void separarUsuariosPorPerfil(BancoDados banco) {
        ArrayList<Estudante> estudantes = new ArrayList<>();
        ArrayList<Monitor> monitores = new ArrayList<>();
        ArrayList<Professor> professores = new ArrayList<>();

        for (Usuario usuario : banco.getUsuarios()) {
            usuario.incluirEmEstudantes(estudantes);
            usuario.incluirEmMonitores(monitores);
            usuario.incluirEmProfessores(professores);
        }

        estudantesPorId.clear();
        monitoresPorId.clear();
        professoresPorId.clear();

        for (Estudante estudante : estudantes) {
            estudantesPorId.put(estudante.getId(), estudante);
        }
        for (Monitor monitor : monitores) {
            monitoresPorId.put(monitor.getId(), monitor);
        }
        for (Professor professor : professores) {
            professoresPorId.put(professor.getId(), professor);
        }
    }

    private HashMap<Integer, Periodo> carregarPeriodos(Connection conexao, HashMap<Integer, Curso> cursos)
            throws SQLException {
        return periodoDao.listar(conexao, cursos);
    }

    private HashMap<Integer, Materia> carregarMaterias(Connection conexao, HashMap<Integer, Periodo> periodos)
            throws SQLException {
        return materiaDao.listar(conexao, periodos, professoresPorId);
    }

    private void carregarParticipantes(Connection conexao, HashMap<Integer, Materia> materias,
            HashMap<Integer, Usuario> usuarios) throws SQLException {
        participanteDao.listar(conexao, materias, usuarios);
    }

    // ------------------------------------------------------------------ dados iniciais

    /**
     * Grava, numa transacao, todos os dados de um BancoDados montado em memoria, com os ids que ja tem.
     * So serve para os dados iniciais: com usuarios no banco, nada e gravado. Depois disso,
     * cada operacao grava pelo seu DAO.
     */
    public void gravarDadosIniciais(BancoDados banco) {
        Connection conexao = abrirConexao();
        boolean gravou = false;
        try {
            criarTabelas(conexao);
            if (contarLinhas(conexao, "usuarios") > 0) {
                throw new RegraDeNegocioException("O banco ja tem dados; os dados iniciais nao foram gravados.");
            }
            conexao.setAutoCommit(false);

            separarUsuariosPorPerfil(banco);
            ArrayList<Materia> materias = listarMaterias(banco);
            ArrayList<Aula> aulas = listarAulas(materias);

            salvarCursos(conexao, banco);
            salvarUsuarios(conexao, banco);
            salvarPeriodos(conexao, banco);
            salvarMaterias(conexao, banco);
            salvarParticipantes(conexao, materias);
            salvarAulas(conexao, materias);
            salvarHorariosMonitor(conexao, materias);
            salvarFeedbacks(conexao, aulas);
            salvarMateriais(conexao, materias);
            salvarChats(conexao, materias);
            salvarGrupos(conexao, materias);
            salvarPedidosMonitoria(conexao, materias);
            salvarQuizzes(conexao, aulas);

            conexao.commit();
            gravou = true;
        } catch (SQLException erro) {
            throw new PersistenciaException("Erro ao gravar os dados iniciais. Nada foi gravado. Detalhe: "
                    + erro.getMessage());
        } finally {
            if (!gravou) {
                desfazer(conexao);
            }
            fechar(conexao);
        }
    }

    private void salvarCursos(Connection conexao, BancoDados banco) throws SQLException {
        for (Curso curso : banco.getCursos()) {
            cursoDao.inserirComId(conexao, curso);
        }
    }

    private void salvarUsuarios(Connection conexao, BancoDados banco) throws SQLException {
        for (Usuario usuario : banco.getUsuarios()) {
            usuarioDao.inserirComId(conexao, usuario);
        }
    }

    private void salvarPeriodos(Connection conexao, BancoDados banco) throws SQLException {
        for (Curso curso : banco.getCursos()) {
            for (Periodo periodo : curso.getPeriodos()) {
                periodoDao.inserirComId(conexao, curso, periodo);
            }
        }
    }

    private void salvarMaterias(Connection conexao, BancoDados banco) throws SQLException {
        for (Curso curso : banco.getCursos()) {
            for (Periodo periodo : curso.getPeriodos()) {
                for (Materia materia : periodo.getMaterias()) {
                    materiaDao.inserirComId(conexao, periodo, materia);
                }
            }
        }
    }

    // O professor da materia ja fica em materias.professor_id; aqui entram alunos e monitores.
    /** So alunos e monitores; o professor fica em materias.professor_id. */
    private void salvarParticipantes(Connection conexao, ArrayList<Materia> materias) throws SQLException {
        for (Materia materia : materias) {
            for (Estudante estudante : materia.getEstudantes()) {
                participanteDao.inserir(conexao, materia, estudante);
            }
            for (Monitor monitor : materia.getMonitores()) {
                participanteDao.inserir(conexao, materia, monitor);
            }
        }
    }

    private void salvarAulas(Connection conexao, ArrayList<Materia> materias) throws SQLException {
        for (Materia materia : materias) {
            for (Aula aula : materia.getAulas()) {
                aulaDao.inserirComId(conexao, materia, aula);
            }
        }
    }

    // Horario de uma materia que ja foi removida nao e gravado.
    private void salvarHorariosMonitor(Connection conexao, ArrayList<Materia> materias) throws SQLException {
        for (Monitor monitor : monitoresPorId.values()) {
            for (HorarioMonitor horario : monitor.getHorarios()) {
                if (materias.contains(horario.getMateria())) {
                    horarioMonitorDao.inserir(conexao, monitor, horario);
                }
            }
        }
    }

    private void salvarFeedbacks(Connection conexao, ArrayList<Aula> aulas) throws SQLException {
        for (Aula aula : aulas) {
            for (Feedback feedback : aula.getFeedbacks()) {
                feedbackDao.inserir(conexao, aula, feedback);
            }
        }
    }

    private void salvarMateriais(Connection conexao, ArrayList<Materia> materias) throws SQLException {
        for (Materia materia : materias) {
            for (Material material : materia.getMateriais()) {
                materialDao.inserirComId(conexao, materia, material);
            }
        }
    }

    // Cada materia tem um chat geral e um de equipe; cada aula tem o seu chat.
    private void salvarChats(Connection conexao, ArrayList<Materia> materias) throws SQLException {
        chatDao.inserirTodos(conexao, materias);
    }

    private void salvarGrupos(Connection conexao, ArrayList<Materia> materias) throws SQLException {
        for (Materia materia : materias) {
            for (GrupoEstudo grupo : materia.getGrupos()) {
                grupoEstudoDao.inserirComId(conexao, materia, grupo);
            }
        }
    }

    private void salvarPedidosMonitoria(Connection conexao, ArrayList<Materia> materias) throws SQLException {
        for (Materia materia : materias) {
            for (PedidoMonitoria pedido : materia.getPedidosMonitoria()) {
                pedidoMonitoriaDao.inserirComId(conexao, materia, pedido);
            }
        }
    }

    private void salvarQuizzes(Connection conexao, ArrayList<Aula> aulas) throws SQLException {
        for (Aula aula : aulas) {
            if (aula.getQuiz() != null) {
                quizDao.inserirComId(conexao, aula, aula.getQuiz());
            }
        }
    }

    // ------------------------------------------------------------------ esquema

    private void criarTabelas(Connection conexao) throws SQLException {
        if (existeTabela(conexao, "usuarios") && !existeTabela(conexao, "perfis")) {
            throw new PersistenciaException(mensagemDeEsquemaAntigo());
        }

        String[] nomes = EsquemaBanco.nomesDasTabelas();
        String[] comandos = EsquemaBanco.comandosDeCriacao();
        Statement comando = conexao.createStatement();
        for (int i = 0; i < nomes.length; i++) {
            if (!existeTabela(conexao, nomes[i])) {
                comando.executeUpdate(comandos[i]);
            }
        }
        comando.close();

        if (contarLinhas(conexao, "perfis") == 0) {
            inserirDadosFixos(conexao);
        }
    }

    private boolean existeTabela(Connection conexao, String tabela) throws SQLException {
        PreparedStatement consulta = conexao.prepareStatement(
                "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = ? AND table_name = ?");
        consulta.setString(1, Conexao.BANCO);
        consulta.setString(2, tabela);
        ResultSet resultado = consulta.executeQuery();
        resultado.next();
        boolean existe = resultado.getInt(1) > 0;
        resultado.close();
        consulta.close();
        return existe;
    }

    private int contarLinhas(Connection conexao, String tabela) throws SQLException {
        Statement consulta = conexao.createStatement();
        ResultSet resultado = consulta.executeQuery("SELECT COUNT(*) FROM " + tabela);
        resultado.next();
        int quantidade = resultado.getInt(1);
        resultado.close();
        consulta.close();
        return quantidade;
    }

    private void inserirDadosFixos(Connection conexao) throws SQLException {
        conexao.setAutoCommit(false);
        Statement comando = conexao.createStatement();
        try {
            for (String insercao : EsquemaBanco.comandosDeDadosFixos()) {
                comando.executeUpdate(insercao);
            }
            conexao.commit();
        } catch (SQLException erro) {
            conexao.rollback();
            throw erro;
        } finally {
            comando.close();
            conexao.setAutoCommit(true);
        }
    }

    // ------------------------------------------------------------------ apoio

    private ArrayList<Materia> listarMaterias(BancoDados banco) {
        ArrayList<Materia> materias = new ArrayList<>();
        for (Curso curso : banco.getCursos()) {
            for (Periodo periodo : curso.getPeriodos()) {
                materias.addAll(periodo.getMaterias());
            }
        }
        return materias;
    }

    private ArrayList<Aula> listarAulas(ArrayList<Materia> materias) {
        ArrayList<Aula> aulas = new ArrayList<>();
        for (Materia materia : materias) {
            aulas.addAll(materia.getAulas());
        }
        return aulas;
    }
}
