package view;

import model.Monitor;

import javax.swing.*;
import java.awt.*;

public class PainelMonitores extends JPanel {
    private JanelaMindDebug janela;

    public PainelMonitores(JanelaMindDebug janela) {
        this.janela = janela;
        setLayout(new GridLayout(1, 2, 10, 10));
        montarTela();
    }

    private void montarTela() {
        JPanel listaPainel = new JPanel(new BorderLayout());
        listaPainel.setBorder(BorderFactory.createTitledBorder("Gestao de monitores"));
        JList<Monitor> lista = new JList<>(janela.modeloLista(janela.getMateriaAberta().getMonitores()));
        JButton remover = janela.botao("Remover item");
        listaPainel.add(new JScrollPane(lista), BorderLayout.CENTER);
        listaPainel.add(remover, BorderLayout.SOUTH);

        JPanel cadastro = new JPanel(new GridBagLayout());
        cadastro.setBorder(BorderFactory.createTitledBorder("Cadastrar monitores"));
        GridBagConstraints gbc = janela.criarGbc();
        JTextField nome = new JTextField(12);
        JTextField login = new JTextField(12);
        JPasswordField senha = new JPasswordField(12);
        JTextField tipo = new JTextField("Monitor", 12);
        JButton confirmar = janela.botao("Confirmar");

        janela.adicionarLinha(cadastro, gbc, 0, "Nome:", nome);
        janela.adicionarLinha(cadastro, gbc, 1, "Login:", login);
        janela.adicionarLinha(cadastro, gbc, 2, "Senha:", senha);
        janela.adicionarLinha(cadastro, gbc, 3, "Cargo:", tipo);
        janela.adicionarComponente(cadastro, gbc, 4, confirmar);

        confirmar.addActionListener(e -> janela.tentar(() -> {
            Monitor monitor = janela.getController().cadastrarMonitor(
                    janela.getUsuarioLogado(),
                    nome.getText(),
                    login.getText(),
                    login.getText() + "@minddebug.edu.br",
                    new String(senha.getPassword()),
                    tipo.getText()
            );
            janela.getController().adicionarMonitorNaMateria(janela.getUsuarioLogado(), janela.getMateriaAberta(), monitor);
            janela.mostrarMateria();
        }));

        remover.addActionListener(e -> janela.tentar(() -> {
            Monitor monitor = lista.getSelectedValue();
            if (monitor == null) {
                throw new RuntimeException("Escolha um monitor.");
            }
            janela.getMateriaAberta().removerParticipante(monitor);
            janela.mostrarMateria();
        }));

        add(cadastro);
        add(listaPainel);
    }
}
