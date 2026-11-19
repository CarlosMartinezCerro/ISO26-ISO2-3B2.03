
package es.uclm.esi.iso2.radiodiagnostico.controlador;

import io.javalin.Javalin;
import java.util.Objects;

public class RutasCitas {

    private final ControladorCitas controlador;

    public RutasCitas(ControladorCitas controlador) {
        this.controlador = Objects.requireNonNull(controlador);
    }

    public void registrar(Javalin app) {
        app.get("/citas", ctx -> {
            String dniPaciente = ctx.sessionAttribute("dniPaciente");

            if (dniPaciente == null || dniPaciente.isBlank()) {
                ctx.status(401).json(
                        java.util.Map.of(
                                "error", "El paciente no está autenticado."
                        )
                );
                return;
            }

            try {
                ctx.json(controlador.consultarCitas(dniPaciente));
            } catch (IllegalStateException e) {
                ctx.status(500).json(
                        java.util.Map.of(
                                "error", "No se pudieron consultar las citas."
                        )
                );
            }
        });
    }
}
