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
import com.licensis.notaire.business.Concept;
import com.licensis.notaire.business.BudgetTemplate;
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
public class ConceptJpaController implements Serializable, IPersistenciaJpa
{

    private static ConceptJpaController instancia = null;
    private UserTransaction utx = null;
    private EntityManagerFactory emf = null;

    public ConceptJpaController(UserTransaction utx, EntityManagerFactory emf)
    {
        this.utx = utx;
        this.emf = emf;
    }

    public EntityManager getEntityManager()
    {
        return emf.createEntityManager();
    }

    public Integer create(Concept concept)
    {
        Integer oid = null;

        if (concept.getBudgetTemplateList() == null)
        {
            concept.setBudgetTemplateList(new ArrayList<BudgetTemplate>());
        }
        EntityManager em = null;
        try
        {
            em = getEntityManager();
            em.getTransaction().begin();

            List<BudgetTemplate> attachedBudgetTemplateList = new ArrayList<BudgetTemplate>();
            for (BudgetTemplate budgetTemplateListBudgetTemplateToAttach : concept.getBudgetTemplateList())
            {
                budgetTemplateListBudgetTemplateToAttach = em.getReference(budgetTemplateListBudgetTemplateToAttach.getClass(), budgetTemplateListBudgetTemplateToAttach.getBudgetTemplatePK());
                attachedBudgetTemplateList.add(budgetTemplateListBudgetTemplateToAttach);
            }
            concept.setBudgetTemplateList(attachedBudgetTemplateList);

            em.persist(concept);

            for (BudgetTemplate budgetTemplateListBudgetTemplate : concept.getBudgetTemplateList())
            {
                Concept oldConceptOfBudgetTemplateListBudgetTemplate = budgetTemplateListBudgetTemplate.getConcept();
                budgetTemplateListBudgetTemplate.setConcept(concept);
                budgetTemplateListBudgetTemplate = em.merge(budgetTemplateListBudgetTemplate);
                if (oldConceptOfBudgetTemplateListBudgetTemplate != null)
                {
                    oldConceptOfBudgetTemplateListBudgetTemplate.getBudgetTemplateList().remove(budgetTemplateListBudgetTemplate);
                    oldConceptOfBudgetTemplateListBudgetTemplate = em.merge(oldConceptOfBudgetTemplateListBudgetTemplate);
                }
            }
            em.getTransaction().commit();

            oid = concept.getIdConcept();
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

    public Boolean edit(Concept concept) throws ClassEliminatedException, ClassModifiedException, IllegalOrphanException, NonexistentEntityException
    {
        Boolean resultado = Boolean.FALSE;
        Integer version = ConstantesPersistencia.VersionINICIAL;
        Integer oldVersion = ConstantesPersistencia.VersionINICIAL;
        EntityManager em = null;

        em = getEntityManager();
        em.getTransaction().begin();

        Concept persistentConcept = em.find(Concept.class, concept.getIdConcept());

        if (persistentConcept != null)
        {
            version = persistentConcept.getVersion(); // Version del Objeto en db
            oldVersion = concept.getVersion(); // Version del Objeto en memoria

            if (version != oldVersion) //Si son distintas "Alguien modifico el objeto"
            {
                if (em != null)
                {
                    em.close();
                }

                throw new ClassModifiedException();

            } else
            {
                List<BudgetTemplate> budgetTemplateListOld = persistentConcept.getBudgetTemplateList();
                List<BudgetTemplate> budgetTemplateListNew = concept.getBudgetTemplateList();
                List<String> illegalOrphanMessages = null;
                for (BudgetTemplate budgetTemplateListOldBudgetTemplate : budgetTemplateListOld)
                {
                    if (!budgetTemplateListNew.contains(budgetTemplateListOldBudgetTemplate))
                    {
                        if (illegalOrphanMessages == null)
                        {
                            illegalOrphanMessages = new ArrayList<String>();
                        }
                        illegalOrphanMessages.add("You must retain PlantillaPresupuesto " + budgetTemplateListOldBudgetTemplate + " since its concepto field is not nullable.");
                    }
                }
                if (illegalOrphanMessages != null)
                {
                    throw new IllegalOrphanException(illegalOrphanMessages);
                }
                List<BudgetTemplate> attachedBudgetTemplateListNew = new ArrayList<BudgetTemplate>();
                for (BudgetTemplate budgetTemplateListNewBudgetTemplateToAttach : budgetTemplateListNew)
                {
                    budgetTemplateListNewBudgetTemplateToAttach = em.getReference(budgetTemplateListNewBudgetTemplateToAttach.getClass(), budgetTemplateListNewBudgetTemplateToAttach.getBudgetTemplatePK());
                    attachedBudgetTemplateListNew.add(budgetTemplateListNewBudgetTemplateToAttach);
                }
                budgetTemplateListNew = attachedBudgetTemplateListNew;
                concept.setBudgetTemplateList(budgetTemplateListNew);
                concept = em.merge(concept);
                for (BudgetTemplate budgetTemplateListNewBudgetTemplate : budgetTemplateListNew)
                {
                    if (!budgetTemplateListOld.contains(budgetTemplateListNewBudgetTemplate))
                    {
                        Concept oldConceptOfBudgetTemplateListNewBudgetTemplate = budgetTemplateListNewBudgetTemplate.getConcept();
                        budgetTemplateListNewBudgetTemplate.setConcept(concept);
                        budgetTemplateListNewBudgetTemplate = em.merge(budgetTemplateListNewBudgetTemplate);
                        if (oldConceptOfBudgetTemplateListNewBudgetTemplate != null && !oldConceptOfBudgetTemplateListNewBudgetTemplate.equals(concept))
                        {
                            oldConceptOfBudgetTemplateListNewBudgetTemplate.getBudgetTemplateList().remove(budgetTemplateListNewBudgetTemplate);
                            oldConceptOfBudgetTemplateListNewBudgetTemplate = em.merge(oldConceptOfBudgetTemplateListNewBudgetTemplate);
                        }
                    }
                }

                em.getTransaction().commit();
                resultado = Boolean.TRUE;

                if (em != null)
                {
                    em.close();
                }
            }
        } else
        {
            throw new ClassEliminatedException();
        }

        return resultado;
    }

    public Boolean destroy(Integer id) throws IllegalOrphanException, NonexistentEntityException, ClassEliminatedException
    {
        Boolean resultado = Boolean.FALSE;

        EntityManager em = null;
        try
        {
            em = getEntityManager();
            em.getTransaction().begin();

            Concept persistentConcept = em.find(Concept.class, id);

            if (persistentConcept != null)
            {
                Concept concept;

                concept = em.getReference(Concept.class, id);
                concept.getIdConcept();
                List<String> illegalOrphanMessages = null;
                List<BudgetTemplate> budgetTemplateListOrphanCheck = concept.getBudgetTemplateList();
                for (BudgetTemplate budgetTemplateListOrphanCheckBudgetTemplate : budgetTemplateListOrphanCheck)
                {
                    if (illegalOrphanMessages == null)
                    {
                        illegalOrphanMessages = new ArrayList<String>();
                    }
                    illegalOrphanMessages.add("This Concepto (" + concept + ") cannot be destroyed since the PlantillaPresupuesto " + budgetTemplateListOrphanCheckBudgetTemplate + " in its plantillaPresupuestoList field has a non-nullable concepto field.");
                }
                if (illegalOrphanMessages != null)
                {
                    throw new IllegalOrphanException(illegalOrphanMessages);
                }

                em.remove(concept);
                em.getTransaction().commit();
                resultado = Boolean.TRUE;
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
        return resultado;
    }

    public List<Concept> findConceptEntities()
    {
        return findConceptEntities(true, -1, -1);
    }

    public List<Concept> findConceptEntities(int maxResults, int firstResult)
    {
        return findConceptEntities(false, maxResults, firstResult);
    }

    private List<Concept> findConceptEntities(boolean all, int maxResults, int firstResult)
    {
        EntityManager em = getEntityManager();
        try
        {
            Query q = em.createQuery("select object(o) from Concepto as o");
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

    public List<Concept> findConceptByName(String nameConcept)
    {

        EntityManager em = getEntityManager();
        List<Concept> listaConcept = new ArrayList<>();

        Query query = em.createNamedQuery("Concepto.findByNombre");
        query.setParameter("nombre", nameConcept);

        listaConcept = query.getResultList();

        return listaConcept;
    }

    public Concept findConcept(Integer id)
    {
        EntityManager em = getEntityManager();

        try
        {
            return em.find(Concept.class, id);
        }
        finally
        {
            em.close();
        }
    }

    public int getConceptCount()
    {
        EntityManager em = getEntityManager();
        try
        {
            Query q = em.createQuery("select count(o) from Concepto as o");
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
