package es.uclm.esi.iso2.radiodiagnostico.prueba;

/**
 * Comprueba si un usuario tiene rol de médico.
 * La implementación simulada se sustituirá cuando el proyecto tenga gestión de usuarios y roles.
 */
public interface RegistroMedicos {

    boolean esMedico(String dni);
}
