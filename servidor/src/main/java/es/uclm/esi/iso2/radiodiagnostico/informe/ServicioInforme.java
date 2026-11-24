package es.uclm.esi.iso2.radiodiagnostico.informe;

import java.util.List;

/** Operaciones del servicio de informes. */
public interface ServicioInforme {

    /**
     * Crea el informe solicitando su archivo en el historial clinico.
     *
     * @param informe informe que se desea crear
     * @return el informe creado
     * @throws DatosInformeInvalidosException si faltan datos obligatorios
     * @throws DniPacienteInvalidoException si falta el DNI del paciente
     * @throws HistorialClinicoNoDisponibleException si no se puede archivar
     */
    Informe crearInforme(Informe informe);

    /**
     * Obtiene los antecedentes clinicos y los incorpora al informe.
     *
     * @param informe informe al que se incorporaran los antecedentes
     * @return los antecedentes generales obtenidos
     * @throws DatosInformeInvalidosException si faltan datos obligatorios
     * @throws DniPacienteInvalidoException si falta el DNI del paciente
     */
    Informe obtenerAntecedentes(Informe informe);

    List<InformeResumen> listarInformes(String dni);

    Informe obtenerInforme(long id);
}
