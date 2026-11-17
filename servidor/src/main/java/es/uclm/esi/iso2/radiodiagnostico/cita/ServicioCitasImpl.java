package es.uclm.esi.iso2.radiodiagnostico.cita;

import es.uclm.esi.iso2.radiodiagnostico.solicitud.EstadoSolicitud;
import es.uclm.esi.iso2.radiodiagnostico.solicitud.SolicitudNoEncontradaException;
import es.uclm.esi.iso2.radiodiagnostico.solicitud.SolicitudPrueba;
import es.uclm.esi.iso2.radiodiagnostico.solicitud.SolicitudRepositorio;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class ServicioCitasImpl implements ServicioCitas {

    private final SolicitudRepositorio solicitudes;
    private final CitaRepositorio citas;
    private final Clock reloj;

    public ServicioCitasImpl(SolicitudRepositorio solicitudes, CitaRepositorio citas) {
        this(solicitudes, citas, Clock.systemDefaultZone());
    }

    /** El reloj se inyecta para poder probar la regla de "fecha futura". */
    public ServicioCitasImpl(SolicitudRepositorio solicitudes, CitaRepositorio citas, Clock reloj) {
        this.solicitudes = Objects.requireNonNull(solicitudes, "El repositorio de solicitudes es obligatorio");
        this.citas = Objects.requireNonNull(citas, "El repositorio de citas es obligatorio");
        this.reloj = Objects.requireNonNull(reloj, "El reloj es obligatorio");
    }

    @Override
    public List<SolicitudPrueba> listarSolicitudesPendientes() {
        return solicitudes.buscarPorEstado(EstadoSolicitud.PENDIENTE).stream()
                .sorted(Comparator.comparing(SolicitudPrueba::prioridad))
                .toList();
    }

    @Override
    public Cita asignarCita(DatosAsignacionCita datos) {
        validar(datos);

        SolicitudPrueba solicitud = solicitudes.buscarPorId(datos.idSolicitud())
                .orElseThrow(() -> new SolicitudNoEncontradaException(
                        "No existe la solicitud " + datos.idSolicitud() + "."));
        if (solicitud.estado() != EstadoSolicitud.PENDIENTE) {
            throw new SolicitudNoPendienteException(
                    "La solicitud " + solicitud.id() + " ya tiene una cita asignada.");
        }
        if (citas.existeCitaEn(datos.sala(), datos.fechaHora())) {
            throw new HuecoOcupadoException("La sala " + datos.sala()
                    + " ya está ocupada el " + datos.fechaHora() + ".");
        }

        Cita cita = new Cita("CITA-" + solicitud.id(), solicitud.id(), solicitud.dniPaciente(),
                solicitud.tipoPrueba(), datos.fechaHora(), datos.sala(), EstadoCita.ASIGNADA);
        citas.guardar(cita);
        solicitudes.guardar(solicitud.conEstado(EstadoSolicitud.CITADA));
        return cita;
    }

    @Override
    public Optional<Cita> buscarCitaPorSolicitud(String idSolicitud) {
        return citas.buscarPorSolicitud(idSolicitud);
    }

    private void validar(DatosAsignacionCita datos) {
        if (datos == null) {
            throw new DatosAsignacionInvalidosException("Los datos de la cita son obligatorios.");
        }
        validarTexto(datos.idSolicitud(), "idSolicitud");
        validarTexto(datos.sala(), "sala");
        if (datos.fechaHora() == null) {
            throw new DatosAsignacionInvalidosException("El campo fechaHora es obligatorio.");
        }
        if (!datos.fechaHora().isAfter(LocalDateTime.now(reloj))) {
            throw new DatosAsignacionInvalidosException(
                    "El campo fechaHora debe ser una fecha futura.");
        }
    }

    private void validarTexto(String valor, String campo) {
        if (valor == null || valor.isBlank()) {
            throw new DatosAsignacionInvalidosException("El campo " + campo + " es obligatorio.");
        }
    }
}
