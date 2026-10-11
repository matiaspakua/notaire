/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.licensis.notaire.jpa;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.Query;
import jakarta.transaction.UserTransaction;
import com.licensis.notaire.jpa.exceptions.ClassModifiedException;
import com.licensis.notaire.jpa.exceptions.IllegalOrphanException;
import com.licensis.notaire.jpa.exceptions.NonexistentEntityException;
import com.licensis.notaire.jpa.JpaPersistence;
import com.licensis.notaire.business.DeedManagement;
import com.licensis.notaire.business.History;
import com.licensis.notaire.business.Person;
import com.licensis.notaire.business.Procedure;
import org.hibernate.StaleObjectStateException;

/**
 *
 * @author juanca
 */
public class DeedManagementJpaController implements Serializable, JpaPersistence
{

    public DeedManagementJpaController(UserTransaction utx, EntityManagerFactory emf)
    {
        this.utx = utx;
        this.emf = emf;
    }
    private UserTransaction utx = null;
    private EntityManagerFactory emf = null;

    public EntityManager getEntityManager()
    {
        return emf.createEntityManager();
    }

    /**
     * Persiste in the database a new management of deeds.
     *
     * @param unaGestionDeEscritura The new management of deeds a persistir.
     * @return oidGestionEscritura The id of the new management of deed, -1 if ocurrio algun
     * error.
     */
    public Integer create(DeedManagement unaDeedManagement)
    {
        Integer oidManagementDeed = new Integer(-1);
        if (unaDeedManagement.getHistoryList() == null)
        {
            unaDeedManagement.setHistoryList(new ArrayList<History>());
        }
        if (unaDeedManagement.getProcedureList() == null)
        {
            unaDeedManagement.setProcedureList(new ArrayList<Procedure>());
        }
        EntityManager em = null;
        try
        {
            em = getEntityManager();
            em.getTransaction().begin();
            Person fkIdNotaryPerson = unaDeedManagement.getFkIdNotaryPerson();
            if (fkIdNotaryPerson != null)
            {
                fkIdNotaryPerson = em.getReference(fkIdNotaryPerson.getClass(), fkIdNotaryPerson.getPersonId());
                unaDeedManagement.setFkIdNotaryPerson(fkIdNotaryPerson);
            }
            List<History> attachedHistoryList = new ArrayList<History>();
            for (History historyListHistoryToAttach : unaDeedManagement.getHistoryList())
            {
                historyListHistoryToAttach = em.getReference(historyListHistoryToAttach.getClass(), historyListHistoryToAttach.getIdHistory());
                attachedHistoryList.add(historyListHistoryToAttach);
            }
            unaDeedManagement.setHistoryList(attachedHistoryList);
            List<Procedure> attachedProcedureList = new ArrayList<Procedure>();
            for (Procedure procedureListProcedureToAttach : unaDeedManagement.getProcedureList())
            {
                procedureListProcedureToAttach = em.getReference(procedureListProcedureToAttach.getClass(), procedureListProcedureToAttach.getIdProcedure());
                attachedProcedureList.add(procedureListProcedureToAttach);
            }
            unaDeedManagement.setProcedureList(attachedProcedureList);
            em.persist(unaDeedManagement);
            if (fkIdNotaryPerson != null)
            {
                fkIdNotaryPerson.getDeedManagementList().add(unaDeedManagement);
                fkIdNotaryPerson = em.merge(fkIdNotaryPerson);
            }
            for (History historyListHistory : unaDeedManagement.getHistoryList())
            {
                DeedManagement oldFkIdManagementOfHistoryListHistory = historyListHistory.getFkIdManagement();
                historyListHistory.setFkIdManagement(unaDeedManagement);
                historyListHistory = em.merge(historyListHistory);
                if (oldFkIdManagementOfHistoryListHistory != null)
                {
                    oldFkIdManagementOfHistoryListHistory.getHistoryList().remove(historyListHistory);
                    oldFkIdManagementOfHistoryListHistory = em.merge(oldFkIdManagementOfHistoryListHistory);
                }
            }
            for (Procedure procedureListProcedure : unaDeedManagement.getProcedureList())
            {
                DeedManagement oldFkIdManagementOfProcedureListProcedure = procedureListProcedure.getFkIdManagement();
                procedureListProcedure.setFkIdManagement(unaDeedManagement);
                procedureListProcedure = em.merge(procedureListProcedure);
                if (oldFkIdManagementOfProcedureListProcedure != null)
                {
                    oldFkIdManagementOfProcedureListProcedure.getProcedureList().remove(procedureListProcedure);
                    oldFkIdManagementOfProcedureListProcedure = em.merge(oldFkIdManagementOfProcedureListProcedure);
                }
            }
            em.getTransaction().commit();

            oidManagementDeed = unaDeedManagement.getIdManagement();
        }
        finally
        {
            if (em != null)
            {
                em.close();
            }
        }
        return oidManagementDeed;
    }

    /**
     * Permite update a management of deed in the persistencia.
     *
     * @param gestionParaModificar The management of deed a update.
     * @return modificado Verdadero si se pudo modificar, falso en caso contrario.
     *
     * @throws IllegalOrphanException
     * @throws NonexistentEntityException
     * @throws Exception
     */
    public Boolean edit(DeedManagement managementParaModificar) throws IllegalOrphanException, NonexistentEntityException, ClassModifiedException
    {
        Boolean modificado = Boolean.FALSE;

        int version;
        int oldVersion;

        EntityManager em = null;
        try
        {
            em = getEntityManager();

            DeedManagement persistentManagement = em.find(DeedManagement.class, managementParaModificar.getIdManagement());

            if (persistentManagement != null)
            {
                version = persistentManagement.getVersion();
                oldVersion = managementParaModificar.getVersion();

                if (version != oldVersion)
                {
                    throw new ClassModifiedException();
                }

                em.getTransaction().begin();
                DeedManagement persistentDeedManagement = em.find(DeedManagement.class, managementParaModificar.getIdManagement());
                Person fkIdNotaryPersonOld = persistentDeedManagement.getFkIdNotaryPerson();
                Person fkIdNotaryPersonNew = managementParaModificar.getFkIdNotaryPerson();
                List<History> historyListOld = persistentDeedManagement.getHistoryList();
                List<History> historyListNew = managementParaModificar.getHistoryList();
                List<Procedure> procedureListOld = persistentDeedManagement.getProcedureList();
                List<Procedure> procedureListNew = managementParaModificar.getProcedureList();
                List<String> illegalOrphanMessages = null;
                for (History historyListOldHistory : historyListOld)
                {
                    if (!historyListNew.contains(historyListOldHistory))
                    {
                        if (illegalOrphanMessages == null)
                        {
                            illegalOrphanMessages = new ArrayList<String>();
                        }
                        illegalOrphanMessages.add("You must retain ManagementHistory " + historyListOldHistory + " since its fkIdGestion field is not nullable.");
                    }
                }
                if (illegalOrphanMessages != null)
                {
                    throw new IllegalOrphanException(illegalOrphanMessages);
                }
                if (fkIdNotaryPersonNew != null)
                {
                    fkIdNotaryPersonNew = em.getReference(fkIdNotaryPersonNew.getClass(), fkIdNotaryPersonNew.getPersonId());
                    managementParaModificar.setFkIdNotaryPerson(fkIdNotaryPersonNew);
                }
                List<History> attachedHistoryListNew = new ArrayList<History>();
                for (History historyListNewHistoryToAttach : historyListNew)
                {
                    historyListNewHistoryToAttach = em.getReference(historyListNewHistoryToAttach.getClass(), historyListNewHistoryToAttach.getIdHistory());
                    attachedHistoryListNew.add(historyListNewHistoryToAttach);
                }
                historyListNew = attachedHistoryListNew;
                managementParaModificar.setHistoryList(historyListNew);
                List<Procedure> attachedProcedureListNew = new ArrayList<Procedure>();
                for (Procedure procedureListNewProcedureToAttach : procedureListNew)
                {
                    procedureListNewProcedureToAttach = em.getReference(procedureListNewProcedureToAttach.getClass(), procedureListNewProcedureToAttach.getIdProcedure());
                    attachedProcedureListNew.add(procedureListNewProcedureToAttach);
                }
                procedureListNew = attachedProcedureListNew;
                managementParaModificar.setProcedureList(procedureListNew);
                managementParaModificar = em.merge(managementParaModificar);
                if (fkIdNotaryPersonOld != null && !fkIdNotaryPersonOld.equals(fkIdNotaryPersonNew))
                {
                    fkIdNotaryPersonOld.getDeedManagementList().remove(managementParaModificar);
                    fkIdNotaryPersonOld = em.merge(fkIdNotaryPersonOld);
                }
                if (fkIdNotaryPersonNew != null && !fkIdNotaryPersonNew.equals(fkIdNotaryPersonOld))
                {
                    fkIdNotaryPersonNew.getDeedManagementList().add(managementParaModificar);
                    fkIdNotaryPersonNew = em.merge(fkIdNotaryPersonNew);
                }
                for (History historyListNewHistory : historyListNew)
                {
                    if (!historyListOld.contains(historyListNewHistory))
                    {
                        DeedManagement oldFkIdManagementOfHistoryListNewHistory = historyListNewHistory.getFkIdManagement();
                        historyListNewHistory.setFkIdManagement(managementParaModificar);
                        historyListNewHistory = em.merge(historyListNewHistory);
                        if (oldFkIdManagementOfHistoryListNewHistory != null && !oldFkIdManagementOfHistoryListNewHistory.equals(managementParaModificar))
                        {
                            oldFkIdManagementOfHistoryListNewHistory.getHistoryList().remove(historyListNewHistory);
                            oldFkIdManagementOfHistoryListNewHistory = em.merge(oldFkIdManagementOfHistoryListNewHistory);
                        }
                    }
                }
                for (Procedure procedureListOldProcedure : procedureListOld)
                {
                    if (!procedureListNew.contains(procedureListOldProcedure))
                    {
                        procedureListOldProcedure.setFkIdManagement(null);
                        procedureListOldProcedure = em.merge(procedureListOldProcedure);
                    }
                }
                for (Procedure procedureListNewProcedure : procedureListNew)
                {
                    if (!procedureListOld.contains(procedureListNewProcedure))
                    {
                        DeedManagement oldFkIdManagementOfProcedureListNewProcedure = procedureListNewProcedure.getFkIdManagement();
                        procedureListNewProcedure.setFkIdManagement(managementParaModificar);
                        procedureListNewProcedure = em.merge(procedureListNewProcedure);
                        if (oldFkIdManagementOfProcedureListNewProcedure != null && !oldFkIdManagementOfProcedureListNewProcedure.equals(managementParaModificar))
                        {
                            oldFkIdManagementOfProcedureListNewProcedure.getProcedureList().remove(procedureListNewProcedure);
                            oldFkIdManagementOfProcedureListNewProcedure = em.merge(oldFkIdManagementOfProcedureListNewProcedure);
                        }
                    }
                }
                em.getTransaction().commit();
                modificado = Boolean.TRUE;
            }
        }
        finally
        {
            if (em != null)
            {
                em.close();
            }
        }
        return modificado;
    }

    /**
     * Permite elimianr a management of deed of the persistencia.
     *
     * @param id The id of the management of deed a delete.
     * @throws IllegalOrphanException
     * @throws NonexistentEntityException
     */
    public void destroy(Integer id) throws IllegalOrphanException, NonexistentEntityException
    {
        EntityManager em = null;
        try
        {
            em = getEntityManager();
            em.getTransaction().begin();
            DeedManagement DeedManagement;
            try
            {
                DeedManagement = em.getReference(DeedManagement.class, id);
                DeedManagement.getIdManagement();
            }
            catch (EntityNotFoundException enfe)
            {
                throw new NonexistentEntityException("The DeedManagement with id " + id + " no longer exists.", enfe);
            }
            List<String> illegalOrphanMessages = null;
            List<History> historyListOrphanCheck = DeedManagement.getHistoryList();
            for (History historyListOrphanCheckHistory : historyListOrphanCheck)
            {
                if (illegalOrphanMessages == null)
                {
                    illegalOrphanMessages = new ArrayList<String>();
                }
                illegalOrphanMessages.add("This DeedManagement (" + DeedManagement + ") cannot be destroyed since the ManagementHistory " + historyListOrphanCheckHistory + " in its historialList field has a non-nullable fkIdGestion field.");
            }
            if (illegalOrphanMessages != null)
            {
                throw new IllegalOrphanException(illegalOrphanMessages);
            }
            Person fkIdNotaryPerson = DeedManagement.getFkIdNotaryPerson();
            if (fkIdNotaryPerson != null)
            {
                fkIdNotaryPerson.getDeedManagementList().remove(DeedManagement);
                fkIdNotaryPerson = em.merge(fkIdNotaryPerson);
            }
            List<Procedure> procedureList = DeedManagement.getProcedureList();
            for (Procedure procedureListProcedure : procedureList)
            {
                procedureListProcedure.setFkIdManagement(null);
                procedureListProcedure = em.merge(procedureListProcedure);
            }
            em.remove(DeedManagement);
            em.getTransaction().commit();
        }
        finally
        {
            if (em != null)
            {
                em.close();
            }
        }
    }

    public List<DeedManagement> findDeedManagementEntities()
    {
        return findDeedManagementEntities(true, -1, -1);
    }

    public List<DeedManagement> findDeedManagementEntities(int maxResults, int firstResult)
    {
        return findDeedManagementEntities(false, maxResults, firstResult);
    }

    private List<DeedManagement> findDeedManagementEntities(boolean all, int maxResults, int firstResult)
    {
        EntityManager em = getEntityManager();
        try
        {
            Query q = em.createQuery("select object(o) from DeedManagement as o");
            if (!all)
            {
                q.setMaxResults(maxResults);
                q.setFirstResult(firstResult);
            }
            return q.getResultList();
        }
        finally
        {
            em.close();
        }
    }

    public DeedManagement findDeedManagement(Integer id)
    {
        EntityManager em = getEntityManager();
        try
        {
            return em.find(DeedManagement.class, id);
        }
        finally
        {
            em.close();
        }
    }

    /**
     * Busca a management of deed in base to the number of management.
     *
     * @param numeroGestionDeEscritura
     * @return
     */
    public DeedManagement findDeedManagementPorNumber(Integer numberDeedManagement)
    {

        EntityManager em = getEntityManager();

        Query query = em.createNamedQuery("DeedManagement.findByNumber");
        query.setParameter("number", numberDeedManagement);

        List<DeedManagement> listaGestiones = query.getResultList();

        if (!listaGestiones.isEmpty())
        {
            // exists the management indicada
            return listaGestiones.get(0);
        }

        DeedManagement managementVacia = new DeedManagement();
        managementVacia.setNumber(ConstantesPersistencia.VersionINICIAL);
        return managementVacia;
    }

    public DeedManagement findDeedManagementPorId(Integer idManagement)
    {

        EntityManager em = getEntityManager();

        Query query = em.createNamedQuery("DeedManagement.findByIdManagement");
        query.setParameter("idGestion", idManagement);

        List<DeedManagement> listaGestiones = query.getResultList();

        if (!listaGestiones.isEmpty())
        {
            // exists the management indicada
            return listaGestiones.get(0);
        }

        DeedManagement managementVacia = new DeedManagement();
        managementVacia.setNumber(ConstantesPersistencia.VersionINICIAL);
        return managementVacia;
    }

    public int getDeedManagementCount()
    {
        EntityManager em = getEntityManager();
        try
        {
            Query q = em.createQuery("select count(o) from DeedManagement as o");
            return ((Long) q.getSingleResult()).intValue();
        }
        finally
        {
            em.close();
        }
    }

    public int getLastManagementNumber()
    {
        EntityManager em = getEntityManager();
        int resultado = 0;
        try
        {
            Query q = em.createQuery("select max(number) as ultimaGestion from DeedManagement");

            if (q.getSingleResult() != null)
            {
                resultado = (int) q.getSingleResult();
            }

            return resultado;
        }
        finally
        {
            em.close();
        }
    }

    /**
     * Managements in the that participa a client (person) vía TramitesPersonas.
     */
    @SuppressWarnings("unchecked")
    public List<DeedManagement> findManagementsByClient(Integer idPerson)
    {
        EntityManager em = getEntityManager();
        try
        {
            Query q = em.createQuery(
                    "SELECT DISTINCT t.fkIdManagement FROM com.licensis.notaire.business.PersonProcedure tp "
                            + "JOIN tp.procedure t WHERE tp.personProcedurePK.fkIdClientPerson = :idPersona");
            q.setParameter("idPersona", idPerson);
            return (List<DeedManagement>) q.getResultList();
        }
        finally
        {
            em.close();
        }
    }

    public List<DeedManagement> findManagementsOfDeed()
    {
        EntityManager em = getEntityManager();

        List<DeedManagement> listaGestiones = null;
        Person person = null;
        Query query = em.createNamedQuery("DeedManagement.findAll");
        listaGestiones = query.getResultList();

        return listaGestiones;
    }

    public boolean archivingManagements(DeedManagement pGestioneDeEscrituras) throws ClassModifiedException
    {

        Boolean flag = false; //Variable for saber the resultado of the transaccion
        int oldVersion = 0; //Variable for Version in memoria of the Objeto
        int version = 0;    //Variable for Version in bd of the Objeto

        EntityManager em = getEntityManager();

        DeedManagement persistentManagement = em.find(DeedManagement.class, pGestioneDeEscrituras.getIdManagement());

        if (persistentManagement != null)
        {
            version = persistentManagement.getVersion(); // Version of the Objeto in db
            oldVersion = pGestioneDeEscrituras.getVersion(); // Version of the Objeto in memoria

            if (version != oldVersion) //Si son distintas "Alguien modifico el objeto"
            {
                throw new ClassModifiedException("The management indicada has been modificada by another user");

            } else
            {
                try
                {
                    em.getTransaction().begin();
                    persistentManagement.setFkIdManagementStatus(pGestioneDeEscrituras.getFkIdManagementStatus());
                    persistentManagement.setNotes(pGestioneDeEscrituras.getNotes());

                    em.getTransaction().commit();
                    em.close();
                    flag = true;

                }
                catch (Exception e)
                {
                    System.out.println("Error de Persistencia: UserAccount JpaController metodo: modificarUsuario");
                }
            }
        } else
        {
            throw new StaleObjectStateException(null, version);
        }
        return flag;
    }

    /**
     * Permite update a management of deed.
     *
     * @param gestionParaModificar
     * @return
     * @throws ClassModifiedException
     */
    public boolean modifyDeedManagement(DeedManagement managementParaModificar) throws ClassModifiedException
    {
        boolean resultado = false;

        int version;
        int oldVersion;

        EntityManager em = null;
        try
        {
            em = getEntityManager();

            DeedManagement persistentManagement = em.find(DeedManagement.class, managementParaModificar.getIdManagement());

            if (persistentManagement != null)
            {
                version = persistentManagement.getVersion();
                oldVersion = managementParaModificar.getVersion();

                if (version != oldVersion)
                {
                    throw new ClassModifiedException();
                }

                em.getTransaction().begin();

                persistentManagement.setHeading(managementParaModificar.getHeading());
                persistentManagement.setNotes(managementParaModificar.getNotes());
                persistentManagement.setFkIdManagementStatus(managementParaModificar.getFkIdManagementStatus());
                //persistentGestion.setHistorialList(gestionParaModificar.getHistorialList());

                em.getTransaction().commit();
                resultado = true;
            }
        }
        finally
        {
            if (em != null)
            {
                em.close();
            }
        }

        return resultado;
    }

    /**
     * permite modifica the "status" of a management of deed.
     *
     * @param gestionParaModificar
     * @return
     * @throws ClassModifiedException
     */
    public boolean modifyDeedManagementStatus(DeedManagement managementParaModificar) throws ClassModifiedException
    {
        boolean resultado = false;

        int version;
        int oldVersion;

        EntityManager em = null;
        try
        {
            em = getEntityManager();

            DeedManagement persistentManagement = em.find(DeedManagement.class, managementParaModificar.getIdManagement());

            if (persistentManagement != null)
            {
                version = persistentManagement.getVersion();
                oldVersion = managementParaModificar.getVersion();

                if (version != oldVersion)
                {
                    throw new ClassModifiedException();
                }

                em.getTransaction().begin();

                persistentManagement.setFkIdManagementStatus(managementParaModificar.getFkIdManagementStatus());

                em.getTransaction().commit();
                resultado = true;
            }
        }
        finally
        {
            if (em != null)
            {
                em.close();
            }
        }

        return resultado;
    }

    @Override
    public String getNameJpa()
    {
        return this.getClass().getName();
    }
}
