package es.uclm.esi.iso2.radiodiagnostico.api;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import es.uclm.esi.iso2.radiodiagnostico.informe.ErrorComunicacionException;
import es.uclm.esi.iso2.radiodiagnostico.informe.ImagenConsultada;
import es.uclm.esi.iso2.radiodiagnostico.informe.RespuestaConsultaImagen;
import es.uclm.esi.iso2.radiodiagnostico.informe.RespuestaListadoImagenes;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;

class ClienteRestImagenesTest {

    @Test
    void pideLasImagenesDelPacienteConLaCabeceraDelMedico() throws Exception {
        try (ServidorFalso servidor = new ServidorFalso()) {
            servidor.respuesta = "{\"correcto\":true,\"mensaje\":\"1 imagen(es) encontradas.\",\"imagenes\":["
                    + "{\"id\":\"IMG-001\",\"idInforme\":\"INF-001\",\"tipoPrueba\":\"TAC\","
                    + "\"fechaCreacion\":\"2026-11-02T09:30\",\"nombreFichero\":\"craneo.dcm\"}]}";

            RespuestaListadoImagenes respuesta = cliente(servidor).obtenerImagenesPaciente("MED-001", "FICTICIO-001");

            assertEquals("GET", servidor.metodo);
            assertEquals("/api/pacientes/FICTICIO-001/imagenes", servidor.ruta);
            assertEquals("MED-001", servidor.medico);
            assertTrue(respuesta.correcto());
            assertEquals("IMG-001", respuesta.imagenes().get(0).id());
            assertEquals(LocalDateTime.of(2026, 11, 2, 9, 30), respuesta.imagenes().get(0).fechaCreacion());
        }
    }

    @Test
    void codificaElDniEnLaRuta() throws Exception {
        try (ServidorFalso servidor = new ServidorFalso()) {
            servidor.respuesta = "{\"correcto\":true,\"mensaje\":\"\",\"imagenes\":[]}";

            cliente(servidor).obtenerImagenesPaciente("MED-001", "12345678 Z/Ñ");

            assertEquals("/api/pacientes/12345678%20Z%2F%C3%91/imagenes", servidor.ruta);
        }
    }

    @Test
    void pideUnaImagenYDecodificaSuContenido() throws Exception {
        try (ServidorFalso servidor = new ServidorFalso()) {
            servidor.respuesta = "{\"correcto\":true,\"mensaje\":\"Imagen recuperada.\",\"imagen\":{"
                    + "\"id\":\"IMG-001\",\"idInforme\":\"INF-001\",\"dniPaciente\":\"FICTICIO-001\","
                    + "\"idMedico\":\"MED-001\",\"tipoPrueba\":\"TAC\",\"nombreFichero\":\"craneo.dcm\","
                    + "\"contenido\":\"AQID\",\"fechaCreacion\":\"2026-11-02T09:30:00\","
                    + "\"referenciaPacs\":\"pacs://radiodiagnostico/FICTICIO-001/IMG-001\"}}";

            RespuestaConsultaImagen respuesta = cliente(servidor).obtenerImagen("MED-001", "IMG-001");

            assertEquals("/api/imagenes/IMG-001", servidor.ruta);
            ImagenConsultada imagen = respuesta.imagen();
            assertEquals("FICTICIO-001", imagen.dniPaciente());
            assertArrayEquals(new byte[] {1, 2, 3}, imagen.contenido());
            assertEquals("pacs://radiodiagnostico/FICTICIO-001/IMG-001", imagen.referenciaPacs());
        }
    }

    @Test
    void unRechazoDelServidorLlegaConSuMensaje() throws Exception {
        try (ServidorFalso servidor = new ServidorFalso()) {
            servidor.respuesta = "{\"correcto\":false,\"mensaje\":\"No existe la imagen IMG-9.\",\"imagen\":null}";

            RespuestaConsultaImagen respuesta = cliente(servidor).obtenerImagen("MED-001", "IMG-9");

            assertEquals("No existe la imagen IMG-9.", respuesta.mensaje());
            assertNull(respuesta.imagen());
        }
    }

    @Test
    void sinMedicoNoSeEnviaLaCabecera() throws Exception {
        try (ServidorFalso servidor = new ServidorFalso()) {
            servidor.respuesta = "{\"correcto\":false,\"mensaje\":\"El campo médico es obligatorio.\",\"imagenes\":[]}";

            cliente(servidor).obtenerImagenesPaciente(null, "FICTICIO-001");

            assertNull(servidor.medico);
        }
    }

    @Test
    void unErrorHttpEsUnErrorDeComunicacion() throws Exception {
        try (ServidorFalso servidor = new ServidorFalso()) {
            servidor.codigo = 404;

            assertThrows(ErrorComunicacionException.class,
                    () -> cliente(servidor).obtenerImagen("MED-001", "IMG-001"));
        }
    }

    @Test
    void sinServidorEsUnErrorDeComunicacion() throws Exception {
        String url;
        try (ServidorFalso servidor = new ServidorFalso()) {
            url = servidor.url();
        }
        ClienteRestImagenes cliente = new ClienteRestImagenes(new ClienteHttp(url));

        assertThrows(ErrorComunicacionException.class,
                () -> cliente.obtenerImagenesPaciente("MED-001", "FICTICIO-001"));
    }

    private static ClienteRestImagenes cliente(ServidorFalso servidor) {
        return new ClienteRestImagenes(new ClienteHttp(servidor.url()));
    }
}
