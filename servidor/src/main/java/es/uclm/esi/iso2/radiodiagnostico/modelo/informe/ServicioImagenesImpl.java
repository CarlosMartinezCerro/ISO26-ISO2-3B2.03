package es.uclm.esi.iso2.radiodiagnostico.modelo.informe;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class ServicioImagenesImpl implements ServicioImagenes {

    static final List<String> FORMATOS_PERMITIDOS = List.of(".dcm", ".png", ".jpg", ".jpeg");

    private final ImagenRepositorio imagenes;
    private final SistemaPacs pacs;
    private final Clock reloj;

    public ServicioImagenesImpl(ImagenRepositorio imagenes, SistemaPacs pacs) {
        this(imagenes, pacs, Clock.systemDefaultZone());
    }

    /** El reloj se inyecta para poder probar la fecha de creación. */
    public ServicioImagenesImpl(ImagenRepositorio imagenes, SistemaPacs pacs, Clock reloj) {
        this.imagenes = Objects.requireNonNull(imagenes, "El repositorio de imágenes es obligatorio");
        this.pacs = Objects.requireNonNull(pacs, "El sistema PACS es obligatorio");
        this.reloj = Objects.requireNonNull(reloj, "El reloj es obligatorio");
    }

    @Override
    public Imagen crearImagen(DatosImagen datos) {
        validar(datos);

        String id = String.format("IMG-%03d", imagenes.contar() + 1);
        Imagen sinArchivar = new Imagen(id, datos.idInforme(), datos.dniPaciente(), datos.idMedico(),
                datos.tipoPrueba(), datos.nombreFichero(), datos.contenido(),
                LocalDateTime.now(reloj), null);

        String referencia = pacs.archivar(sinArchivar);
        Imagen imagen = new Imagen(id, datos.idInforme(), datos.dniPaciente(), datos.idMedico(),
                datos.tipoPrueba(), datos.nombreFichero(), datos.contenido(),
                sinArchivar.fechaCreacion(), referencia);
        imagenes.guardar(imagen);
        return imagen;
    }

    @Override
    public List<Imagen> listarImagenesDeInforme(String idInforme) {
        return imagenes.buscarPorInforme(idInforme);
    }

    @Override
    public List<Imagen> listarImagenesPaciente(String idMedico, String dniPaciente) {
        obligatorio(idMedico, "médico");
        obligatorio(dniPaciente, "paciente");
        return imagenes.buscarPorPaciente(dniPaciente).stream()
                .filter(imagen -> imagen.idMedico().equals(idMedico))
                .sorted(Comparator.comparing(Imagen::fechaCreacion).reversed())
                .toList();
    }

    @Override
    public Imagen consultarImagen(String idMedico, String idImagen) {
        obligatorio(idMedico, "médico");
        obligatorio(idImagen, "imagen");
        Imagen imagen = imagenes.buscarPorId(idImagen)
                .orElseThrow(() -> new ImagenNoEncontradaException(
                        "No existe la imagen " + idImagen + "."));
        if (!imagen.idMedico().equals(idMedico)) {
            throw new AccesoDenegadoException(
                    "La imagen " + idImagen + " no pertenece a un paciente de este médico.");
        }
        return imagen;
    }

    private void validar(DatosImagen datos) {
        if (datos == null) {
            throw new DatosImagenInvalidosException("No se han recibido los datos de la imagen.");
        }
        obligatorio(datos.idInforme(), "informe");
        obligatorio(datos.dniPaciente(), "paciente");
        obligatorio(datos.idMedico(), "médico");
        obligatorio(datos.tipoPrueba(), "tipo de prueba");
        obligatorio(datos.nombreFichero(), "fichero");
        if (datos.contenido() == null || datos.contenido().length == 0) {
            throw new DatosImagenInvalidosException("El fichero de la imagen está vacío.");
        }
        String nombre = datos.nombreFichero().toLowerCase(Locale.ROOT);
        if (FORMATOS_PERMITIDOS.stream().noneMatch(nombre::endsWith)) {
            throw new DatosImagenInvalidosException(
                    "Formato no permitido. Use uno de estos: " + String.join(", ", FORMATOS_PERMITIDOS) + ".");
        }
    }

    private void obligatorio(String valor, String campo) {
        if (valor == null || valor.isBlank()) {
            throw new DatosImagenInvalidosException("El campo " + campo + " es obligatorio.");
        }
    }
}
