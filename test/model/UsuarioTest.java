package model;

public class UsuarioTest {
    public static void main(String[] args) {
        testarAutenticacao();
        testarUsuarioInativo();
        testarTrocaDeSenhaObrigatoria();
        testarPerfilDeCadaSubclasse();
        testarMatricula();
        testarDadosDoProfessor();
    }

    private static void testarAutenticacao() {
        Professor professor = new Professor("Professor", "prof", "prof@teste.edu", "segredo1");

        verificar(professor.autenticar("prof", "segredo1"), "Login e senha corretos devem autenticar.");
        verificar(professor.autenticar("PROF", "segredo1"), "O login nao diferencia maiusculas de minusculas.");
        verificar(!professor.autenticar("prof", "SEGREDO1"), "A senha diferencia maiusculas de minusculas.");
        verificar(!professor.autenticar("outro", "segredo1"), "Login errado nao autentica.");
    }

    private static void testarUsuarioInativo() {
        Monitor monitor = new Monitor("Monitor", "monitor", "monitor@teste.edu", "segredo1", "Monitor");
        verificar(monitor.isAtivo(), "Usuario novo nasce ativo.");

        monitor.setAtivo(false);
        verificar(!monitor.autenticar("monitor", "segredo1"),
                "Usuario inativo nao autentica, mesmo com a senha certa.");

        monitor.setAtivo(true);
        verificar(monitor.autenticar("monitor", "segredo1"), "Usuario reativado volta a autenticar.");
    }

    private static void testarTrocaDeSenhaObrigatoria() {
        Estudante aluno = new Estudante("Aluno", "aluno@teste.edu", "2026001", "segredo1", null);
        verificar(!aluno.isTrocaDeSenhaObrigatoria(), "Usuario novo nao nasce obrigado a trocar a senha.");

        aluno.redefinirSenha("provisoria1");
        verificar(aluno.isTrocaDeSenhaObrigatoria(),
                "Senha redefinida pelo administrador exige troca no proximo acesso.");
        verificar(aluno.autenticar("2026001", "provisoria1"), "A senha provisoria permite entrar para fazer a troca.");
        verificar(!aluno.autenticar("2026001", "segredo1"), "A senha antiga deixa de valer.");

        aluno.trocarSenha("definitiva1");
        verificar(!aluno.isTrocaDeSenhaObrigatoria(), "Trocar a senha desliga a exigencia.");
        verificar(aluno.autenticar("2026001", "definitiva1"), "A senha nova passa a valer.");

        aluno.setTrocaDeSenhaObrigatoria(true);
        verificar(aluno.isTrocaDeSenhaObrigatoria(), "A exigencia pode ser ligada ao criar a conta.");
    }

    private static void testarPerfilDeCadaSubclasse() {
        Usuario administrador = new Administrador("Administrador", "admin", "admin@teste.edu", "segredo1");
        Usuario professor = new Professor("Professor", "prof", "prof@teste.edu", "segredo1");
        Usuario monitor = new Monitor("Monitor", "monitor", "monitor@teste.edu", "segredo1", "Monitor");
        Usuario aluno = new Estudante("Aluno", "aluno@teste.edu", "2026001", "segredo1", null);

        verificar(administrador.getPerfil() == Perfil.ADMINISTRADOR, "Administrador informa o perfil ADMINISTRADOR.");
        verificar(professor.getPerfil() == Perfil.PROFESSOR, "Professor informa o perfil PROFESSOR.");
        verificar(monitor.getPerfil() == Perfil.MONITOR, "Monitor informa o perfil MONITOR.");
        verificar(aluno.getPerfil() == Perfil.ALUNO, "Estudante informa o perfil ALUNO.");
        verificar("Professor".equals(Perfil.PROFESSOR.getNome()), "O perfil tem um nome para aparecer na tela.");
        verificar(Perfil.values().length == 4, "O sistema tem quatro perfis.");
    }

    private static void testarMatricula() {
        Estudante aluno = new Estudante("Aluno", "aluno@teste.edu", "2026001", "segredo1", null);
        verificar("2026001".equals(aluno.getMatricula()), "A matricula do aluno vem do cadastro.");
        verificar(aluno.getMatricula().equals(aluno.getLogin()), "O aluno entra no sistema com a matricula.");

        Monitor monitor = new Monitor("Monitor", "monitor", "monitor@teste.edu", "segredo1", "Monitor");
        verificar(monitor.getMatricula() == null, "Monitor sem matricula informada fica sem matricula.");
        monitor.setMatricula("2025010");
        verificar("2025010".equals(monitor.getMatricula()), "Monitor guarda a matricula informada.");

        Administrador administrador = new Administrador("Administrador", "admin", "admin@teste.edu", "segredo1");
        verificar(administrador.getMatricula() == null, "Administrador pode nao ter matricula.");
    }

    private static void testarDadosDoProfessor() {
        Professor professor = new Professor("Professor", "prof", "prof@teste.edu", "segredo1");
        verificar("".equals(professor.getTitulacao()), "Titulacao nasce vazia.");
        verificar("".equals(professor.getAreaAtuacao()), "Area de atuacao nasce vazia.");

        professor.setMatricula("1234567");
        professor.setTitulacao("Doutorado");
        professor.setAreaAtuacao("Engenharia de Software");

        verificar("1234567".equals(professor.getMatricula()), "A matricula do professor guarda a matricula SIAPE.");
        verificar("Doutorado".equals(professor.getTitulacao()), "Professor guarda a titulacao.");
        verificar("Engenharia de Software".equals(professor.getAreaAtuacao()), "Professor guarda a area de atuacao.");
    }

    private static void verificar(boolean condicao, String mensagem) {
        if (!condicao) {
            throw new AssertionError(mensagem);
        }
    }
}
