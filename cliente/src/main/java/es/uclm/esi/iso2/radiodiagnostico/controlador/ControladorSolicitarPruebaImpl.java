package es.uclm.esi.iso2.radiodiagnostico.controlador;

import es.uclm.esi.iso2.radiodiagnostico.solicitud.ClienteSolicitudes;
import es.uclm.esi.iso2.radiodiagnostico.solicitud.DatosSolicitudPrueba;
import es.uclm.esi.iso2.radiodiagnostico.solicitud.ErrorComunicacionException;
import es.uclm.esi.iso2.radiodiagnostico.vista.VistaSolicitarPrueba;
import java.util.Objects;
import java.util.concurrent.Executor;
import java.util.concurrent.ForkJoinPool;
import java.util.function.Consumer;

public class ControladorSolicitarPruebaImpl implements ControladorSolicitarPrueba {

    private final VistaSolicitarPrueba vista;
    private final ClienteSolicitudes cliente;
    private final Executor ejecutor;
    private final Consumer<Runnable> publicarEnVista;
    private boolean enviando;

    public ControladorSolicitarPruebaImpl(VistaSolicitarPrueba vista, ClienteSolicitudes cliente) {
        this(vista, cliente, ForkJoinPool.commonPool(), javafx.application.Platform::runLater);
    }

    public ControladorSolicitarPruebaImpl(VistaSolicitarPrueba vista, ClienteSolicitudes cliente,
                                          Executor ejecutor, Consumer<Runnable> publicarEnVista) {
        this.vista = Objects.requireNonNull(vista);
        this.cliente = Objects.requireNonNull(cliente);
        this.ejecutor = Objects.requireNonNull(ejecutor);
        this.publicarEnVista = Objects.requireNonNull(publicarEnVista);
    }

    @Override
    public void cargarPruebas() {
        try {
            vista.mostrarPruebas(cliente.obtenerPruebasDisponibles());
        } catch (ErrorComunicacionException e) {
            vista.mostrarError(e.getMessage());
        }
    }

    @Override
    public void solicitar() {
        if (enviando) {
            return;
        }
        String tipo = vista.obtenerTipoPrueba();
        String prioridad = vista.obtenerPrioridad();
        if (tipo == null || tipo.isBlank() || prioridad == null || prioridad.isBlank()) {
            vista.mostrarError("Selecciona el tipo de prueba y la prioridad.");
            return;
        }
        enviando = true;
        vista.establecerEnviando(true);
        ejecutor.execute(() -> {
            try {
                var resultado = cliente.solicitar(new DatosSolicitudPrueba(tipo, prioridad));
                publicarEnVista.accept(() -> {
                    if (resultado.correcto()) {
                        vista.mostrarExito(resultado.mensaje() + " Identificador: "
                                + resultado.idSolicitud() + ". Estado: " + resultado.estado() + ".");
                    } else {
                        vista.mostrarError(resultado.mensaje());
                    }
                    finalizarEnvio();
                });
            } catch (ErrorComunicacionException e) {
                publicarEnVista.accept(() -> {
                    vista.mostrarError(e.getMessage());
                    finalizarEnvio();
                });
            }
        });
    }

    private void finalizarEnvio() {
        enviando = false;
        vista.establecerEnviando(false);
    }
}
