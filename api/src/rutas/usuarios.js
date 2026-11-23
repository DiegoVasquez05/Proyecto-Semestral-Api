// Endpoints de usuarios: registro, inicio de sesión y edición de perfil.
const express = require('express');
const { ROLES } = require('../constantes');
const { ErrorApi, manejarAsync, leerId } = require('../errores');
const { usuarioAJson } = require('../mapeadores');
const { generarHashContrasena, contrasenaEsCorrecta } = require('../seguridad');
const { textoObligatorio, validarCorreo, primerError } = require('../validaciones');

const LARGO_MINIMO_CONTRASENA = 6;

function cargoPorDefecto(rol) {
  return rol === 'Supervisor' ? 'Supervisor de proyecto' : 'Técnico de terreno';
}

function crearRutasUsuarios(baseDeDatos) {
  const rutas = express.Router();

  // POST /api/usuarios/registro → crea una cuenta nueva.
  rutas.post('/registro', manejarAsync(async (peticion, respuesta) => {
    const { nombreCompleto, correo, contrasena, rol } = peticion.body;
    const error = primerError(
      textoObligatorio(nombreCompleto, 'nombreCompleto'),
      validarCorreo(correo),
      typeof contrasena === 'string' && contrasena.length >= LARGO_MINIMO_CONTRASENA
        ? null : `La contraseña debe tener al menos ${LARGO_MINIMO_CONTRASENA} caracteres`,
      ROLES.includes(rol) ? null : 'El rol debe ser Inspector o Supervisor',
    );
    if (error) throw new ErrorApi(400, error);

    const correoLimpio = correo.trim().toLowerCase();
    const existente = await baseDeDatos.query('SELECT id FROM usuarios WHERE correo = $1', [correoLimpio]);
    if (existente.rows.length > 0) throw new ErrorApi(409, 'Ya existe una cuenta con este correo');

    const { rows } = await baseDeDatos.query(
      `INSERT INTO usuarios (nombre_completo, correo, contrasena_hash, rol, cargo)
       VALUES ($1, $2, $3, $4, $5) RETURNING *`,
      [nombreCompleto.trim(), correoLimpio, generarHashContrasena(contrasena), rol, cargoPorDefecto(rol)],
    );
    respuesta.status(201).json(usuarioAJson(rows[0]));
  }));

  // POST /api/usuarios/login → valida correo, contraseña y rol.
  rutas.post('/login', manejarAsync(async (peticion, respuesta) => {
    const { correo, contrasena, rol } = peticion.body;
    const error = primerError(validarCorreo(correo), textoObligatorio(contrasena, 'contrasena'));
    if (error) throw new ErrorApi(400, error);

    const { rows } = await baseDeDatos.query(
      'SELECT * FROM usuarios WHERE correo = $1',
      [correo.trim().toLowerCase()],
    );
    const usuario = rows[0];
    if (!usuario) throw new ErrorApi(404, 'No existe una cuenta con este correo');
    if (!contrasenaEsCorrecta(contrasena, usuario.contrasena_hash)) {
      throw new ErrorApi(401, 'La contraseña es incorrecta');
    }
    if (rol && usuario.rol !== rol) {
      throw new ErrorApi(403, `Esta cuenta está registrada como ${usuario.rol}`);
    }
    respuesta.json(usuarioAJson(usuario));
  }));

  // PUT /api/usuarios/:id → actualiza los datos del perfil.
  rutas.put('/:id', manejarAsync(async (peticion, respuesta) => {
    const usuarioId = leerId(peticion);
    const { nombreCompleto, correo, cargo = '', telefono = '' } = peticion.body;
    const error = primerError(textoObligatorio(nombreCompleto, 'nombreCompleto'), validarCorreo(correo));
    if (error) throw new ErrorApi(400, error);

    const correoLimpio = correo.trim().toLowerCase();
    const otraCuenta = await baseDeDatos.query(
      'SELECT id FROM usuarios WHERE correo = $1 AND id <> $2',
      [correoLimpio, usuarioId],
    );
    if (otraCuenta.rows.length > 0) throw new ErrorApi(409, 'Ese correo ya está en uso por otra cuenta');

    const { rows } = await baseDeDatos.query(
      `UPDATE usuarios SET nombre_completo = $1, correo = $2, cargo = $3, telefono = $4
       WHERE id = $5 RETURNING *`,
      [nombreCompleto.trim(), correoLimpio, String(cargo).trim(), String(telefono).trim(), usuarioId],
    );
    if (rows.length === 0) throw new ErrorApi(404, 'El usuario no existe');
    respuesta.json(usuarioAJson(rows[0]));
  }));

  return rutas;
}

module.exports = { crearRutasUsuarios };
