package view;

import model.Usuario;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class PainelUsuarios extends JPanel {
    private JanelaMindDebug janela;

    public PainelUsuarios(JanelaMindDebug janela) {
        this.janela = janela;
        setLayout(new BorderLayout(10, 10));
        montarTela();
    }

    private void montarTela() {
        String[] colunas = {"Nome", "Login", "Senha", "Cargo"};
        DefaultTableModel modelo = new DefaultTableModel(colunas, 0);
        for (Usuario usuario : janela.getController().listarUsuarios()) {
            modelo.addRow(new Object[]{usuario.getNome(), usuario.getLogin(), usuario.getSenha(), usuario.getCargo()});
        }

        JTable tabela = new JTable(modelo);
        add(new JScrollPane(tabela), BorderLayout.CENTER);

        JPanel cadastro = new JPanel(new FlowLayout(FlowLayout.LEFT));
        cadastro.setBorder(BorderFactory.createTitledBorder("Cadastrar Usuarios"));
        JTextField nome = new JTextField(12);
        JTextField login = new JTextField(10);
        JPasswordField senha = new JPasswordField(10);
        JComboBox<String> cargo = new JComboBox<>(new String[]{"Docente", "Monitor", "Aluno"});
        JButton cadastrar = janela.botao("Cadastrar");
        JButton remover = janela.botao("Remover");

        cadastro.add(new JLabel("Nome:"));
        cadastro.add(nome);
        cadastro.add(new JLabel("Login:"));
        cadastro.add(login);
        cadastro.add(new JLabel("Senha:"));
        cadastro.add(senha);
        cadastro.add(new JLabel("Cargo:"));
        cadastro.add(cargo);
        cadastro.add(cadastrar);
        cadastro.add(remover);

        cadastrar.addActionListener(e -> janela.tentar(() -> {
            janela.getController().cadastrarUsuarioPeloCargo(janela.getUsuarioLogado(), nome.getText(), login.getText(), new String(senha.getPassword()), (String) cargo.getSelectedItem());
            janela.mostrarPainelDev();
        }));

        remover.addActionListener(e -> janela.tentar(() -> {
            int linha = tabela.getSelectedRow();
            if (linha < 0) {
                throw new RuntimeException("Escolha um usuario.");
            }
            janela.getController().removerUsuario(janela.getUsuarioLogado(), janela.getController().listarUsuarios().get(linha));
            janela.mostrarPainelDev();
        }));

        add(cadastro, BorderLayout.SOUTH);
    }
}
