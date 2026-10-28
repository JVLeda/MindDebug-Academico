package view;

import controller.MindDebugController;
import model.Materia;
import model.Usuario;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;

public class JanelaMindDebug extends JFrame {
    private MindDebugController controller;
    private Usuario usuarioLogado;
    private Materia materiaAberta;

    public JanelaMindDebug(MindDebugController controller) {
        this.controller = controller;
        setTitle("MindDebug");
        setSize(1100, 720);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                salvarAoFechar();
            }
        });
        mostrarLogin();
    }

    public void mostrarLogin() {
        setJMenuBar(null);
        mostrarTela(new TelaLogin(this));
    }

    public void mostrarNavegacao() {
        setJMenuBar(null);
        mostrarTela(new TelaNavegacao(this));
    }

    public void mostrarPainelAdministrador() {
        mostrarTela(new PainelAdministrador(this));
    }

    public void mostrarMateria() {
        mostrarTela(new PainelMateria(this));
    }

    public void sair() {
        usuarioLogado = null;
        materiaAberta = null;
        mostrarLogin();
    }

    public void tentar(Runnable acao) {
        try {
            acao.run();
        } catch (RuntimeException erro) {
            JOptionPane.showMessageDialog(this, erro.getMessage(), "Atencao", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void mostrarTela(JPanel painel) {
        setContentPane(painel);
        revalidate();
        repaint();
    }

    private void salvarAoFechar() {
        try {
            controller.salvar();
        } catch (RuntimeException erro) {
            JOptionPane.showMessageDialog(this, "Nao foi possivel salvar os dados: " + erro.getMessage(), "Atencao", JOptionPane.WARNING_MESSAGE);
        }
        dispose();
        System.exit(0);
    }

    JPanel cabecalho(String titulo) {
        JPanel barra = new JPanel(new BorderLayout());
        JLabel nomeSistema = new JLabel("MindDebug");
        nomeSistema.setFont(new Font("Arial", Font.BOLD, 24));
        barra.add(nomeSistema, BorderLayout.WEST);

        if (usuarioLogado != null) {
            JPanel acoes = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            acoes.add(new JLabel("Usuario: " + usuarioLogado.getNome() + " (" + usuarioLogado.getCargo() + ")"));
            barra.add(acoes, BorderLayout.EAST);
        }

        JPanel painel = new JPanel(new BorderLayout());
        painel.add(barra, BorderLayout.NORTH);
        if (titulo != null && !titulo.isEmpty()) {
            JLabel labelTitulo = new JLabel(titulo);
            labelTitulo.setFont(new Font("Arial", Font.BOLD, 18));
            labelTitulo.setBorder(new EmptyBorder(10, 0, 0, 0));
            painel.add(labelTitulo, BorderLayout.SOUTH);
        }
        return painel;
    }

    void trocarSenha() {
        JPasswordField senha = new JPasswordField(12);
        int resposta = JOptionPane.showConfirmDialog(this, senha, "Nova senha", JOptionPane.OK_CANCEL_OPTION);
        if (resposta == JOptionPane.OK_OPTION) {
            tentar(() -> {
                controller.alterarMinhaSenha(usuarioLogado, new String(senha.getPassword()));
                JOptionPane.showMessageDialog(this, "Senha alterada.");
            });
        }
    }

    void salvarDados() {
        tentar(() -> {
            controller.salvar();
            JOptionPane.showMessageDialog(this, "Dados salvos.");
        });
    }

    void voltarAosCursos() {
        materiaAberta = null;
        mostrarNavegacao();
    }

    void mostrarSobre() {
        JOptionPane.showMessageDialog(
                this,
                "MindDebug\n\nSistema de apoio academico e gestao da aprendizagem\nVersao 1.0",
                "Sobre o sistema",
                JOptionPane.PLAIN_MESSAGE
        );
    }

    JButton botao(String texto) {
        return new JButton(texto);
    }

    JPanel painelBotoes(JButton... botoes) {
        JPanel painel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        for (JButton botao : botoes) {
            painel.add(botao);
        }
        return painel;
    }

    GridBagConstraints criarGbc() {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        return gbc;
    }

    void adicionarLinha(JPanel painel, GridBagConstraints gbc, int linha, String texto, JComponent componente) {
        gbc.gridy = linha;
        gbc.gridx = 0;
        gbc.weightx = 0;
        painel.add(new JLabel(texto), gbc);
        gbc.gridx = 1;
        gbc.weightx = 1;
        painel.add(componente, gbc);
    }

    void adicionarComponente(JPanel painel, GridBagConstraints gbc, int linha, JComponent componente) {
        gbc.gridy = linha;
        gbc.gridx = 0;
        gbc.gridwidth = 2;
        painel.add(componente, gbc);
        gbc.gridwidth = 1;
    }

    <T> DefaultListModel<T> modeloLista(ArrayList<T> itens) {
        DefaultListModel<T> modelo = new DefaultListModel<>();
        for (T item : itens) {
            modelo.addElement(item);
        }
        return modelo;
    }

    public MindDebugController getController() {
        return controller;
    }

    public Usuario getUsuarioLogado() {
        return usuarioLogado;
    }

    public void setUsuarioLogado(Usuario usuarioLogado) {
        this.usuarioLogado = usuarioLogado;
    }

    public Materia getMateriaAberta() {
        return materiaAberta;
    }

    public void setMateriaAberta(Materia materiaAberta) {
        this.materiaAberta = materiaAberta;
    }
}
