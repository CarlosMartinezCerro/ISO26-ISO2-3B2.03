package es.uclm.esi.iso2.radiodiagnostico.vista;

import es.uclm.esi.iso2.radiodiagnostico.modelo.prueba.DatosSolicitudSeguimiento;
import es.uclm.esi.iso2.radiodiagnostico.modelo.prueba.RepeticionSeguimiento;
import java.util.List;

/** Lo que el controlador necesita de cualquier vista del CdU13 (escritorio, web...). */
public interface VistaSolicitarSeguimiento {

    /**
     * @return los datos introducidos (solo con el criterio de fin elegido), o null si un número
     *         o una fecha están mal escritos
     */
    DatosSolicitudSeguimiento obtenerDatos();

    /** Muestra la confirmación del servidor y las repeticiones generadas. */
    void mostrarRepeticiones(String mensaje, List<RepeticionSeguimiento> repeticiones);

    void mostrarError(String mensaje);
}
