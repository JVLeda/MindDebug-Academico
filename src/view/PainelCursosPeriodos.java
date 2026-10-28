package view;

import model.Curso;

import javax.swing.*;
import java.awt.*;

public class PainelCursosPeriodos extends JPanel {
    private JanelaMindDebug janela;

    public PainelCursosPeriodos(JanelaMindDebug janela) {
        this.janela = janela;
        setLayout(new BorderLayout(10, 10));
        montarTela();
    }

    private void montarTela() {
        JList<Curso> listaCursos = new JList<>(janela.modeloLista(janela.getController().listarCursos()));
        listaCursos.setBorder(BorderFactory.createTitledBorder("Cursos"));
        add(new JScrollPane(listaCursos), BorderLayout.CENTER);

        JPanel formularios = new JPanel(new GridLayout(1, 2, 10, 10));
        formularios.add(formularioNovoCurso());
        formularios.add(formularioNovoPeriodo());
        add(formularios, BorderLayout.SOUTH);
    }

    private JPanel formularioNovoCurso() {
        JPanel painel = new JPanel(new GridBagLayout());
        painel.setBorder(BorderFactory.createTitledBorder("Novo Curso"));
        GridBagConstraints gbc = janela.criarGbc();

        JTextField nome = new JTextField(12);
        JTextField sigla = new JTextField(8);
        JTextField area = new JTextField("Ciencias Exatas", 12);
        JTextField carga = new JTextField("3600", 8);
        JTextField coordenador = new JTextField(12);
        JButton confirmar = janela.botao("Confirmar");
        JButton limpar = janela.botao("Limpar");

        janela.adicionarLinha(painel, gbc, 0, "Nome:", nome);
        janela.adicionarLinha(painel, gbc, 1, "Sigla:", sigla);
        janela.adicionarLinha(painel, gbc, 2, "Area do conhecimento:", area);
        janela.adicionarLinha(painel, gbc, 3, "Carga horaria:", carga);
        janela.adicionarLinha(painel, gbc, 4, "Coordenador:", coordenador);
        janela.adicionarComponente(painel, gbc, 5, janela.painelBotoes(confirmar, limpar));

        confirmar.addActionListener(e -> janela.tentar(() -> {
            janela.getController().criarCurso(janela.getUsuarioLogado(), nome.getText(), sigla.getText(), area.getText(), Integer.parseInt(carga.getText()), coordenador.getText());
            janela.mostrarPainelAdministrador();
        }));
        limpar.addActionListener(e -> {
            nome.setText("");
            sigla.setText("");
            coordenador.setText("");
        });

        return painel;
    }

    private JPanel formularioNovoPeriodo() {
        JPanel painel = new JPanel(new GridBagLayout());
        painel.setBorder(BorderFactory.createTitledBorder("Novo Periodo"));
        GridBagConstraints gbc = janela.criarGbc();

        JComboBox<Curso> curso = new JComboBox<>(janela.getController().listarCursos().toArray(new Curso[0]));
        JTextField numero = new JTextField("1", 8);
        JTextField nome = new JTextField(12);
        JButton confirmar = janela.botao("Confirmar");
        JButton cancelar = janela.botao("Cancelar");

        janela.adicionarLinha(painel, gbc, 0, "Curso:", curso);
        janela.adicionarLinha(painel, gbc, 1, "Periodo:", numero);
        janela.adicionarLinha(painel, gbc, 2, "Nome:", nome);
        janela.adicionarComponente(painel, gbc, 3, janela.painelBotoes(confirmar, cancelar));

        confirmar.addActionListener(e -> janela.tentar(() -> {
            janela.getController().criarPeriodo(janela.getUsuarioLogado(), (Curso) curso.getSelectedItem(), Integer.parseInt(numero.getText()), nome.getText());
            janela.mostrarPainelAdministrador();
        }));
        cancelar.addActionListener(e -> nome.setText(""));

        return painel;
    }
}
