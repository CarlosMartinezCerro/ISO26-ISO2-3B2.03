package es.uclm.esi.iso2.radiodiagnostico.sistemasexternos;

import es.uclm.esi.iso2.radiodiagnostico.informe.Informe;
import es.uclm.esi.iso2.radiodiagnostico.informe.InformeResumen;
import es.uclm.esi.iso2.radiodiagnostico.informe.DatosInformeInvalidosException;
import es.uclm.esi.iso2.radiodiagnostico.informe.DniPacienteInvalidoException;
import es.uclm.esi.iso2.radiodiagnostico.informe.HistorialClinicoNoDisponibleException;
import es.uclm.esi.iso2.radiodiagnostico.informe.InformeNoEncontradoException;
import java.util.List;

/**
 * Contrato con el sistema externo de historial clínico.
 * Permite incorporar datos previos del paciente al informe y archivar el informe
 * generado sin acoplar la lógica de negocio a una implementación concreta.
 */
public interface HistorialClinico {

    /**
     * Completa los antecedentes generales y los antecedentes de la especialidad
     * a partir del paciente y de la especialidad incluidos en el informe.
     *
     * @param informe informe que debe recibir los datos del historial
     * @throws DniPacienteInvalidoException si falta el DNI del paciente
     * @throws DatosInformeInvalidosException si falta la especialidad
     * @throws HistorialClinicoNoDisponibleException si el sistema externo no está disponible
     */
    Informe getAntecedentes(Informe informe);

    /**
     * Archiva el informe en el sistema externo de historial clínico.
     *
     * @param informe informe que se desea archivar
     * @throws DatosInformeInvalidosException si el informe no es válido
     * @throws DniPacienteInvalidoException si falta el DNI del paciente
     * @throws HistorialClinicoNoDisponibleException si el sistema externo no está disponible
     */
    Informe archivar(Informe informe);

    List<InformeResumen> getInformes(String dniPaciente);

    /**
     * @throws InformeNoEncontradoException si el ID es inválido o no existe
     * @throws HistorialClinicoNoDisponibleException si el sistema externo no está disponible
     */
    Informe getInforme(long idInforme);
}
