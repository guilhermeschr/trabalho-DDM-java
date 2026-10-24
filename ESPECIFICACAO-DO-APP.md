# Especificação do aplicativo — Achados e Perdidos

Documento principal de requisitos do repositório. Consulte este arquivo para definir, desenvolver e validar as funcionalidades do app.

**Disciplina:** Desenvolvimento para Dispositivos Móveis (DDM)

**Data:** 23/10/2026

**Versão:** 1.1 — MVVM, cache Room e câmera

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

A seleção de localização pode ser uma tela reutilizada ou um componente dos formulários. Cada mapa de detalhes deve permitir visualizar e explorar a posição marcada. A primeira versão não exige mapa geral com todos os anúncios, cálculo de distância ou rotas.

A navegação oferece acesso a objetos disponíveis, pontos de coleta e saída da conta. O perfil de ponto também possui acesso a “Meus objetos”.

## 6. Fluxos principais

### 6.1. Publicar e localizar um objeto

1. O usuário comum cria sua conta ou faz login.
2. Depois de deixar o item em um ponto cadastrado, abre o cadastro de objeto.
3. Preenche os dados, marca onde encontrou o objeto e escolhe o ponto de retirada.
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

As fotos ficam no Cloud Storage, separando fotos de anúncios e comprovantes de entrega. O Firestore guarda caminhos dos arquivos, e não imagens. Comprovantes são carregados com acesso autenticado e restrito ao ponto, sem URLs públicas ou links compartilháveis de download gravados nos documentos do anúncio.

## 8. Organização técnica

| Tecnologia | Uso no projeto | Referência oficial |
| --- | --- | --- |
| Android com Java, layouts XML e MVVM | Uma Activity hospeda Fragments. ViewModels expõem estado via LiveData e delegam operações aos repositórios. Componentes Material padrão compõem a interface. | [Arquitetura Android](https://developer.android.com/topic/architecture) |
| Room | Cache local de perfis confirmados e, nas próximas etapas, objetos e pontos consultados. Firebase continua sendo a fonte de verdade; sem fila de alterações offline. | [Room](https://developer.android.com/training/data-storage/room) |
| Câmera | Captura de foto do objeto encontrado, com arquivo privado e compartilhamento por FileProvider quando necessário. | [Captura de fotos](https://developer.android.com/media/camera/camera-deprecated/photobasics) |
| Firebase Authentication | Cadastro e login com e-mail e senha. | [Autenticação por senha no Android](https://firebase.google.com/docs/auth/android/password-auth) |
| Cloud Firestore | Usuários, pontos, objetos e entregas; transação para registrar a entrega e alterar a situação juntas. | [Transações e gravações em lote](https://firebase.google.com/docs/firestore/manage-data/transactions) |
| Cloud Storage for Firebase | Fotos opcionais dos anúncios e fotos obrigatórias das entregas. | [Upload de arquivos no Android](https://firebase.google.com/docs/storage/android/upload-files) |
| Google Maps SDK for Android | Seleção e visualização das posições com marcadores. | [Visão geral do Maps SDK](https://developers.google.com/maps/documentation/android-sdk/overview) |

O aplicativo acessa os serviços pelos SDKs, sem servidor próprio. O padrão MVVM é obrigatório: Fragments exibem os formulários e observam LiveData; AuthViewModel valida entradas e coordena estados; AuthRepository encapsula Firebase Authentication, Firestore e cache Room. As telas não acessam Firebase diretamente. Uma única Activity hospeda os Fragments, e senhas não são armazenadas em Room, arquivos, logs ou estado persistido de telas.

O login somente libera a área autenticada após confirmar o perfil no servidor. Se a conta existir no Authentication e o documento de perfil ainda não existir no Firestore, o usuário é direcionado para concluir o cadastro sem criar outra conta. Usuário e ponto são gravados juntos por lote no Firestore. O cadastro valida confirmação de senha e um mínimo inicial de seis caracteres; a política do Firebase pode exigir mais. Ao sair, a sessão é encerrada e o cache do perfil é apagado.

Na primeira etapa, Room mantém o perfil confirmado, com acesso ao banco fora da thread principal. O cache de objetos e pontos será adicionado com as respectivas telas; conteúdo local deverá ser identificado como potencialmente desatualizado e nunca permitirá entregas offline. Os dados de autorização continuam dependendo de confirmação no servidor.

O pacote Android é `br.edu.ddm.achadoseperdidos`, com Android mínimo 7.0 (API 24), compilação/alvo API 35 e Java 17. As telas usam o tema padrão Material 3 e Fragments, sem personalização visual complexa.

Para o volume pequeno da demonstração, a busca por nome pode ser feita no app sobre os objetos disponíveis carregados. O filtro por ponto deve produzir o mesmo resultado quando combinado com essa busca. Não é necessário contratar um serviço de busca.

No registro da entrega, primeiro são enviados os comprovantes ao Storage. Depois, uma transação no Firestore verifica se o objeto ainda está disponível e pertence ao ponto autenticado, cria a entrega e atualiza sua situação. Essa transação cobre os registros do Firestore; não inclui o upload dos arquivos. Arquivos enviados sem uma entrega concluída não constituem um comprovante de retirada salva.

As permissões devem ser aplicadas nas regras do Firestore e do Storage, além da interface: restringir comprovantes ao ponto responsável, impedir troca de perfil e de ponto por alteração direta dos dados e permitir a mudança de situação apenas no fluxo autorizado de entrega. Esconder botões não substitui essas regras.

Antes da demonstração, o projeto precisa ter Firebase configurado, login por e-mail/senha habilitado, Firestore e Storage com regras de acesso aplicadas e Google Maps habilitado com chave configurada para o aplicativo Android.

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
| Falha no mapa | Manter os textos de localização e endereço visíveis nos detalhes. No cadastro, preservar o formulário e exigir coordenadas válidas antes de concluir. |
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
| RNF06 | Funcionamento sem localização atual do aparelho. | Negar a permissão de localização não impede a marcação manual nem a consulta dos mapas. |

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

## 12. Lista de verificação para o desenvolvimento

- [ ] RF01–RF03: cadastro dos dois perfis, login e saída.
- [ ] RF02 e RF09: cadastro, listagem e detalhes dos pontos.
- [ ] RF04: cadastro de objeto com ponto obrigatório e fotos opcionais.
- [ ] RF05–RF07: lista de disponíveis, busca e filtro combinados.
- [ ] RF08 e RF14: detalhes, seleção de posições e mapas.
- [ ] RF10: inventário do próprio ponto separado por situação.
- [ ] RF11–RF12: entrega com descrição, foto obrigatória e atualização consistente.
- [ ] RF13: histórico com comprovantes privados.
- [ ] RNF01–RNF06: interface, persistência, permissões e feedback.
- [ ] Seção 9: validações, estados vazios e tratamento de falhas.
- [ ] RF15: captura pela câmera no cadastro do objeto.
- [ ] RF16: cache Room de objetos e pontos, além do perfil.
- [ ] CA01–CA12: execução e registro dos resultados dos cenários de aceite.

## 13. Limites da primeira versão

Não fazem parte deste trabalho: anúncios de objetos perdidos, chat, notificações, reservas, aprovação administrativa de pontos, confirmação de recebimento pelo ponto, transferência de objetos, edição ou exclusão de anúncios, troca de perfil, cadastro de múltiplos estabelecimentos na mesma conta, login social, recuperação de senha pelo app, mapa geral de anúncios, rotas, busca por distância ou modo offline completo com sincronização de escritas.

O cadastro de objeto oferecerá captura pela câmera e seleção pela galeria. Os comprovantes de entrega continuam sendo selecionados pela galeria; a câmera exigida pelo trabalho será usada para fotografar o objeto encontrado. A conferência de que a pessoa pode retirar o item acontece presencialmente pelo ponto; o app registra a entrega, sem reconhecimento automático de identidade.

O nome definitivo do aplicativo e seu estilo visual podem ser definidos durante o desenvolvimento, sem alterar estes requisitos. O desenvolvimento foi iniciado com o projeto Android, login, cadastro e estrutura de cache de perfil. Recursos ainda não concluídos permanecem no checklist. A configuração e publicação dos serviços externos dependem do projeto Firebase e das chaves do responsável.


## 14. Atendimento ao enunciado T1 e andamento

O enunciado exige seis telas, navegação por Fragments com Bottom Navigation ou Drawer, duas listas de contextos distintos, MVVM, duas opções de persistência/integração e dois recursos da lista do trabalho. O app atenderá com listas de objetos e pontos (RecyclerView e Adapter), Firebase + Room e Google Maps + câmera. A navegação principal com Bottom Navigation será adicionada junto dessas listas; esta etapa entrega os Fragments de autenticação e início.

Implementado nesta etapa: estrutura Gradle importável no Android Studio, login e cadastro dos dois perfis, validações, estados de carregamento/erro, encerramento de sessão, retomada de perfil incompleto, seleção do ponto no mapa e armazenamento local do perfil. A área inicial autenticada é provisória; não representa as listas prontas. Testes com Firebase real e Maps dependem das configurações descritas no README. Câmera e cache de objetos/pontos são requisitos definidos, ainda pendentes de implementação.

## 15. Histórico de alterações

| Versão | Data | Alteração |
| --- | --- | --- |
| 1.0 | 19/10/2026 | Definição inicial dos requisitos de Achados e Perdidos. |
| 1.1 | 23/10/2026 | Adoção obrigatória de MVVM e Material padrão; inclusão de câmera para objetos e Room para cache; início de login e cadastro; mapeamento dos critérios do T1. |
