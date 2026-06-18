package view;

import model.Material;

import javax.swing.*;
import java.awt.*;
import java.io.File;

public class PainelRepositorio extends JPanel {
    private JanelaMindDebug janela;

    public PainelRepositorio(JanelaMindDebug janela) {
        this.janela = janela;
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createTitledBorder("Repositorio de Arquivos"));
        montarTela();
    }

    private void montarTela() {
        JList<Material> lista = new JList<>(janela.modeloLista(janela.getMateriaAberta().getMateriais()));
        add(new JScrollPane(lista), BorderLayout.CENTER);

        JPanel formulario = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JTextField novoItem = new JTextField(25);
        JButton adicionarLink = janela.botao("Adicionar Link");
        JButton enviarArquivo = janela.botao("Enviar Arquivo");
        JButton apagar = janela.botao("Apagar Selecionado");

        formulario.add(new JLabel("Novo Item:"));
        formulario.add(novoItem);
        formulario.add(adicionarLink);
        formulario.add(enviarArquivo);
        formulario.add(apagar);

        adicionarLink.addActionListener(e -> janela.tentar(() -> {
            janela.getController().adicionarMaterial(janela.getMateriaAberta(), janela.getUsuarioLogado(), "Link", "Link", novoItem.getText());
            janela.mostrarMateria();
        }));

        enviarArquivo.addActionListener(e -> janela.tentar(() -> {
            JFileChooser seletor = new JFileChooser();
            if (seletor.showOpenDialog(janela) == JFileChooser.APPROVE_OPTION) {
                File arquivo = seletor.getSelectedFile();
                janela.getController().adicionarMaterial(janela.getMateriaAberta(), janela.getUsuarioLogado(), arquivo.getName(), "Arquivo", arquivo.getName());
                janela.mostrarMateria();
            }
        }));

        apagar.addActionListener(e -> janela.tentar(() -> {
            janela.getController().removerMaterial(janela.getMateriaAberta(), janela.getUsuarioLogado(), lista.getSelectedValue());
            janela.mostrarMateria();
        }));

        add(formulario, BorderLayout.SOUTH);
    }
}
