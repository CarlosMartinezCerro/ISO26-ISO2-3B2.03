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
    public String getIdPaciente() { return idPaciente; }
    public String getTipoPrueba() { return tipoPrueba; }
    public LocalDate getFechaRealizacion() { return fechaRealizacion; }
    public String getEstado() { return estado; }
    public String getInforme() { return informe; }
    public String getRutaImagen() { return rutaImagen; }
}