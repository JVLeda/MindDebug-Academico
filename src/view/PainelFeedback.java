package view;

import model.Aula;

import javax.swing.*;
import java.awt.*;

public class PainelFeedback extends JPanel {
    private JanelaMindDebug janela;

    public PainelFeedback(JanelaMindDebug janela) {
        this.janela = janela;
        setLayout(new GridBagLayout());
        setBorder(BorderFactory.createTitledBorder("Feedback de Aulas"));
        montarTela();
    }

    private void montarTela() {
        GridBagConstraints gbc = janela.criarGbc();

        JComboBox<Aula> aulas = new JComboBox<>(janela.getMateriaAberta().getAulas().toArray(new Aula[0]));
        JSlider nivel = new JSlider(0, 10, 5);
        nivel.setMajorTickSpacing(1);
        nivel.setPaintTicks(true);
        nivel.setPaintLabels(true);
        JTextField comentario = new JTextField(20);
        JButton registrar = janela.botao("Registrar");
        JButton mostrar = janela.botao("Mostrar feedback");

        janela.adicionarLinha(this, gbc, 0, "Aula:", aulas);
        janela.adicionarLinha(this, gbc, 1, "Nivel de Compreensao:", nivel);
        janela.adicionarLinha(this, gbc, 2, "Comentario:", comentario);
        janela.adicionarComponente(this, gbc, 3, janela.painelBotoes(registrar, mostrar));

        registrar.addActionListener(e -> janela.tentar(() -> {
            janela.getController().registrarFeedback(janela.getUsuarioLogado(), (Aula) aulas.getSelectedItem(), nivel.getValue(), comentario.getText());
            JOptionPane.showMessageDialog(janela, "Feedback registrado.");
        }));

        mostrar.addActionListener(e -> {
            Aula aula = (Aula) aulas.getSelectedItem();
            if (aula != null) {
                JOptionPane.showMessageDialog(janela, "Media atual: " + String.format("%.1f", aula.calcularMedia()) + "\nStatus: " + aula.getStatus());
            }
        });
    }
}
