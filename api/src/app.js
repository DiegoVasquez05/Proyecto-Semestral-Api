// Arma la aplicación Express con todas sus rutas.
// Recibe la base de datos como parámetro para poder usar una base en memoria en las pruebas.
const express = require('express');
const { manejadorDeErrores } = require('./errores');
const { crearRutasUsuarios } = require('./rutas/usuarios');
const { crearRutasLevantamientos, crearRutasComponentes } = require('./rutas/levantamientos');
const { crearRutasCatalogo } = require('./rutas/catalogo');

function crearApp(baseDeDatos) {
  const app = express();
  app.use(express.json());

  // GET /api/salud → permite comprobar que la API está en línea.
  app.get('/api/salud', (peticion, respuesta) => {
    respuesta.json({ estado: 'ok', servicio: 'Rack - Pro API' });
  });

  app.use('/api/usuarios', crearRutasUsuarios(baseDeDatos));
  app.use('/api/levantamientos', crearRutasLevantamientos(baseDeDatos));
  app.use('/api/componentes', crearRutasComponentes(baseDeDatos));
  app.use('/api/catalogo', crearRutasCatalogo(baseDeDatos));

  app.use((peticion, respuesta) => {
    respuesta.status(404).json({ mensaje: 'La ruta solicitada no existe' });
  });
  app.use(manejadorDeErrores);

  return app;
}

module.exports = { crearApp };
