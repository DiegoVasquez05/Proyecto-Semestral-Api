// Endpoint del catálogo de componentes que se pueden agregar a un rack.
const express = require('express');
const { manejarAsync } = require('../errores');

function crearRutasCatalogo(baseDeDatos) {
  const rutas = express.Router();

  // GET /api/catalogo/componentes → lista ordenada por categoría y nombre.
  rutas.get('/componentes', manejarAsync(async (peticion, respuesta) => {
    const { rows } = await baseDeDatos.query(
      'SELECT id, nombre, categoria FROM catalogo_componentes ORDER BY categoria, nombre',
    );
    respuesta.json(rows);
  }));

  return rutas;
}

module.exports = { crearRutasCatalogo };
