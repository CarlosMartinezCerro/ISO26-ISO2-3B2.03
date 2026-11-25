package es.uclm.esi.iso2.radiodiagnostico.controlador;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import es.uclm.esi.iso2.radiodiagnostico.solicitud.ClienteSolicitudes;
import es.uclm.esi.iso2.radiodiagnostico.solicitud.DatosSolicitudPrueba;
import es.uclm.esi.iso2.radiodiagnostico.solicitud.ErrorComunicacionException;
import es.uclm.esi.iso2.radiodiagnostico.solicitud.ResultadoSolicitud;
import es.uclm.esi.iso2.radiodiagnostico.vista.VistaSolicitarPrueba;
import java.util.List;
import java.util.ArrayList;
import org.junit.jupiter.api.Test;

class ControladorSolicitarPruebaImplTest {

    @Test
    void muestraConfirmacionConIdentificadorYEstadoTrasElRegistro() {
        VistaFalsa vista = new VistaFalsa();
        ClienteFalso cliente = new ClienteFalso();
        var controlador = controlador(vista, cliente);
        vista.tipo = "TAC";
        vista.prioridad = "URGENTE";

        controlador.solicitar();

        assertEquals(1, cliente.envios);
        assertTrue(vista.exito.contains("SOL-1"));
        assertTrue(vista.exito.contains("PENDIENTE"));
        assertNull(vista.error);
        assertEquals(false, vista.enviando);
    }

    @Test
    void validaLosObligatoriosAntesDeLlamarAlServidor() {
        VistaFalsa vista = new VistaFalsa();
        ClienteFalso cliente = new ClienteFalso();

        controlador(vista, cliente).solicitar();

        assertEquals(0, cliente.envios);
        assertNotNull(vista.error);
        assertNull(vista.exito);
    }

    @Test
    void informaElErrorDeComunicacionSinConfirmar() {
        VistaFalsa vista = new VistaFalsa();
        ClienteFalso cliente = new ClienteFalso();
        cliente.fallar = true;
        vista.tipo = "TAC";
        vista.prioridad = "URGENTE";

        controlador(vista, cliente).solicitar();

        assertNull(vista.exito);
        assertEquals("sin conexión", vista.error);
        assertEquals(false, vista.enviando);
    }

    @Test
    void evitaEnviosDuplicadosMientrasLaPeticionEstaPendiente() {
        VistaFalsa vista = new VistaFalsa();
        ClienteFalso cliente = new ClienteFalso();
        List<Runnable> pendientes = new ArrayList<>();
        var controlador = new ControladorSolicitarPruebaImpl(
                vista, cliente, pendientes::add, Runnable::run);
        vista.tipo = "TAC";
        vista.prioridad = "URGENTE";

        controlador.solicitar();
        controlador.solicitar();

        assertEquals(1, pendientes.size());
        assertEquals(0, cliente.envios);
        pendientes.get(0).run();
        assertEquals(1, cliente.envios);
    }

    private static ControladorSolicitarPruebaImpl controlador(VistaFalsa vista, ClienteFalso cliente) {
        return new ControladorSolicitarPruebaImpl(vista, cliente, Runnable::run, Runnable::run);
    }

    private static class VistaFalsa implements VistaSolicitarPrueba {
        String tipo;
        String prioridad;
        String exito;
        String error;
        boolean enviando;
        @Override public void mostrarPruebas(List<String> pruebas) { }
        @Override public String obtenerTipoPrueba() { return tipo; }
        @Override public String obtenerPrioridad() { return prioridad; }
        @Override public void establecerEnviando(boolean enviando) { this.enviando = enviando; }
        @Override public void mostrarExito(String mensaje) { exito = mensaje; }
        @Override public void mostrarError(String mensaje) { error = mensaje; }
    }

    private static class ClienteFalso implements ClienteSolicitudes {
        int envios;
        boolean fallar;
        @Override public List<String> obtenerPruebasDisponibles() { return List.of("TAC"); }
        @Override public ResultadoSolicitud solicitar(DatosSolicitudPrueba datos) {
            envios++;
            if (fallar) throw new ErrorComunicacionException("sin conexión", null);
            return new ResultadoSolicitud(true, "Registrada", "SOL-1", "PENDIENTE");
        }
    }
}
