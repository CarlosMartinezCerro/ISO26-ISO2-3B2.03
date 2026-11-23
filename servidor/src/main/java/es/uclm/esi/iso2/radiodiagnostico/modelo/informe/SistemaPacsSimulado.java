package es.uclm.esi.iso2.radiodiagnostico.modelo.informe;

/** PACS simulado: no envía nada, solo devuelve una referencia ficticia. */
public class SistemaPacsSimulado implements SistemaPacs {

    @Override
    public String archivar(Imagen imagen) {
        return "pacs://radiodiagnostico/" + imagen.dniPaciente() + "/" + imagen.id();
    }
}
