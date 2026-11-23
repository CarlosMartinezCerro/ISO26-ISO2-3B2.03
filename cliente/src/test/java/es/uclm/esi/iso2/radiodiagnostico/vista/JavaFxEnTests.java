package es.uclm.esi.iso2.radiodiagnostico.vista;

import javafx.application.Platform;
import org.junit.jupiter.api.Assumptions;

/**
 * Arranca JavaFX una sola vez para todos los tests de vistas y recuerda si se pudo.
 *
 * <p>Si no hay pantalla (por ejemplo en GitHub Actions), {@code Platform.startup} falla la primera
 * vez, pero JavaFX queda marcado como iniciado: una segunda llamada lanza
 * {@code IllegalStateException} y parecería que ya está arrancado. Si cada test lo arrancaba por su
 * cuenta, el segundo creía que JavaFX funcionaba y se quedaba colgado esperando al hilo de JavaFX.
 * Guardando el resultado del primer intento, todos los tests de vistas se omiten igual.
 */
final class JavaFxEnTests {

    private static Boolean disponible;
    private static String motivo;

    private JavaFxEnTests() {
    }

    /** Arranca JavaFX si hace falta; si no está disponible, omite los tests de la clase que lo llama. */
    static synchronized void arrancarOOmitir() {
        if (disponible == null) {
            try {
                Platform.startup(() -> { });
                disponible = true;
            } catch (IllegalStateException yaArrancado) {
                disponible = true;
            } catch (RuntimeException | UnsatisfiedLinkError sinPantalla) {
                disponible = false;
                motivo = sinPantalla.getMessage();
            }
        }
        Assumptions.assumeTrue(disponible, () -> "JavaFX no disponible: " + motivo);
    }
}
