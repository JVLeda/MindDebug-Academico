# Ambiente local

Requisitos: JDK, IntelliJ e Docker Desktop.

## Banco de dados

Na raiz do projeto:

```
docker compose up -d     # inicia o MySQL local
docker compose stop      # para o MySQL, mantendo os dados
docker compose down -v   # remove o container e TODOS os dados
```

O MySQL fica disponivel em `localhost:3306`, banco `minddebug`, usuario `minddebug`, senha `minddebug`.
Esses valores estao em um unico lugar no codigo: `src/util/Conexao.java`, e devem ser iguais aos do `compose.yaml`.
O teste `util.ConexaoTest` confere se os dois arquivos continuam alinhados.

As tabelas sao criadas pela propria aplicacao na primeira execucao. O script `src/MindDebugBD.sql` cria o mesmo esquema manualmente, se necessario (por exemplo, pelo MySQL Workbench).

## Aplicacao e testes

No IntelliJ:

1. Adicionar `src/lib/mysql-connector-j-9.3.0.jar` como biblioteca do modulo.
2. Executar `app.Main` para abrir o sistema.
3. Executar cada classe de teste da pasta `test` (cada uma tem um metodo `main`). Os testes nao precisam do MySQL ativo.
