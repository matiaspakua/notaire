/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.licensis.notaire.negocio;

import com.licensis.notaire.dto.DtoEscritura;
import com.licensis.notaire.dto.DtoFolio;
import com.licensis.notaire.dto.DtoTramite;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import javax.persistence.*;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

/**
 *
 * @author juanca
 */
@Entity
@Table(name = "escrituras")
@XmlRootElement
@NamedQueries(
{
    @NamedQuery(name = "Escritura.findAll", query = "SELECT e FROM Escritura e"),
    @NamedQuery(name = "Escritura.findByIdEscritura", query = "SELECT e FROM Escritura e WHERE e.idEscritura = :idEscritura"),
    @NamedQuery(name = "Escritura.findByNumero", query = "SELECT e FROM Escritura e WHERE e.numero = :numero"),
    @NamedQuery(name = "Escritura.findByFechaEscrituracion", query = "SELECT e FROM Escritura e WHERE e.fechaEscrituracion = :fechaEscrituracion"),
    @NamedQuery(name = "Escritura.findByFechaInscripcion", query = "SELECT e FROM Escritura e WHERE e.fechaInscripcion = :fechaInscripcion")
})
public class Escritura implements Serializable
{

    @Basic(optional = false)
    @Column(name = "fecha_escrituracion")
    @Temporal(TemporalType.DATE)
    private Date fechaEscrituracion;
    @Column(name = "fecha_inscripcion")
    @Temporal(TemporalType.DATE)
    private Date fechaInscripcion;
    @Basic(optional = false)
    @Column(name = "version")
    @Version
    private int version;
    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "id_escritura")
    private Integer idEscritura;
    @Basic(optional = false)
    @Column(name = "numero")
    private int numero;
    @Basic(optional = false)
    @Lob
    @Column(name = "cuerpo")
    private String cuerpo;
    @Basic(optional = false)
    @Lob
    @Column(name = "estado")
    private String estado = ConstantesNegocio.ESCRITURA_SIN_FIRMAR;
    @Lob
    @Column(name = "matricula_inscripcion")
    private String matriculaInscripcion;
    @Lob
    @Column(name = "observaciones")
    private String observaciones;
    @OneToMany(mappedBy = "fkIdEscritura", fetch = FetchType.LAZY)
    private List<Folio> folioList = null;
    @OneToMany(mappedBy = "fkIdEscritura", fetch = FetchType.LAZY)
    private List<Tramite> tramiteList = null;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "fkIdEscritura", fetch = FetchType.LAZY)
    private List<Testimonio> testimonioList = null;

    public Escritura() {
    }

    public Escritura(Integer idEscritura) {
        this.idEscritura = idEscritura;
    }

    public Escritura(Integer idEscritura, int numero, Date fechaEscrituracion, String cuerpo, String estado) {
        this.idEscritura = idEscritura;
        this.numero = numero;
        this.fechaEscrituracion = fechaEscrituracion;
        this.cuerpo = cuerpo;
        this.estado = estado;
    }

    public Integer getIdEscritura() {
        return idEscritura;
    }

    public void setIdEscritura(Integer idEscritura) {
        this.idEscritura = idEscritura;
    }

    public int getNumero() {
        return numero;
    }

    public void setNumero(int numero) {
        this.numero = numero;
    }

    public Date getFechaEscrituracion() {
        return fechaEscrituracion;
    }

    public void setFechaEscrituracion(Date fechaEscrituracion) {
        this.fechaEscrituracion = fechaEscrituracion;
    }

    public String getCuerpo() {
        return cuerpo;
    }

    public void setCuerpo(String cuerpo) {
        this.cuerpo = cuerpo;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public String getMatriculaInscripcion() {
        return matriculaInscripcion;
    }

    public void setMatriculaInscripcion(String matriculaInscripcion) {
        this.matriculaInscripcion = matriculaInscripcion;
    }

    public Date getFechaInscripcion() {
        return fechaInscripcion;
    }

    public void setFechaInscripcion(Date fechaInscripcion) {
        this.fechaInscripcion = fechaInscripcion;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }

    @XmlTransient
    public List<Folio> getFolioList() {
        return folioList;
    }

    public void setFolioList(List<Folio> folioList) {
        this.folioList = folioList;
    }

    @XmlTransient
    public List<Tramite> getTramiteList() {
        return tramiteList;
    }

    public void setTramiteList(List<Tramite> tramiteList) {
        this.tramiteList = tramiteList;
    }

    @XmlTransient
    public List<Testimonio> getTestimonioList() {
        return testimonioList;
    }

    public void setTestimonioList(List<Testimonio> testimonioList) {
        this.testimonioList = testimonioList;
    }

    public void setAtributos(DtoEscritura miDtoEscritura) {
        if (miDtoEscritura != null)
        {
            if (idEscritura != null)
            {
                idEscritura = miDtoEscritura.getIdEscritura();
            }

            numero = miDtoEscritura.getNumero();
            fechaEscrituracion = miDtoEscritura.getFechaEscrituracion();

            if (miDtoEscritura.getFolios() != null)
            {
                folioList = new ArrayList<>();
                for (Iterator<DtoFolio> it = miDtoEscritura.getFolios().iterator(); it.hasNext();)
                {
                    DtoFolio dtoFolio = it.next();
                    Folio miFolio = new Folio();

                    miFolio.setAtributos(dtoFolio);

                    folioList.add(miFolio);
                }
            }

            if (miDtoEscritura.getTramites() != null)
            {
                tramiteList = new ArrayList<>();
                for (Iterator<DtoTramite> it = miDtoEscritura.getTramites().iterator(); it.hasNext();)
                {
                    DtoTramite dtoTramite = it.next();
                    Tramite miTramite = new Tramite();

                    miTramite.setIdTramite(dtoTramite.getIdTramite());
                    tramiteList.add(miTramite);
                }
            }
            cuerpo = miDtoEscritura.getCuerpo();
            estado = miDtoEscritura.getEstado();
            version = miDtoEscritura.getVersion();
        }
    }

    public DtoEscritura getDto() {
        DtoEscritura miDtoEscritura = new DtoEscritura();

        miDtoEscritura.setIdEscritura(idEscritura);
        miDtoEscritura.setFechaEscrituracion(fechaEscrituracion);
        miDtoEscritura.setNumero(numero);
        miDtoEscritura.setCuerpo(cuerpo);
        miDtoEscritura.setEstado(estado);
        miDtoEscritura.setMatriculaInscripcion(matriculaInscripcion);
        miDtoEscritura.setFechaInscripcion(fechaInscripcion);
        
        miDtoEscritura.setVersion(version);

        if (folioList != null && !folioList.isEmpty())
        {

            for (Iterator<Folio> it = folioList.iterator(); it.hasNext();)
            {
                Folio folio = it.next();
                DtoFolio miDtoFolio = folio.getDto();

                miDtoEscritura.getFolios().add(miDtoFolio);
            }
        }
        else
        {
            miDtoEscritura.setFolios(null);
        }

        if (tramiteList != null && !tramiteList.isEmpty())
        {
            for (Iterator<Tramite> it = tramiteList.iterator(); it.hasNext();)
            {
                Tramite tramite = it.next();
                DtoTramite miDtoTramite = tramite.getDto();

                miDtoEscritura.getTramites().add(miDtoTramite);
            }
        }
        else
        {
            miDtoEscritura.setTramites(null);
        }

        return miDtoEscritura;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (idEscritura != null ? idEscritura.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof Escritura))
        {
            return false;
        }
        Escritura other = (Escritura) object;
        if ((this.idEscritura == null && other.idEscritura != null) || (this.idEscritura != null && !this.idEscritura.equals(other.idEscritura)))
        {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "Escritura[ idEscritura=" + idEscritura + " ]"
                 + "[ numero=" + numero 
                 + ", fecha escrituracion: " + this.fechaEscrituracion
                 + ", fecha inscripcion: " + this.fechaInscripcion                 
                 + ", Estado: " + this.estado + " ]";
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }
}
