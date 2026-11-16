package es.uclm.esi.iso2.radiodiagnostico.persistencia;

import es.uclm.esi.iso2.radiodiagnostico.dominio.Paciente;
import es.uclm.esi.iso2.radiodiagnostico.dominio.Prueba;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class PruebaDAO {
    private static PruebaDAO instancia;
    private final Map<String, Paciente> pacientes;
    private final List<Prueba> pruebas;

    private PruebaDAO() {
        this.pacientes = new HashMap<>();
        this.pruebas = new ArrayList<>();
        cargarDatosIniciales();
    }

    public static synchronized PruebaDAO getInstancia() {
        if (instancia == null) {
            instancia = new PruebaDAO();
        }
        return instancia;
    }

    private void cargarDatosIniciales() {
        pacientes.put("PAC-001", new Paciente("PAC-001", "12345678A", "María", "López García"));
        pacientes.put("PAC-002", new Paciente("PAC-002", "87654321B", "Juan", "Pérez Ruiz"));

        pruebas.add(new Prueba("PRU-101", "PAC-001", "Radiografía de tórax",
                LocalDate.of(2025, 11, 10), "REALIZADA",
                "Campos pulmonares libres sin infiltrados.", "/imagenes/rx_torax_101.dcm"));
        pruebas.add(new Prueba("PRU-102", "PAC-001", "Resonancia magnética de rodilla",
                LocalDate.of(2026, 3, 22), "REALIZADA",
                "Menisco interno íntegro. Leve derrame articular.", "/imagenes/rm_rodilla_102.dcm"));
        pruebas.add(new Prueba("PRU-103", "PAC-001", "TAC abdominal",
                LocalDate.of(2026, 12, 15), "PENDIENTE",
                "Pendiente de realización", null));
    }

    public boolean existePaciente(String idPaciente) {
        return pacientes.containsKey(idPaciente);
    }

    public List<Prueba> obtenerPruebasRealizadasPorPaciente(String idPaciente) {
        return pruebas.stream()
                .filter(p -> p.getIdPaciente().equalsIgnoreCase(idPaciente))
                .filter(p -> "REALIZADA".equalsIgnoreCase(p.getEstado()))
                .collect(Collectors.toList());
    }

    public void guardarPrueba(Prueba prueba) {
        pruebas.add(prueba);
    }
}
