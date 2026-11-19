package es.uclm.esi.iso2.radiodiagnostico.api;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import es.uclm.esi.iso2.radiodiagnostico.cita.DatosAsignacionCita;
import es.uclm.esi.iso2.radiodiagnostico.informe.DatosImagen;
import java.time.LocalDate;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;

class JsonTest {

    record ConFecha(LocalDate dia) {
    }

    @Test
    void lasFechasViajanComoTextoIso() {
        String json = Json.escribir(new DatosAsignacionCita("SOL-001", LocalDateTime.of(2026, 11, 17, 9, 30), "SALA-1"));

        assertEquals("{\"idSolicitud\":\"SOL-001\",\"fechaHora\":\"2026-11-17T09:30\",\"sala\":\"SALA-1\"}", json);
    }

    @Test
    void leeFechasYHorasIso() {
        DatosAsignacionCita datos = Json.leer(
                "{\"idSolicitud\":\"SOL-001\",\"fechaHora\":\"2026-11-17T09:30:00\",\"sala\":\"SALA-1\"}",
                DatosAsignacionCita.class);

        assertEquals(LocalDateTime.of(2026, 11, 17, 9, 30), datos.fechaHora());
    }

    @Test
    void escribeYLeeFechasSinHora() {
        String json = Json.escribir(new ConFecha(LocalDate.of(2026, 11, 2)));

        assertEquals("{\"dia\":\"2026-11-02\"}", json);
        assertEquals(LocalDate.of(2026, 11, 2), Json.leer(json, ConFecha.class).dia());
    }

    @Test
    void losBytesViajanEnBase64() {
        DatosImagen leidos = Json.leer(Json.escribir(new DatosImagen("INF-1", "D", "M", "TAC", "a.png",
                new byte[] {1, 2, 3})), DatosImagen.class);

        assertArrayEquals(new byte[] {1, 2, 3}, leidos.contenido());
    }

    @Test
    void ignoraLosCamposQueNoConoce() {
        DatosAsignacionCita datos = Json.leer(
                "{\"idSolicitud\":\"SOL-001\",\"sala\":\"S\",\"otroCampo\":42}", DatosAsignacionCita.class);

        assertEquals("SOL-001", datos.idSolicitud());
    }

    @Test
    void unJsonInvalidoLanzaJsonInvalidoException() {
        assertThrows(JsonInvalidoException.class, () -> Json.leer("[1,2", DatosAsignacionCita.class));
        assertThrows(JsonInvalidoException.class, () -> Json.leer(null, DatosAsignacionCita.class));
    }
}
