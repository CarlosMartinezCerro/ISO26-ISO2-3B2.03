package es.uclm.esi.iso2.radiodiagnostico.modelo.informe;

import java.time.LocalDateTime;

/** Imagen médica que un médico asocia a un informe de un paciente (CdU17). */
public record Imagen(String id, String idInforme, String dniPaciente, String idMedico,
                     String tipoPrueba, String nombreFichero, byte[] contenido,
                     LocalDateTime fechaCreacion, String referenciaPacs) {
}
