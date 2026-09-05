package view;

import model.AlternativaQuiz;
import model.Aula;
import model.Estudante;
import model.PerguntaQuiz;
import model.Quiz;
import model.TentativaQuiz;

import javax.swing.*;
import java.awt.*;

public class PainelQuiz extends JPanel {
    private JanelaMindDebug janela;
    private JComboBox<Aula> aulas;
    private TentativaQuiz tentativa;
    private int indicePergunta;
    private JLabel titulo;
    private JTextArea enunciado;
    private JButton[] botoes;

    public PainelQuiz(JanelaMindDebug janela) {
        this.janela = janela;
        setLayout(new BorderLayout(10, 10));
        montarTela();
    }

    private void montarTela() {
        if (!(janela.getUsuarioLogado() instanceof Estudante)) {
            mostrarResumo();
            return;
        }

        aulas = new JComboBox<>(janela.getMateriaAberta().getAulas().toArray(new Aula[0]));
        JButton iniciar = janela.botao("Iniciar quiz");
        JPanel topo = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topo.add(new JLabel("Aula:"));
        topo.add(aulas);
        topo.add(iniciar);
        add(topo, BorderLayout.NORTH);

        titulo = new JLabel("Selecione uma aula e inicie o quiz.");
        titulo.setFont(new Font("Arial", Font.BOLD, 16));
        enunciado = new JTextArea();
        enunciado.setEditable(false);
        enunciado.setLineWrap(true);
        enunciado.setWrapStyleWord(true);
        botoes = new JButton[4];
        JPanel respostas = new JPanel(new GridLayout(4, 1, 6, 6));
        for (int i = 0; i < 4; i++) {
            final int indice = i;
            botoes[i] = janela.botao("");
            botoes[i].setEnabled(false);
            botoes[i].addActionListener(e -> responder(indice));
            respostas.add(botoes[i]);
        }
        JPanel centro = new JPanel(new BorderLayout(8, 8));
        centro.add(titulo, BorderLayout.NORTH);
        centro.add(new JScrollPane(enunciado), BorderLayout.CENTER);
        centro.add(respostas, BorderLayout.SOUTH);
        add(centro, BorderLayout.CENTER);

        iniciar.addActionListener(e -> janela.tentar(() -> {
            Aula aula = (Aula) aulas.getSelectedItem();
            Quiz quiz = aula == null ? null : aula.getQuiz();
            tentativa = janela.getController().iniciarTentativaQuiz(janela.getUsuarioLogado(), janela.getMateriaAberta(), quiz);
            indicePergunta = 0;
            mostrarPergunta();
        }));
    }

    private void mostrarResumo() {
        String[] colunas = {"Aula", "Tentativas", "Media de acertos"};
        Object[][] dados = new Object[janela.getMateriaAberta().getAulas().size()][3];

        for (int i = 0; i < janela.getMateriaAberta().getAulas().size(); i++) {
            Aula aula = janela.getMateriaAberta().getAulas().get(i);
            int tentativas = aula.getQuantidadeTentativasQuiz();
            dados[i][0] = aula.getTitulo();
            dados[i][1] = tentativas;
            dados[i][2] = tentativas == 0 ? "N/A" : String.format("%.1f%%", aula.calcularMediaAcertosQuiz());
        }

        add(new JLabel("Acompanhamento de resultados dos quizzes"), BorderLayout.NORTH);
        add(new JScrollPane(new JTable(dados, colunas)), BorderLayout.CENTER);
    }

    private void mostrarPergunta() {
        Quiz quiz = tentativa.getQuiz();
        if (indicePergunta == quiz.getPerguntas().size()) {
            janela.getController().finalizarTentativaQuiz(janela.getUsuarioLogado(), tentativa);
            titulo.setText("Quiz finalizado: " + tentativa.getAcertos() + "/" + tentativa.getTotalPerguntas());
            enunciado.setText("Percentual de acertos: " + String.format("%.1f", tentativa.getPercentualAcertos()) + "%");
            for (JButton botao : botoes) botao.setEnabled(false);
            return;
        }
        PerguntaQuiz pergunta = quiz.getPerguntas().get(indicePergunta);
        titulo.setText("Pergunta " + (indicePergunta + 1) + " de " + quiz.getPerguntas().size());
        enunciado.setText(pergunta.getEnunciado());
        for (int i = 0; i < 4; i++) {
            botoes[i].setText((char) ('A' + i) + ") " + pergunta.getAlternativas().get(i).getTexto());
            botoes[i].setEnabled(true);
        }
    }

    private void responder(int indice) {
        PerguntaQuiz pergunta = tentativa.getQuiz().getPerguntas().get(indicePergunta);
        AlternativaQuiz alternativa = pergunta.getAlternativas().get(indice);
        janela.tentar(() -> {
            janela.getController().registrarRespostaQuiz(janela.getUsuarioLogado(), tentativa, pergunta, alternativa);
            JOptionPane.showMessageDialog(janela, alternativa.getExplicacao());
            indicePergunta++;
            mostrarPergunta();
        });
    }
}
