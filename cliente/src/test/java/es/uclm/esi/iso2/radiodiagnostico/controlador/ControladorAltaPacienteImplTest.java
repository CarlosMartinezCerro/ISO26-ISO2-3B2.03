package es.uclm.esi.iso2.radiodiagnostico.controlador;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import es.uclm.esi.iso2.radiodiagnostico.paciente.ClientePacientes;
import es.uclm.esi.iso2.radiodiagnostico.paciente.DatosAltaPaciente;
import es.uclm.esi.iso2.radiodiagnostico.paciente.ErrorComunicacionException;
import es.uclm.esi.iso2.radiodiagnostico.paciente.ResultadoAlta;
import es.uclm.esi.iso2.radiodiagnostico.vista.VistaAltaPaciente;
import java.io.IOException;
import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ControladorAltaPacienteImplTest {

    private static final DatosAltaPaciente DATOS = new DatosAltaPaciente("12345678Z", "Alex",
            "Ejemplo", LocalDate.of(1990, 5, 12), "600000001");

    private VistaFalsa vista;
    private ClienteFalso cliente;
    private ControladorAltaPaciente controlador;

    @BeforeEach
    void crearControlador() {
        vista = new VistaFalsa();
        cliente = new ClienteFalso();
        controlador = new ControladorAltaPacienteImpl(vista, cliente);
    }

    @Test
    void altaCorrectaMuestraExitoYLimpiaElFormulario() {
        vista.datos = DATOS;
        cliente.resultado = new ResultadoAlta(true, "Paciente dado de alta.");

        controlador.darDeAlta();

        assertEquals(DATOS, cliente.datosRecibidos);
        assertEquals("Paciente dado de alta.", vista.exito);
        assertNull(vista.error);
        assertTrue(vista.limpiada);
    }

    @Test
    void altaRechazadaMuestraElMensajeDelServidorYConservaLosDatos() {
        vista.datos = DATOS;
        cliente.resultado = new ResultadoAlta(false, "Ya existe un paciente con el DNI 12345678Z.");

        controlador.darDeAlta();

        assertEquals("Ya existe un paciente con el DNI 12345678Z.", vista.error);
        assertNull(vista.exito);
        assertFalse(vista.limpiada);
    }

    @Test
    void fechaMalEscritaNoLlamaAlServidor() {
        vista.datos = null;

        controlador.darDeAlta();

        assertNull(cliente.datosRecibidos);
        assertEquals(ControladorAltaPacienteImpl.MENSAJE_FECHA_INCORRECTA, vista.error);
    }

    @Test
    void sinConexionMuestraError() {
        vista.datos = DATOS;
        cliente.sinConexion = true;

        controlador.darDeAlta();

        assertEquals(ControladorAltaPacienteImpl.MENSAJE_SIN_CONEXION, vista.error);
        assertNull(vista.exito);
        assertFalse(vista.limpiada);
    }

    private static final class VistaFalsa implements VistaAltaPaciente {
        DatosAltaPaciente datos;
        String exito;
        String error;
        boolean limpiada;

        @Override
        public DatosAltaPaciente obtenerDatos() {
            return datos;
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

    private static final class ClienteFalso implements ClientePacientes {
        boolean sinConexion;
        ResultadoAlta resultado;
        DatosAltaPaciente datosRecibidos;

        @Override
        public ResultadoAlta darDeAlta(DatosAltaPaciente datos) {
            if (sinConexion) {
                throw new ErrorComunicacionException("Servidor no disponible",
                        new IOException("Conexión rechazada"));
            }
            datosRecibidos = datos;
            return resultado;
        }
    }
}
