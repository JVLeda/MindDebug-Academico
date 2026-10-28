package view;

import model.Aula;
import model.PerguntaQuiz;
import model.Quiz;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;

public class PainelGerenciarQuiz extends JPanel {
    private final JanelaMindDebug janela;
    private JComboBox<Aula> aulas;
    private JTextField titulo;
    private JTextField descricao;
    private JButton criar;
    private JButton remover;
    private JLabel statusQuiz;
    private JTabbedPane abas;
    private JTextArea enunciado;
    private JComboBox<String> dificuldade;
    private JTextField[] alternativas;
    private JTextField[] explicacoes;
    private JComboBox<String> correta;
    private JButton adicionar;
    private DefaultListModel<String> perguntasCadastradas;

    public PainelGerenciarQuiz(JanelaMindDebug janela) {
        this.janela = janela;
        setLayout(new BorderLayout(12, 12));
        setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        montarTela();
    }

    private void montarTela() {
        add(criarCabecalho(), BorderLayout.NORTH);

        abas = new JTabbedPane();
        abas.addTab("1. Quiz da aula", criarPainelQuiz());
        abas.addTab("2. Perguntas", criarPainelPerguntas());
        add(abas, BorderLayout.CENTER);

        aulas.addActionListener(e -> atualizarEstadoQuiz());
        criar.addActionListener(e -> janela.tentar(this::criarQuiz));
        remover.addActionListener(e -> janela.tentar(this::removerQuiz));
        adicionar.addActionListener(e -> janela.tentar(this::adicionarPergunta));
        atualizarEstadoQuiz();
    }

    private JPanel criarCabecalho() {
        JPanel cabecalho = new JPanel(new BorderLayout(8, 6));
        JPanel texto = new JPanel();
        texto.setLayout(new BoxLayout(texto, BoxLayout.Y_AXIS));

        JLabel tituloTela = new JLabel("Gestao de quiz");
        tituloTela.setFont(tituloTela.getFont().deriveFont(Font.BOLD, 20f));
        texto.add(tituloTela);
        texto.add(new JLabel("Selecione uma aula, crie ou revise o quiz e cadastre as perguntas no mesmo lugar."));
        cabecalho.add(texto, BorderLayout.NORTH);

        JPanel seletor = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        seletor.add(new JLabel("Aula:"));
        aulas = new JComboBox<>(janela.getMateriaAberta().getAulas().toArray(new Aula[0]));
        aulas.setPreferredSize(new Dimension(330, aulas.getPreferredSize().height));
        seletor.add(aulas);
        cabecalho.add(seletor, BorderLayout.SOUTH);
        return cabecalho;
    }

    private JPanel criarPainelQuiz() {
        JPanel painel = new JPanel();
        painel.setLayout(new BoxLayout(painel, BoxLayout.Y_AXIS));
        painel.setBorder(BorderFactory.createEmptyBorder(12, 4, 4, 4));

        JPanel formulario = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = janela.criarGbc();
        titulo = new JTextField(28);
        descricao = new JTextField(28);
        criar = janela.botao("Criar quiz desta aula");
        remover = janela.botao("Remover quiz");
        titulo.getDocument().addDocumentListener(new AtualizadorCriacaoQuiz());
        descricao.getDocument().addDocumentListener(new AtualizadorCriacaoQuiz());
        janela.adicionarLinha(formulario, gbc, 0, "Titulo:", titulo);
        janela.adicionarLinha(formulario, gbc, 1, "Descricao:", descricao);
        janela.adicionarComponente(formulario, gbc, 2, janela.painelBotoes(criar, remover));
        formulario.setAlignmentX(Component.LEFT_ALIGNMENT);
        formulario.setMaximumSize(new Dimension(Integer.MAX_VALUE, formulario.getPreferredSize().height));
        painel.add(formulario);
        painel.add(Box.createVerticalStrut(12));

        JPanel resumo = new JPanel(new BorderLayout());
        resumo.setBorder(BorderFactory.createTitledBorder("Situacao da aula selecionada"));
        statusQuiz = new JLabel();
        statusQuiz.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        resumo.add(statusQuiz, BorderLayout.CENTER);
        resumo.setAlignmentX(Component.LEFT_ALIGNMENT);
        resumo.setMaximumSize(new Dimension(Integer.MAX_VALUE, resumo.getPreferredSize().height));
        painel.add(resumo);
        painel.add(Box.createVerticalGlue());
        return painel;
    }

    private JPanel criarPainelPerguntas() {
        JPanel painel = new JPanel(new BorderLayout(12, 12));
        painel.setBorder(BorderFactory.createEmptyBorder(12, 4, 4, 4));

        JPanel formulario = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = janela.criarGbc();
        enunciado = new JTextArea(3, 36);
        enunciado.setLineWrap(true);
        enunciado.setWrapStyleWord(true);
        dificuldade = new JComboBox<>(new String[]{"Facil", "Medio", "Dificil"});
        correta = new JComboBox<>(new String[]{"A", "B", "C", "D"});
        adicionar = janela.botao("Adicionar pergunta ao quiz");

        janela.adicionarLinha(formulario, gbc, 0, "Enunciado:", new JScrollPane(enunciado));
        janela.adicionarLinha(formulario, gbc, 1, "Dificuldade:", dificuldade);
        formulario.add(criarAlternativas(), criarGbcBloco(2));
        janela.adicionarLinha(formulario, gbc, 3, "Resposta correta:", correta);
        janela.adicionarComponente(formulario, gbc, 4, janela.painelBotoes(adicionar));

        JPanel perguntas = new JPanel(new BorderLayout(6, 6));
        perguntas.setBorder(BorderFactory.createTitledBorder("Perguntas cadastradas"));
        perguntasCadastradas = new DefaultListModel<>();
        JList<String> lista = new JList<>(perguntasCadastradas);
        lista.setVisibleRowCount(7);
        perguntas.add(new JScrollPane(lista), BorderLayout.CENTER);

        painel.add(formulario, BorderLayout.NORTH);
        painel.add(perguntas, BorderLayout.CENTER);
        return painel;
    }

    private JPanel criarAlternativas() {
        JPanel bloco = new JPanel(new GridBagLayout());
        bloco.setBorder(BorderFactory.createTitledBorder("Alternativas e feedback ao aluno"));
        alternativas = new JTextField[4];
        explicacoes = new JTextField[4];
        GridBagConstraints gbc = janela.criarGbc();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1;

        for (int i = 0; i < 4; i++) {
            alternativas[i] = new JTextField(18);
            explicacoes[i] = new JTextField(18);
            char letra = (char) ('A' + i);
            janela.adicionarLinha(bloco, gbc, i * 2, "Alternativa " + letra + ":", alternativas[i]);
            janela.adicionarLinha(bloco, gbc, i * 2 + 1, "Feedback " + letra + ":", explicacoes[i]);
        }
        return bloco;
    }

    private GridBagConstraints criarGbcBloco(int linha) {
        GridBagConstraints gbc = janela.criarGbc();
        gbc.gridx = 0;
        gbc.gridy = linha;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1;
        return gbc;
    }

    private void criarQuiz() {
        Aula aula = getAulaSelecionada();
        janela.getController().criarQuiz(janela.getUsuarioLogado(), janela.getMateriaAberta(), aula,
                titulo.getText(), descricao.getText());
        atualizarEstadoQuiz();
        abas.setSelectedIndex(1);
        JOptionPane.showMessageDialog(janela, "Quiz criado. Agora cadastre as perguntas.");
    }

    private void removerQuiz() {
        janela.getController().removerQuiz(janela.getUsuarioLogado(), janela.getMateriaAberta(), getAulaSelecionada());
        atualizarEstadoQuiz();
    }

    private void adicionarPergunta() {
        Quiz quiz = getQuizSelecionado();
        String[] textos = new String[4];
        String[] detalhes = new String[4];
        for (int i = 0; i < 4; i++) {
            textos[i] = alternativas[i].getText();
            detalhes[i] = explicacoes[i].getText();
        }
        janela.getController().adicionarPerguntaAoQuiz(janela.getUsuarioLogado(), janela.getMateriaAberta(), quiz,
                enunciado.getText(), (String) dificuldade.getSelectedItem(), textos, detalhes, correta.getSelectedIndex());
        limparFormularioPergunta();
        atualizarListaPerguntas(quiz);
        JOptionPane.showMessageDialog(janela, "Pergunta adicionada ao quiz.");
    }

    private void atualizarEstadoQuiz() {
        Quiz quiz = getQuizSelecionado();
        boolean possuiQuiz = quiz != null;
        titulo.setEnabled(!possuiQuiz);
        descricao.setEnabled(!possuiQuiz);
        atualizarDisponibilidadeCriacao();
        remover.setEnabled(possuiQuiz);
        abas.setEnabledAt(1, possuiQuiz);

        if (possuiQuiz) {
            titulo.setText(quiz.getTitulo());
            descricao.setText(quiz.getDescricao());
            statusQuiz.setText("<html><b>Quiz ativo:</b> " + quiz.getTitulo()
                    + "<br>" + quiz.getPerguntas().size()
                    + " pergunta(s) cadastrada(s). Abra a aba Perguntas para continuar a edicao.</html>");
            atualizarListaPerguntas(quiz);
        } else {
            titulo.setText("");
            descricao.setText("");
            statusQuiz.setText("<html>Nenhum quiz foi criado para esta aula. Informe um titulo e uma descricao para comecar.</html>");
            perguntasCadastradas.clear();
            if (abas.getSelectedIndex() == 1) {
                abas.setSelectedIndex(0);
            }
        }
    }

    private void atualizarListaPerguntas(Quiz quiz) {
        perguntasCadastradas.clear();
        int numero = 1;
        for (PerguntaQuiz pergunta : quiz.getPerguntas()) {
            perguntasCadastradas.addElement(numero++ + ". " + pergunta.getEnunciado());
        }
        if (perguntasCadastradas.isEmpty()) {
            perguntasCadastradas.addElement("Nenhuma pergunta cadastrada ainda.");
        }
    }

    private void limparFormularioPergunta() {
        enunciado.setText("");
        dificuldade.setSelectedIndex(0);
        correta.setSelectedIndex(0);
        for (int i = 0; i < 4; i++) {
            alternativas[i].setText("");
            explicacoes[i].setText("");
        }
    }

    private void atualizarDisponibilidadeCriacao() {
        if (criar == null || titulo == null || descricao == null) {
            return;
        }
        criar.setEnabled(getQuizSelecionado() == null && getAulaSelecionada() != null
                && !titulo.getText().trim().isEmpty() && !descricao.getText().trim().isEmpty());
    }

    private class AtualizadorCriacaoQuiz implements DocumentListener {
        @Override
        public void insertUpdate(DocumentEvent evento) {
            atualizarDisponibilidadeCriacao();
        }

        @Override
        public void removeUpdate(DocumentEvent evento) {
            atualizarDisponibilidadeCriacao();
        }

        @Override
        public void changedUpdate(DocumentEvent evento) {
            atualizarDisponibilidadeCriacao();
        }
    }

    private Aula getAulaSelecionada() {
        return (Aula) aulas.getSelectedItem();
    }

    private Quiz getQuizSelecionado() {
        Aula aula = getAulaSelecionada();
        return aula == null ? null : aula.getQuiz();
    }
}
