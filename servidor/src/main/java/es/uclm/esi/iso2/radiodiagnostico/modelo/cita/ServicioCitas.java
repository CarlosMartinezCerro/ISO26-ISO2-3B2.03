package es.uclm.esi.iso2.radiodiagnostico.modelo.cita;

import es.uclm.esi.iso2.radiodiagnostico.modelo.solicitud.SolicitudNoEncontradaException;
import es.uclm.esi.iso2.radiodiagnostico.modelo.solicitud.SolicitudPrueba;
import java.util.List;
import java.util.Optional;

/** Contrato del servidor para el CdU25 · Asignar cita solicitada. */
public interface ServicioCitas {

    /**
     * Devuelve las solicitudes que aún no tienen cita, ordenadas por prioridad
     * (primero URGENTE, luego PREFERENTE y por último ORDINARIO).
     */
    List<SolicitudPrueba> listarSolicitudesPendientes();

    /**
     * Asigna una cita a una solicitud pendiente y la marca como CITADA.
     *
     * @throws DatosAsignacionInvalidosException si falta algún dato o la fecha ya ha pasado
     * @throws SolicitudNoEncontradaException si no existe la solicitud
     * @throws SolicitudNoPendienteException si la solicitud ya tiene cita
     * @throws HuecoOcupadoException si la sala ya está ocupada a esa hora
     */
    Cita asignarCita(DatosAsignacionCita datos);

    Optional<Cita> buscarCitaPorSolicitud(String idSolicitud);
    List<Cita> buscarCitasPorPaciente(String dniPaciente);
}
