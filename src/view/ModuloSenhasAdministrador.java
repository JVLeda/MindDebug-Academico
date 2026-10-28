package view;

import model.Usuario;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class ModuloSenhasAdministrador extends JPanel {
    private JanelaMindDebug janela;

    public ModuloSenhasAdministrador(JanelaMindDebug janela) {
        this.janela = janela;
        setLayout(new BorderLayout(10, 10));
        montarTela();
    }

    private void montarTela() {
        String[] colunas = {"Nome", "Login", "Cargo"};
        DefaultTableModel modelo = new DefaultTableModel(colunas, 0);
        for (Usuario usuario : janela.getController().listarUsuarios()) {
            modelo.addRow(new Object[]{usuario.getNome(), usuario.getLogin(), usuario.getCargo()});
        }
        JTable tabela = new JTable(modelo);
        add(new JScrollPane(tabela), BorderLayout.CENTER);

        JPanel formulario = new JPanel(new FlowLayout(FlowLayout.LEFT));
        formulario.setBorder(BorderFactory.createTitledBorder("Redefinir Senha"));
        JPasswordField senha = new JPasswordField(12);
        JButton redefinir = janela.botao("Redefinir");
        formulario.add(new JLabel("Nova senha:"));
        formulario.add(senha);
        formulario.add(redefinir);
        redefinir.addActionListener(e -> janela.tentar(() -> {
            int linha = tabela.getSelectedRow();
            if (linha < 0) {
                throw new RuntimeException("Escolha um usuario.");
            }
            Usuario usuario = janela.getController().listarUsuarios().get(linha);
            janela.getController().redefinirSenhaDeUsuario(janela.getUsuarioLogado(), usuario, new String(senha.getPassword()));
            JOptionPane.showMessageDialog(janela, "Senha redefinida para " + usuario.getNome() + ".");
            janela.mostrarPainelAdministrador();
        }));
        add(formulario, BorderLayout.SOUTH);
    }
}
