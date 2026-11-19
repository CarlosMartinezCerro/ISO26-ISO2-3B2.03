package es.uclm.esi.iso2.radiodiagnostico.persistencia;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Abre conexiones con la base de datos. Los repositorios MySQL dependen de esta
 * interfaz y no de la configuración, así se pueden probar con cualquier base de datos.
 */
@FunctionalInterface
public interface FuenteConexiones {

    Connection abrir() throws SQLException;
}
