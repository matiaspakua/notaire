/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.licensis.notaire.jpa;

import com.licensis.notaire.jpa.exceptions.ClassModifiedException;
import com.licensis.notaire.jpa.exceptions.IllegalOrphanException;
import com.licensis.notaire.jpa.exceptions.NonexistentEntityException;
import com.licensis.notaire.jpa.interfaz.IPersistenciaJpa;
import com.licensis.notaire.business.Item;
import com.licensis.notaire.business.Payment;
import com.licensis.notaire.business.Person;
import com.licensis.notaire.business.Budget;
import com.licensis.notaire.business.Procedure;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
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
public class BudgetJpaController implements Serializable, IPersistenciaJpa {

    public BudgetJpaController(UserTransaction utx, EntityManagerFactory emf) {
        this.utx = utx;
        this.emf = emf;
    }

    private UserTransaction utx = null;
    private EntityManagerFactory emf = null;

    public EntityManager getEntityManager() {
        return emf.createEntityManager();
    }

    public int create(Budget budget) {
        int id = -1;
        if (budget.getPaymentList() == null) {
            budget.setPaymentList(new java.util.HashSet<Payment>());
        }
        if (budget.getProcedureList() == null) {
            budget.setProcedureList(new ArrayList<Procedure>());
        }
        if (budget.getItemList() == null) {
            budget.setItemList(new ArrayList<Item>());
        }
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            Person fkIdPerson = budget.getFkIdPerson();
            if (fkIdPerson != null) {
                fkIdPerson = em.getReference(fkIdPerson.getClass(), fkIdPerson.getPersonId());
                budget.setFkIdPerson(fkIdPerson);
            }
            java.util.Set<Payment> attachedPaymentList = new java.util.HashSet<Payment>();
            for (Payment paymentListPaymentToAttach : budget.getPaymentList()) {
                paymentListPaymentToAttach = em.getReference(paymentListPaymentToAttach.getClass(),
                        paymentListPaymentToAttach.getIdPayment());
                attachedPaymentList.add(paymentListPaymentToAttach);
            }
            budget.setPaymentList(attachedPaymentList);
            List<Procedure> attachedProcedureList = new ArrayList<Procedure>();
            for (Procedure procedureListProcedureToAttach : budget.getProcedureList()) {
                procedureListProcedureToAttach = em.getReference(procedureListProcedureToAttach.getClass(),
                        procedureListProcedureToAttach.getIdProcedure());
                attachedProcedureList.add(procedureListProcedureToAttach);
            }
            budget.setProcedureList(attachedProcedureList);
            List<Item> attachedItemList = new ArrayList<Item>();
            for (Item itemListItemToAttach : budget.getItemList()) {
                itemListItemToAttach = em.getReference(itemListItemToAttach.getClass(),
                        itemListItemToAttach.getIdItem());
                attachedItemList.add(itemListItemToAttach);
            }
            budget.setItemList(attachedItemList);
            em.persist(budget);
            if (fkIdPerson != null) {
                fkIdPerson.getBudgetList().add(budget);
                fkIdPerson = em.merge(fkIdPerson);
            }
            for (Payment paymentListPayment : budget.getPaymentList()) {
                Budget oldFkIdBudgetOfPaymentListPayment = paymentListPayment.getBudget();
                paymentListPayment.setBudget(budget);
                paymentListPayment = em.merge(paymentListPayment);
                if (oldFkIdBudgetOfPaymentListPayment != null) {
                    oldFkIdBudgetOfPaymentListPayment.getPaymentList().remove(paymentListPayment);
                    oldFkIdBudgetOfPaymentListPayment = em.merge(oldFkIdBudgetOfPaymentListPayment);
                }
            }
            for (Procedure procedureListProcedure : budget.getProcedureList()) {
                Budget oldFkIdBudgetOfProcedureListProcedure = procedureListProcedure.getFkIdBudget();
                procedureListProcedure.setFkIdBudget(budget);
                procedureListProcedure = em.merge(procedureListProcedure);
                if (oldFkIdBudgetOfProcedureListProcedure != null) {
                    oldFkIdBudgetOfProcedureListProcedure.getProcedureList().remove(procedureListProcedure);
                    oldFkIdBudgetOfProcedureListProcedure = em.merge(oldFkIdBudgetOfProcedureListProcedure);
                }
            }
            for (Item itemListItem : budget.getItemList()) {
                Budget oldFkIdBudgetOfItemListItem = itemListItem.getFkIdBudget();
                itemListItem.setFkIdBudget(budget);
                itemListItem = em.merge(itemListItem);
                if (oldFkIdBudgetOfItemListItem != null) {
                    oldFkIdBudgetOfItemListItem.getItemList().remove(itemListItem);
                    oldFkIdBudgetOfItemListItem = em.merge(oldFkIdBudgetOfItemListItem);
                }
            }
            em.getTransaction().commit();
            id = budget.getIdBudget();
        } finally {
            if (em != null) {
                em.close();
            }
        }
        return id;
    }

    public Boolean edit(Budget budget)
            throws IllegalOrphanException, NonexistentEntityException, ClassModifiedException {
        EntityManager em = null;
        Boolean modificado = Boolean.FALSE;
        Integer version = -1;
        Integer oldVersion = -1;

        em = getEntityManager();
        em.getTransaction().begin();
        Budget persistentBudget = em.find(Budget.class, budget.getIdBudget());

        if (persistentBudget != null) {
            version = persistentBudget.getVersion(); // Version del Objeto en db
            oldVersion = budget.getVersion(); // Version del Objeto en memoria

            if (version != oldVersion) // Si son distintas "Alguien modifico el objeto"
            {
                if (em != null) {
                    em.close();
                }
                throw new ClassModifiedException();

            } else {

                Person fkIdPersonOld = persistentBudget.getFkIdPerson();
                Person fkIdPersonNew = budget.getFkIdPerson();
                java.util.Set<Payment> paymentListOld = persistentBudget.getPaymentList();
                java.util.Set<Payment> paymentListNew = budget.getPaymentList();
                List<Procedure> procedureListOld = persistentBudget.getProcedureList();
                List<Procedure> procedureListNew = budget.getProcedureList();
                List<Item> itemListOld = persistentBudget.getItemList();
                List<Item> itemListNew = budget.getItemList();
                List<String> illegalOrphanMessages = null;
                for (Payment paymentListOldPayment : paymentListOld) {
                    if (!paymentListNew.contains(paymentListOldPayment)) {
                        if (illegalOrphanMessages == null) {
                            illegalOrphanMessages = new ArrayList<String>();
                        }
                        illegalOrphanMessages.add("You must retain Pago " + paymentListOldPayment
                                + " since its fkIdPresupuesto field is not nullable.");
                    }
                }
                for (Procedure procedureListOldProcedure : procedureListOld) {
                    if (!procedureListNew.contains(procedureListOldProcedure)) {
                        if (illegalOrphanMessages == null) {
                            illegalOrphanMessages = new ArrayList<String>();
                        }
                        illegalOrphanMessages.add("You must retain Tramite " + procedureListOldProcedure
                                + " since its fkIdPresupuesto field is not nullable.");
                    }
                }
                for (Item itemListOldItem : itemListOld) {
                    if (!itemListNew.contains(itemListOldItem)) {
                        if (illegalOrphanMessages == null) {
                            illegalOrphanMessages = new ArrayList<String>();
                        }
                        illegalOrphanMessages.add("You must retain Item " + itemListOldItem
                                + " since its fkIdPresupuesto field is not nullable.");
                    }
                }
                if (illegalOrphanMessages != null) {
                    throw new IllegalOrphanException(illegalOrphanMessages);
                }
                if (fkIdPersonNew != null) {
                    fkIdPersonNew = em.getReference(fkIdPersonNew.getClass(), fkIdPersonNew.getPersonId());
                    budget.setFkIdPerson(fkIdPersonNew);
                }
                java.util.Set<Payment> attachedPaymentListNew = new java.util.HashSet<Payment>();
                for (Payment paymentListNewPaymentToAttach : paymentListNew) {
                    paymentListNewPaymentToAttach = em.getReference(paymentListNewPaymentToAttach.getClass(),
                            paymentListNewPaymentToAttach.getIdPayment());
                    attachedPaymentListNew.add(paymentListNewPaymentToAttach);
                }
                paymentListNew = attachedPaymentListNew;
                budget.setPaymentList(paymentListNew);
                List<Procedure> attachedProcedureListNew = new ArrayList<Procedure>();
                for (Procedure procedureListNewProcedureToAttach : procedureListNew) {
                    procedureListNewProcedureToAttach = em.getReference(procedureListNewProcedureToAttach.getClass(),
                            procedureListNewProcedureToAttach.getIdProcedure());
                    attachedProcedureListNew.add(procedureListNewProcedureToAttach);
                }
                procedureListNew = attachedProcedureListNew;
                budget.setProcedureList(procedureListNew);
                List<Item> attachedItemListNew = new ArrayList<Item>();
                for (Item itemListNewItemToAttach : itemListNew) {
                    itemListNewItemToAttach = em.getReference(itemListNewItemToAttach.getClass(),
                            itemListNewItemToAttach.getIdItem());
                    attachedItemListNew.add(itemListNewItemToAttach);
                }
                itemListNew = attachedItemListNew;
                budget.setItemList(itemListNew);
                budget = em.merge(budget);
                if (fkIdPersonOld != null && !fkIdPersonOld.equals(fkIdPersonNew)) {
                    fkIdPersonOld.getBudgetList().remove(budget);
                    fkIdPersonOld = em.merge(fkIdPersonOld);
                }
                if (fkIdPersonNew != null && !fkIdPersonNew.equals(fkIdPersonOld)) {
                    fkIdPersonNew.getBudgetList().add(budget);
                    fkIdPersonNew = em.merge(fkIdPersonNew);
                }
                for (Payment paymentListNewPayment : paymentListNew) {
                    if (!paymentListOld.contains(paymentListNewPayment)) {
                        Budget oldFkIdBudgetOfPaymentListNewPayment = paymentListNewPayment.getBudget();
                        paymentListNewPayment.setBudget(budget);
                        paymentListNewPayment = em.merge(paymentListNewPayment);
                        if (oldFkIdBudgetOfPaymentListNewPayment != null
                                && !oldFkIdBudgetOfPaymentListNewPayment.equals(budget)) {
                            oldFkIdBudgetOfPaymentListNewPayment.getPaymentList().remove(paymentListNewPayment);
                            oldFkIdBudgetOfPaymentListNewPayment = em.merge(oldFkIdBudgetOfPaymentListNewPayment);
                        }
                    }
                }
                for (Procedure procedureListNewProcedure : procedureListNew) {
                    if (!procedureListOld.contains(procedureListNewProcedure)) {
                        Budget oldFkIdBudgetOfProcedureListNewProcedure = procedureListNewProcedure
                                .getFkIdBudget();
                        procedureListNewProcedure.setFkIdBudget(budget);
                        procedureListNewProcedure = em.merge(procedureListNewProcedure);
                        if (oldFkIdBudgetOfProcedureListNewProcedure != null
                                && !oldFkIdBudgetOfProcedureListNewProcedure.equals(budget)) {
                            oldFkIdBudgetOfProcedureListNewProcedure.getProcedureList().remove(procedureListNewProcedure);
                            oldFkIdBudgetOfProcedureListNewProcedure = em
                                    .merge(oldFkIdBudgetOfProcedureListNewProcedure);
                        }
                    }
                }
                for (Item itemListNewItem : itemListNew) {
                    if (!itemListOld.contains(itemListNewItem)) {
                        Budget oldFkIdBudgetOfItemListNewItem = itemListNewItem.getFkIdBudget();
                        itemListNewItem.setFkIdBudget(budget);
                        itemListNewItem = em.merge(itemListNewItem);
                        if (oldFkIdBudgetOfItemListNewItem != null
                                && !oldFkIdBudgetOfItemListNewItem.equals(budget)) {
                            oldFkIdBudgetOfItemListNewItem.getItemList().remove(itemListNewItem);
                            oldFkIdBudgetOfItemListNewItem = em.merge(oldFkIdBudgetOfItemListNewItem);
                        }
                    }
                }
                em.getTransaction().commit();
                modificado = Boolean.TRUE;
                if (em != null) {
                    em.close();
                }
            }
        }
        return modificado;
    }

    public void destroy(Integer id) throws IllegalOrphanException, NonexistentEntityException {
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            Budget budget;

            try {
                budget = em.getReference(Budget.class, id);
                budget.getIdBudget();
            } catch (EntityNotFoundException enfe) {
                throw new NonexistentEntityException("The presupuesto with id " + id + " no longer exists.", enfe);
            }
            List<String> illegalOrphanMessages = null;
            java.util.Set<Payment> paymentListOrphanCheck = budget.getPaymentList();
            for (Payment paymentListOrphanCheckPayment : paymentListOrphanCheck) {
                if (illegalOrphanMessages == null) {
                    illegalOrphanMessages = new ArrayList<String>();
                }
                illegalOrphanMessages.add("This Presupuesto (" + budget + ") cannot be destroyed since the Pago "
                        + paymentListOrphanCheckPayment + " in its pagoList field has a non-nullable fkIdPresupuesto field.");
            }
            List<Procedure> procedureListOrphanCheck = budget.getProcedureList();
            for (Procedure procedureListOrphanCheckProcedure : procedureListOrphanCheck) {
                if (illegalOrphanMessages == null) {
                    illegalOrphanMessages = new ArrayList<String>();
                }
                illegalOrphanMessages.add("This Presupuesto (" + budget
                        + ") cannot be destroyed since the Tramite " + procedureListOrphanCheckProcedure
                        + " in its tramiteList field has a non-nullable fkIdPresupuesto field.");
            }
            List<Item> itemListOrphanCheck = budget.getItemList();
            for (Item itemListOrphanCheckItem : itemListOrphanCheck) {
                if (illegalOrphanMessages == null) {
                    illegalOrphanMessages = new ArrayList<String>();
                }
                illegalOrphanMessages.add("This Presupuesto (" + budget + ") cannot be destroyed since the Item "
                        + itemListOrphanCheckItem + " in its itemList field has a non-nullable fkIdPresupuesto field.");
            }
            if (illegalOrphanMessages != null) {
                throw new IllegalOrphanException(illegalOrphanMessages);
            }
            Person fkIdPerson = budget.getFkIdPerson();
            if (fkIdPerson != null) {
                fkIdPerson.getBudgetList().remove(budget);
                fkIdPerson = em.merge(fkIdPerson);
            }
            em.remove(budget);
            em.getTransaction().commit();
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public List<Budget> findBudgetEntities() {
        return findBudgetEntities(true, -1, -1);
    }

    public List<Budget> findBudgetEntities(int maxResults, int firstResult) {
        return findBudgetEntities(false, maxResults, firstResult);
    }

    private List<Budget> findBudgetEntities(boolean all, int maxResults, int firstResult) {
        EntityManager em = getEntityManager();
        try {
            Query q = em.createQuery("select object(o) from Presupuesto as o");
            if (!all) {
                q.setMaxResults(maxResults);
                q.setFirstResult(firstResult);
            }
            return q.getResultList();
        } finally {
            em.close();
        }
    }

    public Budget findBudget(Integer id) {
        EntityManager em = getEntityManager();
        Budget miBudget = null;

        miBudget = em.find(Budget.class, id);
        if (miBudget != null) {
            miBudget.setItemList(null);
            miBudget.setProcedureList(null);
        }
        return miBudget;
    }

    public int getBudgetCount() {
        EntityManager em = getEntityManager();
        try {
            Query q = em.createQuery("select count(o) from Presupuesto as o");
            return ((Long) q.getSingleResult()).intValue();
        } finally {
            em.close();
        }
    }

    /**
     * Busca todos los presupuestos asociados a una persona.
     *
     * @param pIdPersona El ID de la persona.
     * @return misPresupuestos Una lista con todos los presupuestos asociados a la
     *         persona indicada.
     */
    public List<Budget> findPresupuestosPerson(Integer pIdPerson) {
        EntityManager em = getEntityManager();

        List<Budget> misPresupuestos = new ArrayList<>();

        try {
            // Nombre del @NamedQuery definido en nuestra clase
            Query q = em.createNamedQuery("Presupuesto.findByPersona");

            // Le paso el nombre del parametro del query, y el valor a buscar.
            q.setParameter("idPersona", pIdPerson);

            misPresupuestos = (List<Budget>) q.getResultList();
            ProcedureJpaController jpaProcedure = new ProcedureJpaController(utx, emf);

            for (Iterator<Budget> it = misPresupuestos.iterator(); it.hasNext();) {
                Budget budget = it.next();

                budget.setProcedureList(jpaProcedure.encontrarProcedureBudget(budget.getIdBudget()));
            }

        } finally {
            em.close();
        }
        return misPresupuestos;
    }

    /**
     * Busca un los presupuesto asociados a una persona (incluyendo las referencias
     * hacia el
     * tramite).
     *
     * @param pIdPersona
     * @param pIdTramite
     * @return
     */
    public List<Budget> findPresupuestosPersonProcedure(Integer pIdPerson, Integer pIdProcedure) {
        EntityManager em = getEntityManager();

        List<Budget> misPresupuestos = new ArrayList<>();

        try {
            // Nombre del @NamedQuery definido en nuestra clase
            Query q = em.createNamedQuery("Presupuesto.findByPersonaTramite");

            // Le paso el nombre del parametro del query, y el valor a buscar.
            q.setParameter("idPersona", pIdPerson);
            q.setParameter("idTramite", pIdProcedure);

            misPresupuestos = (List<Budget>) q.getResultList();
        } finally {
            em.close();
        }
        return misPresupuestos;
    }

    public Budget findPresupuestosById(Integer idBudget) {
        EntityManager em = getEntityManager();

        Budget miBudget = null;

        try {
            // Nombre del @NamedQuery definido en nuestra clase
            Query q = em.createNamedQuery("Presupuesto.findByIdPresupuesto");

            // Le paso el nombre del parametro del query, y el valor a buscar.
            q.setParameter("idPresupuesto", idBudget);

            miBudget = (Budget) q.getResultList().get(0);
        } finally {
            em.close();
        }
        return miBudget;
    }

    @Override
    public String getNameJpa() {
        return this.getClass().getName();
    }
}
