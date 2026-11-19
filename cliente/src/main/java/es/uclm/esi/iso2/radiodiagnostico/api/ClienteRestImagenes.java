package es.uclm.esi.iso2.radiodiagnostico.api;

import es.uclm.esi.iso2.radiodiagnostico.informe.ClienteConsultaImagenes;
import es.uclm.esi.iso2.radiodiagnostico.informe.ErrorComunicacionException;
import es.uclm.esi.iso2.radiodiagnostico.informe.RespuestaConsultaImagen;
import es.uclm.esi.iso2.radiodiagnostico.informe.RespuestaListadoImagenes;
import java.io.IOException;
import java.util.Objects;

/** CdU19 por REST: implementa ClienteConsultaImagenes llamando a la API del servidor. */
public class ClienteRestImagenes implements ClienteConsultaImagenes {

    private static final String MENSAJE_ERROR = "No se ha podido comunicar con el servidor.";

    private final ClienteHttp http;

    public ClienteRestImagenes(ClienteHttp http) {
        this.http = Objects.requireNonNull(http, "El cliente HTTP es obligatorio");
    }

    @Override
    public RespuestaListadoImagenes obtenerImagenesPaciente(String idMedico, String dniPaciente) {
        String ruta = "/api/pacientes/" + ClienteHttp.segmento(dniPaciente) + "/imagenes";
        return pedir(ruta, idMedico, RespuestaListadoImagenes.class);
    }

    @Override
    public RespuestaConsultaImagen obtenerImagen(String idMedico, String idImagen) {
        return pedir("/api/imagenes/" + ClienteHttp.segmento(idImagen), idMedico, RespuestaConsultaImagen.class);
    }

    private <T> T pedir(String ruta, String idMedico, Class<T> tipo) {
        try {
            return Json.leer(http.get(ruta, idMedico), tipo);
        } catch (IOException | JsonInvalidoException e) {
            throw new ErrorComunicacionException(MENSAJE_ERROR, e);
        }
    }
}
