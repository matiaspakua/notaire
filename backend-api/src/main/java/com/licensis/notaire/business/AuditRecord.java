/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.licensis.notaire.business;

import com.licensis.notaire.dto.DtoAuditRecord;
import java.io.Serializable;
import org.springframework.data.domain.Persistable;
import java.util.Date;
import jakarta.persistence.Basic;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.NamedQueries;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import jakarta.persistence.Version;
import jakarta.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author juanca
 */
@Entity
@Table(name = "registro_auditoria")
@XmlRootElement
@NamedQueries(
        {
            @NamedQuery(name = "RegistroAuditoria.findAll", query = "SELECT r FROM AuditRecord r"),
            @NamedQuery(name = "RegistroAuditoria.findByIdRegistroAuditoria", query = "SELECT r FROM AuditRecord r WHERE r.idAuditRecord = :idRegistroAuditoria"),
            @NamedQuery(name = "RegistroAuditoria.findByFecha", query = "SELECT r FROM AuditRecord r WHERE r.date = :fecha")
        })
public class AuditRecord implements Serializable, Persistable<Integer>
{

    @Basic(optional = false)
    @Column(name = "fecha")
    @Temporal(TemporalType.TIMESTAMP)
    private Date date;
    @Basic(optional = false)
    @Column(name = "modulo")
    private String module;
    @Basic(optional = false)
    @Column(name = "version")
    @Version
    private int version;
    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "id_registro_auditoria")
    private Integer idAuditRecord;
    @Basic(optional = false)
    @Column(name = "detalle_operacion")
    private String operationDetail;
    @JoinColumn(name = "fk_id_usuario", referencedColumnName = "id_usuario")
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private User fkIdUser;

    public AuditRecord()
    {
    }

    public AuditRecord(Integer idAuditRecord)
    {
        this.idAuditRecord = idAuditRecord;
    }

    public AuditRecord(Integer idAuditRecord, String operationDetail, Date date)
    {
        this.idAuditRecord = idAuditRecord;
        this.operationDetail = operationDetail;
        this.date = date;
    }
    @Override
    @com.fasterxml.jackson.annotation.JsonIgnore
    public Integer getId() {
        return idAuditRecord;
    }

    // Overrides Spring Data's default isNew(), which infers "new" from a primitive
    // @Version field being 0 -- indistinguishable from an already-persisted row that
    // was never updated, causing deleteById()/delete() to silently no-op for it.
    @Override
    @com.fasterxml.jackson.annotation.JsonIgnore
    public boolean isNew() {
        return idAuditRecord == null || idAuditRecord.equals(BusinessConstants.ID_OBJETO_NO_VALIDO);
    }


    public Integer getIdAuditRecord()
    {
        return idAuditRecord;
    }

    public void setIdAuditRecord(Integer idAuditRecord)
    {
        this.idAuditRecord = idAuditRecord;
    }

    public String getOperationDetail()
    {
        return operationDetail;
    }

    public void setOperationDetail(String operationDetail)
    {
        this.operationDetail = operationDetail;
    }

    public User getFkIdUser()
    {
        return fkIdUser;
    }

    public void setFkIdUser(User fkIdUser)
    {
        this.fkIdUser = fkIdUser;
    }

    @Override
    public int hashCode()
    {
        int hash = 0;
        hash += (idAuditRecord != null ? idAuditRecord.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object)
    {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof AuditRecord))
        {
            return false;
        }
        AuditRecord other = (AuditRecord) object;
        if ((this.idAuditRecord == null && other.idAuditRecord != null) || (this.idAuditRecord != null && !this.idAuditRecord.equals(other.idAuditRecord)))
        {
            return false;
        }
        return true;
    }

    @Override
    public String toString()
    {
        return "negocio.RegistroAuditoria[ idRegistroAuditoria=" + idAuditRecord + " ]";
    }

    @com.fasterxml.jackson.annotation.JsonIgnore
    public DtoAuditRecord getDto()
    {

        DtoAuditRecord miDto = new DtoAuditRecord();

        miDto.setOperationDetail(operationDetail);
        miDto.setModule(module);
        miDto.setDate(date);
        miDto.setIdAuditRecord(idAuditRecord);
        miDto.setUsers(this.getFkIdUser().getDto());

        return miDto;
    }

    public int getVersion()
    {
        return version;
    }

    public void setVersion(int version)
    {
        this.version = version;
    }

    public String getModule()
    {
        return module;
    }

    public void setModule(String module)
    {
        this.module = module;
    }

    public void setAtributos(DtoAuditRecord miDto)
    {

        this.setFkIdUser(fkIdUser);
        this.setDate(date);
        this.setModule(module);
        this.setOperationDetail(operationDetail);
        this.setVersion(version);

    }

    public Date getDate()
    {
        return date;
    }

    public void setDate(Date date)
    {
        this.date = date;
    }
}
