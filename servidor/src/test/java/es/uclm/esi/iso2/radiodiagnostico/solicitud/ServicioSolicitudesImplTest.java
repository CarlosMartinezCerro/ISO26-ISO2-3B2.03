package es.uclm.esi.iso2.radiodiagnostico.solicitud;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class ServicioSolicitudesImplTest {

    @Test
    void registraSolicitudPendienteParaElPaciente() {
        SolicitudRepositorioEnMemoria repositorio = new SolicitudRepositorioEnMemoria();
        ServicioSolicitudes servicio = new ServicioSolicitudesImpl(repositorio);

        SolicitudPrueba solicitud = servicio.solicitar("12345678A",
                new DatosSolicitudPrueba("TAC", PrioridadSolicitud.URGENTE));

        assertEquals("12345678A", solicitud.dniPaciente());
        assertEquals("TAC", solicitud.tipoPrueba());
        assertEquals(EstadoSolicitud.PENDIENTE, solicitud.estado());
        assertEquals(solicitud, repositorio.buscarPorId(solicitud.id()).orElseThrow());
    }

    @Test
    void rechazaDatosObligatoriosAusentes() {
        ServicioSolicitudes servicio = new ServicioSolicitudesImpl(new SolicitudRepositorioEnMemoria());

        assertThrows(DatosSolicitudInvalidosException.class,
                () -> servicio.solicitar("12345678A", new DatosSolicitudPrueba(" ", PrioridadSolicitud.ORDINARIO)));
    }

    @Test
    void rechazaUnaPruebaQueNoEstaDisponible() {
        ServicioSolicitudes servicio = new ServicioSolicitudesImpl(new SolicitudRepositorioEnMemoria());

        assertThrows(DatosSolicitudInvalidosException.class,
                () -> servicio.solicitar("12345678A", new DatosSolicitudPrueba("Prueba inventada", PrioridadSolicitud.ORDINARIO)));
    }

    @Test
    void noConfirmaSiFallaLaPersistencia() {
        ServicioSolicitudes servicio = new ServicioSolicitudesImpl(new SolicitudRepositorio() {
            @Override public java.util.Optional<SolicitudPrueba> buscarPorId(String id) { return java.util.Optional.empty(); }
            @Override public java.util.List<SolicitudPrueba> buscarPorEstado(EstadoSolicitud estado) { return java.util.List.of(); }
            @Override public java.util.List<SolicitudPrueba> buscarPorPaciente(String dniPaciente) { return java.util.List.of(); }
            @Override public void guardar(SolicitudPrueba solicitud) { throw new IllegalStateException("fallo"); }
        });

        assertThrows(IllegalStateException.class,
                () -> servicio.solicitar("12345678A", new DatosSolicitudPrueba("TAC", PrioridadSolicitud.ORDINARIO)));
    }
}
