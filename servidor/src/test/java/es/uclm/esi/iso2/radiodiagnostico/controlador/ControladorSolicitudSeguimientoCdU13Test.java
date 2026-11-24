package es.uclm.esi.iso2.radiodiagnostico.controlador;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import es.uclm.esi.iso2.radiodiagnostico.prueba.DatosSeguimientoInvalidosException;
import es.uclm.esi.iso2.radiodiagnostico.prueba.DatosSolicitudSeguimiento;
import es.uclm.esi.iso2.radiodiagnostico.prueba.EstadoRepeticionSeguimiento;
import es.uclm.esi.iso2.radiodiagnostico.prueba.PeriodicidadSeguimiento;
import es.uclm.esi.iso2.radiodiagnostico.prueba.RepeticionSeguimiento;
import es.uclm.esi.iso2.radiodiagnostico.prueba.ResultadoSolicitudSeguimiento;
import es.uclm.esi.iso2.radiodiagnostico.prueba.ServicioSolicitudSeguimiento;
import es.uclm.esi.iso2.radiodiagnostico.prueba.SolicitudSeguimiento;
import es.uclm.esi.iso2.radiodiagnostico.prueba.TipoPruebaSinSeguimientoException;
import es.uclm.esi.iso2.radiodiagnostico.prueba.UnidadPeriodicidad;
import es.uclm.esi.iso2.radiodiagnostico.prueba.UsuarioNoMedicoException;
import es.uclm.esi.iso2.radiodiagnostico.solicitud.PrioridadSolicitud;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

/**
 * CdU13, unidades CS y CK, con un servicio falso. Diseño: docs/testing/CdU13-diseno-pruebas.md.
 * Con dos variables, pairwise coincide con el producto cartesiano y contiene a each use (6.7).
 */
@DisplayName("CdU13 · ControladorSolicitudSeguimiento")
class ControladorSolicitudSeguimientoCdU13Test {

    private static final DatosSolicitudSeguimiento J1 = new DatosSolicitudSeguimiento("12345678Z",
            "00000001R", "TAC", "Control de nódulo pulmonar (caso ficticio)",
            "Seguimiento de evolución (caso ficticio)", PrioridadSolicitud.ORDINARIO, 3,
            UnidadPeriodicidad.MESES, LocalDate.of(2026, 11, 18), 4, null);
    private static final List<RepeticionSeguimiento> TRES = List.of(repeticion(1, 11), repeticion(2, 12),
            repeticion(3, 13));
    private static final SolicitudSeguimiento SOLICITUD = new SolicitudSeguimiento("SEG-1", "12345678Z",
            "00000001R", "TAC", "Indicación ficticia", "Justificación ficticia",
            PrioridadSolicitud.ORDINARIO, new PeriodicidadSeguimiento(1, UnidadPeriodicidad.DIAS),
            LocalDate.of(2026, 12, 11), 3, null, TRES);

    @Nested
    @DisplayName("CS y CK · Each use")
    class EachUse {

        @ParameterizedTest(name = "{0}")
        @DisplayName("CS: solicitar")
        @CsvSource({"CS-PW-01, J1, S1", "CS-PW-02, J1, S2", "CS-PW-03, J1, S3", "CS-PW-04, J1, S4",
                "CS-PW-05, J1, S5", "CS-PW-06, J1, S6", "CS-PW-08, J2, S2"})
        void solicitarEachUse(String id, String datos, String respuesta) {
            comprobar(id, datos, respuesta);
        }

        @Test
        @DisplayName("CK-EU-02: el servicio es obligatorio")
        void servicioNuloEachUse() {
            assertThrows(NullPointerException.class, () -> new ControladorSolicitudSeguimiento(null));
        }
    }

    @Nested
    @DisplayName("CS y CK · Pairwise")
    class Pairwise {

        @ParameterizedTest(name = "{0}")
        @DisplayName("CS: datos null")
        @CsvSource({"CS-PW-07, J2, S1", "CS-PW-09, J2, S3", "CS-PW-10, J2, S4", "CS-PW-11, J2, S5",
                "CS-PW-12, J2, S6"})
        void solicitarPairwise(String id, String datos, String respuesta) {
            comprobar(id, datos, respuesta);
        }
    }

    private static void comprobar(String id, String codigoDatos, String codigoRespuesta) {
        DatosSolicitudSeguimiento datos = codigoDatos.equals("J1") ? J1 : null;
        ServicioFalso servicio = new ServicioFalso(codigoRespuesta);
        ControladorSolicitudSeguimiento controlador = new ControladorSolicitudSeguimiento(servicio);

        if (codigoRespuesta.equals("S6")) {
            assertThrows(NullPointerException.class, () -> controlador.solicitar(datos), id);
        } else {
            assertEquals(switch (codigoRespuesta) {
                case "S1" -> new ResultadoSolicitudSeguimiento(true, "Solicitud de seguimiento de TAC "
                        + "registrada con 3 solicitudes pendientes de cita.", "SEG-1", TRES);
                case "S2" -> error("El campo indicacionClinica es obligatorio.");
                case "S3" -> error("Solo un médico puede solicitar una prueba de seguimiento periódica.");
                case "S4" -> error("El tipo de prueba Radiografía no admite seguimiento periódico.");
                case "S5" -> error("No se ha podido registrar la solicitud de seguimiento. "
                        + "Inténtelo de nuevo más tarde.");
                default -> throw new IllegalArgumentException(codigoRespuesta);
            }, controlador.solicitar(datos), id);
        }
        assertEquals(Arrays.asList(datos), servicio.recibidos, id + ": postcondición");
    }

    private static ResultadoSolicitudSeguimiento error(String mensaje) {
        return new ResultadoSolicitudSeguimiento(false, mensaje, null, List.of());
    }

    private static RepeticionSeguimiento repeticion(int numero, int dia) {
        return new RepeticionSeguimiento("SEG-1-" + numero, "SEG-1", numero,
                LocalDate.of(2026, 12, dia), EstadoRepeticionSeguimiento.PENDIENTE_CITA);
    }

    private static final class ServicioFalso implements ServicioSolicitudSeguimiento {
        final List<DatosSolicitudSeguimiento> recibidos = new ArrayList<>();
        private final String codigo;

        ServicioFalso(String codigo) {
            this.codigo = codigo;
        }

        @Override
        public SolicitudSeguimiento solicitar(DatosSolicitudSeguimiento datos) {
            recibidos.add(datos);
            return switch (codigo) {
                case "S1" -> SOLICITUD;
                case "S2" -> throw new DatosSeguimientoInvalidosException(
                        "El campo indicacionClinica es obligatorio.");
                case "S3" -> throw new UsuarioNoMedicoException(
                        "Solo un médico puede solicitar una prueba de seguimiento periódica.");
                case "S4" -> throw new TipoPruebaSinSeguimientoException(
                        "El tipo de prueba Radiografía no admite seguimiento periódico.");
                case "S5" -> throw new IllegalStateException("detalle interno");
                case "S6" -> throw new NullPointerException("fallo no previsto");
                default -> throw new IllegalArgumentException(codigo);
            };
        }

        @Override
        public Optional<SolicitudSeguimiento> buscarPorId(String id) {
            throw new UnsupportedOperationException();
        }

        @Override
        public List<SolicitudSeguimiento> buscarPorPaciente(String dniPaciente) {
            throw new UnsupportedOperationException();
        }
    }
}
