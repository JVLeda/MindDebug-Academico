package view;

import model.Curso;

import javax.swing.*;
import java.awt.*;

public class TelaLogin extends JPanel {
    private JanelaMindDebug janela;

    public TelaLogin(JanelaMindDebug janela) {
        this.janela = janela;
        setLayout(new GridBagLayout());
        montarTela();
    }

    private void montarTela() {
        JPanel caixa = new JPanel(new GridBagLayout());
        caixa.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder("Cadastro Login"),
                BorderFactory.createEmptyBorder(15, 25, 15, 25)
        ));

        GridBagConstraints gbc = janela.criarGbc();
        JLabel titulo = new JLabel("MindDebug", SwingConstants.CENTER);
        titulo.setFont(new Font("Arial", Font.BOLD, 28));
        janela.adicionarComponente(caixa, gbc, 0, titulo);

        JTextField campoUsuario = new JTextField(18);
        JPasswordField campoSenha = new JPasswordField(18);
        JButton entrar = janela.botao("Entrar");
        JButton cadastrar = janela.botao("Cadastrar");
        JButton esqueciSenha = janela.botao("Esqueci minha senha");

        janela.adicionarLinha(caixa, gbc, 1, "Usuario:", campoUsuario);
        janela.adicionarLinha(caixa, gbc, 2, "Senha:", campoSenha);
        janela.adicionarComponente(caixa, gbc, 3, entrar);
        janela.adicionarComponente(caixa, gbc, 4, cadastrar);
        janela.adicionarComponente(caixa, gbc, 5, esqueciSenha);

        JTextArea acessos = new JTextArea("Login Administrador: admin / 1234\nLogin Professor: prof / 1234\nLogin Monitor: monitor / 1234\nLogin Aluno: 2026001 / 1234");
        acessos.setEditable(false);
        acessos.setOpaque(false);
        janela.adicionarComponente(caixa, gbc, 6, acessos);

        entrar.addActionListener(e -> janela.tentar(() -> {
            janela.setUsuarioLogado(janela.getController().fazerLogin(campoUsuario.getText(), new String(campoSenha.getPassword())));
            if (janela.getUsuarioLogado().getNivel() == 4) {
                janela.mostrarPainelAdministrador();
            } else {
                janela.mostrarNavegacao();
            }
        }));

        cadastrar.addActionListener(e -> abrirCadastroAluno());
        esqueciSenha.addActionListener(e -> JOptionPane.showMessageDialog(janela, "Procure o Administrador do Sistema para redefinir sua senha."));

        add(caixa);
    }

    private void abrirCadastroAluno() {
        JPanel formulario = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = janela.criarGbc();

        JTextField nome = new JTextField(18);
        JTextField email = new JTextField(18);
        JTextField matricula = new JTextField(18);
        JPasswordField senha = new JPasswordField(18);
        JComboBox<Curso> curso = new JComboBox<>(janela.getController().listarCursos().toArray(new Curso[0]));

        janela.adicionarComponente(formulario, gbc, 0, new JLabel("MindDebug", SwingConstants.CENTER));
        janela.adicionarLinha(formulario, gbc, 1, "Nome:", nome);
        janela.adicionarLinha(formulario, gbc, 2, "Email:", email);
        janela.adicionarLinha(formulario, gbc, 3, "Matricula:", matricula);
        janela.adicionarLinha(formulario, gbc, 4, "Senha:", senha);
        janela.adicionarLinha(formulario, gbc, 5, "Curso:", curso);

        int resposta = JOptionPane.showConfirmDialog(janela, formulario, "Cadastro", JOptionPane.OK_CANCEL_OPTION);
        if (resposta == JOptionPane.OK_OPTION) {
            janela.tentar(() -> {
                janela.getController().cadastrarEstudante(nome.getText(), email.getText(), matricula.getText(), new String(senha.getPassword()), (Curso) curso.getSelectedItem());
                JOptionPane.showMessageDialog(janela, "Cadastro realizado.");
            });
        }
    }
}
