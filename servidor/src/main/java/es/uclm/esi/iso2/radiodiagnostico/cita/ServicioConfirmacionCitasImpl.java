package es.uclm.esi.iso2.radiodiagnostico.cita;

import java.util.Objects;

public class ServicioConfirmacionCitasImpl implements ServicioConfirmacionCitas {

    private final CitaRepositorio citas;

    public ServicioConfirmacionCitasImpl(CitaRepositorio citas) {
        this.citas = Objects.requireNonNull(citas, "El repositorio de citas es obligatorio");
    }

    @Override
    public Cita confirmarCita(String idCita, String dniPaciente) {
        obligatorio(idCita, "cita");
        obligatorio(dniPaciente, "DNI del paciente");

        // Si la cita es de otro paciente se responde igual que si no existiera,
        // para no revelar citas ajenas.
        Cita cita = citas.buscarPorId(idCita)
                .filter(encontrada -> encontrada.dniPaciente().equalsIgnoreCase(dniPaciente.trim()))
                .orElseThrow(() -> new CitaNoEncontradaException(
                        "No existe la cita " + idCita + " para este paciente."));

        if (cita.estado() != EstadoCita.ASIGNADA) {
            throw new CitaNoConfirmableException("La cita " + idCita
                    + " no se puede confirmar porque está " + cita.estado() + ".");
        }

        Cita confirmada = new Cita(cita.id(), cita.idSolicitud(), cita.dniPaciente(),
                cita.tipoPrueba(), cita.fechaHora(), cita.sala(), EstadoCita.CONFIRMADA);
        citas.actualizar(confirmada);
        return confirmada;
    }

    private void obligatorio(String valor, String campo) {
        if (valor == null || valor.isBlank()) {
            throw new DatosConfirmacionInvalidosException("El campo " + campo + " es obligatorio.");
        }
    }
}
