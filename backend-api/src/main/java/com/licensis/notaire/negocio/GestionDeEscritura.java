/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.licensis.notaire.negocio;

import com.licensis.notaire.dto.DtoEstadoDeGestion;
import com.licensis.notaire.dto.DtoGestionDeEscritura;
import com.licensis.notaire.dto.DtoInmueble;
import com.licensis.notaire.dto.DtoPerson;
import com.licensis.notaire.dto.DtoTipoIdentificacion;
import com.licensis.notaire.dto.DtoTramite;
import com.licensis.notaire.dto.exceptions.DtoInvalidoException;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.io.Serializable;
import org.springframework.data.domain.Persistable;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import jakarta.persistence.Basic;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.NamedQueries;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import jakarta.persistence.Version;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlTransient;

/**
 * Clase que representa un gestion de escritura.
 * <p>
 * REGLA DE NEGOCIO:
 * <p>
 *
 * <lo>
 * <li>El numero de carpeta es auto-incremental sugerido por el sistema, pero
 * puede ser
 * modificado por el usuario, donde se verifica que el numero indicado no exista
 * ya registrado.
 * </li> </lo>
 *
 *
 * @author User
 */
@Entity
@Table(name = "gestiones_de_escrituras")
@XmlRootElement
@NamedQueries({
        @NamedQuery(name = "GestionDeEscritura.findAll", query = "SELECT g FROM GestionDeEscritura g"),
        @NamedQuery(name = "GestionDeEscritura.findByIdGestion", query = "SELECT g FROM GestionDeEscritura g WHERE g.idGestion = :idGestion"),
        @NamedQuery(name = "GestionDeEscritura.findByNumero", query = "SELECT g FROM GestionDeEscritura g WHERE g.numero = :numero"),
        @NamedQuery(name = "GestionDeEscritura.findByFechaInicio", query = "SELECT g FROM GestionDeEscritura g WHERE g.fechaInicio = :fechaInicio")
})
public class GestionDeEscritura implements Serializable, Persistable<Integer> {

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
    @Column(name = "encabezado")
    private String encabezado;
    @Column(name = "observaciones")
    private String observaciones;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "fkIdGestion")
    private List<Historial> historialList;
    @JoinColumn(name = "fk_id_persona_escribano", referencedColumnName = "id")
    @ManyToOne(optional = false)
    private Person fkIdPersonaEscribano;
    @OneToMany(mappedBy = "fkIdGestion")
    private List<Tramite> tramiteList;
    @Column(name = "deuda_pendiente_al_archivar")
    private Boolean deudaPendienteAlArchivar;

    /**
     * Constructor por default para gestion de escritura. Asigna al ID y al numero
     * de gestion el
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
    @Override
    @com.fasterxml.jackson.annotation.JsonIgnore
    public Integer getId() {
        return idGestion;
    }

    // Overrides Spring Data's default isNew(), which infers "new" from a primitive
    // @Version field being 0 -- indistinguishable from an already-persisted row that
    // was never updated, causing deleteById()/delete() to silently no-op for it.
    @Override
    @com.fasterxml.jackson.annotation.JsonIgnore
    public boolean isNew() {
        return idGestion == null || idGestion.equals(ConstantesNegocio.ID_OBJETO_NO_VALIDO);
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

    @XmlTransient
    @JsonIgnore
    public List<Historial> getHistorialList() {
        return historialList;
    }

    public void setHistorialList(List<Historial> historialList) {
        this.historialList = historialList;
    }

    public Person getFkIdPersonaEscribano() {
        return fkIdPersonaEscribano;
    }

    public void setFkIdPersonaEscribano(Person fkIdPersonaEscribano) {
        this.fkIdPersonaEscribano = fkIdPersonaEscribano;
    }

    @XmlTransient
    @JsonIgnore
    public List<Tramite> getTramiteList() {
        return tramiteList;
    }

    public void setTramiteList(List<Tramite> tramiteList) {
        this.tramiteList = tramiteList;
    }

    public Boolean getDeudaPendienteAlArchivar() {
        return deudaPendienteAlArchivar;
    }

    public void setDeudaPendienteAlArchivar(Boolean deudaPendienteAlArchivar) {
        this.deudaPendienteAlArchivar = deudaPendienteAlArchivar;
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
        if (!(object instanceof GestionDeEscritura)) {
            return false;
        }
        GestionDeEscritura other = (GestionDeEscritura) object;
        if ((this.idGestion == null && other.idGestion != null)
                || (this.idGestion != null && !this.idGestion.equals(other.idGestion))) {
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

        if (dtoGestion.getPersonaEscribano() != null) {
            Person escribano = new Person();
            escribano.setAtributos(dtoGestion.getPersonaEscribano());

            this.setFkIdPersonaEscribano(escribano);
        }

        for (Iterator<DtoTramite> it = dtoGestion.getListaTramitesAsociados().iterator(); it.hasNext();) {
            DtoTramite dtoTramite = it.next();

            dtoTramite.setGestionDeEscritura(dtoGestion);
            dtoTramite.setInmueble(new DtoInmueble());
        }

        // Estado de la gestion
        EstadoDeGestion fkEstadoGestion = new EstadoDeGestion();
        fkEstadoGestion.setAtributo(dtoGestion.getEstado());
        this.setFkIdEstadoDeGestion(fkEstadoGestion);

    }

    @com.fasterxml.jackson.annotation.JsonIgnore
    public DtoGestionDeEscritura getDto() {
        DtoGestionDeEscritura dtoGestion = new DtoGestionDeEscritura();

        dtoGestion.setVersion(this.getVersion());
        dtoGestion.setIdGestion(this.getIdGestion());
        dtoGestion.setNumero(this.getNumero());
        dtoGestion.setEncabezado(this.getEncabezado());
        dtoGestion.setFechaInicio(this.getFechaInicio());
        dtoGestion.setObservaciones(this.getObservaciones());
        dtoGestion.setEstado(this.fkIdEstadoDeGestion.getDto());

        // Evito que se produzca un bucle, por esta razon esta el
        // metodo getDtoEscribano y no getDto.
        dtoGestion.setPersonaEscribano(this.getDtoEscribano());

        // Tramites asociados a la gestion
        ArrayList<DtoTramite> listaDtoTramites = new ArrayList<>();
        if (!tramiteList.isEmpty()) {
            for (int i = 0; i < tramiteList.size(); i++) {
                listaDtoTramites.add(this.getDtoTramite(tramiteList.get(i)));
            }
            dtoGestion.setListaTramitesAsociados(listaDtoTramites);
        }

        /*
         * Personas asociadas a la gestion
         * Cargo los clientes asociados a la gestion, no descrimino por tRamite,
         * esto signifca que se eliminan las personas duplicadas,
         * debido a que una gestion tien mas de un tarmite, y un tarmite mas de una
         * persona
         * involucrada,
         * esto produce que se repitan las personas involucradas en la gesion
         * Atencion: para mejorar se puede filtrar que persona pertenece a que tramite,
         * lo soporta
         */
        ArrayList<DtoPerson> listaDtoPersonas = new ArrayList<>();
        ArrayList<Integer> listaIdPersona = new ArrayList<>();

        if (!(this.tramiteList.isEmpty())) {
            for (int j = 0; j < tramiteList.size(); j++) {
                for (int i = 0; i < tramiteList.get(j).getPersonaList().size(); i++) {

                    DtoPerson miDtoPersona = this.getDtoPersonaInvolucrada(tramiteList.get(j).getPersonaList().get(i));

                    if (!listaIdPersona.contains(miDtoPersona.getId())) {
                        listaIdPersona.add(miDtoPersona.getId());
                        listaDtoPersonas.add(miDtoPersona);
                    }
                }
            }

            dtoGestion.setListaClientesInvolucrados(listaDtoPersonas);
        }

        // Estado de la gestion
        DtoEstadoDeGestion estadoDto = new DtoEstadoDeGestion();
        estadoDto.setIdEstadoGestion(this.getFkIdEstadoDeGestion().getIdEstadoGestion());
        estadoDto.setNombre(fkIdEstadoDeGestion.getNombre());
        estadoDto.setObservaciones(fkIdEstadoDeGestion.getObservaciones());
        estadoDto.setVersion(fkIdEstadoDeGestion.getVersion());

        dtoGestion.setEstado(estadoDto);

        return dtoGestion;
    }

    @com.fasterxml.jackson.annotation.JsonIgnore
    public DtoPerson getDtoEscribano() {

        DtoPerson dtoPersona = new DtoPerson();

        // Version del objeto
        dtoPersona.setVersion(fkIdPersonaEscribano.getVersion());
        dtoPersona.setId(fkIdPersonaEscribano.getPersonId());
        dtoPersona.setFirstName(fkIdPersonaEscribano.getFirstName());
        dtoPersona.setLastName(fkIdPersonaEscribano.getLastName());
        dtoPersona.setTaxId(fkIdPersonaEscribano.getTaxId());
        dtoPersona.setEmail(fkIdPersonaEscribano.getEmail());
        dtoPersona.setIsClient(fkIdPersonaEscribano.getIsClient());
        dtoPersona.setMaritalStatus(fkIdPersonaEscribano.getMaritalStatus());
        dtoPersona.setBirthDate(fkIdPersonaEscribano.getBirthDate());
        dtoPersona.setNationality(fkIdPersonaEscribano.getNationality());
        dtoPersona.setIdentificationNumber(fkIdPersonaEscribano.getIdentificationNumber());
        dtoPersona.setMarriageCount(fkIdPersonaEscribano.getMarriageCount());
        dtoPersona.setOccupation(fkIdPersonaEscribano.getOccupation());
        dtoPersona.setAddress(fkIdPersonaEscribano.getAddress());
        dtoPersona.setNotaryRegistrationNumber(fkIdPersonaEscribano.getNotaryRegistrationNumber());
        dtoPersona.setSex(fkIdPersonaEscribano.getSex());
        dtoPersona.setPhone(fkIdPersonaEscribano.getPhone());

        DtoTipoIdentificacion dtoTipoIdentificacion = new DtoTipoIdentificacion();
        dtoTipoIdentificacion
                .setIdTipoIdentificacion(fkIdPersonaEscribano.getFkIdIdentificationType().getIdTipoIdentificacion());

        dtoPersona.setDtoTipoIdentificacion(dtoTipoIdentificacion);

        // Asocio el id_Fk_TipoIdentificacion con el nombre tipo de identificacion
        dtoTipoIdentificacion.setNombre(ControllerNegocio.getInstancia().asociarNombreTipoIdentificacion(dtoPersona));

        return dtoPersona;
    }

    public DtoTramite getDtoTramite(Tramite miTramite) {

        DtoTramite miDto = new DtoTramite();

        miDto.setIdTramite(miTramite.getIdTramite());
        miDto.setObservaciones(miTramite.getObservaciones());
        miDto.setTiposDeTramite(miTramite.getFkIdTipoTramite().getDto());

        if (miTramite.getFkIdInmueble() != null) {
            miDto.setInmueble(miTramite.getFkIdInmueble().getDto());
        } else {
            miDto.setInmueble(null);
        }

        return miDto;
    }

    public DtoPerson getDtoPersonaInvolucrada(Person miPersona) {
        DtoPerson dtoPersona = new DtoPerson();

        // Version del objeto
        dtoPersona.setVersion(miPersona.getVersion());
        dtoPersona.setId(miPersona.getPersonId());
        dtoPersona.setFirstName(miPersona.getFirstName());
        dtoPersona.setLastName(miPersona.getLastName());
        dtoPersona.setTaxId(miPersona.getTaxId());
        dtoPersona.setEmail(miPersona.getEmail());
        dtoPersona.setIsClient(miPersona.getIsClient());
        dtoPersona.setMaritalStatus(miPersona.getMaritalStatus());
        dtoPersona.setBirthDate(miPersona.getBirthDate());
        dtoPersona.setNationality(miPersona.getNationality());
        dtoPersona.setIdentificationNumber(miPersona.getIdentificationNumber());
        dtoPersona.setMarriageCount(miPersona.getMarriageCount());
        dtoPersona.setOccupation(miPersona.getOccupation());
        dtoPersona.setAddress(miPersona.getAddress());
        dtoPersona.setNotaryRegistrationNumber(miPersona.getNotaryRegistrationNumber());
        dtoPersona.setSex(miPersona.getSex());
        dtoPersona.setPhone(miPersona.getPhone());

        DtoTipoIdentificacion dtoTipoIdentificacion = new DtoTipoIdentificacion();
        dtoTipoIdentificacion.setIdTipoIdentificacion(miPersona.getFkIdIdentificationType().getIdTipoIdentificacion());

        dtoPersona.setDtoTipoIdentificacion(dtoTipoIdentificacion);

        // Asocio el id_Fk_TipoIdentificacion con el nombre tipo de identificacion
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
