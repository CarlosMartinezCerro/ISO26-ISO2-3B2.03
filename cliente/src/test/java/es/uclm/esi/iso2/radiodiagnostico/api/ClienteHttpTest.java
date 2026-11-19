package es.uclm.esi.iso2.radiodiagnostico.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import org.junit.jupiter.api.Test;

class ClienteHttpTest {

    @Test
    void quitaLaBarraFinalDeLaUrl() throws Exception {
        try (ServidorFalso servidor = new ServidorFalso()) {
            new ClienteHttp(servidor.url() + "///").get("/api/x", null);

            assertEquals("/api/x", servidor.ruta);
        }
    }

    @Test
    void siSeInterrumpeElHiloEsUnErrorYSeMantieneLaInterrupcion() throws Exception {
        try (ServidorFalso servidor = new ServidorFalso()) {
            ClienteHttp http = new ClienteHttp(servidor.url());
            Thread.currentThread().interrupt();

            assertThrows(IOException.class, () -> http.get("/api/x", null));
            assertTrue(Thread.interrupted(), "Se debe conservar la marca de interrupción");
        }
    }

    @Test
    void codificaLosSegmentosDeLaRuta() {
        assertEquals("a%20b%2Fc", ClienteHttp.segmento("a b/c"));
    }
}
