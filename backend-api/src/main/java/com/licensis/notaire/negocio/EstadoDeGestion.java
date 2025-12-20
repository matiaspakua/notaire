/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.licensis.notaire.negocio;

import com.licensis.notaire.dto.DtoEstadoDeGestion;
import com.licensis.notaire.dto.exceptions.DtoInvalidoException;
import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Version;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

/**
 * Clase que representa un estado de gestion (en un momento del tiempo dado).
 *
 * @author User
 */
@Entity
@Table(name = "estados_de_gestion")
@XmlRootElement
@NamedQueries({
        @NamedQuery(name = "EstadoDeGestion.findAll", query = "SELECT e FROM EstadoDeGestion e"),
        @NamedQuery(name = "EstadoDeGestion.findByIdEstadoGestion", query = "SELECT e FROM EstadoDeGestion e WHERE e.idEstadoGestion = :idEstadoGestion")
})
public class EstadoDeGestion implements Serializable {

    @OneToMany(mappedBy = "fkIdEstadoDeGestion")
    private Collection<GestionDeEscritura> gestionDeEscrituraCollection;
    @Basic(optional = false)
    @Column(name = "version")
    @Version
    private int version;
    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "id_estado_gestion")
    private Integer idEstadoGestion;
    @Basic(optional = false)
    @Lob
    @Column(name = "nombre")
    private String nombre;
    @Lob
    @Column(name = "observaciones")
    private String observaciones;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "fkIdEstadoGestion", fetch = FetchType.EAGER)
    private java.util.Set<Historial> historialList;

    /**
     * Constructor por default para estado de gestion. Asigna al ID el valor de
     * {@link ConstantesNegocio}.ID_OBJETO_NO_VALIDO.
     */
    public EstadoDeGestion() {
        this.idEstadoGestion = ConstantesNegocio.ID_OBJETO_NO_VALIDO;
    }

    public EstadoDeGestion(Integer idEstadoGestion) {
        this.idEstadoGestion = idEstadoGestion;
    }

    public EstadoDeGestion(Integer idEstadoGestion, String nombre) {
        this.idEstadoGestion = idEstadoGestion;
        this.nombre = nombre;
    }

    public Integer getIdEstadoGestion() {
        return idEstadoGestion;
    }

    public void setIdEstadoGestion(Integer idEstadoGestion) {
        this.idEstadoGestion = idEstadoGestion;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }

    @XmlTransient
    public java.util.Set<Historial> getHistorialList() {
        return historialList;
    }

    public void setHistorialList(java.util.Set<Historial> historialList) {
        this.historialList = historialList;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (idEstadoGestion != null ? idEstadoGestion.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof EstadoDeGestion)) {
            return false;
        }
        EstadoDeGestion other = (EstadoDeGestion) object;
        if ((this.idEstadoGestion == null && other.idEstadoGestion != null)
                || (this.idEstadoGestion != null && !this.idEstadoGestion.equals(other.idEstadoGestion))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "EstadoDeGestion[ idEstadoGestion=" + idEstadoGestion + " ]"
                + "[ nombre=" + nombre + " ]";
    }

    public void setAtributo(DtoEstadoDeGestion miDto) throws DtoInvalidoException {
        if (miDto.isValido() == Boolean.TRUE) {
            this.setIdEstadoGestion(miDto.getIdEstadoGestion());
            this.setNombre(miDto.getNombre());
            this.setObservaciones(miDto.getObservaciones());
            this.version = miDto.getVersion();
        } else {
            throw new DtoInvalidoException("Dto invalido");
        }
    }

    public DtoEstadoDeGestion getDto() throws NullPointerException {
        DtoEstadoDeGestion miDto = new DtoEstadoDeGestion();

        miDto.setIdEstadoGestion(this.getIdEstadoGestion());
        miDto.setNombre(this.getNombre());
        miDto.setObservaciones(this.getObservaciones());
        miDto.setVersion(this.getVersion());

        return miDto;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    @XmlTransient
    public Collection<GestionDeEscritura> getGestionDeEscrituraCollection() {
        return gestionDeEscrituraCollection;
    }

    public void setGestionDeEscrituraCollection(Collection<GestionDeEscritura> gestionDeEscrituraCollection) {
        this.gestionDeEscrituraCollection = gestionDeEscrituraCollection;
    }
}
