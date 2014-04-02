/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.licensis.notaire.jpa;

import com.licensis.notaire.jpa.exceptions.CreateEntityException;
import com.licensis.notaire.jpa.exceptions.NonexistentEntityException;
import com.licensis.notaire.jpa.interfaz.IPersistenciaJpa;
import com.licensis.notaire.negocio.ConstantesNegocio;
import com.licensis.notaire.negocio.EstadoDeGestion;
import com.licensis.notaire.negocio.GestionDeEscritura;
import com.licensis.notaire.negocio.Historial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityNotFoundException;
import javax.persistence.Query;
import javax.transaction.UserTransaction;

/**
 *
 * @author juanca
 */
public class HistorialJpaController implements Serializable, IPersistenciaJpa
{

    public HistorialJpaController(UserTransaction utx, EntityManagerFactory emf)
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
     * Crea una nueva instancia de historial.
     *
     * @param historial La nueva instancia de historial a persistir.
     * @return idHistorial El del historial persistido,
     * {@link ConstantesNegocio}.ID_OBJETO_NO_VALIDO si ha ocurrido algun error.
     */
    public Integer create(Historial historial) throws CreateEntityException
    {
        Integer idHistorial = ConstantesNegocio.ID_OBJETO_NO_VALIDO;
        EntityManager em = null;
        try
        {
            em = getEntityManager();
            em.getTransaction().begin();
            EstadoDeGestion fkIdEstadoGestion = historial.getFkIdEstadoGestion();
            if (fkIdEstadoGestion != null)
            {
                fkIdEstadoGestion = em.getReference(fkIdEstadoGestion.getClass(), fkIdEstadoGestion.getIdEstadoGestion());
                historial.setFkIdEstadoGestion(fkIdEstadoGestion);
            }
            GestionDeEscritura fkIdGestion = historial.getFkIdGestion();
            if (fkIdGestion != null)
            {
                fkIdGestion = em.getReference(fkIdGestion.getClass(), fkIdGestion.getIdGestion());
                historial.setFkIdGestion(fkIdGestion);
            }
            em.persist(historial);
            if (fkIdEstadoGestion != null)
            {
                fkIdEstadoGestion.getHistorialList().add(historial);
                fkIdEstadoGestion = em.merge(fkIdEstadoGestion);
            }
            if (fkIdGestion != null)
            {
                fkIdGestion.getHistorialList().add(historial);
                fkIdGestion = em.merge(fkIdGestion);
            }
            em.getTransaction().commit();
            idHistorial = historial.getIdHistorial();
        }
        catch (Exception ex)
        {
            throw new CreateEntityException("No se pudo crear una entidad tipo: Historial");
        }
        finally
        {
            if (em != null)
            {
                em.close();
            }
        }
        return idHistorial;
    }

    public void edit(Historial historial) throws NonexistentEntityException, Exception
    {
        EntityManager em = null;
        try
        {
            em = getEntityManager();
            em.getTransaction().begin();
            Historial persistentHistorial = em.find(Historial.class, historial.getIdHistorial());
            EstadoDeGestion fkIdEstadoGestionOld = persistentHistorial.getFkIdEstadoGestion();
            EstadoDeGestion fkIdEstadoGestionNew = historial.getFkIdEstadoGestion();
            GestionDeEscritura fkIdGestionOld = persistentHistorial.getFkIdGestion();
            GestionDeEscritura fkIdGestionNew = historial.getFkIdGestion();
            if (fkIdEstadoGestionNew != null)
            {
                fkIdEstadoGestionNew = em.getReference(fkIdEstadoGestionNew.getClass(), fkIdEstadoGestionNew.getIdEstadoGestion());
                historial.setFkIdEstadoGestion(fkIdEstadoGestionNew);
            }
            if (fkIdGestionNew != null)
            {
                fkIdGestionNew = em.getReference(fkIdGestionNew.getClass(), fkIdGestionNew.getIdGestion());
                historial.setFkIdGestion(fkIdGestionNew);
            }
            historial = em.merge(historial);
            if (fkIdEstadoGestionOld != null && !fkIdEstadoGestionOld.equals(fkIdEstadoGestionNew))
            {
                fkIdEstadoGestionOld.getHistorialList().remove(historial);
                fkIdEstadoGestionOld = em.merge(fkIdEstadoGestionOld);
            }
            if (fkIdEstadoGestionNew != null && !fkIdEstadoGestionNew.equals(fkIdEstadoGestionOld))
            {
                fkIdEstadoGestionNew.getHistorialList().add(historial);
                fkIdEstadoGestionNew = em.merge(fkIdEstadoGestionNew);
            }
            if (fkIdGestionOld != null && !fkIdGestionOld.equals(fkIdGestionNew))
            {
                fkIdGestionOld.getHistorialList().remove(historial);
                fkIdGestionOld = em.merge(fkIdGestionOld);
            }
            if (fkIdGestionNew != null && !fkIdGestionNew.equals(fkIdGestionOld))
            {
                fkIdGestionNew.getHistorialList().add(historial);
                fkIdGestionNew = em.merge(fkIdGestionNew);
            }
            em.getTransaction().commit();
        }
        catch (Exception ex)
        {
            String msg = ex.getLocalizedMessage();
            if (msg == null || msg.length() == 0)
            {
                Integer id = historial.getIdHistorial();
                if (findHistorial(id) == null)
                {
                    throw new NonexistentEntityException("The historial with id " + id + " no longer exists.");
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
            Historial historial;
            try
            {
                historial = em.getReference(Historial.class, id);
                historial.getIdHistorial();
            }
            catch (EntityNotFoundException enfe)
            {
                throw new NonexistentEntityException("The historial with id " + id + " no longer exists.", enfe);
            }
            EstadoDeGestion fkIdEstadoGestion = historial.getFkIdEstadoGestion();
            if (fkIdEstadoGestion != null)
            {
                fkIdEstadoGestion.getHistorialList().remove(historial);
                fkIdEstadoGestion = em.merge(fkIdEstadoGestion);
            }
            GestionDeEscritura fkIdGestion = historial.getFkIdGestion();
            if (fkIdGestion != null)
            {
                fkIdGestion.getHistorialList().remove(historial);
                fkIdGestion = em.merge(fkIdGestion);
            }
            em.remove(historial);
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

    /**
     * Retorna el estado actual de una gestion en particular.
     *
     * @param idGestion El id de la gestion.
     * @return estadoActualGestion Una entidad tipo historial con el estado actual de la gestion.
     */
    public Historial findEstadoActualGestion(Integer idGestion)
    {
        Historial estadoActualGestion = new Historial();

        EntityManager em = getEntityManager();

        Query q = em.createNamedQuery("Historial.estadoActualGestion");
        q.setParameter("fkIdGestion", idGestion);

        estadoActualGestion = (Historial) q.getSingleResult();

        return estadoActualGestion;
    }

    /**
     * Retorna una lista con todos los registros de historial de una gestion en particular (cambios
     * de estado en el tiempo.
     *
     * @param idGestion El id de la gestion.
     * @return registroHistorial una lista con todos los cambios de estado de la gestion.
     */
    public List<Historial> findRegistroHistial(Integer idGestion)
    {
        List<Historial> registroHistorial = new ArrayList<Historial>();

        EntityManager em = getEntityManager();

        Query q = em.createNamedQuery("Historial.findByIdGestion");
        q.setParameter("idGestion", idGestion);

        registroHistorial = q.getResultList();

        return registroHistorial;
    }

    public List<Historial> findHistorialEntities()
    {
        return findHistorialEntities(true, -1, -1);
    }

    public List<Historial> findHistorialEntities(int maxResults, int firstResult)
    {
        return findHistorialEntities(false, maxResults, firstResult);
    }

    private List<Historial> findHistorialEntities(boolean all, int maxResults, int firstResult)
    {
        EntityManager em = getEntityManager();
        try
        {
            Query q = em.createQuery("select object(o) from Historial as o");
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

    public Historial findHistorial(Integer id)
    {
        EntityManager em = getEntityManager();
        try
        {
            return em.find(Historial.class, id);
        }
        finally
        {
            em.close();
        }
    }

    public int getHistorialCount()
    {
        EntityManager em = getEntityManager();
        try
        {
            Query q = em.createQuery("select count(o) from Historial as o");
            return ((Long) q.getSingleResult()).intValue();
        }
        finally
        {
            em.close();
        }
    }

    @Override
    public String getNombreJpa()
    {
        return this.getClass().getName();
    }
}
