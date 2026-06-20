package view;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class PainelDev extends JPanel {
    private JanelaMindDebug janela;
    private JPanel areaConteudo;
    private JLabel tituloConteudo;

    private JMenuItem itemCursos;
    private JMenuItem itemDisciplinas;
    private JMenuItem itemUsuarios;
    private JMenuItem itemSenhas;
    private JMenuItem itemAcessarMateria;
    private JMenuItem itemSalvar;
    private JMenuItem itemTrocarSenha;
    private JMenuItem itemSair;
    private JMenuItem itemSobre;

    public PainelDev(JanelaMindDebug janela) {
        this.janela = janela;
        setLayout(new BorderLayout(12, 12));
        setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));
        montarMenu();
        montarTela();
    }

    private void montarMenu() {
        JMenuBar barraMenu = new JMenuBar();
        JMenu menuCadastros = new JMenu("Cadastros");
        JMenu menuSistema = new JMenu("Sistema");
        JMenu menuAjuda = new JMenu("Ajuda");

        menuCadastros.setMnemonic('C');
        menuSistema.setMnemonic('S');
        menuAjuda.setMnemonic('A');

        itemCursos = item("Cursos e periodos", 'C');
        itemDisciplinas = item("Disciplinas", 'D');
        itemUsuarios = item("Usuarios", 'U');
        itemSenhas = item("Senhas", 'N');
        itemAcessarMateria = item("Acessar materia", 'M');
        itemSalvar = item("Salvar", 'V');
        itemTrocarSenha = item("Trocar minha senha", 'T');
        itemSair = item("Sair", 'R');
        itemSobre = item("Sobre", 'B');

        menuCadastros.add(itemCursos);
        menuCadastros.add(itemDisciplinas);
        menuCadastros.add(itemUsuarios);
        menuCadastros.add(itemSenhas);

        menuSistema.add(itemAcessarMateria);
        menuSistema.add(itemSalvar);
        menuSistema.add(itemTrocarSenha);
        menuSistema.addSeparator();
        menuSistema.add(itemSair);

        menuAjuda.add(itemSobre);

        barraMenu.add(menuCadastros);
        barraMenu.add(menuSistema);
        barraMenu.add(menuAjuda);
        janela.setJMenuBar(barraMenu);
    }

    private JMenuItem item(String texto, char atalho) {
        JMenuItem item = new JMenuItem(texto);
        item.setMnemonic(atalho);
        item.addActionListener(new GerenciadorMenu());
        return item;
    }

    private void montarTela() {
        add(janela.cabecalho(""), BorderLayout.NORTH);

        JPanel centro = new JPanel(new BorderLayout(8, 8));
        tituloConteudo = new JLabel();
        tituloConteudo.setFont(new Font("Arial", Font.BOLD, 18));
        areaConteudo = new JPanel(new BorderLayout());

        centro.add(tituloConteudo, BorderLayout.NORTH);
        centro.add(areaConteudo, BorderLayout.CENTER);
        add(centro, BorderLayout.CENTER);

        mostrarConteudo("Cursos e periodos", new PainelCursosPeriodos(janela));
    }

    private void mostrarConteudo(String titulo, JPanel painel) {
        tituloConteudo.setText(titulo);
        areaConteudo.removeAll();
        areaConteudo.add(painel, BorderLayout.CENTER);
        areaConteudo.revalidate();
        areaConteudo.repaint();
    }

    private class GerenciadorMenu implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent evento) {
            Object origem = evento.getSource();

            if (origem == itemCursos) {
                mostrarConteudo("Cursos e periodos", new PainelCursosPeriodos(janela));
            } else if (origem == itemDisciplinas) {
                mostrarConteudo("Gestao de disciplinas", new PainelDisciplinas(janela));
            } else if (origem == itemUsuarios) {
                mostrarConteudo("Gestao de usuarios", new PainelUsuarios(janela));
            } else if (origem == itemSenhas) {
                mostrarConteudo("Redefinicao de senhas", new ModuloSenhasDev(janela));
            } else if (origem == itemAcessarMateria) {
                janela.mostrarNavegacao();
            } else if (origem == itemSalvar) {
                janela.salvarDados();
            } else if (origem == itemTrocarSenha) {
                janela.trocarSenha();
            } else if (origem == itemSair) {
                janela.sair();
            } else if (origem == itemSobre) {
                janela.mostrarSobre();
            }
        }
    }
}
