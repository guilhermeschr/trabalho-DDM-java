# Achados e Perdidos — DDM

Aplicativo Android em Java para cadastrar objetos encontrados, localizar pontos de coleta e registrar retiradas. Projeto da disciplina de Desenvolvimento para Dispositivos Móveis, com Firebase e Google Maps.

## Documento principal

**[Especificação do aplicativo](ESPECIFICACAO-DO-APP.md)** — referência para requisitos, perfis, permissões, telas, regras de negócio, dados e critérios de aceite.

Consulte e atualize esse documento sempre que houver uma definição ou alteração importante no app.

## Instruções de contribuição

[AGENTS.md](AGENTS.md) reúne as regras de trabalho e os padrões de commits e branches.


## Abrir e executar no Android Studio

1. Abra a pasta raiz deste repositório no Android Studio (Open) e aguarde o Gradle Sync.
2. Use JDK 17 para o Gradle e instale o Android SDK 35. O pacote é `br.edu.ddm.achadoseperdidos` e o Android mínimo é API 24.
3. Configure o caminho do SDK em `local.properties` (gerado pelo Studio; não versionado).
4. Crie ou use seu projeto Firebase, registre o aplicativo com esse pacote e coloque o arquivo baixado em `app/google-services.json` (não versionado).
5. Habilite Authentication por e-mail/senha e crie o Firestore. Aplique as regras de `firestore.rules` ao projeto escolhido. O arquivo fecha acesso aos módulos ainda não implementados; nenhuma regra é publicada automaticamente.
6. Habilite Maps SDK for Android e preencha `MAPS_API_KEY=sua-chave` em `local.properties`. Restrinja a chave ao pacote e ao SHA-1 da assinatura usada. Sem chave, o app explica a configuração ao abrir a seleção de mapa.
7. Selecione o módulo `app` e execute em emulador ou aparelho. Sem configuração Firebase, o app compila e abre, mas informa a configuração pendente e não simula login.

O plugin Google Services só é aplicado quando o JSON existir; após adicioná-lo, sincronize e recompile. Para consultar a assinatura de desenvolvimento: `./gradlew signingReport`.

## Estrutura MVVM

- `ui`: Activity, Fragments XML e seleção de posição no mapa.
- `auth`: ViewModel, estados, validação e interface de repositório; implementação Firebase.
- `data`: entidade, DAO e banco Room de cache de perfil, sem senhas.

Login e cadastro dos dois perfis são a primeira etapa. A tela inicial autenticada é provisória. Listas com Bottom Navigation, câmera para foto do objeto e cache Room de objetos/pontos serão adicionados nas próximas etapas. O cache não permite autenticação ou entregas offline.

## Verificações

```sh
./gradlew testDebugUnitTest assembleDebug lintDebug
```

Testes unitários cobrem validação e estados da autenticação com um repositório de teste, sem acessar contas reais. Para validar a integração, configure Firebase/Maps e teste cadastro comum e de ponto, e-mail duplicado, senha incorreta, saída da conta, rotação e retomada de perfil incompleto. As regras precisam ser publicadas e verificadas no projeto Firebase antes de usar dados reais.
