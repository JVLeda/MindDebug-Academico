package view;

import model.Curso;
import model.Materia;
import model.Periodo;
import model.Professor;

import javax.swing.*;
import java.awt.*;

public class PainelDisciplinas extends JPanel {
    private JanelaMindDebug janela;

    public PainelDisciplinas(JanelaMindDebug janela) {
        this.janela = janela;
        setLayout(new BorderLayout(10, 10));
        montarTela();
    }

    private void montarTela() {
        JPanel filtros = new JPanel(new FlowLayout(FlowLayout.LEFT));

        JComboBox<Curso> cursos = new JComboBox<>(janela.getController().listarCursos().toArray(new Curso[0]));
        JComboBox<Periodo> periodos = new JComboBox<>();
        DefaultListModel<Materia> modelo = new DefaultListModel<>();
        JList<Materia> lista = new JList<>(modelo);
        lista.setBorder(BorderFactory.createTitledBorder("Disciplinas:"));

        Runnable atualizarPeriodos = () -> {
            periodos.removeAllItems();
            Curso curso = (Curso) cursos.getSelectedItem();
            if (curso != null) {
                for (Periodo periodo : curso.getPeriodos()) {
                    periodos.addItem(periodo);
                }
            }
        };

        Runnable atualizarMaterias = () -> {
            modelo.clear();
            Periodo periodo = (Periodo) periodos.getSelectedItem();
            if (periodo != null) {
                for (Materia materia : periodo.getMaterias()) {
                    modelo.addElement(materia);
                }
            }
        };

        cursos.addActionListener(e -> {
            atualizarPeriodos.run();
            atualizarMaterias.run();
        });
        periodos.addActionListener(e -> atualizarMaterias.run());
        atualizarPeriodos.run();
        atualizarMaterias.run();

        filtros.add(new JLabel("Curso:"));
        filtros.add(cursos);
        filtros.add(new JLabel("Periodo:"));
        filtros.add(periodos);

        JPanel cadastro = new JPanel(new FlowLayout(FlowLayout.LEFT));
        cadastro.setBorder(BorderFactory.createTitledBorder("Cadastro"));
        JTextField nome = new JTextField(18);
        JComboBox<Professor> professores = new JComboBox<>(janela.getController().listarProfessores().toArray(new Professor[0]));
        JButton confirmar = janela.botao("Confirmar");
        JButton designarProfessor = janela.botao("Designar professor");
        JButton removerProfessor = janela.botao("Remover professor");
        JButton cancelar = janela.botao("Cancelar");
        JButton remover = janela.botao("Remover");

        cadastro.add(new JLabel("Nome da disciplina:"));
        cadastro.add(nome);
        cadastro.add(new JLabel("Professor:"));
        cadastro.add(professores);
        cadastro.add(confirmar);
        cadastro.add(designarProfessor);
        cadastro.add(removerProfessor);
        cadastro.add(cancelar);
        cadastro.add(remover);

        confirmar.addActionListener(e -> janela.tentar(() -> {
            janela.getController().criarMateria(janela.getUsuarioLogado(), (Periodo) periodos.getSelectedItem(), nome.getText(), (Professor) professores.getSelectedItem());
            atualizarMaterias.run();
            nome.setText("");
        }));

        designarProfessor.addActionListener(e -> janela.tentar(() -> {
            janela.getController().designarProfessorNaMateria(
                    janela.getUsuarioLogado(),
                    lista.getSelectedValue(),
                    (Professor) professores.getSelectedItem()
            );
            atualizarMaterias.run();
            lista.repaint();
        }));

        removerProfessor.addActionListener(e -> janela.tentar(() -> {
            janela.getController().removerProfessorDaMateria(
                    janela.getUsuarioLogado(),
                    lista.getSelectedValue()
            );
            atualizarMaterias.run();
            lista.repaint();
        }));

        cancelar.addActionListener(e -> nome.setText(""));
        remover.addActionListener(e -> janela.tentar(() -> {
            Periodo periodo = (Periodo) periodos.getSelectedItem();
            Materia materia = lista.getSelectedValue();
            if (periodo == null || materia == null) {
                throw new RuntimeException("Escolha uma disciplina.");
            }
            periodo.removerMateria(materia);
            atualizarMaterias.run();
        }));

        add(filtros, BorderLayout.NORTH);
        add(new JScrollPane(lista), BorderLayout.CENTER);
        add(cadastro, BorderLayout.SOUTH);
    }
}
