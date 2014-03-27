/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.licensis.notaire.jpa;

import java.io.Serializable;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Query;
import javax.persistence.EntityNotFoundException;
import javax.transaction.UserTransaction;
import com.licensis.notaire.jpa.exceptions.NonexistentEntityException;
import com.licensis.notaire.jpa.interfaz.IPersistenciaJpa;
import com.licensis.notaire.negocio.Pago;
import com.licensis.notaire.negocio.Presupuesto;

/**
 *
 * @author juanca
 */
public class PagoJpaController implements Serializable, IPersistenciaJpa
{

    public PagoJpaController(UserTransaction utx, EntityManagerFactory emf) {
        this.utx = utx;
        this.emf = emf;
    }
    private UserTransaction utx = null;
    private EntityManagerFactory emf = null;

    public EntityManager getEntityManager() {
        return emf.createEntityManager();
    }

    public int create(Pago pago) {
        EntityManager em = null;
        int id = -1;
        try
        {
            em = getEntityManager();
            em.getTransaction().begin();
            Presupuesto fkIdPresupuesto = pago.getPresupuesto();
            if (fkIdPresupuesto != null)
            {
                fkIdPresupuesto = em.getReference(fkIdPresupuesto.getClass(), fkIdPresupuesto.getIdPresupuesto());
                pago.setPresupuesto(fkIdPresupuesto);
            }
            em.persist(pago);
            if (fkIdPresupuesto != null)
            {
                fkIdPresupuesto.getPagoList().add(pago);
                fkIdPresupuesto = em.merge(fkIdPresupuesto);
            }
            em.getTransaction().commit();
            id = pago.getIdPago();
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

    public void edit(Pago pago) throws NonexistentEntityException, Exception {
        EntityManager em = null;
        try
        {
            em = getEntityManager();
            em.getTransaction().begin();
            Pago persistentPago = em.find(Pago.class, pago.getIdPago());
            Presupuesto fkIdPresupuestoOld = persistentPago.getPresupuesto();
            Presupuesto fkIdPresupuestoNew = pago.getPresupuesto();
            if (fkIdPresupuestoNew != null)
            {
                fkIdPresupuestoNew = em.getReference(fkIdPresupuestoNew.getClass(), fkIdPresupuestoNew.getIdPresupuesto());
                pago.setPresupuesto(fkIdPresupuestoNew);
            }
            pago = em.merge(pago);
            if (fkIdPresupuestoOld != null && !fkIdPresupuestoOld.equals(fkIdPresupuestoNew))
            {
                fkIdPresupuestoOld.getPagoList().remove(pago);
                fkIdPresupuestoOld = em.merge(fkIdPresupuestoOld);
            }
            if (fkIdPresupuestoNew != null && !fkIdPresupuestoNew.equals(fkIdPresupuestoOld))
            {
                fkIdPresupuestoNew.getPagoList().add(pago);
                fkIdPresupuestoNew = em.merge(fkIdPresupuestoNew);
            }
            em.getTransaction().commit();
        }
        catch (Exception ex)
        {
            String msg = ex.getLocalizedMessage();
            if (msg == null || msg.length() == 0)
            {
                Integer id = pago.getIdPago();
                if (findPago(id) == null)
                {
                    throw new NonexistentEntityException("The pago with id " + id + " no longer exists.");
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
            Pago pago;
            try
            {
                pago = em.getReference(Pago.class, id);
                pago.getIdPago();
            }
            catch (EntityNotFoundException enfe)
            {
                throw new NonexistentEntityException("The pago with id " + id + " no longer exists.", enfe);
            }
            Presupuesto fkIdPresupuesto = pago.getPresupuesto();
            if (fkIdPresupuesto != null)
            {
                fkIdPresupuesto.getPagoList().remove(pago);
                fkIdPresupuesto = em.merge(fkIdPresupuesto);
            }
            em.remove(pago);
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

    public List<Pago> findPagoEntities() {
        return findPagoEntities(true, -1, -1);
    }

    public List<Pago> findPagoEntities(int maxResults, int firstResult) {
        return findPagoEntities(false, maxResults, firstResult);
    }

    private List<Pago> findPagoEntities(boolean all, int maxResults, int firstResult) {
        EntityManager em = getEntityManager();
        try
        {
            Query q = em.createQuery("select object(o) from Pago as o");
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

    public Pago findPago(Integer id) {
        EntityManager em = getEntityManager();
        try
        {
            return em.find(Pago.class, id);
        }
        finally
        {
            em.close();
        }
    }

    public int getPagoCount() {
        EntityManager em = getEntityManager();
        try
        {
            Query q = em.createQuery("select count(o) from Pago as o");
            return ((Long) q.getSingleResult()).intValue();
        }
        finally
        {
            em.close();
        }
    }

    public List<Pago> findPagosPresupuesto(Integer pIdPresupuesto) {
        EntityManager em = getEntityManager();

        List<Pago> misPagos = null;

        try
        {
            // Nombre del @NamedQuery definido en nuestra clase
            Query q = em.createNamedQuery("Pago.findByPresupuesto");

            //Le paso el nombre del parametro del query, y el valor a buscar.
            q.setParameter("idPresupuesto", pIdPresupuesto);

            misPagos = (List<Pago>) q.getResultList();

            return misPagos;

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
