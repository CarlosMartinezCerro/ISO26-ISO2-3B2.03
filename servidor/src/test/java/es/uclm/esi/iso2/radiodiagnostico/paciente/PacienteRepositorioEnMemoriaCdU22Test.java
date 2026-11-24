package es.uclm.esi.iso2.radiodiagnostico.paciente;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.LocalDate;
import java.util.Optional;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * CdU22, unidades MG y MB. Diseño: docs/testing/CdU22-diseno-pruebas.md.
 * Con dos variables, pairwise coincide con el producto cartesiano y contiene a each use (6.4).
 */
@DisplayName("CdU22 · PacienteRepositorioEnMemoria")
class PacienteRepositorioEnMemoriaCdU22Test {

    private static final String DNI = "12345678Z";
    private static final Paciente ALEX = new Paciente(DNI, "Alex", "Ejemplo",
            LocalDate.of(1990, 5, 12), "600000001");
    private static final Paciente SAM = new Paciente(DNI, "Sam", "Prueba",
            LocalDate.of(1992, 8, 24), "600000002");

    private final PacienteRepositorio vacio = new PacienteRepositorioEnMemoria();

    @Nested
    @DisplayName("MG y MB · Each use")
    class EachUse {

        @Disabled("PF-6: pendiente de decisión del equipo, ver apartado 11")
        @Test
        @DisplayName("MG-PW-02 · Posible fallo PF-6: guardar con el DNI ya presente")
        void posibleFalloPf6EachUse() {
            PacienteRepositorio repositorio = conAlex();

            assertThrows(PacienteDuplicadoException.class, () -> repositorio.guardar(SAM));

            assertEquals(Optional.of(ALEX), repositorio.buscarPorDni(DNI));
        }

        @Test
        @DisplayName("MG-PW-03: guardar null con el mapa vacío")
        void guardarNuloEnVacioEachUse() {
            assertThrows(NullPointerException.class, () -> vacio.guardar(null));

            assertEquals(Optional.empty(), vacio.buscarPorDni(null));
        }

        @Test
        @DisplayName("MB-PW-05: buscar null con el mapa vacío")
        void buscarNuloEnVacioEachUse() {
            assertEquals(Optional.empty(), vacio.buscarPorDni(null));
        }
    }

    @Nested
    @DisplayName("MG y MB · Pairwise")
    class Pairwise {

        @Test
        @DisplayName("MG-PW-04: guardar null con el mapa ocupado")
        void guardarNuloPairwise() {
            PacienteRepositorio repositorio = conAlex();

            assertThrows(NullPointerException.class, () -> repositorio.guardar(null));

            assertEquals(Optional.of(ALEX), repositorio.buscarPorDni(DNI));
            assertEquals(Optional.empty(), repositorio.buscarPorDni(null));
        }

        @Test
        @DisplayName("MB-PW-01: buscar con el mapa vacío")
        void buscarEnVacioPairwise() {
            assertEquals(Optional.empty(), vacio.buscarPorDni(DNI));
        }

        @Test
        @DisplayName("MB-PW-04: buscar otro DNI")
        void buscarOtroPairwise() {
            PacienteRepositorio repositorio = conAlex();

            assertEquals(Optional.empty(), repositorio.buscarPorDni("00000000T"));
            assertEquals(Optional.of(ALEX), repositorio.buscarPorDni(DNI));
        }

        @Test
        @DisplayName("MB-PW-06: buscar null")
        void buscarNuloPairwise() {
            PacienteRepositorio repositorio = conAlex();

            assertEquals(Optional.empty(), repositorio.buscarPorDni(null));
            assertEquals(Optional.of(ALEX), repositorio.buscarPorDni(DNI));
        }
    }

    private static PacienteRepositorio conAlex() {
        PacienteRepositorio repositorio = new PacienteRepositorioEnMemoria();
        repositorio.guardar(ALEX);
        return repositorio;
    }
}
