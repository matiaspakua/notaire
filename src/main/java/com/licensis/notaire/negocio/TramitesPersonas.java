/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.licensis.notaire.negocio;

import java.io.Serializable;
import javax.persistence.*;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author matias
 */
@Entity
@Table(name = "tramites_personas")
@XmlRootElement
@NamedQueries(
{
    @NamedQuery(name = "TramitesPersonas.findAll", query = "SELECT t FROM TramitesPersonas t"),
    @NamedQuery(name = "TramitesPersonas.findByFkIdTramite", query = "SELECT t FROM TramitesPersonas t WHERE t.tramitesPersonasPK.fkIdTramite = :fkIdTramite"),
    @NamedQuery(name = "TramitesPersonas.findByFkIdPersonaCliente", query = "SELECT t FROM TramitesPersonas t WHERE t.tramitesPersonasPK.fkIdPersonaCliente = :fkIdPersonaCliente"),
    @NamedQuery(name = "TramitesPersonas.findByTramiteCliente", query = "SELECT t FROM TramitesPersonas t WHERE t.tramitesPersonasPK.fkIdPersonaCliente = :fkIdPersonaCliente AND t.tramitesPersonasPK.fkIdTramite = :fkIdTramite"),
    //@NamedQuery(name = "TramitesPersonas.eliminarRegistro", query = "DELETE FROM TramitesPersonas t WHERE t.tramitesPersonasPK.fkIdPersonaCliente = :fkIdPersonaCliente AND t.tramitesPersonasPK.fkIdTramite = :fkIdTramite"),
})
public class TramitesPersonas implements Serializable
{

    @Basic(optional = false)
    @Column(name = "version")
    @Version
    private int version;
    private static final long serialVersionUID = 1L;
    @EmbeddedId
    protected TramitesPersonasPK tramitesPersonasPK;
    @Basic(optional = false)
    @Lob
    @Column(name = "observaciones")
    private String observaciones;
    @JoinColumn(name = "fk_id_persona_cliente", referencedColumnName = "id_persona", insertable = false, updatable = false)
    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    private Persona persona;
    @JoinColumn(name = "fk_id_tramite", referencedColumnName = "id_tramite", insertable = false, updatable = false)
    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    private Tramite tramite;

    public TramitesPersonas() {
    }

    public TramitesPersonas(TramitesPersonasPK tramitesPersonasPK) {
        this.tramitesPersonasPK = tramitesPersonasPK;
    }

    public TramitesPersonas(TramitesPersonasPK tramitesPersonasPK, String observaciones) {
        this.tramitesPersonasPK = tramitesPersonasPK;
        this.observaciones = observaciones;
    }

    public TramitesPersonas(int fkIdTramite, int fkIdPersonaCliente) {
        this.tramitesPersonasPK = new TramitesPersonasPK(fkIdTramite, fkIdPersonaCliente);
    }

    public TramitesPersonasPK getTramitesPersonasPK() {
        return tramitesPersonasPK;
    }

    public void setTramitesPersonasPK(TramitesPersonasPK tramitesPersonasPK) {
        this.tramitesPersonasPK = tramitesPersonasPK;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }

    public Persona getPersona() {
        return persona;
    }

    public void setPersona(Persona persona) {
        this.persona = persona;
    }

    public Tramite getTramite() {
        return tramite;
    }

    public void setTramite(Tramite tramite) {
        this.tramite = tramite;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (tramitesPersonasPK != null ? tramitesPersonasPK.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof TramitesPersonas))
        {
            return false;
        }
        TramitesPersonas other = (TramitesPersonas) object;
        if ((this.tramitesPersonasPK == null && other.tramitesPersonasPK != null) || (this.tramitesPersonasPK != null && !this.tramitesPersonasPK.equals(other.tramitesPersonasPK)))
        {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "TramitesPersonas[ tramitesPersonasPK=" + tramitesPersonasPK + " ]";
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }
}
