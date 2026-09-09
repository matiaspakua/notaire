/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.licensis.notaire.jpa;

import com.licensis.notaire.jpa.exceptions.ClassEliminatedException;
import com.licensis.notaire.jpa.exceptions.NonexistentEntityException;
import com.licensis.notaire.jpa.exceptions.PreexistingEntityException;
import com.licensis.notaire.jpa.interfaz.IPersistenciaJpa;
import com.licensis.notaire.business.Concept;
import com.licensis.notaire.business.BudgetTemplate;
import com.licensis.notaire.business.BudgetTemplatePK;
import com.licensis.notaire.business.ProcedureType;
import java.io.Serializable;
import java.util.List;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.Query;
import jakarta.transaction.UserTransaction;

/**
 *
 * @author User
 */
public class BudgetTemplateJpaController implements Serializable, IPersistenciaJpa
{

    public BudgetTemplateJpaController(UserTransaction utx, EntityManagerFactory emf)
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

    public Boolean create(BudgetTemplate budgetTemplate) throws PreexistingEntityException
    {
        Boolean creada = false;

        if (budgetTemplate.getBudgetTemplatePK() == null)
        {
            budgetTemplate.setBudgetTemplatePK(new BudgetTemplatePK());
        }
        budgetTemplate.getBudgetTemplatePK().setFkIdConcept(budgetTemplate.getConcept().getIdConcept());
        budgetTemplate.getBudgetTemplatePK().setFkIdProcedureType(budgetTemplate.getProcedureType().getIdProcedureType());
        if (findBudgetTemplate(budgetTemplate.getBudgetTemplatePK()) != null)
        {
            throw new PreexistingEntityException("PlantillaPresupuesto " + budgetTemplate.getBudgetTemplatePK() + " already exists.");
        }
        EntityManager em = null;
        try
        {
            em = getEntityManager();
            em.getTransaction().begin();
            ProcedureType procedureType = budgetTemplate.getProcedureType();
            if (procedureType != null)
            {
                procedureType = em.getReference(procedureType.getClass(), procedureType.getIdProcedureType());
                budgetTemplate.setProcedureType(procedureType);
            }
            Concept concept = budgetTemplate.getConcept();
            if (concept != null)
            {
                concept = em.getReference(concept.getClass(), concept.getIdConcept());
                budgetTemplate.setConcept(concept);
            }
            em.persist(budgetTemplate);
            if (procedureType != null)
            {
                procedureType.getBudgetTemplateList().add(budgetTemplate);
                procedureType = em.merge(procedureType);
            }
            if (concept != null)
            {
                concept.getBudgetTemplateList().add(budgetTemplate);
                concept = em.merge(concept);
            }
            em.getTransaction().commit();
            creada = true;
        }
        catch (Exception ex)
        {
            if (findBudgetTemplate(budgetTemplate.getBudgetTemplatePK()) != null)
            {
                throw new PreexistingEntityException("PlantillaPresupuesto " + budgetTemplate + " already exists.", ex);
            }
        }
        finally
        {
            if (em != null)
            {
                em.close();
            }
        }
        return creada;
    }

    public void edit(BudgetTemplate budgetTemplate) throws NonexistentEntityException, Exception
    {
        budgetTemplate.getBudgetTemplatePK().setFkIdConcept(budgetTemplate.getConcept().getIdConcept());
        budgetTemplate.getBudgetTemplatePK().setFkIdProcedureType(budgetTemplate.getProcedureType().getIdProcedureType());
        EntityManager em = null;
        try
        {
            em = getEntityManager();
            em.getTransaction().begin();
            BudgetTemplate persistentBudgetTemplate = em.find(BudgetTemplate.class, budgetTemplate.getBudgetTemplatePK());
            ProcedureType procedureTypeOld = persistentBudgetTemplate.getProcedureType();
            ProcedureType procedureTypeNew = budgetTemplate.getProcedureType();
            Concept conceptOld = persistentBudgetTemplate.getConcept();
            Concept conceptNew = budgetTemplate.getConcept();
            if (procedureTypeNew != null)
            {
                procedureTypeNew = em.getReference(procedureTypeNew.getClass(), procedureTypeNew.getIdProcedureType());
                budgetTemplate.setProcedureType(procedureTypeNew);
            }
            if (conceptNew != null)
            {
                conceptNew = em.getReference(conceptNew.getClass(), conceptNew.getIdConcept());
                budgetTemplate.setConcept(conceptNew);
            }
            budgetTemplate = em.merge(budgetTemplate);
            if (procedureTypeOld != null && !procedureTypeOld.equals(procedureTypeNew))
            {
                procedureTypeOld.getBudgetTemplateList().remove(budgetTemplate);
                procedureTypeOld = em.merge(procedureTypeOld);
            }
            if (procedureTypeNew != null && !procedureTypeNew.equals(procedureTypeOld))
            {
                procedureTypeNew.getBudgetTemplateList().add(budgetTemplate);
                procedureTypeNew = em.merge(procedureTypeNew);
            }
            if (conceptOld != null && !conceptOld.equals(conceptNew))
            {
                conceptOld.getBudgetTemplateList().remove(budgetTemplate);
                conceptOld = em.merge(conceptOld);
            }
            if (conceptNew != null && !conceptNew.equals(conceptOld))
            {
                conceptNew.getBudgetTemplateList().add(budgetTemplate);
                conceptNew = em.merge(conceptNew);
            }
            em.getTransaction().commit();
        }
        catch (Exception ex)
        {
            String msg = ex.getLocalizedMessage();
            if (msg == null || msg.length() == 0)
            {
                BudgetTemplatePK id = budgetTemplate.getBudgetTemplatePK();
                if (findBudgetTemplate(id) == null)
                {
                    throw new NonexistentEntityException("The plantillaPresupuesto with id " + id + " no longer exists.");
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
    }

    public void destroy(BudgetTemplatePK id) throws NonexistentEntityException
    {
        EntityManager em = null;
        try
        {
            em = getEntityManager();
            em.getTransaction().begin();
            BudgetTemplate budgetTemplate;
            try
            {
                budgetTemplate = em.getReference(BudgetTemplate.class, id);
                budgetTemplate.getBudgetTemplatePK();
            }
            catch (EntityNotFoundException enfe)
            {
                throw new NonexistentEntityException("The plantillaPresupuesto with id " + id + " no longer exists.", enfe);
            }
            ProcedureType procedureType = budgetTemplate.getProcedureType();
            if (procedureType != null)
            {
                procedureType.getBudgetTemplateList().remove(budgetTemplate);
                procedureType = em.merge(procedureType);
            }
            Concept concept = budgetTemplate.getConcept();
            if (concept != null)
            {
                concept.getBudgetTemplateList().remove(budgetTemplate);
                concept = em.merge(concept);
            }
            em.remove(budgetTemplate);
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

    public List<BudgetTemplate> findBudgetTemplateEntities()
    {
        return findBudgetTemplateEntities(true, -1, -1);
    }

    public List<BudgetTemplate> findBudgetTemplateEntities(int maxResults, int firstResult)
    {
        return findBudgetTemplateEntities(false, maxResults, firstResult);
    }

    private List<BudgetTemplate> findBudgetTemplateEntities(boolean all, int maxResults, int firstResult)
    {
        EntityManager em = getEntityManager();
        try
        {
            Query q = em.createQuery("select object(o) from BudgetTemplate as o");
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

    public BudgetTemplate findBudgetTemplate(BudgetTemplatePK id)
    {
        EntityManager em = getEntityManager();
        try
        {
            return em.find(BudgetTemplate.class, id);
        }
        finally
        {
            em.close();
        }
    }

    public int getBudgetTemplateCount()
    {
        EntityManager em = getEntityManager();
        try
        {
            Query q = em.createQuery("select count(o) from BudgetTemplate as o");
            return ((Long) q.getSingleResult()).intValue();
        }
        finally
        {
            em.close();
        }
    }

    public List<BudgetTemplate> findPlantillasDeBudget(int idProcedureType)
    {
        EntityManager em = getEntityManager();

        List<BudgetTemplate> misPlantillas = null;

        try
        {
            // Nombre del @NamedQuery definido en nuestra clase
            Query q = em.createNamedQuery("PlantillaPresupuesto.findByFkIdTipoTramite");

            //Le paso el nombre del parametro del query, y el valor a buscar.
            q.setParameter("fkIdTipoTramite", idProcedureType);

            misPlantillas = (List<BudgetTemplate>) q.getResultList();

            return misPlantillas;

        }
        finally
        {
            em.close();
        }
    }

    public Boolean eliminarBudgetTemplate(BudgetTemplate miTemplate) throws ClassEliminatedException
    {

        Boolean eliminada = false;
        int deleted = 0;

        EntityManager em = this.getEntityManager();
        EntityTransaction tx = null;

        // Abro una nueva transaccion
        tx = em.getTransaction();
        tx.begin();

        BudgetTemplate miBudgetTemplate = this.findBudgetTemplate(miTemplate.getBudgetTemplatePK());

        if (miBudgetTemplate != null)
        {

            // Creo el query
            Query query = em.createQuery("DELETE FROM BudgetTemplate p WHERE p.budgetTemplatePK.fkIdProcedureType = ?1 AND  p.budgetTemplatePK.fkIdConcept = ?2");

            //Seteo el parametro 1 y 2
            query.setParameter(1, miTemplate.getBudgetTemplatePK().getFkIdProcedureType());
            query.setParameter(2, miTemplate.getBudgetTemplatePK().getFkIdConcept());

            // Ejecuto el query
            deleted = query.executeUpdate();

            // Hago commit
            tx.commit();

            if (deleted > 0)
            {
                eliminada = true;
            }
        } else
        {
            throw new ClassEliminatedException();
        }

        em.close();

        return eliminada;
    }

    @Override
    public String getNameJpa()
    {
        return this.getClass().getName();
    }
}
