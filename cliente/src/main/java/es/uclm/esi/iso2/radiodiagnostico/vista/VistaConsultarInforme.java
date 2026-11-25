package es.uclm.esi.iso2.radiodiagnostico.vista;

import es.uclm.esi.iso2.radiodiagnostico.informe.InformeRespuesta;
import es.uclm.esi.iso2.radiodiagnostico.informe.InformeResumen;
import java.util.List;

public interface VistaConsultarInforme {

    String obtenerDniPaciente();

    InformeResumen obtenerInformeSeleccionado();

    void mostrarInformes(List<InformeResumen> informes);

    void mostrarInforme(InformeRespuesta informe);

    void mostrarMensaje(String mensaje);

    void mostrarError(String mensaje);
}
