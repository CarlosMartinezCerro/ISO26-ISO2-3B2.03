package es.uclm.esi.iso2.radiodiagnostico.modelo.prueba;

import java.util.List;
import java.util.Optional;

/** Contrato del servidor para el CdU13 · Solicitar prueba de seguimiento periódica (RF-08). */
public interface ServicioSolicitudSeguimiento {

    /** Máximo de repeticiones que puede generar una sola solicitud de seguimiento. */
    int MAXIMO_REPETICIONES = 100;

    /**
     * Crea la solicitud de seguimiento y genera una repetición en estado PENDIENTE_CITA por
     * cada fecha prevista, empezando por la fecha de inicio.
     *
     * @throws UsuarioNoMedicoException si {@code dniMedico} no corresponde a un médico
     * @throws TipoPruebaSinSeguimientoException si el tipo de prueba no admite seguimiento
     * @throws DatosSeguimientoInvalidosException si falta algún dato obligatorio, el tipo de
     *         prueba no existe, la periodicidad no es mayor que 0, la fecha de inicio es anterior
     *         a hoy, no se indica exactamente un criterio de fin (número de repeticiones mayor o
     *         igual que 1 o fecha límite posterior a la de inicio) o se superarían
     *         {@link #MAXIMO_REPETICIONES} repeticiones
     */
    SolicitudSeguimiento solicitar(DatosSolicitudSeguimiento datos);

    Optional<SolicitudSeguimiento> buscarPorId(String id);

    List<SolicitudSeguimiento> buscarPorPaciente(String dniPaciente);
}
