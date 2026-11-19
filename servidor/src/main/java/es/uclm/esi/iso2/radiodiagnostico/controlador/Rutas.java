package es.uclm.esi.iso2.radiodiagnostico.controlador;

import io.javalin.Javalin;

/**
 * Conjunto de rutas REST de un bloque funcional (D-009).
 * Cada bloque (pacientes, imágenes, citas...) implementa esta interfaz y se registra en
 * {@link es.uclm.esi.iso2.radiodiagnostico.Servidor}, así el servidor no depende de cada caso de uso.
 */
public interface Rutas {

    void registrar(Javalin app);
}
