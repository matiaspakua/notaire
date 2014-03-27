/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.licensis.notaire.negocio;

import com.licensis.notaire.dto.DtoConcepto;
import com.licensis.notaire.dto.DtoCopia;
import com.licensis.notaire.dto.DtoDocumentoPresentado;
import com.licensis.notaire.dto.DtoEscritura;
import com.licensis.notaire.dto.DtoEstadoDeGestion;
import com.licensis.notaire.dto.DtoFlag;
import com.licensis.notaire.dto.DtoFolio;
import com.licensis.notaire.dto.DtoGestionDeEscritura;
import com.licensis.notaire.dto.DtoHistorial;
import com.licensis.notaire.dto.DtoInmueble;
import com.licensis.notaire.dto.DtoItem;
import com.licensis.notaire.dto.DtoMovimientoTestimonio;
import com.licensis.notaire.dto.DtoPago;
import com.licensis.notaire.dto.DtoPersona;
import com.licensis.notaire.dto.DtoPlantillaPresupuesto;
import com.licensis.notaire.dto.DtoPlantillaTramite;
import com.licensis.notaire.dto.DtoPresupuesto;
import com.licensis.notaire.dto.DtoRegistroAuditoria;
import com.licensis.notaire.dto.DtoSuplencia;
import com.licensis.notaire.dto.DtoTestimonio;
import com.licensis.notaire.dto.DtoTipoDeDocumento;
import com.licensis.notaire.dto.DtoTipoDeFolio;
import com.licensis.notaire.dto.DtoTipoDeTramite;
import com.licensis.notaire.dto.DtoTipoIdentificacion;
import com.licensis.notaire.dto.DtoTramite;
import com.licensis.notaire.dto.DtoUsuario;
import com.licensis.notaire.dto.exceptions.DtoInvalidoException;
import com.licensis.notaire.gui.ConstantesGui;
import com.licensis.notaire.jpa.ConceptoJpaController;
import com.licensis.notaire.jpa.ConstantesPersistencia;
import com.licensis.notaire.jpa.CopiaJpaController;
import com.licensis.notaire.jpa.DocumentoPresentadoJpaController;
import com.licensis.notaire.jpa.EscrituraJpaController;
import com.licensis.notaire.jpa.EstadoDeGestionJpaController;
import com.licensis.notaire.jpa.FolioJpaController;
import com.licensis.notaire.jpa.GestionDeEscrituraJpaController;
import com.licensis.notaire.jpa.HistorialJpaController;
import com.licensis.notaire.jpa.InmuebleJpaController;
import com.licensis.notaire.jpa.ItemJpaController;
import com.licensis.notaire.jpa.MovimientoTestimonioJpaController;
import com.licensis.notaire.jpa.PagoJpaController;
import com.licensis.notaire.jpa.PersonaJpaController;
import com.licensis.notaire.jpa.PlantillaPresupuestoJpaController;
import com.licensis.notaire.jpa.PlantillaTramiteJpaController;
import com.licensis.notaire.jpa.PresupuestoJpaController;
import com.licensis.notaire.jpa.RegistroAuditoriaJpaController;
import com.licensis.notaire.jpa.SuplenciaJpaController;
import com.licensis.notaire.jpa.TestimonioJpaController;
import com.licensis.notaire.jpa.TipoDeDocumentoJpaController;
import com.licensis.notaire.jpa.TipoDeFolioJpaController;
import com.licensis.notaire.jpa.TipoDeTramiteJpaController;
import com.licensis.notaire.jpa.TipoIdentificacionJpaController;
import com.licensis.notaire.jpa.TramiteJpaController;
import com.licensis.notaire.jpa.TramitesPersonasJpaController;
import com.licensis.notaire.jpa.UsuarioJpaController;
import com.licensis.notaire.jpa.exceptions.ClassEliminatedException;
import com.licensis.notaire.jpa.exceptions.ClassModifiedException;
import com.licensis.notaire.jpa.exceptions.CreateEntityException;
import com.licensis.notaire.jpa.exceptions.IllegalOrphanException;
import com.licensis.notaire.jpa.exceptions.NonexistentEntityException;
import com.licensis.notaire.jpa.exceptions.NonexistentJpaException;
import com.licensis.notaire.jpa.exceptions.PreexistingEntityException;
import com.licensis.notaire.servicios.AdministradorJpa;
import com.licensis.notaire.servicios.AdministradorSesion;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.persistence.PersistenceException;

/**
 * Clase controller de la capa de negocio. Administra toda la logica de la capa
 * de negocio.
 *
 *
 * @author User
 */
public class ControllerNegocio
{

// <editor-fold defaultstate="collapsed" desc="ATRIBUTOS">
    private static ControllerNegocio instancia = null;
    private RegistroAuditoriaJpaController miJpaAuditoria;
    private TipoDeDocumentoJpaController miJpaTipoDocumento;
    private TipoDeTramiteJpaController miJpaTipoTramite;
    private PlantillaTramiteJpaController miJpaPlantillaTramite;
    private PlantillaPresupuestoJpaController miJpaPlantillaPresupuesto;
    private ConceptoJpaController miJpaConcepto;
    private EstadoDeGestionJpaController miJpaEstadoDeGestion;
    private TipoDeFolioJpaController miJpaTipoDeFolio;
    private PersonaJpaController miJpaPersona;
    private SuplenciaJpaController miJpaSuplencia;
    private AdministradorJpa miAdministradorJpa = null;
    private InmuebleJpaController miJpaInmueble;
    private TramiteJpaController miJpaTramite;
    private TramitesPersonasJpaController miJpaTramitesPersonas;
    private ItemJpaController miJpaItem;
    private PresupuestoJpaController miJpaPresupuesto;
    private PagoJpaController miJpaPago;
    private GestionDeEscrituraJpaController miJpaGestionDeEscritura;
    private FolioJpaController miJpaFolio;
    private EscrituraJpaController miJpaEscritura;
    private HistorialJpaController miJpaHistorial;
    private CopiaJpaController miJpaCopia;
    private TestimonioJpaController miJpaTestimonio;
    private MovimientoTestimonioJpaController miJpaMovimientoTestimonio;
    private DocumentoPresentadoJpaController miJpaDocumentoPresentado;
    private UsuarioJpaController miJpaUsuario;
    private TipoIdentificacionJpaController miJpaTipoIdentificacion;
    private DtoUsuario sesionUsuario;

// </editor-fold>
// <editor-fold defaultstate="collapsed" desc="CONSTRUCTOR">
    private ControllerNegocio()
    {
        try
        {
            miAdministradorJpa = AdministradorJpa.getInstancia();
            miJpaAuditoria = (RegistroAuditoriaJpaController) AdministradorJpa.getInstancia().obtenerJpa(RegistroAuditoriaJpaController.class.getName());
            miJpaInmueble = (InmuebleJpaController) AdministradorJpa.getInstancia().obtenerJpa(InmuebleJpaController.class.getName());
            miJpaTramite = (TramiteJpaController) AdministradorJpa.getInstancia().obtenerJpa(TramiteJpaController.class.getName());
            miJpaTramitesPersonas = (TramitesPersonasJpaController) AdministradorJpa.getInstancia().obtenerJpa(TramitesPersonasJpaController.class.getName());
            miJpaItem = (ItemJpaController) AdministradorJpa.getInstancia().obtenerJpa(ItemJpaController.class.getName());
            miJpaPresupuesto = (PresupuestoJpaController) AdministradorJpa.getInstancia().obtenerJpa(PresupuestoJpaController.class.getName());
            miJpaPersona = (PersonaJpaController) AdministradorJpa.getInstancia().obtenerJpa(PersonaJpaController.class.getName());
            miJpaPlantillaPresupuesto = (PlantillaPresupuestoJpaController) AdministradorJpa.getInstancia().obtenerJpa(PlantillaPresupuestoJpaController.class.getName());
            miJpaTipoDocumento = (TipoDeDocumentoJpaController) AdministradorJpa.getInstancia().obtenerJpa(TipoDeDocumentoJpaController.class.getName());
            miJpaTipoTramite = (TipoDeTramiteJpaController) AdministradorJpa.getInstancia().obtenerJpa(TipoDeTramiteJpaController.class.getName());
            miJpaPlantillaTramite = (PlantillaTramiteJpaController) AdministradorJpa.getInstancia().obtenerJpa(PlantillaTramiteJpaController.class.getName());
            miJpaConcepto = (ConceptoJpaController) AdministradorJpa.getInstancia().obtenerJpa(ConceptoJpaController.class.getName());
            miJpaEstadoDeGestion = (EstadoDeGestionJpaController) AdministradorJpa.getInstancia().obtenerJpa(EstadoDeGestionJpaController.class.getName());
            miJpaTipoDeFolio = (TipoDeFolioJpaController) AdministradorJpa.getInstancia().obtenerJpa(TipoDeFolioJpaController.class.getName());
            miJpaSuplencia = (SuplenciaJpaController) AdministradorJpa.getInstancia().obtenerJpa(SuplenciaJpaController.class.getName());
            miJpaPago = (PagoJpaController) AdministradorJpa.getInstancia().obtenerJpa(PagoJpaController.class.getName());
            miJpaFolio = (FolioJpaController) AdministradorJpa.getInstancia().obtenerJpa(FolioJpaController.class.getName());
            miJpaGestionDeEscritura = (GestionDeEscrituraJpaController) AdministradorJpa.getInstancia().obtenerJpa(GestionDeEscrituraJpaController.class.getName());
            miJpaEscritura = (EscrituraJpaController) AdministradorJpa.getInstancia().obtenerJpa(EscrituraJpaController.class.getName());
            miJpaHistorial = (HistorialJpaController) AdministradorJpa.getInstancia().obtenerJpa(HistorialJpaController.class.getName());
            miJpaCopia = (CopiaJpaController) AdministradorJpa.getInstancia().obtenerJpa(CopiaJpaController.class.getName());
            miJpaTestimonio = (TestimonioJpaController) AdministradorJpa.getInstancia().obtenerJpa(TestimonioJpaController.class.getName());
            miJpaMovimientoTestimonio = (MovimientoTestimonioJpaController) AdministradorJpa.getInstancia().obtenerJpa(MovimientoTestimonioJpaController.class.getName());
            miJpaDocumentoPresentado = (DocumentoPresentadoJpaController) AdministradorJpa.getInstancia().obtenerJpa(DocumentoPresentadoJpaController.class.getName());
            miJpaUsuario = (UsuarioJpaController) AdministradorJpa.getInstancia().obtenerJpa(UsuarioJpaController.class.getName());
            miJpaTipoIdentificacion = (TipoIdentificacionJpaController) AdministradorJpa.getInstancia().obtenerJpa(TipoIdentificacionJpaController.class.getName());
        } catch (NonexistentJpaException ex)
        {
            ex.printStackTrace();
        }
    }

    public static ControllerNegocio getInstancia()
    {
        if (instancia == null)
        {
            instancia = new ControllerNegocio();
        }

        return instancia;
    }

    public AdministradorJpa getMiAdministradorJpa()
    {
        return miAdministradorJpa;
    }

    public void setMiAdministradorJpa(AdministradorJpa miAdministradorJpa)
    {
        this.miAdministradorJpa = miAdministradorJpa;
    }

    // </editor-fold>
// <editor-fold defaultstate="collapsed" desc="Login">
    // </editor-fold>
// <editor-fold defaultstate="collapsed" desc="Clientes">
    /**
     * Metodo que permite dar de alta una nueva persona
     *
     * @param dtoPersona La nueva persona para ser dada de alta.
     * @return Un DtoPersona para comprobar el resultado del metodo
     */
    public DtoPersona darAltaPersona(DtoPersona dtoPersona) throws NonexistentJpaException
    {

        //Set atributos persona
        Persona miPersona = new Persona();
        miPersona.setAtributos(dtoPersona);

        //Persisto persona
        int oid = -1;

        //Llamo jpa Persona

        oid = miJpaPersona.create(miPersona);


        if (oid == -1)
        {
            dtoPersona = null;
        } else
        {
            this.registrarAuditoria(miPersona, ConstantesGui.DAR_ALTA_PERSONA);
        }

        return dtoPersona;
    }

    /**
     * Metodo que permite listar todos los tipos de identificaciones posibles
     *
     * @return Una ArrayList<DtoTipoIdentificacion>
     */
    public ArrayList<DtoTipoIdentificacion> listarTiposIdentificacion()
    {
        ArrayList<DtoTipoIdentificacion> listaDtoTipoIdentificaciones = new ArrayList<>();


        List<TipoIdentificacion> miListaTipotIdentificaciones = miJpaTipoIdentificacion.findTipoIdentificacionEntities();

        for (Iterator<TipoIdentificacion> it = miListaTipotIdentificaciones.iterator(); it.hasNext();)
        {
            TipoIdentificacion tipoIdentificacion = it.next();
            listaDtoTipoIdentificaciones.add(tipoIdentificacion.getDto());
        }

        return listaDtoTipoIdentificaciones;
    }

    /**
     * Metodo que permite saber si existe una persona, por su tipo y numero de
     * identificacien
     *
     * @param dtoPersona
     * @return Si existe o no la persona
     */
    public Boolean siExistePersona(DtoPersona dtoPersona)
    {
        Boolean existeEnPersistencia = false;

        //Busco el id del tipo de identificacion y lo asocio al dto persona
        dtoPersona.getDtoTipoIdentificacion().setIdTipoIdentificacion(this.asociarFkTipoIdentificacion(dtoPersona));

        Persona miPersona = PersonaJpaController.getInstancia().findPersonaTipoNumeroIdentificacion(dtoPersona);

        if (miPersona != null)
        {
            existeEnPersistencia = true;
        }

        return existeEnPersistencia;
    }

    /**
     * Metodo que permite busca una persona por tipo y numero de identificacien
     *
     * @param miDtoPersona
     * @return Un DtoPersona con la persona indicada
     */
    public DtoPersona buscarPersonaTipoNumeroIdentificacion(DtoPersona miDtoPersona)
    {

        Persona miPersona = null;

        //Busco la persona
        miPersona = miJpaPersona.findPersonaTipoNumeroIdentificacion(miDtoPersona);
        String apellido = miPersona.getApellido();
        if (miPersona != null && !apellido.equals(ConstantesGui.ADMINISTRADOR))
        {
            //Control Version del objeto
            miDtoPersona.setVersion(miPersona.getVersion());

            miDtoPersona.setIdPersona(miPersona.getIdPersona());
            miDtoPersona.setNombre(miPersona.getNombre());
            miDtoPersona.setApellido(miPersona.getApellido());
            miDtoPersona.setNumeroIdentificacion(miPersona.getNumeroIdentificacion());
            miDtoPersona.setTelefono(miPersona.getTelefono());
            miDtoPersona.setEmail(miPersona.getEMail());
            miDtoPersona.getDtoTipoIdentificacion().setNombre(miPersona.getFkIdTipoIdentificacion().getNombre());
            miDtoPersona.getDtoTipoIdentificacion().setIdTipoIdentificacion(miPersona.getFkIdTipoIdentificacion().getIdTipoIdentificacion());

            miDtoPersona.setNacionalidad(miPersona.getNacionalidad());
            miDtoPersona.setFechaNacimiento(miPersona.getFechaNacimiento());
            miDtoPersona.setCuit(miPersona.getCuit());
            miDtoPersona.setEstadoCivil(miPersona.getEstadoCivil());
            miDtoPersona.setNumeroNupcias(miPersona.getNumeroNupcias());
            miDtoPersona.setSexo(miPersona.getSexo());
            miDtoPersona.setOcupacion(miPersona.getOcupacion());
            miDtoPersona.setDomicilio(miPersona.getDomicilio());

            miDtoPersona.setEsCliente(miPersona.getEsCliente());

            miDtoPersona.setRegistroEscribano(miPersona.getRegistroEscribano());
        } else
        {
            miDtoPersona = null;
        }

        return miDtoPersona;
    }

    /**
     * Metodo que permite busca una persona por tipo y numero de identificacien
     * que tenga una o varias gestiones asociadas.
     *
     * @param miDtoPersona
     * @return
     */
    public DtoPersona buscarPersonaTipoNumeroIdentificacionConGestion(DtoPersona miDtoPersona)
    {

        Persona miPersona = null;

        //Busco la persona
        miPersona = PersonaJpaController.getInstancia().findPersonaTipoNumeroIdentificacion(miDtoPersona);
        String apellido = miPersona.getApellido();

        if (miPersona != null && miPersona.getGestionDeEscrituraList().size() > 0
                && !apellido.equals(ConstantesGui.ADMINISTRADOR))
        {
            miDtoPersona = miPersona.getDto();

        } else
        {
            miDtoPersona = null;
        }

        return miDtoPersona;
    }

    /**
     * Metodo que permite buscar personas, por aproximacion, con el nombre y
     * apellido.
     *
     * @param dtoPersona Un dto tipo persona, con el nombre y apellido cargados,
     * para buscar.
     * @return listaDtoPersonas Una lista de DtoPersona con todas las
     * coincidencias aproximadas.
     *
     */
    public ArrayList<DtoPersona> buscarPersonaNombreApellido(DtoPersona dtoPersona)
    {

        ArrayList<DtoPersona> listaDtoPersonas = new ArrayList<>();

        try
        {
            ArrayList<Persona> listaPersona = (ArrayList<Persona>) PersonaJpaController.getInstancia().findPersonaNombreApellido(dtoPersona);

            if (listaPersona != null)
            {
                listaDtoPersonas = new ArrayList<>();
                for (int i = 0; i < listaPersona.size(); i++)
                {
                    String apellido = listaPersona.get(i).getApellido();

                    if (!apellido.equals(ConstantesGui.ADMINISTRADOR))
                    {
                        listaDtoPersonas.add(listaPersona.get(i).getDto());
                    }
                }
            }
        } catch (PersistenceException e)
        {
            e.printStackTrace();
        }
        return listaDtoPersonas;
    }

    /**
     * Metodo que busca personas, por aproximacion, con el nombre y apellido,
     * que tengan asociada una gestion
     *
     * @param dtoPersona
     * @return
     */
    public ArrayList<DtoPersona> buscarPersonaNombreApellidoConGestion(DtoPersona dtoPersona)
    {

        ArrayList<DtoPersona> listaDtoPersonas = new ArrayList<>();
        ArrayList<DtoPersona> listaDtoPersonasConGestion = new ArrayList<>();
        ArrayList<Persona> listaPersona;

        try
        {
            listaPersona = (ArrayList<Persona>) PersonaJpaController.getInstancia().findPersonaNombreApellido(dtoPersona);

            if (listaPersona != null)
            {
                listaDtoPersonas = new ArrayList<>();

                //Armo lista de DtoPerona para tener toda la red de objetos
                for (int i = 0; i < listaPersona.size(); i++)
                {
                    dtoPersona = listaPersona.get(i).getDto();
                    listaDtoPersonas.add(dtoPersona);
                }

                //Recorro las personas buscando la que tienen gestion
                for (int i = 0; i < listaDtoPersonas.size(); i++)
                {
                    String apellido = listaDtoPersonas.get(i).getApellido();
                            
                    if (!listaDtoPersonas.get(i).getListaDtoGestionDeEscriturasPersona().isEmpty()
                            && !apellido.equals(ConstantesGui.ADMINISTRADOR))
                    {
                        DtoPersona dtoPersonaConGestion = listaDtoPersonas.get(i);
                        listaDtoPersonasConGestion.add(dtoPersonaConGestion);
                    }
                }
            }

        } catch (PersistenceException e)
        {
            e.printStackTrace();
        }
        return listaDtoPersonasConGestion;
    }

    /**
     * Metodo que permite busacar todas las personas y clientes
     *
     * @return listaDtoPersonas Una lista de DtoPersonas con todas las personas
     * registradas
     * @throws NonexistentJpaException
     */
    public ArrayList<DtoPersona> buscarPersonasClientes() throws NonexistentJpaException
    {

        ArrayList<DtoPersona> listaDtoPersonas = new ArrayList<>();
        List<Persona> listaPersona = new ArrayList<Persona>();

        //Llamo jpa Persona

        listaPersona = miJpaPersona.findPersonas();

        // TODO: corregir el null y la iteracioinicin.
        if (listaPersona != null)
        {
            listaDtoPersonas = new ArrayList<>();
            for (int i = 0; i < listaPersona.size(); i++)
            {
                String apellido = listaPersona.get(i).getApellido();
                if (!apellido.equals(ConstantesGui.ADMINISTRADOR))
                {
                    listaDtoPersonas.add(listaPersona.get(i).getDto());
                }

            }
        }

        return listaDtoPersonas;
    }

    /**
     * Metodo que permite modificar una persona
     *
     * @param dtoPersona La persona a ser modificada.
     * @return Un Dtopersona para comprobar la modificacion.
     */
    public DtoPersona modificarPersona(DtoPersona dtoPersona) throws ClassModifiedException, ClassEliminatedException
    {

        Persona miPersona = new Persona();

        miPersona.setAtributos(dtoPersona);

        boolean flag = miJpaPersona.modificarPersona(miPersona);

        if (!flag)
        {
            dtoPersona = null;
        } else
        {
            this.registrarAuditoria(miPersona, ConstantesGui.MODIFICAR_PERSONA);
        }


        return dtoPersona;
    }

    /**
     * Metodo que permite dar de alta un cliente
     *
     * @param dtoCliente El cliente a ser modificado.
     * @return Un Dtopersona para comprobar la modificacien.
     */
    public DtoPersona darAltaCliente(DtoPersona dtoCliente) throws ClassModifiedException, ClassEliminatedException
    {

        Persona miClientePersona = new Persona();

        miClientePersona.setAtributos(dtoCliente);

        boolean flag = miJpaPersona.modificarCliente(miClientePersona);

        if (!flag)
        {
            dtoCliente = null;
        } else
        {
            this.registrarAuditoria(miClientePersona, ConstantesGui.DAR_ALTA_CLIENTE);
        }


        return dtoCliente;
    }

    /**
     * Metodo que permite modificar un cliente
     *
     * @param dtoCliente El cliente a ser modificado.
     * @return Un Dtopersona para comprobar la modificacien.
     */
    public DtoPersona modificarCliente(DtoPersona dtoCliente) throws ClassModifiedException, ClassEliminatedException
    {

        Persona miClientePersona = new Persona();

        miClientePersona.setAtributos(dtoCliente);

        boolean flag = miJpaPersona.modificarCliente(miClientePersona);

        if (!flag)
        {
            dtoCliente = null;
        } else
        {
            this.registrarAuditoria(miClientePersona, ConstantesGui.MODIFICAR_CLIENTE);
        }


        return dtoCliente;
    }

    /**
     * Metodo permite asociar el nombre del tipo de identificacien con el
     * id_tipoDocumento correspondiente
     *
     * @param dtoPersona
     * @return Un int, es el id_fk_identificaciones del tipo elegido en el
     * combo, para ser posteriormente update
     */
    public int asociarFkTipoIdentificacion(DtoPersona dtoPersona)
    {
        int id_fk_tipo_identificacion = 0;

        //Busco el ID_tipo y creo creo el tipoIdentificacion y completo PK
        List<TipoIdentificacion> listaIdentificaciones = new ArrayList<TipoIdentificacion>();

        //Busco los tipo de identificaciones disponibles
        listaIdentificaciones = TipoIdentificacionJpaController.getInstancia().findTipoIdentificacionEntities();


        for (int i = 0; i < listaIdentificaciones.size(); i++)
        {
            if (listaIdentificaciones.get(i).getNombre().contains(dtoPersona.getDtoTipoIdentificacion().getNombre()))
            {
                id_fk_tipo_identificacion = listaIdentificaciones.get(i).getIdTipoIdentificacion();
            }
        }
        return id_fk_tipo_identificacion;
    }

    /**
     * Metodo que permite asociar el id_fk_tipoIdentificacion con su nombre
     * correspondiente
     *
     * @param dtoPersona
     * @return El nombre del tipo de identificacien, asociado a un
     * id_fk_tipoIdentificacion
     */
    public String asociarNombreTipoIdentificacion(DtoPersona dtoPersona)
    {
        int id_fk_tipo_identificacion = 0;

        //Busco el ID_tipo y creo creo el tipoIdentificacion y completo PK
        List<TipoIdentificacion> listaIdentificaciones = new ArrayList<TipoIdentificacion>();

        //Busco los tipo de identificaciones disponibles

        listaIdentificaciones = TipoIdentificacionJpaController.getInstancia().findTipoIdentificacionEntities();

        String nombre_tipo_identificacion = null;


        for (int i = 0; i < listaIdentificaciones.size(); i++)
        {
            if (listaIdentificaciones.get(i).getIdTipoIdentificacion().toString().contains(dtoPersona.getDtoTipoIdentificacion().getIdTipoIdentificacion().toString()))
            {
                nombre_tipo_identificacion = listaIdentificaciones.get(i).getNombre();
            }
        }
        return nombre_tipo_identificacion;
    }

    /**
     * Metodo que controla cuando se modifica una persona, que no existe otra
     * con el mismo tipo y numero de identificacien
     *
     * @param dtoPersonaModificada
     * @param dtoPersonaOrginal
     * @return El dtoPersona modificado
     */
    public Boolean controlModificacionPersona(DtoPersona dtoPersonaOrginal, DtoPersona dtoPersonaModificada)
    {
        Boolean flag = false;

        //Control si fue modificado el  tipo o numero de identificacien
        if (dtoPersonaModificada.getDtoTipoIdentificacion().getNombre().equals(dtoPersonaOrginal.getDtoTipoIdentificacion().getNombre()) == false
                || dtoPersonaModificada.getNumeroIdentificacion().equals(dtoPersonaOrginal.getNumeroIdentificacion()) == false)
        {
            Persona miPersona = miJpaPersona.findPersonaTipoNumeroIdentificacion(dtoPersonaModificada);

            if (miPersona != null)
            {
                flag = true;
            }

        }

        return flag;

    }

// </editor-fold>
// <editor-fold defaultstate="collapsed" desc="Presupuestos">
    /**
     * Busca un Inmueble por su Nomenclatura Catastral.
     *
     * @param miDtoInmueble, DtoInmueble con la Nomenclatura Catastral a buscar.
     * @return el Dto del Inmueble encontrado, o null en caso contrario.
     */
    public DtoInmueble buscarInmueble(DtoInmueble miDtoInmueble)
    {
        DtoInmueble dtoInmueble = null;


        Inmueble miInmueble = miJpaInmueble.findInmueble(miDtoInmueble);

        if (miInmueble != null)
        {
            dtoInmueble = miInmueble.getDto();
        }

        return dtoInmueble;
    }

    /**
     * Busca un inmueble asociado a un Tramite en particular.
     *
     * @param miDtoTramite, con los datos del Tramite, asociado al Inmueble a
     * buscar.
     * @return el Dto del Inmueble encontrado asociado al Tramite indicado.
     */
    public DtoInmueble buscarInmueble(DtoTramite miDtoTramite)
    {
        DtoInmueble dtoInmueble = null;

        Tramite miTramite = miJpaTramite.findTramite(miDtoTramite.getIdTramite());

        if (miTramite != null)
        {
            Inmueble miInmueble = miTramite.getFkIdInmueble();

            if (miInmueble != null)
            {
                dtoInmueble = miInmueble.getDto();
            }
        }

        return dtoInmueble;
    }

    /**
     * Busca los Conceptos asociados a un determinado Tipo de Tramite.
     *
     * @param dtoTipoTramite, Dto del Tipo de Tramite del cual buscar los
     * conceptos.
     * @return los Dto de los Conceptos asociado al Tipo de Tramite indicado.
     */
    public ArrayList<DtoConcepto> obtenerConceptosTramite(DtoTipoDeTramite dtoTipoTramite)
    {
        ArrayList<DtoConcepto> dtosConceptos = new ArrayList<>();
        ArrayList<PlantillaPresupuesto> plantillas = null;

        plantillas = (ArrayList<PlantillaPresupuesto>) miJpaPlantillaPresupuesto.findPlantillasDePresupuesto(dtoTipoTramite.getIdTipoTramite());

        if (plantillas != null)
        {
            for (int i = 0; i < plantillas.size(); i++)
            {
                PlantillaPresupuesto plantillaPresupuesto = plantillas.get(i);

                Concepto miConcepto = plantillaPresupuesto.getConcepto();

                dtosConceptos.add(miConcepto.getDto());
            }
        }

        return dtosConceptos;
    }

    /**
     * Crea un Presupuesto que tiene asociado un Inmueble.
     *
     * @param dtoPersona datos de la Persona que solicita el Presupuesto.
     * @param dtoPresupuesto datos del Presupuesto.
     * @param dtoTramite datos del Tramite asociado al Presupuesto.
     * @param dtoInmueble datos del Inmueble sobre el cual se realiza el
     * presupuesto.
     * @param dtosItems datos de los Items del Presupuesto.
     * @return el numero del Presupuesto creado.
     */
    public int crearPresupuesto(DtoPersona dtoPersona, DtoPresupuesto dtoPresupuesto, DtoTramite dtoTramite, DtoInmueble dtoInmueble, ArrayList<DtoItem> dtosItems)
    {
        int creado = -1;

        int idInmueble = darAltaInmueble(dtoInmueble);

        if (idInmueble != -1)
        {
            try
            {
                Inmueble miInmueble;
                miInmueble = miJpaInmueble.findInmueble(idInmueble);

                int idTramite = darAltaTramite(dtoTramite, miInmueble);

                if (idTramite != -1)
                {

                    Tramite miTramite = miJpaTramite.findTramite(idTramite);

                    Persona miPersona = miJpaPersona.findPersona(dtoPersona.getIdPersona());

                    //Creo el presupuesto:
                    Presupuesto miPresupuesto = new Presupuesto();

                    miPresupuesto.setFecha(Calendar.getInstance().getTime());
                    miPresupuesto.setFkIdTramite(miTramite);
                    miPresupuesto.setSaldo(dtoPresupuesto.getSaldo());
                    miPresupuesto.setTotal(dtoPresupuesto.getTotal());
                    miPresupuesto.setFkIdPersona(miPersona);
                    miPresupuesto.setObservaciones(dtoPresupuesto.getObservaciones());

                    creado = miJpaPresupuesto.create(miPresupuesto);

                    if (creado != -1)
                    {
                        miPresupuesto.setIdPresupuesto(creado);

                        //Actualizo el tramite:
                        miTramite.setFkIdPresupuesto(miPresupuesto);

                        Boolean modificado = miJpaTramite.asociarPresupuesto(miTramite);

                        if (modificado)
                        {
                            //Creo los Items:
                            ArrayList<Item> misItems = new ArrayList<>();

                            for (int i = 0; i < dtosItems.size(); i++)
                            {
                                DtoItem dtoItem = dtosItems.get(i);
                                Item miItem = new Item();

                                miItem.setAtributos(dtoItem);
                                miItem.setFkIdPresupuesto(miPresupuesto);

                                int id = miJpaItem.create(miItem);

                                if (id != -1)
                                {
                                    miItem.setIdItem(id);
                                    misItems.add(miItem);
                                }
                            }
                        }
                        //  registramos en auditoria en nuevo presupuesto
                        this.registrarAuditoria(miPresupuesto, ConstantesGui.CREAR_PRESUPUESTO);
                    }
                }

            } catch (Exception ex)
            {
                ex.printStackTrace();
            }
        }

        return creado;
    }

    /**
     * Crea un Presupuesto sin un Inmueble asociado.
     *
     * @param dtoPersona datos de la Persona que solicita el Presupuesto.
     * @param dtoPresupuesto datos del Presupuesto.
     * @param dtoTramite datos del Tramite asociado al Presupuesto.
     * @param dtosItems datos de los Items del Presupuesto.
     * @return el numero del Presupuesto creado.
     */
    public int crearPresupuesto(DtoPersona dtoPersona, DtoPresupuesto dtoPresupuesto, DtoTramite dtoTramite, ArrayList<DtoItem> dtosItems)
    {
        int creado = -1;


        int idTramite = darAltaTramite(dtoTramite);

        if (idTramite != -1)
        {

            Tramite miTramite = miJpaTramite.findTramite(idTramite);

            Persona miPersona = miJpaPersona.findPersona(dtoPersona.getIdPersona());

            //Creo el presupuesto:
            Presupuesto miPresupuesto = new Presupuesto();

            miPresupuesto.setFecha(Calendar.getInstance().getTime());
            miPresupuesto.setFkIdTramite(miTramite);
            miPresupuesto.setSaldo(dtoPresupuesto.getSaldo());
            miPresupuesto.setTotal(dtoPresupuesto.getTotal());
            miPresupuesto.setFkIdPersona(miPersona);
            miPresupuesto.setObservaciones(dtoPresupuesto.getObservaciones());

            creado = miJpaPresupuesto.create(miPresupuesto);

            if (creado != -1)
            {
                miPresupuesto.setIdPresupuesto(creado);

                //Actualizo el tramite:
                miTramite.setFkIdPresupuesto(miPresupuesto);

                Boolean modificado = miJpaTramite.asociarPresupuesto(miTramite);

                if (modificado)
                {
                    //Creo los Items:
                    ArrayList<Item> misItems = new ArrayList<>();

                    for (int i = 0; i < dtosItems.size(); i++)
                    {
                        DtoItem dtoItem = dtosItems.get(i);
                        Item miItem = new Item();

                        miItem.setAtributos(dtoItem);
                        miItem.setFkIdPresupuesto(miPresupuesto);

                        int id = miJpaItem.create(miItem);

                        if (id != -1)
                        {
                            miItem.setIdItem(id);
                            misItems.add(miItem);
                        }
                    }
                    //  registramos en auditoria en nuevo presupuesto
                    this.registrarAuditoria(miPresupuesto, ConstantesGui.CREAR_PRESUPUESTO);
                }
            }
        }

        return creado;
    }

    /**
     * Da de alta un Inmueble.
     *
     * @param miDtoInmueble datos del Inmueble a dar de alta.
     * @return el id del Inmueble si se creo, -1 de lo contrario.
     */
    private int darAltaInmueble(DtoInmueble miDtoInmueble)
    {
        int idInmueble = -1;

        // Creo el inmueble:
        Inmueble miInmueble = new Inmueble();
        miInmueble.setAtributos(miDtoInmueble);

        idInmueble = miJpaInmueble.create(miInmueble);

        this.registrarAuditoria(miInmueble, ConstantesGui.CREAR_PRESUPUESTO);

        return idInmueble;
    }

    /**
     * Da de alta un Tramite con un Inmueble asociado.
     *
     * @param miDtoTramite datos del Tramite a dar de alta.
     * @param miInmueble datos del Inmueble asociado.
     * @return el id del Tramite dado de alta, -1 de lo contrario.
     */
    private int darAltaTramite(DtoTramite miDtoTramite, Inmueble miInmueble)
    {
        int idTramite = -1;
        try
        {

            Tramite miTramite = new Tramite();

            TipoDeTramite miTipoDeTramite = new TipoDeTramite();
            miTipoDeTramite.setAtributos(miDtoTramite.getTipoDeTramite());

            miTramite.setFkIdTipoTramite(miTipoDeTramite);
            miTramite.setFkIdInmueble(miInmueble);

            idTramite = miJpaTramite.create(miTramite);

        } catch (IllegalOrphanException ex)
        {
            ex.printStackTrace();
        }
        return idTramite;
    }

    /**
     * Da de alta un Tramite.
     *
     * @param miDtoTramite datos del Tramite a dar de alta.
     * @return el id del Tramite dado de alta, -1 de lo contrario.
     */
    private int darAltaTramite(DtoTramite miDtoTramite)
    {
        int idTramite = -1;
        try
        {

            Tramite miTramite = new Tramite();

            TipoDeTramite miTipoDeTramite = new TipoDeTramite();
            miTipoDeTramite.setAtributos(miDtoTramite.getTipoDeTramite());

            miTramite.setFkIdTipoTramite(miTipoDeTramite);

            idTramite = miJpaTramite.create(miTramite);

        } catch (IllegalOrphanException ex)
        {
            ex.printStackTrace();
        }
        return idTramite;
    }

    /**
     * Busca todos los presupuestos asociados a una determinada persona.
     *
     * @param dtoPersona Los datos de la persona.
     * @return dtosPresupuestosEncontrados Una lista de Dto con los presupuestos
     * encontrados. Retorna la lista vacia en caso de no haber presupuesto
     * registrados.
     */
    public ArrayList<DtoPresupuesto> buscarPresupuestosPersona(DtoPersona dtoPersona) throws NonexistentJpaException
    {
        ArrayList<DtoPresupuesto> dtosPresupuestosEncontrados = new ArrayList<>();

//        Persona miPersona = miJpaPersona.findPersona(dtoPersona.getIdPersona());

        ArrayList<Presupuesto> presupuestos = (ArrayList<Presupuesto>) miJpaPresupuesto.findPresupuestosPersona(dtoPersona.getIdPersona());

        if ((presupuestos != null) && (!presupuestos.isEmpty()))
        {

            for (int i = 0; i < presupuestos.size(); i++)
            {
                Presupuesto presupuesto = presupuestos.get(i);

                //La persona no tiene la red de objetos, la busco
                Persona persona = presupuesto.getFkIdPersona();
                persona = this.obtenerRedObjetosPersona(persona);

                //Set persona con su red d objetos
                presupuesto.setFkIdPersona(persona);

                if (presupuesto.getFkIdTramite() != null)
                {
                    Tramite tramitePresupuesto = new Tramite();
                    tramitePresupuesto.setIdTramite(presupuesto.getFkIdTramite().getIdTramite());

                    tramitePresupuesto = miJpaTramite.encontrarTramite(tramitePresupuesto.getIdTramite());

                    presupuesto.setFkIdTramite(tramitePresupuesto);
                }
                DtoPresupuesto miDto = presupuesto.getDto();
                dtosPresupuestosEncontrados.add(miDto);
            }
        }

        return dtosPresupuestosEncontrados;
    }

    /**
     * Metodo que permite obtener la red de objetos de una persona
     *
     * @param persona El objeto persona, del cual se quiere obtener la red de
     * objeros.
     * @return Persona La instancia de persona con la red de objetos asignada.
     * @throws NonexistentJpaException
     */
    public Persona obtenerRedObjetosPersona(Persona persona) throws NonexistentJpaException
    {

        boolean flag = false;

        ArrayList<Persona> listaPersona = (ArrayList<Persona>) miJpaPersona.findPersonas();
        for (int j = 0; j < listaPersona.size() && !flag; j++)
        {
            Persona personaList = listaPersona.get(j);
            Integer idPersona = persona.getIdPersona();
            Integer idPersonaList = personaList.getIdPersona();
            if (idPersona.equals(idPersonaList))
            {
                persona = personaList;
                flag = true;
            }
        }
        return persona;
    }

    /**
     * Busca todos los Items asociados a un Presupuesto en particular.
     *
     * @param dtoPresupuesto datos del Presupuesto en particular.
     * @return una lista de Dto de los Items asociados al Presupuesto indicado.
     */
    public ArrayList<DtoItem> buscarItemsPresupuesto(DtoPresupuesto dtoPresupuesto)
    {
        ArrayList<DtoItem> dtosItemsEncontrados = null;
        ArrayList<Item> items = null;

        items = (ArrayList<Item>) miJpaItem.findItemsPresupuesto(dtoPresupuesto.getIdPresupuesto());

        if (items != null)
        {
            dtosItemsEncontrados = new ArrayList<>();

            for (int i = 0; i < items.size(); i++)
            {
                Item item = items.get(i);

                DtoItem miDtoItem = item.getDto();
                dtosItemsEncontrados.add(miDtoItem);
            }
        }


        return dtosItemsEncontrados;
    }

    /**
     * Busca un Presupuesto por su numero.
     *
     * @param miDtoPresupuesto datos del Presupuesto a buscar.
     * @return el Dto del Presupuesto encontrado.
     */
    public DtoPresupuesto buscarPresupuestoPorNumero(DtoPresupuesto miDtoPresupuesto)
    {
        DtoPresupuesto dtoPresupuestoEncontrado = null;
        Presupuesto miPresupuesto;

        miPresupuesto = miJpaPresupuesto.findPresupuesto(miDtoPresupuesto.getIdPresupuesto());

        if (miPresupuesto != null)
        {
            Persona miPersona = miPresupuesto.getFkIdPersona();

            if (miPersona != null)
            {
                dtoPresupuestoEncontrado = miPresupuesto.getDto();
            }

            dtoPresupuestoEncontrado.setPersona(miPersona.getDto());

        }

        return dtoPresupuestoEncontrado;
    }

    /**
     * Modifica los datos de un Presupuesto en particular.
     *
     * @param miDtoPresupuesto datos del Presupuesto a modificar.
     * @param dtosItems datos de los Items actuales del Presupuesto.
     * @param dtosItemsNuevos datos de los nuevos Items del Presupuesto.
     * @return TRUE si se modifico el Presupuesto, FALSE de lo contrario.
     */
    public Boolean modificarPresupuesto(DtoPresupuesto miDtoPresupuesto, ArrayList<DtoItem> dtosItems, ArrayList<DtoItem> dtosItemsNuevos) throws ClassModifiedException
    {
        Boolean modificado = false;
        ArrayList<Item> itemsTodosJuntos = new ArrayList<>();

        Presupuesto miPresupuesto = miJpaPresupuesto.findPresupuesto(miDtoPresupuesto.getIdPresupuesto());

        if (miPresupuesto != null)
        {
            if (miPresupuesto.getVersion() == miDtoPresupuesto.getVersion())
            {

                try
                {

                    miPresupuesto.setFecha(miDtoPresupuesto.getFecha());
                    miPresupuesto.setObservaciones(miDtoPresupuesto.getObservaciones());
                    miPresupuesto.setSaldo(miDtoPresupuesto.getSaldo());
                    miPresupuesto.setTotal(miDtoPresupuesto.getTotal());
                    miPresupuesto.setVersion(miDtoPresupuesto.getVersion());

                    Tramite miTramite = new Tramite();
                    miTramite.setAtributos(miDtoPresupuesto.getTramite());
                    miTramite.setFkIdPresupuesto(miPresupuesto);

                    ArrayList<Tramite> tramites = new ArrayList<>();
                    tramites.add(miTramite);

                    miPresupuesto.setTramiteList(tramites);

                    ArrayList<Item> itemsViejos = new ArrayList<Item>();
                    for (int i = 0; i < dtosItems.size(); i++)
                    {
                        DtoItem dtoItem = dtosItems.get(i);

                        Item itemViejo = new Item();
                        itemViejo.setAtributos(dtoItem);

                        itemsViejos.add(itemViejo);
                    }

                    miPresupuesto.setItemList(itemsViejos);

                    modificado = miJpaPresupuesto.edit(miPresupuesto);

                    if (modificado)
                    {
                        this.registrarAuditoria(miPresupuesto, ConstantesGui.MODIFICAR_PRESUPUESTO);

                        Boolean eliminadoItem = false;

                        for (int i = 0; i < itemsViejos.size(); i++)
                        {

                            Item miItem = miJpaItem.findItem(itemsViejos.get(i).getIdItem());

                            if (miItem != null)
                            {
                                eliminadoItem = miJpaItem.eliminarItem(miItem);


                                if (!eliminadoItem)
                                {
                                    break;
                                }
                                this.registrarAuditoria(miItem, ConstantesGui.MODIFICAR_PRESUPUESTO);
                            }
                        }

                        for (int i = 0; i < dtosItemsNuevos.size(); i++)
                        {
                            DtoItem dtoItem = dtosItemsNuevos.get(i);
                            Item item = new Item();
                            item.setAtributos(dtoItem);
                            item.setFkIdPresupuesto(miPresupuesto);

                            int id = miJpaItem.create(item);
                            item.setIdItem(id);

                            itemsTodosJuntos.add(item);

                            this.registrarAuditoria(item, ConstantesGui.MODIFICAR_PRESUPUESTO);
                        }
                    }
                } catch (IllegalOrphanException ex)
                {
                    modificado = false;
                } catch (NonexistentEntityException ex)
                {
                    modificado = false;
                }
            } else
            {
                throw new ClassModifiedException();
            }
        }

        return modificado;
    }

    public DtoPresupuesto buscarPresupuesto(DtoPresupuesto miDtoPresupuesto) throws NonexistentJpaException
    {
        DtoPresupuesto miPresupuesto = null;
        Presupuesto presupuesto = miJpaPresupuesto.findPresupuestosById(miDtoPresupuesto.getIdPresupuesto());

        if (presupuesto != null)
        {
            //Busco red de objetos de la persona
            Persona miPersona = null;
            Persona miPersonaPresupuesto = presupuesto.getFkIdPersona();
            miPersona = ControllerNegocio.getInstancia().obtenerRedObjetosPersona(miPersonaPresupuesto);
            presupuesto.setFkIdPersona(miPersona);
            miPresupuesto = presupuesto.getDto();
        }

        return miPresupuesto;
    }

// </editor-fold>
// <editor-fold defaultstate="collapsed" desc="Gestiones">
    // <editor-fold defaultstate="collapsed" desc="Gestion">
    /**
     * Metodo que permite obtene el proximo numero de gestion de escritura para
     * ser utilizado en un nueva gestion.
     *
     * @return dtoProximaGestion Un dto tipo gestion de escritura con el valor
     * del ultimo numero de gestion registrado o cero en caso de no existir
     * escrituras registradas.
     */
    public DtoGestionDeEscritura obtenerProximaGestionDeEscritura()
    {
        DtoGestionDeEscritura dtoProximaGestion = new DtoGestionDeEscritura();

        int cantidadGestiones = miJpaGestionDeEscritura.obtenerUltimoNumeroGestion();
        //int cantidadGestiones = miJpaGestionDeEscritura.getGestionDeEscrituraCount();

        dtoProximaGestion.setNumero(cantidadGestiones);

        return dtoProximaGestion;
    }

    /**
     * Busca una determinada gestion de escritura por su numero.
     *
     * @param gestionBuscar Un DTO con la gestion de escritura a buscar.
     * @return gestionEncontrada Un objeto de negocio de tipo gestion de
     * escritura.
     */
    public GestionDeEscritura buscarGestion(DtoGestionDeEscritura gestionBuscar)
    {
        GestionDeEscritura gestionEncontrada = new GestionDeEscritura();

        gestionEncontrada = miJpaGestionDeEscritura.findGestionDeEscrituraPorNumero(gestionBuscar.getNumero());

        return gestionEncontrada;
    }

    /**
     * Inicia una gestion de escrituras. Extrae desde el
     * {@link DtoGestionDeEscritura} los datos de una nueva gestion, junto con
     * el cliente de referencia y la lista de clientes involucrados. Ademas se
     * crea un registro de Historial y en el caso de haber mas de un cliente
     * involucrado, se crean tantos registros por tramite, como clientes hayan,
     * en la tabla "tramites_personas".
     *
     * @param dtoNuevaGestion Un DTO tipo gestion de escritura con todos los
     * datos necesario para iniciar una gestion.
     * @return nuevaGestion Un DTO tipo gestion de escritura con id de gestion y
     * el numero de gestion asigando.
     */
    public DtoGestionDeEscritura iniciarGestionDeEscritura(DtoGestionDeEscritura dtoNuevaGestion)
    {
        try
        {
            GestionDeEscritura nuevaGestion = new GestionDeEscritura();
            dtoNuevaGestion.setIdGestion(ConstantesNegocio.ID_OBJETO_NO_VALIDO);

            GestionDeEscritura gestionExistente = buscarGestion(dtoNuevaGestion);

            //  verificamos que la gestion que vamos a iniciar, ya no se encuentre registrada.
            if (gestionExistente.getNumero() == ConstantesPersistencia.VERSION_INICIAL)
            {
                //  gesiton -> estado iniciaL
                //  gestion -> escribano             
                //  gestion -> datos de la gestion
                nuevaGestion.setAtributos(dtoNuevaGestion);

                //  gestion -> cliente referencia        
                //  gestion -> lista clientes involucrados        

                //  gestion <- tramites_personas (relacional)
                List<TramitesPersonas> relaciones = new ArrayList<>();

                // Para el cliente de referencia.                
                Persona clienteReferencia = miJpaPersona.findPersonaTipoNumeroIdentificacion(dtoNuevaGestion.getClienteReferencia());

                //  gestion -> lista tramites asociados
                //  gestion <- tramite(s) (nueva)

                List<Tramite> listaTramites = new ArrayList<Tramite>();

                for (Iterator<DtoTramite> it = dtoNuevaGestion.getListaTramitesAsociados().iterator(); it.hasNext();)
                {
                    DtoTramite dtoTramite = it.next();
                    dtoTramite.setGestionDeEscritura(dtoNuevaGestion);

                    Tramite tramite = miJpaTramite.findTramite(dtoTramite.getIdTramite());

                    tramite.setFkIdGestion(nuevaGestion);

                    listaTramites.add(tramite);

                    nuevaGestion.getTramiteList().add(tramite);

                    TramitesPersonas relacionClienteReferencia = new TramitesPersonas();
                    relacionClienteReferencia.setPersona(clienteReferencia);
                    relacionClienteReferencia.setTramite(tramite);
                    relaciones.add(relacionClienteReferencia);
                }

                // Para la lista de clientes involucrados.                
                for (Iterator<DtoPersona> itClientes = dtoNuevaGestion.getListaClientesInvolucrados().iterator(); itClientes.hasNext();)
                {
                    DtoPersona dtoPersona = itClientes.next();
                    Persona cliente = miJpaPersona.findPersonaTipoNumeroIdentificacion(dtoPersona);

                    for (Iterator<Tramite> itTramites = listaTramites.iterator(); itTramites.hasNext();)
                    {
                        Tramite tramite = itTramites.next();

                        TramitesPersonas relacionClienteTramite = new TramitesPersonas();
                        relacionClienteTramite.setPersona(cliente);
                        relacionClienteTramite.setTramite(tramite);

                        relaciones.add(relacionClienteTramite);
                    }
                }

                for (Iterator<TramitesPersonas> itRelaciones = relaciones.iterator(); itRelaciones.hasNext();)
                {
                    TramitesPersonas tramitesPersonas = itRelaciones.next();

                    tramitesPersonas.setObservaciones("Tramites de la gestion: " + nuevaGestion.getNumero());

                    miJpaTramitesPersonas.create(tramitesPersonas);
                }

                //  Persistir entidades
                Integer idGestion = miJpaGestionDeEscritura.create(nuevaGestion);
                nuevaGestion.setIdGestion(idGestion);
                dtoNuevaGestion.setIdGestion(idGestion);

                DtoHistorial historial = this.registrarMovimientoHistorial(dtoNuevaGestion);

                this.registrarAuditoria(nuevaGestion, ConstantesGui.INICIAR_GESTION);
                try
                {
                    this.ingresarDocumentacion(dtoNuevaGestion);
                } catch (NonexistentEntityException ex)
                {
                    Logger.getLogger(ControllerNegocio.class.getName()).log(Level.SEVERE, null, ex);
                } catch (ClassModifiedException ex)
                {
                    Logger.getLogger(ControllerNegocio.class.getName()).log(Level.SEVERE, null, ex);
                }

            } else
            {
                //  La gestion ya se encuentra registrada.
                return dtoNuevaGestion;
            }
        } catch (CreateEntityException | PreexistingEntityException | DtoInvalidoException ex)
        {
            Logger.getLogger(ControllerNegocio.class.getName()).log(Level.SEVERE, null, ex);
        }
        return dtoNuevaGestion;
    }

    /**
     * Modifica alguno de los siguientes datos de una gestion: Encabezado,
     * observaciones y/o la lista de clientes asociados.
     *
     * @param dtoGestionModificar La gestion a modificar, junto con la lista de
     * clientes involucrados.
     * @return El dto gestion con el ID original si se pudo modificar, con el
     * ID_OBJETO_NO_VALIDO en caso contrario.
     */
    public DtoGestionDeEscritura modificarGestionDeEscritura(DtoGestionDeEscritura dtoGestionModificar, List<DtoPersona> listaDtoClientesAgregados, List<DtoPersona> listaDtoClientesEliminados) throws ClassModifiedException
    {
        try
        {
            GestionDeEscritura gestionParaModificar = new GestionDeEscritura();

            gestionParaModificar.setAtributos(dtoGestionModificar);

            if (!listaDtoClientesEliminados.isEmpty())
            {
                // eliminar clientes de la lista de tramites clientes, para una gestion dada.
                for (Iterator<DtoPersona> itClientes = listaDtoClientesEliminados.iterator(); itClientes.hasNext();)
                {
                    DtoPersona dtoPersona = itClientes.next();
                    Persona cliente = miJpaPersona.findPersonaTipoNumeroIdentificacion(dtoPersona);

                    for (Iterator<DtoTramite> itTramites = dtoGestionModificar.getListaTramitesAsociados().iterator(); itTramites.hasNext();)
                    {
                        DtoTramite dtoTramite = itTramites.next();

                        Tramite tramite = miJpaTramite.findTramite(dtoTramite.getIdTramite());

                        TramitesPersonas relacionClienteTramite = new TramitesPersonas();
                        relacionClienteTramite.setPersona(cliente);
                        relacionClienteTramite.setTramite(tramite);

                        List<TramitesPersonas> listaEliminar = miJpaTramitesPersonas.findTramitesClientes(cliente.getIdPersona(), tramite.getIdTramite());

                        for (Iterator<TramitesPersonas> it = listaEliminar.iterator(); it.hasNext();)
                        {
                            TramitesPersonas tramitesPersonas = it.next();

                            TramitesPersonasPK pk = new TramitesPersonasPK(tramite.getIdTramite(), cliente.getIdPersona());

                            tramitesPersonas.setTramitesPersonasPK(pk);

                            miJpaTramitesPersonas.eliminarRegistro(tramitesPersonas);
                        }
                    }
                }
            }

            if (!listaDtoClientesAgregados.isEmpty())
            {

                List<TramitesPersonas> relaciones = new ArrayList<>();

                //  agregar clientes a la lista de clientes involucrados.                
                for (Iterator<DtoPersona> itClientes = listaDtoClientesAgregados.iterator(); itClientes.hasNext();)
                {
                    DtoPersona dtoPersona = itClientes.next();
                    Persona cliente = miJpaPersona.findPersonaTipoNumeroIdentificacion(dtoPersona);

                    for (Iterator<DtoTramite> itTramites = dtoGestionModificar.getListaTramitesAsociados().iterator(); itTramites.hasNext();)
                    {
                        DtoTramite dtoTramite = itTramites.next();
                        Tramite tramite = miJpaTramite.findTramite(dtoTramite.getIdTramite());

                        TramitesPersonas relacionClienteTramite = new TramitesPersonas();
                        relacionClienteTramite.setPersona(cliente);
                        relacionClienteTramite.setTramite(tramite);

                        relaciones.add(relacionClienteTramite);
                    }
                }

                for (Iterator<TramitesPersonas> itRelaciones = relaciones.iterator(); itRelaciones.hasNext();)
                {
                    TramitesPersonas tramitesPersonas = itRelaciones.next();

                    tramitesPersonas.setObservaciones("Tramites de la gestion: " + dtoGestionModificar.getNumero());

                    miJpaTramitesPersonas.create(tramitesPersonas);
                }

            }

            List<DtoHistorial> historialGestion = this.obtenerHistorialGestion(dtoGestionModificar);

            for (Iterator<DtoHistorial> it = historialGestion.iterator(); it.hasNext();)
            {
                DtoHistorial dtoHistorial = it.next();
                Historial historial = new Historial();
                historial.setAtributos(dtoHistorial);
                gestionParaModificar.getHistorialList().add(historial);
            }

            EstadoDeGestion nuevoEstadoModificada = this.obtenerEstadoDeGestion(ConstantesNegocio.ESTADO_DE_GESTION_MODIFICADA);
            nuevoEstadoModificada.setObservaciones("Gestion: " + gestionParaModificar.getIdGestion() + ", Modificada");


            if (miJpaGestionDeEscritura.modificarGestionDeEscritura(gestionParaModificar) == true)
            {
                DtoEstadoDeGestion nuevoEstado = nuevoEstadoModificada.getDto();
                dtoGestionModificar.setEstado(nuevoEstado);

                this.registrarMovimientoHistorial(dtoGestionModificar);

                this.registrarAuditoria(gestionParaModificar, ConstantesGui.MODIFICAR_GESTION);
            } else
            {
                dtoGestionModificar.setIdGestion(ConstantesNegocio.ID_OBJETO_NO_VALIDO);
            }


        } catch (PreexistingEntityException | CreateEntityException | DtoInvalidoException ex)
        {
            Logger.getLogger(ControllerNegocio.class.getName()).log(Level.SEVERE, null, ex);
        }
        return dtoGestionModificar;
    }

    /**
     * Permite obtener un estado de gestion en base al nombre del mismo.
     *
     * @param nombreEstadoGestion El nombre del estado de gestion a buscar.
     * @return La instancia del estado de gestion encontrado.
     */
    public EstadoDeGestion obtenerEstadoDeGestion(String nombreEstadoGestion)
    {
        for (Iterator<DtoEstadoDeGestion> it = this.obtenerListaEstadosDeGestionDisponibles().iterator(); it.hasNext();)
        {
            DtoEstadoDeGestion dtoEstadoDeGestion = it.next();
            if (dtoEstadoDeGestion.getNombre().equals(nombreEstadoGestion))
            {
                EstadoDeGestion estadoGestionEncontrado = new EstadoDeGestion();
                try
                {
                    estadoGestionEncontrado.setAtributo(dtoEstadoDeGestion);
                    return estadoGestionEncontrado;
                } catch (DtoInvalidoException ex)
                {
                    Logger.getLogger(ControllerNegocio.class.getName()).log(Level.SEVERE, null, ex);
                }

            }
        }
        return new EstadoDeGestion();
    }

    /**
     * Metodo que permite obtener, en base a un dto estado de gestion, el estado
     * de gestion indicado.
     *
     * @param nombreEstadoGestion Un dto con el nombre del estado de gestion.
     * @return dtoEstadoDeGestion El estado de gestion indicado si se encontro.
     */
    public DtoEstadoDeGestion obtenerDtoEstadoDeGestion(DtoEstadoDeGestion nombreEstadoGestion)
    {
        for (Iterator<DtoEstadoDeGestion> it = this.obtenerListaEstadosDeGestionDisponibles().iterator(); it.hasNext();)
        {
            DtoEstadoDeGestion dtoEstadoDeGestion = it.next();
            if (dtoEstadoDeGestion.getNombre().equals(nombreEstadoGestion.getNombre()))
            {
                return dtoEstadoDeGestion;

            }
        }
        return new DtoEstadoDeGestion("Estado no valido");
    }

    /**
     * Metodo que permite modificar el "estado" de una gestion de escritura
     * (previamente setteado). Una vez que la gestion ha sido modificada (su
     * estado ha cambiado), se debe asignar al DTO de gestion el nuevo estado y
     * este metodo es responsable de persistir el cambio de estado en la base de
     * datos.
     *
     * @param dtoGestion
     * @return
     */
    public DtoGestionDeEscritura modificarEstadoDeGestionDeEscritura(DtoGestionDeEscritura dtoGestion) throws ClassModifiedException
    {
        try
        {
            GestionDeEscritura gestion = this.buscarGestion(dtoGestion);

            EstadoDeGestion estado = new EstadoDeGestion();
            estado.setAtributo(dtoGestion.getEstado());

            gestion.setFkIdEstadoDeGestion(estado);

            miJpaGestionDeEscritura.modificarEstadoDeGestionDeEscritura(gestion);

        } catch (DtoInvalidoException ex)
        {
            Logger.getLogger(ControllerNegocio.class.getName()).log(Level.SEVERE, null, ex);
        }
        return dtoGestion;
    }

    /**
     * Permite obtener el estado actual de una determinada gestion.
     *
     * @param dtoGestion Una gestion de la cual se desea conocer su estado
     * actual.
     * @return estadoActual El estado actual de la gestion indicada.
     *
     */
    public DtoEstadoDeGestion obtenerEstadoActualDeGestion(DtoGestionDeEscritura dtoGestion)
    {
        DtoEstadoDeGestion estadoActual = new DtoEstadoDeGestion();

        miJpaHistorial.findEstadoActualGestion(dtoGestion.getIdGestion());

        return estadoActual;
    }

//    public List<DtoGestionDeEscritura> obtenerListaGestionesCliente(DtoPersona dtoCliente) {
//        List<DtoGestionDeEscritura> listaGestionesDeEscrituras = new ArrayList<>();
//        try
//        {
//
//            Persona cliente = miJpaPersona.findPersona(dtoCliente.getIdPersona());
//
//            TramitesPersonasJpaController tramitesPersonasJpaController = (TramitesPersonasJpaController) AdministradorJpa.getInstancia().obtenerJpa(TramitesPersonasJpaController.class.getName());
//
////            tramitesPersonasJpaController
//        }
//        catch (NonexistentJpaException ex)
//        {
//            Logger.getLogger(ControllerNegocio.class.getName()).log(Level.SEVERE, null, ex);
//        }
//
//        return listaGestionesDeEscrituras;
//    }
    /**
     * Metodo que permite obtener una lista de todas las gestiones en tramite
     *
     * @return List<DtoGestionDeEscritura> listaDtoGestionDeEscrituras
     */
    public List<DtoGestionDeEscritura> obtenerGestionesEnTramite() throws NonexistentJpaException
    {
        List<DtoGestionDeEscritura> listaDtoGestionDeEscrituras = new ArrayList<>();
        List<GestionDeEscritura> listaGestionDeEscrituras = new ArrayList<>();

        listaGestionDeEscrituras = miJpaGestionDeEscritura.findGestionesDeEscritura();

        if (!listaGestionDeEscrituras.isEmpty())
        {
            for (int i = 0; i < listaGestionDeEscrituras.size(); i++)
            {
                GestionDeEscritura miGestion = listaGestionDeEscrituras.get(i);
                String estado = miGestion.getFkIdEstadoDeGestion().getNombre();
                if (!estado.equals(ConstantesNegocio.GESTION_ARCHIVADA))
                {
                    listaDtoGestionDeEscrituras.add(miGestion.getDto());
                }
            }
        }

        return listaDtoGestionDeEscrituras;
    }

    /**
     * Metodo que permite archivar una o varias gestiones, en estado de tramite
     *
     * @param listaDtoGestionesDeEscritura La lista de gestiones para ser
     * archivadas.
     * @return DtoFlag Un dto tipo flag (bandera) para indicar si se pudieron
     * archivar las gestiones o no.
     */
    public DtoFlag archivarGestion(List<DtoGestionDeEscritura> listaDtoGestionesDeEscritura) throws NonexistentJpaException, DtoInvalidoException, ClassModifiedException
    {
        DtoFlag flag = new DtoFlag();
        flag.setFlag(false);
        int idEstadoGestion = 0;
        List<GestionDeEscritura> lisGestionEscrituras = new ArrayList<>();
        List<EstadoDeGestion> listEstadosGestion = new ArrayList<>();

        listEstadosGestion = miJpaEstadoDeGestion.findEstadoDeGestionEntities();

        for (int i = 0; i < listEstadosGestion.size(); i++)
        {
            String nombreEstado = listEstadosGestion.get(i).getNombre();
            EstadoDeGestion estadoGetsion = listEstadosGestion.get(i);

            if (nombreEstado.equals(ConstantesNegocio.GESTION_ARCHIVADA))
            {
                idEstadoGestion = estadoGetsion.getIdEstadoGestion();
            }

        }

        for (int i = 0; i < listaDtoGestionesDeEscritura.size(); i++)
        {
            GestionDeEscritura gestionDeEscritura = new GestionDeEscritura();
            gestionDeEscritura.setAtributos(listaDtoGestionesDeEscritura.get(i));
            gestionDeEscritura.getFkIdEstadoDeGestion().setIdEstadoGestion(idEstadoGestion);
            // lisGestionEscrituras.add(gestionDeEscritura);
            flag.setFlag(miJpaGestionDeEscritura.archivarGestiones(gestionDeEscritura));

            this.registrarAuditoria(gestionDeEscritura, ConstantesGui.ARCHIVAR_GESTION);

            this.registrarMovimientoHistorial(gestionDeEscritura.getDto());

        }
        return flag;

    }

    /**
     * Permite busca una gestion de escritura dada, por su numero.
     *
     * @param gestionBuscar Un dto Gestion de escritura, con el numero de
     * gestion a buscar.
     * @return
     */
    public DtoGestionDeEscritura buscarDtoGestion(DtoGestionDeEscritura gestionBuscar)
    {
        GestionDeEscritura gestionEncontrada = new GestionDeEscritura();
        DtoGestionDeEscritura dtoGestion = null;

            gestionEncontrada = miJpaGestionDeEscritura.findGestionDeEscrituraPorId(gestionBuscar.getIdGestion());

            dtoGestion = this.obtenerDocNecesarioEntregadosNoEntregadosDeGestion(gestionEncontrada.getDto());

        return dtoGestion;
    }

    /**
     * En base a una gestion, permite obtener todos los estados (registro) por
     * los que paso.
     *
     *
     * @param dtoGestion Un dto gestion con el ID de la gestion.
     * @return listaDtoHistorial Una lista tipo DTO con todos los registros de
     * historial de la gestion.
     */
    public List<DtoHistorial> obtenerHistorialGestion(DtoGestionDeEscritura dtoGestion)
    {
        List<DtoHistorial> listaDtoHistorial = new ArrayList<>();

        List<Historial> registroHistorial = miJpaHistorial.findRegistroHistial(dtoGestion.getIdGestion());

        if (!registroHistorial.isEmpty())
        {
            for (Iterator<Historial> it = registroHistorial.iterator(); it.hasNext();)
            {
                Historial historial = it.next();

                DtoHistorial dtoHistorial = historial.getDto();

                listaDtoHistorial.add(dtoHistorial);
            }
        }

        return listaDtoHistorial;
    }

    /**
     * Registra un nuevo movimiento de una gestion en el historial de la misma.
     * Este metodo requiere que la gestion ya haya sido persistida y que posee
     * un estado asignado "valido".
     *
     * @param dtoGestion Un dto tipo Gestion con los datos para registrar un
     * nuevo movimiento en el hitorial de la gestion.
     * @return dtoNuevoHistorial Un dto que representa el nuevo registro de
     * historial, si el ID es ID_OBJETO_NO_VALIDO, significa que ocurrio un
     * error al persistir el nuevo registro.
     */
    public DtoHistorial registrarMovimientoHistorial(DtoGestionDeEscritura dtoGestion)
    {
        DtoHistorial dtoNuevoHistorial = new DtoHistorial();
        try
        {

            Historial nuevoRegistroHistorial = new Historial();

            Date fecha = Calendar.getInstance().getTime();
            nuevoRegistroHistorial.setFecha(fecha);

            GestionDeEscritura gestion = new GestionDeEscritura();

            DtoPersona dtoEscribano = dtoGestion.getPersonaEscribano();
            Persona escribano = miJpaPersona.findPersonaPorId(dtoEscribano.getIdPersona());

            dtoGestion.setPersonaEscribano(escribano.getDto());

            gestion.setAtributos(dtoGestion);
            nuevoRegistroHistorial.setFkIdGestion(gestion);

            EstadoDeGestion estado = new EstadoDeGestion();
            estado.setAtributo(dtoGestion.getEstado());

            nuevoRegistroHistorial.setFkIdEstadoGestion(estado);
            nuevoRegistroHistorial.setObservaciones("Gestion: " + dtoGestion.getNumero() + ", Estado: " + estado.getNombre());

            Integer idHistorial = miJpaHistorial.create(nuevoRegistroHistorial);

        } catch (CreateEntityException | DtoInvalidoException ex)
        {
            Logger.getLogger(ControllerNegocio.class.getName()).log(Level.SEVERE, null, ex);
        }
        return dtoNuevoHistorial;
    }

    /**
     * Metodo que permite obtener el cliente de referencia de una determinada
     * gestion, en base al id de la gestion.
     *
     * @param dtoGestion Un DTO tipo gestion con el ID para buscar la gestion.
     * @return clienteReferencia Un DTO tipo persona que representa el cliente
     * de referencia de la gestion indicada.
     */
    public DtoPersona obtenerClienteReferenciaGestion(DtoGestionDeEscritura dtoGestion) throws NonexistentJpaException
    {
        DtoPersona clienteReferencia = null;

        DtoTramite unTramite = dtoGestion.getListaTramitesAsociados().get(0);
        Tramite tramiteGestion = new Tramite();

        tramiteGestion.setAtributos(this.buscarTramite(unTramite));

        DtoPresupuesto presupuesto = new DtoPresupuesto();
        presupuesto.setIdPresupuesto(tramiteGestion.getFkIdPresupuesto().getIdPresupuesto());

        DtoPresupuesto unPresupuesto = this.buscarPresupuesto(presupuesto);

        clienteReferencia = this.buscarPersonaTipoNumeroIdentificacion(unPresupuesto.getPersona());

        return clienteReferencia;
    }

    // </editor-fold>
    // <editor-fold defaultstate="collapsed" desc="Documentacion">
    /**
     * Metodo que permite obtener los docuento necesarios para un tramite
     * determiando0
     *
     * @param dtoTramite
     * @return listaDtoDocumentosNecesarios para el tramite
     */
    public ArrayList<DtoTipoDeDocumento> obtenerDocumentosNecesarioTipoTramite(DtoTramite dtoTramite)
    {

        ArrayList<DtoTipoDeDocumento> listaDtoDocumentosNecesarios = new ArrayList<>();
        ArrayList<TipoDeDocumento> listaDocumentosNecesarios = new ArrayList<>();
        ArrayList<PlantillaTramite> listaPlantillaTramites = new ArrayList<>();
        TipoDeTramite tipoTramite = null;


        listaPlantillaTramites = (ArrayList<PlantillaTramite>) miJpaPlantillaTramite.findPlantillasTramites();

        for (int i = 0; i < listaPlantillaTramites.size(); i++)
        {
            tipoTramite = listaPlantillaTramites.get(i).getTipoDeTramite();

            if (tipoTramite.getNombre().equals(dtoTramite.getTipoDeTramite().getNombre())) //&& tipoTramite.getHabilitado())
            {
                TipoDeDocumento tipoDeDocumento = listaPlantillaTramites.get(i).getTipoDeDocumento();

                listaDocumentosNecesarios.add(tipoDeDocumento);
            }
        }

        for (int i = 0; i < listaDocumentosNecesarios.size(); i++)
        {
            DtoTipoDeDocumento dtoTipoDeDocumento = new DtoTipoDeDocumento();
            dtoTipoDeDocumento = listaDocumentosNecesarios.get(i).getDto();
            listaDtoDocumentosNecesarios.add(dtoTipoDeDocumento);
        }
        return listaDtoDocumentosNecesarios;
    }

    /**
     * Metodo que permite obtener los documentos necesarios para un tramite
     *
     * @param listaDtoTramitesDeGestion
     * @return Una lista de tramites, cada uno, con su documentos necesarios
     */
    public ArrayList<DtoTramite> obtenerDocumentosNecesariosPorTramite(ArrayList<DtoTramite> listaDtoTramitesDeGestion)
    {
        ArrayList<DtoTipoDeDocumento> listaDocumentosNecesariosPorTramite = null;
        ArrayList<DtoTramite> listaTramitesConSusDocumentosNecesarios = new ArrayList<>();

        for (int i = 0; i < listaDtoTramitesDeGestion.size(); i++)
        {
            DtoTramite dtoTramiteDeLaGesion = listaDtoTramitesDeGestion.get(i);
            listaDocumentosNecesariosPorTramite = ControllerNegocio.getInstancia().obtenerDocumentosNecesarioTipoTramite(dtoTramiteDeLaGesion);

            //Guardo los documentos necesarios para un tramite determinado
            dtoTramiteDeLaGesion.setListaDocumentosNecesarios(listaDocumentosNecesariosPorTramite);

            //Cargo el tramite con sus documentos necesarios, cada tramite tiene la gestion asociada
            listaTramitesConSusDocumentosNecesarios.add(dtoTramiteDeLaGesion);
        }
        return listaTramitesConSusDocumentosNecesarios;
    }

    /**
     * Metodo que permite obtener los documentos presentados para un tramite
     * determinado
     *
     * @param listaDtoTramitesDeGestion
     * @return
     */
    public ArrayList<DtoTramite> obtenerDocumentosPresentadosPorTramite(ArrayList<DtoTramite> listaDtoTramitesDeGestion) 
    {
        ArrayList<DtoTramite> listaDtoTramitesConSusDocumentosEntregados = new ArrayList<>();
        List<DocumentoPresentado> listaDocumentoPresentados = null;
        boolean flag = false;

        //Obtengo todos  los documentos presentados, de los tramites

        listaDocumentoPresentados = miJpaDocumentoPresentado.findDocumentosPresentados();

        for (int i = 0; i < listaDtoTramitesDeGestion.size(); i++)
        {
            DtoTramite dtoTramite = listaDtoTramitesDeGestion.get(i);
            int id_tramiteConsultar = listaDtoTramitesDeGestion.get(i).getIdTramite();

            //Recorro toda la lista de documentos presentados, buscando mi tramite
            for (int j = 0; j < listaDocumentoPresentados.size(); j++)
            {
                DtoDocumentoPresentado dtoDocumentoPresentado = listaDocumentoPresentados.get(j).getDto();
                int id_tramite_documentosPresentados = dtoDocumentoPresentado.getFkTramite().getIdTramite();

                if (id_tramite_documentosPresentados == id_tramiteConsultar)//Si son =, se entrego un documento
                {
                    //Cargo lista de tramites con sus documentos presentados si, los tienen
                    flag = true;
                    dtoTramite.getListaDocumentosGestion().add(dtoDocumentoPresentado);
                }

            }
            listaDtoTramitesConSusDocumentosEntregados.add(dtoTramite);

        }
        return listaDtoTramitesConSusDocumentosEntregados;
    }

    /**
     * Metodo que permite obtener los documentos no presentados para un un
     * tramite
     *
     * @param listaDtoTramitesConDocumentosNecesarios
     * @param listaDtoTramitesDeGestion
     * @return
     */
    public ArrayList<DtoTramite> obtenerDocumentosNoPresentadosPorTramite(ArrayList<DtoTramite> listaDtoTramitesDeGestion)
    {

        boolean flag = false;


        //Comparo los documentos necesarios con los entregados , para una tramite de una gestion determinada
        for (int i = 0; i < listaDtoTramitesDeGestion.size(); i++)
        {
            DtoTramite dtoTramite = listaDtoTramitesDeGestion.get(i);
            //Recorro toda la lista de documentos necesario, buscando el no entregado
            for (int j = 0; j < dtoTramite.getListaDocumentosNecesarios().size(); j++)
            {
                DtoTipoDeDocumento dtoDocumentoNecesario = dtoTramite.getListaDocumentosNecesarios().get(j);
                String nombreDocNececesario = dtoDocumentoNecesario.getNombre();

                flag = false;

                for (int k = 0; k < dtoTramite.getListaDocumentosGestion().size() && !flag; k++)
                {

                    String nombreDocPresentado = dtoTramite.getListaDocumentosGestion().get(k).getNombre();

                    if (nombreDocNececesario.equals(nombreDocPresentado))
                    {
                        flag = true;
                    } else
                    {
                        flag = false;
                    }

                }
                if (!flag)
                {
                    DtoDocumentoPresentado documentoPresentado = new DtoDocumentoPresentado();

                    if (dtoDocumentoNecesario.getDiasVencimiento() != null)
                    {
                        documentoPresentado.setDiasVencimiento(dtoDocumentoNecesario.getDiasVencimiento());
                    }
                    documentoPresentado.setNombre(dtoDocumentoNecesario.getNombre());
                    documentoPresentado.setQuienEntrega(dtoDocumentoNecesario.getQuienEntrega());

                    dtoTramite.getListaDocumentosNoPrecentados().add(documentoPresentado);
                }
            }

        }

        return listaDtoTramitesDeGestion;
    }

    /**
     * Metodo que permite saber si la documentacion de un cliente fue entregada
     * en su totalidad
     *
     * @param listaDocumentoPresentados
     * @return True si la documentacion fue entregada, false en caso contrario
     */
    public boolean documentacionCompletaCliente(DtoGestionDeEscritura dtoGestionDeEscritura)
    {

        boolean flag = true;

        ArrayList<DtoDocumentoPresentado> listadDocumentosPresentados = null;

        for (int i = 0; i < dtoGestionDeEscritura.getListaTramitesAsociados().size() && flag; i++)
        {
            DtoTramite dtoTramite = dtoGestionDeEscritura.getListaTramitesAsociados().get(i);

            for (int j = 0; j < dtoTramite.getListaDocumentosGestion().size() && flag; j++)
            {
                DtoDocumentoPresentado dtoDocumentoPresentado = dtoTramite.getListaDocumentosGestion().get(j);
                boolean entregado = dtoDocumentoPresentado.isEntregado();
                String cliente = dtoDocumentoPresentado.getQuienEntrega();

                if (cliente.equals(ConstantesNegocio.DOCUMENTACION_CLIENTE))
                {
                    if (!entregado)
                    {
                        flag = false;
                    }
                }
            }
        }
        return flag;
    }
    
       public boolean documentacionCompletaExterna(DtoGestionDeEscritura dtoGestionDeEscritura)
    {

        boolean flag = true;

        ArrayList<DtoDocumentoPresentado> listadDocumentosPresentados = null;

        for (int i = 0; i < dtoGestionDeEscritura.getListaTramitesAsociados().size() && flag; i++)
        {
            DtoTramite dtoTramite = dtoGestionDeEscritura.getListaTramitesAsociados().get(i);

            for (int j = 0; j < dtoTramite.getListaDocumentosGestion().size() && flag; j++)
            {
                DtoDocumentoPresentado dtoDocumentoPresentado = dtoTramite.getListaDocumentosGestion().get(j);
                boolean entregado = dtoDocumentoPresentado.isEntregado();
                String cliente = dtoDocumentoPresentado.getQuienEntrega();

                if (cliente.equals(ConstantesNegocio.DOCUMENTACION_ENTIDAD_EXTERNA))
                {
                    if (!entregado)
                    {
                        flag = false;
                    }
                }
            }
        }
        return flag;
    }

    /**
     * Metodo que permite saber si toda la documentacion de una gestion fueron
     * entregados
     *
     * @param dtoGestionDeEscritura
     * @return Verdadero si lo documentos fueron entregados, de lo contrario
     * retorna falso
     */
    public boolean iscompletaDocumentacion(DtoGestionDeEscritura dtoGestionDeEscritura) 
    {
        boolean flag = true;

        ArrayList<DtoDocumentoPresentado> listadDocumentosPresentados = null;

        for (int i = 0; i < dtoGestionDeEscritura.getListaTramitesAsociados().size() && flag; i++)
        {
            DtoTramite dtoTramite = dtoGestionDeEscritura.getListaTramitesAsociados().get(i);

            for (int j = 0; j < dtoTramite.getListaDocumentosGestion().size() && flag; j++)
            {
                DtoDocumentoPresentado dtoDocumentoPresentado = dtoTramite.getListaDocumentosGestion().get(j);
                boolean entregado = dtoDocumentoPresentado.isEntregado();

                if (entregado)
                {
                    flag = true;
                } else
                {
                    flag = false;
                }
            }
        }

        if (flag)
        {
            try
            {
                String nombreEstado = null;
                //Busco  los estados de gestion

                List<EstadoDeGestion> listEstadosGestion = new ArrayList<>();
                int idEstadoGestion = 0;

                listEstadosGestion = miJpaEstadoDeGestion.findEstadoDeGestionEntities();

                EstadoDeGestion estadoGestion = null;

                for (int i = 0; i < listEstadosGestion.size(); i++)
                {
                    nombreEstado = listEstadosGestion.get(i).getNombre();

                    estadoGestion = listEstadosGestion.get(i);

                    if (nombreEstado.equals(ConstantesNegocio.DOCUMENTACION_COMPLETA))
                    {
                        idEstadoGestion = estadoGestion.getIdEstadoGestion();

                        break;
                    }

                }

                DtoEstadoDeGestion dtoGestion = new DtoEstadoDeGestion();
                dtoGestion.setIdEstadoGestion(idEstadoGestion);
                dtoGestion.setNombre(nombreEstado);

                dtoGestionDeEscritura.setEstado(dtoGestion);

                this.modificarEstadoDeGestionDeEscritura(dtoGestionDeEscritura);

                this.registrarMovimientoHistorial(dtoGestionDeEscritura);

                this.registrarAuditoria(dtoGestion, ConstantesGui.DOCUMENTACION_INGRESO);
            } catch (ClassModifiedException ex)
            {
                Logger.getLogger(ControllerNegocio.class.getName()).log(Level.SEVERE, null, ex);
            }

        }

        return flag;
    }

    /**
     * Metodo que permite obtener los documentos no precentados, presentados y
     * necesarios de un tramite determinado, correspondiente a una gestion, Los
     * documentos se pueden repetir, porque dos o mas tramites pueden usar los
     * mismos documentos
     *
     * @param dtoGestion
     * @return Una lista de los odcumentos que no fueron entregados para una
     * gestion
     */
    public DtoGestionDeEscritura obtenerDocNecesarioEntregadosNoEntregadosDeGestion(DtoGestionDeEscritura dtoGestion) 
    {

        ArrayList<DtoTramite> listaDtoTramitesDeGestion = new ArrayList<>();
        ArrayList<DtoTramite> listaDtoTramitesConEstadoDeDocumentacion = null;
        boolean flag = false;

        DtoTramite dtoTramite = null;

        //Cargo la lista de Tramites de la gestion y a cada tramite le asocio la gestion que le pertenece
        for (int i = 0; i < dtoGestion.getListaTramitesAsociados().size(); i++)
        {
            dtoTramite = dtoGestion.getListaTramitesAsociados().get(i);
            dtoTramite.getListaDocumentosNoPrecentados().clear();
            dtoTramite.getListaDocumentosGestion().clear();
            dtoTramite.getListaDocumentosNecesarios().clear();
            dtoTramite.setGestionDeEscritura(dtoGestion);
            listaDtoTramitesDeGestion.add(dtoTramite);
        }

        //Busco documentos necesarios para los tramites de la gestion, que obtuve  
        //listaTramitesConSusDocumentosNecesarios Contiene el tramite, sus documentos y su gestion
        listaDtoTramitesConEstadoDeDocumentacion = this.obtenerDocumentosNecesariosPorTramite(listaDtoTramitesDeGestion);

        //Busco los documentos entregados de cada tramite,  perteneciente a una gestion
        listaDtoTramitesConEstadoDeDocumentacion = this.obtenerDocumentosPresentadosPorTramite(listaDtoTramitesDeGestion);

        //Busco los documentos no entregados de cada tramite, perteneciente a una gesion
        listaDtoTramitesConEstadoDeDocumentacion = this.obtenerDocumentosNoPresentadosPorTramite(listaDtoTramitesDeGestion);


        return dtoGestion;

    }

    /**
     * Metodo que permite registrar los documentos de un determinado tramite,
     * cuando estos son entregados en su totalidad, la gestion para al estado
     * Documentacion_Completa
     *
     * @param listaDtoDocumentoPresentados
     * @return Un Dtoflag, true si se registraron, no de lo contrario
     */
    public DtoFlag ingresarDocumentos(ArrayList<DtoDocumentoPresentado> listaDtoDocumentoPresentados, DtoGestionDeEscritura dtoGestionDeEscritura) throws NonexistentEntityException, ClassModifiedException
    {

        ArrayList<DocumentoPresentado> listaDocumentoPresentados = new ArrayList<>();
        DtoFlag flag = new DtoFlag();
        flag.setFlag(false);

        //Paso de dto a objetos
        for (int i = 0; i < listaDtoDocumentoPresentados.size(); i++)
        {
            DtoDocumentoPresentado dtoDocumentoPresentado = listaDtoDocumentoPresentados.get(i);
            DocumentoPresentado documentoPresentado = new DocumentoPresentado();

            documentoPresentado.setAtributos(dtoDocumentoPresentado);

            listaDocumentoPresentados.add(documentoPresentado);
        }
        
            //Persisto los objetos
            for (int i = 0; i < listaDocumentoPresentados.size(); i++)
            {
                DocumentoPresentado documentoPresentado = listaDocumentoPresentados.get(i);       
     

                flag.setFlag(miJpaDocumentoPresentado.create(documentoPresentado));

                this.registrarAuditoria(documentoPresentado, ConstantesGui.DOCUMENTACION_INGRESO);
            }
        return flag;
    }

    /**
     * Metodo que permite registrar el pago de deudas de los documentos
     * asociados a una gestion.
     *
     * @param listaDtoDocumentoPresentados La lista de los documentos con
     * deudas.
     * @param dtoGestionDeEscritura La gestion a la cual estan asociados los
     * documentos.
     * @return dtoResultado Un dto tio flag (bandera) que indica si se pudieron
     * registrar los cambios o no.
     * @throws NonexistentEntityException
     * @throws ClassModifiedException
     */
    public DtoFlag modificarDocumentacion(ArrayList<DtoDocumentoPresentado> listaDtoDocumentoPresentados, DtoGestionDeEscritura dtoGestionDeEscritura) throws ClassModifiedException, NonexistentEntityException 
    {

        ArrayList<DocumentoPresentado> listaDocumentoPresentados = new ArrayList<>();
        DtoFlag dtoResultado = new DtoFlag();
        dtoResultado.setFlag(false);

        //Paso de dto a objetos
        for (int i = 0; i < listaDtoDocumentoPresentados.size(); i++)
        {
            DtoDocumentoPresentado dtoDocumentoPresentado = listaDtoDocumentoPresentados.get(i);
            DocumentoPresentado documentoPresentado = new DocumentoPresentado();

            documentoPresentado.setAtributos(dtoDocumentoPresentado);

            listaDocumentoPresentados.add(documentoPresentado);
        }

        //Persisto los objetos
        for (int i = 0; i < listaDocumentoPresentados.size(); i++)
        {
            DocumentoPresentado documentoPresentado = listaDocumentoPresentados.get(i);
            dtoResultado.setFlag(miJpaDocumentoPresentado.edit(documentoPresentado));

            this.registrarAuditoria(documentoPresentado, ConstantesGui.DOCUMENTACION_DEUDA);
        }

        return dtoResultado;
    }

    /**
     * Metodo que permite registrar el ingreso de documentos de entidades
     * externas, junto con la informacion que se adjunta a cada uno: numero de
     * carton, fecha de ingreso, pago, salida, etc.
     *
     * @param listaDtoDocumentoPresentados La lista de documentos presentados
     * por la entidad externa.
     * @param dtoGestionDeEscritura La gestion a la cual estan asociados los
     * documentos.
     * @return dtoResultado Un dto tipo Flag (bandera) que indica si se pudieron
     * registrar los cambios o no.
     * @throws NonexistentEntityException
     * @throws ClassModifiedException
     */
    public DtoFlag modificarDocumentacionEntidadesExternas(ArrayList<DtoDocumentoPresentado> listaDtoDocumentoPresentados, DtoGestionDeEscritura dtoGestionDeEscritura) throws NonexistentEntityException, ClassModifiedException
    {

        ArrayList<DocumentoPresentado> listaDocumentoPresentados = new ArrayList<>();
        DtoFlag dtoResultado = new DtoFlag();
        dtoResultado.setFlag(false);

        //Paso de dto a objetos
        for (int i = 0; i < listaDtoDocumentoPresentados.size(); i++)
        {
            DtoDocumentoPresentado dtoDocumentoPresentado = listaDtoDocumentoPresentados.get(i);
            DocumentoPresentado documentoPresentado = new DocumentoPresentado();

            documentoPresentado.setAtributos(dtoDocumentoPresentado);

            listaDocumentoPresentados.add(documentoPresentado);
        }

        //Persisto los objetos
        for (int i = 0; i < listaDocumentoPresentados.size(); i++)
        {
                DocumentoPresentado documentoPresentado = listaDocumentoPresentados.get(i);

                dtoResultado.setFlag(miJpaDocumentoPresentado.edit(documentoPresentado));

                this.registrarAuditoria(documentoPresentado, ConstantesGui.DOCUMENTACION_ENTIDAD_EXTERNA);
        }

        return dtoResultado;
    }

    /**
     * Metodo que permite registrar el reingreso de documentos de entidades
     * externas, junto con la informacion que se adjunta a cada uno: numero de
     * carton, fecha de ingreso, pago, salida, etc.
     *
     * @param listaDtoDocumentoPresentados La lista de documentos presentados
     * por la entidad externa.
     * @param dtoGestionDeEscritura La gestion a la cual estan asociados los
     * documentos.
     * @return dtoResultado Un dto tipo Flag (bandera) que indica si se pudieron
     * registrar los cambios o no.
     * @throws NonexistentEntityException
     * @throws ClassModifiedException
     */
    public DtoFlag modificarDocumentacionReingreso(ArrayList<DtoDocumentoPresentado> listaDtoDocumentoPresentados, DtoGestionDeEscritura dtoGestionDeEscritura) throws NonexistentEntityException, ClassModifiedException
    {

        ArrayList<DocumentoPresentado> listaDocumentoPresentados = new ArrayList<>();
        DtoFlag dtoResultado = new DtoFlag();
        dtoResultado.setFlag(false);

        //Paso de dto a objetos
        for (int i = 0; i < listaDtoDocumentoPresentados.size(); i++)
        {
            DtoDocumentoPresentado dtoDocumentoPresentado = listaDtoDocumentoPresentados.get(i);
            DocumentoPresentado documentoPresentado = new DocumentoPresentado();

            documentoPresentado.setAtributos(dtoDocumentoPresentado);

            listaDocumentoPresentados.add(documentoPresentado);
        }

        //Persisto los objetos
        for (int i = 0; i < listaDocumentoPresentados.size(); i++)
        {
            DocumentoPresentado documentoPresentado = listaDocumentoPresentados.get(i);
            dtoResultado.setFlag(miJpaDocumentoPresentado.edit(documentoPresentado));

            this.registrarAuditoria(documentoPresentado, ConstantesGui.REGISTRAR_REINGRESO);
        }

        return dtoResultado;
    }

    /**
     * Retorna una lista con todos los documentos (de todas las gestiones) que
     * esten proximos a vencer.
     *
     * @return listaDocumentosPorVencer Una lista tipo DTO con todos los
     * documentos proximos a vencer.
     */
    public List<DtoDocumentoPresentado> consultarDocumentosProximosVencer()
    {
        List<DtoDocumentoPresentado> listaDocumentosPorVencer = new ArrayList<>();
        List<DocumentoPresentado> listaDocumentos = new ArrayList<>();

        listaDocumentos = miJpaDocumentoPresentado.findDocumentosPorVencer();

        if (!listaDocumentos.isEmpty())
        {
            for (Iterator<DocumentoPresentado> it = listaDocumentos.iterator(); it.hasNext();)
            {
                DocumentoPresentado documentoPresentado = it.next();

                DtoDocumentoPresentado dtoDocumentoPresentado = documentoPresentado.getDto();

                // la gestion
                GestionDeEscritura gestion = miJpaGestionDeEscritura.findGestionDeEscrituraPorNumero(documentoPresentado.getFkIdTramite().getFkIdGestion().getNumero());
              
                
                DtoGestionDeEscritura dtoGestion = gestion.getDto();


                // el tramite de la gestion
                Tramite tramite = miJpaTramite.findTramite(documentoPresentado.getFkIdTramite().getIdTramite());
                DtoTramite dtoTramite = tramite.getDto();

                // el presupuesto del tramite
                Presupuesto presupuesto = miJpaPresupuesto.findPresupuesto(tramite.getFkIdPresupuesto().getIdPresupuesto());
                DtoPresupuesto dtoPresupuesto = presupuesto.getDto();

                // el cliente de referencia del preupuesto
                Persona clienteReferencia = miJpaPersona.findPersonaPorId(presupuesto.getFkIdPersona().getIdPersona());
                DtoPersona dtoPersona = clienteReferencia.getDto();

                dtoPresupuesto.setPersona(dtoPersona);
                dtoGestion.setClienteReferencia(dtoPersona);
                dtoTramite.setGestionDeEscritura(dtoGestion);
                dtoTramite.setPresupuesto(dtoPresupuesto);
                dtoDocumentoPresentado.setFkTramite(dtoTramite);

                listaDocumentosPorVencer.add(dtoDocumentoPresentado);
            }
        }

        return listaDocumentosPorVencer;
    }

    /**
     * Metodo que permite ingresar tota la documentacion correspondiente a una determinada
     * gestion, cuando esta es iniciada. 
     * para luego se modificada
     * @param dtoGestionDeEscritura
     * @throws NonexistentEntityException
     * @throws ClassModifiedException 
     */
 public void ingresarDocumentacion(DtoGestionDeEscritura dtoGestionDeEscritura) throws NonexistentEntityException, ClassModifiedException
    {

        ArrayList<DtoDocumentoPresentado> listaDocumentosExternos = new ArrayList<>();

        dtoGestionDeEscritura = this.obtenerDocNecesarioEntregadosNoEntregadosDeGestion(dtoGestionDeEscritura);
     

        DtoDocumentoPresentado dtoDocumento = null;
        DtoDocumentoPresentado dtoDocumentoPresentado = null;

        for (int i = 0; i < dtoGestionDeEscritura.getListaTramitesAsociados().size(); i++)
        {
            DtoTramite dtoTramite = dtoGestionDeEscritura.getListaTramitesAsociados().get(i);

            for (int j = 0; j < dtoTramite.getListaDocumentosNecesarios().size(); j++)
            {
                dtoDocumentoPresentado = dtoTramite.getListaDocumentosNoPrecentados().get(j);

                dtoDocumento = new DtoDocumentoPresentado();

                dtoDocumento.setFkTramite(dtoTramite);

                dtoDocumento.setNombre(dtoDocumentoPresentado.getNombre());

                dtoDocumento.setQuienEntrega(dtoDocumentoPresentado.getQuienEntrega());

                dtoDocumento.setReingresado(false);

                dtoDocumento.setEntregado(false);
                
                listaDocumentosExternos.add(dtoDocumento);

            }


        }

        ControllerNegocio.getInstancia().ingresarDocumentos(listaDocumentosExternos, dtoGestionDeEscritura);

    }
    // </editor-fold>
    // <editor-fold defaultstate="collapsed" desc="Escrituras">

    /**
     * Crea una nueva Escritura con los datos indicados.
     *
     * @param miDtoEscritura, DtoEscritura con los datos de la nueva escritura.
     * @return TRUE si fue creada, FALSE de lo contrario.
     * @throws PreexistingEntityException, si existe la Escritura a crear,
     * devuelve la excepcion.
     */
    public Boolean crearEscritura(DtoEscritura miDtoEscritura) throws PreexistingEntityException, ClassModifiedException, ClassEliminatedException
    {
        Boolean creada = Boolean.FALSE;
        Escritura miEscritura = new Escritura();

        miEscritura.setAtributos(miDtoEscritura);

        if (!existeEscritura(miDtoEscritura))
        {
            for (Iterator<DtoFolio> it = miDtoEscritura.getFolios().iterator(); it.hasNext();)
            {
                DtoFolio dtoFolio = it.next();
                Folio miFolio = new Folio();
                miFolio.setAtributos(dtoFolio);

                miJpaFolio.modificarFolio(miFolio);

            }
            creada = miJpaEscritura.create(miEscritura);

            if (creada)
            {
                this.registrarAuditoria(miEscritura, ConstantesGui.PREPARAR_ESCRITURA);
            }

        } else
        {
            throw new PreexistingEntityException("La Escritura ya existe con el numero indicado.");
        }

        return creada;
    }

    /**
     * Verifica si una Escritura ya existe con el numero indicado para un
     * Escribano en particular.
     *
     * @param miDtoEscritura, datos de la Escritura a buscar (Numero de
     * Escritura)
     * @return TRUE si ya existe la Escritura, FALSE de lo contrario.
     */
    private Boolean existeEscritura(DtoEscritura miDtoEscritura)
    {
        Boolean existe = false;

        List<Escritura> escriturasConMismoNumero = miJpaEscritura.findEscrituraByNumero(miDtoEscritura.getNumero());

        if (escriturasConMismoNumero != null && !escriturasConMismoNumero.isEmpty())
        {
            for (Iterator<Escritura> it = escriturasConMismoNumero.iterator(); it.hasNext();)
            {
                Escritura escritura = it.next();

                if (escritura.getFolioList().get(0).getFkIdPersonaEscribano().getRegistroEscribano().intValue()
                        == miDtoEscritura.getFolios().get(0).getPersonaEscribano().getRegistroEscribano().intValue())
                {
                    existe = true;
                }
            }
        }

        return existe;
    }

    /**
     * Busca las Escrituras de un Escribano en particular.
     *
     * @param miEscribano, datos del Escribano a buscar (Numero de Registro)
     * @return Lista de DtoEscritura, de Escrituras encontradas.
     */
    public List<DtoEscritura> buscarEscriturasPorRegistro(DtoPersona miEscribano)
    {
        List<DtoEscritura> dtosEscriturasEncontradas = new ArrayList<>();
        List<Escritura> escrituras = new ArrayList<>();
        List<GestionDeEscritura> gestionesEscribano = new ArrayList<>();
        Persona miPersonaEscribano = new Persona();

        miPersonaEscribano.setRegistroEscribano(miEscribano.getRegistroEscribano());

        miPersonaEscribano = miJpaPersona.findPersonaEscribano(miPersonaEscribano);

        if (miPersonaEscribano.getIdPersona() != null)
        {

            gestionesEscribano = miPersonaEscribano.getGestionDeEscrituraList();
            for (Iterator<GestionDeEscritura> it = gestionesEscribano.iterator(); it.hasNext();)
            {
                GestionDeEscritura gestionDeEscritura = it.next();

                if (gestionDeEscritura.getTramiteList() != null)
                {

                    for (Iterator<Tramite> it1 = gestionDeEscritura.getTramiteList().iterator(); it1.hasNext();)
                    {
                        Tramite tramite = it1.next();

                        if (tramite.getFkIdEscritura() != null)
                        {
                            if (!escrituras.contains(tramite.getFkIdEscritura()))
                            {
                                escrituras.add(tramite.getFkIdEscritura());
                            }
                        }
                    }
                }
            }

            if (!escrituras.isEmpty())
            {
                for (Iterator<Escritura> it = escrituras.iterator(); it.hasNext();)
                {
                    Escritura escritura = it.next();

                    DtoEscritura miDtoEscritura = escritura.getDto();

                    dtosEscriturasEncontradas.add(miDtoEscritura);
                }
            }
        }

        return dtosEscriturasEncontradas;
    }

    /**
     * Busca las Escrituras de un Escribano, que estan en estado "Firmada"
     *
     * @param miEscribano, registro de escribano a buscar.
     * @return Lista de DtoEscritura, con las Escrituras encontradas.
     */
    public List<DtoEscritura> buscarEscriturasPorRegistroFirmadas(DtoPersona miEscribano)
    {
        List<DtoEscritura> dtosEscriturasEncontradas = new ArrayList<>();
        List<Escritura> escrituras = new ArrayList<>();
        List<GestionDeEscritura> gestionesEscribano = new ArrayList<>();
        Persona miPersonaEscribano = new Persona();

        miPersonaEscribano.setRegistroEscribano(miEscribano.getRegistroEscribano());

        miPersonaEscribano = miJpaPersona.findPersonaEscribano(miPersonaEscribano);

        if (miPersonaEscribano.getIdPersona() != null)
        {
            gestionesEscribano = miPersonaEscribano.getGestionDeEscrituraList();

            for (Iterator<GestionDeEscritura> it = gestionesEscribano.iterator(); it.hasNext();)
            {
                GestionDeEscritura gestionDeEscritura = it.next();

                if (gestionDeEscritura.getTramiteList() != null)
                {
                    for (Iterator<Tramite> it1 = gestionDeEscritura.getTramiteList().iterator(); it1.hasNext();)
                    {
                        Tramite tramite = it1.next();

                        if (tramite.getFkIdEscritura() != null)
                        {
                            if (!escrituras.contains(tramite.getFkIdEscritura()))
                            {
                                Escritura miEscritura = miJpaEscritura.findEscrituraById(tramite.getFkIdEscritura().getIdEscritura());

                                if (miEscritura.getEstado().equals(ConstantesNegocio.ESCRITURA_FIRMADA))
                                {
                                    escrituras.add(miEscritura);
                                }
                            }
                        }

                    }
                }

            }

            if (!escrituras.isEmpty())
            {
                for (Iterator<Escritura> it = escrituras.iterator(); it.hasNext();)
                {
                    Escritura escritura = it.next();

                    DtoEscritura miDtoEscritura = escritura.getDto();

                    dtosEscriturasEncontradas.add(miDtoEscritura);
                }
            }
        }

        return dtosEscriturasEncontradas;
    }

    public List<DtoEscritura> buscarEscriturasPorRegistroFirmadasSinArchivo(DtoPersona miEscribano)
    {
        List<DtoEscritura> dtosEscriturasEncontradas = new ArrayList<>();
        List<Escritura> escrituras = new ArrayList<>();
        List<GestionDeEscritura> gestionesEscribano = new ArrayList<>();
        Persona miPersonaEscribano = new Persona();

        miPersonaEscribano.setRegistroEscribano(miEscribano.getRegistroEscribano());

        miPersonaEscribano = miJpaPersona.findPersonaEscribano(miPersonaEscribano);

        if (miPersonaEscribano.getIdPersona() != null)
        {
            gestionesEscribano = miPersonaEscribano.getGestionDeEscrituraList();

            for (Iterator<GestionDeEscritura> it = gestionesEscribano.iterator(); it.hasNext();)
            {
                GestionDeEscritura gestionDeEscritura = it.next();

                if (!gestionDeEscritura.getFkIdEstadoDeGestion().getNombre().equals(ConstantesNegocio.GESTION_ARCHIVADA))
                {
                    if (gestionDeEscritura.getTramiteList() != null)
                    {
                        for (Iterator<Tramite> it1 = gestionDeEscritura.getTramiteList().iterator(); it1.hasNext();)
                        {
                            Tramite tramite = it1.next();

                            if (tramite.getFkIdEscritura() != null)
                            {
                                if (!escrituras.contains(tramite.getFkIdEscritura()))
                                {
                                    Escritura miEscritura = miJpaEscritura.findEscrituraById(tramite.getFkIdEscritura().getIdEscritura());

                                    if (miEscritura.getEstado().equals(ConstantesNegocio.ESCRITURA_FIRMADA))
                                    {
                                        escrituras.add(miEscritura);
                                    }
                                }
                            }

                        }
                    }
                }
            }

            if (!escrituras.isEmpty())
            {
                for (Iterator<Escritura> it = escrituras.iterator(); it.hasNext();)
                {
                    Escritura escritura = it.next();

                    DtoEscritura miDtoEscritura = escritura.getDto();

                    dtosEscriturasEncontradas.add(miDtoEscritura);
                }
            }
        }

        return dtosEscriturasEncontradas;
    }

    /**
     * Busca las Escrituras de un Escribano, que estan en estado "Firmada" o
     * "Inscripta".
     *
     * @param miEscribano, registro de escribano a buscar.
     * @return Lista de DtoEscritura, con las Escrituras encontradas.
     */
    public List<DtoEscritura> buscarEscriturasPorRegistroFirmadasInscriptas(DtoPersona miEscribano)
    {
        List<DtoEscritura> dtosEscriturasEncontradas = new ArrayList<>();
        List<Escritura> escrituras = new ArrayList<>();
        List<GestionDeEscritura> gestionesEscribano = new ArrayList<>();
        Persona miPersonaEscribano = new Persona();

        miPersonaEscribano.setRegistroEscribano(miEscribano.getRegistroEscribano());

        miPersonaEscribano = miJpaPersona.findPersonaEscribano(miPersonaEscribano);

        if (miPersonaEscribano.getIdPersona() != null)
        {

            gestionesEscribano = miPersonaEscribano.getGestionDeEscrituraList();
            for (Iterator<GestionDeEscritura> it = gestionesEscribano.iterator(); it.hasNext();)
            {
                GestionDeEscritura gestionDeEscritura = it.next();
                EstadoDeGestion miEstado = miJpaEstadoDeGestion.findEstadoDeGestion(gestionDeEscritura.getIdGestion());

                if (!miEstado.equals(ConstantesNegocio.GESTION_ARCHIVADA))
                {
                    if (gestionDeEscritura.getTramiteList() != null)
                    {
                        for (Iterator<Tramite> it1 = gestionDeEscritura.getTramiteList().iterator(); it1.hasNext();)
                        {
                            Tramite tramite = it1.next();

                            if (tramite.getFkIdEscritura() != null)
                            {
                                if (!escrituras.contains(tramite.getFkIdEscritura()))
                                {
                                    Escritura miEscritura = miJpaEscritura.findEscrituraById(tramite.getFkIdEscritura().getIdEscritura());

                                    if (miEscritura.getEstado().equals(ConstantesNegocio.ESCRITURA_FIRMADA)
                                            || miEscritura.getEstado().equals(ConstantesNegocio.ESCRITURA_INSCRIPTA))
                                    {
                                        escrituras.add(miEscritura);
                                    }
                                }
                            }
                        }
                    }
                }
            }

            if (!escrituras.isEmpty())
            {
                for (Iterator<Escritura> it = escrituras.iterator(); it.hasNext();)
                {
                    Escritura escritura = it.next();

                    DtoEscritura miDtoEscritura = escritura.getDto();

                    dtosEscriturasEncontradas.add(miDtoEscritura);
                }
            }
        }

        return dtosEscriturasEncontradas;
    }

    /**
     * Busca una Escritura en particular.
     *
     * @param miDtoEscritura, datos de la Escritura a buscar (Numero de
     * Escritura)
     * @return Lista de DtoEscritura, con todas las Escrituras que coincidan con
     * el numero indicado.
     */
    public List<DtoEscritura> buscarEscrituraPorNumero(DtoEscritura miDtoEscritura)
    {
        List<DtoEscritura> escriturasEncontradas = new ArrayList<>();
        List<Escritura> escrituras = null;

        escrituras = miJpaEscritura.findEscrituraByNumero(miDtoEscritura.getNumero());

        if (escrituras != null && !escrituras.isEmpty())
        {
            for (Iterator<Escritura> it = escrituras.iterator(); it.hasNext();)
            {
                Escritura escritura = it.next();
                DtoEscritura miEscritura = escritura.getDto();

                escriturasEncontradas.add(miEscritura);
            }
        }

        return escriturasEncontradas;
    }

    /**
     * Busca una Escritura en particular, que este con estado "Firmada"
     *
     * @param miDtoEscritura, datos de la Escritura a buscar (Numero de
     * Escritura)
     * @return Lista de DtoEscritura, con todas las Escrituras que coincidan con
     * el numero indicado.
     */
    public List<DtoEscritura> buscarEscrituraPorNumeroFirmada(DtoEscritura miDtoEscritura)
    {
        List<DtoEscritura> escriturasEncontradas = new ArrayList<>();
        List<Escritura> escrituras = null;

        escrituras = miJpaEscritura.findEscrituraByNumero(miDtoEscritura.getNumero());

        if (escrituras != null && !escrituras.isEmpty())
        {
            for (Iterator<Escritura> it = escrituras.iterator(); it.hasNext();)
            {
                Escritura escritura = it.next();

                DtoEscritura miEscritura = escritura.getDto();

                if (escritura.getEstado().equals(ConstantesNegocio.ESCRITURA_FIRMADA))
                {
                    escriturasEncontradas.add(miEscritura);
                }
            }
        }

        return escriturasEncontradas;
    }

    public List<DtoEscritura> buscarEscrituraPorNumeroFirmadaSinArchivo(DtoEscritura miDtoEscritura)
    {
        List<DtoEscritura> escriturasEncontradas = new ArrayList<>();
        List<Escritura> escrituras = null;

        escrituras = miJpaEscritura.findEscrituraByNumero(miDtoEscritura.getNumero());

        if (escrituras != null && !escrituras.isEmpty())
        {
            for (Iterator<Escritura> it = escrituras.iterator(); it.hasNext();)
            {
                Escritura escritura = it.next();

                Tramite miTramite = escritura.getTramiteList().get(0);
                GestionDeEscritura miGestion = miTramite.getFkIdGestion();

                if (!miGestion.getFkIdEstadoDeGestion().getNombre().equals(ConstantesNegocio.GESTION_ARCHIVADA))
                {
                    DtoEscritura miEscritura = escritura.getDto();

                    if (escritura.getEstado().equals(ConstantesNegocio.ESCRITURA_FIRMADA))
                    {
                        escriturasEncontradas.add(miEscritura);
                    }
                }
            }
        }

        return escriturasEncontradas;
    }

    /**
     * Busca una Escritura en particular, que este con estado "Firmada" o
     * "Inscripta".
     *
     * @param miDtoEscritura, datos de la Escritura a buscar (Numero de
     * Escritura)
     * @return Lista de DtoEscritura, con todas las Escrituras que coincidan con
     * el numero indicado.
     */
    public List<DtoEscritura> buscarEscrituraPorNumeroFirmadaInscripta(DtoEscritura miDtoEscritura)
    {
        List<DtoEscritura> escriturasEncontradas = new ArrayList<>();
        List<Escritura> escrituras = null;

        escrituras = miJpaEscritura.findEscrituraByNumero(miDtoEscritura.getNumero());

        if (escrituras != null && !escrituras.isEmpty())
        {
            for (Iterator<Escritura> it = escrituras.iterator(); it.hasNext();)
            {
                Escritura escritura = it.next();
                DtoEscritura miEscritura = escritura.getDto();

                if (escritura.getEstado().equals(ConstantesNegocio.ESCRITURA_FIRMADA)
                        || escritura.getEstado().equals(ConstantesNegocio.ESCRITURA_INSCRIPTA))
                {
                    escriturasEncontradas.add(miEscritura);
                }
            }
        }

        return escriturasEncontradas;
    }

    /**
     * busca una Escritura especifica.
     *
     * @param miDtoEscritura, datos de la Escritura a buscar (IdEscritura)
     * @return El DtoEscritura, de la Escritura encontrada.
     */
    public DtoEscritura buscarEscritura(DtoEscritura miDtoEscritura)
    {
        DtoEscritura escrituraEncontrada = null;
        Escritura escritura = null;

        escritura = miJpaEscritura.findEscrituraById(miDtoEscritura.getIdEscritura());

        if (escritura != null)
        {
            escrituraEncontrada = escritura.getDto();
        }

        return escrituraEncontrada;
    }

    /**
     * Modifica los datos de una Escritura en particular.
     *
     * @param miDtoEscritura, datos nuevos de la Escritura a modificar.
     * @return TRUE si se modifico, FALSE de lo contrario.
     * @throws ClassEliminatedException, si la clase fue eliminada durante la
     * modificacion.
     * @throws ClassModifiedException, si la clase fue modificada durante la
     * modificacion.
     */
    public Boolean modificarEscritura(DtoEscritura miDtoEscritura) throws ClassEliminatedException, ClassModifiedException
    {
        Boolean modificada = false;
        Escritura escrituraEncontrada = miJpaEscritura.findEscrituraById(miDtoEscritura.getIdEscritura());
        List<Tramite> tramitesEscrituraVieja = new ArrayList<>();

        if (escrituraEncontrada != null)
        {

            for (Iterator<Tramite> it = escrituraEncontrada.getTramiteList().iterator(); it.hasNext();)
            {
                Tramite miTramite = it.next();
                tramitesEscrituraVieja.add(miTramite);
            }

            escrituraEncontrada.setAtributos(miDtoEscritura);

            for (Iterator<DtoFolio> it = miDtoEscritura.getFolios().iterator(); it.hasNext();)
            {
                DtoFolio dtoFolio = it.next();
                Folio miFolio = new Folio();
                miFolio.setAtributos(dtoFolio);

                miJpaFolio.modificarFolio(miFolio);

                this.registrarAuditoria(miFolio, ConstantesGui.MODIFICAR_ESCRITURA);

            }

            modificada = miJpaEscritura.modificarEscritura(escrituraEncontrada);

            if (modificada == true)
            {
                this.registrarAuditoria(escrituraEncontrada, ConstantesGui.MODIFICAR_ESCRITURA);
            }
            for (Iterator<Tramite> it = tramitesEscrituraVieja.iterator(); it.hasNext();)
            {
                Tramite tramite = it.next();

                if (!escrituraEncontrada.getTramiteList().contains(tramite))
                {
                    tramite = miJpaTramite.encontrarTramite(tramite.getIdTramite());
                    tramite.setFkIdEscritura(null);
                    Boolean modificado = miJpaTramite.editTramite(tramite);

                    if (modificado == true)
                    {
                        this.registrarAuditoria(tramite, ConstantesGui.MODIFICAR_ESCRITURA);
                    }
                }
            }

        } else
        {
            throw new ClassEliminatedException();
        }

        return modificada;
    }

    /**
     * Busca un Tramite especifico.
     *
     * @param miDtoTramite, datos del Tramite a buscar (IdTramite)
     * @return DtoTramite del Tramite encontrado.
     */
    public DtoTramite buscarTramite(DtoTramite miDtoTramite)
    {

        DtoTramite tramiteEncontrado = null;
        Tramite tramite = miJpaTramite.encontrarTramite(miDtoTramite.getIdTramite());

        if (tramite != null)
        {
            tramiteEncontrado = tramite.getDto();
        }

        return tramiteEncontrado;
    }

    /**
     * Busca el Escribano asociado a la Escritura indicada.
     *
     * @param miDtoEscritura, numero de la Escritura a buscar.
     * @return DtoPersona con el Escribano asociado.
     */
    public DtoPersona obtenerEscribanoEscritura(DtoEscritura miDtoEscritura)
    {
        DtoPersona miPersonaEscribano = null;
        Escritura miEscritura = miJpaEscritura.findEscrituraById(miDtoEscritura.getIdEscritura());
        List<Tramite> tramites = miEscritura.getTramiteList();

        if (tramites != null)
        {
            Persona miPersona = tramites.get(0).getFkIdGestion().getFkIdPersonaEscribano();

            miPersonaEscribano = miPersona.getDto();
        }

        return miPersonaEscribano;
    }

    /**
     * Busca las Escrituras de una Gestion en particular.
     *
     * @param miDtoGestion, datos de la Gestion a buscar.
     * @return Lista de DtoEscritura, de las Escrituras asociadas a la Gestion
     * indicada.
     */
    public List<DtoEscritura> buscarEscriturasGestion(DtoGestionDeEscritura miDtoGestion)
    {
        List<DtoEscritura> listaDtoEscrituras = new ArrayList<>();
        List<Tramite> tramitesGestion = null;
        GestionDeEscritura miGestion = null;

        miGestion = miJpaGestionDeEscritura.findGestionDeEscrituraPorNumero(miDtoGestion.getNumero());

        if (miGestion != null && miGestion.getNumero() != ConstantesPersistencia.VERSION_INICIAL)
        {
            tramitesGestion = miGestion.getTramiteList();

            if (tramitesGestion != null && !tramitesGestion.isEmpty())
            {
                for (Iterator<Tramite> it = tramitesGestion.iterator(); it.hasNext();)
                {
                    Tramite tramite = it.next();
                    Tramite miTramite = miJpaTramite.encontrarTramite(tramite.getIdTramite());

                    if (miTramite.getFkIdEscritura() != null)
                    {
                        Escritura miEscritura = miJpaEscritura.findEscrituraById(miTramite.getFkIdEscritura().getIdEscritura());

                        if (miEscritura != null)
                        {
                            listaDtoEscrituras.add(miEscritura.getDto());
                        }
                    }
                }
            }
        }


        return listaDtoEscrituras;
    }

    /**
     * Verifica si una Escritura en particular se inscribe.
     *
     * @param miDtoEscritura, datos de la escritura a buscar (idEscritura)
     * @return TRUE si se inscribe, FALSE de lo contrario.
     */
    public Boolean verificarSeInscribeEscritura(DtoEscritura miDtoEscritura)
    {
        Boolean seInscribe = false;
        Escritura miEscritura = miJpaEscritura.findEscrituraById(miDtoEscritura.getIdEscritura());

        if (miEscritura != null)
        {
            for (Iterator<Tramite> it = miEscritura.getTramiteList().iterator(); it.hasNext();)
            {
                Tramite tramite = it.next();

                if (tramite.getFkIdTipoTramite().getSeInscribe())
                {
                    seInscribe = true;
                    break;
                }
            }
        }


        return seInscribe;
    }

    // </editor-fold>
    // <editor-fold defaultstate="collapsed" desc="Testimonios">
    /**
     * Crea un Testimonio.
     *
     * @param miDtoTestimonio, datos del Testimonio a crear.
     * @param listaDtoCopias, lista de Copias creadas a partir del Testimonio a
     * crear.
     * @return TRUE si se creo el Testimonio, FALSE de lo contrario.
     */
    public Boolean crearTestimonio(DtoTestimonio miDtoTestimonio, List<DtoCopia> listaDtoCopias)
    {
        Boolean creado = false;
        List<Copia> copias = new ArrayList<>();

        for (Iterator<DtoCopia> it = listaDtoCopias.iterator(); it.hasNext();)
        {
            DtoCopia dtoCopia = it.next();
            Copia miCopia = new Copia();
            miCopia.setAtributos(dtoCopia);

            int idCopia = miJpaCopia.crearCopia(miCopia);

            if (idCopia != -1)
            {
                copias.add(miCopia);
            }
        }

        if (!copias.isEmpty())
        {
            Testimonio miTestimonio = new Testimonio();
            miTestimonio.setAtributos(miDtoTestimonio);
            miTestimonio.setCopiaList(copias);

            creado = miJpaTestimonio.create(miTestimonio);

            if (creado)
            {
                this.registrarAuditoria(miTestimonio, ConstantesGui.GENERAR_TESTIMONIO);
            }

        }

        return creado;
    }

    /**
     * Busca los Testimonios generados para una escritura en particular.
     *
     * @param miDtoEscritura, id de la Escritura a buscar.
     * @return Lista de DtoTestimonio con los Testimonios encontrados.
     */
    public List<DtoTestimonio> obtenerTestimoniosEscritura(DtoEscritura miDtoEscritura)
    {
        List<DtoTestimonio> listaDtoTestimonios = new ArrayList<>();
        List<Testimonio> listaTestimonios = null;

        listaTestimonios = miJpaTestimonio.findTestimoniosEscritura(miDtoEscritura.getIdEscritura());

        if (listaTestimonios != null && !listaTestimonios.isEmpty())
        {

            for (Iterator<Testimonio> it = listaTestimonios.iterator(); it.hasNext();)
            {
                Testimonio testimonio = it.next();
                DtoTestimonio miDtoTestimonio = testimonio.getDto();
                listaDtoTestimonios.add(miDtoTestimonio);
            }
        }

        return listaDtoTestimonios;
    }

    /**
     * Busca los MovimientoTestimonio de un Testimonio en particular.
     *
     * @param miDtoTestimonio, id del Testimonio a buscar.
     * @return Lista de DtoMovimientoTestimonio, con los MovimientoTestimonio
     * encontrados.
     */
    public List<DtoMovimientoTestimonio> obtenerMovimientosTestimonio(DtoTestimonio miDtoTestimonio)
    {
        List<DtoMovimientoTestimonio> listaDtoMovimientoTestimonios = new ArrayList<>();
        List<MovimientoTestimonio> listaMovimientos = null;

        listaMovimientos = miJpaMovimientoTestimonio.buscarMovimientosPorTestimonio(miDtoTestimonio.getIdTestimonio());

        if (listaMovimientos != null && !listaMovimientos.isEmpty())
        {
            for (Iterator<MovimientoTestimonio> it = listaMovimientos.iterator(); it.hasNext();)
            {
                MovimientoTestimonio movimientoTestimonio = it.next();
                DtoMovimientoTestimonio miDtoMovimientoTestimonio = movimientoTestimonio.getDto();

                listaDtoMovimientoTestimonios.add(miDtoMovimientoTestimonio);
            }
        }

        return listaDtoMovimientoTestimonios;
    }

    /**
     * Busca las Copias de un Testimonio en particular.
     *
     * @param miDtoTestimonio, id del Testimonio a buscar.
     * @return Lista de DtoCopia con las Copias encontradas.
     */
    public List<DtoCopia> obtenerCopiasTestimonio(DtoTestimonio miDtoTestimonio)
    {
        List<DtoCopia> listaDtoCopias = new ArrayList<>();
        List<Copia> listaCopias = null;

        listaCopias = miJpaCopia.buscarCopiasTestimonio(miDtoTestimonio.getIdTestimonio());

        if (listaCopias != null && !listaCopias.isEmpty())
        {
            for (Iterator<Copia> it = listaCopias.iterator(); it.hasNext();)
            {
                Copia copia = it.next();
                DtoCopia miDtoCopia = copia.getDto();

                listaDtoCopias.add(miDtoCopia);
            }
        }

        return listaDtoCopias;
    }

    /**
     * Modifica una lista de Copias de un Testimonio en particular.
     *
     * @param listaDtoCopias, lista de DtoCopia a modificar.
     * @param miDtoTestimonio, Testimonio al que se asocian las Copias.
     * @return TRUE si se modificaron las Copias, FALSE de lo contrario.
     * @throws ClassModifiedException
     * @throws ClassEliminatedException
     */
    public Boolean modificarCopiasTestimonio(List<DtoCopia> listaDtoCopias, DtoTestimonio miDtoTestimonio) throws ClassModifiedException, ClassEliminatedException
    {
        Boolean modificada = false;

        List<Copia> listaCopias = null;

        listaCopias = miJpaCopia.buscarCopiasTestimonio(miDtoTestimonio.getIdTestimonio());

        if (listaCopias != null && !listaCopias.isEmpty())
        {
            for (Iterator<Copia> it = listaCopias.iterator(); it.hasNext();)
            {
                Copia copia = it.next();

                for (Iterator<DtoCopia> it1 = listaDtoCopias.iterator(); it1.hasNext();)
                {
                    DtoCopia dtoCopia = it1.next();

                    if (dtoCopia.getIdCopia().intValue() == copia.getIdCopia().intValue())
                    {
                        copia.setAtributos(dtoCopia);
                        modificada = miJpaCopia.modificarCopia(copia);
                        break;
                    }
                }
            }
        }

        return modificada;
    }

    /**
     * Verifica si un testimonio de una escritura ya fue ingresado para
     * inscribir.
     *
     * @param miDtoEscritura, datos de la escritura a buscar el testimonio.
     * @return TRUE, si esta ingresado, FALSE de lo contrario.
     */
    public Boolean verificarTestimonioIngresadoParaInscribir(DtoEscritura miDtoEscritura)
    {
        Boolean estaIngresado = false;
        Testimonio miTestimonio = null;
        List<DtoMovimientoTestimonio> movimientos = null;

        List<Testimonio> misTestimonios = miJpaTestimonio.findTestimoniosEscritura(miDtoEscritura.getIdEscritura());

        miTestimonio = misTestimonios.get(misTestimonios.size() - 1);

        movimientos = obtenerMovimientosTestimonio(miTestimonio.getDto());

        if (movimientos != null && !movimientos.isEmpty())
        {
            estaIngresado = true;
        }

        return estaIngresado;
    }

    // </editor-fold>
    // <editor-fold defaultstate="collapsed" desc="Inscripciones">
    /**
     * Crea un MovimientoTestimonio para un Testimonio en particular.
     *
     * @param miDtoMovimientoTestimonio, datos del MovimientoTestimonio a crear.
     * @return TRUE si se creo, FALSE de lo contrario.
     */
    public Boolean crearMovimientoTestimonio(DtoMovimientoTestimonio miDtoMovimientoTestimonio) throws ClassModifiedException, ClassEliminatedException
    {
        Boolean creado = false;
        Boolean modificado = true;
        Testimonio miTestimonio = null;

        miTestimonio = miJpaTestimonio.findTestimonioById(miDtoMovimientoTestimonio.getTestimonio().getIdTestimonio());

        if (miTestimonio != null)
        {
            miTestimonio.setObservado(miDtoMovimientoTestimonio.getTestimonio().isObservado());

            modificado = miJpaTestimonio.modificarTestimonio(miTestimonio);

            if (modificado)
            {
                MovimientoTestimonio miMovimientoTestimonio = new MovimientoTestimonio();
                miMovimientoTestimonio.setAtributos(miDtoMovimientoTestimonio);
                creado = miJpaMovimientoTestimonio.create(miMovimientoTestimonio);

                if (creado)
                {
                    this.registrarAuditoria(miMovimientoTestimonio, ConstantesGui.INGRESAR_PARA_INSCRIPCION);
                }
            }
        }

        return creado;
    }

    /**
     * Busca un MovimientoTestimonio por el id.
     *
     * @param miDtoMovimientoTestimonio, id del movimientoTestimonio a buscar.
     * @return DtoMovimientoTestimonio del MovimientoTestimonio encontrado.
     */
    public DtoMovimientoTestimonio buscarMovimientoTestimonio(DtoMovimientoTestimonio miDtoMovimientoTestimonio)
    {
        DtoMovimientoTestimonio movimientoTestimonioEncontrado = null;
        MovimientoTestimonio movimientoTestimonio;

        movimientoTestimonio = miJpaMovimientoTestimonio.findMovimientoById(miDtoMovimientoTestimonio.getIdMovimientoTestimonio());

        if (movimientoTestimonio != null)
        {
            movimientoTestimonioEncontrado = movimientoTestimonio.getDto();
        }

        return movimientoTestimonioEncontrado;
    }

    /**
     * Modifica los datos de un MovimientoTestimonio en particular.
     *
     * @param miDtoMovimientoTestimonio, datos a modificar.
     * @return TRUE si se modifico, FALSE de lo contrario.
     * @throws ClassEliminatedException
     * @throws ClassModifiedException
     */
    public Boolean modificarMovimientoTestimonio(DtoMovimientoTestimonio miDtoMovimientoTestimonio) throws ClassEliminatedException, ClassModifiedException
    {
        Boolean modificado = true;
        MovimientoTestimonio movimientoTestimonio;

        movimientoTestimonio = miJpaMovimientoTestimonio.findMovimientoById(miDtoMovimientoTestimonio.getIdMovimientoTestimonio());

        if (movimientoTestimonio != null)
        {
            movimientoTestimonio.setAtributos(miDtoMovimientoTestimonio);
            modificado = miJpaMovimientoTestimonio.edit(movimientoTestimonio);

            if (modificado)
            {
                this.registrarAuditoria(movimientoTestimonio, ConstantesGui.REGISTRAR_REINGRESO);
            }
        }

        return modificado;
    }

    /**
     * Modifica los datos de un MovimientoTestimonio en particular en
     * inscripcion.
     *
     * @param miDtoMovimientoTestimonio, datos a modificar.
     * @return TRUE si se modifico, FALSE de lo contrario.
     * @throws ClassEliminatedException
     * @throws ClassModifiedException
     */
    public Boolean modificarMovimientoTestimonioInscripcion(DtoMovimientoTestimonio miDtoMovimientoTestimonio, DtoEscritura miDtoEscritura) throws ClassEliminatedException, ClassModifiedException
    {
        Boolean modificado = true;
        MovimientoTestimonio movimientoTestimonio;

        movimientoTestimonio = miJpaMovimientoTestimonio.findMovimientoById(miDtoMovimientoTestimonio.getIdMovimientoTestimonio());

        if (movimientoTestimonio != null)
        {
            movimientoTestimonio.setAtributos(miDtoMovimientoTestimonio);
            modificado = miJpaMovimientoTestimonio.edit(movimientoTestimonio);

            if (modificado)
            {
                this.registrarAuditoria(movimientoTestimonio, ConstantesGui.REGISTRAR_INSCRIPCION);

                Testimonio miTestimonio = miJpaTestimonio.findTestimonioById(miDtoMovimientoTestimonio.getTestimonio().getIdTestimonio());

                Escritura miEscritura = miJpaEscritura.findEscrituraById(miTestimonio.getFkIdEscritura().getIdEscritura());
                miEscritura.setMatriculaInscripcion(miDtoEscritura.getMatriculaInscripcion());
                miEscritura.setFechaInscripcion(miDtoEscritura.getFechaInscripcion());
                miEscritura.setEstado(miDtoEscritura.getEstado());

                modificado = miJpaEscritura.modificarEscrituraSimple(miEscritura);

                if (modificado)
                {
                    this.registrarAuditoria(miEscritura, ConstantesGui.REGISTRAR_INSCRIPCION);
                }
            }
        }

        return modificado;
    }

    // </editor-fold>
// </editor-fold>
// <editor-fold defaultstate="collapsed" desc="Protocolo">
    // <editor-fold defaultstate="collapsed" desc="Folios">
    /**
     * Permite buscar un tipo de folio en particular, recibiendo como argumento
     * un Dto de TipoFolio con el nombre del tipo de folio a buscar.
     *
     * @param dtoTipoDeFolio Dto con el nombre del tipo de folio a buscar.
     * @return miTipoDeFolio Un Dto de Tipo de folio encontrado o Null en caso
     * contrario.
     */
    public DtoTipoDeFolio buscarTipoDeFolio(DtoTipoDeFolio dtoTipoDeFolio)
    {
        DtoTipoDeFolio miTipoDeFolio = null;
        TipoDeFolio tipoDeFolioEncontrado = null;
        List<TipoDeFolio> listaTipoDeFolios = null;

        try
        {
            TipoDeFolioJpaController mitipoDeFolioJpaController = (TipoDeFolioJpaController) this.getMiAdministradorJpa().obtenerJpa(TipoDeFolioJpaController.class.getName());

            listaTipoDeFolios = mitipoDeFolioJpaController.findTipoDeFolioEntities();

            for (Iterator<TipoDeFolio> it = listaTipoDeFolios.iterator(); it.hasNext();)
            {
                TipoDeFolio tipoDeFolio = it.next();

                if (tipoDeFolio.getNombre().contains(dtoTipoDeFolio.getNombre()))
                {
                    tipoDeFolioEncontrado = tipoDeFolio;

                    miTipoDeFolio = tipoDeFolioEncontrado.getDto();

                    break;
                }
            }
        } catch (NonexistentJpaException e)
        {
            e.printStackTrace();
        } catch (PersistenceException e)
        {
            e.printStackTrace();
        }
        return miTipoDeFolio;
    }

    /**
     * Determina si ya se encuentran o no, registrados un conjunto de folios,
     * determinado por el numero "desde", hasta el numero "hasta" para un
     * determinado escribano. En caso de no encontrarse registrado ese conjunto
     * de folios, entonces el nuevo grupo de folios es valido.
     *
     * @param desde Folio inicial.
     * @param hasta Folio final.
     * @return Verdadero si se puede ingresar el conjunto de folios indicados,
     * falso en caso contrario.
     */
    public Boolean verificarExistenciaFolios(DtoFolio desde, DtoFolio hasta)
    {
        boolean resultado = false;

        List<Folio> folios = miJpaFolio.findFoliosRegistroAnio(desde.getPersonaEscribano().getRegistroEscribano(), desde.getAnio());

        if (!folios.isEmpty())
        {

            for (Iterator<Folio> it = folios.iterator(); it.hasNext();)
            {
                Folio folio = it.next();
                if ((folio.getNumero() == desde.getNumero()) || folio.getNumero() == hasta.getNumero())
                {
                    return false;
                }
            }

            resultado = true;
        } else
        {
            resultado = true;
        }

        return resultado;
    }

    /**
     * Permite registrar el ingreso de un conjunto de folios nuevos, para un
     * determinado a�o y registro de escribano dados.
     *
     * @param desde
     * @param hasta
     * @return resultado Verdadero si se pudo registrar el ingreso o falso en
     * caso contrario.
     */
    public Boolean registrarIngresoNuevosFolios(DtoFolio desde, DtoFolio hasta)
    {
        Boolean resultado = Boolean.FALSE;

        try
        {
            DtoPersona miDtoPersonaEscribano = this.buscarPersonaTipoNumeroIdentificacion(desde.getPersonaEscribano());
            Persona escribano = new Persona();
            escribano.setAtributos(miDtoPersonaEscribano);


            int j = hasta.getNumero();

            for (int i = desde.getNumero(); i <= j; i++)
            {

                Folio nuevoFolio = new Folio();

                desde.setNumero(i);

                nuevoFolio.setAtributos(desde);

                TipoDeFolio protocoloPrincipal = new TipoDeFolio();

                try
                {
                    protocoloPrincipal.setAtributos(desde.getTiposDeFolio());
                } catch (DtoInvalidoException ex)
                {
                    Logger.getLogger(ControllerNegocio.class.getName()).log(Level.SEVERE, null, ex);
                }

                nuevoFolio.setFkIdPersonaEscribano(escribano);
                nuevoFolio.setFkIdTipoFolio(protocoloPrincipal);

                miJpaFolio.create(nuevoFolio);

                this.registrarAuditoria(nuevoFolio, ConstantesGui.INGRESAR_NUEVOS_FOLIOS);
            }



            resultado = Boolean.TRUE;
        } catch (PersistenceException e)
        {
            e.printStackTrace();
        }

        return resultado;
    }

    /**
     * Permite obtener una lista de todos los folios disponibles para un anio y
     * registro de escribano dados. Tanto el anio como el registro de escribano
     * vienen detallados en el parametro (Dto tipo Folio).
     *
     * @param dtoDatosRegistroAnio Un Dto Folio con el registro de escribano y
     * anio necesarios para realizar la busqueda
     * @return listaFoliosDisponibles Una lista de todos los folios encontrados,
     * para el registro y anio indicados.
     */
    public List<DtoFolio> obtenerListaFolios(DtoFolio dtoDatosRegistroAnio)
    {
        List<DtoFolio> dtoListaFolios = new ArrayList<>();
        List<Folio> listaFolios = null;

        try
        {
            FolioJpaController miFolioJpaController;
            try
            {
                miFolioJpaController = (FolioJpaController) this.getMiAdministradorJpa().obtenerJpa(FolioJpaController.class.getName());

                listaFolios = miFolioJpaController.findFoliosRegistroAnio(dtoDatosRegistroAnio.getPersonaEscribano().getRegistroEscribano(), dtoDatosRegistroAnio.getAnio());
            } catch (NonexistentJpaException e)
            {
                e.printStackTrace();
            }

            for (Iterator<Folio> it = listaFolios.iterator(); it.hasNext();)
            {
                Folio folio = it.next();
                dtoListaFolios.add(folio.getDto());
            }

        } catch (PersistenceException e)
        {
            e.printStackTrace();
        }

        return dtoListaFolios;
    }

    /**
     * Permite modificar alguno de los datos de un folio.
     *
     * @param folioModificado El folio que ha sido modificado.
     * @return resultado Verdadero si se pudieron modificar los datos del folio,
     * falso en caso contrario.
     * @throws NonexistentJpaException
     * @throws ClassModifiedException
     * @throws ClassEliminatedException
     */
    public Boolean modificarFolio(DtoFolio dtoFolioModificado) throws NonexistentJpaException, ClassModifiedException, ClassEliminatedException, NonexistentEntityException
    {
        Boolean resultado = Boolean.FALSE;

        //Folio folioModificado = miJpaFolio.findFolio(folioModificado.getIdFolio());

        Folio folioModificado = new Folio();

        folioModificado.setAtributos(dtoFolioModificado);

        if (miJpaFolio.modificarFoliosCompleto(folioModificado))
        {
            resultado = Boolean.TRUE;

            this.registrarAuditoria(folioModificado, ConstantesGui.MODIFICAR_FOLIO);
        }
        return resultado;
    }

    /**
     * Busca los folios disponibles para usar, de un Escribano en particular.
     *
     * @param numeroRegistro, numero del registro de Escribano
     * @return una lista de DtoFolio, de Folios disponibles.
     */
    public List<DtoFolio> buscarFoliosDisponibles(Integer numeroRegistro)
    {
        List<DtoFolio> listaDtoFoliosDisponibles = new ArrayList<>();
        List<Folio> listaFolios = null;
        List<Folio> listaFoliosDisponibles = new ArrayList<>();

        listaFolios = miJpaFolio.findFolioEntities();

        if (listaFolios != null && !listaFolios.isEmpty())
        {
            for (Iterator<Folio> it = listaFolios.iterator(); it.hasNext();)
            {
                Folio folio = it.next();
                Integer numeroRegistroFolio = folio.getFkIdPersonaEscribano().getRegistroEscribano();
                if (numeroRegistroFolio.intValue() == numeroRegistro.intValue())
                {
                    if (folio.getEstado().equals(ConstantesNegocio.ESTADO_FOLIO_NUEVOS))
                    {
                        listaFoliosDisponibles.add(folio);
                    }
                }
            }
            for (Iterator<Folio> it1 = listaFoliosDisponibles.iterator(); it1.hasNext();)
            {
                Folio folio = it1.next();

                listaDtoFoliosDisponibles.add(folio.getDto());
            }
        }

        return listaDtoFoliosDisponibles;
    }
    // </editor-fold>
// </editor-fold>
// <editor-fold defaultstate="collapsed" desc="Pagos">

    /**
     * Da de alta un Pago.
     *
     * @param miDtoPago, DtoPago con los datos del nuevo Pago.
     * @return TRUE si se creo, FALSE en caso contrario.
     */
    public Boolean darAltaPago(DtoPago miDtoPago)
    {
        Boolean creado = false;
        int id = -1;

        Pago miPago = new Pago();

        miPago.setAtributos(miDtoPago);

        id = miJpaPago.create(miPago);

        if (id != -1)
        {
            creado = true;

            this.registrarAuditoria(miPago, ConstantesGui.MODIFICAR_PRESUPUESTO);
        }

        return creado;
    }

    /**
     * Busca los Pagos asociados a un Presupuesto en particular.
     *
     * @param miDtoPresupuesto, DtoPresupuesto con los datos del Presupuesto a
     * buscar.
     * @return Una lista con los Pagos asociados al Presupuesto indicado.
     */
    public List<DtoPago> buscarPagosPresupuesto(DtoPresupuesto miDtoPresupuesto)
    {
        List<DtoPago> dtosPagos = new ArrayList<>();
        List<Pago> pagos = null;

        pagos = miJpaPago.findPagosPresupuesto(miDtoPresupuesto.getIdPresupuesto());

        if (pagos != null && !pagos.isEmpty())
        {
            for (int i = 0; i < pagos.size(); i++)
            {
                Pago pago = pagos.get(i);
                DtoPago miDtoPago = pago.getDto();

                dtosPagos.add(miDtoPago);
            }
        }

        return dtosPagos;
    }

// </editor-fold>
// <editor-fold defaultstate="collapsed" desc="Administracion">
    // <editor-fold defaultstate="collapsed" desc="Usuarios">
    /**
     * Metodo que permite dar de alta un usuario
     *
     * @param dtoUsuario
     * @return El dtoUsuario para confirmar el alta
     */
    public DtoUsuario darAltaUsuario(DtoUsuario dtoUsuario)
    {
        Usuario usuario = new Usuario();
        List<Usuario> listaUsuarios = new ArrayList<>();
        Boolean flag = false;
        usuario.setAtributos(dtoUsuario);

        //Encrypto la clave del usuario
        String clave = usuario.getContrasenia();
        usuario.setContrasenia(this.encriptaEnMD5(clave));


        //Controlo que el nombre del usuario no se repita            

        listaUsuarios = (ArrayList<Usuario>) miJpaUsuario.buscarUsuarios();

        for (int i = 0; i < listaUsuarios.size(); i++)
        {
            if (listaUsuarios.get(i).getNombre().equals(dtoUsuario.getNombre()))
            {
                flag = true;
            }
        }

        if (!flag)
        {
            try
            {
                miJpaUsuario.create(usuario);

                this.registrarAuditoria(usuario, ConstantesGui.DAR_ALTA_USUARIO);
            } catch (Exception ex)
            {
                //  No se pudo crear el usuario.
                Logger.getLogger(ControllerNegocio.class.getName()).log(Level.SEVERE, null, ex);
            }
        } else
        {
            dtoUsuario = null;
        }

        return dtoUsuario;
    }

    public String encriptaEnMD5(String stringAEncriptar)
    {
        {
            char[] CONSTS_HEX =
            {
                '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'
            };
            try
            {
                MessageDigest msgd = MessageDigest.getInstance("MD5");
                byte[] bytes = msgd.digest(stringAEncriptar.getBytes());
                StringBuilder strbCadenaMD5 = new StringBuilder(2 * bytes.length);
                for (int i = 0; i < bytes.length; i++)
                {
                    int bajo = (int) (bytes[i] & 0x0f);
                    int alto = (int) ((bytes[i] & 0xf0) >> 4);
                    strbCadenaMD5.append(CONSTS_HEX[alto]);
                    strbCadenaMD5.append(CONSTS_HEX[bajo]);
                }
                return strbCadenaMD5.toString();
            } catch (NoSuchAlgorithmException e)
            {
                return null;
            }
        }
    }

    /**
     * Metodo que permite buscar un usuario
     *
     * @param dtoUsuario
     * @return El dtoUsuario para confirmar la busqueda
     */
    public DtoUsuario buscarUsuario(DtoUsuario dtoUsuario) throws NonexistentJpaException
    {

        List<Usuario> listaUsuarios = new ArrayList<>();
        Usuario miUsuario = null;
        Integer idPersona = dtoUsuario.getPersonas().getIdPersona();
        boolean flag = false;

        //Busco los usuario actuales

        listaUsuarios = miJpaUsuario.buscarUsuarios();


        if (!listaUsuarios.isEmpty())
        {

            //Busco coincidencia de id_persona con fk_id_usuario

            for (int i = 0; i < listaUsuarios.size(); i++)
            {
                if (idPersona.intValue() == listaUsuarios.get(i).getFkIdPersona().getIdPersona().intValue())
                {
                    flag = true;
                    miUsuario = listaUsuarios.get(i);
                    break;
                }

            }

            if (flag)
            {
                // Tener en cuenta que el dtoUsuario ya tiene la referencia a la persona.
                dtoUsuario.setContrasenia(miUsuario.getContrasenia());
                dtoUsuario.setEstado(miUsuario.getEstado());
                dtoUsuario.setIdUsuario(miUsuario.getIdUsuario());
                dtoUsuario.setNombre(miUsuario.getNombre());
                dtoUsuario.setTipo(miUsuario.getTipo());
                dtoUsuario.setPersonas(miUsuario.getFkIdPersona().getDto());

            } else
            {
                dtoUsuario = null;
            }
        } else
        {
            //  La lista de usuario es vacia, error.
            dtoUsuario = null;
        }

        return dtoUsuario;
    }

    /**
     * Metodo que permite buscar los usuarios registrados en el sistema
     *
     * @return Una lista de DtoUsuario con el resultado de busqueda
     */
    public ArrayList<DtoUsuario> buscarUsuariosDisponibles() throws NonexistentJpaException
    {

        List<Usuario> listaUsuarios = null;
        ArrayList<DtoUsuario> listaDtoUsuarios = null;
        Usuario miUsuario = new Usuario();
        boolean flag = false;

        //Busco los usuario disponibles
        // listaUsuarios = UsuarioJpaController.getInstancia().buscarUsuarios();

        listaUsuarios = miJpaUsuario.buscarUsuarios();

        if (!listaUsuarios.isEmpty())
        {
            listaDtoUsuarios = new ArrayList<DtoUsuario>();
            flag = true;

            for (int i = 0; i < listaUsuarios.size(); i++)
            {
                DtoUsuario miDtoUsuario = new DtoUsuario();
                miDtoUsuario = listaUsuarios.get(i).getDto();
                listaDtoUsuarios.add(miDtoUsuario);
            }
        } else
        {
            listaDtoUsuarios = null;
        }

        return listaDtoUsuarios;
    }

    /**
     * Metodo que permite modificar un usuario del sistema
     *
     * @param miDtoUsuario
     * @return El dtoUsuario para confirmar la modificacion.
     */
    public DtoUsuario modificarUsuario(DtoUsuario miDtoUsuario) throws ClassModifiedException, ClassEliminatedException
    {

        Usuario miUsuario = new Usuario();

        ArrayList<Usuario> listaUsuarios = null;
        Boolean flag = false;//Variable para controlar el resultado de la modificacion
        int count = 0;

        miUsuario.setContrasenia(miDtoUsuario.getContrasenia());
        miUsuario.setEstado(miDtoUsuario.isEstado());
        miUsuario.setNombre(miDtoUsuario.getNombre());
        miUsuario.setTipo(miDtoUsuario.getTipo());
        miUsuario.setIdUsuario(miDtoUsuario.getIdUsuario());
        miUsuario.setVersion(miDtoUsuario.getVersion());

        //Encrypto la clave del usuario
        String clave = miUsuario.getContrasenia();
        miUsuario.setContrasenia(this.encriptaEnMD5(clave));


        //Controlo que el nombre del usuario no se repita

        listaUsuarios = (ArrayList<Usuario>) miJpaUsuario.buscarUsuarios();

        if (listaUsuarios != null)
        {
            for (int i = 0; i < listaUsuarios.size(); i++)
            {
                if (listaUsuarios.get(i).getNombre().equals(miDtoUsuario.getNombre()))
                {
                    count++; //El usuario puede repetirse una vez, que es el nombre actual.
                }
            }
            if (count <= 1)
            {

                flag = miJpaUsuario.modificarUsuario(miUsuario);

                if (flag)
                {

                    this.registrarAuditoria(miUsuario, ConstantesGui.MODIFICAR_USUARIO);
                } else
                {
                    //  error al modificar usuario.
                }
            } else if (!flag)
            {
            } else if (!flag)
            {
                miDtoUsuario = null;
            }
        }
        return miDtoUsuario;

    }

    /**
     * Metodo que permite comparar dos campos encriptados
     *
     * @param j1
     * @param j2
     * @return un dtoBoolean con el resultado de la comparacion
     */
    public DtoFlag isPasswordCorrect(char[] j1, char[] j2)
    {
        DtoFlag dtoFlag = new DtoFlag();
        boolean valor = true;
        int puntero = 0;
        if (j1.length != j2.length)
        {
            valor = false;
        } else
        {
            while ((valor) && (puntero < j1.length))
            {
                if (j1[puntero] != j2[puntero])
                {
                    valor = false;
                }
                puntero++;
            }
        }
        dtoFlag.setFlag(valor);
        return dtoFlag;
    }

    /**
     * Metodo que permite buscar el registro de actividades de un usuario
     * determinado en el sistema, dentro de un rango de fechas indicado.
     *
     * @param miDtoUsuario El usuario seleccionado para buscar el registro de
     * sus actividades.
     * @return Una lista con todos los registros correspondientes a un usuario
     * determinado.
     * @throws NonexistentJpaException
     */
    public ArrayList<DtoRegistroAuditoria> buscarRegistrosAuditoria(DtoUsuario miDtoUsuario) throws NonexistentJpaException
    {

        Usuario miUsuario = new Usuario();
        miUsuario.setIdUsuario(miDtoUsuario.getIdUsuario());
        ArrayList<DtoRegistroAuditoria> dtoListaAuditoria = new ArrayList<>();
        ArrayList<RegistroAuditoria> listaRegistroAuditoria = new ArrayList<>();

        listaRegistroAuditoria = miJpaAuditoria.buscarRegistroAuditoriasUsuario(miUsuario);

        if (listaRegistroAuditoria != null)
        {
            dtoListaAuditoria = new ArrayList<>();
            for (int i = 0; i < listaRegistroAuditoria.size(); i++)
            {
                dtoListaAuditoria.add(listaRegistroAuditoria.get(i).getDto());

            }
        }

        return dtoListaAuditoria;

    }

    /**
     * Metodo que permite registrarAuditoria los movimientos del sistema, no
     * recibe como parametro un objeto de tipo DTO, porque no recibe parametros
     * desde la GUI, es a nivel de controller.
     *
     * @param registroAuditoria
     * @param Objet objeto para auditar,String Modulo del evento
     * @return Un DtoFlag con verdadero, si se ejecuto correctamente y falso de
     * no ser asi
     */
    public boolean registrarAuditoria(Object miObjeto, String modulo)
    {
        boolean flag = false;

        RegistroAuditoria auditoria = new RegistroAuditoria();
        auditoria.setDetalleOperacion(miObjeto.toString());
        auditoria.setFkIdUsuario(AdministradorSesion.getInstancia().getSesionUsuario());
        auditoria.setModulo(modulo);
        auditoria.setFecha(new Date());

        if (miJpaAuditoria.create(auditoria))
        {
            flag = true;
        }

        return flag;
    }

    // </editor-fold>
    // <editor-fold defaultstate="collapsed" desc="Escribanos">
    /**
     * Permite dar de alta un nuevo escribano. Una instancia de escribano esta
     * definidida por la existencia de una instancia de Persona, la cual ademas
     * de todos los datos necesarios para una Persona, tiene un numero de
     * registro, lo cual la identifica como un Escribano.
     *
     * @param dtoNuevoEscribano Un Dto Persona, con todos los datos de una
     * persona y ademas con un numero de registro de escribano.
     * @return resultado Verdadero si se pudo dar de alta al escribano, Falso en
     * caso contrario.
     */
    public Boolean darAltaEscribano(DtoPersona dtoNuevoEscribano) throws ClassModifiedException
    {
        Boolean resultado = Boolean.FALSE;

        Persona nuevoEscribano = new Persona();
        nuevoEscribano.setAtributos(dtoNuevoEscribano);

        try
        {
            if (nuevoEscribano.getRegistroEscribano() == null)
            {
                nuevoEscribano.setRegistroEscribano(dtoNuevoEscribano.getRegistroEscribano());
                if (miJpaPersona.registrarEscribano(nuevoEscribano))
                {
                    resultado = Boolean.TRUE;

                    this.registrarAuditoria(nuevoEscribano, ConstantesGui.DAR_ALTA_ESCRIBANO);
                }
            } else
            {
                nuevoEscribano.setRegistroEscribano(dtoNuevoEscribano.getRegistroEscribano());
                if (miJpaPersona.registrarEscribano(nuevoEscribano))
                {
                    resultado = Boolean.TRUE;

                    this.registrarAuditoria(nuevoEscribano, ConstantesGui.MODIFICAR_ESCRIBANO);
                }
            }
        } catch (NonexistentEntityException ex)
        {
            Logger.getLogger(ControllerNegocio.class.getName()).log(Level.SEVERE, null, ex);
        }


        return resultado;
    }

    /**
     * Permite obtener una lista de todos los escribanos registrados en el
     * sistemas.
     *
     * @return listaDtoEscribanos Retorna una lista tipo DtoPersona de todos los
     * escribanos registrados.
     */
    public List<DtoPersona> obtenerListaEscribanosDisponibles()
    {
        List<DtoPersona> listaDtoEscribanos = new ArrayList<>();
        List<Persona> listaPersonas = new ArrayList<>();

        try
        {
            listaPersonas = miJpaPersona.findPersonas();
            listaDtoEscribanos = new ArrayList<>();

            for (Iterator<Persona> it = listaPersonas.iterator(); it.hasNext();)
            {
                Persona unaPersona = it.next();

                if ((unaPersona.getRegistroEscribano() != null) && (unaPersona.getRegistroEscribano() != 0))
                {
                    listaDtoEscribanos.add(unaPersona.getDto());
                }
            }

        } catch (PersistenceException e)
        {
            Logger.getLogger(ControllerNegocio.class.getName()).log(Level.SEVERE, null, e);
        }

        return listaDtoEscribanos;
    }

    /**
     * Permite registra la suplencia de un escribano por otro, para un
     * determinado periodo de tiempo.
     *
     * @param detalleSuplencia Un dto tipo Suplencia con la informacion de la
     * suplencia.
     * @return resultado Verdadero si se pudo registrar la suplencia, falso en
     * caso contrario.
     */
    public Boolean registrarSuplenciaEscribano(DtoSuplencia detalleSuplencia)
    {
        Boolean resultado = Boolean.FALSE;
        try
        {
            Suplencia nuevaSuplencia = new Suplencia();
            nuevaSuplencia.setAtributos(detalleSuplencia);

            try
            {
                miJpaSuplencia.create(nuevaSuplencia);

                resultado = Boolean.TRUE;

                this.registrarAuditoria(nuevaSuplencia, ConstantesGui.REGISTRAR_SUPLENCIA);
            } catch (PersistenceException e)
            {
                e.printStackTrace();
            }

        } catch (DtoInvalidoException ex)
        {
            Logger.getLogger(ControllerNegocio.class.getName()).log(Level.SEVERE, null, ex);
        }
        return resultado;
    }

    /**
     * Metodo que permite obtener una lista de todas las suplencias registradas
     * en un determinado periodo (anio).
     *
     * @param dtoSuplenciasDesde Un dto tipo suplencia indicando el periodo
     * (fecha desde, donde el dato que importa es el anio).
     * @return listaDtoSuplencias una lista tipo dto suplencia con todas las
     * suplencias registradas para el periodo indicado.
     */
    public List<DtoSuplencia> consultarSuplencias(DtoSuplencia dtoSuplenciasDesde)
    {
        List<DtoSuplencia> listaDtoSuplencias = new ArrayList<>();
        List<Suplencia> listaSuplencias = new ArrayList<>();

        Suplencia unaSuplencia = new Suplencia();
        unaSuplencia.setFechaInicio(dtoSuplenciasDesde.getFechaInicio());
        unaSuplencia.setFechaFin(dtoSuplenciasDesde.getFechaFin());

        listaSuplencias = miJpaSuplencia.findSuplenciasPorAnio(unaSuplencia);

        if (!listaSuplencias.isEmpty())
        {
            for (Iterator<Suplencia> it = listaSuplencias.iterator(); it.hasNext();)
            {
                Suplencia suplencia = it.next();

                listaDtoSuplencias.add(suplencia.getDto());
            }
        }

        return listaDtoSuplencias;
    }

    // </editor-fold>
    // <editor-fold defaultstate="collapsed" desc="Tramites">
    /**
     * Da de alta un nuevo Tipo de Tramite.
     *
     * @param miDtoTipoDeTramite Un dto del Tipo de Tramite a crear.
     * @param listaDtoTipoDeDocumentosDocumentos La lista de Tipos de Documento
     * para asociar al Tipo de Tramite en una Plantilla de Tramite.
     * @return resultado Verdadero si se pudo crear el Tipo de Tramite, Falso de
     * lo contrario.
     */
    public Boolean darAltaTipoDeTramite(DtoTipoDeTramite miDtoTipoDeTramite, ArrayList<DtoTipoDeDocumento> listaDtoTipoDeDocumentosDocumentos) throws ClassModifiedException, ClassEliminatedException, PreexistingEntityException, IllegalOrphanException, NonexistentEntityException
    {
        Boolean resultado = Boolean.FALSE;
        Integer idTipoTramite = -1;
        Boolean existe;
        TipoDeTramite miTipoDeTramite = new TipoDeTramite();

        existe = this.existeTipoDeTramite(miDtoTipoDeTramite.getNombre());

        if (!existe)
        {

            // Creo el tipo de tramite

            miTipoDeTramite.setAtributos(miDtoTipoDeTramite);

            try
            {
                // Lo persisto
                idTipoTramite = miJpaTipoTramite.create(miTipoDeTramite);

                // Si se creo correctamente, creo una Plantilla de Tramite por cada Tipo de Documento de la lista.
                if (idTipoTramite != -1)
                {
                    this.registrarAuditoria(miTipoDeTramite, ConstantesGui.INGRESAR_NUEVO_TIPO_DE_TRAMITE);
                    resultado = true;

                    if (listaDtoTipoDeDocumentosDocumentos != null && !listaDtoTipoDeDocumentosDocumentos.isEmpty())
                    {

                        miTipoDeTramite.setIdTipoTramite(idTipoTramite);

                        PlantillaTramite miPlantilla;

                        for (Iterator<DtoTipoDeDocumento> it = listaDtoTipoDeDocumentosDocumentos.iterator(); it.hasNext();)
                        {
                            DtoTipoDeDocumento dtoTipoDeDocumento = it.next();

                            TipoDeDocumento miTipoDeDocumento = miJpaTipoDocumento.findTipoDeDocumento(dtoTipoDeDocumento.getIdTipoDocumento());

                            if (miTipoDeDocumento != null)
                            {

                                miPlantilla = new PlantillaTramite();

                                miPlantilla.setTipoDeDocumento(miTipoDeDocumento);
                                miPlantilla.setTipoDeTramite(miTipoDeTramite);

                                Boolean creada = miJpaPlantillaTramite.create(miPlantilla);

                                this.registrarAuditoria(miPlantilla, ConstantesGui.INGRESAR_NUEVA_PLANTILLA_TRAMITE);

                                if (creada == false)
                                {
                                    resultado = false;
                                }

                            }

                        }
                    }
                }
            } catch (PersistenceException e)
            {
                e.printStackTrace();
            }
        } else
        {
            List<TipoDeTramite> tipoDeTramites = miJpaTipoTramite.findTipoDeTramite(miDtoTipoDeTramite.getNombre());
            miTipoDeTramite = tipoDeTramites.get(0);

            if (miTipoDeTramite.getHabilitado() == false)
            {
                miTipoDeTramite.setHabilitado(true);
                miTipoDeTramite.setNombre(miDtoTipoDeTramite.getNombre());
                miTipoDeTramite.setSeArchiva(miDtoTipoDeTramite.isSeArchiva());
                miTipoDeTramite.setSeInscribe(miDtoTipoDeTramite.isSeInscribe());
                miTipoDeTramite.setAsociaInmuebles(miDtoTipoDeTramite.getAsociaInmuebles());
                miTipoDeTramite.setObservaciones(miDtoTipoDeTramite.getObservaciones());
                // miTipoDeTramite.setVersion(miDtoTipoDeTramite.getVersion());

                resultado = miJpaTipoTramite.edit(miTipoDeTramite);

                this.registrarAuditoria(miTipoDeTramite, ConstantesGui.MODIFICAR_TIPO_DE_TRAMITE);
            } else
            {
                throw new PreexistingEntityException("Ya existe el tipo de tramite.");
            }
        }

        return resultado;
    }

    /**
     * Verifica si existe un Tipo de Tramite en particular, por su nombre.
     *
     * @param nombreTipoTramite El nombre del tipo de tramite.
     * @return resultado Verdadero si existe, Falso de lo contrario.
     */
    private Boolean existeTipoDeTramite(String nombreTipoTramite)
    {
        Boolean resultado = Boolean.FALSE;

        List<TipoDeTramite> miTipoDeTramite = null;

        try
        {
            miTipoDeTramite = miJpaTipoTramite.findTipoDeTramite(nombreTipoTramite);

            if (miTipoDeTramite != null && !miTipoDeTramite.isEmpty())
            {
                resultado = true;
            }
        } catch (PersistenceException e)
        {
            e.printStackTrace();
        }
        return resultado;
    }

    /**
     * Busca y devuelve los Tipos de Tramites existentes.
     *
     * @return dtoListaTiposDeTramites Una lista con los Tipos de Tramite
     * disponibles, null en caso contrario.
     */
    public ArrayList<DtoTipoDeTramite> buscarTiposDeTramiteHabilitados()
    {
        List<TipoDeTramite> tramitesExistentes = null;
        ArrayList<DtoTipoDeTramite> dtoListaTiposDeTramites = new ArrayList<>();

        try
        {

            tramitesExistentes = miJpaTipoTramite.findTipoDeTramiteEntities();

            if (tramitesExistentes != null && !tramitesExistentes.isEmpty())
            {
                for (int i = 0; i < tramitesExistentes.size(); i++)
                {

                    if (tramitesExistentes.get(i).getHabilitado())
                    {
                        DtoTipoDeTramite dtoTipoDeTramite = tramitesExistentes.get(i).getDto();

                        dtoListaTiposDeTramites.add(dtoTipoDeTramite);
                    }
                }
            }
        } catch (PersistenceException e)
        {
            e.printStackTrace();
        }
        return dtoListaTiposDeTramites;
    }

    /**
     * Busca y devuelve las Plantillas de Tramite asociadas a un Tipo de Tramite
     * en particular.
     *
     * @param miDtoTipoDeTramite Un dto de tipo de un tipo de tramite.
     * @return miListaDtoPlantillas Una lista con los dto de las Plantillas de
     * Tramite asociadas al Tipo de Tramite, o null en caso de no existir.
     */
    public ArrayList<DtoPlantillaTramite> obtenerPlantillasTramite(DtoTipoDeTramite miDtoTipoDeTramite)
    {
        ArrayList<DtoPlantillaTramite> miListaDtoPlantillas = new ArrayList<>();

        try
        {
            PlantillaTramiteJpaController miPlantillaTramiteJpaController = (PlantillaTramiteJpaController) this.getMiAdministradorJpa().obtenerJpa(PlantillaTramiteJpaController.class.getName());

            List<PlantillaTramite> miListaPlantillas = miPlantillaTramiteJpaController.findPlantillasDeTramite(miDtoTipoDeTramite.getIdTipoTramite().intValue());

            if (!miListaPlantillas.isEmpty() && miListaPlantillas != null)
            {
                for (Iterator<PlantillaTramite> it = miListaPlantillas.iterator(); it.hasNext();)
                {
                    PlantillaTramite plantillaTramite = it.next();

                    miListaDtoPlantillas.add(plantillaTramite.getDto());
                }
            }
        } catch (NonexistentJpaException ex)
        {
            Logger.getLogger(ControllerNegocio.class.getName()).log(Level.SEVERE, null, ex);
        } catch (PersistenceException e)
        {
            e.printStackTrace();
        }
        return miListaDtoPlantillas;
    }

    /**
     * Modifica un Tipo de Tramite con los datos indicados.
     *
     * @param miDtoTipoDeTramite Un dto con los nuevos datos del Tipo de
     * Tramite.
     * @param listaDtoDocumentosAsociados Tipos de Documentos asociados al Tipo
     * de Tramite.
     * @param dtoPlantillasActuales La lista de documentos asociados a la
     * plantilla actual.
     * @return modificado Verdadero si fue modificado, Falso de lo contrario.
     */
    public Boolean modificarTipoDeTramite(DtoTipoDeTramite miDtoTipoDeTramite, ArrayList<DtoTipoDeDocumento> listaDtoDocumentosAsociados) throws ClassModifiedException, ClassEliminatedException
    {

        Boolean modificado = false;
        Boolean eliminada = false;
        TipoDeTramite tipoDeTramiteModificar = null;

        List<TipoDeTramite> tipoDeTramiteModificarList = miJpaTipoTramite.findTipoDeTramite(miDtoTipoDeTramite.getIdTipoTramite());

        tipoDeTramiteModificar = tipoDeTramiteModificarList.get(0);

        if (tipoDeTramiteModificar != null)
        {
            tipoDeTramiteModificar.setAtributos(miDtoTipoDeTramite);
            try
            {
                modificado = miJpaTipoTramite.edit(tipoDeTramiteModificar);

                if (modificado)
                {
                    this.registrarAuditoria(tipoDeTramiteModificar, ConstantesGui.MODIFICAR_TIPO_DE_TRAMITE);

                    List<PlantillaTramite> plantillasActuales = miJpaPlantillaTramite.findPlantillasDeTramite(tipoDeTramiteModificar.getIdTipoTramite());

                    if (plantillasActuales != null && !plantillasActuales.isEmpty())
                    {
                        for (int i = 0; i < plantillasActuales.size(); i++)
                        {
                            PlantillaTramite plantillaTramite = plantillasActuales.get(i);

                            eliminada = miJpaPlantillaTramite.eliminarPlantillaTramite(plantillaTramite);

                            this.registrarAuditoria(plantillaTramite, ConstantesGui.ELIMINAR_PLANTILLA_TRAMITE);
                        }
                    } else
                    {
                        eliminada = true;
                    }

                    if (eliminada)
                    {
                        for (int i = 0; i < listaDtoDocumentosAsociados.size(); i++)
                        {
                            DtoTipoDeDocumento dtoTipoDeDocumento = listaDtoDocumentosAsociados.get(i);

                            PlantillaTramite miPlantillaTramite = new PlantillaTramite();

                            TipoDeDocumento miTipoDeDocumento = new TipoDeDocumento();
                            miTipoDeDocumento.setAtributos(dtoTipoDeDocumento);

                            miPlantillaTramite.setTipoDeDocumento(miTipoDeDocumento);
                            miPlantillaTramite.setTipoDeTramite(tipoDeTramiteModificar);
                            try
                            {

                                Boolean creada = miJpaPlantillaTramite.create(miPlantillaTramite);

                                this.registrarAuditoria(miPlantillaTramite, ConstantesGui.MODIFICAR_PLANTILLA_TRAMITE);

                            } catch (PreexistingEntityException ex)
                            {
                                Logger.getLogger(ControllerNegocio.class.getName()).log(Level.SEVERE, null, ex);
                            } catch (Exception ex)
                            {
                                Logger.getLogger(ControllerNegocio.class.getName()).log(Level.SEVERE, null, ex);
                            }

                        }
                    }

                }
            } catch (IllegalOrphanException ex)
            {
                Logger.getLogger(ControllerNegocio.class.getName()).log(Level.SEVERE, null, ex);
            } catch (NonexistentEntityException ex)
            {
                Logger.getLogger(ControllerNegocio.class.getName()).log(Level.SEVERE, null, ex);
            }
        } else
        {
            throw new ClassEliminatedException();
        }
        return modificado;
    }

    /**
     * Permite eliminar un determinado tipo de tramite, junto con las plantillas
     * de tramites a las cuales esta asociado.
     *
     * @param miDtoTipoTramite Un dto que representa el tipo de tramite a
     * eliminar.
     * @param dtosPlantillas Las plantillas de tramites asociadas al tramite
     * indicado.
     * @return eliminado Verdadero si se pudo eliminar el tipo de tramite, false
     * en caso contrario.
     */
    public Boolean eliminarTipoDeTramite(DtoTipoDeTramite miDtoTipoTramite) throws ClassEliminatedException, ClassModifiedException
    {
        Boolean eliminado = false;

        List<TipoDeTramite> miTipoDeTramite = miJpaTipoTramite.findTipoDeTramite(miDtoTipoTramite.getIdTipoTramite());

        if (miTipoDeTramite != null)
        {
            try
            {
                TipoDeTramite miTramite = miTipoDeTramite.get(0);

                miTramite.setAtributos(miDtoTipoTramite);

                eliminado = miJpaTipoTramite.edit(miTramite);

                this.registrarAuditoria(miTramite, ConstantesGui.ELIMINAR_TIPO_DE_TRAMITE);


            } catch (IllegalOrphanException ex)
            {
                Logger.getLogger(ControllerNegocio.class.getName()).log(Level.SEVERE, null, ex);
            } catch (NonexistentEntityException ex)
            {
                Logger.getLogger(ControllerNegocio.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return eliminado;
    }
// </editor-fold>
    // <editor-fold defaultstate="collapsed" desc="Documentos">

    /**
     * Da de alta un nuevo Tipo de Documento.
     *
     * @param miDtoTipoDocumento Un dto que representa el tipo de documento a
     * dar de alta.
     * @return resultado Verdadero si se dio de alta, Falso en caso de que el
     * documento indicado ya exista o haya ocurrido algun error.
     */
    public Boolean darAltaDocumento(DtoTipoDeDocumento miDtoTipoDocumento) throws ClassEliminatedException, ClassModifiedException, IllegalOrphanException, NonexistentEntityException
    {
        Boolean resultado = Boolean.FALSE;

        Boolean existe = Boolean.FALSE;
        TipoDeDocumento miTipoDeDocumento = null;

        existe = this.existeDocumento(miDtoTipoDocumento.getNombre());
        miTipoDeDocumento = new TipoDeDocumento();

        if (!existe)
        {
            miTipoDeDocumento.setAtributos(miDtoTipoDocumento);
            int id = miJpaTipoDocumento.create(miTipoDeDocumento);

            if (id != -1)
            {
                this.registrarAuditoria(miTipoDeDocumento, ConstantesGui.INGRESAR_NUEVO_TIPO_DOCUMENTO);

                resultado = Boolean.TRUE;
            }
        } else
        {

            TipoDeDocumento tipoDeDocumento = (miJpaTipoDocumento.findTipoDeDocumento(miDtoTipoDocumento.getNombre())).get(0);
            tipoDeDocumento.setHabilitado(true);

            Boolean modificado = miJpaTipoDocumento.edit(tipoDeDocumento);

            this.registrarAuditoria(tipoDeDocumento, ConstantesGui.MODIFICAR_TIPO_DOCUMENTO);

            resultado = modificado;
        }

        return resultado;
    }

    /**
     * Verifica si ya existe un Tipo de Documento con el nombre indicado.
     *
     * @param nombreDocumento
     * @return True si existe, False de lo contrario.
     */
    private Boolean existeDocumento(String nombreDocumento)
    {
        Boolean resultado = Boolean.FALSE;

        List<TipoDeDocumento> miTipoDeDocumento = null;
        TipoDeDocumentoJpaController miTipoDeDocumentoJpaController;

        miTipoDeDocumento = miJpaTipoDocumento.findTipoDeDocumento(nombreDocumento);


        if (miTipoDeDocumento != null && !miTipoDeDocumento.isEmpty())
        {
            resultado = Boolean.TRUE;
        }

        return resultado;

    }

    /**
     * Busca y devuelve todo los Tipos de Documento disponibles.
     *
     * @return misDtoTiposDocumentos Una lista con los dto de los Tipos de
     * Documento encontrados.
     */
    public ArrayList<DtoTipoDeDocumento> buscarTiposDeDocumentoDisponibles()
    {
        List<TipoDeDocumento> miListaTiposDocumentos = null;
        ArrayList<DtoTipoDeDocumento> misDtoTiposDocumentos = null;

        try
        {
            miListaTiposDocumentos = miJpaTipoDocumento.findTipoDeDocumentoEntities();

            if (miListaTiposDocumentos != null)
            {
                misDtoTiposDocumentos = new ArrayList<>();

                for (Iterator<TipoDeDocumento> it = miListaTiposDocumentos.iterator(); it.hasNext();)
                {
                    TipoDeDocumento tipoDeDocumento = it.next();

                    if (tipoDeDocumento.getHabilitado())
                    {
                        DtoTipoDeDocumento unDto = new DtoTipoDeDocumento();

                        unDto = tipoDeDocumento.getDto();

                        misDtoTiposDocumentos.add(unDto);
                    }
                }

            }
        } catch (PersistenceException e)
        {
            e.printStackTrace();
        }

        return misDtoTiposDocumentos;
    }

    /**
     * Busca y devuelve un Tipo de Documento, por su nombre.
     *
     * @param nombre nombre del Tipo de Documento a buscar.
     * @return dtoMiTipoDeDocumento Un Dto del Tipo de Documento encontrado.
     */
    public DtoTipoDeDocumento buscarTipoDeDocumento(String nombre)
    {
        DtoTipoDeDocumento dtoMiTipoDeDocumento = null;

        List<TipoDeDocumento> miTipoDeDocumento = null;

        miTipoDeDocumento = miJpaTipoDocumento.findTipoDeDocumento(nombre);

        if (miTipoDeDocumento != null && !miTipoDeDocumento.isEmpty())
        {
            dtoMiTipoDeDocumento = miTipoDeDocumento.get(0).getDto();
        }

        return dtoMiTipoDeDocumento;
    }

    /**
     * Modifica los datos de un Tipo de Documento en particular.
     *
     * @param miDto El dto del Tipo de Documento a modificar, con los nuevos
     * datos.
     * @return modificado Verdadero si se pudo modificar el Tipo de Documento,
     * Falso de lo contrario.
     */
    public DtoFlag modificarTipoDeDocumento(DtoTipoDeDocumento miDto) throws ClassModifiedException, ClassEliminatedException, IllegalOrphanException, NonexistentEntityException
    {
        DtoFlag modificado = new DtoFlag();
        modificado.setFlag(false);

        TipoDeDocumento miTipoDeDocumento = miJpaTipoDocumento.findTipoDeDocumento(miDto.getIdTipoDocumento());

        if (miTipoDeDocumento != null)
        {
            miTipoDeDocumento.setAtributos(miDto);

            modificado.setFlag(miJpaTipoDocumento.edit(miTipoDeDocumento));

            this.registrarAuditoria(miTipoDeDocumento, ConstantesGui.MODIFICAR_TIPO_DOCUMENTO);

        }

        return modificado;
    }

    /**
     * Metodo que permite eliminar (deshabilitar) un tipo de documento.
     *
     * @param miDto El tipo de documento a eliminar.
     * @return modificar Verdadero si se pudo eliminar, falso en caso contrario.
     * @throws ClassModifiedException
     * @throws ClassEliminatedException
     * @throws IllegalOrphanException
     * @throws NonexistentEntityException
     */
    public DtoFlag eliminarTipoDeDocumento(DtoTipoDeDocumento miDto) throws ClassModifiedException, ClassEliminatedException, IllegalOrphanException, NonexistentEntityException
    {
        DtoFlag modificado = new DtoFlag();
        modificado.setFlag(false);

        TipoDeDocumento miTipoDeDocumento = miJpaTipoDocumento.findTipoDeDocumento(miDto.getIdTipoDocumento());

        if (miTipoDeDocumento != null)
        {
            miTipoDeDocumento.setAtributos(miDto);

            modificado.setFlag(miJpaTipoDocumento.edit(miTipoDeDocumento));

            this.registrarAuditoria(miTipoDeDocumento, ConstantesGui.ELIMINAR_TIPO_DOCUMENTO);

        }

        return modificado;
    }

    // </editor-fold>
    // <editor-fold defaultstate="collapsed" desc="Conceptos">
    /**
     * Permite dar de alta un nuevo concepto. Primero verifica que el nuevo
     * concepto no existe en la base de datos.
     *
     * @param miDto Un dto que representa el nuevo concepto a dar de alta.
     * @return resultado Verdadero si pudo persistir los cambios, falso en caso
     * contrario.
     */
    public Boolean darAltaConcepto(DtoConcepto miDto)
    {
        Boolean resultado = Boolean.FALSE;
        try
        {
            Concepto nuevoConcepto = new Concepto();
            nuevoConcepto.setAtributos(miDto);
            try
            {
                miJpaConcepto.create(nuevoConcepto);

                resultado = Boolean.TRUE;

                this.registrarAuditoria(nuevoConcepto, ConstantesGui.INGRESAR_NUEVO_CONCEPTO);


            } catch (PersistenceException e)
            {
                Logger.getLogger(ControllerNegocio.class.getName()).log(Level.SEVERE, null, e);
            }


        } catch (DtoInvalidoException ex)
        {
            Logger.getLogger(ControllerNegocio.class.getName()).log(Level.SEVERE, null, ex);
        }
        return resultado;
    }

    /**
     * Metodo que permite obtener una lista de todos los tipos de conceptos
     * registrados.
     *
     * @return dtoListaConceptos Una lista de dto con todos los conceptos
     * encontrados.
     */
    public List<DtoConcepto> obtenerListaConceptosDisponibles()
    {
        List<Concepto> miListaConceptos = null;
        List<DtoConcepto> dtoListaConceptos = null;

        try
        {

            miListaConceptos = miJpaConcepto.findConceptoEntities();

            if (miListaConceptos != null)
            {
                dtoListaConceptos = new ArrayList<>();

                for (Iterator<Concepto> it = miListaConceptos.iterator(); it.hasNext();)
                {
                    Concepto unConcepto = it.next();

                    if (unConcepto.getHabilitado())
                    {

                        DtoConcepto unDto = unConcepto.getDto();
                        dtoListaConceptos.add(unDto);
                    }
                }

            }
        } catch (PersistenceException e)
        {
            e.printStackTrace();
        }

        return dtoListaConceptos;
    }

    /**
     * Metodo que permite modificar un tipo de concepto.
     *
     * @param conceptoParaModificar Un dto que representa el Concepto a
     * modificar.
     * @return resultado Verdadero si pudo persistir los cambios, falso en caso
     * contrario.
     * @throws ClassModifiedException
     * @throws ClassEliminatedException
     * @throws NonexistentEntityException
     */
    public Boolean modificarConcepto(DtoConcepto conceptoParaModificar) throws ClassModifiedException, ClassEliminatedException, NonexistentEntityException, IllegalOrphanException, PreexistingEntityException
    {
        Boolean resultado = Boolean.FALSE;

        try
        {

            Concepto nuevoConcepto = miJpaConcepto.findConcepto(conceptoParaModificar.getIdConcepto());

            if (nuevoConcepto != null)
            {
//                if (!this.existeConcepto(conceptoParaModificar.getNombre()))
//                {
                nuevoConcepto.setAtributos(conceptoParaModificar);

                resultado = miJpaConcepto.edit(nuevoConcepto);

                if (resultado == true)
                {
                    this.registrarAuditoria(nuevoConcepto, ConstantesGui.MODIFICAR_CONCEPTO);


                }
//                }                
//                else
//                {
//                    throw new PreexistingEntityException("El concepto indica ya existe");
//                }

            }

        } catch (DtoInvalidoException ex)
        {
            //  TODO: Eliminar de la version Final
            Logger.getLogger(ControllerNegocio.class.getName()).log(Level.SEVERE, null, ex);
        } catch (PersistenceException e)
        {
            //  TODO: Eliminar de la version Final
            Logger.getLogger(ControllerNegocio.class.getName()).log(Level.SEVERE, null, e);
        }

        return resultado;
    }

    /**
     * Metodo que permite eliminar un determinado concepto.
     *
     * @param conceptoParaEliminar Un dto que represeta el concepto a eliminar.
     * @return resultado Verdadero si pudo persistir los cambios, falso en caso
     * contrario.
     */
    public Boolean eliminarConcepto(DtoConcepto conceptoParaEliminar) throws ClassEliminatedException, ClassModifiedException, IllegalOrphanException, NonexistentEntityException
    {
        Boolean resultado = Boolean.TRUE;
        try
        {
            Concepto miConcepto = new Concepto();

            miConcepto.setAtributos(conceptoParaEliminar);

            try
            {
                resultado = miJpaConcepto.edit(miConcepto);

                if (resultado == true)
                {
                    this.registrarAuditoria(miConcepto, ConstantesGui.ELIMINAR_CONCEPTO);
                }
            } catch (PersistenceException e)
            {
                e.printStackTrace();


            }

        } catch (DtoInvalidoException ex)
        {
            Logger.getLogger(ControllerNegocio.class.getName()).log(Level.SEVERE, null, ex);
        }
        return resultado;
    }

    /**
     * Metodo que permite determinar si el nombre de un concepto ya se encuentra
     * rgistrado.
     *
     * @param nombreConcepto El nombre del concepto.
     * @return resultado Verdadero si el concepto ya se encuentra registrado,
     * falso en caso contrario.
     */
    public boolean existeConcepto(String nombreConcepto)
    {
        boolean resultado = false;

        List<Concepto> listaConcepto = miJpaConcepto.findConceptoByNombre(nombreConcepto);

        if (!listaConcepto.isEmpty())
        {
            resultado = true;
        }

        return resultado;
    }

    // </editor-fold>
    // <editor-fold defaultstate="collapsed" desc="Estados de Gestion">
    /**
     * Metodo que permite dar de alta un nuevo estado de gestien, los cuales
     * representan estados del proceso de una gestien en un momento dado.
     *
     * @param miDto Un Dto que representa el nuevo estado de gestien.
     * @return Verdadero si pudo persistir los cambios, falso en caso contrario.
     * @throws PreexistingEntityException Excepcion lanzada si ya existe
     * registrado el estado indicado.
     */
    public Boolean darAltaEstadoDeGestion(DtoEstadoDeGestion miDto) throws PreexistingEntityException
    {
        Boolean resultado = Boolean.FALSE;

        EstadoDeGestion nuevoEstadoDeGestion = new EstadoDeGestion();
        try
        {
            nuevoEstadoDeGestion.setAtributo(miDto);


            try
            {
                miJpaEstadoDeGestion = (EstadoDeGestionJpaController) miAdministradorJpa.obtenerJpa(EstadoDeGestionJpaController.class.getName());

                miJpaEstadoDeGestion.create(nuevoEstadoDeGestion);
                resultado = Boolean.TRUE;

                this.registrarAuditoria(nuevoEstadoDeGestion, ConstantesGui.INGRESAR_ESTADO_GESTION);
            } catch (NonexistentJpaException ex)
            {
                Logger.getLogger(ControllerNegocio.class.getName()).log(Level.SEVERE, null, ex);
            } catch (PersistenceException e)
            {
                e.printStackTrace();


            }
        } catch (DtoInvalidoException ex)
        {
            Logger.getLogger(ControllerNegocio.class.getName()).log(Level.SEVERE, null, ex);
        }

        return resultado;
    }

    /**
     * Metodo que permite obtener una lista de todos los estados de gestion
     * registrados.
     *
     * @return Una lista tipo DtoEstadoDeGestion, o una lista vacia en caso de
     * no existir estados de gestion registrados.
     */
    public List<DtoEstadoDeGestion> obtenerListaEstadosDeGestionDisponibles()
    {
        List<DtoEstadoDeGestion> milistaDtoEstadoDeGestions = new ArrayList<>();
        List<EstadoDeGestion> listaEstadosDeGestion = new ArrayList<>();




        try
        {
            miJpaEstadoDeGestion = (EstadoDeGestionJpaController) miAdministradorJpa.obtenerJpa(EstadoDeGestionJpaController.class.getName());

            listaEstadosDeGestion = miJpaEstadoDeGestion.findEstadoDeGestionEntities();

            if (!listaEstadosDeGestion.isEmpty())
            {
                for (Iterator<EstadoDeGestion> it = listaEstadosDeGestion.iterator(); it.hasNext();)
                {
                    EstadoDeGestion estadoDeGestion = it.next();

                    DtoEstadoDeGestion unDtoEstadoDeGestio = estadoDeGestion.getDto();

                    milistaDtoEstadoDeGestions.add(unDtoEstadoDeGestio);
                }
            }
        } catch (NonexistentJpaException ex)
        {
            Logger.getLogger(ControllerNegocio.class.getName()).log(Level.SEVERE, null, ex);
        } catch (PersistenceException e)
        {
            e.printStackTrace();
        }

        return milistaDtoEstadoDeGestions;
    }

    /**
     * Metodo que permite modificar algunos de los atributos de un estado de
     * gestion: el nombre o las observaciones.
     *
     * @param dtoEstadoDeGestion Un DtoEstadoDeGestion
     * @return Verdadero si pudo persistir los cambios, falso en caso contrario.
     * @throws ClassEliminatedException Indica que el objeto que se quiere
     * modificar, ya ha sido eliminado.
     * @throws ClassModifiedException Indica que el objeto que se quiere
     * modificar, acaba de ser modificado por otro proceso.
     */
    public Boolean modificarEstadoDeGestion(DtoEstadoDeGestion dtoEstadoDeGestion) throws ClassEliminatedException, ClassModifiedException
    {
        Boolean resultado = Boolean.FALSE;
        try
        {
            EstadoDeGestion unEstadoDeGestion = miJpaEstadoDeGestion.findEstadoDeGestion(dtoEstadoDeGestion.getIdEstadoGestion());
            if (unEstadoDeGestion != null)
            {
                try
                {
                    unEstadoDeGestion.setAtributo(dtoEstadoDeGestion);
                    try
                    {
                        miJpaEstadoDeGestion.edit(unEstadoDeGestion);


                    } catch (IllegalOrphanException ex)
                    {
                        //  TODO: Eliminar de la version Final
                        Logger.getLogger(ControllerNegocio.class.getName()).log(Level.SEVERE, null, ex);
                    } catch (NonexistentEntityException ex)
                    {
                        //  TODO: Eliminar de la version Final
                        Logger.getLogger(ControllerNegocio.class.getName()).log(Level.SEVERE, null, ex);
                    }

                    resultado = Boolean.TRUE;

                    this.registrarAuditoria(unEstadoDeGestion, ConstantesGui.MODIFICAR_ESTADO_GESTION);


                } catch (DtoInvalidoException ex)
                {
                    //  TODO: Eliminar de la version Final
                    Logger.getLogger(ControllerNegocio.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        } catch (PersistenceException e)
        {
            //  TODO: Eliminar de la version Final
            e.printStackTrace();
        }


        return resultado;
    }

    // </editor-fold>
    // <editor-fold defaultstate="collapsed" desc="Folios">
    /**
     * Metodo que permite dar de alta un nuevo tipo de folio.
     *
     * @param nuevoDtoTipoDeFolio
     * @return Verdadero si pudo persistir los cambios, falso en caso contrario.
     */
    public Boolean darDeAltaTipoDeFolio(DtoTipoDeFolio nuevoDtoTipoDeFolio) throws ClassModifiedException
    {
        Boolean resultado = Boolean.FALSE;

        TipoDeFolio nuevoTipoDeFolio = new TipoDeFolio();

        try
        {
            nuevoDtoTipoDeFolio.setHabilitado(ConstantesNegocio.TIPO_DE_FOLIO_HABILITADO);
            nuevoTipoDeFolio.setAtributos(nuevoDtoTipoDeFolio);
            try
            {
                miJpaTipoDeFolio.create(nuevoTipoDeFolio);

                resultado = Boolean.TRUE;

                this.registrarAuditoria(nuevoTipoDeFolio, ConstantesGui.INGRESAR_NUEVO_TIPO_FOLIO);
            } catch (PreexistingEntityException ex)
            {
                try
                {
                    nuevoDtoTipoDeFolio = this.buscarTipoDeFolio(nuevoDtoTipoDeFolio);

                    nuevoTipoDeFolio.setAtributos(nuevoDtoTipoDeFolio);
                    nuevoTipoDeFolio.setHabilitado(ConstantesNegocio.TIPO_DE_FOLIO_HABILITADO);

                    miJpaTipoDeFolio.edit(nuevoTipoDeFolio);

                    this.registrarAuditoria(nuevoTipoDeFolio, ConstantesGui.MODIFICAR_TIPO_FOLIO);
                    resultado = Boolean.TRUE;


                } catch (IllegalOrphanException | NonexistentEntityException | ClassEliminatedException ex1)
                {
                    Logger.getLogger(ControllerNegocio.class.getName()).log(Level.SEVERE, null, ex1);
                }
            }

        } catch (DtoInvalidoException e)
        {
            e.printStackTrace();
        }


        return resultado;
    }

    /**
     * Metodo que permite obtener la lista de todos los folios registrados.
     *
     * @return Una lista tipo DtoTipoDeFolio.
     */
    public List<DtoTipoDeFolio> obtenerListaTiposDeFoliosDisponibles()
    {
        List<TipoDeFolio> listaTiposDeFolios = null;
        List<DtoTipoDeFolio> miListaDtoFolios = null;

        listaTiposDeFolios = miJpaTipoDeFolio.findTipoDeFolioEntities();
        if (listaTiposDeFolios != null)
        {
            miListaDtoFolios = new ArrayList<>();

            for (Iterator<TipoDeFolio> it = listaTiposDeFolios.iterator(); it.hasNext();)
            {
                TipoDeFolio miTipoDeFolio = it.next();
                if (miTipoDeFolio.getHabilitado() == ConstantesNegocio.TIPO_DE_FOLIO_HABILITADO)
                {
                    DtoTipoDeFolio unDto = miTipoDeFolio.getDto();

                    miListaDtoFolios.add(unDto);
                }
            }
        }


        return miListaDtoFolios;
    }

    /**
     * Metodo que permite modificar algunas de las propiedades de un tipo de
     * folio determinado.
     *
     * @param dtoTipoDeFolioModificar
     * @return Verdadero si pudo persistir los cambios, falso en caso contrario.
     */
    public Boolean modificarTipoDeFolios(DtoTipoDeFolio dtoTipoDeFolioModificar) throws ClassModifiedException
    {
        Boolean resultado = Boolean.FALSE;
        try
        {
            TipoDeFolio miTipoDeFolio = null;

            miTipoDeFolio = miJpaTipoDeFolio.findTipoDeFolio(dtoTipoDeFolioModificar.getIdTipoFolio());

            miTipoDeFolio.setAtributos(dtoTipoDeFolioModificar);

            miJpaTipoDeFolio.edit(miTipoDeFolio);

            resultado = Boolean.TRUE;

            this.registrarAuditoria(miTipoDeFolio, ConstantesGui.MODIFICAR_TIPO_FOLIO);


        } catch (IllegalOrphanException | NonexistentEntityException | ClassEliminatedException | DtoInvalidoException ex)
        {
            Logger.getLogger(ControllerNegocio.class.getName()).log(Level.SEVERE, null, ex);
        }
        return resultado;
    }

    /**
     * Metodo que permite eliminar un tipo de folio determinado. El tipo de
     * folio no es eliminado de la persistencia, en lugar de eso, se cambia el
     * valor del campo "habilidado" a falso.
     *
     * @param dtoTipoDeFolioEliminar
     * @return Verdadero si pudo persistir los cambios, falso en caso contrario.
     */
    public Boolean eliminarTipoDeFolio(DtoTipoDeFolio dtoTipoDeFolioEliminar) throws ClassModifiedException
    {
        Boolean resultado = Boolean.FALSE;
        TipoDeFolio miTipoDeFolio = null;

        miTipoDeFolio = miJpaTipoDeFolio.findTipoDeFolio(dtoTipoDeFolioEliminar.getIdTipoFolio());
        if (miTipoDeFolio != null)
        {
            try
            {
                miTipoDeFolio.setHabilitado(ConstantesNegocio.TIPO_DE_FOLIO_DESHABILITADO);

                miJpaTipoDeFolio.edit(miTipoDeFolio);

                resultado = Boolean.TRUE;

                this.registrarAuditoria(miTipoDeFolio, ConstantesGui.ELIMINAR_TIPO_FOLIO);


            } catch (IllegalOrphanException | NonexistentEntityException | ClassEliminatedException ex)
            {
                Logger.getLogger(ControllerNegocio.class.getName()).log(Level.SEVERE, null, ex);
            }

        }
        return resultado;
    }

    // </editor-fold>
    // <editor-fold defaultstate="collapsed" desc="Plantillas de Presupuesto">
    /**
     * Comprueba si un Tipo de tramite tiene Plantillas de Presupuesto
     * asociadas.
     *
     * @param dtoTipoDeTramite, datos del Tipo de Tramite en cuestion.
     * @return True si tiene plantillas de presupuesto asociadas, False de lo
     * contrario.
     */
    public Boolean existePlantillaPresupuesto(DtoTipoDeTramite dtoTipoDeTramite)
    {
        Boolean existe = Boolean.FALSE;
        ArrayList<DtoPlantillaPresupuesto> plantillas = null;

        plantillas = this.obtenerPlantillasPresupuesto(dtoTipoDeTramite);

        if (plantillas != null && !plantillas.isEmpty())
        {
            existe = Boolean.TRUE;
        }

        return existe;
    }

    /**
     * Crea una nueva Plantilla de Presupuesto con el Tipo de Tramite y
     * Conceptos indicados.
     *
     * @param miDtoTipoDeTramite, datos del Tipo de Tramite asociado.
     * @param misDtoConceptos, datos de los Conceptos asociados.
     * @return True si se cree, False de lo contrario.
     */
    public Boolean crearPlantillaPresupuesto(DtoTipoDeTramite miDtoTipoDeTramite, ArrayList<DtoConcepto> misDtoConceptos) throws PreexistingEntityException
    {
        Boolean creada = false;
        PlantillaPresupuesto miPlantillaPresupuesto = new PlantillaPresupuesto();

        List<TipoDeTramite> miTipoDeTramite = miJpaTipoTramite.findTipoDeTramite(miDtoTipoDeTramite.getIdTipoTramite());

        Concepto miConcepto;
        Boolean plantillaCreada = false;

        for (int i = 0; i < misDtoConceptos.size(); i++)
        {

            DtoConcepto dtoConcepto = misDtoConceptos.get(i);

            miConcepto = miJpaConcepto.findConcepto(dtoConcepto.getIdConcepto());

            miPlantillaPresupuesto.setConcepto(miConcepto);
            miPlantillaPresupuesto.setTipoDeTramite(miTipoDeTramite.get(0));

            plantillaCreada = miJpaPlantillaPresupuesto.create(miPlantillaPresupuesto);

        }

        if (plantillaCreada)
        {
            creada = true;

            this.registrarAuditoria(miPlantillaPresupuesto, ConstantesGui.CREAR_PLANTILLA_DE_PRESUPUESTO);
        }

        return creada;
    }

    /**
     * Busca y devuelve una lista de las Plantillas de Presupuesto asociadas al
     * Tipo de Tramite indicado.
     *
     * @param miDtoTipoDeTramite, datos del Tipo de Tramite asociado a las
     * plantillas.
     * @return Una lista de DtoPlantillaPresupuesto, con los datos de las
     * plantillas encontradas.
     */
    public ArrayList<DtoPlantillaPresupuesto> obtenerPlantillasPresupuesto(DtoTipoDeTramite miDtoTipoDeTramite)
    {
        ArrayList<DtoPlantillaPresupuesto> miListaDtoPlantillas = new ArrayList<>();

        List<PlantillaPresupuesto> miListaPlantillas = miJpaPlantillaPresupuesto.findPlantillasDePresupuesto(miDtoTipoDeTramite.getIdTipoTramite().intValue());

        if (!miListaPlantillas.isEmpty() && miListaPlantillas != null)
        {
            for (Iterator<PlantillaPresupuesto> it = miListaPlantillas.iterator(); it.hasNext();)
            {
                PlantillaPresupuesto plantillaPresupuesto = it.next();

                miListaDtoPlantillas.add(plantillaPresupuesto.getDto());
            }
        }

        return miListaDtoPlantillas;
    }

    /**
     * Modifica los datos de las Plantillas de Presupuesto de un Tipo de Tramite
     * en particular.
     *
     * @param miDtoTipoDeTramite, datos del Tipo de Tramite asociado a las
     * Plantillas a modificar.
     * @param misDtoConceptos, datos de los Conceptos a asociar al Tipo de
     * Tramite.
     * @param dtoPlantillasActuales, Plantillas de Presupuestos asociadas al
     * Tipo de Tramite.
     * @return True si se pudo realizar la modificacion, False de lo contrario.
     */
    public Boolean modificarPlantillaPresupuesto(DtoTipoDeTramite miDtoTipoDeTramite, ArrayList<DtoConcepto> misDtoConceptos) throws ClassEliminatedException, PreexistingEntityException, ClassModifiedException
    {
        Boolean modificado = false;
        Boolean modificada = false;
        Boolean eliminada = false;
        TipoDeTramite tipoDeTramiteModificar = null;

        List<TipoDeTramite> tipoDeTramiteModificarList = miJpaTipoTramite.findTipoDeTramite(miDtoTipoDeTramite.getIdTipoTramite());

        tipoDeTramiteModificar = tipoDeTramiteModificarList.get(0);

        if (tipoDeTramiteModificar != null)
        {
            try
            {
                tipoDeTramiteModificar.setAtributos(miDtoTipoDeTramite);
                modificado = miJpaTipoTramite.edit(tipoDeTramiteModificar);

                if (modificado)
                {
                    this.registrarAuditoria(tipoDeTramiteModificar, ConstantesGui.MODIFICAR_TIPO_DE_TRAMITE);

                    List<PlantillaPresupuesto> plantillasActuales = miJpaPlantillaPresupuesto.findPlantillasDePresupuesto(tipoDeTramiteModificar.getIdTipoTramite());

                    if (plantillasActuales != null && !plantillasActuales.isEmpty())
                    {
                        for (int i = 0; i < plantillasActuales.size(); i++)
                        {
                            PlantillaPresupuesto plantillaPresupuesto = plantillasActuales.get(i);

                            eliminada = miJpaPlantillaPresupuesto.eliminarPlantillaPresupuesto(plantillaPresupuesto);

                            this.registrarAuditoria(plantillaPresupuesto, ConstantesGui.ELIMINAR_PLANTILLA_DE_PRESUPUESTO);
                        }
                    } else
                    {
                        eliminada = true;
                    }

                    if (eliminada)
                    {
                        if (misDtoConceptos != null && !misDtoConceptos.isEmpty())
                        {
                            PlantillaPresupuesto miPlantilla = null;
                            Boolean creada = false;

                            for (Iterator<DtoConcepto> it = misDtoConceptos.iterator(); it.hasNext();)
                            {
                                DtoConcepto dtoConcepto = it.next();

                                Concepto miConcepto = miJpaConcepto.findConcepto(dtoConcepto.getIdConcepto());

                                if (miConcepto != null)
                                {

                                    miPlantilla = new PlantillaPresupuesto();

                                    miPlantilla.setConcepto(miConcepto);
                                    miPlantilla.setTipoDeTramite(tipoDeTramiteModificar);

                                    creada = miJpaPlantillaPresupuesto.create(miPlantilla);
                                }
                            }

                            if (creada)
                            {
                                modificada = true;

                                this.registrarAuditoria(miPlantilla, ConstantesGui.MODIFICAR_PLANTILLA_DE_PRESUPUESTO);
                            }
                        }
                    }
                } else
                {
                    throw new ClassEliminatedException();
                }
            } catch (IllegalOrphanException ex)
            {
                Logger.getLogger(ControllerNegocio.class.getName()).log(Level.SEVERE, null, ex);
            } catch (NonexistentEntityException ex)
            {
                Logger.getLogger(ControllerNegocio.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        return modificada;
    }

    /**
     * Elimina las Plantillas de Presupuesto indicadas.
     *
     * @param dtosPlantillasPresupuesto, lista con los datos de las plantillas
     * de presupuesto a eliminar.
     * @return True si se eliminaron las plantillas de presupuesto, False de lo
     * contrario.
     */
    public Boolean eliminarPlantillaPresupuesto(ArrayList<DtoPlantillaPresupuesto> dtosPlantillasPresupuesto) throws ClassEliminatedException
    {
        Boolean eliminada = false;

        // Elimino todas las plantillas asociadas al Tipo de Tramite:
        if (dtosPlantillasPresupuesto != null && !dtosPlantillasPresupuesto.isEmpty())
        {
            Boolean eliminado = false;
            for (int i = 0; i < dtosPlantillasPresupuesto.size(); i++)
            {

                PlantillaPresupuesto plantillaPresupuesto = new PlantillaPresupuesto();
                plantillaPresupuesto.setAtributos(dtosPlantillasPresupuesto.get(i));

                eliminado = miJpaPlantillaPresupuesto.eliminarPlantillaPresupuesto(plantillaPresupuesto);

                if (eliminado)
                {
                    this.registrarAuditoria(plantillaPresupuesto, ConstantesGui.ELIMINAR_PLANTILLA_DE_PRESUPUESTO);
                }
            }

            if (eliminado)
            {
                eliminada = true;
            }
        }
        return eliminada;
    }
    // </editor-fold>
// </editor-fold>
}
