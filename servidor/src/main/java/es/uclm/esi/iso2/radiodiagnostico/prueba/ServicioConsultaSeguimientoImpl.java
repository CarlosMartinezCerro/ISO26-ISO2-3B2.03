package es.uclm.esi.iso2.radiodiagnostico.prueba;

import es.uclm.esi.iso2.radiodiagnostico.informe.DniPacienteInvalidoException;
import java.time.Clock;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

/**
 * Implementación del CdU11. Reutiliza:
 * <ul>
 *   <li>{@link ServicioSolicitudSeguimiento#buscarPorPaciente(String)} del CdU13, que ya busca las
 *       solicitudes de seguimiento normalizando el DNI, junto con sus entidades
 *       ({@link SolicitudSeguimiento}, {@link PeriodicidadSeguimiento},
 *       {@link RepeticionSeguimiento}).</li>
 *   <li>{@link DniPacienteInvalidoException} del CdU10 para validar el DNI igual que en la
 *       consulta del historial de pruebas.</li>
 * </ul>
 */
public class ServicioConsultaSeguimientoImpl implements ServicioConsultaSeguimiento {

    static final String DNI_OBLIGATORIO = "El DNI del paciente es obligatorio.";

    /** Primero los seguimientos en curso por próxima fecha; después los finalizados, más recientes antes. */
    private static final Comparator<SeguimientoPeriodico> ORDEN = Comparator
            .comparing(SeguimientoPeriodico::proximaFecha, Comparator.nullsLast(Comparator.naturalOrder()))
            .thenComparing(SeguimientoPeriodico::fechaFin, Comparator.reverseOrder());

    private final ServicioSolicitudSeguimiento solicitudes;
    private final Clock reloj;

    public ServicioConsultaSeguimientoImpl(ServicioSolicitudSeguimiento solicitudes) {
        this(solicitudes, Clock.systemDefaultZone());
    }

    /** El reloj se inyecta para poder probar qué repetición es la próxima. */
    public ServicioConsultaSeguimientoImpl(ServicioSolicitudSeguimiento solicitudes, Clock reloj) {
        this.solicitudes = Objects.requireNonNull(solicitudes,
                "El servicio de solicitudes de seguimiento es obligatorio");
        this.reloj = Objects.requireNonNull(reloj, "El reloj es obligatorio");
    }

    @Override
    public List<SeguimientoPeriodico> consultarSeguimientos(String dniPaciente) {
        if (dniPaciente == null || dniPaciente.isBlank()) {
            throw new DniPacienteInvalidoException(DNI_OBLIGATORIO);
        }
        LocalDate hoy = LocalDate.now(reloj);
        return solicitudes.buscarPorPaciente(dniPaciente.trim()).stream()
                .map(solicitud -> resumir(solicitud, hoy))
                .sorted(ORDEN)
                .toList();
    }

    private static SeguimientoPeriodico resumir(SolicitudSeguimiento solicitud, LocalDate hoy) {
        List<RepeticionSeguimiento> repeticiones = solicitud.repeticiones();

        LocalDate proxima = repeticiones.stream()
                .map(RepeticionSeguimiento::fechaPrevista)
                .filter(fecha -> !fecha.isBefore(hoy))
                .min(Comparator.naturalOrder())
                .orElse(null);
        LocalDate fin = repeticiones.stream()
                .map(RepeticionSeguimiento::fechaPrevista)
                .max(Comparator.naturalOrder())
                .orElse(solicitud.fechaInicio());
        int citadas = (int) repeticiones.stream()
                .filter(r -> r.estado() == EstadoRepeticionSeguimiento.CITADA)
                .count();

        return new SeguimientoPeriodico(solicitud.id(), solicitud.dniPaciente(),
                solicitud.dniMedico(), solicitud.tipoPrueba(), solicitud.indicacionClinica(),
                solicitud.prioridad(), describir(solicitud.periodicidad()),
                solicitud.fechaInicio(), fin, repeticiones.size(), citadas, proxima,
                proxima == null ? SeguimientoPeriodico.FINALIZADO : SeguimientoPeriodico.EN_CURSO,
                repeticiones);
    }

    /** Texto legible de la periodicidad: "Cada mes", "Cada 3 meses", "Cada 2 semanas"... */
    static String describir(PeriodicidadSeguimiento periodicidad) {
        boolean una = periodicidad.cada() == 1;
        String unidad = switch (periodicidad.unidad()) {
            case DIAS -> una ? "día" : "días";
            case SEMANAS -> una ? "semana" : "semanas";
            case MESES -> una ? "mes" : "meses";
        };
        return una ? "Cada " + unidad : "Cada " + periodicidad.cada() + " " + unidad;
    }
}
