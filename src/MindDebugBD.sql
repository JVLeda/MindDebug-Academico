create database if not exists minddebug;

use minddebug;

create table if not exists cursos (
    id INT NOT NULL,
    nome VARCHAR(100) NOT NULL,
    sigla VARCHAR(20),
    area VARCHAR(100),
    carga_horaria INT,
    coordenador VARCHAR(100),
    PRIMARY KEY (id)
) ;

create table if not exists usuarios (
    id INT NOT NULL,
    nome VARCHAR(100) NOT NULL,
    login VARCHAR(50) NOT NULL,
    email VARCHAR(100),
    senha VARCHAR(50) NOT NULL,
    cargo VARCHAR(30) NOT NULL,
    tipo_monitor VARCHAR(30),
    matricula VARCHAR(50),
    curso_id INT,
    PRIMARY KEY (id)
) ;

create table if not exists periodos (
    id INT NOT NULL,
    curso_id INT NOT NULL,
    numero INT,
    nome VARCHAR(100),
    PRIMARY KEY (id)
) ;

create table if not exists materias (
    id INT NOT NULL,
    periodo_id INT NOT NULL,
    nome VARCHAR(100) NOT NULL,
    professor_id INT,
    PRIMARY KEY (id)
) ;

create table if not exists participantes (
    materia_id INT NOT NULL,
    usuario_id INT NOT NULL
) ;

create table if not exists aulas (
    id INT NOT NULL,
    materia_id INT NOT NULL,
    titulo VARCHAR(100) NOT NULL,
    data_aula VARCHAR(20),
    objetivo VARCHAR(200),
    PRIMARY KEY (id)
) ;

create table if not exists quizzes (
    id INT NOT NULL,
    aula_id INT NOT NULL,
    titulo VARCHAR(100) NOT NULL,
    descricao VARCHAR(300),
    PRIMARY KEY (id),
    UNIQUE (aula_id),
    FOREIGN KEY (aula_id) REFERENCES aulas(id)
) ;

create table if not exists perguntas_quiz (
    id INT NOT NULL,
    quiz_id INT NOT NULL,
    enunciado VARCHAR(500) NOT NULL,
    dificuldade VARCHAR(20) NOT NULL,
    PRIMARY KEY (id),
    FOREIGN KEY (quiz_id) REFERENCES quizzes(id)
) ;

create table if not exists alternativas_quiz (
    id INT NOT NULL,
    pergunta_id INT NOT NULL,
    texto VARCHAR(255) NOT NULL,
    explicacao VARCHAR(500),
    correta BOOLEAN NOT NULL,
    PRIMARY KEY (id),
    FOREIGN KEY (pergunta_id) REFERENCES perguntas_quiz(id)
) ;

create table if not exists tentativas_quiz (
    id INT NOT NULL,
    quiz_id INT NOT NULL,
    estudante_id INT NOT NULL,
    data_tentativa VARCHAR(30),
    acertos INT NOT NULL,
    total_perguntas INT NOT NULL,
    PRIMARY KEY (id),
    FOREIGN KEY (quiz_id) REFERENCES quizzes(id),
    FOREIGN KEY (estudante_id) REFERENCES usuarios(id)
) ;

create table if not exists respostas_quiz (
    id INT NOT NULL,
    tentativa_id INT NOT NULL,
    pergunta_id INT NOT NULL,
    alternativa_id INT NOT NULL,
    acertou BOOLEAN NOT NULL,
    PRIMARY KEY (id),
    FOREIGN KEY (tentativa_id) REFERENCES tentativas_quiz(id),
    FOREIGN KEY (pergunta_id) REFERENCES perguntas_quiz(id),
    FOREIGN KEY (alternativa_id) REFERENCES alternativas_quiz(id)
) ;

create table if not exists feedbacks (
    id INT NOT NULL AUTO_INCREMENT,
    aula_id INT NOT NULL,
    estudante_id INT NOT NULL,
    nota INT,
    comentario VARCHAR(200),
    PRIMARY KEY (id)
) ;

create table if not exists materiais (
    id INT NOT NULL,
    materia_id INT NOT NULL,
    titulo VARCHAR(100),
    tipo VARCHAR(30),
    referencia VARCHAR(300),
    autor_id INT,
    PRIMARY KEY (id)
) ;

create table if not exists mensagens (
    id INT NOT NULL,
    origem VARCHAR(20) NOT NULL,
    origem_id INT NOT NULL,
    autor_id INT,
    texto VARCHAR(500),
    PRIMARY KEY (id)
) ;

create table if not exists grupos_estudo (
    id INT NOT NULL,
    materia_id INT NOT NULL,
    nome VARCHAR(100) NOT NULL,
    aula_id INT,
    monitor_id INT,
    PRIMARY KEY (id)
) ;

create table if not exists grupos_estudo_alunos (
    grupo_id INT NOT NULL,
    estudante_id INT NOT NULL
) ;

create table if not exists grupos_estudo_monitores (
    grupo_id INT NOT NULL,
    monitor_id INT NOT NULL
) ;

create table if not exists pedidos_monitoria (
    id INT NOT NULL,
    materia_id INT NOT NULL,
    estudante_id INT NOT NULL,
    aula_id INT,
    monitor_id INT,
    duvida VARCHAR(300),
    status VARCHAR(30),
    data_monitoria VARCHAR(20),
    horario VARCHAR(10),
    PRIMARY KEY (id)
) ;

select * from usuarios;
