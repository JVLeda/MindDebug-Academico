package view;

import model.Aula;
import model.Quiz;

import javax.swing.*;
import java.awt.*;

public class PainelGerenciarQuiz extends JPanel {
    private JanelaMindDebug janela;

    public PainelGerenciarQuiz(JanelaMindDebug janela) {
        this.janela = janela;
        setLayout(new BorderLayout(10, 10));
        montarTela();
    }

    private void montarTela() {
        JComboBox<Aula> aulas = new JComboBox<>(janela.getMateriaAberta().getAulas().toArray(new Aula[0]));
        JTextField titulo = new JTextField(18);
        JTextField descricao = new JTextField(18);
        JButton criar = janela.botao("Criar quiz");
        JButton remover = janela.botao("Remover quiz");

        JPanel quiz = new JPanel(new GridBagLayout());
        quiz.setBorder(BorderFactory.createTitledBorder("Quiz da aula"));
        GridBagConstraints gbc = janela.criarGbc();
        janela.adicionarLinha(quiz, gbc, 0, "Aula:", aulas);
        janela.adicionarLinha(quiz, gbc, 1, "Titulo:", titulo);
        janela.adicionarLinha(quiz, gbc, 2, "Descricao:", descricao);
        janela.adicionarComponente(quiz, gbc, 3, janela.painelBotoes(criar, remover));

        JTextField enunciado = new JTextField(24);
        JComboBox<String> dificuldade = new JComboBox<>(new String[]{"Facil", "Medio", "Dificil"});
        JTextField[] alternativas = new JTextField[4];
        JTextField[] explicacoes = new JTextField[4];
        for (int i = 0; i < 4; i++) {
            alternativas[i] = new JTextField(18);
            explicacoes[i] = new JTextField(18);
        }
        JComboBox<String> correta = new JComboBox<>(new String[]{"A", "B", "C", "D"});
        JButton adicionar = janela.botao("Adicionar pergunta");

        JPanel pergunta = new JPanel(new GridBagLayout());
        pergunta.setBorder(BorderFactory.createTitledBorder("Nova pergunta"));
        GridBagConstraints gbcPergunta = janela.criarGbc();
        janela.adicionarLinha(pergunta, gbcPergunta, 0, "Enunciado:", enunciado);
        janela.adicionarLinha(pergunta, gbcPergunta, 1, "Dificuldade:", dificuldade);
        for (int i = 0; i < 4; i++) {
            janela.adicionarLinha(pergunta, gbcPergunta, i + 2, "Alternativa " + (char) ('A' + i) + ":", alternativas[i]);
            janela.adicionarLinha(pergunta, gbcPergunta, i + 6, "Explicacao " + (char) ('A' + i) + ":", explicacoes[i]);
        }
        janela.adicionarLinha(pergunta, gbcPergunta, 10, "Correta:", correta);
        janela.adicionarComponente(pergunta, gbcPergunta, 11, janela.painelBotoes(adicionar));

        criar.addActionListener(e -> janela.tentar(() -> {
            janela.getController().criarQuiz(janela.getUsuarioLogado(), janela.getMateriaAberta(),
                    (Aula) aulas.getSelectedItem(), titulo.getText(), descricao.getText());
            janela.mostrarMateria();
        }));
        remover.addActionListener(e -> janela.tentar(() -> {
            janela.getController().removerQuiz(janela.getUsuarioLogado(), janela.getMateriaAberta(),
                    (Aula) aulas.getSelectedItem());
            janela.mostrarMateria();
        }));
        adicionar.addActionListener(e -> janela.tentar(() -> {
            Aula aula = (Aula) aulas.getSelectedItem();
            Quiz quizDaAula = aula == null ? null : aula.getQuiz();
            String[] textos = new String[4];
            String[] detalhes = new String[4];
            for (int i = 0; i < 4; i++) {
                textos[i] = alternativas[i].getText();
                detalhes[i] = explicacoes[i].getText();
            }
            janela.getController().adicionarPerguntaAoQuiz(janela.getUsuarioLogado(), janela.getMateriaAberta(),
                    quizDaAula, enunciado.getText(), (String) dificuldade.getSelectedItem(), textos, detalhes,
                    correta.getSelectedIndex());
            JOptionPane.showMessageDialog(janela, "Pergunta adicionada ao quiz.");
        }));

        add(quiz, BorderLayout.NORTH);
        add(new JScrollPane(pergunta), BorderLayout.CENTER);
    }
}
