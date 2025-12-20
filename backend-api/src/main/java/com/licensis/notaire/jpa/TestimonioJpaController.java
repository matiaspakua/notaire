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
import com.licensis.notaire.negocio.Copia;
import com.licensis.notaire.negocio.Escritura;
import com.licensis.notaire.negocio.MovimientoTestimonio;
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
public class TestimonioJpaController implements Serializable, IPersistenciaJpa
{

    public TestimonioJpaController(UserTransaction utx, EntityManagerFactory emf)
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

    public Boolean create(Testimonio testimonio)
    {
        Boolean creado = false;
        if (testimonio.getMovimientoTestimonioList() == null)
        {
            testimonio.setMovimientoTestimonioList(new ArrayList<MovimientoTestimonio>());
        }
        if (testimonio.getCopiaList() == null)
        {
            testimonio.setCopiaList(new ArrayList<Copia>());
        }
        EntityManager em = null;
        try
        {
            em = getEntityManager();
            em.getTransaction().begin();
            Escritura fkIdEscritura = testimonio.getFkIdEscritura();
            if (fkIdEscritura != null)
            {
                fkIdEscritura = em.getReference(fkIdEscritura.getClass(), fkIdEscritura.getIdEscritura());
                testimonio.setFkIdEscritura(fkIdEscritura);
            }
            List<MovimientoTestimonio> attachedMovimientoTestimonioList = new ArrayList<MovimientoTestimonio>();
            for (MovimientoTestimonio movimientoTestimonioListMovimientoTestimonioToAttach : testimonio.getMovimientoTestimonioList())
            {
                movimientoTestimonioListMovimientoTestimonioToAttach = em.getReference(movimientoTestimonioListMovimientoTestimonioToAttach.getClass(), movimientoTestimonioListMovimientoTestimonioToAttach.getIdMovimientoTestimonio());
                attachedMovimientoTestimonioList.add(movimientoTestimonioListMovimientoTestimonioToAttach);
            }
            testimonio.setMovimientoTestimonioList(attachedMovimientoTestimonioList);
            List<Copia> attachedCopiaList = new ArrayList<Copia>();
            for (Copia copiaListCopiaToAttach : testimonio.getCopiaList())
            {
                copiaListCopiaToAttach = em.getReference(copiaListCopiaToAttach.getClass(), copiaListCopiaToAttach.getIdCopia());
                attachedCopiaList.add(copiaListCopiaToAttach);
            }
            testimonio.setCopiaList(attachedCopiaList);
            em.persist(testimonio);
            if (fkIdEscritura != null)
            {
                fkIdEscritura.getTestimonioList().add(testimonio);
                fkIdEscritura = em.merge(fkIdEscritura);
            }
            for (MovimientoTestimonio movimientoTestimonioListMovimientoTestimonio : testimonio.getMovimientoTestimonioList())
            {
                Testimonio oldFkIdTestimonioOfMovimientoTestimonioListMovimientoTestimonio = movimientoTestimonioListMovimientoTestimonio.getTestimonio();
                movimientoTestimonioListMovimientoTestimonio.setTestimonio(testimonio);
                movimientoTestimonioListMovimientoTestimonio = em.merge(movimientoTestimonioListMovimientoTestimonio);
                if (oldFkIdTestimonioOfMovimientoTestimonioListMovimientoTestimonio != null)
                {
                    oldFkIdTestimonioOfMovimientoTestimonioListMovimientoTestimonio.getMovimientoTestimonioList().remove(movimientoTestimonioListMovimientoTestimonio);
                    oldFkIdTestimonioOfMovimientoTestimonioListMovimientoTestimonio = em.merge(oldFkIdTestimonioOfMovimientoTestimonioListMovimientoTestimonio);
                }
            }
            for (Copia copiaListCopia : testimonio.getCopiaList())
            {
                Testimonio oldFkIdTestimonioOfCopiaListCopia = copiaListCopia.getFkIdTestimonio();
                copiaListCopia.setFkIdTestimonio(testimonio);
                copiaListCopia = em.merge(copiaListCopia);
                if (oldFkIdTestimonioOfCopiaListCopia != null)
                {
                    oldFkIdTestimonioOfCopiaListCopia.getCopiaList().remove(copiaListCopia);
                    oldFkIdTestimonioOfCopiaListCopia = em.merge(oldFkIdTestimonioOfCopiaListCopia);
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

    public void edit(Testimonio testimonio) throws IllegalOrphanException, NonexistentEntityException, Exception
    {
        EntityManager em = null;
        try
        {
            em = getEntityManager();
            em.getTransaction().begin();
            Testimonio persistentTestimonio = em.find(Testimonio.class, testimonio.getIdTestimonio());
            Escritura fkIdEscrituraOld = persistentTestimonio.getFkIdEscritura();
            Escritura fkIdEscrituraNew = testimonio.getFkIdEscritura();
            List<MovimientoTestimonio> movimientoTestimonioListOld = persistentTestimonio.getMovimientoTestimonioList();
            List<MovimientoTestimonio> movimientoTestimonioListNew = testimonio.getMovimientoTestimonioList();
            List<Copia> copiaListOld = persistentTestimonio.getCopiaList();
            List<Copia> copiaListNew = testimonio.getCopiaList();
            List<String> illegalOrphanMessages = null;
            for (MovimientoTestimonio movimientoTestimonioListOldMovimientoTestimonio : movimientoTestimonioListOld)
            {
                if (!movimientoTestimonioListNew.contains(movimientoTestimonioListOldMovimientoTestimonio))
                {
                    if (illegalOrphanMessages == null)
                    {
                        illegalOrphanMessages = new ArrayList<String>();
                    }
                    illegalOrphanMessages.add("You must retain MovimientoTestimonio " + movimientoTestimonioListOldMovimientoTestimonio + " since its fkIdTestimonio field is not nullable.");
                }
            }
            for (Copia copiaListOldCopia : copiaListOld)
            {
                if (!copiaListNew.contains(copiaListOldCopia))
                {
                    if (illegalOrphanMessages == null)
                    {
                        illegalOrphanMessages = new ArrayList<String>();
                    }
                    illegalOrphanMessages.add("You must retain Copia " + copiaListOldCopia + " since its fkIdTestimonio field is not nullable.");
                }
            }
            if (illegalOrphanMessages != null)
            {
                throw new IllegalOrphanException(illegalOrphanMessages);
            }
            if (fkIdEscrituraNew != null)
            {
                fkIdEscrituraNew = em.getReference(fkIdEscrituraNew.getClass(), fkIdEscrituraNew.getIdEscritura());
                testimonio.setFkIdEscritura(fkIdEscrituraNew);
            }
            List<MovimientoTestimonio> attachedMovimientoTestimonioListNew = new ArrayList<MovimientoTestimonio>();
            for (MovimientoTestimonio movimientoTestimonioListNewMovimientoTestimonioToAttach : movimientoTestimonioListNew)
            {
                movimientoTestimonioListNewMovimientoTestimonioToAttach = em.getReference(movimientoTestimonioListNewMovimientoTestimonioToAttach.getClass(), movimientoTestimonioListNewMovimientoTestimonioToAttach.getIdMovimientoTestimonio());
                attachedMovimientoTestimonioListNew.add(movimientoTestimonioListNewMovimientoTestimonioToAttach);
            }
            movimientoTestimonioListNew = attachedMovimientoTestimonioListNew;
            testimonio.setMovimientoTestimonioList(movimientoTestimonioListNew);
            List<Copia> attachedCopiaListNew = new ArrayList<Copia>();
            for (Copia copiaListNewCopiaToAttach : copiaListNew)
            {
                copiaListNewCopiaToAttach = em.getReference(copiaListNewCopiaToAttach.getClass(), copiaListNewCopiaToAttach.getIdCopia());
                attachedCopiaListNew.add(copiaListNewCopiaToAttach);
            }
            copiaListNew = attachedCopiaListNew;
            testimonio.setCopiaList(copiaListNew);
            testimonio = em.merge(testimonio);
            if (fkIdEscrituraOld != null && !fkIdEscrituraOld.equals(fkIdEscrituraNew))
            {
                fkIdEscrituraOld.getTestimonioList().remove(testimonio);
                fkIdEscrituraOld = em.merge(fkIdEscrituraOld);
            }
            if (fkIdEscrituraNew != null && !fkIdEscrituraNew.equals(fkIdEscrituraOld))
            {
                fkIdEscrituraNew.getTestimonioList().add(testimonio);
                fkIdEscrituraNew = em.merge(fkIdEscrituraNew);
            }
            for (MovimientoTestimonio movimientoTestimonioListNewMovimientoTestimonio : movimientoTestimonioListNew)
            {
                if (!movimientoTestimonioListOld.contains(movimientoTestimonioListNewMovimientoTestimonio))
                {
                    Testimonio oldFkIdTestimonioOfMovimientoTestimonioListNewMovimientoTestimonio = movimientoTestimonioListNewMovimientoTestimonio.getTestimonio();
                    movimientoTestimonioListNewMovimientoTestimonio.setTestimonio(testimonio);
                    movimientoTestimonioListNewMovimientoTestimonio = em.merge(movimientoTestimonioListNewMovimientoTestimonio);
                    if (oldFkIdTestimonioOfMovimientoTestimonioListNewMovimientoTestimonio != null && !oldFkIdTestimonioOfMovimientoTestimonioListNewMovimientoTestimonio.equals(testimonio))
                    {
                        oldFkIdTestimonioOfMovimientoTestimonioListNewMovimientoTestimonio.getMovimientoTestimonioList().remove(movimientoTestimonioListNewMovimientoTestimonio);
                        oldFkIdTestimonioOfMovimientoTestimonioListNewMovimientoTestimonio = em.merge(oldFkIdTestimonioOfMovimientoTestimonioListNewMovimientoTestimonio);
                    }
                }
            }
            for (Copia copiaListNewCopia : copiaListNew)
            {
                if (!copiaListOld.contains(copiaListNewCopia))
                {
                    Testimonio oldFkIdTestimonioOfCopiaListNewCopia = copiaListNewCopia.getFkIdTestimonio();
                    copiaListNewCopia.setFkIdTestimonio(testimonio);
                    copiaListNewCopia = em.merge(copiaListNewCopia);
                    if (oldFkIdTestimonioOfCopiaListNewCopia != null && !oldFkIdTestimonioOfCopiaListNewCopia.equals(testimonio))
                    {
                        oldFkIdTestimonioOfCopiaListNewCopia.getCopiaList().remove(copiaListNewCopia);
                        oldFkIdTestimonioOfCopiaListNewCopia = em.merge(oldFkIdTestimonioOfCopiaListNewCopia);
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
                Integer id = testimonio.getIdTestimonio();
                if (findTestimonio(id) == null)
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
            Testimonio testimonio;
            try
            {
                testimonio = em.getReference(Testimonio.class, id);
                testimonio.getIdTestimonio();
            }
            catch (EntityNotFoundException enfe)
            {
                throw new NonexistentEntityException("The testimonio with id " + id + " no longer exists.", enfe);
            }
            List<String> illegalOrphanMessages = null;
            List<MovimientoTestimonio> movimientoTestimonioListOrphanCheck = testimonio.getMovimientoTestimonioList();
            for (MovimientoTestimonio movimientoTestimonioListOrphanCheckMovimientoTestimonio : movimientoTestimonioListOrphanCheck)
            {
                if (illegalOrphanMessages == null)
                {
                    illegalOrphanMessages = new ArrayList<String>();
                }
                illegalOrphanMessages.add("This Testimonio (" + testimonio + ") cannot be destroyed since the MovimientoTestimonio " + movimientoTestimonioListOrphanCheckMovimientoTestimonio + " in its movimientoTestimonioList field has a non-nullable fkIdTestimonio field.");
            }
            List<Copia> copiaListOrphanCheck = testimonio.getCopiaList();
            for (Copia copiaListOrphanCheckCopia : copiaListOrphanCheck)
            {
                if (illegalOrphanMessages == null)
                {
                    illegalOrphanMessages = new ArrayList<String>();
                }
                illegalOrphanMessages.add("This Testimonio (" + testimonio + ") cannot be destroyed since the Copia " + copiaListOrphanCheckCopia + " in its copiaList field has a non-nullable fkIdTestimonio field.");
            }
            if (illegalOrphanMessages != null)
            {
                throw new IllegalOrphanException(illegalOrphanMessages);
            }
            Escritura fkIdEscritura = testimonio.getFkIdEscritura();
            if (fkIdEscritura != null)
            {
                fkIdEscritura.getTestimonioList().remove(testimonio);
                fkIdEscritura = em.merge(fkIdEscritura);
            }
            em.remove(testimonio);
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

    public List<Testimonio> findTestimonioEntities()
    {
        return findTestimonioEntities(true, -1, -1);
    }

    public List<Testimonio> findTestimonioEntities(int maxResults, int firstResult)
    {
        return findTestimonioEntities(false, maxResults, firstResult);
    }

    private List<Testimonio> findTestimonioEntities(boolean all, int maxResults, int firstResult)
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

    public Testimonio findTestimonio(Integer id)
    {
        EntityManager em = getEntityManager();
        try
        {
            return em.find(Testimonio.class, id);
        }
        finally
        {
            em.close();
        }
    }

    public int getTestimonioCount()
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

    public List<Testimonio> findTestimoniosEscritura(Integer idEscritura)
    {
        EntityManager em = getEntityManager();
        List<Testimonio> lista = null;

        Query query = em.createNamedQuery("Testimonio.findByEscritura");
        query.setParameter("idEscritura", idEscritura);

        lista = (List<Testimonio>) query.getResultList();

        return lista;
    }

    public Testimonio findTestimonioById(Integer idTestimonio)
    {

        Testimonio testimonio = null;
        EntityManager em = getEntityManager();

        Query query = em.createNamedQuery("Testimonio.findByIdTestimonio");
        query.setParameter("idTestimonio", idTestimonio);

        testimonio = (Testimonio) query.getResultList().get(0);

        return testimonio;
    }

    public Boolean modificarTestimonio(Testimonio miTestimonio) throws ClassModifiedException, ClassEliminatedException
    {

        Boolean flag = false; //Variable para saber el resultado de la transaccion
        int oldVersion = 0; //Variable para Version en memoria del Objeto
        int version = 0;    //Variable para Version en bd del Objeto

        EntityManager em = getEntityManager();

        Testimonio persistentTestimonio = em.find(Testimonio.class, miTestimonio.getIdTestimonio());

        if (persistentTestimonio != null)
        {
            version = persistentTestimonio.getVersion(); // Version del Objeto en db
            oldVersion = miTestimonio.getVersion(); // Version del Objeto en memoria

            if (version != oldVersion) //Si son distintas "Alguien modifico el objeto"
            {
                throw new ClassModifiedException();
            } else
            {
                em.getTransaction().begin();

                persistentTestimonio.setObservado(miTestimonio.getObservado());

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
    public String getNombreJpa()
    {
        return this.getClass().getName();
    }
}
