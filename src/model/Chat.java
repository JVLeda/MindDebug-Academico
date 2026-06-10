package model;

import java.util.ArrayList;

public class Chat extends Entidade {
    private String nome;
    private ArrayList<Mensagem> mensagens = new ArrayList<>();

    public Chat(String nome) {
        this.nome = nome;
    }

    public Mensagem enviar(Usuario autor, String texto) {
        Mensagem mensagem = new Mensagem(autor, texto);
        mensagens.add(mensagem);
        return mensagem;
    }

    public void apagar(Mensagem mensagem) {
        mensagens.remove(mensagem);
    }

    public ArrayList<Mensagem> getMensagens() {
        return mensagens;
    }

    @Override
    public String toString() {
        return nome;
    }
}
