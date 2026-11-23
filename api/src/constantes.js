// Valores permitidos que comparten la API y la app.
const ROLES = ['Inspector', 'Supervisor'];

const ESTADOS = {
  PENDIENTE: 'Pendiente',
  APROBADO: 'Aprobado',
  VALIDADO: 'Validado',
  OBSERVADO: 'Observado',
};

const ESTADOS_COMPONENTE = [ESTADOS.PENDIENTE, ESTADOS.VALIDADO, ESTADOS.OBSERVADO];

module.exports = { ROLES, ESTADOS, ESTADOS_COMPONENTE };
