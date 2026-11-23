package es.uclm.esi.iso2.radiodiagnostico.informe;

import java.time.LocalDate;

import es.uclm.esi.iso2.radiodiagnostico.usuario.Medico;
import es.uclm.esi.iso2.radiodiagnostico.usuario.Paciente;

public class Informe {

    private long id;
    private Medico medico;
    private Paciente paciente;
    private String antecedentes;
    private String especialidad;
    private String motivoConsulta;
    private String historialEspecialidad;
    private String consultaActual;
    private String diagnostico;
    private String pruebas;
    private String tratamiento;
    private boolean alta;
    private LocalDate fecha;
    private String anamnesis;

    public Informe(Medico medico, Paciente paciente, String especialidad,
                   String motivoConsulta, String consultaActual, String diagnostico,
                   String pruebas, String tratamiento, boolean alta,
                   LocalDate fecha, String anamnesis) {
        this.medico = medico;
        this.paciente = paciente;
        this.especialidad = especialidad;
        this.motivoConsulta = motivoConsulta;
        this.consultaActual = consultaActual;
        this.diagnostico = diagnostico;
        this.pruebas = pruebas;
        this.tratamiento = tratamiento;
        this.alta = alta;
        this.fecha = fecha;
        this.anamnesis = anamnesis;
    }

    public Informe(long id, Medico medico, Paciente paciente, String especialidad,
                   String motivoConsulta, String consultaActual, String diagnostico,
                   String pruebas, String tratamiento, boolean alta,
                   LocalDate fecha, String anamnesis) {
        this(medico, paciente, especialidad, motivoConsulta, consultaActual, diagnostico,
                pruebas, tratamiento, alta, fecha, anamnesis);
        this.id = id;
    }

    public Informe(Medico medico, Paciente paciente, String especialidad,
                   String motivoConsulta, String consultaActual, String diagnostico,
                   String pruebas, String tratamiento, boolean alta,
                   LocalDate fecha, String anamnesis, long id) {
        this(id, medico, paciente, especialidad, motivoConsulta, consultaActual, diagnostico,
                pruebas, tratamiento, alta, fecha, anamnesis);
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String obtenerAntecedentes() {
        return antecedentes;
    }

    public String obtenerHistorial() {
        return historialEspecialidad;
    }

    public Medico getMedico() {
        return medico;
    }

    public void setMedico(Medico medico) {
        this.medico = medico;
    }

    public Paciente getPaciente() {
        return paciente;
    }

    public void setPaciente(Paciente paciente) {
        this.paciente = paciente;
    }

    public void setAntecedentes(String antecedentes) {
        this.antecedentes = antecedentes;
    }

    public String getEspecialidad() {
        return especialidad;
    }

    public void setEspecialidad(String especialidad) {
        this.especialidad = especialidad;
    }

    public String getMotivoConsulta() {
        return motivoConsulta;
    }

    public void setMotivoConsulta(String motivoConsulta) {
        this.motivoConsulta = motivoConsulta;
    }

    public void setHistorialEspecialidad(String historialEspecialidad) {
        this.historialEspecialidad = historialEspecialidad;
    }

    public String getConsultaActual() {
        return consultaActual;
    }

    public void setConsultaActual(String consultaActual) {
        this.consultaActual = consultaActual;
    }

    public String getDiagnostico() {
        return diagnostico;
    }

    public void setDiagnostico(String diagnostico) {
        this.diagnostico = diagnostico;
    }

    public String getPruebas() {
        return pruebas;
    }

    public void setPruebas(String pruebas) {
        this.pruebas = pruebas;
    }

    public String gettratamiento() {
        return tratamiento;
    }

    public void settratamiento(String tratamiento) {
        this.tratamiento = tratamiento;
    }

    public String getTratamiento() {
        return tratamiento;
    }

    public void setTratamiento(String tratamiento) {
        this.tratamiento = tratamiento;
    }

    public boolean isAlta() {
        return alta;
    }

    public void setAlta(boolean alta) {
        this.alta = alta;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public void setFecha(LocalDate fecha) {
        this.fecha = fecha;
    }

    public String getAnamnesis() {
        return anamnesis;
    }

    public void setAnamnesis(String anamnesis) {
        this.anamnesis = anamnesis;
    }
}
