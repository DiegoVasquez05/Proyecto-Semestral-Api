// Punto de entrada: conecta con PostgreSQL, prepara las tablas y levanta el servidor.
const { Pool } = require('pg');
const { crearApp } = require('./app');
const { prepararBaseDeDatos } = require('./baseDeDatos');

const PUERTO = process.env.PORT || 3000;
const URL_BASE_DE_DATOS = process.env.DATABASE_URL;

async function iniciar() {
  if (!URL_BASE_DE_DATOS) {
    throw new Error('Falta la variable de entorno DATABASE_URL');
  }

  // DATABASE_SSL=true activa la conexión cifrada (necesaria al conectarse desde fuera de Render).
  const usarSsl = process.env.DATABASE_SSL === 'true';
  const baseDeDatos = new Pool({
    connectionString: URL_BASE_DE_DATOS,
    ssl: usarSsl ? { rejectUnauthorized: false } : false,
  });

  await prepararBaseDeDatos(baseDeDatos);
  crearApp(baseDeDatos).listen(PUERTO, () => {
    console.log(`Rack - Pro API escuchando en el puerto ${PUERTO}`);
  });
}

iniciar().catch((error) => {
  console.error('No se pudo iniciar la API:', error.message);
  process.exit(1);
});
