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
4. Use o projeto Firebase `trabalho-ddm-java-3cc2a` (nome de exibição `trabalho-ddm-java`), da conta D2G Trabalhos, com o app Android registrado como `trabalho.DDM.java`. Coloque o arquivo baixado em `app/google-services.json` (não versionado); o arquivo correto foi instalado localmente em 05/11/2026. `.firebaserc` aponta para esse projeto. As classes Java continuam no namespace `br.edu.ddm.achadoseperdidos`.
5. Authentication por e-mail/senha está habilitado e o Firestore Standard padrão foi criado em São Paulo (`southamerica-east1`), no plano Spark. As regras de `firestore.rules` foram publicadas nesse novo projeto em 05/11/2026. Ao usar outro projeto, aplique-as novamente. O arquivo fecha acesso aos módulos ainda não implementados; nenhuma regra é publicada automaticamente pelo build. Contas e dados do projeto anterior não foram migrados: é necessário criar uma conta pelo app para usar o novo ambiente.
6. Para o protótipo, use a Maps Demo Key da conta D2G (projeto `gmp-demo-project-991730262`) e preencha `MAPS_API_KEY=sua-chave-demo` em `local.properties`, sem versionar o valor. O arquivo foi configurado localmente em 05/11/2026. O mapa usa Google Maps JavaScript API em WebView, sem cartão ou faturamento. Não é necessário habilitar o SDK Android nativo. Mantenha Android System WebView atualizado. Storage não é usado nesta configuração.
7. Selecione o módulo `app` e execute em emulador ou aparelho. Sem configuração Firebase, o app compila e abre, mas informa a configuração pendente e não simula login.

O plugin Google Services só é aplicado quando o JSON existir; após adicioná-lo, sincronize e recompile. Se o Android Studio mostrar `Activity class ... does not exist` com o identificador antigo `br.edu.ddm.achadoseperdidos`, execute **Sync Project with Gradle Files** e rode o módulo `app` novamente; o applicationId atual é `trabalho.DDM.java`. Para consultar a assinatura de desenvolvimento: `./gradlew signingReport`.

Maps em 05/11/2026: por autorização do responsável, o SDK nativo foi substituído por Google Maps JavaScript API em WebView, compatível com a [Demo Key gratuita para protótipos](https://developers.google.com/maps/documentation/javascript/demo-key) e com [Chrome WebView no Android](https://developers.google.com/maps/documentation/javascript/browsersupport). A chave tem limites diários e não se destina a produção. A página local usa AndroidX WebKit, sem servidor. A localização atual é opcional, obtida pelo Android somente nos formulários e após autorização; negar a permissão mantém a seleção manual. A comunicação JavaScript/Java é limitada à origem HTTPS local e ao frame principal. O mapa mantém seleção, marcadores e coordenadas ao recriar a tela; falhas mostram nova tentativa e impedem confirmação sem mapa pronto. Nenhum faturamento ou permissão da chave foi alterado.

## Estrutura MVVM

- `ui`: Activity, Fragments XML e seleção de posição no mapa.
- `auth`: ViewModel, estados, validação e interface de repositório; implementação Firebase.
- `data`: entidade, DAO e banco Room de cache de perfil, sem senhas.

A área autenticada possui Bottom Navigation por perfil (Objetos/Pontos/Conta para comuns; Meus objetos/Conta para pontos), duas listas com RecyclerView/Adapter, detalhes com acesso ao mapa e cadastro de objeto com fotos opcionais para usuário comum. Busca por nome e filtro por ponto funcionam juntos. O cadastro exige um ponto existente e a orientação de deixar o objeto no local antes de anunciar. Atualizar recarrega os dados do servidor.

O pacote `catalog` contém CatalogViewModel, validações e o repositório Firestore. A criação de objeto usa transação online e um ID reutilizado em tentativas para evitar duplicação. Fotos e câmera estão implementadas. O pacote delivery adiciona inventário próprio, entrega e histórico privado. Não há cache Room de objetos/pontos nem uso do Storage. Room armazena apenas perfis; não permite autenticação ou entregas offline.

## Verificações

```sh
./gradlew testDebugUnitTest assembleDebug lintDebug
```

Testes unitários cobrem validação e estados da autenticação com um repositório de teste, sem acessar contas reais. Para validar a integração, configure Firebase/Maps e teste cadastro comum e de ponto, e-mail duplicado, senha incorreta, saída da conta, rotação e retomada de perfil incompleto. As regras precisam ser publicadas e verificadas no projeto Firebase antes de usar dados reais.

## Regras e validação dos novos módulos

As regras de objetos estão em `firestore.rules` e foram publicadas no Firebase atual `trabalho-ddm-java-3cc2a` em 05/11/2026. Somente contas COMUM podem criar objetos e usuários autenticados podem consultar os disponíveis. Não habilitar acesso irrestrito para contornar falhas de permissão.

Testes locais das permissões, sem alterar dados reais (Firebase CLI e Java 21):

```sh
firebase emulators:exec --only firestore,auth --project demo-ddm-catalog --config firebase.test.json 'python3 tests/firebase/check_rules.py'
```

O teste usa apenas emuladores e valida publicação, consultas, autor, perfil, ponto, coordenadas, conteúdo obrigatório e bloqueio de alterações/entregas. Testes unitários do app totalizam 32; compilação e lint passaram. Para a validação completa no app, publicar as regras, cadastrar um ponto de teste, publicar um objeto sem foto, testar busca/filtro e abrir os detalhes de objeto/ponto.

Validação no projeto anterior em 02/11/2026: listas de objetos/pontos carregaram no Firebase real; detalhes do ponto e validação de nome vazio foram conferidos. O mapa apresentou falha de autorização da chave. Conferir, no projeto de origem da chave, Maps SDK for Android e a restrição de aplicativo para `trabalho.DDM.java` com SHA-1 de desenvolvimento `55:09:DB:46:11:C9:58:44:F3:0A:33:E2:D3:0A:9B:BA:DA:EA:4E:21`. A chave foi localizada no projeto `gmp-demo-project-991730262` (Maps Platform Demo Project), na conta de trabalhos indicada pelo responsável. É uma **Maps Demo Key**, cuja lista de serviços não inclui Maps SDK for Android. Segundo a [documentação oficial](https://developers.google.com/maps/demo-key), o uso de serviços fora da demonstração exige faturamento configurado. Esse diagnóstico motivou a adaptação posterior para Google Maps JavaScript API em WebView; nenhuma configuração financeira foi alterada.

Validação do novo projeto em 05/11/2026: 32 testes unitários passaram; build e lint passaram (zero erros, 16 avisos). APK instalado e aberto no emulador na tela de login, sem aviso de Firebase ausente. Authentication respondeu `INVALID_LOGIN_CREDENTIALS` para uma conta fictícia inexistente; Firestore negou leitura de perfil sem autenticação (`PERMISSION_DENIED`). Não foram criadas contas ou coleções nesses testes. Cadastro, login válido e consultas autenticadas no novo projeto ainda precisam de validação.

Testes da integração do mapa:

```sh
node --test tests/maps/map.test.cjs
./gradlew connectedDebugAndroidTest
```

Os nove testes JavaScript usam uma chave fictícia e cobrem seleção, consulta sem edição, coordenadas inválidas e falhas. Os seis testes Android exigem emulador com internet, WebView atualizado e `MAPS_API_KEY` local válida; carregam o mapa real, validam seleção/retorno de coordenadas após recriar a Activity e consulta sem alteração. Não criam contas ou dados no Firebase.

Validação inicial da adaptação em 05/11/2026: mapa real carregado no emulador; seleção, confirmação de coordenadas após recriar a Activity e consulta sem edição passaram nos dois testes Android. Os 32 testes Java e seis testes JavaScript passaram, assim como build e lint (zero erros, 20 avisos). Cadastro completo de ponto/objeto no novo Firebase continua pendente de validação.

Localização atual: ao abrir um mapa de cadastro sem coordenadas, o app pode pedir permissão aproximada/precisa e sugerir a posição do aparelho. O usuário deve conferir, ajustar e confirmar. Uma posição anterior é preservada; o botão **Usar minha localização** permite solicitar outra sugestão. Não há localização em segundo plano ou gravação automática. GPS desligado, permissão negada ou ausência de posição mantêm o mapa manual; uma escolha manual cancela a busca em andamento.

Os testes Android de localização usam coordenadas fictícias pelo provedor de teste, restauram o estado do serviço e verificam pré-seleção, preservação de posição anterior, botão de nova sugestão, falta de permissão e seleção manual com GPS desligado. Os testes JavaScript também verificam que uma resposta atrasada não sobrescreve a escolha manual.

Validação da localização atual em 06/11/2026: 32 testes Java, nove testes JavaScript e seis testes Android passaram; build e lint passaram (zero erros, 22 avisos). No emulador, a localização atual corresponde à posição simulada do dispositivo. Se o mapa deixar de carregar após o computador suspender e o emulador não resolver os nomes dos servidores, reinicie o AVD com DNS explícito (por exemplo, `emulator -avd Medium_Phone_API_36.1 -dns-server 8.8.8.8,8.8.4.4 -no-snapshot-load`), sem usar opções de apagar dados.

## Fotos dos objetos
Até cinco fotos opcionais via câmera ou seletor de imagens, comprimidas em JPEG até 102.400 bytes; a primeira fornece a capa até 12.288 bytes. Imagens ficam em Base64 no Firestore Spark, em `objetos/{id}/fotos/{slot}`. Não exige habilitar Storage ou Blaze; sujeito às cotas do Firestore. Para futuras atualizações, publicar regras e `firestore.indexes.json` juntos com `firebase deploy --only firestore --project trabalho-ddm-java-3cc2a` antes de usar fotos no projeto real. Nenhuma credencial é incluída no repositório.

A transação publica anúncio e anexos juntos. Repetição mantém ID e conteúdo enquanto houver confirmação pendente; encerramento do processo não preserva rascunhos. Comprovantes de entrega continuam fora desta implementação.

Verificação desta etapa: 34 testes Java, nove testes Android no moto g15 (Android 15), nove testes JavaScript e 31 cenários de regras Firebase passaram. Build e lint concluíram sem erros. Os testes Android cobrem compressão, orientação EXIF, remoção de localização, FileProvider e cancelamento do contrato de câmera; seleção/captura e publicação reais completas ainda exigem validação manual. Em 06/11/2026, após autorização explícita, as regras e as duas isenções de índices foram publicadas pelo Console do Firebase no Safari com a conta D2G. A ferramenta local usa uma conta pessoal sem permissão neste projeto; para futuros deploys pela CLI, autenticar a conta autorizada.

## Inventário e comprovantes privados

Pontos de coleta têm somente **Meus objetos** e **Conta**, iniciando em Meus objetos, com Disponíveis/Entregues. Só o ponto vinculado registra retirada: descrição e de uma a cinco fotos pela galeria. A entrega, as fotos Base64 e a mudança para ENTREGUE são gravadas em uma transação. Reenvios usam operacaoId; outra sessão não sobrescreve comprovantes existentes. O histórico exige acesso do próprio ponto. Nenhum comprovante fica no anúncio público ou no Room.

RF16 agora inclui **somente perfil confirmado** no Room, apagado ao sair; não há cache Room de objetos/pontos/comprovantes, login offline ou fila de entregas. Cloud Storage não é utilizado; Firestore Spark continua sujeito às cotas gratuitas. Os campos Base64 de fotos públicas e privadas não são indexados. O índice de inventário está em firestore.indexes.json.

Regras de entregas e índice por ponto/situação publicados e ativados no projeto Firebase da D2G em 06/11/2026, com autorização do responsável.
