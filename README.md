# MindDebug

## Fluxo demonstravel

O MindDebug apoia intervenções pedagógicas a partir do feedback da turma:

1. Cada aluno registra, de 0 a 10, o nível de compreensão de uma aula.
2. O dashboard do professor mostra participação, média da turma e status da aula.
3. Quando a média é inferior a 7, o sistema recomenda planejar uma ação de reforço, como quiz, grupo de estudo, monitoria, material complementar ou revisão da aula.
4. O professor pode criar o quiz no menu `Matéria > Quiz` e vinculá-lo à aula com maior dificuldade.
5. Os alunos respondem ao quiz e o dashboard apresenta o número de tentativas e a média de acertos da turma.

Esse ciclo permite comparar o diagnóstico inicial de compreensão com o desempenho após a atividade de reforço. A reutilização do mesmo quiz entre turmas de períodos futuros permanece como evolução planejada.

## Roteiro para apresentação

1. Entre como professor e abra a disciplina.
2. Mostre o dashboard e escolha uma aula marcada como `Crítico` ou `Regular`.
3. Explique a coluna `Ação pedagógica`, que sinaliza a necessidade de reforço sem limitar a decisão do professor.
4. Use o quiz como um exemplo de intervenção no menu `Matéria > Quiz` e adicione perguntas sobre o objetivo da aula.
5. Entre como aluno, responda o quiz e volte ao perfil do professor.
6. Atualize o dashboard para mostrar as tentativas e a média de acertos do quiz.

## Executar a versao atual

Antes da demonstracao, compile a aplicacao a partir de `src`. Nao use apenas o conteudo ja existente em `out/production`, pois esse diretorio pode conter uma compilacao anterior.

No PowerShell, na raiz do projeto:

```powershell
$classes = "tmp\\minddebug-demo"
New-Item -ItemType Directory -Force -Path $classes | Out-Null
$fontes = Get-ChildItem src -Recurse -Filter *.java | ForEach-Object { $_.FullName }
javac -encoding UTF-8 -cp "src\\lib\\mysql-connector-j-9.3.0.jar" -d $classes $fontes
java -cp "$classes;src\\lib\\mysql-connector-j-9.3.0.jar" app.Main
```

Assim, a demonstracao usa a gestao de quiz atualizada e o estado de disponibilidade do quiz para o aluno.
