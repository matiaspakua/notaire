/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.licensis.notaire.negocio;

import com.licensis.notaire.dto.DtoEstadoDeGestion;
import com.licensis.notaire.dto.DtoGestionDeEscritura;
import com.licensis.notaire.dto.DtoInmueble;
import com.licensis.notaire.dto.DtoPersona;
import com.licensis.notaire.dto.DtoTipoIdentificacion;
import com.licensis.notaire.dto.DtoTramite;
import com.licensis.notaire.dto.exceptions.DtoInvalidoException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Version;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

/**
 * Clase que representa un gestion de escritura. <p> REGLA DE NEGOCIO: <p>
 *
 * <lo> <li> El numero de carpeta es auto-incremental sugerido por el sistema, pero puede ser
 * modificado por el usuario, donde se verifica que el numero indicado no exista ya registrado.
 * </li> </lo>
 *
 *
 * @author User
 */
@Entity
@Table(name = "gestiones_de_escrituras")
@XmlRootElement
@NamedQueries(
{
    @NamedQuery(name = "GestionDeEscritura.findAll", query = "SELECT g FROM GestionDeEscritura g"),
    @NamedQuery(name = "GestionDeEscritura.findByIdGestion", query = "SELECT g FROM GestionDeEscritura g WHERE g.idGestion = :idGestion"),
    @NamedQuery(name = "GestionDeEscritura.findByNumero", query = "SELECT g FROM GestionDeEscritura g WHERE g.numero = :numero"),
    @NamedQuery(name = "GestionDeEscritura.findByFechaInicio", query = "SELECT g FROM GestionDeEscritura g WHERE g.fechaInicio = :fechaInicio"),
    @NamedQuery(name = "GestionDeEscritura.findByNumeroArchivo", query = "SELECT g FROM GestionDeEscritura g WHERE g.numeroArchivo = :numeroArchivo"),
    @NamedQuery(name = "GestionDeEscritura.findByNumeroBibliorato", query = "SELECT g FROM GestionDeEscritura g WHERE g.numeroBibliorato = :numeroBibliorato")
})
public class GestionDeEscritura implements Serializable
{

    @Basic(optional = false)
    @Column(name = "fecha_inicio")
    @Temporal(TemporalType.DATE)
    private Date fechaInicio;
    @JoinColumn(name = "fk_id_estado_de_gestion", referencedColumnName = "id_estado_gestion")
    @ManyToOne
    private EstadoDeGestion fkIdEstadoDeGestion;
    @Basic(optional = false)
    @Column(name = "version")
    @Version
    private int version;
    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "id_gestion")
    private Integer idGestion;
    @Basic(optional = false)
    @Column(name = "numero")
    private int numero;
    @Basic(optional = false)
    @Lob
    @Column(name = "encabezado")
    private String encabezado;
    @Lob
    @Column(name = "observaciones")
    private String observaciones;
    @Column(name = "numero_archivo")
    private Integer numeroArchivo;
    @Column(name = "numero_bibliorato")
    private Integer numeroBibliorato;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "fkIdGestion")
    private List<Historial> historialList;
    @JoinColumn(name = "fk_id_persona_escribano", referencedColumnName = "id_persona")
    @ManyToOne(optional = false)
    private Persona fkIdPersonaEscribano;
    @OneToMany(mappedBy = "fkIdGestion")
    private List<Tramite> tramiteList;

    /**
     * Constructor por default para gestion de escritura. Asigna al ID y al numero de gestion el
     * valor de {@link ConstantesNegocio}.ID_OBJETO_NO_VALIDO.
     */
    public GestionDeEscritura() {
        this.idGestion = ConstantesNegocio.ID_OBJETO_NO_VALIDO;
        this.numero = ConstantesNegocio.ID_OBJETO_NO_VALIDO;
        this.tramiteList = new ArrayList<>();
        this.historialList = new ArrayList<>();
    }

    public GestionDeEscritura(Integer idGestion) {
        this.idGestion = idGestion;
    }

    public GestionDeEscritura(Integer idGestion, int numero, Date fechaInicio, String encabezado) {
        this.idGestion = idGestion;
        this.numero = numero;
        this.fechaInicio = fechaInicio;
        this.encabezado = encabezado;
    }

    public Integer getIdGestion() {
        return idGestion;
    }

    public void setIdGestion(Integer idGestion) {
        this.idGestion = idGestion;
    }

    public int getNumero() {
        return numero;
    }

    public void setNumero(int numero) {
        this.numero = numero;
    }

    public Date getFechaInicio() {
        return fechaInicio;
    }

    public void setFechaInicio(Date fechaInicio) {
        this.fechaInicio = fechaInicio;
    }

    public String getEncabezado() {
        return encabezado;
    }

    public void setEncabezado(String encabezado) {
        this.encabezado = encabezado;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }

    public Integer getNumeroArchivo() {
        return numeroArchivo;
    }

    public void setNumeroArchivo(Integer numeroArchivo) {
        this.numeroArchivo = numeroArchivo;
    }

    public Integer getNumeroBibliorato() {
        return numeroBibliorato;
    }

    public void setNumeroBibliorato(Integer numeroBibliorato) {
        this.numeroBibliorato = numeroBibliorato;
    }

    @XmlTransient
    public List<Historial> getHistorialList() {
        return historialList;
    }

    public void setHistorialList(List<Historial> historialList) {
        this.historialList = historialList;
    }

    public Persona getFkIdPersonaEscribano() {
        return fkIdPersonaEscribano;
    }

    public void setFkIdPersonaEscribano(Persona fkIdPersonaEscribano) {
        this.fkIdPersonaEscribano = fkIdPersonaEscribano;
    }

    @XmlTransient
    public List<Tramite> getTramiteList() {
        return tramiteList;
    }

    public void setTramiteList(List<Tramite> tramiteList) {
        this.tramiteList = tramiteList;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (idGestion != null ? idGestion.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof GestionDeEscritura))
        {
            return false;
        }
        GestionDeEscritura other = (GestionDeEscritura) object;
        if ((this.idGestion == null && other.idGestion != null) || (this.idGestion != null && !this.idGestion.equals(other.idGestion)))
        {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "GestionDeEscritura[ idGestion=" + idGestion + " ]"
                 + "[ numero=" + numero + " ]";
    }

    public void setAtributos(DtoGestionDeEscritura dtoGestion) throws DtoInvalidoException {

        this.setVersion(dtoGestion.getVersion());
        this.setIdGestion(dtoGestion.getIdGestion());
        this.setNumero(dtoGestion.getNumero());
        this.setEncabezado(dtoGestion.getEncabezado());
        this.setFechaInicio(dtoGestion.getFechaInicio());
        this.setObservaciones(dtoGestion.getObservaciones());
        this.setNumeroArchivo(dtoGestion.getNumeroArchivo());
        this.setNumeroBibliorato(dtoGestion.getNumeroBibliorato());

        if (dtoGestion.getPersonaEscribano()!=null){
        Persona escribano = new Persona();
        escribano.setAtributos(dtoGestion.getPersonaEscribano());
      
        this.setFkIdPersonaEscribano(escribano);
        }
        
        for (Iterator<DtoTramite> it = dtoGestion.getListaTramitesAsociados().iterator(); it.hasNext();)
        {
            DtoTramite dtoTramite = it.next();

            dtoTramite.setGestionDeEscritura(dtoGestion);
            dtoTramite.setInmueble(new DtoInmueble());

//            Tramite tramiteAsociado = new Tramite();
//            tramiteAsociado.setAtributos(dtoTramite);
//
//            this.getTramiteList().add(tramiteAsociado);
        }

        //Estado de la gestion
        EstadoDeGestion fkEstadoGestion = new EstadoDeGestion();
        fkEstadoGestion.setAtributo(dtoGestion.getEstado());
        this.setFkIdEstadoDeGestion(fkEstadoGestion);

    }

    public DtoGestionDeEscritura getDto() {
        DtoGestionDeEscritura dtoGestion = new DtoGestionDeEscritura();

        dtoGestion.setVersion(this.getVersion());
        dtoGestion.setIdGestion(this.getIdGestion());
        dtoGestion.setNumero(this.getNumero());
        dtoGestion.setEncabezado(this.getEncabezado());
        dtoGestion.setFechaInicio(this.getFechaInicio());
        dtoGestion.setObservaciones(this.getObservaciones());
        dtoGestion.setNumeroArchivo(this.getNumeroArchivo());
        dtoGestion.setNumeroBibliorato(this.getNumeroBibliorato());
        dtoGestion.setEstado(this.fkIdEstadoDeGestion.getDto());

        //Evito que se produzca un bucle, por esta razon esta el 
        //metodo getDtoEscribano y no getDto.
        dtoGestion.setPersonaEscribano(this.getDtoEscribano());

        //Tramites asociados a la gestion
        ArrayList<DtoTramite> listaDtoTramites = new ArrayList<>();
        if (!tramiteList.isEmpty())
        {
            for (int i = 0; i < tramiteList.size(); i++)
            {
                listaDtoTramites.add(this.getDtoTramite(tramiteList.get(i)));
            }
            dtoGestion.setListaTramitesAsociados(listaDtoTramites);
        }

        /*Personas asociadas a la gestion
         * Cargo los clientes asociados a la gestion, no descrimino por tRamite, 
         * esto signifca que se eliminan las personas duplicadas, 
         * debido a que una gestion tien mas de un tarmite, y un tarmite mas de una persona involucrada,
         * esto produce que se repitan las personas involucradas en la gesion
         * Atencion: para mejorar se puede filtrar que persona pertenece a que tramite, lo soporta
         */
        ArrayList<DtoPersona> listaDtoPersonas = new ArrayList<>();
        ArrayList<Integer> listaIdPersona = new ArrayList<>();

        if (!(this.tramiteList.isEmpty()))
        {
            for (int j = 0; j < tramiteList.size(); j++)
            {
                for (int i = 0; i < tramiteList.get(j).getPersonaList().size(); i++)
                {

                    DtoPersona miDtoPersona = this.getDtoPersonaInvolucrada(tramiteList.get(j).getPersonaList().get(i));

                    if (!listaIdPersona.contains(miDtoPersona.getIdPersona()))
                    {
                        listaIdPersona.add(miDtoPersona.getIdPersona());
                        listaDtoPersonas.add(miDtoPersona);
                    }
                }
            }

            dtoGestion.setListaClientesInvolucrados(listaDtoPersonas);
        }

        //Estado de la gestion
        DtoEstadoDeGestion estadoDto = new DtoEstadoDeGestion();
        estadoDto.setIdEstadoGestion(this.getFkIdEstadoDeGestion().getIdEstadoGestion());
        estadoDto.setNombre(fkIdEstadoDeGestion.getNombre());
        estadoDto.setObservaciones(fkIdEstadoDeGestion.getObservaciones());
        estadoDto.setVersion(fkIdEstadoDeGestion.getVersion());

        dtoGestion.setEstado(estadoDto);

        return dtoGestion;
    }

    public DtoPersona getDtoEscribano() {

        DtoPersona dtoPersona = new DtoPersona();

        //Version del objeto
        dtoPersona.setVersion(fkIdPersonaEscribano.getVersion());
        dtoPersona.setIdPersona(fkIdPersonaEscribano.getIdPersona());
        dtoPersona.setNombre(fkIdPersonaEscribano.getNombre());
        dtoPersona.setApellido(fkIdPersonaEscribano.getApellido());
        dtoPersona.setCuit(fkIdPersonaEscribano.getCuit());
        dtoPersona.setEmail(fkIdPersonaEscribano.getEMail());
        dtoPersona.setEsCliente(fkIdPersonaEscribano.getEsCliente());
        dtoPersona.setEstadoCivil(fkIdPersonaEscribano.getEstadoCivil());
        dtoPersona.setFechaNacimiento(fkIdPersonaEscribano.getFechaNacimiento());
        dtoPersona.setNacionalidad(fkIdPersonaEscribano.getNacionalidad());
        dtoPersona.setNumeroIdentificacion(fkIdPersonaEscribano.getNumeroIdentificacion());
        dtoPersona.setNumeroNupcias(fkIdPersonaEscribano.getNumeroNupcias());
        dtoPersona.setOcupacion(fkIdPersonaEscribano.getOcupacion());
        dtoPersona.setDomicilio(fkIdPersonaEscribano.getDomicilio());
        dtoPersona.setRegistroEscribano(fkIdPersonaEscribano.getRegistroEscribano());
        dtoPersona.setSexo(fkIdPersonaEscribano.getSexo());
        dtoPersona.setTelefono(fkIdPersonaEscribano.getTelefono());

        DtoTipoIdentificacion dtoTipoIdentificacion = new DtoTipoIdentificacion();
        dtoTipoIdentificacion.setIdTipoIdentificacion(fkIdPersonaEscribano.getFkIdTipoIdentificacion().getIdTipoIdentificacion());

        dtoPersona.setDtoTipoIdentificacion(dtoTipoIdentificacion);

        //Asocio el id_Fk_TipoIdentificacion con el nombre tipo de identificacion
        dtoTipoIdentificacion.setNombre(ControllerNegocio.getInstancia().asociarNombreTipoIdentificacion(dtoPersona));

        return dtoPersona;
    }

    public DtoTramite getDtoTramite(Tramite miTramite) {

        DtoTramite miDto = new DtoTramite();

        miDto.setIdTramite(miTramite.getIdTramite());
        miDto.setObservaciones(miTramite.getObservaciones());
        miDto.setTiposDeTramite(miTramite.getFkIdTipoTramite().getDto());

        if (miTramite.getFkIdInmueble() != null)
        {
            miDto.setInmueble(miTramite.getFkIdInmueble().getDto());
        }
        else
        {
            miDto.setInmueble(null);
        }

        return miDto;
    }

    public DtoPersona getDtoPersonaInvolucrada(Persona miPersona) {
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

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public EstadoDeGestion getFkIdEstadoDeGestion() {
        return fkIdEstadoDeGestion;
    }

    public void setFkIdEstadoDeGestion(EstadoDeGestion fkIdEstadoDeGestion) {
        this.fkIdEstadoDeGestion = fkIdEstadoDeGestion;
    }
}
