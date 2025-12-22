/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.licensis.notaire.negocio;

import com.licensis.notaire.dto.DtoItem;
import com.licensis.notaire.dto.DtoPersona;
import com.licensis.notaire.dto.DtoPresupuesto;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import jakarta.persistence.*;
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
        @NamedQuery(name = "Presupuesto.findByTotal", query = "SELECT p FROM Presupuesto p WHERE p.total = :total"),
        @NamedQuery(name = "Presupuesto.findBySaldo", query = "SELECT p FROM Presupuesto p WHERE p.saldo = :saldo"),
        @NamedQuery(name = "Presupuesto.findByPersona", query = "SELECT p FROM Presupuesto p WHERE p.fkIdPersona.idPersona = :idPersona"),
        @NamedQuery(name = "Presupuesto.findByPersonaTramie", query = "SELECT p FROM Presupuesto p WHERE p.fkIdPersona.idPersona = :idPersona AND p.fkIdTramite.idTramite = :fkIdTramite"),
})
public class Presupuesto implements Serializable {

    @Basic(optional = false)
    @Column(name = "fecha")
    @Temporal(TemporalType.DATE)
    private Date fecha;
    @Basic(optional = false)
    @Column(name = "version")
    @Version
    private int version = 0;
    @Basic(optional = false)
    @Column(name = "total")
    private float total;
    @Basic(optional = false)
    @Column(name = "saldo")
    private float saldo;
    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "id_presupuesto")
    private Integer idPresupuesto;
    @Lob
    @Column(name = "observaciones")
    private String observaciones;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "fkIdPresupuesto", fetch = FetchType.EAGER)
    private java.util.Set<Pago> pagoList;
    @JoinColumn(name = "fk_id_persona", referencedColumnName = "id_persona")
    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    private Persona fkIdPersona;
    @JoinColumn(name = "fk_id_tramite", referencedColumnName = "id_tramite")
    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    private Tramite fkIdTramite;
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

    public Presupuesto(Integer idPresupuesto, Date fecha, Float total, Float saldo) {
        this.idPresupuesto = idPresupuesto;
        this.fecha = fecha;
        this.total = total;
        this.saldo = saldo;
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
    public List<Tramite> getTramiteList() {
        return tramiteList;
    }

    public void setTramiteList(List<Tramite> tramiteList) {
        this.tramiteList = tramiteList;
    }

    @XmlTransient
    public List<Item> getItemList() {
        return itemList;
    }

    public void setItemList(List<Item> itemList) {
        this.itemList = itemList;
    }

    public DtoPresupuesto getDto() {
        DtoPresupuesto miDto = new DtoPresupuesto();

        miDto.setIdPresupuesto(idPresupuesto);
        miDto.setFecha(fecha);
        miDto.setSaldo(saldo);
        miDto.setTotal(total);
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
        this.setObservaciones(dtoPresupuesto.getObservaciones());
        this.setSaldo(dtoPresupuesto.getSaldo());
        this.setTotal(dtoPresupuesto.getTotal());

        Persona cliente = new Persona();
        cliente.setAtributos(dtoPresupuesto.getPersona());
        this.setFkIdPersona(cliente);

        if (!dtoPresupuesto.getItems().isEmpty()) {
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

    public float getTotal() {
        return total;
    }

    public void setTotal(float total) {
        this.total = total;
    }

    public float getSaldo() {
        return saldo;
    }

    public void setSaldo(float saldo) {
        this.saldo = saldo;
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
}
