# Responsabilidades por ator

## Objetivo

O sistema foi organizado por responsabilidade funcional dos quatro atores do projeto. A separacao facilita que cada integrante desenvolva, teste e apresente sua parte sem duplicar regras de negocio ou acesso aos dados.

## Aluno

- Cadastro de compreensao da aula de 0 a 10: `Estudante`, `Feedback` e `PainelFeedback`.
- Consulta e resposta de quizzes liberados: `PainelQuiz`, `Quiz`, `PerguntaQuiz`, `AlternativaQuiz`, `RespostaQuiz` e `TentativaQuiz`.
- Participacao nos grupos e chats da materia: `GrupoEstudo`, `Chat`, `Mensagem` e `PainelChatsPorAula`.

## Monitor

- Atendimento de pedidos e acompanhamento de monitoria: `Monitor`, `PedidoMonitoria` e `ModuloMonitoria`.
- Apoio aos grupos de estudo definidos para a materia, sem assumir funcoes de configuracao academica.

## Professor

- Planejamento de aulas, materiais, grupos e distribuicao de monitores: `Professor`, `PainelAulas`, `PainelRepositorio`, `ModuloGruposEstudo` e `PainelMonitores`.
- Leitura do nivel de entendimento da turma: `PainelDashboard`, `PainelResumoMetricas`, `GraficoMediasAulas` e `GraficoStatusAulas`.
- Decisao sobre acao de reforco e elaboracao de quiz quando apropriado: `PainelGerenciarQuiz`.

## Administrador do Sistema

- Manutencao da estrutura academica e das contas: `Administrador`, `PainelAdministrador`, `PainelCursosPeriodos`, `PainelDisciplinas`, `PainelUsuarios` e `ModuloSenhasAdministrador`.
- O cargo tem nivel administrativo para cadastros e acesso ao sistema, mas nao apaga conteudo pedagogico de outro usuario.
- Registros antigos com a classificacao tecnica anterior sao lidos como administrador apenas para manter a compatibilidade dos dados ja existentes.

## Integracao e persistencia

- `MindDebugController` concentra regras de permissao e casos de uso compartilhados.
- As classes da pasta `dao` isolam o acesso JDBC e mantem a separacao entre regras do sistema e banco de dados.
- As entidades do pacote `model` representam os dados e seus relacionamentos, com identificadores preservados na persistencia.

## Criterios de validacao

- Aluno registra um feedback por aula e responde apenas quiz com pergunta cadastrada.
- Professor visualiza aulas cuja media da turma esteja abaixo de 7 como aulas que exigem reforco; a acao pode ser quiz, grupo de estudos ou outra intervencao.
- Administrador cadastra estrutura e usuarios sem remover conteudo pedagogico de outros atores.
- Cada alteracao deve ser validada com testes unitarios do pacote `test/model` e compilacao completa do projeto.
