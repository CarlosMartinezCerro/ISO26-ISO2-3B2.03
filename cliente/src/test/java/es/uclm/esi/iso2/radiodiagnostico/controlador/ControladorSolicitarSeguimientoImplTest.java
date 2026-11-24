package es.uclm.esi.iso2.radiodiagnostico.controlador;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import es.uclm.esi.iso2.radiodiagnostico.prueba.ClienteSeguimientos;
import es.uclm.esi.iso2.radiodiagnostico.prueba.DatosSolicitudSeguimiento;
import es.uclm.esi.iso2.radiodiagnostico.prueba.ErrorComunicacionException;
import es.uclm.esi.iso2.radiodiagnostico.prueba.PrioridadSeguimiento;
import es.uclm.esi.iso2.radiodiagnostico.prueba.RepeticionSeguimiento;
import es.uclm.esi.iso2.radiodiagnostico.prueba.ResultadoSolicitudSeguimiento;
import es.uclm.esi.iso2.radiodiagnostico.prueba.UnidadPeriodicidad;
import es.uclm.esi.iso2.radiodiagnostico.vista.VistaSolicitarSeguimiento;
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ControladorSolicitarSeguimientoImplTest {

    private static final LocalDate INICIO = LocalDate.of(2026, 11, 20);
    private static final List<RepeticionSeguimiento> REPETICIONES = List.of(
            new RepeticionSeguimiento("SEG-1-1", "SEG-1", 1, INICIO, "PENDIENTE_CITA"),
            new RepeticionSeguimiento("SEG-1-2", "SEG-1", 2, INICIO.plusMonths(3), "PENDIENTE_CITA"));

    private VistaFalsa vista;
    private ClienteFalso cliente;
    private ControladorSolicitarSeguimiento controlador;

    @BeforeEach
    void crearControlador() {
        vista = new VistaFalsa();
        cliente = new ClienteFalso();
        controlador = new ControladorSolicitarSeguimientoImpl(vista, cliente);
    }

    @Test
    void solicitudCorrectaConRepeticionesMuestraElMensajeYLasRepeticiones() {
        vista.datos = datos("12345678Z", "00000001R", 3, 2, null);
        cliente.resultado = new ResultadoSolicitudSeguimiento(true,
                "Solicitud de seguimiento de TAC registrada con 2 solicitudes pendientes de cita.",
                "SEG-1", REPETICIONES);

        controlador.solicitar();

        assertEquals(vista.datos, cliente.datosRecibidos);
        assertEquals("Solicitud de seguimiento de TAC registrada con 2 solicitudes pendientes de cita.",
                vista.exito);
        assertEquals(REPETICIONES, vista.repeticiones);
        assertNull(vista.error);
    }

    @Test
    void solicitudCorrectaConFechaLimiteSeEnvia() {
        vista.datos = datos("12345678Z", "00000001R", 3, null, INICIO.plusMonths(3));
        cliente.resultado = new ResultadoSolicitudSeguimiento(true, "ok", "SEG-1", REPETICIONES);

        controlador.solicitar();

        assertEquals(INICIO.plusMonths(3), cliente.datosRecibidos.fechaLimite());
        assertNull(cliente.datosRecibidos.numeroRepeticiones());
        assertEquals(REPETICIONES, vista.repeticiones);
    }

    @Test
    void rechazoDelServidorMuestraSuMensajeTalCual() {
        vista.datos = datos("12345678Z", "00000001R", 3, 2, null);
        cliente.resultado = new ResultadoSolicitudSeguimiento(false,
                "Solo un médico puede solicitar una prueba de seguimiento periódica.", null, null);

        controlador.solicitar();

        assertEquals("Solo un médico puede solicitar una prueba de seguimiento periódica.", vista.error);
        assertNull(vista.exito);
    }

    @Test
    void sinConexionMuestraMensajeSinDetallesTecnicos() {
        vista.datos = datos("12345678Z", "00000001R", 3, 2, null);
        cliente.sinConexion = true;

        controlador.solicitar();

        assertEquals(ControladorSolicitarSeguimientoImpl.MENSAJE_SIN_CONEXION, vista.error);
        assertNull(vista.exito);
    }

    @Test
    void numeroOFechaMalEscritosNoLlamanAlServidor() {
        vista.datos = null;

        controlador.solicitar();

        assertEquals(ControladorSolicitarSeguimientoImpl.MENSAJE_FORMATO_INCORRECTO, vista.error);
        assertNull(cliente.datosRecibidos);
    }

    @Test
    void dniPacienteVacioNoLlamaAlServidor() {
        assertErrorSinLlamar("Indique el DNI del paciente.", datos(" ", "00000001R", 3, 2, null));
    }

    @Test
    void dniMedicoVacioNoLlamaAlServidor() {
        assertErrorSinLlamar("Indique el DNI del médico.", datos("12345678Z", "", 3, 2, null));
    }

    @Test
    void tipoPruebaSinSeleccionarNoLlamaAlServidor() {
        assertErrorSinLlamar("Seleccione el tipo de prueba.", new DatosSolicitudSeguimiento(
                "12345678Z", "00000001R", null, "Indicación", "Justificación",
                PrioridadSeguimiento.ORDINARIO, 3, UnidadPeriodicidad.MESES, INICIO, 2, null));
    }

    @Test
    void indicacionVaciaNoLlamaAlServidor() {
        assertErrorSinLlamar("Indique la indicación clínica.", new DatosSolicitudSeguimiento(
                "12345678Z", "00000001R", "TAC", "", "Justificación",
                PrioridadSeguimiento.ORDINARIO, 3, UnidadPeriodicidad.MESES, INICIO, 2, null));
    }

    @Test
    void justificacionVaciaNoLlamaAlServidor() {
        assertErrorSinLlamar("Indique la justificación.", new DatosSolicitudSeguimiento(
                "12345678Z", "00000001R", "TAC", "Indicación", null,
                PrioridadSeguimiento.ORDINARIO, 3, UnidadPeriodicidad.MESES, INICIO, 2, null));
    }

    @Test
    void prioridadSinSeleccionarNoLlamaAlServidor() {
        assertErrorSinLlamar("Seleccione la prioridad.", new DatosSolicitudSeguimiento(
                "12345678Z", "00000001R", "TAC", "Indicación", "Justificación",
                null, 3, UnidadPeriodicidad.MESES, INICIO, 2, null));
    }

    @Test
    void periodicidadVaciaNoLlamaAlServidor() {
        assertErrorSinLlamar("Indique cada cuánto se repite la prueba.",
                datos("12345678Z", "00000001R", null, 2, null));
    }

    @Test
    void periodicidadCeroONegativaNoLlamaAlServidor() {
        assertErrorSinLlamar("La periodicidad debe ser un número mayor que 0.",
                datos("12345678Z", "00000001R", 0, 2, null));
        assertErrorSinLlamar("La periodicidad debe ser un número mayor que 0.",
                datos("12345678Z", "00000001R", -1, 2, null));
    }

    @Test
    void fechaDeInicioVaciaNoLlamaAlServidor() {
        assertErrorSinLlamar("Indique la fecha de inicio.", new DatosSolicitudSeguimiento(
                "12345678Z", "00000001R", "TAC", "Indicación", "Justificación",
                PrioridadSeguimiento.ORDINARIO, 3, UnidadPeriodicidad.MESES, null, 2, null));
    }

    @Test
    void sinCriterioDeFinNoLlamaAlServidor() {
        assertErrorSinLlamar(ControladorSolicitarSeguimientoImpl.MENSAJE_SIN_FIN,
                datos("12345678Z", "00000001R", 3, null, null));
    }

    @Test
    void numeroDeRepeticionesCeroNoLlamaAlServidor() {
        assertErrorSinLlamar("El número de repeticiones debe ser mayor que 0.",
                datos("12345678Z", "00000001R", 3, 0, null));
    }

    @Test
    void constructorRechazaDependenciasNulas() {
        assertThrows(NullPointerException.class,
                () -> new ControladorSolicitarSeguimientoImpl(null, cliente));
        assertThrows(NullPointerException.class,
                () -> new ControladorSolicitarSeguimientoImpl(vista, null));
    }

    private void assertErrorSinLlamar(String mensaje, DatosSolicitudSeguimiento datos) {
        vista.datos = datos;

        controlador.solicitar();

        assertEquals(mensaje, vista.error);
        assertNull(cliente.datosRecibidos);
    }

    private static DatosSolicitudSeguimiento datos(String dniPaciente, String dniMedico, Integer cada,
                                                   Integer repeticiones, LocalDate limite) {
        return new DatosSolicitudSeguimiento(dniPaciente, dniMedico, "TAC",
                "Indicación ficticia", "Justificación ficticia", PrioridadSeguimiento.URGENTE,
                cada, UnidadPeriodicidad.MESES, INICIO, repeticiones, limite);
    }

    private static final class VistaFalsa implements VistaSolicitarSeguimiento {
        DatosSolicitudSeguimiento datos;
        String exito;
        List<RepeticionSeguimiento> repeticiones;
        String error;

        @Override
        public DatosSolicitudSeguimiento obtenerDatos() {
            return datos;
        }

        @Override
        public void mostrarRepeticiones(String mensaje, List<RepeticionSeguimiento> lista) {
            exito = mensaje;
            repeticiones = lista;
        }

        @Override
        public void mostrarError(String mensaje) {
            error = mensaje;
        }
    }

    private static final class ClienteFalso implements ClienteSeguimientos {
        boolean sinConexion;
        ResultadoSolicitudSeguimiento resultado;
        DatosSolicitudSeguimiento datosRecibidos;

        @Override
        public ResultadoSolicitudSeguimiento solicitar(DatosSolicitudSeguimiento datos) {
            if (sinConexion) {
                throw new ErrorComunicacionException("Servidor no disponible",
                        new IOException("Conexión rechazada"));
            }
            datosRecibidos = datos;
            return resultado;
        }
    }
}
