package es.uclm.esi.iso2.radiodiagnostico.controlador;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import es.uclm.esi.iso2.radiodiagnostico.informe.ClienteInformes;
import es.uclm.esi.iso2.radiodiagnostico.informe.ErrorComunicacionException;
import es.uclm.esi.iso2.radiodiagnostico.informe.DatosCreacionInforme;
import es.uclm.esi.iso2.radiodiagnostico.informe.InformeRespuesta;
import es.uclm.esi.iso2.radiodiagnostico.informe.InformeResumen;
import es.uclm.esi.iso2.radiodiagnostico.informe.ResultadoAntecedentes;
import es.uclm.esi.iso2.radiodiagnostico.informe.ResultadoCreacionInforme;
import es.uclm.esi.iso2.radiodiagnostico.vista.VistaConsultarInforme;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ControladorConsultarInformeImplTest {

    private VistaFalsa vista;
    private ClienteFalso cliente;
    private ControladorConsultarInformeImpl controlador;

    @BeforeEach
    void preparar() {
        vista = new VistaFalsa();
        cliente = new ClienteFalso();
        controlador = new ControladorConsultarInformeImpl(vista, cliente);
    }

    @Test
    void constructorRechazaVistaNula() {
        assertThrows(NullPointerException.class,
                () -> new ControladorConsultarInformeImpl(null, cliente));
    }

    @Test
    void constructorRechazaClienteNulo() {
        assertThrows(NullPointerException.class,
                () -> new ControladorConsultarInformeImpl(vista, null));
    }

    @Test
    void dniNuloVacioOVacioEnBlancoNoLlamaAlCliente() {
        for (String dni : new String[] {null, "", "   "}) {
            vista.dni = dni;

            controlador.listarInformes();

            assertEquals("El DNI del paciente es obligatorio.", vista.error);
            assertEquals(0, cliente.listados);
        }
    }

    @Test
    void listaVaciaSeMuestraConMensaje() {
        controlador.listarInformes();

        assertEquals(List.of(), vista.informes);
        assertEquals("No hay informes para este paciente.", vista.mensaje);
        assertEquals(1, cliente.listados);
    }

    @Test
    void listaConInformesSeMuestraSinMensajeVacio() {
        InformeResumen resumen = new InformeResumen(
                1, "Radiología", LocalDate.of(2026, 11, 19));
        cliente.informes = List.of(resumen);

        controlador.listarInformes();

        assertEquals(List.of(resumen), vista.informes);
        assertNull(vista.mensaje);
    }

    @Test
    void dniSeRecortaAntesDeLlamarAlCliente() {
        vista.dni = " 87654321B ";

        controlador.listarInformes();

        assertEquals("87654321B", cliente.ultimoDni);
    }

    @Test
    void erroresAlListarSeMuestranEnLaVista() {
        for (RuntimeException error : new RuntimeException[] {
                new ErrorComunicacionException("Sin conexión", null),
                new IllegalArgumentException("DNI no válido")
        }) {
            cliente.error = error;
            controlador.listarInformes();
            assertEquals(error.getMessage(), vista.error);
            vista.error = null;
        }
    }

    @Test
    void ausenciaDeSeleccionNoLlamaAlCliente() {
        controlador.obtenerInforme();

        assertEquals("Seleccione un informe.", vista.error);
        assertEquals(0, cliente.detalles);
    }

    @Test
    void informeSeleccionadoMuestraElDetalle() {
        InformeResumen resumen = new InformeResumen(
                Long.MAX_VALUE, "Radiología", LocalDate.now());
        InformeRespuesta informe = informeValido();
        vista.seleccionado = resumen;
        cliente.detalle = informe;

        controlador.obtenerInforme();

        assertEquals(informe, vista.informe);
        assertEquals(Long.MAX_VALUE, cliente.ultimoId);
    }

    @Test
    void detalleNoDisponibleSeMuestraComoError() {
        vista.seleccionado = new InformeResumen(1, "Radiología", null);

        controlador.obtenerInforme();

        assertEquals("El informe seleccionado ya no está disponible.", vista.error);
    }

    @Test
    void erroresAlObtenerDetalleSeMuestranEnLaVista() {
        vista.seleccionado = new InformeResumen(1, "Radiología", null);
        for (RuntimeException error : new RuntimeException[] {
                new ErrorComunicacionException("Sin conexión", null),
                new IllegalArgumentException("ID no válido")
        }) {
            cliente.error = error;
            controlador.obtenerInforme();
            assertEquals(error.getMessage(), vista.error);
            vista.error = null;
        }
    }

    private static InformeRespuesta informeValido() {
        return new InformeRespuesta(1, "12345678A", "87654321B",
                "Radiología", "Motivo", "Antecedentes", "Historial",
                "Consulta", "Diagnóstico", "Pruebas", "Tratamiento",
                true, LocalDate.of(2026, 11, 19), "Anamnesis");
    }

    private static final class VistaFalsa implements VistaConsultarInforme {
        private String dni = "87654321B";
        private InformeResumen seleccionado;
        private List<InformeResumen> informes = List.of();
        private InformeRespuesta informe;
        private String mensaje;
        private String error;

        @Override
        public String obtenerDniPaciente() {
            return dni;
        }

        @Override
        public InformeResumen obtenerInformeSeleccionado() {
            return seleccionado;
        }

        @Override
        public void mostrarInformes(List<InformeResumen> informes) {
            this.informes = informes;
        }

        @Override
        public void mostrarInforme(InformeRespuesta informe) {
            this.informe = informe;
        }

        @Override
        public void mostrarMensaje(String mensaje) {
            this.mensaje = mensaje;
        }

        @Override
        public void mostrarError(String mensaje) {
            error = mensaje;
        }
    }

    private static final class ClienteFalso implements ClienteInformes {
        private List<InformeResumen> informes = List.of();
        private InformeRespuesta detalle;
        private RuntimeException error;
        private int listados;
        private int detalles;
        private String ultimoDni;
        private long ultimoId;

        @Override
        public List<InformeResumen> listarInformes(String dniPaciente) {
            listados++;
            ultimoDni = dniPaciente;
            if (error != null) {
                throw error;
            }
            return informes;
        }

        @Override
        public InformeRespuesta obtenerInforme(long idInforme) {
            detalles++;
            ultimoId = idInforme;
            if (error != null) {
                throw error;
            }
            return detalle;
        }

        @Override
        public ResultadoCreacionInforme crearInforme(
                DatosCreacionInforme datos) {
            throw new UnsupportedOperationException();
        }

        @Override
        public ResultadoAntecedentes obtenerAntecedentes(
                String dniPaciente, String especialidad) {
            throw new UnsupportedOperationException();
        }
    }
}
