# Matriz de rastreabilidade

| Identificacao | Regra ou requisito | Ator principal | Componentes | Evidencia de teste |
| --- | --- | --- | --- | --- |
| RF01 | Registrar nivel de compreensao de 0 a 10 por aula | Aluno | `PainelFeedback`, `Aula`, `Feedback`, `Validador` | `ValidadorTest`, `AulaIntervencaoPedagogicaTest` |
| RF02 | Consultar o entendimento da turma e orientar intervencao | Professor | `PainelDashboard`, `PainelResumoMetricas`, `Aula` | `AulaIntervencaoPedagogicaTest` |
| RF03 | Organizar grupos de estudo e apoio de monitoria | Professor e Monitor | `ModuloGruposEstudo`, `ModuloMonitoria`, `GrupoEstudo`, `PedidoMonitoria` | Validacao de sistema no roteiro |
| RF04 | Manter cursos, periodos, disciplinas e turmas | Administrador do Sistema | `PainelAdministrador`, `PainelCursosPeriodos`, `PainelDisciplinas` | Validacao de sistema no roteiro |
| RF05 | Manter contas, perfis e permissoes | Administrador do Sistema | `Administrador`, `PainelUsuarios`, `ModuloSenhasAdministrador` | `AdministradorTest` |
| RN01 | Administrador nao remove conteudo pedagogico de outro ator | Administrador do Sistema | `Usuario.podeApagarConteudoDe` | `AdministradorTest` |
| RN02 | Login deve identificar uma conta de forma unica | Administrador do Sistema | `EsquemaBanco`, tabela `usuarios` | `EsquemaBancoTest` |
| RN03 | Um aluno registra apenas um feedback por aula | Aluno | `Aula.registrarFeedback`, `EsquemaBanco` | `AulaIntervencaoPedagogicaTest`, `EsquemaBancoTest` |
| RN04 | Quiz so pode ser respondido com pergunta cadastrada | Aluno e Professor | `Quiz`, `PainelQuiz`, `PainelGerenciarQuiz` | `QuizDisponibilidadeTest` |
| RN05 | Uma tentativa possui somente uma resposta por pergunta | Aluno | `TentativaQuiz`, `EsquemaBanco` | `IntegracaoQuizTest`, `EsquemaBancoTest` |
| RN06 | Media abaixo de 7 indica necessidade de reforco sem impor quiz | Professor | `Aula`, `PainelResumoMetricas` | `AulaIntervencaoPedagogicaTest` |

## Uso da matriz

Toda alteracao deve atualizar a linha afetada, manter o ator responsavel, indicar o componente modificado e incluir ou ajustar o caso de teste correspondente. O roteiro de sistema complementa os itens que dependem de MySQL e interface grafica.
