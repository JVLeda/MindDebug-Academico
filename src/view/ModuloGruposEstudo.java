package view;

import model.Aula;
import model.Estudante;
import model.GrupoEstudo;
import model.Monitor;

import javax.swing.*;
import java.awt.*;

public class ModuloGruposEstudo extends JPanel {
    private JanelaMindDebug janela;
    private JList<GrupoEstudo> listaGrupos;
    private JList<Estudante> listaAlunos;
    private JList<Estudante> listaIntegrantes;
    private JList<Monitor> listaMonitores;
    private JList<Monitor> listaMonitoresGrupo;

    public ModuloGruposEstudo(JanelaMindDebug janela) {
        this.janela = janela;
        setLayout(new BorderLayout(10, 10));
        montarTela();
    }

    private void montarTela() {
        listaGrupos = new JList<>(janela.modeloLista(janela.getMateriaAberta().getGrupos()));
        listaAlunos = new JList<>(janela.modeloLista(janela.getMateriaAberta().getEstudantes()));
        listaIntegrantes = new JList<>();
        listaMonitores = new JList<>(janela.modeloLista(janela.getMateriaAberta().getMonitores()));
        listaMonitoresGrupo = new JList<>();

        JPanel pessoas = new JPanel(new GridLayout(2, 2, 10, 10));
        pessoas.add(painelLista("Alunos da materia", listaAlunos));
        pessoas.add(painelLista("Alunos do grupo", listaIntegrantes));
        pessoas.add(painelLista("Monitores da materia", listaMonitores));
        pessoas.add(painelLista("Monitores do grupo", listaMonitoresGrupo));

        JPanel listas = new JPanel(new BorderLayout(10, 10));
        JPanel painelGrupos = painelLista("Grupos", listaGrupos);
        painelGrupos.setPreferredSize(new Dimension(270, 0));
        listas.add(painelGrupos, BorderLayout.WEST);
        listas.add(pessoas, BorderLayout.CENTER);
        add(listas, BorderLayout.CENTER);

        listaGrupos.addListSelectionListener(e -> atualizarPessoasDoGrupo());

        JPanel rodape = new JPanel(new BorderLayout(8, 8));
        rodape.add(formularioNovoGrupo(), BorderLayout.CENTER);
        rodape.add(botoesDoGrupo(), BorderLayout.SOUTH);
        add(rodape, BorderLayout.SOUTH);
    }

    private JPanel formularioNovoGrupo() {
        JPanel formulario = new JPanel(new FlowLayout(FlowLayout.LEFT));
        formulario.setBorder(BorderFactory.createTitledBorder("Novo grupo para trabalho"));

        JTextField nome = new JTextField(14);
        JComboBox<Aula> aulas = new JComboBox<>();
        JComboBox<Monitor> monitores = new JComboBox<>();
        JButton criar = janela.botao("Criar grupo");

        aulas.addItem(null);
        for (Aula aula : janela.getMateriaAberta().getAulas()) {
            aulas.addItem(aula);
        }

        monitores.addItem(null);
        for (Monitor monitor : janela.getMateriaAberta().getMonitores()) {
            monitores.addItem(monitor);
        }

        formulario.add(new JLabel("Nome ou trabalho:"));
        formulario.add(nome);
        formulario.add(new JLabel("Aula opcional:"));
        formulario.add(aulas);
        formulario.add(new JLabel("Monitor opcional:"));
        formulario.add(monitores);
        formulario.add(criar);

        criar.addActionListener(e -> janela.tentar(() -> {
            GrupoEstudo grupo = janela.getController().criarGrupoEstudo(
                    janela.getUsuarioLogado(),
                    janela.getMateriaAberta(),
                    nome.getText(),
                    (Aula) aulas.getSelectedItem(),
                    (Monitor) monitores.getSelectedItem()
            );
            atualizarGrupos();
            listaGrupos.setSelectedValue(grupo, true);
            nome.setText("");
            JOptionPane.showMessageDialog(janela, "Grupo criado. Agora selecione os alunos.");
        }));

        return formulario;
    }

    private JPanel botoesDoGrupo() {
        JButton adicionarAluno = janela.botao("Adicionar aluno");
        JButton removerAluno = janela.botao("Remover aluno");
        JButton adicionarMonitor = janela.botao("Adicionar monitor");
        JButton removerMonitor = janela.botao("Remover monitor");
        JButton excluirGrupo = janela.botao("Excluir grupo");

        adicionarAluno.addActionListener(e -> janela.tentar(() -> {
            janela.getController().adicionarAlunoAoGrupo(
                    janela.getUsuarioLogado(),
                    janela.getMateriaAberta(),
                    listaGrupos.getSelectedValue(),
                    listaAlunos.getSelectedValue()
            );
            atualizarPessoasDoGrupo();
        }));

        removerAluno.addActionListener(e -> janela.tentar(() -> {
            janela.getController().removerAlunoDoGrupo(
                    janela.getUsuarioLogado(),
                    janela.getMateriaAberta(),
                    listaGrupos.getSelectedValue(),
                    listaIntegrantes.getSelectedValue()
            );
            atualizarPessoasDoGrupo();
        }));

        adicionarMonitor.addActionListener(e -> janela.tentar(() -> {
            janela.getController().adicionarMonitorAoGrupo(
                    janela.getUsuarioLogado(),
                    janela.getMateriaAberta(),
                    listaGrupos.getSelectedValue(),
                    listaMonitores.getSelectedValue()
            );
            atualizarPessoasDoGrupo();
        }));

        removerMonitor.addActionListener(e -> janela.tentar(() -> {
            janela.getController().removerMonitorDoGrupo(
                    janela.getUsuarioLogado(),
                    janela.getMateriaAberta(),
                    listaGrupos.getSelectedValue(),
                    listaMonitoresGrupo.getSelectedValue()
            );
            atualizarPessoasDoGrupo();
        }));

        excluirGrupo.addActionListener(e -> janela.tentar(() -> {
            janela.getController().removerGrupoEstudo(
                    janela.getUsuarioLogado(),
                    janela.getMateriaAberta(),
                    listaGrupos.getSelectedValue()
            );
            atualizarGrupos();
            atualizarPessoasDoGrupo();
        }));

        return janela.painelBotoes(excluirGrupo, removerMonitor, adicionarMonitor, removerAluno, adicionarAluno);
    }

    private JPanel painelLista(String titulo, JList<?> lista) {
        JPanel painel = new JPanel(new BorderLayout());
        painel.setBorder(BorderFactory.createTitledBorder(titulo));
        painel.add(new JScrollPane(lista), BorderLayout.CENTER);
        return painel;
    }

    private void atualizarGrupos() {
        listaGrupos.setModel(janela.modeloLista(janela.getMateriaAberta().getGrupos()));
    }

    private void atualizarPessoasDoGrupo() {
        GrupoEstudo grupo = listaGrupos.getSelectedValue();
        if (grupo == null) {
            listaIntegrantes.setModel(new DefaultListModel<>());
            listaMonitoresGrupo.setModel(new DefaultListModel<>());
        } else {
            listaIntegrantes.setModel(janela.modeloLista(grupo.getEstudantes()));
            listaMonitoresGrupo.setModel(janela.modeloLista(grupo.getMonitores()));
        }
    }
}
