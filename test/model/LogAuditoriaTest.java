package model;

import util.Validador;

import java.util.Date;

public class LogAuditoriaTest {
    public static void main(String[] args) {
        testarRegistroNovo();
        testarRegistroRestaurado();
    }

    private static void testarRegistroNovo() {
        Administrador administrador = new Administrador("Administrador", "admin", "admin@teste.edu", "segredo1");
        Date antes = new Date();
        LogAuditoria log = new LogAuditoria(administrador, "Criar curso", "Curso EC");
        Date depois = new Date();

        verificar(log.getUsuario() == administrador, "O registro guarda quem fez a acao.");
        verificar("Criar curso".equals(log.getAcao()), "O registro guarda a acao.");
        verificar("Curso EC".equals(log.getDetalhes()), "O registro guarda os detalhes.");
        verificar(!log.getData().before(antes) && !log.getData().after(depois),
                "O registro guarda o momento em que foi criado.");
        verificar(log.getHora().length() == 5 && log.getHora().charAt(2) == ':', "A hora fica no formato HH:mm.");
    }

    private static void testarRegistroRestaurado() {
        Administrador administrador = new Administrador("Administrador", "admin", "admin@teste.edu", "segredo1");
        Date data = new Validador().data("17/11/2026", "Data");
        LogAuditoria log = new LogAuditoria(administrador, data, "08:30", "Criar curso", "Curso EC");

        verificar(log.getData() == data, "O registro restaurado guarda a data informada.");
        verificar("08:30".equals(log.getHora()), "O registro restaurado guarda a hora informada.");
        verificar("[17/11/2026 08:30] Administrador: Criar curso - Curso EC".equals(log.toString()),
                "O texto do registro mostra data, hora, usuario, acao e detalhes.");
    }

    private static void verificar(boolean condicao, String mensagem) {
        if (!condicao) {
            throw new AssertionError(mensagem);
        }
    }
}
