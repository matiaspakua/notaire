/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.licensis.notaire.jpa;

import com.licensis.notaire.jpa.exceptions.ClassEliminatedException;
import com.licensis.notaire.jpa.exceptions.ClassModifiedException;
import com.licensis.notaire.jpa.exceptions.IllegalOrphanException;
import com.licensis.notaire.jpa.exceptions.NonexistentEntityException;
import com.licensis.notaire.jpa.interfaz.IPersistenciaJpa;
import com.licensis.notaire.business.Deed;
import com.licensis.notaire.business.Folio;
import com.licensis.notaire.business.Testimony;
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
public class DeedJpaController implements Serializable, IPersistenciaJpa
{

    public DeedJpaController(UserTransaction utx, EntityManagerFactory emf)
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

    public Boolean create(Deed deed)
    {
        Boolean creada = false;

        if (deed.getFolioList() == null)
        {
            deed.setFolioList(new ArrayList<Folio>());
        }
        if (deed.getProcedureList() == null)
        {
            deed.setProcedureList(new ArrayList<Procedure>());
        }
        if (deed.getTestimonyList() == null)
        {
            deed.setTestimonyList(new ArrayList<Testimony>());
        }
        EntityManager em = null;
        try
        {
            em = getEntityManager();
            em.getTransaction().begin();
            List<Folio> attachedFolioList = new ArrayList<Folio>();
            for (Folio folioListFolioToAttach : deed.getFolioList())
            {
                folioListFolioToAttach = em.getReference(folioListFolioToAttach.getClass(), folioListFolioToAttach.getIdFolio());
                attachedFolioList.add(folioListFolioToAttach);
            }
            deed.setFolioList(attachedFolioList);
            List<Procedure> attachedProcedureList = new ArrayList<Procedure>();
            for (Procedure procedureListProcedureToAttach : deed.getProcedureList())
            {
                procedureListProcedureToAttach = em.getReference(procedureListProcedureToAttach.getClass(), procedureListProcedureToAttach.getIdProcedure());
                attachedProcedureList.add(procedureListProcedureToAttach);
            }
            deed.setProcedureList(attachedProcedureList);
            List<Testimony> attachedTestimonyList = new ArrayList<Testimony>();
            for (Testimony testimonyListTestimonyToAttach : deed.getTestimonyList())
            {
                testimonyListTestimonyToAttach = em.getReference(testimonyListTestimonyToAttach.getClass(), testimonyListTestimonyToAttach.getIdTestimony());
                attachedTestimonyList.add(testimonyListTestimonyToAttach);
            }
            deed.setTestimonyList(attachedTestimonyList);
            em.persist(deed);
            for (Folio folioListFolio : deed.getFolioList())
            {
                Deed oldFkIdDeedOfFolioListFolio = folioListFolio.getFkIdDeed();
                folioListFolio.setFkIdDeed(deed);
                folioListFolio = em.merge(folioListFolio);
                if (oldFkIdDeedOfFolioListFolio != null)
                {
                    oldFkIdDeedOfFolioListFolio.getFolioList().remove(folioListFolio);
                    oldFkIdDeedOfFolioListFolio = em.merge(oldFkIdDeedOfFolioListFolio);
                }
            }
            for (Procedure procedureListProcedure : deed.getProcedureList())
            {
                Deed oldFkIdDeedOfProcedureListProcedure = procedureListProcedure.getFkIdDeed();
                procedureListProcedure.setFkIdDeed(deed);
                procedureListProcedure = em.merge(procedureListProcedure);
                if (oldFkIdDeedOfProcedureListProcedure != null)
                {
                    oldFkIdDeedOfProcedureListProcedure.getProcedureList().remove(procedureListProcedure);
                    oldFkIdDeedOfProcedureListProcedure = em.merge(oldFkIdDeedOfProcedureListProcedure);
                }
            }
            for (Testimony testimonyListTestimony : deed.getTestimonyList())
            {
                Deed oldFkIdDeedOfTestimonyListTestimony = testimonyListTestimony.getFkIdDeed();
                testimonyListTestimony.setFkIdDeed(deed);
                testimonyListTestimony = em.merge(testimonyListTestimony);
                if (oldFkIdDeedOfTestimonyListTestimony != null)
                {
                    oldFkIdDeedOfTestimonyListTestimony.getTestimonyList().remove(testimonyListTestimony);
                    oldFkIdDeedOfTestimonyListTestimony = em.merge(oldFkIdDeedOfTestimonyListTestimony);
                }
            }
            em.getTransaction().commit();
            creada = true;
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

    public Boolean edit(Deed deed) throws IllegalOrphanException, NonexistentEntityException, ClassModifiedException, ClassEliminatedException
    {
        EntityManager em = null;
        Boolean modificada = false;

        int version;
        int oldVersion;

        em = getEntityManager();
        em.getTransaction().begin();
        Deed persistentDeed = em.find(Deed.class, deed.getIdDeed());

        if (persistentDeed != null)
        {
            version = persistentDeed.getVersion(); // Version del Objeto en db
            oldVersion = deed.getVersion(); // Version del Objeto en memoria

            if (version != oldVersion) //Si son distintas "Alguien modifico el objeto"
            {
                throw new ClassModifiedException();

            }

            List<Folio> folioListOld = persistentDeed.getFolioList();
            List<Folio> folioListNew = deed.getFolioList();
            List<Procedure> procedureListOld = persistentDeed.getProcedureList();
            List<Procedure> procedureListNew = deed.getProcedureList();
            List<Testimony> testimonyListOld = persistentDeed.getTestimonyList();
            List<Testimony> testimonyListNew = deed.getTestimonyList();
            List<String> illegalOrphanMessages = null;
            for (Testimony testimonyListOldTestimony : testimonyListOld)
            {
                if (!testimonyListNew.contains(testimonyListOldTestimony))
                {
                    if (illegalOrphanMessages == null)
                    {
                        illegalOrphanMessages = new ArrayList<String>();
                    }
                    illegalOrphanMessages.add("You must retain Testimonio " + testimonyListOldTestimony + " since its fkIdEscritura field is not nullable.");
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
            deed.setFolioList(folioListNew);
            List<Procedure> attachedProcedureListNew = new ArrayList<Procedure>();
            for (Procedure procedureListNewProcedureToAttach : procedureListNew)
            {
                procedureListNewProcedureToAttach = em.getReference(procedureListNewProcedureToAttach.getClass(), procedureListNewProcedureToAttach.getIdProcedure());
                attachedProcedureListNew.add(procedureListNewProcedureToAttach);
            }
            procedureListNew = attachedProcedureListNew;
            deed.setProcedureList(procedureListNew);
            List<Testimony> attachedTestimonyListNew = new ArrayList<Testimony>();
            for (Testimony testimonyListNewTestimonyToAttach : testimonyListNew)
            {
                testimonyListNewTestimonyToAttach = em.getReference(testimonyListNewTestimonyToAttach.getClass(), testimonyListNewTestimonyToAttach.getIdTestimony());
                attachedTestimonyListNew.add(testimonyListNewTestimonyToAttach);
            }
            testimonyListNew = attachedTestimonyListNew;
            deed.setTestimonyList(testimonyListNew);
            deed = em.merge(deed);
            for (Folio folioListOldFolio : folioListOld)
            {
                if (!folioListNew.contains(folioListOldFolio))
                {
                    folioListOldFolio.setFkIdDeed(null);
                    folioListOldFolio = em.merge(folioListOldFolio);
                }
            }
            for (Folio folioListNewFolio : folioListNew)
            {
                if (!folioListOld.contains(folioListNewFolio))
                {
                    Deed oldFkIdDeedOfFolioListNewFolio = folioListNewFolio.getFkIdDeed();
                    folioListNewFolio.setFkIdDeed(deed);
                    folioListNewFolio = em.merge(folioListNewFolio);
                    if (oldFkIdDeedOfFolioListNewFolio != null && !oldFkIdDeedOfFolioListNewFolio.equals(deed))
                    {
                        oldFkIdDeedOfFolioListNewFolio.getFolioList().remove(folioListNewFolio);
                        oldFkIdDeedOfFolioListNewFolio = em.merge(oldFkIdDeedOfFolioListNewFolio);
                    }
                }
            }
            for (Procedure procedureListOldProcedure : procedureListOld)
            {
                if (!procedureListNew.contains(procedureListOldProcedure))
                {
                    procedureListOldProcedure.setFkIdDeed(null);
                    procedureListOldProcedure = em.merge(procedureListOldProcedure);
                }
            }
            for (Procedure procedureListNewProcedure : procedureListNew)
            {
                if (!procedureListOld.contains(procedureListNewProcedure))
                {
                    Deed oldFkIdDeedOfProcedureListNewProcedure = procedureListNewProcedure.getFkIdDeed();
                    procedureListNewProcedure.setFkIdDeed(deed);
                    procedureListNewProcedure = em.merge(procedureListNewProcedure);
                    if (oldFkIdDeedOfProcedureListNewProcedure != null && !oldFkIdDeedOfProcedureListNewProcedure.equals(deed))
                    {
                        oldFkIdDeedOfProcedureListNewProcedure.getProcedureList().remove(procedureListNewProcedure);
                        oldFkIdDeedOfProcedureListNewProcedure = em.merge(oldFkIdDeedOfProcedureListNewProcedure);
                    }
                }
            }
            for (Testimony testimonyListNewTestimony : testimonyListNew)
            {
                if (!testimonyListOld.contains(testimonyListNewTestimony))
                {
                    Deed oldFkIdDeedOfTestimonyListNewTestimony = testimonyListNewTestimony.getFkIdDeed();
                    testimonyListNewTestimony.setFkIdDeed(deed);
                    testimonyListNewTestimony = em.merge(testimonyListNewTestimony);
                    if (oldFkIdDeedOfTestimonyListNewTestimony != null && !oldFkIdDeedOfTestimonyListNewTestimony.equals(deed))
                    {
                        oldFkIdDeedOfTestimonyListNewTestimony.getTestimonyList().remove(testimonyListNewTestimony);
                        oldFkIdDeedOfTestimonyListNewTestimony = em.merge(oldFkIdDeedOfTestimonyListNewTestimony);
                    }
                }
            }
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

    public void destroy(Integer id) throws IllegalOrphanException, NonexistentEntityException
    {
        EntityManager em = null;
        try
        {
            em = getEntityManager();
            em.getTransaction().begin();
            Deed deed;
            try
            {
                deed = em.getReference(Deed.class, id);
                deed.getIdDeed();
            }
            catch (EntityNotFoundException enfe)
            {
                throw new NonexistentEntityException("The escritura with id " + id + " no longer exists.", enfe);
            }
            List<String> illegalOrphanMessages = null;
            List<Testimony> testimonyListOrphanCheck = deed.getTestimonyList();
            for (Testimony testimonyListOrphanCheckTestimony : testimonyListOrphanCheck)
            {
                if (illegalOrphanMessages == null)
                {
                    illegalOrphanMessages = new ArrayList<String>();
                }
                illegalOrphanMessages.add("This Escritura (" + deed + ") cannot be destroyed since the Testimonio " + testimonyListOrphanCheckTestimony + " in its testimonioList field has a non-nullable fkIdEscritura field.");
            }
            if (illegalOrphanMessages != null)
            {
                throw new IllegalOrphanException(illegalOrphanMessages);
            }
            List<Folio> folioList = deed.getFolioList();
            for (Folio folioListFolio : folioList)
            {
                folioListFolio.setFkIdDeed(null);
                folioListFolio = em.merge(folioListFolio);
            }
            List<Procedure> procedureList = deed.getProcedureList();
            for (Procedure procedureListProcedure : procedureList)
            {
                procedureListProcedure.setFkIdDeed(null);
                procedureListProcedure = em.merge(procedureListProcedure);
            }
            em.remove(deed);
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

    public List<Deed> findDeedEntities()
    {
        return findDeedEntities(true, -1, -1);
    }

    public List<Deed> findDeedEntities(int maxResults, int firstResult)
    {
        return findDeedEntities(false, maxResults, firstResult);
    }

    private List<Deed> findDeedEntities(boolean all, int maxResults, int firstResult)
    {
        EntityManager em = getEntityManager();
        try
        {
            Query q = em.createQuery("select object(o) from Deed as o");
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

    public List<Deed> findDeedByNumber(Integer number)
    {
        EntityManager em = getEntityManager();
        List<Deed> escrituras = null;

        Query query = em.createNamedQuery("Escritura.findByNumero");
        query.setParameter("numero", number);

        escrituras = query.getResultList();

        return escrituras;
    }

    public Deed findDeedById(Integer id)
    {
        EntityManager em = getEntityManager();
        Deed deed = null;

        Query query = em.createNamedQuery("Escritura.findByIdEscritura");
        query.setParameter("idEscritura", id);

        deed = (Deed) query.getResultList().get(0);

        return deed;
    }

    public Deed findDeed(Integer id)
    {
        EntityManager em = getEntityManager();
        try
        {
            return em.find(Deed.class, id);
        }
        finally
        {
            em.close();
        }
    }

    public int getDeedCount()
    {
        EntityManager em = getEntityManager();
        try
        {
            Query q = em.createQuery("select count(o) from Deed as o");
            return ((Long) q.getSingleResult()).intValue();
        }
        finally
        {
            em.close();
        }
    }

    public Boolean modificarDeed(Deed miDeed) throws ClassModifiedException, ClassEliminatedException
    {
        EntityManager em = null;
        Boolean modificada = false;

        int version;
        int oldVersion;

        em = getEntityManager();
        em.getTransaction().begin();
        Deed persistentDeed = em.find(Deed.class, miDeed.getIdDeed());

        if (persistentDeed != null)
        {
            version = persistentDeed.getVersion(); // Version del Objeto en db
            oldVersion = miDeed.getVersion(); // Version del Objeto en memoria

            if (version != oldVersion) //Si son distintas "Alguien modifico el objeto"
            {
                throw new ClassModifiedException();

            }

            persistentDeed.setBody(miDeed.getBody());
            persistentDeed.setStatus(miDeed.getStatus());
            persistentDeed.setDateDeedrecording(miDeed.getDateDeedrecording());
            persistentDeed.setDateRegistration(miDeed.getDateRegistration());

            persistentDeed.setFolioList(new ArrayList<Folio>());
            for (Iterator<Folio> it = miDeed.getFolioList().iterator(); it.hasNext();)
            {
                Folio folio = it.next();
                persistentDeed.getFolioList().add(folio);
            }

            persistentDeed.setRegistrationEntryNumber(miDeed.getRegistrationEntryNumber());
            persistentDeed.setNumber(miDeed.getNumber());
            persistentDeed.setProcedureList(new ArrayList<Procedure>());
            for (Iterator<Procedure> it = miDeed.getProcedureList().iterator(); it.hasNext();)
            {
                Procedure procedure = it.next();
                persistentDeed.getProcedureList().add(procedure);
            }

            em.getTransaction().commit();
            modificada = true;
            em.close();
        } else
        {
            throw new ClassEliminatedException();
        }

        return modificada;
    }

    public Boolean modificarDeedSimple(Deed miDeed) throws ClassModifiedException, ClassEliminatedException
    {
        EntityManager em = null;
        Boolean modificada = false;

        int version;
        int oldVersion;

        em = getEntityManager();
        em.getTransaction().begin();
        Deed persistentDeed = em.find(Deed.class, miDeed.getIdDeed());

        if (persistentDeed != null)
        {
            version = persistentDeed.getVersion(); // Version del Objeto en db
            oldVersion = miDeed.getVersion(); // Version del Objeto en memoria

            if (version != oldVersion) //Si son distintas "Alguien modifico el objeto"
            {
                throw new ClassModifiedException();

            }

            persistentDeed.setBody(miDeed.getBody());
            persistentDeed.setStatus(miDeed.getStatus());
            persistentDeed.setDateDeedrecording(miDeed.getDateDeedrecording());
            persistentDeed.setDateRegistration(miDeed.getDateRegistration());
            persistentDeed.setRegistrationEntryNumber(miDeed.getRegistrationEntryNumber());
            persistentDeed.setNumber(miDeed.getNumber());

            em.getTransaction().commit();
            modificada = true;
            em.close();
        } else
        {
            throw new ClassEliminatedException();
        }

        return modificada;
    }

    @Override
    public String getNameJpa()
    {
        return this.getClass().getName();
    }
}
