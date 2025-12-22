/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.licensis.notaire.negocio;

import com.licensis.notaire.dto.DtoFolio;
import com.licensis.notaire.dto.DtoPersona;
import com.licensis.notaire.jpa.ConstantesPersistencia;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
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
import jakarta.persistence.JoinTable;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.NamedQueries;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlTransient;

/**
 * <p>
 * REGLA DE NEGOCIO:
 * <p>
 *
 * <lo> <li> Estados posibles de un Folio: -Nuevo -Utilizado -Errose </li> </lo>
 *
 *
 * @author Tefi
 */
@Entity
@Table(name = "folios")
@XmlRootElement
@NamedQueries(
        {
            @NamedQuery(name = "Folio.findAll", query = "SELECT f FROM Folio f"),
            @NamedQuery(name = "Folio.findByIdFolio", query = "SELECT f FROM Folio f WHERE f.idFolio = :idFolio"),
            @NamedQuery(name = "Folio.findByNumero", query = "SELECT f FROM Folio f WHERE f.numero = :numero"),
            @NamedQuery(name = "Folio.findByAnio", query = "SELECT f FROM Folio f WHERE f.anio = :anio"),
            @NamedQuery(name = "Folio.findByAnioAndRegistro", query = "SELECT f FROM Folio f WHERE f.anio = :anio AND f.fkIdPersonaEscribano.registroEscribano =:registro")
        })
public class Folio implements Serializable
{

    @Basic(optional = false)
    @Column(name = "version")
    @Version
    private int version = ConstantesPersistencia.VERSION_INICIAL;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "folio")
    private Collection<FoliosCopias> foliosCopiasCollection;
    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "id_folio")
    private Integer idFolio;
    @Basic(optional = false)
    @Column(name = "numero")
    private int numero;
    @Basic(optional = false)
    @Column(name = "anio")
    private int anio;
    @Basic(optional = false)
    @Lob
    @Column(name = "estado")
    private String estado;
    @Lob
    @Column(name = "observaciones")
    private String observaciones;
    @JoinTable(name = "folios_copias", joinColumns =
    {
        @JoinColumn(name = "fk_id_folio", referencedColumnName = "id_folio")
    }, inverseJoinColumns =
    {
        @JoinColumn(name = "fk_id_copia", referencedColumnName = "id_copia")
    })
    @ManyToMany(fetch = FetchType.LAZY)
    private List<Copia> copiaList;
    @JoinColumn(name = "fk_id_persona_escribano", referencedColumnName = "id_persona")
    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    private Persona fkIdPersonaEscribano;
    @JoinColumn(name = "fk_id_tipo_folio", referencedColumnName = "id_tipo_folio")
    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    private TipoDeFolio fkIdTipoFolio;
    @JoinColumn(name = "fk_id_escritura", referencedColumnName = "id_escritura")
    @ManyToOne(fetch = FetchType.LAZY)
    private Escritura fkIdEscritura;

    public Folio()
    {
        this.copiaList = new ArrayList<>();
        this.foliosCopiasCollection = new ArrayList<>();

    }

    public Folio(Integer idFolio)
    {
        this.idFolio = idFolio;
    }

    public Folio(Integer idFolio, int numero, int anio, String estado)
    {
        this.idFolio = idFolio;
        this.numero = numero;
        this.anio = anio;
        this.estado = estado;
    }

    public Integer getIdFolio()
    {
        return idFolio;
    }

    public void setIdFolio(Integer idFolio)
    {
        this.idFolio = idFolio;
    }

    public int getNumero()
    {
        return numero;
    }

    public void setNumero(int numero)
    {
        this.numero = numero;
    }

    public int getAnio()
    {
        return anio;
    }

    public void setAnio(int anio)
    {
        this.anio = anio;
    }

    public String getEstado()
    {
        return estado;
    }

    public void setEstado(String estado)
    {
        this.estado = estado;
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
    public List<Copia> getCopiaList()
    {
        return copiaList;
    }

    public void setCopiaList(List<Copia> copiaList)
    {
        this.copiaList = copiaList;
    }

    public Persona getFkIdPersonaEscribano()
    {
        return fkIdPersonaEscribano;
    }

    public void setFkIdPersonaEscribano(Persona fkIdPersonaEscribano)
    {
        this.fkIdPersonaEscribano = fkIdPersonaEscribano;
    }

    public TipoDeFolio getFkIdTipoFolio()
    {
        return fkIdTipoFolio;
    }

    public void setFkIdTipoFolio(TipoDeFolio fkIdTipoFolio)
    {
        this.fkIdTipoFolio = fkIdTipoFolio;
    }

    public Escritura getFkIdEscritura()
    {
        return fkIdEscritura;
    }

    public void setFkIdEscritura(Escritura fkIdEscritura)
    {
        this.fkIdEscritura = fkIdEscritura;
    }

    @Override
    public int hashCode()
    {
        int hash = 0;
        hash += (idFolio != null ? idFolio.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object)
    {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof Folio))
        {
            return false;
        }
        Folio other = (Folio) object;
        if ((this.idFolio == null && other.idFolio != null) || (this.idFolio != null && !this.idFolio.equals(other.idFolio)))
        {
            return false;
        }
        return true;
    }

    @Override
    public String toString()
    {
        return "Folio[ idFolio=" + idFolio + " ]"
                + "[ numero=" + numero + " ]"
                + "[ anio=" + anio + " ]";
    }

    public void setAtributos(DtoFolio unDtoFolio)
    {
        if (unDtoFolio.isValido())
        {
            this.setIdFolio(unDtoFolio.getIdFolio());
            this.setNumero(unDtoFolio.getNumero());
            this.setAnio(unDtoFolio.getAnio());
            this.setEstado(unDtoFolio.getEstado());
            this.setObservaciones(unDtoFolio.getObservaciones());
            this.setVersion(unDtoFolio.getVersion());
        }
    }

    public DtoFolio getDto()
    {
        DtoFolio miDtoFolio = new DtoFolio();

        miDtoFolio.setIdFolio(this.idFolio);
        miDtoFolio.setNumero(this.numero);
        miDtoFolio.setEstado(this.estado);
        miDtoFolio.setObservaciones(this.observaciones);
        miDtoFolio.setAnio(this.anio);
        miDtoFolio.setVersion(this.getVersion());

        if (this.getFkIdPersonaEscribano() != null)
        {
            DtoPersona miPersona = new DtoPersona();
            miPersona.setIdPersona(fkIdPersonaEscribano.getIdPersona());
            miPersona.setRegistroEscribano(fkIdPersonaEscribano.getRegistroEscribano());

            miDtoFolio.setPersonaEscribano(miPersona);
        }

        miDtoFolio.setTiposDeFolio(this.fkIdTipoFolio.getDto());

        return miDtoFolio;
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
}
