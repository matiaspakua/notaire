/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.licensis.notaire.jpa;

import com.licensis.notaire.dto.DtoInmueble;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityNotFoundException;
import javax.persistence.Query;
import javax.transaction.UserTransaction;
import com.licensis.notaire.jpa.exceptions.NonexistentEntityException;
import com.licensis.notaire.jpa.interfaz.IPersistenciaJpa;
import com.licensis.notaire.negocio.Inmueble;
import com.licensis.notaire.negocio.Tramite;

/**
 *
 * @author juanca
 */
public class InmuebleJpaController implements Serializable, IPersistenciaJpa
{

    public InmuebleJpaController(UserTransaction utx, EntityManagerFactory emf) {
        this.utx = utx;
        this.emf = emf;
    }
    private UserTransaction utx = null;
    private EntityManagerFactory emf = null;

    public EntityManager getEntityManager() {
        return emf.createEntityManager();
    }

    public int create(Inmueble inmueble) {
        int id = -1;
        if (inmueble.getTramiteList() == null)
        {
            inmueble.setTramiteList(new ArrayList<Tramite>());
        }
        EntityManager em = null;
        try
        {
            em = getEntityManager();
            em.getTransaction().begin();
            List<Tramite> attachedTramiteList = new ArrayList<Tramite>();
            for (Tramite tramiteListTramiteToAttach : inmueble.getTramiteList())
            {
                tramiteListTramiteToAttach = em.getReference(tramiteListTramiteToAttach.getClass(), tramiteListTramiteToAttach.getIdTramite());
                attachedTramiteList.add(tramiteListTramiteToAttach);
            }
            inmueble.setTramiteList(attachedTramiteList);
            em.persist(inmueble);
            for (Tramite tramiteListTramite : inmueble.getTramiteList())
            {
                Inmueble oldFkIdInmuebleOfTramiteListTramite = tramiteListTramite.getFkIdInmueble();
                tramiteListTramite.setFkIdInmueble(inmueble);
                tramiteListTramite = em.merge(tramiteListTramite);
                if (oldFkIdInmuebleOfTramiteListTramite != null)
                {
                    oldFkIdInmuebleOfTramiteListTramite.getTramiteList().remove(tramiteListTramite);
                    oldFkIdInmuebleOfTramiteListTramite = em.merge(oldFkIdInmuebleOfTramiteListTramite);
                }
            }
            em.getTransaction().commit();
            id = inmueble.getIdInmueble();
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

    public void edit(Inmueble inmueble) throws NonexistentEntityException, Exception {
        EntityManager em = null;
        try
        {
            em = getEntityManager();
            em.getTransaction().begin();
            Inmueble persistentInmueble = em.find(Inmueble.class, inmueble.getIdInmueble());
            List<Tramite> tramiteListOld = persistentInmueble.getTramiteList();
            List<Tramite> tramiteListNew = inmueble.getTramiteList();
            List<Tramite> attachedTramiteListNew = new ArrayList<Tramite>();
            for (Tramite tramiteListNewTramiteToAttach : tramiteListNew)
            {
                tramiteListNewTramiteToAttach = em.getReference(tramiteListNewTramiteToAttach.getClass(), tramiteListNewTramiteToAttach.getIdTramite());
                attachedTramiteListNew.add(tramiteListNewTramiteToAttach);
            }
            tramiteListNew = attachedTramiteListNew;
            inmueble.setTramiteList(tramiteListNew);
            inmueble = em.merge(inmueble);
            for (Tramite tramiteListOldTramite : tramiteListOld)
            {
                if (!tramiteListNew.contains(tramiteListOldTramite))
                {
                    tramiteListOldTramite.setFkIdInmueble(null);
                    tramiteListOldTramite = em.merge(tramiteListOldTramite);
                }
            }
            for (Tramite tramiteListNewTramite : tramiteListNew)
            {
                if (!tramiteListOld.contains(tramiteListNewTramite))
                {
                    Inmueble oldFkIdInmuebleOfTramiteListNewTramite = tramiteListNewTramite.getFkIdInmueble();
                    tramiteListNewTramite.setFkIdInmueble(inmueble);
                    tramiteListNewTramite = em.merge(tramiteListNewTramite);
                    if (oldFkIdInmuebleOfTramiteListNewTramite != null && !oldFkIdInmuebleOfTramiteListNewTramite.equals(inmueble))
                    {
                        oldFkIdInmuebleOfTramiteListNewTramite.getTramiteList().remove(tramiteListNewTramite);
                        oldFkIdInmuebleOfTramiteListNewTramite = em.merge(oldFkIdInmuebleOfTramiteListNewTramite);
                    }
                }
            }
            em.getTransaction().commit();
        }
        catch (Exception ex)
        {
            String msg = ex.getLocalizedMessage();
            if (msg == null || msg.length() == 0)
            {
                Integer id = inmueble.getIdInmueble();
                if (findInmueble(id) == null)
                {
                    throw new NonexistentEntityException("The inmueble with id " + id + " no longer exists.");
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
            Inmueble inmueble;
            try
            {
                inmueble = em.getReference(Inmueble.class, id);
                inmueble.getIdInmueble();
            }
            catch (EntityNotFoundException enfe)
            {
                throw new NonexistentEntityException("The inmueble with id " + id + " no longer exists.", enfe);
            }
            List<Tramite> tramiteList = inmueble.getTramiteList();
            for (Tramite tramiteListTramite : tramiteList)
            {
                tramiteListTramite.setFkIdInmueble(null);
                tramiteListTramite = em.merge(tramiteListTramite);
            }
            em.remove(inmueble);
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

    public List<Inmueble> findInmuebleEntities() {
        return findInmuebleEntities(true, -1, -1);
    }

    public List<Inmueble> findInmuebleEntities(int maxResults, int firstResult) {
        return findInmuebleEntities(false, maxResults, firstResult);
    }

    private List<Inmueble> findInmuebleEntities(boolean all, int maxResults, int firstResult) {
        EntityManager em = getEntityManager();
        try
        {
            Query q = em.createQuery("select object(o) from Inmueble as o");
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

    public Inmueble findInmueble(Integer id) {
        EntityManager em = getEntityManager();
        try
        {
            return em.find(Inmueble.class, id);
        }
        finally
        {
            em.close();
        }
    }

    /**
     * Busca un inmueble por su nomenclatura catastral.
     *
     * @param dtoInmueble
     * @return el Inmueble encontrado.
     */
    public Inmueble findInmueble(DtoInmueble dtoInmueble) {

        EntityManager em = getEntityManager();

        List<Inmueble> inmuebles = null;
        Inmueble miInmueble = null;

        Query query = em.createNamedQuery("Inmueble.findByNomenclatura");
        query.setParameter("nomenclatura", dtoInmueble.getNomenclaturaCatastral());

        inmuebles = query.getResultList();

        if (inmuebles != null && !inmuebles.isEmpty())
        {
            miInmueble = inmuebles.get(0);
        }

        return miInmueble;
    }

    public int getInmuebleCount() {
        EntityManager em = getEntityManager();
        try
        {
            Query q = em.createQuery("select count(o) from Inmueble as o");
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
