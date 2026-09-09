/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.licensis.notaire.jpa;

import com.licensis.notaire.jpa.exceptions.ClassEliminatedException;
import com.licensis.notaire.jpa.exceptions.ClassModifiedException;
import com.licensis.notaire.jpa.exceptions.NonexistentEntityException;
import com.licensis.notaire.jpa.interfaz.IPersistenciaJpa;
import com.licensis.notaire.business.Copy;
import com.licensis.notaire.business.Folio;
import com.licensis.notaire.business.Person;
import com.licensis.notaire.business.Testimony;
import java.io.Serializable;
import java.util.ArrayList;
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
public class CopyJpaController implements Serializable, IPersistenciaJpa
{

    public CopyJpaController(UserTransaction utx, EntityManagerFactory emf)
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

    public int create(Copy copy)
    {
        int creada = -1;
        if (copy.getFolioList() == null)
        {
            copy.setFolioList(new ArrayList<Folio>());
        }
        EntityManager em = null;
        try
        {
            em = getEntityManager();
            em.getTransaction().begin();
            Person fkIdPerson = copy.getFkIdPerson();
            if (fkIdPerson != null)
            {
                fkIdPerson = em.getReference(fkIdPerson.getClass(), fkIdPerson.getPersonId());
                copy.setFkIdPerson(fkIdPerson);
            }
            Testimony fkIdTestimony = copy.getFkIdTestimony();
            if (fkIdTestimony != null)
            {
                fkIdTestimony = em.getReference(fkIdTestimony.getClass(), fkIdTestimony.getIdTestimony());
                copy.setFkIdTestimony(fkIdTestimony);
            }
            List<Folio> attachedFolioList = new ArrayList<Folio>();
            for (Folio folioListFolioToAttach : copy.getFolioList())
            {
                folioListFolioToAttach = em.getReference(folioListFolioToAttach.getClass(), folioListFolioToAttach.getIdFolio());
                attachedFolioList.add(folioListFolioToAttach);
            }
            copy.setFolioList(attachedFolioList);
            em.persist(copy);
            if (fkIdPerson != null)
            {
                fkIdPerson.getCopyList().add(copy);
                fkIdPerson = em.merge(fkIdPerson);
            }
            if (fkIdTestimony != null)
            {
                fkIdTestimony.getCopyList().add(copy);
                fkIdTestimony = em.merge(fkIdTestimony);
            }
            for (Folio folioListFolio : copy.getFolioList())
            {
                folioListFolio.getCopyList().add(copy);
                folioListFolio = em.merge(folioListFolio);
            }
            em.getTransaction().commit();
            creada = copy.getIdCopy();
        }
        finally
        {
            if (em != null)
            {
                em.close();
            }
        }
        return creada;
    }

    public void edit(Copy copy) throws NonexistentEntityException, Exception
    {
        EntityManager em = null;
        try
        {
            em = getEntityManager();
            em.getTransaction().begin();
            Copy persistentCopy = em.find(Copy.class, copy.getIdCopy());
            Person fkIdPersonOld = persistentCopy.getFkIdPerson();
            Person fkIdPersonNew = copy.getFkIdPerson();
            Testimony fkIdTestimonyOld = persistentCopy.getFkIdTestimony();
            Testimony fkIdTestimonyNew = copy.getFkIdTestimony();
            List<Folio> folioListOld = persistentCopy.getFolioList();
            List<Folio> folioListNew = copy.getFolioList();
            if (fkIdPersonNew != null)
            {
                fkIdPersonNew = em.getReference(fkIdPersonNew.getClass(), fkIdPersonNew.getPersonId());
                copy.setFkIdPerson(fkIdPersonNew);
            }
            if (fkIdTestimonyNew != null)
            {
                fkIdTestimonyNew = em.getReference(fkIdTestimonyNew.getClass(), fkIdTestimonyNew.getIdTestimony());
                copy.setFkIdTestimony(fkIdTestimonyNew);
            }
            List<Folio> attachedFolioListNew = new ArrayList<Folio>();
            for (Folio folioListNewFolioToAttach : folioListNew)
            {
                folioListNewFolioToAttach = em.getReference(folioListNewFolioToAttach.getClass(), folioListNewFolioToAttach.getIdFolio());
                attachedFolioListNew.add(folioListNewFolioToAttach);
            }
            folioListNew = attachedFolioListNew;
            copy.setFolioList(folioListNew);
            copy = em.merge(copy);
            if (fkIdPersonOld != null && !fkIdPersonOld.equals(fkIdPersonNew))
            {
                fkIdPersonOld.getCopyList().remove(copy);
                fkIdPersonOld = em.merge(fkIdPersonOld);
            }
            if (fkIdPersonNew != null && !fkIdPersonNew.equals(fkIdPersonOld))
            {
                fkIdPersonNew.getCopyList().add(copy);
                fkIdPersonNew = em.merge(fkIdPersonNew);
            }
            if (fkIdTestimonyOld != null && !fkIdTestimonyOld.equals(fkIdTestimonyNew))
            {
                fkIdTestimonyOld.getCopyList().remove(copy);
                fkIdTestimonyOld = em.merge(fkIdTestimonyOld);
            }
            if (fkIdTestimonyNew != null && !fkIdTestimonyNew.equals(fkIdTestimonyOld))
            {
                fkIdTestimonyNew.getCopyList().add(copy);
                fkIdTestimonyNew = em.merge(fkIdTestimonyNew);
            }
            for (Folio folioListOldFolio : folioListOld)
            {
                if (!folioListNew.contains(folioListOldFolio))
                {
                    folioListOldFolio.getCopyList().remove(copy);
                    folioListOldFolio = em.merge(folioListOldFolio);
                }
            }
            for (Folio folioListNewFolio : folioListNew)
            {
                if (!folioListOld.contains(folioListNewFolio))
                {
                    folioListNewFolio.getCopyList().add(copy);
                    folioListNewFolio = em.merge(folioListNewFolio);
                }
            }
            em.getTransaction().commit();
        }
        catch (Exception ex)
        {
            String msg = ex.getLocalizedMessage();
            if (msg == null || msg.length() == 0)
            {
                Integer id = copy.getIdCopy();
                if (findCopy(id) == null)
                {
                    throw new NonexistentEntityException("The copia with id " + id + " no longer exists.");
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
            Copy copy;
            try
            {
                copy = em.getReference(Copy.class, id);
                copy.getIdCopy();
            }
            catch (EntityNotFoundException enfe)
            {
                throw new NonexistentEntityException("The copia with id " + id + " no longer exists.", enfe);
            }
            Person fkIdPerson = copy.getFkIdPerson();
            if (fkIdPerson != null)
            {
                fkIdPerson.getCopyList().remove(copy);
                fkIdPerson = em.merge(fkIdPerson);
            }
            Testimony fkIdTestimony = copy.getFkIdTestimony();
            if (fkIdTestimony != null)
            {
                fkIdTestimony.getCopyList().remove(copy);
                fkIdTestimony = em.merge(fkIdTestimony);
            }
            List<Folio> folioList = copy.getFolioList();
            for (Folio folioListFolio : folioList)
            {
                folioListFolio.getCopyList().remove(copy);
                folioListFolio = em.merge(folioListFolio);
            }
            em.remove(copy);
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

    public List<Copy> findCopyEntities()
    {
        return findCopyEntities(true, -1, -1);
    }

    public List<Copy> findCopyEntities(int maxResults, int firstResult)
    {
        return findCopyEntities(false, maxResults, firstResult);
    }

    private List<Copy> findCopyEntities(boolean all, int maxResults, int firstResult)
    {
        EntityManager em = getEntityManager();
        try
        {
            Query q = em.createQuery("select object(o) from Copia as o");
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

    public Copy findCopy(Integer id)
    {
        EntityManager em = getEntityManager();
        try
        {
            return em.find(Copy.class, id);
        }
        finally
        {
            em.close();
        }
    }

    public int getCopyCount()
    {
        EntityManager em = getEntityManager();
        try
        {
            Query q = em.createQuery("select count(o) from Copia as o");
            return ((Long) q.getSingleResult()).intValue();
        }
        finally
        {
            em.close();
        }
    }

    public int crearCopy(Copy miCopy)
    {
        int creada = -1;

        EntityManager em = null;
        try
        {
            em = getEntityManager();
            em.getTransaction().begin();

            em.persist(miCopy);

            em.getTransaction().commit();
            creada = miCopy.getIdCopy();
        }
        finally
        {
            if (em != null)
            {
                em.close();
            }
        }
        return creada;
    }

    public List<Copy> searchCopiesTestimony(Integer idTestimony)
    {
        List<Copy> copies = null;
        EntityManager em = getEntityManager();

        Query query = em.createNamedQuery("Copia.findByTestimonio");
        query.setParameter("idTestimonio", idTestimony);

        copies = (List<Copy>) query.getResultList();

        return copies;
    }

    public Boolean modificarCopy(Copy copy) throws ClassModifiedException, ClassEliminatedException
    {
        EntityManager em = null;
        Boolean modificada = false;
        int version = 0;
        int oldVersion = 0;

        em = getEntityManager();
        em.getTransaction().begin();
        Copy persistentCopy = em.find(Copy.class, copy.getIdCopy());

        if (persistentCopy != null)
        {
            version = persistentCopy.getVersion(); // Version del Objeto en db
            oldVersion = copy.getVersion(); // Version del Objeto en memoria

            if (version != oldVersion) //Si son distintas "Alguien modifico el objeto"
            {
                throw new ClassModifiedException();
            }

            persistentCopy.setDateWithdrawal(copy.getDateWithdrawal());
            persistentCopy.setNotes(copy.getNotes());

            em.getTransaction().commit();
            modificada = true;
        } else
        {
            throw new ClassEliminatedException();
        }
        if (em != null)
        {
            em.close();
        }

        return modificada;
    }

    @Override
    public String getNameJpa()
    {
        return this.getClass().getName();
    }
}
