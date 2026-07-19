/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.licensis.notaire.negocio;

import com.licensis.notaire.dto.DtoItem;
import com.licensis.notaire.dto.DtoPersona;
import com.licensis.notaire.dto.DtoPresupuesto;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
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
 * Clase que representa un presupuesto.
 *
 * @author juanca
 */
@Entity
@Table(name = "presupuestos")
@XmlRootElement
@NamedQueries({
        @NamedQuery(name = "Presupuesto.findAll", query = "SELECT p FROM Presupuesto p"),
        @NamedQuery(name = "Presupuesto.findByIdPresupuesto", query = "SELECT p FROM Presupuesto p WHERE p.idPresupuesto = :idPresupuesto"),
        @NamedQuery(name = "Presupuesto.findByFecha", query = "SELECT p FROM Presupuesto p WHERE p.fecha = :fecha"),
        @NamedQuery(name = "Presupuesto.findByNumero", query = "SELECT p FROM Presupuesto p WHERE p.numero = :numero"),
        @NamedQuery(name = "Presupuesto.findByEstado", query = "SELECT p FROM Presupuesto p WHERE p.estado = :estado"),
        @NamedQuery(name = "Presupuesto.findByPersona", query = "SELECT p FROM Presupuesto p WHERE p.fkIdPersona.idPersona = :idPersona"),
        @NamedQuery(name = "Presupuesto.findByPersonaTramie", query = "SELECT p FROM Presupuesto p WHERE p.fkIdPersona.idPersona = :idPersona AND p.fkIdTramite.idTramite = :fkIdTramite"),
})
public class Presupuesto implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "id_presupuesto")
    private Integer idPresupuesto;
    
    @Basic(optional = false)
    @Column(name = "numero")
    private int numero;
    
    @Basic(optional = false)
    @Column(name = "fecha")
    @Temporal(TemporalType.DATE)
    private Date fecha;
    
    @Basic(optional = false)
    @Column(name = "encabezado")
    private String encabezado;
    
    @Column(name = "observaciones")
    private String observaciones;
    
    @Basic(optional = false)
    @Column(name = "estado")
    private String estado;
    
    @Column(name = "monto_inmueble")
    private Float montoInmueble;
    
    @Basic(optional = false)
    @Column(name = "version")
    @Version
    private int version = 0;
    
    private static final long serialVersionUID = 1L;
    
    @JoinColumn(name = "fk_id_persona", referencedColumnName = "id_persona")
    @ManyToOne(fetch = FetchType.EAGER)
    private Persona fkIdPersona;
    
    @JoinColumn(name = "fk_id_tramite", referencedColumnName = "id_tramite")
    @ManyToOne(fetch = FetchType.EAGER)
    private Tramite fkIdTramite;
    
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "fkIdPresupuesto", fetch = FetchType.EAGER)
    private java.util.Set<Pago> pagoList;
    
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "fkIdPresupuesto", fetch = FetchType.LAZY)
    private List<Tramite> tramiteList;
    
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "fkIdPresupuesto", fetch = FetchType.LAZY)
    private List<Item> itemList = new ArrayList<>();

    /**
     * Constructor por default de presupuesto. Inicializa el ID presupuesto segun el
     * campo
     * {@link ConstantesNegocio}.ID_OBJETO_NO_VALIDO, y todas las listas de objetos.
     */
    public Presupuesto() {
        this.idPresupuesto = ConstantesNegocio.ID_OBJETO_NO_VALIDO;
        this.itemList = new ArrayList<>();
        this.pagoList = new java.util.HashSet<>();
        this.tramiteList = new ArrayList<>();
    }

    public Presupuesto(Integer idPresupuesto) {
        this.idPresupuesto = idPresupuesto;
    }

    public Presupuesto(Integer idPresupuesto, int numero, Date fecha, String encabezado, String estado) {
        this.idPresupuesto = idPresupuesto;
        this.numero = numero;
        this.fecha = fecha;
        this.encabezado = encabezado;
        this.estado = estado;
    }

    public Integer getIdPresupuesto() {
        return idPresupuesto;
    }

    public void setIdPresupuesto(Integer idPresupuesto) {
        this.idPresupuesto = idPresupuesto;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }

    @XmlTransient
    @JsonIgnore
    public java.util.Set<Pago> getPagoList() {
        return pagoList;
    }

    public void setPagoList(java.util.Set<Pago> pagoList) {
        this.pagoList = pagoList;
    }

    public Persona getFkIdPersona() {
        return fkIdPersona;
    }

    public void setFkIdPersona(Persona fkIdPersona) {
        this.fkIdPersona = fkIdPersona;
    }

    public Tramite getFkIdTramite() {
        return fkIdTramite;
    }

    public void setFkIdTramite(Tramite fkIdTramite) {
        this.fkIdTramite = fkIdTramite;
    }

    @XmlTransient
    @JsonIgnore
    public List<Tramite> getTramiteList() {
        return tramiteList;
    }

    public void setTramiteList(List<Tramite> tramiteList) {
        this.tramiteList = tramiteList;
    }

    @XmlTransient
    @JsonIgnore
    public List<Item> getItemList() {
        return itemList;
    }

    public void setItemList(List<Item> itemList) {
        this.itemList = itemList;
    }

    @com.fasterxml.jackson.annotation.JsonIgnore
    public DtoPresupuesto getDto() {
        DtoPresupuesto miDto = new DtoPresupuesto();

        miDto.setIdPresupuesto(idPresupuesto);
        miDto.setFecha(fecha);
        miDto.setNumero(numero);
        miDto.setEncabezado(encabezado);
        miDto.setEstado(estado);
        miDto.setMontoInmueble(montoInmueble);
        miDto.setObservaciones(observaciones);

        if (fkIdTramite != null) {
            miDto.setTramite(fkIdTramite.getDto());
        } else {
            miDto.setTramite(null);
        }

        if (fkIdPersona != null) {
            try {
                miDto.setPersona(fkIdPersona.getDto());
            } catch (Exception ex) {
                DtoPersona personas = new DtoPersona();
                personas.setIdPersona(fkIdPersona.getIdPersona());
                personas.setNombre(fkIdPersona.getNombre());
                personas.setApellido(fkIdPersona.getApellido());
                personas.setDtoTipoIdentificacion(fkIdPersona.getFkIdTipoIdentificacion().getDto());
                personas.setNumeroIdentificacion(fkIdPersona.getNumeroIdentificacion());
            }
        } else {
            miDto.setPersona(null);
        }

        miDto.setVersion(version);

        return miDto;
    }

    public void setAtributos(DtoPresupuesto dtoPresupuesto) {
        this.setIdPresupuesto(dtoPresupuesto.getIdPresupuesto());
        this.setFecha(dtoPresupuesto.getFecha());
        this.setNumero(dtoPresupuesto.getNumero());
        this.setEncabezado(dtoPresupuesto.getEncabezado());
        this.setEstado(dtoPresupuesto.getEstado());
        this.setMontoInmueble(dtoPresupuesto.getMontoInmueble());
        this.setObservaciones(dtoPresupuesto.getObservaciones());

        if (dtoPresupuesto.getPersona() != null) {
            Persona cliente = new Persona();
            cliente.setAtributos(dtoPresupuesto.getPersona());
            this.setFkIdPersona(cliente);
        }

        if (dtoPresupuesto.getItems() != null && !dtoPresupuesto.getItems().isEmpty()) {
            for (Iterator<DtoItem> it = dtoPresupuesto.getItems().iterator(); it.hasNext();) {
                DtoItem dtoItem = it.next();
                Item item = new Item();
                item.setAtributos(dtoItem);
            }
        }

        version = dtoPresupuesto.getVersion();
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (idPresupuesto != null ? idPresupuesto.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof Presupuesto)) {
            return false;
        }
        Presupuesto other = (Presupuesto) object;
        if ((this.idPresupuesto == null && other.idPresupuesto != null)
                || (this.idPresupuesto != null && !this.idPresupuesto.equals(other.idPresupuesto))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "Presupuesto[ idPresupuesto=" + idPresupuesto + " ]";
    }

    public int getNumero() {
        return numero;
    }

    public void setNumero(int numero) {
        this.numero = numero;
    }

    public String getEncabezado() {
        return encabezado;
    }

    public void setEncabezado(String encabezado) {
        this.encabezado = encabezado;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    @JsonProperty("monto")
    public Float getMontoInmueble() {
        return montoInmueble;
    }

    @JsonProperty("monto")
    public void setMontoInmueble(Float montoInmueble) {
        this.montoInmueble = montoInmueble;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public Date getFecha() {
        return fecha;
    }

    public void setFecha(Date fecha) {
        this.fecha = fecha;
    }

    @Deprecated
    public Float getSaldo() {
        return null;
    }

    @Deprecated
    public void setSaldo(Float saldo) {
    }

    @Deprecated
    public Float getTotal() {
        return montoInmueble;
    }

    @Deprecated
    public void setTotal(Float total) {
    }
}
