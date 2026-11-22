package es.uclm.esi.iso2.radiodiagnostico.prueba;

import java.util.List;

/** Contrato del servidor para el CdU11 · Consultar prueba de seguimiento periódica (RF-06). */
public interface ServicioConsultaSeguimiento {

    /**
     * Devuelve las pruebas de seguimiento periódicas del paciente: primero las que siguen en
     * curso (ordenadas por la próxima fecha prevista) y después las finalizadas.
     *
     * @throws es.uclm.esi.iso2.radiodiagnostico.informe.DniPacienteInvalidoException
     *         si el DNI del paciente es nulo o está vacío
     */
    List<SeguimientoPeriodico> consultarSeguimientos(String dniPaciente);
}
