package dao;

public class EsquemaBancoTest {
    public static void main(String[] args) {
        testarNomesEComandosAlinhados();
        testarOrdemDasChavesEstrangeiras();
        testarSemExtensaoDoMysql();
        testarTodaTabelaTemChavePrimaria();
        testarRestricoes();
        testarDadosFixos();
    }

    private static void testarNomesEComandosAlinhados() {
        String[] nomes = EsquemaBanco.nomesDasTabelas();
        String[] comandos = EsquemaBanco.comandosDeCriacao();

        verificar(nomes.length == 27, "O esquema deve ter 27 tabelas. Atual: " + nomes.length);
        verificar(nomes.length == comandos.length, "Cada tabela deve ter um comando de criacao.");
        for (int i = 0; i < nomes.length; i++) {
            verificar(comandos[i].startsWith("CREATE TABLE " + nomes[i] + " ("),
                    "O comando " + i + " deve criar a tabela " + nomes[i] + ".");
            for (int j = i + 1; j < nomes.length; j++) {
                verificar(!nomes[i].equals(nomes[j]), "Tabela repetida: " + nomes[i]);
            }
        }
    }

    private static void testarOrdemDasChavesEstrangeiras() {
        String[] nomes = EsquemaBanco.nomesDasTabelas();
        String[] comandos = EsquemaBanco.comandosDeCriacao();

        for (int i = 0; i < comandos.length; i++) {
            String comando = comandos[i];
            int posicao = comando.indexOf("REFERENCES ");
            while (posicao >= 0) {
                int inicio = posicao + "REFERENCES ".length();
                String referenciada = comando.substring(inicio, comando.indexOf('(', inicio));
                int indice = indiceDe(nomes, referenciada);
                verificar(indice >= 0, nomes[i] + " referencia uma tabela que nao existe: " + referenciada);
                verificar(indice < i, nomes[i] + " referencia " + referenciada + ", que precisa ser criada antes.");
                posicao = comando.indexOf("REFERENCES ", inicio);
            }
        }
    }

    private static int indiceDe(String[] nomes, String nome) {
        for (int i = 0; i < nomes.length; i++) {
            if (nomes[i].equals(nome)) {
                return i;
            }
        }
        return -1;
    }

    private static void testarSemExtensaoDoMysql() {
        String ddl = juntar(EsquemaBanco.comandosDeCriacao()) + juntar(EsquemaBanco.comandosDeDadosFixos());
        verificar(!ddl.toLowerCase().contains("if not exists"), "O esquema nao pode usar IF NOT EXISTS.");
        verificar(!ddl.toLowerCase().contains("if exists"), "O esquema nao pode usar IF EXISTS.");
    }

    private static void testarTodaTabelaTemChavePrimaria() {
        String[] nomes = EsquemaBanco.nomesDasTabelas();
        String[] comandos = EsquemaBanco.comandosDeCriacao();
        for (int i = 0; i < comandos.length; i++) {
            verificar(comandos[i].contains("PRIMARY KEY"), "A tabela " + nomes[i] + " precisa de chave primaria.");
        }
    }

    private static void testarRestricoes() {
        String ddl = juntar(EsquemaBanco.comandosDeCriacao()).toLowerCase();

        contem(ddl, "login varchar(50) not null unique", "Login deve ser unico.");
        contem(ddl, "email varchar(100) not null unique", "Email deve ser unico.");
        contem(ddl, "matricula varchar(50) unique", "Matricula deve ser unica.");
        contem(ddl, "sigla varchar(20) not null unique", "Sigla do curso deve ser unica.");
        contem(ddl, "foreign key (perfil_id) references perfis(id)", "Usuario deve ter um perfil cadastrado.");
        contem(ddl, "trocar_senha boolean not null default false", "Troca de senha obrigatoria deve existir.");
        contem(ddl, "ativo boolean not null default true", "Usuario deve ter status ativo.");
        contem(ddl, "create table estudantes (usuario_id int primary key", "Aluno deve ter tabela propria.");
        contem(ddl, "foreign key (professor_id) references professores(usuario_id)",
                "A materia so pode ter um professor que esteja na tabela de professores.");
        contem(ddl, "unique (periodo_id, nome)", "Uma disciplina nao pode se repetir no mesmo periodo.");
        contem(ddl, "unique (curso_id, numero)", "Um periodo nao pode se repetir no mesmo curso.");
        contem(ddl, "check (data_fim > data_inicio)", "O periodo deve terminar depois de comecar.");
        contem(ddl, "media_esperada decimal(4,2) not null default 7.00", "A media esperada padrao deve ser 7.");
        contem(ddl, "check (media_esperada between 0 and 10)", "A media esperada deve ficar entre 0 e 10.");
        contem(ddl, "unique (aula_id, estudante_id)", "Feedback deve ser unico por aluno e aula.");
        contem(ddl, "check (nota between 0 and 10)", "Nota deve respeitar a escala de 0 a 10.");
        contem(ddl, "foreign key (estudante_id) references estudantes(usuario_id)", "Feedback deve ser de um aluno.");
        contem(ddl, "primary key (materia_id, usuario_id)", "Participantes deve usar chave composta.");
        contem(ddl, "foreign key (chat_id) references chats(id)", "Mensagem deve pertencer a um chat cadastrado.");
        contem(ddl, "check (hora_fim > hora_inicio)", "O horario do monitor deve terminar depois de comecar.");
        contem(ddl, "unique (tentativa_id, pergunta_id)", "Resposta deve ser unica por tentativa e pergunta.");
        contem(ddl, "check (acertos >= 0 and acertos <= total_perguntas)", "Acertos nao podem passar do total.");
        verificar(!ddl.contains("origem_id"), "Mensagem nao deve mais usar origem e origem_id sem chave estrangeira.");
        verificar(!ddl.contains(" cargo "), "O perfil substitui a coluna cargo.");
    }

    private static void testarDadosFixos() {
        String[] dados = EsquemaBanco.comandosDeDadosFixos();
        int perfis = 0;
        int permissoes = 0;
        int padrao = 0;
        for (String comando : dados) {
            if (comando.startsWith("INSERT INTO perfis (")) {
                perfis++;
            } else if (comando.startsWith("INSERT INTO permissoes (")) {
                permissoes++;
            } else if (comando.startsWith("INSERT INTO perfis_permissoes (")) {
                padrao++;
            }
        }
        verificar(perfis == 4, "Devem ser inseridos 4 perfis. Atual: " + perfis);
        verificar(permissoes == 8, "Devem ser inseridas 8 permissoes. Atual: " + permissoes);
        verificar(padrao == 12, "A matriz padrao tem 12 permissoes concedidas. Atual: " + padrao);
        verificar(perfis + permissoes + padrao == dados.length, "So deve haver perfis e permissoes nos dados fixos.");
        verificar(dados[0].equals("INSERT INTO perfis (id, nome) VALUES (1, 'Administrador')"),
                "O primeiro perfil deve ser o administrador, com o id 1.");
    }

    private static String juntar(String[] comandos) {
        String texto = "";
        for (int i = 0; i < comandos.length; i++) {
            texto = texto + comandos[i] + "\n";
        }
        return texto;
    }

    private static void contem(String texto, String esperado, String mensagem) {
        verificar(texto.contains(esperado), mensagem + " Trecho ausente: " + esperado);
    }

    private static void verificar(boolean condicao, String mensagem) {
        if (!condicao) {
            throw new AssertionError(mensagem);
        }
    }
}
