package view;

import model.Aula;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;

public class PainelDashboard extends JPanel {
    private JanelaMindDebug janela;

    public PainelDashboard(JanelaMindDebug janela) {
        this.janela = janela;
        setLayout(new BorderLayout(10, 10));
        montarTela();
    }

    private void montarTela() {
        ArrayList<Aula> aulas = janela.getMateriaAberta().getAulas();
        int totalAlunos = janela.getMateriaAberta().getEstudantes().size();

        JPanel topo = new JPanel(new BorderLayout(10, 10));
        JLabel titulo = new JLabel("Dashboard do Professor");
        titulo.setFont(new Font("Arial", Font.BOLD, 18));
        topo.add(titulo, BorderLayout.NORTH);
        topo.add(new PainelResumoMetricas(aulas, totalAlunos), BorderLayout.CENTER);
        add(topo, BorderLayout.NORTH);

        JPanel graficos = new JPanel(new GridLayout(1, 2, 10, 10));
        graficos.add(new GraficoMediasAulas(aulas));
        graficos.add(new GraficoStatusAulas(aulas));

        String[] colunas = {"Aula", "Data", "Participacao", "Media feedback", "Status", "Acao pedagogica", "Tentativas quiz", "Media quiz"};
        DefaultTableModel modelo = new DefaultTableModel(colunas, 0);
        for (Aula aula : aulas) {
            int respostas = aula.getFeedbacks().size();
            String media = respostas == 0 ? "N/A" : String.format("%.1f", aula.calcularMedia());
            int tentativas = aula.getQuantidadeTentativasQuiz();
            String mediaQuiz = tentativas == 0 ? "N/A" : String.format("%.1f%%", aula.calcularMediaAcertosQuiz());
            modelo.addRow(new Object[]{
                    aula.getTitulo(),
                    aula.getData(),
                    respostas + "/" + totalAlunos + " (" + aula.getParticipacaoDaTurma(totalAlunos) + ")",
                    media,
                    aula.getStatus(),
                    aula.getAcaoPedagogica(),
                    tentativas,
                    mediaQuiz
            });
        }

        JPanel centro = new JPanel(new BorderLayout(10, 10));
        centro.add(graficos, BorderLayout.NORTH);
        centro.add(new JScrollPane(new JTable(modelo)), BorderLayout.CENTER);

        add(centro, BorderLayout.CENTER);
    }
}
