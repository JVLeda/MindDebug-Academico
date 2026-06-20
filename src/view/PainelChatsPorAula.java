package view;

import model.Aula;

import javax.swing.*;
import java.awt.*;

public class PainelChatsPorAula extends JPanel {
    private JanelaMindDebug janela;

    public PainelChatsPorAula(JanelaMindDebug janela) {
        this.janela = janela;
        setLayout(new BorderLayout(10, 10));
        montarTela();
    }

    private void montarTela() {
        JPanel topo = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JComboBox<Aula> aulas = new JComboBox<>(janela.getMateriaAberta().getAulas().toArray(new Aula[0]));
        JPanel areaChat = new JPanel(new BorderLayout());

        Runnable atualizar = () -> {
            areaChat.removeAll();
            Aula aula = (Aula) aulas.getSelectedItem();
            if (aula != null) {
                areaChat.add(new PainelChat(janela, "", aula.getChat()), BorderLayout.CENTER);
            }
            areaChat.revalidate();
            areaChat.repaint();
        };

        aulas.addActionListener(e -> atualizar.run());
        topo.add(new JLabel("Aula:"));
        topo.add(aulas);
        add(topo, BorderLayout.NORTH);
        add(areaChat, BorderLayout.CENTER);
        atualizar.run();
    }
}
