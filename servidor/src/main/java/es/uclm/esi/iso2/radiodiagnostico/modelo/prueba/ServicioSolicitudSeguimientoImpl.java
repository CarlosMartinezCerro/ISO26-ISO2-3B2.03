package es.uclm.esi.iso2.radiodiagnostico.modelo.prueba;

import java.time.Clock;
import java.time.DateTimeException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public class ServicioSolicitudSeguimientoImpl implements ServicioSolicitudSeguimiento {

    private final SolicitudSeguimientoRepositorio solicitudes;
    private final CatalogoTiposPrueba catalogo;
    private final RegistroMedicos medicos;
    private final Clock reloj;

    public ServicioSolicitudSeguimientoImpl(SolicitudSeguimientoRepositorio solicitudes,
                                            CatalogoTiposPrueba catalogo,
                                            RegistroMedicos medicos) {
        this(solicitudes, catalogo, medicos, Clock.systemDefaultZone());
    }

    /** El reloj se inyecta para poder probar la regla de "fecha de inicio no anterior a hoy". */
    public ServicioSolicitudSeguimientoImpl(SolicitudSeguimientoRepositorio solicitudes,
                                            CatalogoTiposPrueba catalogo,
                                            RegistroMedicos medicos, Clock reloj) {
        this.solicitudes = Objects.requireNonNull(solicitudes,
                "El repositorio de solicitudes de seguimiento es obligatorio");
        this.catalogo = Objects.requireNonNull(catalogo, "El catálogo de tipos de prueba es obligatorio");
        this.medicos = Objects.requireNonNull(medicos, "El registro de médicos es obligatorio");
        this.reloj = Objects.requireNonNull(reloj, "El reloj es obligatorio");
    }

    @Override
    public SolicitudSeguimiento solicitar(DatosSolicitudSeguimiento datos) {
        validar(datos);

        String id = "SEG-" + UUID.randomUUID();
        PeriodicidadSeguimiento periodicidad = new PeriodicidadSeguimiento(datos.cada(), datos.unidad());
        List<RepeticionSeguimiento> repeticiones = generarRepeticiones(id, periodicidad, datos);

        SolicitudSeguimiento solicitud = new SolicitudSeguimiento(id,
                normalizarDni(datos.dniPaciente()), normalizarDni(datos.dniMedico()),
                datos.tipoPrueba().trim(), datos.indicacionClinica().trim(),
                datos.justificacion().trim(), datos.prioridad(), periodicidad, datos.fechaInicio(),
                datos.numeroRepeticiones(), datos.fechaLimite(), repeticiones);
        solicitudes.guardar(solicitud);
        // TODO(unificación CdU12/CdU25): cada repetición deberá registrarse también como
        //  solicitud pendiente de cita en el SolicitudRepositorio del CdU25, para que el personal
        //  de admisión la vea en ServicioCitas.listarSolicitudesPendientes() y pueda citarla.
        //  Hasta entonces las repeticiones solo existen en SolicitudSeguimientoRepositorio.
        return solicitud;
    }

    @Override
    public Optional<SolicitudSeguimiento> buscarPorId(String id) {
        return solicitudes.buscarPorId(id);
    }

    @Override
    public List<SolicitudSeguimiento> buscarPorPaciente(String dniPaciente) {
        return solicitudes.buscarPorPaciente(normalizarDni(dniPaciente));
    }

    private List<RepeticionSeguimiento> generarRepeticiones(String id,
                                                            PeriodicidadSeguimiento periodicidad,
                                                            DatosSolicitudSeguimiento datos) {
        List<RepeticionSeguimiento> repeticiones = new ArrayList<>();
        for (int indice = 0; ; indice++) {
            boolean porNumero = datos.numeroRepeticiones() != null;
            if (porNumero && indice >= datos.numeroRepeticiones()) {
                return repeticiones;
            }
            LocalDate fecha;
            try {
                fecha = periodicidad.fechaRepeticion(datos.fechaInicio(), indice);
            } catch (DateTimeException e) {
                if (!porNumero) {
                    // Una fecha que no se puede representar siempre es posterior a la fecha límite.
                    return repeticiones;
                }
                throw new DatosSeguimientoInvalidosException("El seguimiento generaría fechas "
                        + "demasiado lejanas. Reduzca la periodicidad o el número de repeticiones.");
            }
            if (!porNumero && fecha.isAfter(datos.fechaLimite())) {
                return repeticiones;
            }
            if (repeticiones.size() == MAXIMO_REPETICIONES) {
                throw new DatosSeguimientoInvalidosException("El seguimiento generaría más de "
                        + MAXIMO_REPETICIONES + " repeticiones. Amplíe la periodicidad o "
                        + "reduzca el fin del seguimiento.");
            }
            int numero = indice + 1;
            repeticiones.add(new RepeticionSeguimiento(id + "-" + numero, id, numero, fecha,
                    EstadoRepeticionSeguimiento.PENDIENTE_CITA));
        }
    }

    private void validar(DatosSolicitudSeguimiento datos) {
        if (datos == null) {
            throw new DatosSeguimientoInvalidosException(
                    "Los datos de la solicitud de seguimiento son obligatorios.");
        }
        validarTexto(datos.dniMedico(), "dniMedico");
        if (!medicos.esMedico(datos.dniMedico())) {
            throw new UsuarioNoMedicoException(
                    "Solo un médico puede solicitar una prueba de seguimiento periódica.");
        }

        validarTexto(datos.dniPaciente(), "dniPaciente");
        validarTexto(datos.tipoPrueba(), "tipoPrueba");
        validarTexto(datos.indicacionClinica(), "indicacionClinica");
        validarTexto(datos.justificacion(), "justificacion");
        validarPresente(datos.prioridad(), "prioridad");
        validarPresente(datos.cada(), "cada");
        validarPresente(datos.unidad(), "unidad");
        validarPresente(datos.fechaInicio(), "fechaInicio");

        if (!catalogo.existe(datos.tipoPrueba())) {
            throw new DatosSeguimientoInvalidosException(
                    "El tipo de prueba " + datos.tipoPrueba().trim() + " no existe en el catálogo.");
        }
        if (!catalogo.admiteSeguimiento(datos.tipoPrueba())) {
            throw new TipoPruebaSinSeguimientoException("El tipo de prueba "
                    + datos.tipoPrueba().trim() + " no admite seguimiento periódico.");
        }
        if (datos.cada() <= 0) {
            throw new DatosSeguimientoInvalidosException(
                    "El campo cada debe ser mayor que 0.");
        }
        if (datos.fechaInicio().isBefore(LocalDate.now(reloj))) {
            throw new DatosSeguimientoInvalidosException(
                    "El campo fechaInicio no puede ser anterior a hoy.");
        }
        validarFin(datos);
    }

    private void validarFin(DatosSolicitudSeguimiento datos) {
        boolean conRepeticiones = datos.numeroRepeticiones() != null;
        boolean conFechaLimite = datos.fechaLimite() != null;
        if (conRepeticiones == conFechaLimite) {
            throw new DatosSeguimientoInvalidosException("Indique exactamente un criterio de fin "
                    + "del seguimiento: numeroRepeticiones o fechaLimite.");
        }
        if (conRepeticiones && datos.numeroRepeticiones() < 1) {
            throw new DatosSeguimientoInvalidosException(
                    "El campo numeroRepeticiones debe ser mayor o igual que 1.");
        }
        if (conRepeticiones && datos.numeroRepeticiones() > MAXIMO_REPETICIONES) {
            throw new DatosSeguimientoInvalidosException(
                    "El campo numeroRepeticiones no puede ser mayor que " + MAXIMO_REPETICIONES + ".");
        }
        if (conFechaLimite && !datos.fechaLimite().isAfter(datos.fechaInicio())) {
            throw new DatosSeguimientoInvalidosException(
                    "El campo fechaLimite debe ser posterior a fechaInicio.");
        }
    }

    private static void validarTexto(String valor, String campo) {
        if (valor == null || valor.trim().isBlank()) {
            throw new DatosSeguimientoInvalidosException("El campo " + campo + " es obligatorio.");
        }
    }

    private static void validarPresente(Object valor, String campo) {
        if (valor == null) {
            throw new DatosSeguimientoInvalidosException("El campo " + campo + " es obligatorio.");
        }
    }

    private static String normalizarDni(String dni) {
        return dni == null ? null : dni.trim().toUpperCase(Locale.ROOT);
    }
}
