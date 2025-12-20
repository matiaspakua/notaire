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
import com.licensis.notaire.negocio.Escritura;
import com.licensis.notaire.negocio.Folio;
import com.licensis.notaire.negocio.Testimonio;
import com.licensis.notaire.negocio.Tramite;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
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
public class EscrituraJpaController implements Serializable, IPersistenciaJpa
{

    public EscrituraJpaController(UserTransaction utx, EntityManagerFactory emf)
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

    public Boolean create(Escritura escritura)
    {
        Boolean creada = false;

        if (escritura.getFolioList() == null)
        {
            escritura.setFolioList(new ArrayList<Folio>());
        }
        if (escritura.getTramiteList() == null)
        {
            escritura.setTramiteList(new ArrayList<Tramite>());
        }
        if (escritura.getTestimonioList() == null)
        {
            escritura.setTestimonioList(new ArrayList<Testimonio>());
        }
        EntityManager em = null;
        try
        {
            em = getEntityManager();
            em.getTransaction().begin();
            List<Folio> attachedFolioList = new ArrayList<Folio>();
            for (Folio folioListFolioToAttach : escritura.getFolioList())
            {
                folioListFolioToAttach = em.getReference(folioListFolioToAttach.getClass(), folioListFolioToAttach.getIdFolio());
                attachedFolioList.add(folioListFolioToAttach);
            }
            escritura.setFolioList(attachedFolioList);
            List<Tramite> attachedTramiteList = new ArrayList<Tramite>();
            for (Tramite tramiteListTramiteToAttach : escritura.getTramiteList())
            {
                tramiteListTramiteToAttach = em.getReference(tramiteListTramiteToAttach.getClass(), tramiteListTramiteToAttach.getIdTramite());
                attachedTramiteList.add(tramiteListTramiteToAttach);
            }
            escritura.setTramiteList(attachedTramiteList);
            List<Testimonio> attachedTestimonioList = new ArrayList<Testimonio>();
            for (Testimonio testimonioListTestimonioToAttach : escritura.getTestimonioList())
            {
                testimonioListTestimonioToAttach = em.getReference(testimonioListTestimonioToAttach.getClass(), testimonioListTestimonioToAttach.getIdTestimonio());
                attachedTestimonioList.add(testimonioListTestimonioToAttach);
            }
            escritura.setTestimonioList(attachedTestimonioList);
            em.persist(escritura);
            for (Folio folioListFolio : escritura.getFolioList())
            {
                Escritura oldFkIdEscrituraOfFolioListFolio = folioListFolio.getFkIdEscritura();
                folioListFolio.setFkIdEscritura(escritura);
                folioListFolio = em.merge(folioListFolio);
                if (oldFkIdEscrituraOfFolioListFolio != null)
                {
                    oldFkIdEscrituraOfFolioListFolio.getFolioList().remove(folioListFolio);
                    oldFkIdEscrituraOfFolioListFolio = em.merge(oldFkIdEscrituraOfFolioListFolio);
                }
            }
            for (Tramite tramiteListTramite : escritura.getTramiteList())
            {
                Escritura oldFkIdEscrituraOfTramiteListTramite = tramiteListTramite.getFkIdEscritura();
                tramiteListTramite.setFkIdEscritura(escritura);
                tramiteListTramite = em.merge(tramiteListTramite);
                if (oldFkIdEscrituraOfTramiteListTramite != null)
                {
                    oldFkIdEscrituraOfTramiteListTramite.getTramiteList().remove(tramiteListTramite);
                    oldFkIdEscrituraOfTramiteListTramite = em.merge(oldFkIdEscrituraOfTramiteListTramite);
                }
            }
            for (Testimonio testimonioListTestimonio : escritura.getTestimonioList())
            {
                Escritura oldFkIdEscrituraOfTestimonioListTestimonio = testimonioListTestimonio.getFkIdEscritura();
                testimonioListTestimonio.setFkIdEscritura(escritura);
                testimonioListTestimonio = em.merge(testimonioListTestimonio);
                if (oldFkIdEscrituraOfTestimonioListTestimonio != null)
                {
                    oldFkIdEscrituraOfTestimonioListTestimonio.getTestimonioList().remove(testimonioListTestimonio);
                    oldFkIdEscrituraOfTestimonioListTestimonio = em.merge(oldFkIdEscrituraOfTestimonioListTestimonio);
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

    public Boolean edit(Escritura escritura) throws IllegalOrphanException, NonexistentEntityException, ClassModifiedException, ClassEliminatedException
    {
        EntityManager em = null;
        Boolean modificada = false;

        int version;
        int oldVersion;

        em = getEntityManager();
        em.getTransaction().begin();
        Escritura persistentEscritura = em.find(Escritura.class, escritura.getIdEscritura());

        if (persistentEscritura != null)
        {
            version = persistentEscritura.getVersion(); // Version del Objeto en db
            oldVersion = escritura.getVersion(); // Version del Objeto en memoria

            if (version != oldVersion) //Si son distintas "Alguien modifico el objeto"
            {
                throw new ClassModifiedException();

            }

            List<Folio> folioListOld = persistentEscritura.getFolioList();
            List<Folio> folioListNew = escritura.getFolioList();
            List<Tramite> tramiteListOld = persistentEscritura.getTramiteList();
            List<Tramite> tramiteListNew = escritura.getTramiteList();
            List<Testimonio> testimonioListOld = persistentEscritura.getTestimonioList();
            List<Testimonio> testimonioListNew = escritura.getTestimonioList();
            List<String> illegalOrphanMessages = null;
            for (Testimonio testimonioListOldTestimonio : testimonioListOld)
            {
                if (!testimonioListNew.contains(testimonioListOldTestimonio))
                {
                    if (illegalOrphanMessages == null)
                    {
                        illegalOrphanMessages = new ArrayList<String>();
                    }
                    illegalOrphanMessages.add("You must retain Testimonio " + testimonioListOldTestimonio + " since its fkIdEscritura field is not nullable.");
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
            escritura.setFolioList(folioListNew);
            List<Tramite> attachedTramiteListNew = new ArrayList<Tramite>();
            for (Tramite tramiteListNewTramiteToAttach : tramiteListNew)
            {
                tramiteListNewTramiteToAttach = em.getReference(tramiteListNewTramiteToAttach.getClass(), tramiteListNewTramiteToAttach.getIdTramite());
                attachedTramiteListNew.add(tramiteListNewTramiteToAttach);
            }
            tramiteListNew = attachedTramiteListNew;
            escritura.setTramiteList(tramiteListNew);
            List<Testimonio> attachedTestimonioListNew = new ArrayList<Testimonio>();
            for (Testimonio testimonioListNewTestimonioToAttach : testimonioListNew)
            {
                testimonioListNewTestimonioToAttach = em.getReference(testimonioListNewTestimonioToAttach.getClass(), testimonioListNewTestimonioToAttach.getIdTestimonio());
                attachedTestimonioListNew.add(testimonioListNewTestimonioToAttach);
            }
            testimonioListNew = attachedTestimonioListNew;
            escritura.setTestimonioList(testimonioListNew);
            escritura = em.merge(escritura);
            for (Folio folioListOldFolio : folioListOld)
            {
                if (!folioListNew.contains(folioListOldFolio))
                {
                    folioListOldFolio.setFkIdEscritura(null);
                    folioListOldFolio = em.merge(folioListOldFolio);
                }
            }
            for (Folio folioListNewFolio : folioListNew)
            {
                if (!folioListOld.contains(folioListNewFolio))
                {
                    Escritura oldFkIdEscrituraOfFolioListNewFolio = folioListNewFolio.getFkIdEscritura();
                    folioListNewFolio.setFkIdEscritura(escritura);
                    folioListNewFolio = em.merge(folioListNewFolio);
                    if (oldFkIdEscrituraOfFolioListNewFolio != null && !oldFkIdEscrituraOfFolioListNewFolio.equals(escritura))
                    {
                        oldFkIdEscrituraOfFolioListNewFolio.getFolioList().remove(folioListNewFolio);
                        oldFkIdEscrituraOfFolioListNewFolio = em.merge(oldFkIdEscrituraOfFolioListNewFolio);
                    }
                }
            }
            for (Tramite tramiteListOldTramite : tramiteListOld)
            {
                if (!tramiteListNew.contains(tramiteListOldTramite))
                {
                    tramiteListOldTramite.setFkIdEscritura(null);
                    tramiteListOldTramite = em.merge(tramiteListOldTramite);
                }
            }
            for (Tramite tramiteListNewTramite : tramiteListNew)
            {
                if (!tramiteListOld.contains(tramiteListNewTramite))
                {
                    Escritura oldFkIdEscrituraOfTramiteListNewTramite = tramiteListNewTramite.getFkIdEscritura();
                    tramiteListNewTramite.setFkIdEscritura(escritura);
                    tramiteListNewTramite = em.merge(tramiteListNewTramite);
                    if (oldFkIdEscrituraOfTramiteListNewTramite != null && !oldFkIdEscrituraOfTramiteListNewTramite.equals(escritura))
                    {
                        oldFkIdEscrituraOfTramiteListNewTramite.getTramiteList().remove(tramiteListNewTramite);
                        oldFkIdEscrituraOfTramiteListNewTramite = em.merge(oldFkIdEscrituraOfTramiteListNewTramite);
                    }
                }
            }
            for (Testimonio testimonioListNewTestimonio : testimonioListNew)
            {
                if (!testimonioListOld.contains(testimonioListNewTestimonio))
                {
                    Escritura oldFkIdEscrituraOfTestimonioListNewTestimonio = testimonioListNewTestimonio.getFkIdEscritura();
                    testimonioListNewTestimonio.setFkIdEscritura(escritura);
                    testimonioListNewTestimonio = em.merge(testimonioListNewTestimonio);
                    if (oldFkIdEscrituraOfTestimonioListNewTestimonio != null && !oldFkIdEscrituraOfTestimonioListNewTestimonio.equals(escritura))
                    {
                        oldFkIdEscrituraOfTestimonioListNewTestimonio.getTestimonioList().remove(testimonioListNewTestimonio);
                        oldFkIdEscrituraOfTestimonioListNewTestimonio = em.merge(oldFkIdEscrituraOfTestimonioListNewTestimonio);
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
            Escritura escritura;
            try
            {
                escritura = em.getReference(Escritura.class, id);
                escritura.getIdEscritura();
            }
            catch (EntityNotFoundException enfe)
            {
                throw new NonexistentEntityException("The escritura with id " + id + " no longer exists.", enfe);
            }
            List<String> illegalOrphanMessages = null;
            List<Testimonio> testimonioListOrphanCheck = escritura.getTestimonioList();
            for (Testimonio testimonioListOrphanCheckTestimonio : testimonioListOrphanCheck)
            {
                if (illegalOrphanMessages == null)
                {
                    illegalOrphanMessages = new ArrayList<String>();
                }
                illegalOrphanMessages.add("This Escritura (" + escritura + ") cannot be destroyed since the Testimonio " + testimonioListOrphanCheckTestimonio + " in its testimonioList field has a non-nullable fkIdEscritura field.");
            }
            if (illegalOrphanMessages != null)
            {
                throw new IllegalOrphanException(illegalOrphanMessages);
            }
            List<Folio> folioList = escritura.getFolioList();
            for (Folio folioListFolio : folioList)
            {
                folioListFolio.setFkIdEscritura(null);
                folioListFolio = em.merge(folioListFolio);
            }
            List<Tramite> tramiteList = escritura.getTramiteList();
            for (Tramite tramiteListTramite : tramiteList)
            {
                tramiteListTramite.setFkIdEscritura(null);
                tramiteListTramite = em.merge(tramiteListTramite);
            }
            em.remove(escritura);
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

    public List<Escritura> findEscrituraEntities()
    {
        return findEscrituraEntities(true, -1, -1);
    }

    public List<Escritura> findEscrituraEntities(int maxResults, int firstResult)
    {
        return findEscrituraEntities(false, maxResults, firstResult);
    }

    private List<Escritura> findEscrituraEntities(boolean all, int maxResults, int firstResult)
    {
        EntityManager em = getEntityManager();
        try
        {
            Query q = em.createQuery("select object(o) from Escritura as o");
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

    public List<Escritura> findEscrituraByNumero(Integer numero)
    {
        EntityManager em = getEntityManager();
        List<Escritura> escrituras = null;

        Query query = em.createNamedQuery("Escritura.findByNumero");
        query.setParameter("numero", numero);

        escrituras = query.getResultList();

        return escrituras;
    }

    public Escritura findEscrituraById(Integer id)
    {
        EntityManager em = getEntityManager();
        Escritura escritura = null;

        Query query = em.createNamedQuery("Escritura.findByIdEscritura");
        query.setParameter("idEscritura", id);

        escritura = (Escritura) query.getResultList().get(0);

        return escritura;
    }

    public Escritura findEscritura(Integer id)
    {
        EntityManager em = getEntityManager();
        try
        {
            return em.find(Escritura.class, id);
        }
        finally
        {
            em.close();
        }
    }

    public int getEscrituraCount()
    {
        EntityManager em = getEntityManager();
        try
        {
            Query q = em.createQuery("select count(o) from Escritura as o");
            return ((Long) q.getSingleResult()).intValue();
        }
        finally
        {
            em.close();
        }
    }

    public Boolean modificarEscritura(Escritura miEscritura) throws ClassModifiedException, ClassEliminatedException
    {
        EntityManager em = null;
        Boolean modificada = false;

        int version;
        int oldVersion;

        em = getEntityManager();
        em.getTransaction().begin();
        Escritura persistentEscritura = em.find(Escritura.class, miEscritura.getIdEscritura());

        if (persistentEscritura != null)
        {
            version = persistentEscritura.getVersion(); // Version del Objeto en db
            oldVersion = miEscritura.getVersion(); // Version del Objeto en memoria

            if (version != oldVersion) //Si son distintas "Alguien modifico el objeto"
            {
                throw new ClassModifiedException();

            }

            persistentEscritura.setCuerpo(miEscritura.getCuerpo());
            persistentEscritura.setEstado(miEscritura.getEstado());
            persistentEscritura.setFechaEscrituracion(miEscritura.getFechaEscrituracion());
            persistentEscritura.setFechaInscripcion(miEscritura.getFechaInscripcion());

            persistentEscritura.setFolioList(new ArrayList<Folio>());
            for (Iterator<Folio> it = miEscritura.getFolioList().iterator(); it.hasNext();)
            {
                Folio folio = it.next();
                persistentEscritura.getFolioList().add(folio);
            }

            persistentEscritura.setMatriculaInscripcion(miEscritura.getMatriculaInscripcion());
            persistentEscritura.setNumero(miEscritura.getNumero());
            persistentEscritura.setTramiteList(new ArrayList<Tramite>());
            for (Iterator<Tramite> it = miEscritura.getTramiteList().iterator(); it.hasNext();)
            {
                Tramite tramite = it.next();
                persistentEscritura.getTramiteList().add(tramite);
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

    public Boolean modificarEscrituraSimple(Escritura miEscritura) throws ClassModifiedException, ClassEliminatedException
    {
        EntityManager em = null;
        Boolean modificada = false;

        int version;
        int oldVersion;

        em = getEntityManager();
        em.getTransaction().begin();
        Escritura persistentEscritura = em.find(Escritura.class, miEscritura.getIdEscritura());

        if (persistentEscritura != null)
        {
            version = persistentEscritura.getVersion(); // Version del Objeto en db
            oldVersion = miEscritura.getVersion(); // Version del Objeto en memoria

            if (version != oldVersion) //Si son distintas "Alguien modifico el objeto"
            {
                throw new ClassModifiedException();

            }

            persistentEscritura.setCuerpo(miEscritura.getCuerpo());
            persistentEscritura.setEstado(miEscritura.getEstado());
            persistentEscritura.setFechaEscrituracion(miEscritura.getFechaEscrituracion());
            persistentEscritura.setFechaInscripcion(miEscritura.getFechaInscripcion());
            persistentEscritura.setMatriculaInscripcion(miEscritura.getMatriculaInscripcion());
            persistentEscritura.setNumero(miEscritura.getNumero());

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
    public String getNombreJpa()
    {
        return this.getClass().getName();
    }
}
