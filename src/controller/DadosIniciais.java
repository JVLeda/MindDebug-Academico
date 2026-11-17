package controller;

import dao.BancoDao;
import dao.BancoDados;
import model.Administrador;
import model.Aula;
import model.Curso;
import model.Estudante;
import model.Feedback;
import model.Materia;
import model.Material;
import model.Mensagem;
import model.Monitor;
import model.Periodo;
import model.Professor;

/**
 * Dados de demonstracao gravados na primeira vez que o sistema abre, com o banco vazio:
 * um usuario de cada perfil, o curso de Engenharia da Computacao com a grade dos dez modulos
 * e uma materia com aulas, feedbacks, materiais e mensagens.
 * Tudo e montado em memoria e gravado numa unica transacao.
 */
public class DadosIniciais {

    /** Monta e grava os dados se o banco ainda nao tiver usuarios. Devolve verdadeiro se gravou. */
    public boolean gravarSeVazio(BancoDados banco) {
        if (!banco.getUsuarios().isEmpty()) {
            return false;
        }
        montar(banco);
        new BancoDao().gravarDadosIniciais(banco);
        return true;
    }

    /** So monta os objetos em memoria, com ids gerados pelo BancoDados. */
    public void montar(BancoDados banco) {
        Administrador administrador = new Administrador("Administrador do Sistema", "admin", "admin@minddebug.edu.br", "1234");
        Professor professor = new Professor("Professor 1", "prof", "prof@minddebug.edu.br", "1234");
        Monitor monitor = new Monitor("Monitor 1", "monitor", "monitor@minddebug.edu.br", "1234", "Monitor");
        banco.adicionarUsuario(administrador);
        banco.adicionarUsuario(professor);
        banco.adicionarUsuario(monitor);

        Curso curso = new Curso("Engenharia da Computacao", "EC", "Ciencias Exatas", 3600, "Coordenador 1");
        banco.adicionarCurso(curso);

        Periodo periodo = new Periodo(1, "Primeiro Modulo");
        periodo.setId(banco.gerarId());
        curso.adicionarPeriodo(periodo);

        Materia materia = new Materia("Algoritmos e Programacao", professor);
        materia.setId(banco.gerarId());
        materia.adicionarParticipante(monitor);
        periodo.adicionarMateria(materia);

        montarAulasDeDemonstracao(banco, curso, professor, materia);
        montarGrade(banco, curso, professor);
    }

    private void montarAulasDeDemonstracao(BancoDados banco, Curso curso, Professor professor, Materia materia) {
        Aula aula1 = new Aula("Aula 1", "16/03/2026", "Objetos e classes");
        Aula aula2 = new Aula("Aula 2", "23/03/2026", "Heranca e polimorfismo");
        Aula aula3 = new Aula("Aula 3", "30/03/2026", "Colecoes e excecoes");
        aula1.setId(banco.gerarId());
        aula2.setId(banco.gerarId());
        aula3.setId(banco.gerarId());
        materia.adicionarAula(aula1);
        materia.adicionarAula(aula2);
        materia.adicionarAula(aula3);

        Estudante aluno = new Estudante("Aluno 1", "aluno@aluno.edu.br", "2026001", "1234", curso);
        banco.adicionarUsuario(aluno);
        materia.adicionarParticipante(aluno);

        aula1.registrarFeedback(new Feedback(aluno, 8, "Entendi bem"));
        aula2.registrarFeedback(new Feedback(aluno, 5, "Preciso revisar"));

        Material material1 = new Material("Material1.pdf", "Arquivo", "Material1.pdf", professor);
        Material material2 = new Material("Video de apoio", "Link", "https://www.youtube.com/watch?v=dXZRgW-X2ls", professor);
        material1.setId(banco.gerarId());
        material2.setId(banco.gerarId());
        materia.adicionarMaterial(material1);
        materia.adicionarMaterial(material2);

        Mensagem mensagemGeral = materia.getChatGeral().enviar(professor, "Bem-vindos ao MindDebug.");
        Mensagem mensagemEquipe = materia.getChatEquipe().enviar(professor, "Monitores, acompanhem os feedbacks.");
        mensagemGeral.setId(banco.gerarId());
        mensagemEquipe.setId(banco.gerarId());
    }

    /** Grade dos dez modulos de Engenharia da Computacao. A materia que ja existe no periodo nao e repetida. */
    private void montarGrade(BancoDados banco, Curso curso, Professor professor) {
        String[][] grade = {
                {"1", "Primeiro Modulo", "Matematica Basica", "Algoritmos e Programacao", "Arquitetura de Computadores", "Eletronica Digital", "Metodologia Cientifica", "Introducao a Ciencia do Ambiente"},
                {"2", "Segundo Modulo", "Calculo I", "Algebra Linear", "Algebra Vetorial e Geometria Analitica", "Programacao Estruturada e Estrutura de Dados I", "Estatistica", "Direito e Legislacao", "Administracao e Empreendedorismo"},
                {"3", "Terceiro Modulo", "Calculo II", "Equacoes Diferenciais Ordinarias", "Programacao Orientada a Objetos", "Fisica Geral I", "Eletricidade e Magnetismo", "Calculo Numerico"},
                {"4", "Quarto Modulo", "Matematica Discreta e Teoria dos Grafos", "Probabilidade e Processos Estocasticos", "Banco de Dados", "Engenharia de Software", "Fisica Geral II", "Circuitos Eletricos"},
                {"5", "Quinto Modulo", "Sinais e Sistemas", "Programacao Para Dispositivos Moveis", "Microcontroladores e Microprocessadores", "Interacao Humano Computador", "Sistemas Operacionais", "Eletronica Analogica"},
                {"6", "Sexto Modulo", "Sistemas de Controle", "Teoria das Comunicacoes", "Estrutura de Dados II", "Sistemas Embarcados", "Redes de Computadores", "Processamento Digital de Sinais"},
                {"7", "Setimo Modulo", "Otimizacao de Sistemas", "Linguagens Formais e Automatos", "Comunicacoes Digitais", "Sistemas Distribuidos", "Avaliacao de Desempenho de Sistemas", "Computacao Grafica"},
                {"8", "Oitavo Modulo", "Optativa I", "Optativa II", "Projeto em Engenharia", "Seguranca de Sistemas Computacionais", "Automacao Industrial", "Inteligencia Artificial"},
                {"9", "Nono Modulo", "Optativa III", "Optativa IV", "PFC I", "Estagio Supervisionado"},
                {"10", "Decimo Modulo", "Atividades de Extensao Universitaria", "Atividades Curriculares Complementares", "PFC II"}
        };

        for (String[] dadosPeriodo : grade) {
            int numero = Integer.parseInt(dadosPeriodo[0]);
            Periodo periodo = buscarPeriodo(curso, numero);
            if (periodo == null) {
                periodo = new Periodo(numero, dadosPeriodo[1]);
                periodo.setId(banco.gerarId());
                curso.adicionarPeriodo(periodo);
            }

            for (int i = 2; i < dadosPeriodo.length; i++) {
                if (!temMateria(periodo, dadosPeriodo[i])) {
                    Materia materia = new Materia(dadosPeriodo[i], professor);
                    materia.setId(banco.gerarId());
                    periodo.adicionarMateria(materia);
                }
            }
        }
    }

    private Periodo buscarPeriodo(Curso curso, int numero) {
        for (Periodo periodo : curso.getPeriodos()) {
            if (periodo.getNumero() == numero) {
                return periodo;
            }
        }
        return null;
    }

    private boolean temMateria(Periodo periodo, String nome) {
        for (Materia materia : periodo.getMaterias()) {
            if (materia.getNome().equalsIgnoreCase(nome)) {
                return true;
            }
        }
        return false;
    }
}
