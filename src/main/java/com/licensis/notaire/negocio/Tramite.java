/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.licensis.notaire.negocio;

import com.licensis.notaire.dto.DtoDocumentoPresentado;
import com.licensis.notaire.dto.DtoEscritura;
import com.licensis.notaire.dto.DtoGestionDeEscritura;
import com.licensis.notaire.dto.DtoPersona;
import com.licensis.notaire.dto.DtoPresupuesto;
import com.licensis.notaire.dto.DtoTipoIdentificacion;
import com.licensis.notaire.dto.DtoTramite;
import com.licensis.notaire.dto.exceptions.DtoInvalidoException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.Lob;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Version;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

/**
 * Clase que representa un tramite en curso (no es la definicion de un tramite, sino un tramite
 * concreto que se esta llevando a cabo). Posee las referencias hacia el tipo de tramite, el
 * cliente, el presupuesto del cual se origino y el numero de gestion. <p> REGLA DE NEGOCIO: <p> +
 * Cuando se crea un presupuesto, en la tabla tramites se crea un registo, el cual asocia al
 * presupuesto un determinado tramite junto con el tipo de tramite indicado. <p> + Cuando se inicia
 * una gestion, se debe asociar la misma con los registros de tramites asociados a los presupuestos
 * seleccionado. La combinacion de "GestionDeEscritura" y "Tramite" representan el concepto
 * abstracto de una "gestion". <p> + Cuando se iniciar una gestion, pueden haber varios clientes
 * involucrados en la misma. La tabla tramites_personas, expresa esta relacion. El atributo
 * personasList es utilizado por el framework de persistencia para escribir sobre la tabla
 * relacional indicada. <p>
 *
 * @author juanca
 */
@Entity
@Table(name = "tramites")
@XmlRootElement
@NamedQueries(
{
    @NamedQuery(name = "Tramite.findAll", query = "SELECT t FROM Tramite t"),
    @NamedQuery(name = "Tramite.findByIdTramite", query = "SELECT t FROM Tramite t WHERE t.idTramite = :idTramite"),
    @NamedQuery(name = "Tramite.findByIdPresupuesto", query = "SELECT t FROM Tramite t WHERE t.fkIdPresupuesto.idPresupuesto = :idPresupuesto")
})
public class Tramite implements Serializable
{

    @Basic(optional = false)
    @Column(name = "version")
    @Version
    private int version;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "tramite", fetch = FetchType.LAZY)
    private List<TramitesPersonas> tramitesPersonasList;
    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "id_tramite")
    private Integer idTramite;
    @Lob
    @Column(name = "observaciones")
    private String observaciones;
    @JoinTable(name = "tramites_personas", joinColumns =
    {
        @JoinColumn(name = "fk_id_tramite", referencedColumnName = "id_tramite")
    }, inverseJoinColumns =
    {
        @JoinColumn(name = "fk_id_persona_cliente", referencedColumnName = "id_persona")
    })
    @ManyToMany(fetch = FetchType.LAZY)
    private List<Persona> personaList;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "fkIdTramite", fetch = FetchType.LAZY)
    private List<DocumentoPresentado> documentoPresentadoList;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "fkIdTramite", fetch = FetchType.EAGER)
    private List<Presupuesto> presupuestoList;
    @JoinColumn(name = "fk_id_inmueble", referencedColumnName = "id_inmueble")
    @ManyToOne(fetch = FetchType.EAGER)
    private Inmueble fkIdInmueble;
    @JoinColumn(name = "fk_id_presupuesto", referencedColumnName = "id_presupuesto")
    @ManyToOne(optional = true, fetch = FetchType.EAGER)
    private Presupuesto fkIdPresupuesto;
    @JoinColumn(name = "fk_id_escritura", referencedColumnName = "id_escritura")
    @ManyToOne(fetch = FetchType.EAGER)
    private Escritura fkIdEscritura;
    @JoinColumn(name = "fk_id_gestion", referencedColumnName = "id_gestion")
    @ManyToOne(fetch = FetchType.EAGER)
    private GestionDeEscritura fkIdGestion;
    @JoinColumn(name = "fk_id_tipo_tramite", referencedColumnName = "id_tipo_tramite")
    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    private TipoDeTramite fkIdTipoTramite;

    /**
     * Constructor por default para Tramite. Inicializa el ID presupuesto segun el campo
     * {@link ConstantesNegocio}.ID_OBJETO_NO_VALIDO, y todas las listas.
     */
    public Tramite() {
        this.idTramite = ConstantesNegocio.ID_OBJETO_NO_VALIDO;
        this.documentoPresentadoList = new ArrayList<>();
        this.personaList = new ArrayList<>();
        this.presupuestoList = new ArrayList<>();
    }

    public Tramite(Integer idTramite) {
        this.idTramite = idTramite;
    }

    public Integer getIdTramite() {
        return idTramite;
    }

    public void setIdTramite(Integer idTramite) {
        this.idTramite = idTramite;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }

    @XmlTransient
    public List<Persona> getPersonaList() {
        return personaList;
    }

    public void setPersonaList(List<Persona> personaList) {
        this.personaList = personaList;
    }

    @XmlTransient
    public List<DocumentoPresentado> getDocumentoPresentadoList() {
        return documentoPresentadoList;
    }

    public void setDocumentoPresentadoList(List<DocumentoPresentado> documentoPresentadoList) {
        this.documentoPresentadoList = documentoPresentadoList;
    }

    @XmlTransient
    public List<Presupuesto> getPresupuestoList() {
        return presupuestoList;
    }

    public void setPresupuestoList(List<Presupuesto> presupuestoList) {
        this.presupuestoList = presupuestoList;
    }

    public Inmueble getFkIdInmueble() {
        return fkIdInmueble;
    }

    public void setFkIdInmueble(Inmueble fkIdInmueble) {
        this.fkIdInmueble = fkIdInmueble;
    }

    public Presupuesto getFkIdPresupuesto() {
        return fkIdPresupuesto;
    }

    public void setFkIdPresupuesto(Presupuesto fkIdPresupuesto) {
        this.fkIdPresupuesto = fkIdPresupuesto;
    }

    public Escritura getFkIdEscritura() {
        return fkIdEscritura;
    }

    public void setFkIdEscritura(Escritura fkIdEscritura) {
        this.fkIdEscritura = fkIdEscritura;
    }

    public GestionDeEscritura getFkIdGestion() {
        return fkIdGestion;
    }

    public void setFkIdGestion(GestionDeEscritura fkIdGestion) {
        this.fkIdGestion = fkIdGestion;
    }

    public TipoDeTramite getFkIdTipoTramite() {
        return fkIdTipoTramite;
    }

    public void setFkIdTipoTramite(TipoDeTramite fkIdTipoTramite) {
        this.fkIdTipoTramite = fkIdTipoTramite;
    }

    public void setAtributos(DtoTramite dtoTramite) {
       
        this.setIdTramite(dtoTramite.getIdTramite());
        this.setObservaciones(dtoTramite.getObservaciones());

        TipoDeTramite tipoDeTramite = new TipoDeTramite();
        tipoDeTramite.setAtributos(dtoTramite.getTipoDeTramite());
        this.setFkIdTipoTramite(tipoDeTramite);

        if (dtoTramite.getInmueble() != null)
        {
            Inmueble inmueble = new Inmueble();
            inmueble.setAtributos(dtoTramite.getInmueble());
            this.setFkIdInmueble(inmueble);
        }

        if (dtoTramite.getEscritura() != null)
        {
            Escritura escritura = new Escritura();
            escritura.setAtributos(dtoTramite.getEscritura());
            this.setFkIdEscritura(escritura);
        }

        if (dtoTramite.getGestion() != null)
        {
            try
            {
                GestionDeEscritura gestion = new GestionDeEscritura();
                gestion.setAtributos(dtoTramite.getGestion());
                this.setFkIdGestion(gestion);
            }
            catch (DtoInvalidoException ex)
            {
                Logger.getLogger(Tramite.class.getName()).log(Level.SEVERE, null, ex);
            }
            catch (NullPointerException ex)
            {
                // El dto tramite no tiene la referencias hacia la gestion a la cual pertenece.
                GestionDeEscritura gestion = new GestionDeEscritura();

                gestion.setIdGestion(dtoTramite.getGestion().getIdGestion());
                this.setFkIdGestion(gestion);
            }

        }

        if (dtoTramite.getPresupuesto() != null)
        {
            try
            {
                Presupuesto presupuesto = new Presupuesto();
                presupuesto.setAtributos(dtoTramite.getPresupuesto());

                this.setFkIdPresupuesto(presupuesto);
            }
            catch (NullPointerException ex)
            {
                Presupuesto presupuesto = new Presupuesto();
                presupuesto.setIdPresupuesto(dtoTramite.getPresupuesto().getIdPresupuesto());
                this.setFkIdPresupuesto(presupuesto);
            }
        }
    }

    public DtoTramite getDto() {
        DtoTramite miDto = new DtoTramite();

        miDto.setIdTramite(getIdTramite());
        miDto.setObservaciones(observaciones);
        miDto.setTiposDeTramite(fkIdTipoTramite.getDto());

        if (fkIdEscritura != null)
        {
            DtoEscritura miDtoEscritura = new DtoEscritura();

            miDtoEscritura.setIdEscritura(fkIdEscritura.getIdEscritura());
            miDtoEscritura.setNumero(fkIdEscritura.getNumero());

            miDto.setEscritura(miDtoEscritura);
        }

        if (fkIdGestion != null)
        {
            DtoGestionDeEscritura miDtoGestionDeEscritura = new DtoGestionDeEscritura();

            miDtoGestionDeEscritura.setIdGestion(fkIdGestion.getIdGestion());
            miDtoGestionDeEscritura.setNumero(fkIdGestion.getNumero());

            DtoPersona miEscribano = new DtoPersona();
            miEscribano.setIdPersona(fkIdGestion.getFkIdPersonaEscribano().getIdPersona());
            miEscribano.setRegistroEscribano(fkIdGestion.getFkIdPersonaEscribano().getRegistroEscribano());

            miDtoGestionDeEscritura.setPersonaEscribano(miEscribano);

            miDto.setGestionDeEscritura(miDtoGestionDeEscritura);
        }

        if (fkIdInmueble != null)
        {
            miDto.setInmueble(fkIdInmueble.getDto());
        }
        else
        {
            miDto.setInmueble(null);
        }

        if (this.fkIdPresupuesto != null)
        {
            DtoPresupuesto presupuesto = new DtoPresupuesto();

            presupuesto.setIdPresupuesto(fkIdPresupuesto.getIdPresupuesto());

            miDto.setPresupuesto(presupuesto);
        }

        this.documentoPresentadoList = new ArrayList<>();

//        //Documentos asociados al tramite
//        if((this.getDocumentoPresentadoList() != null) && (!this.getDocumentoPresentadoList().isEmpty()))
//        {        
//            for (Iterator<DocumentoPresentado> it = this.getDocumentoPresentadoList().iterator(); it.hasNext();)
//            {
//                DocumentoPresentado documentoPresentado = it.next();
//                
//                miDto.getListaDocumentosPresentados().add(documentoPresentado.getDto());
//            }            
//        }

        return miDto;
    }

    public DtoDocumentoPresentado setDtoDocumento(DocumentoPresentado documento) {
        DtoDocumentoPresentado dtoDocumentoPresentado = new DtoDocumentoPresentado();

        dtoDocumentoPresentado.setVersion(documento.getVersion());
        dtoDocumentoPresentado.setEntregado(documento.getEntregado());
        dtoDocumentoPresentado.setDiasVencimiento(documento.getDiasVencimiento());
        dtoDocumentoPresentado.setFechaIngreso(documento.getFechaIngreso());
        dtoDocumentoPresentado.setFechaLiberado(documento.getFechaLiberado());
        dtoDocumentoPresentado.setFechaPago(documento.getFechaPago());
        dtoDocumentoPresentado.setFechaSalida(documento.getFechaSalida());
        dtoDocumentoPresentado.setFechaVencimiento(documento.getFechaVencimiento());
        dtoDocumentoPresentado.setIdDocumentoPresentado(documento.getIdDocumentoPresentado());
        dtoDocumentoPresentado.setImporteApagar(documento.getImporteAPagar());
        dtoDocumentoPresentado.setLiberado(documento.getLiberado());
        dtoDocumentoPresentado.setNombre(documento.getNombre());
        dtoDocumentoPresentado.setNumeroCarton(documento.getNumeroCarton());
        dtoDocumentoPresentado.setObservaciones(documento.getObservaciones());
        dtoDocumentoPresentado.setObservado(documento.getObservado());
        dtoDocumentoPresentado.setPreparado(documento.getPreparado());
        dtoDocumentoPresentado.setVence(documento.getVence());

        //No se hace set de fkidtramite , porque se produce bucle
        return dtoDocumentoPresentado;
    }

    public DtoPersona getDtoPersona(Persona miPersona) {

        DtoPersona dtoPersona = new DtoPersona();

        //Version del objeto
        dtoPersona.setVersion(miPersona.getVersion());
        dtoPersona.setIdPersona(miPersona.getIdPersona());
        dtoPersona.setNombre(miPersona.getNombre());
        dtoPersona.setApellido(miPersona.getApellido());
        dtoPersona.setCuit(miPersona.getCuit());
        dtoPersona.setEmail(miPersona.getEMail());
        dtoPersona.setEsCliente(miPersona.getEsCliente());
        dtoPersona.setEstadoCivil(miPersona.getEstadoCivil());
        dtoPersona.setFechaNacimiento(miPersona.getFechaNacimiento());
        dtoPersona.setNacionalidad(miPersona.getNacionalidad());
        dtoPersona.setNumeroIdentificacion(miPersona.getNumeroIdentificacion());
        dtoPersona.setNumeroNupcias(miPersona.getNumeroNupcias());
        dtoPersona.setOcupacion(miPersona.getOcupacion());
        dtoPersona.setDomicilio(miPersona.getDomicilio());
        dtoPersona.setRegistroEscribano(miPersona.getRegistroEscribano());
        dtoPersona.setSexo(miPersona.getSexo());
        dtoPersona.setTelefono(miPersona.getTelefono());

        DtoTipoIdentificacion dtoTipoIdentificacion = new DtoTipoIdentificacion();
        dtoTipoIdentificacion.setIdTipoIdentificacion(miPersona.getFkIdTipoIdentificacion().getIdTipoIdentificacion());

        dtoPersona.setDtoTipoIdentificacion(dtoTipoIdentificacion);

        //Asocio el id_Fk_TipoIdentificacion con el nombre tipo de identificacion
        dtoTipoIdentificacion.setNombre(ControllerNegocio.getInstancia().asociarNombreTipoIdentificacion(dtoPersona));

        return dtoPersona;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (getIdTramite() != null ? getIdTramite().hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof Tramite))
        {
            return false;
        }
        Tramite other = (Tramite) object;
        if ((this.getIdTramite() == null && other.getIdTramite() != null) || (this.getIdTramite() != null && !this.idTramite.equals(other.idTramite)))
        {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "Tramite[ idTramite=" + getIdTramite() + " ]";
    }

    @XmlTransient
    public List<TramitesPersonas> getTramitesPersonasList() {
        return tramitesPersonasList;
    }

    public void setTramitesPersonasList(List<TramitesPersonas> tramitesPersonasList) {
        this.tramitesPersonasList = tramitesPersonasList;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }
}
