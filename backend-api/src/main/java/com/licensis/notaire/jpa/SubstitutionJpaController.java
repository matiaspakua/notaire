/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.licensis.notaire.jpa;

import com.licensis.notaire.jpa.exceptions.NonexistentEntityException;
import com.licensis.notaire.jpa.interfaz.IPersistenciaJpa;
import com.licensis.notaire.business.Person;
import com.licensis.notaire.business.Substitution;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Query;
import jakarta.transaction.UserTransaction;

/**
 *
 * @author juanca
 */
public class SubstitutionJpaController implements Serializable, IPersistenciaJpa
{

    public SubstitutionJpaController(UserTransaction utx, EntityManagerFactory emf)
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

    public void create(Substitution substitution)
    {
        EntityManager em = null;
        try
        {
            em = getEntityManager();
            em.getTransaction().begin();
            Person fkIdSubstitute = substitution.getFkIdSubstitute();
            if (fkIdSubstitute != null)
            {
                fkIdSubstitute = em.getReference(fkIdSubstitute.getClass(), fkIdSubstitute.getPersonId());
                substitution.setFkIdSubstitute(fkIdSubstitute);
            }
            Person fkIdSubstituted = substitution.getFkIdSubstituted();
            if (fkIdSubstituted != null)
            {
                fkIdSubstituted = em.getReference(fkIdSubstituted.getClass(), fkIdSubstituted.getPersonId());
                substitution.setFkIdSubstituted(fkIdSubstituted);
            }
            em.persist(substitution);
            if (fkIdSubstitute != null)
            {
                fkIdSubstitute.getSubstitutionList().add(substitution);
                fkIdSubstitute = em.merge(fkIdSubstitute);
            }
            if (fkIdSubstituted != null)
            {
                fkIdSubstituted.getSubstitutionList().add(substitution);
                fkIdSubstituted = em.merge(fkIdSubstituted);
            }
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

    public void edit(Substitution substitution) throws NonexistentEntityException, Exception
    {
        EntityManager em = null;
        try
        {
            em = getEntityManager();
            em.getTransaction().begin();
            Substitution persistentSubstitution = em.find(Substitution.class, substitution.getIdSubstitution());
            Person fkIdSubstituteOld = persistentSubstitution.getFkIdSubstitute();
            Person fkIdSubstituteNew = substitution.getFkIdSubstitute();
            Person fkIdSubstitutedOld = persistentSubstitution.getFkIdSubstituted();
            Person fkIdSubstitutedNew = substitution.getFkIdSubstituted();
            if (fkIdSubstituteNew != null)
            {
                fkIdSubstituteNew = em.getReference(fkIdSubstituteNew.getClass(), fkIdSubstituteNew.getPersonId());
                substitution.setFkIdSubstitute(fkIdSubstituteNew);
            }
            if (fkIdSubstitutedNew != null)
            {
                fkIdSubstitutedNew = em.getReference(fkIdSubstitutedNew.getClass(), fkIdSubstitutedNew.getPersonId());
                substitution.setFkIdSubstituted(fkIdSubstitutedNew);
            }
            substitution = em.merge(substitution);
            if (fkIdSubstituteOld != null && !fkIdSubstituteOld.equals(fkIdSubstituteNew))
            {
                fkIdSubstituteOld.getSubstitutionList().remove(substitution);
                fkIdSubstituteOld = em.merge(fkIdSubstituteOld);
            }
            if (fkIdSubstituteNew != null && !fkIdSubstituteNew.equals(fkIdSubstituteOld))
            {
                fkIdSubstituteNew.getSubstitutionList().add(substitution);
                fkIdSubstituteNew = em.merge(fkIdSubstituteNew);
            }
            if (fkIdSubstitutedOld != null && !fkIdSubstitutedOld.equals(fkIdSubstitutedNew))
            {
                fkIdSubstitutedOld.getSubstitutionList().remove(substitution);
                fkIdSubstitutedOld = em.merge(fkIdSubstitutedOld);
            }
            if (fkIdSubstitutedNew != null && !fkIdSubstitutedNew.equals(fkIdSubstitutedOld))
            {
                fkIdSubstitutedNew.getSubstitutionList().add(substitution);
                fkIdSubstitutedNew = em.merge(fkIdSubstitutedNew);
            }
            em.getTransaction().commit();
        }
        catch (Exception ex)
        {
            String msg = ex.getLocalizedMessage();
            if (msg == null || msg.length() == 0)
            {
                Integer id = substitution.getIdSubstitution();
                if (findSubstitution(id) == null)
                {
                    throw new NonexistentEntityException("The suplencia with id " + id + " no longer exists.");
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

    public void destroy(Integer id) throws NonexistentEntityException
    {
        EntityManager em = null;
        try
        {
            em = getEntityManager();
            em.getTransaction().begin();
            // Load directly (not via getReference + reverse-collection removal): Suplencia
            // is the FK-owning side, so removing it from Persona.suplenciaList/suplenciaList1
            // in memory has no cascading DB effect (no orphanRemoval) and previously left the
            // row un-flushed — em.remove() below is sufficient and correct on its own.
            Substitution substitution = em.find(Substitution.class, id);
            if (substitution == null)
            {
                throw new NonexistentEntityException("The suplencia with id " + id + " no longer exists.");
            }
            em.remove(substitution);
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

    public List<Substitution> findSubstitutionEntities()
    {
        return findSubstitutionEntities(true, -1, -1);
    }

    public List<Substitution> findSubstitutionEntities(int maxResults, int firstResult)
    {
        return findSubstitutionEntities(false, maxResults, firstResult);
    }

    private List<Substitution> findSubstitutionEntities(boolean all, int maxResults, int firstResult)
    {
        EntityManager em = getEntityManager();
        try
        {
            Query q = em.createQuery(
                "SELECT s FROM Suplencia s LEFT JOIN FETCH s.fkIdSuplantado LEFT JOIN FETCH s.fkIdSuplente");
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

    public List<Substitution> findSuplenciasPorYear(Substitution unaSubstitution)
    {
        List<Substitution> listaSuplencias = new ArrayList<>();
        EntityManager em = getEntityManager();

        Query query = em.createNamedQuery("Suplencia.findSuplenciasPorAnio");
        query.setParameter("fechaInicio", unaSubstitution.getDateStart());
        query.setParameter("fechaFin", unaSubstitution.getDateEnd());

        listaSuplencias = query.getResultList();

        return listaSuplencias;
    }

    public Substitution findSubstitution(Integer id)
    {
        EntityManager em = getEntityManager();
        try
        {
            // JOIN FETCH: las personas son LAZY y la entidad se serializa con el
            // EntityManager ya cerrado (detached); sin fetch la serialización falla.
            Query q = em.createQuery(
                    "SELECT s FROM Suplencia s "
                            + "JOIN FETCH s.fkIdSuplente "
                            + "JOIN FETCH s.fkIdSuplantado "
                            + "WHERE s.idSuplencia = :id");
            q.setParameter("id", id);
            @SuppressWarnings("unchecked")
            List<Substitution> resultado = q.getResultList();
            return resultado.isEmpty() ? null : resultado.get(0);
        }
        finally
        {
            em.close();
        }
    }

    public int getSubstitutionCount()
    {
        EntityManager em = getEntityManager();
        try
        {
            Query q = em.createQuery("select count(o) from Suplencia as o");
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
