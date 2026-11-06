# Especificação do aplicativo — Achados e Perdidos

Documento principal de requisitos do repositório. Consulte este arquivo para definir, desenvolver e validar as funcionalidades do app.

**Disciplina:** Desenvolvimento para Dispositivos Móveis (DDM)

**Data:** 06/11/2026

**Versão:** 1.10 — Formulários com teclado sem sobreposição do menu

## 1. Objetivo

Desenvolver um aplicativo Android em Java que ajude pessoas a localizar objetos encontrados e retirá-los em pontos de coleta. O app permite publicar objetos, pesquisar os disponíveis, consultar localizações e registrar a entrega ao responsável pela retirada.

Este documento é a referência para implementar e apresentar o trabalho da faculdade. O escopo prioriza um fluxo completo e simples, usando Firebase para autenticação e armazenamento, Room para cache local, Google Maps para os mapas e câmera para fotografar objetos encontrados.

Neste documento, **objeto encontrado** é o anúncio de um item que alguém encontrou. **Ponto de coleta** é o estabelecimento que guarda esse objeto e realiza a entrega. O local onde o objeto foi encontrado pode ser diferente do local de retirada.

## 2. Perfis e permissões

Cada conta possui um único perfil, escolhido no cadastro e mantido na primeira versão. Cada conta de ponto de coleta representa um estabelecimento.

| Ação | Usuário comum | Ponto de coleta |
| --- | --- | --- |
| Criar conta, entrar e sair | Sim | Sim |
| Consultar objetos disponíveis, buscar e filtrar | Sim | Sim |
| Ver detalhes e localização de um objeto disponível | Sim | Sim |
| Consultar a lista e os detalhes dos pontos de coleta | Sim | Sim |
| Cadastrar um objeto encontrado | Sim | Não |
| Usar localização atual como sugestão em um cadastro permitido | Sim | Sim, no cadastro do próprio ponto |
| Aparecer na lista de pontos de coleta | Não | Sim |
| Consultar objetos disponíveis e entregues de um ponto | Não | Somente do próprio ponto |
| Registrar a entrega de um objeto | Não | Somente dos objetos do próprio ponto |
| Consultar descrição e fotos de uma entrega | Não | Somente das entregas do próprio ponto |

O acesso às funcionalidades exige login. Quando este documento mencionar informações públicas, significa informações visíveis aos dois perfis autenticados, sem acesso aos comprovantes de retirada.

## 3. Requisitos funcionais

| ID | Requisito | Critério de aceite |
| --- | --- | --- |
| RF01 | Cadastrar usuário comum com nome, e-mail e senha. | Com dados válidos e e-mail não utilizado, a conta é criada com perfil de usuário comum e permite acessar o app. |
| RF02 | Cadastrar ponto de coleta com nome do estabelecimento, e-mail, senha, endereço e localização no mapa. | Após concluir o cadastro, o estabelecimento aparece na lista de pontos e a conta acessa as funções do seu perfil. |
| RF03 | Permitir login por e-mail e senha e saída da conta para os dois perfis. | Credenciais válidas abrem o app; credenciais inválidas exibem uma mensagem; após sair, telas protegidas exigem novo login. |
| RF04 | Cadastrar um objeto encontrado. | Um usuário comum consegue salvar nome, descrição, localização aproximada em texto e coordenadas, ponto de retirada e fotos opcionais, capturadas pela câmera ou selecionadas na galeria. O objeto fica disponível após o cadastro ser confirmado. |
| RF05 | Listar os objetos disponíveis. | A lista mostra nome, local aproximado em texto, nome do ponto de retirada e uma foto quando existir; objetos entregues não aparecem. |
| RF06 | Pesquisar objetos pelo nome. | A busca retorna os objetos disponíveis cujo nome contém o texto informado, sem diferenciar maiúsculas e minúsculas. Texto vazio mantém a lista sem restrição por nome. |
| RF07 | Filtrar objetos por ponto de coleta. | Ao selecionar um ponto, somente seus objetos disponíveis aparecem. Busca e filtro podem ser combinados, e remover o filtro volta a considerar todos os pontos. |
| RF08 | Exibir os detalhes de um objeto disponível. | A tela apresenta nome, descrição, fotos quando houver, local aproximado em texto e no mapa e ponto de retirada vinculado. |
| RF09 | Consultar pontos de coleta. | Os dois perfis conseguem abrir a lista de estabelecimentos e consultar nome, endereço e localização no mapa de cada ponto. |
| RF10 | Consultar os objetos do próprio ponto. | O ponto visualiza apenas seu inventário, separado entre disponíveis e entregues. |
| RF11 | Registrar uma entrega com descrição e pelo menos uma foto. | O ponto vinculado consegue concluir a retirada de um objeto disponível; descrição, fotos, data/hora e responsável pelo registro ficam associados à entrega. |
| RF12 | Atualizar a situação após a entrega. | Após a gravação confirmada, o objeto fica entregue, sai das buscas de disponíveis e permanece no histórico do ponto. |
| RF13 | Consultar o histórico e os comprovantes das próprias entregas. | O ponto abre um objeto entregue e consulta os dados do objeto, a descrição, as fotos e a data/hora da retirada. Outros usuários e pontos não acessam os comprovantes. |
| RF14 | Selecionar localizações manualmente no mapa. | No cadastro de objeto e de ponto, o usuário consegue marcar e ajustar uma posição sem conceder acesso à localização atual do celular. |
| RF15 | Fotografar um objeto encontrado usando a câmera do celular. | No cadastro do objeto, o usuário pode abrir a câmera, confirmar a foto e anexá-la ao anúncio; cancelar a captura mantém o formulário e não cria anexo. |
| RF16 | Manter cache local com Room. | Perfis confirmados são armazenados localmente nesta etapa; os módulos de consulta deverão armazenar objetos disponíveis e pontos já carregados. O cache nunca autentica usuários, armazena senhas ou autoriza entregas. |
| RF17 | Sugerir a localização atual ao selecionar um local no mapa. | Sem posição anterior, o app pede permissão de localização em contexto e tenta pré-selecionar a posição do aparelho. O usuário pode ajustar o marcador e precisa confirmar. Uma posição já escolhida é preservada; o botão “Usar minha localização” permite solicitar outra sugestão. Permissão negada, GPS desligado ou ausência de posição não impedem a seleção manual. |

## 4. Regras de negócio

| ID | Regra |
| --- | --- |
| RN01 | O objeto deve ser cadastrado depois de ser deixado fisicamente no ponto escolhido. A tela de cadastro orienta o usuário sobre isso. Não existe confirmação de recebimento pelo ponto. |
| RN02 | Todo objeto tem exatamente um ponto de retirada existente. Se não houver pontos cadastrados, o app informa a situação e impede concluir o cadastro do objeto. |
| RN03 | As únicas situações são `DISPONIVEL` e `ENTREGUE`. O cadastro começa em `DISPONIVEL`; uma entrega concluída muda para `ENTREGUE`. Não há reabertura nesta versão. |
| RN04 | Somente a conta do ponto vinculado pode entregar o objeto. Ser o autor do anúncio não permite registrar a retirada. |
| RN05 | A entrega exige descrição não vazia e pelo menos uma foto que ajude a identificar quem retirou o objeto. Exemplo de descrição: nome informado pelo recebedor e como o item foi reconhecido. O recebedor não precisa ter conta no app. |
| RN06 | As fotos do objeto são opcionais e visíveis junto ao anúncio. As fotos e a descrição da entrega são privadas para o ponto responsável. Os dois tipos de foto não devem ser misturados. |
| RN07 | Um objeto pode ter apenas uma entrega concluída. Dois toques no botão ou duas sessões da mesma conta não podem gerar entregas duplicadas. |
| RN08 | O objeto só pode ser marcado como entregue depois do envio das fotos e da gravação conjunta do registro de entrega e da nova situação. Uma falha no envio dos comprovantes não pode concluir a retirada. |
| RN09 | O local aproximado do encontro é registrado em texto e por um marcador no mapa. O ponto de retirada usa seu próprio endereço e suas próprias coordenadas. |
| RN10 | O ponto aparece na lista assim que seu cadastro estiver completo. Não há aprovação administrativa. |
| RN11 | Nome, descrição e textos de localização obrigatórios não podem conter apenas espaços. E-mail deve ser válido, e a senha deve cumprir a política configurada no Firebase Authentication. |
| RN12 | Objetos e entregas são preservados para consulta do histórico. Edição e exclusão de anúncios, alteração de perfil e transferência de objetos entre pontos não fazem parte desta versão. |
| RN13 | A localização do aparelho é apenas uma sugestão para formulários, podendo ser aproximada. Ela não substitui uma posição anterior automaticamente nem uma escolha manual feita durante a busca. O usuário deve conferir o local correto do encontro ou do ponto e confirmar; consultar um anúncio/ponto não solicita localização do aparelho. |

## 5. Telas previstas

| Tela | Conteúdo e ações principais |
| --- | --- |
| Login | E-mail, senha, entrar e acesso ao cadastro. |
| Cadastro | Escolha do perfil e formulário correspondente; cadastro de ponto inclui endereço e seleção no mapa. |
| Objetos disponíveis | Lista, busca pelo nome, filtro por ponto e acesso aos detalhes. Para usuário comum, botão para cadastrar objeto. |
| Cadastro de objeto | Nome, descrição, texto do local aproximado, seleção no mapa, seleção do ponto, fotos opcionais e salvar. |
| Detalhes do objeto | Informações do anúncio, fotos, marcador do local encontrado e acesso aos detalhes do ponto de retirada. |
| Pontos de coleta | Lista de estabelecimentos e acesso aos detalhes. |
| Detalhes do ponto | Nome, endereço e mapa com marcador do estabelecimento. |
| Meus objetos — ponto de coleta | Abas ou filtros de disponíveis e entregues, com acesso aos detalhes de cada objeto. |
| Registrar entrega | Resumo do objeto, descrição da retirada, seleção de uma ou mais fotos e botão de confirmação. |
| Detalhes da entrega | Dados do objeto entregue, descrição, fotos e data/hora; acesso restrito ao ponto responsável. |

A seleção de localização pode ser uma tela reutilizada ou um componente dos formulários. Cada mapa de detalhes deve permitir visualizar e explorar a posição marcada. A primeira versão não exige mapa geral com todos os anúncios, cálculo de distância ou rotas. Nos formulários, uma posição ainda não preenchida pode ser sugerida a partir da localização atual autorizada; existe também o botão “Usar minha localização”. A confirmação manual do marcador continua obrigatória.

A navegação implementada usa Bottom Navigation com Objetos, Pontos e Conta (perfil e saída). Detalhes e cadastro abrem em Fragments internos com retorno à lista. O acesso “Meus objetos” será adicionado com o inventário do ponto.

## 6. Fluxos principais

### 6.1. Publicar e localizar um objeto

1. O usuário comum cria sua conta ou faz login.
2. Depois de deixar o item em um ponto cadastrado, abre o cadastro de objeto.
3. Preenche os dados, marca onde encontrou o objeto e escolhe o ponto de retirada. Ao abrir um mapa sem posição anterior, pode autorizar a localização para receber uma sugestão inicial; deve ajustar para o local do encontro e confirmar. Sem permissão ou posição disponível, marca manualmente.
4. Se desejar, fotografa o objeto com a câmera ou seleciona fotos pela galeria do celular.
5. O app valida e salva o cadastro. Quando confirmado, o objeto aparece como disponível.
6. Outro usuário autenticado pesquisa, filtra e abre os detalhes para identificar o item e consultar onde retirá-lo.

### 6.2. Retirar um objeto

1. A pessoa vai ao ponto indicado e se identifica presencialmente. Não existe solicitação de retirada ou reserva pelo app.
2. O ponto abre o objeto na sua lista de disponíveis e seleciona “Entregar”.
3. Registra uma descrição e seleciona pela galeria pelo menos uma foto relacionada à identificação de quem recebeu o item.
4. O app envia as fotos e confirma a gravação da entrega e da mudança de situação.
5. O objeto sai da lista geral e aparece entre os entregues do ponto, com os comprovantes privados.

## 7. Dados principais

O Firebase Authentication gerencia credenciais. A senha não é armazenada nas coleções do aplicativo. Os registros do Firestore guardam os dados do domínio e referências aos arquivos do Cloud Storage.

| Entidade / coleção | Campos principais | Relações |
| --- | --- | --- |
| Usuário — `usuarios/{uid}` | UID, nome de exibição, perfil (`COMUM` ou `PONTO_COLETA`) e data de criação. | UID corresponde à conta do Authentication. Para ponto, o nome de exibição é o nome do estabelecimento. |
| Ponto de coleta — `pontosColeta/{uid}` | UID do responsável, nome do estabelecimento, endereço, latitude, longitude e data de criação. | Um ponto por conta de perfil `PONTO_COLETA`; o ID é o UID da conta. |
| Objeto encontrado — `objetos/{id}` | ID, nome, descrição, local aproximado em texto, latitude, longitude, UID do autor, ID do ponto, situação, referências das fotos opcionais e data de cadastro. | Um autor, um ponto de retirada e, após a retirada, uma entrega. |
| Entrega — `entregas/{objetoId}` | ID do objeto, ID do ponto, UID de quem registrou, descrição, referências das fotos e data/hora da entrega. | O ID igual ao do objeto permite manter um único registro de entrega por objeto. |

O e-mail é usado pelo Authentication e não precisa ser exposto nos registros públicos do ponto. A conta acessa os próprios dados de perfil; consultas a estabelecimentos usam a coleção de pontos.

As fotos dos anúncios ficam em Base64 no Firestore: até cinco JPEGs opcionais de 102.400 bytes cada em `objetos/{id}/fotos/{slot}` (slots ordenados de `0` a `4`, campos `base64` e `tipo`). O anúncio guarda as referências `fotos` e `miniaturaBase64` da primeira foto, de até 12.288 bytes. Esses campos Base64 não são indexados. Anúncio e fotos são criados em uma transação online, sem anexos posteriores. A lista carrega apenas a capa; os detalhes consultam as imagens completas. Objetos antigos sem capa continuam compatíveis. Comprovantes de entrega continuam previstos separadamente no Cloud Storage, ainda pendentes de implementação e configuração. Comprovantes são carregados com acesso autenticado e restrito ao ponto, sem URLs públicas ou links compartilháveis de download gravados nos documentos do anúncio.

## 8. Organização técnica

| Tecnologia | Uso no projeto | Referência oficial |
| --- | --- | --- |
| Android com Java, layouts XML e MVVM | Uma Activity hospeda Fragments. ViewModels expõem estado via LiveData e delegam operações aos repositórios. Componentes Material padrão compõem a interface. | [Arquitetura Android](https://developer.android.com/topic/architecture) |
| Room | Cache local de perfis confirmados e, nas próximas etapas, objetos e pontos consultados. Firebase continua sendo a fonte de verdade; sem fila de alterações offline. | [Room](https://developer.android.com/training/data-storage/room) |
| Câmera | Captura de foto do objeto encontrado, com arquivo privado e compartilhamento por FileProvider quando necessário. | [Captura de fotos](https://developer.android.com/media/camera/camera-deprecated/photobasics) |
| Firebase Authentication | Cadastro e login com e-mail e senha. | [Autenticação por senha no Android](https://firebase.google.com/docs/auth/android/password-auth) |
| Cloud Firestore | Usuários, pontos, objetos e entregas; transação para registrar a entrega e alterar a situação juntas. | [Transações e gravações em lote](https://firebase.google.com/docs/firestore/manage-data/transactions) |
| Cloud Storage for Firebase | Somente comprovantes de entrega futuros; fotos dos anúncios usam Firestore/Spark. | [Upload de arquivos no Android](https://firebase.google.com/docs/storage/android/upload-files) |
| Localização atual — Google Play services Location 21.3.0 | Consulta pontual e cancelável em primeiro plano, com permissão aproximada ou precisa. Não exige API key ou faturamento adicional. | [Localização no Android](https://developer.android.com/develop/sensors-and-location/location) |
| Google Maps JavaScript API em WebView | Seleção e visualização de posições com marcadores, usando Maps Demo Key para o protótipo sem faturamento. AndroidX WebKit carrega a página local e limita a comunicação à origem do app. | [Maps Demo Key](https://developers.google.com/maps/documentation/javascript/demo-key), [suporte a WebView](https://developers.google.com/maps/documentation/javascript/browsersupport) |

O aplicativo acessa o Firebase pelos SDKs e o Google Maps pela API JavaScript em WebView, sem servidor próprio. O padrão MVVM é obrigatório: Fragments exibem os formulários e observam LiveData; AuthViewModel valida entradas e coordena estados; AuthRepository encapsula Firebase Authentication, Firestore e cache Room. As telas não acessam Firebase diretamente. Uma única Activity hospeda os Fragments, e senhas não são armazenadas em Room, arquivos, logs ou estado persistido de telas.

O login somente libera a área autenticada após confirmar o perfil no servidor. Se a conta existir no Authentication e o documento de perfil ainda não existir no Firestore, o usuário é direcionado para concluir o cadastro sem criar outra conta. Usuário e ponto são gravados juntos por lote no Firestore. O cadastro valida confirmação de senha e um mínimo inicial de seis caracteres; a política do Firebase pode exigir mais. Ao sair, a sessão é encerrada e o cache do perfil é apagado.

Na primeira etapa, Room mantém o perfil confirmado, com acesso ao banco fora da thread principal. O cache de objetos e pontos será adicionado com as respectivas telas; conteúdo local deverá ser identificado como potencialmente desatualizado e nunca permitirá entregas offline. Os dados de autorização continuam dependendo de confirmação no servidor.

O identificador Android (`applicationId`) é `trabalho.DDM.java`, registrado no projeto Firebase atual `trabalho-ddm-java-3cc2a`, da conta D2G Trabalhos. O arquivo `app/google-services.json` é instalado localmente e permanece fora do versionamento; `.firebaserc` identifica o projeto padrão para a CLI. O namespace das classes Java permanece `br.edu.ddm.achadoseperdidos`, com Android mínimo 7.0 (API 24), compilação/alvo API 35 e Java 17. As telas usam o tema padrão Material 3 e Fragments, sem personalização visual complexa.

A configuração atual usa o plano Spark, Authentication por e-mail/senha e Firestore Standard, banco `(default)`, em `southamerica-east1` (São Paulo), com as regras de `firestore.rules` publicadas. O novo projeto não contém as contas e os dados do projeto anterior; não houve migração. O Google Maps usa a Maps Demo Key do projeto separado `gmp-demo-project-991730262`, da mesma conta D2G, sem vincular faturamento. Storage continua pendente de configuração.

Por autorização do responsável, a integração nativa foi substituída por Google Maps JavaScript API em WebView para usar a Maps Demo Key gratuita, sem cartão ou faturamento. `MAPS_API_KEY` continua em `local.properties`, ignorado pelo Git, e é incluída no APK como configuração do mapa. A chave Demo é destinada a protótipos e tem limites diários do provedor; não atende à publicação em produção. O SDK nativo e suas restrições por pacote/SHA-1 deixam de ser usados pelo mapa.

`MapPickerDialog` carrega `assets/map.html` por HTTPS local com `WebViewAssetLoader` e AndroidX WebKit 1.12.1. Apenas a página principal da origem `https://appassets.androidplatform.net` envia mensagens de carregamento e seleção; coordenadas são validadas no Java e não são aceitas no modo de consulta. Navegação para outras páginas, a geolocalização direta pelo JavaScript, acesso a arquivos e conteúdo local e conteúdo HTTP misto ficam desativados. A localização opcional do aparelho é consultada pelo Android e só é repassada ao mapa para sugerir o marcador. É necessário Android System WebView atualizado com suporte a `WEB_MESSAGE_LISTENER`. A API externa recebe a chave e as coordenadas necessárias à exibição do mapa, sem credenciais ou perfil do Firebase.

A localização atual usa `ACCESS_COARSE_LOCATION` e `ACCESS_FINE_LOCATION`, solicitadas juntas para permitir a escolha entre localização aproximada e precisa quando o Android oferecer essa opção. Não há permissão de localização em segundo plano. Sem posição preenchida, o mapa tenta sugerir a posição atual; a permissão é solicitada automaticamente apenas na primeira tentativa, e novas solicitações podem ser feitas pelo botão “Usar minha localização”. Negar a permissão mantém o mapa manual. Uma posição existente só é substituída ao usar esse botão. A consulta pontual aceita posição recente de até dez segundos, espera no máximo doze segundos e é cancelada ao escolher manualmente, sair da tela ou fechar o diálogo. Respostas atrasadas não sobrescrevem a escolha manual. Sem posição ou com localização do aparelho desligada, o app informa a situação e mantém o mapa utilizável. Não há rastreamento, histórico de deslocamento ou gravação automática da sugestão.

O mapa exibe carregamento, informa falhas de rede, autorização ou tempo limite e oferece nova tentativa. A confirmação exige mapa pronto e uma posição válida. A seleção é preservada ao recriar a tela ou tentar novamente; cancelar não altera as coordenadas do formulário. Nos detalhes, o marcador fica fixo e o usuário pode explorar o mapa. Textos de localização e endereço permanecem nas telas nativas.

Para o volume pequeno da demonstração, a busca por nome pode ser feita no app sobre os objetos disponíveis carregados. O filtro por ponto deve produzir o mesmo resultado quando combinado com essa busca. Não é necessário contratar um serviço de busca.

No registro da entrega, primeiro são enviados os comprovantes ao Storage. Depois, uma transação no Firestore verifica se o objeto ainda está disponível e pertence ao ponto autenticado, cria a entrega e atualiza sua situação. Essa transação cobre os registros do Firestore; não inclui o upload dos arquivos. Arquivos enviados sem uma entrega concluída não constituem um comprovante de retirada salva.

As permissões devem ser aplicadas nas regras do Firestore e do Storage, além da interface: restringir comprovantes ao ponto responsável, impedir troca de perfil e de ponto por alteração direta dos dados e permitir a mudança de situação apenas no fluxo autorizado de entrega. Esconder botões não substitui essas regras.

Antes da demonstração, o projeto precisa ter Firebase configurado, login por e-mail/senha habilitado, Firestore e Storage com regras de acesso aplicadas e a chave Demo do Google Maps configurada para a página web exibida dentro do app Android.

## 9. Validações e tratamento de falhas

| Situação | Comportamento esperado |
| --- | --- |
| Campo obrigatório vazio ou preenchido apenas com espaços | Mostrar qual campo precisa ser corrigido e não concluir a operação. |
| E-mail inválido, e-mail já cadastrado ou senha rejeitada | Exibir mensagem compreensível e permitir corrigir os dados. |
| Falha no login | Informar que não foi possível entrar e permitir tentar novamente. |
| Conta criada no Authentication, mas cadastro de perfil incompleto | Não liberar as funções do app nem listar um ponto incompleto; permitir concluir os dados ao retomar o cadastro ou entrar novamente. |
| Busca ou filtro sem resultados | Exibir “Nenhum objeto encontrado” e permitir alterar a busca ou remover o filtro. |
| Nenhum ponto cadastrado | Exibir mensagem na lista e orientar que é necessário um ponto antes de cadastrar objetos. |
| Sem conexão ou falha ao carregar dados | Exibir erro e opção de tentar novamente; não confundir falha com lista vazia. |
| Permissão de localização negada, localização do aparelho desligada ou consulta sem resultado | Informar a situação e manter a escolha manual do marcador. Preservar coordenadas anteriores; permitir outra tentativa pelo botão “Usar minha localização”. |
| Falha no mapa, falta de suporte do WebView ou limite da Demo Key | Informar a falha; quando aplicável, oferecer nova tentativa ou orientar a atualização do Android System WebView. Manter textos de localização e endereço visíveis. Preservar o formulário e a seleção anterior; só permitir nova confirmação com mapa pronto e coordenadas válidas. |
| Falha no upload de fotos do objeto | Preservar o formulário e não anunciar sucesso. Permitir tentar novamente ou remover as fotos opcionais antes de salvar. |
| Retirada sem descrição ou sem fotos | Impedir a confirmação e informar os campos obrigatórios. |
| Falha no envio dos comprovantes | Não marcar o objeto como entregue. Preservar o formulário enquanto a tela estiver aberta e permitir nova tentativa. |
| Resposta incerta ao salvar uma entrega | Consultar o estado gravado antes de repetir a operação, evitando duplicação ou indicação incorreta de falha. |
| Tentativa de entrega por outro ponto | Negar a operação, inclusive se a tentativa for feita diretamente contra o Firebase. |
| Objeto já entregue ou dois envios simultâneos | Manter uma única entrega, atualizar a tela e informar que o objeto já foi entregue. |

Durante operações de gravação, o app mostra carregamento e impede novos toques no botão de confirmação. O sucesso só é informado após a confirmação do serviço. Operações offline com sincronização posterior não são requisito da primeira versão.

## 10. Requisitos não funcionais

| ID | Requisito | Condição de verificação |
| --- | --- | --- |
| RNF01 | Interface simples em português. | Títulos, botões, mensagens e campos permitem realizar os fluxos sem instruções externas. |
| RNF02 | Execução em Android com Java. | O aplicativo instala e permite demonstrar os fluxos em aparelho ou emulador Android com suporte aos serviços utilizados. |
| RNF03 | Persistência dos dados. | Após fechar e abrir o app e entrar na conta, os registros salvos continuam disponíveis conforme as permissões. |
| RNF04 | Controle de acesso no Firebase. | Usuário comum e outro ponto não conseguem consultar comprovantes ou registrar entregas de um ponto diferente. |
| RNF05 | Feedback das operações. | Carregamento, sucesso, validação e falhas ficam visíveis, sem marcar como concluída uma operação que falhou. |
| RNF06 | Funcionamento sem localização atual do aparelho. | Negar a permissão, desligar a localização do aparelho ou não obter uma posição não impede a marcação manual nem a consulta dos mapas. A sugestão atual é opcional. |

## 11. Cenários de aceite

Usar na demonstração pelo menos um usuário comum, dois pontos de coleta e objetos vinculados a cada ponto. Os cenários abaixo são verificações do app a ser desenvolvido, não funcionalidades já implementadas.

| Cenário | Procedimento | Resultado esperado | Requisitos |
| --- | --- | --- | --- |
| CA01 — Contas e sessão | Criar uma conta de cada perfil, sair e entrar novamente; tentar credenciais inválidas. | Perfis e permissões corretos; login inválido mostra erro; saída bloqueia acesso protegido. | RF01–RF03 |
| CA02 — Publicação | Publicar um objeto com todos os campos obrigatórios, primeiro sem foto e depois outro com foto. | Ambos aparecem disponíveis com o ponto e o local informado; fotos aparecem quando cadastradas. | RF04–RF05 |
| CA03 — Busca e filtro | Criar “Mochila azul” em um ponto e “Chave” em outro; buscar “MOCHILA”, filtrar pelo ponto e combinar as duas condições. | A busca ignora maiúsculas; o filtro limita o ponto; a combinação retorna apenas a interseção; uma combinação sem correspondência exibe estado vazio. | RF06–RF07 |
| CA04 — Detalhes e mapas | Abrir objeto e ponto com coordenadas diferentes; selecionar locais manualmente sem permissão de localização atual. | Cada tela mostra o marcador e o texto correspondentes; a falta da permissão não bloqueia o fluxo. | RF08–RF09, RF14, RNF06 |
| CA05 — Inventário separado | Entrar em cada ponto e abrir seus objetos. | Cada ponto consulta somente seu inventário, com disponíveis e entregues separados. | RF10 |
| CA06 — Entrega completa | Entregar um objeto com descrição e foto; reabrir o app. | Existe uma entrega; o objeto saiu da busca e permanece entregue no histórico, com os comprovantes. | RF11–RF13, RNF03 |
| CA07 — Entrega incompleta ou com falha | Omitir descrição, omitir foto e simular falha de upload em tentativas distintas. | Nenhuma tentativa conclui a entrega; o app explica o problema e permite corrigir ou tentar novamente. | RF11–RF12, RNF05 |
| CA08 — Acesso indevido | Tentar entregar e ler comprovantes usando usuário comum ou outro ponto, incluindo acesso direto aos serviços. | Firestore e Storage negam as operações não autorizadas. | RF13, RNF04 |
| CA09 — Repetição e concorrência | Tocar duas vezes em entregar ou enviar por duas sessões do mesmo ponto. | Somente uma entrega é persistida; a situação final é entregue. | RF11–RF12 |
| CA10 — Erros e ausência de dados | Testar cadastro inválido, lista sem pontos, busca vazia, falha de carregamento e falha do mapa. | Validações e mensagens seguem a seção 9; dados já preenchidos são preservados durante a tentativa. | RF01–RF09, RNF05 |
| CA11 — Câmera | Abrir a câmera no cadastro de objeto, confirmar uma foto e depois cancelar outra captura. | A foto confirmada é anexada; cancelar não adiciona arquivo nem apaga o formulário. | RF15 |
| CA12 — Cache local | Carregar dados, consultar o cache Room, encerrar sessão e tentar autenticar sem rede. | Dados confirmados são persistidos; o perfil em cache é apagado na saída e não libera login offline. | RF16 |
| CA13 — Localização atual opcional | Abrir mapa sem posição e autorizar localização; abrir com posição anterior; usar o botão de localização; negar permissão/desligar GPS; escolher manualmente enquanto a busca está em andamento; consultar detalhes. | Sugestão inicial ajustável e confirmada pelo usuário; posição anterior preservada; seleção manual disponível em falhas e sem ser substituída por resposta atrasada; detalhes não consultam a posição do aparelho. | RF14, RF17, RN13, RNF06 |

## 12. Lista de verificação para o desenvolvimento

- [x] Firebase atual configurado na conta D2G, arquivo Android instalado e app aberto no emulador em 05/11/2026; cadastro/login completos no novo projeto ainda pendentes de validação.
- [ ] RF01–RF03: cadastro dos dois perfis, login e saída.
- [x] RF02 e RF09: código de cadastro, listagem e detalhes dos pontos; mapa web carregado no emulador e consulta sem edição validada. Cadastro completo de ponto no novo Firebase ainda pendente de validação.
- [x] RF04: cadastro com ponto obrigatório e até cinco fotos opcionais implementado; validação manual completa com Firebase real pendente.
- [x] RF05–RF07: lista de disponíveis, busca e filtro combinados (anúncios sem fotos nesta etapa).
- [x] RF08 e RF14: código de detalhes e mapas para objetos e pontos, sem fotos; mapa web, seleção, confirmação de coordenadas e preservação ao recriar a tela validados no emulador. Fluxo completo com objetos reais ainda pendente de validação.
- [ ] RF10: inventário do próprio ponto separado por situação.
- [ ] RF11–RF12: entrega com descrição, foto obrigatória e atualização consistente.
- [ ] RF13: histórico com comprovantes privados.
- [ ] RNF01–RNF06: interface, persistência, permissões e feedback.
- [ ] Seção 9: validações, estados vazios e tratamento de falhas.
- [x] RF15: captura com câmera externa e FileProvider implementada; validação manual em aparelho pendente.
- [ ] RF16: cache Room de objetos e pontos, além do perfil.
- [ ] CA01–CA13: execução e registro dos resultados dos cenários de aceite.
- [x] RF17: código de pré-seleção opcional e botão de localização atual; validação no emulador registrada no andamento.

## 13. Limites da primeira versão

Não fazem parte deste trabalho: anúncios de objetos perdidos, chat, notificações, reservas, aprovação administrativa de pontos, confirmação de recebimento pelo ponto, transferência de objetos, edição ou exclusão de anúncios, troca de perfil, cadastro de múltiplos estabelecimentos na mesma conta, login social, recuperação de senha pelo app, mapa geral de anúncios, rotas, busca por distância ou modo offline completo com sincronização de escritas.

O cadastro de objeto oferecerá captura pela câmera e seleção pela galeria. Os comprovantes de entrega continuam sendo selecionados pela galeria; a câmera exigida pelo trabalho será usada para fotografar o objeto encontrado. A conferência de que a pessoa pode retirar o item acontece presencialmente pelo ponto; o app registra a entrega, sem reconhecimento automático de identidade.

O nome definitivo do aplicativo e seu estilo visual podem ser definidos durante o desenvolvimento, sem alterar estes requisitos. O desenvolvimento foi iniciado com o projeto Android, login, cadastro e estrutura de cache de perfil. Recursos ainda não concluídos permanecem no checklist. A configuração e publicação dos serviços externos dependem do projeto Firebase e das chaves do responsável.


## 14. Atendimento ao enunciado T1 e andamento

O enunciado exige seis telas, navegação por Fragments com Bottom Navigation ou Drawer, duas listas de contextos distintos, MVVM, duas opções de persistência/integração e dois recursos da lista do trabalho. O app atenderá com listas de objetos e pontos (RecyclerView e Adapter), Firebase + Room e Google Maps + câmera. A navegação principal com Bottom Navigation está implementada, com listas de objetos e pontos em RecyclerView e Adapter, além da aba Conta.

### Registro anterior — 02/11/2026

Os testes abaixo pertencem ao projeto anterior; a configuração vigente está na etapa de 05/11/2026 ao final desta seção.

Implementado nesta etapa: estrutura Gradle importável no Android Studio, login e cadastro dos dois perfis, validações, estados de carregamento/erro, encerramento de sessão, retomada de perfil incompleto, seleção do ponto no mapa e armazenamento local do perfil. A área autenticada contém listas e detalhes de pontos e objetos disponíveis, busca por nome sem diferenciar maiúsculas, filtro combinado por ponto e cadastro de objeto sem fotos nesta etapa. O arquivo Firebase fornecido foi instalado localmente e o applicationId foi ajustado ao app registrado no projeto `trabalho-ddm-java`. Authentication por e-mail/senha está habilitado e o Firestore padrão foi criado em `southamerica-east1` (São Paulo), no plano Spark. As regras existentes em `firestore.rules` foram publicadas com autorização do responsável. Build, 20 testes unitários e lint passaram (zero erros de lint). Em 02/11/2026, cadastro de usuário comum, saída e novo login foram validados no emulador com Firebase real, usando uma conta fictícia de teste. O app exibiu o nome e o perfil corretos após o cadastro, voltou à tela de login na saída e recuperou o mesmo perfil após entrar novamente. Cadastro de ponto e mapa continuam pendentes de validação. A chave do Maps fornecida foi adicionada em `local.properties`, fora do versionamento. O carregamento do mapa no aparelho ainda precisa ser validado, incluindo habilitação do Maps SDK for Android e restrições da chave no Google Cloud. Câmera e cache de objetos/pontos são requisitos definidos, ainda pendentes de implementação.

Verificação das regras em 02/11/2026: no simulador do console, leitura de perfil sem autenticação foi negada e leitura do próprio perfil com UID correspondente foi permitida. Essas simulações não criam contas ou dados e não substituem o teste completo do aplicativo.

### Etapa de pontos, objetos e navegação — 02/11/2026

Quatro módulos implementados: lista/detalhes de pontos; cadastro de objeto sem fotos; lista/detalhes de objetos com busca e filtro combinados; navegação principal. Fragments compartilham CatalogViewModel por sessão da área autenticada e usam CatalogRepository para acessar o Firestore. As listas consultam o servidor ao entrar na área, trocar de aba, tocar em Atualizar ou salvar um objeto. Falhas são distintas de ausência de dados; há nova tentativa. O cadastro exige perfil COMUM, nome, descrição, local em texto, coordenadas válidas e ponto existente. A orientação de deixar o objeto no ponto aparece no formulário. Sem pontos, não permite publicar. O formulário mantém os textos durante tentativas e a rotação; coordenadas ficam no ViewModel durante a sessão.

Objetos são gravados em `objetos/{id}` com `nome`, `descricao`, `localAproximado`, `latitude`, `longitude`, `pontoId`, `autorUid`, `situacao`, `fotos` (lista vazia nesta etapa) e `criadoEm`. A criação usa transação online e reutiliza o ID em uma repetição após falha, evitando anúncios duplicados. A lista consulta somente `DISPONIVEL`; pesquisa e filtro são aplicados juntos sobre os objetos carregados. Os detalhes mostram texto, coordenadas e acesso ao mapa do encontro; o ponto de retirada abre seus próprios detalhes e mapa. O mapa de detalhes permite explorar, mas não alterar o registro. As telas de mapa reutilizam MapPickerDialog, mantendo os textos visíveis fora do mapa.

As novas regras permitem criação somente por usuário COMUM com autor correspondente, ponto existente, campos válidos, data do servidor e situação DISPONIVEL. Leitura exige autenticação e objetos disponíveis; alteração, exclusão e entregas permanecem bloqueadas. As regras foram verificadas em 19 cenários nos emuladores oficiais do Firebase, sem dados de produção. As novas regras foram publicadas no projeto real `trabalho-ddm-java` em 02/11/2026, após confirmação do responsável.

Verificações: compilação e lint concluídos sem erros; 32 testes unitários passaram, incluindo combinação de busca/filtro, permissões de perfil, ponto inexistente, validações, reenvio e falhas de rede. Fotos, Storage, inventário, entregas, histórico e cache Room de objetos/pontos continuam pendentes. RF04/RF08 permanecem parciais quanto às fotos; o escopo final dos requisitos foi preservado.

Verificação após publicação em 02/11/2026: a lista de objetos carregou sem erro no Firebase real (sem anúncios), a lista de pontos exibiu o ponto de teste existente e seus detalhes abriram corretamente. O formulário de objeto foi inspecionado e impediu envio com nome vazio. O mapa abriu, mas não carregou: o SDK registrou Authorization failure para a chave configurada. A origem foi identificada na conta de trabalhos indicada pelo responsável: projeto gmp-demo-project-991730262 (Maps Platform Demo Project), chave Maps Platform Demo API Key. Foi confirmado que o valor corresponde à chave instalada localmente. Trata-se de uma Maps Demo Key, autorizada para sete APIs, sem Maps SDK for Android. Conforme a documentação oficial (https://developers.google.com/maps/demo-key), serviços fora do conjunto de demonstração exigem vincular uma conta de faturamento ou usar uma chave padrão com faturamento. A configuração financeira depende do responsável; não foi alterada. Após obter uma chave habilitada para Android, conferir o pacote trabalho.DDM.java e a assinatura de desenvolvimento. A publicação completa de um objeto pelo formulário não foi validada nesta verificação.

### Configuração do Firebase na conta D2G — 05/11/2026

A configuração vigente substitui a do projeto anterior `trabalho-ddm-java`, citado nos registros de 02/11/2026 acima. Na branch `configurar-firebase-do-aplicativo`, foi criado o projeto `trabalho-ddm-java-3cc2a`, com nome de exibição `trabalho-ddm-java`, na conta D2G Trabalhos, após autorização do responsável e aceite dos Termos do Firebase. O projeto anterior não estava acessível nessa conta. O novo projeto permanece no plano Spark, sem Google Analytics, Gemini ou adesão ao Programa para desenvolvedores do Google nesta configuração.

O app Android foi registrado com pacote `trabalho.DDM.java` e apelido Achados e Perdidos. Seu arquivo de configuração foi baixado do console e instalado em `app/google-services.json`, ignorado pelo Git. Authentication por e-mail/senha foi habilitado. O Firestore Standard foi criado em modo de produção, banco `(default)`, região `southamerica-east1`, e recebeu as mesmas regras de `firestore.rules` do repositório. `.firebaserc` aponta para esse novo projeto. Não foram migradas contas nem coleções do projeto anterior.

Verificações realizadas: `testDebugUnitTest assembleDebug lintDebug` passou, com 32 testes, zero falhas e zero erros de lint (16 avisos). O APK foi instalado e aberto no emulador Medium Phone API 36.1 (`emulator-5554`); a tela de login apareceu sem a mensagem de Firebase não configurado. Uma tentativa pela API com conta fictícia inexistente retornou `INVALID_LOGIN_CREDENTIALS`, confirmando a resposta do Authentication; a leitura de perfil sem autenticação no Firestore foi negada com `PERMISSION_DENIED`. Essas verificações não criaram contas ou dados e não substituem CA01–CA12. Cadastro, login com conta válida e consultas autenticadas no novo projeto ainda precisam de validação.

Nesta instalação não há `MAPS_API_KEY` em `local.properties`; Google Maps continua pendente. Storage e fotos também permanecem pendentes. Nenhuma configuração de faturamento foi alterada.

### Diagnóstico anterior à adaptação do Google Maps — 05/11/2026

O responsável solicitou obter uma chave gratuita pelo Safari, usando a conta D2G. A biblioteca de APIs do projeto `trabalho-ddm-java-3cc2a` mostrou Maps SDK for Android ainda desativado, e a página de faturamento confirmou que não há conta vinculada. A [documentação oficial do SDK](https://developers.google.com/maps/documentation/android-sdk/usage-and-billing) exige faturamento habilitado mesmo quando o uso se enquadra em gratuidade; a [Maps Demo Key](https://developers.google.com/maps/demo-key) sem dados de faturamento não inclui o SDK Android.

Não foi criada uma chave incompatível nem alterado o faturamento. `MAPS_API_KEY` continua vazio, e o mapa ainda não foi validado. A execução de `signingReport` confirmou o SHA-1 de debug `69:C8:C2:46:A3:AB:47:34:40:C1:C5:EC:AD:C0:8E:4B:F7:82:08:27`; a integração web adotada depois não utiliza essa restrição. Naquele momento, aguardava-se definição sobre a integração; a decisão final está registrada na etapa de adaptação abaixo.

A conta D2G possui a chave existente Maps Platform Demo API Key, criada em 02/11/2026 no projeto `gmp-demo-project-991730262`, restrita a sete APIs, incluindo Maps JavaScript API e sem Maps SDK for Android. A [Demo Key suporta mapas web para protótipos](https://developers.google.com/maps/documentation/javascript/demo-key), e a API JavaScript tem [suporte ao Chrome WebView no Android](https://developers.google.com/maps/documentation/javascript/browsersupport). Foi apresentada a opção de adaptar a tela de mapa para essa integração, preservando seleção manual, marcadores e visualização dentro do app; a adaptação foi posteriormente autorizada e está registrada abaixo. A chave não foi usada com a integração nativa incompatível.

### Adaptação do Google Maps para a chave gratuita — 05/11/2026

O responsável autorizou substituir o SDK nativo por Google Maps JavaScript API em WebView. A chave Demo existente foi recuperada pelo Safari na conta D2G e instalada somente em `local.properties`; nenhum faturamento ou permissão da chave foi alterado. O aplicativo mantém Java, XML, Fragments e os fluxos de seleção e consulta previstos em RF02, RF08, RF09, RF14 e RNF06.

Foi removida a dependência `play-services-maps` e adicionada AndroidX WebKit 1.12.1. O mapa local preserva marcadores e coordenadas, distingue consulta de edição e trata falha de carregamento sem permitir confirmação incorreta. Seis testes JavaScript cobrem cliques antes do carregamento, consulta sem edição, falha de autorização/rede/tempo limite, limites das coordenadas e restauração da posição. Os dois testes de integração passaram no emulador Medium Phone API 36.1: o mapa real carregou, um toque selecionou coordenadas válidas e a confirmação devolveu o resultado após recriar a Activity; o modo de consulta carregou o marcador e fechou sem emitir alteração. Foi corrigida a medição da área do WebView e o início do carregamento para impedir confirmação antes de haver uma seleção válida. Ao reabrir o mapa pelo cadastro de ponto, as coordenadas já confirmadas são enviadas novamente ao componente.

Verificações finais: 32 testes unitários Java, seis testes JavaScript e dois testes Android passaram. `testDebugUnitTest assembleDebug lintDebug` concluiu com zero erros de lint e 20 avisos. Os testes Android usam a chave Demo local e acesso à internet; não criam contas nem registros do Firebase. A publicação completa de ponto/objeto no novo Firebase permanece pendente, assim como fotos, entregas, histórico e cache de objetos/pontos.

### Localização atual como sugestão inicial — 06/11/2026

O responsável solicitou pré-selecionar a localização atual. Foi adicionada a consulta pontual nativa com Google Play services Location 21.3.0 e permissões opcionais aproximada/precisa. Um mapa sem posição anterior tenta obter uma sugestão; o botão “Usar minha localização” permite solicitar outra, sem alterar posições existentes automaticamente. A sugestão exige confirmação e pode ser ajustada; não há gravação automática no Firebase nem acesso de localização nos detalhes.

A seleção manual cancela a consulta pendente, e o mapa recusa respostas que chegarem depois dessa escolha. Permissão negada, GPS desligado, ausência de resultado e saída da tela têm tratamento sem bloquear o mapa manual. Verificações finais: 32 testes Java, nove testes JavaScript e seis testes Android passaram; build e lint concluídos com zero erros e 22 avisos. Os quatro novos testes Android verificam sugestão automática sem toque e sem gravação antes da confirmação, preservação de coordenadas anteriores com atualização pelo botão, seleção manual com GPS desligado e retorno sem consultar o provedor quando falta permissão. As coordenadas dos testes são fictícias e o modo de simulação e o estado do serviço foram restaurados. Os dois testes anteriores do mapa também passaram.

Durante a validação, o emulador apresentou falha de DNS; foi reiniciado com DNS explícito somente na execução do AVD, sem apagar dados ou alterar a rede do computador. O Google Maps voltou a carregar e os testes reais foram repetidos com sucesso. A pré-seleção no emulador corresponde à posição simulada do dispositivo, e em um aparelho físico depende da posição fornecida pelo Android.

## 15. Histórico de alterações

| Versão | Data | Alteração |
| --- | --- | --- |
| 1.0 | 19/10/2026 | Definição inicial dos requisitos de Achados e Perdidos. |
| 1.1 | 23/10/2026 | Adoção obrigatória de MVVM e Material padrão; inclusão de câmera para objetos e Room para cache; início de login e cadastro; mapeamento dos critérios do T1. |
| 1.2 | 02/11/2026 | Instalação local do JSON fornecido, ajuste do applicationId para `trabalho.DDM.java`, confirmação do login por e-mail/senha, criação do Firestore em São Paulo e publicação das regras existentes; Maps pendente de chave. |
| 1.3 | 02/11/2026 | Chave do Google Maps fornecida instalada em local.properties, sem versionamento; validação do mapa no aparelho pendente. |
| 1.4 | 02/11/2026 | Listas e detalhes de pontos e objetos, cadastro de objeto sem fotos, busca/filtro combinados, Bottom Navigation e regras de objetos testadas no emulador. |
| 1.5 | 05/11/2026 | Novo projeto Firebase na conta D2G, registro Android, configuração local, Authentication e Firestore com regras publicadas; app recompilado e aberto no emulador. Maps, Storage e validação completa no novo projeto permanecem pendentes. |
| 1.6 | 05/11/2026 | Verificação do Maps na conta D2G: projeto sem faturamento e SDK Android desativado; registro da limitação da Demo Key e da assinatura debug atual. Configuração do mapa permanece pendente. |
| 1.7 | 05/11/2026 | Adaptação autorizada para Maps JavaScript API em WebView com a Demo Key gratuita da D2G, sem faturamento; atualização de dependências, mensagens, validações e testes do mapa. |
| 1.8 | 06/11/2026 | Inclusão de RF17, RN13 e CA13: localização atual opcional como pré-seleção, permissões em contexto, botão de nova tentativa e preservação da escolha manual. |

### Fotos dos objetos — versão 1.9 (06/11/2026)

RF04, RF05, RF08 e RF15: câmera externa e seletor de imagens do Android, sem acesso amplo à galeria. Cancelamento preserva o formulário. Prévia, contador e remoção antes da publicação; primeira foto é a capa. O app aplica o teto de cinco inclusive no seletor alternativo. Preparação em segundo plano corrige orientação EXIF, remove metadados e converte JPEG com lado maior inicial de 1.280 pixels, reduzindo qualidade/resolução para respeitar 100 KiB. Erros preservam anexos já preparados. Rotação mantém anexos no ViewModel e captura pendente; não há rascunho permanente nem recuperação após encerramento do processo.

A gravação conjunta usa o mesmo ID nas repetições. Durante confirmação incerta, o snapshot permanece bloqueado para edição e pode ser reenviado; somente uma consulta ao servidor após o fim da operação libera alterações ao confirmar ausência. Fotos completas são carregadas nos detalhes com nova tentativa, rolagem horizontal e ampliação. Somente autenticados leem fotos de anúncios disponíveis; apenas o autor comum cria fotos junto de um anúncio novo. Atualização, exclusão e fotos órfãs são bloqueadas. Fotos de entrega não fazem parte desta alteração.

Critérios adicionais: publicar zero/uma/cinco fotos, rejeitar sexta, respeitar limites de imagem/capa, cancelar captura sem anexo, preservar na rotação, repetir falha sem duplicação, carregar capa/detalhes de anúncios antigos e novos, bloquear leitura anônima e gravações parciais/avulsas. Publicação real e avaliação visual completa continuam dependentes da verificação manual.

| 1.9 | 06/11/2026 | Fotos opcionais de anúncios em Base64 no Firestore Spark, câmera/galeria, capa, galeria e gravação atômica; comprovantes preservados fora desta etapa. |

Verificação v1.9: 34 testes Java, nove Android no moto g15/Android 15, nove JavaScript e 31 cenários Firebase locais passaram; build e lint sem erros. Após autorização explícita em 06/11/2026, regras de fotos e isenções de índices de `objetos.miniaturaBase64` e `fotos.base64` foram publicadas pelo Console do Firebase, no Safari com a conta D2G. Plano Spark preservado, sem alteração de faturamento. Fluxo manual completo de publicação com fotos no projeto real ainda não validado.

### Ajuste do teclado — versão 1.10 (06/11/2026)

Na área autenticada, o menu inferior fica oculto enquanto o teclado estiver aberto e reaparece ao fechá-lo, mantendo a aba selecionada. O contêiner principal trata uma única vez os espaços das barras do sistema e do teclado; componentes filhos não repetem o preenchimento. Formulários permanecem roláveis, com os campos acessíveis durante a digitação, sem apagar dados ou anexos. Cenário de aceite: abrir um campo do cadastro de objeto ou a busca, digitar, fechar e reabrir o teclado; não há área vazia do menu nem sobreposição e a aba permanece selecionada.

| 1.10 | 06/11/2026 | Corrigir duplicação do espaço do teclado e ocultar o menu inferior durante a digitação. |

Verificação v1.10: build, 34 testes Java e lint passaram. No moto g15/Android 15, o cadastro ficou com 1.372 pixels úteis com o teclado aberto (antes, 247), sem a área vazia do menu. Na busca, o ciclo de abrir e fechar teclado confirmou menu oculto e retorno à altura normal de 200 pixels. Versão corrigida instalada no celular.
