/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.licensis.notaire.jpa;

import com.licensis.notaire.jpa.exceptions.ClassEliminatedException;
import com.licensis.notaire.jpa.exceptions.ClassModifiedException;
import com.licensis.notaire.jpa.exceptions.NonexistentEntityException;
import com.licensis.notaire.jpa.JpaPersistence;
import com.licensis.notaire.business.TestimonyMovement;
import com.licensis.notaire.business.Testimony;
import java.io.Serializable;
import java.util.List;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.Query;
import jakarta.transaction.UserTransaction;

/**
 *
 * @author juanca
 */
public class TestimonyMovementJpaController implements Serializable, JpaPersistence
{

    public TestimonyMovementJpaController(UserTransaction utx, EntityManagerFactory emf)
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

    public Boolean create(TestimonyMovement testimonyMovement)
    {
        EntityManager em = null;
        Boolean created = false;
        try
        {
            em = getEntityManager();
            em.getTransaction().begin();
            Testimony fkIdTestimony = testimonyMovement.getTestimony();
            if (fkIdTestimony != null)
            {
                fkIdTestimony = em.getReference(fkIdTestimony.getClass(), fkIdTestimony.getIdTestimony());
                testimonyMovement.setTestimony(fkIdTestimony);
            }
            em.persist(testimonyMovement);
            if (fkIdTestimony != null)
            {
                fkIdTestimony.getTestimonyMovementList().add(testimonyMovement);
                fkIdTestimony = em.merge(fkIdTestimony);
            }
            em.getTransaction().commit();
            created = true;
        }
        finally
        {
            if (em != null)
            {
                em.close();
            }
        }

        return created;
    }

    public Boolean edit(TestimonyMovement testimonyMovement) throws ClassEliminatedException, ClassModifiedException
    {
        EntityManager em = null;
        Boolean modified = false;
        int version = 0;
        int oldVersion = 0;
        em = getEntityManager();
        em.getTransaction().begin();
        TestimonyMovement persistentTestimonyMovement = em.find(TestimonyMovement.class, testimonyMovement.getIdTestimonyMovement());

        if (persistentTestimonyMovement != null)
        {
            version = persistentTestimonyMovement.getVersion(); // Version of the object in the database
            oldVersion = testimonyMovement.getVersion(); // Version of the object in memory

            if (version != oldVersion) // Versions differ: someone else modified the object
            {
                throw new ClassModifiedException();

            } else
            {
                Testimony fkIdTestimonyOld = persistentTestimonyMovement.getTestimony();
                Testimony fkIdTestimonyNew = testimonyMovement.getTestimony();
                if (fkIdTestimonyNew != null)
                {
                    fkIdTestimonyNew = em.getReference(fkIdTestimonyNew.getClass(), fkIdTestimonyNew.getIdTestimony());
                    testimonyMovement.setTestimony(fkIdTestimonyNew);
                }
                testimonyMovement = em.merge(testimonyMovement);
                if (fkIdTestimonyOld != null && !fkIdTestimonyOld.equals(fkIdTestimonyNew))
                {
                    fkIdTestimonyOld.getTestimonyMovementList().remove(testimonyMovement);
                    fkIdTestimonyOld = em.merge(fkIdTestimonyOld);
                }
                if (fkIdTestimonyNew != null && !fkIdTestimonyNew.equals(fkIdTestimonyOld))
                {
                    fkIdTestimonyNew.getTestimonyMovementList().add(testimonyMovement);
                    fkIdTestimonyNew = em.merge(fkIdTestimonyNew);
                }
                em.getTransaction().commit();
                modified = true;
            }
            if (em != null)
            {
                em.close();
            }
        } else
        {
            throw new ClassEliminatedException();
        }
        return modified;
    }

    public void destroy(Integer id) throws NonexistentEntityException
    {
        EntityManager em = null;
        try
        {
            em = getEntityManager();
            em.getTransaction().begin();
            TestimonyMovement testimonyMovement;

            try
            {
                testimonyMovement = em.getReference(TestimonyMovement.class, id);
                testimonyMovement.getIdTestimonyMovement();
            }
            catch (EntityNotFoundException enfe)
            {
                throw new NonexistentEntityException("The testimony movement with id " + id + " no longer exists.", enfe);
            }
            Testimony fkIdTestimony = testimonyMovement.getTestimony();
            if (fkIdTestimony != null)
            {
                fkIdTestimony.getTestimonyMovementList().remove(testimonyMovement);
                fkIdTestimony = em.merge(fkIdTestimony);
            }
            em.remove(testimonyMovement);
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

    public List<TestimonyMovement> findTestimonyMovementEntities()
    {
        return findTestimonyMovementEntities(true, -1, -1);
    }

    public List<TestimonyMovement> findTestimonyMovementEntities(int maxResults, int firstResult)
    {
        return findTestimonyMovementEntities(false, maxResults, firstResult);
    }

    private List<TestimonyMovement> findTestimonyMovementEntities(boolean all, int maxResults, int firstResult)
    {
        EntityManager em = getEntityManager();
        try
        {
            Query q = em.createQuery("select object(o) from TestimonyMovement as o");
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

    public TestimonyMovement findTestimonyMovement(Integer id)
    {
        EntityManager em = getEntityManager();

        try
        {
            return em.find(TestimonyMovement.class, id);
        }
        finally
        {
            em.close();
        }
    }

    public List<TestimonyMovement> searchMovimientosPorTestimony(Integer idTestimony)
    {
        List<TestimonyMovement> movTestimony = null;
        EntityManager em = getEntityManager();

        Query query = em.createNamedQuery("TestimonyMovement.findByTestimony");
        query.setParameter("idTestimony", idTestimony);

        movTestimony = (List<TestimonyMovement>) query.getResultList();

        return movTestimony;
    }

    public TestimonyMovement findMovementById(Integer idMovement)
    {

        TestimonyMovement movTestimony = null;
        EntityManager em = getEntityManager();

        Query query = em.createNamedQuery("TestimonyMovement.findByIdTestimonyMovement");
        query.setParameter("idTestimonyMovement", idMovement);

        movTestimony = (TestimonyMovement) query.getResultList().get(0);

        return movTestimony;
    }

    public int getTestimonyMovementCount()
    {
        EntityManager em = getEntityManager();
        try
        {
            Query q = em.createQuery("select count(o) from TestimonyMovement as o");
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
