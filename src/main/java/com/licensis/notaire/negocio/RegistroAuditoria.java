/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.licensis.notaire.negocio;

import com.licensis.notaire.dto.DtoRegistroAuditoria;
import com.licensis.notaire.dto.DtoUsuario;
import java.io.Serializable;
import java.util.Date;
import javax.persistence.*;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author juanca
 */
@Entity
@Table(name = "registro_auditoria")
@XmlRootElement
@NamedQueries(
{
    @NamedQuery(name = "RegistroAuditoria.findAll", query = "SELECT r FROM RegistroAuditoria r"),
    @NamedQuery(name = "RegistroAuditoria.findByIdRegistroAuditoria", query = "SELECT r FROM RegistroAuditoria r WHERE r.idRegistroAuditoria = :idRegistroAuditoria"),
    @NamedQuery(name = "RegistroAuditoria.findByFecha", query = "SELECT r FROM RegistroAuditoria r WHERE r.fecha = :fecha")
})
public class RegistroAuditoria implements Serializable
{

    @Basic(optional = false)
    @Column(name = "fecha")
    @Temporal(TemporalType.TIMESTAMP)
    private Date fecha;
    @Basic(optional = false)
    @Column(name = "modulo")
    private String modulo;
    @Basic(optional = false)
    @Column(name = "version")
    @Version
    private int version;
    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "id_registro_auditoria")
    private Integer idRegistroAuditoria;
    @Basic(optional = false)
    @Lob
    @Column(name = "detalle_operacion")
    private String detalleOperacion;
    @JoinColumn(name = "fk_id_usuario", referencedColumnName = "id_usuario")
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private Usuario fkIdUsuario;

    public RegistroAuditoria() {
    }

    public RegistroAuditoria(Integer idRegistroAuditoria) {
        this.idRegistroAuditoria = idRegistroAuditoria;
    }

    public RegistroAuditoria(Integer idRegistroAuditoria, String detalleOperacion, Date fecha) {
        this.idRegistroAuditoria = idRegistroAuditoria;
        this.detalleOperacion = detalleOperacion;
        this.fecha = fecha;
    }

    public Integer getIdRegistroAuditoria() {
        return idRegistroAuditoria;
    }

    public void setIdRegistroAuditoria(Integer idRegistroAuditoria) {
        this.idRegistroAuditoria = idRegistroAuditoria;
    }

    public String getDetalleOperacion() {
        return detalleOperacion;
    }

    public void setDetalleOperacion(String detalleOperacion) {
        this.detalleOperacion = detalleOperacion;
    }

    public Usuario getFkIdUsuario() {
        return fkIdUsuario;
    }

    public void setFkIdUsuario(Usuario fkIdUsuario) {
        this.fkIdUsuario = fkIdUsuario;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (idRegistroAuditoria != null ? idRegistroAuditoria.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof RegistroAuditoria))
        {
            return false;
        }
        RegistroAuditoria other = (RegistroAuditoria) object;
        if ((this.idRegistroAuditoria == null && other.idRegistroAuditoria != null) || (this.idRegistroAuditoria != null && !this.idRegistroAuditoria.equals(other.idRegistroAuditoria)))
        {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "negocio.RegistroAuditoria[ idRegistroAuditoria=" + idRegistroAuditoria + " ]";
    }

    public DtoRegistroAuditoria getDto() {

        DtoRegistroAuditoria miDto = new DtoRegistroAuditoria();

        miDto.setDetalleOperacion(detalleOperacion);
        miDto.setModulo(modulo);
        miDto.setFecha(fecha);
        miDto.setIdRegistroAuditoria(idRegistroAuditoria);
        miDto.setUsuarios(this.getFkIdUsuario().getDto());

        return miDto;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public String getModulo() {
        return modulo;
    }

    public void setModulo(String modulo) {
        this.modulo = modulo;
    }

    public void setAtributos(DtoRegistroAuditoria miDto) {

        this.setFkIdUsuario(fkIdUsuario);
        this.setFecha(fecha);
        this.setModulo(modulo);
        this.setDetalleOperacion(detalleOperacion);
        this.setVersion(version);

    }

    public Date getFecha() {
        return fecha;
    }

    public void setFecha(Date fecha) {
        this.fecha = fecha;
    }
}
