package com.licensis.notaire.negocio;

import com.licensis.notaire.dto.DtoCarpetaTramite;
import jakarta.persistence.Basic;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.io.Serializable;

/**
 * Carpeta que agrupa la documentación de un único trámite dentro de una
 * gestión, con ciclo de vida activa/espera/archivada (CU85).
 */
@Entity
@Table(name = "carpetas_tramite")
public class CarpetaTramite implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_carpeta")
    private Integer idCarpeta;

    @Basic(optional = false)
    @Column(name = "numero")
    private int numero;

    @Basic(optional = false)
    @Column(name = "estado")
    private String estado;

    @Column(name = "motivo_espera")
    private String motivoEspera;

    @JoinColumn(name = "fk_id_gestion", referencedColumnName = "id_gestion")
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private GestionDeEscritura fkIdGestion;

    @JoinColumn(name = "fk_id_tramite", referencedColumnName = "id_tramite")
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private Tramite fkIdTramite;

    public Integer getIdCarpeta() {
        return idCarpeta;
    }

    public void setIdCarpeta(Integer idCarpeta) {
        this.idCarpeta = idCarpeta;
    }

    public int getNumero() {
        return numero;
    }

    public void setNumero(int numero) {
        this.numero = numero;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public String getMotivoEspera() {
        return motivoEspera;
    }

    public void setMotivoEspera(String motivoEspera) {
        this.motivoEspera = motivoEspera;
    }

    public GestionDeEscritura getFkIdGestion() {
        return fkIdGestion;
    }

    public void setFkIdGestion(GestionDeEscritura fkIdGestion) {
        this.fkIdGestion = fkIdGestion;
    }

    public Tramite getFkIdTramite() {
        return fkIdTramite;
    }

    public void setFkIdTramite(Tramite fkIdTramite) {
        this.fkIdTramite = fkIdTramite;
    }

    @Override
    public int hashCode() {
        return idCarpeta != null ? idCarpeta.hashCode() : 0;
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof CarpetaTramite other)) {
            return false;
        }
        return idCarpeta != null && idCarpeta.equals(other.idCarpeta);
    }

    @Override
    public String toString() {
        return "CarpetaTramite[ idCarpeta=" + idCarpeta + " ][ numero=" + numero + " ][ estado=" + estado + " ]";
    }

    public DtoCarpetaTramite getDto() {
        return new DtoCarpetaTramite(idCarpeta, numero, estado, motivoEspera,
                fkIdGestion != null ? fkIdGestion.getIdGestion() : null,
                fkIdTramite != null ? fkIdTramite.getIdTramite() : null);
    }
}
