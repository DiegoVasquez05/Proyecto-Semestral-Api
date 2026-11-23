// Convierte las filas de PostgreSQL (snake_case) al JSON que consume la app (camelCase).

// PostgreSQL entrega BIGINT como texto; se convierte a número o se deja null.
function aNumeroOpcional(valor) {
  return valor === null || valor === undefined ? null : Number(valor);
}

function usuarioAJson(fila) {
  return {
    id: fila.id,
    nombreCompleto: fila.nombre_completo,
    correo: fila.correo,
    rol: fila.rol,
    cargo: fila.cargo,
    telefono: fila.telefono,
  };
}

function componenteAJson(fila) {
  return {
    id: fila.id,
    levantamientoId: fila.levantamiento_id,
    nombre: fila.nombre,
    estado: fila.estado,
  };
}

function levantamientoAJson(fila, componentes = []) {
  return {
    id: fila.id,
    codigoRack: fila.codigo_rack,
    dataCenter: fila.data_center,
    sala: fila.sala,
    fecha: fila.fecha,
    estado: fila.estado,
    inspectorId: fila.inspector_id,
    inspectorNombre: fila.inspector_nombre,
    creadoEn: aNumeroOpcional(fila.creado_en),
    actualizadoEn: aNumeroOpcional(fila.actualizado_en),
    aprobadoPor: fila.aprobado_por,
    aprobadoEn: aNumeroOpcional(fila.aprobado_en),
    componentes: componentes.map(componenteAJson),
  };
}

module.exports = { usuarioAJson, componenteAJson, levantamientoAJson };
