package view;

import model.Aula;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

public class PainelResumoMetricas extends JPanel {
    private ArrayList<Aula> aulas;
    private int totalAlunos;

    public PainelResumoMetricas(ArrayList<Aula> aulas, int totalAlunos) {
        this.aulas = aulas;
        this.totalAlunos = totalAlunos;
        setLayout(new GridLayout(1, 4, 10, 10));
        montarTela();
    }

    private void montarTela() {
        add(cartao("Aulas", String.valueOf(aulas.size())));
        add(cartao("Alunos", String.valueOf(totalAlunos)));
        add(cartao("Media geral", calcularMediaGeral()));
        add(cartao("Aulas criticas", String.valueOf(contarCriticas())));
    }

    private JPanel cartao(String titulo, String valor) {
        JPanel painel = new JPanel(new BorderLayout());
        painel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(210, 210, 210)),
                BorderFactory.createEmptyBorder(8, 10, 8, 10)
        ));

        JLabel labelTitulo = new JLabel(titulo);
        JLabel labelValor = new JLabel(valor);
        labelValor.setFont(new Font("Arial", Font.BOLD, 20));

        painel.add(labelTitulo, BorderLayout.NORTH);
        painel.add(labelValor, BorderLayout.CENTER);
        return painel;
    }

    private String calcularMediaGeral() {
        double soma = 0;
        int quantidade = 0;
        for (Aula aula : aulas) {
            if (!aula.getFeedbacks().isEmpty()) {
                soma += aula.calcularMedia();
                quantidade++;
            }
        }
        if (quantidade == 0) {
            return "N/A";
        }
        return String.format("%.1f", soma / quantidade);
    }

    private int contarCriticas() {
        int quantidade = 0;
        for (Aula aula : aulas) {
            if (aula.getStatus().equals("Critico")) {
                quantidade++;
            }
        }
        return quantidade;
    }
}
