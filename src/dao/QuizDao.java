package dao;

import excecao.PersistenciaException;
import excecao.RegraDeNegocioException;
import model.AlternativaQuiz;
import model.Aula;
import model.Estudante;
import model.PerguntaQuiz;
import model.Quiz;
import model.RespostaQuiz;
import model.TentativaQuiz;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Acesso as cinco tabelas do quiz: quizzes, perguntas_quiz, alternativas_quiz,
 * tentativas_quiz e respostas_quiz. Cada aula tem no maximo um quiz.
 * A pergunta e gravada junto com as quatro alternativas, e a tentativa junto com as respostas,
 * sempre numa transacao.
 */
public class QuizDao extends AcessoBanco {
    // Filtro opcional das consultas: so o quiz de uma aula. Sem ele, as consultas leem todos os quizzes.
    private static final String DA_AULA = " WHERE q.aula_id = ?";

    // ------------------------------------------------------------------ consultas

    /** Quiz da aula com perguntas, alternativas, tentativas e respostas; nulo se a aula nao tiver quiz. */
    public Quiz buscarDaAula(Aula aula) {
        HashMap<Integer, Aula> aulas = new HashMap<>();
        aulas.put(aula.getId(), aula);
        HashMap<Integer, Estudante> estudantes = new UsuarioDao().estudantesPorId();

        Connection conexao = abrirConexao();
        try {
            HashMap<Integer, Quiz> quizzes = ler(conexao, DA_AULA, aula.getId(), aulas, estudantes);
            if (quizzes.isEmpty()) {
                return null;
            }
            return quizzes.values().iterator().next();
        } catch (SQLException erro) {
            throw new PersistenciaException("Erro ao consultar o quiz: " + erro.getMessage());
        } finally {
            fechar(conexao);
        }
    }

    /** Usado pelo BancoDao: coloca cada quiz, completo, dentro da sua aula. */
    void listar(Connection conexao, HashMap<Integer, Aula> aulas, HashMap<Integer, Estudante> estudantes)
            throws SQLException {
        ler(conexao, "", 0, aulas, estudantes);
    }

    private HashMap<Integer, Quiz> ler(Connection conexao, String filtro, int aulaId, HashMap<Integer, Aula> aulas,
            HashMap<Integer, Estudante> estudantes) throws SQLException {
        HashMap<Integer, Quiz> quizzes = lerQuizzes(conexao, filtro, aulaId, aulas);
        HashMap<Integer, PerguntaQuiz> perguntas = new HashMap<>();
        HashMap<Integer, AlternativaQuiz> alternativas = new HashMap<>();
        lerPerguntas(conexao, filtro, aulaId, quizzes, perguntas, alternativas);
        lerTentativas(conexao, filtro, aulaId, quizzes, perguntas, alternativas, estudantes);
        return quizzes;
    }

    private PreparedStatement preparar(Connection conexao, String sql, String filtro, int aulaId) throws SQLException {
        PreparedStatement consulta = conexao.prepareStatement(sql + filtro + " ORDER BY id");
        if (!filtro.isEmpty()) {
            consulta.setInt(1, aulaId);
        }
        return consulta;
    }

    private HashMap<Integer, Quiz> lerQuizzes(Connection conexao, String filtro, int aulaId,
            HashMap<Integer, Aula> aulas) throws SQLException {
        HashMap<Integer, Quiz> quizzes = new HashMap<>();
        PreparedStatement consulta = preparar(conexao,
                "SELECT q.id AS id, q.aula_id, q.titulo, q.descricao FROM quizzes q", filtro, aulaId);
        ResultSet resultado = consulta.executeQuery();
        while (resultado.next()) {
            Aula aula = aulas.get(resultado.getInt("aula_id"));
            if (aula != null) {
                Quiz quiz = new Quiz(resultado.getString("titulo"), resultado.getString("descricao"));
                quiz.setId(resultado.getInt("id"));
                quiz.setAula(aula);
                aula.setQuiz(quiz);
                quizzes.put(quiz.getId(), quiz);
            }
        }
        resultado.close();
        consulta.close();
        return quizzes;
    }

    /**
     * Le perguntas e alternativas. A pergunta so entra no quiz depois de receber as quatro alternativas.
     * Uma pergunta incompleta (so possivel se alguem mexer no banco por fora do sistema) fica de fora,
     * para nao impedir o sistema de abrir.
     */
    private void lerPerguntas(Connection conexao, String filtro, int aulaId, HashMap<Integer, Quiz> quizzes,
            HashMap<Integer, PerguntaQuiz> perguntas, HashMap<Integer, AlternativaQuiz> alternativas)
            throws SQLException {
        ArrayList<PerguntaQuiz> perguntasEmOrdem = new ArrayList<>();
        HashMap<Integer, Quiz> quizDaPergunta = new HashMap<>();
        PreparedStatement consulta = preparar(conexao,
                "SELECT p.id AS id, p.quiz_id, p.enunciado, p.dificuldade "
                        + "FROM perguntas_quiz p JOIN quizzes q ON q.id = p.quiz_id", filtro, aulaId);
        ResultSet resultado = consulta.executeQuery();
        while (resultado.next()) {
            Quiz quiz = quizzes.get(resultado.getInt("quiz_id"));
            if (quiz != null) {
                PerguntaQuiz pergunta = new PerguntaQuiz(resultado.getString("enunciado"), resultado.getString("dificuldade"));
                pergunta.setId(resultado.getInt("id"));
                perguntasEmOrdem.add(pergunta);
                perguntas.put(pergunta.getId(), pergunta);
                quizDaPergunta.put(pergunta.getId(), quiz);
            }
        }
        resultado.close();
        consulta.close();

        consulta = preparar(conexao,
                "SELECT a.id AS id, a.pergunta_id, a.texto, a.explicacao, a.correta FROM alternativas_quiz a "
                        + "JOIN perguntas_quiz p ON p.id = a.pergunta_id JOIN quizzes q ON q.id = p.quiz_id",
                filtro, aulaId);
        resultado = consulta.executeQuery();
        while (resultado.next()) {
            PerguntaQuiz pergunta = perguntas.get(resultado.getInt("pergunta_id"));
            if (pergunta != null) {
                AlternativaQuiz alternativa = new AlternativaQuiz(
                        resultado.getString("texto"),
                        resultado.getString("explicacao"),
                        resultado.getBoolean("correta")
                );
                alternativa.setId(resultado.getInt("id"));
                pergunta.adicionarAlternativa(alternativa);
                alternativas.put(alternativa.getId(), alternativa);
            }
        }
        resultado.close();
        consulta.close();

        for (PerguntaQuiz pergunta : perguntasEmOrdem) {
            if (pergunta.temQuatroAlternativas() && pergunta.getAlternativaCorreta() != null) {
                quizDaPergunta.get(pergunta.getId()).adicionarPergunta(pergunta);
            }
        }
    }

    private void lerTentativas(Connection conexao, String filtro, int aulaId, HashMap<Integer, Quiz> quizzes,
            HashMap<Integer, PerguntaQuiz> perguntas, HashMap<Integer, AlternativaQuiz> alternativas,
            HashMap<Integer, Estudante> estudantes) throws SQLException {
        HashMap<Integer, TentativaQuiz> tentativas = new HashMap<>();
        PreparedStatement consulta = preparar(conexao,
                "SELECT t.id AS id, t.quiz_id, t.estudante_id, t.data_tentativa, t.hora_tentativa, t.acertos, "
                        + "t.total_perguntas FROM tentativas_quiz t JOIN quizzes q ON q.id = t.quiz_id",
                filtro, aulaId);
        ResultSet resultado = consulta.executeQuery();
        while (resultado.next()) {
            Quiz quiz = quizzes.get(resultado.getInt("quiz_id"));
            Estudante estudante = estudantes.get(resultado.getInt("estudante_id"));
            if (quiz != null && estudante != null) {
                TentativaQuiz tentativa = TentativaQuiz.restaurar(
                        estudante, quiz, resultado.getInt("acertos"), resultado.getInt("total_perguntas"));
                tentativa.setId(resultado.getInt("id"));
                tentativa.setDataTentativa(textoDaData(resultado.getDate("data_tentativa")) + " "
                        + resultado.getString("hora_tentativa"));
                quiz.adicionarTentativa(tentativa);
                tentativas.put(tentativa.getId(), tentativa);
            }
        }
        resultado.close();
        consulta.close();

        consulta = preparar(conexao,
                "SELECT r.id AS id, r.tentativa_id, r.pergunta_id, r.alternativa_id, r.acertou FROM respostas_quiz r "
                        + "JOIN tentativas_quiz t ON t.id = r.tentativa_id JOIN quizzes q ON q.id = t.quiz_id",
                filtro, aulaId);
        resultado = consulta.executeQuery();
        while (resultado.next()) {
            TentativaQuiz tentativa = tentativas.get(resultado.getInt("tentativa_id"));
            PerguntaQuiz pergunta = perguntas.get(resultado.getInt("pergunta_id"));
            AlternativaQuiz alternativa = alternativas.get(resultado.getInt("alternativa_id"));
            if (tentativa != null && pergunta != null && alternativa != null
                    && tentativa.getQuiz().getPerguntas().contains(pergunta)) {
                RespostaQuiz resposta = RespostaQuiz.restaurar(pergunta, alternativa, resultado.getBoolean("acertou"));
                resposta.setId(resultado.getInt("id"));
                tentativa.adicionarResposta(resposta);
            }
        }
        resultado.close();
        consulta.close();
    }

    // ------------------------------------------------------------------ gravacao

    /** Cria o quiz da aula, ainda sem perguntas. A aula so pode ter um quiz. */
    public void inserir(Aula aula, Quiz quiz) {
        Connection conexao = abrirConexao();
        boolean gravou = false;
        try {
            conexao.setAutoCommit(false);
            if (contarPorId(conexao, "SELECT COUNT(*) FROM quizzes WHERE aula_id = ?", aula.getId()) > 0) {
                throw new RegraDeNegocioException("Esta aula ja possui quiz.");
            }
            PreparedStatement comando = conexao.prepareStatement(
                    "INSERT INTO quizzes (aula_id, titulo, descricao) VALUES (?, ?, ?)");
            comando.setInt(1, aula.getId());
            comando.setString(2, quiz.getTitulo());
            comando.setString(3, quiz.getDescricao());
            comando.executeUpdate();
            comando.close();
            int idGerado = lerUltimoId(conexao, "quizzes");

            conexao.commit();
            gravou = true;
            quiz.setId(idGerado);
        } catch (SQLException erro) {
            throw new PersistenciaException("Erro ao criar o quiz. Nada foi gravado. Detalhe: " + erro.getMessage());
        } finally {
            if (!gravou) {
                desfazer(conexao);
            }
            fechar(conexao);
        }
    }

    /**
     * Grava a pergunta e as quatro alternativas numa transacao: se uma alternativa falhar,
     * a pergunta tambem nao fica no banco. Os ids so vao para os objetos depois do commit.
     */
    public void adicionarPergunta(Quiz quiz, PerguntaQuiz pergunta) {
        if (!pergunta.temQuatroAlternativas() || pergunta.getAlternativaCorreta() == null) {
            throw new RegraDeNegocioException("Pergunta deve possuir quatro alternativas e uma correta");
        }
        Connection conexao = abrirConexao();
        boolean gravou = false;
        try {
            conexao.setAutoCommit(false);
            PreparedStatement comando = conexao.prepareStatement(
                    "INSERT INTO perguntas_quiz (quiz_id, enunciado, dificuldade) VALUES (?, ?, ?)");
            comando.setInt(1, quiz.getId());
            comando.setString(2, pergunta.getEnunciado());
            comando.setString(3, pergunta.getDificuldade());
            comando.executeUpdate();
            comando.close();
            int perguntaId = lerUltimoId(conexao, "perguntas_quiz");

            int[] alternativaIds = new int[4];
            for (int i = 0; i < 4; i++) {
                inserirAlternativa(conexao, perguntaId, pergunta.getAlternativas().get(i), false);
                alternativaIds[i] = lerUltimoId(conexao, "alternativas_quiz");
            }

            conexao.commit();
            gravou = true;
            pergunta.setId(perguntaId);
            for (int i = 0; i < 4; i++) {
                pergunta.getAlternativas().get(i).setId(alternativaIds[i]);
            }
        } catch (SQLException erro) {
            throw new PersistenciaException("Erro ao gravar a pergunta. Nada foi gravado. Detalhe: " + erro.getMessage());
        } finally {
            if (!gravou) {
                desfazer(conexao);
            }
            fechar(conexao);
        }
    }

    /** Grava a tentativa finalizada e as respostas dela numa transacao. */
    public void registrarTentativa(TentativaQuiz tentativa) {
        Connection conexao = abrirConexao();
        boolean gravou = false;
        try {
            conexao.setAutoCommit(false);
            inserirTentativa(conexao, tentativa, false);
            int tentativaId = lerUltimoId(conexao, "tentativas_quiz");

            int quantidade = tentativa.getRespostas().size();
            int[] respostaIds = new int[quantidade];
            for (int i = 0; i < quantidade; i++) {
                inserirResposta(conexao, tentativaId, tentativa.getRespostas().get(i), false);
                respostaIds[i] = lerUltimoId(conexao, "respostas_quiz");
            }

            conexao.commit();
            gravou = true;
            tentativa.setId(tentativaId);
            for (int i = 0; i < quantidade; i++) {
                tentativa.getRespostas().get(i).setId(respostaIds[i]);
            }
        } catch (SQLException erro) {
            throw new PersistenciaException("Erro ao gravar a tentativa. Nada foi gravado. Detalhe: " + erro.getMessage());
        } finally {
            if (!gravou) {
                desfazer(conexao);
            }
            fechar(conexao);
        }
    }

    /** Apaga o quiz inteiro: respostas, tentativas, alternativas, perguntas e o quiz, numa transacao. */
    public void remover(Quiz quiz) {
        Connection conexao = abrirConexao();
        boolean gravou = false;
        try {
            conexao.setAutoCommit(false);
            for (int tentativaId : listarIds(conexao, "SELECT id FROM tentativas_quiz WHERE quiz_id = ?", quiz.getId())) {
                apagar(conexao, "DELETE FROM respostas_quiz WHERE tentativa_id = ?", tentativaId);
            }
            for (int perguntaId : listarIds(conexao, "SELECT id FROM perguntas_quiz WHERE quiz_id = ?", quiz.getId())) {
                apagar(conexao, "DELETE FROM alternativas_quiz WHERE pergunta_id = ?", perguntaId);
            }
            apagar(conexao, "DELETE FROM tentativas_quiz WHERE quiz_id = ?", quiz.getId());
            apagar(conexao, "DELETE FROM perguntas_quiz WHERE quiz_id = ?", quiz.getId());
            apagar(conexao, "DELETE FROM quizzes WHERE id = ?", quiz.getId());

            conexao.commit();
            gravou = true;
        } catch (SQLException erro) {
            throw new PersistenciaException("Erro ao remover o quiz: " + erro.getMessage());
        } finally {
            if (!gravou) {
                desfazer(conexao);
            }
            fechar(conexao);
        }
    }

    /** Usado pelo BancoDao, dentro da transacao dele: regrava o quiz inteiro com os ids que ja tem. */
    void inserirComId(Connection conexao, Aula aula, Quiz quiz) throws SQLException {
        PreparedStatement comando = conexao.prepareStatement(
                "INSERT INTO quizzes (id, aula_id, titulo, descricao) VALUES (?, ?, ?, ?)");
        comando.setInt(1, quiz.getId());
        comando.setInt(2, aula.getId());
        comando.setString(3, quiz.getTitulo());
        comando.setString(4, quiz.getDescricao());
        comando.executeUpdate();
        comando.close();

        for (PerguntaQuiz pergunta : quiz.getPerguntas()) {
            comando = conexao.prepareStatement(
                    "INSERT INTO perguntas_quiz (id, quiz_id, enunciado, dificuldade) VALUES (?, ?, ?, ?)");
            comando.setInt(1, pergunta.getId());
            comando.setInt(2, quiz.getId());
            comando.setString(3, pergunta.getEnunciado());
            comando.setString(4, pergunta.getDificuldade());
            comando.executeUpdate();
            comando.close();
            for (AlternativaQuiz alternativa : pergunta.getAlternativas()) {
                inserirAlternativa(conexao, pergunta.getId(), alternativa, true);
            }
        }
        for (TentativaQuiz tentativa : quiz.getTentativas()) {
            inserirTentativa(conexao, tentativa, true);
            for (RespostaQuiz resposta : tentativa.getRespostas()) {
                inserirResposta(conexao, tentativa.getId(), resposta, true);
            }
        }
    }

    // ------------------------------------------------------------------ apoio

    /** Com comId verdadeiro, grava o id que o objeto ja tem; senao, o banco gera o id. */
    private void inserirAlternativa(Connection conexao, int perguntaId, AlternativaQuiz alternativa, boolean comId)
            throws SQLException {
        PreparedStatement comando = prepararInsercao(conexao, comId,
                "alternativas_quiz", "pergunta_id, texto, explicacao, correta", "?, ?, ?, ?");
        int posicao = comId ? 2 : 1;
        if (comId) {
            comando.setInt(1, alternativa.getId());
        }
        comando.setInt(posicao, perguntaId);
        comando.setString(posicao + 1, alternativa.getTexto());
        comando.setString(posicao + 2, alternativa.getExplicacao());
        comando.setBoolean(posicao + 3, alternativa.isCorreta());
        comando.executeUpdate();
        comando.close();
    }

    /** A data da tentativa fica no modelo como "dd/MM/yyyy HH:mm"; no banco, em data_tentativa e hora_tentativa. */
    private void inserirTentativa(Connection conexao, TentativaQuiz tentativa, boolean comId) throws SQLException {
        PreparedStatement comando = prepararInsercao(conexao, comId, "tentativas_quiz",
                "quiz_id, estudante_id, data_tentativa, hora_tentativa, acertos, total_perguntas", "?, ?, ?, ?, ?, ?");
        int posicao = comId ? 2 : 1;
        if (comId) {
            comando.setInt(1, tentativa.getId());
        }
        String dataEHora = tentativa.getDataTentativa();
        int espaco = dataEHora.indexOf(' ');
        comando.setInt(posicao, tentativa.getQuiz().getId());
        comando.setInt(posicao + 1, tentativa.getEstudante().getId());
        definirData(comando, posicao + 2, converterTextoEmData(dataEHora.substring(0, espaco)));
        comando.setString(posicao + 3, dataEHora.substring(espaco + 1));
        comando.setInt(posicao + 4, tentativa.getAcertos());
        comando.setInt(posicao + 5, tentativa.getTotalPerguntas());
        comando.executeUpdate();
        comando.close();
    }

    private void inserirResposta(Connection conexao, int tentativaId, RespostaQuiz resposta, boolean comId)
            throws SQLException {
        PreparedStatement comando = prepararInsercao(conexao, comId,
                "respostas_quiz", "tentativa_id, pergunta_id, alternativa_id, acertou", "?, ?, ?, ?");
        int posicao = comId ? 2 : 1;
        if (comId) {
            comando.setInt(1, resposta.getId());
        }
        comando.setInt(posicao, tentativaId);
        comando.setInt(posicao + 1, resposta.getPergunta().getId());
        comando.setInt(posicao + 2, resposta.getAlternativa().getId());
        comando.setBoolean(posicao + 3, resposta.isAcertou());
        comando.executeUpdate();
        comando.close();
    }

    /** Monta o INSERT com ou sem a coluna id. */
    private PreparedStatement prepararInsercao(Connection conexao, boolean comId, String tabela, String colunas,
            String parametros) throws SQLException {
        if (comId) {
            return conexao.prepareStatement(
                    "INSERT INTO " + tabela + " (id, " + colunas + ") VALUES (?, " + parametros + ")");
        }
        return conexao.prepareStatement("INSERT INTO " + tabela + " (" + colunas + ") VALUES (" + parametros + ")");
    }

    private ArrayList<Integer> listarIds(Connection conexao, String sql, int id) throws SQLException {
        PreparedStatement consulta = conexao.prepareStatement(sql);
        consulta.setInt(1, id);
        ResultSet resultado = consulta.executeQuery();
        ArrayList<Integer> ids = new ArrayList<>();
        while (resultado.next()) {
            ids.add(resultado.getInt("id"));
        }
        resultado.close();
        consulta.close();
        return ids;
    }

    private void apagar(Connection conexao, String sql, int id) throws SQLException {
        PreparedStatement comando = conexao.prepareStatement(sql);
        comando.setInt(1, id);
        comando.executeUpdate();
        comando.close();
    }
}
