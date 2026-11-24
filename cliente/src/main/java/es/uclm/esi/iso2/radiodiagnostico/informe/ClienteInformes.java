package es.uclm.esi.iso2.radiodiagnostico.informe;

import java.util.List;

/** Comunicacion del cliente con las APIs REST de informes. */
public interface ClienteInformes {

    ResultadoCreacionInforme crearInforme(DatosCreacionInforme datos);

    ResultadoAntecedentes obtenerAntecedentes(String dniPaciente, String especialidad);

    List<InformeResumen> listarInformes(String dniPaciente);

    InformeRespuesta obtenerInforme(long idInforme);
}
