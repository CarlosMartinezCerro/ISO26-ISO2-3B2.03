package es.uclm.esi.iso2.radiodiagnostico.cita;

/** Cuerpo de la petición del CdU2: el DNI del paciente que confirma (el id va en la URL). */
public record DatosConfirmacionCita(String idCita, String dniPaciente) {
}
