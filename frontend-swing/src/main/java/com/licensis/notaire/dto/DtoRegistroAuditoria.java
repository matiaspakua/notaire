package com.licensis.notaire.dto;

import java.util.Date;

/**
 * DTO para registro de auditoría (stub frontend).
 */
public class DtoRegistroAuditoria {
    private Integer idRegistro;
    private Date fecha;
    private String accion;
    private Integer fkIdUsuario;
    private Integer version;

    public DtoRegistroAuditoria() {}

    public Integer getIdRegistro() { return idRegistro; }
    public void setIdRegistro(Integer idRegistro) { this.idRegistro = idRegistro; }
    public Date getFecha() { return fecha; }
    public void setFecha(Date fecha) { this.fecha = fecha; }
    public String getAccion() { return accion; }
    public void setAccion(String accion) { this.accion = accion; }
    public Integer getFkIdUsuario() { return fkIdUsuario; }
    public void setFkIdUsuario(Integer fkIdUsuario) { this.fkIdUsuario = fkIdUsuario; }
    public Integer getVersion() { return version; }
    public void setVersion(Integer version) { this.version = version; }
}
