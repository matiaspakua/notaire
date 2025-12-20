/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.licensis.notaire.negocio;

import com.licensis.notaire.dto.DtoCopia;
import com.licensis.notaire.dto.DtoTestimonio;
import java.io.Serializable;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import jakarta.persistence.Basic;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.NamedQueries;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import jakarta.persistence.Version;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlTransient;

/**
 *
 * @author juanca
 */
@Entity
@Table(name = "copias")
@XmlRootElement
@NamedQueries(
        {
            @NamedQuery(name = "Copia.findAll", query = "SELECT c FROM Copia c"),
            @NamedQuery(name = "Copia.findByIdCopia", query = "SELECT c FROM Copia c WHERE c.idCopia = :idCopia"),
            @NamedQuery(name = "Copia.findByNumero", query = "SELECT c FROM Copia c WHERE c.numero = :numero"),
            @NamedQuery(name = "Copia.findByFechaImpresion", query = "SELECT c FROM Copia c WHERE c.fechaImpresion = :fechaImpresion"),
            @NamedQuery(name = "Copia.findByTestimonio", query = "SELECT c FROM Copia c WHERE c.fkIdTestimonio.idTestimonio = :idTestimonio"),
            @NamedQuery(name = "Copia.findByFechaRetiro", query = "SELECT c FROM Copia c WHERE c.fechaRetiro = :fechaRetiro")
        })
public class Copia implements Serializable
{

    @Basic(optional = false)
    @Column(name = "fecha_impresion")
    @Temporal(TemporalType.DATE)
    private Date fechaImpresion;
    @Column(name = "fecha_retiro")
    @Temporal(TemporalType.DATE)
    private Date fechaRetiro;
    @Basic(optional = false)
    @Column(name = "version")
    @Version
    private int version = 0;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "copia")
    private Collection<FoliosCopias> foliosCopiasCollection;
    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "id_copia")
    private Integer idCopia;
    @Basic(optional = false)
    @Column(name = "numero")
    private int numero;
    @Lob
    @Column(name = "observaciones")
    private String observaciones;
    @ManyToMany(mappedBy = "copiaList", fetch = FetchType.LAZY)
    private List<Folio> folioList;
    @JoinColumn(name = "fk_id_persona", referencedColumnName = "id_persona")
    @ManyToOne(optional = true, fetch = FetchType.EAGER)
    private Persona fkIdPersona;
    @JoinColumn(name = "fk_id_testimonio", referencedColumnName = "id_testimonio")
    @ManyToOne(optional = true, fetch = FetchType.EAGER)
    private Testimonio fkIdTestimonio;

    public Copia()
    {
    }

    public Copia(Integer idCopia)
    {
        this.idCopia = idCopia;
    }

    public Copia(Integer idCopia, int numero, Date fechaImpresion)
    {
        this.idCopia = idCopia;
        this.numero = numero;
        this.fechaImpresion = fechaImpresion;
    }

    public Integer getIdCopia()
    {
        return idCopia;
    }

    public void setIdCopia(Integer idCopia)
    {
        this.idCopia = idCopia;
    }

    public int getNumero()
    {
        return numero;
    }

    public void setNumero(int numero)
    {
        this.numero = numero;
    }

    public Date getFechaImpresion()
    {
        return fechaImpresion;
    }

    public void setFechaImpresion(Date fechaImpresion)
    {
        this.fechaImpresion = fechaImpresion;
    }

    public Date getFechaRetiro()
    {
        return fechaRetiro;
    }

    public void setFechaRetiro(Date fechaRetiro)
    {
        this.fechaRetiro = fechaRetiro;
    }

    public String getObservaciones()
    {
        return observaciones;
    }

    public void setObservaciones(String observaciones)
    {
        this.observaciones = observaciones;
    }

    @XmlTransient
    public List<Folio> getFolioList()
    {
        return folioList;
    }

    public void setFolioList(List<Folio> folioList)
    {
        this.folioList = folioList;
    }

    public Persona getFkIdPersona()
    {
        return fkIdPersona;
    }

    public void setFkIdPersona(Persona fkIdPersona)
    {
        this.fkIdPersona = fkIdPersona;
    }

    public Testimonio getFkIdTestimonio()
    {
        return fkIdTestimonio;
    }

    public void setFkIdTestimonio(Testimonio fkIdTestimonio)
    {
        this.fkIdTestimonio = fkIdTestimonio;
    }

    @Override
    public int hashCode()
    {
        int hash = 0;
        hash += (idCopia != null ? idCopia.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object)
    {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof Copia))
        {
            return false;
        }
        Copia other = (Copia) object;
        if ((this.idCopia == null && other.idCopia != null) || (this.idCopia != null && !this.idCopia.equals(other.idCopia)))
        {
            return false;
        }
        return true;
    }

    @Override
    public String toString()
    {
        return "Copia[ idCopia=" + idCopia + " ]";
    }

    public int getVersion()
    {
        return version;
    }

    public void setVersion(int version)
    {
        this.version = version;
    }

    @XmlTransient
    public Collection<FoliosCopias> getFoliosCopiasCollection()
    {
        return foliosCopiasCollection;
    }

    public void setFoliosCopiasCollection(Collection<FoliosCopias> foliosCopiasCollection)
    {
        this.foliosCopiasCollection = foliosCopiasCollection;
    }

    public DtoCopia getDto()
    {
        DtoCopia miDto = new DtoCopia();

        miDto.setIdCopia(idCopia);
        miDto.setFechaImpresion(fechaImpresion);
        miDto.setFechaRetiro(fechaRetiro);
        miDto.setNumero(numero);
        miDto.setObservaciones(observaciones);

        if (fkIdTestimonio != null)
        {
            DtoTestimonio miDtoTestimonio;
            miDtoTestimonio = fkIdTestimonio.getDto();
            miDto.setTestimonio(miDtoTestimonio);
        }

        miDto.setVersion(version);

        return miDto;
    }

    public void setAtributos(DtoCopia miDto)
    {

        if (miDto.getIdCopia() != null)
        {
            idCopia = miDto.getIdCopia();
        }

        version = miDto.getVersion();
        fechaImpresion = miDto.getFechaImpresion();
        fechaRetiro = miDto.getFechaRetiro();
        numero = miDto.getNumero();
        observaciones = miDto.getObservaciones();

        if (miDto.getTestimonio() != null)
        {
            fkIdTestimonio = new Testimonio(miDto.getTestimonio().getIdTestimonio());
        }
    }
}
