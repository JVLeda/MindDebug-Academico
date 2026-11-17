package controller.monitor;

import controller.ControllerBase;
import dao.BancoDados;
import dao.PedidoMonitoriaDao;
import excecao.AcessoNegadoException;
import excecao.DadoInvalidoException;
import excecao.RegraDeNegocioException;
import model.Aula;
import model.Materia;
import model.Monitor;
import model.PedidoMonitoria;
import model.Usuario;

/**
 * Pedidos de monitoria, modulo do Monitor: o aluno solicita, o monitor (ou o professor da materia)
 * aceita ou recusa.
 */
public class SolicitacaoController extends ControllerBase {
    private PedidoMonitoriaDao pedidoMonitoriaDao = new PedidoMonitoriaDao();

    public SolicitacaoController(BancoDados banco) {
        super(banco);
    }

    public PedidoMonitoria solicitarMonitoria(Usuario usuarioLogado, Materia materia, Aula aula, Monitor monitor, String duvida, String data, String horario) {
        if (usuarioLogado == null || !usuarioLogado.podeSolicitarMonitoria()) {
            throw new AcessoNegadoException("Somente aluno solicita monitoria.");
        }
        if (materia == null) {
            throw new DadoInvalidoException("Materia invalida.");
        }
        if (aula == null) {
            throw new DadoInvalidoException("Selecione uma aula.");
        }
        if (monitor == null) {
            throw new DadoInvalidoException("Selecione um monitor.");
        }
        if (!materia.getAulas().contains(aula)) {
            throw new RegraDeNegocioException("A aula escolhida nao pertence a esta materia.");
        }
        if (!materia.getMonitores().contains(monitor)) {
            throw new RegraDeNegocioException("O monitor escolhido nao esta cadastrado nesta materia.");
        }

        PedidoMonitoria pedido = usuarioLogado.criarPedidoMonitoria(
                aula,
                monitor,
                validador.texto(duvida, "Duvida"),
                validador.textoDeData(data, "Data"),
                validador.hora(horario, "Horario")
        );
        pedidoMonitoriaDao.inserir(materia, pedido);
        materia.adicionarPedidoMonitoria(pedido);
        return pedido;
    }

    public void aceitarMonitoria(Usuario usuarioLogado, PedidoMonitoria pedido, Monitor monitor) {
        exigirAtendimentoDeMonitoria(usuarioLogado, "aceitar monitoria");
        if (pedido == null) {
            throw new DadoInvalidoException("Selecione um pedido.");
        }
        if ("Aceito".equals(pedido.getStatus())) {
            throw new RegraDeNegocioException("Este pedido ja foi aceito.");
        }
        monitor = usuarioLogado.escolherMonitorDoAtendimento(monitor);
        if (monitor == null) {
            throw new DadoInvalidoException("Selecione um monitor.");
        }
        pedidoMonitoriaDao.atualizarStatus(pedido, "Aceito", monitor);
        pedido.aceitar(monitor);
    }

    public void recusarMonitoria(Usuario usuarioLogado, PedidoMonitoria pedido) {
        exigirAtendimentoDeMonitoria(usuarioLogado, "recusar monitoria");
        if (pedido == null) {
            throw new DadoInvalidoException("Selecione um pedido.");
        }
        if (!usuarioLogado.podeRecusarPedido(pedido)) {
            throw new AcessoNegadoException("Monitor so pode recusar pedidos direcionados a ele.");
        }
        pedidoMonitoriaDao.atualizarStatus(pedido, "Recusado", pedido.getMonitor());
        pedido.recusar();
    }

    private void exigirAtendimentoDeMonitoria(Usuario usuario, String acao) {
        if (usuario == null || !usuario.podeAtenderMonitoria()) {
            negarAcesso(acao);
        }
    }
}
