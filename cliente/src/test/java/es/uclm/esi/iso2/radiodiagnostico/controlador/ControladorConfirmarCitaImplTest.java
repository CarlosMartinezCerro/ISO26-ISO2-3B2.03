package es.uclm.esi.iso2.radiodiagnostico.controlador;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import es.uclm.esi.iso2.radiodiagnostico.modelo.cita.ClienteConfirmacionCitas;
import es.uclm.esi.iso2.radiodiagnostico.modelo.cita.DatosConfirmacionCita;
import es.uclm.esi.iso2.radiodiagnostico.modelo.cita.ErrorComunicacionException;
import es.uclm.esi.iso2.radiodiagnostico.modelo.cita.ResultadoConfirmacion;
import es.uclm.esi.iso2.radiodiagnostico.vista.VistaConfirmarCita;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ControladorConfirmarCitaImplTest {

    private static final DatosConfirmacionCita DATOS =
            new DatosConfirmacionCita("CITA-SOL-001", "FICTICIO-001");

    private VistaFalsa vista;
    private ClienteFalso cliente;
    private ControladorConfirmarCita controlador;

    @BeforeEach
    void crearControlador() {
        vista = new VistaFalsa();
        cliente = new ClienteFalso();
        controlador = new ControladorConfirmarCitaImpl(vista, cliente);
    }

    @Test
    void confirmacionCorrectaMuestraExito() {
        vista.datos = DATOS;
        cliente.resultado = new ResultadoConfirmacion(true, "Cita confirmada.");

        controlador.confirmarCita();

        assertEquals(DATOS, cliente.datosRecibidos);
        assertEquals("Cita confirmada.", vista.exito);
        assertNull(vista.error);
    }

    @Test
    void confirmacionRechazadaMuestraElMensajeDelServidor() {
        vista.datos = DATOS;
        cliente.resultado = new ResultadoConfirmacion(false, "La cita ya está CONFIRMADA.");

        controlador.confirmarCita();

        assertEquals("La cita ya está CONFIRMADA.", vista.error);
        assertNull(vista.exito);
    }

    @Test
    void sinDatosNoLlamaAlServidor() {
        vista.datos = null;

        controlador.confirmarCita();

        assertNull(cliente.datosRecibidos);
        assertEquals(ControladorConfirmarCitaImpl.MENSAJE_DATOS_INCOMPLETOS, vista.error);
    }

    @Test
    void sinConexionMuestraError() {
        vista.datos = DATOS;
        cliente.sinConexion = true;

        controlador.confirmarCita();

        assertEquals(ControladorConfirmarCitaImpl.MENSAJE_SIN_CONEXION, vista.error);
        assertNull(vista.exito);
    }

    private static final class VistaFalsa implements VistaConfirmarCita {
        DatosConfirmacionCita datos;
        String exito;
        String error;

        @Override
        public DatosConfirmacionCita obtenerDatos() {
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
    }

    private static final class ClienteFalso implements ClienteConfirmacionCitas {
        boolean sinConexion;
        ResultadoConfirmacion resultado;
        DatosConfirmacionCita datosRecibidos;

        @Override
        public ResultadoConfirmacion confirmarCita(DatosConfirmacionCita datos) {
            if (sinConexion) {
                throw new ErrorComunicacionException("Servidor no disponible",
                        new java.io.IOException("Conexión rechazada"));
            }
            datosRecibidos = datos;
            return resultado;
        }
    }
}
