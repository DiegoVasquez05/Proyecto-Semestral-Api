// Validaciones de los datos que llegan en las peticiones.
// Cada función devuelve un mensaje de error o null si el dato es válido.
const FORMATO_CORREO = /^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}$/;
const FORMATO_CODIGO_RACK = /^[A-Za-z]-[0-9]{2}$/;
const FORMATO_FECHA = /^(\d{2})\/(\d{2})\/(\d{4})$/;

function textoObligatorio(valor, nombreCampo) {
  if (typeof valor !== 'string' || valor.trim() === '') {
    return `El campo ${nombreCampo} es obligatorio`;
  }
  return null;
}

function validarCorreo(correo) {
  return textoObligatorio(correo, 'correo')
    ?? (FORMATO_CORREO.test(correo.trim()) ? null : 'El correo no tiene un formato válido');
}

function validarCodigoRack(codigoRack) {
  return textoObligatorio(codigoRack, 'codigoRack')
    ?? (FORMATO_CODIGO_RACK.test(codigoRack.trim()) ? null : 'El ID de rack debe tener el formato A-01');
}

function validarFecha(fecha) {
  const errorObligatorio = textoObligatorio(fecha, 'fecha');
  if (errorObligatorio) return errorObligatorio;

  const partes = FORMATO_FECHA.exec(fecha.trim());
  if (!partes) return 'La fecha debe tener el formato dd/mm/aaaa';

  const [, dia, mes, anio] = partes.map(Number);
  const fechaConstruida = new Date(anio, mes - 1, dia);
  const fechaExiste = fechaConstruida.getFullYear() === anio
    && fechaConstruida.getMonth() === mes - 1
    && fechaConstruida.getDate() === dia;
  return fechaExiste ? null : 'La fecha no existe';
}

// Devuelve el primer error encontrado en la lista, o null si no hay ninguno.
function primerError(...errores) {
  return errores.find((error) => error !== null) ?? null;
}

module.exports = { textoObligatorio, validarCorreo, validarCodigoRack, validarFecha, primerError };
