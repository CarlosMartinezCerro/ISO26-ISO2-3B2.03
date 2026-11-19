
package es.uclm.esi.iso2.radiodiagnostico.controlador;

import io.javalin.Javalin;
import java.util.Objects;

public class RutasCitas {

    private final ControladorCitas controlador;

    public RutasCitas(ControladorCitas controlador) {
        this.controlador = Objects.requireNonNull(controlador);
    }

    public void registrar(Javalin app) {
        app.get("/citas/{dniPaciente}", ctx -> {
            String dniPaciente = ctx.pathParam("dniPaciente");
            ctx.json(controlador.consultarCitas(dniPaciente));
        });
    }
}
