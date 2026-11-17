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

As tabelas sao criadas pela propria aplicacao na primeira execucao: para cada tabela do esquema (`src/dao/EsquemaBanco.java`), o sistema consulta o catalogo do MySQL (`information_schema.tables`) e cria so as que faltam. Na mesma hora grava os dados fixos: os quatro perfis, as permissoes e as permissoes padrao de cada perfil.

Com o banco ainda sem usuarios, o sistema grava tambem os dados de demonstracao (um usuario de cada perfil, o curso de Engenharia da Computacao com a grade e uma materia com aulas), numa unica transacao. Depois disso, cada operacao feita nas telas e gravada no banco na hora; fechar a janela nao salva nada.

O script `src/MindDebugBD.sql` cria o mesmo esquema manualmente, se necessario (por exemplo, pelo MySQL Workbench). Ele deve ser executado uma vez, com o banco ainda vazio. O teste `dao.ScriptBancoTest` confere se o script e o esquema continuam iguais.

### Banco com as tabelas da versao anterior

Se o banco ainda tiver as tabelas antigas (antes da tabela `perfis`), o sistema nao abre e avisa. Os dados antigos nao sao convertidos. Para recriar:

- com o `compose.yaml` do projeto: `docker compose down -v` e depois `docker compose up -d`;
- em outro MySQL: apagar as tabelas antigas do banco configurado e abrir o sistema de novo.

### Datas e horas

Datas sao digitadas no formato `dd/mm/aaaa` e horas no formato `HH:mm`. No banco, datas ficam em colunas `DATE` e horas em `VARCHAR(5)`.

## Aplicacao e testes

No IntelliJ:

1. Adicionar `src/lib/mysql-connector-j-9.3.0.jar` como biblioteca do modulo.
2. Executar `app.Main` para abrir o sistema.
3. Executar cada classe de teste da pasta `test` (cada uma tem um metodo `main`). Os testes nao precisam do MySQL ativo.
