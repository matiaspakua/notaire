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
import com.licensis.notaire.business.Copy;
import com.licensis.notaire.business.Deed;
import com.licensis.notaire.business.TestimonyMovement;
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
public class TestimonyJpaController implements Serializable, IPersistenciaJpa
{

    public TestimonyJpaController(UserTransaction utx, EntityManagerFactory emf)
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

    public Boolean create(Testimony testimony)
    {
        Boolean creado = false;
        if (testimony.getTestimonyMovementList() == null)
        {
            testimony.setTestimonyMovementList(new ArrayList<TestimonyMovement>());
        }
        if (testimony.getCopyList() == null)
        {
            testimony.setCopyList(new ArrayList<Copy>());
        }
        EntityManager em = null;
        try
        {
            em = getEntityManager();
            em.getTransaction().begin();
            Deed fkIdDeed = testimony.getFkIdDeed();
            if (fkIdDeed != null)
            {
                fkIdDeed = em.getReference(fkIdDeed.getClass(), fkIdDeed.getIdDeed());
                testimony.setFkIdDeed(fkIdDeed);
            }
            List<TestimonyMovement> attachedTestimonyMovementList = new ArrayList<TestimonyMovement>();
            for (TestimonyMovement testimonyMovementListTestimonyMovementToAttach : testimony.getTestimonyMovementList())
            {
                testimonyMovementListTestimonyMovementToAttach = em.getReference(testimonyMovementListTestimonyMovementToAttach.getClass(), testimonyMovementListTestimonyMovementToAttach.getIdTestimonyMovement());
                attachedTestimonyMovementList.add(testimonyMovementListTestimonyMovementToAttach);
            }
            testimony.setTestimonyMovementList(attachedTestimonyMovementList);
            List<Copy> attachedCopyList = new ArrayList<Copy>();
            for (Copy copyListCopyToAttach : testimony.getCopyList())
            {
                copyListCopyToAttach = em.getReference(copyListCopyToAttach.getClass(), copyListCopyToAttach.getIdCopy());
                attachedCopyList.add(copyListCopyToAttach);
            }
            testimony.setCopyList(attachedCopyList);
            em.persist(testimony);
            if (fkIdDeed != null)
            {
                fkIdDeed.getTestimonyList().add(testimony);
                fkIdDeed = em.merge(fkIdDeed);
            }
            for (TestimonyMovement testimonyMovementListTestimonyMovement : testimony.getTestimonyMovementList())
            {
                Testimony oldFkIdTestimonyOfTestimonyMovementListTestimonyMovement = testimonyMovementListTestimonyMovement.getTestimony();
                testimonyMovementListTestimonyMovement.setTestimony(testimony);
                testimonyMovementListTestimonyMovement = em.merge(testimonyMovementListTestimonyMovement);
                if (oldFkIdTestimonyOfTestimonyMovementListTestimonyMovement != null)
                {
                    oldFkIdTestimonyOfTestimonyMovementListTestimonyMovement.getTestimonyMovementList().remove(testimonyMovementListTestimonyMovement);
                    oldFkIdTestimonyOfTestimonyMovementListTestimonyMovement = em.merge(oldFkIdTestimonyOfTestimonyMovementListTestimonyMovement);
                }
            }
            for (Copy copyListCopy : testimony.getCopyList())
            {
                Testimony oldFkIdTestimonyOfCopyListCopy = copyListCopy.getFkIdTestimony();
                copyListCopy.setFkIdTestimony(testimony);
                copyListCopy = em.merge(copyListCopy);
                if (oldFkIdTestimonyOfCopyListCopy != null)
                {
                    oldFkIdTestimonyOfCopyListCopy.getCopyList().remove(copyListCopy);
                    oldFkIdTestimonyOfCopyListCopy = em.merge(oldFkIdTestimonyOfCopyListCopy);
                }
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

    public void edit(Testimony testimony) throws IllegalOrphanException, NonexistentEntityException, Exception
    {
        EntityManager em = null;
        try
        {
            em = getEntityManager();
            em.getTransaction().begin();
            Testimony persistentTestimony = em.find(Testimony.class, testimony.getIdTestimony());
            Deed fkIdDeedOld = persistentTestimony.getFkIdDeed();
            Deed fkIdDeedNew = testimony.getFkIdDeed();
            List<TestimonyMovement> testimonyMovementListOld = persistentTestimony.getTestimonyMovementList();
            List<TestimonyMovement> testimonyMovementListNew = testimony.getTestimonyMovementList();
            List<Copy> copyListOld = persistentTestimony.getCopyList();
            List<Copy> copyListNew = testimony.getCopyList();
            List<String> illegalOrphanMessages = null;
            for (TestimonyMovement testimonyMovementListOldTestimonyMovement : testimonyMovementListOld)
            {
                if (!testimonyMovementListNew.contains(testimonyMovementListOldTestimonyMovement))
                {
                    if (illegalOrphanMessages == null)
                    {
                        illegalOrphanMessages = new ArrayList<String>();
                    }
                    illegalOrphanMessages.add("You must retain MovimientoTestimonio " + testimonyMovementListOldTestimonyMovement + " since its fkIdTestimonio field is not nullable.");
                }
            }
            for (Copy copyListOldCopy : copyListOld)
            {
                if (!copyListNew.contains(copyListOldCopy))
                {
                    if (illegalOrphanMessages == null)
                    {
                        illegalOrphanMessages = new ArrayList<String>();
                    }
                    illegalOrphanMessages.add("You must retain Copia " + copyListOldCopy + " since its fkIdTestimonio field is not nullable.");
                }
            }
            if (illegalOrphanMessages != null)
            {
                throw new IllegalOrphanException(illegalOrphanMessages);
            }
            if (fkIdDeedNew != null)
            {
                fkIdDeedNew = em.getReference(fkIdDeedNew.getClass(), fkIdDeedNew.getIdDeed());
                testimony.setFkIdDeed(fkIdDeedNew);
            }
            List<TestimonyMovement> attachedTestimonyMovementListNew = new ArrayList<TestimonyMovement>();
            for (TestimonyMovement testimonyMovementListNewTestimonyMovementToAttach : testimonyMovementListNew)
            {
                testimonyMovementListNewTestimonyMovementToAttach = em.getReference(testimonyMovementListNewTestimonyMovementToAttach.getClass(), testimonyMovementListNewTestimonyMovementToAttach.getIdTestimonyMovement());
                attachedTestimonyMovementListNew.add(testimonyMovementListNewTestimonyMovementToAttach);
            }
            testimonyMovementListNew = attachedTestimonyMovementListNew;
            testimony.setTestimonyMovementList(testimonyMovementListNew);
            List<Copy> attachedCopyListNew = new ArrayList<Copy>();
            for (Copy copyListNewCopyToAttach : copyListNew)
            {
                copyListNewCopyToAttach = em.getReference(copyListNewCopyToAttach.getClass(), copyListNewCopyToAttach.getIdCopy());
                attachedCopyListNew.add(copyListNewCopyToAttach);
            }
            copyListNew = attachedCopyListNew;
            testimony.setCopyList(copyListNew);
            testimony = em.merge(testimony);
            if (fkIdDeedOld != null && !fkIdDeedOld.equals(fkIdDeedNew))
            {
                fkIdDeedOld.getTestimonyList().remove(testimony);
                fkIdDeedOld = em.merge(fkIdDeedOld);
            }
            if (fkIdDeedNew != null && !fkIdDeedNew.equals(fkIdDeedOld))
            {
                fkIdDeedNew.getTestimonyList().add(testimony);
                fkIdDeedNew = em.merge(fkIdDeedNew);
            }
            for (TestimonyMovement testimonyMovementListNewTestimonyMovement : testimonyMovementListNew)
            {
                if (!testimonyMovementListOld.contains(testimonyMovementListNewTestimonyMovement))
                {
                    Testimony oldFkIdTestimonyOfTestimonyMovementListNewTestimonyMovement = testimonyMovementListNewTestimonyMovement.getTestimony();
                    testimonyMovementListNewTestimonyMovement.setTestimony(testimony);
                    testimonyMovementListNewTestimonyMovement = em.merge(testimonyMovementListNewTestimonyMovement);
                    if (oldFkIdTestimonyOfTestimonyMovementListNewTestimonyMovement != null && !oldFkIdTestimonyOfTestimonyMovementListNewTestimonyMovement.equals(testimony))
                    {
                        oldFkIdTestimonyOfTestimonyMovementListNewTestimonyMovement.getTestimonyMovementList().remove(testimonyMovementListNewTestimonyMovement);
                        oldFkIdTestimonyOfTestimonyMovementListNewTestimonyMovement = em.merge(oldFkIdTestimonyOfTestimonyMovementListNewTestimonyMovement);
                    }
                }
            }
            for (Copy copyListNewCopy : copyListNew)
            {
                if (!copyListOld.contains(copyListNewCopy))
                {
                    Testimony oldFkIdTestimonyOfCopyListNewCopy = copyListNewCopy.getFkIdTestimony();
                    copyListNewCopy.setFkIdTestimony(testimony);
                    copyListNewCopy = em.merge(copyListNewCopy);
                    if (oldFkIdTestimonyOfCopyListNewCopy != null && !oldFkIdTestimonyOfCopyListNewCopy.equals(testimony))
                    {
                        oldFkIdTestimonyOfCopyListNewCopy.getCopyList().remove(copyListNewCopy);
                        oldFkIdTestimonyOfCopyListNewCopy = em.merge(oldFkIdTestimonyOfCopyListNewCopy);
                    }
                }
            }
            em.getTransaction().commit();
        }
        catch (Exception ex)
        {
            String msg = ex.getLocalizedMessage();
            if (msg == null || msg.length() == 0)
            {
                Integer id = testimony.getIdTestimony();
                if (findTestimony(id) == null)
                {
                    throw new NonexistentEntityException("The testimonio with id " + id + " no longer exists.");
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

    public void destroy(Integer id) throws IllegalOrphanException, NonexistentEntityException
    {
        EntityManager em = null;
        try
        {
            em = getEntityManager();
            em.getTransaction().begin();
            Testimony testimony;
            try
            {
                testimony = em.getReference(Testimony.class, id);
                testimony.getIdTestimony();
            }
            catch (EntityNotFoundException enfe)
            {
                throw new NonexistentEntityException("The testimonio with id " + id + " no longer exists.", enfe);
            }
            List<String> illegalOrphanMessages = null;
            List<TestimonyMovement> testimonyMovementListOrphanCheck = testimony.getTestimonyMovementList();
            for (TestimonyMovement testimonyMovementListOrphanCheckTestimonyMovement : testimonyMovementListOrphanCheck)
            {
                if (illegalOrphanMessages == null)
                {
                    illegalOrphanMessages = new ArrayList<String>();
                }
                illegalOrphanMessages.add("This Testimonio (" + testimony + ") cannot be destroyed since the MovimientoTestimonio " + testimonyMovementListOrphanCheckTestimonyMovement + " in its movimientoTestimonioList field has a non-nullable fkIdTestimonio field.");
            }
            List<Copy> copyListOrphanCheck = testimony.getCopyList();
            for (Copy copyListOrphanCheckCopy : copyListOrphanCheck)
            {
                if (illegalOrphanMessages == null)
                {
                    illegalOrphanMessages = new ArrayList<String>();
                }
                illegalOrphanMessages.add("This Testimonio (" + testimony + ") cannot be destroyed since the Copia " + copyListOrphanCheckCopy + " in its copiaList field has a non-nullable fkIdTestimonio field.");
            }
            if (illegalOrphanMessages != null)
            {
                throw new IllegalOrphanException(illegalOrphanMessages);
            }
            Deed fkIdDeed = testimony.getFkIdDeed();
            if (fkIdDeed != null)
            {
                fkIdDeed.getTestimonyList().remove(testimony);
                fkIdDeed = em.merge(fkIdDeed);
            }
            em.remove(testimony);
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

    public List<Testimony> findTestimonyEntities()
    {
        return findTestimonyEntities(true, -1, -1);
    }

    public List<Testimony> findTestimonyEntities(int maxResults, int firstResult)
    {
        return findTestimonyEntities(false, maxResults, firstResult);
    }

    private List<Testimony> findTestimonyEntities(boolean all, int maxResults, int firstResult)
    {
        EntityManager em = getEntityManager();
        try
        {
            Query q = em.createQuery("select object(o) from Testimonio as o");
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

    public Testimony findTestimony(Integer id)
    {
        EntityManager em = getEntityManager();
        try
        {
            return em.find(Testimony.class, id);
        }
        finally
        {
            em.close();
        }
    }

    public int getTestimonyCount()
    {
        EntityManager em = getEntityManager();
        try
        {
            Query q = em.createQuery("select count(o) from Testimonio as o");
            return ((Long) q.getSingleResult()).intValue();
        }
        finally
        {
            em.close();
        }
    }

    public List<Testimony> findTestimoniosDeed(Integer idDeed)
    {
        EntityManager em = getEntityManager();
        List<Testimony> lista = null;

        Query query = em.createNamedQuery("Testimonio.findByEscritura");
        query.setParameter("idEscritura", idDeed);

        lista = (List<Testimony>) query.getResultList();

        return lista;
    }

    public Testimony findTestimonyById(Integer idTestimony)
    {

        Testimony testimony = null;
        EntityManager em = getEntityManager();

        Query query = em.createNamedQuery("Testimonio.findByIdTestimonio");
        query.setParameter("idTestimonio", idTestimony);

        testimony = (Testimony) query.getResultList().get(0);

        return testimony;
    }

    public Boolean modificarTestimony(Testimony miTestimony) throws ClassModifiedException, ClassEliminatedException
    {

        Boolean flag = false; //Variable para saber el resultado de la transaccion
        int oldVersion = 0; //Variable para Version en memoria del Objeto
        int version = 0;    //Variable para Version en bd del Objeto

        EntityManager em = getEntityManager();

        Testimony persistentTestimony = em.find(Testimony.class, miTestimony.getIdTestimony());

        if (persistentTestimony != null)
        {
            version = persistentTestimony.getVersion(); // Version del Objeto en db
            oldVersion = miTestimony.getVersion(); // Version del Objeto en memoria

            if (version != oldVersion) //Si son distintas "Alguien modifico el objeto"
            {
                throw new ClassModifiedException();
            } else
            {
                em.getTransaction().begin();

                persistentTestimony.setFlagged(miTestimony.getFlagged());

                em.getTransaction().commit();
                em.close();
                flag = true;
            }
        } else //Si fue  eliminado se dispara una excepcion
        {
            throw new ClassEliminatedException();
        }
        return flag;
    }

    @Override
    public String getNameJpa()
    {
        return this.getClass().getName();
    }
}
