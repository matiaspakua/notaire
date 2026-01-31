package com.licensis.notaire.dto;

import com.licensis.notaire.dto.interfaces.DtoValido;
import java.util.Date;

public class DtoEscritura implements DtoValido {
    private Integer idEscritura;
    private Integer numero;
    private Date fechaEscrituracion;
    private String cuerpo;
    private String estado;
    private String matriculaInscripcion;
    private Date fechaInscripcion;
    private String observaciones;
    private Integer version;

    public DtoEscritura() {}

    public Integer getIdEscritura() { return idEscritura; }
    public void setIdEscritura(Integer idEscritura) { this.idEscritura = idEscritura; }
    public Integer getNumero() { return numero; }
    public void setNumero(Integer numero) { this.numero = numero; }
    public Date getFechaEscrituracion() { return fechaEscrituracion; }
    public void setFechaEscrituracion(Date fechaEscrituracion) { this.fechaEscrituracion = fechaEscrituracion; }
    public String getCuerpo() { return cuerpo; }
    public void setCuerpo(String cuerpo) { this.cuerpo = cuerpo; }
    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
    public String getMatriculaInscripcion() { return matriculaInscripcion; }
    public void setMatriculaInscripcion(String matriculaInscripcion) { this.matriculaInscripcion = matriculaInscripcion; }
    public Date getFechaInscripcion() { return fechaInscripcion; }
    public void setFechaInscripcion(Date fechaInscripcion) { this.fechaInscripcion = fechaInscripcion; }
    public String getObservaciones() { return observaciones; }
    public void setObservaciones(String observaciones) { this.observaciones = observaciones; }
    public Integer getVersion() { return version; }
    public void setVersion(Integer version) { this.version = version; }
}
