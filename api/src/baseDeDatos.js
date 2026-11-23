// Creación de las tablas y carga de los datos iniciales en PostgreSQL.
const { generarHashContrasena } = require('./seguridad');
const { ESTADOS } = require('./constantes');

const ESQUEMA = `
  CREATE TABLE IF NOT EXISTS usuarios (
    id SERIAL PRIMARY KEY,
    nombre_completo TEXT NOT NULL,
    correo TEXT NOT NULL UNIQUE,
    contrasena_hash TEXT NOT NULL,
    rol TEXT NOT NULL,
    cargo TEXT NOT NULL DEFAULT '',
    telefono TEXT NOT NULL DEFAULT ''
  );

  CREATE TABLE IF NOT EXISTS levantamientos (
    id SERIAL PRIMARY KEY,
    codigo_rack TEXT NOT NULL,
    data_center TEXT NOT NULL,
    sala TEXT NOT NULL,
    fecha TEXT NOT NULL,
    estado TEXT NOT NULL DEFAULT 'Pendiente',
    inspector_id INTEGER NOT NULL,
    inspector_nombre TEXT NOT NULL,
    creado_en BIGINT NOT NULL,
    actualizado_en BIGINT,
    aprobado_por TEXT,
    aprobado_en BIGINT
  );

  CREATE TABLE IF NOT EXISTS componentes (
    id SERIAL PRIMARY KEY,
    levantamiento_id INTEGER NOT NULL REFERENCES levantamientos(id) ON DELETE CASCADE,
    nombre TEXT NOT NULL,
    estado TEXT NOT NULL DEFAULT 'Pendiente'
  );

  CREATE TABLE IF NOT EXISTS catalogo_componentes (
    id SERIAL PRIMARY KEY,
    nombre TEXT NOT NULL UNIQUE,
    categoria TEXT NOT NULL
  );
`;

const CATALOGO_INICIAL = [
  ['UPS 20 kVA', 'Energía'],
  ['PDU 32A', 'Energía'],
  ['Switch core 48p', 'Red'],
  ['Patch panel 24p', 'Red'],
  ['Bandeja de fibra', 'Red'],
  ['Firewall 1U', 'Seguridad'],
  ['Servidor 2U', 'Cómputo'],
  ['KVM 8 puertos', 'Cómputo'],
];

// Cuentas de demostración para probar la app sin registrarse.
const CONTRASENA_DEMO = 'rackpro123';
const USUARIOS_DEMO = [
  ['Inspector Demo', 'inspector@cmc360.cl', 'Inspector', 'Técnico de terreno'],
  ['Supervisor Demo', 'supervisor@cmc360.cl', 'Supervisor', 'Supervisor de proyecto'],
];

// Convierte "dd/mm/aaaa hh:mm" a milisegundos (hora de Chile aproximada, UTC-3).
function aMilisegundos(fechaConHora) {
  const [fecha, hora] = fechaConHora.split(' ');
  const [dia, mes, anio] = fecha.split('/');
  return Date.parse(`${anio}-${mes}-${dia}T${hora}:00-03:00`);
}

const LEVANTAMIENTOS_DE_EJEMPLO = [
  {
    codigoRack: 'C-03', dataCenter: 'DC Norte', sala: 'Sala 3', fecha: '10/10/2026',
    estado: ESTADOS.APROBADO, creadoEn: '10/10/2026 08:40', actualizadoEn: '10/10/2026 10:15',
    aprobadoEn: '10/10/2026 16:20',
    componentes: [['Servidor 2U', ESTADOS.VALIDADO]],
  },
  {
    codigoRack: 'B-05', dataCenter: 'DC Respaldo', sala: 'Sala 1', fecha: '12/10/2026',
    estado: ESTADOS.APROBADO, creadoEn: '12/10/2026 09:05', actualizadoEn: '12/10/2026 12:30',
    aprobadoEn: '12/10/2026 17:45',
    componentes: [['UPS 20 kVA', ESTADOS.VALIDADO], ['Firewall 1U', ESTADOS.VALIDADO]],
  },
  {
    codigoRack: 'A-02', dataCenter: 'DC Principal', sala: 'Sala 2', fecha: '14/10/2026',
    estado: ESTADOS.PENDIENTE, creadoEn: '14/10/2026 09:40',
    componentes: [['PDU 32A', ESTADOS.PENDIENTE], ['Patch panel 24p', ESTADOS.PENDIENTE]],
  },
  {
    codigoRack: 'A-01', dataCenter: 'DC Principal', sala: 'Sala 2', fecha: '14/10/2026',
    estado: ESTADOS.PENDIENTE, creadoEn: '14/10/2026 09:12', actualizadoEn: '14/10/2026 11:30',
    componentes: [
      ['UPS 20 kVA', ESTADOS.VALIDADO],
      ['Switch core 48p', ESTADOS.PENDIENTE],
      ['PDU 32A', ESTADOS.OBSERVADO],
    ],
  },
];

async function cargarCatalogo(baseDeDatos) {
  for (const [nombre, categoria] of CATALOGO_INICIAL) {
    await baseDeDatos.query(
      'INSERT INTO catalogo_componentes (nombre, categoria) VALUES ($1, $2) ON CONFLICT (nombre) DO NOTHING',
      [nombre, categoria],
    );
  }
}

async function cargarUsuariosDemo(baseDeDatos) {
  for (const [nombreCompleto, correo, rol, cargo] of USUARIOS_DEMO) {
    await baseDeDatos.query(
      `INSERT INTO usuarios (nombre_completo, correo, contrasena_hash, rol, cargo)
       VALUES ($1, $2, $3, $4, $5) ON CONFLICT (correo) DO NOTHING`,
      [nombreCompleto, correo, generarHashContrasena(CONTRASENA_DEMO), rol, cargo],
    );
  }
}

async function cargarLevantamientosDeEjemplo(baseDeDatos) {
  const { rows } = await baseDeDatos.query('SELECT COUNT(*) AS total FROM levantamientos');
  if (Number(rows[0].total) > 0) return;

  const inspector = await baseDeDatos.query(
    "SELECT id, nombre_completo FROM usuarios WHERE correo = 'inspector@cmc360.cl'",
  );
  const { id: inspectorId, nombre_completo: inspectorNombre } = inspector.rows[0];

  for (const ejemplo of LEVANTAMIENTOS_DE_EJEMPLO) {
    const insercion = await baseDeDatos.query(
      `INSERT INTO levantamientos (codigo_rack, data_center, sala, fecha, estado, inspector_id,
         inspector_nombre, creado_en, actualizado_en, aprobado_por, aprobado_en)
       VALUES ($1, $2, $3, $4, $5, $6, $7, $8, $9, $10, $11) RETURNING id`,
      [
        ejemplo.codigoRack, ejemplo.dataCenter, ejemplo.sala, ejemplo.fecha, ejemplo.estado,
        inspectorId, inspectorNombre,
        aMilisegundos(ejemplo.creadoEn),
        ejemplo.actualizadoEn ? aMilisegundos(ejemplo.actualizadoEn) : null,
        ejemplo.aprobadoEn ? 'Supervisor Demo' : null,
        ejemplo.aprobadoEn ? aMilisegundos(ejemplo.aprobadoEn) : null,
      ],
    );
    const levantamientoId = insercion.rows[0].id;
    for (const [nombre, estado] of ejemplo.componentes) {
      await baseDeDatos.query(
        'INSERT INTO componentes (levantamiento_id, nombre, estado) VALUES ($1, $2, $3)',
        [levantamientoId, nombre, estado],
      );
    }
  }
}

// Crea las tablas si no existen y carga los datos iniciales solo la primera vez.
async function prepararBaseDeDatos(baseDeDatos) {
  await baseDeDatos.query(ESQUEMA);
  await cargarCatalogo(baseDeDatos);
  await cargarUsuariosDemo(baseDeDatos);
  await cargarLevantamientosDeEjemplo(baseDeDatos);
}

module.exports = { prepararBaseDeDatos, CONTRASENA_DEMO };
