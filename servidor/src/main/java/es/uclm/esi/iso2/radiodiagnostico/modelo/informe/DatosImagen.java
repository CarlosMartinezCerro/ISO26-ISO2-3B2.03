package es.uclm.esi.iso2.radiodiagnostico.modelo.informe;

/** Datos que envía el cliente para crear una imagen (CdU17). */
public record DatosImagen(String idInforme, String dniPaciente, String idMedico,
                          String tipoPrueba, String nombreFichero, byte[] contenido) {
}
