// Manejo centralizado de errores de la API.
// Todas las respuestas de error tienen la forma { "mensaje": "..." }.

class ErrorApi extends Error {
  constructor(codigoHttp, mensaje) {
    super(mensaje);
    this.codigoHttp = codigoHttp;
  }
}

// Express 4 no captura errores de funciones async; este envoltorio los envía al manejador.
function manejarAsync(controlador) {
  return (peticion, respuesta, siguiente) => {
    controlador(peticion, respuesta, siguiente).catch(siguiente);
  };
}

// Convierte el parámetro :id de la URL a número o lanza un error 400.
function leerId(peticion) {
  const id = Number(peticion.params.id);
  if (!Number.isInteger(id) || id <= 0) {
    throw new ErrorApi(400, 'El id debe ser un número entero positivo');
  }
  return id;
}

// eslint-disable-next-line no-unused-vars
function manejadorDeErrores(error, peticion, respuesta, siguiente) {
  if (error instanceof ErrorApi) {
    respuesta.status(error.codigoHttp).json({ mensaje: error.message });
    return;
  }
  if (error.type === 'entity.parse.failed') {
    respuesta.status(400).json({ mensaje: 'El cuerpo de la petición no es un JSON válido' });
    return;
  }
  console.error(error);
  respuesta.status(500).json({ mensaje: 'Error interno del servidor' });
}

module.exports = { ErrorApi, manejarAsync, leerId, manejadorDeErrores };
