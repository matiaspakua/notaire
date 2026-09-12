/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.licensis.notaire.jpa;

import com.licensis.notaire.jpa.exceptions.ClassEliminatedException;
import com.licensis.notaire.jpa.exceptions.ClassModifiedException;
import com.licensis.notaire.jpa.exceptions.IllegalOrphanException;
import com.licensis.notaire.jpa.exceptions.NonexistentEntityException;
import com.licensis.notaire.jpa.interfaz.IPersistenciaJpa;
import com.licensis.notaire.business.BudgetTemplate;
import com.licensis.notaire.business.ProcedureTemplate;
import com.licensis.notaire.business.ProcedureType;
import com.licensis.notaire.business.Procedure;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.Query;
import jakarta.transaction.UserTransaction;

/**
 *
 * @author User
 */
public class ProcedureTypeJpaController implements Serializable, IPersistenciaJpa
{

    public ProcedureTypeJpaController(UserTransaction utx, EntityManagerFactory emf)
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

    public int create(ProcedureType procedureType)
    {
        int oid = -1;
        if (procedureType.getBudgetTemplateList() == null)
        {
            procedureType.setBudgetTemplateList(new ArrayList<BudgetTemplate>());
        }
        if (procedureType.getProcedureTemplateList() == null)
        {
            procedureType.setProcedureTemplateList(new ArrayList<ProcedureTemplate>());
        }
        if (procedureType.getProcedureList() == null)
        {
            procedureType.setProcedureList(new ArrayList<Procedure>());
        }
        EntityManager em = null;
        try
        {
            em = getEntityManager();
            em.getTransaction().begin();
            List<BudgetTemplate> attachedBudgetTemplateList = new ArrayList<BudgetTemplate>();
            for (BudgetTemplate budgetTemplateListBudgetTemplateToAttach : procedureType.getBudgetTemplateList())
            {
                budgetTemplateListBudgetTemplateToAttach = em.getReference(budgetTemplateListBudgetTemplateToAttach.getClass(), budgetTemplateListBudgetTemplateToAttach.getBudgetTemplatePK());
                attachedBudgetTemplateList.add(budgetTemplateListBudgetTemplateToAttach);
            }
            procedureType.setBudgetTemplateList(attachedBudgetTemplateList);
            List<ProcedureTemplate> attachedProcedureTemplateList = new ArrayList<ProcedureTemplate>();
            for (ProcedureTemplate procedureTemplateListProcedureTemplateToAttach : procedureType.getProcedureTemplateList())
            {
                procedureTemplateListProcedureTemplateToAttach = em.getReference(procedureTemplateListProcedureTemplateToAttach.getClass(), procedureTemplateListProcedureTemplateToAttach.getProcedureTemplatePK());
                attachedProcedureTemplateList.add(procedureTemplateListProcedureTemplateToAttach);
            }
            procedureType.setProcedureTemplateList(attachedProcedureTemplateList);
            List<Procedure> attachedProcedureList = new ArrayList<Procedure>();
            for (Procedure procedureListProcedureToAttach : procedureType.getProcedureList())
            {
                procedureListProcedureToAttach = em.getReference(procedureListProcedureToAttach.getClass(), procedureListProcedureToAttach.getIdProcedure());
                attachedProcedureList.add(procedureListProcedureToAttach);
            }
            procedureType.setProcedureList(attachedProcedureList);
            em.persist(procedureType);
            for (BudgetTemplate budgetTemplateListBudgetTemplate : procedureType.getBudgetTemplateList())
            {
                ProcedureType oldProcedureTypeOfBudgetTemplateListBudgetTemplate = budgetTemplateListBudgetTemplate.getProcedureType();
                budgetTemplateListBudgetTemplate.setProcedureType(procedureType);
                budgetTemplateListBudgetTemplate = em.merge(budgetTemplateListBudgetTemplate);
                if (oldProcedureTypeOfBudgetTemplateListBudgetTemplate != null)
                {
                    oldProcedureTypeOfBudgetTemplateListBudgetTemplate.getBudgetTemplateList().remove(budgetTemplateListBudgetTemplate);
                    oldProcedureTypeOfBudgetTemplateListBudgetTemplate = em.merge(oldProcedureTypeOfBudgetTemplateListBudgetTemplate);
                }
            }
            for (ProcedureTemplate procedureTemplateListProcedureTemplate : procedureType.getProcedureTemplateList())
            {
                ProcedureType oldProcedureTypeOfProcedureTemplateListProcedureTemplate = procedureTemplateListProcedureTemplate.getProcedureType();
                procedureTemplateListProcedureTemplate.setProcedureType(procedureType);
                procedureTemplateListProcedureTemplate = em.merge(procedureTemplateListProcedureTemplate);
                if (oldProcedureTypeOfProcedureTemplateListProcedureTemplate != null)
                {
                    oldProcedureTypeOfProcedureTemplateListProcedureTemplate.getProcedureTemplateList().remove(procedureTemplateListProcedureTemplate);
                    oldProcedureTypeOfProcedureTemplateListProcedureTemplate = em.merge(oldProcedureTypeOfProcedureTemplateListProcedureTemplate);
                }
            }
            for (Procedure procedureListProcedure : procedureType.getProcedureList())
            {
                ProcedureType oldFkIdProcedureTypeOfProcedureListProcedure = procedureListProcedure.getFkIdProcedureType();
                procedureListProcedure.setFkIdProcedureType(procedureType);
                procedureListProcedure = em.merge(procedureListProcedure);
                if (oldFkIdProcedureTypeOfProcedureListProcedure != null)
                {
                    oldFkIdProcedureTypeOfProcedureListProcedure.getProcedureList().remove(procedureListProcedure);
                    oldFkIdProcedureTypeOfProcedureListProcedure = em.merge(oldFkIdProcedureTypeOfProcedureListProcedure);
                }
            }
            em.getTransaction().commit();
            oid = procedureType.getIdProcedureType();
        }
        finally
        {
            if (em != null)
            {
                em.close();
            }
        }
        return oid;
    }

    public Boolean edit(ProcedureType procedureType) throws ClassModifiedException, ClassEliminatedException, IllegalOrphanException, NonexistentEntityException
    {

        Boolean modificado = false;
        Integer version = -1;
        Integer oldVersion = -1;

        EntityManager em = getEntityManager();

        em.getTransaction().begin();

        ProcedureType procedureTypeEncontrado = em.find(ProcedureType.class, procedureType.getIdProcedureType());

        if (procedureTypeEncontrado != null)
        {
            version = procedureTypeEncontrado.getVersion(); // Version del Objeto en db
            oldVersion = procedureType.getVersion(); // Version del Objeto en memoria

            if (version != oldVersion) //Si son distintas "Alguien modifico el objeto"
            {
                if (em != null)
                {
                    em.close();
                }

                throw new ClassModifiedException();

            } else
            {

                procedureTypeEncontrado.setEnabled(procedureType.getEnabled());
                procedureTypeEncontrado.setName(procedureType.getName());
                procedureTypeEncontrado.setIsArchived(procedureType.getIsArchived());
                procedureTypeEncontrado.setIsRegistered(procedureType.getIsRegistered());
                procedureTypeEncontrado.setAssociatesProperties(procedureType.getAssociatesProperties());
                procedureTypeEncontrado.setNotes(procedureType.getNotes());

                em.getTransaction().commit();
                em.close();
                modificado = true;

            }
        } else
        {
            throw new ClassEliminatedException();
        }

        return modificado;
    }

    public Boolean destroy(Integer id) throws ClassEliminatedException, IllegalOrphanException, NonexistentEntityException
    {
        EntityManager em = null;
        Boolean eliminado = false;
        try
        {
            em = getEntityManager();
            em.getTransaction().begin();

            ProcedureType procedureType = em.find(ProcedureType.class, id);

            if (procedureType != null)
            {
                procedureType = em.getReference(ProcedureType.class, id);
                procedureType.getIdProcedureType();

                List<String> illegalOrphanMessages = null;
                List<BudgetTemplate> budgetTemplateListOrphanCheck = procedureType.getBudgetTemplateList();
                for (BudgetTemplate budgetTemplateListOrphanCheckBudgetTemplate : budgetTemplateListOrphanCheck)
                {
                    if (illegalOrphanMessages == null)
                    {
                        illegalOrphanMessages = new ArrayList<String>();
                    }
                    illegalOrphanMessages.add("This TipoDeTramite (" + procedureType + ") cannot be destroyed since the PlantillaPresupuesto " + budgetTemplateListOrphanCheckBudgetTemplate + " in its plantillaPresupuestoList field has a non-nullable tipoDeTramite field.");
                }
                List<ProcedureTemplate> procedureTemplateListOrphanCheck = procedureType.getProcedureTemplateList();
                for (ProcedureTemplate procedureTemplateListOrphanCheckProcedureTemplate : procedureTemplateListOrphanCheck)
                {
                    if (illegalOrphanMessages == null)
                    {
                        illegalOrphanMessages = new ArrayList<String>();
                    }
                    illegalOrphanMessages.add("This TipoDeTramite (" + procedureType + ") cannot be destroyed since the PlantillaTramite " + procedureTemplateListOrphanCheckProcedureTemplate + " in its plantillaTramiteList field has a non-nullable tipoDeTramite field.");
                }
                List<Procedure> procedureListOrphanCheck = procedureType.getProcedureList();
                for (Procedure procedureListOrphanCheckProcedure : procedureListOrphanCheck)
                {
                    if (illegalOrphanMessages == null)
                    {
                        illegalOrphanMessages = new ArrayList<String>();
                    }
                    illegalOrphanMessages.add("This TipoDeTramite (" + procedureType + ") cannot be destroyed since the Tramite " + procedureListOrphanCheckProcedure + " in its tramiteList field has a non-nullable fkIdTipoTramite field.");
                }
                if (illegalOrphanMessages != null)
                {
                    throw new IllegalOrphanException(illegalOrphanMessages);
                }
                em.remove(procedureType);
                em.getTransaction().commit();
                eliminado = true;
            } else
            {
                throw new ClassEliminatedException();
            }
        }
        catch (EntityNotFoundException enfe)
        {
            throw new NonexistentEntityException("The concepto with id " + id + " no longer exists.", enfe);
        }
        finally
        {
            if (em != null)
            {
                em.close();
            }
        }
        return eliminado;
    }

    public List<ProcedureType> findProcedureTypeEntities()
    {
        return findProcedureTypeEntities(true, -1, -1);
    }

    public List<ProcedureType> findProcedureTypeEntities(int maxResults, int firstResult)
    {
        return findProcedureTypeEntities(false, maxResults, firstResult);
    }

    private List<ProcedureType> findProcedureTypeEntities(boolean all, int maxResults, int firstResult)
    {
        EntityManager em = getEntityManager();
        try
        {
            Query q = em.createQuery("select object(o) from ProcedureType as o");
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

    public List<ProcedureType> findProcedureType(Integer id)
    {
        EntityManager em = getEntityManager();

        List<ProcedureType> misTiposDeProcedure = null;

        try
        {
            // Nombre del @NamedQuery definido en nuestra clase
            Query q = em.createNamedQuery("TipoDeTramite.findByIdTipoTramite");

            //Le paso el nombre del parametro del query, y el valor a buscar.
            q.setParameter("idTipoTramite", id);

            misTiposDeProcedure = (List<ProcedureType>) q.getResultList();

            if (misTiposDeProcedure != null)
            {

                misTiposDeProcedure.get(0).setBudgetTemplateList(new ArrayList<BudgetTemplate>());
                misTiposDeProcedure.get(0).setProcedureTemplateList(new ArrayList<ProcedureTemplate>());
                misTiposDeProcedure.get(0).setProcedureList(new ArrayList<Procedure>());
            }

            return misTiposDeProcedure;

        }
        finally
        {
            em.close();
        }
    }

    public List<ProcedureType> findProcedureType(String name)
    {
        EntityManager em = getEntityManager();

        List<ProcedureType> misTiposDeProcedure = null;

        try
        {
            // Nombre del @NamedQuery definido en nuestra clase
            Query q = em.createNamedQuery("TipoDeTramite.findByNombre");

            //Le paso el nombre del parametro del query, y el valor a buscar.
            q.setParameter("nombre", name);

            misTiposDeProcedure = (List<ProcedureType>) q.getResultList();

            return misTiposDeProcedure;

        }
        finally
        {
            em.close();
        }
    }

    public int getProcedureTypeCount()
    {
        EntityManager em = getEntityManager();
        try
        {
            Query q = em.createQuery("select count(o) from ProcedureType as o");
            return ((Long) q.getSingleResult()).intValue();
        }
        finally
        {
            em.close();
        }
    }

    @Override
    public String getNameJpa()
    {
        return this.getClass().getName();
    }
}
