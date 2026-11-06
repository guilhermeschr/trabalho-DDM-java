import json, urllib.request, urllib.error
PROJECT='demo-ddm-catalog'
BASE=f'http://127.0.0.1:8088/v1/projects/{PROJECT}/databases/(default)/documents'

def request(url, data=None, token=None, method=None):
 headers={'Content-Type':'application/json'}
 if token: headers['Authorization']='Bearer '+token
 req=urllib.request.Request(url,data=None if data is None else json.dumps(data).encode(),headers=headers,method=method)
 try:
  with urllib.request.urlopen(req) as resp:return resp.status,json.load(resp)
 except urllib.error.HTTPError as error:
  return error.code,json.load(error)

def account(email):
 code,data=request('http://127.0.0.1:9098/identitytoolkit.googleapis.com/v1/accounts:signUp?key=fake-key',{'email':email,'password':'TesteEmulador2026!','returnSecureToken':True})
 assert code==200,data
 return data['localId'],data['idToken']

def fields(data):
 return {k:({'stringValue':v} if isinstance(v,str) else {'doubleValue':v} if isinstance(v,(int,float)) else {'arrayValue':{'values':[]}}) for k,v in data.items()}

def seed(path,data):
 code,result=request(BASE+'/'+path,{'fields':fields(data)},'owner','PATCH');assert code==200,result

def create(id,data,token):
 name=f'projects/{PROJECT}/databases/(default)/documents/objetos/{id}'
 return request(BASE+':commit',{'writes':[{'update':{'name':name,'fields':fields(data)},'currentDocument':{'exists':False},'updateTransforms':[{'fieldPath':'criadoEm','setToServerValue':'REQUEST_TIME'}]}]},token)

def expect(label, result, allowed):
 code,data=result
 assert (code==200)==allowed,(label,code,data)
 if not allowed: assert code in (401,403), (label,code,data)
 print('OK:',label)

u,ut=account('comum@example.com');p,pt=account('ponto@example.com');other,ot=account('outro@example.com')
seed('usuarios/'+u,{'nome':'Comum','perfil':'COMUM'})
seed('usuarios/'+p,{'nome':'Ponto','perfil':'PONTO_COLETA'})
seed('usuarios/'+other,{'nome':'Outro','perfil':'COMUM'})
seed('pontosColeta/'+p,{'responsavelUid':p,'nome':'Ponto','endereco':'Rua A','latitude':-23.0,'longitude':-46.0})
valid={'nome':'Mochila Azul','descricao':'Mochila com cadernos','localAproximado':'Praça','latitude':-23.1,'longitude':-46.1,'pontoId':p,'autorUid':u,'situacao':'DISPONIVEL','fotos':[]}
expect('usuário comum publica objeto válido',create('valido',valid,ut),True)
expect('ponto não pode publicar',create('ponto',dict(valid,autorUid=p),pt),False)
expect('sem autenticação não publica',create('anonimo',valid,None),False)
expect('autor falsificado é rejeitado',create('autor',valid,ot),False)
expect('ponto inexistente é rejeitado',create('sem-ponto',dict(valid,pontoId='inexistente'),ut),False)
expect('coordenada inválida é rejeitada',create('coordenada',dict(valid,latitude=91),ut),False)
expect('descrição com espaços é rejeitada',create('vazio',dict(valid,descricao=' \n\t'),ut),False)
expect('situação entregue na criação é rejeitada',create('entregue',dict(valid,situacao='ENTREGUE'),ut),False)
expect('campos extras são rejeitados',create('extra',dict(valid,segredo='campo'),ut),False)
expect('usuário autenticado lê disponível',request(BASE+'/objetos/valido',token=ot),True)
expect('ponto lê disponível',request(BASE+'/objetos/valido',token=pt),True)
expect('sem autenticação não lê objeto',request(BASE+'/objetos/valido'),False)
expect('consulta filtrada por disponível',request(BASE+':runQuery',{'structuredQuery':{'from':[{'collectionId':'objetos'}],'where':{'fieldFilter':{'field':{'fieldPath':'situacao'},'op':'EQUAL','value':{'stringValue':'DISPONIVEL'}}}}},ut),True)
expect('consulta sem filtro é bloqueada',request(BASE+':runQuery',{'structuredQuery':{'from':[{'collectionId':'objetos'}]}},ut),False)
code,data=request(BASE+'/objetos/novo',token=ut);assert code==404,(code,data);print('OK: leitura autorizada de ID ausente retorna 404 para criação transacional')
expect('edição de objeto é bloqueada',request(BASE+'/objetos/valido',{'fields':fields(dict(valid,nome='Alterado'))},ut,'PATCH'),False)
expect('exclusão de objeto é bloqueada',request(BASE+'/objetos/valido',token=ut,method='DELETE'),False)
expect('entregas continuam privadas e bloqueadas',request(BASE+'/entregas/valido',token=ut),False)
seed('objetos/historico',dict(valid,situacao='ENTREGUE'))
expect('objeto entregue não pode ser lido neste módulo',request(BASE+'/objetos/historico',token=ut),False)
print('Regras validadas no emulador; nenhum dado de produção foi alterado.')

# Fotos fazem parte da mesma publicação, nunca de um upload posterior.
def photo_commit(id, count, token, oversized=False, missing=False):
 data=dict(valid, fotos=[str(i) for i in range(count)], miniaturaBase64='YQ==')
 name=f'projects/{PROJECT}/databases/(default)/documents/objetos/{id}'
 objfields=fields(data);objfields['fotos']={'arrayValue':{'values':[{'stringValue':str(i)} for i in range(count)]}}
 writes=[{'update':{'name':name,'fields':objfields},'currentDocument':{'exists':False},'updateTransforms':[{'fieldPath':'criadoEm','setToServerValue':'REQUEST_TIME'}]}]
 for i in range(0 if missing else count):
  writes.append({'update':{'name':name+'/fotos/'+str(i),'fields':fields({'tipo':'image/jpeg','base64':'A'*136540 if oversized else 'YQ=='})},'currentDocument':{'exists':False}})
 return request(BASE+':commit',{'writes':writes},token)
expect('uma foto é publicada atomicamente',photo_commit('foto-uma',1,ut),True)
expect('cinco fotos cabem nos limites das regras',photo_commit('foto-cinco',5,ut),True)
expect('seis fotos são rejeitadas',photo_commit('foto-seis',6,ut),False)
expect('foto declarada ausente impede anúncio',photo_commit('foto-ausente',1,ut,missing=True),False)
expect('foto grande impede anúncio',photo_commit('foto-grande',1,ut,oversized=True),False)
expect('outro autor não publica fotos',photo_commit('foto-autor',1,ot),False)
expect('leitura autenticada da foto',request(BASE+'/objetos/foto-uma/fotos/0',token=ot),True)
expect('leitura anônima da foto bloqueada',request(BASE+'/objetos/foto-uma/fotos/0'),False)
expect('foto posterior bloqueada',request(BASE+'/objetos/valido/fotos/0',{'fields':fields({'tipo':'image/jpeg','base64':'YQ=='})},ut,'PATCH'),False)
expect('foto órfã bloqueada',request(BASE+'/objetos/orfao/fotos/0',{'fields':fields({'tipo':'image/jpeg','base64':'YQ=='})},ut,'PATCH'),False)
expect('editar foto bloqueado',request(BASE+'/objetos/foto-uma/fotos/0',{'fields':fields({'tipo':'image/jpeg','base64':'Yg=='})},ut,'PATCH'),False)
expect('excluir foto bloqueado',request(BASE+'/objetos/foto-uma/fotos/0',token=ut,method='DELETE'),False)
code,data=request(BASE+'/objetos/foto-grande',token=ut); assert code==404,(code,data)
print('Fotos e anúncio não deixam publicação parcial.')
