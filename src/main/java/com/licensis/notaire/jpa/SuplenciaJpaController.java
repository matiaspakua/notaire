/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.licensis.notaire.jpa;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Query;
import javax.persistence.EntityNotFoundException;
import javax.transaction.UserTransaction;
import com.licensis.notaire.jpa.exceptions.NonexistentEntityException;
import com.licensis.notaire.jpa.interfaz.IPersistenciaJpa;
import com.licensis.notaire.negocio.Persona;
import com.licensis.notaire.negocio.Suplencia;

/**
 *
 * @author juanca
 */
public class SuplenciaJpaController implements Serializable, IPersistenciaJpa
{

    public SuplenciaJpaController(UserTransaction utx, EntityManagerFactory emf) {
        this.utx = utx;
        this.emf = emf;
    }
    private UserTransaction utx = null;
    private EntityManagerFactory emf = null;

    public EntityManager getEntityManager() {
        return emf.createEntityManager();
    }

    public void create(Suplencia suplencia) {
        EntityManager em = null;
        try
        {
            em = getEntityManager();
            em.getTransaction().begin();
            Persona fkIdSuplente = suplencia.getFkIdSuplente();
            if (fkIdSuplente != null)
            {
                fkIdSuplente = em.getReference(fkIdSuplente.getClass(), fkIdSuplente.getIdPersona());
                suplencia.setFkIdSuplente(fkIdSuplente);
            }
            Persona fkIdSuplantado = suplencia.getFkIdSuplantado();
            if (fkIdSuplantado != null)
            {
                fkIdSuplantado = em.getReference(fkIdSuplantado.getClass(), fkIdSuplantado.getIdPersona());
                suplencia.setFkIdSuplantado(fkIdSuplantado);
            }
            em.persist(suplencia);
            if (fkIdSuplente != null)
            {
                fkIdSuplente.getSuplenciaList().add(suplencia);
                fkIdSuplente = em.merge(fkIdSuplente);
            }
            if (fkIdSuplantado != null)
            {
                fkIdSuplantado.getSuplenciaList().add(suplencia);
                fkIdSuplantado = em.merge(fkIdSuplantado);
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

    public void edit(Suplencia suplencia) throws NonexistentEntityException, Exception {
        EntityManager em = null;
        try
        {
            em = getEntityManager();
            em.getTransaction().begin();
            Suplencia persistentSuplencia = em.find(Suplencia.class, suplencia.getIdSuplencia());
            Persona fkIdSuplenteOld = persistentSuplencia.getFkIdSuplente();
            Persona fkIdSuplenteNew = suplencia.getFkIdSuplente();
            Persona fkIdSuplantadoOld = persistentSuplencia.getFkIdSuplantado();
            Persona fkIdSuplantadoNew = suplencia.getFkIdSuplantado();
            if (fkIdSuplenteNew != null)
            {
                fkIdSuplenteNew = em.getReference(fkIdSuplenteNew.getClass(), fkIdSuplenteNew.getIdPersona());
                suplencia.setFkIdSuplente(fkIdSuplenteNew);
            }
            if (fkIdSuplantadoNew != null)
            {
                fkIdSuplantadoNew = em.getReference(fkIdSuplantadoNew.getClass(), fkIdSuplantadoNew.getIdPersona());
                suplencia.setFkIdSuplantado(fkIdSuplantadoNew);
            }
            suplencia = em.merge(suplencia);
            if (fkIdSuplenteOld != null && !fkIdSuplenteOld.equals(fkIdSuplenteNew))
            {
                fkIdSuplenteOld.getSuplenciaList().remove(suplencia);
                fkIdSuplenteOld = em.merge(fkIdSuplenteOld);
            }
            if (fkIdSuplenteNew != null && !fkIdSuplenteNew.equals(fkIdSuplenteOld))
            {
                fkIdSuplenteNew.getSuplenciaList().add(suplencia);
                fkIdSuplenteNew = em.merge(fkIdSuplenteNew);
            }
            if (fkIdSuplantadoOld != null && !fkIdSuplantadoOld.equals(fkIdSuplantadoNew))
            {
                fkIdSuplantadoOld.getSuplenciaList().remove(suplencia);
                fkIdSuplantadoOld = em.merge(fkIdSuplantadoOld);
            }
            if (fkIdSuplantadoNew != null && !fkIdSuplantadoNew.equals(fkIdSuplantadoOld))
            {
                fkIdSuplantadoNew.getSuplenciaList().add(suplencia);
                fkIdSuplantadoNew = em.merge(fkIdSuplantadoNew);
            }
            em.getTransaction().commit();
        }
        catch (Exception ex)
        {
            String msg = ex.getLocalizedMessage();
            if (msg == null || msg.length() == 0)
            {
                Integer id = suplencia.getIdSuplencia();
                if (findSuplencia(id) == null)
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

    public void destroy(Integer id) throws NonexistentEntityException {
        EntityManager em = null;
        try
        {
            em = getEntityManager();
            em.getTransaction().begin();
            Suplencia suplencia;
            try
            {
                suplencia = em.getReference(Suplencia.class, id);
                suplencia.getIdSuplencia();
            }
            catch (EntityNotFoundException enfe)
            {
                throw new NonexistentEntityException("The suplencia with id " + id + " no longer exists.", enfe);
            }
            Persona fkIdSuplente = suplencia.getFkIdSuplente();
            if (fkIdSuplente != null)
            {
                fkIdSuplente.getSuplenciaList().remove(suplencia);
                fkIdSuplente = em.merge(fkIdSuplente);
            }
            Persona fkIdSuplantado = suplencia.getFkIdSuplantado();
            if (fkIdSuplantado != null)
            {
                fkIdSuplantado.getSuplenciaList().remove(suplencia);
                fkIdSuplantado = em.merge(fkIdSuplantado);
            }
            em.remove(suplencia);
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

    public List<Suplencia> findSuplenciaEntities() {
        return findSuplenciaEntities(true, -1, -1);
    }

    public List<Suplencia> findSuplenciaEntities(int maxResults, int firstResult) {
        return findSuplenciaEntities(false, maxResults, firstResult);
    }

    private List<Suplencia> findSuplenciaEntities(boolean all, int maxResults, int firstResult) {
        EntityManager em = getEntityManager();
        try
        {
            Query q = em.createQuery("select object(o) from Suplencia as o");
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

    public List<Suplencia> findSuplenciasPorAnio(Suplencia unaSuplencia) {
        List<Suplencia> listaSuplencias = new ArrayList<>();
        EntityManager em = getEntityManager();

        Query query = em.createNamedQuery("Suplencia.findSuplenciasPorAnio");
        query.setParameter("fechaInicio", unaSuplencia.getFechaInicio());
        query.setParameter("fechaFin", unaSuplencia.getFechaFin());

        listaSuplencias = query.getResultList();

        return listaSuplencias;
    }

    public Suplencia findSuplencia(Integer id) {
        EntityManager em = getEntityManager();
        try
        {
            return em.find(Suplencia.class, id);
        }
        finally
        {
            em.close();
        }
    }

    public int getSuplenciaCount() {
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
    public String getNombreJpa() {
        return this.getClass().getName();
    }
}
