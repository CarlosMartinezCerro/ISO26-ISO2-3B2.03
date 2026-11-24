package es.uclm.esi.iso2.radiodiagnostico.cita;

/** Estado de una cita. CdU2 (confirmar) y CdU27 (cancelar) añadirán transiciones. */
public enum EstadoCita {
    ASIGNADA,
    CONFIRMADA
}
