package es.uclm.esi.iso2.radiodiagnostico.cita;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import es.uclm.esi.iso2.radiodiagnostico.solicitud.EstadoSolicitud;
import es.uclm.esi.iso2.radiodiagnostico.solicitud.PrioridadSolicitud;
import es.uclm.esi.iso2.radiodiagnostico.solicitud.SolicitudNoEncontradaException;
import es.uclm.esi.iso2.radiodiagnostico.solicitud.SolicitudPrueba;
import es.uclm.esi.iso2.radiodiagnostico.solicitud.SolicitudRepositorioEnMemoria;
import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ServicioCitasImplTest {

    private static final ZoneId ZONA = ZoneId.of("Europe/Madrid");
    /** "Ahora" fijo para las pruebas: 16/11/2026 a las 10:00. */
    private static final LocalDateTime AHORA = LocalDateTime.of(2026, 11, 16, 10, 0);
    private static final LocalDateTime MANANA = AHORA.plusDays(1);

    private SolicitudRepositorioEnMemoria solicitudes;
    private ServicioCitas servicio;
    private CitaRepositorioEnMemoria citas;

    @BeforeEach
    void crearServicio() {
        solicitudes = new SolicitudRepositorioEnMemoria();
        citas = new CitaRepositorioEnMemoria();

        Clock reloj = Clock.fixed(AHORA.atZone(ZONA).toInstant(), ZONA);
        servicio = new ServicioCitasImpl(solicitudes, citas, reloj);

        solicitudes.guardar(solicitud("SOL-001", PrioridadSolicitud.ORDINARIO));
        solicitudes.guardar(solicitud("SOL-002", PrioridadSolicitud.URGENTE));
        solicitudes.guardar(solicitud("SOL-003", PrioridadSolicitud.PREFERENTE));
    }

    @Test
    void asignaCitaASolicitudPendiente() {
        Cita cita = servicio.asignarCita(new DatosAsignacionCita("SOL-001", MANANA, "SALA-RX-1"));

        assertEquals(new Cita("CITA-SOL-001", "SOL-001", "FICTICIO-001", "Radiografía",
                MANANA, "SALA-RX-1", EstadoCita.ASIGNADA), cita);
    }

    @Test
    void laSolicitudPasaACitadaTrasAsignar() {
        servicio.asignarCita(new DatosAsignacionCita("SOL-001", MANANA, "SALA-RX-1"));

        assertEquals(EstadoSolicitud.CITADA, solicitudes.buscarPorId("SOL-001").orElseThrow().estado());
    }

    @Test
    void permiteRecuperarLaCitaPorSolicitud() {
        Cita cita = servicio.asignarCita(new DatosAsignacionCita("SOL-001", MANANA, "SALA-RX-1"));

        assertEquals(Optional.of(cita), servicio.buscarCitaPorSolicitud("SOL-001"));
    }

    @Test
    void listaPendientesOrdenadasPorPrioridad() {
        List<String> ids = servicio.listarSolicitudesPendientes().stream()
                .map(SolicitudPrueba::id)
                .toList();

        assertEquals(List.of("SOL-002", "SOL-003", "SOL-001"), ids);
    }

    @Test
    void unaSolicitudCitadaDejaDeSalirEnPendientes() {
        servicio.asignarCita(new DatosAsignacionCita("SOL-002", MANANA, "SALA-RX-1"));

        List<String> ids = servicio.listarSolicitudesPendientes().stream()
                .map(SolicitudPrueba::id)
                .toList();

        assertEquals(List.of("SOL-003", "SOL-001"), ids);
    }

    @Test
    void rechazaDatosNulos() {
        assertThrows(DatosAsignacionInvalidosException.class, () -> servicio.asignarCita(null));
    }

    @Test
    void rechazaIdSolicitudVacio() {
        assertDatoInvalido(new DatosAsignacionCita(" ", MANANA, "SALA-RX-1"), "idSolicitud");
    }

    @Test
    void rechazaIdSolicitudNulo() {
        assertDatoInvalido(new DatosAsignacionCita(null, MANANA, "SALA-RX-1"), "idSolicitud");
    }

    @Test
    void rechazaSalaVacia() {
        assertDatoInvalido(new DatosAsignacionCita("SOL-001", MANANA, ""), "sala");
    }

    @Test
    void rechazaFechaNula() {
        assertDatoInvalido(new DatosAsignacionCita("SOL-001", null, "SALA-RX-1"), "fechaHora");
    }

    @Test
    void rechazaFechaPasada() {
        assertDatoInvalido(new DatosAsignacionCita("SOL-001", AHORA.minusHours(1), "SALA-RX-1"),
                "fechaHora");
    }

    @Test
    void rechazaFechaIgualAAhora() {
        assertDatoInvalido(new DatosAsignacionCita("SOL-001", AHORA, "SALA-RX-1"), "fechaHora");
    }

    @Test
    void rechazaSolicitudInexistente() {
        SolicitudNoEncontradaException excepcion = assertThrows(SolicitudNoEncontradaException.class,
                () -> servicio.asignarCita(new DatosAsignacionCita("SOL-999", MANANA, "SALA-RX-1")));

        assertTrue(excepcion.getMessage().contains("SOL-999"));
    }

    @Test
    void rechazaAsignarDosVecesLaMismaSolicitud() {
        servicio.asignarCita(new DatosAsignacionCita("SOL-001", MANANA, "SALA-RX-1"));

        assertThrows(SolicitudNoPendienteException.class,
                () -> servicio.asignarCita(new DatosAsignacionCita("SOL-001",
                        MANANA.plusHours(1), "SALA-RX-2")));
    }

    @Test
    void rechazaHuecoOcupadoEnLaMismaSala() {
        servicio.asignarCita(new DatosAsignacionCita("SOL-001", MANANA, "SALA-RX-1"));

        assertThrows(HuecoOcupadoException.class,
                () -> servicio.asignarCita(new DatosAsignacionCita("SOL-002", MANANA, "SALA-RX-1")));
    }

    @Test
    void huecoOcupadoNoCambiaElEstadoDeLaSolicitud() {
        servicio.asignarCita(new DatosAsignacionCita("SOL-001", MANANA, "SALA-RX-1"));

        assertThrows(HuecoOcupadoException.class,
                () -> servicio.asignarCita(new DatosAsignacionCita("SOL-002", MANANA, "SALA-RX-1")));

        assertEquals(EstadoSolicitud.PENDIENTE, solicitudes.buscarPorId("SOL-002").orElseThrow().estado());
        assertEquals(Optional.empty(), servicio.buscarCitaPorSolicitud("SOL-002"));
    }

    @Test
    void permiteMismaHoraEnOtraSala() {
        servicio.asignarCita(new DatosAsignacionCita("SOL-001", MANANA, "SALA-RX-1"));

        Cita cita = servicio.asignarCita(new DatosAsignacionCita("SOL-002", MANANA, "SALA-RX-2"));

        assertEquals("SALA-RX-2", cita.sala());
    }

    @Test
    void elConstructorSinRelojFunciona() {
        ServicioCitas sinReloj = new ServicioCitasImpl(solicitudes, new CitaRepositorioEnMemoria());

        Cita cita = sinReloj.asignarCita(new DatosAsignacionCita("SOL-001",
                LocalDateTime.now().plusYears(1), "SALA-RX-1"));

        assertEquals(EstadoCita.ASIGNADA, cita.estado());
    }

    @Test
    void permiteConsultarCitasDeUnPaciente() {
        Cita cita1 = new Cita("CITA-1", "SOL-1", "12345678A",
                "Radiografía", MANANA, "SALA-RX-1", EstadoCita.ASIGNADA);

        Cita cita2 = new Cita("CITA-2", "SOL-2", "87654321B",
                "Resonancia", MANANA.plusDays(1), "SALA-RX-2", EstadoCita.ASIGNADA);

        citas.guardar(cita1);
        citas.guardar(cita2);

        List<Cita> resultado = servicio.buscarCitasPorPaciente("12345678A");

        assertEquals(1, resultado.size());
        assertEquals(cita1, resultado.get(0));
    }

    private void assertDatoInvalido(DatosAsignacionCita datos, String campo) {
        DatosAsignacionInvalidosException excepcion = assertThrows(
                DatosAsignacionInvalidosException.class, () -> servicio.asignarCita(datos));

        assertTrue(excepcion.getMessage().contains(campo),
                "El mensaje debe identificar el campo inválido: " + campo);
    }

    private static SolicitudPrueba solicitud(String id, PrioridadSolicitud prioridad) {
        return new SolicitudPrueba(id, "FICTICIO-001", "Radiografía", prioridad,
                EstadoSolicitud.PENDIENTE);
    }
}
