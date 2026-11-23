package es.uclm.esi.iso2.radiodiagnostico.modelo.informe;

/** Comunicacion del cliente con las APIs REST de informes. */
public interface ClienteInformes {

    ResultadoCreacionInforme crearInforme(DatosCreacionInforme datos);

    ResultadoAntecedentes obtenerAntecedentes(String dniPaciente, String especialidad);
}
