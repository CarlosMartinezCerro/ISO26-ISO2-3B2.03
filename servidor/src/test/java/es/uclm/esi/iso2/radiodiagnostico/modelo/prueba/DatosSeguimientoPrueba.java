package es.uclm.esi.iso2.radiodiagnostico.modelo.prueba;

import es.uclm.esi.iso2.radiodiagnostico.modelo.solicitud.PrioridadSolicitud;
import java.time.LocalDate;

/**
 * Constructor de {@link DatosSolicitudSeguimiento} para las pruebas, con datos ficticios.
 * Por defecto: médico válido, TAC cada 3 meses desde {@link #HOY}, 4 repeticiones.
 */
public final class DatosSeguimientoPrueba {

    public static final LocalDate HOY = LocalDate.of(2026, 11, 18);
    public static final String DNI_PACIENTE = "12345678Z";
    public static final String DNI_MEDICO = "00000001R";
    public static final String DNI_NO_MEDICO = "99999999R";

    String dniPaciente = DNI_PACIENTE;
    String dniMedico = DNI_MEDICO;
    String tipoPrueba = "TAC";
    String indicacionClinica = "Control de nódulo pulmonar (caso ficticio)";
    String justificacion = "Seguimiento de evolución (caso ficticio)";
    PrioridadSolicitud prioridad = PrioridadSolicitud.ORDINARIO;
    Integer cada = 3;
    UnidadPeriodicidad unidad = UnidadPeriodicidad.MESES;
    LocalDate fechaInicio = HOY;
    Integer numeroRepeticiones = 4;
    LocalDate fechaLimite;

    public static DatosSeguimientoPrueba validos() {
        return new DatosSeguimientoPrueba();
    }

    public DatosSeguimientoPrueba medico(String dni) {
        dniMedico = dni;
        return this;
    }

    public DatosSeguimientoPrueba tipo(String tipo) {
        tipoPrueba = tipo;
        return this;
    }

    public DatosSeguimientoPrueba cada(Integer valor, UnidadPeriodicidad nuevaUnidad) {
        cada = valor;
        unidad = nuevaUnidad;
        return this;
    }

    public DatosSeguimientoPrueba inicio(LocalDate fecha) {
        fechaInicio = fecha;
        return this;
    }

    public DatosSeguimientoPrueba repeticiones(Integer numero) {
        numeroRepeticiones = numero;
        return this;
    }

    public DatosSeguimientoPrueba limite(LocalDate fecha) {
        fechaLimite = fecha;
        return this;
    }

    /** Deja vacío (nulo o en blanco) el campo indicado. */
    public DatosSeguimientoPrueba sin(String campo, boolean enBlanco) {
        String vacio = enBlanco ? "  " : null;
        switch (campo) {
            case "dniPaciente" -> dniPaciente = vacio;
            case "dniMedico" -> dniMedico = vacio;
            case "tipoPrueba" -> tipoPrueba = vacio;
            case "indicacionClinica" -> indicacionClinica = vacio;
            case "justificacion" -> justificacion = vacio;
            case "prioridad" -> prioridad = null;
            case "cada" -> cada = null;
            case "unidad" -> unidad = null;
            case "fechaInicio" -> fechaInicio = null;
            default -> throw new IllegalArgumentException("Campo desconocido: " + campo);
        }
        return this;
    }

    public DatosSolicitudSeguimiento crear() {
        return new DatosSolicitudSeguimiento(dniPaciente, dniMedico, tipoPrueba, indicacionClinica,
                justificacion, prioridad, cada, unidad, fechaInicio, numeroRepeticiones, fechaLimite);
    }
}
