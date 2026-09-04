package com.licensis.notaire.negocio;

import com.licensis.notaire.dto.DtoMinutaInscripcion;

import jakarta.persistence.Basic;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import jakarta.persistence.Version;

import java.io.Serializable;
import java.util.Date;

/**
 * Minuta de Inscripción de una escritura sobre un inmueble ante el Registro
 * de la Propiedad Inmueble, y su circuito registral (CU82): Generada,
 * Presentada, Observada, Inscripta.
 */
@Entity
@Table(name = "minutas_inscripcion")
public class MinutaInscripcion implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "id_minuta_inscripcion")
    private Integer idMinutaInscripcion;

    @Basic(optional = false)
    @Column(name = "numero")
    private int numero;

    @Column(name = "precio_operacion")
    private Float precioOperacion;

    @Basic(optional = false)
    @Column(name = "estado")
    private String estado;

    @Basic(optional = false)
    @Temporal(TemporalType.DATE)
    @Column(name = "fecha_generacion")
    private Date fechaGeneracion;

    @Temporal(TemporalType.DATE)
    @Column(name = "fecha_presentacion")
    private Date fechaPresentacion;

    @Column(name = "numero_entrada_registral")
    private String numeroEntradaRegistral;

    @Temporal(TemporalType.DATE)
    @Column(name = "fecha_recepcion")
    private Date fechaRecepcion;

    @Column(name = "numero_inscripcion_definitivo")
    private String numeroInscripcionDefinitivo;

    @Column(name = "observaciones_registro")
    private String observacionesRegistro;

    @Temporal(TemporalType.DATE)
    @Column(name = "fecha_subsanacion")
    private Date fechaSubsanacion;

    @JoinColumn(name = "fk_id_escritura", referencedColumnName = "id_escritura", unique = true)
    @OneToOne(optional = false, fetch = FetchType.EAGER)
    private Escritura fkIdEscritura;

    @Basic(optional = false)
    @Column(name = "version")
    @Version
    private int version;

    public Integer getIdMinutaInscripcion() {
        return idMinutaInscripcion;
    }

    public void setIdMinutaInscripcion(Integer idMinutaInscripcion) {
        this.idMinutaInscripcion = idMinutaInscripcion;
    }

    public int getNumero() {
        return numero;
    }

    public void setNumero(int numero) {
        this.numero = numero;
    }

    public Float getPrecioOperacion() {
        return precioOperacion;
    }

    public void setPrecioOperacion(Float precioOperacion) {
        this.precioOperacion = precioOperacion;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public Date getFechaGeneracion() {
        return fechaGeneracion;
    }

    public void setFechaGeneracion(Date fechaGeneracion) {
        this.fechaGeneracion = fechaGeneracion;
    }

    public Date getFechaPresentacion() {
        return fechaPresentacion;
    }

    public void setFechaPresentacion(Date fechaPresentacion) {
        this.fechaPresentacion = fechaPresentacion;
    }

    public String getNumeroEntradaRegistral() {
        return numeroEntradaRegistral;
    }

    public void setNumeroEntradaRegistral(String numeroEntradaRegistral) {
        this.numeroEntradaRegistral = numeroEntradaRegistral;
    }

    public Date getFechaRecepcion() {
        return fechaRecepcion;
    }

    public void setFechaRecepcion(Date fechaRecepcion) {
        this.fechaRecepcion = fechaRecepcion;
    }

    public String getNumeroInscripcionDefinitivo() {
        return numeroInscripcionDefinitivo;
    }

    public void setNumeroInscripcionDefinitivo(String numeroInscripcionDefinitivo) {
        this.numeroInscripcionDefinitivo = numeroInscripcionDefinitivo;
    }

    public String getObservacionesRegistro() {
        return observacionesRegistro;
    }

    public void setObservacionesRegistro(String observacionesRegistro) {
        this.observacionesRegistro = observacionesRegistro;
    }

    public Date getFechaSubsanacion() {
        return fechaSubsanacion;
    }

    public void setFechaSubsanacion(Date fechaSubsanacion) {
        this.fechaSubsanacion = fechaSubsanacion;
    }

    public Escritura getFkIdEscritura() {
        return fkIdEscritura;
    }

    public void setFkIdEscritura(Escritura fkIdEscritura) {
        this.fkIdEscritura = fkIdEscritura;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    @Override
    public int hashCode() {
        return idMinutaInscripcion != null ? idMinutaInscripcion.hashCode() : 0;
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof MinutaInscripcion other)) {
            return false;
        }
        return idMinutaInscripcion != null && idMinutaInscripcion.equals(other.idMinutaInscripcion);
    }

    @Override
    public String toString() {
        return "MinutaInscripcion[ idMinutaInscripcion=" + idMinutaInscripcion + " ][ numero=" + numero
                + " ][ estado=" + estado + " ]";
    }

    public DtoMinutaInscripcion getDto() {
        return new DtoMinutaInscripcion(idMinutaInscripcion, numero, precioOperacion, estado, fechaGeneracion,
                fechaPresentacion, numeroEntradaRegistral, fechaRecepcion, numeroInscripcionDefinitivo,
                observacionesRegistro, fechaSubsanacion, fkIdEscritura != null ? fkIdEscritura.getIdEscritura() : null);
    }
}
