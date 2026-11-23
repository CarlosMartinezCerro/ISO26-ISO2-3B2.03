package es.uclm.esi.iso2.radiodiagnostico.controlador;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import es.uclm.esi.iso2.radiodiagnostico.modelo.informe.ClienteImagenes;
import es.uclm.esi.iso2.radiodiagnostico.modelo.informe.DatosImagen;
import es.uclm.esi.iso2.radiodiagnostico.modelo.informe.ErrorComunicacionException;
import es.uclm.esi.iso2.radiodiagnostico.modelo.informe.ResultadoCreacionImagen;
import es.uclm.esi.iso2.radiodiagnostico.vista.VistaCrearImagen;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ControladorCrearImagenImplTest {

    private static final DatosImagen DATOS = new DatosImagen("INF-001", "FICTICIO-001",
            "MED-001", "Radiografía", "torax.dcm", new byte[] {1, 2, 3});

    private VistaFalsa vista;
    private ClienteFalso cliente;
    private ControladorCrearImagen controlador;

    @BeforeEach
    void crearControlador() {
        vista = new VistaFalsa();
        cliente = new ClienteFalso();
        controlador = new ControladorCrearImagenImpl(vista, cliente);
    }

    @Test
    void creacionCorrectaMuestraExitoYLimpiaElFormulario() {
        vista.datos = DATOS;
        cliente.resultado = new ResultadoCreacionImagen(true, "Imagen IMG-001 creada.");

        controlador.crearImagen();

        assertEquals(DATOS, cliente.datosRecibidos);
        assertEquals("Imagen IMG-001 creada.", vista.exito);
        assertNull(vista.error);
        assertTrue(vista.limpiada);
    }

    @Test
    void creacionRechazadaMuestraElMensajeDelServidor() {
        vista.datos = DATOS;
        cliente.resultado = new ResultadoCreacionImagen(false, "Formato no permitido.");

        controlador.crearImagen();

        assertEquals("Formato no permitido.", vista.error);
        assertNull(vista.exito);
        assertFalse(vista.limpiada);
    }

    @Test
    void sinDatosNoLlamaAlServidor() {
        vista.datos = null;

        controlador.crearImagen();

        assertNull(cliente.datosRecibidos);
        assertEquals(ControladorCrearImagenImpl.MENSAJE_DATOS_INCOMPLETOS, vista.error);
    }

    @Test
    void sinConexionMuestraError() {
        vista.datos = DATOS;
        cliente.sinConexion = true;

        controlador.crearImagen();

        assertEquals(ControladorCrearImagenImpl.MENSAJE_SIN_CONEXION, vista.error);
        assertNull(vista.exito);
    }

    private static final class VistaFalsa implements VistaCrearImagen {
        DatosImagen datos;
        String exito;
        String error;
        boolean limpiada;

        @Override
        public DatosImagen obtenerDatos() {
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

    private static final class ClienteFalso implements ClienteImagenes {
        boolean sinConexion;
        ResultadoCreacionImagen resultado;
        DatosImagen datosRecibidos;

        @Override
        public ResultadoCreacionImagen crearImagen(DatosImagen datos) {
            if (sinConexion) {
                throw new ErrorComunicacionException("Servidor no disponible",
                        new java.io.IOException("Conexión rechazada"));
            }
            datosRecibidos = datos;
            return resultado;
        }
    }
}
