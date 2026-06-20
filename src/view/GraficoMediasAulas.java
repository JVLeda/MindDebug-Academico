package view;

import model.Aula;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

public class GraficoMediasAulas extends JPanel {
    private ArrayList<Aula> aulas;

    public GraficoMediasAulas(ArrayList<Aula> aulas) {
        this.aulas = aulas;
        setPreferredSize(new Dimension(420, 230));
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createTitledBorder("Media por Aula"));
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D grafico = (Graphics2D) g;
        grafico.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        if (aulas.isEmpty()) {
            desenharTextoCentralizado(grafico, "Nenhuma aula cadastrada");
            return;
        }

        int margemEsquerda = 45;
        int margemBaixo = 45;
        int margemTopo = 25;
        int larguraUtil = getWidth() - margemEsquerda - 20;
        int alturaUtil = getHeight() - margemTopo - margemBaixo;
        int larguraBarra = Math.max(18, larguraUtil / aulas.size() - 18);

        desenharLinhas(grafico, margemEsquerda, margemTopo, larguraUtil, alturaUtil);

        for (int i = 0; i < aulas.size(); i++) {
            Aula aula = aulas.get(i);
            double media = aula.calcularMedia();
            int alturaBarra = (int) ((media / 10.0) * alturaUtil);
            int x = margemEsquerda + 10 + i * (larguraUtil / aulas.size());
            int y = margemTopo + alturaUtil - alturaBarra;

            grafico.setColor(corDaMedia(media, aula.getFeedbacks().isEmpty()));
            grafico.fillRoundRect(x, y, larguraBarra, alturaBarra, 8, 8);

            grafico.setColor(Color.DARK_GRAY);
            grafico.drawString(aula.getFeedbacks().isEmpty() ? "N/A" : String.format("%.1f", media), x, y - 5);
            grafico.drawString(nomeCurto(aula.getTitulo()), x, margemTopo + alturaUtil + 18);
        }
    }

    private void desenharLinhas(Graphics2D grafico, int x, int y, int largura, int altura) {
        grafico.setColor(new Color(230, 230, 230));
        for (int nota = 0; nota <= 10; nota += 2) {
            int linhaY = y + altura - (int) ((nota / 10.0) * altura);
            grafico.drawLine(x, linhaY, x + largura, linhaY);
            grafico.setColor(Color.GRAY);
            grafico.drawString(String.valueOf(nota), 12, linhaY + 4);
            grafico.setColor(new Color(230, 230, 230));
        }
        grafico.setColor(Color.DARK_GRAY);
        grafico.drawLine(x, y, x, y + altura);
        grafico.drawLine(x, y + altura, x + largura, y + altura);
    }

    private Color corDaMedia(double media, boolean semResposta) {
        if (semResposta) {
            return new Color(150, 150, 150);
        }
        if (media < 4) {
            return new Color(210, 67, 67);
        }
        if (media < 7) {
            return new Color(231, 176, 64);
        }
        return new Color(64, 150, 96);
    }

    private String nomeCurto(String texto) {
        if (texto.length() <= 9) {
            return texto;
        }
        return texto.substring(0, 8) + ".";
    }

    private void desenharTextoCentralizado(Graphics2D grafico, String texto) {
        FontMetrics medidas = grafico.getFontMetrics();
        int x = (getWidth() - medidas.stringWidth(texto)) / 2;
        int y = getHeight() / 2;
        grafico.setColor(Color.GRAY);
        grafico.drawString(texto, x, y);
    }
}
