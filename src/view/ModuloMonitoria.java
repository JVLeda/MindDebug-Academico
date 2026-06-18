package view;

import model.Aula;
import model.Monitor;
import model.PedidoMonitoria;

import javax.swing.*;
import java.awt.*;

public class ModuloMonitoria extends JPanel {
    private JanelaMindDebug janela;

    public ModuloMonitoria(JanelaMindDebug janela) {
        this.janela = janela;
        setLayout(new BorderLayout(10, 10));
        montarTela();
    }

    private void montarTela() {
        JList<PedidoMonitoria> lista = new JList<>(janela.modeloLista(janela.getMateriaAberta().getPedidosMonitoria()));
        lista.setBorder(BorderFactory.createTitledBorder("Pedidos"));
        add(new JScrollPane(lista), BorderLayout.CENTER);

        JPanel formulario = new JPanel(new FlowLayout(FlowLayout.LEFT));
        formulario.setBorder(BorderFactory.createTitledBorder("Monitoria"));
        JComboBox<Aula> aulas = new JComboBox<>(janela.getMateriaAberta().getAulas().toArray(new Aula[0]));
        JComboBox<Monitor> monitores = new JComboBox<>(janela.getMateriaAberta().getMonitores().toArray(new Monitor[0]));
        JTextField duvida = new JTextField(16);
        JTextField data = new JTextField(8);
        JTextField horario = new JTextField(5);
        JButton solicitar = janela.botao("Solicitar");
        JButton aceitar = janela.botao("Aceitar");
        JButton recusar = janela.botao("Recusar");

        solicitar.setEnabled(janela.getUsuarioLogado().getNivel() == 1);
        aceitar.setEnabled(janela.getUsuarioLogado().getNivel() >= 2);
        recusar.setEnabled(janela.getUsuarioLogado().getNivel() >= 2);

        formulario.add(new JLabel("Aula:"));
        formulario.add(aulas);
        formulario.add(new JLabel("Monitor:"));
        formulario.add(monitores);
        formulario.add(new JLabel("Duvida:"));
        formulario.add(duvida);
        formulario.add(new JLabel("Data:"));
        formulario.add(data);
        formulario.add(new JLabel("Horario:"));
        formulario.add(horario);
        formulario.add(solicitar);
        formulario.add(aceitar);
        formulario.add(recusar);

        solicitar.addActionListener(e -> janela.tentar(() -> {
            PedidoMonitoria pedido = janela.getController().solicitarMonitoria(
                    janela.getUsuarioLogado(),
                    janela.getMateriaAberta(),
                    (Aula) aulas.getSelectedItem(),
                    (Monitor) monitores.getSelectedItem(),
                    duvida.getText(),
                    data.getText(),
                    horario.getText()
            );
            JOptionPane.showMessageDialog(janela, "Pedido criado: " + pedido.getStatus());
            janela.mostrarMateria();
        }));

        aceitar.addActionListener(e -> janela.tentar(() -> {
            PedidoMonitoria pedido = lista.getSelectedValue();
            if (pedido == null) {
                throw new RuntimeException("Escolha um pedido.");
            }
            janela.getController().aceitarMonitoria(janela.getUsuarioLogado(), pedido, (Monitor) monitores.getSelectedItem());
            JOptionPane.showMessageDialog(janela, "Pedido aceito.");
            janela.mostrarMateria();
        }));

        recusar.addActionListener(e -> janela.tentar(() -> {
            PedidoMonitoria pedido = lista.getSelectedValue();
            if (pedido == null) {
                throw new RuntimeException("Escolha um pedido.");
            }
            janela.getController().recusarMonitoria(janela.getUsuarioLogado(), pedido);
            JOptionPane.showMessageDialog(janela, "Pedido recusado.");
            janela.mostrarMateria();
        }));

        add(formulario, BorderLayout.SOUTH);
    }
}
