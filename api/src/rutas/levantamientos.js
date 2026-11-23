// Endpoints de levantamientos de racks y de sus componentes.
const express = require('express');
const { ESTADOS, ESTADOS_COMPONENTE } = require('../constantes');
const { ErrorApi, manejarAsync, leerId } = require('../errores');
const { levantamientoAJson, componenteAJson } = require('../mapeadores');
const {
  textoObligatorio, validarCodigoRack, validarFecha, primerError,
} = require('../validaciones');

// Busca un levantamiento con sus componentes. Lanza 404 si no existe.
async function buscarLevantamientoCompleto(baseDeDatos, levantamientoId) {
  const { rows } = await baseDeDatos.query('SELECT * FROM levantamientos WHERE id = $1', [levantamientoId]);
  if (rows.length === 0) throw new ErrorApi(404, 'El levantamiento no existe');

  const componentes = await baseDeDatos.query(
    'SELECT * FROM componentes WHERE levantamiento_id = $1 ORDER BY id',
    [levantamientoId],
  );
  return levantamientoAJson(rows[0], componentes.rows);
}

// Un levantamiento aprobado queda bloqueado: no se puede editar ni eliminar.
function exigirQueEstePendiente(levantamiento) {
  if (levantamiento.estado === ESTADOS.APROBADO) {
    throw new ErrorApi(409, 'El levantamiento ya fue aprobado y no se puede modificar');
  }
}

function crearRutasLevantamientos(baseDeDatos) {
  const rutas = express.Router();

  // GET /api/levantamientos?estado=Pendiente → lista con sus componentes (filtro opcional).
  rutas.get('/', manejarAsync(async (peticion, respuesta) => {
    const { estado } = peticion.query;
    const levantamientos = estado
      ? await baseDeDatos.query('SELECT * FROM levantamientos WHERE estado = $1 ORDER BY id DESC', [estado])
      : await baseDeDatos.query('SELECT * FROM levantamientos ORDER BY id DESC');
    const componentes = await baseDeDatos.query('SELECT * FROM componentes ORDER BY id');

    const componentesPorLevantamiento = new Map();
    for (const componente of componentes.rows) {
      const lista = componentesPorLevantamiento.get(componente.levantamiento_id) ?? [];
      lista.push(componente);
      componentesPorLevantamiento.set(componente.levantamiento_id, lista);
    }

    respuesta.json(levantamientos.rows.map(
      (fila) => levantamientoAJson(fila, componentesPorLevantamiento.get(fila.id)),
    ));
  }));

  // GET /api/levantamientos/:id → un levantamiento con sus componentes.
  rutas.get('/:id', manejarAsync(async (peticion, respuesta) => {
    respuesta.json(await buscarLevantamientoCompleto(baseDeDatos, leerId(peticion)));
  }));

  // POST /api/levantamientos → crea una inspección nueva en estado Pendiente.
  rutas.post('/', manejarAsync(async (peticion, respuesta) => {
    const {
      codigoRack, dataCenter, sala, fecha, inspectorId, inspectorNombre,
    } = peticion.body;
    const error = primerError(
      validarCodigoRack(codigoRack),
      textoObligatorio(dataCenter, 'dataCenter'),
      textoObligatorio(sala, 'sala'),
      validarFecha(fecha),
      Number.isInteger(inspectorId) ? null : 'El campo inspectorId es obligatorio',
      textoObligatorio(inspectorNombre, 'inspectorNombre'),
    );
    if (error) throw new ErrorApi(400, error);

    const { rows } = await baseDeDatos.query(
      `INSERT INTO levantamientos (codigo_rack, data_center, sala, fecha, inspector_id, inspector_nombre, creado_en)
       VALUES ($1, $2, $3, $4, $5, $6, $7) RETURNING *`,
      [
        codigoRack.trim().toUpperCase(), dataCenter.trim(), sala.trim(), fecha.trim(),
        inspectorId, inspectorNombre.trim(), Date.now(),
      ],
    );
    respuesta.status(201).json(levantamientoAJson(rows[0]));
  }));

  // PUT /api/levantamientos/:id → edita el ID del rack y la fecha.
  rutas.put('/:id', manejarAsync(async (peticion, respuesta) => {
    const levantamientoId = leerId(peticion);
    const { codigoRack, fecha } = peticion.body;
    const error = primerError(validarCodigoRack(codigoRack), validarFecha(fecha));
    if (error) throw new ErrorApi(400, error);

    exigirQueEstePendiente(await buscarLevantamientoCompleto(baseDeDatos, levantamientoId));
    await baseDeDatos.query(
      'UPDATE levantamientos SET codigo_rack = $1, fecha = $2, actualizado_en = $3 WHERE id = $4',
      [codigoRack.trim().toUpperCase(), fecha.trim(), Date.now(), levantamientoId],
    );
    respuesta.json(await buscarLevantamientoCompleto(baseDeDatos, levantamientoId));
  }));

  // PATCH /api/levantamientos/:id/aprobacion → el supervisor aprueba el levantamiento.
  rutas.patch('/:id/aprobacion', manejarAsync(async (peticion, respuesta) => {
    const levantamientoId = leerId(peticion);
    const { supervisorNombre } = peticion.body;
    const error = textoObligatorio(supervisorNombre, 'supervisorNombre');
    if (error) throw new ErrorApi(400, error);

    exigirQueEstePendiente(await buscarLevantamientoCompleto(baseDeDatos, levantamientoId));
    await baseDeDatos.query(
      'UPDATE levantamientos SET estado = $1, aprobado_por = $2, aprobado_en = $3 WHERE id = $4',
      [ESTADOS.APROBADO, supervisorNombre.trim(), Date.now(), levantamientoId],
    );
    respuesta.json(await buscarLevantamientoCompleto(baseDeDatos, levantamientoId));
  }));

  // DELETE /api/levantamientos/:id → elimina un levantamiento pendiente y sus componentes.
  rutas.delete('/:id', manejarAsync(async (peticion, respuesta) => {
    const levantamientoId = leerId(peticion);
    exigirQueEstePendiente(await buscarLevantamientoCompleto(baseDeDatos, levantamientoId));
    await baseDeDatos.query('DELETE FROM componentes WHERE levantamiento_id = $1', [levantamientoId]);
    await baseDeDatos.query('DELETE FROM levantamientos WHERE id = $1', [levantamientoId]);
    respuesta.status(204).end();
  }));

  // POST /api/levantamientos/:id/componentes → agrega un componente del catálogo.
  rutas.post('/:id/componentes', manejarAsync(async (peticion, respuesta) => {
    const levantamientoId = leerId(peticion);
    const { nombre } = peticion.body;
    const error = textoObligatorio(nombre, 'nombre');
    if (error) throw new ErrorApi(400, error);

    exigirQueEstePendiente(await buscarLevantamientoCompleto(baseDeDatos, levantamientoId));
    const { rows } = await baseDeDatos.query(
      'INSERT INTO componentes (levantamiento_id, nombre, estado) VALUES ($1, $2, $3) RETURNING *',
      [levantamientoId, nombre.trim(), ESTADOS.PENDIENTE],
    );
    respuesta.status(201).json(componenteAJson(rows[0]));
  }));

  return rutas;
}

function crearRutasComponentes(baseDeDatos) {
  const rutas = express.Router();

  // PATCH /api/componentes/:id → cambia el estado (Pendiente, Validado u Observado).
  rutas.patch('/:id', manejarAsync(async (peticion, respuesta) => {
    const componenteId = leerId(peticion);
    const { estado } = peticion.body;
    if (!ESTADOS_COMPONENTE.includes(estado)) {
      throw new ErrorApi(400, 'El estado debe ser Pendiente, Validado u Observado');
    }

    const { rows } = await baseDeDatos.query('SELECT * FROM componentes WHERE id = $1', [componenteId]);
    if (rows.length === 0) throw new ErrorApi(404, 'El componente no existe');
    exigirQueEstePendiente(await buscarLevantamientoCompleto(baseDeDatos, rows[0].levantamiento_id));

    const actualizado = await baseDeDatos.query(
      'UPDATE componentes SET estado = $1 WHERE id = $2 RETURNING *',
      [estado, componenteId],
    );
    respuesta.json(componenteAJson(actualizado.rows[0]));
  }));

  return rutas;
}

module.exports = { crearRutasLevantamientos, crearRutasComponentes };
