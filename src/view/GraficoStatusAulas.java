package view;

import model.Aula;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

public class GraficoStatusAulas extends JPanel {
    private ArrayList<Aula> aulas;

    public GraficoStatusAulas(ArrayList<Aula> aulas) {
        this.aulas = aulas;
        setPreferredSize(new Dimension(360, 230));
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createTitledBorder("Status das Aulas"));
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D grafico = (Graphics2D) g;
        grafico.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int critico = contar("Critico");
        int regular = contar("Regular");
        int dominio = contar("Dominio total");
        int semResposta = contar("Sem Respostas");
        int total = aulas.size();

        if (total == 0) {
            desenharTextoCentralizado(grafico, "Nenhuma aula cadastrada");
            return;
        }

        int tamanho = Math.min(getWidth() / 2, getHeight() - 70);
        int x = 35;
        int y = 35;
        int inicio = 90;

        inicio = desenharFatia(grafico, x, y, tamanho, inicio, critico, total, new Color(210, 67, 67));
        inicio = desenharFatia(grafico, x, y, tamanho, inicio, regular, total, new Color(231, 176, 64));
        inicio = desenharFatia(grafico, x, y, tamanho, inicio, dominio, total, new Color(64, 150, 96));
        desenharFatia(grafico, x, y, tamanho, inicio, semResposta, total, new Color(150, 150, 150));

        int legendaX = x + tamanho + 30;
        int legendaY = 45;
        desenharLegenda(grafico, legendaX, legendaY, "Critico", critico, new Color(210, 67, 67));
        desenharLegenda(grafico, legendaX, legendaY + 28, "Regular", regular, new Color(231, 176, 64));
        desenharLegenda(grafico, legendaX, legendaY + 56, "Dominio", dominio, new Color(64, 150, 96));
        desenharLegenda(grafico, legendaX, legendaY + 84, "Sem resposta", semResposta, new Color(150, 150, 150));
    }

    private int contar(String status) {
        int quantidade = 0;
        for (Aula aula : aulas) {
            if (aula.getStatus().equals(status)) {
                quantidade++;
            }
        }
        return quantidade;
    }

    private int desenharFatia(Graphics2D grafico, int x, int y, int tamanho, int inicio, int valor, int total, Color cor) {
        if (valor == 0) {
            return inicio;
        }
        int angulo = Math.round((valor * 360f) / total);
        grafico.setColor(cor);
        grafico.fillArc(x, y, tamanho, tamanho, inicio, -angulo);
        grafico.setColor(Color.WHITE);
        grafico.drawArc(x, y, tamanho, tamanho, inicio, -angulo);
        return inicio - angulo;
    }

    private void desenharLegenda(Graphics2D grafico, int x, int y, String texto, int valor, Color cor) {
        grafico.setColor(cor);
        grafico.fillRoundRect(x, y - 12, 18, 18, 5, 5);
        grafico.setColor(Color.DARK_GRAY);
        grafico.drawString(texto + ": " + valor, x + 26, y + 2);
    }

    private void desenharTextoCentralizado(Graphics2D grafico, String texto) {
        FontMetrics medidas = grafico.getFontMetrics();
        int x = (getWidth() - medidas.stringWidth(texto)) / 2;
        int y = getHeight() / 2;
        grafico.setColor(Color.GRAY);
        grafico.drawString(texto, x, y);
    }
}
