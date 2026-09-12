package com.licensis.notaire.business;

import com.licensis.notaire.dto.DtoProcedureFolder;
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
public class ProcedureFolder implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_carpeta")
    private Integer idFolder;

    @Basic(optional = false)
    @Column(name = "numero")
    private int number;

    @Basic(optional = false)
    @Column(name = "estado")
    private String status;

    @Column(name = "motivo_espera")
    private String waitReason;

    @JoinColumn(name = "fk_id_gestion", referencedColumnName = "id_gestion")
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private DeedManagement fkIdManagement;

    @JoinColumn(name = "fk_id_tramite", referencedColumnName = "id_tramite")
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private Procedure fkIdProcedure;

    public Integer getIdFolder() {
        return idFolder;
    }

    public void setIdFolder(Integer idFolder) {
        this.idFolder = idFolder;
    }

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getWaitReason() {
        return waitReason;
    }

    public void setWaitReason(String waitReason) {
        this.waitReason = waitReason;
    }

    public DeedManagement getFkIdManagement() {
        return fkIdManagement;
    }

    public void setFkIdManagement(DeedManagement fkIdManagement) {
        this.fkIdManagement = fkIdManagement;
    }

    public Procedure getFkIdProcedure() {
        return fkIdProcedure;
    }

    public void setFkIdProcedure(Procedure fkIdProcedure) {
        this.fkIdProcedure = fkIdProcedure;
    }

    @Override
    public int hashCode() {
        return idFolder != null ? idFolder.hashCode() : 0;
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof ProcedureFolder other)) {
            return false;
        }
        return idFolder != null && idFolder.equals(other.idFolder);
    }

    @Override
    public String toString() {
        return "CarpetaTramite[ idCarpeta=" + idFolder + " ][ numero=" + number + " ][ estado=" + status + " ]";
    }

    public DtoProcedureFolder getDto() {
        return new DtoProcedureFolder(idFolder, number, status, waitReason,
                fkIdManagement != null ? fkIdManagement.getIdManagement() : null,
                fkIdProcedure != null ? fkIdProcedure.getIdProcedure() : null);
    }
}
