package es.uclm.esi.iso2.radiodiagnostico.notificacion;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import es.uclm.esi.iso2.radiodiagnostico.paciente.Paciente;
import es.uclm.esi.iso2.radiodiagnostico.paciente.PacienteRepositorioEnMemoria;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ServicioNotificacionesImplTest {

    private static final LocalDateTime AHORA = LocalDateTime.of(2026, 11, 21, 10, 0);
    private static final String DNI_CON_TELEFONO = "12345678Z";
    private static final String DNI_SIN_TELEFONO = "01234567L";

    private final List<Notificacion> enviadas = new ArrayList<>();
    private NotificacionRepositorioEnMemoria notificaciones;
    private ServicioNotificacionesImpl servicio;

    @BeforeEach
    void crearServicio() {
        PacienteRepositorioEnMemoria pacientes = new PacienteRepositorioEnMemoria();
        pacientes.guardar(new Paciente(DNI_CON_TELEFONO, "Juan", "García", LocalDate.of(1990, 1, 1), "612345678"));
        pacientes.guardar(new Paciente(DNI_SIN_TELEFONO, "Lucía", "Pérez", LocalDate.of(1985, 5, 5), null));
        notificaciones = new NotificacionRepositorioEnMemoria();
        Clock reloj = Clock.fixed(AHORA.toInstant(ZoneOffset.UTC), ZoneId.of("UTC"));
        servicio = new ServicioNotificacionesImpl(pacientes, notificaciones, enviadas::add, reloj);
    }

    @Test
    void notificaPorSmsAlTelefonoDelPacienteYLaGuarda() {
        Notificacion notificacion = servicio.notificar(
                datos(" 12345678z ", CanalNotificacion.SMS, " Recordatorio ", " Traiga su tarjeta sanitaria. "),
                TipoNotificacion.GENERAL);

        assertEquals(new Notificacion("NOT-001", DNI_CON_TELEFONO, TipoNotificacion.GENERAL, CanalNotificacion.SMS,
                "612345678", "Recordatorio", "Traiga su tarjeta sanitaria.", AHORA), notificacion);
        assertEquals(List.of(notificacion), enviadas);
        assertEquals(List.of(notificacion), notificaciones.buscarPorPaciente(DNI_CON_TELEFONO));
    }

    @Test
    void notificaPorLaAplicacionAunqueElPacienteNoTengaTelefono() {
        Notificacion notificacion = servicio.notificar(
                datos(DNI_SIN_TELEFONO, CanalNotificacion.APLICACION, "Aviso", "Su prueba está lista."),
                TipoNotificacion.GENERAL);

        assertEquals(DNI_SIN_TELEFONO, notificacion.destino());
        assertEquals(CanalNotificacion.APLICACION, notificacion.canal());
    }

    @Test
    void guardaElTipoQueIndicanOtrosCasosDeUso() {
        Notificacion notificacion = servicio.notificar(
                datos(DNI_CON_TELEFONO, CanalNotificacion.APLICACION, "Informe", "Ya puede consultarlo."),
                TipoNotificacion.INFORME);

        assertEquals(TipoNotificacion.INFORME, notificacion.tipo());
    }

    @Test
    void numeraLasNotificacionesSeguidas() {
        servicio.notificar(datos(DNI_CON_TELEFONO, CanalNotificacion.SMS, "A", "B"), TipoNotificacion.GENERAL);
        Notificacion segunda = servicio.notificar(
                datos(DNI_CON_TELEFONO, CanalNotificacion.SMS, "C", "D"), TipoNotificacion.GENERAL);

        assertEquals("NOT-002", segunda.id());
    }

    @Test
    void smsAPacienteSinTelefonoSeRechaza() {
        NotificacionInvalidaException e = assertThrows(NotificacionInvalidaException.class, () -> servicio.notificar(
                datos(DNI_SIN_TELEFONO, CanalNotificacion.SMS, "Aviso", "Hola"), TipoNotificacion.GENERAL));

        assertEquals("El paciente 01234567L no tiene teléfono: no se le puede enviar un SMS.", e.getMessage());
        assertTrue(enviadas.isEmpty());
        assertEquals(0, notificaciones.contar());
    }

    @Test
    void pacienteQueNoExisteSeRechaza() {
        PacienteNoEncontradoException e = assertThrows(PacienteNoEncontradoException.class, () -> servicio.notificar(
                datos("87654321X", CanalNotificacion.SMS, "Aviso", "Hola"), TipoNotificacion.GENERAL));

        assertEquals("No existe ningún paciente con el DNI 87654321X.", e.getMessage());
    }

    @Test
    void datosNulosSeRechazan() {
        assertMensaje("Los datos de la notificación son obligatorios.", null);
    }

    @Test
    void dniVacioSeRechaza() {
        assertMensaje("El campo dni es obligatorio.", datos("  ", CanalNotificacion.SMS, "A", "B"));
        assertMensaje("El campo dni es obligatorio.", datos(null, CanalNotificacion.SMS, "A", "B"));
    }

    @Test
    void canalNuloSeRechaza() {
        assertMensaje("El campo canal es obligatorio.", datos(DNI_CON_TELEFONO, null, "A", "B"));
    }

    @Test
    void asuntoYMensajeSonObligatorios() {
        assertMensaje("El campo asunto es obligatorio.", datos(DNI_CON_TELEFONO, CanalNotificacion.SMS, " ", "B"));
        assertMensaje("El campo mensaje es obligatorio.", datos(DNI_CON_TELEFONO, CanalNotificacion.SMS, "A", null));
    }

    @Test
    void textosDemasiadoLargosSeRechazan() {
        assertMensaje("El campo asunto no puede tener más de 100 caracteres.",
                datos(DNI_CON_TELEFONO, CanalNotificacion.SMS, "a".repeat(101), "B"));
        assertMensaje("El campo mensaje no puede tener más de 500 caracteres.",
                datos(DNI_CON_TELEFONO, CanalNotificacion.SMS, "A", "m".repeat(501)));
    }

    @Test
    void textosEnElLimiteSeAceptan() {
        Notificacion notificacion = servicio.notificar(datos(DNI_CON_TELEFONO, CanalNotificacion.SMS,
                "a".repeat(100), "m".repeat(500)), TipoNotificacion.GENERAL);

        assertEquals(100, notificacion.asunto().length());
    }

    @Test
    void siLaPasarelaFallaNoSeGuarda() {
        ServicioNotificacionesImpl conPasarelaCaida = new ServicioNotificacionesImpl(pacientesConJuan(),
                notificaciones, notificacion -> {
                    throw new PasarelaNoDisponibleException("Operador caído");
                });

        assertThrows(PasarelaNoDisponibleException.class, () -> conPasarelaCaida.notificar(
                datos(DNI_CON_TELEFONO, CanalNotificacion.SMS, "A", "B"), TipoNotificacion.GENERAL));
        assertEquals(0, notificaciones.contar());
    }

    @Test
    void historialDevuelveLasDelPacienteDeLaMasRecienteALaMasAntigua() {
        servicio.notificar(datos(DNI_CON_TELEFONO, CanalNotificacion.SMS, "Primera", "1"), TipoNotificacion.GENERAL);
        servicio.notificar(datos(DNI_SIN_TELEFONO, CanalNotificacion.APLICACION, "Otra", "2"), TipoNotificacion.GENERAL);
        servicio.notificar(datos(DNI_CON_TELEFONO, CanalNotificacion.SMS, "Segunda", "3"), TipoNotificacion.GENERAL);

        List<Notificacion> historial = servicio.historialPaciente(" 12345678z ");

        assertEquals(List.of("Segunda", "Primera"), historial.stream().map(Notificacion::asunto).toList());
    }

    @Test
    void historialSinDniSeRechaza() {
        assertThrows(NotificacionInvalidaException.class, () -> servicio.historialPaciente(""));
    }

    @Test
    void dependenciasObligatorias() {
        PacienteRepositorioEnMemoria pacientes = new PacienteRepositorioEnMemoria();
        PasarelaNotificaciones pasarela = enviadas::add;
        assertThrows(NullPointerException.class, () -> new ServicioNotificacionesImpl(null, notificaciones, pasarela));
        assertThrows(NullPointerException.class, () -> new ServicioNotificacionesImpl(pacientes, null, pasarela));
        assertThrows(NullPointerException.class, () -> new ServicioNotificacionesImpl(pacientes, notificaciones, null));
        assertThrows(NullPointerException.class,
                () -> new ServicioNotificacionesImpl(pacientes, notificaciones, pasarela, null));
        assertThrows(NullPointerException.class, () -> servicio.notificar(
                datos(DNI_CON_TELEFONO, CanalNotificacion.SMS, "A", "B"), null));
    }

    @Test
    void laPasarelaSimuladaNoFalla() {
        new PasarelaNotificacionesSimulada().enviar(new Notificacion("NOT-001", DNI_CON_TELEFONO,
                TipoNotificacion.GENERAL, CanalNotificacion.SMS, "612345678", "A", "B", AHORA));
    }

    private static PacienteRepositorioEnMemoria pacientesConJuan() {
        PacienteRepositorioEnMemoria pacientes = new PacienteRepositorioEnMemoria();
        pacientes.guardar(new Paciente(DNI_CON_TELEFONO, "Juan", "García", LocalDate.of(1990, 1, 1), "612345678"));
        return pacientes;
    }

    private void assertMensaje(String esperado, DatosNotificacion datos) {
        NotificacionInvalidaException e = assertThrows(NotificacionInvalidaException.class,
                () -> servicio.notificar(datos, TipoNotificacion.GENERAL));
        assertEquals(esperado, e.getMessage());
    }

    private static DatosNotificacion datos(String dni, CanalNotificacion canal, String asunto, String mensaje) {
        return new DatosNotificacion(dni, canal, asunto, mensaje);
    }
}
