package es.uclm.esi.iso2.radiodiagnostico.paciente;

/** Respuesta del servidor tras intentar dar de alta a un paciente. */
public record ResultadoAlta(boolean correcto, String mensaje) {
}
