/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.licensis.notaire.jpa;

import com.licensis.notaire.jpa.exceptions.ClassEliminatedException;
import com.licensis.notaire.jpa.exceptions.ClassModifiedException;
import com.licensis.notaire.jpa.exceptions.NonexistentEntityException;
import com.licensis.notaire.jpa.interfaz.IPersistenciaJpa;
import com.licensis.notaire.negocio.Copia;
import com.licensis.notaire.negocio.Folio;
import com.licensis.notaire.negocio.Persona;
import com.licensis.notaire.negocio.Testimonio;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.Query;
import javax.transaction.UserTransaction;

/**
 *
 * @author juanca
 */
public class CopiaJpaController implements Serializable, IPersistenciaJpa
{

    public CopiaJpaController(UserTransaction utx, EntityManagerFactory emf)
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

    public int create(Copia copia)
    {
        int creada = -1;
        if (copia.getFolioList() == null)
        {
            copia.setFolioList(new ArrayList<Folio>());
        }
        EntityManager em = null;
        try
        {
            em = getEntityManager();
            em.getTransaction().begin();
            Persona fkIdPersona = copia.getFkIdPersona();
            if (fkIdPersona != null)
            {
                fkIdPersona = em.getReference(fkIdPersona.getClass(), fkIdPersona.getIdPersona());
                copia.setFkIdPersona(fkIdPersona);
            }
            Testimonio fkIdTestimonio = copia.getFkIdTestimonio();
            if (fkIdTestimonio != null)
            {
                fkIdTestimonio = em.getReference(fkIdTestimonio.getClass(), fkIdTestimonio.getIdTestimonio());
                copia.setFkIdTestimonio(fkIdTestimonio);
            }
            List<Folio> attachedFolioList = new ArrayList<Folio>();
            for (Folio folioListFolioToAttach : copia.getFolioList())
            {
                folioListFolioToAttach = em.getReference(folioListFolioToAttach.getClass(), folioListFolioToAttach.getIdFolio());
                attachedFolioList.add(folioListFolioToAttach);
            }
            copia.setFolioList(attachedFolioList);
            em.persist(copia);
            if (fkIdPersona != null)
            {
                fkIdPersona.getCopiaList().add(copia);
                fkIdPersona = em.merge(fkIdPersona);
            }
            if (fkIdTestimonio != null)
            {
                fkIdTestimonio.getCopiaList().add(copia);
                fkIdTestimonio = em.merge(fkIdTestimonio);
            }
            for (Folio folioListFolio : copia.getFolioList())
            {
                folioListFolio.getCopiaList().add(copia);
                folioListFolio = em.merge(folioListFolio);
            }
            em.getTransaction().commit();
            creada = copia.getIdCopia();
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

    public void edit(Copia copia) throws NonexistentEntityException, Exception
    {
        EntityManager em = null;
        try
        {
            em = getEntityManager();
            em.getTransaction().begin();
            Copia persistentCopia = em.find(Copia.class, copia.getIdCopia());
            Persona fkIdPersonaOld = persistentCopia.getFkIdPersona();
            Persona fkIdPersonaNew = copia.getFkIdPersona();
            Testimonio fkIdTestimonioOld = persistentCopia.getFkIdTestimonio();
            Testimonio fkIdTestimonioNew = copia.getFkIdTestimonio();
            List<Folio> folioListOld = persistentCopia.getFolioList();
            List<Folio> folioListNew = copia.getFolioList();
            if (fkIdPersonaNew != null)
            {
                fkIdPersonaNew = em.getReference(fkIdPersonaNew.getClass(), fkIdPersonaNew.getIdPersona());
                copia.setFkIdPersona(fkIdPersonaNew);
            }
            if (fkIdTestimonioNew != null)
            {
                fkIdTestimonioNew = em.getReference(fkIdTestimonioNew.getClass(), fkIdTestimonioNew.getIdTestimonio());
                copia.setFkIdTestimonio(fkIdTestimonioNew);
            }
            List<Folio> attachedFolioListNew = new ArrayList<Folio>();
            for (Folio folioListNewFolioToAttach : folioListNew)
            {
                folioListNewFolioToAttach = em.getReference(folioListNewFolioToAttach.getClass(), folioListNewFolioToAttach.getIdFolio());
                attachedFolioListNew.add(folioListNewFolioToAttach);
            }
            folioListNew = attachedFolioListNew;
            copia.setFolioList(folioListNew);
            copia = em.merge(copia);
            if (fkIdPersonaOld != null && !fkIdPersonaOld.equals(fkIdPersonaNew))
            {
                fkIdPersonaOld.getCopiaList().remove(copia);
                fkIdPersonaOld = em.merge(fkIdPersonaOld);
            }
            if (fkIdPersonaNew != null && !fkIdPersonaNew.equals(fkIdPersonaOld))
            {
                fkIdPersonaNew.getCopiaList().add(copia);
                fkIdPersonaNew = em.merge(fkIdPersonaNew);
            }
            if (fkIdTestimonioOld != null && !fkIdTestimonioOld.equals(fkIdTestimonioNew))
            {
                fkIdTestimonioOld.getCopiaList().remove(copia);
                fkIdTestimonioOld = em.merge(fkIdTestimonioOld);
            }
            if (fkIdTestimonioNew != null && !fkIdTestimonioNew.equals(fkIdTestimonioOld))
            {
                fkIdTestimonioNew.getCopiaList().add(copia);
                fkIdTestimonioNew = em.merge(fkIdTestimonioNew);
            }
            for (Folio folioListOldFolio : folioListOld)
            {
                if (!folioListNew.contains(folioListOldFolio))
                {
                    folioListOldFolio.getCopiaList().remove(copia);
                    folioListOldFolio = em.merge(folioListOldFolio);
                }
            }
            for (Folio folioListNewFolio : folioListNew)
            {
                if (!folioListOld.contains(folioListNewFolio))
                {
                    folioListNewFolio.getCopiaList().add(copia);
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
                Integer id = copia.getIdCopia();
                if (findCopia(id) == null)
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
            Copia copia;
            try
            {
                copia = em.getReference(Copia.class, id);
                copia.getIdCopia();
            }
            catch (EntityNotFoundException enfe)
            {
                throw new NonexistentEntityException("The copia with id " + id + " no longer exists.", enfe);
            }
            Persona fkIdPersona = copia.getFkIdPersona();
            if (fkIdPersona != null)
            {
                fkIdPersona.getCopiaList().remove(copia);
                fkIdPersona = em.merge(fkIdPersona);
            }
            Testimonio fkIdTestimonio = copia.getFkIdTestimonio();
            if (fkIdTestimonio != null)
            {
                fkIdTestimonio.getCopiaList().remove(copia);
                fkIdTestimonio = em.merge(fkIdTestimonio);
            }
            List<Folio> folioList = copia.getFolioList();
            for (Folio folioListFolio : folioList)
            {
                folioListFolio.getCopiaList().remove(copia);
                folioListFolio = em.merge(folioListFolio);
            }
            em.remove(copia);
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

    public List<Copia> findCopiaEntities()
    {
        return findCopiaEntities(true, -1, -1);
    }

    public List<Copia> findCopiaEntities(int maxResults, int firstResult)
    {
        return findCopiaEntities(false, maxResults, firstResult);
    }

    private List<Copia> findCopiaEntities(boolean all, int maxResults, int firstResult)
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

    public Copia findCopia(Integer id)
    {
        EntityManager em = getEntityManager();
        try
        {
            return em.find(Copia.class, id);
        }
        finally
        {
            em.close();
        }
    }

    public int getCopiaCount()
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

    public int crearCopia(Copia miCopia)
    {
        int creada = -1;

        EntityManager em = null;
        try
        {
            em = getEntityManager();
            em.getTransaction().begin();

            em.persist(miCopia);

            em.getTransaction().commit();
            creada = miCopia.getIdCopia();
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

    public List<Copia> buscarCopiasTestimonio(Integer idTestimonio)
    {
        List<Copia> copias = null;
        EntityManager em = getEntityManager();

        Query query = em.createNamedQuery("Copia.findByTestimonio");
        query.setParameter("idTestimonio", idTestimonio);

        copias = (List<Copia>) query.getResultList();

        return copias;
    }

    public Boolean modificarCopia(Copia copia) throws ClassModifiedException, ClassEliminatedException
    {
        EntityManager em = null;
        Boolean modificada = false;
        int version = 0;
        int oldVersion = 0;

        em = getEntityManager();
        em.getTransaction().begin();
        Copia persistentCopia = em.find(Copia.class, copia.getIdCopia());

        if (persistentCopia != null)
        {
            version = persistentCopia.getVersion(); // Version del Objeto en db
            oldVersion = copia.getVersion(); // Version del Objeto en memoria

            if (version != oldVersion) //Si son distintas "Alguien modifico el objeto"
            {
                throw new ClassModifiedException();
            }

            persistentCopia.setFechaRetiro(copia.getFechaRetiro());
            persistentCopia.setObservaciones(copia.getObservaciones());

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
    public String getNombreJpa()
    {
        return this.getClass().getName();
    }
}
