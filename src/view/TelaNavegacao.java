package view;

import model.Curso;
import model.Estudante;
import model.Materia;
import model.Periodo;

import javax.swing.*;
import java.awt.*;

public class TelaNavegacao extends JPanel {
    private JanelaMindDebug janela;

    public TelaNavegacao(JanelaMindDebug janela) {
        this.janela = janela;
        setLayout(new BorderLayout(12, 12));
        setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));
        montarTela();
    }

    private void montarTela() {
        add(janela.cabecalho("Tela inicial"), BorderLayout.NORTH);

        JPanel centro = new JPanel(new BorderLayout(10, 10));
        JPanel topo = new JPanel(new FlowLayout(FlowLayout.LEFT));

        JComboBox<Curso> comboCurso = new JComboBox<>();
        if (janela.getUsuarioLogado() instanceof Estudante) {
            comboCurso.addItem(((Estudante) janela.getUsuarioLogado()).getCurso());
        } else {
            for (Curso curso : janela.getController().listarCursos()) {
                comboCurso.addItem(curso);
            }
        }

        JComboBox<Periodo> comboPeriodo = new JComboBox<>();
        DefaultListModel<Materia> modeloMaterias = new DefaultListModel<>();
        JList<Materia> listaMaterias = new JList<>(modeloMaterias);
        listaMaterias.setBorder(BorderFactory.createTitledBorder("Disciplinas:"));

        Runnable atualizarPeriodos = () -> {
            comboPeriodo.removeAllItems();
            Curso curso = (Curso) comboCurso.getSelectedItem();
            if (curso != null) {
                for (Periodo periodo : curso.getPeriodos()) {
                    comboPeriodo.addItem(periodo);
                }
            }
        };

        Runnable atualizarMaterias = () -> {
            modeloMaterias.clear();
            Periodo periodo = (Periodo) comboPeriodo.getSelectedItem();
            if (periodo != null) {
                for (Materia materia : periodo.getMaterias()) {
                    modeloMaterias.addElement(materia);
                }
            }
        };

        comboCurso.addActionListener(e -> {
            atualizarPeriodos.run();
            atualizarMaterias.run();
        });
        comboPeriodo.addActionListener(e -> atualizarMaterias.run());
        atualizarPeriodos.run();
        atualizarMaterias.run();

        topo.add(new JLabel("Curso:"));
        topo.add(comboCurso);
        topo.add(new JLabel("Periodo:"));
        topo.add(comboPeriodo);

        JButton acessar = janela.botao("Acessar Disciplina");
        JButton sair = janela.botao("Sair");
        JPanel rodape = janela.painelBotoes(acessar, sair);

        acessar.addActionListener(e -> janela.tentar(() -> {
            Materia materia = listaMaterias.getSelectedValue();
            if (materia == null) {
                throw new RuntimeException("Escolha uma disciplina.");
            }
            materia.adicionarParticipante(janela.getUsuarioLogado());
            janela.setMateriaAberta(materia);
            janela.mostrarMateria();
        }));

        sair.addActionListener(e -> janela.sair());

        centro.add(topo, BorderLayout.NORTH);
        centro.add(new JScrollPane(listaMaterias), BorderLayout.CENTER);
        centro.add(rodape, BorderLayout.SOUTH);
        add(centro, BorderLayout.CENTER);
    }
}
