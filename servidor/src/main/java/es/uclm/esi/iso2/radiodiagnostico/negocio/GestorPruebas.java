package es.uclm.esi.iso2.radiodiagnostico.negocio;

import es.uclm.esi.iso2.radiodiagnostico.dominio.Prueba;
import es.uclm.esi.iso2.radiodiagnostico.persistencia.PruebaDAO;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class GestorPruebas {
    private final PruebaDAO pruebaDAO;

    public GestorPruebas() {
        this.pruebaDAO = PruebaDAO.getInstancia();
    }

    public GestorPruebas(PruebaDAO pruebaDAO) {
        this.pruebaDAO = pruebaDAO;
    }

    public List<Prueba> consultarPruebasHistoricas(String idPaciente) {
        if (idPaciente == null || idPaciente.trim().isEmpty()) {
            throw new IllegalArgumentException("El identificador del paciente no puede estar vacío.");
        }

        String idNormalizado = idPaciente.trim().toUpperCase();
        if (!pruebaDAO.existePaciente(idNormalizado)) {
            throw new IllegalArgumentException("No existe ningún paciente registrado con ID: " + idNormalizado);
        }

        return pruebaDAO.obtenerPruebasRealizadasPorPaciente(idNormalizado).stream()
                .sorted(Comparator.comparing(Prueba::getFechaRealizacion).reversed())
                .collect(Collectors.toList());
    }
}