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
import com.licensis.notaire.business.Folio;
import com.licensis.notaire.business.FolioType;
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
 * @author matias
 */
public class FolioTypeJpaController implements Serializable, IPersistenciaJpa
{

    public FolioTypeJpaController(UserTransaction utx, EntityManagerFactory emf)
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

    public void create(FolioType folioType) throws PreexistingEntityException
    {
        if (folioType.getFolioList() == null)
        {
            folioType.setFolioList(new ArrayList<Folio>());
        }
        EntityManager em = null;
        try
        {
            em = getEntityManager();
            em.getTransaction().begin();
            List<Folio> attachedFolioList = new ArrayList<Folio>();
            for (Folio folioListFolioToAttach : folioType.getFolioList())
            {
                folioListFolioToAttach = em.getReference(folioListFolioToAttach.getClass(), folioListFolioToAttach.getIdFolio());
                attachedFolioList.add(folioListFolioToAttach);
            }
            folioType.setFolioList(attachedFolioList);
            em.persist(folioType);
            for (Folio folioListFolio : folioType.getFolioList())
            {
                FolioType oldFkIdFolioTypeOfFolioListFolio = folioListFolio.getFkIdFolioType();
                folioListFolio.setFkIdFolioType(folioType);
                folioListFolio = em.merge(folioListFolio);
                if (oldFkIdFolioTypeOfFolioListFolio != null)
                {
                    oldFkIdFolioTypeOfFolioListFolio.getFolioList().remove(folioListFolio);
                    oldFkIdFolioTypeOfFolioListFolio = em.merge(oldFkIdFolioTypeOfFolioListFolio);
                }
            }

            if (this.verificarExistenciaFolioType(folioType.getName()))
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

    public boolean edit(FolioType folioType) throws IllegalOrphanException, NonexistentEntityException, ClassModifiedException, ClassEliminatedException
    {

        Integer version = ConstantesPersistencia.VersionINICIAL;
        Integer oldVersion = ConstantesPersistencia.VersionINICIAL;
        Boolean resultado = Boolean.FALSE;

        EntityManager em = null;

        em = getEntityManager();

        FolioType persistenFolioType = em.find(FolioType.class, folioType.getIdFolioType());

        if (persistenFolioType != null)
        {
            version = persistenFolioType.getVersion();
            oldVersion = folioType.getVersion();

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

                FolioType persistentFolioType = em.find(FolioType.class, folioType.getIdFolioType());
                List<Folio> folioListOld = persistentFolioType.getFolioList();
                List<Folio> folioListNew = folioType.getFolioList();
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
                folioType.setFolioList(folioListNew);
                folioType = em.merge(folioType);
                for (Folio folioListNewFolio : folioListNew)
                {
                    if (!folioListOld.contains(folioListNewFolio))
                    {
                        FolioType oldFkIdFolioTypeOfFolioListNewFolio = folioListNewFolio.getFkIdFolioType();
                        folioListNewFolio.setFkIdFolioType(folioType);
                        folioListNewFolio = em.merge(folioListNewFolio);
                        if (oldFkIdFolioTypeOfFolioListNewFolio != null && !oldFkIdFolioTypeOfFolioListNewFolio.equals(folioType))
                        {
                            oldFkIdFolioTypeOfFolioListNewFolio.getFolioList().remove(folioListNewFolio);
                            oldFkIdFolioTypeOfFolioListNewFolio = em.merge(oldFkIdFolioTypeOfFolioListNewFolio);
                        }
                    }
                }

                em.getTransaction().commit();

                resultado = true;
            }
        } else
        {
            throw new ClassEliminatedException("El tipo de folio con ID:" + folioType.getIdFolioType() + ", ya no existe");
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
            FolioType folioType;
            try
            {
                folioType = em.getReference(FolioType.class, id);
                folioType.getIdFolioType();
            }
            catch (EntityNotFoundException enfe)
            {
                throw new NonexistentEntityException("The tipoDeFolio with id " + id + " no longer exists.", enfe);
            }
            List<String> illegalOrphanMessages = null;
            List<Folio> folioListOrphanCheck = folioType.getFolioList();
            for (Folio folioListOrphanCheckFolio : folioListOrphanCheck)
            {
                if (illegalOrphanMessages == null)
                {
                    illegalOrphanMessages = new ArrayList<String>();
                }
                illegalOrphanMessages.add("This TipoDeFolio (" + folioType + ") cannot be destroyed since the Folio " + folioListOrphanCheckFolio + " in its folioList field has a non-nullable fkIdTipoFolio field.");
            }
            if (illegalOrphanMessages != null)
            {
                throw new IllegalOrphanException(illegalOrphanMessages);
            }
            em.remove(folioType);
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

    public boolean verificarExistenciaFolioType(String name)
    {

        List<FolioType> listaTypeDeFolios = this.findFolioTypeEntities();

        for (Iterator<FolioType> it = listaTypeDeFolios.iterator(); it.hasNext();)
        {
            FolioType folioType = it.next();

            if (folioType.getName().equals(name))
            {
                return true;
            }
        }
        return false;

    }

    public List<FolioType> findFolioTypeEntities()
    {
        return findFolioTypeEntities(true, -1, -1);
    }

    public List<FolioType> findFolioTypeEntities(int maxResults, int firstResult)
    {
        return findFolioTypeEntities(false, maxResults, firstResult);
    }

    private List<FolioType> findFolioTypeEntities(boolean all, int maxResults, int firstResult)
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

    public FolioType findFolioType(Integer id)
    {
        EntityManager em = getEntityManager();
        try
        {
            return em.find(FolioType.class, id);
        }
        finally
        {
            em.close();
        }
    }

    public int getFolioTypeCount()
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
    public String getNameJpa()
    {
        return this.getClass().getName();
    }
}
