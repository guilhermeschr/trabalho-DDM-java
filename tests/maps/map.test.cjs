const {test} = require('node:test');
const assert = require('node:assert/strict');
const {readFileSync} = require('node:fs');
const {resolve} = require('node:path');
const vm = require('node:vm');

const html = readFileSync(resolve(__dirname, '../../app/src/main/assets/map.html'), 'utf8');
const script = html.match(/<script>([\s\S]*?)<\/script>/)[1];

function openMap(config = {}) {
  const messages = [], listeners = {}, markers = [], timers = new Set();
  let loader, options;
  const context = {
    MapHost: {postMessage: value => messages.push(JSON.parse(value))},
    setTimeout: callback => { timers.add(callback); return callback; },
    clearTimeout: callback => timers.delete(callback),
    document: {
      getElementById: () => ({}), createElement: () => ({}),
      head: {appendChild: element => { loader = element; }}
    },
    google: {maps: {
      Map: function (element, value) {
        options = value;
        this.setCenter = center => { options.center = center; };
        this.setZoom = zoom => { options.zoom = zoom; };
        this.addListener = (event, callback) => { listeners[event] = callback; };
      },
      Marker: function (value) { markers.push(value); this.setPosition = position => { value.position = position; }; },
      event: {addListenerOnce: (map, event, callback) => { listeners[event] = callback; }}
    }}
  };
  context.window = context;
  vm.createContext(context);
  vm.runInContext(script, context);
  context.initializeMap({key: 'chave-ficticia', lat: null, lng: null, readOnly: false, ...config});
  context.mapLoaded();
  return {context, messages, listeners, markers, timers, loader, options,
    click: (lat, lng) => listeners.click?.({latLng: {lat: () => lat, lng: () => lng}})};
}

test('não aceita cliques antes dos tiles e atualiza o mesmo marcador após carregar', () => {
  const map = openMap();
  map.click(-27, -49);
  assert.equal(map.messages.length, 0);
  assert.equal(map.markers.length, 0);
  map.listeners.tilesloaded();
  map.click(-27, -49);
  map.click(-26, -48);
  assert.deepEqual(map.messages, [
    {type: 'ready'}, {type: 'selected', lat: -27, lng: -49}, {type: 'selected', lat: -26, lng: -48}
  ]);
  assert.equal(map.markers.length, 1);
});

test('modo de consulta mantém o marcador salvo e não emite mudança de localização', () => {
  const map = openMap({lat: -27, lng: -49, readOnly: true});
  map.listeners.tilesloaded();
  map.click(0, 0);
  assert.equal(map.listeners.click, undefined);
  assert.equal(map.markers.length, 1);
  assert.equal(map.options.center.lat, -27);
  assert.equal(map.options.center.lng, -49);
  assert.deepEqual(map.messages, [{type: 'ready'}]);
});

test('erro de autorização impede seleção e sucesso tardio', () => {
  const map = openMap();
  map.context.gm_authFailure();
  map.listeners.tilesloaded();
  map.click(-27, -49);
  assert.deepEqual(map.messages, [{type: 'error'}]);
  assert.equal(map.timers.size, 0);
});

test('falha de rede e tempo limite informam erro', () => {
  const failedScript = openMap();
  failedScript.loader.onerror();
  assert.deepEqual(failedScript.messages, [{type: 'error'}]);
  const timeout = openMap();
  [...timeout.timers][0]();
  timeout.context.mapLoaded();
  assert.deepEqual(timeout.messages, [{type: 'error'}]);
});

test('coordenadas fora dos limites e valores não finitos não viram seleção', () => {
  const map = openMap();
  map.listeners.tilesloaded();
  for (const [lat, lng] of [[91, 0], [0, -181], [NaN, 0], [0, Infinity]]) map.click(lat, lng);
  assert.deepEqual(map.messages, [{type: 'ready'}]);
  map.click(0, 0);
  assert.deepEqual(map.messages[1], {type: 'selected', lat: 0, lng: 0});
});

test('carregamento preserva a seleção e não duplica scripts na mesma página', () => {
  const map = openMap({lat: 0, lng: 0});
  assert.equal(map.markers[0].position.lat, 0);
  assert.match(map.loader.src, /^https:\/\/maps\.googleapis\.com\/maps\/api\/js\?/);
  map.context.initializeMap({key: 'outra-chave'});
  map.listeners.tilesloaded();
  assert.equal(map.timers.size, 0);
});

test('posição atual pré-seleciona marcador, aproxima o mapa e permite ajuste manual', () => {
  const map = openMap();
  map.listeners.tilesloaded();
  assert.equal(map.context.setCurrentLocation(-27, -49), true);
  assert.equal(map.markers.length, 1);
  assert.equal(map.options.center.lat, -27);
  assert.equal(map.options.zoom, 15);
  assert.deepEqual(map.messages[1], {type: 'selected', source: 'current', lat: -27, lng: -49});
  map.click(-26, -48);
  assert.equal(map.markers.length, 1);
  assert.deepEqual(map.messages[2], {type: 'selected', lat: -26, lng: -48});
});

test('resposta atrasada da localização não substitui escolha manual', () => {
  const map = openMap();
  map.listeners.tilesloaded();
  map.click(-27, -49);
  assert.equal(map.context.setCurrentLocation(0, 0), false);
  assert.deepEqual(map.messages.at(-1), {type: 'selected', lat: -27, lng: -49});
  map.context.beginCurrentLocationRequest();
  assert.equal(map.context.setCurrentLocation(0, 0), true);
});

test('consulta de detalhes e coordenadas inválidas recusam localização do aparelho', () => {
  const details = openMap({lat: -27, lng: -49, readOnly: true});
  details.listeners.tilesloaded();
  assert.equal(details.context.setCurrentLocation(0, 0), false);
  assert.equal(details.options.center.lat, -27);
  const edit = openMap();
  edit.listeners.tilesloaded();
  assert.equal(edit.context.setCurrentLocation(NaN, 0), false);
  assert.equal(edit.context.setCurrentLocation(0, 181), false);
  assert.deepEqual(edit.messages, [{type: 'ready'}]);
});
