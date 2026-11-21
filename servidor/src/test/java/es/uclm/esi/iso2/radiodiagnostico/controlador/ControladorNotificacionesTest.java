package es.uclm.esi.iso2.radiodiagnostico.controlador;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import es.uclm.esi.iso2.radiodiagnostico.notificacion.CanalNotificacion;
import es.uclm.esi.iso2.radiodiagnostico.notificacion.DatosNotificacion;
import es.uclm.esi.iso2.radiodiagnostico.notificacion.Notificacion;
import es.uclm.esi.iso2.radiodiagnostico.notificacion.NotificacionRepositorioEnMemoria;
import es.uclm.esi.iso2.radiodiagnostico.notificacion.PasarelaNoDisponibleException;
import es.uclm.esi.iso2.radiodiagnostico.notificacion.PasarelaNotificaciones;
import es.uclm.esi.iso2.radiodiagnostico.notificacion.RespuestaListadoNotificaciones;
import es.uclm.esi.iso2.radiodiagnostico.notificacion.ResultadoNotificacion;
import es.uclm.esi.iso2.radiodiagnostico.notificacion.ServicioNotificacionesImpl;
import es.uclm.esi.iso2.radiodiagnostico.notificacion.TipoNotificacion;
import es.uclm.esi.iso2.radiodiagnostico.paciente.Paciente;
import es.uclm.esi.iso2.radiodiagnostico.paciente.PacienteRepositorioEnMemoria;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;

class ControladorNotificacionesTest {

    private static final DatosNotificacion SMS_A_JUAN =
            new DatosNotificacion("12345678Z", CanalNotificacion.SMS, "Recordatorio", "Traiga la tarjeta.");

    @Test
    void notificarCorrectamenteDevuelveElIdYUnMensaje() {
        ResultadoNotificacion resultado = controlador(notificacion -> { }).notificar(SMS_A_JUAN);

        assertEquals(new ResultadoNotificacion(true,
                "Notificación NOT-001 enviada por SMS al 612345678.", "NOT-001"), resultado);
    }

    @Test
    void elMensajeDiceQueSeEnvioALaAplicacion() {
        ResultadoNotificacion resultado = controlador(n -> { }).notificar(
                new DatosNotificacion("12345678Z", CanalNotificacion.APLICACION, "Aviso", "Hola"));

        assertEquals("Notificación NOT-001 enviada a la aplicación del paciente 12345678Z.", resultado.mensaje());
    }

    @Test
    void elCdU5EnviaNotificacionesGenerales() {
        NotificacionRepositorioEnMemoria notificaciones = new NotificacionRepositorioEnMemoria();
        new ControladorNotificaciones(new ServicioNotificacionesImpl(pacientes(), notificaciones, n -> { }))
                .notificar(SMS_A_JUAN);

        assertEquals(TipoNotificacion.GENERAL, notificaciones.buscarPorPaciente("12345678Z").get(0).tipo());
    }

    @Test
    void datosInvalidosDevuelvenElMensajeDelModelo() {
        ResultadoNotificacion resultado = controlador(n -> { }).notificar(
                new DatosNotificacion("12345678Z", CanalNotificacion.SMS, "", "Hola"));

        assertFalse(resultado.correcto());
        assertEquals("El campo asunto es obligatorio.", resultado.mensaje());
        assertNull(resultado.idNotificacion());
    }

    @Test
    void pacienteInexistenteDevuelveError() {
        ResultadoNotificacion resultado = controlador(n -> { }).notificar(
                new DatosNotificacion("87654321X", CanalNotificacion.SMS, "A", "B"));

        assertFalse(resultado.correcto());
        assertEquals("No existe ningún paciente con el DNI 87654321X.", resultado.mensaje());
    }

    @Test
    void pasarelaCaidaDevuelveUnMensajeSinDetallesInternos() {
        ResultadoNotificacion resultado = controlador(n -> {
            throw new PasarelaNoDisponibleException("timeout operador 503");
        }).notificar(SMS_A_JUAN);

        assertFalse(resultado.correcto());
        assertEquals(ControladorNotificaciones.PASARELA_NO_DISPONIBLE, resultado.mensaje());
    }

    @Test
    void falloDeBaseDeDatosAlNotificarDevuelveErrorInterno() {
        ResultadoNotificacion resultado = sinBaseDeDatos().notificar(SMS_A_JUAN);

        assertFalse(resultado.correcto());
        assertEquals(ControladorNotificaciones.ERROR_INTERNO, resultado.mensaje());
    }

    @Test
    void historialDevuelveLasNotificacionesYCuantasHay() {
        ControladorNotificaciones controlador = controlador(n -> { });
        controlador.notificar(SMS_A_JUAN);

        RespuestaListadoNotificaciones respuesta = controlador.historialPaciente("12345678Z");

        assertTrue(respuesta.correcto());
        assertEquals("1 notificación(es) encontradas.", respuesta.mensaje());
        assertEquals(List.of("NOT-001"), respuesta.notificaciones().stream().map(Notificacion::id).toList());
    }

    @Test
    void historialSinDniDevuelveError() {
        RespuestaListadoNotificaciones respuesta = controlador(n -> { }).historialPaciente(" ");

        assertFalse(respuesta.correcto());
        assertEquals("El campo dni es obligatorio.", respuesta.mensaje());
        assertTrue(respuesta.notificaciones().isEmpty());
    }

    @Test
    void falloDeBaseDeDatosAlConsultarDevuelveErrorInterno() {
        RespuestaListadoNotificaciones respuesta = sinBaseDeDatos().historialPaciente("12345678Z");

        assertFalse(respuesta.correcto());
        assertEquals(ControladorNotificaciones.ERROR_INTERNO, respuesta.mensaje());
    }

    @Test
    void servicioObligatorio() {
        assertThrows(NullPointerException.class, () -> new ControladorNotificaciones(null));
    }

    private static ControladorNotificaciones controlador(PasarelaNotificaciones pasarela) {
        return new ControladorNotificaciones(new ServicioNotificacionesImpl(pacientes(),
                new NotificacionRepositorioEnMemoria(), pasarela));
    }

    /** Repositorio que falla como lo haría MySQL caído. */
    private static ControladorNotificaciones sinBaseDeDatos() {
        NotificacionRepositorioEnMemoria caido = new NotificacionRepositorioEnMemoria() {
            @Override
            public int contar() {
                throw new IllegalStateException("MySQL caído");
            }

            @Override
            public List<Notificacion> buscarPorPaciente(String dniPaciente) {
                throw new IllegalStateException("MySQL caído");
            }
        };
        return new ControladorNotificaciones(new ServicioNotificacionesImpl(pacientes(), caido, n -> { }));
    }

    static PacienteRepositorioEnMemoria pacientes() {
        PacienteRepositorioEnMemoria pacientes = new PacienteRepositorioEnMemoria();
        pacientes.guardar(new Paciente("12345678Z", "Juan", "García", LocalDate.of(1990, 1, 1), "612345678"));
        return pacientes;
    }
}
