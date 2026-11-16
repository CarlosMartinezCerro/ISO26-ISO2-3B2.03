package es.uclm.esi.iso2.radiodiagnostico.dominio;

import java.io.Serializable;
import java.time.LocalDate;

public class Prueba implements Serializable {
    private static final long serialVersionUID = 1L;

    private String idPrueba;
    private String idPaciente;
    private String tipoPrueba;
    private LocalDate fechaRealizacion;
    private String estado;
    private String informe;
    private String rutaImagen;

    public Prueba(String idPrueba, String idPaciente, String tipoPrueba,
                  LocalDate fechaRealizacion, String estado, String informe, String rutaImagen) {
        this.idPrueba = idPrueba;
        this.idPaciente = idPaciente;
        this.tipoPrueba = tipoPrueba;
        this.fechaRealizacion = fechaRealizacion;
        this.estado = estado;
        this.informe = informe;
        this.rutaImagen = rutaImagen;
    }

    public String getIdPrueba() { return idPrueba; }
    public void setIdPrueba(String idPrueba) { this.idPrueba = idPrueba; }

    public String getIdPaciente() { return idPaciente; }
    public void setIdPaciente(String idPaciente) { this.idPaciente = idPaciente; }

    public String getTipoPrueba() { return tipoPrueba; }
    public void setTipoPrueba(String tipoPrueba) { this.tipoPrueba = tipoPrueba; }

    public LocalDate getFechaRealizacion() { return fechaRealizacion; }
    public void setFechaRealizacion(LocalDate fechaRealizacion) { this.fechaRealizacion = fechaRealizacion; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public String getInforme() { return informe; }
    public void setInforme(String informe) { this.informe = informe; }

    public String getRutaImagen() { return rutaImagen; }
    public void setRutaImagen(String rutaImagen) { this.rutaImagen = rutaImagen; }

    @Override
    public String toString() {
        return "[" + fechaRealizacion + "] " + tipoPrueba + " (" + estado + ") - Informe: " + informe;
    }
}
