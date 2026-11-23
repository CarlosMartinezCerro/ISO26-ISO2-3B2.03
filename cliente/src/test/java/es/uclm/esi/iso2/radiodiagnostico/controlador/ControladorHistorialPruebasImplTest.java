package es.uclm.esi.iso2.radiodiagnostico.controlador;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import es.uclm.esi.iso2.radiodiagnostico.modelo.informe.ErrorComunicacionException;
import es.uclm.esi.iso2.radiodiagnostico.modelo.informe.PruebaHistorica;
import es.uclm.esi.iso2.radiodiagnostico.modelo.informe.ResultadoHistorialPruebas;
import es.uclm.esi.iso2.radiodiagnostico.vista.VistaHistorialPruebas;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;

class ControladorHistorialPruebasImplTest {

    @Test
    void muestraPruebasCuandoConsultaEsCorrecta() {
        VistaFalsa vista = new VistaFalsa("12345678A");
        List<PruebaHistorica> lista = List.of(
                new PruebaHistorica("IMG-001", "12345678A", "TAC", "REALIZADA", "INF-1", "PACS-1",
                        LocalDateTime.of(2026, 5, 10, 12, 0)));
        ControladorHistorialPruebasImpl controlador = new ControladorHistorialPruebasImpl(
                vista, dni -> new ResultadoHistorialPruebas(true, "OK", lista));

        controlador.consultarPruebasHistoricas();

        assertEquals(1, vista.pruebasMostradas.size());
        assertNull(vista.errorMostrado);
    }

    @Test
    void muestraErrorCuandoDniEstaVacio() {
        VistaFalsa vista = new VistaFalsa(" ");
        ControladorHistorialPruebasImpl controlador = new ControladorHistorialPruebasImpl(
                vista, dni -> new ResultadoHistorialPruebas(true, "OK", List.of()));

        controlador.consultarPruebasHistoricas();

        assertEquals(ControladorHistorialPruebasImpl.MENSAJE_DNI_VACIO, vista.errorMostrado);
    }

    @Test
    void muestraErrorDeConexionSiFallaElCliente() {
        VistaFalsa vista = new VistaFalsa("12345678A");
        ControladorHistorialPruebasImpl controlador = new ControladorHistorialPruebasImpl(
                vista, dni -> {
                    throw new ErrorComunicacionException("Fallo", new RuntimeException());
                });

        controlador.consultarPruebasHistoricas();

        assertEquals(ControladorHistorialPruebasImpl.MENSAJE_SIN_CONEXION, vista.errorMostrado);
    }

    private static class VistaFalsa implements VistaHistorialPruebas {
        private final String dni;
        private List<PruebaHistorica> pruebasMostradas = List.of();
        private String errorMostrado;

        VistaFalsa(String dni) {
            this.dni = dni;
        }

        @Override
        public String obtenerDniPaciente() {
            return dni;
        }

        @Override
        public void mostrarPruebas(List<PruebaHistorica> pruebas) {
            this.pruebasMostradas = pruebas;
        }

        @Override
        public void mostrarMensaje(String mensaje) {
        }

        @Override
        public void mostrarError(String mensaje) {
            this.errorMostrado = mensaje;
        }

        @Override
        public void limpiar() {
            this.pruebasMostradas = List.of();
        }
    }
}