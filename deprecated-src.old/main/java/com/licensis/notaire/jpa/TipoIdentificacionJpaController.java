/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.licensis.notaire.jpa;

import com.licensis.notaire.jpa.exceptions.IllegalOrphanException;
import com.licensis.notaire.jpa.exceptions.NonexistentEntityException;
import com.licensis.notaire.jpa.interfaz.IPersistenciaJpa;
import com.licensis.notaire.negocio.Persona;
import com.licensis.notaire.negocio.TipoIdentificacion;
import com.licensis.notaire.servicios.AdministradorJpa;
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
public class TipoIdentificacionJpaController implements Serializable, IPersistenciaJpa
{

    private static TipoIdentificacionJpaController instancia = null;

    public TipoIdentificacionJpaController(UserTransaction utx, EntityManagerFactory emf)
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

    public void create(TipoIdentificacion tipoIdentificacion)
    {
        if (tipoIdentificacion.getPersonaList() == null)
        {
            tipoIdentificacion.setPersonaList(new ArrayList<Persona>());
        }
        EntityManager em = null;
        try
        {
            em = getEntityManager();
            em.getTransaction().begin();
            List<Persona> attachedPersonaList = new ArrayList<Persona>();
            for (Persona personaListPersonaToAttach : tipoIdentificacion.getPersonaList())
            {
                personaListPersonaToAttach = em.getReference(personaListPersonaToAttach.getClass(), personaListPersonaToAttach.getIdPersona());
                attachedPersonaList.add(personaListPersonaToAttach);
            }
            tipoIdentificacion.setPersonaList(attachedPersonaList);
            em.persist(tipoIdentificacion);
            for (Persona personaListPersona : tipoIdentificacion.getPersonaList())
            {
                TipoIdentificacion oldFkIdTipoIdentificacionOfPersonaListPersona = personaListPersona.getFkIdTipoIdentificacion();
                personaListPersona.setFkIdTipoIdentificacion(tipoIdentificacion);
                personaListPersona = em.merge(personaListPersona);
                if (oldFkIdTipoIdentificacionOfPersonaListPersona != null)
                {
                    oldFkIdTipoIdentificacionOfPersonaListPersona.getPersonaList().remove(personaListPersona);
                    oldFkIdTipoIdentificacionOfPersonaListPersona = em.merge(oldFkIdTipoIdentificacionOfPersonaListPersona);
                }
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

    public void edit(TipoIdentificacion tipoIdentificacion) throws IllegalOrphanException, NonexistentEntityException, Exception
    {
        EntityManager em = null;
        try
        {
            em = getEntityManager();
            em.getTransaction().begin();
            TipoIdentificacion persistentTipoIdentificacion = em.find(TipoIdentificacion.class, tipoIdentificacion.getIdTipoIdentificacion());
            List<Persona> personaListOld = persistentTipoIdentificacion.getPersonaList();
            List<Persona> personaListNew = tipoIdentificacion.getPersonaList();
            List<String> illegalOrphanMessages = null;
            for (Persona personaListOldPersona : personaListOld)
            {
                if (!personaListNew.contains(personaListOldPersona))
                {
                    if (illegalOrphanMessages == null)
                    {
                        illegalOrphanMessages = new ArrayList<String>();
                    }
                    illegalOrphanMessages.add("You must retain Persona " + personaListOldPersona + " since its fkIdTipoIdentificacion field is not nullable.");
                }
            }
            if (illegalOrphanMessages != null)
            {
                throw new IllegalOrphanException(illegalOrphanMessages);
            }
            List<Persona> attachedPersonaListNew = new ArrayList<Persona>();
            for (Persona personaListNewPersonaToAttach : personaListNew)
            {
                personaListNewPersonaToAttach = em.getReference(personaListNewPersonaToAttach.getClass(), personaListNewPersonaToAttach.getIdPersona());
                attachedPersonaListNew.add(personaListNewPersonaToAttach);
            }
            personaListNew = attachedPersonaListNew;
            tipoIdentificacion.setPersonaList(personaListNew);
            tipoIdentificacion = em.merge(tipoIdentificacion);
            for (Persona personaListNewPersona : personaListNew)
            {
                if (!personaListOld.contains(personaListNewPersona))
                {
                    TipoIdentificacion oldFkIdTipoIdentificacionOfPersonaListNewPersona = personaListNewPersona.getFkIdTipoIdentificacion();
                    personaListNewPersona.setFkIdTipoIdentificacion(tipoIdentificacion);
                    personaListNewPersona = em.merge(personaListNewPersona);
                    if (oldFkIdTipoIdentificacionOfPersonaListNewPersona != null && !oldFkIdTipoIdentificacionOfPersonaListNewPersona.equals(tipoIdentificacion))
                    {
                        oldFkIdTipoIdentificacionOfPersonaListNewPersona.getPersonaList().remove(personaListNewPersona);
                        oldFkIdTipoIdentificacionOfPersonaListNewPersona = em.merge(oldFkIdTipoIdentificacionOfPersonaListNewPersona);
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
                Integer id = tipoIdentificacion.getIdTipoIdentificacion();
                if (findTipoIdentificacion(id) == null)
                {
                    throw new NonexistentEntityException("The tipoIdentificacion with id " + id + " no longer exists.");
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

    public void destroy(Integer id) throws IllegalOrphanException, NonexistentEntityException
    {
        EntityManager em = null;
        try
        {
            em = getEntityManager();
            em.getTransaction().begin();
            TipoIdentificacion tipoIdentificacion;
            try
            {
                tipoIdentificacion = em.getReference(TipoIdentificacion.class, id);
                tipoIdentificacion.getIdTipoIdentificacion();
            }
            catch (EntityNotFoundException enfe)
            {
                throw new NonexistentEntityException("The tipoIdentificacion with id " + id + " no longer exists.", enfe);
            }
            List<String> illegalOrphanMessages = null;
            List<Persona> personaListOrphanCheck = tipoIdentificacion.getPersonaList();
            for (Persona personaListOrphanCheckPersona : personaListOrphanCheck)
            {
                if (illegalOrphanMessages == null)
                {
                    illegalOrphanMessages = new ArrayList<String>();
                }
                illegalOrphanMessages.add("This TipoIdentificacion (" + tipoIdentificacion + ") cannot be destroyed since the Persona " + personaListOrphanCheckPersona + " in its personaList field has a non-nullable fkIdTipoIdentificacion field.");
            }
            if (illegalOrphanMessages != null)
            {
                throw new IllegalOrphanException(illegalOrphanMessages);
            }
            em.remove(tipoIdentificacion);
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

    public List<TipoIdentificacion> findTipoIdentificacionEntities()
    {
        return findTipoIdentificacionEntities(true, -1, -1);
    }

    public List<TipoIdentificacion> findTipoIdentificacionEntities(int maxResults, int firstResult)
    {
        return findTipoIdentificacionEntities(false, maxResults, firstResult);
    }

    private List<TipoIdentificacion> findTipoIdentificacionEntities(boolean all, int maxResults, int firstResult)
    {
        EntityManager em = getEntityManager();
        try
        {
            Query q = em.createQuery("select object(o) from TipoIdentificacion as o");
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

    public TipoIdentificacion findTipoIdentificacion(Integer id)
    {
        EntityManager em = getEntityManager();
        try
        {
            return em.find(TipoIdentificacion.class, id);
        }
        finally
        {
            em.close();
        }
    }

    public int getTipoIdentificacionCount()
    {
        EntityManager em = getEntityManager();
        try
        {
            Query q = em.createQuery("select count(o) from TipoIdentificacion as o");
            return ((Long) q.getSingleResult()).intValue();
        }
        finally
        {
            em.close();
        }
    }

    public static TipoIdentificacionJpaController getInstancia()
    {

        EntityManagerFactory emf = AdministradorJpa.getEmf();

        if (instancia == null)
        {
            instancia = new TipoIdentificacionJpaController(null, emf);
        }
        return instancia;
    }

    @Override
    public String getNombreJpa()
    {
        return this.getClass().getName();
    }
}
