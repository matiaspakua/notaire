/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.licensis.notaire.negocio;

import com.licensis.notaire.dto.DtoTipoDeDocumento;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.persistence.*;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

/**
 *
 * @author User
 */
@Entity
@Table(name = "tipos_de_documento")
@XmlRootElement
@NamedQueries(
{
    @NamedQuery(name = "TipoDeDocumento.findAll", query = "SELECT t FROM TipoDeDocumento t"),
    @NamedQuery(name = "TipoDeDocumento.findByIdTipoDocumento", query = "SELECT t FROM TipoDeDocumento t WHERE t.idTipoDocumento = :idTipoDocumento"),
    @NamedQuery(name = "TipoDeDocumento.findByVence", query = "SELECT t FROM TipoDeDocumento t WHERE t.vence = :vence"),
    @NamedQuery(name = "TipoDeDocumento.findByDiasVencimiento", query = "SELECT t FROM TipoDeDocumento t WHERE t.diasVencimiento = :diasVencimiento"),
    @NamedQuery(name = "TipoDeDocumento.findByNombre", query = "SELECT t FROM TipoDeDocumento t WHERE t.nombre = :nombre")
})
public class TipoDeDocumento implements Serializable
{

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "fkIdTipoDocumento")
    private Collection<DocumentoPresentado> documentoPresentadoCollection;
    @Basic(optional = false)
    @Column(name = "habilitado")
    private boolean habilitado;
    @Basic(optional = false)
    @Column(name = "version")
    @Version
    private int version = 0;
    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "id_tipo_documento")
    private Integer idTipoDocumento;
    @Basic(optional = false)
    @Lob
    @Column(name = "nombre")
    private String nombre;
    @Basic(optional = false)
    @Column(name = "vence")
    private boolean vence;
    @Column(name = "dias_vencimiento")
    private Integer diasVencimiento;
    @Basic(optional = false)
    @Lob
    @Column(name = "quien_entrega")
    private String quienEntrega;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "tipoDeDocumento", fetch = FetchType.EAGER)
    private List<PlantillaTramite> plantillaTramiteList = new ArrayList<>();

    public TipoDeDocumento() {
    }

    public TipoDeDocumento(Integer idTipoDocumento) {
        this.idTipoDocumento = idTipoDocumento;
    }

    public TipoDeDocumento(Integer idTipoDocumento, String nombre, boolean vence, String quienEntrega) {
        this.idTipoDocumento = idTipoDocumento;
        this.nombre = nombre;
        this.vence = vence;
        this.quienEntrega = quienEntrega;
    }

    public Integer getIdTipoDocumento() {
        return idTipoDocumento;
    }

    public void setIdTipoDocumento(Integer idTipoDocumento) {
        this.idTipoDocumento = idTipoDocumento;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public boolean getVence() {
        return vence;
    }

    public void setVence(boolean vence) {
        this.vence = vence;
    }

    public Integer getDiasVencimiento() {
        return diasVencimiento;
    }

    public void setDiasVencimiento(Integer diasVencimiento) {
        this.diasVencimiento = diasVencimiento;
    }

    public String getQuienEntrega() {
        return quienEntrega;
    }

    public void setQuienEntrega(String quienEntrega) {
        this.quienEntrega = quienEntrega;
    }

    @XmlTransient
    public List<PlantillaTramite> getPlantillaTramiteList() {
        return plantillaTramiteList;
    }

    public void setPlantillaTramiteList(List<PlantillaTramite> plantillaTramiteList) {
        this.plantillaTramiteList = plantillaTramiteList;
    }

    public void setAtributos(DtoTipoDeDocumento miDto) {
        if (miDto.getIdTipoDocumento() != null)
        {
            this.idTipoDocumento = miDto.getIdTipoDocumento();
        }

        this.nombre = miDto.getNombre();
        this.vence = miDto.isVence();

        if (this.vence)
        {
            this.diasVencimiento = miDto.getDiasVencimiento();
        }
        else
        {
            this.diasVencimiento = null;
        }

        this.quienEntrega = miDto.getQuienEntrega();
        this.version = miDto.getVersion();

        habilitado = miDto.getHabilitado();
    }

    public DtoTipoDeDocumento getDto() {
        DtoTipoDeDocumento miDto = new DtoTipoDeDocumento();

        miDto.setIdTipoDocumento(this.idTipoDocumento);
        miDto.setNombre(this.nombre);
        miDto.setVence(this.vence);

        if (miDto.isVence())
        {
            miDto.setDiasVencimiento(this.diasVencimiento);
        }

        miDto.setQuienEntrega(this.quienEntrega);

        miDto.setVersion(version);
        miDto.setHabilitado(habilitado);

        return miDto;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (idTipoDocumento != null ? idTipoDocumento.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof TipoDeDocumento))
        {
            return false;
        }
        TipoDeDocumento other = (TipoDeDocumento) object;
        if ((this.idTipoDocumento == null && other.idTipoDocumento != null) || (this.idTipoDocumento != null && !this.idTipoDocumento.equals(other.idTipoDocumento)))
        {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "TipoDeDocumento[ idTipoDocumento=" + idTipoDocumento + " ]"
                 + "[ nombre=" + nombre + " ]";
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public boolean getHabilitado() {
        return habilitado;
    }

    public void setHabilitado(boolean habilitado) {
        this.habilitado = habilitado;
    }

    @XmlTransient
    public Collection<DocumentoPresentado> getDocumentoPresentadoCollection() {
        return documentoPresentadoCollection;
    }

    public void setDocumentoPresentadoCollection(Collection<DocumentoPresentado> documentoPresentadoCollection) {
        this.documentoPresentadoCollection = documentoPresentadoCollection;
    }
}
