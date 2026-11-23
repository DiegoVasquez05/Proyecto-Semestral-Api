// Pruebas de los endpoints usando una base PostgreSQL en memoria (pg-mem).
const { test, beforeEach } = require('node:test');
const assert = require('node:assert/strict');
const request = require('supertest');
const { newDb } = require('pg-mem');
const { crearApp } = require('../src/app');
const { prepararBaseDeDatos, CONTRASENA_DEMO } = require('../src/baseDeDatos');

let app;

beforeEach(async () => {
  const { Pool } = newDb().adapters.createPg();
  const baseDeDatos = new Pool();
  await prepararBaseDeDatos(baseDeDatos);
  app = crearApp(baseDeDatos);
});

const nuevoLevantamiento = {
  codigoRack: 'd-07',
  dataCenter: 'DC Sur',
  sala: 'Sala 4',
  fecha: '20/11/2026',
  inspectorId: 1,
  inspectorNombre: 'Inspector Demo',
};

test('GET /api/salud responde ok', async () => {
  const respuesta = await request(app).get('/api/salud');
  assert.equal(respuesta.status, 200);
  assert.equal(respuesta.body.estado, 'ok');
});

test('GET /api/catalogo/componentes entrega el catálogo inicial', async () => {
  const respuesta = await request(app).get('/api/catalogo/componentes');
  assert.equal(respuesta.status, 200);
  assert.equal(respuesta.body.length, 8);
});

test('POST /api/usuarios/registro crea la cuenta sin devolver la contraseña', async () => {
  const respuesta = await request(app).post('/api/usuarios/registro').send({
    nombreCompleto: 'Ana Pérez', correo: 'Ana@CMC360.cl', contrasena: 'secreto1', rol: 'Inspector',
  });
  assert.equal(respuesta.status, 201);
  assert.equal(respuesta.body.correo, 'ana@cmc360.cl');
  assert.equal(respuesta.body.contrasena, undefined);
});

test('POST /api/usuarios/registro rechaza un correo repetido', async () => {
  const respuesta = await request(app).post('/api/usuarios/registro').send({
    nombreCompleto: 'Otro', correo: 'inspector@cmc360.cl', contrasena: 'secreto1', rol: 'Inspector',
  });
  assert.equal(respuesta.status, 409);
});

test('POST /api/usuarios/login valida contraseña y rol', async () => {
  const correcto = await request(app).post('/api/usuarios/login')
    .send({ correo: 'supervisor@cmc360.cl', contrasena: CONTRASENA_DEMO, rol: 'Supervisor' });
  assert.equal(correcto.status, 200);
  assert.equal(correcto.body.rol, 'Supervisor');

  const contrasenaMala = await request(app).post('/api/usuarios/login')
    .send({ correo: 'supervisor@cmc360.cl', contrasena: 'incorrecta', rol: 'Supervisor' });
  assert.equal(contrasenaMala.status, 401);

  const rolEquivocado = await request(app).post('/api/usuarios/login')
    .send({ correo: 'supervisor@cmc360.cl', contrasena: CONTRASENA_DEMO, rol: 'Inspector' });
  assert.equal(rolEquivocado.status, 403);
});

test('PUT /api/usuarios/:id actualiza el perfil', async () => {
  const respuesta = await request(app).put('/api/usuarios/1').send({
    nombreCompleto: 'Inspector Editado', correo: 'inspector@cmc360.cl', cargo: 'Jefe', telefono: '+56912345678',
  });
  assert.equal(respuesta.status, 200);
  assert.equal(respuesta.body.nombreCompleto, 'Inspector Editado');
  assert.equal(respuesta.body.telefono, '+56912345678');
});

test('GET /api/levantamientos lista los ejemplos con sus componentes', async () => {
  const respuesta = await request(app).get('/api/levantamientos');
  assert.equal(respuesta.status, 200);
  assert.equal(respuesta.body.length, 4);
  const rackA01 = respuesta.body.find((levantamiento) => levantamiento.codigoRack === 'A-01');
  assert.equal(rackA01.componentes.length, 3);
  assert.equal(typeof rackA01.creadoEn, 'number');

  const pendientes = await request(app).get('/api/levantamientos?estado=Pendiente');
  assert.equal(pendientes.body.length, 2);
});

test('POST /api/levantamientos crea un levantamiento pendiente', async () => {
  const respuesta = await request(app).post('/api/levantamientos').send(nuevoLevantamiento);
  assert.equal(respuesta.status, 201);
  assert.equal(respuesta.body.codigoRack, 'D-07');
  assert.equal(respuesta.body.estado, 'Pendiente');

  const detalle = await request(app).get(`/api/levantamientos/${respuesta.body.id}`);
  assert.equal(detalle.status, 200);
  assert.equal(detalle.body.dataCenter, 'DC Sur');
});

test('POST /api/levantamientos valida el formato del ID y la fecha', async () => {
  const respuesta = await request(app).post('/api/levantamientos')
    .send({ ...nuevoLevantamiento, codigoRack: 'D07' });
  assert.equal(respuesta.status, 400);

  const fechaInexistente = await request(app).post('/api/levantamientos')
    .send({ ...nuevoLevantamiento, fecha: '31/02/2026' });
  assert.equal(fechaInexistente.status, 400);
  assert.equal(fechaInexistente.body.mensaje, 'La fecha no existe');
});

test('flujo completo: editar, agregar componente, validarlo y aprobar', async () => {
  const creado = await request(app).post('/api/levantamientos').send(nuevoLevantamiento);
  const levantamientoId = creado.body.id;

  const editado = await request(app).put(`/api/levantamientos/${levantamientoId}`)
    .send({ codigoRack: 'D-08', fecha: '21/11/2026' });
  assert.equal(editado.status, 200);
  assert.equal(editado.body.codigoRack, 'D-08');
  assert.equal(typeof editado.body.actualizadoEn, 'number');

  const componente = await request(app).post(`/api/levantamientos/${levantamientoId}/componentes`)
    .send({ nombre: 'UPS 20 kVA' });
  assert.equal(componente.status, 201);

  const validado = await request(app).patch(`/api/componentes/${componente.body.id}`)
    .send({ estado: 'Validado' });
  assert.equal(validado.status, 200);
  assert.equal(validado.body.estado, 'Validado');

  const aprobado = await request(app).patch(`/api/levantamientos/${levantamientoId}/aprobacion`)
    .send({ supervisorNombre: 'Supervisor Demo' });
  assert.equal(aprobado.status, 200);
  assert.equal(aprobado.body.estado, 'Aprobado');
  assert.equal(aprobado.body.aprobadoPor, 'Supervisor Demo');

  const edicionBloqueada = await request(app).put(`/api/levantamientos/${levantamientoId}`)
    .send({ codigoRack: 'D-09', fecha: '21/11/2026' });
  assert.equal(edicionBloqueada.status, 409);
});

test('DELETE /api/levantamientos/:id elimina solo levantamientos pendientes', async () => {
  const creado = await request(app).post('/api/levantamientos').send(nuevoLevantamiento);
  const eliminado = await request(app).delete(`/api/levantamientos/${creado.body.id}`);
  assert.equal(eliminado.status, 204);

  const buscado = await request(app).get(`/api/levantamientos/${creado.body.id}`);
  assert.equal(buscado.status, 404);

  const aprobados = await request(app).get('/api/levantamientos?estado=Aprobado');
  const eliminarAprobado = await request(app).delete(`/api/levantamientos/${aprobados.body[0].id}`);
  assert.equal(eliminarAprobado.status, 409);
});

test('PATCH /api/componentes/:id rechaza estados inválidos', async () => {
  const respuesta = await request(app).patch('/api/componentes/1').send({ estado: 'Roto' });
  assert.equal(respuesta.status, 400);
});
