/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.licensis.notaire.jpa;

import com.licensis.notaire.jpa.exceptions.NonexistentEntityException;
import com.licensis.notaire.jpa.interfaz.IPersistenciaJpa;
import com.licensis.notaire.business.Payment;
import com.licensis.notaire.business.Budget;
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
public class PaymentJpaController implements Serializable, IPersistenciaJpa
{

    public PaymentJpaController(UserTransaction utx, EntityManagerFactory emf)
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

    public int create(Payment payment)
    {
        EntityManager em = null;
        int id = -1;
        try
        {
            em = getEntityManager();
            em.getTransaction().begin();
            Budget fkIdBudget = payment.getBudget();
            if (fkIdBudget != null)
            {
                fkIdBudget = em.getReference(fkIdBudget.getClass(), fkIdBudget.getIdBudget());
                payment.setBudget(fkIdBudget);
            }
            em.persist(payment);
            if (fkIdBudget != null)
            {
                fkIdBudget.getPaymentList().add(payment);
                fkIdBudget = em.merge(fkIdBudget);
            }
            em.getTransaction().commit();
            id = payment.getIdPayment();
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

    public void edit(Payment payment) throws NonexistentEntityException, Exception
    {
        EntityManager em = null;
        try
        {
            em = getEntityManager();
            em.getTransaction().begin();
            Payment persistentPayment = em.find(Payment.class, payment.getIdPayment());
            Budget fkIdBudgetOld = persistentPayment.getBudget();
            Budget fkIdBudgetNew = payment.getBudget();
            if (fkIdBudgetNew != null)
            {
                fkIdBudgetNew = em.getReference(fkIdBudgetNew.getClass(), fkIdBudgetNew.getIdBudget());
                payment.setBudget(fkIdBudgetNew);
            }
            payment = em.merge(payment);
            if (fkIdBudgetOld != null && !fkIdBudgetOld.equals(fkIdBudgetNew))
            {
                fkIdBudgetOld.getPaymentList().remove(payment);
                fkIdBudgetOld = em.merge(fkIdBudgetOld);
            }
            if (fkIdBudgetNew != null && !fkIdBudgetNew.equals(fkIdBudgetOld))
            {
                fkIdBudgetNew.getPaymentList().add(payment);
                fkIdBudgetNew = em.merge(fkIdBudgetNew);
            }
            em.getTransaction().commit();
        }
        catch (Exception ex)
        {
            String msg = ex.getLocalizedMessage();
            if (msg == null || msg.length() == 0)
            {
                Integer id = payment.getIdPayment();
                if (findPayment(id) == null)
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

    public void destroy(Integer id) throws NonexistentEntityException
    {
        EntityManager em = null;
        try
        {
            em = getEntityManager();
            em.getTransaction().begin();
            Payment payment;
            try
            {
                payment = em.getReference(Payment.class, id);
                payment.getIdPayment();
            }
            catch (EntityNotFoundException enfe)
            {
                throw new NonexistentEntityException("The pago with id " + id + " no longer exists.", enfe);
            }
            Budget fkIdBudget = payment.getBudget();
            if (fkIdBudget != null)
            {
                fkIdBudget.getPaymentList().remove(payment);
                fkIdBudget = em.merge(fkIdBudget);
            }
            em.remove(payment);
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

    public List<Payment> findPaymentEntities()
    {
        return findPaymentEntities(true, -1, -1);
    }

    public List<Payment> findPaymentEntities(int maxResults, int firstResult)
    {
        return findPaymentEntities(false, maxResults, firstResult);
    }

    private List<Payment> findPaymentEntities(boolean all, int maxResults, int firstResult)
    {
        EntityManager em = getEntityManager();
        try
        {
            Query q = em.createQuery("select object(o) from Payment as o");
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

    public Payment findPayment(Integer id)
    {
        EntityManager em = getEntityManager();
        try
        {
            return em.find(Payment.class, id);
        }
        finally
        {
            em.close();
        }
    }

    public int getPaymentCount()
    {
        EntityManager em = getEntityManager();
        try
        {
            Query q = em.createQuery("select count(o) from Payment as o");
            return ((Long) q.getSingleResult()).intValue();
        }
        finally
        {
            em.close();
        }
    }

    public List<Payment> findPaymentsBudget(Integer pIdBudget)
    {
        EntityManager em = getEntityManager();

        List<Payment> misPayments = null;

        try
        {
            // Nombre del @NamedQuery definido en nuestra clase
            Query q = em.createNamedQuery("Pago.findByPresupuesto");

            //Le paso el nombre del parametro del query, y el valor a buscar.
            q.setParameter("idPresupuesto", pIdBudget);

            misPayments = (List<Payment>) q.getResultList();

            return misPayments;

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
