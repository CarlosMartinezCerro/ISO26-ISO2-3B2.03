package es.uclm.esi.iso2.radiodiagnostico.sistemasexternos;

import es.uclm.esi.iso2.radiodiagnostico.informe.Informe;
import es.uclm.esi.iso2.radiodiagnostico.informe.InformeResumen;
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
     * @throws IllegalArgumentException si el informe no contiene DNI o especialidad
     * @throws IllegalStateException si el sistema externo no está disponible
     */
    void getAntecedentes(Informe informe);

    /**
     * Archiva el informe en el sistema externo de historial clínico.
     *
     * @param informe informe que se desea archivar
     * @throws IllegalArgumentException si el informe no es válido
     * @throws IllegalStateException si el sistema externo no está disponible
     */
    void archivar(Informe informe);

    List<InformeResumen> getInformes(String dniPaciente);

    Informe getInforme(long idInforme);
}
