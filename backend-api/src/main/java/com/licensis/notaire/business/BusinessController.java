/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.licensis.notaire.business;

import com.licensis.notaire.dto.DtoConcept;
import com.licensis.notaire.dto.DtoCopy;
import com.licensis.notaire.dto.DtoSubmittedDocument;
import com.licensis.notaire.dto.DtoDeed;
import com.licensis.notaire.dto.DtoManagementStatus;
import com.licensis.notaire.dto.DtoFlag;
import com.licensis.notaire.dto.DtoFolio;
import com.licensis.notaire.dto.DtoDeedManagement;
import com.licensis.notaire.dto.DtoHistory;
import com.licensis.notaire.dto.DtoProperty;
import com.licensis.notaire.dto.DtoItem;
import com.licensis.notaire.dto.DtoTestimonyMovement;
import com.licensis.notaire.dto.DtoPayment;
import com.licensis.notaire.dto.DtoPerson;
import com.licensis.notaire.dto.DtoBudgetTemplate;
import com.licensis.notaire.dto.DtoProcedureTemplate;
import com.licensis.notaire.dto.DtoBudget;
import com.licensis.notaire.dto.DtoAuditRecord;
import com.licensis.notaire.dto.DtoSubstitution;
import com.licensis.notaire.dto.DtoTestimony;
import com.licensis.notaire.dto.DtoDocumentType;
import com.licensis.notaire.dto.DtoFolioType;
import com.licensis.notaire.dto.DtoProcedureType;
import com.licensis.notaire.dto.DtoIdentificationType;
import com.licensis.notaire.dto.DtoProcedure;
import com.licensis.notaire.dto.DtoUser;
import com.licensis.notaire.dto.exceptions.DtoInvalidoException;
import com.licensis.notaire.audit.ConstantesGui;
import com.licensis.notaire.jpa.ConceptJpaController;
import com.licensis.notaire.jpa.ConstantesPersistencia;
import com.licensis.notaire.jpa.CopyJpaController;
import com.licensis.notaire.jpa.SubmittedDocumentJpaController;
import com.licensis.notaire.jpa.DeedJpaController;
import com.licensis.notaire.jpa.ManagementStatusJpaController;
import com.licensis.notaire.jpa.FolioJpaController;
import com.licensis.notaire.jpa.DeedManagementJpaController;
import com.licensis.notaire.jpa.HistoryJpaController;
import com.licensis.notaire.jpa.PropertyJpaController;
import com.licensis.notaire.jpa.ItemJpaController;
import com.licensis.notaire.jpa.TestimonyMovementJpaController;
import com.licensis.notaire.jpa.PaymentJpaController;
import com.licensis.notaire.jpa.PersonJpaController;
import com.licensis.notaire.jpa.BudgetTemplateJpaController;
import com.licensis.notaire.jpa.ProcedureTemplateJpaController;
import com.licensis.notaire.jpa.BudgetJpaController;
import com.licensis.notaire.jpa.AuditRecordJpaController;
import com.licensis.notaire.jpa.SubstitutionJpaController;
import com.licensis.notaire.jpa.TestimonyJpaController;
import com.licensis.notaire.jpa.DocumentTypeJpaController;
import com.licensis.notaire.jpa.FolioTypeJpaController;
import com.licensis.notaire.jpa.ProcedureTypeJpaController;
import com.licensis.notaire.jpa.IdentificationTypeJpaController;
import com.licensis.notaire.jpa.ProcedureJpaController;
import com.licensis.notaire.jpa.PersonProcedureJpaController;
import com.licensis.notaire.jpa.UserJpaController;
import com.licensis.notaire.jpa.exceptions.ClassEliminatedException;
import com.licensis.notaire.jpa.exceptions.ClassModifiedException;
import com.licensis.notaire.jpa.exceptions.CreateEntityException;
import com.licensis.notaire.jpa.exceptions.IllegalOrphanException;
import com.licensis.notaire.jpa.exceptions.NonexistentEntityException;
import com.licensis.notaire.jpa.exceptions.NonexistentJpaException;
import com.licensis.notaire.jpa.exceptions.PreexistingEntityException;
import com.licensis.notaire.service.AdministradorJpa;
import com.licensis.notaire.service.AdministradorSession;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import jakarta.persistence.PersistenceException;

/**
 * Clase controller de la capa de negocio. Administra toda la logica de la capa
 * de negocio.
 *
 *
 * @author User
 */
public class BusinessController
{

// <editor-fold defaultstate="collapsed" desc="ATRIBUTOS">
    private static BusinessController instancia = null;
    private AuditRecordJpaController miJpaAudit;
    private DocumentTypeJpaController myJpaDocumentType;
    private ProcedureTypeJpaController myJpaProcedureType;
    private ProcedureTemplateJpaController myJpaProcedureTemplate;
    private BudgetTemplateJpaController myJpaBudgetTemplate;
    private ConceptJpaController miJpaConcept;
    private ManagementStatusJpaController miJpaManagementStatus;
    private FolioTypeJpaController myJpaFolioType;
    private PersonJpaController myJpaPerson;
    private SubstitutionJpaController myJpaSubstitution;
    private AdministradorJpa miAdministradorJpa = null;
    private PropertyJpaController myJpaProperty;
    private ProcedureJpaController myJpaProcedure;
    private PersonProcedureJpaController myJpaPersonProcedure;
    private ItemJpaController myJpaItem;
    private BudgetJpaController myJpaBudget;
    private PaymentJpaController myJpaPayment;
    private DeedManagementJpaController miJpaDeedManagement;
    private FolioJpaController myJpaFolio;
    private DeedJpaController myJpaDeed;
    private HistoryJpaController myJpaHistory;
    private CopyJpaController myJpaCopy;
    private TestimonyJpaController myJpaTestimony;
    private TestimonyMovementJpaController myJpaTestimonyMovement;
    private SubmittedDocumentJpaController myJpaSubmittedDocument;
    private UserJpaController myJpaUser;
    private IdentificationTypeJpaController myJpaIdentificationType;
    private DtoUser userSession;

// </editor-fold>
// <editor-fold defaultstate="collapsed" desc="CONSTRUCTOR">
    private BusinessController()
    {
        try
        {
            miAdministradorJpa = AdministradorJpa.getInstancia();
            miJpaAudit = (AuditRecordJpaController) AdministradorJpa.getInstancia().obtenerJpa(AuditRecordJpaController.class.getName());
            myJpaProperty = (PropertyJpaController) AdministradorJpa.getInstancia().obtenerJpa(PropertyJpaController.class.getName());
            myJpaProcedure = (ProcedureJpaController) AdministradorJpa.getInstancia().obtenerJpa(ProcedureJpaController.class.getName());
            myJpaPersonProcedure = (PersonProcedureJpaController) AdministradorJpa.getInstancia().obtenerJpa(PersonProcedureJpaController.class.getName());
            myJpaItem = (ItemJpaController) AdministradorJpa.getInstancia().obtenerJpa(ItemJpaController.class.getName());
            myJpaBudget = (BudgetJpaController) AdministradorJpa.getInstancia().obtenerJpa(BudgetJpaController.class.getName());
            myJpaPerson = (PersonJpaController) AdministradorJpa.getInstancia().obtenerJpa(PersonJpaController.class.getName());
            myJpaBudgetTemplate = (BudgetTemplateJpaController) AdministradorJpa.getInstancia().obtenerJpa(BudgetTemplateJpaController.class.getName());
            myJpaDocumentType = (DocumentTypeJpaController) AdministradorJpa.getInstancia().obtenerJpa(DocumentTypeJpaController.class.getName());
            myJpaProcedureType = (ProcedureTypeJpaController) AdministradorJpa.getInstancia().obtenerJpa(ProcedureTypeJpaController.class.getName());
            myJpaProcedureTemplate = (ProcedureTemplateJpaController) AdministradorJpa.getInstancia().obtenerJpa(ProcedureTemplateJpaController.class.getName());
            miJpaConcept = (ConceptJpaController) AdministradorJpa.getInstancia().obtenerJpa(ConceptJpaController.class.getName());
            miJpaManagementStatus = (ManagementStatusJpaController) AdministradorJpa.getInstancia().obtenerJpa(ManagementStatusJpaController.class.getName());
            myJpaFolioType = (FolioTypeJpaController) AdministradorJpa.getInstancia().obtenerJpa(FolioTypeJpaController.class.getName());
            myJpaSubstitution = (SubstitutionJpaController) AdministradorJpa.getInstancia().obtenerJpa(SubstitutionJpaController.class.getName());
            myJpaPayment = (PaymentJpaController) AdministradorJpa.getInstancia().obtenerJpa(PaymentJpaController.class.getName());
            myJpaFolio = (FolioJpaController) AdministradorJpa.getInstancia().obtenerJpa(FolioJpaController.class.getName());
            miJpaDeedManagement = (DeedManagementJpaController) AdministradorJpa.getInstancia().obtenerJpa(DeedManagementJpaController.class.getName());
            myJpaDeed = (DeedJpaController) AdministradorJpa.getInstancia().obtenerJpa(DeedJpaController.class.getName());
            myJpaHistory = (HistoryJpaController) AdministradorJpa.getInstancia().obtenerJpa(HistoryJpaController.class.getName());
            myJpaCopy = (CopyJpaController) AdministradorJpa.getInstancia().obtenerJpa(CopyJpaController.class.getName());
            myJpaTestimony = (TestimonyJpaController) AdministradorJpa.getInstancia().obtenerJpa(TestimonyJpaController.class.getName());
            myJpaTestimonyMovement = (TestimonyMovementJpaController) AdministradorJpa.getInstancia().obtenerJpa(TestimonyMovementJpaController.class.getName());
            myJpaSubmittedDocument = (SubmittedDocumentJpaController) AdministradorJpa.getInstancia().obtenerJpa(SubmittedDocumentJpaController.class.getName());
            myJpaUser = (UserJpaController) AdministradorJpa.getInstancia().obtenerJpa(UserJpaController.class.getName());
            myJpaIdentificationType = (IdentificationTypeJpaController) AdministradorJpa.getInstancia().obtenerJpa(IdentificationTypeJpaController.class.getName());
        }
        catch (NonexistentJpaException ex)
        {
            ex.printStackTrace();
        }
    }

    public static BusinessController getInstancia()
    {
        if (instancia == null)
        {
            instancia = new BusinessController();
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
    public DtoPerson darAltaPerson(DtoPerson dtoPerson) throws NonexistentJpaException
    {

        //Set atributos persona
        Person miPerson = new Person();
        miPerson.setAtributos(dtoPerson);

        //Persisto persona
        int oid = -1;

        //Llamo jpa Persona
        oid = myJpaPerson.create(miPerson);

        if (oid == -1)
        {
            dtoPerson = null;
        } else
        {
            this.registrarAudit(miPerson, ConstantesGui.DARALTAPerson);
        }

        return dtoPerson;
    }

    /**
     * Metodo que permite listar todos los tipos de identificaciones posibles
     *
     * @return Una ArrayList<DtoTipoIdentificacion>
     */
    public ArrayList<DtoIdentificationType> listarTiposIdentification()
    {
        ArrayList<DtoIdentificationType> listaDtoTypeIdentificaciones = new ArrayList<>();

        List<IdentificationType> miListaTipotIdentificaciones = myJpaIdentificationType.findIdentificationTypeEntities();

        for (Iterator<IdentificationType> it = miListaTipotIdentificaciones.iterator(); it.hasNext();)
        {
            IdentificationType identificationType = it.next();
            listaDtoTypeIdentificaciones.add(identificationType.getDto());
        }

        return listaDtoTypeIdentificaciones;
    }

    /**
     * Metodo que permite saber si existe una persona, por su tipo y numero de
     * identificacien
     *
     * @param dtoPersona
     * @return Si existe o no la persona
     */
    public Boolean siExistePerson(DtoPerson dtoPerson)
    {
        Boolean existeEnPersistencia = false;

        //Busco el id del tipo de identificacion y lo asocio al dto persona
        dtoPerson.getDtoIdentificationType().setIdIdentificationType(this.asociarFkIdentificationType(dtoPerson));

        Person miPerson = PersonJpaController.getInstancia().findPersonTypeIdentificationNumber(dtoPerson);

        if (miPerson != null)
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
    public DtoPerson searchPersonTypeIdentificationNumber(DtoPerson miDtoPerson)
    {

        Person miPerson = null;

        //Busco la persona
        miPerson = myJpaPerson.findPersonTypeIdentificationNumber(miDtoPerson);
        String lastName = miPerson.getLastName();
        if (miPerson != null && !lastName.equals(ConstantesGui.ADMINISTRADOR))
        {
            //Control Version del objeto
            miDtoPerson.setVersion(miPerson.getVersion());

            miDtoPerson.setId(miPerson.getPersonId());
            miDtoPerson.setFirstName(miPerson.getFirstName());
            miDtoPerson.setLastName(miPerson.getLastName());
            miDtoPerson.setIdentificationNumber(miPerson.getIdentificationNumber());
            miDtoPerson.setPhone(miPerson.getPhone());
            miDtoPerson.setEmail(miPerson.getEmail());
            miDtoPerson.getDtoIdentificationType().setName(miPerson.getFkIdIdentificationType().getName());
            miDtoPerson.getDtoIdentificationType().setIdIdentificationType(miPerson.getFkIdIdentificationType().getIdIdentificationType());

            miDtoPerson.setNationality(miPerson.getNationality());
            miDtoPerson.setBirthDate(miPerson.getBirthDate());
            miDtoPerson.setTaxId(miPerson.getTaxId());
            miDtoPerson.setMaritalStatus(miPerson.getMaritalStatus());
            miDtoPerson.setMarriageCount(miPerson.getMarriageCount());
            miDtoPerson.setSex(miPerson.getSex());
            miDtoPerson.setOccupation(miPerson.getOccupation());
            miDtoPerson.setAddress(miPerson.getAddress());

            miDtoPerson.setIsClient(miPerson.getIsClient());

            miDtoPerson.setNotaryRegistrationNumber(miPerson.getNotaryRegistrationNumber());
        } else
        {
            miDtoPerson = null;
        }

        return miDtoPerson;
    }

    /**
     * Metodo que permite busca una persona por tipo y numero de identificacien
     * que tenga una o varias gestiones asociadas.
     *
     * @param miDtoPersona
     * @return
     */
    public DtoPerson searchPersonTypeIdentificationNumberConManagement(DtoPerson miDtoPerson)
    {

        Person miPerson = null;

        //Busco la persona
        miPerson = PersonJpaController.getInstancia().findPersonTypeIdentificationNumber(miDtoPerson);
        String lastName = miPerson.getLastName();

        if (miPerson != null && miPerson.getDeedManagementList().size() > 0
                && !lastName.equals(ConstantesGui.ADMINISTRADOR))
        {
            miDtoPerson = miPerson.getDto();

        } else
        {
            miDtoPerson = null;
        }

        return miDtoPerson;
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
    public ArrayList<DtoPerson> searchPersonNameLastName(DtoPerson dtoPerson)
    {

        ArrayList<DtoPerson> listaDtoPersons = new ArrayList<>();

        try
        {
            ArrayList<Person> listaPerson = (ArrayList<Person>) PersonJpaController.getInstancia().findPersonNameLastName(dtoPerson);

            if (listaPerson != null)
            {
                listaDtoPersons = new ArrayList<>();
                for (int i = 0; i < listaPerson.size(); i++)
                {
                    String lastName = listaPerson.get(i).getLastName();

                    if (!lastName.equals(ConstantesGui.ADMINISTRADOR))
                    {
                        listaDtoPersons.add(listaPerson.get(i).getDto());
                    }
                }
            }
        }
        catch (PersistenceException e)
        {
            e.printStackTrace();
        }
        return listaDtoPersons;
    }

    /**
     * Metodo que busca personas, por aproximacion, con el nombre y apellido,
     * que tengan asociada una gestion
     *
     * @param dtoPersona
     * @return
     */
    public ArrayList<DtoPerson> searchPersonNameLastNameConManagement(DtoPerson dtoPerson)
    {

        ArrayList<DtoPerson> listaDtoPersons = new ArrayList<>();
        ArrayList<DtoPerson> listaDtoPersonsConManagement = new ArrayList<>();
        ArrayList<Person> listaPerson;

        try
        {
            listaPerson = (ArrayList<Person>) PersonJpaController.getInstancia().findPersonNameLastName(dtoPerson);

            if (listaPerson != null)
            {
                listaDtoPersons = new ArrayList<>();

                //Armo lista de DtoPerona para tener toda la red de objetos
                for (int i = 0; i < listaPerson.size(); i++)
                {
                    dtoPerson = listaPerson.get(i).getDto();
                    listaDtoPersons.add(dtoPerson);
                }

                //Recorro las personas buscando la que tienen gestion
                for (int i = 0; i < listaDtoPersons.size(); i++)
                {
                    String lastName = listaDtoPersons.get(i).getLastName();

                    if (!listaDtoPersons.get(i).getListaDtoManagementDeEscriturasPerson().isEmpty()
                            && !lastName.equals(ConstantesGui.ADMINISTRADOR))
                    {
                        DtoPerson dtoPersonConManagement = listaDtoPersons.get(i);
                        listaDtoPersonsConManagement.add(dtoPersonConManagement);
                    }
                }
            }

        }
        catch (PersistenceException e)
        {
            e.printStackTrace();
        }
        return listaDtoPersonsConManagement;
    }

    /**
     * Metodo que permite busacar todas las personas y clientes
     *
     * @return listaDtoPersonas Una lista de DtoPersonas con todas las personas
     * registradas
     * @throws NonexistentJpaException
     */
    public ArrayList<DtoPerson> searchPersonsClientes() throws NonexistentJpaException
    {

        ArrayList<DtoPerson> listaDtoPersons = new ArrayList<>();
        List<Person> listaPerson = new ArrayList<Person>();

        //Llamo jpa Persona
        listaPerson = myJpaPerson.findPersons();

        // TODO: corregir el null y la iteracioinicin.
        if (listaPerson != null)
        {
            listaDtoPersons = new ArrayList<>();
            for (int i = 0; i < listaPerson.size(); i++)
            {
                String lastName = listaPerson.get(i).getLastName();
                if (!lastName.equals(ConstantesGui.ADMINISTRADOR))
                {
                    listaDtoPersons.add(listaPerson.get(i).getDto());
                }

            }
        }

        return listaDtoPersons;
    }

    /**
     * Metodo que permite modificar una persona
     *
     * @param dtoPersona La persona a ser modificada.
     * @return Un Dtopersona para comprobar la modificacion.
     */
    public DtoPerson modificarPerson(DtoPerson dtoPerson) throws ClassModifiedException, ClassEliminatedException
    {

        Person miPerson = new Person();

        miPerson.setAtributos(dtoPerson);

        boolean flag = myJpaPerson.modificarPerson(miPerson);

        if (!flag)
        {
            dtoPerson = null;
        } else
        {
            this.registrarAudit(miPerson, ConstantesGui.MODIFICARPerson);
        }

        return dtoPerson;
    }

    /**
     * Metodo que permite dar de alta un cliente
     *
     * @param dtoCliente El cliente a ser modificado.
     * @return Un Dtopersona para comprobar la modificacien.
     */
    public DtoPerson darAltaClient(DtoPerson dtoClient) throws ClassModifiedException, ClassEliminatedException
    {

        Person miClientPerson = new Person();

        miClientPerson.setAtributos(dtoClient);

        boolean flag = myJpaPerson.modificarClient(miClientPerson);

        if (!flag)
        {
            dtoClient = null;
        } else
        {
            this.registrarAudit(miClientPerson, ConstantesGui.DARALTAClient);
        }

        return dtoClient;
    }

    /**
     * Metodo que permite modificar un cliente
     *
     * @param dtoCliente El cliente a ser modificado.
     * @return Un Dtopersona para comprobar la modificacien.
     */
    public DtoPerson modificarClient(DtoPerson dtoClient) throws ClassModifiedException, ClassEliminatedException
    {

        Person miClientPerson = new Person();

        miClientPerson.setAtributos(dtoClient);

        boolean flag = myJpaPerson.modificarClient(miClientPerson);

        if (!flag)
        {
            dtoClient = null;
        } else
        {
            this.registrarAudit(miClientPerson, ConstantesGui.MODIFICARClient);
        }

        return dtoClient;
    }

    /**
     * Metodo permite asociar el nombre del tipo de identificacien con el
     * id_tipoDocumento correspondiente
     *
     * @param dtoPersona
     * @return Un int, es el id_fk_identificaciones del tipo elegido en el
     * combo, para ser posteriormente update
     */
    public int asociarFkIdentificationType(DtoPerson dtoPerson)
    {
        int idFkIdentificationType = 0;

        //Busco el ID_tipo y creo creo el tipoIdentificacion y completo PK
        List<IdentificationType> listaIdentificaciones = new ArrayList<IdentificationType>();

        //Busco los tipo de identificaciones disponibles
        listaIdentificaciones = IdentificationTypeJpaController.getInstancia().findIdentificationTypeEntities();

        for (int i = 0; i < listaIdentificaciones.size(); i++)
        {
            if (listaIdentificaciones.get(i).getName().contains(dtoPerson.getDtoIdentificationType().getName()))
            {
                idFkIdentificationType = listaIdentificaciones.get(i).getIdIdentificationType();
            }
        }
        return idFkIdentificationType;
    }

    /**
     * Metodo que permite asociar el id_fk_tipoIdentificacion con su nombre
     * correspondiente
     *
     * @param dtoPersona
     * @return El nombre del tipo de identificacien, asociado a un
     * id_fk_tipoIdentificacion
     */
    public String asociarNameIdentificationType(DtoPerson dtoPerson)
    {
        int idFkIdentificationType = 0;

        //Busco el ID_tipo y creo creo el tipoIdentificacion y completo PK
        List<IdentificationType> listaIdentificaciones = new ArrayList<IdentificationType>();

        //Busco los tipo de identificaciones disponibles
        listaIdentificaciones = IdentificationTypeJpaController.getInstancia().findIdentificationTypeEntities();

        String nameIdentificationType = null;

        for (int i = 0; i < listaIdentificaciones.size(); i++)
        {
            if (listaIdentificaciones.get(i).getIdIdentificationType().toString().contains(dtoPerson.getDtoIdentificationType().getIdIdentificationType().toString()))
            {
                nameIdentificationType = listaIdentificaciones.get(i).getName();
            }
        }
        return nameIdentificationType;
    }

    /**
     * Metodo que controla cuando se modifica una persona, que no existe otra
     * con el mismo tipo y numero de identificacien
     *
     * @param dtoPersonaModificada
     * @param dtoPersonaOrginal
     * @return El dtoPersona modificado
     */
    public Boolean controlModificacionPerson(DtoPerson dtoPersonOrginal, DtoPerson dtoPersonModificada)
    {
        Boolean flag = false;

        //Control si fue modificado el  tipo o numero de identificacien
        if (dtoPersonModificada.getDtoIdentificationType().getName().equals(dtoPersonOrginal.getDtoIdentificationType().getName()) == false
                || dtoPersonModificada.getIdentificationNumber().equals(dtoPersonOrginal.getIdentificationNumber()) == false)
        {
            Person miPerson = myJpaPerson.findPersonTypeIdentificationNumber(dtoPersonModificada);

            if (miPerson != null)
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
    public DtoProperty searchProperty(DtoProperty miDtoProperty)
    {
        DtoProperty dtoProperty = null;

        Property miProperty = myJpaProperty.findProperty(miDtoProperty);

        if (miProperty != null)
        {
            dtoProperty = miProperty.getDto();
        }

        return dtoProperty;
    }

    /**
     * Busca un inmueble asociado a un Tramite en particular.
     *
     * @param miDtoTramite, con los datos del Tramite, asociado al Inmueble a
     * buscar.
     * @return el Dto del Inmueble encontrado asociado al Tramite indicado.
     */
    public DtoProperty searchProperty(DtoProcedure miDtoProcedure)
    {
        DtoProperty dtoProperty = null;

        Procedure miProcedure = myJpaProcedure.findProcedure(miDtoProcedure.getIdProcedure());

        if (miProcedure != null)
        {
            Property miProperty = miProcedure.getFkIdProperty();

            if (miProperty != null)
            {
                dtoProperty = miProperty.getDto();
            }
        }

        return dtoProperty;
    }

    /**
     * Busca los Conceptos asociados a un determinado Tipo de Tramite.
     *
     * @param dtoTipoTramite, Dto del Tipo de Tramite del cual buscar los
     * conceptos.
     * @return los Dto de los Conceptos asociado al Tipo de Tramite indicado.
     */
    public ArrayList<DtoConcept> obtenerConceptosProcedure(DtoProcedureType dtoTypeProcedure)
    {
        ArrayList<DtoConcept> dtosConceptos = new ArrayList<>();
        ArrayList<BudgetTemplate> plantillas = null;

        plantillas = (ArrayList<BudgetTemplate>) myJpaBudgetTemplate.findPlantillasDeBudget(dtoTypeProcedure.getIdProcedureType());

        if (plantillas != null)
        {
            for (int i = 0; i < plantillas.size(); i++)
            {
                BudgetTemplate budgetTemplate = plantillas.get(i);

                Concept miConcept = budgetTemplate.getConcept();

                dtosConceptos.add(miConcept.getDto());
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
    public int crearBudget(DtoPerson dtoPerson, DtoBudget dtoBudget, DtoProcedure dtoProcedure, DtoProperty dtoProperty, ArrayList<DtoItem> dtosItems)
    {
        int creado = -1;

        int idProperty = darAltaProperty(dtoProperty);

        if (idProperty != -1)
        {
            try
            {
                Property miProperty;
                miProperty = myJpaProperty.findProperty(idProperty);

                int idProcedure = darAltaProcedure(dtoProcedure, miProperty);

                if (idProcedure != -1)
                {

                    Procedure miProcedure = myJpaProcedure.findProcedure(idProcedure);

                    Person miPerson = myJpaPerson.findPerson(dtoPerson.getId());

                    //Creo el presupuesto:
                    Budget miBudget = new Budget();

                    miBudget.setDate(Calendar.getInstance().getTime());
                    miBudget.setSaldo(dtoBudget.getSaldo());
                    miBudget.setTotal(dtoBudget.getTotal());
                    miBudget.setFkIdPerson(miPerson);
                    miBudget.setNotes(dtoBudget.getNotes());

                    creado = myJpaBudget.create(miBudget);

                    if (creado != -1)
                    {
                        miBudget.setIdBudget(creado);

                        //Actualizo el tramite:
                        miProcedure.setFkIdBudget(miBudget);

                        Boolean modificado = myJpaProcedure.asociarBudget(miProcedure);

                        if (modificado)
                        {
                            //Creo los Items:
                            ArrayList<Item> misItems = new ArrayList<>();

                            for (int i = 0; i < dtosItems.size(); i++)
                            {
                                DtoItem dtoItem = dtosItems.get(i);
                                Item miItem = new Item();

                                miItem.setAtributos(dtoItem);
                                miItem.setFkIdBudget(miBudget);

                                int id = myJpaItem.create(miItem);

                                if (id != -1)
                                {
                                    miItem.setIdItem(id);
                                    misItems.add(miItem);
                                }
                            }
                        }
                        //  registramos en auditoria en nuevo presupuesto
                        this.registrarAudit(miBudget, ConstantesGui.CREARBudget);
                    }
                }

            }
            catch (Exception ex)
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
    public int crearBudget(DtoPerson dtoPerson, DtoBudget dtoBudget, DtoProcedure dtoProcedure, ArrayList<DtoItem> dtosItems)
    {
        int creado = -1;

        int idProcedure = darAltaProcedure(dtoProcedure);

        if (idProcedure != -1)
        {

            Procedure miProcedure = myJpaProcedure.findProcedure(idProcedure);

            Person miPerson = myJpaPerson.findPerson(dtoPerson.getId());

            //Creo el presupuesto:
            Budget miBudget = new Budget();

            miBudget.setDate(Calendar.getInstance().getTime());
            miBudget.setSaldo(dtoBudget.getSaldo());
            miBudget.setTotal(dtoBudget.getTotal());
            miBudget.setFkIdPerson(miPerson);
            miBudget.setNotes(dtoBudget.getNotes());

            creado = myJpaBudget.create(miBudget);

            if (creado != -1)
            {
                miBudget.setIdBudget(creado);

                //Actualizo el tramite:
                miProcedure.setFkIdBudget(miBudget);

                Boolean modificado = myJpaProcedure.asociarBudget(miProcedure);

                if (modificado)
                {
                    //Creo los Items:
                    ArrayList<Item> misItems = new ArrayList<>();

                    for (int i = 0; i < dtosItems.size(); i++)
                    {
                        DtoItem dtoItem = dtosItems.get(i);
                        Item miItem = new Item();

                        miItem.setAtributos(dtoItem);
                        miItem.setFkIdBudget(miBudget);

                        int id = myJpaItem.create(miItem);

                        if (id != -1)
                        {
                            miItem.setIdItem(id);
                            misItems.add(miItem);
                        }
                    }
                    //  registramos en auditoria en nuevo presupuesto
                    this.registrarAudit(miBudget, ConstantesGui.CREARBudget);
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
    private int darAltaProperty(DtoProperty miDtoProperty)
    {
        int idProperty = -1;

        // Creo el inmueble:
        Property miProperty = new Property();
        miProperty.setAtributos(miDtoProperty);

        idProperty = myJpaProperty.create(miProperty);

        this.registrarAudit(miProperty, ConstantesGui.CREARBudget);

        return idProperty;
    }

    /**
     * Da de alta un Tramite con un Inmueble asociado.
     *
     * @param miDtoTramite datos del Tramite a dar de alta.
     * @param miInmueble datos del Inmueble asociado.
     * @return el id del Tramite dado de alta, -1 de lo contrario.
     */
    private int darAltaProcedure(DtoProcedure miDtoProcedure, Property miProperty)
    {
        int idProcedure = -1;
        try
        {

            Procedure miProcedure = new Procedure();

            ProcedureType miProcedureType = new ProcedureType();
            miProcedureType.setAtributos(miDtoProcedure.getProcedureType());

            miProcedure.setFkIdProcedureType(miProcedureType);
            miProcedure.setFkIdProperty(miProperty);

            idProcedure = myJpaProcedure.create(miProcedure);

        }
        catch (IllegalOrphanException ex)
        {
            ex.printStackTrace();
        }
        return idProcedure;
    }

    /**
     * Da de alta un Tramite.
     *
     * @param miDtoTramite datos del Tramite a dar de alta.
     * @return el id del Tramite dado de alta, -1 de lo contrario.
     */
    private int darAltaProcedure(DtoProcedure miDtoProcedure)
    {
        int idProcedure = -1;
        try
        {

            Procedure miProcedure = new Procedure();

            ProcedureType miProcedureType = new ProcedureType();
            miProcedureType.setAtributos(miDtoProcedure.getProcedureType());

            miProcedure.setFkIdProcedureType(miProcedureType);

            idProcedure = myJpaProcedure.create(miProcedure);

        }
        catch (IllegalOrphanException ex)
        {
            ex.printStackTrace();
        }
        return idProcedure;
    }

    /**
     * Busca todos los presupuestos asociados a una determinada persona.
     *
     * @param dtoPersona Los datos de la persona.
     * @return dtosPresupuestosEncontrados Una lista de Dto con los presupuestos
     * encontrados. Retorna la lista vacia en caso de no haber presupuesto
     * registrados.
     */
    public ArrayList<DtoBudget> searchPresupuestosPerson(DtoPerson dtoPerson) throws NonexistentJpaException
    {
        ArrayList<DtoBudget> dtosPresupuestosEncontrados = new ArrayList<>();

//        Persona miPersona = miJpaPersona.findPersona(dtoPersona.getIdPersona());
        ArrayList<Budget> presupuestos = (ArrayList<Budget>) myJpaBudget.findPresupuestosPerson(dtoPerson.getId());

        if ((presupuestos != null) && (!presupuestos.isEmpty()))
        {

            for (int i = 0; i < presupuestos.size(); i++)
            {
                Budget budget = presupuestos.get(i);

                //La persona no tiene la red de objetos, la busco
                Person person = budget.getFkIdPerson();
                person = this.obtenerRedObjetosPerson(person);

                //Set persona con su red d objetos
                budget.setFkIdPerson(person);

                DtoBudget miDto = budget.getDto();
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
    public Person obtenerRedObjetosPerson(Person person) throws NonexistentJpaException
    {

        boolean flag = false;

        ArrayList<Person> listaPerson = (ArrayList<Person>) myJpaPerson.findPersons();
        for (int j = 0; j < listaPerson.size() && !flag; j++)
        {
            Person personList = listaPerson.get(j);
            Integer idPerson = person.getPersonId();
            Integer idPersonList = personList.getPersonId();
            if (idPerson.equals(idPersonList))
            {
                person = personList;
                flag = true;
            }
        }
        return person;
    }

    /**
     * Busca todos los Items asociados a un Presupuesto en particular.
     *
     * @param dtoPresupuesto datos del Presupuesto en particular.
     * @return una lista de Dto de los Items asociados al Presupuesto indicado.
     */
    public ArrayList<DtoItem> searchItemsBudget(DtoBudget dtoBudget)
    {
        ArrayList<DtoItem> dtosItemsEncontrados = null;
        ArrayList<Item> items = null;

        items = (ArrayList<Item>) myJpaItem.findItemsBudget(dtoBudget.getIdBudget());

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
    public DtoBudget searchBudgetPorNumber(DtoBudget miDtoBudget)
    {
        DtoBudget dtoBudgetEncontrado = null;
        Budget miBudget;

        miBudget = myJpaBudget.findBudget(miDtoBudget.getIdBudget());

        if (miBudget != null)
        {
            Person miPerson = miBudget.getFkIdPerson();

            if (miPerson != null)
            {
                dtoBudgetEncontrado = miBudget.getDto();
            }

            dtoBudgetEncontrado.setPerson(miPerson.getDto());

        }

        return dtoBudgetEncontrado;
    }

    /**
     * Modifica los datos de un Presupuesto en particular.
     *
     * @param miDtoPresupuesto datos del Presupuesto a modificar.
     * @param dtosItems datos de los Items actuales del Presupuesto.
     * @param dtosItemsNuevos datos de los nuevos Items del Presupuesto.
     * @return TRUE si se modifico el Presupuesto, FALSE de lo contrario.
     */
    public Boolean modificarBudget(DtoBudget miDtoBudget, ArrayList<DtoItem> dtosItems, ArrayList<DtoItem> dtosItemsNuevos) throws ClassModifiedException
    {
        Boolean modificado = false;
        ArrayList<Item> itemsTodosJuntos = new ArrayList<>();

        Budget miBudget = myJpaBudget.findBudget(miDtoBudget.getIdBudget());

        if (miBudget != null)
        {
            if (miBudget.getVersion() == miDtoBudget.getVersion())
            {

                try
                {

                    miBudget.setDate(miDtoBudget.getDate());
                    miBudget.setNotes(miDtoBudget.getNotes());
                    miBudget.setSaldo(miDtoBudget.getSaldo());
                    miBudget.setTotal(miDtoBudget.getTotal());
                    miBudget.setVersion(miDtoBudget.getVersion());

                    ArrayList<Item> itemsViejos = new ArrayList<Item>();
                    for (int i = 0; i < dtosItems.size(); i++)
                    {
                        DtoItem dtoItem = dtosItems.get(i);

                        Item itemViejo = new Item();
                        itemViejo.setAtributos(dtoItem);

                        itemsViejos.add(itemViejo);
                    }

                    miBudget.setItemList(itemsViejos);

                    modificado = myJpaBudget.edit(miBudget);

                    if (modificado)
                    {
                        this.registrarAudit(miBudget, ConstantesGui.MODIFICARBudget);

                        Boolean eliminadoItem = false;

                        for (int i = 0; i < itemsViejos.size(); i++)
                        {

                            Item miItem = myJpaItem.findItem(itemsViejos.get(i).getIdItem());

                            if (miItem != null)
                            {
                                eliminadoItem = myJpaItem.eliminarItem(miItem);

                                if (!eliminadoItem)
                                {
                                    break;
                                }
                                this.registrarAudit(miItem, ConstantesGui.MODIFICARBudget);
                            }
                        }

                        for (int i = 0; i < dtosItemsNuevos.size(); i++)
                        {
                            DtoItem dtoItem = dtosItemsNuevos.get(i);
                            Item item = new Item();
                            item.setAtributos(dtoItem);
                            item.setFkIdBudget(miBudget);

                            int id = myJpaItem.create(item);
                            item.setIdItem(id);

                            itemsTodosJuntos.add(item);

                            this.registrarAudit(item, ConstantesGui.MODIFICARBudget);
                        }
                    }
                }
                catch (IllegalOrphanException ex)
                {
                    modificado = false;
                }
                catch (NonexistentEntityException ex)
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

    public DtoBudget searchBudget(DtoBudget miDtoBudget) throws NonexistentJpaException
    {
        DtoBudget miBudget = null;
        Budget budget = myJpaBudget.findPresupuestosById(miDtoBudget.getIdBudget());

        if (budget != null)
        {
            //Busco red de objetos de la persona
            Person miPerson = null;
            Person miPersonBudget = budget.getFkIdPerson();
            miPerson = BusinessController.getInstancia().obtenerRedObjetosPerson(miPersonBudget);
            budget.setFkIdPerson(miPerson);
            miBudget = budget.getDto();
        }

        return miBudget;
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
    public DtoDeedManagement obtenerProximaDeedManagement()
    {
        DtoDeedManagement dtoProximaManagement = new DtoDeedManagement();

        int cantidadGestiones = miJpaDeedManagement.obtenerUltimoNumberManagement();
        //int cantidadGestiones = miJpaGestionDeEscritura.getGestionDeEscrituraCount();

        dtoProximaManagement.setNumber(cantidadGestiones);

        return dtoProximaManagement;
    }

    /**
     * Busca una determinada gestion de escritura por su numero.
     *
     * @param gestionBuscar Un DTO con la gestion de escritura a buscar.
     * @return gestionEncontrada Un objeto de negocio de tipo gestion de
     * escritura.
     */
    public DeedManagement searchManagement(DtoDeedManagement managementSearch)
    {
        DeedManagement managementEncontrada = new DeedManagement();

        managementEncontrada = miJpaDeedManagement.findDeedManagementPorNumber(managementSearch.getNumber());

        return managementEncontrada;
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
    public DtoDeedManagement iniciarDeedManagement(DtoDeedManagement dtoNuevaManagement)
    {
        try
        {
            DeedManagement nuevaManagement = new DeedManagement();
            dtoNuevaManagement.setIdManagement(BusinessConstants.ID_OBJETO_NO_VALIDO);

            DeedManagement managementExistente = searchManagement(dtoNuevaManagement);

            //  verificamos que la gestion que vamos a iniciar, ya no se encuentre registrada.
            if (managementExistente.getNumber() == ConstantesPersistencia.VersionINICIAL)
            {
                //  gesiton -> estado iniciaL
                //  gestion -> escribano
                //  gestion -> datos de la gestion
                nuevaManagement.setAtributos(dtoNuevaManagement);

                //  gestion -> cliente referencia
                //  gestion -> lista clientes involucrados
                //  gestion <- tramites_personas (relacional)
                List<PersonProcedure> relaciones = new ArrayList<>();

                // Para el cliente de referencia.
                Person clientReferencia = myJpaPerson.findPersonTypeIdentificationNumber(dtoNuevaManagement.getClientReferencia());

                //  gestion -> lista tramites asociados
                //  gestion <- tramite(s) (nueva)
                List<Procedure> listaProcedures = new ArrayList<Procedure>();

                for (Iterator<DtoProcedure> it = dtoNuevaManagement.getListaProceduresAsociados().iterator(); it.hasNext();)
                {
                    DtoProcedure dtoProcedure = it.next();
                    dtoProcedure.setDeedManagement(dtoNuevaManagement);

                    Procedure procedure = myJpaProcedure.findProcedure(dtoProcedure.getIdProcedure());

                    procedure.setFkIdManagement(nuevaManagement);

                    listaProcedures.add(procedure);

                    nuevaManagement.getProcedureList().add(procedure);

                    PersonProcedure relacionClientReferencia = new PersonProcedure();
                    relacionClientReferencia.setPerson(clientReferencia);
                    relacionClientReferencia.setProcedure(procedure);
                    relaciones.add(relacionClientReferencia);
                }

                // Para la lista de clientes involucrados.
                for (Iterator<DtoPerson> itClientes = dtoNuevaManagement.getListaClientesInvolucrados().iterator(); itClientes.hasNext();)
                {
                    DtoPerson dtoPerson = itClientes.next();
                    Person client = myJpaPerson.findPersonTypeIdentificationNumber(dtoPerson);

                    for (Iterator<Procedure> itProcedures = listaProcedures.iterator(); itProcedures.hasNext();)
                    {
                        Procedure procedure = itProcedures.next();

                        PersonProcedure relacionClientProcedure = new PersonProcedure();
                        relacionClientProcedure.setPerson(client);
                        relacionClientProcedure.setProcedure(procedure);

                        relaciones.add(relacionClientProcedure);
                    }
                }

                for (Iterator<PersonProcedure> itRelaciones = relaciones.iterator(); itRelaciones.hasNext();)
                {
                    PersonProcedure personProcedure = itRelaciones.next();

                    personProcedure.setNotes("Tramites de la gestion: " + nuevaManagement.getNumber());

                    myJpaPersonProcedure.create(personProcedure);
                }

                //  Persistir entidades
                Integer idManagement = miJpaDeedManagement.create(nuevaManagement);
                nuevaManagement.setIdManagement(idManagement);
                dtoNuevaManagement.setIdManagement(idManagement);

                DtoHistory history = this.registrarMovementHistory(dtoNuevaManagement);

                this.registrarAudit(nuevaManagement, ConstantesGui.INICIARManagement);
                try
                {
                    this.ingresarDocumentacion(dtoNuevaManagement);
                }
                catch (NonexistentEntityException ex)
                {
                    Logger.getLogger(BusinessController.class.getName()).log(Level.SEVERE, null, ex);
                }
                catch (ClassModifiedException ex)
                {
                    Logger.getLogger(BusinessController.class.getName()).log(Level.SEVERE, null, ex);
                }

            } else
            {
                //  La gestion ya se encuentra registrada.
                return dtoNuevaManagement;
            }
        }
        catch (CreateEntityException | PreexistingEntityException | DtoInvalidoException ex)
        {
            Logger.getLogger(BusinessController.class.getName()).log(Level.SEVERE, null, ex);
        }
        return dtoNuevaManagement;
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
    public DtoDeedManagement modificarDeedManagement(DtoDeedManagement dtoManagementModificar, List<DtoPerson> listaDtoClientesAgregados, List<DtoPerson> listaDtoClientesEliminados) throws ClassModifiedException
    {
        try
        {
            DeedManagement managementParaModificar = new DeedManagement();

            managementParaModificar.setAtributos(dtoManagementModificar);

            if (!listaDtoClientesEliminados.isEmpty())
            {
                // eliminar clientes de la lista de tramites clientes, para una gestion dada.
                for (Iterator<DtoPerson> itClientes = listaDtoClientesEliminados.iterator(); itClientes.hasNext();)
                {
                    DtoPerson dtoPerson = itClientes.next();
                    Person client = myJpaPerson.findPersonTypeIdentificationNumber(dtoPerson);

                    for (Iterator<DtoProcedure> itProcedures = dtoManagementModificar.getListaProceduresAsociados().iterator(); itProcedures.hasNext();)
                    {
                        DtoProcedure dtoProcedure = itProcedures.next();

                        Procedure procedure = myJpaProcedure.findProcedure(dtoProcedure.getIdProcedure());

                        PersonProcedure relacionClientProcedure = new PersonProcedure();
                        relacionClientProcedure.setPerson(client);
                        relacionClientProcedure.setProcedure(procedure);

                        List<PersonProcedure> listaEliminar = myJpaPersonProcedure.findProceduresClientes(client.getPersonId(), procedure.getIdProcedure());

                        for (Iterator<PersonProcedure> it = listaEliminar.iterator(); it.hasNext();)
                        {
                            PersonProcedure personProcedure = it.next();

                            PersonProcedurePK pk = new PersonProcedurePK(procedure.getIdProcedure(), client.getPersonId());

                            personProcedure.setPersonProcedurePK(pk);

                            myJpaPersonProcedure.eliminarRecord(personProcedure);
                        }
                    }
                }
            }

            if (!listaDtoClientesAgregados.isEmpty())
            {

                List<PersonProcedure> relaciones = new ArrayList<>();

                //  agregar clientes a la lista de clientes involucrados.
                for (Iterator<DtoPerson> itClientes = listaDtoClientesAgregados.iterator(); itClientes.hasNext();)
                {
                    DtoPerson dtoPerson = itClientes.next();
                    Person client = myJpaPerson.findPersonTypeIdentificationNumber(dtoPerson);

                    for (Iterator<DtoProcedure> itProcedures = dtoManagementModificar.getListaProceduresAsociados().iterator(); itProcedures.hasNext();)
                    {
                        DtoProcedure dtoProcedure = itProcedures.next();
                        Procedure procedure = myJpaProcedure.findProcedure(dtoProcedure.getIdProcedure());

                        PersonProcedure relacionClientProcedure = new PersonProcedure();
                        relacionClientProcedure.setPerson(client);
                        relacionClientProcedure.setProcedure(procedure);

                        relaciones.add(relacionClientProcedure);
                    }
                }

                for (Iterator<PersonProcedure> itRelaciones = relaciones.iterator(); itRelaciones.hasNext();)
                {
                    PersonProcedure personProcedure = itRelaciones.next();

                    personProcedure.setNotes("Tramites de la gestion: " + dtoManagementModificar.getNumber());

                    myJpaPersonProcedure.create(personProcedure);
                }

            }

            List<DtoHistory> historyManagement = this.obtenerHistoryManagement(dtoManagementModificar);

            for (Iterator<DtoHistory> it = historyManagement.iterator(); it.hasNext();)
            {
                DtoHistory dtoHistory = it.next();
                History history = new History();
                history.setAtributos(dtoHistory);
                managementParaModificar.getHistoryList().add(history);
            }

            ManagementStatus nuevoStatusModificada = this.obtenerManagementStatus(BusinessConstants.ManagementStatusMODIFICADA);
            nuevoStatusModificada.setNotes("Gestion: " + managementParaModificar.getIdManagement() + ", Modificada");

            if (miJpaDeedManagement.modificarDeedManagement(managementParaModificar) == true)
            {
                DtoManagementStatus nuevoStatus = nuevoStatusModificada.getDto();
                dtoManagementModificar.setStatus(nuevoStatus);

                this.registrarMovementHistory(dtoManagementModificar);

                this.registrarAudit(managementParaModificar, ConstantesGui.MODIFICARManagement);
            } else
            {
                dtoManagementModificar.setIdManagement(BusinessConstants.ID_OBJETO_NO_VALIDO);
            }

        }
        catch (PreexistingEntityException | CreateEntityException | DtoInvalidoException ex)
        {
            Logger.getLogger(BusinessController.class.getName()).log(Level.SEVERE, null, ex);
        }
        return dtoManagementModificar;
    }

    /**
     * Permite obtener un estado de gestion en base al nombre del mismo.
     *
     * @param nombreEstadoGestion El nombre del estado de gestion a buscar.
     * @return La instancia del estado de gestion encontrado.
     */
    public ManagementStatus obtenerManagementStatus(String nameStatusManagement)
    {
        for (Iterator<DtoManagementStatus> it = this.obtenerListaEstadosDeManagementDisponibles().iterator(); it.hasNext();)
        {
            DtoManagementStatus dtoManagementStatus = it.next();
            if (dtoManagementStatus.getName().equals(nameStatusManagement))
            {
                ManagementStatus statusManagementEncontrado = new ManagementStatus();
                try
                {
                    statusManagementEncontrado.setAtributo(dtoManagementStatus);
                    return statusManagementEncontrado;
                }
                catch (DtoInvalidoException ex)
                {
                    Logger.getLogger(BusinessController.class.getName()).log(Level.SEVERE, null, ex);
                }

            }
        }
        return new ManagementStatus();
    }

    /**
     * Metodo que permite obtener, en base a un dto estado de gestion, el estado
     * de gestion indicado.
     *
     * @param nombreEstadoGestion Un dto con el nombre del estado de gestion.
     * @return dtoEstadoDeGestion El estado de gestion indicado si se encontro.
     */
    public DtoManagementStatus obtenerDtoManagementStatus(DtoManagementStatus nameStatusManagement)
    {
        for (Iterator<DtoManagementStatus> it = this.obtenerListaEstadosDeManagementDisponibles().iterator(); it.hasNext();)
        {
            DtoManagementStatus dtoManagementStatus = it.next();
            if (dtoManagementStatus.getName().equals(nameStatusManagement.getName()))
            {
                return dtoManagementStatus;

            }
        }
        return new DtoManagementStatus("Estado no valido");
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
    public DtoDeedManagement modificarManagementStatusDeDeed(DtoDeedManagement dtoManagement) throws ClassModifiedException
    {
        try
        {
            DeedManagement management = this.searchManagement(dtoManagement);

            ManagementStatus status = new ManagementStatus();
            status.setAtributo(dtoManagement.getStatus());

            management.setFkIdManagementStatus(status);

            miJpaDeedManagement.modificarManagementStatusDeDeed(management);

        }
        catch (DtoInvalidoException ex)
        {
            Logger.getLogger(BusinessController.class.getName()).log(Level.SEVERE, null, ex);
        }
        return dtoManagement;
    }

    /**
     * Permite obtener el estado actual de una determinada gestion.
     *
     * @param dtoGestion Una gestion de la cual se desea conocer su estado
     * actual.
     * @return estadoActual El estado actual de la gestion indicada.
     *
     */
    public DtoManagementStatus obtenerStatusActualDeManagement(DtoDeedManagement dtoManagement)
    {
        DtoManagementStatus statusActual = new DtoManagementStatus();

        myJpaHistory.findStatusActualManagement(dtoManagement.getIdManagement());

        return statusActual;
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
    public List<DtoDeedManagement> obtenerGestionesEnProcedure() throws NonexistentJpaException
    {
        List<DtoDeedManagement> listaDtoManagementDeEscrituras = new ArrayList<>();
        List<DeedManagement> listaManagementDeEscrituras = new ArrayList<>();

        listaManagementDeEscrituras = miJpaDeedManagement.findGestionesDeDeed();

        if (!listaManagementDeEscrituras.isEmpty())
        {
            for (int i = 0; i < listaManagementDeEscrituras.size(); i++)
            {
                DeedManagement miManagement = listaManagementDeEscrituras.get(i);
                String status = miManagement.getFkIdManagementStatus().getName();
                if (!status.equals(BusinessConstants.ManagementARCHIVADA))
                {
                    listaDtoManagementDeEscrituras.add(miManagement.getDto());
                }
            }
        }

        return listaDtoManagementDeEscrituras;
    }

    /**
     * Metodo que permite archivar una o varias gestiones, en estado de tramite
     *
     * @param listaDtoGestionesDeEscritura La lista de gestiones para ser
     * archivadas.
     * @return DtoFlag Un dto tipo flag (bandera) para indicar si se pudieron
     * archivar las gestiones o no.
     */
    public DtoFlag archivingManagement(List<DtoDeedManagement> listaDtoGestionesDeDeed) throws NonexistentJpaException, DtoInvalidoException, ClassModifiedException
    {
        DtoFlag flag = new DtoFlag();
        flag.setFlag(false);
        int idManagementStatus = 0;
        List<DeedManagement> lisManagementEscrituras = new ArrayList<>();
        List<ManagementStatus> listEstadosManagement = new ArrayList<>();

        listEstadosManagement = miJpaManagementStatus.findManagementStatusEntities();

        for (int i = 0; i < listEstadosManagement.size(); i++)
        {
            String nameStatus = listEstadosManagement.get(i).getName();
            ManagementStatus statusGetsion = listEstadosManagement.get(i);

            if (nameStatus.equals(BusinessConstants.ManagementARCHIVADA))
            {
                idManagementStatus = statusGetsion.getIdManagementStatus();
            }

        }

        for (int i = 0; i < listaDtoGestionesDeDeed.size(); i++)
        {
            DeedManagement deedManagement = new DeedManagement();
            deedManagement.setAtributos(listaDtoGestionesDeDeed.get(i));
            deedManagement.getFkIdManagementStatus().setIdManagementStatus(idManagementStatus);
            // lisGestionEscrituras.add(gestionDeEscritura);
            flag.setFlag(miJpaDeedManagement.archivingGestiones(deedManagement));

            this.registrarAudit(deedManagement, ConstantesGui.ArchivingManagement);

            this.registrarMovementHistory(deedManagement.getDto());

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
    public DtoDeedManagement searchDtoManagement(DtoDeedManagement managementSearch)
    {
        DeedManagement managementEncontrada = new DeedManagement();
        DtoDeedManagement dtoManagement = null;

        managementEncontrada = miJpaDeedManagement.findDeedManagementPorId(managementSearch.getIdManagement());

        dtoManagement = this.obtenerDocNecesarioEntregadosNoEntregadosDeManagement(managementEncontrada.getDto());

        return dtoManagement;
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
    public List<DtoHistory> obtenerHistoryManagement(DtoDeedManagement dtoManagement)
    {
        List<DtoHistory> listaDtoHistory = new ArrayList<>();

        List<History> recordHistory = myJpaHistory.findRecordHistial(dtoManagement.getIdManagement());

        if (!recordHistory.isEmpty())
        {
            for (Iterator<History> it = recordHistory.iterator(); it.hasNext();)
            {
                History history = it.next();

                DtoHistory dtoHistory = history.getDto();

                listaDtoHistory.add(dtoHistory);
            }
        }

        return listaDtoHistory;
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
    public DtoHistory registrarMovementHistory(DtoDeedManagement dtoManagement)
    {
        DtoHistory dtoNuevoHistory = new DtoHistory();
        try
        {

            History nuevoRecordHistory = new History();

            Date date = Calendar.getInstance().getTime();
            nuevoRecordHistory.setDate(date);

            DeedManagement management = new DeedManagement();

            DtoPerson dtoNotary = dtoManagement.getPersonNotary();
            Person notary = myJpaPerson.findPersonPorId(dtoNotary.getId());

            dtoManagement.setPersonNotary(notary.getDto());

            management.setAtributos(dtoManagement);
            nuevoRecordHistory.setFkIdManagement(management);

            ManagementStatus status = new ManagementStatus();
            status.setAtributo(dtoManagement.getStatus());

            nuevoRecordHistory.setFkIdManagementStatus(status);
            nuevoRecordHistory.setNotes("Gestion: " + dtoManagement.getNumber() + ", Estado: " + status.getName());

            Integer idHistory = myJpaHistory.create(nuevoRecordHistory);

        }
        catch (CreateEntityException | DtoInvalidoException ex)
        {
            Logger.getLogger(BusinessController.class.getName()).log(Level.SEVERE, null, ex);
        }
        return dtoNuevoHistory;
    }

    /**
     * Metodo que permite obtener el cliente de referencia de una determinada
     * gestion, en base al id de la gestion.
     *
     * @param dtoGestion Un DTO tipo gestion con el ID para buscar la gestion.
     * @return clienteReferencia Un DTO tipo persona que representa el cliente
     * de referencia de la gestion indicada.
     */
    public DtoPerson obtenerClientReferenciaManagement(DtoDeedManagement dtoManagement) throws NonexistentJpaException
    {
        DtoPerson clientReferencia = null;

        DtoProcedure unProcedure = dtoManagement.getListaProceduresAsociados().get(0);
        Procedure procedureManagement = new Procedure();

        procedureManagement.setAtributos(this.searchProcedure(unProcedure));

        DtoBudget budget = new DtoBudget();
        budget.setIdBudget(procedureManagement.getFkIdBudget().getIdBudget());

        DtoBudget unBudget = this.searchBudget(budget);

        clientReferencia = this.searchPersonTypeIdentificationNumber(unBudget.getPerson());

        return clientReferencia;
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
    public ArrayList<DtoDocumentType> obtenerDocumentsNecesarioTypeProcedure(DtoProcedure dtoProcedure)
    {

        ArrayList<DtoDocumentType> listaDtoDocumentsNecesarios = new ArrayList<>();
        ArrayList<DocumentType> listaDocumentsNecesarios = new ArrayList<>();
        ArrayList<ProcedureTemplate> listaTemplateProcedures = new ArrayList<>();
        ProcedureType typeProcedure = null;

        listaTemplateProcedures = (ArrayList<ProcedureTemplate>) myJpaProcedureTemplate.findPlantillasProcedures();

        for (int i = 0; i < listaTemplateProcedures.size(); i++)
        {
            typeProcedure = listaTemplateProcedures.get(i).getProcedureType();

            if (typeProcedure.getName().equals(dtoProcedure.getProcedureType().getName())) //&& tipoTramite.getHabilitado())
            {
                DocumentType documentType = listaTemplateProcedures.get(i).getDocumentType();

                listaDocumentsNecesarios.add(documentType);
            }
        }

        for (int i = 0; i < listaDocumentsNecesarios.size(); i++)
        {
            DtoDocumentType dtoDocumentType = new DtoDocumentType();
            dtoDocumentType = listaDocumentsNecesarios.get(i).getDto();
            listaDtoDocumentsNecesarios.add(dtoDocumentType);
        }
        return listaDtoDocumentsNecesarios;
    }

    /**
     * Metodo que permite obtener los documentos necesarios para un tramite
     *
     * @param listaDtoTramitesDeGestion
     * @return Una lista de tramites, cada uno, con su documentos necesarios
     */
    public ArrayList<DtoProcedure> obtenerDocumentsNecesariosPorProcedure(ArrayList<DtoProcedure> listaDtoProceduresDeManagement)
    {
        ArrayList<DtoDocumentType> listaDocumentsNecesariosPorProcedure = null;
        ArrayList<DtoProcedure> listaProceduresConSusDocumentsNecesarios = new ArrayList<>();

        for (int i = 0; i < listaDtoProceduresDeManagement.size(); i++)
        {
            DtoProcedure dtoProcedureDeLaGesion = listaDtoProceduresDeManagement.get(i);
            listaDocumentsNecesariosPorProcedure = BusinessController.getInstancia().obtenerDocumentsNecesarioTypeProcedure(dtoProcedureDeLaGesion);

            //Guardo los documentos necesarios para un tramite determinado
            dtoProcedureDeLaGesion.setListaDocumentsNecesarios(listaDocumentsNecesariosPorProcedure);

            //Cargo el tramite con sus documentos necesarios, cada tramite tiene la gestion asociada
            listaProceduresConSusDocumentsNecesarios.add(dtoProcedureDeLaGesion);
        }
        return listaProceduresConSusDocumentsNecesarios;
    }

    /**
     * Metodo que permite obtener los documentos presentados para un tramite
     * determinado
     *
     * @param listaDtoTramitesDeGestion
     * @return
     */
    public ArrayList<DtoProcedure> obtenerDocumentsPresentadosPorProcedure(ArrayList<DtoProcedure> listaDtoProceduresDeManagement)
    {
        ArrayList<DtoProcedure> listaDtoProceduresConSusDocumentsEntregados = new ArrayList<>();
        List<SubmittedDocument> listaDocumentPresentados = null;
        boolean flag = false;

        //Obtengo todos  los documentos presentados, de los tramites
        listaDocumentPresentados = myJpaSubmittedDocument.findDocumentsPresentados();

        for (int i = 0; i < listaDtoProceduresDeManagement.size(); i++)
        {
            DtoProcedure dtoProcedure = listaDtoProceduresDeManagement.get(i);
            int idProcedureConsultar = listaDtoProceduresDeManagement.get(i).getIdProcedure();

            //Recorro toda la lista de documentos presentados, buscando mi tramite
            for (int j = 0; j < listaDocumentPresentados.size(); j++)
            {
                DtoSubmittedDocument dtoSubmittedDocument = listaDocumentPresentados.get(j).getDto();
                int idProcedureDocumentsPresentados = dtoSubmittedDocument.getFkProcedure().getIdProcedure();

                if (idProcedureDocumentsPresentados == idProcedureConsultar)//Si son =, se entrego un documento
                {
                    //Cargo lista de tramites con sus documentos presentados si, los tienen
                    flag = true;
                    dtoProcedure.getListaDocumentsManagement().add(dtoSubmittedDocument);
                }

            }
            listaDtoProceduresConSusDocumentsEntregados.add(dtoProcedure);

        }
        return listaDtoProceduresConSusDocumentsEntregados;
    }

    /**
     * Metodo que permite obtener los documentos no presentados para un un
     * tramite
     *
     * @param listaDtoTramitesConDocumentosNecesarios
     * @param listaDtoTramitesDeGestion
     * @return
     */
    public ArrayList<DtoProcedure> obtenerDocumentsNoPresentadosPorProcedure(ArrayList<DtoProcedure> listaDtoProceduresDeManagement)
    {

        boolean flag = false;

        //Comparo los documentos necesarios con los entregados , para una tramite de una gestion determinada
        for (int i = 0; i < listaDtoProceduresDeManagement.size(); i++)
        {
            DtoProcedure dtoProcedure = listaDtoProceduresDeManagement.get(i);
            //Recorro toda la lista de documentos necesario, buscando el no entregado
            for (int j = 0; j < dtoProcedure.getListaDocumentsNecesarios().size(); j++)
            {
                DtoDocumentType dtoDocumentNecesario = dtoProcedure.getListaDocumentsNecesarios().get(j);
                String nameDocNececesario = dtoDocumentNecesario.getName();

                flag = false;

                for (int k = 0; k < dtoProcedure.getListaDocumentsManagement().size() && !flag; k++)
                {

                    String nameDocSubmitted = dtoProcedure.getListaDocumentsManagement().get(k).getName();

                    if (nameDocNececesario.equals(nameDocSubmitted))
                    {
                        flag = true;
                    } else
                    {
                        flag = false;
                    }

                }
                if (!flag)
                {
                    DtoSubmittedDocument submittedDocument = new DtoSubmittedDocument();

                    if (dtoDocumentNecesario.getDueDays() != null)
                    {
                        submittedDocument.setDueDays(dtoDocumentNecesario.getDueDays());
                    }
                    submittedDocument.setName(dtoDocumentNecesario.getName());
                    submittedDocument.setDeliveredBy(dtoDocumentNecesario.getDeliveredBy());

                    dtoProcedure.getListaDocumentsNoPrecentados().add(submittedDocument);
                }
            }

        }

        return listaDtoProceduresDeManagement;
    }

    /**
     * Metodo que permite saber si la documentacion de un cliente fue entregada
     * en su totalidad
     *
     * @param listaDocumentoPresentados
     * @return True si la documentacion fue entregada, false en caso contrario
     */
    public boolean documentacionCompletaClient(DtoDeedManagement dtoDeedManagement)
    {

        boolean flag = true;

        ArrayList<DtoSubmittedDocument> listadDocumentsPresentados = null;

        for (int i = 0; i < dtoDeedManagement.getListaProceduresAsociados().size() && flag; i++)
        {
            DtoProcedure dtoProcedure = dtoDeedManagement.getListaProceduresAsociados().get(i);

            for (int j = 0; j < dtoProcedure.getListaDocumentsManagement().size() && flag; j++)
            {
                DtoSubmittedDocument dtoSubmittedDocument = dtoProcedure.getListaDocumentsManagement().get(j);
                boolean delivered = dtoSubmittedDocument.isDelivered();
                String client = dtoSubmittedDocument.getDeliveredBy();

                if (client.equals(BusinessConstants.DOCUMENTACIONClient))
                {
                    if (!delivered)
                    {
                        flag = false;
                    }
                }
            }
        }
        return flag;
    }

    public boolean documentacionCompletaExterna(DtoDeedManagement dtoDeedManagement)
    {

        boolean flag = true;

        ArrayList<DtoSubmittedDocument> listadDocumentsPresentados = null;

        for (int i = 0; i < dtoDeedManagement.getListaProceduresAsociados().size() && flag; i++)
        {
            DtoProcedure dtoProcedure = dtoDeedManagement.getListaProceduresAsociados().get(i);

            for (int j = 0; j < dtoProcedure.getListaDocumentsManagement().size() && flag; j++)
            {
                DtoSubmittedDocument dtoSubmittedDocument = dtoProcedure.getListaDocumentsManagement().get(j);
                boolean delivered = dtoSubmittedDocument.isDelivered();
                String client = dtoSubmittedDocument.getDeliveredBy();

                if (client.equals(BusinessConstants.DOCUMENTACION_ENTIDAD_EXTERNA))
                {
                    if (!delivered)
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
    public boolean iscompletaDocumentacion(DtoDeedManagement dtoDeedManagement)
    {
        boolean flag = true;

        ArrayList<DtoSubmittedDocument> listadDocumentsPresentados = null;

        for (int i = 0; i < dtoDeedManagement.getListaProceduresAsociados().size() && flag; i++)
        {
            DtoProcedure dtoProcedure = dtoDeedManagement.getListaProceduresAsociados().get(i);

            for (int j = 0; j < dtoProcedure.getListaDocumentsManagement().size() && flag; j++)
            {
                DtoSubmittedDocument dtoSubmittedDocument = dtoProcedure.getListaDocumentsManagement().get(j);
                boolean delivered = dtoSubmittedDocument.isDelivered();

                if (delivered)
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
                String nameStatus = null;
                //Busco  los estados de gestion

                List<ManagementStatus> listEstadosManagement = new ArrayList<>();
                int idManagementStatus = 0;

                listEstadosManagement = miJpaManagementStatus.findManagementStatusEntities();

                ManagementStatus statusManagement = null;

                for (int i = 0; i < listEstadosManagement.size(); i++)
                {
                    nameStatus = listEstadosManagement.get(i).getName();

                    statusManagement = listEstadosManagement.get(i);

                    if (nameStatus.equals(BusinessConstants.DOCUMENTACION_COMPLETA))
                    {
                        idManagementStatus = statusManagement.getIdManagementStatus();

                        break;
                    }

                }

                DtoManagementStatus dtoManagement = new DtoManagementStatus();
                dtoManagement.setIdManagementStatus(idManagementStatus);
                dtoManagement.setName(nameStatus);

                dtoDeedManagement.setStatus(dtoManagement);

                this.modificarManagementStatusDeDeed(dtoDeedManagement);

                this.registrarMovementHistory(dtoDeedManagement);

                this.registrarAudit(dtoManagement, ConstantesGui.DOCUMENTACIONEntry);
            }
            catch (ClassModifiedException ex)
            {
                Logger.getLogger(BusinessController.class.getName()).log(Level.SEVERE, null, ex);
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
    public DtoDeedManagement obtenerDocNecesarioEntregadosNoEntregadosDeManagement(DtoDeedManagement dtoManagement)
    {

        ArrayList<DtoProcedure> listaDtoProceduresDeManagement = new ArrayList<>();
        ArrayList<DtoProcedure> listaDtoProceduresConStatusDeDocumentacion = null;
        boolean flag = false;

        DtoProcedure dtoProcedure = null;

        //Cargo la lista de Tramites de la gestion y a cada tramite le asocio la gestion que le pertenece
        for (int i = 0; i < dtoManagement.getListaProceduresAsociados().size(); i++)
        {
            dtoProcedure = dtoManagement.getListaProceduresAsociados().get(i);
            dtoProcedure.getListaDocumentsNoPrecentados().clear();
            dtoProcedure.getListaDocumentsManagement().clear();
            dtoProcedure.getListaDocumentsNecesarios().clear();
            dtoProcedure.setDeedManagement(dtoManagement);
            listaDtoProceduresDeManagement.add(dtoProcedure);
        }

        //Busco documentos necesarios para los tramites de la gestion, que obtuve
        //listaTramitesConSusDocumentosNecesarios Contiene el tramite, sus documentos y su gestion
        listaDtoProceduresConStatusDeDocumentacion = this.obtenerDocumentsNecesariosPorProcedure(listaDtoProceduresDeManagement);

        //Busco los documentos entregados de cada tramite,  perteneciente a una gestion
        listaDtoProceduresConStatusDeDocumentacion = this.obtenerDocumentsPresentadosPorProcedure(listaDtoProceduresDeManagement);

        //Busco los documentos no entregados de cada tramite, perteneciente a una gesion
        listaDtoProceduresConStatusDeDocumentacion = this.obtenerDocumentsNoPresentadosPorProcedure(listaDtoProceduresDeManagement);

        return dtoManagement;

    }

    /**
     * Metodo que permite registrar los documentos de un determinado tramite,
     * cuando estos son entregados en su totalidad, la gestion para al estado
     * Documentacion_Completa
     *
     * @param listaDtoDocumentoPresentados
     * @return Un Dtoflag, true si se registraron, no de lo contrario
     */
    public DtoFlag ingresarDocuments(ArrayList<DtoSubmittedDocument> listaDtoDocumentPresentados, DtoDeedManagement dtoDeedManagement) throws NonexistentEntityException, ClassModifiedException
    {

        ArrayList<SubmittedDocument> listaDocumentPresentados = new ArrayList<>();
        DtoFlag flag = new DtoFlag();
        flag.setFlag(false);

        //Paso de dto a objetos
        for (int i = 0; i < listaDtoDocumentPresentados.size(); i++)
        {
            DtoSubmittedDocument dtoSubmittedDocument = listaDtoDocumentPresentados.get(i);
            SubmittedDocument submittedDocument = new SubmittedDocument();

            submittedDocument.setAtributos(dtoSubmittedDocument);

            listaDocumentPresentados.add(submittedDocument);
        }

        //Persisto los objetos
        for (int i = 0; i < listaDocumentPresentados.size(); i++)
        {
            SubmittedDocument submittedDocument = listaDocumentPresentados.get(i);

            flag.setFlag(myJpaSubmittedDocument.create(submittedDocument));

            this.registrarAudit(submittedDocument, ConstantesGui.DOCUMENTACIONEntry);
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
    public DtoFlag modificarDocumentacion(ArrayList<DtoSubmittedDocument> listaDtoDocumentPresentados, DtoDeedManagement dtoDeedManagement) throws ClassModifiedException, NonexistentEntityException
    {

        ArrayList<SubmittedDocument> listaDocumentPresentados = new ArrayList<>();
        DtoFlag dtoResultado = new DtoFlag();
        dtoResultado.setFlag(false);

        //Paso de dto a objetos
        for (int i = 0; i < listaDtoDocumentPresentados.size(); i++)
        {
            DtoSubmittedDocument dtoSubmittedDocument = listaDtoDocumentPresentados.get(i);
            SubmittedDocument submittedDocument = new SubmittedDocument();

            submittedDocument.setAtributos(dtoSubmittedDocument);

            listaDocumentPresentados.add(submittedDocument);
        }

        //Persisto los objetos
        for (int i = 0; i < listaDocumentPresentados.size(); i++)
        {
            SubmittedDocument submittedDocument = listaDocumentPresentados.get(i);
            dtoResultado.setFlag(myJpaSubmittedDocument.edit(submittedDocument));

            this.registrarAudit(submittedDocument, ConstantesGui.DOCUMENTACIONDebt);
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
    public DtoFlag modificarDocumentacionEntidadesExternas(ArrayList<DtoSubmittedDocument> listaDtoDocumentPresentados, DtoDeedManagement dtoDeedManagement) throws NonexistentEntityException, ClassModifiedException
    {

        ArrayList<SubmittedDocument> listaDocumentPresentados = new ArrayList<>();
        DtoFlag dtoResultado = new DtoFlag();
        dtoResultado.setFlag(false);

        //Paso de dto a objetos
        for (int i = 0; i < listaDtoDocumentPresentados.size(); i++)
        {
            DtoSubmittedDocument dtoSubmittedDocument = listaDtoDocumentPresentados.get(i);
            SubmittedDocument submittedDocument = new SubmittedDocument();

            submittedDocument.setAtributos(dtoSubmittedDocument);

            listaDocumentPresentados.add(submittedDocument);
        }

        //Persisto los objetos
        for (int i = 0; i < listaDocumentPresentados.size(); i++)
        {
            SubmittedDocument submittedDocument = listaDocumentPresentados.get(i);

            dtoResultado.setFlag(myJpaSubmittedDocument.edit(submittedDocument));

            this.registrarAudit(submittedDocument, ConstantesGui.DOCUMENTACION_ENTIDAD_EXTERNA);
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
    public DtoFlag modificarDocumentacionReingreso(ArrayList<DtoSubmittedDocument> listaDtoDocumentPresentados, DtoDeedManagement dtoDeedManagement) throws NonexistentEntityException, ClassModifiedException
    {

        ArrayList<SubmittedDocument> listaDocumentPresentados = new ArrayList<>();
        DtoFlag dtoResultado = new DtoFlag();
        dtoResultado.setFlag(false);

        //Paso de dto a objetos
        for (int i = 0; i < listaDtoDocumentPresentados.size(); i++)
        {
            DtoSubmittedDocument dtoSubmittedDocument = listaDtoDocumentPresentados.get(i);
            SubmittedDocument submittedDocument = new SubmittedDocument();

            submittedDocument.setAtributos(dtoSubmittedDocument);

            listaDocumentPresentados.add(submittedDocument);
        }

        //Persisto los objetos
        for (int i = 0; i < listaDocumentPresentados.size(); i++)
        {
            SubmittedDocument submittedDocument = listaDocumentPresentados.get(i);
            dtoResultado.setFlag(myJpaSubmittedDocument.edit(submittedDocument));

            this.registrarAudit(submittedDocument, ConstantesGui.REGISTRAR_REINGRESO);
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
    public List<DtoSubmittedDocument> consultarDocumentsProximosVencer()
    {
        List<DtoSubmittedDocument> listaDocumentsPorVencer = new ArrayList<>();
        List<SubmittedDocument> listaDocuments = new ArrayList<>();

        listaDocuments = myJpaSubmittedDocument.findDocumentsPorVencer();

        if (!listaDocuments.isEmpty())
        {
            for (Iterator<SubmittedDocument> it = listaDocuments.iterator(); it.hasNext();)
            {
                SubmittedDocument submittedDocument = it.next();

                DtoSubmittedDocument dtoSubmittedDocument = submittedDocument.getDto();

                // la gestion
                DeedManagement management = miJpaDeedManagement.findDeedManagementPorNumber(submittedDocument.getFkIdProcedure().getFkIdManagement().getNumber());

                DtoDeedManagement dtoManagement = management.getDto();

                // el tramite de la gestion
                Procedure procedure = myJpaProcedure.findProcedure(submittedDocument.getFkIdProcedure().getIdProcedure());
                DtoProcedure dtoProcedure = procedure.getDto();

                // el presupuesto del tramite
                Budget budget = myJpaBudget.findBudget(procedure.getFkIdBudget().getIdBudget());
                DtoBudget dtoBudget = budget.getDto();

                // el cliente de referencia del preupuesto
                Person clientReferencia = myJpaPerson.findPersonPorId(budget.getFkIdPerson().getPersonId());
                DtoPerson dtoPerson = clientReferencia.getDto();

                dtoBudget.setPerson(dtoPerson);
                dtoManagement.setClientReferencia(dtoPerson);
                dtoProcedure.setDeedManagement(dtoManagement);
                dtoProcedure.setBudget(dtoBudget);
                dtoSubmittedDocument.setFkProcedure(dtoProcedure);

                listaDocumentsPorVencer.add(dtoSubmittedDocument);
            }
        }

        return listaDocumentsPorVencer;
    }

    /**
     * Metodo que permite ingresar tota la documentacion correspondiente a una determinada
     * gestion, cuando esta es iniciada.
     * para luego se modificada
     *
     * @param dtoGestionDeEscritura
     * @throws NonexistentEntityException
     * @throws ClassModifiedException
     */
    public void ingresarDocumentacion(DtoDeedManagement dtoDeedManagement) throws NonexistentEntityException, ClassModifiedException
    {

        ArrayList<DtoSubmittedDocument> listaDocumentsExternos = new ArrayList<>();

        dtoDeedManagement = this.obtenerDocNecesarioEntregadosNoEntregadosDeManagement(dtoDeedManagement);

        DtoSubmittedDocument dtoDocument = null;
        DtoSubmittedDocument dtoSubmittedDocument = null;

        for (int i = 0; i < dtoDeedManagement.getListaProceduresAsociados().size(); i++)
        {
            DtoProcedure dtoProcedure = dtoDeedManagement.getListaProceduresAsociados().get(i);

            for (int j = 0; j < dtoProcedure.getListaDocumentsNecesarios().size(); j++)
            {
                dtoSubmittedDocument = dtoProcedure.getListaDocumentsNoPrecentados().get(j);

                dtoDocument = new DtoSubmittedDocument();

                dtoDocument.setFkProcedure(dtoProcedure);

                dtoDocument.setName(dtoSubmittedDocument.getName());

                dtoDocument.setDeliveredBy(dtoSubmittedDocument.getDeliveredBy());

                dtoDocument.setReentered(false);

                dtoDocument.setDelivered(false);

                listaDocumentsExternos.add(dtoDocument);

            }

        }

        BusinessController.getInstancia().ingresarDocuments(listaDocumentsExternos, dtoDeedManagement);

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
    public Boolean crearDeed(DtoDeed miDtoDeed) throws PreexistingEntityException, ClassModifiedException, ClassEliminatedException
    {
        Boolean creada = Boolean.FALSE;
        Deed miDeed = new Deed();

        miDeed.setAtributos(miDtoDeed);

        if (!existeDeed(miDtoDeed))
        {
            for (Iterator<DtoFolio> it = miDtoDeed.getFolios().iterator(); it.hasNext();)
            {
                DtoFolio dtoFolio = it.next();
                Folio miFolio = new Folio();
                miFolio.setAtributos(dtoFolio);

                myJpaFolio.modificarFolio(miFolio);

            }
            creada = myJpaDeed.create(miDeed);

            if (creada)
            {
                this.registrarAudit(miDeed, ConstantesGui.PREPARARDeed);
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
    private Boolean existeDeed(DtoDeed miDtoDeed)
    {
        Boolean existe = false;

        List<Deed> escriturasConMismoNumber = myJpaDeed.findDeedByNumber(miDtoDeed.getNumber());

        if (escriturasConMismoNumber != null && !escriturasConMismoNumber.isEmpty())
        {
            for (Iterator<Deed> it = escriturasConMismoNumber.iterator(); it.hasNext();)
            {
                Deed deed = it.next();

                if (deed.getFolioList().get(0).getFkIdNotaryPerson().getNotaryRegistrationNumber().intValue()
                        == miDtoDeed.getFolios().get(0).getPersonNotary().getNotaryRegistrationNumber().intValue())
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
    public List<DtoDeed> searchEscriturasPorRecord(DtoPerson miNotary)
    {
        List<DtoDeed> dtosEscriturasEncontradas = new ArrayList<>();
        List<Deed> escrituras = new ArrayList<>();
        List<DeedManagement> gestionesNotary = new ArrayList<>();
        Person miPersonNotary = new Person();

        miPersonNotary.setNotaryRegistrationNumber(miNotary.getNotaryRegistrationNumber());

        miPersonNotary = myJpaPerson.findPersonNotary(miPersonNotary);

        if (miPersonNotary.getPersonId() != null)
        {

            gestionesNotary = miPersonNotary.getDeedManagementList();
            for (Iterator<DeedManagement> it = gestionesNotary.iterator(); it.hasNext();)
            {
                DeedManagement deedManagement = it.next();

                if (deedManagement.getProcedureList() != null)
                {

                    for (Iterator<Procedure> it1 = deedManagement.getProcedureList().iterator(); it1.hasNext();)
                    {
                        Procedure procedure = it1.next();

                        if (procedure.getFkIdDeed() != null)
                        {
                            if (!escrituras.contains(procedure.getFkIdDeed()))
                            {
                                escrituras.add(procedure.getFkIdDeed());
                            }
                        }
                    }
                }
            }

            if (!escrituras.isEmpty())
            {
                for (Iterator<Deed> it = escrituras.iterator(); it.hasNext();)
                {
                    Deed deed = it.next();

                    DtoDeed miDtoDeed = deed.getDto();

                    dtosEscriturasEncontradas.add(miDtoDeed);
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
    public List<DtoDeed> searchEscriturasPorRecordFirmadas(DtoPerson miNotary)
    {
        List<DtoDeed> dtosEscriturasEncontradas = new ArrayList<>();
        List<Deed> escrituras = new ArrayList<>();
        List<DeedManagement> gestionesNotary = new ArrayList<>();
        Person miPersonNotary = new Person();

        miPersonNotary.setNotaryRegistrationNumber(miNotary.getNotaryRegistrationNumber());

        miPersonNotary = myJpaPerson.findPersonNotary(miPersonNotary);

        if (miPersonNotary.getPersonId() != null)
        {
            gestionesNotary = miPersonNotary.getDeedManagementList();

            for (Iterator<DeedManagement> it = gestionesNotary.iterator(); it.hasNext();)
            {
                DeedManagement deedManagement = it.next();

                if (deedManagement.getProcedureList() != null)
                {
                    for (Iterator<Procedure> it1 = deedManagement.getProcedureList().iterator(); it1.hasNext();)
                    {
                        Procedure procedure = it1.next();

                        if (procedure.getFkIdDeed() != null)
                        {
                            if (!escrituras.contains(procedure.getFkIdDeed()))
                            {
                                Deed miDeed = myJpaDeed.findDeedById(procedure.getFkIdDeed().getIdDeed());

                                if (miDeed.getStatus().equals(BusinessConstants.DeedFIRMADA))
                                {
                                    escrituras.add(miDeed);
                                }
                            }
                        }

                    }
                }

            }

            if (!escrituras.isEmpty())
            {
                for (Iterator<Deed> it = escrituras.iterator(); it.hasNext();)
                {
                    Deed deed = it.next();

                    DtoDeed miDtoDeed = deed.getDto();

                    dtosEscriturasEncontradas.add(miDtoDeed);
                }
            }
        }

        return dtosEscriturasEncontradas;
    }

    public List<DtoDeed> searchEscriturasPorRecordFirmadasSinArchivo(DtoPerson miNotary)
    {
        List<DtoDeed> dtosEscriturasEncontradas = new ArrayList<>();
        List<Deed> escrituras = new ArrayList<>();
        List<DeedManagement> gestionesNotary = new ArrayList<>();
        Person miPersonNotary = new Person();

        miPersonNotary.setNotaryRegistrationNumber(miNotary.getNotaryRegistrationNumber());

        miPersonNotary = myJpaPerson.findPersonNotary(miPersonNotary);

        if (miPersonNotary.getPersonId() != null)
        {
            gestionesNotary = miPersonNotary.getDeedManagementList();

            for (Iterator<DeedManagement> it = gestionesNotary.iterator(); it.hasNext();)
            {
                DeedManagement deedManagement = it.next();

                if (!deedManagement.getFkIdManagementStatus().getName().equals(BusinessConstants.ManagementARCHIVADA))
                {
                    if (deedManagement.getProcedureList() != null)
                    {
                        for (Iterator<Procedure> it1 = deedManagement.getProcedureList().iterator(); it1.hasNext();)
                        {
                            Procedure procedure = it1.next();

                            if (procedure.getFkIdDeed() != null)
                            {
                                if (!escrituras.contains(procedure.getFkIdDeed()))
                                {
                                    Deed miDeed = myJpaDeed.findDeedById(procedure.getFkIdDeed().getIdDeed());

                                    if (miDeed.getStatus().equals(BusinessConstants.DeedFIRMADA))
                                    {
                                        escrituras.add(miDeed);
                                    }
                                }
                            }

                        }
                    }
                }
            }

            if (!escrituras.isEmpty())
            {
                for (Iterator<Deed> it = escrituras.iterator(); it.hasNext();)
                {
                    Deed deed = it.next();

                    DtoDeed miDtoDeed = deed.getDto();

                    dtosEscriturasEncontradas.add(miDtoDeed);
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
    public List<DtoDeed> searchEscriturasPorRecordFirmadasInscriptas(DtoPerson miNotary)
    {
        List<DtoDeed> dtosEscriturasEncontradas = new ArrayList<>();
        List<Deed> escrituras = new ArrayList<>();
        List<DeedManagement> gestionesNotary = new ArrayList<>();
        Person miPersonNotary = new Person();

        miPersonNotary.setNotaryRegistrationNumber(miNotary.getNotaryRegistrationNumber());

        miPersonNotary = myJpaPerson.findPersonNotary(miPersonNotary);

        if (miPersonNotary.getPersonId() != null)
        {

            gestionesNotary = miPersonNotary.getDeedManagementList();
            for (Iterator<DeedManagement> it = gestionesNotary.iterator(); it.hasNext();)
            {
                DeedManagement deedManagement = it.next();
                ManagementStatus miStatus = miJpaManagementStatus.findManagementStatus(deedManagement.getIdManagement());

                if (!miStatus.equals(BusinessConstants.ManagementARCHIVADA))
                {
                    if (deedManagement.getProcedureList() != null)
                    {
                        for (Iterator<Procedure> it1 = deedManagement.getProcedureList().iterator(); it1.hasNext();)
                        {
                            Procedure procedure = it1.next();

                            if (procedure.getFkIdDeed() != null)
                            {
                                if (!escrituras.contains(procedure.getFkIdDeed()))
                                {
                                    Deed miDeed = myJpaDeed.findDeedById(procedure.getFkIdDeed().getIdDeed());

                                    if (miDeed.getStatus().equals(BusinessConstants.DeedFIRMADA)
                                            || miDeed.getStatus().equals(BusinessConstants.DeedRegistered))
                                    {
                                        escrituras.add(miDeed);
                                    }
                                }
                            }
                        }
                    }
                }
            }

            if (!escrituras.isEmpty())
            {
                for (Iterator<Deed> it = escrituras.iterator(); it.hasNext();)
                {
                    Deed deed = it.next();

                    DtoDeed miDtoDeed = deed.getDto();

                    dtosEscriturasEncontradas.add(miDtoDeed);
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
    public List<DtoDeed> searchDeedPorNumber(DtoDeed miDtoDeed)
    {
        List<DtoDeed> escriturasEncontradas = new ArrayList<>();
        List<Deed> escrituras = null;

        escrituras = myJpaDeed.findDeedByNumber(miDtoDeed.getNumber());

        if (escrituras != null && !escrituras.isEmpty())
        {
            for (Iterator<Deed> it = escrituras.iterator(); it.hasNext();)
            {
                Deed deed = it.next();
                DtoDeed miDeed = deed.getDto();

                escriturasEncontradas.add(miDeed);
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
    public List<DtoDeed> searchDeedPorNumberFirmada(DtoDeed miDtoDeed)
    {
        List<DtoDeed> escriturasEncontradas = new ArrayList<>();
        List<Deed> escrituras = null;

        escrituras = myJpaDeed.findDeedByNumber(miDtoDeed.getNumber());

        if (escrituras != null && !escrituras.isEmpty())
        {
            for (Iterator<Deed> it = escrituras.iterator(); it.hasNext();)
            {
                Deed deed = it.next();

                DtoDeed miDeed = deed.getDto();

                if (deed.getStatus().equals(BusinessConstants.DeedFIRMADA))
                {
                    escriturasEncontradas.add(miDeed);
                }
            }
        }

        return escriturasEncontradas;
    }

    public List<DtoDeed> searchDeedPorNumberFirmadaSinArchivo(DtoDeed miDtoDeed)
    {
        List<DtoDeed> escriturasEncontradas = new ArrayList<>();
        List<Deed> escrituras = null;

        escrituras = myJpaDeed.findDeedByNumber(miDtoDeed.getNumber());

        if (escrituras != null && !escrituras.isEmpty())
        {
            for (Iterator<Deed> it = escrituras.iterator(); it.hasNext();)
            {
                Deed deed = it.next();

                Procedure miProcedure = deed.getProcedureList().get(0);
                DeedManagement miManagement = miProcedure.getFkIdManagement();

                if (!miManagement.getFkIdManagementStatus().getName().equals(BusinessConstants.ManagementARCHIVADA))
                {
                    DtoDeed miDeed = deed.getDto();

                    if (deed.getStatus().equals(BusinessConstants.DeedFIRMADA))
                    {
                        escriturasEncontradas.add(miDeed);
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
    public List<DtoDeed> searchDeedPorNumberFirmadaRegistered(DtoDeed miDtoDeed)
    {
        List<DtoDeed> escriturasEncontradas = new ArrayList<>();
        List<Deed> escrituras = null;

        escrituras = myJpaDeed.findDeedByNumber(miDtoDeed.getNumber());

        if (escrituras != null && !escrituras.isEmpty())
        {
            for (Iterator<Deed> it = escrituras.iterator(); it.hasNext();)
            {
                Deed deed = it.next();
                DtoDeed miDeed = deed.getDto();

                if (deed.getStatus().equals(BusinessConstants.DeedFIRMADA)
                        || deed.getStatus().equals(BusinessConstants.DeedRegistered))
                {
                    escriturasEncontradas.add(miDeed);
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
    public DtoDeed searchDeed(DtoDeed miDtoDeed)
    {
        DtoDeed deedEncontrada = null;
        Deed deed = null;

        deed = myJpaDeed.findDeedById(miDtoDeed.getIdDeed());

        if (deed != null)
        {
            deedEncontrada = deed.getDto();
        }

        return deedEncontrada;
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
    public Boolean modificarDeed(DtoDeed miDtoDeed) throws ClassEliminatedException, ClassModifiedException
    {
        Boolean modificada = false;
        Deed deedEncontrada = myJpaDeed.findDeedById(miDtoDeed.getIdDeed());
        List<Procedure> proceduresDeedVieja = new ArrayList<>();

        if (deedEncontrada != null)
        {

            for (Iterator<Procedure> it = deedEncontrada.getProcedureList().iterator(); it.hasNext();)
            {
                Procedure miProcedure = it.next();
                proceduresDeedVieja.add(miProcedure);
            }

            deedEncontrada.setAtributos(miDtoDeed);

            for (Iterator<DtoFolio> it = miDtoDeed.getFolios().iterator(); it.hasNext();)
            {
                DtoFolio dtoFolio = it.next();
                Folio miFolio = new Folio();
                miFolio.setAtributos(dtoFolio);

                myJpaFolio.modificarFolio(miFolio);

                this.registrarAudit(miFolio, ConstantesGui.MODIFICARDeed);

            }

            modificada = myJpaDeed.modificarDeed(deedEncontrada);

            if (modificada == true)
            {
                this.registrarAudit(deedEncontrada, ConstantesGui.MODIFICARDeed);
            }
            for (Iterator<Procedure> it = proceduresDeedVieja.iterator(); it.hasNext();)
            {
                Procedure procedure = it.next();

                if (!deedEncontrada.getProcedureList().contains(procedure))
                {
                    procedure = myJpaProcedure.encontrarProcedure(procedure.getIdProcedure());
                    procedure.setFkIdDeed(null);
                    Boolean modificado = myJpaProcedure.editProcedure(procedure);

                    if (modificado == true)
                    {
                        this.registrarAudit(procedure, ConstantesGui.MODIFICARDeed);
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
    public DtoProcedure searchProcedure(DtoProcedure miDtoProcedure)
    {

        DtoProcedure procedureEncontrado = null;
        Procedure procedure = myJpaProcedure.encontrarProcedure(miDtoProcedure.getIdProcedure());

        if (procedure != null)
        {
            procedureEncontrado = procedure.getDto();
        }

        return procedureEncontrado;
    }

    /**
     * Busca el Escribano asociado a la Escritura indicada.
     *
     * @param miDtoEscritura, numero de la Escritura a buscar.
     * @return DtoPersona con el Escribano asociado.
     */
    public DtoPerson obtenerNotaryDeed(DtoDeed miDtoDeed)
    {
        DtoPerson miPersonNotary = null;
        Deed miDeed = myJpaDeed.findDeedById(miDtoDeed.getIdDeed());
        List<Procedure> procedures = miDeed.getProcedureList();

        if (procedures != null)
        {
            Person miPerson = procedures.get(0).getFkIdManagement().getFkIdNotaryPerson();

            miPersonNotary = miPerson.getDto();
        }

        return miPersonNotary;
    }

    /**
     * Busca las Escrituras de una Gestion en particular.
     *
     * @param miDtoGestion, datos de la Gestion a buscar.
     * @return Lista de DtoEscritura, de las Escrituras asociadas a la Gestion
     * indicada.
     */
    public List<DtoDeed> searchEscriturasManagement(DtoDeedManagement miDtoManagement)
    {
        List<DtoDeed> listaDtoEscrituras = new ArrayList<>();
        List<Procedure> proceduresManagement = null;
        DeedManagement miManagement = null;

        miManagement = miJpaDeedManagement.findDeedManagementPorNumber(miDtoManagement.getNumber());

        if (miManagement != null && miManagement.getNumber() != ConstantesPersistencia.VersionINICIAL)
        {
            proceduresManagement = miManagement.getProcedureList();

            if (proceduresManagement != null && !proceduresManagement.isEmpty())
            {
                for (Iterator<Procedure> it = proceduresManagement.iterator(); it.hasNext();)
                {
                    Procedure procedure = it.next();
                    Procedure miProcedure = myJpaProcedure.encontrarProcedure(procedure.getIdProcedure());

                    if (miProcedure.getFkIdDeed() != null)
                    {
                        Deed miDeed = myJpaDeed.findDeedById(miProcedure.getFkIdDeed().getIdDeed());

                        if (miDeed != null)
                        {
                            listaDtoEscrituras.add(miDeed.getDto());
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
    public Boolean verificarIsRegisteredDeed(DtoDeed miDtoDeed)
    {
        Boolean isRegistered = false;
        Deed miDeed = myJpaDeed.findDeedById(miDtoDeed.getIdDeed());

        if (miDeed != null)
        {
            for (Iterator<Procedure> it = miDeed.getProcedureList().iterator(); it.hasNext();)
            {
                Procedure procedure = it.next();

                if (procedure.getFkIdProcedureType().getIsRegistered())
                {
                    isRegistered = true;
                    break;
                }
            }
        }

        return isRegistered;
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
    public Boolean crearTestimony(DtoTestimony miDtoTestimony, List<DtoCopy> listaDtoCopies)
    {
        Boolean creado = false;
        List<Copy> copies = new ArrayList<>();

        for (Iterator<DtoCopy> it = listaDtoCopies.iterator(); it.hasNext();)
        {
            DtoCopy dtoCopy = it.next();
            Copy miCopy = new Copy();
            miCopy.setAtributos(dtoCopy);

            int idCopy = myJpaCopy.crearCopy(miCopy);

            if (idCopy != -1)
            {
                copies.add(miCopy);
            }
        }

        if (!copies.isEmpty())
        {
            Testimony miTestimony = new Testimony();
            miTestimony.setAtributos(miDtoTestimony);
            miTestimony.setCopyList(copies);

            creado = myJpaTestimony.create(miTestimony);

            if (creado)
            {
                this.registrarAudit(miTestimony, ConstantesGui.GENERARTestimony);
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
    public List<DtoTestimony> obtenerTestimoniosDeed(DtoDeed miDtoDeed)
    {
        List<DtoTestimony> listaDtoTestimonios = new ArrayList<>();
        List<Testimony> listaTestimonios = null;

        listaTestimonios = myJpaTestimony.findTestimoniosDeed(miDtoDeed.getIdDeed());

        if (listaTestimonios != null && !listaTestimonios.isEmpty())
        {

            for (Iterator<Testimony> it = listaTestimonios.iterator(); it.hasNext();)
            {
                Testimony testimony = it.next();
                DtoTestimony miDtoTestimony = testimony.getDto();
                listaDtoTestimonios.add(miDtoTestimony);
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
    public List<DtoTestimonyMovement> obtenerMovimientosTestimony(DtoTestimony miDtoTestimony)
    {
        List<DtoTestimonyMovement> listaDtoMovementTestimonios = new ArrayList<>();
        List<TestimonyMovement> listaMovimientos = null;

        listaMovimientos = myJpaTestimonyMovement.searchMovimientosPorTestimony(miDtoTestimony.getIdTestimony());

        if (listaMovimientos != null && !listaMovimientos.isEmpty())
        {
            for (Iterator<TestimonyMovement> it = listaMovimientos.iterator(); it.hasNext();)
            {
                TestimonyMovement testimonyMovement = it.next();
                DtoTestimonyMovement miDtoTestimonyMovement = testimonyMovement.getDto();

                listaDtoMovementTestimonios.add(miDtoTestimonyMovement);
            }
        }

        return listaDtoMovementTestimonios;
    }

    /**
     * Busca las Copias de un Testimonio en particular.
     *
     * @param miDtoTestimonio, id del Testimonio a buscar.
     * @return Lista de DtoCopia con las Copias encontradas.
     */
    public List<DtoCopy> obtenerCopiesTestimony(DtoTestimony miDtoTestimony)
    {
        List<DtoCopy> listaDtoCopies = new ArrayList<>();
        List<Copy> listaCopies = null;

        listaCopies = myJpaCopy.searchCopiesTestimony(miDtoTestimony.getIdTestimony());

        if (listaCopies != null && !listaCopies.isEmpty())
        {
            for (Iterator<Copy> it = listaCopies.iterator(); it.hasNext();)
            {
                Copy copy = it.next();
                DtoCopy miDtoCopy = copy.getDto();

                listaDtoCopies.add(miDtoCopy);
            }
        }

        return listaDtoCopies;
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
    public Boolean modificarCopiesTestimony(List<DtoCopy> listaDtoCopies, DtoTestimony miDtoTestimony) throws ClassModifiedException, ClassEliminatedException
    {
        Boolean modificada = false;

        List<Copy> listaCopies = null;

        listaCopies = myJpaCopy.searchCopiesTestimony(miDtoTestimony.getIdTestimony());

        if (listaCopies != null && !listaCopies.isEmpty())
        {
            for (Iterator<Copy> it = listaCopies.iterator(); it.hasNext();)
            {
                Copy copy = it.next();

                for (Iterator<DtoCopy> it1 = listaDtoCopies.iterator(); it1.hasNext();)
                {
                    DtoCopy dtoCopy = it1.next();

                    if (dtoCopy.getIdCopy().intValue() == copy.getIdCopy().intValue())
                    {
                        copy.setAtributos(dtoCopy);
                        modificada = myJpaCopy.modificarCopy(copy);
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
    public Boolean verificarTestimonyIngresadoParaInscribir(DtoDeed miDtoDeed)
    {
        Boolean estaIngresado = false;
        Testimony miTestimony = null;
        List<DtoTestimonyMovement> movimientos = null;

        List<Testimony> misTestimonios = myJpaTestimony.findTestimoniosDeed(miDtoDeed.getIdDeed());

        miTestimony = misTestimonios.get(misTestimonios.size() - 1);

        movimientos = obtenerMovimientosTestimony(miTestimony.getDto());

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
    public Boolean crearTestimonyMovement(DtoTestimonyMovement miDtoTestimonyMovement) throws ClassModifiedException, ClassEliminatedException
    {
        Boolean creado = false;
        Boolean modificado = true;
        Testimony miTestimony = null;

        miTestimony = myJpaTestimony.findTestimonyById(miDtoTestimonyMovement.getTestimony().getIdTestimony());

        if (miTestimony != null)
        {
            miTestimony.setFlagged(miDtoTestimonyMovement.getTestimony().isFlagged());

            modificado = myJpaTestimony.modificarTestimony(miTestimony);

            if (modificado)
            {
                TestimonyMovement miTestimonyMovement = new TestimonyMovement();
                miTestimonyMovement.setAtributos(miDtoTestimonyMovement);
                creado = myJpaTestimonyMovement.create(miTestimonyMovement);

                if (creado)
                {
                    this.registrarAudit(miTestimonyMovement, ConstantesGui.INGRESARPARARegistration);
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
    public DtoTestimonyMovement searchTestimonyMovement(DtoTestimonyMovement miDtoTestimonyMovement)
    {
        DtoTestimonyMovement testimonyMovementEncontrado = null;
        TestimonyMovement testimonyMovement;

        testimonyMovement = myJpaTestimonyMovement.findMovementById(miDtoTestimonyMovement.getIdTestimonyMovement());

        if (testimonyMovement != null)
        {
            testimonyMovementEncontrado = testimonyMovement.getDto();
        }

        return testimonyMovementEncontrado;
    }

    /**
     * Modifica los datos de un MovimientoTestimonio en particular.
     *
     * @param miDtoMovimientoTestimonio, datos a modificar.
     * @return TRUE si se modifico, FALSE de lo contrario.
     * @throws ClassEliminatedException
     * @throws ClassModifiedException
     */
    public Boolean modificarTestimonyMovement(DtoTestimonyMovement miDtoTestimonyMovement) throws ClassEliminatedException, ClassModifiedException
    {
        Boolean modificado = true;
        TestimonyMovement testimonyMovement;

        testimonyMovement = myJpaTestimonyMovement.findMovementById(miDtoTestimonyMovement.getIdTestimonyMovement());

        if (testimonyMovement != null)
        {
            testimonyMovement.setAtributos(miDtoTestimonyMovement);
            modificado = myJpaTestimonyMovement.edit(testimonyMovement);

            if (modificado)
            {
                this.registrarAudit(testimonyMovement, ConstantesGui.REGISTRAR_REINGRESO);
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
    public Boolean modificarTestimonyMovementRegistration(DtoTestimonyMovement miDtoTestimonyMovement, DtoDeed miDtoDeed) throws ClassEliminatedException, ClassModifiedException
    {
        Boolean modificado = true;
        TestimonyMovement testimonyMovement;

        testimonyMovement = myJpaTestimonyMovement.findMovementById(miDtoTestimonyMovement.getIdTestimonyMovement());

        if (testimonyMovement != null)
        {
            testimonyMovement.setAtributos(miDtoTestimonyMovement);
            modificado = myJpaTestimonyMovement.edit(testimonyMovement);

            if (modificado)
            {
                this.registrarAudit(testimonyMovement, ConstantesGui.REGISTRARRegistration);

                Testimony miTestimony = myJpaTestimony.findTestimonyById(miDtoTestimonyMovement.getTestimony().getIdTestimony());

                Deed miDeed = myJpaDeed.findDeedById(miTestimony.getFkIdDeed().getIdDeed());
                miDeed.setRegistrationEntryNumber(miDtoDeed.getRegistrationEntryNumber());
                miDeed.setDateRegistration(miDtoDeed.getDateRegistration());
                miDeed.setStatus(miDtoDeed.getStatus());

                modificado = myJpaDeed.modificarDeedSimple(miDeed);

                if (modificado)
                {
                    this.registrarAudit(miDeed, ConstantesGui.REGISTRARRegistration);
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
    public DtoFolioType searchFolioType(DtoFolioType dtoFolioType)
    {
        DtoFolioType miFolioType = null;
        FolioType folioTypeEncontrado = null;
        List<FolioType> listaTypeDeFolios = null;

        try
        {
            FolioTypeJpaController mitipoDeFolioJpaController = (FolioTypeJpaController) this.getMiAdministradorJpa().obtenerJpa(FolioTypeJpaController.class.getName());

            listaTypeDeFolios = mitipoDeFolioJpaController.findFolioTypeEntities();

            for (Iterator<FolioType> it = listaTypeDeFolios.iterator(); it.hasNext();)
            {
                FolioType folioType = it.next();

                if (folioType.getName().contains(dtoFolioType.getName()))
                {
                    folioTypeEncontrado = folioType;

                    miFolioType = folioTypeEncontrado.getDto();

                    break;
                }
            }
        }
        catch (NonexistentJpaException e)
        {
            e.printStackTrace();
        }
        catch (PersistenceException e)
        {
            e.printStackTrace();
        }
        return miFolioType;
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

        List<Folio> folios = myJpaFolio.findFoliosRecordYear(desde.getPersonNotary().getNotaryRegistrationNumber(), desde.getYear());

        if (!folios.isEmpty())
        {

            for (Iterator<Folio> it = folios.iterator(); it.hasNext();)
            {
                Folio folio = it.next();
                if ((folio.getNumber() == desde.getNumber()) || folio.getNumber() == hasta.getNumber())
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
    public Boolean registrarEntryNuevosFolios(DtoFolio desde, DtoFolio hasta)
    {
        Boolean resultado = Boolean.FALSE;

        try
        {
            DtoPerson miDtoPersonNotary = this.searchPersonTypeIdentificationNumber(desde.getPersonNotary());
            Person notary = new Person();
            notary.setAtributos(miDtoPersonNotary);

            int j = hasta.getNumber();

            for (int i = desde.getNumber(); i <= j; i++)
            {

                Folio nuevoFolio = new Folio();

                desde.setNumber(i);

                nuevoFolio.setAtributos(desde);

                FolioType protocoloPrincipal = new FolioType();

                try
                {
                    protocoloPrincipal.setAtributos(desde.getTiposDeFolio());
                }
                catch (DtoInvalidoException ex)
                {
                    Logger.getLogger(BusinessController.class.getName()).log(Level.SEVERE, null, ex);
                }

                nuevoFolio.setFkIdNotaryPerson(notary);
                nuevoFolio.setFkIdFolioType(protocoloPrincipal);

                myJpaFolio.create(nuevoFolio);

                this.registrarAudit(nuevoFolio, ConstantesGui.INGRESAR_NUEVOS_FOLIOS);
            }

            resultado = Boolean.TRUE;
        }
        catch (PersistenceException e)
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
    public List<DtoFolio> obtenerListaFolios(DtoFolio dtoDatosRecordYear)
    {
        List<DtoFolio> dtoListaFolios = new ArrayList<>();
        List<Folio> listaFolios = null;

        try
        {
            FolioJpaController miFolioJpaController;
            try
            {
                miFolioJpaController = (FolioJpaController) this.getMiAdministradorJpa().obtenerJpa(FolioJpaController.class.getName());

                listaFolios = miFolioJpaController.findFoliosRecordYear(dtoDatosRecordYear.getPersonNotary().getNotaryRegistrationNumber(), dtoDatosRecordYear.getYear());
            }
            catch (NonexistentJpaException e)
            {
                e.printStackTrace();
            }

            for (Iterator<Folio> it = listaFolios.iterator(); it.hasNext();)
            {
                Folio folio = it.next();
                dtoListaFolios.add(folio.getDto());
            }

        }
        catch (PersistenceException e)
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

        if (myJpaFolio.modificarFoliosCompleto(folioModificado))
        {
            resultado = Boolean.TRUE;

            this.registrarAudit(folioModificado, ConstantesGui.MODIFICARFolio);
        }
        return resultado;
    }

    /**
     * Busca los folios disponibles para usar, de un Escribano en particular.
     *
     * @param numeroRegistro, numero del registro de Escribano
     * @return una lista de DtoFolio, de Folios disponibles.
     */
    public List<DtoFolio> searchFoliosDisponibles(Integer numberRecord)
    {
        List<DtoFolio> listaDtoFoliosDisponibles = new ArrayList<>();
        List<Folio> listaFolios = null;
        List<Folio> listaFoliosDisponibles = new ArrayList<>();

        listaFolios = myJpaFolio.findFolioEntities();

        if (listaFolios != null && !listaFolios.isEmpty())
        {
            for (Iterator<Folio> it = listaFolios.iterator(); it.hasNext();)
            {
                Folio folio = it.next();
                Integer numberRecordFolio = folio.getFkIdNotaryPerson().getNotaryRegistrationNumber();
                if (numberRecordFolio.intValue() == numberRecord.intValue())
                {
                    if (folio.getStatus().equals(BusinessConstants.StatusFolioNUEVOS))
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
    public Boolean darAltaPayment(DtoPayment miDtoPayment)
    {
        Boolean creado = false;
        int id = -1;

        Payment miPayment = new Payment();

        miPayment.setAtributos(miDtoPayment);

        id = myJpaPayment.create(miPayment);

        if (id != -1)
        {
            creado = true;

            this.registrarAudit(miPayment, ConstantesGui.MODIFICARBudget);
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
    public List<DtoPayment> searchPaymentsBudget(DtoBudget miDtoBudget)
    {
        List<DtoPayment> dtosPayments = new ArrayList<>();
        List<Payment> payments = null;

        payments = myJpaPayment.findPaymentsBudget(miDtoBudget.getIdBudget());

        if (payments != null && !payments.isEmpty())
        {
            for (int i = 0; i < payments.size(); i++)
            {
                Payment payment = payments.get(i);
                DtoPayment miDtoPayment = payment.getDto();

                dtosPayments.add(miDtoPayment);
            }
        }

        return dtosPayments;
    }

// </editor-fold>
// <editor-fold defaultstate="collapsed" desc="Administracion">
    // <editor-fold defaultstate="collapsed" desc="Usuarios">
    /**
     * Metodo que permite buscar un usuario
     *
     * @param dtoUsuario
     * @return El dtoUsuario para confirmar la busqueda
     */
    public DtoUser searchUser(DtoUser dtoUser) throws NonexistentJpaException
    {

        List<User> listaUsers = new ArrayList<>();
        User miUser = null;
        Integer idPerson = dtoUser.getPersons().getId();
        boolean flag = false;

        //Busco los usuario actuales
        listaUsers = myJpaUser.searchUsers();

        if (!listaUsers.isEmpty())
        {

            //Busco coincidencia de id_persona con fk_id_usuario
            for (int i = 0; i < listaUsers.size(); i++)
            {
                if (idPerson.intValue() == listaUsers.get(i).getFkIdPerson().getPersonId().intValue())
                {
                    flag = true;
                    miUser = listaUsers.get(i);
                    break;
                }

            }

            if (flag)
            {
                // Tener en cuenta que el dtoUsuario ya tiene la referencia a la persona.
                dtoUser.setPassword(miUser.getPassword());
                dtoUser.setStatus(miUser.getStatus());
                dtoUser.setIdUser(miUser.getIdUser());
                dtoUser.setName(miUser.getName());
                dtoUser.setType(miUser.getType());
                dtoUser.setPersons(miUser.getFkIdPerson().getDto());

            } else
            {
                dtoUser = null;
            }
        } else
        {
            //  La lista de usuario es vacia, error.
            dtoUser = null;
        }

        return dtoUser;
    }

    /**
     * Metodo que permite buscar los usuarios registrados en el sistema
     *
     * @return Una lista de DtoUsuario con el resultado de busqueda
     */
    public ArrayList<DtoUser> searchUsersDisponibles() throws NonexistentJpaException
    {

        List<User> listaUsers = null;
        ArrayList<DtoUser> listaDtoUsers = null;
        User miUser = new User();
        boolean flag = false;

        //Busco los usuario disponibles
        // listaUsuarios = UsuarioJpaController.getInstancia().buscarUsuarios();
        listaUsers = myJpaUser.searchUsers();

        if (!listaUsers.isEmpty())
        {
            listaDtoUsers = new ArrayList<DtoUser>();
            flag = true;

            for (int i = 0; i < listaUsers.size(); i++)
            {
                DtoUser miDtoUser = new DtoUser();
                miDtoUser = listaUsers.get(i).getDto();
                listaDtoUsers.add(miDtoUser);
            }
        } else
        {
            listaDtoUsers = null;
        }

        return listaDtoUsers;
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
        boolean value = true;
        int puntero = 0;
        if (j1.length != j2.length)
        {
            value = false;
        } else
        {
            while ((value) && (puntero < j1.length))
            {
                if (j1[puntero] != j2[puntero])
                {
                    value = false;
                }
                puntero++;
            }
        }
        dtoFlag.setFlag(value);
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
    public ArrayList<DtoAuditRecord> searchRegistrosAudit(DtoUser miDtoUser) throws NonexistentJpaException
    {

        User miUser = new User();
        miUser.setIdUser(miDtoUser.getIdUser());
        ArrayList<DtoAuditRecord> dtoListaAudit = new ArrayList<>();
        ArrayList<AuditRecord> listaAuditRecord = new ArrayList<>();

        listaAuditRecord = miJpaAudit.searchRecordAuditoriasUser(miUser);

        if (listaAuditRecord != null)
        {
            dtoListaAudit = new ArrayList<>();
            for (int i = 0; i < listaAuditRecord.size(); i++)
            {
                dtoListaAudit.add(listaAuditRecord.get(i).getDto());

            }
        }

        return dtoListaAudit;

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
    public boolean registrarAudit(Object miObjeto, String module)
    {
        boolean flag = false;

        AuditRecord audit = new AuditRecord();
        audit.setOperationDetail(miObjeto.toString());
        audit.setFkIdUser(AdministradorSession.getInstancia().getUserSession());
        audit.setModule(module);
        audit.setDate(new Date());

        if (miJpaAudit.create(audit))
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
    public Boolean darAltaNotary(DtoPerson dtoNuevoNotary) throws ClassModifiedException
    {
        Boolean resultado = Boolean.FALSE;

        Person nuevoNotary = new Person();
        nuevoNotary.setAtributos(dtoNuevoNotary);

        try
        {
            if (nuevoNotary.getNotaryRegistrationNumber() == null)
            {
                nuevoNotary.setNotaryRegistrationNumber(dtoNuevoNotary.getNotaryRegistrationNumber());
                if (myJpaPerson.registrarNotary(nuevoNotary))
                {
                    resultado = Boolean.TRUE;

                    this.registrarAudit(nuevoNotary, ConstantesGui.DARALTANotary);
                }
            } else
            {
                nuevoNotary.setNotaryRegistrationNumber(dtoNuevoNotary.getNotaryRegistrationNumber());
                if (myJpaPerson.registrarNotary(nuevoNotary))
                {
                    resultado = Boolean.TRUE;

                    this.registrarAudit(nuevoNotary, ConstantesGui.MODIFICARNotary);
                }
            }
        }
        catch (NonexistentEntityException ex)
        {
            Logger.getLogger(BusinessController.class.getName()).log(Level.SEVERE, null, ex);
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
    public List<DtoPerson> obtenerListaEscribanosDisponibles()
    {
        List<DtoPerson> listaDtoEscribanos = new ArrayList<>();
        List<Person> listaPersons = new ArrayList<>();

        try
        {
            listaPersons = myJpaPerson.findPersons();
            listaDtoEscribanos = new ArrayList<>();

            for (Iterator<Person> it = listaPersons.iterator(); it.hasNext();)
            {
                Person unaPerson = it.next();

                if ((unaPerson.getNotaryRegistrationNumber() != null) && (unaPerson.getNotaryRegistrationNumber() != 0))
                {
                    listaDtoEscribanos.add(unaPerson.getDto());
                }
            }

        }
        catch (PersistenceException e)
        {
            Logger.getLogger(BusinessController.class.getName()).log(Level.SEVERE, null, e);
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
    public Boolean registrarSubstitutionNotary(DtoSubstitution detailSubstitution)
    {
        Boolean resultado = Boolean.FALSE;
        try
        {
            Substitution nuevaSubstitution = new Substitution();
            nuevaSubstitution.setAtributos(detailSubstitution);

            try
            {
                myJpaSubstitution.create(nuevaSubstitution);

                resultado = Boolean.TRUE;

                this.registrarAudit(nuevaSubstitution, ConstantesGui.REGISTRARSubstitution);
            }
            catch (PersistenceException e)
            {
                e.printStackTrace();
            }

        }
        catch (DtoInvalidoException ex)
        {
            Logger.getLogger(BusinessController.class.getName()).log(Level.SEVERE, null, ex);
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
    public List<DtoSubstitution> consultarSuplencias(DtoSubstitution dtoSuplenciasDesde)
    {
        List<DtoSubstitution> listaDtoSuplencias = new ArrayList<>();
        List<Substitution> listaSuplencias = new ArrayList<>();

        Substitution unaSubstitution = new Substitution();
        unaSubstitution.setDateStart(dtoSuplenciasDesde.getDateStart());
        unaSubstitution.setDateEnd(dtoSuplenciasDesde.getDateEnd());

        listaSuplencias = myJpaSubstitution.findSuplenciasPorYear(unaSubstitution);

        if (!listaSuplencias.isEmpty())
        {
            for (Iterator<Substitution> it = listaSuplencias.iterator(); it.hasNext();)
            {
                Substitution substitution = it.next();

                listaDtoSuplencias.add(substitution.getDto());
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
    public Boolean darAltaProcedureType(DtoProcedureType miDtoProcedureType, ArrayList<DtoDocumentType> listaDtoTypeDeDocumentsDocuments) throws ClassModifiedException, ClassEliminatedException, PreexistingEntityException, IllegalOrphanException, NonexistentEntityException
    {
        Boolean resultado = Boolean.FALSE;
        Integer idProcedureType = -1;
        Boolean existe;
        ProcedureType miProcedureType = new ProcedureType();

        existe = this.existeProcedureType(miDtoProcedureType.getName());

        if (!existe)
        {

            // Creo el tipo de tramite
            miProcedureType.setAtributos(miDtoProcedureType);

            try
            {
                // Lo persisto
                idProcedureType = myJpaProcedureType.create(miProcedureType);

                // Si se creo correctamente, creo una Plantilla de Tramite por cada Tipo de Documento de la lista.
                if (idProcedureType != -1)
                {
                    this.registrarAudit(miProcedureType, ConstantesGui.INGRESARNUEVOProcedureType);
                    resultado = true;

                    if (listaDtoTypeDeDocumentsDocuments != null && !listaDtoTypeDeDocumentsDocuments.isEmpty())
                    {

                        miProcedureType.setIdProcedureType(idProcedureType);

                        ProcedureTemplate miTemplate;

                        for (Iterator<DtoDocumentType> it = listaDtoTypeDeDocumentsDocuments.iterator(); it.hasNext();)
                        {
                            DtoDocumentType dtoDocumentType = it.next();

                            DocumentType miDocumentType = myJpaDocumentType.findDocumentType(dtoDocumentType.getIdDocumentType());

                            if (miDocumentType != null)
                            {

                                miTemplate = new ProcedureTemplate();

                                miTemplate.setDocumentType(miDocumentType);
                                miTemplate.setProcedureType(miProcedureType);

                                Boolean creada = myJpaProcedureTemplate.create(miTemplate);

                                this.registrarAudit(miTemplate, ConstantesGui.INGRESARNUEVAProcedureTemplate);

                                if (creada == false)
                                {
                                    resultado = false;
                                }

                            }

                        }
                    }
                }
            }
            catch (PersistenceException e)
            {
                e.printStackTrace();
            }
        } else
        {
            List<ProcedureType> typeDeProcedures = myJpaProcedureType.findProcedureType(miDtoProcedureType.getName());
            miProcedureType = typeDeProcedures.get(0);

            if (miProcedureType.getEnabled() == false)
            {
                miProcedureType.setEnabled(true);
                miProcedureType.setName(miDtoProcedureType.getName());
                miProcedureType.setIsArchived(miDtoProcedureType.isIsArchived());
                miProcedureType.setIsRegistered(miDtoProcedureType.isIsRegistered());
                miProcedureType.setAssociatesProperties(miDtoProcedureType.getAssociatesProperties());
                miProcedureType.setNotes(miDtoProcedureType.getNotes());
                // miTipoDeTramite.setVersion(miDtoTipoDeTramite.getVersion());

                resultado = myJpaProcedureType.edit(miProcedureType);

                this.registrarAudit(miProcedureType, ConstantesGui.MODIFICARProcedureType);
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
    private Boolean existeProcedureType(String nameTypeProcedure)
    {
        Boolean resultado = Boolean.FALSE;

        List<ProcedureType> miProcedureType = null;

        try
        {
            miProcedureType = myJpaProcedureType.findProcedureType(nameTypeProcedure);

            if (miProcedureType != null && !miProcedureType.isEmpty())
            {
                resultado = true;
            }
        }
        catch (PersistenceException e)
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
    public ArrayList<DtoProcedureType> searchTiposDeProcedureHabilitados()
    {
        List<ProcedureType> proceduresExistentes = null;
        ArrayList<DtoProcedureType> dtoListaTiposDeProcedures = new ArrayList<>();

        try
        {

            proceduresExistentes = myJpaProcedureType.findProcedureTypeEntities();

            if (proceduresExistentes != null && !proceduresExistentes.isEmpty())
            {
                for (int i = 0; i < proceduresExistentes.size(); i++)
                {

                    if (proceduresExistentes.get(i).getEnabled())
                    {
                        DtoProcedureType dtoProcedureType = proceduresExistentes.get(i).getDto();

                        dtoListaTiposDeProcedures.add(dtoProcedureType);
                    }
                }
            }
        }
        catch (PersistenceException e)
        {
            e.printStackTrace();
        }
        return dtoListaTiposDeProcedures;
    }

    /**
     * Busca y devuelve las Plantillas de Tramite asociadas a un Tipo de Tramite
     * en particular.
     *
     * @param miDtoTipoDeTramite Un dto de tipo de un tipo de tramite.
     * @return miListaDtoPlantillas Una lista con los dto de las Plantillas de
     * Tramite asociadas al Tipo de Tramite, o null en caso de no existir.
     */
    public ArrayList<DtoProcedureTemplate> obtenerPlantillasProcedure(DtoProcedureType miDtoProcedureType)
    {
        ArrayList<DtoProcedureTemplate> miListaDtoPlantillas = new ArrayList<>();

        try
        {
            ProcedureTemplateJpaController miProcedureTemplateJpaController = (ProcedureTemplateJpaController) this.getMiAdministradorJpa().obtenerJpa(ProcedureTemplateJpaController.class.getName());

            List<ProcedureTemplate> miListaPlantillas = miProcedureTemplateJpaController.findPlantillasDeProcedure(miDtoProcedureType.getIdProcedureType().intValue());

            if (!miListaPlantillas.isEmpty() && miListaPlantillas != null)
            {
                for (Iterator<ProcedureTemplate> it = miListaPlantillas.iterator(); it.hasNext();)
                {
                    ProcedureTemplate procedureTemplate = it.next();

                    miListaDtoPlantillas.add(procedureTemplate.getDto());
                }
            }
        }
        catch (NonexistentJpaException ex)
        {
            Logger.getLogger(BusinessController.class.getName()).log(Level.SEVERE, null, ex);
        }
        catch (PersistenceException e)
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
    public Boolean modificarProcedureType(DtoProcedureType miDtoProcedureType, ArrayList<DtoDocumentType> listaDtoDocumentsAsociados) throws ClassModifiedException, ClassEliminatedException
    {

        Boolean modificado = false;
        Boolean eliminada = false;
        ProcedureType procedureTypeModificar = null;

        List<ProcedureType> procedureTypeModificarList = myJpaProcedureType.findProcedureType(miDtoProcedureType.getIdProcedureType());

        procedureTypeModificar = procedureTypeModificarList.get(0);

        if (procedureTypeModificar != null)
        {
            procedureTypeModificar.setAtributos(miDtoProcedureType);
            try
            {
                modificado = myJpaProcedureType.edit(procedureTypeModificar);

                if (modificado)
                {
                    this.registrarAudit(procedureTypeModificar, ConstantesGui.MODIFICARProcedureType);

                    List<ProcedureTemplate> plantillasActuales = myJpaProcedureTemplate.findPlantillasDeProcedure(procedureTypeModificar.getIdProcedureType());

                    if (plantillasActuales != null && !plantillasActuales.isEmpty())
                    {
                        for (int i = 0; i < plantillasActuales.size(); i++)
                        {
                            ProcedureTemplate procedureTemplate = plantillasActuales.get(i);

                            eliminada = myJpaProcedureTemplate.eliminarProcedureTemplate(procedureTemplate);

                            this.registrarAudit(procedureTemplate, ConstantesGui.ELIMINARProcedureTemplate);
                        }
                    } else
                    {
                        eliminada = true;
                    }

                    if (eliminada)
                    {
                        for (int i = 0; i < listaDtoDocumentsAsociados.size(); i++)
                        {
                            DtoDocumentType dtoDocumentType = listaDtoDocumentsAsociados.get(i);

                            ProcedureTemplate miProcedureTemplate = new ProcedureTemplate();

                            DocumentType miDocumentType = new DocumentType();
                            miDocumentType.setAtributos(dtoDocumentType);

                            miProcedureTemplate.setDocumentType(miDocumentType);
                            miProcedureTemplate.setProcedureType(procedureTypeModificar);
                            try
                            {

                                Boolean creada = myJpaProcedureTemplate.create(miProcedureTemplate);

                                this.registrarAudit(miProcedureTemplate, ConstantesGui.MODIFICARProcedureTemplate);

                            }
                            catch (PreexistingEntityException ex)
                            {
                                Logger.getLogger(BusinessController.class.getName()).log(Level.SEVERE, null, ex);
                            }
                            catch (Exception ex)
                            {
                                Logger.getLogger(BusinessController.class.getName()).log(Level.SEVERE, null, ex);
                            }

                        }
                    }

                }
            }
            catch (IllegalOrphanException ex)
            {
                Logger.getLogger(BusinessController.class.getName()).log(Level.SEVERE, null, ex);
            }
            catch (NonexistentEntityException ex)
            {
                Logger.getLogger(BusinessController.class.getName()).log(Level.SEVERE, null, ex);
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
    public Boolean eliminarProcedureType(DtoProcedureType miDtoTypeProcedure) throws ClassEliminatedException, ClassModifiedException
    {
        Boolean eliminado = false;

        List<ProcedureType> miProcedureType = myJpaProcedureType.findProcedureType(miDtoTypeProcedure.getIdProcedureType());

        if (miProcedureType != null)
        {
            try
            {
                ProcedureType miProcedure = miProcedureType.get(0);

                miProcedure.setAtributos(miDtoTypeProcedure);

                eliminado = myJpaProcedureType.edit(miProcedure);

                this.registrarAudit(miProcedure, ConstantesGui.ELIMINARProcedureType);

            }
            catch (IllegalOrphanException ex)
            {
                Logger.getLogger(BusinessController.class.getName()).log(Level.SEVERE, null, ex);
            }
            catch (NonexistentEntityException ex)
            {
                Logger.getLogger(BusinessController.class.getName()).log(Level.SEVERE, null, ex);
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
    public Boolean darAltaDocument(DtoDocumentType miDtoTypeDocument) throws ClassEliminatedException, ClassModifiedException, IllegalOrphanException, NonexistentEntityException
    {
        Boolean resultado = Boolean.FALSE;

        Boolean existe = Boolean.FALSE;
        DocumentType miDocumentType = null;

        existe = this.existeDocument(miDtoTypeDocument.getName());
        miDocumentType = new DocumentType();

        if (!existe)
        {
            miDocumentType.setAtributos(miDtoTypeDocument);
            int id = myJpaDocumentType.create(miDocumentType);

            if (id != -1)
            {
                this.registrarAudit(miDocumentType, ConstantesGui.INGRESARNUEVOTypeDocument);

                resultado = Boolean.TRUE;
            }
        } else
        {

            DocumentType documentType = (myJpaDocumentType.findDocumentType(miDtoTypeDocument.getName())).get(0);
            documentType.setEnabled(true);

            Boolean modificado = myJpaDocumentType.edit(documentType);

            this.registrarAudit(documentType, ConstantesGui.MODIFICARTypeDocument);

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
    private Boolean existeDocument(String nameDocument)
    {
        Boolean resultado = Boolean.FALSE;

        List<DocumentType> miDocumentType = null;
        DocumentTypeJpaController miDocumentTypeJpaController;

        miDocumentType = myJpaDocumentType.findDocumentType(nameDocument);

        if (miDocumentType != null && !miDocumentType.isEmpty())
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
    public ArrayList<DtoDocumentType> searchTiposDeDocumentDisponibles()
    {
        List<DocumentType> miListaTiposDocuments = null;
        ArrayList<DtoDocumentType> misDtoTiposDocuments = null;

        try
        {
            miListaTiposDocuments = myJpaDocumentType.findDocumentTypeEntities();

            if (miListaTiposDocuments != null)
            {
                misDtoTiposDocuments = new ArrayList<>();

                for (Iterator<DocumentType> it = miListaTiposDocuments.iterator(); it.hasNext();)
                {
                    DocumentType documentType = it.next();

                    if (documentType.getEnabled())
                    {
                        DtoDocumentType unDto = new DtoDocumentType();

                        unDto = documentType.getDto();

                        misDtoTiposDocuments.add(unDto);
                    }
                }

            }
        }
        catch (PersistenceException e)
        {
            e.printStackTrace();
        }

        return misDtoTiposDocuments;
    }

    /**
     * Busca y devuelve un Tipo de Documento, por su nombre.
     *
     * @param nombre nombre del Tipo de Documento a buscar.
     * @return dtoMiTipoDeDocumento Un Dto del Tipo de Documento encontrado.
     */
    public DtoDocumentType searchDocumentType(String name)
    {
        DtoDocumentType dtoMiDocumentType = null;

        List<DocumentType> miDocumentType = null;

        miDocumentType = myJpaDocumentType.findDocumentType(name);

        if (miDocumentType != null && !miDocumentType.isEmpty())
        {
            dtoMiDocumentType = miDocumentType.get(0).getDto();
        }

        return dtoMiDocumentType;
    }

    /**
     * Modifica los datos de un Tipo de Documento en particular.
     *
     * @param miDto El dto del Tipo de Documento a modificar, con los nuevos
     * datos.
     * @return modificado Verdadero si se pudo modificar el Tipo de Documento,
     * Falso de lo contrario.
     */
    public DtoFlag modificarDocumentType(DtoDocumentType miDto) throws ClassModifiedException, ClassEliminatedException, IllegalOrphanException, NonexistentEntityException
    {
        DtoFlag modificado = new DtoFlag();
        modificado.setFlag(false);

        DocumentType miDocumentType = myJpaDocumentType.findDocumentType(miDto.getIdDocumentType());

        if (miDocumentType != null)
        {
            miDocumentType.setAtributos(miDto);

            modificado.setFlag(myJpaDocumentType.edit(miDocumentType));

            this.registrarAudit(miDocumentType, ConstantesGui.MODIFICARTypeDocument);

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
    public DtoFlag eliminarDocumentType(DtoDocumentType miDto) throws ClassModifiedException, ClassEliminatedException, IllegalOrphanException, NonexistentEntityException
    {
        DtoFlag modificado = new DtoFlag();
        modificado.setFlag(false);

        DocumentType miDocumentType = myJpaDocumentType.findDocumentType(miDto.getIdDocumentType());

        if (miDocumentType != null)
        {
            miDocumentType.setAtributos(miDto);

            modificado.setFlag(myJpaDocumentType.edit(miDocumentType));

            this.registrarAudit(miDocumentType, ConstantesGui.ELIMINARTypeDocument);

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
    public Boolean darAltaConcept(DtoConcept miDto)
    {
        Boolean resultado = Boolean.FALSE;
        try
        {
            Concept nuevoConcept = new Concept();
            nuevoConcept.setAtributos(miDto);
            try
            {
                miJpaConcept.create(nuevoConcept);

                resultado = Boolean.TRUE;

                this.registrarAudit(nuevoConcept, ConstantesGui.INGRESARNUEVOConcept);

            }
            catch (PersistenceException e)
            {
                Logger.getLogger(BusinessController.class.getName()).log(Level.SEVERE, null, e);
            }

        }
        catch (DtoInvalidoException ex)
        {
            Logger.getLogger(BusinessController.class.getName()).log(Level.SEVERE, null, ex);
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
    public List<DtoConcept> obtenerListaConceptosDisponibles()
    {
        List<Concept> miListaConceptos = null;
        List<DtoConcept> dtoListaConceptos = null;

        try
        {

            miListaConceptos = miJpaConcept.findConceptEntities();

            if (miListaConceptos != null)
            {
                dtoListaConceptos = new ArrayList<>();

                for (Iterator<Concept> it = miListaConceptos.iterator(); it.hasNext();)
                {
                    Concept unConcept = it.next();

                    if (unConcept.getEnabled())
                    {

                        DtoConcept unDto = unConcept.getDto();
                        dtoListaConceptos.add(unDto);
                    }
                }

            }
        }
        catch (PersistenceException e)
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
    public Boolean modificarConcept(DtoConcept conceptParaModificar) throws ClassModifiedException, ClassEliminatedException, NonexistentEntityException, IllegalOrphanException, PreexistingEntityException
    {
        Boolean resultado = Boolean.FALSE;

        try
        {

            Concept nuevoConcept = miJpaConcept.findConcept(conceptParaModificar.getIdConcept());

            if (nuevoConcept != null)
            {
//                if (!this.existeConcepto(conceptoParaModificar.getNombre()))
//                {
                nuevoConcept.setAtributos(conceptParaModificar);

                resultado = miJpaConcept.edit(nuevoConcept);

                if (resultado == true)
                {
                    this.registrarAudit(nuevoConcept, ConstantesGui.MODIFICARConcept);

                }
//                }
//                else
//                {
//                    throw new PreexistingEntityException("El concepto indica ya existe");
//                }

            }

        }
        catch (DtoInvalidoException ex)
        {
            //  TODO: Eliminar de la version Final
            Logger.getLogger(BusinessController.class.getName()).log(Level.SEVERE, null, ex);
        }
        catch (PersistenceException e)
        {
            //  TODO: Eliminar de la version Final
            Logger.getLogger(BusinessController.class.getName()).log(Level.SEVERE, null, e);
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
    public Boolean eliminarConcept(DtoConcept conceptParaEliminar) throws ClassEliminatedException, ClassModifiedException, IllegalOrphanException, NonexistentEntityException
    {
        Boolean resultado = Boolean.TRUE;
        try
        {
            Concept miConcept = new Concept();

            miConcept.setAtributos(conceptParaEliminar);

            try
            {
                resultado = miJpaConcept.edit(miConcept);

                if (resultado == true)
                {
                    this.registrarAudit(miConcept, ConstantesGui.ELIMINARConcept);
                }
            }
            catch (PersistenceException e)
            {
                e.printStackTrace();

            }

        }
        catch (DtoInvalidoException ex)
        {
            Logger.getLogger(BusinessController.class.getName()).log(Level.SEVERE, null, ex);
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
    public boolean existeConcept(String nameConcept)
    {
        boolean resultado = false;

        List<Concept> listaConcept = miJpaConcept.findConceptByName(nameConcept);

        if (!listaConcept.isEmpty())
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
    public Boolean darAltaManagementStatus(DtoManagementStatus miDto) throws PreexistingEntityException
    {
        Boolean resultado = Boolean.FALSE;

        ManagementStatus nuevoManagementStatus = new ManagementStatus();
        try
        {
            nuevoManagementStatus.setAtributo(miDto);

            try
            {
                miJpaManagementStatus = (ManagementStatusJpaController) miAdministradorJpa.obtenerJpa(ManagementStatusJpaController.class.getName());

                miJpaManagementStatus.create(nuevoManagementStatus);
                resultado = Boolean.TRUE;

                this.registrarAudit(nuevoManagementStatus, ConstantesGui.INGRESARStatusManagement);
            }
            catch (NonexistentJpaException ex)
            {
                Logger.getLogger(BusinessController.class.getName()).log(Level.SEVERE, null, ex);
            }
            catch (PersistenceException e)
            {
                e.printStackTrace();

            }
        }
        catch (DtoInvalidoException ex)
        {
            Logger.getLogger(BusinessController.class.getName()).log(Level.SEVERE, null, ex);
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
    public List<DtoManagementStatus> obtenerListaEstadosDeManagementDisponibles()
    {
        List<DtoManagementStatus> milistaDtoStatusDeGestions = new ArrayList<>();
        List<ManagementStatus> listaEstadosDeManagement = new ArrayList<>();

        try
        {
            miJpaManagementStatus = (ManagementStatusJpaController) miAdministradorJpa.obtenerJpa(ManagementStatusJpaController.class.getName());

            listaEstadosDeManagement = miJpaManagementStatus.findManagementStatusEntities();

            if (!listaEstadosDeManagement.isEmpty())
            {
                for (Iterator<ManagementStatus> it = listaEstadosDeManagement.iterator(); it.hasNext();)
                {
                    ManagementStatus managementStatus = it.next();

                    DtoManagementStatus unDtoStatusDeGestio = managementStatus.getDto();

                    milistaDtoStatusDeGestions.add(unDtoStatusDeGestio);
                }
            }
        }
        catch (NonexistentJpaException ex)
        {
            Logger.getLogger(BusinessController.class.getName()).log(Level.SEVERE, null, ex);
        }
        catch (PersistenceException e)
        {
            e.printStackTrace();
        }

        return milistaDtoStatusDeGestions;
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
    public Boolean modificarManagementStatus(DtoManagementStatus dtoManagementStatus) throws ClassEliminatedException, ClassModifiedException
    {
        Boolean resultado = Boolean.FALSE;
        try
        {
            ManagementStatus unManagementStatus = miJpaManagementStatus.findManagementStatus(dtoManagementStatus.getIdManagementStatus());
            if (unManagementStatus != null)
            {
                try
                {
                    unManagementStatus.setAtributo(dtoManagementStatus);
                    try
                    {
                        miJpaManagementStatus.edit(unManagementStatus);

                    }
                    catch (IllegalOrphanException ex)
                    {
                        //  TODO: Eliminar de la version Final
                        Logger.getLogger(BusinessController.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    catch (NonexistentEntityException ex)
                    {
                        //  TODO: Eliminar de la version Final
                        Logger.getLogger(BusinessController.class.getName()).log(Level.SEVERE, null, ex);
                    }

                    resultado = Boolean.TRUE;

                    this.registrarAudit(unManagementStatus, ConstantesGui.MODIFICARStatusManagement);

                }
                catch (DtoInvalidoException ex)
                {
                    //  TODO: Eliminar de la version Final
                    Logger.getLogger(BusinessController.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
        catch (PersistenceException e)
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
    public Boolean darDeAltaFolioType(DtoFolioType nuevoDtoFolioType) throws ClassModifiedException
    {
        Boolean resultado = Boolean.FALSE;

        FolioType nuevoFolioType = new FolioType();

        try
        {
            nuevoDtoFolioType.setEnabled(BusinessConstants.FolioTypeEnabled);
            nuevoFolioType.setAtributos(nuevoDtoFolioType);
            try
            {
                myJpaFolioType.create(nuevoFolioType);

                resultado = Boolean.TRUE;

                this.registrarAudit(nuevoFolioType, ConstantesGui.INGRESARNUEVOTypeFolio);
            }
            catch (PreexistingEntityException ex)
            {
                try
                {
                    nuevoDtoFolioType = this.searchFolioType(nuevoDtoFolioType);

                    nuevoFolioType.setAtributos(nuevoDtoFolioType);
                    nuevoFolioType.setEnabled(BusinessConstants.FolioTypeEnabled);

                    myJpaFolioType.edit(nuevoFolioType);

                    this.registrarAudit(nuevoFolioType, ConstantesGui.MODIFICARTypeFolio);
                    resultado = Boolean.TRUE;

                }
                catch (IllegalOrphanException | NonexistentEntityException | ClassEliminatedException ex1)
                {
                    Logger.getLogger(BusinessController.class.getName()).log(Level.SEVERE, null, ex1);
                }
            }

        }
        catch (DtoInvalidoException e)
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
    public List<DtoFolioType> obtenerListaTiposDeFoliosDisponibles()
    {
        List<FolioType> listaTiposDeFolios = null;
        List<DtoFolioType> miListaDtoFolios = null;

        listaTiposDeFolios = myJpaFolioType.findFolioTypeEntities();
        if (listaTiposDeFolios != null)
        {
            miListaDtoFolios = new ArrayList<>();

            for (Iterator<FolioType> it = listaTiposDeFolios.iterator(); it.hasNext();)
            {
                FolioType miFolioType = it.next();
                if (miFolioType.getEnabled() == BusinessConstants.FolioTypeEnabled)
                {
                    DtoFolioType unDto = miFolioType.getDto();

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
    public Boolean modificarTypeDeFolios(DtoFolioType dtoFolioTypeModificar) throws ClassModifiedException
    {
        Boolean resultado = Boolean.FALSE;
        try
        {
            FolioType miFolioType = null;

            miFolioType = myJpaFolioType.findFolioType(dtoFolioTypeModificar.getIdFolioType());

            miFolioType.setAtributos(dtoFolioTypeModificar);

            myJpaFolioType.edit(miFolioType);

            resultado = Boolean.TRUE;

            this.registrarAudit(miFolioType, ConstantesGui.MODIFICARTypeFolio);

        }
        catch (IllegalOrphanException | NonexistentEntityException | ClassEliminatedException | DtoInvalidoException ex)
        {
            Logger.getLogger(BusinessController.class.getName()).log(Level.SEVERE, null, ex);
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
    public Boolean eliminarFolioType(DtoFolioType dtoFolioTypeEliminar) throws ClassModifiedException
    {
        Boolean resultado = Boolean.FALSE;
        FolioType miFolioType = null;

        miFolioType = myJpaFolioType.findFolioType(dtoFolioTypeEliminar.getIdFolioType());
        if (miFolioType != null)
        {
            try
            {
                miFolioType.setEnabled(BusinessConstants.FolioTypeDESHABILITADO);

                myJpaFolioType.edit(miFolioType);

                resultado = Boolean.TRUE;

                this.registrarAudit(miFolioType, ConstantesGui.ELIMINARTypeFolio);

            }
            catch (IllegalOrphanException | NonexistentEntityException | ClassEliminatedException ex)
            {
                Logger.getLogger(BusinessController.class.getName()).log(Level.SEVERE, null, ex);
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
    public Boolean existeBudgetTemplate(DtoProcedureType dtoProcedureType)
    {
        Boolean existe = Boolean.FALSE;
        ArrayList<DtoBudgetTemplate> plantillas = null;

        plantillas = this.obtenerPlantillasBudget(dtoProcedureType);

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
    public Boolean crearBudgetTemplate(DtoProcedureType miDtoProcedureType, ArrayList<DtoConcept> misDtoConceptos) throws PreexistingEntityException
    {
        Boolean creada = false;
        BudgetTemplate miBudgetTemplate = new BudgetTemplate();

        List<ProcedureType> miProcedureType = myJpaProcedureType.findProcedureType(miDtoProcedureType.getIdProcedureType());

        Concept miConcept;
        Boolean templateCreada = false;

        for (int i = 0; i < misDtoConceptos.size(); i++)
        {

            DtoConcept dtoConcept = misDtoConceptos.get(i);

            miConcept = miJpaConcept.findConcept(dtoConcept.getIdConcept());

            miBudgetTemplate.setConcept(miConcept);
            miBudgetTemplate.setProcedureType(miProcedureType.get(0));

            templateCreada = myJpaBudgetTemplate.create(miBudgetTemplate);

        }

        if (templateCreada)
        {
            creada = true;

            this.registrarAudit(miBudgetTemplate, ConstantesGui.CREARTemplateDEBudget);
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
    public ArrayList<DtoBudgetTemplate> obtenerPlantillasBudget(DtoProcedureType miDtoProcedureType)
    {
        ArrayList<DtoBudgetTemplate> miListaDtoPlantillas = new ArrayList<>();

        List<BudgetTemplate> miListaPlantillas = myJpaBudgetTemplate.findPlantillasDeBudget(miDtoProcedureType.getIdProcedureType().intValue());

        if (!miListaPlantillas.isEmpty() && miListaPlantillas != null)
        {
            for (Iterator<BudgetTemplate> it = miListaPlantillas.iterator(); it.hasNext();)
            {
                BudgetTemplate budgetTemplate = it.next();

                miListaDtoPlantillas.add(budgetTemplate.getDto());
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
    public Boolean modificarBudgetTemplate(DtoProcedureType miDtoProcedureType, ArrayList<DtoConcept> misDtoConceptos) throws ClassEliminatedException, PreexistingEntityException, ClassModifiedException
    {
        Boolean modificado = false;
        Boolean modificada = false;
        Boolean eliminada = false;
        ProcedureType procedureTypeModificar = null;

        List<ProcedureType> procedureTypeModificarList = myJpaProcedureType.findProcedureType(miDtoProcedureType.getIdProcedureType());

        procedureTypeModificar = procedureTypeModificarList.get(0);

        if (procedureTypeModificar != null)
        {
            try
            {
                procedureTypeModificar.setAtributos(miDtoProcedureType);
                modificado = myJpaProcedureType.edit(procedureTypeModificar);

                if (modificado)
                {
                    this.registrarAudit(procedureTypeModificar, ConstantesGui.MODIFICARProcedureType);

                    List<BudgetTemplate> plantillasActuales = myJpaBudgetTemplate.findPlantillasDeBudget(procedureTypeModificar.getIdProcedureType());

                    if (plantillasActuales != null && !plantillasActuales.isEmpty())
                    {
                        for (int i = 0; i < plantillasActuales.size(); i++)
                        {
                            BudgetTemplate budgetTemplate = plantillasActuales.get(i);

                            eliminada = myJpaBudgetTemplate.eliminarBudgetTemplate(budgetTemplate);

                            this.registrarAudit(budgetTemplate, ConstantesGui.ELIMINARTemplateDEBudget);
                        }
                    } else
                    {
                        eliminada = true;
                    }

                    if (eliminada)
                    {
                        if (misDtoConceptos != null && !misDtoConceptos.isEmpty())
                        {
                            BudgetTemplate miTemplate = null;
                            Boolean creada = false;

                            for (Iterator<DtoConcept> it = misDtoConceptos.iterator(); it.hasNext();)
                            {
                                DtoConcept dtoConcept = it.next();

                                Concept miConcept = miJpaConcept.findConcept(dtoConcept.getIdConcept());

                                if (miConcept != null)
                                {

                                    miTemplate = new BudgetTemplate();

                                    miTemplate.setConcept(miConcept);
                                    miTemplate.setProcedureType(procedureTypeModificar);

                                    creada = myJpaBudgetTemplate.create(miTemplate);
                                }
                            }

                            if (creada)
                            {
                                modificada = true;

                                this.registrarAudit(miTemplate, ConstantesGui.MODIFICARTemplateDEBudget);
                            }
                        }
                    }
                } else
                {
                    throw new ClassEliminatedException();
                }
            }
            catch (IllegalOrphanException ex)
            {
                Logger.getLogger(BusinessController.class.getName()).log(Level.SEVERE, null, ex);
            }
            catch (NonexistentEntityException ex)
            {
                Logger.getLogger(BusinessController.class.getName()).log(Level.SEVERE, null, ex);
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
    public Boolean eliminarBudgetTemplate(ArrayList<DtoBudgetTemplate> dtosPlantillasBudget) throws ClassEliminatedException
    {
        Boolean eliminada = false;

        // Elimino todas las plantillas asociadas al Tipo de Tramite:
        if (dtosPlantillasBudget != null && !dtosPlantillasBudget.isEmpty())
        {
            Boolean eliminado = false;
            for (int i = 0; i < dtosPlantillasBudget.size(); i++)
            {

                BudgetTemplate budgetTemplate = new BudgetTemplate();
                budgetTemplate.setAtributos(dtosPlantillasBudget.get(i));

                eliminado = myJpaBudgetTemplate.eliminarBudgetTemplate(budgetTemplate);

                if (eliminado)
                {
                    this.registrarAudit(budgetTemplate, ConstantesGui.ELIMINARTemplateDEBudget);
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
