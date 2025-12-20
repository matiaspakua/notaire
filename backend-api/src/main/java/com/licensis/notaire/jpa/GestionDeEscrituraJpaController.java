/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.licensis.notaire.jpa;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.Query;
import javax.transaction.UserTransaction;
import com.licensis.notaire.jpa.exceptions.ClassModifiedException;
import com.licensis.notaire.jpa.exceptions.IllegalOrphanException;
import com.licensis.notaire.jpa.exceptions.NonexistentEntityException;
import com.licensis.notaire.jpa.interfaz.IPersistenciaJpa;
import com.licensis.notaire.negocio.GestionDeEscritura;
import com.licensis.notaire.negocio.Historial;
import com.licensis.notaire.negocio.Persona;
import com.licensis.notaire.negocio.Tramite;
import org.hibernate.StaleObjectStateException;

/**
 *
 * @author juanca
 */
public class GestionDeEscrituraJpaController implements Serializable, IPersistenciaJpa
{

    public GestionDeEscrituraJpaController(UserTransaction utx, EntityManagerFactory emf)
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

    /**
     * Persiste en la base de datos una nueva gestion de escrituras.
     *
     * @param unaGestionDeEscritura La nueva gestion de escrituras a persistir.
     * @return oidGestionEscritura El id de la nueva gestion de escritura, -1 si ocurrio algun
     * error.
     */
    public Integer create(GestionDeEscritura unaGestionDeEscritura)
    {
        Integer oidGestionEscritura = new Integer(-1);
        if (unaGestionDeEscritura.getHistorialList() == null)
        {
            unaGestionDeEscritura.setHistorialList(new ArrayList<Historial>());
        }
        if (unaGestionDeEscritura.getTramiteList() == null)
        {
            unaGestionDeEscritura.setTramiteList(new ArrayList<Tramite>());
        }
        EntityManager em = null;
        try
        {
            em = getEntityManager();
            em.getTransaction().begin();
            Persona fkIdPersonaEscribano = unaGestionDeEscritura.getFkIdPersonaEscribano();
            if (fkIdPersonaEscribano != null)
            {
                fkIdPersonaEscribano = em.getReference(fkIdPersonaEscribano.getClass(), fkIdPersonaEscribano.getIdPersona());
                unaGestionDeEscritura.setFkIdPersonaEscribano(fkIdPersonaEscribano);
            }
            List<Historial> attachedHistorialList = new ArrayList<Historial>();
            for (Historial historialListHistorialToAttach : unaGestionDeEscritura.getHistorialList())
            {
                historialListHistorialToAttach = em.getReference(historialListHistorialToAttach.getClass(), historialListHistorialToAttach.getIdHistorial());
                attachedHistorialList.add(historialListHistorialToAttach);
            }
            unaGestionDeEscritura.setHistorialList(attachedHistorialList);
            List<Tramite> attachedTramiteList = new ArrayList<Tramite>();
            for (Tramite tramiteListTramiteToAttach : unaGestionDeEscritura.getTramiteList())
            {
                tramiteListTramiteToAttach = em.getReference(tramiteListTramiteToAttach.getClass(), tramiteListTramiteToAttach.getIdTramite());
                attachedTramiteList.add(tramiteListTramiteToAttach);
            }
            unaGestionDeEscritura.setTramiteList(attachedTramiteList);
            em.persist(unaGestionDeEscritura);
            if (fkIdPersonaEscribano != null)
            {
                fkIdPersonaEscribano.getGestionDeEscrituraList().add(unaGestionDeEscritura);
                fkIdPersonaEscribano = em.merge(fkIdPersonaEscribano);
            }
            for (Historial historialListHistorial : unaGestionDeEscritura.getHistorialList())
            {
                GestionDeEscritura oldFkIdGestionOfHistorialListHistorial = historialListHistorial.getFkIdGestion();
                historialListHistorial.setFkIdGestion(unaGestionDeEscritura);
                historialListHistorial = em.merge(historialListHistorial);
                if (oldFkIdGestionOfHistorialListHistorial != null)
                {
                    oldFkIdGestionOfHistorialListHistorial.getHistorialList().remove(historialListHistorial);
                    oldFkIdGestionOfHistorialListHistorial = em.merge(oldFkIdGestionOfHistorialListHistorial);
                }
            }
            for (Tramite tramiteListTramite : unaGestionDeEscritura.getTramiteList())
            {
                GestionDeEscritura oldFkIdGestionOfTramiteListTramite = tramiteListTramite.getFkIdGestion();
                tramiteListTramite.setFkIdGestion(unaGestionDeEscritura);
                tramiteListTramite = em.merge(tramiteListTramite);
                if (oldFkIdGestionOfTramiteListTramite != null)
                {
                    oldFkIdGestionOfTramiteListTramite.getTramiteList().remove(tramiteListTramite);
                    oldFkIdGestionOfTramiteListTramite = em.merge(oldFkIdGestionOfTramiteListTramite);
                }
            }
            em.getTransaction().commit();

            oidGestionEscritura = unaGestionDeEscritura.getIdGestion();
        }
        finally
        {
            if (em != null)
            {
                em.close();
            }
        }
        return oidGestionEscritura;
    }

    /**
     * Permite modificar una gestion de escritura en la persistencia.
     *
     * @param gestionParaModificar La gestion de escritura a modificar.
     * @return modificado Verdadero si se pudo modificar, falso en caso contrario.
     *
     * @throws IllegalOrphanException
     * @throws NonexistentEntityException
     * @throws Exception
     */
    public Boolean edit(GestionDeEscritura gestionParaModificar) throws IllegalOrphanException, NonexistentEntityException, ClassModifiedException
    {
        Boolean modificado = Boolean.FALSE;

        int version;
        int oldVersion;

        EntityManager em = null;
        try
        {
            em = getEntityManager();

            GestionDeEscritura persistentGestion = em.find(GestionDeEscritura.class, gestionParaModificar.getIdGestion());

            if (persistentGestion != null)
            {
                version = persistentGestion.getVersion();
                oldVersion = gestionParaModificar.getVersion();

                if (version != oldVersion)
                {
                    throw new ClassModifiedException();
                }

                em.getTransaction().begin();
                GestionDeEscritura persistentGestionDeEscritura = em.find(GestionDeEscritura.class, gestionParaModificar.getIdGestion());
                Persona fkIdPersonaEscribanoOld = persistentGestionDeEscritura.getFkIdPersonaEscribano();
                Persona fkIdPersonaEscribanoNew = gestionParaModificar.getFkIdPersonaEscribano();
                List<Historial> historialListOld = persistentGestionDeEscritura.getHistorialList();
                List<Historial> historialListNew = gestionParaModificar.getHistorialList();
                List<Tramite> tramiteListOld = persistentGestionDeEscritura.getTramiteList();
                List<Tramite> tramiteListNew = gestionParaModificar.getTramiteList();
                List<String> illegalOrphanMessages = null;
                for (Historial historialListOldHistorial : historialListOld)
                {
                    if (!historialListNew.contains(historialListOldHistorial))
                    {
                        if (illegalOrphanMessages == null)
                        {
                            illegalOrphanMessages = new ArrayList<String>();
                        }
                        illegalOrphanMessages.add("You must retain Historial " + historialListOldHistorial + " since its fkIdGestion field is not nullable.");
                    }
                }
                if (illegalOrphanMessages != null)
                {
                    throw new IllegalOrphanException(illegalOrphanMessages);
                }
                if (fkIdPersonaEscribanoNew != null)
                {
                    fkIdPersonaEscribanoNew = em.getReference(fkIdPersonaEscribanoNew.getClass(), fkIdPersonaEscribanoNew.getIdPersona());
                    gestionParaModificar.setFkIdPersonaEscribano(fkIdPersonaEscribanoNew);
                }
                List<Historial> attachedHistorialListNew = new ArrayList<Historial>();
                for (Historial historialListNewHistorialToAttach : historialListNew)
                {
                    historialListNewHistorialToAttach = em.getReference(historialListNewHistorialToAttach.getClass(), historialListNewHistorialToAttach.getIdHistorial());
                    attachedHistorialListNew.add(historialListNewHistorialToAttach);
                }
                historialListNew = attachedHistorialListNew;
                gestionParaModificar.setHistorialList(historialListNew);
                List<Tramite> attachedTramiteListNew = new ArrayList<Tramite>();
                for (Tramite tramiteListNewTramiteToAttach : tramiteListNew)
                {
                    tramiteListNewTramiteToAttach = em.getReference(tramiteListNewTramiteToAttach.getClass(), tramiteListNewTramiteToAttach.getIdTramite());
                    attachedTramiteListNew.add(tramiteListNewTramiteToAttach);
                }
                tramiteListNew = attachedTramiteListNew;
                gestionParaModificar.setTramiteList(tramiteListNew);
                gestionParaModificar = em.merge(gestionParaModificar);
                if (fkIdPersonaEscribanoOld != null && !fkIdPersonaEscribanoOld.equals(fkIdPersonaEscribanoNew))
                {
                    fkIdPersonaEscribanoOld.getGestionDeEscrituraList().remove(gestionParaModificar);
                    fkIdPersonaEscribanoOld = em.merge(fkIdPersonaEscribanoOld);
                }
                if (fkIdPersonaEscribanoNew != null && !fkIdPersonaEscribanoNew.equals(fkIdPersonaEscribanoOld))
                {
                    fkIdPersonaEscribanoNew.getGestionDeEscrituraList().add(gestionParaModificar);
                    fkIdPersonaEscribanoNew = em.merge(fkIdPersonaEscribanoNew);
                }
                for (Historial historialListNewHistorial : historialListNew)
                {
                    if (!historialListOld.contains(historialListNewHistorial))
                    {
                        GestionDeEscritura oldFkIdGestionOfHistorialListNewHistorial = historialListNewHistorial.getFkIdGestion();
                        historialListNewHistorial.setFkIdGestion(gestionParaModificar);
                        historialListNewHistorial = em.merge(historialListNewHistorial);
                        if (oldFkIdGestionOfHistorialListNewHistorial != null && !oldFkIdGestionOfHistorialListNewHistorial.equals(gestionParaModificar))
                        {
                            oldFkIdGestionOfHistorialListNewHistorial.getHistorialList().remove(historialListNewHistorial);
                            oldFkIdGestionOfHistorialListNewHistorial = em.merge(oldFkIdGestionOfHistorialListNewHistorial);
                        }
                    }
                }
                for (Tramite tramiteListOldTramite : tramiteListOld)
                {
                    if (!tramiteListNew.contains(tramiteListOldTramite))
                    {
                        tramiteListOldTramite.setFkIdGestion(null);
                        tramiteListOldTramite = em.merge(tramiteListOldTramite);
                    }
                }
                for (Tramite tramiteListNewTramite : tramiteListNew)
                {
                    if (!tramiteListOld.contains(tramiteListNewTramite))
                    {
                        GestionDeEscritura oldFkIdGestionOfTramiteListNewTramite = tramiteListNewTramite.getFkIdGestion();
                        tramiteListNewTramite.setFkIdGestion(gestionParaModificar);
                        tramiteListNewTramite = em.merge(tramiteListNewTramite);
                        if (oldFkIdGestionOfTramiteListNewTramite != null && !oldFkIdGestionOfTramiteListNewTramite.equals(gestionParaModificar))
                        {
                            oldFkIdGestionOfTramiteListNewTramite.getTramiteList().remove(tramiteListNewTramite);
                            oldFkIdGestionOfTramiteListNewTramite = em.merge(oldFkIdGestionOfTramiteListNewTramite);
                        }
                    }
                }
                em.getTransaction().commit();
                modificado = Boolean.TRUE;
            }
        }
        finally
        {
            if (em != null)
            {
                em.close();
            }
        }
        return modificado;
    }

    /**
     * Permite elimianr una gestion de escritura de la persistencia.
     *
     * @param id El id de la gestion de escritura a eliminar.
     * @throws IllegalOrphanException
     * @throws NonexistentEntityException
     */
    public void destroy(Integer id) throws IllegalOrphanException, NonexistentEntityException
    {
        EntityManager em = null;
        try
        {
            em = getEntityManager();
            em.getTransaction().begin();
            GestionDeEscritura GestionDeEscritura;
            try
            {
                GestionDeEscritura = em.getReference(GestionDeEscritura.class, id);
                GestionDeEscritura.getIdGestion();
            }
            catch (EntityNotFoundException enfe)
            {
                throw new NonexistentEntityException("The GestionDeEscritura with id " + id + " no longer exists.", enfe);
            }
            List<String> illegalOrphanMessages = null;
            List<Historial> historialListOrphanCheck = GestionDeEscritura.getHistorialList();
            for (Historial historialListOrphanCheckHistorial : historialListOrphanCheck)
            {
                if (illegalOrphanMessages == null)
                {
                    illegalOrphanMessages = new ArrayList<String>();
                }
                illegalOrphanMessages.add("This GestionDeEscritura (" + GestionDeEscritura + ") cannot be destroyed since the Historial " + historialListOrphanCheckHistorial + " in its historialList field has a non-nullable fkIdGestion field.");
            }
            if (illegalOrphanMessages != null)
            {
                throw new IllegalOrphanException(illegalOrphanMessages);
            }
            Persona fkIdPersonaEscribano = GestionDeEscritura.getFkIdPersonaEscribano();
            if (fkIdPersonaEscribano != null)
            {
                fkIdPersonaEscribano.getGestionDeEscrituraList().remove(GestionDeEscritura);
                fkIdPersonaEscribano = em.merge(fkIdPersonaEscribano);
            }
            List<Tramite> tramiteList = GestionDeEscritura.getTramiteList();
            for (Tramite tramiteListTramite : tramiteList)
            {
                tramiteListTramite.setFkIdGestion(null);
                tramiteListTramite = em.merge(tramiteListTramite);
            }
            em.remove(GestionDeEscritura);
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

    public List<GestionDeEscritura> findGestionDeEscrituraEntities()
    {
        return findGestionDeEscrituraEntities(true, -1, -1);
    }

    public List<GestionDeEscritura> findGestionDeEscrituraEntities(int maxResults, int firstResult)
    {
        return findGestionDeEscrituraEntities(false, maxResults, firstResult);
    }

    private List<GestionDeEscritura> findGestionDeEscrituraEntities(boolean all, int maxResults, int firstResult)
    {
        EntityManager em = getEntityManager();
        try
        {
            Query q = em.createQuery("select object(o) from GestionDeEscritura as o");
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

    public GestionDeEscritura findGestionDeEscritura(Integer id)
    {
        EntityManager em = getEntityManager();
        try
        {
            return em.find(GestionDeEscritura.class, id);
        }
        finally
        {
            em.close();
        }
    }

    /**
     * Busca una gestion de escritura en base al numero de gestion.
     *
     * @param numeroGestionDeEscritura
     * @return
     */
    public GestionDeEscritura findGestionDeEscrituraPorNumero(Integer numeroGestionDeEscritura)
    {

        EntityManager em = getEntityManager();

        Query query = em.createNamedQuery("GestionDeEscritura.findByNumero");
        query.setParameter("numero", numeroGestionDeEscritura);

        List<GestionDeEscritura> listaGestiones = query.getResultList();

        if (!listaGestiones.isEmpty())
        {
            // existe la gestion indicada
            return listaGestiones.get(0);
        }

        GestionDeEscritura gestionVacia = new GestionDeEscritura();
        gestionVacia.setNumero(ConstantesPersistencia.VERSION_INICIAL);
        return gestionVacia;
    }

    public GestionDeEscritura findGestionDeEscrituraPorId(Integer idGestion)
    {

        EntityManager em = getEntityManager();

        Query query = em.createNamedQuery("GestionDeEscritura.findByIdGestion");
        query.setParameter("idGestion", idGestion);

        List<GestionDeEscritura> listaGestiones = query.getResultList();

        if (!listaGestiones.isEmpty())
        {
            // existe la gestion indicada
            return listaGestiones.get(0);
        }

        GestionDeEscritura gestionVacia = new GestionDeEscritura();
        gestionVacia.setNumero(ConstantesPersistencia.VERSION_INICIAL);
        return gestionVacia;
    }

    public int getGestionDeEscrituraCount()
    {
        EntityManager em = getEntityManager();
        try
        {
            Query q = em.createQuery("select count(o) from GestionDeEscritura as o");
            return ((Long) q.getSingleResult()).intValue();
        }
        finally
        {
            em.close();
        }
    }

    public int obtenerUltimoNumeroGestion()
    {
        EntityManager em = getEntityManager();
        int resultado = 0;
        try
        {
            Query q = em.createQuery("select max(numero) as ultimaGestion from GestionDeEscritura");

            if (q.getSingleResult() != null)
            {
                resultado = (int) q.getSingleResult();
            }

            return resultado;
        }
        finally
        {
            em.close();
        }
    }

    public List<GestionDeEscritura> findGestionesDeEscritura()
    {
        EntityManager em = getEntityManager();

        List<GestionDeEscritura> listaGestiones = null;
        Persona persona = null;
        Query query = em.createNamedQuery("GestionDeEscritura.findAll");
        listaGestiones = query.getResultList();

        return listaGestiones;
    }

    public boolean archivarGestiones(GestionDeEscritura pGestioneDeEscrituras) throws ClassModifiedException
    {

        Boolean flag = false; //Variable para saber el resultado de la transaccion
        int oldVersion = 0; //Variable para Version en memoria del Objeto
        int version = 0;    //Variable para Version en bd del Objeto       

        EntityManager em = getEntityManager();

        GestionDeEscritura persistentGestion = em.find(GestionDeEscritura.class, pGestioneDeEscrituras.getIdGestion());

        if (persistentGestion != null)
        {
            version = persistentGestion.getVersion(); // Version del Objeto en db
            oldVersion = pGestioneDeEscrituras.getVersion(); // Version del Objeto en memoria

            if (version != oldVersion) //Si son distintas "Alguien modifico el objeto"
            {
                throw new ClassModifiedException("La gestion indicada ha sido modificada por otro usuario");

            } else
            {
                try
                {
                    em.getTransaction().begin();
                    persistentGestion.setFkIdEstadoDeGestion(pGestioneDeEscrituras.getFkIdEstadoDeGestion());
                    persistentGestion.setObservaciones(pGestioneDeEscrituras.getObservaciones());
                    persistentGestion.setNumeroArchivo(pGestioneDeEscrituras.getNumeroBibliorato());
                    persistentGestion.setNumeroArchivo(pGestioneDeEscrituras.getIdGestion());

                    em.getTransaction().commit();
                    em.close();
                    flag = true;

                }
                catch (Exception e)
                {
                    System.out.println("Error de Persistencia: Usuario JpaController metodo: modificarUsuario");
                }
            }
        } else
        {
            throw new StaleObjectStateException(null, version);
        }
        return flag;
    }

    /**
     * Permite modificar una gestion de escritura.
     *
     * @param gestionParaModificar
     * @return
     * @throws ClassModifiedException
     */
    public boolean modificarGestionDeEscritura(GestionDeEscritura gestionParaModificar) throws ClassModifiedException
    {
        boolean resultado = false;

        int version;
        int oldVersion;

        EntityManager em = null;
        try
        {
            em = getEntityManager();

            GestionDeEscritura persistentGestion = em.find(GestionDeEscritura.class, gestionParaModificar.getIdGestion());

            if (persistentGestion != null)
            {
                version = persistentGestion.getVersion();
                oldVersion = gestionParaModificar.getVersion();

                if (version != oldVersion)
                {
                    throw new ClassModifiedException();
                }

                em.getTransaction().begin();

                persistentGestion.setEncabezado(gestionParaModificar.getEncabezado());
                persistentGestion.setObservaciones(gestionParaModificar.getObservaciones());
                persistentGestion.setFkIdEstadoDeGestion(gestionParaModificar.getFkIdEstadoDeGestion());
                //persistentGestion.setHistorialList(gestionParaModificar.getHistorialList());

                em.getTransaction().commit();
                resultado = true;
            }
        }
        finally
        {
            if (em != null)
            {
                em.close();
            }
        }

        return resultado;
    }

    /**
     * permite modifica el "estado" de una gestion de escritura.
     *
     * @param gestionParaModificar
     * @return
     * @throws ClassModifiedException
     */
    public boolean modificarEstadoDeGestionDeEscritura(GestionDeEscritura gestionParaModificar) throws ClassModifiedException
    {
        boolean resultado = false;

        int version;
        int oldVersion;

        EntityManager em = null;
        try
        {
            em = getEntityManager();

            GestionDeEscritura persistentGestion = em.find(GestionDeEscritura.class, gestionParaModificar.getIdGestion());

            if (persistentGestion != null)
            {
                version = persistentGestion.getVersion();
                oldVersion = gestionParaModificar.getVersion();

                if (version != oldVersion)
                {
                    throw new ClassModifiedException();
                }

                em.getTransaction().begin();

                persistentGestion.setFkIdEstadoDeGestion(gestionParaModificar.getFkIdEstadoDeGestion());

                em.getTransaction().commit();
                resultado = true;
            }
        }
        finally
        {
            if (em != null)
            {
                em.close();
            }
        }

        return resultado;
    }

    @Override
    public String getNombreJpa()
    {
        return this.getClass().getName();
    }
}
