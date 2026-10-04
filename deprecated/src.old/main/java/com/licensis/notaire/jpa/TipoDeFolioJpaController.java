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
import com.licensis.notaire.negocio.Folio;
import com.licensis.notaire.negocio.TipoDeFolio;
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
 * @author matias
 */
public class TipoDeFolioJpaController implements Serializable, IPersistenciaJpa
{

    public TipoDeFolioJpaController(UserTransaction utx, EntityManagerFactory emf)
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

    public void create(TipoDeFolio tipoDeFolio) throws PreexistingEntityException
    {
        if (tipoDeFolio.getFolioList() == null)
        {
            tipoDeFolio.setFolioList(new ArrayList<Folio>());
        }
        EntityManager em = null;
        try
        {
            em = getEntityManager();
            em.getTransaction().begin();
            List<Folio> attachedFolioList = new ArrayList<Folio>();
            for (Folio folioListFolioToAttach : tipoDeFolio.getFolioList())
            {
                folioListFolioToAttach = em.getReference(folioListFolioToAttach.getClass(), folioListFolioToAttach.getIdFolio());
                attachedFolioList.add(folioListFolioToAttach);
            }
            tipoDeFolio.setFolioList(attachedFolioList);
            em.persist(tipoDeFolio);
            for (Folio folioListFolio : tipoDeFolio.getFolioList())
            {
                TipoDeFolio oldFkIdTipoFolioOfFolioListFolio = folioListFolio.getFkIdTipoFolio();
                folioListFolio.setFkIdTipoFolio(tipoDeFolio);
                folioListFolio = em.merge(folioListFolio);
                if (oldFkIdTipoFolioOfFolioListFolio != null)
                {
                    oldFkIdTipoFolioOfFolioListFolio.getFolioList().remove(folioListFolio);
                    oldFkIdTipoFolioOfFolioListFolio = em.merge(oldFkIdTipoFolioOfFolioListFolio);
                }
            }

            if (this.verificarExistenciaTipoDeFolio(tipoDeFolio.getNombre()))
            {
                throw new PreexistingEntityException("El tipo de folio indicado ya existe");
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

    public boolean edit(TipoDeFolio tipoDeFolio) throws IllegalOrphanException, NonexistentEntityException, ClassModifiedException, ClassEliminatedException
    {

        Integer version = ConstantesPersistencia.VERSION_INICIAL;
        Integer oldVersion = ConstantesPersistencia.VERSION_INICIAL;
        Boolean resultado = Boolean.FALSE;

        EntityManager em = null;

        em = getEntityManager();

        TipoDeFolio persistenTipoDeFolio = em.find(TipoDeFolio.class, tipoDeFolio.getIdTipoFolio());

        if (persistenTipoDeFolio != null)
        {
            version = persistenTipoDeFolio.getVersion();
            oldVersion = tipoDeFolio.getVersion();

            if (version != oldVersion)
            {
                if (em != null)
                {
                    em.close();
                }

                throw new ClassModifiedException();
            } else
            {
                em.getTransaction().begin();

                TipoDeFolio persistentTipoDeFolio = em.find(TipoDeFolio.class, tipoDeFolio.getIdTipoFolio());
                List<Folio> folioListOld = persistentTipoDeFolio.getFolioList();
                List<Folio> folioListNew = tipoDeFolio.getFolioList();
                List<String> illegalOrphanMessages = null;
                for (Folio folioListOldFolio : folioListOld)
                {
                    if (!folioListNew.contains(folioListOldFolio))
                    {
                        if (illegalOrphanMessages == null)
                        {
                            illegalOrphanMessages = new ArrayList<String>();
                        }
                        illegalOrphanMessages.add("You must retain Folio " + folioListOldFolio + " since its fkIdTipoFolio field is not nullable.");
                    }
                }
                if (illegalOrphanMessages != null)
                {
                    throw new IllegalOrphanException(illegalOrphanMessages);
                }
                List<Folio> attachedFolioListNew = new ArrayList<Folio>();
                for (Folio folioListNewFolioToAttach : folioListNew)
                {
                    folioListNewFolioToAttach = em.getReference(folioListNewFolioToAttach.getClass(), folioListNewFolioToAttach.getIdFolio());
                    attachedFolioListNew.add(folioListNewFolioToAttach);
                }
                folioListNew = attachedFolioListNew;
                tipoDeFolio.setFolioList(folioListNew);
                tipoDeFolio = em.merge(tipoDeFolio);
                for (Folio folioListNewFolio : folioListNew)
                {
                    if (!folioListOld.contains(folioListNewFolio))
                    {
                        TipoDeFolio oldFkIdTipoFolioOfFolioListNewFolio = folioListNewFolio.getFkIdTipoFolio();
                        folioListNewFolio.setFkIdTipoFolio(tipoDeFolio);
                        folioListNewFolio = em.merge(folioListNewFolio);
                        if (oldFkIdTipoFolioOfFolioListNewFolio != null && !oldFkIdTipoFolioOfFolioListNewFolio.equals(tipoDeFolio))
                        {
                            oldFkIdTipoFolioOfFolioListNewFolio.getFolioList().remove(folioListNewFolio);
                            oldFkIdTipoFolioOfFolioListNewFolio = em.merge(oldFkIdTipoFolioOfFolioListNewFolio);
                        }
                    }
                }

                em.getTransaction().commit();

                resultado = true;
            }
        } else
        {
            throw new ClassEliminatedException("El tipo de folio con ID:" + tipoDeFolio.getIdTipoFolio() + ", ya no existe");
        }

        return resultado;
    }

    public void destroy(Integer id) throws IllegalOrphanException, NonexistentEntityException
    {
        EntityManager em = null;
        try
        {
            em = getEntityManager();
            em.getTransaction().begin();
            TipoDeFolio tipoDeFolio;
            try
            {
                tipoDeFolio = em.getReference(TipoDeFolio.class, id);
                tipoDeFolio.getIdTipoFolio();
            }
            catch (EntityNotFoundException enfe)
            {
                throw new NonexistentEntityException("The tipoDeFolio with id " + id + " no longer exists.", enfe);
            }
            List<String> illegalOrphanMessages = null;
            List<Folio> folioListOrphanCheck = tipoDeFolio.getFolioList();
            for (Folio folioListOrphanCheckFolio : folioListOrphanCheck)
            {
                if (illegalOrphanMessages == null)
                {
                    illegalOrphanMessages = new ArrayList<String>();
                }
                illegalOrphanMessages.add("This TipoDeFolio (" + tipoDeFolio + ") cannot be destroyed since the Folio " + folioListOrphanCheckFolio + " in its folioList field has a non-nullable fkIdTipoFolio field.");
            }
            if (illegalOrphanMessages != null)
            {
                throw new IllegalOrphanException(illegalOrphanMessages);
            }
            em.remove(tipoDeFolio);
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

    public boolean verificarExistenciaTipoDeFolio(String nombre)
    {

        List<TipoDeFolio> listaTipoDeFolios = this.findTipoDeFolioEntities();

        for (Iterator<TipoDeFolio> it = listaTipoDeFolios.iterator(); it.hasNext();)
        {
            TipoDeFolio tipoDeFolio = it.next();

            if (tipoDeFolio.getNombre().equals(nombre))
            {
                return true;
            }
        }
        return false;

    }

    public List<TipoDeFolio> findTipoDeFolioEntities()
    {
        return findTipoDeFolioEntities(true, -1, -1);
    }

    public List<TipoDeFolio> findTipoDeFolioEntities(int maxResults, int firstResult)
    {
        return findTipoDeFolioEntities(false, maxResults, firstResult);
    }

    private List<TipoDeFolio> findTipoDeFolioEntities(boolean all, int maxResults, int firstResult)
    {
        EntityManager em = getEntityManager();
        try
        {
            Query q = em.createQuery("select object(o) from TipoDeFolio as o");
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

    public TipoDeFolio findTipoDeFolio(Integer id)
    {
        EntityManager em = getEntityManager();
        try
        {
            return em.find(TipoDeFolio.class, id);
        }
        finally
        {
            em.close();
        }
    }

    public int getTipoDeFolioCount()
    {
        EntityManager em = getEntityManager();
        try
        {
            Query q = em.createQuery("select count(o) from TipoDeFolio as o");
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
