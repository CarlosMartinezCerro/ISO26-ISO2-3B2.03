package es.uclm.esi.iso2.radiodiagnostico.notificacion;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;

class NotificacionRepositorioEnMemoriaTest {

    private static final LocalDateTime FECHA = LocalDateTime.of(2026, 11, 21, 10, 0);

    @Test
    void buscaSinDistinguirMayusculasYDeLaMasRecienteALaMasAntigua() {
        NotificacionRepositorioEnMemoria repositorio = new NotificacionRepositorioEnMemoria();
        repositorio.guardar(notificacion("NOT-001", "12345678Z", FECHA));
        repositorio.guardar(notificacion("NOT-002", "01234567L", FECHA));
        repositorio.guardar(notificacion("NOT-003", "12345678Z", FECHA.plusMinutes(5)));
        repositorio.guardar(notificacion("NOT-004", "12345678Z", FECHA));

        List<Notificacion> encontradas = repositorio.buscarPorPaciente("12345678z");

        assertEquals(List.of("NOT-003", "NOT-004", "NOT-001"), encontradas.stream().map(Notificacion::id).toList());
        assertEquals(4, repositorio.contar());
    }

    private static Notificacion notificacion(String id, String dni, LocalDateTime fecha) {
        return new Notificacion(id, dni, TipoNotificacion.GENERAL, CanalNotificacion.APLICACION, dni,
                "Asunto", "Mensaje", fecha);
    }
}
