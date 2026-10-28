# Roteiro de validacao do sistema

## Precondicoes

- MySQL ativo em `localhost:3307` com o banco definido pela propriedade `minddebug.banco`.
- Projeto compilado com `src/lib/mysql-connector-j-9.3.0.jar`.
- Base nova ou base compativel com o esquema atual.

## Fluxo demonstravel

1. Iniciar a aplicacao e confirmar que a tela de login abre sem erro de conexao.
2. Entrar como Administrador do Sistema e cadastrar curso, periodo, disciplina e usuarios.
3. Entrar como Professor, abrir a disciplina, cadastrar aula e verificar o dashboard.
4. Entrar como Aluno e registrar feedback entre 0 e 10 para a aula.
5. Retornar ao Professor e confirmar participacao, media e acao de reforco quando a media for abaixo de 7.
6. Criar um grupo de estudos ou outra intervencao pedagogica; criar quiz apenas quando for a decisao do professor.
7. Criar quiz com titulo, descricao e pergunta valida de quatro alternativas.
8. Entrar como Aluno, responder o quiz e confirmar que o resultado e registrado uma unica vez por pergunta.
9. Retornar ao Professor e conferir tentativas e media de acertos do quiz.
10. Salvar, reiniciar a aplicacao e confirmar que os dados permanecem disponiveis.

## Criterios de aceitacao

- Cada ator acessa somente as funcoes compativeis com seu papel.
- O sistema bloqueia nota fora de 0 a 10, quiz vazio e resposta duplicada.
- O dashboard apresenta a necessidade de reforco para media abaixo de 7 sem obrigar a criacao de quiz.
- Nenhuma mensagem de erro exibe senha ou outro dado sensivel.
- A aplicacao abre e salva com o MySQL ativo.
