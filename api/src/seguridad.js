// Funciones para guardar contraseñas de forma segura.
// Nunca se guarda la contraseña en texto plano: se guarda "sal:hash" generado con scrypt.
const crypto = require('crypto');

const LARGO_HASH = 64;

function generarHashContrasena(contrasena) {
  const sal = crypto.randomBytes(16).toString('hex');
  const hash = crypto.scryptSync(contrasena, sal, LARGO_HASH).toString('hex');
  return `${sal}:${hash}`;
}

function contrasenaEsCorrecta(contrasena, hashGuardado) {
  const [sal, hashEsperado] = hashGuardado.split(':');
  const hashCalculado = crypto.scryptSync(contrasena, sal, LARGO_HASH);
  return crypto.timingSafeEqual(hashCalculado, Buffer.from(hashEsperado, 'hex'));
}

module.exports = { generarHashContrasena, contrasenaEsCorrecta };
