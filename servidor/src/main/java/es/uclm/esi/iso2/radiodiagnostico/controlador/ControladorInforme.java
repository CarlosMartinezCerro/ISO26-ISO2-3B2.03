package es.uclm.esi.iso2.radiodiagnostico.controlador;

import java.util.Objects;

import es.uclm.esi.iso2.radiodiagnostico.modelo.informe.Informe;
import es.uclm.esi.iso2.radiodiagnostico.modelo.informe.ServicioInforme;

/** Controlador servidor que delega las operaciones de informes en su servicio. */
public class ControladorInforme {

    private final ServicioInforme servicio;

    public ControladorInforme(ServicioInforme servicio) {
        this.servicio = Objects.requireNonNull(servicio, "El servicio de informes es obligatorio");
    }

    public Informe crearInforme(Informe informe) {
        return servicio.crearInforme(informe);
    }

    public String obtenerAntecedentes(Informe informe) {
        return servicio.obtenerAntecedentes(informe);
    }
}
