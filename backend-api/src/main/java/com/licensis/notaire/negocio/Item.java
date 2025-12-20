/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.licensis.notaire.negocio;

import com.licensis.notaire.dto.DtoItem;
import java.io.Serializable;
import jakarta.persistence.Basic;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.NamedQueries;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import jakarta.xml.bind.annotation.XmlRootElement;

/**
 * Clase que presenta los items que pueden ser asociados a los presupuestos.
 *
 * @author juanca
 */
@Entity
@Table(name = "items")
@XmlRootElement
@NamedQueries(
        {
            @NamedQuery(name = "Item.findAll", query = "SELECT i FROM Item i"),
            @NamedQuery(name = "Item.findByIdItem", query = "SELECT i FROM Item i WHERE i.idItem = :idItem"),
            @NamedQuery(name = "Item.findByValor", query = "SELECT i FROM Item i WHERE i.valor = :valor"),
            @NamedQuery(name = "Item.findByPorcentaje", query = "SELECT i FROM Item i WHERE i.porcentaje = :porcentaje"),
            @NamedQuery(name = "Item.findByPresupuesto", query = "SELECT i FROM Item i WHERE i.fkIdPresupuesto.idPresupuesto = :idPresupuesto")
        })
public class Item implements Serializable
{

    @Basic(optional = false)
    @Column(name = "concepto_fijo")
    private boolean conceptoFijo;
    @Basic(optional = false)
    @Column(name = "version")
    @Version
    private int version = 0;
    @Basic(optional = false)
    @Column(name = "valor")
    private float valor;
    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "id_item")
    private Integer idItem;
    @Basic(optional = false)
    @Lob
    @Column(name = "nombre")
    private String nombre;
    @Column(name = "porcentaje")
    private Integer porcentaje;
    @Lob
    @Column(name = "observaciones")
    private String observaciones;
    @JoinColumn(name = "fk_id_presupuesto", referencedColumnName = "id_presupuesto")
    @ManyToOne(fetch = FetchType.EAGER)
    private Presupuesto fkIdPresupuesto;

    /**
     * Constructor por defaul de Item. Inicializa el ID presupuesto segun el campo
     * {@link ConstantesNegocio}.ID_OBJETO_NO_VALIDO.
     */
    public Item()
    {
        this.idItem = ConstantesNegocio.ID_OBJETO_NO_VALIDO;
    }

    public Item(Integer idItem)
    {
        this.idItem = idItem;
    }

    public Item(Integer idItem, String nombre, Float valor)
    {
        this.idItem = idItem;
        this.nombre = nombre;
        this.valor = valor;
    }

    public Integer getIdItem()
    {
        return idItem;
    }

    public void setIdItem(Integer idItem)
    {
        this.idItem = idItem;
    }

    public String getNombre()
    {
        return nombre;
    }

    public void setNombre(String nombre)
    {
        this.nombre = nombre;
    }

    public Integer getPorcentaje()
    {
        return porcentaje;
    }

    public void setPorcentaje(Integer porcentaje)
    {
        this.porcentaje = porcentaje;
    }

    public String getObservaciones()
    {
        return observaciones;
    }

    public void setObservaciones(String observaciones)
    {
        this.observaciones = observaciones;
    }

    public Presupuesto getFkIdPresupuesto()
    {
        return fkIdPresupuesto;
    }

    public void setFkIdPresupuesto(Presupuesto fkIdPresupuesto)
    {
        this.fkIdPresupuesto = fkIdPresupuesto;
    }

    public void setAtributos(DtoItem miDto)
    {
        idItem = miDto.getIdItem();
        nombre = miDto.getNombre();
        valor = miDto.getValor();

        if (miDto.getPorcentaje() != null)
        {
            porcentaje = miDto.getPorcentaje();
        }

        if (miDto.getObservaciones() != null)
        {
            observaciones = miDto.getObservaciones();
        }

        conceptoFijo = miDto.isFijo();

        version = miDto.getVersion();
    }

    public DtoItem getDto()
    {
        DtoItem miDtoItem = new DtoItem();

        miDtoItem.setIdItem(idItem);
        miDtoItem.setNombre(nombre);
        miDtoItem.setObservaciones(observaciones);
        miDtoItem.setPorcentaje(porcentaje);
        miDtoItem.setValor(valor);
        miDtoItem.setVersion(version);
        miDtoItem.setConceptoFijo(conceptoFijo);

        return miDtoItem;
    }

    @Override
    public int hashCode()
    {
        int hash = 0;
        hash += (idItem != null ? idItem.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object)
    {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof Item))
        {
            return false;
        }
        Item other = (Item) object;
        if ((this.idItem == null && other.idItem != null) || (this.idItem != null && !this.idItem.equals(other.idItem)))
        {
            return false;
        }
        return true;
    }

    @Override
    public String toString()
    {
        return "negocio.Item[ idItem=" + idItem + " ]"
                + "[ nombre=" + nombre + " ]";
    }

    public int getVersion()
    {
        return version;
    }

    public void setVersion(int version)
    {
        this.version = version;
    }

    public float getValor()
    {
        return valor;
    }

    public void setValor(float valor)
    {
        this.valor = valor;
    }

    public boolean isFijo()
    {
        return conceptoFijo;
    }

    public void setConceptoFijo(boolean conceptoFijo)
    {
        this.conceptoFijo = conceptoFijo;
    }
}
