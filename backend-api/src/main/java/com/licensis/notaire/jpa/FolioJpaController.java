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
import com.licensis.notaire.negocio.Escritura;
import com.licensis.notaire.negocio.Folio;
import com.licensis.notaire.negocio.Persona;
import com.licensis.notaire.negocio.TipoDeFolio;
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
public class FolioJpaController implements Serializable, IPersistenciaJpa
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
        if (folio.getCopiaList() == null)
        {
            folio.setCopiaList(new ArrayList<Copia>());
        }
        EntityManager em = null;
        try
        {
            em = getEntityManager();
            em.getTransaction().begin();
            Persona fkIdPersonaEscribano = folio.getFkIdPersonaEscribano();
            if (fkIdPersonaEscribano != null)
            {
                fkIdPersonaEscribano = em.getReference(fkIdPersonaEscribano.getClass(), fkIdPersonaEscribano.getIdPersona());
                folio.setFkIdPersonaEscribano(fkIdPersonaEscribano);
            }
            TipoDeFolio fkIdTipoFolio = folio.getFkIdTipoFolio();
            if (fkIdTipoFolio != null)
            {
                fkIdTipoFolio = em.getReference(fkIdTipoFolio.getClass(), fkIdTipoFolio.getIdTipoFolio());
                folio.setFkIdTipoFolio(fkIdTipoFolio);
            }
            Escritura fkIdEscritura = folio.getFkIdEscritura();
            if (fkIdEscritura != null)
            {
                fkIdEscritura = em.getReference(fkIdEscritura.getClass(), fkIdEscritura.getIdEscritura());
                folio.setFkIdEscritura(fkIdEscritura);
            }
            List<Copia> attachedCopiaList = new ArrayList<Copia>();
            for (Copia copiaListCopiaToAttach : folio.getCopiaList())
            {
                copiaListCopiaToAttach = em.getReference(copiaListCopiaToAttach.getClass(), copiaListCopiaToAttach.getIdCopia());
                attachedCopiaList.add(copiaListCopiaToAttach);
            }
            folio.setCopiaList(attachedCopiaList);
            em.persist(folio);
            if (fkIdPersonaEscribano != null)
            {
                fkIdPersonaEscribano.getFolioList().add(folio);
                fkIdPersonaEscribano = em.merge(fkIdPersonaEscribano);
            }
            if (fkIdTipoFolio != null)
            {
                fkIdTipoFolio.getFolioList().add(folio);
                fkIdTipoFolio = em.merge(fkIdTipoFolio);
            }
            if (fkIdEscritura != null)
            {
                fkIdEscritura.getFolioList().add(folio);
                fkIdEscritura = em.merge(fkIdEscritura);
            }
            for (Copia copiaListCopia : folio.getCopiaList())
            {
                copiaListCopia.getFolioList().add(folio);
                copiaListCopia = em.merge(copiaListCopia);
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
        Integer version = ConstantesPersistencia.VERSION_INICIAL;
        Integer oldVersion = ConstantesPersistencia.VERSION_INICIAL;

        em = getEntityManager();
        em.getTransaction().begin();
        Folio persistentFolio = em.find(Folio.class, folio.getIdFolio());

        if (persistentFolio != null)
        {
            version = persistentFolio.getVersion();
            oldVersion = folio.getVersion();

            if (version != oldVersion) //Si son distintas "Alguien modifico el objeto"
            {
                if (em != null)
                {
                    em.close();
                }
                throw new ClassModifiedException();
            } else
            {
                Persona fkIdPersonaEscribanoOld = persistentFolio.getFkIdPersonaEscribano();
                Persona fkIdPersonaEscribanoNew = folio.getFkIdPersonaEscribano();
                TipoDeFolio fkIdTipoFolioOld = persistentFolio.getFkIdTipoFolio();
                TipoDeFolio fkIdTipoFolioNew = folio.getFkIdTipoFolio();
                Escritura fkIdEscrituraOld = persistentFolio.getFkIdEscritura();
                Escritura fkIdEscrituraNew = folio.getFkIdEscritura();
//                List<Copia> copiaListOld = persistentFolio.getCopiaList();
//                List<Copia> copiaListNew = folio.getCopiaList();
                if (fkIdPersonaEscribanoNew != null)
                {
                    fkIdPersonaEscribanoNew = em.getReference(fkIdPersonaEscribanoNew.getClass(), fkIdPersonaEscribanoNew.getIdPersona());
                    folio.setFkIdPersonaEscribano(fkIdPersonaEscribanoNew);
                }
                if (fkIdTipoFolioNew != null)
                {
                    fkIdTipoFolioNew = em.getReference(fkIdTipoFolioNew.getClass(), fkIdTipoFolioNew.getIdTipoFolio());
                    folio.setFkIdTipoFolio(fkIdTipoFolioNew);
                }
                if (fkIdEscrituraNew != null)
                {
                    fkIdEscrituraNew = em.getReference(fkIdEscrituraNew.getClass(), fkIdEscrituraNew.getIdEscritura());
                    folio.setFkIdEscritura(fkIdEscrituraNew);
                }
//                List<Copia> attachedCopiaListNew = new ArrayList<Copia>();
//                for (Copia copiaListNewCopiaToAttach : copiaListNew)
//                {
//                    copiaListNewCopiaToAttach = em.getReference(copiaListNewCopiaToAttach.getClass(), copiaListNewCopiaToAttach.getIdCopia());
//                    attachedCopiaListNew.add(copiaListNewCopiaToAttach);
//                }
//                copiaListNew = attachedCopiaListNew;
//                folio.setCopiaList(copiaListNew);
                folio = em.merge(folio);
//                if (fkIdPersonaEscribanoOld != null && !fkIdPersonaEscribanoOld.equals(fkIdPersonaEscribanoNew))
//                {
//                    fkIdPersonaEscribanoOld.getFolioList().remove(folio);
//                    fkIdPersonaEscribanoOld = em.merge(fkIdPersonaEscribanoOld);
//                }
//                if (fkIdPersonaEscribanoNew != null && !fkIdPersonaEscribanoNew.equals(fkIdPersonaEscribanoOld))
//                {
//                    fkIdPersonaEscribanoNew.getFolioList().add(folio);
//                    fkIdPersonaEscribanoNew = em.merge(fkIdPersonaEscribanoNew);
//                }
//                if (fkIdTipoFolioOld != null && !fkIdTipoFolioOld.equals(fkIdTipoFolioNew))
//                {
//                    fkIdTipoFolioOld.getFolioList().remove(folio);
//                    fkIdTipoFolioOld = em.merge(fkIdTipoFolioOld);
//                }
//                if (fkIdTipoFolioNew != null && !fkIdTipoFolioNew.equals(fkIdTipoFolioOld))
//                {
//                    fkIdTipoFolioNew.getFolioList().add(folio);
//                    fkIdTipoFolioNew = em.merge(fkIdTipoFolioNew);
//                }
//                if (fkIdEscrituraOld != null && !fkIdEscrituraOld.equals(fkIdEscrituraNew))
//                {
//                    fkIdEscrituraOld.getFolioList().remove(folio);
//                    fkIdEscrituraOld = em.merge(fkIdEscrituraOld);
//                }
//                if (fkIdEscrituraNew != null && !fkIdEscrituraNew.equals(fkIdEscrituraOld))
//                {
//                    fkIdEscrituraNew.getFolioList().add(folio);
//                    fkIdEscrituraNew = em.merge(fkIdEscrituraNew);
//                }
//                for (Copia copiaListOldCopia : copiaListOld)
//                {
//                    if (!copiaListNew.contains(copiaListOldCopia))
//                    {
//                        copiaListOldCopia.getFolioList().remove(folio);
//                        copiaListOldCopia = em.merge(copiaListOldCopia);
//                    }
//                }
//                for (Copia copiaListNewCopia : copiaListNew)
//                {
//                    if (!copiaListOld.contains(copiaListNewCopia))
//                    {
//                        copiaListNewCopia.getFolioList().add(folio);
//                        copiaListNewCopia = em.merge(copiaListNewCopia);
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

    public boolean modificarFoliosCompleto(Folio folioModificado) throws ClassModifiedException
    {
        boolean resultado = false;

        EntityManager em = null;
        Integer version = ConstantesPersistencia.VERSION_INICIAL;
        Integer oldVersion = ConstantesPersistencia.VERSION_INICIAL;

        em = getEntityManager();
        em.getTransaction().begin();
        Folio persistentFolio = em.find(Folio.class, folioModificado.getIdFolio());

        if (persistentFolio != null)
        {
            version = persistentFolio.getVersion();
            oldVersion = folioModificado.getVersion();

            if (version != oldVersion) //Si son distintas "Alguien modifico el objeto"
            {
                if (em != null)
                {
                    em.close();
                }
                throw new ClassModifiedException();
            } else
            {
                persistentFolio.setAnio(folioModificado.getAnio());
                persistentFolio.setEstado(folioModificado.getEstado());
//                persistentFolio.setFkIdPersonaEscribano(folioModificado.getFkIdPersonaEscribano());
//                persistentFolio.setFkIdTipoFolio(folioModificado.getFkIdTipoFolio());
                persistentFolio.setNumero(folioModificado.getNumero());
                persistentFolio.setObservaciones(folioModificado.getObservaciones());

                em.getTransaction().commit();

                if (em != null)
                {
                    em.close();
                }

                resultado = true;
            }
        }

        return resultado;
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
            Persona fkIdPersonaEscribano = folio.getFkIdPersonaEscribano();
            if (fkIdPersonaEscribano != null)
            {
                fkIdPersonaEscribano.getFolioList().remove(folio);
                fkIdPersonaEscribano = em.merge(fkIdPersonaEscribano);
            }
            TipoDeFolio fkIdTipoFolio = folio.getFkIdTipoFolio();
            if (fkIdTipoFolio != null)
            {
                fkIdTipoFolio.getFolioList().remove(folio);
                fkIdTipoFolio = em.merge(fkIdTipoFolio);
            }
            Escritura fkIdEscritura = folio.getFkIdEscritura();
            if (fkIdEscritura != null)
            {
                fkIdEscritura.getFolioList().remove(folio);
                fkIdEscritura = em.merge(fkIdEscritura);
            }
            List<Copia> copiaList = folio.getCopiaList();
            for (Copia copiaListCopia : copiaList)
            {
                copiaListCopia.getFolioList().remove(folio);
                copiaListCopia = em.merge(copiaListCopia);
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

    public List<Folio> findFoliosRegistroAnio(Integer registro, Integer anio)
    {
        EntityManager em = getEntityManager();
        List<Folio> miListaFolios = null;
        try
        {
            Query q = em.createNamedQuery("Folio.findByAnioAndRegistro");
            q.setParameter("anio", anio);
            q.setParameter("registro", registro);
            miListaFolios = q.getResultList();

            for (Iterator<Folio> it = miListaFolios.iterator(); it.hasNext();)
            {
                Folio folio = it.next();
                folio.setFkIdPersonaEscribano(null);
            }

            return miListaFolios;
        }
        finally
        {
            em.close();
        }

    }

    public List<Folio> findFoliosbyNumero(Integer numero)
    {
        EntityManager em = getEntityManager();
        List<Folio> miListaFolios = null;
        try
        {
            Query q = em.createNamedQuery("Folio.findByNumero");
            q.setParameter("numero", numero);

            miListaFolios = q.getResultList();

            for (Iterator<Folio> it = miListaFolios.iterator(); it.hasNext();)
            {
                Folio folio = it.next();
                folio.setFkIdPersonaEscribano(null);
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

            folio.setCopiaList(new ArrayList<Copia>());

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

    public void modificarFolio(Folio miFolio) throws ClassModifiedException, ClassEliminatedException
    {
        EntityManager em = null;

        int version;
        int oldVersion;

        em = getEntityManager();
        em.getTransaction().begin();
        Folio persistentFolio = em.find(Folio.class, miFolio.getIdFolio());

        if (persistentFolio != null)
        {
            version = persistentFolio.getVersion(); // Version del Objeto en db
            oldVersion = miFolio.getVersion(); // Version del Objeto en memoria

            if (version != oldVersion) //Si son distintas "Alguien modifico el objeto"
            {
                throw new ClassModifiedException();

            }

            persistentFolio.setEstado(miFolio.getEstado());

            em.getTransaction().commit();

            em.close();
        } else
        {
            throw new ClassEliminatedException();
        }
    }

    @Override
    public String getNombreJpa()
    {
        return this.getClass().getName();
    }
}
