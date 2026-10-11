/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.licensis.notaire.jpa;

import com.licensis.notaire.jpa.exceptions.ClassEliminatedException;
import com.licensis.notaire.jpa.exceptions.ClassModifiedException;
import com.licensis.notaire.jpa.exceptions.NonexistentEntityException;
import com.licensis.notaire.jpa.JpaPersistence;
import com.licensis.notaire.business.Copy;
import com.licensis.notaire.business.Deed;
import com.licensis.notaire.business.Folio;
import com.licensis.notaire.business.Person;
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
 * @author juanca
 */
public class FolioJpaController implements Serializable, JpaPersistence
{

    public FolioJpaController(UserTransaction utx, EntityManagerFactory emf)
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

    public void create(Folio folio)
    {
        if (folio.getCopyList() == null)
        {
            folio.setCopyList(new ArrayList<Copy>());
        }
        EntityManager em = null;
        try
        {
            em = getEntityManager();
            em.getTransaction().begin();
            Person fkIdNotaryPerson = folio.getFkIdNotaryPerson();
            if (fkIdNotaryPerson != null)
            {
                fkIdNotaryPerson = em.getReference(fkIdNotaryPerson.getClass(), fkIdNotaryPerson.getPersonId());
                folio.setFkIdNotaryPerson(fkIdNotaryPerson);
            }
            FolioType fkIdFolioType = folio.getFkIdFolioType();
            if (fkIdFolioType != null)
            {
                fkIdFolioType = em.getReference(fkIdFolioType.getClass(), fkIdFolioType.getIdFolioType());
                folio.setFkIdFolioType(fkIdFolioType);
            }
            Deed fkIdDeed = folio.getFkIdDeed();
            if (fkIdDeed != null)
            {
                fkIdDeed = em.getReference(fkIdDeed.getClass(), fkIdDeed.getIdDeed());
                folio.setFkIdDeed(fkIdDeed);
            }
            List<Copy> attachedCopyList = new ArrayList<Copy>();
            for (Copy copyListCopyToAttach : folio.getCopyList())
            {
                copyListCopyToAttach = em.getReference(copyListCopyToAttach.getClass(), copyListCopyToAttach.getIdCopy());
                attachedCopyList.add(copyListCopyToAttach);
            }
            folio.setCopyList(attachedCopyList);
            em.persist(folio);
            if (fkIdNotaryPerson != null)
            {
                fkIdNotaryPerson.getFolioList().add(folio);
                fkIdNotaryPerson = em.merge(fkIdNotaryPerson);
            }
            if (fkIdFolioType != null)
            {
                fkIdFolioType.getFolioList().add(folio);
                fkIdFolioType = em.merge(fkIdFolioType);
            }
            if (fkIdDeed != null)
            {
                fkIdDeed.getFolioList().add(folio);
                fkIdDeed = em.merge(fkIdDeed);
            }
            for (Copy copyListCopy : folio.getCopyList())
            {
                copyListCopy.getFolioList().add(folio);
                copyListCopy = em.merge(copyListCopy);
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

    public void edit(Folio folio) throws NonexistentEntityException, ClassModifiedException, ClassEliminatedException
    {
        EntityManager em = null;
        Integer version = PersistenceConstants.INITIAL_VERSION;
        Integer oldVersion = PersistenceConstants.INITIAL_VERSION;

        em = getEntityManager();
        em.getTransaction().begin();
        Folio persistentFolio = em.find(Folio.class, folio.getIdFolio());

        if (persistentFolio != null)
        {
            version = persistentFolio.getVersion();
            oldVersion = folio.getVersion();

            if (version != oldVersion) // Versions differ: someone else modified the object
            {
                if (em != null)
                {
                    em.close();
                }
                throw new ClassModifiedException();
            } else
            {
                Person fkIdNotaryPersonOld = persistentFolio.getFkIdNotaryPerson();
                Person fkIdNotaryPersonNew = folio.getFkIdNotaryPerson();
                FolioType fkIdFolioTypeOld = persistentFolio.getFkIdFolioType();
                FolioType fkIdFolioTypeNew = folio.getFkIdFolioType();
                Deed fkIdDeedOld = persistentFolio.getFkIdDeed();
                Deed fkIdDeedNew = folio.getFkIdDeed();
//                List<Copy> copyListOld = persistentFolio.getCopyList();
//                List<Copy> copyListNew = folio.getCopyList();
                if (fkIdNotaryPersonNew != null)
                {
                    fkIdNotaryPersonNew = em.getReference(fkIdNotaryPersonNew.getClass(), fkIdNotaryPersonNew.getPersonId());
                    folio.setFkIdNotaryPerson(fkIdNotaryPersonNew);
                }
                if (fkIdFolioTypeNew != null)
                {
                    fkIdFolioTypeNew = em.getReference(fkIdFolioTypeNew.getClass(), fkIdFolioTypeNew.getIdFolioType());
                    folio.setFkIdFolioType(fkIdFolioTypeNew);
                }
                if (fkIdDeedNew != null)
                {
                    fkIdDeedNew = em.getReference(fkIdDeedNew.getClass(), fkIdDeedNew.getIdDeed());
                    folio.setFkIdDeed(fkIdDeedNew);
                }
//                List<Copy> attachedCopyListNew = new ArrayList<Copy>();
//                for (Copy copyListNewCopyToAttach : copyListNew)
//                {
//                    copyListNewCopyToAttach = em.getReference(copyListNewCopyToAttach.getClass(), copyListNewCopyToAttach.getIdCopy());
//                    attachedCopyListNew.add(copyListNewCopyToAttach);
//                }
//                copyListNew = attachedCopyListNew;
//                folio.setCopyList(copyListNew);
                folio = em.merge(folio);
//                if (fkIdNotaryPersonOld != null && !fkIdNotaryPersonOld.equals(fkIdNotaryPersonNew))
//                {
//                    fkIdNotaryPersonOld.getFolioList().remove(folio);
//                    fkIdNotaryPersonOld = em.merge(fkIdNotaryPersonOld);
//                }
//                if (fkIdNotaryPersonNew != null && !fkIdNotaryPersonNew.equals(fkIdNotaryPersonOld))
//                {
//                    fkIdNotaryPersonNew.getFolioList().add(folio);
//                    fkIdNotaryPersonNew = em.merge(fkIdNotaryPersonNew);
//                }
//                if (fkIdFolioTypeOld != null && !fkIdFolioTypeOld.equals(fkIdFolioTypeNew))
//                {
//                    fkIdFolioTypeOld.getFolioList().remove(folio);
//                    fkIdFolioTypeOld = em.merge(fkIdFolioTypeOld);
//                }
//                if (fkIdFolioTypeNew != null && !fkIdFolioTypeNew.equals(fkIdFolioTypeOld))
//                {
//                    fkIdFolioTypeNew.getFolioList().add(folio);
//                    fkIdFolioTypeNew = em.merge(fkIdFolioTypeNew);
//                }
//                if (fkIdDeedOld != null && !fkIdDeedOld.equals(fkIdDeedNew))
//                {
//                    fkIdDeedOld.getFolioList().remove(folio);
//                    fkIdDeedOld = em.merge(fkIdDeedOld);
//                }
//                if (fkIdDeedNew != null && !fkIdDeedNew.equals(fkIdDeedOld))
//                {
//                    fkIdDeedNew.getFolioList().add(folio);
//                    fkIdDeedNew = em.merge(fkIdDeedNew);
//                }
//                for (Copy copyListOldCopy : copyListOld)
//                {
//                    if (!copyListNew.contains(copyListOldCopy))
//                    {
//                        copyListOldCopy.getFolioList().remove(folio);
//                        copyListOldCopy = em.merge(copyListOldCopy);
//                    }
//                }
//                for (Copy copyListNewCopy : copyListNew)
//                {
//                    if (!copyListOld.contains(copyListNewCopy))
//                    {
//                        copyListNewCopy.getFolioList().add(folio);
//                        copyListNewCopy = em.merge(copyListNewCopy);
//                    }
//                }
                em.getTransaction().commit();

                if (em != null)
                {
                    em.close();
                }
            }
        } else
        {
            throw new ClassEliminatedException();
        }
    }

    public boolean updateFolioFull(Folio folioModificado) throws ClassModifiedException
    {
        boolean result = false;

        EntityManager em = null;
        Integer version = PersistenceConstants.INITIAL_VERSION;
        Integer oldVersion = PersistenceConstants.INITIAL_VERSION;

        em = getEntityManager();
        em.getTransaction().begin();
        Folio persistentFolio = em.find(Folio.class, folioModificado.getIdFolio());

        if (persistentFolio != null)
        {
            version = persistentFolio.getVersion();
            oldVersion = folioModificado.getVersion();

            if (version != oldVersion) // Versions differ: someone else modified the object
            {
                if (em != null)
                {
                    em.close();
                }
                throw new ClassModifiedException();
            } else
            {
                persistentFolio.setYear(folioModificado.getYear());
                persistentFolio.setStatus(folioModificado.getStatus());
//                persistentFolio.setFkIdPersonaEscribano(folioModificado.getFkIdPersonaEscribano());
//                persistentFolio.setFkIdTipoFolio(folioModificado.getFkIdTipoFolio());
                persistentFolio.setNumber(folioModificado.getNumber());
                persistentFolio.setNotes(folioModificado.getNotes());

                em.getTransaction().commit();

                if (em != null)
                {
                    em.close();
                }

                result = true;
            }
        }

        return result;
    }

    public void destroy(Integer id) throws NonexistentEntityException
    {
        EntityManager em = null;
        try
        {
            em = getEntityManager();
            em.getTransaction().begin();
            Folio folio;

            try
            {
                folio = em.getReference(Folio.class, id);
                folio.getIdFolio();
            }
            catch (EntityNotFoundException enfe)
            {
                throw new NonexistentEntityException("The folio with id " + id + " no longer exists.", enfe);
            }
            Person fkIdNotaryPerson = folio.getFkIdNotaryPerson();
            if (fkIdNotaryPerson != null)
            {
                fkIdNotaryPerson.getFolioList().remove(folio);
                fkIdNotaryPerson = em.merge(fkIdNotaryPerson);
            }
            FolioType fkIdFolioType = folio.getFkIdFolioType();
            if (fkIdFolioType != null)
            {
                fkIdFolioType.getFolioList().remove(folio);
                fkIdFolioType = em.merge(fkIdFolioType);
            }
            Deed fkIdDeed = folio.getFkIdDeed();
            if (fkIdDeed != null)
            {
                fkIdDeed.getFolioList().remove(folio);
                fkIdDeed = em.merge(fkIdDeed);
            }
            List<Copy> copyList = folio.getCopyList();
            for (Copy copyListCopy : copyList)
            {
                copyListCopy.getFolioList().remove(folio);
                copyListCopy = em.merge(copyListCopy);
            }
            em.remove(folio);
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

    public List<Folio> findFolioEntities()
    {
        return findFolioEntities(true, -1, -1);
    }

    public List<Folio> findFoliosByRecordYear(Integer record, Integer year)
    {
        EntityManager em = getEntityManager();
        List<Folio> miListaFolios = null;
        try
        {
            Query q = em.createNamedQuery("Folio.findByYearAndRegistration");
            q.setParameter("year", year);
            q.setParameter("registration", record);
            miListaFolios = q.getResultList();

            for (Iterator<Folio> it = miListaFolios.iterator(); it.hasNext();)
            {
                Folio folio = it.next();
                folio.setFkIdNotaryPerson(null);
            }

            return miListaFolios;
        }
        finally
        {
            em.close();
        }

    }

    public List<Folio> findFoliosByNumber(Integer number)
    {
        EntityManager em = getEntityManager();
        List<Folio> miListaFolios = null;
        try
        {
            Query q = em.createNamedQuery("Folio.findByNumber");
            q.setParameter("number", number);

            miListaFolios = q.getResultList();

            for (Iterator<Folio> it = miListaFolios.iterator(); it.hasNext();)
            {
                Folio folio = it.next();
                folio.setFkIdNotaryPerson(null);
            }

            return miListaFolios;
        }
        finally
        {
            em.close();
        }

    }

    public List<Folio> findFolioEntities(int maxResults, int firstResult)
    {
        return findFolioEntities(false, maxResults, firstResult);
    }

    private List<Folio> findFolioEntities(boolean all, int maxResults, int firstResult)
    {
        EntityManager em = getEntityManager();
        try
        {
            Query q = em.createQuery("select object(o) from Folio as o");
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

    public Folio findFolio(Integer id)
    {
        EntityManager em = getEntityManager();

        try
        {
            Folio folio = em.find(Folio.class, id);

            folio.setCopyList(new ArrayList<Copy>());

            return folio;
        }
        finally
        {
            em.close();
        }
    }

    public int getFolioCount()
    {
        EntityManager em = getEntityManager();
        try
        {
            Query q = em.createQuery("select count(o) from Folio as o");
            return ((Long) q.getSingleResult()).intValue();
        }
        finally
        {
            em.close();
        }
    }

    public void updateFolio(Folio miFolio) throws ClassModifiedException, ClassEliminatedException
    {
        EntityManager em = null;

        int version;
        int oldVersion;

        em = getEntityManager();
        em.getTransaction().begin();
        Folio persistentFolio = em.find(Folio.class, miFolio.getIdFolio());

        if (persistentFolio != null)
        {
            version = persistentFolio.getVersion(); // Version of the object in the database
            oldVersion = miFolio.getVersion(); // Version of the object in memory

            if (version != oldVersion) // Versions differ: someone else modified the object
            {
                throw new ClassModifiedException();

            }

            persistentFolio.setStatus(miFolio.getStatus());

            em.getTransaction().commit();

            em.close();
        } else
        {
            throw new ClassEliminatedException();
        }
    }

    @Override
    public String getNameJpa()
    {
        return this.getClass().getName();
    }
}
