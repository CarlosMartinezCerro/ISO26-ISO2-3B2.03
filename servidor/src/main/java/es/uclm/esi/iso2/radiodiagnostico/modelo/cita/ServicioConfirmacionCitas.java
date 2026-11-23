package es.uclm.esi.iso2.radiodiagnostico.modelo.cita;

/** Contrato del servidor para el CdU2 · Confirmar cita (lo usa el paciente). */
public interface ServicioConfirmacionCitas {

    /**
     * Confirma una cita del paciente, que pasa de ASIGNADA a CONFIRMADA.
     *
     * @throws DatosConfirmacionInvalidosException si falta el id de la cita o el DNI
     * @throws CitaNoEncontradaException si la cita no existe o no es de ese paciente
     * @throws CitaNoConfirmableException si la cita no está ASIGNADA (por ejemplo, ya confirmada)
     */
    Cita confirmarCita(String idCita, String dniPaciente);
}
