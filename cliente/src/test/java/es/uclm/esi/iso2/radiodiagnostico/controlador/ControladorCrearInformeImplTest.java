package es.uclm.esi.iso2.radiodiagnostico.controlador;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import es.uclm.esi.iso2.radiodiagnostico.informe.ClienteInformes;
import es.uclm.esi.iso2.radiodiagnostico.informe.DatosCreacionInforme;
import es.uclm.esi.iso2.radiodiagnostico.informe.ErrorComunicacionException;
import es.uclm.esi.iso2.radiodiagnostico.informe.Informe;
import es.uclm.esi.iso2.radiodiagnostico.informe.InformeResumen;
import es.uclm.esi.iso2.radiodiagnostico.informe.InformeRespuesta;
import es.uclm.esi.iso2.radiodiagnostico.informe.ResultadoAntecedentes;
import es.uclm.esi.iso2.radiodiagnostico.informe.ResultadoCreacionInforme;
import es.uclm.esi.iso2.radiodiagnostico.vista.VistaCrearInforme;
import es.uclm.esi.iso2.radiodiagnostico.usuario.Medico;
import es.uclm.esi.iso2.radiodiagnostico.usuario.Paciente;

class ControladorCrearInformeImplTest {

    private VistaFalsa vista;
    private ClienteFalso cliente;
    private ControladorCrearInformeImpl controlador;

    @BeforeEach
    void preparar() {
        vista = new VistaFalsa();
        cliente = new ClienteFalso();
        controlador = new ControladorCrearInformeImpl(vista, cliente);
    }

    @Test
    @DisplayName("Vista nula en el constructor lanza excepción")
    void constructorVistaNula() {
        assertThrows(NullPointerException.class,
                () -> new ControladorCrearInformeImpl(null, cliente));
    }

    @Test
    @DisplayName("Cliente nulo en el constructor lanza excepción")
    void constructorClienteNulo() {
        assertThrows(NullPointerException.class,
                () -> new ControladorCrearInformeImpl(vista, null));
    }

    @Test
    @DisplayName("Constructor con vista y cliente válidos crea el controlador")
    void constructorValido() {
        ControladorCrearInformeImpl creado =
                new ControladorCrearInformeImpl(vista, cliente);
        assertEquals(controlador.getClass(), creado.getClass());
    }

    @Test
    @DisplayName("Informe nulo muestra error y no llama al cliente")
    void crearInformeNulo() {
        vista.informe = null;
        controlador.crearInforme();
        assertTrue(vista.error.contains("datos obligatorios"));
        assertEquals(0, cliente.creaciones);
    }

    @Test
    @DisplayName("Cliente devuelve éxito y la vista se limpia")
    void crearInformeExito() {
        vista.informe = informeValido(null, null);
        cliente.resultadoCreacion = new ResultadoCreacionInforme(true, "Creado", null);
        controlador.crearInforme();
        assertEquals("Creado", vista.exito);
        assertTrue(vista.limpiada);
    }

    @Test
    @DisplayName("Cliente devuelve rechazo y la vista no se limpia")
    void crearInformeRechazo() {
        vista.informe = informeValido(null, null);
        cliente.resultadoCreacion =
                new ResultadoCreacionInforme(false, "Datos inválidos", null);
        controlador.crearInforme();
        assertEquals("Datos inválidos", vista.error);
        assertFalse(vista.limpiada);
    }

    @Test
    @DisplayName("Error de comunicación muestra mensaje de servidor")
    void crearInformeErrorComunicacion() {
        vista.informe = informeValido(null, null);
        cliente.errorCreacion = new ErrorComunicacionException("Sin conexión", null);
        controlador.crearInforme();
        assertEquals("No se pudo conectar con el servidor.", vista.error);
    }

    @Test
    @DisplayName("Informe sin médico conserva DNI de paciente")
    void crearInformeSinMedico() {
        vista.informe = informeValido(null,
                new Paciente("Luis", "Pérez", "87654321B", "", "", null, ""));
        cliente.resultadoCreacion = new ResultadoCreacionInforme(true, "Creado", null);
        controlador.crearInforme();
        assertNull(cliente.datosCreacion.dniMedico());
        assertEquals("87654321B", cliente.datosCreacion.dniPaciente());
    }

    @Test
    @DisplayName("Informe sin paciente conserva DNI de médico")
    void crearInformeSinPaciente() {
        vista.informe = informeValido(
                new Medico("Ana", "Ruiz", "12345678A", "", "", null, "", "Radiología"),
                null);
        cliente.resultadoCreacion = new ResultadoCreacionInforme(true, "Creado", null);
        controlador.crearInforme();
        assertEquals("12345678A", cliente.datosCreacion.dniMedico());
        assertNull(cliente.datosCreacion.dniPaciente());
    }

    @Test
    @DisplayName("Informe sin médico ni paciente envía ambos DNI nulos")
    void crearInformeSinUsuarios() {
        vista.informe = informeValido(null, null);
        cliente.resultadoCreacion = new ResultadoCreacionInforme(true, "Creado", null);
        controlador.crearInforme();
        assertNull(cliente.datosCreacion.dniMedico());
        assertNull(cliente.datosCreacion.dniPaciente());
    }

    @Test
    @DisplayName("Campos opcionales vacíos se envían como cadenas vacías")
    void crearInformeCamposOpcionalesVacios() {
        vista.informe = informeValido(null, null);
        cliente.resultadoCreacion = new ResultadoCreacionInforme(true, "Creado", null);
        controlador.crearInforme();
        assertEquals("", cliente.datosCreacion.motivoConsulta());
        assertEquals("", cliente.datosCreacion.pruebas());
        assertEquals("", cliente.datosCreacion.tratamiento());
    }

    @Test
    @DisplayName("Campos opcionales normales se envían al cliente")
    void crearInformeCamposOpcionalesNormales() {
        Informe base = informeValido(null, null);
        Informe informe = new Informe(base.id(), base.dniMedico(), base.dniPaciente(),
                base.especialidad(), "Dolor", base.antecedentes(),
                base.historialEspecialidad(), base.consultaActual(),
                base.diagnostico(), "Resonancia", base.tratamiento(),
                base.alta(), base.fecha(), base.anamnesis());
        vista.informe = informe;
        cliente.resultadoCreacion = new ResultadoCreacionInforme(true, "Creado", null);
        controlador.crearInforme();
        assertEquals("Dolor", cliente.datosCreacion.motivoConsulta());
        assertEquals("Resonancia", cliente.datosCreacion.pruebas());
    }

    @Test
    @DisplayName("Mensaje vacío con éxito no impide limpiar la vista")
    void crearInformeMensajeVacio() {
        vista.informe = informeValido(null, null);
        cliente.resultadoCreacion = new ResultadoCreacionInforme(true, "", null);
        controlador.crearInforme();
        assertEquals("", vista.exito);
        assertTrue(vista.limpiada);
    }

    @Test
    @DisplayName("Resultado nulo se maneja como error")
    void crearInformeResultadoNulo() {
        vista.informe = informeValido(null, null);
        cliente.resultadoCreacion = null;
        assertThrows(NullPointerException.class, controlador::crearInforme);
    }

    @Test
    @DisplayName("Consulta válida con resultado exitoso muestra antecedentes")
    void consultarAntecedentesExito() {
        cliente.resultadoAntecedentes =
                new ResultadoAntecedentes(true, "OK", "Antecedentes", "Historial");
        controlador.consultarAntecedentes("87654321B", "Radiología");
        assertEquals("Antecedentes", vista.antecedentes);
        assertEquals("Historial", vista.historialEspecialidad);
        assertTrue(vista.camposHabilitados);
    }

    @Test
    @DisplayName("Consulta con rechazo muestra el mensaje")
    void consultarAntecedentesRechazo() {
        cliente.resultadoAntecedentes =
                new ResultadoAntecedentes(false, "Datos inválidos", null, null);
        controlador.consultarAntecedentes("87654321B", "Radiología");
        assertEquals("Datos inválidos", vista.error);
    }

    @Test
    @DisplayName("DNI nulo muestra el error recibido")
    void consultarAntecedentesDniNulo() {
        cliente.errorAntecedentes =
                new IllegalArgumentException("dniPaciente y especialidad son obligatorios.");
        controlador.consultarAntecedentes(null, "Radiología");
        assertTrue(vista.error.contains("obligatorios"));
    }

    @Test
    @DisplayName("Especialidad vacía muestra el error recibido")
    void consultarAntecedentesEspecialidadVacia() {
        cliente.errorAntecedentes =
                new IllegalArgumentException("dniPaciente y especialidad son obligatorios.");
        controlador.consultarAntecedentes("87654321B", "");
        assertTrue(vista.error.contains("obligatorios"));
    }

    @Test
    @DisplayName("DNI y especialidad vacíos muestran un único error")
    void consultarAntecedentesAmbosVacios() {
        cliente.errorAntecedentes =
                new IllegalArgumentException("dniPaciente y especialidad son obligatorios.");
        controlador.consultarAntecedentes("", "");
        assertEquals("DNI del paciente y especialidad son obligatorios.", vista.error);
    }

    @Test
    @DisplayName("Error de comunicación al consultar antecedentes se muestra")
    void consultarAntecedentesErrorComunicacion() {
        cliente.errorAntecedentes =
                new ErrorComunicacionException("Sin conexión", null);
        controlador.consultarAntecedentes("87654321B", "Radiología");
        assertEquals("Sin conexión", vista.error);
    }

    @Test
    @DisplayName("Resultado nulo al consultar antecedentes se maneja")
    void consultarAntecedentesResultadoNulo() {
        cliente.resultadoAntecedentes = null;
        assertThrows(NullPointerException.class,
                () -> controlador.consultarAntecedentes("87654321B", "Radiología"));
    }

    private Informe informeValido(Medico medico, Paciente paciente) {
        String dniMedico = medico == null ? null : medico.dni();
        String dniPaciente = paciente == null ? null : paciente.dni();
        return new Informe(0L, dniMedico, dniPaciente, "Radiología", "",
                "", "", "Consulta", "Diagnóstico", "", "", false,
                LocalDate.of(2026, 11, 19), "");
    }

    private static class VistaFalsa implements VistaCrearInforme {
        private Informe informe;
        private String exito;
        private String error;
        private boolean limpiada;
        private boolean camposHabilitados;
        private String dniPaciente = "";
        private String especialidad = "";
        private String antecedentes;
        private String historialEspecialidad;

        @Override
        public Informe obtenerInforme() {
            return informe;
        }

        @Override
        public String obtenerDniPaciente() {
            return dniPaciente;
        }

        @Override
        public String obtenerEspecialidad() {
            return especialidad;
        }

        @Override
        public void cargarAntecedentes(String antecedentes, String historialEspecialidad) {
            this.antecedentes = antecedentes;
            this.historialEspecialidad = historialEspecialidad;
        }

        @Override
        public void habilitarCamposInforme(boolean habilitado) {
            camposHabilitados = habilitado;
        }

        @Override
        public void mostrarExito(String mensaje) {
            exito = mensaje;
        }

        @Override
        public void mostrarError(String mensaje) {
            error = mensaje;
        }

        @Override
        public void limpiar() {
            limpiada = true;
        }
    }

    private static class ClienteFalso implements ClienteInformes {
        private int creaciones;
        private DatosCreacionInforme datosCreacion;
        private ResultadoCreacionInforme resultadoCreacion =
                new ResultadoCreacionInforme(false, "Error", null);
        private RuntimeException errorCreacion;
        private ResultadoAntecedentes resultadoAntecedentes =
                new ResultadoAntecedentes(false, "Error", null, null);
        private RuntimeException errorAntecedentes;
        private final List<DatosCreacionInforme> creacionesDatos = new ArrayList<>();

        @Override
        public ResultadoCreacionInforme crearInforme(DatosCreacionInforme datos) {
            creaciones++;
            datosCreacion = datos;
            creacionesDatos.add(datos);
            if (errorCreacion != null) {
                throw errorCreacion;
            }
            return resultadoCreacion;
        }

        @Override
        public ResultadoAntecedentes obtenerAntecedentes(String dniPaciente,
                                                         String especialidad) {
            if (errorAntecedentes != null) {
                throw errorAntecedentes;
            }
            return resultadoAntecedentes;
        }

        @Override
        public java.util.List<InformeResumen> listarInformes(String dniPaciente) {
            return java.util.List.of();
        }

        @Override
        public InformeRespuesta obtenerInforme(long idInforme) {
            return null;
        }
    }
}
