package es.uclm.esi.iso2.radiodiagnostico.usuario;

import java.time.LocalDate;

public class Paciente {

    private String nombre;
    private String apellido;
    private String dni;
    private String numeroTelefono;
    private String numeroSeguridadSocial;
    private LocalDate fechaNacimiento;
    private String domicilio;

    public Paciente(String nombre, String apellido, String dni, String numeroTelefono,
                    String numeroSeguridadSocial, LocalDate fechaNacimiento,
                    String domicilio) {
        this.nombre = nombre;
        this.apellido = apellido;
        this.dni = dni;
        this.numeroTelefono = numeroTelefono;
        this.numeroSeguridadSocial = numeroSeguridadSocial;
        this.fechaNacimiento = fechaNacimiento;
        this.domicilio = domicilio;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getApellido() {
        return apellido;
    }

    public void setApellido(String apellido) {
        this.apellido = apellido;
    }

    public String getDni() {
        return dni;
    }

    public void setDni(String dni) {
        this.dni = dni;
    }

    public String getNumeroTelefono() {
        return numeroTelefono;
    }

    public void setNumeroTelefono(String numeroTelefono) {
        this.numeroTelefono = numeroTelefono;
    }

    public String getNumeroSeguridadSocial() {
        return numeroSeguridadSocial;
    }

    public void setNumeroSeguridadSocial(String numeroSeguridadSocial) {
        this.numeroSeguridadSocial = numeroSeguridadSocial;
    }

    public LocalDate getFechaNacimiento() {
        return fechaNacimiento;
    }

    public void setFechaNacimiento(LocalDate fechaNacimiento) {
        this.fechaNacimiento = fechaNacimiento;
    }

    public String getDomicilio() {
        return domicilio;
    }

    public void setDomicilio(String domicilio) {
        this.domicilio = domicilio;
    }
}
