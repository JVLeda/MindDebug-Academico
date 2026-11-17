package dao;

import excecao.MindDebugException;
import excecao.RegraDeNegocioException;
import model.Aula;
import model.Curso;
import model.DiaSemana;
import model.Estudante;
import model.Feedback;
import model.GrupoEstudo;
import model.HorarioMonitor;
import model.Materia;
import model.Material;
import model.Mensagem;
import model.Monitor;
import model.PedidoMonitoria;
import model.Periodo;
import model.Professor;
import model.Usuario;

import java.util.ArrayList;

/**
 * Teste de integracao dos DAOs pedagogicos: aulas, feedbacks, materiais, chats, grupos,
 * pedidos de monitoria e horarios. Precisa do MySQL ativo. Usa o curso de sigla TESTE8
 * e usuarios com login TESTE8_, apagados no fim mesmo se o teste falhar.
 */
public class PedagogicoDaoIntegracaoTest {
    private static final String SIGLA = "TESTE8";
    private static final String[] LOGINS = {"TESTE8_prof", "TESTE8_aluno1", "TESTE8_aluno2", "TESTE8_aluno3",
        "TESTE8_monitor"};

    private static UsuarioDao usuarioDao = new UsuarioDao();
    private static CursoDao cursoDao = new CursoDao();
    private static PeriodoDao periodoDao = new PeriodoDao();
    private static MateriaDao materiaDao = new MateriaDao();
    private static AulaDao aulaDao = new AulaDao();
    private static FeedbackDao feedbackDao = new FeedbackDao();
    private static MaterialDao materialDao = new MaterialDao();
    private static ChatDao chatDao = new ChatDao();
    private static GrupoEstudoDao grupoDao = new GrupoEstudoDao();
    private static PedidoMonitoriaDao pedidoDao = new PedidoMonitoriaDao();
    private static HorarioMonitorDao horarioDao = new HorarioMonitorDao();

    private static Professor professor;
    private static Estudante aluno1;
    private static Estudante aluno2;
    private static Estudante aluno3;
    private static Monitor monitor;

    public static void main(String[] args) {
        new BancoDao().carregar(); // garante que as tabelas existem
        limpar();
        try {
            Materia materia = criarEstrutura();
            Aula aula = aulas(materia);
            feedbacks(materia, aula);
            materiais(materia);
            chats(materia, aula);
            grupos(materia, aula);
            pedidos(materia, aula);
            horarios(materia);
            aulaComFeedbackNaoExclui(aula);
        } finally {
            limpar();
        }
        verificar(cursoDao.buscarPorSigla(SIGLA) == null, "dados de teste apagados no fim");
    }

    private static Materia criarEstrutura() {
        professor = new Professor("Professor de Teste", "TESTE8_prof", "teste8_prof@teste.local", "1234");
        aluno1 = new Estudante("Aluno Um", "teste8_aluno1@teste.local", "TESTE8_aluno1", "1234", null);
        aluno2 = new Estudante("Aluno Dois", "teste8_aluno2@teste.local", "TESTE8_aluno2", "1234", null);
        aluno3 = new Estudante("Aluno Tres", "teste8_aluno3@teste.local", "TESTE8_aluno3", "1234", null);
        monitor = new Monitor("Monitor de Teste", "TESTE8_monitor", "teste8_monitor@teste.local", "1234", "Bolsista");
        usuarioDao.inserir(professor);
        usuarioDao.inserir(aluno1);
        usuarioDao.inserir(aluno2);
        usuarioDao.inserir(aluno3);
        usuarioDao.inserir(monitor);

        Curso curso = new Curso("Curso de Teste", SIGLA, "Teste", 3000, "Coordenacao de Teste");
        cursoDao.inserir(curso);
        Periodo periodo = new Periodo(1, "Periodo de teste");
        periodoDao.inserir(curso, periodo);
        Materia materia = new Materia("Materia de Teste", professor);
        materiaDao.inserir(periodo, materia);
        return materia;
    }

    private static Aula aulas(Materia materia) {
        Aula aula = new Aula("Aula de Teste", "05/03/2026", "Primeira aula");
        aula.setMediaEsperada(6.0);
        aulaDao.inserir(materia, aula);
        verificar(aula.getId() > 0, "aula recebe o id gerado");
        aulaDao.inserir(materia, new Aula("Aula sem feedback", "", ""));

        ArrayList<Aula> lidas = aulaDao.listarDaMateria(materia);
        verificar(lidas.size() == 2, "duas aulas na materia: " + lidas.size());
        Aula lida = lidas.get(0);
        verificar("Aula de Teste".equals(lida.getTitulo()) && "05/03/2026".equals(lida.getData())
                && "Primeira aula".equals(lida.getDescricao()), "titulo, data e descricao lidos");
        verificar(lida.getMediaEsperada() == 6.0, "media esperada lida");
        verificar("".equals(lidas.get(1).getData()), "aula sem data fica sem data");

        aula.setMediaEsperada(7.5);
        aulaDao.atualizar(aula);
        verificar(aulaDao.listarDaMateria(materia).get(0).getMediaEsperada() == 7.5, "media esperada atualizada");
        return aula;
    }

    private static void feedbacks(Materia materia, Aula aula) {
        feedbackDao.registrar(aula, new Feedback(aluno1, 0, "Nao entendi"));
        feedbackDao.registrar(aula, new Feedback(aluno2, 10, "Entendi tudo"));
        verificar(feedbackDao.listarDaAula(aula).size() == 2, "notas 0 e 10 aceitas");

        feedbackDao.registrar(aula, new Feedback(aluno1, 8, "Revisei e entendi"));
        ArrayList<Feedback> lidos = feedbackDao.listarDaAula(aula);
        verificar(lidos.size() == 2, "o aluno continua com um feedback so na aula");
        verificar(lidos.get(0).getEstudante().getId() == aluno1.getId() && lidos.get(0).getNota() == 8
                && "Revisei e entendi".equals(lidos.get(0).getComentario()), "o novo feedback troca nota e comentario");

        verificar(recusouNota(aula, new Feedback(aluno3, 11, "Nota alta demais")), "nota 11 recusada");
        verificar(recusouNota(aula, new Feedback(aluno1, -1, "Nota negativa")), "nota -1 recusada");
        lidos = feedbackDao.listarDaAula(aula);
        verificar(lidos.size() == 2 && lidos.get(0).getNota() == 8, "notas recusadas nao mudam nada");

        Aula comFeedbacks = aulaDao.listarDaMateria(materia).get(0);
        for (Feedback feedback : lidos) {
            comFeedbacks.registrarFeedback(feedback);
        }
        double mediaDoBanco = feedbackDao.calcularMedia(aula);
        verificar(Math.abs(mediaDoBanco - comFeedbacks.calcularMedia()) < 0.0001 && Math.abs(mediaDoBanco - 9.0) < 0.0001,
                "AVG do banco igual a Aula.calcularMedia: " + mediaDoBanco);

        Aula semFeedback = aulaDao.listarDaMateria(materia).get(1);
        verificar(feedbackDao.calcularMedia(semFeedback) == semFeedback.calcularMedia(), "aula sem feedback tem media 0");
    }

    private static boolean recusouNota(Aula aula, Feedback feedback) {
        try {
            feedbackDao.registrar(aula, feedback);
            return false;
        } catch (MindDebugException esperado) {
            return true;
        }
    }

    private static void materiais(Materia materia) {
        Material material = new Material("Apostila de teste", "Arquivo", "apostila.pdf", professor);
        materialDao.inserir(materia, material);
        ArrayList<Material> lidos = materialDao.listarDaMateria(materia);
        verificar(lidos.size() == 1 && lidos.get(0).getId() == material.getId()
                && lidos.get(0).getAutor().getId() == professor.getId()
                && "apostila.pdf".equals(lidos.get(0).getReferencia()), "material publicado e lido com o autor");
        materialDao.remover(material);
        verificar(materialDao.listarDaMateria(materia).isEmpty(), "material removido");
    }

    private static void chats(Materia materia, Aula aula) {
        Mensagem geral = new Mensagem(aluno1, "Mensagem no chat geral");
        Mensagem equipe = new Mensagem(monitor, "Mensagem no chat da equipe");
        Mensagem daAula = new Mensagem(aluno2, "Mensagem no chat da aula");
        chatDao.enviarNoChatGeral(materia, geral);
        chatDao.enviarNoChatEquipe(materia, equipe);
        chatDao.enviarNoChatDaAula(aula, daAula);
        verificar(geral.getId() > 0 && equipe.getId() > geral.getId() && daAula.getId() > equipe.getId(),
                "mensagens recebem ids gerados");

        ArrayList<Mensagem> lidas = chatDao.listarDoChatGeral(materia);
        verificar(lidas.size() == 1 && "Mensagem no chat geral".equals(lidas.get(0).getTexto())
                && lidas.get(0).getAutor().getId() == aluno1.getId(), "mensagem do chat geral lida com o autor");
        verificar(geral.getHora().equals(lidas.get(0).getHora()) && lidas.get(0).getData() != null, "data e hora lidas");
        verificar(chatDao.listarDoChatEquipe(materia).size() == 1, "cada chat da materia tem as suas mensagens");
        verificar(chatDao.listarDoChatDaAula(aula).size() == 1, "a aula tem chat proprio, criado junto com ela");

        chatDao.removerMensagem(geral);
        verificar(chatDao.listarDoChatGeral(materia).isEmpty(), "mensagem apagada");
    }

    private static void grupos(Materia materia, Aula aula) {
        GrupoEstudo grupo = new GrupoEstudo("Grupo de Teste", aula, monitor);
        grupo.adicionarEstudante(aluno1);
        grupoDao.inserir(materia, grupo);
        verificar(grupo.getId() > 0, "grupo recebe o id gerado");

        boolean recusou = false;
        try {
            grupoDao.inserir(materia, new GrupoEstudo("Grupo de Teste", null, null));
        } catch (RegraDeNegocioException esperado) {
            recusou = true;
        }
        verificar(recusou, "nome de grupo repetido na materia recusado");

        recusou = false;
        try {
            grupoDao.adicionarAluno(grupo, aluno1);
        } catch (RegraDeNegocioException esperado) {
            recusou = true;
        }
        verificar(recusou, "o mesmo aluno nao entra duas vezes no grupo");
        grupoDao.adicionarAluno(grupo, aluno2);

        ArrayList<GrupoEstudo> lidos = grupoDao.listarDaMateria(materia);
        verificar(lidos.size() == 1, "um grupo na materia");
        GrupoEstudo lido = lidos.get(0);
        verificar(lido.getEstudantes().size() == 2 && lido.getMonitores().size() == 1, "alunos e monitor do grupo lidos");
        verificar(lido.getAula() != null && lido.getAula().getId() == aula.getId(), "aula do grupo lida");

        grupoDao.removerAluno(grupo, aluno2);
        verificar(grupoDao.listarDaMateria(materia).get(0).getEstudantes().size() == 1, "aluno retirado do grupo");
        grupoDao.remover(grupo);
        verificar(grupoDao.listarDaMateria(materia).isEmpty(), "grupo excluido junto com os membros");
    }

    private static void pedidos(Materia materia, Aula aula) {
        PedidoMonitoria pedido = new PedidoMonitoria(aluno1, aula, null, "Duvida de teste", "10/03/2026", "14:00");
        pedidoDao.inserir(materia, pedido);
        ArrayList<PedidoMonitoria> lidos = pedidoDao.listarDaMateria(materia);
        verificar(lidos.size() == 1, "um pedido na materia");
        PedidoMonitoria lido = lidos.get(0);
        verificar("Solicitado".equals(lido.getStatus()) && lido.getMonitor() == null, "pedido comeca solicitado");
        verificar("10/03/2026".equals(lido.getData()) && "14:00".equals(lido.getHorario())
                && lido.getAula().getId() == aula.getId(), "data, horario e aula do pedido lidos");

        pedidoDao.atualizarStatus(pedido, "Aceito", monitor);
        pedido.aceitar(monitor);
        lido = pedidoDao.listarDaMateria(materia).get(0);
        verificar("Aceito".equals(lido.getStatus()) && lido.getMonitor().getId() == monitor.getId(),
                "pedido aceito pelo monitor");
    }

    private static void horarios(Materia materia) {
        horarioDao.inserir(monitor, new HorarioMonitor(materia, DiaSemana.SEGUNDA, "14:00", "16:00"));

        boolean recusou = false;
        try {
            horarioDao.inserir(monitor, new HorarioMonitor(materia, DiaSemana.SEGUNDA, "15:00", "17:00"));
        } catch (RegraDeNegocioException esperado) {
            recusou = true;
        }
        verificar(recusou, "horario que se sobrepoe a outro do monitor recusado");

        horarioDao.inserir(monitor, new HorarioMonitor(materia, DiaSemana.SEGUNDA, "16:00", "18:00"));
        horarioDao.inserir(monitor, new HorarioMonitor(materia, DiaSemana.TERCA, "15:00", "17:00"));
        ArrayList<HorarioMonitor> lidos = horarioDao.listarDoMonitor(monitor);
        verificar(lidos.size() == 3, "horario logo depois e em outro dia aceitos: " + lidos.size());
        verificar(lidos.get(0).getMateria().getId() == materia.getId() && lidos.get(0).getDia() == DiaSemana.SEGUNDA
                && "14:00".equals(lidos.get(0).getHoraInicio()), "dia, horas e materia lidos");
    }

    private static void aulaComFeedbackNaoExclui(Aula aula) {
        boolean recusou = false;
        try {
            aulaDao.remover(aula);
        } catch (RegraDeNegocioException esperado) {
            recusou = true;
        }
        verificar(recusou, "aula com feedbacks nao e excluida");
    }

    /** Apaga, na ordem das chaves estrangeiras, tudo o que o teste pode ter criado. */
    private static void limpar() {
        Monitor monitorDeTeste = null;
        for (Monitor candidato : usuarioDao.monitoresPorId().values()) {
            if ("TESTE8_monitor".equals(candidato.getLogin())) {
                monitorDeTeste = candidato;
            }
        }
        if (monitorDeTeste != null) {
            for (HorarioMonitor horario : horarioDao.listarDoMonitor(monitorDeTeste)) {
                horarioDao.remover(horario);
            }
        }

        Curso curso = cursoDao.buscarPorSigla(SIGLA);
        if (curso != null) {
            for (Periodo periodo : periodoDao.listarDoCurso(curso)) {
                for (Materia materia : materiaDao.listarDoPeriodo(periodo)) {
                    limparMateria(materia);
                }
            }
            cursoDao.remover(curso);
        }

        for (String login : LOGINS) {
            Usuario usuario = usuarioDao.buscarPorLogin(login);
            if (usuario != null) {
                usuarioDao.remover(usuario);
            }
        }
    }

    private static void limparMateria(Materia materia) {
        for (GrupoEstudo grupo : grupoDao.listarDaMateria(materia)) {
            grupoDao.remover(grupo);
        }
        for (PedidoMonitoria pedido : pedidoDao.listarDaMateria(materia)) {
            pedidoDao.remover(pedido);
        }
        for (Material material : materialDao.listarDaMateria(materia)) {
            materialDao.remover(material);
        }
        apagarMensagens(chatDao.listarDoChatGeral(materia));
        apagarMensagens(chatDao.listarDoChatEquipe(materia));
        for (Aula aula : aulaDao.listarDaMateria(materia)) {
            apagarMensagens(chatDao.listarDoChatDaAula(aula));
            feedbackDao.removerDaAula(aula);
            aulaDao.remover(aula);
        }
        materiaDao.remover(materia);
    }

    private static void apagarMensagens(ArrayList<Mensagem> mensagens) {
        for (Mensagem mensagem : mensagens) {
            chatDao.removerMensagem(mensagem);
        }
    }

    private static void verificar(boolean condicao, String descricao) {
        if (!condicao) {
            throw new AssertionError("FALHOU: " + descricao);
        }
    }
}
