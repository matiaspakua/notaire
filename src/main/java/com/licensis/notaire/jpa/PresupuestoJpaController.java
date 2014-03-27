/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.licensis.notaire.jpa;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityNotFoundException;
import javax.persistence.Query;
import javax.transaction.UserTransaction;
import com.licensis.notaire.jpa.exceptions.ClassModifiedException;
import com.licensis.notaire.jpa.exceptions.IllegalOrphanException;
import com.licensis.notaire.jpa.exceptions.NonexistentEntityException;
import com.licensis.notaire.jpa.interfaz.IPersistenciaJpa;
import com.licensis.notaire.negocio.Item;
import com.licensis.notaire.negocio.Pago;
import com.licensis.notaire.negocio.Persona;
import com.licensis.notaire.negocio.Presupuesto;
import com.licensis.notaire.negocio.Tramite;

/**
 *
 * @author juanca
 */
public class PresupuestoJpaController implements Serializable, IPersistenciaJpa
{

    public PresupuestoJpaController(UserTransaction utx, EntityManagerFactory emf) {
        this.utx = utx;
        this.emf = emf;
    }
    private UserTransaction utx = null;
    private EntityManagerFactory emf = null;

    public EntityManager getEntityManager() {
        return emf.createEntityManager();
    }

    public int create(Presupuesto presupuesto) {
        int id = -1;
        if (presupuesto.getPagoList() == null)
        {
            presupuesto.setPagoList(new ArrayList<Pago>());
        }
        if (presupuesto.getTramiteList() == null)
        {
            presupuesto.setTramiteList(new ArrayList<Tramite>());
        }
        if (presupuesto.getItemList() == null)
        {
            presupuesto.setItemList(new ArrayList<Item>());
        }
        EntityManager em = null;
        try
        {
            em = getEntityManager();
            em.getTransaction().begin();
            Persona fkIdPersona = presupuesto.getFkIdPersona();
            if (fkIdPersona != null)
            {
                fkIdPersona = em.getReference(fkIdPersona.getClass(), fkIdPersona.getIdPersona());
                presupuesto.setFkIdPersona(fkIdPersona);
            }
            Tramite fkIdTramite = presupuesto.getFkIdTramite();
            if (fkIdTramite != null)
            {
                fkIdTramite = em.getReference(fkIdTramite.getClass(), fkIdTramite.getIdTramite());
                presupuesto.setFkIdTramite(fkIdTramite);
            }
            List<Pago> attachedPagoList = new ArrayList<Pago>();
            for (Pago pagoListPagoToAttach : presupuesto.getPagoList())
            {
                pagoListPagoToAttach = em.getReference(pagoListPagoToAttach.getClass(), pagoListPagoToAttach.getIdPago());
                attachedPagoList.add(pagoListPagoToAttach);
            }
            presupuesto.setPagoList(attachedPagoList);
            List<Tramite> attachedTramiteList = new ArrayList<Tramite>();
            for (Tramite tramiteListTramiteToAttach : presupuesto.getTramiteList())
            {
                tramiteListTramiteToAttach = em.getReference(tramiteListTramiteToAttach.getClass(), tramiteListTramiteToAttach.getIdTramite());
                attachedTramiteList.add(tramiteListTramiteToAttach);
            }
            presupuesto.setTramiteList(attachedTramiteList);
            List<Item> attachedItemList = new ArrayList<Item>();
            for (Item itemListItemToAttach : presupuesto.getItemList())
            {
                itemListItemToAttach = em.getReference(itemListItemToAttach.getClass(), itemListItemToAttach.getIdItem());
                attachedItemList.add(itemListItemToAttach);
            }
            presupuesto.setItemList(attachedItemList);
            em.persist(presupuesto);
            if (fkIdPersona != null)
            {
                fkIdPersona.getPresupuestoList().add(presupuesto);
                fkIdPersona = em.merge(fkIdPersona);
            }
            if (fkIdTramite != null)
            {
                fkIdTramite.getPresupuestoList().add(presupuesto);
                fkIdTramite = em.merge(fkIdTramite);
            }
            for (Pago pagoListPago : presupuesto.getPagoList())
            {
                Presupuesto oldFkIdPresupuestoOfPagoListPago = pagoListPago.getPresupuesto();
                pagoListPago.setPresupuesto(presupuesto);
                pagoListPago = em.merge(pagoListPago);
                if (oldFkIdPresupuestoOfPagoListPago != null)
                {
                    oldFkIdPresupuestoOfPagoListPago.getPagoList().remove(pagoListPago);
                    oldFkIdPresupuestoOfPagoListPago = em.merge(oldFkIdPresupuestoOfPagoListPago);
                }
            }
            for (Tramite tramiteListTramite : presupuesto.getTramiteList())
            {
                Presupuesto oldFkIdPresupuestoOfTramiteListTramite = tramiteListTramite.getFkIdPresupuesto();
                tramiteListTramite.setFkIdPresupuesto(presupuesto);
                tramiteListTramite = em.merge(tramiteListTramite);
                if (oldFkIdPresupuestoOfTramiteListTramite != null)
                {
                    oldFkIdPresupuestoOfTramiteListTramite.getTramiteList().remove(tramiteListTramite);
                    oldFkIdPresupuestoOfTramiteListTramite = em.merge(oldFkIdPresupuestoOfTramiteListTramite);
                }
            }
            for (Item itemListItem : presupuesto.getItemList())
            {
                Presupuesto oldFkIdPresupuestoOfItemListItem = itemListItem.getFkIdPresupuesto();
                itemListItem.setFkIdPresupuesto(presupuesto);
                itemListItem = em.merge(itemListItem);
                if (oldFkIdPresupuestoOfItemListItem != null)
                {
                    oldFkIdPresupuestoOfItemListItem.getItemList().remove(itemListItem);
                    oldFkIdPresupuestoOfItemListItem = em.merge(oldFkIdPresupuestoOfItemListItem);
                }
            }
            em.getTransaction().commit();
            id = presupuesto.getIdPresupuesto();
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

    public Boolean edit(Presupuesto presupuesto) throws IllegalOrphanException, NonexistentEntityException, ClassModifiedException {
        EntityManager em = null;
        Boolean modificado = Boolean.FALSE;
        Integer version = -1;
        Integer oldVersion = -1;

        em = getEntityManager();
        em.getTransaction().begin();
        Presupuesto persistentPresupuesto = em.find(Presupuesto.class, presupuesto.getIdPresupuesto());

        if (persistentPresupuesto != null)
        {
            version = persistentPresupuesto.getVersion(); // Version del Objeto en db
            oldVersion = presupuesto.getVersion(); // Version del Objeto en memoria

            if (version != oldVersion) //Si son distintas "Alguien modifico el objeto"
            {
                if (em != null)
                {
                    em.close();
                }
                throw new ClassModifiedException();

            }
            else
            {

                Persona fkIdPersonaOld = persistentPresupuesto.getFkIdPersona();
                Persona fkIdPersonaNew = presupuesto.getFkIdPersona();
                Tramite fkIdTramiteOld = persistentPresupuesto.getFkIdTramite();
                Tramite fkIdTramiteNew = presupuesto.getFkIdTramite();
                List<Pago> pagoListOld = persistentPresupuesto.getPagoList();
                List<Pago> pagoListNew = presupuesto.getPagoList();
                List<Tramite> tramiteListOld = persistentPresupuesto.getTramiteList();
                List<Tramite> tramiteListNew = presupuesto.getTramiteList();
                List<Item> itemListOld = persistentPresupuesto.getItemList();
                List<Item> itemListNew = presupuesto.getItemList();
                List<String> illegalOrphanMessages = null;
                for (Pago pagoListOldPago : pagoListOld)
                {
                    if (!pagoListNew.contains(pagoListOldPago))
                    {
                        if (illegalOrphanMessages == null)
                        {
                            illegalOrphanMessages = new ArrayList<String>();
                        }
                        illegalOrphanMessages.add("You must retain Pago " + pagoListOldPago + " since its fkIdPresupuesto field is not nullable.");
                    }
                }
                for (Tramite tramiteListOldTramite : tramiteListOld)
                {
                    if (!tramiteListNew.contains(tramiteListOldTramite))
                    {
                        if (illegalOrphanMessages == null)
                        {
                            illegalOrphanMessages = new ArrayList<String>();
                        }
                        illegalOrphanMessages.add("You must retain Tramite " + tramiteListOldTramite + " since its fkIdPresupuesto field is not nullable.");
                    }
                }
                for (Item itemListOldItem : itemListOld)
                {
                    if (!itemListNew.contains(itemListOldItem))
                    {
                        if (illegalOrphanMessages == null)
                        {
                            illegalOrphanMessages = new ArrayList<String>();
                        }
                        illegalOrphanMessages.add("You must retain Item " + itemListOldItem + " since its fkIdPresupuesto field is not nullable.");
                    }
                }
                if (illegalOrphanMessages != null)
                {
                    throw new IllegalOrphanException(illegalOrphanMessages);
                }
                if (fkIdPersonaNew != null)
                {
                    fkIdPersonaNew = em.getReference(fkIdPersonaNew.getClass(), fkIdPersonaNew.getIdPersona());
                    presupuesto.setFkIdPersona(fkIdPersonaNew);
                }
                if (fkIdTramiteNew != null)
                {
                    fkIdTramiteNew = em.getReference(fkIdTramiteNew.getClass(), fkIdTramiteNew.getIdTramite());
                    presupuesto.setFkIdTramite(fkIdTramiteNew);
                }
                List<Pago> attachedPagoListNew = new ArrayList<Pago>();
                for (Pago pagoListNewPagoToAttach : pagoListNew)
                {
                    pagoListNewPagoToAttach = em.getReference(pagoListNewPagoToAttach.getClass(), pagoListNewPagoToAttach.getIdPago());
                    attachedPagoListNew.add(pagoListNewPagoToAttach);
                }
                pagoListNew = attachedPagoListNew;
                presupuesto.setPagoList(pagoListNew);
                List<Tramite> attachedTramiteListNew = new ArrayList<Tramite>();
                for (Tramite tramiteListNewTramiteToAttach : tramiteListNew)
                {
                    tramiteListNewTramiteToAttach = em.getReference(tramiteListNewTramiteToAttach.getClass(), tramiteListNewTramiteToAttach.getIdTramite());
                    attachedTramiteListNew.add(tramiteListNewTramiteToAttach);
                }
                tramiteListNew = attachedTramiteListNew;
                presupuesto.setTramiteList(tramiteListNew);
                List<Item> attachedItemListNew = new ArrayList<Item>();
                for (Item itemListNewItemToAttach : itemListNew)
                {
                    itemListNewItemToAttach = em.getReference(itemListNewItemToAttach.getClass(), itemListNewItemToAttach.getIdItem());
                    attachedItemListNew.add(itemListNewItemToAttach);
                }
                itemListNew = attachedItemListNew;
                presupuesto.setItemList(itemListNew);
                presupuesto = em.merge(presupuesto);
                if (fkIdPersonaOld != null && !fkIdPersonaOld.equals(fkIdPersonaNew))
                {
                    fkIdPersonaOld.getPresupuestoList().remove(presupuesto);
                    fkIdPersonaOld = em.merge(fkIdPersonaOld);
                }
                if (fkIdPersonaNew != null && !fkIdPersonaNew.equals(fkIdPersonaOld))
                {
                    fkIdPersonaNew.getPresupuestoList().add(presupuesto);
                    fkIdPersonaNew = em.merge(fkIdPersonaNew);
                }
                if (fkIdTramiteOld != null && !fkIdTramiteOld.equals(fkIdTramiteNew))
                {
                    fkIdTramiteOld.getPresupuestoList().remove(presupuesto);
                    fkIdTramiteOld = em.merge(fkIdTramiteOld);
                }
                if (fkIdTramiteNew != null && !fkIdTramiteNew.equals(fkIdTramiteOld))
                {
                    fkIdTramiteNew.getPresupuestoList().add(presupuesto);
                    fkIdTramiteNew = em.merge(fkIdTramiteNew);
                }
                for (Pago pagoListNewPago : pagoListNew)
                {
                    if (!pagoListOld.contains(pagoListNewPago))
                    {
                        Presupuesto oldFkIdPresupuestoOfPagoListNewPago = pagoListNewPago.getPresupuesto();
                        pagoListNewPago.setPresupuesto(presupuesto);
                        pagoListNewPago = em.merge(pagoListNewPago);
                        if (oldFkIdPresupuestoOfPagoListNewPago != null && !oldFkIdPresupuestoOfPagoListNewPago.equals(presupuesto))
                        {
                            oldFkIdPresupuestoOfPagoListNewPago.getPagoList().remove(pagoListNewPago);
                            oldFkIdPresupuestoOfPagoListNewPago = em.merge(oldFkIdPresupuestoOfPagoListNewPago);
                        }
                    }
                }
                for (Tramite tramiteListNewTramite : tramiteListNew)
                {
                    if (!tramiteListOld.contains(tramiteListNewTramite))
                    {
                        Presupuesto oldFkIdPresupuestoOfTramiteListNewTramite = tramiteListNewTramite.getFkIdPresupuesto();
                        tramiteListNewTramite.setFkIdPresupuesto(presupuesto);
                        tramiteListNewTramite = em.merge(tramiteListNewTramite);
                        if (oldFkIdPresupuestoOfTramiteListNewTramite != null && !oldFkIdPresupuestoOfTramiteListNewTramite.equals(presupuesto))
                        {
                            oldFkIdPresupuestoOfTramiteListNewTramite.getTramiteList().remove(tramiteListNewTramite);
                            oldFkIdPresupuestoOfTramiteListNewTramite = em.merge(oldFkIdPresupuestoOfTramiteListNewTramite);
                        }
                    }
                }
                for (Item itemListNewItem : itemListNew)
                {
                    if (!itemListOld.contains(itemListNewItem))
                    {
                        Presupuesto oldFkIdPresupuestoOfItemListNewItem = itemListNewItem.getFkIdPresupuesto();
                        itemListNewItem.setFkIdPresupuesto(presupuesto);
                        itemListNewItem = em.merge(itemListNewItem);
                        if (oldFkIdPresupuestoOfItemListNewItem != null && !oldFkIdPresupuestoOfItemListNewItem.equals(presupuesto))
                        {
                            oldFkIdPresupuestoOfItemListNewItem.getItemList().remove(itemListNewItem);
                            oldFkIdPresupuestoOfItemListNewItem = em.merge(oldFkIdPresupuestoOfItemListNewItem);
                        }
                    }
                }
                em.getTransaction().commit();
                modificado = Boolean.TRUE;
                if (em != null)
                {
                    em.close();
                }
            }
        }
        return modificado;
    }

    public void destroy(Integer id) throws IllegalOrphanException, NonexistentEntityException {
        EntityManager em = null;
        try
        {
            em = getEntityManager();
            em.getTransaction().begin();
            Presupuesto presupuesto;


            try
            {
                presupuesto = em.getReference(Presupuesto.class, id);
                presupuesto.getIdPresupuesto();
            }
            catch (EntityNotFoundException enfe)
            {
                throw new NonexistentEntityException("The presupuesto with id " + id + " no longer exists.", enfe);
            }
            List<String> illegalOrphanMessages = null;
            List<Pago> pagoListOrphanCheck = presupuesto.getPagoList();
            for (Pago pagoListOrphanCheckPago : pagoListOrphanCheck)
            {
                if (illegalOrphanMessages == null)
                {
                    illegalOrphanMessages = new ArrayList<String>();
                }
                illegalOrphanMessages.add("This Presupuesto (" + presupuesto + ") cannot be destroyed since the Pago " + pagoListOrphanCheckPago + " in its pagoList field has a non-nullable fkIdPresupuesto field.");
            }
            List<Tramite> tramiteListOrphanCheck = presupuesto.getTramiteList();
            for (Tramite tramiteListOrphanCheckTramite : tramiteListOrphanCheck)
            {
                if (illegalOrphanMessages == null)
                {
                    illegalOrphanMessages = new ArrayList<String>();
                }
                illegalOrphanMessages.add("This Presupuesto (" + presupuesto + ") cannot be destroyed since the Tramite " + tramiteListOrphanCheckTramite + " in its tramiteList field has a non-nullable fkIdPresupuesto field.");
            }
            List<Item> itemListOrphanCheck = presupuesto.getItemList();
            for (Item itemListOrphanCheckItem : itemListOrphanCheck)
            {
                if (illegalOrphanMessages == null)
                {
                    illegalOrphanMessages = new ArrayList<String>();
                }
                illegalOrphanMessages.add("This Presupuesto (" + presupuesto + ") cannot be destroyed since the Item " + itemListOrphanCheckItem + " in its itemList field has a non-nullable fkIdPresupuesto field.");
            }
            if (illegalOrphanMessages != null)
            {
                throw new IllegalOrphanException(illegalOrphanMessages);
            }
            Persona fkIdPersona = presupuesto.getFkIdPersona();
            if (fkIdPersona != null)
            {
                fkIdPersona.getPresupuestoList().remove(presupuesto);
                fkIdPersona = em.merge(fkIdPersona);
            }
            Tramite fkIdTramite = presupuesto.getFkIdTramite();
            if (fkIdTramite != null)
            {
                fkIdTramite.getPresupuestoList().remove(presupuesto);
                fkIdTramite = em.merge(fkIdTramite);
            }
            em.remove(presupuesto);
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

    public List<Presupuesto> findPresupuestoEntities() {
        return findPresupuestoEntities(true, -1, -1);
    }

    public List<Presupuesto> findPresupuestoEntities(int maxResults, int firstResult) {
        return findPresupuestoEntities(false, maxResults, firstResult);
    }

    private List<Presupuesto> findPresupuestoEntities(boolean all, int maxResults, int firstResult) {
        EntityManager em = getEntityManager();
        try
        {
            Query q = em.createQuery("select object(o) from Presupuesto as o");
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

    public Presupuesto findPresupuesto(Integer id) {
        EntityManager em = getEntityManager();
        Presupuesto miPresupuesto = null;

        miPresupuesto = em.find(Presupuesto.class, id);
        if (miPresupuesto
                != null)
        {
            miPresupuesto.setItemList(null);
            miPresupuesto.setTramiteList(null);
        }
        return miPresupuesto;
    }

    public int getPresupuestoCount() {
        EntityManager em = getEntityManager();
        try
        {
            Query q = em.createQuery("select count(o) from Presupuesto as o");
            return ((Long) q.getSingleResult()).intValue();
        }
        finally
        {
            em.close();
        }
    }

    /**
     * Busca todos los presupuestos asociados a una persona.
     *
     * @param pIdPersona El ID de la persona.
     * @return misPresupuestos Una lista con todos los presupuestos asociados a la persona indicada.
     */
    public List<Presupuesto> findPresupuestosPersona(Integer pIdPersona) {
        EntityManager em = getEntityManager();

        List<Presupuesto> misPresupuestos = new ArrayList<>();

        try
        {
            // Nombre del @NamedQuery definido en nuestra clase
            Query q = em.createNamedQuery("Presupuesto.findByPersona");

            //Le paso el nombre del parametro del query, y el valor a buscar.
            q.setParameter("idPersona", pIdPersona);

            misPresupuestos = (List<Presupuesto>) q.getResultList();
            TramiteJpaController jpaTramite = new TramiteJpaController(utx, emf);

            for (Iterator<Presupuesto> it = misPresupuestos.iterator(); it.hasNext();)
            {
                Presupuesto presupuesto = it.next();

                presupuesto.setTramiteList(jpaTramite.encontrarTramitePresupuesto(presupuesto.getIdPresupuesto()));
            }

        }
        finally
        {
            em.close();
        }
        return misPresupuestos;
    }

    /**
     * Busca un los presupuesto asociados a una persona (incluyendo las referencias hacia el
     * tramite).
     *
     * @param pIdPersona
     * @param pIdTramite
     * @return
     */
    public List<Presupuesto> findPresupuestosPersonaTramite(Integer pIdPersona, Integer pIdTramite) {
        EntityManager em = getEntityManager();

        List<Presupuesto> misPresupuestos = new ArrayList<>();

        try
        {
            // Nombre del @NamedQuery definido en nuestra clase
            Query q = em.createNamedQuery("Presupuesto.findByPersonaTramite");

            //Le paso el nombre del parametro del query, y el valor a buscar.
            q.setParameter("idPersona", pIdPersona);
            q.setParameter("idTramite", pIdTramite);

            misPresupuestos = (List<Presupuesto>) q.getResultList();
        }
        finally
        {
            em.close();
        }
        return misPresupuestos;
    }
    
    public Presupuesto findPresupuestosById(Integer idPresupuesto) {
        EntityManager em = getEntityManager();

        Presupuesto miPresupuesto = null;

        try
        {
            // Nombre del @NamedQuery definido en nuestra clase
            Query q = em.createNamedQuery("Presupuesto.findByIdPresupuesto");

            //Le paso el nombre del parametro del query, y el valor a buscar.
            q.setParameter("idPresupuesto", idPresupuesto);

            miPresupuesto = (Presupuesto) q.getResultList().get(0);
        }
        finally
        {
            em.close();
        }
        return miPresupuesto;
    }

    @Override
    public String getNombreJpa() {
        return this.getClass().getName();
    }
}
