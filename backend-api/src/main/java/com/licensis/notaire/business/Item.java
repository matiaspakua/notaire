/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.licensis.notaire.business;

import com.licensis.notaire.dto.DtoItem;
import com.licensis.notaire.dto.TypeItem;
import java.io.Serializable;
import jakarta.persistence.Basic;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.NamedQueries;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.xml.bind.annotation.XmlRootElement;
import org.springframework.data.domain.Persistable;

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
            @NamedQuery(name = "Item.findByValor", query = "SELECT i FROM Item i WHERE i.value = :valor"),
            @NamedQuery(name = "Item.findByPorcentaje", query = "SELECT i FROM Item i WHERE i.percentage = :porcentaje"),
            @NamedQuery(name = "Item.findByPresupuesto", query = "SELECT i FROM Item i WHERE i.fkIdBudget.idBudget = :idPresupuesto")
        })
public class Item implements Serializable, Persistable<Integer>
{

    @Basic(optional = false)
    @Column(name = "concepto_fijo")
    private boolean fixedConcept;
    @Basic(optional = false)
    @Column(name = "version")
    @Version
    private int version = 0;
    @Basic(optional = false)
    @Column(name = "valor")
    private float value;
    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "id_item")
    private Integer idItem;
    @Basic(optional = false)
    @Column(name = "nombre")
    private String name;
    @Column(name = "porcentaje")
    private Integer percentage;
    @Column(name = "observaciones")
    private String notes;
    @Column(name = "tipo")
    @Enumerated(EnumType.STRING)
    private TypeItem type = TypeItem.NORMAL;
    @Column(name = "motivo")
    private String reason;
    @JoinColumn(name = "fk_id_presupuesto", referencedColumnName = "id_presupuesto")
    @ManyToOne(fetch = FetchType.EAGER)
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private Budget fkIdBudget;

    /**
     * Constructor por defaul de Item. Inicializa el ID presupuesto segun el campo
     * {@link ConstantesNegocio}.ID_OBJETO_NO_VALIDO.
     */
    public Item()
    {
        this.idItem = BusinessConstants.ID_OBJETO_NO_VALIDO;
    }

    public Item(Integer idItem)
    {
        this.idItem = idItem;
    }

    public Item(Integer idItem, String name, Float value)
    {
        this.idItem = idItem;
        this.name = name;
        this.value = value;
    }

    public Integer getIdItem()
    {
        return idItem;
    }

    public void setIdItem(Integer idItem)
    {
        this.idItem = idItem;
    }

    @Override
    public Integer getId()
    {
        return idItem;
    }

    // Overrides Spring Data's default isNew(), which infers "new" from a primitive
    // @Version field being 0 — indistinguishable from an already-persisted row that
    // was never updated, causing deleteById()/delete() to silently no-op for it.
    @Override
    @com.fasterxml.jackson.annotation.JsonIgnore
    public boolean isNew()
    {
        return idItem == null || idItem.equals(BusinessConstants.ID_OBJETO_NO_VALIDO);
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public Integer getPercentage()
    {
        return percentage;
    }

    public void setPercentage(Integer percentage)
    {
        this.percentage = percentage;
    }

    public String getNotes()
    {
        return notes;
    }

    public void setNotes(String notes)
    {
        this.notes = notes;
    }

    public Budget getFkIdBudget()
    {
        return fkIdBudget;
    }

    public void setFkIdBudget(Budget fkIdBudget)
    {
        this.fkIdBudget = fkIdBudget;
    }

    public TypeItem getType()
    {
        return type;
    }

    public void setType(TypeItem type)
    {
        this.type = type;
    }

    public String getReason()
    {
        return reason;
    }

    public void setReason(String reason)
    {
        this.reason = reason;
    }

    public void setAtributos(DtoItem miDto)
    {
        idItem = miDto.getIdItem();
        name = miDto.getName();
        value = miDto.getValue();

        if (miDto.getPercentage() != null)
        {
            percentage = miDto.getPercentage();
        }

        if (miDto.getNotes() != null)
        {
            notes = miDto.getNotes();
        }

        fixedConcept = miDto.isFixed();

        if (miDto.getType() != null)
        {
            type = miDto.getType();
        }

        reason = miDto.getReason();

        version = miDto.getVersion();
    }

    public DtoItem getDto()
    {
        DtoItem miDtoItem = new DtoItem();

        miDtoItem.setIdItem(idItem);
        miDtoItem.setName(name);
        miDtoItem.setNotes(notes);
        miDtoItem.setPercentage(percentage);
        miDtoItem.setValue(value);
        miDtoItem.setVersion(version);
        miDtoItem.setFixedConcept(fixedConcept);
        miDtoItem.setType(type);
        miDtoItem.setReason(reason);

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
                + "[ nombre=" + name + " ]";
    }

    public int getVersion()
    {
        return version;
    }

    public void setVersion(int version)
    {
        this.version = version;
    }

    public float getValue()
    {
        return value;
    }

    public void setValue(float value)
    {
        this.value = value;
    }

    public boolean isFixed()
    {
        return fixedConcept;
    }

    public void setFixedConcept(boolean fixedConcept)
    {
        this.fixedConcept = fixedConcept;
    }
}
