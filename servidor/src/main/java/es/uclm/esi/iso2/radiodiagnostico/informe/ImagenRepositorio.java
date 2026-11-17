package es.uclm.esi.iso2.radiodiagnostico.informe;

import java.util.List;
import java.util.Optional;

public interface ImagenRepositorio {

    void guardar(Imagen imagen);

    Optional<Imagen> buscarPorId(String id);

    List<Imagen> buscarPorInforme(String idInforme);

    /** Imágenes de un paciente (CdU19 · Consultar imagen). */
    List<Imagen> buscarPorPaciente(String dniPaciente);

    /** Número de imágenes guardadas; se usa para generar el siguiente identificador. */
    int contar();
}
