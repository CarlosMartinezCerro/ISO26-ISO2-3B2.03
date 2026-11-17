package es.uclm.esi.iso2.radiodiagnostico.dominio;

import java.io.Serializable;

public class Paciente implements Serializable {
    private static final long serialVersionUID = 1L;

    private String idPaciente;
    private String dni;
    private String nombre;
    private String apellidos;

    public Paciente(String idPaciente, String dni, String nombre, String apellidos) {
        this.idPaciente = idPaciente;
        this.dni = dni;
        this.nombre = nombre;
        this.apellidos = apellidos;
    }

    public String getIdPaciente() { return idPaciente; }
    public void setIdPaciente(String idPaciente) { this.idPaciente = idPaciente; }

    public String getDni() { return dni; }
    public void setDni(String dni) { this.dni = dni; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getApellidos() { return apellidos; }
    public void setApellidos(String apellidos) { this.apellidos = apellidos; }

    @Override
    public String toString() {
        return nombre + " " + apellidos + " (" + dni + ")";
    }
}