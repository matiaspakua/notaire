/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.licensis.notaire.negocio;

import com.licensis.notaire.dto.DtoMovimientoTestimonio;
import com.licensis.notaire.dto.DtoTestimonio;
import java.io.Serializable;
import java.util.ArrayList;
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
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.NamedQueries;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlTransient;

/**
 *
 * @author juanca
 */
@Entity
@Table(name = "testimonios")
@XmlRootElement
@NamedQueries(
        {
            @NamedQuery(name = "Testimonio.findAll", query = "SELECT t FROM Testimonio t"),
            @NamedQuery(name = "Testimonio.findByIdTestimonio", query = "SELECT t FROM Testimonio t WHERE t.idTestimonio = :idTestimonio"),
            @NamedQuery(name = "Testimonio.findByNumero", query = "SELECT t FROM Testimonio t WHERE t.numero = :numero"),
            @NamedQuery(name = "Testimonio.findByEscritura", query = "SELECT t FROM Testimonio t WHERE t.fkIdEscritura.idEscritura = :idEscritura"),
            @NamedQuery(name = "Testimonio.findByObservado", query = "SELECT t FROM Testimonio t WHERE t.observado = :observado")
        })
public class Testimonio implements Serializable
{

    @Basic(optional = false)
    @Column(name = "version")
    @Version
    private int version = 0;
    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "id_testimonio")
    private Integer idTestimonio;
    @Basic(optional = false)
    @Column(name = "numero")
    private int numero;
    @Basic(optional = false)
    @Column(name = "observado")
    private boolean observado;
    @Lob
    @Column(name = "observaciones")
    private String observaciones;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "fkIdTestimonio", fetch = FetchType.LAZY)
    private List<MovimientoTestimonio> movimientoTestimonioList = new ArrayList<>();
    @JoinColumn(name = "fk_id_escritura", referencedColumnName = "id_escritura")
    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    private Escritura fkIdEscritura;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "fkIdTestimonio", fetch = FetchType.LAZY)
    private List<Copia> copiaList = new ArrayList<>();

    public Testimonio()
    {
    }

    public Testimonio(Integer idTestimonio)
    {
        this.idTestimonio = idTestimonio;
    }

    public Testimonio(Integer idTestimonio, int numero, boolean observado)
    {
        this.idTestimonio = idTestimonio;
        this.numero = numero;
        this.observado = observado;
    }

    public Integer getIdTestimonio()
    {
        return idTestimonio;
    }

    public void setIdTestimonio(Integer idTestimonio)
    {
        this.idTestimonio = idTestimonio;
    }

    public int getNumero()
    {
        return numero;
    }

    public void setNumero(int numero)
    {
        this.numero = numero;
    }

    public boolean getObservado()
    {
        return observado;
    }

    public void setObservado(boolean observado)
    {
        this.observado = observado;
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
    public List<MovimientoTestimonio> getMovimientoTestimonioList()
    {
        return movimientoTestimonioList;
    }

    public void setMovimientoTestimonioList(List<MovimientoTestimonio> movimientoTestimonioList)
    {
        this.movimientoTestimonioList = movimientoTestimonioList;
    }

    public Escritura getFkIdEscritura()
    {
        return fkIdEscritura;
    }

    public void setFkIdEscritura(Escritura fkIdEscritura)
    {
        this.fkIdEscritura = fkIdEscritura;
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

    public void setAtributos(DtoTestimonio miDto)
    {

        if (miDto.getIdTestimonio() != null)
        {
            idTestimonio = miDto.getIdTestimonio();
        }

        numero = miDto.getNumero();
        observaciones = miDto.getObservaciones();
        observado = miDto.isObservado();
        version = miDto.getVersion();

        if (miDto.getEscritura() != null)
        {
            Escritura miEscritura = new Escritura();
            miEscritura.setIdEscritura(miDto.getEscritura().getIdEscritura());

            fkIdEscritura = miEscritura;
        }

        if (miDto.getMovimientosTestimonios() != null && !miDto.getMovimientosTestimonios().isEmpty())
        {
            movimientoTestimonioList = new ArrayList<>();

            for (Iterator<DtoMovimientoTestimonio> it = miDto.getMovimientosTestimonios().iterator(); it.hasNext();)
            {
                DtoMovimientoTestimonio dtoMovimientoTestimonio = it.next();
                MovimientoTestimonio miMovimientoTestimonio = new MovimientoTestimonio();
                miMovimientoTestimonio.setAtributos(dtoMovimientoTestimonio);
                movimientoTestimonioList.add(miMovimientoTestimonio);
            }
        }
    }

    public DtoTestimonio getDto()
    {
        DtoTestimonio miDto = new DtoTestimonio();

        miDto.setIdTestimonio(idTestimonio);
        miDto.setNumero(numero);
        miDto.setObservaciones(observaciones);
        miDto.setObservado(observado);
        miDto.setVersion(version);

        if (fkIdEscritura != null)
        {
            miDto.setEscritura(fkIdEscritura.getDto());
        }

        if (movimientoTestimonioList != null && !movimientoTestimonioList.isEmpty())
        {
            List<DtoMovimientoTestimonio> miLista = new ArrayList<>();

            for (Iterator<MovimientoTestimonio> it = movimientoTestimonioList.iterator(); it.hasNext();)
            {
                MovimientoTestimonio movimientoTestimonio = it.next();
                DtoMovimientoTestimonio miDtoMovimientoTestimonio = movimientoTestimonio.getDto();
                miLista.add(miDtoMovimientoTestimonio);
            }

            miDto.setMovimientosTestimonios(miLista);
        }

        return miDto;
    }

    @Override
    public int hashCode()
    {
        int hash = 0;
        hash += (idTestimonio != null ? idTestimonio.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object)
    {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof Testimonio))
        {
            return false;
        }
        Testimonio other = (Testimonio) object;
        if ((this.idTestimonio == null && other.idTestimonio != null) || (this.idTestimonio != null && !this.idTestimonio.equals(other.idTestimonio)))
        {
            return false;
        }
        return true;
    }

    @Override
    public String toString()
    {
        return "Testimonio[ idTestimonio=" + idTestimonio + " ]"
                + "[ numero=" + numero + " ]"
                + "[ idEscritura=" + fkIdEscritura.getIdEscritura() + " ]";
    }

    public int getVersion()
    {
        return version;
    }

    public void setVersion(int version)
    {
        this.version = version;
    }
}
