package es.uclm.esi.iso2.radiodiagnostico.notificacion;

/** Medio por el que se avisa al paciente. */
public enum CanalNotificacion {
    /** Mensaje de texto al teléfono del paciente. */
    SMS,
    /** Aviso en la aplicación del paciente (se identifica por su DNI). */
    APLICACION
}
