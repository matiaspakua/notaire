/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.licensis.notaire.negocio;

import com.licensis.notaire.dto.DtoDocumentoPresentado;
import com.licensis.notaire.dto.DtoTipoDeDocumento;
import com.licensis.notaire.dto.DtoTramite;
import java.io.Serializable;
import java.util.Date;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Version;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author juanca
 */
@Entity
@Table(name = "documentos_presentados")
@XmlRootElement
@NamedQueries(
        {
            @NamedQuery(name = "DocumentoPresentado.findAll", query = "SELECT d FROM DocumentoPresentado d"),
            @NamedQuery(name = "DocumentoPresentado.findByIdDocumentoPresentado", query = "SELECT d FROM DocumentoPresentado d WHERE d.idDocumentoPresentado = :idDocumentoPresentado"),
            @NamedQuery(name = "DocumentoPresentado.findByNumeroCarton", query = "SELECT d FROM DocumentoPresentado d WHERE d.numeroCarton = :numeroCarton"),
            @NamedQuery(name = "DocumentoPresentado.findByFechaIngreso", query = "SELECT d FROM DocumentoPresentado d WHERE d.fechaIngreso = :fechaIngreso"),
            @NamedQuery(name = "DocumentoPresentado.findByFechaSalida", query = "SELECT d FROM DocumentoPresentado d WHERE d.fechaSalida = :fechaSalida"),
            @NamedQuery(name = "DocumentoPresentado.findByPreparado", query = "SELECT d FROM DocumentoPresentado d WHERE d.preparado = :preparado"),
            @NamedQuery(name = "DocumentoPresentado.findByVence", query = "SELECT d FROM DocumentoPresentado d WHERE d.vence = :vence"),
            @NamedQuery(name = "DocumentoPresentado.findByFechaVencimiento", query = "SELECT d FROM DocumentoPresentado d WHERE d.fechaVencimiento >= :fechaVencimiento"),
            @NamedQuery(name = "DocumentoPresentado.findByDiasVencimiento", query = "SELECT d FROM DocumentoPresentado d WHERE d.diasVencimiento = :diasVencimiento"),
            @NamedQuery(name = "DocumentoPresentado.findByImporteAPagar", query = "SELECT d FROM DocumentoPresentado d WHERE d.importeAPagar = :importeAPagar"),
            @NamedQuery(name = "DocumentoPresentado.findByFechaPago", query = "SELECT d FROM DocumentoPresentado d WHERE d.fechaPago = :fechaPago"),
            @NamedQuery(name = "DocumentoPresentado.findByLiberado", query = "SELECT d FROM DocumentoPresentado d WHERE d.liberado = :liberado"),
            @NamedQuery(name = "DocumentoPresentado.findByFechaLiberado", query = "SELECT d FROM DocumentoPresentado d WHERE d.fechaLiberado = :fechaLiberado"),
            @NamedQuery(name = "DocumentoPresentado.findByObservado", query = "SELECT d FROM DocumentoPresentado d WHERE d.observado = :observado")
        })
public class DocumentoPresentado implements Serializable
{

    @Column(name = "fecha_ingreso")
    @Temporal(TemporalType.DATE)
    private Date fechaIngreso;
    @Column(name = "liberado")
    private Boolean liberado;
    @Column(name = "observado")
    private Boolean observado;
    @Column(name = "fk_id_tipo_documento")
    private Integer fkIdTipoDocumento;
    @Basic(optional = false)
    @Lob
    @Column(name = "quien_entrega")
    private String quienEntrega;
    @Column(name = "reingresado")
    private Boolean reingresado;
    @Column(name = "fecha_salida")
    @Temporal(TemporalType.DATE)
    private Date fechaSalida;
    @Column(name = "fecha_vencimiento")
    @Temporal(TemporalType.DATE)
    private Date fechaVencimiento;
    @Column(name = "fecha_pago")
    @Temporal(TemporalType.DATE)
    private Date fechaPago;
    @Column(name = "fecha_liberado")
    @Temporal(TemporalType.DATE)
    private Date fechaLiberado;
    @Column(name = "entregado")
    private Boolean entregado;
    @Basic(optional = false)
    @Column(name = "version")
    @Version
    private int version;
    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "id_documento_presentado")
    private Integer idDocumentoPresentado;
    @Basic(optional = false)
    @Lob
    @Column(name = "nombre")
    private String nombre;
    @Column(name = "numero_carton")
    private Integer numeroCarton;
    @Basic(optional = false)
    @Column(name = "preparado")
    private boolean preparado;
    @Basic(optional = false)
    @Column(name = "vence")
    private boolean vence;
    @Column(name = "dias_vencimiento")
    private Integer diasVencimiento;
    // @Max(value=?)  @Min(value=?)//if you know range of your decimal fields consider using these annotations to enforce field validation
    @Column(name = "importe_a_pagar")
    private Float importeAPagar;
    @Lob
    @Column(name = "observaciones")
    private String observaciones;
    @JoinColumn(name = "fk_id_tramite", referencedColumnName = "id_tramite")
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private Tramite fkIdTramite;

    public DocumentoPresentado()
    {
    }

    public DocumentoPresentado(Integer idDocumentoPresentado)
    {
        this.idDocumentoPresentado = idDocumentoPresentado;
    }

    public DocumentoPresentado(Integer idDocumentoPresentado, String nombre, Date fechaIngreso, boolean preparado, boolean vence, boolean liberado, boolean observado)
    {
        this.idDocumentoPresentado = idDocumentoPresentado;
        this.nombre = nombre;
        //this.fechaIngreso = fechaIngreso;
        this.preparado = preparado;
        this.vence = vence;
        this.liberado = liberado;
        this.observado = observado;
        this.reingresado = reingresado;
    }

    public Integer getIdDocumentoPresentado()
    {
        return idDocumentoPresentado;
    }

    public void setIdDocumentoPresentado(Integer idDocumentoPresentado)
    {
        this.idDocumentoPresentado = idDocumentoPresentado;
    }

    public String getNombre()
    {
        return nombre;
    }

    public void setNombre(String nombre)
    {
        this.nombre = nombre;
    }

    public Integer getNumeroCarton()
    {
        return numeroCarton;
    }

    public void setNumeroCarton(Integer numeroCarton)
    {
        this.numeroCarton = numeroCarton;
    }

    public Date getFechaSalida()
    {
        return fechaSalida;
    }

    public void setFechaSalida(Date fechaSalida)
    {
        this.fechaSalida = fechaSalida;
    }

    public boolean getPreparado()
    {
        return preparado;
    }

    public void setPreparado(boolean preparado)
    {
        this.preparado = preparado;
    }

    public boolean getVence()
    {
        return vence;
    }

    public void setVence(boolean vence)
    {
        this.vence = vence;
    }

    public Date getFechaVencimiento()
    {
        return fechaVencimiento;
    }

    public void setFechaVencimiento(Date fechaVencimiento)
    {
        this.fechaVencimiento = fechaVencimiento;
    }

    public Integer getDiasVencimiento()
    {
        return diasVencimiento;
    }

    public void setDiasVencimiento(Integer diasVencimiento)
    {
        this.diasVencimiento = diasVencimiento;
    }

    public Float getImporteAPagar()
    {
        return importeAPagar;
    }

    public void setImporteAPagar(Float importeAPagar)
    {
        this.importeAPagar = importeAPagar;
    }

    public Date getFechaPago()
    {
        return fechaPago;
    }

    public void setFechaPago(Date fechaPago)
    {
        this.fechaPago = fechaPago;
    }

    public Date getFechaLiberado()
    {
        return fechaLiberado;
    }

    public void setFechaLiberado(Date fechaLiberado)
    {
        this.fechaLiberado = fechaLiberado;
    }

    public String getObservaciones()
    {
        return observaciones;
    }

    public void setObservaciones(String observaciones)
    {
        this.observaciones = observaciones;
    }

    public Tramite getFkIdTramite()
    {
        return fkIdTramite;
    }

    public void setFkIdTramite(Tramite fkIdTramite)
    {
        this.fkIdTramite = fkIdTramite;
    }

    @Override
    public int hashCode()
    {
        int hash = 0;
        hash += (idDocumentoPresentado != null ? idDocumentoPresentado.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object)
    {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof DocumentoPresentado))
        {
            return false;
        }
        DocumentoPresentado other = (DocumentoPresentado) object;
        if ((this.idDocumentoPresentado == null && other.idDocumentoPresentado != null) || (this.idDocumentoPresentado != null && !this.idDocumentoPresentado.equals(other.idDocumentoPresentado)))
        {
            return false;
        }
        return true;
    }

    @Override
    public String toString()
    {
        return "DocumentoPresentado[ idDocumentoPresentado=" + idDocumentoPresentado
                + ", carton: " + this.numeroCarton
                + ", fecha ingreso: " + this.fechaIngreso
                + ", fecha salida: " + this.fechaSalida
                + ", observado: " + this.observado
                + ", importe: " + this.importeAPagar
                + ", echa pago: " + this.fechaPago
                + ", liberado" + this.liberado
                + ", fecha liberacion: " + this.fechaLiberado
                + ", observaciones: " + this.observaciones
                + "]";
    }

    public int getVersion()
    {
        return version;
    }

    public void setVersion(int version)
    {
        this.version = version;
    }

    public Boolean getEntregado()
    {
        return entregado;
    }

    public void setEntregado(Boolean entregado)
    {
        this.entregado = entregado;
    }

    public DtoDocumentoPresentado getDto()
    {

        DtoDocumentoPresentado dtoDocumentoPresentado = new DtoDocumentoPresentado();

        dtoDocumentoPresentado.setVersion(version);
        dtoDocumentoPresentado.setDiasVencimiento(diasVencimiento);
        dtoDocumentoPresentado.setFechaIngreso(fechaIngreso);
        dtoDocumentoPresentado.setFechaLiberado(fechaLiberado);
        dtoDocumentoPresentado.setFechaPago(fechaPago);
        dtoDocumentoPresentado.setFechaSalida(fechaSalida);
        dtoDocumentoPresentado.setFechaVencimiento(fechaVencimiento);
        dtoDocumentoPresentado.setIdDocumentoPresentado(idDocumentoPresentado);
        dtoDocumentoPresentado.setImporteApagar(importeAPagar);
        dtoDocumentoPresentado.setLiberado(liberado);
        dtoDocumentoPresentado.setNombre(nombre);
        dtoDocumentoPresentado.setNumeroCarton(numeroCarton);
        dtoDocumentoPresentado.setObservaciones(observaciones);
        dtoDocumentoPresentado.setObservado(observado);
        dtoDocumentoPresentado.setPreparado(preparado);
        dtoDocumentoPresentado.setVence(vence);
        dtoDocumentoPresentado.setQuienEntrega(quienEntrega);

        if (entregado != null)
        {
            dtoDocumentoPresentado.setEntregado(entregado);
        }

        if (reingresado != null)
        {
            dtoDocumentoPresentado.setReingresado(reingresado);
        }

        DtoTramite dtoTramite = new DtoTramite();
        dtoTramite = fkIdTramite.getDto();
        dtoDocumentoPresentado.setFkTramite(dtoTramite);

        DtoTipoDeDocumento dtoTipoDeDocumento = new DtoTipoDeDocumento();

        return dtoDocumentoPresentado;
    }

    public void setAtributos(DtoDocumentoPresentado dtoDocumentoPresentado)
    {

        version = dtoDocumentoPresentado.getVersion();
        idDocumentoPresentado = dtoDocumentoPresentado.getIdDocumentoPresentado();
        nombre = dtoDocumentoPresentado.getNombre();
        numeroCarton = dtoDocumentoPresentado.getNumeroCarton();
        fechaIngreso = dtoDocumentoPresentado.getFechaIngreso();
        fechaSalida = dtoDocumentoPresentado.getFechaSalida();
        preparado = dtoDocumentoPresentado.isPreparado();
        vence = dtoDocumentoPresentado.isVence();
        fechaVencimiento = dtoDocumentoPresentado.getFechaVencimiento();
        diasVencimiento = dtoDocumentoPresentado.getDiasVencimiento();
        importeAPagar = dtoDocumentoPresentado.getImporteAPagar();
        fechaPago = dtoDocumentoPresentado.getFechaPago();
        liberado = dtoDocumentoPresentado.isLiberado();
        fechaLiberado = dtoDocumentoPresentado.getFechaLiberado();
        observado = dtoDocumentoPresentado.isObservado();
        observaciones = dtoDocumentoPresentado.getObservaciones();
        reingresado = dtoDocumentoPresentado.isReingresado();
        quienEntrega = dtoDocumentoPresentado.getQuienEntrega();
        entregado = dtoDocumentoPresentado.isEntregado();
        idDocumentoPresentado = dtoDocumentoPresentado.getIdDocumentoPresentado();

        Tramite tramite = new Tramite();
        tramite.setAtributos(dtoDocumentoPresentado.getFkTramite());

        fkIdTramite = tramite;
    }

    public Boolean getReingresado()
    {
        return reingresado;
    }

    public void setReingresado(Boolean reingresado)
    {
        this.reingresado = reingresado;
    }

    public String getQuienEntrega()
    {
        return quienEntrega;
    }

    public void setQuienEntrega(String quienEntrega)
    {
        this.quienEntrega = quienEntrega;
    }

    public int getFkIdTipoDocumento()
    {
        return fkIdTipoDocumento;
    }

    public void setFkIdTipoDocumento(int fkIdTipoDocumento)
    {
        this.fkIdTipoDocumento = fkIdTipoDocumento;
    }

    public Boolean getLiberado()
    {
        return liberado;
    }

    public void setLiberado(Boolean liberado)
    {
        this.liberado = liberado;
    }

    public Boolean getObservado()
    {
        return observado;
    }

    public void setObservado(Boolean observado)
    {
        this.observado = observado;
    }

    public void setFkIdTipoDocumento(Integer fkIdTipoDocumento)
    {
        this.fkIdTipoDocumento = fkIdTipoDocumento;
    }

    public Date getFechaIngreso()
    {
        return fechaIngreso;
    }

    public void setFechaIngreso(Date fechaIngreso)
    {
        this.fechaIngreso = fechaIngreso;
    }
}
