/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.licensis.notaire.jpa;

import com.licensis.notaire.jpa.exceptions.ClassEliminatedException;
import com.licensis.notaire.jpa.exceptions.ClassModifiedException;
import com.licensis.notaire.jpa.exceptions.NonexistentEntityException;
import com.licensis.notaire.jpa.interfaz.IPersistenciaJpa;
import com.licensis.notaire.negocio.MovimientoTestimonio;
import com.licensis.notaire.negocio.Testimonio;
import java.io.Serializable;
import java.util.List;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.Query;
import javax.transaction.UserTransaction;

/**
 *
 * @author juanca
 */
public class MovimientoTestimonioJpaController implements Serializable, IPersistenciaJpa
{

    public MovimientoTestimonioJpaController(UserTransaction utx, EntityManagerFactory emf)
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

    public Boolean create(MovimientoTestimonio movimientoTestimonio)
    {
        EntityManager em = null;
        Boolean creado = false;
        try
        {
            em = getEntityManager();
            em.getTransaction().begin();
            Testimonio fkIdTestimonio = movimientoTestimonio.getTestimonio();
            if (fkIdTestimonio != null)
            {
                fkIdTestimonio = em.getReference(fkIdTestimonio.getClass(), fkIdTestimonio.getIdTestimonio());
                movimientoTestimonio.setTestimonio(fkIdTestimonio);
            }
            em.persist(movimientoTestimonio);
            if (fkIdTestimonio != null)
            {
                fkIdTestimonio.getMovimientoTestimonioList().add(movimientoTestimonio);
                fkIdTestimonio = em.merge(fkIdTestimonio);
            }
            em.getTransaction().commit();
            creado = true;
        }
        finally
        {
            if (em != null)
            {
                em.close();
            }
        }

        return creado;
    }

    public Boolean edit(MovimientoTestimonio movimientoTestimonio) throws ClassEliminatedException, ClassModifiedException
    {
        EntityManager em = null;
        Boolean modificado = false;
        int version = 0;
        int oldVersion = 0;
        em = getEntityManager();
        em.getTransaction().begin();
        MovimientoTestimonio persistentMovimientoTestimonio = em.find(MovimientoTestimonio.class, movimientoTestimonio.getIdMovimientoTestimonio());

        if (persistentMovimientoTestimonio != null)
        {
            version = persistentMovimientoTestimonio.getVersion(); // Version del Objeto en db
            oldVersion = movimientoTestimonio.getVersion(); // Version del Objeto en memoria

            if (version != oldVersion) //Si son distintas "Alguien modifico el objeto"
            {
                throw new ClassModifiedException();

            } else
            {
                Testimonio fkIdTestimonioOld = persistentMovimientoTestimonio.getTestimonio();
                Testimonio fkIdTestimonioNew = movimientoTestimonio.getTestimonio();
                if (fkIdTestimonioNew != null)
                {
                    fkIdTestimonioNew = em.getReference(fkIdTestimonioNew.getClass(), fkIdTestimonioNew.getIdTestimonio());
                    movimientoTestimonio.setTestimonio(fkIdTestimonioNew);
                }
                movimientoTestimonio = em.merge(movimientoTestimonio);
                if (fkIdTestimonioOld != null && !fkIdTestimonioOld.equals(fkIdTestimonioNew))
                {
                    fkIdTestimonioOld.getMovimientoTestimonioList().remove(movimientoTestimonio);
                    fkIdTestimonioOld = em.merge(fkIdTestimonioOld);
                }
                if (fkIdTestimonioNew != null && !fkIdTestimonioNew.equals(fkIdTestimonioOld))
                {
                    fkIdTestimonioNew.getMovimientoTestimonioList().add(movimientoTestimonio);
                    fkIdTestimonioNew = em.merge(fkIdTestimonioNew);
                }
                em.getTransaction().commit();
                modificado = true;
            }
            if (em != null)
            {
                em.close();
            }
        } else
        {
            throw new ClassEliminatedException();
        }
        return modificado;
    }

    public void destroy(Integer id) throws NonexistentEntityException
    {
        EntityManager em = null;
        try
        {
            em = getEntityManager();
            em.getTransaction().begin();
            MovimientoTestimonio movimientoTestimonio;

            try
            {
                movimientoTestimonio = em.getReference(MovimientoTestimonio.class, id);
                movimientoTestimonio.getIdMovimientoTestimonio();
            }
            catch (EntityNotFoundException enfe)
            {
                throw new NonexistentEntityException("The movimientoTestimonio with id " + id + " no longer exists.", enfe);
            }
            Testimonio fkIdTestimonio = movimientoTestimonio.getTestimonio();
            if (fkIdTestimonio != null)
            {
                fkIdTestimonio.getMovimientoTestimonioList().remove(movimientoTestimonio);
                fkIdTestimonio = em.merge(fkIdTestimonio);
            }
            em.remove(movimientoTestimonio);
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

    public List<MovimientoTestimonio> findMovimientoTestimonioEntities()
    {
        return findMovimientoTestimonioEntities(true, -1, -1);
    }

    public List<MovimientoTestimonio> findMovimientoTestimonioEntities(int maxResults, int firstResult)
    {
        return findMovimientoTestimonioEntities(false, maxResults, firstResult);
    }

    private List<MovimientoTestimonio> findMovimientoTestimonioEntities(boolean all, int maxResults, int firstResult)
    {
        EntityManager em = getEntityManager();
        try
        {
            Query q = em.createQuery("select object(o) from MovimientoTestimonio as o");
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

    public MovimientoTestimonio findMovimientoTestimonio(Integer id)
    {
        EntityManager em = getEntityManager();

        try
        {
            return em.find(MovimientoTestimonio.class, id);
        }
        finally
        {
            em.close();
        }
    }

    public List<MovimientoTestimonio> buscarMovimientosPorTestimonio(Integer idTestimonio)
    {
        List<MovimientoTestimonio> movTestimonio = null;
        EntityManager em = getEntityManager();

        Query query = em.createNamedQuery("MovimientoTestimonio.findByTestimonio");
        query.setParameter("idTestimonio", idTestimonio);

        movTestimonio = (List<MovimientoTestimonio>) query.getResultList();

        return movTestimonio;
    }

    public MovimientoTestimonio findMovimientoById(Integer idMovimiento)
    {

        MovimientoTestimonio movTestimonio = null;
        EntityManager em = getEntityManager();

        Query query = em.createNamedQuery("MovimientoTestimonio.findByIdMovimientoTestimonio");
        query.setParameter("idMovimientoTestimonio", idMovimiento);

        movTestimonio = (MovimientoTestimonio) query.getResultList().get(0);

        return movTestimonio;
    }

    public int getMovimientoTestimonioCount()
    {
        EntityManager em = getEntityManager();
        try
        {
            Query q = em.createQuery("select count(o) from MovimientoTestimonio as o");
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
