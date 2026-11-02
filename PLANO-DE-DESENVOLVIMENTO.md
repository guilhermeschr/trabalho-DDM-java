# Login e cadastro — plano de implementação

**Objetivo:** iniciar RF01–RF03 e a seleção de mapa de RF14, em Java/MVVM.
**Especificação:** [ESPECIFICACAO-DO-APP.md](ESPECIFICACAO-DO-APP.md).
**Execução:** nesta sessão e neste repositório, conforme solicitado.

## Decisões

Uma Activity hospeda Fragments XML com componentes Material padrão. AuthViewModel publica estado via LiveData e chama AuthRepository. Firebase é a fonte de autenticação e perfis; Room mantém cache local sem senha e sem conceder login offline. O cadastro incompleto pode ser retomado. O ponto inclui seleção manual no Google Maps. Sem configuração Firebase, o app informa como configurar e não simula autenticação.

## Etapas

- [x] Criar projeto Gradle/Android Studio, Manifest e recursos padrão.
- [x] Testar validação de e-mail, senha, confirmação, perfil e coordenadas.
- [x] Implementar repositório Firebase, cache Room e regras de acesso.
- [x] Implementar ViewModel e testar login, saída, falhas e cadastro incompleto.
- [x] Criar Fragments de login, cadastro, mapa e início autenticado.
- [x] Atualizar especificação/README; compilar, testar e executar lint.

## Verificação

`./gradlew testDebugUnitTest assembleDebug lintDebug`.
Testar manualmente com Firebase configurado: usuário comum, ponto, e-mail duplicado, senha incorreta, retomada de perfil incompleto, voltar após logout e rotação durante requisição. Não publicar regras ou criar projeto externo nesta etapa.

## Escopo posterior

Listas de objetos e pontos com RecyclerView/Adapter, Bottom Navigation, câmera para foto do objeto e cache desses objetos serão implementados nos módulos correspondentes. A base Room desta etapa armazena perfis.

## Resultado da execução

Projeto criado e aberto no Android Studio. `testDebugUnitTest assembleDebug lintDebug` passou: 20 testes, zero falhas e zero erros de lint. O lint registra avisos de versões mais recentes e compatibilidade. Revisão estática adicional não encontrou defeitos importantes.

Atualização em 02/11/2026: JSON do Firebase instalado localmente, fora do versionamento, com applicationId `trabalho.DDM.java`. Login por e-mail/senha confirmado no console e Firestore padrão criado em São Paulo, no plano Spark. Regras existentes publicadas com autorização. Build, 20 testes unitários e lint passaram (zero erros e 11 avisos de lint). Cadastro de usuário comum, saída e novo login validados no emulador com Firebase real e conta fictícia de teste; Chave do Maps fornecida adicionada em `local.properties`, sem versionamento; carregamento do mapa no aparelho ainda precisa de validação. O cache de perfil é gravado e limpo pelo repositório; a leitura de conteúdo offline pertence aos módulos de consulta futuros. Câmera e cache de objetos/pontos estão definidos na especificação e permanecem pendentes.

Verificação visual: APK instalado no emulador, Activity iniciada e telas de login/cadastro abertas e inspecionadas. O emulador apresentou lentidão e um alerta de System UI, recuperado; a checagem não substitui os testes com serviços Firebase/Maps configurados.

Verificação das regras em 02/11/2026: no simulador do console, leitura de perfil sem autenticação foi negada e leitura do próprio perfil com UID correspondente foi permitida. Essas simulações não criam contas ou dados e não substituem o teste completo do aplicativo.

Em 02/11/2026, cadastro de usuário comum, saída e novo login foram validados no emulador com Firebase real, usando uma conta fictícia de teste. O app exibiu o nome e o perfil corretos após o cadastro, voltou à tela de login na saída e recuperou o mesmo perfil após entrar novamente. Cadastro de ponto e mapa continuam pendentes de validação.

## Etapa seguinte — pontos, objetos e navegação (02/11/2026)

- [x] Listar e abrir detalhes de pontos, com endereço, coordenadas e acesso ao mapa.
- [x] Cadastrar objeto sem fotos, exigindo usuário comum, ponto e localização; gravar no Firestore com transação online e ID estável nas tentativas.
- [x] Listar disponíveis, combinar busca por nome/filtro de ponto e abrir detalhes do objeto e de seu ponto.
- [x] Adicionar Bottom Navigation com Objetos, Pontos e Conta, preservando o padrão Java/XML/MVVM.
- [x] Executar build, lint (zero erros) e 32 testes unitários.
- [x] Validar 19 cenários de regras em emuladores Firebase isolados.
- [x] Publicar novas regras de objetos com confirmação no projeto real.
- [ ] Validar o fluxo completo de publicação de objeto com ponto e mapa no aparelho.

Fotos, câmera/Storage, inventário, entregas, histórico e cache Room de objetos/pontos continuam pendentes. Esta etapa não conclui os requisitos de anexos de fotos.

Verificação após publicação em 02/11/2026: a lista de objetos carregou sem erro no Firebase real (sem anúncios), a lista de pontos exibiu o ponto de teste existente e seus detalhes abriram corretamente. O formulário de objeto foi inspecionado e impediu envio com nome vazio. O mapa abriu, mas não carregou: o SDK registrou Authorization failure para a chave configurada. A origem foi identificada na conta de trabalhos indicada pelo responsável: projeto gmp-demo-project-991730262 (Maps Platform Demo Project), chave Maps Platform Demo API Key. Foi confirmado que o valor corresponde à chave instalada localmente. Trata-se de uma Maps Demo Key, autorizada para sete APIs, sem Maps SDK for Android. Conforme a documentação oficial (https://developers.google.com/maps/demo-key), serviços fora do conjunto de demonstração exigem vincular uma conta de faturamento ou usar uma chave padrão com faturamento. A configuração financeira depende do responsável; não foi alterada. Após obter uma chave habilitada para Android, conferir o pacote trabalho.DDM.java e a assinatura de desenvolvimento. A publicação completa de um objeto pelo formulário não foi validada nesta verificação.
