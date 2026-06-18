package view;

import model.Chat;
import model.Mensagem;

import javax.swing.*;
import java.awt.*;

public class PainelChat extends JPanel {
    private JanelaMindDebug janela;
    private Chat chat;
    private String titulo;

    public PainelChat(JanelaMindDebug janela, String titulo, Chat chat) {
        this.janela = janela;
        this.titulo = titulo;
        this.chat = chat;
        setLayout(new BorderLayout(10, 10));
        montarTela();
    }

    private void montarTela() {
        if (titulo != null && !titulo.isEmpty()) {
            JLabel label = new JLabel(titulo);
            label.setFont(new Font("Arial", Font.BOLD, 18));
            add(label, BorderLayout.NORTH);
        }

        JList<Mensagem> lista = new JList<>(janela.modeloLista(chat.getMensagens()));
        add(new JScrollPane(lista), BorderLayout.CENTER);

        JPanel rodape = new JPanel(new BorderLayout(8, 8));
        JTextField texto = new JTextField();
        JButton enviar = janela.botao("Enviar");
        JButton apagar = janela.botao("Apagar mensagem");
        JPanel botoes = janela.painelBotoes(apagar, enviar);

        rodape.add(texto, BorderLayout.CENTER);
        rodape.add(botoes, BorderLayout.EAST);

        enviar.addActionListener(e -> janela.tentar(() -> {
            janela.getController().enviarMensagem(chat, janela.getUsuarioLogado(), texto.getText());
            janela.mostrarMateria();
        }));

        apagar.addActionListener(e -> janela.tentar(() -> {
            janela.getController().apagarMensagem(chat, janela.getUsuarioLogado(), lista.getSelectedValue());
            janela.mostrarMateria();
        }));

        add(rodape, BorderLayout.SOUTH);
    }
}
