# Instruções para agentes e colaboradores

Estas regras valem para todo o repositório. O objetivo é manter o trabalho de DDM simples, consistente e documentado.

## 1. Documento base do aplicativo

Antes de definir ou implementar funcionalidades, leia a [especificação do aplicativo](ESPECIFICACAO-DO-APP.md), documento principal de requisitos localizado na raiz do repositório.

Esse documento é a referência para escopo, perfis, permissões, requisitos, regras de negócio, telas, fluxos, dados, tecnologias e critérios de aceite. Preserve o foco em um aplicativo Android em Java com Firebase e Google Maps, adequado ao trabalho da faculdade.

## 2. Atualização obrigatória da documentação

Toda alteração ou definição importante deve atualizar o documento base na mesma tarefa. Isso inclui decisões tomadas antes de existir código.

Considere importante qualquer mudança que:

- Adicione, remova ou altere uma funcionalidade ou requisito.
- Modifique perfis, permissões, regras de negócio ou situações dos objetos.
- Altere telas, navegação ou fluxos de cadastro, busca, recebimento ou entrega.
- Mude campos, relações, armazenamento ou acesso aos dados e fotos.
- Altere arquitetura, tecnologias, integrações ou dependências relevantes.
- Modifique validações, tratamento de falhas ou critérios de aceite.
- Amplie ou reduza os limites da primeira versão.

Ao atualizar o documento:

1. Ajuste as seções afetadas para descrever a decisão ou o comportamento final, sem contradições.
2. Revise requisitos, regras, permissões, dados, cenários de aceite e checklist relacionados.
3. Preserve os identificadores existentes; use novos identificadores para novos requisitos, sem renumerar os demais.
4. Atualize a versão e a data quando houver uma mudança relevante e registre um resumo em uma seção de histórico de alterações, criando-a quando necessário.
5. Mantenha código e documentação no mesmo commit lógico quando representarem a mesma mudança.

Não deixe divergências silenciosas entre o código e o documento. Uma nova orientação explícita do usuário deve ser refletida na documentação. Se houver uma contradição relevante que a orientação não resolva, esclareça-a antes de implementar a parte afetada.

Correções de digitação, formatação e refatorações internas que preservem o comportamento e a arquitetura documentados não exigem alterar os requisitos.

## 3. Padrão de commits

Adote a convenção usada no DocX, removendo o identificador do card.

Formato obrigatório:

```text
<tipo>(<escopo>): <descrição curta>
```

| Tipo | Uso |
| --- | --- |
| `feat` | Nova funcionalidade |
| `fix` | Correção de comportamento |
| `refactor` | Refatoração sem mudança de comportamento |
| `test` | Inclusão ou ajuste de testes |
| `docs` | Documentação e instruções do projeto |
| `chore` | Manutenção técnica, configuração ou dependências |

Regras:

- Escreva o título e o corpo em português do Brasil, preservando os tipos acima e identificadores técnicos quando necessário.
- Use tipo e escopo em minúsculas; o escopo deve ser curto, sem espaços ou acentos, como `auth`, `objetos`, `coleta`, `entregas`, `mapas` ou `firebase`.
- Descreva a ação de forma objetiva, preferencialmente no infinitivo, sem ponto final. Prefira títulos de até 72 caracteres.
- Não inclua ID de card, ticket ou prefixo fictício.
- Agrupe apenas mudanças relacionadas. Evite títulos genéricos como “ajustes” ou “atualizações”.
- Quando necessário, use o corpo para explicar o motivo, as mudanças e as verificações realizadas.

Exemplos:

```text
feat(objetos): implementar cadastro de objetos encontrados
fix(entregas): impedir retirada sem foto de identificação
refactor(firebase): separar acesso aos dados de objetos
test(coleta): validar permissões do ponto responsável
docs(requisitos): atualizar regras de retirada de objetos
chore(projeto): definir instruções para colaboradores
```

## 4. Padrão de branches

Adote o nome descritivo usado após o ID de card no DocX, sem o ID e sem prefixos adicionais.

Formato obrigatório:

```text
<descricao-curta-em-kebab-case>
```

- Use uma descrição em português do Brasil, em minúsculas e sem acentos.
- Separe palavras com hífen; não use espaços, underscores ou barras.
- Prefira nomes curtos e específicos, de 3 a 6 palavras.
- Não inclua ID de card, ticket, prefixo fictício ou `codex/`.
- Esta regra vale para novas branches de trabalho; não renomeie branches existentes sem solicitação.

Exemplos:

```text
implementar-cadastro-de-objetos
corrigir-validacao-de-retirada
documentar-regras-do-aplicativo
configurar-login-com-firebase
```

## 5. Fluxo de trabalho e verificação

1. Verifique `git status --short` e preserve alterações existentes que não pertençam à tarefa.
2. Leia o documento base e identifique os requisitos afetados.
3. Faça a menor alteração que atenda ao pedido, mantendo o escopo simples.
4. Atualize o documento quando houver uma definição ou alteração importante.
5. Execute as verificações adequadas ao que mudou. Para documentação, revise conteúdo, links e formatação; para código, execute os testes e o build aplicáveis que existirem no projeto.
6. Confira o diff e informe o que mudou, o que foi verificado e eventuais limitações. Não afirme ter executado verificações que não foram feitas.

Ao criar um commit, selecione somente os arquivos da tarefa, revise `git diff --cached` e execute `git diff --cached --check`. Não inclua arquivos pessoais da IDE, artefatos de build ou credenciais. Evite `git add .` e `git add -A` quando houver alterações não relacionadas.

Definir estas convenções não autoriza por si só commit, push, merge ou criação de Pull Request; execute essas operações quando solicitadas pelo usuário.
