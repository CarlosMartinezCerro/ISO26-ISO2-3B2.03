package es.uclm.esi.iso2.radiodiagnostico.vista;

/**
 * Mensajes emergentes de las vistas. Las ventanas reales usan {@link AvisosDialogo};
 * los tests usan una versión falsa para no quedarse bloqueados esperando a que alguien pulse "Aceptar".
 * Común para todas las vistas del cliente.
 */
public interface Avisos {

    void informacion(String titulo, String mensaje);

    void error(String titulo, String mensaje);
}
