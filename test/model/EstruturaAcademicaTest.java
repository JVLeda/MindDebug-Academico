package model;

import excecao.DadoInvalidoException;
import excecao.RegraDeNegocioException;
import util.Validador;

import java.util.Date;

public class EstruturaAcademicaTest {
    public static void main(String[] args) {
        testarNivelDoCurso();
        testarAnoESemestreDoPeriodo();
        testarDatasDoPeriodo();
        testarStatusDoPeriodo();
        testarCodigoECargaDaMateria();
    }

    private static void testarNivelDoCurso() {
        Curso curso = new Curso("Engenharia da Computacao", "EC", "Exatas", 3600, "Coordenacao");
        verificar(curso.getNivel() == NivelCurso.GRADUACAO, "Curso nasce como graduacao.");

        curso.setNivel(NivelCurso.POS_GRADUACAO);
        verificar(curso.getNivel() == NivelCurso.POS_GRADUACAO, "Curso guarda o nivel informado.");
        verificar("Pos-graduacao".equals(curso.getNivel().getNome()), "O nivel tem um nome para aparecer na tela.");
        verificar("EC - Engenharia da Computacao".equals(curso.toString()), "O texto do curso continua igual.");
    }

    private static void testarAnoESemestreDoPeriodo() {
        Periodo periodo = new Periodo(4, "Quarto Modulo");
        verificar("".equals(periodo.getAnoSemestre()), "Periodo sem ano informado nao mostra ano e semestre.");

        periodo.setAno(2026);
        periodo.setSemestre(2);
        verificar(periodo.getAno() == 2026, "Periodo guarda o ano.");
        verificar(periodo.getSemestre() == 2, "Periodo guarda o semestre.");
        verificar("2026.2".equals(periodo.getAnoSemestre()), "Ano e semestre aparecem juntos, como 2026.2.");
        verificar("4o Periodo".equals(periodo.toString()), "O texto do periodo continua igual.");
    }

    private static void testarDatasDoPeriodo() {
        Validador validador = new Validador();
        Date inicio = validador.data("10/08/2026", "Data de inicio");
        Date diaSeguinte = validador.data("11/08/2026", "Data de termino");
        Date fim = validador.data("17/12/2026", "Data de termino");
        Periodo periodo = new Periodo(4, "Quarto Modulo");
        verificar(periodo.getDataInicio() == null && periodo.getDataFim() == null, "Periodo nasce sem datas.");

        periodo.definirDatas(inicio, fim);
        verificar(periodo.getDataInicio() == inicio, "Periodo guarda a data de inicio.");
        verificar(periodo.getDataFim() == fim, "Periodo guarda a data de termino.");

        verificar(datasForamRecusadas(periodo, fim, inicio), "Termino antes do inicio deve ser recusado.");
        verificar(datasForamRecusadas(periodo, inicio, inicio), "Termino no mesmo dia do inicio deve ser recusado.");
        verificar(periodo.getDataInicio() == inicio && periodo.getDataFim() == fim,
                "Datas recusadas nao substituem as anteriores.");

        periodo.definirDatas(inicio, diaSeguinte);
        verificar(periodo.getDataFim() == diaSeguinte, "Termino no dia seguinte ao inicio e aceito.");

        boolean rejeitou = false;
        try {
            periodo.definirDatas(null, fim);
        } catch (DadoInvalidoException esperado) {
            rejeitou = true;
        }
        verificar(rejeitou, "Periodo sem data de inicio deve ser rejeitado.");
    }

    private static boolean datasForamRecusadas(Periodo periodo, Date inicio, Date fim) {
        try {
            periodo.definirDatas(inicio, fim);
            return false;
        } catch (RegraDeNegocioException esperado) {
            return true;
        }
    }

    private static void testarStatusDoPeriodo() {
        Periodo periodo = new Periodo(4, "Quarto Modulo");
        verificar(periodo.isAtivo(), "Periodo nasce ativo.");

        periodo.setAtivo(false);
        verificar(!periodo.isAtivo(), "Periodo pode ser desativado.");
    }

    private static void testarCodigoECargaDaMateria() {
        Professor professor = new Professor("Professor", "prof", "prof@teste.edu", "segredo1");
        Materia materia = new Materia("Banco de Dados", professor);
        verificar("".equals(materia.getCodigo()), "Materia nasce sem codigo informado.");
        verificar(materia.getCargaHoraria() == 0, "Materia nasce sem carga horaria informada.");

        materia.setCodigo("EC04003");
        materia.setCargaHoraria(60);
        verificar("EC04003".equals(materia.getCodigo()), "Materia guarda o codigo.");
        verificar(materia.getCargaHoraria() == 60, "Materia guarda a carga horaria.");
        verificar(materia.getProfessor() == professor, "O professor da materia continua o mesmo.");
        verificar("Banco de Dados".equals(materia.toString()), "O texto da materia continua igual.");
    }

    private static void verificar(boolean condicao, String mensagem) {
        if (!condicao) {
            throw new AssertionError(mensagem);
        }
    }
}
