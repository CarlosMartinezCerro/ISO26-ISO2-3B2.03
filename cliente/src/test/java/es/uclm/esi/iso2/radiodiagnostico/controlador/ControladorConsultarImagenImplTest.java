package es.uclm.esi.iso2.radiodiagnostico.controlador;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import es.uclm.esi.iso2.radiodiagnostico.informe.ClienteConsultaImagenes;
import es.uclm.esi.iso2.radiodiagnostico.informe.ErrorComunicacionException;
import es.uclm.esi.iso2.radiodiagnostico.informe.ImagenConsultada;
import es.uclm.esi.iso2.radiodiagnostico.informe.RespuestaConsultaImagen;
import es.uclm.esi.iso2.radiodiagnostico.informe.RespuestaListadoImagenes;
import es.uclm.esi.iso2.radiodiagnostico.informe.ResumenImagen;
import es.uclm.esi.iso2.radiodiagnostico.vista.VistaConsultarImagen;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ControladorConsultarImagenImplTest {

    private static final String MEDICO = "MED-001";
    private static final LocalDateTime FECHA = LocalDateTime.of(2026, 11, 2, 9, 0);
    private static final List<ResumenImagen> IMAGENES = List.of(
            new ResumenImagen("IMG-001", "INF-001", "TAC", FECHA, "craneo.dcm"));
    private static final ImagenConsultada IMAGEN = new ImagenConsultada("IMG-001", "INF-001",
            "FICTICIO-001", "TAC", FECHA, "craneo.dcm", new byte[] {1},
            "pacs://radiodiagnostico/FICTICIO-001/IMG-001");

    private VistaFalsa vista;
    private ClienteFalso cliente;
    private ControladorConsultarImagen controlador;

    @BeforeEach
    void crearControlador() {
        vista = new VistaFalsa();
        cliente = new ClienteFalso();
        controlador = new ControladorConsultarImagenImpl(vista, cliente, MEDICO);
    }

    @Test
    void buscarMuestraLasImagenesDelPaciente() {
        vista.dni = "FICTICIO-001";
        cliente.respuestaListado = new RespuestaListadoImagenes(true, "1 imagen(es) encontradas.", IMAGENES);

        controlador.buscarImagenes();

        assertEquals(IMAGENES, vista.imagenesMostradas);
        assertEquals("1 imagen(es) encontradas.", vista.mensajeListado);
        assertEquals(MEDICO, cliente.medicoRecibido);
        assertEquals("FICTICIO-001", cliente.dniRecibido);
    }

    @Test
    void buscarSinDniNoLlamaAlServidor() {
        vista.dni = " ";

        controlador.buscarImagenes();

        assertNull(cliente.dniRecibido);
        assertEquals(ControladorConsultarImagenImpl.MENSAJE_SIN_DNI, vista.error);
    }

    @Test
    void buscarConDniNuloNoLlamaAlServidor() {
        vista.dni = null;

        controlador.buscarImagenes();

        assertNull(cliente.dniRecibido);
        assertEquals(ControladorConsultarImagenImpl.MENSAJE_SIN_DNI, vista.error);
    }

    @Test
    void buscarRechazadoMuestraElMensajeDelServidor() {
        vista.dni = "FICTICIO-001";
        cliente.respuestaListado = new RespuestaListadoImagenes(false, "El campo paciente es obligatorio.", List.of());

        controlador.buscarImagenes();

        assertEquals("El campo paciente es obligatorio.", vista.error);
        assertNull(vista.imagenesMostradas);
    }

    @Test
    void buscarSinConexionMuestraError() {
        vista.dni = "FICTICIO-001";
        cliente.sinConexion = true;

        controlador.buscarImagenes();

        assertEquals(ControladorConsultarImagenImpl.MENSAJE_SIN_CONEXION, vista.error);
    }

    @Test
    void verMuestraLaImagenSeleccionada() {
        vista.seleccionada = "IMG-001";
        cliente.respuestaConsulta = new RespuestaConsultaImagen(true, "Imagen recuperada.", IMAGEN);

        controlador.verImagen();

        assertEquals(IMAGEN, vista.imagenMostrada);
        assertEquals("IMG-001", cliente.imagenPedida);
    }

    @Test
    void verSinSeleccionNoLlamaAlServidor() {
        vista.seleccionada = null;

        controlador.verImagen();

        assertNull(cliente.imagenPedida);
        assertEquals(ControladorConsultarImagenImpl.MENSAJE_SIN_SELECCION, vista.error);
    }

    @Test
    void verRechazadoMuestraElMensajeDelServidor() {
        vista.seleccionada = "IMG-002";
        cliente.respuestaConsulta = new RespuestaConsultaImagen(false,
                "La imagen IMG-002 no pertenece a un paciente de este médico.", null);

        controlador.verImagen();

        assertEquals("La imagen IMG-002 no pertenece a un paciente de este médico.", vista.error);
        assertNull(vista.imagenMostrada);
    }

    @Test
    void verSinConexionMuestraError() {
        vista.seleccionada = "IMG-001";
        cliente.sinConexion = true;

        controlador.verImagen();

        assertEquals(ControladorConsultarImagenImpl.MENSAJE_SIN_CONEXION, vista.error);
    }

    private static final class VistaFalsa implements VistaConsultarImagen {
        String dni;
        String seleccionada;
        List<ResumenImagen> imagenesMostradas;
        String mensajeListado;
        ImagenConsultada imagenMostrada;
        String error;

        @Override
        public String obtenerDniPaciente() {
            return dni;
        }

        @Override
        public String obtenerImagenSeleccionada() {
            return seleccionada;
        }

        @Override
        public void mostrarImagenes(List<ResumenImagen> imagenes, String mensaje) {
            imagenesMostradas = imagenes;
            mensajeListado = mensaje;
        }

        @Override
        public void mostrarImagen(ImagenConsultada imagen) {
            imagenMostrada = imagen;
        }

        @Override
        public void mostrarError(String mensaje) {
            error = mensaje;
        }
    }

    private static final class ClienteFalso implements ClienteConsultaImagenes {
        boolean sinConexion;
        RespuestaListadoImagenes respuestaListado;
        RespuestaConsultaImagen respuestaConsulta;
        String medicoRecibido;
        String dniRecibido;
        String imagenPedida;

        @Override
        public RespuestaListadoImagenes obtenerImagenesPaciente(String idMedico, String dniPaciente) {
            comprobarConexion();
            medicoRecibido = idMedico;
            dniRecibido = dniPaciente;
            return respuestaListado;
        }

        @Override
        public RespuestaConsultaImagen obtenerImagen(String idMedico, String idImagen) {
            comprobarConexion();
            imagenPedida = idImagen;
            return respuestaConsulta;
        }

        private void comprobarConexion() {
            if (sinConexion) {
                throw new ErrorComunicacionException("Servidor no disponible",
                        new java.io.IOException("Conexión rechazada"));
            }
        }
    }
}
