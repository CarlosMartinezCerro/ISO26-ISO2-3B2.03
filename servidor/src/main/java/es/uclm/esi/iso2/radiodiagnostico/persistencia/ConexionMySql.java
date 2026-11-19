package es.uclm.esi.iso2.radiodiagnostico.persistencia;

import java.sql.DriverManager;

/**
 * Configuración común de MySQL para todos los repositorios del servidor.
 * Lee las mismas variables de entorno que el CdU22:
 * MYSQL_URL (p. ej. jdbc:mysql://localhost:3306/radiodiagnostico), MYSQL_USER y MYSQL_PASSWORD.
 */
public final class ConexionMySql {

    private ConexionMySql() {
    }

    public static FuenteConexiones desdeEntorno() {
        String url = obligatoria("MYSQL_URL");
        String usuario = obligatoria("MYSQL_USER");
        String contrasena = obligatoria("MYSQL_PASSWORD");
        return () -> DriverManager.getConnection(url, usuario, contrasena);
    }

    private static String obligatoria(String nombre) {
        String valor = System.getenv(nombre);
        if (valor == null || valor.isBlank()) {
            throw new IllegalStateException("Debe configurarse la variable de entorno " + nombre + ".");
        }
        return valor;
    }
}
