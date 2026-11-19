package es.uclm.esi.iso2.radiodiagnostico.vista;

/** Avisos de mentira para los tests: guardan el último mensaje en vez de abrir un diálogo. */
class AvisosFalsos implements Avisos {

    String tituloInformacion;
    String informacion;
    String tituloError;
    String error;

    @Override
    public void informacion(String titulo, String mensaje) {
        tituloInformacion = titulo;
        informacion = mensaje;
    }

    @Override
    public void error(String titulo, String mensaje) {
        tituloError = titulo;
        error = mensaje;
    }
}
