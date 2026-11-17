package excecao;

import model.AlternativaQuiz;
import model.PerguntaQuiz;
import util.Validador;

public class ExcecoesTest {
    public static void main(String[] args) {
        testarSubclassesSaoTratadasPelaClasseBase();
        testarValidadorLancaDadoInvalido();
        testarRegraDoModeloLancaRegraDeNegocio();
    }

    private static void testarSubclassesSaoTratadasPelaClasseBase() {
        verificar("acesso".equals(mensagemCapturada(new AcessoNegadoException("acesso"))),
                "AcessoNegadoException deve ser tratada como MindDebugException.");
        verificar("regra".equals(mensagemCapturada(new RegraDeNegocioException("regra"))),
                "RegraDeNegocioException deve ser tratada como MindDebugException.");
        verificar("dado".equals(mensagemCapturada(new DadoInvalidoException("dado"))),
                "DadoInvalidoException deve ser tratada como MindDebugException.");
        verificar("banco".equals(mensagemCapturada(new PersistenciaException("banco"))),
                "PersistenciaException deve ser tratada como MindDebugException.");
    }

    private static String mensagemCapturada(MindDebugException excecao) {
        try {
            throw excecao;
        } catch (MindDebugException erro) {
            return erro.getMessage();
        }
    }

    private static void testarValidadorLancaDadoInvalido() {
        Validador validador = new Validador();
        boolean lancou = false;
        try {
            validador.texto("   ", "Nome");
        } catch (DadoInvalidoException erro) {
            lancou = true;
        }
        verificar(lancou, "Texto vazio deve lancar DadoInvalidoException.");

        lancou = false;
        try {
            validador.email("sem-arroba");
        } catch (DadoInvalidoException erro) {
            lancou = true;
        }
        verificar(lancou, "Email sem arroba deve lancar DadoInvalidoException.");
    }

    private static void testarRegraDoModeloLancaRegraDeNegocio() {
        PerguntaQuiz pergunta = new PerguntaQuiz("Qual e o indice inicial?", "Facil");
        pergunta.adicionarAlternativa(new AlternativaQuiz("0", "Correto.", true));

        boolean lancou = false;
        try {
            pergunta.adicionarAlternativa(new AlternativaQuiz("1", "Tambem correto.", true));
        } catch (RegraDeNegocioException erro) {
            lancou = true;
        }
        verificar(lancou, "Segunda alternativa correta deve lancar RegraDeNegocioException.");

        lancou = false;
        try {
            pergunta.adicionarAlternativa(null);
        } catch (DadoInvalidoException erro) {
            lancou = true;
        }
        verificar(lancou, "Alternativa nula deve lancar DadoInvalidoException.");
    }

    private static void verificar(boolean condicao, String mensagem) {
        if (!condicao) {
            throw new AssertionError(mensagem);
        }
    }
}
