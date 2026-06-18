package view;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class PainelMateria extends JPanel {
    private JanelaMindDebug janela;
    private JPanel areaConteudo;
    private JLabel tituloConteudo;

    private JMenuItem itemDashboard;
    private JMenuItem itemFeedback;
    private JMenuItem itemAulas;
    private JMenuItem itemMonitores;
    private JMenuItem itemGrupos;
    private JMenuItem itemMonitoria;
    private JMenuItem itemRepositorio;
    private JMenuItem itemChatEquipe;
    private JMenuItem itemChatGeral;
    private JMenuItem itemChatsAulas;
    private JMenuItem itemSalvar;
    private JMenuItem itemTrocarSenha;
    private JMenuItem itemVoltar;
    private JMenuItem itemLogoff;
    private JMenuItem itemSobre;

    public PainelMateria(JanelaMindDebug janela) {
        this.janela = janela;
        setLayout(new BorderLayout(12, 12));
        setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));
        montarMenu();
        montarTela();
    }

    private void montarMenu() {
        JMenuBar barraMenu = new JMenuBar();
        JMenu menuMateria = new JMenu("Materia");
        JMenu menuGestao = new JMenu("Gestao");
        JMenu menuChats = new JMenu("Chats");
        JMenu menuSistema = new JMenu("Sistema");
        JMenu menuAjuda = new JMenu("Ajuda");

        menuMateria.setMnemonic('M');
        menuGestao.setMnemonic('G');
        menuChats.setMnemonic('C');
        menuSistema.setMnemonic('S');
        menuAjuda.setMnemonic('A');

        if (janela.getUsuarioLogado().getNivel() >= 2) {
            itemDashboard = item("Dashboard", 'D');
            menuMateria.add(itemDashboard);
        } else {
            itemFeedback = item("Feedback de aulas", 'F');
            menuMateria.add(itemFeedback);
        }

        itemMonitoria = item("Monitoria", 'M');
        itemRepositorio = item("Repositorio", 'R');
        menuMateria.add(itemMonitoria);
        menuMateria.add(itemRepositorio);

        if (janela.getUsuarioLogado().getNivel() >= 3) {
            itemAulas = item("Aulas", 'A');
            itemMonitores = item("Monitores", 'M');
            itemGrupos = item("Grupos de estudo", 'G');
            menuGestao.add(itemAulas);
            menuGestao.add(itemMonitores);
            menuGestao.add(itemGrupos);
        }

        if (janela.getUsuarioLogado().getNivel() >= 2) {
            itemChatEquipe = item("Chat de equipe", 'E');
            menuChats.add(itemChatEquipe);
        }
        itemChatGeral = item("Chat geral", 'G');
        itemChatsAulas = item("Chats por aula", 'A');
        menuChats.add(itemChatGeral);
        menuChats.add(itemChatsAulas);

        itemSalvar = item("Salvar", 'V');
        itemTrocarSenha = item("Trocar senha", 'T');
        itemVoltar = item("Voltar aos cursos", 'C');
        itemLogoff = item("Logoff", 'L');
        itemSobre = item("Sobre", 'B');

        menuSistema.add(itemSalvar);
        menuSistema.add(itemTrocarSenha);
        menuSistema.add(itemVoltar);
        menuSistema.addSeparator();
        menuSistema.add(itemLogoff);
        menuAjuda.add(itemSobre);

        barraMenu.add(menuMateria);
        if (janela.getUsuarioLogado().getNivel() >= 3) {
            barraMenu.add(menuGestao);
        }
        barraMenu.add(menuChats);
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
        add(janela.cabecalho(janela.getMateriaAberta().getNome()), BorderLayout.NORTH);

        JPanel centro = new JPanel(new BorderLayout(8, 8));
        tituloConteudo = new JLabel();
        tituloConteudo.setFont(new Font("Arial", Font.BOLD, 18));
        areaConteudo = new JPanel(new BorderLayout());

        centro.add(tituloConteudo, BorderLayout.NORTH);
        centro.add(areaConteudo, BorderLayout.CENTER);
        add(centro, BorderLayout.CENTER);

        if (janela.getUsuarioLogado().getNivel() >= 2) {
            mostrarConteudo("Dashboard", new PainelDashboard(janela));
        } else {
            mostrarConteudo("Feedback de aulas", new PainelFeedback(janela));
        }
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

            if (origem == itemDashboard) {
                mostrarConteudo("Dashboard", new PainelDashboard(janela));
            } else if (origem == itemFeedback) {
                mostrarConteudo("Feedback de aulas", new PainelFeedback(janela));
            } else if (origem == itemAulas) {
                mostrarConteudo("Gestao de aulas", new PainelAulas(janela));
            } else if (origem == itemMonitores) {
                mostrarConteudo("Gestao de monitores", new PainelMonitores(janela));
            } else if (origem == itemGrupos) {
                mostrarConteudo("Grupos para trabalhos", new ModuloGruposEstudo(janela));
            } else if (origem == itemMonitoria) {
                mostrarConteudo("Monitoria", new ModuloMonitoria(janela));
            } else if (origem == itemRepositorio) {
                mostrarConteudo("Repositorio de arquivos", new PainelRepositorio(janela));
            } else if (origem == itemChatEquipe) {
                mostrarConteudo("Chat de equipe", new PainelChat(janela, "Chat de Equipe", janela.getMateriaAberta().getChatEquipe()));
            } else if (origem == itemChatGeral) {
                mostrarConteudo("Chat geral", new PainelChat(janela, "Chat geral", janela.getMateriaAberta().getChatGeral()));
            } else if (origem == itemChatsAulas) {
                mostrarConteudo("Chats por aula", new PainelChatsPorAula(janela));
            } else if (origem == itemSalvar) {
                janela.salvarDados();
            } else if (origem == itemTrocarSenha) {
                janela.trocarSenha();
            } else if (origem == itemVoltar) {
                janela.voltarAosCursos();
            } else if (origem == itemLogoff) {
                janela.sair();
            } else if (origem == itemSobre) {
                janela.mostrarSobre();
            }
        }
    }
}
