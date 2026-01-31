package com.licensis.notaire.dto;

import com.licensis.notaire.dto.interfaces.DtoValido;
import java.util.List;

public class DtoTipoDeDocumento implements DtoValido {
    private Integer idTipoDocumento;
    private String nombre;
    private Boolean vence;
    private Integer diasVencimiento;
    private String quienEntrega;
    private Integer version = 0;
    private Boolean habilitado;
    private List<DtoPlantillaTramite> plantillaTramites;

    public DtoTipoDeDocumento() {}
    public DtoTipoDeDocumento(String nombre, boolean vence, String quienEntrega) {
        this.nombre = nombre;
        this.vence = vence;
        this.quienEntrega = quienEntrega;
    }

    public Integer getIdTipoDocumento() { return idTipoDocumento; }
    public void setIdTipoDocumento(Integer idTipoDocumento) { this.idTipoDocumento = idTipoDocumento; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public Boolean getVence() { return vence; }
    public void setVence(Boolean vence) { this.vence = vence; }
    public Integer getDiasVencimiento() { return diasVencimiento; }
    public void setDiasVencimiento(Integer diasVencimiento) { this.diasVencimiento = diasVencimiento; }
    public String getQuienEntrega() { return quienEntrega; }
    public void setQuienEntrega(String quienEntrega) { this.quienEntrega = quienEntrega; }
    public Integer getVersion() { return version; }
    public void setVersion(Integer version) { this.version = version; }
    public Boolean getHabilitado() { return habilitado; }
    public void setHabilitado(Boolean habilitado) { this.habilitado = habilitado; }
    public List<DtoPlantillaTramite> getPlantillaTramites() { return plantillaTramites; }
    public void setPlantillaTramites(List<DtoPlantillaTramite> plantillaTramites) { this.plantillaTramites = plantillaTramites; }
}
