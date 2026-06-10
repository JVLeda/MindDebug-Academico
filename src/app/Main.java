package app;

import controller.MindDebugController;
import dao.BancoDao;
import dao.BancoDados;
import view.JanelaMindDebug;

import javax.swing.SwingUtilities;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            BancoDao bancoDao = new BancoDao();
            BancoDados banco = bancoDao.carregar();
            MindDebugController controller = new MindDebugController(banco);
            JanelaMindDebug janela = new JanelaMindDebug(controller);
            janela.setVisible(true);
        });
    }
}
