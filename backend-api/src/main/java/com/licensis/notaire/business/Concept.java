/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.licensis.notaire.business;

import com.licensis.notaire.dto.DtoConcept;
import com.licensis.notaire.dto.exceptions.DtoInvalidoException;
import java.io.Serializable;
import org.springframework.data.domain.Persistable;
import java.util.ArrayList;
import java.util.List;
import jakarta.persistence.Basic;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.NamedQueries;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlTransient;

/**
 *
 * @author User
 */
@Entity
@Table(name = "conceptos")
@XmlRootElement
@NamedQueries(
        {
            @NamedQuery(name = "Concepto.findAll", query = "SELECT c FROM Concept c"),
            @NamedQuery(name = "Concepto.findByIdConcepto", query = "SELECT c FROM Concept c WHERE c.idConcept = :idConcepto"),
            @NamedQuery(name = "Concepto.findByValor", query = "SELECT c FROM Concept c WHERE c.value = :valor"),
            @NamedQuery(name = "Concepto.findByNombre", query = "SELECT c FROM Concept c WHERE c.name = :nombre"),
            @NamedQuery(name = "Concepto.findByPorcentaje", query = "SELECT c FROM Concept c WHERE c.percentage = :porcentaje")
        })
public class Concept implements Serializable, Persistable<Integer>
{

    @Basic(optional = false)
    @Column(name = "concepto_fijo")
    private boolean fixedConcept;
    @Basic(optional = false)
    @Column(name = "habilitado")
    private boolean enabled;
    @Basic(optional = false)
    @Version
    @Column(name = "version")
    private int version = 0;
    @Basic(optional = false)
    @Column(name = "valor")
    private float value;
    @Basic(optional = false)
    @Column(name = "porcentaje")
    private int percentage;
    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "id_concepto")
    private Integer idConcept;
    @Basic(optional = false)
    @Column(name = "nombre")
    private String name;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "concept", fetch = FetchType.EAGER)
    private List<BudgetTemplate> budgetTemplateList = new ArrayList<>();

    public Concept()
    {
    }

    public Concept(Integer idConcept)
    {
        this.idConcept = idConcept;
    }

    public Concept(Integer idConcept, String name, Float value, Integer percentage)
    {
        this.idConcept = idConcept;
        this.name = name;
        this.value = value;
        this.percentage = percentage;
    }
    @Override
    @com.fasterxml.jackson.annotation.JsonIgnore
    public Integer getId() {
        return idConcept;
    }

    // Overrides Spring Data's default isNew(), which infers "new" from a primitive
    // @Version field being 0 -- indistinguishable from an already-persisted row that
    // was never updated, causing deleteById()/delete() to silently no-op for it.
    @Override
    @com.fasterxml.jackson.annotation.JsonIgnore
    public boolean isNew() {
        return idConcept == null || idConcept.equals(BusinessConstants.ID_OBJETO_NO_VALIDO);
    }


    public Integer getIdConcept()
    {
        return idConcept;
    }

    public void setIdConcept(Integer idConcept)
    {
        this.idConcept = idConcept;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    @XmlTransient
    @com.fasterxml.jackson.annotation.JsonIgnore
    public List<BudgetTemplate> getBudgetTemplateList()
    {
        return budgetTemplateList;
    }

    public void setBudgetTemplateList(List<BudgetTemplate> budgetTemplateList)
    {
        this.budgetTemplateList = budgetTemplateList;
    }

    /**
     * Retorna una instancia de DtoConcepto con todos los valores actuales de la instancia de
     * Concepto.
     *
     * @return
     */
    @com.fasterxml.jackson.annotation.JsonIgnore
    public DtoConcept getDto()
    {
        DtoConcept miDto = new DtoConcept();

        miDto.setIdConcept(this.getIdConcept());
        miDto.setName(this.getName());
        miDto.setValue(this.getValue());
        miDto.setPercentage(this.getPercentage());
        miDto.setVersion(this.version);
        miDto.setEnabled(enabled);
        miDto.setFixed(fixedConcept);

        return miDto;
    }

    public void setAtributos(DtoConcept nuevoDto) throws DtoInvalidoException
    {
        this.setIdConcept(nuevoDto.getIdConcept());

        if (nuevoDto.getName() != null)
        {
            this.setName(nuevoDto.getName());
        }

        if (nuevoDto.getValue() != null)
        {
            this.setValue(nuevoDto.getValue());
        }

        if (nuevoDto.getPercentage() != null)
        {
            this.setPercentage(nuevoDto.getPercentage());
        }

        this.setVersion(nuevoDto.getVersion());
        this.setEnabled(nuevoDto.getEnabled());
        this.fixedConcept = nuevoDto.isFixed();
    }

    @Override
    public int hashCode()
    {
        int hash = 0;
        hash += (idConcept != null ? idConcept.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object)
    {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof Concept))
        {
            return false;
        }
        Concept other = (Concept) object;
        if ((this.idConcept == null && other.idConcept != null) || (this.idConcept != null && !this.idConcept.equals(other.idConcept)))
        {
            return false;
        }
        return true;
    }

    @Override
    public String toString()
    {
        return "Conceptos[ version=" + version + " ]"
                + "[ idConcepto=" + idConcept + " ]"
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

    public int getPercentage()
    {
        return percentage;
    }

    public void setPercentage(int percentage)
    {
        this.percentage = percentage;
    }

    public boolean getEnabled()
    {
        return enabled;
    }

    public void setEnabled(boolean enabled)
    {
        this.enabled = enabled;
    }

    public boolean isFixedConcept()
    {
        return fixedConcept;
    }

    public void setFixedConcept(boolean fixedConcept)
    {
        this.fixedConcept = fixedConcept;
    }
}
