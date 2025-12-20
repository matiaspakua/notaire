/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.licensis.notaire.negocio;

import com.licensis.notaire.dto.DtoGestionDeEscritura;
import com.licensis.notaire.dto.DtoPersona;
import com.licensis.notaire.dto.DtoTipoIdentificacion;
import com.licensis.notaire.dto.DtoTramite;
import java.io.Serializable;
import java.util.*;
import javax.persistence.*;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import org.hibernate.LazyInitializationException;

/**
 * Clase que representa a la entidad persona (general).
 * <p>
 * DISEÑO DEL SISTEMA
 * <p>
 * + Tanto los
 * usuarios del sistema, como los escribanos y los clientes, son "personas". Esta clase representa,
 * mendiante el valor de derminados atributos, cada una de estas entidades.
 * <p>
 * REGLA DE NEGOCIO
 * <p>
 * + Si una instancia de personas, solo tiene asignado los siguientes valores: nombre, apellido,
 * telefono, domicilio, tipo y numero de identificado e e-mail, la instancia representa a una
 * persona que tiene presupuestos asociados (No es un cliente).
 * <p>
 * + Si una instancia de persona,
 * tiene ademas de los atributos de personas, los demas atributos (a excepcion del numero de
 * registro) asignado, entonces se trata de un "cliente" y por lo tanto, debe tener gestiones
 * asociadas.
 * <p>
 * + Si una instancia de personas, tiene todos los atributos asignados, pero ademas
 * posee un numero de registro, entonces se trata de un "escribano".
 * <p>
 *
 * @author juanca
 */
@Entity
@Table(name = "personas")
@XmlRootElement
@NamedQueries(
        {
            @NamedQuery(name = "Persona.findAll", query = "SELECT p FROM Persona p"),
            @NamedQuery(name = "Persona.findByIdPersona", query = "SELECT p FROM Persona p WHERE p.idPersona = :idPersona"),
            @NamedQuery(name = "Persona.findByNumeroIdentificacion", query = "SELECT p FROM Persona p WHERE p.numeroIdentificacion = :numeroIdentificacion"),
            @NamedQuery(name = "Persona.findBySexo", query = "SELECT p FROM Persona p WHERE p.sexo = :sexo"),
            @NamedQuery(name = "Persona.findByFechaNacimiento", query = "SELECT p FROM Persona p WHERE p.fechaNacimiento = :fechaNacimiento"),
            @NamedQuery(name = "Persona.findByNumeroNupcias", query = "SELECT p FROM Persona p WHERE p.numeroNupcias = :numeroNupcias"),
            @NamedQuery(name = "Persona.findByRegistroEscribano", query = "SELECT p FROM Persona p WHERE p.registroEscribano = :registroEscribano"),
            @NamedQuery(name = "Persona.findByEsCliente", query = "SELECT p FROM Persona p WHERE p.esCliente = :esCliente"),
            @NamedQuery(name = "Persona.findByPersonaNombreApellido", query = "SELECT p FROM Persona p WHERE p.nombre LIKE :nombre and p.apellido LIKE :apellido"),
        })
public class Persona implements Serializable
{

    @Column(name = "fecha_nacimiento")
    @Temporal(TemporalType.DATE)
    private Date fechaNacimiento;
    @Basic(optional = false)
    @Column(name = "version")
    @Version
    private int version;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "persona", fetch = FetchType.LAZY)
    private List<TramitesPersonas> tramitesPersonasList;
    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "id_persona")
    private Integer idPersona;
    @Basic(optional = false)
    @Lob
    @Column(name = "nombre")
    private String nombre;
    @Basic(optional = false)
    @Lob
    @Column(name = "apellido")
    private String apellido;
    @Lob
    @Column(name = "nacionalidad")
    private String nacionalidad;
    @Basic(optional = false)
    @Column(name = "numero_identificacion")
    private String numeroIdentificacion;
    @Lob
    @Column(name = "cuit")
    private String cuit;
    @Column(name = "sexo")
    private String sexo;
    @Lob
    @Column(name = "estado_civil")
    private String estadoCivil;
    @Column(name = "numero_nupcias")
    private Integer numeroNupcias;
    @Lob
    @Column(name = "ocupacion")
    private String ocupacion;
    @Lob
    @Column(name = "domicilio")
    private String domicilio;
    @Lob
    @Column(name = "telefono")
    private String telefono;
    @Lob
    @Column(name = "e_mail")
    private String eMail;
    @Column(name = "registro_escribano")
    private Integer registroEscribano;
    @Basic(optional = false)
    @Column(name = "es_cliente")
    private boolean esCliente;
    @ManyToMany(mappedBy = "personaList", fetch = FetchType.LAZY)
    private List<Tramite> tramiteList;
    @JoinColumn(name = "fk_id_tipo_identificacion", referencedColumnName = "id_tipo_identificacion")
    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    private TipoIdentificacion fkIdTipoIdentificacion;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "fkIdPersona", fetch = FetchType.LAZY)
    private List<Presupuesto> presupuestoList;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "fkIdPersonaEscribano", fetch = FetchType.LAZY)
    private List<GestionDeEscritura> GestionDeEscrituraList;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "fkIdPersonaEscribano", fetch = FetchType.LAZY)
    private List<Folio> folioList;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "fkIdSuplente", fetch = FetchType.LAZY)
    private List<Suplencia> suplenciaList;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "fkIdSuplantado", fetch = FetchType.LAZY)
    private List<Suplencia> suplenciaList1;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "fkIdPersona", fetch = FetchType.LAZY)
    private List<Usuario> usuariosList;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "fkIdPersona", fetch = FetchType.LAZY)
    private List<Copia> copiaList;

    public Persona()
    {
    }

    public Persona(Integer idPersona)
    {
        this.idPersona = idPersona;
    }

    public Persona(Integer idPersona, String nombre, String apellido, String numeroIdentificacion, boolean esCliente)
    {
        this.idPersona = idPersona;
        this.nombre = nombre;
        this.apellido = apellido;
        this.numeroIdentificacion = numeroIdentificacion;
        this.esCliente = esCliente;
    }

    public Integer getIdPersona()
    {
        return idPersona;
    }

    public void setIdPersona(Integer idPersona)
    {
        this.idPersona = idPersona;
    }

    public String getNombre()
    {
        return nombre;
    }

    public void setNombre(String nombre)
    {
        this.nombre = nombre;
    }

    public String getApellido()
    {
        return apellido;
    }

    public void setApellido(String apellido)
    {
        this.apellido = apellido;
    }

    public String getNacionalidad()
    {
        return nacionalidad;
    }

    public void setNacionalidad(String nacionalidad)
    {
        this.nacionalidad = nacionalidad;
    }

    public String getNumeroIdentificacion()
    {
        return numeroIdentificacion;
    }

    public void setNumeroIdentificacion(String numeroIdentificacion)
    {
        this.numeroIdentificacion = numeroIdentificacion;
    }

    public String getCuit()
    {
        return cuit;
    }

    public void setCuit(String cuit)
    {
        this.cuit = cuit;
    }

    public String getSexo()
    {
        return sexo;
    }

    public void setSexo(String sexo)
    {
        this.sexo = sexo;
    }

    public Date getFechaNacimiento()
    {
        return fechaNacimiento;
    }

    public void setFechaNacimiento(Date fechaNacimiento)
    {
        this.fechaNacimiento = fechaNacimiento;
    }

    public String getEstadoCivil()
    {
        return estadoCivil;
    }

    public void setEstadoCivil(String estadoCivil)
    {
        this.estadoCivil = estadoCivil;
    }

    public Integer getNumeroNupcias()
    {
        return numeroNupcias;
    }

    public void setNumeroNupcias(Integer numeroNupcias)
    {
        this.numeroNupcias = numeroNupcias;
    }

    public String getOcupacion()
    {
        return ocupacion;
    }

    public void setOcupacion(String ocupacion)
    {
        this.ocupacion = ocupacion;
    }

    public String getDomicilio()
    {
        return domicilio;
    }

    public void setDomicilio(String domicilio)
    {
        this.domicilio = domicilio;
    }

    public String getTelefono()
    {
        return telefono;
    }

    public void setTelefono(String telefono)
    {
        this.telefono = telefono;
    }

    public String getEMail()
    {
        return eMail;
    }

    public void setEMail(String eMail)
    {
        this.eMail = eMail;
    }

    public Integer getRegistroEscribano()
    {
        return registroEscribano;
    }

    public void setRegistroEscribano(Integer registroEscribano)
    {
        this.registroEscribano = registroEscribano;
    }

    public boolean getEsCliente()
    {
        return esCliente;
    }

    public void setEsCliente(boolean esCliente)
    {
        this.esCliente = esCliente;
    }

    @XmlTransient
    public List<Tramite> getTramiteList()
    {
        return tramiteList;
    }

    public void setTramiteList(List<Tramite> tramiteList)
    {
        this.tramiteList = tramiteList;
    }

    public TipoIdentificacion getFkIdTipoIdentificacion()
    {
        return fkIdTipoIdentificacion;
    }

    public void setFkIdTipoIdentificacion(TipoIdentificacion fkIdTipoIdentificacion)
    {
        this.fkIdTipoIdentificacion = fkIdTipoIdentificacion;
    }

    @XmlTransient
    public List<Presupuesto> getPresupuestoList()
    {
        return presupuestoList;
    }

    public void setPresupuestoList(List<Presupuesto> presupuestoList)
    {
        this.presupuestoList = presupuestoList;
    }

    @XmlTransient
    public List<GestionDeEscritura> getGestionDeEscrituraList()
    {
        return GestionDeEscrituraList;
    }

    public void setGestionDeEscrituraList(List<GestionDeEscritura> GestionDeEscrituraList)
    {
        this.GestionDeEscrituraList = GestionDeEscrituraList;
    }

    @XmlTransient
    public List<Folio> getFolioList()
    {
        return folioList;
    }

    public void setFolioList(List<Folio> folioList)
    {
        this.folioList = folioList;
    }

    @XmlTransient
    public List<Suplencia> getSuplenciaList()
    {
        return suplenciaList;
    }

    public void setSuplenciaList(List<Suplencia> suplenciaList)
    {
        this.suplenciaList = suplenciaList;
    }

    @XmlTransient
    public List<Suplencia> getSuplenciaList1()
    {
        return suplenciaList1;
    }

    public void setSuplenciaList1(List<Suplencia> suplenciaList1)
    {
        this.suplenciaList1 = suplenciaList1;
    }

    @XmlTransient
    public List<Usuario> getUsuariosList()
    {
        return usuariosList;
    }

    public void setUsuariosList(List<Usuario> usuariosList)
    {
        this.usuariosList = usuariosList;
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

    @Override
    public int hashCode()
    {
        int hash = 0;
        hash += (idPersona != null ? idPersona.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object)
    {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof Persona))
        {
            return false;
        }
        Persona other = (Persona) object;
        if ((this.idPersona == null && other.idPersona != null) || (this.idPersona != null && !this.idPersona.equals(other.idPersona)))
        {
            return false;
        }
        return true;
    }

    @Override
    public String toString()
    {
        return "Persona[ idPersona=" + idPersona + " ]"
                + "[ nombre=" + nombre + " ]"
                + "[ apellido=" + apellido + " ]";
    }

    public DtoPersona getDto()
    {

        DtoPersona dtoPersona = new DtoPersona();

        //Version del objeto
        dtoPersona.setVersion(this.version);
        dtoPersona.setIdPersona(this.idPersona);
        dtoPersona.setNombre(this.nombre);
        dtoPersona.setApellido(this.apellido);
        dtoPersona.setCuit(this.cuit);
        dtoPersona.setEmail(this.eMail);
        dtoPersona.setEsCliente(this.esCliente);
        dtoPersona.setEstadoCivil(this.estadoCivil);
        dtoPersona.setFechaNacimiento(this.fechaNacimiento);
        dtoPersona.setNacionalidad(this.nacionalidad);
        dtoPersona.setNumeroIdentificacion(this.getNumeroIdentificacion());
        dtoPersona.setNumeroNupcias(this.numeroNupcias);
        dtoPersona.setOcupacion(this.ocupacion);
        dtoPersona.setDomicilio(this.domicilio);
        dtoPersona.setRegistroEscribano(this.registroEscribano);
        dtoPersona.setSexo(this.sexo);
        dtoPersona.setTelefono(this.telefono);

        DtoTipoIdentificacion dtoTipoIdentificacion = new DtoTipoIdentificacion();
        dtoTipoIdentificacion.setIdTipoIdentificacion(getFkIdTipoIdentificacion().getIdTipoIdentificacion());

        dtoPersona.setDtoTipoIdentificacion(dtoTipoIdentificacion);

        //Asocio el id_Fk_TipoIdentificacion con el nombre tipo de identificacion
        dtoTipoIdentificacion.setNombre(ControllerNegocio.getInstancia().asociarNombreTipoIdentificacion(dtoPersona));

        //Asocio la lista de gestiones que tiene la persona si es  Escribano
        if (this.getRegistroEscribano() != null)
        {
            ArrayList<DtoGestionDeEscritura> miListaDtoGestionEscribano = new ArrayList<>();

            if (!this.GestionDeEscrituraList.isEmpty())
            {
                for (int i = 0; i < this.GestionDeEscrituraList.size(); i++)
                {
                    DtoGestionDeEscritura dtoGestionDeEscritura = this.GestionDeEscrituraList.get(i).getDto();
                    miListaDtoGestionEscribano.add(dtoGestionDeEscritura);
                }
                dtoPersona.setListDtoGestionDeEscriturasDeEscribano(miListaDtoGestionEscribano);
            }
        }

        //Asocio la lista de tramites que pertenece a la persona
        ArrayList<DtoTramite> miListaDtoTramites = new ArrayList<>();
        if (this.tramiteList != null && !this.tramiteList.isEmpty())
        {
            for (int i = 0; i < this.tramiteList.size(); i++)
            {
                DtoTramite dtoTramite = this.tramiteList.get(i).getDto();
                miListaDtoTramites.add(dtoTramite);
            }
            dtoPersona.setListaTramitesPersona(miListaDtoTramites);
        }

        //Asocio la lista de gestiones que tiene la persona
        ArrayList<DtoGestionDeEscritura> miListaDtoGestionPersona = new ArrayList<>();
        ArrayList<Integer> listaIdGestiones = new ArrayList<>();

        try
        {
            if (tramiteList != null && !tramiteList.isEmpty())
            {
                for (int i = 0; i < this.tramiteList.size(); i++)
                {
                    //esto retorna tantas gestiones como tramites tenga la persona, si hay una tramite con una persona, esa persona tiene gestion
                    //si es otro tramite pero de la misma pgestion con la misma persona, repite la gestion
                    DtoGestionDeEscritura dtoGestionDeEscritura = this.tramiteList.get(i).getFkIdGestion().getDto();

                    //Elimino Gestiones duplicadas, a causa de los tramites
                    //Si retorna entero positivo esta, sino no.
                    if (!listaIdGestiones.contains(dtoGestionDeEscritura.getIdGestion()))
                    {
                        listaIdGestiones.add(dtoGestionDeEscritura.getIdGestion());
                        miListaDtoGestionPersona.add(dtoGestionDeEscritura);
                    }
                }
                dtoPersona.setListaDtoGestionDeEscriturasPersona(miListaDtoGestionPersona);
            }
        }
        catch (LazyInitializationException ex)
        {
            dtoPersona.setListaDtoGestionDeEscriturasPersona(miListaDtoGestionPersona);
        }
        return dtoPersona;
    }

    public ArrayList<DtoGestionDeEscritura> elimimarDuplicados(ArrayList<DtoGestionDeEscritura> listaDtoEscritura)
    {

        //Creamos un objeto HashSet
        HashSet hs = new HashSet();

        //Lo cargamos con los valores del array, esto hace quite los repetidos
        hs.addAll(listaDtoEscritura);

        //Limpiamos el array
        listaDtoEscritura.clear();
        listaDtoEscritura.addAll(hs);

        return listaDtoEscritura;

    }

    public void setAtributos(DtoPersona dtoPersona)
    {

        //Version del objeto
        this.setVersion(dtoPersona.getVersion());

        this.setNombre(dtoPersona.getNombre());
        this.setApellido(dtoPersona.getApellido());
        this.setTelefono(dtoPersona.getTelefono());
        this.setEMail(dtoPersona.getEmail());
        this.setIdPersona(dtoPersona.getIdPersona());
        this.setNumeroIdentificacion(dtoPersona.getNumeroIdentificacion());

        TipoIdentificacion tipoIdentificacion = new TipoIdentificacion();
        tipoIdentificacion.setIdTipoIdentificacion(dtoPersona.getDtoTipoIdentificacion().getIdTipoIdentificacion());
        tipoIdentificacion.setNombre(dtoPersona.getDtoTipoIdentificacion().getNombre());
        this.setFkIdTipoIdentificacion(tipoIdentificacion);

        //Set atributos Cliente
        if (dtoPersona.getEsCliente())
        {
            this.setNacionalidad(dtoPersona.getNacionalidad());
            this.setFechaNacimiento(dtoPersona.getFechaNacimiento());
            this.setCuit(dtoPersona.getCuit());
            this.setEstadoCivil(dtoPersona.getEstadoCivil());
            this.setNumeroNupcias(dtoPersona.getNumeroNupcias());
            this.setSexo(dtoPersona.getSexo());
            this.setOcupacion(dtoPersona.getOcupacion());
            this.setDomicilio(dtoPersona.getDomicilio());
            this.setEsCliente(dtoPersona.getEsCliente());
        }

    }

    @XmlTransient
    public List<TramitesPersonas> getTramitesPersonasList()
    {
        return tramitesPersonasList;
    }

    public void setTramitesPersonasList(List<TramitesPersonas> tramitesPersonasList)
    {
        this.tramitesPersonasList = tramitesPersonasList;
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
