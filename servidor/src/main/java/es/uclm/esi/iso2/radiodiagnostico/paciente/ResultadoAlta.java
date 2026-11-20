package es.uclm.esi.iso2.radiodiagnostico.paciente;

/** Respuesta que el servidor devuelve al cliente tras intentar dar de alta a un paciente. */
public record ResultadoAlta(boolean correcto, String mensaje) {
}
