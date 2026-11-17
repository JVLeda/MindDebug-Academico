package dao;

import model.MatrizPermissoes;
import model.Perfil;
import model.Permissao;

import java.util.ArrayList;

/**
 * Esquema do banco do MindDebug.
 * As tabelas estao na ordem de criacao: cada uma so referencia tabelas que vem antes dela.
 * A coluna id das tabelas de dados usa AUTO_INCREMENT; perfis e permissoes tem ids fixos,
 * iguais aos codigos dos enums Perfil e Permissao.
 */
public final class EsquemaBanco {
    private static final String[][] TABELAS = {
        {"perfis", "CREATE TABLE perfis (id INT PRIMARY KEY, nome VARCHAR(30) NOT NULL UNIQUE)"},
        {"permissoes", "CREATE TABLE permissoes (id INT PRIMARY KEY, modulo VARCHAR(50) NOT NULL, acao VARCHAR(30) NOT NULL, descricao VARCHAR(200) NOT NULL, UNIQUE (modulo, acao))"},
        {"perfis_permissoes", "CREATE TABLE perfis_permissoes (perfil_id INT NOT NULL, permissao_id INT NOT NULL, PRIMARY KEY (perfil_id, permissao_id), FOREIGN KEY (perfil_id) REFERENCES perfis(id), FOREIGN KEY (permissao_id) REFERENCES permissoes(id))"},
        {"cursos", "CREATE TABLE cursos (id INT PRIMARY KEY AUTO_INCREMENT, nome VARCHAR(100) NOT NULL, sigla VARCHAR(20) NOT NULL UNIQUE, nivel VARCHAR(20) NOT NULL DEFAULT 'Graduacao', area VARCHAR(100) NOT NULL, carga_horaria INT NOT NULL, coordenador VARCHAR(100) NOT NULL, CHECK (nivel = 'Graduacao' OR nivel = 'Pos-graduacao'), CHECK (carga_horaria > 0))"},
        {"usuarios", "CREATE TABLE usuarios (id INT PRIMARY KEY AUTO_INCREMENT, nome VARCHAR(100) NOT NULL, login VARCHAR(50) NOT NULL UNIQUE, email VARCHAR(100) NOT NULL UNIQUE, senha VARCHAR(50) NOT NULL, matricula VARCHAR(50) UNIQUE, perfil_id INT NOT NULL, ativo BOOLEAN NOT NULL DEFAULT TRUE, trocar_senha BOOLEAN NOT NULL DEFAULT FALSE, FOREIGN KEY (perfil_id) REFERENCES perfis(id))"},
        {"estudantes", "CREATE TABLE estudantes (usuario_id INT PRIMARY KEY, curso_id INT, FOREIGN KEY (usuario_id) REFERENCES usuarios(id), FOREIGN KEY (curso_id) REFERENCES cursos(id))"},
        {"professores", "CREATE TABLE professores (usuario_id INT PRIMARY KEY, titulacao VARCHAR(50), area_atuacao VARCHAR(100), FOREIGN KEY (usuario_id) REFERENCES usuarios(id))"},
        {"monitores", "CREATE TABLE monitores (usuario_id INT PRIMARY KEY, tipo VARCHAR(30) NOT NULL, FOREIGN KEY (usuario_id) REFERENCES usuarios(id))"},
        {"periodos", "CREATE TABLE periodos (id INT PRIMARY KEY AUTO_INCREMENT, curso_id INT NOT NULL, numero INT NOT NULL, nome VARCHAR(100) NOT NULL, ano INT, semestre INT, data_inicio DATE, data_fim DATE, ativo BOOLEAN NOT NULL DEFAULT TRUE, UNIQUE (curso_id, numero), CHECK (numero > 0), CHECK (semestre = 1 OR semestre = 2), CHECK (data_fim > data_inicio), FOREIGN KEY (curso_id) REFERENCES cursos(id))"},
        {"materias", "CREATE TABLE materias (id INT PRIMARY KEY AUTO_INCREMENT, periodo_id INT NOT NULL, nome VARCHAR(100) NOT NULL, codigo VARCHAR(20) UNIQUE, carga_horaria INT, professor_id INT, UNIQUE (periodo_id, nome), CHECK (carga_horaria > 0), FOREIGN KEY (periodo_id) REFERENCES periodos(id), FOREIGN KEY (professor_id) REFERENCES professores(usuario_id))"},
        {"participantes", "CREATE TABLE participantes (materia_id INT NOT NULL, usuario_id INT NOT NULL, PRIMARY KEY (materia_id, usuario_id), FOREIGN KEY (materia_id) REFERENCES materias(id), FOREIGN KEY (usuario_id) REFERENCES usuarios(id))"},
        {"aulas", "CREATE TABLE aulas (id INT PRIMARY KEY AUTO_INCREMENT, materia_id INT NOT NULL, titulo VARCHAR(100) NOT NULL, data_aula DATE, descricao VARCHAR(500), media_esperada DECIMAL(4,2) NOT NULL DEFAULT 7.00, CHECK (media_esperada BETWEEN 0 AND 10), FOREIGN KEY (materia_id) REFERENCES materias(id))"},
        {"horarios_monitor", "CREATE TABLE horarios_monitor (id INT PRIMARY KEY AUTO_INCREMENT, monitor_id INT NOT NULL, materia_id INT NOT NULL, dia VARCHAR(10) NOT NULL, hora_inicio VARCHAR(5) NOT NULL, hora_fim VARCHAR(5) NOT NULL, CHECK (dia = 'Segunda' OR dia = 'Terca' OR dia = 'Quarta' OR dia = 'Quinta' OR dia = 'Sexta' OR dia = 'Sabado'), CHECK (hora_fim > hora_inicio), FOREIGN KEY (monitor_id) REFERENCES monitores(usuario_id), FOREIGN KEY (materia_id) REFERENCES materias(id))"},
        {"feedbacks", "CREATE TABLE feedbacks (id INT PRIMARY KEY AUTO_INCREMENT, aula_id INT NOT NULL, estudante_id INT NOT NULL, nota INT NOT NULL, comentario VARCHAR(200), UNIQUE (aula_id, estudante_id), CHECK (nota BETWEEN 0 AND 10), FOREIGN KEY (aula_id) REFERENCES aulas(id), FOREIGN KEY (estudante_id) REFERENCES estudantes(usuario_id))"},
        {"materiais", "CREATE TABLE materiais (id INT PRIMARY KEY AUTO_INCREMENT, materia_id INT NOT NULL, titulo VARCHAR(100) NOT NULL, tipo VARCHAR(30) NOT NULL, referencia VARCHAR(300) NOT NULL, autor_id INT NOT NULL, FOREIGN KEY (materia_id) REFERENCES materias(id), FOREIGN KEY (autor_id) REFERENCES usuarios(id))"},
        {"chats", "CREATE TABLE chats (id INT PRIMARY KEY AUTO_INCREMENT, tipo VARCHAR(10) NOT NULL, materia_id INT, aula_id INT UNIQUE, UNIQUE (materia_id, tipo), CHECK (((tipo = 'GERAL' OR tipo = 'EQUIPE') AND materia_id IS NOT NULL AND aula_id IS NULL) OR (tipo = 'AULA' AND aula_id IS NOT NULL AND materia_id IS NULL)), FOREIGN KEY (materia_id) REFERENCES materias(id), FOREIGN KEY (aula_id) REFERENCES aulas(id))"},
        {"mensagens", "CREATE TABLE mensagens (id INT PRIMARY KEY AUTO_INCREMENT, chat_id INT NOT NULL, autor_id INT NOT NULL, texto VARCHAR(500) NOT NULL, data_envio DATE NOT NULL, hora_envio VARCHAR(5) NOT NULL, FOREIGN KEY (chat_id) REFERENCES chats(id), FOREIGN KEY (autor_id) REFERENCES usuarios(id))"},
        {"grupos_estudo", "CREATE TABLE grupos_estudo (id INT PRIMARY KEY AUTO_INCREMENT, materia_id INT NOT NULL, nome VARCHAR(100) NOT NULL, aula_id INT, UNIQUE (materia_id, nome), FOREIGN KEY (materia_id) REFERENCES materias(id), FOREIGN KEY (aula_id) REFERENCES aulas(id))"},
        {"grupos_estudo_alunos", "CREATE TABLE grupos_estudo_alunos (grupo_id INT NOT NULL, estudante_id INT NOT NULL, PRIMARY KEY (grupo_id, estudante_id), FOREIGN KEY (grupo_id) REFERENCES grupos_estudo(id), FOREIGN KEY (estudante_id) REFERENCES estudantes(usuario_id))"},
        {"grupos_estudo_monitores", "CREATE TABLE grupos_estudo_monitores (grupo_id INT NOT NULL, monitor_id INT NOT NULL, PRIMARY KEY (grupo_id, monitor_id), FOREIGN KEY (grupo_id) REFERENCES grupos_estudo(id), FOREIGN KEY (monitor_id) REFERENCES monitores(usuario_id))"},
        {"pedidos_monitoria", "CREATE TABLE pedidos_monitoria (id INT PRIMARY KEY AUTO_INCREMENT, materia_id INT NOT NULL, estudante_id INT NOT NULL, aula_id INT, monitor_id INT, duvida VARCHAR(300) NOT NULL, status VARCHAR(20) NOT NULL DEFAULT 'Solicitado', data_monitoria DATE, horario VARCHAR(5), CHECK (status = 'Solicitado' OR status = 'Aceito' OR status = 'Recusado'), FOREIGN KEY (materia_id) REFERENCES materias(id), FOREIGN KEY (estudante_id) REFERENCES estudantes(usuario_id), FOREIGN KEY (aula_id) REFERENCES aulas(id), FOREIGN KEY (monitor_id) REFERENCES monitores(usuario_id))"},
        {"log_auditoria", "CREATE TABLE log_auditoria (id INT PRIMARY KEY AUTO_INCREMENT, usuario_id INT NOT NULL, data_registro DATE NOT NULL, hora_registro VARCHAR(5) NOT NULL, acao VARCHAR(100) NOT NULL, detalhes VARCHAR(500), FOREIGN KEY (usuario_id) REFERENCES usuarios(id))"},
        {"quizzes", "CREATE TABLE quizzes (id INT PRIMARY KEY AUTO_INCREMENT, aula_id INT NOT NULL UNIQUE, titulo VARCHAR(100) NOT NULL, descricao VARCHAR(300), FOREIGN KEY (aula_id) REFERENCES aulas(id))"},
        {"perguntas_quiz", "CREATE TABLE perguntas_quiz (id INT PRIMARY KEY AUTO_INCREMENT, quiz_id INT NOT NULL, enunciado VARCHAR(500) NOT NULL, dificuldade VARCHAR(20) NOT NULL, FOREIGN KEY (quiz_id) REFERENCES quizzes(id))"},
        {"alternativas_quiz", "CREATE TABLE alternativas_quiz (id INT PRIMARY KEY AUTO_INCREMENT, pergunta_id INT NOT NULL, texto VARCHAR(255) NOT NULL, explicacao VARCHAR(500), correta BOOLEAN NOT NULL, FOREIGN KEY (pergunta_id) REFERENCES perguntas_quiz(id))"},
        {"tentativas_quiz", "CREATE TABLE tentativas_quiz (id INT PRIMARY KEY AUTO_INCREMENT, quiz_id INT NOT NULL, estudante_id INT NOT NULL, data_tentativa DATE NOT NULL, hora_tentativa VARCHAR(5) NOT NULL, acertos INT NOT NULL, total_perguntas INT NOT NULL, CHECK (acertos >= 0 AND acertos <= total_perguntas), FOREIGN KEY (quiz_id) REFERENCES quizzes(id), FOREIGN KEY (estudante_id) REFERENCES estudantes(usuario_id))"},
        {"respostas_quiz", "CREATE TABLE respostas_quiz (id INT PRIMARY KEY AUTO_INCREMENT, tentativa_id INT NOT NULL, pergunta_id INT NOT NULL, alternativa_id INT NOT NULL, acertou BOOLEAN NOT NULL, UNIQUE (tentativa_id, pergunta_id), FOREIGN KEY (tentativa_id) REFERENCES tentativas_quiz(id), FOREIGN KEY (pergunta_id) REFERENCES perguntas_quiz(id), FOREIGN KEY (alternativa_id) REFERENCES alternativas_quiz(id))"}
    };

    private EsquemaBanco() {
    }

    public static String[] nomesDasTabelas() {
        String[] nomes = new String[TABELAS.length];
        for (int i = 0; i < TABELAS.length; i++) {
            nomes[i] = TABELAS[i][0];
        }
        return nomes;
    }

    public static String[] comandosDeCriacao() {
        String[] comandos = new String[TABELAS.length];
        for (int i = 0; i < TABELAS.length; i++) {
            comandos[i] = TABELAS[i][1];
        }
        return comandos;
    }

    /**
     * Dados que o sistema precisa para funcionar: os perfis, as permissoes
     * e as permissoes padrao de cada perfil.
     */
    public static String[] comandosDeDadosFixos() {
        ArrayList<String> comandos = new ArrayList<>();

        for (Perfil perfil : Perfil.values()) {
            comandos.add("INSERT INTO perfis (id, nome) VALUES (" + perfil.getCodigo() + ", '" + perfil.getNome() + "')");
        }

        for (Permissao permissao : Permissao.values()) {
            comandos.add("INSERT INTO permissoes (id, modulo, acao, descricao) VALUES (" + permissao.getCodigo() + ", '"
                    + permissao.getModulo() + "', '" + permissao.getAcao() + "', '" + permissao.getDescricao() + "')");
        }

        MatrizPermissoes padrao = new MatrizPermissoes();
        for (Perfil perfil : Perfil.values()) {
            for (Permissao permissao : padrao.listar(perfil)) {
                comandos.add("INSERT INTO perfis_permissoes (perfil_id, permissao_id) VALUES ("
                        + perfil.getCodigo() + ", " + permissao.getCodigo() + ")");
            }
        }

        return comandos.toArray(new String[0]);
    }
}
