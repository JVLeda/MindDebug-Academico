package view;

import model.Aula;

import javax.swing.*;
import java.awt.*;

public class PainelAulas extends JPanel {
    private JanelaMindDebug janela;

    public PainelAulas(JanelaMindDebug janela) {
        this.janela = janela;
        setLayout(new BorderLayout(10, 10));
        montarTela();
    }

    private void montarTela() {
        JList<Aula> lista = new JList<>(janela.modeloLista(janela.getMateriaAberta().getAulas()));
        add(new JScrollPane(lista), BorderLayout.CENTER);

        JPanel formulario = new JPanel(new FlowLayout(FlowLayout.LEFT));
        formulario.setBorder(BorderFactory.createTitledBorder("Gestao de aulas"));
        JTextField titulo = new JTextField(16);
        JTextField data = new JTextField(10);
        JButton criar = janela.botao("Criar Aula");
        JButton remover = janela.botao("Remover");

        formulario.add(new JLabel("Titulo da aula:"));
        formulario.add(titulo);
        formulario.add(new JLabel("Data:"));
        formulario.add(data);
        formulario.add(criar);
        formulario.add(remover);

        criar.addActionListener(e -> janela.tentar(() -> {
            janela.getController().criarAula(janela.getUsuarioLogado(), janela.getMateriaAberta(), titulo.getText(), data.getText());
            janela.mostrarMateria();
        }));

        remover.addActionListener(e -> janela.tentar(() -> {
            Aula aula = lista.getSelectedValue();
            if (aula == null) {
                throw new RuntimeException("Escolha uma aula.");
            }
            janela.getMateriaAberta().getAulas().remove(aula);
            janela.mostrarMateria();
        }));

        add(formulario, BorderLayout.SOUTH);
    }
}
