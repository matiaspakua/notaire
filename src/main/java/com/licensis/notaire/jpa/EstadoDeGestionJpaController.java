/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.licensis.notaire.jpa;

import com.licensis.notaire.jpa.exceptions.ClassEliminatedException;
import com.licensis.notaire.jpa.exceptions.ClassModifiedException;
import com.licensis.notaire.jpa.exceptions.IllegalOrphanException;
import com.licensis.notaire.jpa.exceptions.NonexistentEntityException;
import com.licensis.notaire.jpa.exceptions.PreexistingEntityException;
import com.licensis.notaire.jpa.interfaz.IPersistenciaJpa;
import com.licensis.notaire.negocio.EstadoDeGestion;
import com.licensis.notaire.negocio.Historial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityNotFoundException;
import javax.persistence.Query;
import javax.transaction.UserTransaction;

/**
 *
 * @author User
 */
public class EstadoDeGestionJpaController implements Serializable, IPersistenciaJpa
{

    public EstadoDeGestionJpaController(UserTransaction utx, EntityManagerFactory emf) {
        this.utx = utx;
        this.emf = emf;
    }
    private UserTransaction utx = null;
    private EntityManagerFactory emf = null;

    public EntityManager getEntityManager() {
        return emf.createEntityManager();
    }

    public Integer create(EstadoDeGestion estadoDeGestion) throws PreexistingEntityException {
        Integer oid = null;
        if (estadoDeGestion.getHistorialList() == null)
        {
            estadoDeGestion.setHistorialList(new ArrayList<Historial>());
        }
        EntityManager em = null;
        try
        {
            em = getEntityManager();
            em.getTransaction().begin();
            List<Historial> attachedHistorialList = new ArrayList<Historial>();
            for (Historial historialListHistorialToAttach : estadoDeGestion.getHistorialList())
            {
                historialListHistorialToAttach = em.getReference(historialListHistorialToAttach.getClass(), historialListHistorialToAttach.getIdHistorial());
                attachedHistorialList.add(historialListHistorialToAttach);
            }
            estadoDeGestion.setHistorialList(attachedHistorialList);
            em.persist(estadoDeGestion);
            for (Historial historialListHistorial : estadoDeGestion.getHistorialList())
            {
                EstadoDeGestion oldFkIdEstadoGestionOfHistorialListHistorial = historialListHistorial.getFkIdEstadoGestion();
                historialListHistorial.setFkIdEstadoGestion(estadoDeGestion);
                historialListHistorial = em.merge(historialListHistorial);
                if (oldFkIdEstadoGestionOfHistorialListHistorial != null)
                {
                    oldFkIdEstadoGestionOfHistorialListHistorial.getHistorialList().remove(historialListHistorial);
                    oldFkIdEstadoGestionOfHistorialListHistorial = em.merge(oldFkIdEstadoGestionOfHistorialListHistorial);
                }
            }

            if (this.verificarExistenciaEstadoDeGestion(estadoDeGestion.getNombre()))
            {
                throw new PreexistingEntityException("La entidad ya existe");
            }

            em.getTransaction().commit();

            oid = estadoDeGestion.getIdEstadoGestion();
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

    public Boolean edit(EstadoDeGestion estadoDeGestion) throws IllegalOrphanException, NonexistentEntityException, ClassEliminatedException, ClassModifiedException {
        Integer version = ConstantesPersistencia.VERSION_INICIAL;
        Integer oldVersion = ConstantesPersistencia.VERSION_INICIAL;
        Boolean resultado = Boolean.FALSE;
        EntityManager em = null;

        em = getEntityManager();
        em.getTransaction().begin();
        EstadoDeGestion persistentEstadoDeGestion = em.find(EstadoDeGestion.class, estadoDeGestion.getIdEstadoGestion());

        if (persistentEstadoDeGestion != null)
        {
            version = persistentEstadoDeGestion.getVersion();
            oldVersion = estadoDeGestion.getVersion();

            if (version != oldVersion) //Si son distintas "Alguien modifico o elimino el objeto"
            {
                if (em != null)
                {
                    em.close();
                }

                throw new ClassModifiedException();
            }
            else
            {
                List<Historial> historialListOld = persistentEstadoDeGestion.getHistorialList();
                List<Historial> historialListNew = estadoDeGestion.getHistorialList();
                List<String> illegalOrphanMessages = null;
                for (Historial historialListOldHistorial : historialListOld)
                {
                    if (!historialListNew.contains(historialListOldHistorial))
                    {
                        if (illegalOrphanMessages == null)
                        {
                            illegalOrphanMessages = new ArrayList<String>();
                        }
                        illegalOrphanMessages.add("You must retain Historial " + historialListOldHistorial + " since its fkIdEstadoGestion field is not nullable.");
                    }
                }
                if (illegalOrphanMessages != null)
                {
                    throw new IllegalOrphanException(illegalOrphanMessages);
                }
                List<Historial> attachedHistorialListNew = new ArrayList<Historial>();
                for (Historial historialListNewHistorialToAttach : historialListNew)
                {
                    historialListNewHistorialToAttach = em.getReference(historialListNewHistorialToAttach.getClass(), historialListNewHistorialToAttach.getIdHistorial());
                    attachedHistorialListNew.add(historialListNewHistorialToAttach);
                }
                historialListNew = attachedHistorialListNew;
                estadoDeGestion.setHistorialList(historialListNew);
                estadoDeGestion = em.merge(estadoDeGestion);
                for (Historial historialListNewHistorial : historialListNew)
                {
                    if (!historialListOld.contains(historialListNewHistorial))
                    {
                        EstadoDeGestion oldFkIdEstadoGestionOfHistorialListNewHistorial = historialListNewHistorial.getFkIdEstadoGestion();
                        historialListNewHistorial.setFkIdEstadoGestion(estadoDeGestion);
                        historialListNewHistorial = em.merge(historialListNewHistorial);
                        if (oldFkIdEstadoGestionOfHistorialListNewHistorial != null && !oldFkIdEstadoGestionOfHistorialListNewHistorial.equals(estadoDeGestion))
                        {
                            oldFkIdEstadoGestionOfHistorialListNewHistorial.getHistorialList().remove(historialListNewHistorial);
                            oldFkIdEstadoGestionOfHistorialListNewHistorial = em.merge(oldFkIdEstadoGestionOfHistorialListNewHistorial);
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
        }
        else
        {
            throw new ClassEliminatedException();
        }

        return resultado;
    }

    public void destroy(Integer id) throws IllegalOrphanException, NonexistentEntityException {
        EntityManager em = null;
        try
        {
            em = getEntityManager();
            em.getTransaction().begin();
            EstadoDeGestion estadoDeGestion;


            try
            {
                estadoDeGestion = em.getReference(EstadoDeGestion.class, id);
                estadoDeGestion.getIdEstadoGestion();
            }
            catch (EntityNotFoundException enfe)
            {
                throw new NonexistentEntityException("The estadoDeGestion with id " + id + " no longer exists.", enfe);
            }
            List<String> illegalOrphanMessages = null;
            List<Historial> historialListOrphanCheck = estadoDeGestion.getHistorialList();
            for (Historial historialListOrphanCheckHistorial : historialListOrphanCheck)
            {
                if (illegalOrphanMessages == null)
                {
                    illegalOrphanMessages = new ArrayList<String>();
                }
                illegalOrphanMessages.add("This EstadoDeGestion (" + estadoDeGestion + ") cannot be destroyed since the Historial " + historialListOrphanCheckHistorial + " in its historialList field has a non-nullable fkIdEstadoGestion field.");
            }
            if (illegalOrphanMessages != null)
            {
                throw new IllegalOrphanException(illegalOrphanMessages);
            }
            em.remove(estadoDeGestion);
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
     * Verifica si el estado de gestion indicado ya se encuentra registrado o no.
     *
     * @param nombreEstado El nombre del estado de gestion.
     * @return Verdadero si es estado de gestion ya se encuentra registrado, falso en caso
     * contrario.
     *
     */
    public boolean verificarExistenciaEstadoDeGestion(String nombreEstado) {
        boolean resultado = false;

        List<EstadoDeGestion> listaEstadosDeGestiones = this.findEstadoDeGestionEntities();

        for (Iterator<EstadoDeGestion> it = listaEstadosDeGestiones.iterator(); it.hasNext();)
        {
            EstadoDeGestion estadoDeGestion = it.next();

            if (estadoDeGestion.getNombre().equals(nombreEstado))
            {
                return true;
            }
        }
        return resultado;
    }

    public List<EstadoDeGestion> findEstadoDeGestionEntities() {
        return findEstadoDeGestionEntities(true, -1, -1);
    }

    public List<EstadoDeGestion> findEstadoDeGestionEntities(int maxResults, int firstResult) {
        return findEstadoDeGestionEntities(false, maxResults, firstResult);
    }

    private List<EstadoDeGestion> findEstadoDeGestionEntities(boolean all, int maxResults, int firstResult) {
        EntityManager em = getEntityManager();
        try
        {
            Query q = em.createQuery("select object(o) from EstadoDeGestion as o");
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

    public EstadoDeGestion findEstadoDeGestion(Integer id) {
        EntityManager em = getEntityManager();


        try
        {
            return em.find(EstadoDeGestion.class, id);
        }
        finally
        {
            em.close();
        }
    }

    public int getEstadoDeGestionCount() {
        EntityManager em = getEntityManager();
        try
        {
            Query q = em.createQuery("select count(o) from EstadoDeGestion as o");
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
