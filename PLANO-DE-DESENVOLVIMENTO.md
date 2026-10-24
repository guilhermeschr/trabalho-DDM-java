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

Não há Firebase ou Maps configurados no repositório: autenticação real e mapa com chave ainda precisam de verificação após fornecer as configurações. O cache de perfil é gravado e limpo pelo repositório; a leitura de conteúdo offline pertence aos módulos de consulta futuros. Câmera e cache de objetos/pontos estão definidos na especificação e permanecem pendentes.

Verificação visual: APK instalado no emulador, Activity iniciada e telas de login/cadastro abertas e inspecionadas. O emulador apresentou lentidão e um alerta de System UI, recuperado; a checagem não substitui os testes com serviços Firebase/Maps configurados.
