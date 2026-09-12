/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.licensis.notaire.jpa;

import com.licensis.notaire.jpa.exceptions.IllegalOrphanException;
import com.licensis.notaire.jpa.exceptions.NonexistentEntityException;
import com.licensis.notaire.jpa.interfaz.IPersistenciaJpa;
import com.licensis.notaire.business.SubmittedDocument;
import com.licensis.notaire.business.Deed;
import com.licensis.notaire.business.DeedManagement;
import com.licensis.notaire.business.Property;
import com.licensis.notaire.business.Person;
import com.licensis.notaire.business.Budget;
import com.licensis.notaire.business.ProcedureType;
import com.licensis.notaire.business.Procedure;
import com.licensis.notaire.business.PersonProcedure;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.Query;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import jakarta.transaction.UserTransaction;

/**
 *
 * @author matias
 */
public class ProcedureJpaController implements Serializable, IPersistenciaJpa
{

    public ProcedureJpaController(UserTransaction utx, EntityManagerFactory emf)
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

    public int create(Procedure procedure) throws IllegalOrphanException
    {
        int id = -1;
        if (procedure.getSubmittedDocumentList() == null)
        {
            procedure.setSubmittedDocumentList(new ArrayList<SubmittedDocument>());
        }
        if (procedure.getPersonProcedureList() == null)
        {
            procedure.setPersonProcedureList(new ArrayList<PersonProcedure>());
        }
        EntityManager em = null;
        try
        {
            em = getEntityManager();
            em.getTransaction().begin();
            Property fkIdProperty = procedure.getFkIdProperty();
            if (fkIdProperty != null)
            {
                fkIdProperty = em.getReference(fkIdProperty.getClass(), fkIdProperty.getIdProperty());
                procedure.setFkIdProperty(fkIdProperty);
            }
            Budget fkIdBudget = procedure.getFkIdBudget();
            if (fkIdBudget != null)
            {
                fkIdBudget = em.getReference(fkIdBudget.getClass(), fkIdBudget.getIdBudget());
                procedure.setFkIdBudget(fkIdBudget);
            }
            Deed fkIdDeed = procedure.getFkIdDeed();
            if (fkIdDeed != null)
            {
                fkIdDeed = em.getReference(fkIdDeed.getClass(), fkIdDeed.getIdDeed());
                procedure.setFkIdDeed(fkIdDeed);
            }
            DeedManagement fkIdManagement = procedure.getFkIdManagement();
            if (fkIdManagement != null)
            {
                fkIdManagement = em.getReference(fkIdManagement.getClass(), fkIdManagement.getIdManagement());
                procedure.setFkIdManagement(fkIdManagement);
            }
            ProcedureType fkIdProcedureType = procedure.getFkIdProcedureType();
            if (fkIdProcedureType != null)
            {
                fkIdProcedureType = em.getReference(fkIdProcedureType.getClass(), fkIdProcedureType.getIdProcedureType());
                procedure.setFkIdProcedureType(fkIdProcedureType);
            }
            List<SubmittedDocument> attachedDocumentsSubmittedList = new ArrayList<SubmittedDocument>();
            for (SubmittedDocument documentsSubmittedListDocumentsSubmittedToAttach : procedure.getSubmittedDocumentList())
            {
                documentsSubmittedListDocumentsSubmittedToAttach = em.getReference(documentsSubmittedListDocumentsSubmittedToAttach.getClass(), documentsSubmittedListDocumentsSubmittedToAttach.getIdSubmittedDocument());
                attachedDocumentsSubmittedList.add(documentsSubmittedListDocumentsSubmittedToAttach);
            }
            procedure.setSubmittedDocumentList(attachedDocumentsSubmittedList);
            List<PersonProcedure> attachedPersonProcedureList = new ArrayList<PersonProcedure>();
            for (PersonProcedure personProcedureListPersonProcedureToAttach : procedure.getPersonProcedureList())
            {
                personProcedureListPersonProcedureToAttach = em.getReference(personProcedureListPersonProcedureToAttach.getClass(), personProcedureListPersonProcedureToAttach.getPersonProcedurePK());
                attachedPersonProcedureList.add(personProcedureListPersonProcedureToAttach);
            }
            procedure.setPersonProcedureList(attachedPersonProcedureList);
            em.persist(procedure);
            if (fkIdProperty != null)
            {
                fkIdProperty.getProcedureList().add(procedure);
                fkIdProperty = em.merge(fkIdProperty);
            }
            if (fkIdDeed != null)
            {
                fkIdDeed.getProcedureList().add(procedure);
                fkIdDeed = em.merge(fkIdDeed);
            }
            if (fkIdManagement != null)
            {
                fkIdManagement.getProcedureList().add(procedure);
                fkIdManagement = em.merge(fkIdManagement);
            }
            if (fkIdProcedureType != null)
            {
                fkIdProcedureType.getProcedureList().add(procedure);
                fkIdProcedureType = em.merge(fkIdProcedureType);
            }
            for (SubmittedDocument documentsSubmittedListDocumentsSubmitted : procedure.getSubmittedDocumentList())
            {
                Procedure oldFkIdProcedureOfDocumentsSubmittedListDocumentsSubmitted = documentsSubmittedListDocumentsSubmitted.getFkIdProcedure();
                documentsSubmittedListDocumentsSubmitted.setFkIdProcedure(procedure);
                documentsSubmittedListDocumentsSubmitted = em.merge(documentsSubmittedListDocumentsSubmitted);
                if (oldFkIdProcedureOfDocumentsSubmittedListDocumentsSubmitted != null)
                {
                    oldFkIdProcedureOfDocumentsSubmittedListDocumentsSubmitted.getSubmittedDocumentList().remove(documentsSubmittedListDocumentsSubmitted);
                    oldFkIdProcedureOfDocumentsSubmittedListDocumentsSubmitted = em.merge(oldFkIdProcedureOfDocumentsSubmittedListDocumentsSubmitted);
                }
            }
            for (PersonProcedure personProcedureListPersonProcedure : procedure.getPersonProcedureList())
            {
                Procedure oldProcedureOfPersonProcedureListPersonProcedure = personProcedureListPersonProcedure.getProcedure();
                personProcedureListPersonProcedure.setProcedure(procedure);
                personProcedureListPersonProcedure = em.merge(personProcedureListPersonProcedure);
                if (oldProcedureOfPersonProcedureListPersonProcedure != null)
                {
                    oldProcedureOfPersonProcedureListPersonProcedure.getPersonProcedureList().remove(personProcedureListPersonProcedure);
                    oldProcedureOfPersonProcedureListPersonProcedure = em.merge(oldProcedureOfPersonProcedureListPersonProcedure);
                }
            }
            em.getTransaction().commit();
            id = procedure.getIdProcedure();
        }
        finally
        {
            if (em != null)
            {
                em.close();
            }
        }
        return id;
    }

    public boolean edit(Procedure procedure) throws IllegalOrphanException, NonexistentEntityException, Exception
    {
        Boolean modificado = false;
        EntityManager em = null;
        try
        {
            em = getEntityManager();
            em.getTransaction().begin();
            Procedure persistentProcedure = em.find(Procedure.class, procedure.getIdProcedure());
            Property fkIdPropertyOld = persistentProcedure.getFkIdProperty();
            Property fkIdPropertyNew = procedure.getFkIdProperty();
            Budget fkIdBudgetOld = persistentProcedure.getFkIdBudget();
            Budget fkIdBudgetNew = procedure.getFkIdBudget();
            Deed fkIdDeedOld = persistentProcedure.getFkIdDeed();
            Deed fkIdDeedNew = procedure.getFkIdDeed();
            DeedManagement fkIdManagementOld = persistentProcedure.getFkIdManagement();
            DeedManagement fkIdManagementNew = procedure.getFkIdManagement();
            ProcedureType fkIdProcedureTypeOld = persistentProcedure.getFkIdProcedureType();
            ProcedureType fkIdProcedureTypeNew = procedure.getFkIdProcedureType();
            List<SubmittedDocument> documentsSubmittedListOld = persistentProcedure.getSubmittedDocumentList();
            List<SubmittedDocument> documentsSubmittedListNew = procedure.getSubmittedDocumentList();
            List<PersonProcedure> personProcedureListOld = persistentProcedure.getPersonProcedureList();
            List<PersonProcedure> personProcedureListNew = procedure.getPersonProcedureList();
            List<String> illegalOrphanMessages = null;
            if (fkIdBudgetOld != null && !fkIdBudgetOld.equals(fkIdBudgetNew))
            {
                if (illegalOrphanMessages == null)
                {
                    illegalOrphanMessages = new ArrayList<String>();
                }
                illegalOrphanMessages.add("You must retain Presupuesto " + fkIdBudgetOld + " since its fkIdTramite field is not nullable.");
            }
            for (SubmittedDocument documentsSubmittedListOldDocumentsSubmitted : documentsSubmittedListOld)
            {
                if (!documentsSubmittedListNew.contains(documentsSubmittedListOldDocumentsSubmitted))
                {
                    if (illegalOrphanMessages == null)
                    {
                        illegalOrphanMessages = new ArrayList<String>();
                    }
                    illegalOrphanMessages.add("You must retain DocumentosPresentado " + documentsSubmittedListOldDocumentsSubmitted + " since its fkIdTramite field is not nullable.");
                }
            }
            for (PersonProcedure personProcedureListOldPersonProcedure : personProcedureListOld)
            {
                if (!personProcedureListNew.contains(personProcedureListOldPersonProcedure))
                {
                    if (illegalOrphanMessages == null)
                    {
                        illegalOrphanMessages = new ArrayList<String>();
                    }
                    illegalOrphanMessages.add("You must retain TramitesPersonas " + personProcedureListOldPersonProcedure + " since its tramite field is not nullable.");
                }
            }
            if (illegalOrphanMessages != null)
            {
                throw new IllegalOrphanException(illegalOrphanMessages);
            }
            if (fkIdPropertyNew != null)
            {
                fkIdPropertyNew = em.getReference(fkIdPropertyNew.getClass(), fkIdPropertyNew.getIdProperty());
                procedure.setFkIdProperty(fkIdPropertyNew);
            }
            if (fkIdBudgetNew != null)
            {
                fkIdBudgetNew = em.getReference(fkIdBudgetNew.getClass(), fkIdBudgetNew.getIdBudget());
                procedure.setFkIdBudget(fkIdBudgetNew);
            }
            if (fkIdDeedNew != null)
            {
                fkIdDeedNew = em.getReference(fkIdDeedNew.getClass(), fkIdDeedNew.getIdDeed());
                procedure.setFkIdDeed(fkIdDeedNew);
            }
            if (fkIdManagementNew != null)
            {
                fkIdManagementNew = em.getReference(fkIdManagementNew.getClass(), fkIdManagementNew.getIdManagement());
                procedure.setFkIdManagement(fkIdManagementNew);
            }
            if (fkIdProcedureTypeNew != null)
            {
                fkIdProcedureTypeNew = em.getReference(fkIdProcedureTypeNew.getClass(), fkIdProcedureTypeNew.getIdProcedureType());
                procedure.setFkIdProcedureType(fkIdProcedureTypeNew);
            }
            List<SubmittedDocument> attachedDocumentsSubmittedListNew = new ArrayList<SubmittedDocument>();
            for (SubmittedDocument documentsSubmittedListNewDocumentsSubmittedToAttach : documentsSubmittedListNew)
            {
                documentsSubmittedListNewDocumentsSubmittedToAttach = em.getReference(documentsSubmittedListNewDocumentsSubmittedToAttach.getClass(), documentsSubmittedListNewDocumentsSubmittedToAttach.getIdSubmittedDocument());
                attachedDocumentsSubmittedListNew.add(documentsSubmittedListNewDocumentsSubmittedToAttach);
            }
            documentsSubmittedListNew = attachedDocumentsSubmittedListNew;
            procedure.setSubmittedDocumentList(documentsSubmittedListNew);
            List<PersonProcedure> attachedPersonProcedureListNew = new ArrayList<PersonProcedure>();
            for (PersonProcedure personProcedureListNewPersonProcedureToAttach : personProcedureListNew)
            {
                personProcedureListNewPersonProcedureToAttach = em.getReference(personProcedureListNewPersonProcedureToAttach.getClass(), personProcedureListNewPersonProcedureToAttach.getPersonProcedurePK());
                attachedPersonProcedureListNew.add(personProcedureListNewPersonProcedureToAttach);
            }
            personProcedureListNew = attachedPersonProcedureListNew;
            procedure.setPersonProcedureList(personProcedureListNew);
            procedure = em.merge(procedure);
            if (fkIdPropertyOld != null && !fkIdPropertyOld.equals(fkIdPropertyNew))
            {
                fkIdPropertyOld.getProcedureList().remove(procedure);
                fkIdPropertyOld = em.merge(fkIdPropertyOld);
            }
            if (fkIdPropertyNew != null && !fkIdPropertyNew.equals(fkIdPropertyOld))
            {
                fkIdPropertyNew.getProcedureList().add(procedure);
                fkIdPropertyNew = em.merge(fkIdPropertyNew);
            }
            if (fkIdDeedOld != null && !fkIdDeedOld.equals(fkIdDeedNew))
            {
                fkIdDeedOld.getProcedureList().remove(procedure);
                fkIdDeedOld = em.merge(fkIdDeedOld);
            }
            if (fkIdDeedNew != null && !fkIdDeedNew.equals(fkIdDeedOld))
            {
                fkIdDeedNew.getProcedureList().add(procedure);
                fkIdDeedNew = em.merge(fkIdDeedNew);
            }
            if (fkIdManagementOld != null && !fkIdManagementOld.equals(fkIdManagementNew))
            {
                fkIdManagementOld.getProcedureList().remove(procedure);
                fkIdManagementOld = em.merge(fkIdManagementOld);
            }
            if (fkIdManagementNew != null && !fkIdManagementNew.equals(fkIdManagementOld))
            {
                fkIdManagementNew.getProcedureList().add(procedure);
                fkIdManagementNew = em.merge(fkIdManagementNew);
            }
            if (fkIdProcedureTypeOld != null && !fkIdProcedureTypeOld.equals(fkIdProcedureTypeNew))
            {
                fkIdProcedureTypeOld.getProcedureList().remove(procedure);
                fkIdProcedureTypeOld = em.merge(fkIdProcedureTypeOld);
            }
            if (fkIdProcedureTypeNew != null && !fkIdProcedureTypeNew.equals(fkIdProcedureTypeOld))
            {
                fkIdProcedureTypeNew.getProcedureList().add(procedure);
                fkIdProcedureTypeNew = em.merge(fkIdProcedureTypeNew);
            }
            for (SubmittedDocument documentsSubmittedListNewDocumentsSubmitted : documentsSubmittedListNew)
            {
                if (!documentsSubmittedListOld.contains(documentsSubmittedListNewDocumentsSubmitted))
                {
                    Procedure oldFkIdProcedureOfDocumentsSubmittedListNewDocumentsSubmitted = documentsSubmittedListNewDocumentsSubmitted.getFkIdProcedure();
                    documentsSubmittedListNewDocumentsSubmitted.setFkIdProcedure(procedure);
                    documentsSubmittedListNewDocumentsSubmitted = em.merge(documentsSubmittedListNewDocumentsSubmitted);
                    if (oldFkIdProcedureOfDocumentsSubmittedListNewDocumentsSubmitted != null && !oldFkIdProcedureOfDocumentsSubmittedListNewDocumentsSubmitted.equals(procedure))
                    {
                        oldFkIdProcedureOfDocumentsSubmittedListNewDocumentsSubmitted.getSubmittedDocumentList().remove(documentsSubmittedListNewDocumentsSubmitted);
                        oldFkIdProcedureOfDocumentsSubmittedListNewDocumentsSubmitted = em.merge(oldFkIdProcedureOfDocumentsSubmittedListNewDocumentsSubmitted);
                    }
                }
            }
            for (PersonProcedure personProcedureListNewPersonProcedure : personProcedureListNew)
            {
                if (!personProcedureListOld.contains(personProcedureListNewPersonProcedure))
                {
                    Procedure oldProcedureOfPersonProcedureListNewPersonProcedure = personProcedureListNewPersonProcedure.getProcedure();
                    personProcedureListNewPersonProcedure.setProcedure(procedure);
                    personProcedureListNewPersonProcedure = em.merge(personProcedureListNewPersonProcedure);
                    if (oldProcedureOfPersonProcedureListNewPersonProcedure != null && !oldProcedureOfPersonProcedureListNewPersonProcedure.equals(procedure))
                    {
                        oldProcedureOfPersonProcedureListNewPersonProcedure.getPersonProcedureList().remove(personProcedureListNewPersonProcedure);
                        oldProcedureOfPersonProcedureListNewPersonProcedure = em.merge(oldProcedureOfPersonProcedureListNewPersonProcedure);
                    }
                }
            }
            em.getTransaction().commit();
            modificado = true;
        }
        catch (Exception ex)
        {
            String msg = ex.getLocalizedMessage();
            if (msg == null || msg.length() == 0)
            {
                Integer id = procedure.getIdProcedure();
                if (findProcedure(id) == null)
                {
                    throw new NonexistentEntityException("The tramite with id " + id + " no longer exists.");
                }
            }
            throw ex;
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

    public Boolean editProcedure(Procedure procedureModificado)
    {
        Boolean modificado = Boolean.FALSE;

        try
        {
            EntityManager em = getEntityManager();

            em.getTransaction().begin();

            Procedure procedureViejo = em.find(Procedure.class, procedureModificado.getIdProcedure());

            procedureViejo.setFkIdManagement(procedureModificado.getFkIdManagement());
            procedureViejo.setFkIdDeed(procedureModificado.getFkIdDeed());

            em.getTransaction().commit();
            em.close();
            modificado = Boolean.TRUE;
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return modificado;
    }

    public Boolean editProcedureManagementOnly(Procedure procedureModificado)
    {
        Boolean modificado = Boolean.FALSE;

        try
        {
            EntityManager em = getEntityManager();

            em.getTransaction().begin();

            Procedure procedureViejo = em.find(Procedure.class, procedureModificado.getIdProcedure());

            procedureViejo.setFkIdManagement(procedureModificado.getFkIdManagement());

            em.getTransaction().commit();
            em.close();
            modificado = Boolean.TRUE;
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return modificado;
    }

    public void destroy(Integer id) throws IllegalOrphanException, NonexistentEntityException
    {
        EntityManager em = null;
        try
        {
            em = getEntityManager();
            em.getTransaction().begin();
            Procedure procedure;
            try
            {
                procedure = em.getReference(Procedure.class, id);
                procedure.getIdProcedure();
            }
            catch (EntityNotFoundException enfe)
            {
                throw new NonexistentEntityException("The tramite with id " + id + " no longer exists.", enfe);
            }
            List<String> illegalOrphanMessages = null;
            Budget fkIdBudgetOrphanCheck = procedure.getFkIdBudget();
            if (fkIdBudgetOrphanCheck != null)
            {
                if (illegalOrphanMessages == null)
                {
                    illegalOrphanMessages = new ArrayList<String>();
                }
                illegalOrphanMessages.add("This Tramite (" + procedure + ") cannot be destroyed since the Presupuesto " + fkIdBudgetOrphanCheck + " in its fkIdPresupuesto field has a non-nullable fkIdTramite field.");
            }
            List<SubmittedDocument> documentsSubmittedListOrphanCheck = procedure.getSubmittedDocumentList();
            for (SubmittedDocument documentsSubmittedListOrphanCheckDocumentsSubmitted : documentsSubmittedListOrphanCheck)
            {
                if (illegalOrphanMessages == null)
                {
                    illegalOrphanMessages = new ArrayList<String>();
                }
                illegalOrphanMessages.add("This Tramite (" + procedure + ") cannot be destroyed since the DocumentosPresentado " + documentsSubmittedListOrphanCheckDocumentsSubmitted + " in its documentosPresentadoList field has a non-nullable fkIdTramite field.");
            }
            List<PersonProcedure> personProcedureListOrphanCheck = procedure.getPersonProcedureList();
            for (PersonProcedure personProcedureListOrphanCheckPersonProcedure : personProcedureListOrphanCheck)
            {
                if (illegalOrphanMessages == null)
                {
                    illegalOrphanMessages = new ArrayList<String>();
                }
                illegalOrphanMessages.add("This Tramite (" + procedure + ") cannot be destroyed since the TramitesPersonas " + personProcedureListOrphanCheckPersonProcedure + " in its tramitesPersonasList field has a non-nullable tramite field.");
            }
            if (illegalOrphanMessages != null)
            {
                throw new IllegalOrphanException(illegalOrphanMessages);
            }
            Property fkIdProperty = procedure.getFkIdProperty();
            if (fkIdProperty != null)
            {
                fkIdProperty.getProcedureList().remove(procedure);
                fkIdProperty = em.merge(fkIdProperty);
            }
            Deed fkIdDeed = procedure.getFkIdDeed();
            if (fkIdDeed != null)
            {
                fkIdDeed.getProcedureList().remove(procedure);
                fkIdDeed = em.merge(fkIdDeed);
            }
            DeedManagement fkIdManagement = procedure.getFkIdManagement();
            if (fkIdManagement != null)
            {
                fkIdManagement.getProcedureList().remove(procedure);
                fkIdManagement = em.merge(fkIdManagement);
            }
            ProcedureType fkIdProcedureType = procedure.getFkIdProcedureType();
            if (fkIdProcedureType != null)
            {
                fkIdProcedureType.getProcedureList().remove(procedure);
                fkIdProcedureType = em.merge(fkIdProcedureType);
            }
            em.remove(procedure);
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

    public List<Procedure> findProcedureEntities()
    {
        return findProcedureEntities(true, -1, -1);
    }

    public List<Procedure> findProcedureEntities(int maxResults, int firstResult)
    {
        return findProcedureEntities(false, maxResults, firstResult);
    }

    private List<Procedure> findProcedureEntities(boolean all, int maxResults, int firstResult)
    {
        EntityManager em = getEntityManager();
        try
        {
            Query q = em.createQuery("select object(o) from Procedure as o");

            if (!all)
            {
                q.setMaxResults(maxResults);
                q.setFirstResult(firstResult);
            }
            List<Procedure> listaProcedures = q.getResultList();

            for (Iterator<Procedure> it = listaProcedures.iterator(); it.hasNext();)
            {
                Procedure procedure = it.next();
                procedure.setPersonList(new ArrayList<Person>());
                procedure.setSubmittedDocumentList(new ArrayList<SubmittedDocument>());
            }

            return listaProcedures;
        }
        finally
        {
            em.close();
        }
    }

    public Procedure findProcedure(Integer id)
    {
        EntityManager em = getEntityManager();
        try
        {
            return em.find(Procedure.class, id);
        }
        finally
        {
            em.close();
        }
    }

    public Procedure encontrarProcedure(Integer id)
    {
        Procedure procedure = new Procedure();
        EntityManager em = getEntityManager();
        try
        {
            Procedure miProcedure = em.find(Procedure.class, id);

            procedure.setIdProcedure(miProcedure.getIdProcedure());
            procedure.setFkIdDeed(miProcedure.getFkIdDeed());
            procedure.setFkIdManagement(miProcedure.getFkIdManagement());
            procedure.setFkIdProperty(miProcedure.getFkIdProperty());
            procedure.setFkIdBudget(miProcedure.getFkIdBudget());
            procedure.setFkIdProcedureType(miProcedure.getFkIdProcedureType());
            procedure.setNotes(miProcedure.getNotes());
            procedure.setVersion(miProcedure.getVersion());
        }
        finally
        {
            em.close();
        }
        return procedure;
    }

    public List<Procedure> encontrarProcedureBudget(Integer idBudget)
    {

        EntityManager em = getEntityManager();

        List<Procedure> miProcedure = null;

        try
        {
            // Nombre del @NamedQuery definido en nuestra clase
            Query q = em.createNamedQuery("Tramite.findByIdPresupuesto");

            //Le paso el nombre del parametro del query, y el valor a buscar.
            q.setParameter("idPresupuesto", idBudget);

            miProcedure = (List<Procedure>) q.getResultList();

        }
        finally
        {
            em.close();
        }
        return miProcedure;
    }

    public int getProcedureCount()
    {
        EntityManager em = getEntityManager();
        try
        {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            Root<Procedure> rt = cq.from(Procedure.class);
            cq.select(em.getCriteriaBuilder().count(rt));
            Query q = em.createQuery(cq);
            return ((Long) q.getSingleResult()).intValue();
        }
        finally
        {
            em.close();
        }
    }

    public Boolean asociarBudget(Procedure miProcedure)
    {
        Boolean modificado = false;
        EntityManager em = this.getEntityManager();
        EntityTransaction tx = null;

        // Abro una nueva transaccion
        tx = em.getTransaction();
        tx.begin();

        Query q = em.createQuery("UPDATE Procedure t SET t.fkIdBudget.idBudget = :idPresupuesto WHERE t.idProcedure = :id");
        q.setParameter("idPresupuesto", miProcedure.getFkIdBudget().getIdBudget());
        q.setParameter("id", miProcedure.getIdProcedure());

        int updated = q.executeUpdate();

        tx.commit();

        em.close();

        if (updated > 0)
        {
            modificado = true;
        }

        return modificado;
    }

    @Override
    public String getNameJpa()
    {
        return this.getClass().getName();
    }
}
