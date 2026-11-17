package controller.comum;

import controller.ControllerBase;
import dao.BancoDados;
import dao.MaterialDao;
import excecao.AcessoNegadoException;
import excecao.DadoInvalidoException;
import model.Materia;
import model.Material;
import model.Usuario;

/** Materiais de apoio da materia: arquivos e links. */
public class MaterialController extends ControllerBase {
    private MaterialDao materialDao = new MaterialDao();

    public MaterialController(BancoDados banco) {
        super(banco);
    }

    public void adicionarMaterial(Materia materia, Usuario autor, String titulo, String tipo, String referencia) {
        Material material = new Material(
                validador.texto(titulo, "Titulo"),
                validador.texto(tipo, "Tipo"),
                validador.texto(referencia, "Referencia"),
                autor
        );
        materialDao.inserir(materia, material);
        materia.adicionarMaterial(material);
    }

    public void removerMaterial(Materia materia, Usuario usuarioLogado, Material material) {
        if (material == null) {
            throw new DadoInvalidoException("Selecione um material.");
        }
        if (!usuarioLogado.podeApagarConteudoDe(material.getAutor())) {
            throw new AcessoNegadoException("Sem permissao para apagar este material.");
        }
        materialDao.remover(material);
        materia.getMateriais().remove(material);
    }
}
