# Achados e Perdidos — DDM

Aplicativo Android em Java para cadastrar objetos encontrados, localizar pontos de coleta e registrar retiradas. Projeto da disciplina de Desenvolvimento para Dispositivos Móveis, com Firebase e Google Maps.

## Documento principal

**[Especificação do aplicativo](ESPECIFICACAO-DO-APP.md)** — referência para requisitos, perfis, permissões, telas, regras de negócio, dados e critérios de aceite.

Consulte e atualize esse documento sempre que houver uma definição ou alteração importante no app.

## Instruções de contribuição

[AGENTS.md](AGENTS.md) reúne as regras de trabalho e os padrões de commits e branches.


## Abrir e executar no Android Studio

1. Abra a pasta raiz deste repositório no Android Studio (Open) e aguarde o Gradle Sync.
2. Use JDK 17 para o Gradle e instale o Android SDK 35. O identificador do aplicativo (`applicationId`) é `trabalho.DDM.java` e o Android mínimo é API 24.
3. Configure o caminho do SDK em `local.properties` (gerado pelo Studio; não versionado).
4. Use o projeto Firebase `trabalho-ddm-java`, com o app Android registrado como `trabalho.DDM.java`, e coloque o arquivo baixado em `app/google-services.json` (não versionado). O JSON fornecido já foi instalado localmente. As classes Java continuam no namespace `br.edu.ddm.achadoseperdidos`.
5. Authentication por e-mail/senha está habilitado e o Firestore padrão foi criado em São Paulo (`southamerica-east1`), no plano Spark. As regras de `firestore.rules` foram publicadas nesse projeto. Ao usar outro projeto, aplique-as novamente. O arquivo fecha acesso aos módulos ainda não implementados; nenhuma regra é publicada automaticamente pelo build.
6. Habilite Maps SDK for Android e preencha `MAPS_API_KEY=sua-chave` em `local.properties`. Restrinja a chave ao pacote `trabalho.DDM.java` e ao SHA-1 da assinatura usada. A chave fornecida foi adicionada localmente em 02/11/2026, sem versionamento; o carregamento do mapa no aparelho ainda precisa ser validado. Sem chave, o app explica a configuração ao abrir a seleção de mapa.
7. Selecione o módulo `app` e execute em emulador ou aparelho. Sem configuração Firebase, o app compila e abre, mas informa a configuração pendente e não simula login.

O plugin Google Services só é aplicado quando o JSON existir; após adicioná-lo, sincronize e recompile. Se o Android Studio mostrar `Activity class ... does not exist` com o identificador antigo `br.edu.ddm.achadoseperdidos`, execute **Sync Project with Gradle Files** e rode o módulo `app` novamente; o applicationId atual é `trabalho.DDM.java`. Para consultar a assinatura de desenvolvimento: `./gradlew signingReport`.

## Estrutura MVVM

- `ui`: Activity, Fragments XML e seleção de posição no mapa.
- `auth`: ViewModel, estados, validação e interface de repositório; implementação Firebase.
- `data`: entidade, DAO e banco Room de cache de perfil, sem senhas.

A área autenticada possui Bottom Navigation (Objetos, Pontos e Conta), duas listas com RecyclerView/Adapter, detalhes com acesso ao mapa e cadastro de objeto sem fotos para usuário comum. Busca por nome e filtro por ponto funcionam juntos. O cadastro exige um ponto existente e a orientação de deixar o objeto no local antes de anunciar. Atualizar recarrega os dados do servidor.

O pacote `catalog` contém CatalogViewModel, validações e o repositório Firestore. A criação de objeto usa transação online e um ID reutilizado em tentativas para evitar duplicação. Fotos/câmera/Storage, inventário, entregas, histórico e cache de objetos/pontos continuam pendentes. Room armazena apenas perfis; não permite autenticação ou entregas offline.

## Verificações

```sh
./gradlew testDebugUnitTest assembleDebug lintDebug
```

Testes unitários cobrem validação e estados da autenticação com um repositório de teste, sem acessar contas reais. Para validar a integração, configure Firebase/Maps e teste cadastro comum e de ponto, e-mail duplicado, senha incorreta, saída da conta, rotação e retomada de perfil incompleto. As regras precisam ser publicadas e verificadas no projeto Firebase antes de usar dados reais.

## Regras e validação dos novos módulos

As novas regras de objetos estão em `firestore.rules` e foram publicadas no Firebase `trabalho-ddm-java` em 02/11/2026 com autorização do responsável. Somente contas COMUM podem criar objetos e usuários autenticados podem consultar os disponíveis. Não habilitar acesso irrestrito para contornar falhas de permissão.

Testes locais das permissões, sem alterar dados reais (Firebase CLI e Java 21):

```sh
firebase emulators:exec --only firestore,auth --project demo-ddm-catalog --config firebase.test.json 'python3 tests/firebase/check_rules.py'
```

O teste usa apenas emuladores e valida publicação, consultas, autor, perfil, ponto, coordenadas, conteúdo obrigatório e bloqueio de alterações/entregas. Testes unitários do app totalizam 32; compilação e lint passaram. Para a validação completa no app, publicar as regras, cadastrar um ponto de teste, publicar um objeto sem foto, testar busca/filtro e abrir os detalhes de objeto/ponto.

Validação no emulador: listas de objetos/pontos carregam no Firebase real; detalhes do ponto e validação de nome vazio conferidos. O mapa apresentou falha de autorização da chave. Conferir, no projeto de origem da chave, Maps SDK for Android e a restrição de aplicativo para `trabalho.DDM.java` com SHA-1 de desenvolvimento `55:09:DB:46:11:C9:58:44:F3:0A:33:E2:D3:0A:9B:BA:DA:EA:4E:21`. A chave foi localizada no projeto `gmp-demo-project-991730262` (Maps Platform Demo Project), na conta de trabalhos indicada pelo responsável. É uma **Maps Demo Key**, cuja lista de serviços não inclui Maps SDK for Android. Segundo a [documentação oficial](https://developers.google.com/maps/demo-key), o uso de serviços fora da demonstração exige faturamento configurado. O responsável precisa concluir essa etapa e disponibilizar uma chave habilitada para Android; nenhuma configuração financeira foi alterada.
