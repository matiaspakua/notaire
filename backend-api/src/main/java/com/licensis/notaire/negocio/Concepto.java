/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.licensis.notaire.negocio;

import com.licensis.notaire.dto.DtoConcepto;
import com.licensis.notaire.dto.exceptions.DtoInvalidoException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import jakarta.persistence.*;
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
            @NamedQuery(name = "Concepto.findAll", query = "SELECT c FROM Concepto c"),
            @NamedQuery(name = "Concepto.findByIdConcepto", query = "SELECT c FROM Concepto c WHERE c.idConcepto = :idConcepto"),
            @NamedQuery(name = "Concepto.findByValor", query = "SELECT c FROM Concepto c WHERE c.valor = :valor"),
            @NamedQuery(name = "Concepto.findByNombre", query = "SELECT c FROM Concepto c WHERE c.nombre = :nombre"),
            @NamedQuery(name = "Concepto.findByPorcentaje", query = "SELECT c FROM Concepto c WHERE c.porcentaje = :porcentaje")
        })
public class Concepto implements Serializable
{

    @Basic(optional = false)
    @Column(name = "concepto_fijo")
    private boolean conceptoFijo;
    @Basic(optional = false)
    @Column(name = "habilitado")
    private boolean habilitado;
    @Basic(optional = false)
    @Version
    @Column(name = "version")
    private int version = 0;
    @Basic(optional = false)
    @Column(name = "valor")
    private float valor;
    @Basic(optional = false)
    @Column(name = "porcentaje")
    private int porcentaje;
    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "id_concepto")
    private Integer idConcepto;
    @Basic(optional = false)
    @Lob
    @Column(name = "nombre")
    private String nombre;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "concepto", fetch = FetchType.EAGER)
    private List<PlantillaPresupuesto> plantillaPresupuestoList = new ArrayList<>();

    public Concepto()
    {
    }

    public Concepto(Integer idConcepto)
    {
        this.idConcepto = idConcepto;
    }

    public Concepto(Integer idConcepto, String nombre, Float valor, Integer porcentaje)
    {
        this.idConcepto = idConcepto;
        this.nombre = nombre;
        this.valor = valor;
        this.porcentaje = porcentaje;
    }

    public Integer getIdConcepto()
    {
        return idConcepto;
    }

    public void setIdConcepto(Integer idConcepto)
    {
        this.idConcepto = idConcepto;
    }

    public String getNombre()
    {
        return nombre;
    }

    public void setNombre(String nombre)
    {
        this.nombre = nombre;
    }

    @XmlTransient
    public List<PlantillaPresupuesto> getPlantillaPresupuestoList()
    {
        return plantillaPresupuestoList;
    }

    public void setPlantillaPresupuestoList(List<PlantillaPresupuesto> plantillaPresupuestoList)
    {
        this.plantillaPresupuestoList = plantillaPresupuestoList;
    }

    /**
     * Retorna una instancia de DtoConcepto con todos los valores actuales de la instancia de
     * Concepto.
     *
     * @return
     */
    public DtoConcepto getDto()
    {
        DtoConcepto miDto = new DtoConcepto();

        miDto.setIdConcepto(this.getIdConcepto());
        miDto.setNombre(this.getNombre());
        miDto.setValor(this.getValor());
        miDto.setPorcentaje(this.getPorcentaje());
        miDto.setVersion(this.version);
        miDto.setHabilitado(habilitado);
        miDto.setFijo(conceptoFijo);

        return miDto;
    }

    public void setAtributos(DtoConcepto nuevoDto) throws DtoInvalidoException
    {
        this.setIdConcepto(nuevoDto.getIdConcepto());

        if (nuevoDto.getNombre() != null)
        {
            this.setNombre(nuevoDto.getNombre());
        }

        if (nuevoDto.getValor() != null)
        {
            this.setValor(nuevoDto.getValor());
        }

        if (nuevoDto.getPorcentaje() != null)
        {
            this.setPorcentaje(nuevoDto.getPorcentaje());
        }

        this.setVersion(nuevoDto.getVersion());
        this.setHabilitado(nuevoDto.getHabilitado());
        this.conceptoFijo = nuevoDto.isFijo();
    }

    @Override
    public int hashCode()
    {
        int hash = 0;
        hash += (idConcepto != null ? idConcepto.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object)
    {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof Concepto))
        {
            return false;
        }
        Concepto other = (Concepto) object;
        if ((this.idConcepto == null && other.idConcepto != null) || (this.idConcepto != null && !this.idConcepto.equals(other.idConcepto)))
        {
            return false;
        }
        return true;
    }

    @Override
    public String toString()
    {
        return "Conceptos[ version=" + version + " ]"
                + "[ idConcepto=" + idConcepto + " ]"
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

    public int getPorcentaje()
    {
        return porcentaje;
    }

    public void setPorcentaje(int porcentaje)
    {
        this.porcentaje = porcentaje;
    }

    public boolean getHabilitado()
    {
        return habilitado;
    }

    public void setHabilitado(boolean habilitado)
    {
        this.habilitado = habilitado;
    }

    public boolean isConceptoFijo()
    {
        return conceptoFijo;
    }

    public void setConceptoFijo(boolean conceptoFijo)
    {
        this.conceptoFijo = conceptoFijo;
    }
}
