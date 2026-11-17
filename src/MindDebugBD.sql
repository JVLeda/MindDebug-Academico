-- Banco de dados do MindDebug
-- Mesmo esquema de src/dao/EsquemaBanco.java (o teste ScriptBancoTest confere).
-- Execute uma vez, com o banco ainda vazio. O sistema tambem cria as tabelas sozinho.

CREATE DATABASE minddebug;
USE minddebug;

-- Tabelas, na ordem em que precisam ser criadas

CREATE TABLE perfis (
    id INT PRIMARY KEY,
    nome VARCHAR(30) NOT NULL UNIQUE
);

CREATE TABLE permissoes (
    id INT PRIMARY KEY,
    modulo VARCHAR(50) NOT NULL,
    acao VARCHAR(30) NOT NULL,
    descricao VARCHAR(200) NOT NULL,
    UNIQUE (modulo, acao)
);

CREATE TABLE perfis_permissoes (
    perfil_id INT NOT NULL,
    permissao_id INT NOT NULL,
    PRIMARY KEY (perfil_id, permissao_id),
    FOREIGN KEY (perfil_id) REFERENCES perfis(id),
    FOREIGN KEY (permissao_id) REFERENCES permissoes(id)
);

CREATE TABLE cursos (
    id INT PRIMARY KEY AUTO_INCREMENT,
    nome VARCHAR(100) NOT NULL,
    sigla VARCHAR(20) NOT NULL UNIQUE,
    nivel VARCHAR(20) NOT NULL DEFAULT 'Graduacao',
    area VARCHAR(100) NOT NULL,
    carga_horaria INT NOT NULL,
    coordenador VARCHAR(100) NOT NULL,
    CHECK (nivel = 'Graduacao' OR nivel = 'Pos-graduacao'),
    CHECK (carga_horaria > 0)
);

CREATE TABLE usuarios (
    id INT PRIMARY KEY AUTO_INCREMENT,
    nome VARCHAR(100) NOT NULL,
    login VARCHAR(50) NOT NULL UNIQUE,
    email VARCHAR(100) NOT NULL UNIQUE,
    senha VARCHAR(50) NOT NULL,
    matricula VARCHAR(50) UNIQUE,
    perfil_id INT NOT NULL,
    ativo BOOLEAN NOT NULL DEFAULT TRUE,
    trocar_senha BOOLEAN NOT NULL DEFAULT FALSE,
    FOREIGN KEY (perfil_id) REFERENCES perfis(id)
);

CREATE TABLE estudantes (
    usuario_id INT PRIMARY KEY,
    curso_id INT,
    FOREIGN KEY (usuario_id) REFERENCES usuarios(id),
    FOREIGN KEY (curso_id) REFERENCES cursos(id)
);

CREATE TABLE professores (
    usuario_id INT PRIMARY KEY,
    titulacao VARCHAR(50),
    area_atuacao VARCHAR(100),
    FOREIGN KEY (usuario_id) REFERENCES usuarios(id)
);

CREATE TABLE monitores (
    usuario_id INT PRIMARY KEY,
    tipo VARCHAR(30) NOT NULL,
    FOREIGN KEY (usuario_id) REFERENCES usuarios(id)
);

CREATE TABLE periodos (
    id INT PRIMARY KEY AUTO_INCREMENT,
    curso_id INT NOT NULL,
    numero INT NOT NULL,
    nome VARCHAR(100) NOT NULL,
    ano INT,
    semestre INT,
    data_inicio DATE,
    data_fim DATE,
    ativo BOOLEAN NOT NULL DEFAULT TRUE,
    UNIQUE (curso_id, numero),
    CHECK (numero > 0),
    CHECK (semestre = 1 OR semestre = 2),
    CHECK (data_fim > data_inicio),
    FOREIGN KEY (curso_id) REFERENCES cursos(id)
);

CREATE TABLE materias (
    id INT PRIMARY KEY AUTO_INCREMENT,
    periodo_id INT NOT NULL,
    nome VARCHAR(100) NOT NULL,
    codigo VARCHAR(20) UNIQUE,
    carga_horaria INT,
    professor_id INT,
    UNIQUE (periodo_id, nome),
    CHECK (carga_horaria > 0),
    FOREIGN KEY (periodo_id) REFERENCES periodos(id),
    FOREIGN KEY (professor_id) REFERENCES professores(usuario_id)
);

CREATE TABLE participantes (
    materia_id INT NOT NULL,
    usuario_id INT NOT NULL,
    PRIMARY KEY (materia_id, usuario_id),
    FOREIGN KEY (materia_id) REFERENCES materias(id),
    FOREIGN KEY (usuario_id) REFERENCES usuarios(id)
);

CREATE TABLE aulas (
    id INT PRIMARY KEY AUTO_INCREMENT,
    materia_id INT NOT NULL,
    titulo VARCHAR(100) NOT NULL,
    data_aula DATE,
    descricao VARCHAR(500),
    media_esperada DECIMAL(4,2) NOT NULL DEFAULT 7.00,
    CHECK (media_esperada BETWEEN 0 AND 10),
    FOREIGN KEY (materia_id) REFERENCES materias(id)
);

CREATE TABLE horarios_monitor (
    id INT PRIMARY KEY AUTO_INCREMENT,
    monitor_id INT NOT NULL,
    materia_id INT NOT NULL,
    dia VARCHAR(10) NOT NULL,
    hora_inicio VARCHAR(5) NOT NULL,
    hora_fim VARCHAR(5) NOT NULL,
    CHECK (dia = 'Segunda' OR dia = 'Terca' OR dia = 'Quarta' OR dia = 'Quinta' OR dia = 'Sexta' OR dia = 'Sabado'),
    CHECK (hora_fim > hora_inicio),
    FOREIGN KEY (monitor_id) REFERENCES monitores(usuario_id),
    FOREIGN KEY (materia_id) REFERENCES materias(id)
);

CREATE TABLE feedbacks (
    id INT PRIMARY KEY AUTO_INCREMENT,
    aula_id INT NOT NULL,
    estudante_id INT NOT NULL,
    nota INT NOT NULL,
    comentario VARCHAR(200),
    UNIQUE (aula_id, estudante_id),
    CHECK (nota BETWEEN 0 AND 10),
    FOREIGN KEY (aula_id) REFERENCES aulas(id),
    FOREIGN KEY (estudante_id) REFERENCES estudantes(usuario_id)
);

CREATE TABLE materiais (
    id INT PRIMARY KEY AUTO_INCREMENT,
    materia_id INT NOT NULL,
    titulo VARCHAR(100) NOT NULL,
    tipo VARCHAR(30) NOT NULL,
    referencia VARCHAR(300) NOT NULL,
    autor_id INT NOT NULL,
    FOREIGN KEY (materia_id) REFERENCES materias(id),
    FOREIGN KEY (autor_id) REFERENCES usuarios(id)
);

CREATE TABLE chats (
    id INT PRIMARY KEY AUTO_INCREMENT,
    tipo VARCHAR(10) NOT NULL,
    materia_id INT,
    aula_id INT UNIQUE,
    UNIQUE (materia_id, tipo),
    CHECK (((tipo = 'GERAL' OR tipo = 'EQUIPE') AND materia_id IS NOT NULL AND aula_id IS NULL) OR (tipo = 'AULA' AND aula_id IS NOT NULL AND materia_id IS NULL)),
    FOREIGN KEY (materia_id) REFERENCES materias(id),
    FOREIGN KEY (aula_id) REFERENCES aulas(id)
);

CREATE TABLE mensagens (
    id INT PRIMARY KEY AUTO_INCREMENT,
    chat_id INT NOT NULL,
    autor_id INT NOT NULL,
    texto VARCHAR(500) NOT NULL,
    data_envio DATE NOT NULL,
    hora_envio VARCHAR(5) NOT NULL,
    FOREIGN KEY (chat_id) REFERENCES chats(id),
    FOREIGN KEY (autor_id) REFERENCES usuarios(id)
);

CREATE TABLE grupos_estudo (
    id INT PRIMARY KEY AUTO_INCREMENT,
    materia_id INT NOT NULL,
    nome VARCHAR(100) NOT NULL,
    aula_id INT,
    UNIQUE (materia_id, nome),
    FOREIGN KEY (materia_id) REFERENCES materias(id),
    FOREIGN KEY (aula_id) REFERENCES aulas(id)
);

CREATE TABLE grupos_estudo_alunos (
    grupo_id INT NOT NULL,
    estudante_id INT NOT NULL,
    PRIMARY KEY (grupo_id, estudante_id),
    FOREIGN KEY (grupo_id) REFERENCES grupos_estudo(id),
    FOREIGN KEY (estudante_id) REFERENCES estudantes(usuario_id)
);

CREATE TABLE grupos_estudo_monitores (
    grupo_id INT NOT NULL,
    monitor_id INT NOT NULL,
    PRIMARY KEY (grupo_id, monitor_id),
    FOREIGN KEY (grupo_id) REFERENCES grupos_estudo(id),
    FOREIGN KEY (monitor_id) REFERENCES monitores(usuario_id)
);

CREATE TABLE pedidos_monitoria (
    id INT PRIMARY KEY AUTO_INCREMENT,
    materia_id INT NOT NULL,
    estudante_id INT NOT NULL,
    aula_id INT,
    monitor_id INT,
    duvida VARCHAR(300) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'Solicitado',
    data_monitoria DATE,
    horario VARCHAR(5),
    CHECK (status = 'Solicitado' OR status = 'Aceito' OR status = 'Recusado'),
    FOREIGN KEY (materia_id) REFERENCES materias(id),
    FOREIGN KEY (estudante_id) REFERENCES estudantes(usuario_id),
    FOREIGN KEY (aula_id) REFERENCES aulas(id),
    FOREIGN KEY (monitor_id) REFERENCES monitores(usuario_id)
);

CREATE TABLE log_auditoria (
    id INT PRIMARY KEY AUTO_INCREMENT,
    usuario_id INT NOT NULL,
    data_registro DATE NOT NULL,
    hora_registro VARCHAR(5) NOT NULL,
    acao VARCHAR(100) NOT NULL,
    detalhes VARCHAR(500),
    FOREIGN KEY (usuario_id) REFERENCES usuarios(id)
);

CREATE TABLE quizzes (
    id INT PRIMARY KEY AUTO_INCREMENT,
    aula_id INT NOT NULL UNIQUE,
    titulo VARCHAR(100) NOT NULL,
    descricao VARCHAR(300),
    FOREIGN KEY (aula_id) REFERENCES aulas(id)
);

CREATE TABLE perguntas_quiz (
    id INT PRIMARY KEY AUTO_INCREMENT,
    quiz_id INT NOT NULL,
    enunciado VARCHAR(500) NOT NULL,
    dificuldade VARCHAR(20) NOT NULL,
    FOREIGN KEY (quiz_id) REFERENCES quizzes(id)
);

CREATE TABLE alternativas_quiz (
    id INT PRIMARY KEY AUTO_INCREMENT,
    pergunta_id INT NOT NULL,
    texto VARCHAR(255) NOT NULL,
    explicacao VARCHAR(500),
    correta BOOLEAN NOT NULL,
    FOREIGN KEY (pergunta_id) REFERENCES perguntas_quiz(id)
);

CREATE TABLE tentativas_quiz (
    id INT PRIMARY KEY AUTO_INCREMENT,
    quiz_id INT NOT NULL,
    estudante_id INT NOT NULL,
    data_tentativa DATE NOT NULL,
    hora_tentativa VARCHAR(5) NOT NULL,
    acertos INT NOT NULL,
    total_perguntas INT NOT NULL,
    CHECK (acertos >= 0 AND acertos <= total_perguntas),
    FOREIGN KEY (quiz_id) REFERENCES quizzes(id),
    FOREIGN KEY (estudante_id) REFERENCES estudantes(usuario_id)
);

CREATE TABLE respostas_quiz (
    id INT PRIMARY KEY AUTO_INCREMENT,
    tentativa_id INT NOT NULL,
    pergunta_id INT NOT NULL,
    alternativa_id INT NOT NULL,
    acertou BOOLEAN NOT NULL,
    UNIQUE (tentativa_id, pergunta_id),
    FOREIGN KEY (tentativa_id) REFERENCES tentativas_quiz(id),
    FOREIGN KEY (pergunta_id) REFERENCES perguntas_quiz(id),
    FOREIGN KEY (alternativa_id) REFERENCES alternativas_quiz(id)
);

-- Dados fixos: perfis, permissoes e permissoes padrao de cada perfil

INSERT INTO perfis (id, nome) VALUES (1, 'Administrador');
INSERT INTO perfis (id, nome) VALUES (2, 'Professor');
INSERT INTO perfis (id, nome) VALUES (3, 'Monitor');
INSERT INTO perfis (id, nome) VALUES (4, 'Aluno');
INSERT INTO permissoes (id, modulo, acao, descricao) VALUES (1, 'Estrutura academica', 'Gerenciar', 'Cadastrar cursos, periodos, disciplinas e contas de usuario');
INSERT INTO permissoes (id, modulo, acao, descricao) VALUES (2, 'Monitores', 'Cadastrar', 'Cadastrar monitores');
INSERT INTO permissoes (id, modulo, acao, descricao) VALUES (3, 'Materia', 'Gerenciar', 'Criar aulas, grupos de estudo e quiz da materia');
INSERT INTO permissoes (id, modulo, acao, descricao) VALUES (4, 'Monitoria', 'Atender', 'Aceitar ou recusar pedidos de monitoria');
INSERT INTO permissoes (id, modulo, acao, descricao) VALUES (5, 'Conteudo', 'Moderar', 'Apagar mensagens e materiais de outros usuarios');
INSERT INTO permissoes (id, modulo, acao, descricao) VALUES (6, 'Feedback', 'Registrar', 'Registrar o entendimento de uma aula');
INSERT INTO permissoes (id, modulo, acao, descricao) VALUES (7, 'Monitoria', 'Solicitar', 'Pedir ajuda a um monitor');
INSERT INTO permissoes (id, modulo, acao, descricao) VALUES (8, 'Quiz', 'Responder', 'Responder o quiz de uma aula');
INSERT INTO perfis_permissoes (perfil_id, permissao_id) VALUES (1, 1);
INSERT INTO perfis_permissoes (perfil_id, permissao_id) VALUES (1, 2);
INSERT INTO perfis_permissoes (perfil_id, permissao_id) VALUES (1, 4);
INSERT INTO perfis_permissoes (perfil_id, permissao_id) VALUES (2, 2);
INSERT INTO perfis_permissoes (perfil_id, permissao_id) VALUES (2, 3);
INSERT INTO perfis_permissoes (perfil_id, permissao_id) VALUES (2, 4);
INSERT INTO perfis_permissoes (perfil_id, permissao_id) VALUES (2, 5);
INSERT INTO perfis_permissoes (perfil_id, permissao_id) VALUES (3, 4);
INSERT INTO perfis_permissoes (perfil_id, permissao_id) VALUES (3, 5);
INSERT INTO perfis_permissoes (perfil_id, permissao_id) VALUES (4, 6);
INSERT INTO perfis_permissoes (perfil_id, permissao_id) VALUES (4, 7);
INSERT INTO perfis_permissoes (perfil_id, permissao_id) VALUES (4, 8);
