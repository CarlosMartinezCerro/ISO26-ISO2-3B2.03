package es.uclm.esi.iso2.radiodiagnostico.notificacion;

import es.uclm.esi.iso2.radiodiagnostico.paciente.Paciente;
import es.uclm.esi.iso2.radiodiagnostico.paciente.PacienteRepositorio;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class ServicioNotificacionesImpl implements ServicioNotificaciones {

    static final int MAXIMO_ASUNTO = 100;
    static final int MAXIMO_MENSAJE = 500;

    private final PacienteRepositorio pacientes;
    private final NotificacionRepositorio notificaciones;
    private final PasarelaNotificaciones pasarela;
    private final Clock reloj;

    public ServicioNotificacionesImpl(PacienteRepositorio pacientes, NotificacionRepositorio notificaciones,
                                      PasarelaNotificaciones pasarela) {
        this(pacientes, notificaciones, pasarela, Clock.systemDefaultZone());
    }

    /** El reloj se inyecta para poder probar la fecha de envío. */
    public ServicioNotificacionesImpl(PacienteRepositorio pacientes, NotificacionRepositorio notificaciones,
                                      PasarelaNotificaciones pasarela, Clock reloj) {
        this.pacientes = Objects.requireNonNull(pacientes, "El repositorio de pacientes es obligatorio");
        this.notificaciones = Objects.requireNonNull(notificaciones,
                "El repositorio de notificaciones es obligatorio");
        this.pasarela = Objects.requireNonNull(pasarela, "La pasarela de notificaciones es obligatoria");
        this.reloj = Objects.requireNonNull(reloj, "El reloj es obligatorio");
    }

    @Override
    public Notificacion notificar(DatosNotificacion datos, TipoNotificacion tipo) {
        if (datos == null) {
            throw new NotificacionInvalidaException("Los datos de la notificación son obligatorios.");
        }
        Objects.requireNonNull(tipo, "El tipo de notificación es obligatorio");
        String dni = normalizarDni(datos.dniPaciente());
        if (datos.canal() == null) {
            throw new NotificacionInvalidaException("El campo canal es obligatorio.");
        }
        String asunto = texto(datos.asunto(), "asunto", MAXIMO_ASUNTO);
        String mensaje = texto(datos.mensaje(), "mensaje", MAXIMO_MENSAJE);

        Paciente paciente = pacientes.buscarPorDni(dni).orElseThrow(() ->
                new PacienteNoEncontradoException("No existe ningún paciente con el DNI " + dni + "."));
        String destino = destino(paciente, datos.canal());

        String id = String.format("NOT-%03d", notificaciones.contar() + 1);
        Notificacion notificacion = new Notificacion(id, dni, tipo, datos.canal(), destino, asunto, mensaje,
                LocalDateTime.now(reloj));
        pasarela.enviar(notificacion);
        notificaciones.guardar(notificacion);
        return notificacion;
    }

    @Override
    public List<Notificacion> historialPaciente(String dniPaciente) {
        return notificaciones.buscarPorPaciente(normalizarDni(dniPaciente));
    }

    private static String normalizarDni(String dni) {
        if (dni == null || dni.isBlank()) {
            throw new NotificacionInvalidaException("El campo dni es obligatorio.");
        }
        return dni.trim().toUpperCase(Locale.ROOT);
    }

    private static String texto(String valor, String campo, int maximo) {
        if (valor == null || valor.isBlank()) {
            throw new NotificacionInvalidaException("El campo " + campo + " es obligatorio.");
        }
        String limpio = valor.trim();
        if (limpio.length() > maximo) {
            throw new NotificacionInvalidaException(
                    "El campo " + campo + " no puede tener más de " + maximo + " caracteres.");
        }
        return limpio;
    }

    /** Un SMS necesita el teléfono del paciente; un aviso en la aplicación va a su DNI. */
    private static String destino(Paciente paciente, CanalNotificacion canal) {
        if (canal == CanalNotificacion.APLICACION) {
            return paciente.dni();
        }
        if (paciente.telefono() == null || paciente.telefono().isBlank()) {
            throw new NotificacionInvalidaException("El paciente " + paciente.dni()
                    + " no tiene teléfono: no se le puede enviar un SMS.");
        }
        return paciente.telefono();
    }
}
