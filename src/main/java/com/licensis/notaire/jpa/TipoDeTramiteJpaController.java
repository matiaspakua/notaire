/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.licensis.notaire.jpa;

import java.io.Serializable;
import javax.persistence.Query;
import javax.persistence.EntityNotFoundException;
import com.licensis.notaire.negocio.PlantillaPresupuesto;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.transaction.UserTransaction;
import com.licensis.notaire.jpa.exceptions.ClassEliminatedException;
import com.licensis.notaire.jpa.exceptions.ClassModifiedException;
import com.licensis.notaire.jpa.exceptions.IllegalOrphanException;
import com.licensis.notaire.jpa.exceptions.NonexistentEntityException;
import com.licensis.notaire.jpa.interfaz.IPersistenciaJpa;
import com.licensis.notaire.negocio.PlantillaTramite;
import com.licensis.notaire.negocio.TipoDeTramite;
import com.licensis.notaire.negocio.Tramite;

/**
 *
 * @author User
 */
public class TipoDeTramiteJpaController implements Serializable, IPersistenciaJpa
{

    public TipoDeTramiteJpaController(UserTransaction utx, EntityManagerFactory emf) {
        this.utx = utx;
        this.emf = emf;
    }
    private UserTransaction utx = null;
    private EntityManagerFactory emf = null;

    public EntityManager getEntityManager() {
        return emf.createEntityManager();
    }

    public int create(TipoDeTramite tipoDeTramite) {
        int oid = -1;
        if (tipoDeTramite.getPlantillaPresupuestoList() == null)
        {
            tipoDeTramite.setPlantillaPresupuestoList(new ArrayList<PlantillaPresupuesto>());
        }
        if (tipoDeTramite.getPlantillaTramiteList() == null)
        {
            tipoDeTramite.setPlantillaTramiteList(new ArrayList<PlantillaTramite>());
        }
        if (tipoDeTramite.getTramiteList() == null)
        {
            tipoDeTramite.setTramiteList(new ArrayList<Tramite>());
        }
        EntityManager em = null;
        try
        {
            em = getEntityManager();
            em.getTransaction().begin();
            List<PlantillaPresupuesto> attachedPlantillaPresupuestoList = new ArrayList<PlantillaPresupuesto>();
            for (PlantillaPresupuesto plantillaPresupuestoListPlantillaPresupuestoToAttach : tipoDeTramite.getPlantillaPresupuestoList())
            {
                plantillaPresupuestoListPlantillaPresupuestoToAttach = em.getReference(plantillaPresupuestoListPlantillaPresupuestoToAttach.getClass(), plantillaPresupuestoListPlantillaPresupuestoToAttach.getPlantillaPresupuestoPK());
                attachedPlantillaPresupuestoList.add(plantillaPresupuestoListPlantillaPresupuestoToAttach);
            }
            tipoDeTramite.setPlantillaPresupuestoList(attachedPlantillaPresupuestoList);
            List<PlantillaTramite> attachedPlantillaTramiteList = new ArrayList<PlantillaTramite>();
            for (PlantillaTramite plantillaTramiteListPlantillaTramiteToAttach : tipoDeTramite.getPlantillaTramiteList())
            {
                plantillaTramiteListPlantillaTramiteToAttach = em.getReference(plantillaTramiteListPlantillaTramiteToAttach.getClass(), plantillaTramiteListPlantillaTramiteToAttach.getPlantillaTramitePK());
                attachedPlantillaTramiteList.add(plantillaTramiteListPlantillaTramiteToAttach);
            }
            tipoDeTramite.setPlantillaTramiteList(attachedPlantillaTramiteList);
            List<Tramite> attachedTramiteList = new ArrayList<Tramite>();
            for (Tramite tramiteListTramiteToAttach : tipoDeTramite.getTramiteList())
            {
                tramiteListTramiteToAttach = em.getReference(tramiteListTramiteToAttach.getClass(), tramiteListTramiteToAttach.getIdTramite());
                attachedTramiteList.add(tramiteListTramiteToAttach);
            }
            tipoDeTramite.setTramiteList(attachedTramiteList);
            em.persist(tipoDeTramite);
            for (PlantillaPresupuesto plantillaPresupuestoListPlantillaPresupuesto : tipoDeTramite.getPlantillaPresupuestoList())
            {
                TipoDeTramite oldTipoDeTramiteOfPlantillaPresupuestoListPlantillaPresupuesto = plantillaPresupuestoListPlantillaPresupuesto.getTipoDeTramite();
                plantillaPresupuestoListPlantillaPresupuesto.setTipoDeTramite(tipoDeTramite);
                plantillaPresupuestoListPlantillaPresupuesto = em.merge(plantillaPresupuestoListPlantillaPresupuesto);
                if (oldTipoDeTramiteOfPlantillaPresupuestoListPlantillaPresupuesto != null)
                {
                    oldTipoDeTramiteOfPlantillaPresupuestoListPlantillaPresupuesto.getPlantillaPresupuestoList().remove(plantillaPresupuestoListPlantillaPresupuesto);
                    oldTipoDeTramiteOfPlantillaPresupuestoListPlantillaPresupuesto = em.merge(oldTipoDeTramiteOfPlantillaPresupuestoListPlantillaPresupuesto);
                }
            }
            for (PlantillaTramite plantillaTramiteListPlantillaTramite : tipoDeTramite.getPlantillaTramiteList())
            {
                TipoDeTramite oldTipoDeTramiteOfPlantillaTramiteListPlantillaTramite = plantillaTramiteListPlantillaTramite.getTipoDeTramite();
                plantillaTramiteListPlantillaTramite.setTipoDeTramite(tipoDeTramite);
                plantillaTramiteListPlantillaTramite = em.merge(plantillaTramiteListPlantillaTramite);
                if (oldTipoDeTramiteOfPlantillaTramiteListPlantillaTramite != null)
                {
                    oldTipoDeTramiteOfPlantillaTramiteListPlantillaTramite.getPlantillaTramiteList().remove(plantillaTramiteListPlantillaTramite);
                    oldTipoDeTramiteOfPlantillaTramiteListPlantillaTramite = em.merge(oldTipoDeTramiteOfPlantillaTramiteListPlantillaTramite);
                }
            }
            for (Tramite tramiteListTramite : tipoDeTramite.getTramiteList())
            {
                TipoDeTramite oldFkIdTipoTramiteOfTramiteListTramite = tramiteListTramite.getFkIdTipoTramite();
                tramiteListTramite.setFkIdTipoTramite(tipoDeTramite);
                tramiteListTramite = em.merge(tramiteListTramite);
                if (oldFkIdTipoTramiteOfTramiteListTramite != null)
                {
                    oldFkIdTipoTramiteOfTramiteListTramite.getTramiteList().remove(tramiteListTramite);
                    oldFkIdTipoTramiteOfTramiteListTramite = em.merge(oldFkIdTipoTramiteOfTramiteListTramite);
                }
            }
            em.getTransaction().commit();
            oid = tipoDeTramite.getIdTipoTramite();
        }
        finally
        {
            if (em != null)
            {
                em.close();
            }
        }
        return oid;
    }

    public Boolean edit(TipoDeTramite tipoDeTramite) throws ClassModifiedException, ClassEliminatedException, IllegalOrphanException, NonexistentEntityException {

        Boolean modificado = false;
        Integer version = -1;
        Integer oldVersion = -1;

        EntityManager em = getEntityManager();


        em.getTransaction().begin();

        TipoDeTramite tipoDeTramiteEncontrado = em.find(TipoDeTramite.class, tipoDeTramite.getIdTipoTramite());

        if (tipoDeTramiteEncontrado != null)
        {
            version = tipoDeTramiteEncontrado.getVersion(); // Version del Objeto en db
            oldVersion = tipoDeTramite.getVersion(); // Version del Objeto en memoria

            if (version != oldVersion) //Si son distintas "Alguien modifico el objeto"
            {
                if (em != null)
                {
                    em.close();
                }

                throw new ClassModifiedException();

            }
            else
            {

                tipoDeTramiteEncontrado.setHabilitado(tipoDeTramite.getHabilitado());
                tipoDeTramiteEncontrado.setNombre(tipoDeTramite.getNombre());
                tipoDeTramiteEncontrado.setSeArchiva(tipoDeTramite.getSeArchiva());
                tipoDeTramiteEncontrado.setSeInscribe(tipoDeTramite.getSeInscribe());
                tipoDeTramiteEncontrado.setAsociaInmuebles(tipoDeTramite.getAsociaInmuebles());
                tipoDeTramiteEncontrado.setObservaciones(tipoDeTramite.getObservaciones());

                em.getTransaction().commit();
                em.close();
                modificado = true;

            }
        }
        else
        {
            throw new ClassEliminatedException();
        }

        return modificado;
    }

    public Boolean destroy(Integer id) throws ClassEliminatedException, IllegalOrphanException, NonexistentEntityException {
        EntityManager em = null;
        Boolean eliminado = false;
        try
        {
            em = getEntityManager();
            em.getTransaction().begin();

            TipoDeTramite tipoDeTramite = em.find(TipoDeTramite.class, id);

            if (tipoDeTramite != null)
            {
                tipoDeTramite = em.getReference(TipoDeTramite.class, id);
                tipoDeTramite.getIdTipoTramite();

                List<String> illegalOrphanMessages = null;
                List<PlantillaPresupuesto> plantillaPresupuestoListOrphanCheck = tipoDeTramite.getPlantillaPresupuestoList();
                for (PlantillaPresupuesto plantillaPresupuestoListOrphanCheckPlantillaPresupuesto : plantillaPresupuestoListOrphanCheck)
                {
                    if (illegalOrphanMessages == null)
                    {
                        illegalOrphanMessages = new ArrayList<String>();
                    }
                    illegalOrphanMessages.add("This TipoDeTramite (" + tipoDeTramite + ") cannot be destroyed since the PlantillaPresupuesto " + plantillaPresupuestoListOrphanCheckPlantillaPresupuesto + " in its plantillaPresupuestoList field has a non-nullable tipoDeTramite field.");
                }
                List<PlantillaTramite> plantillaTramiteListOrphanCheck = tipoDeTramite.getPlantillaTramiteList();
                for (PlantillaTramite plantillaTramiteListOrphanCheckPlantillaTramite : plantillaTramiteListOrphanCheck)
                {
                    if (illegalOrphanMessages == null)
                    {
                        illegalOrphanMessages = new ArrayList<String>();
                    }
                    illegalOrphanMessages.add("This TipoDeTramite (" + tipoDeTramite + ") cannot be destroyed since the PlantillaTramite " + plantillaTramiteListOrphanCheckPlantillaTramite + " in its plantillaTramiteList field has a non-nullable tipoDeTramite field.");
                }
                List<Tramite> tramiteListOrphanCheck = tipoDeTramite.getTramiteList();
                for (Tramite tramiteListOrphanCheckTramite : tramiteListOrphanCheck)
                {
                    if (illegalOrphanMessages == null)
                    {
                        illegalOrphanMessages = new ArrayList<String>();
                    }
                    illegalOrphanMessages.add("This TipoDeTramite (" + tipoDeTramite + ") cannot be destroyed since the Tramite " + tramiteListOrphanCheckTramite + " in its tramiteList field has a non-nullable fkIdTipoTramite field.");
                }
                if (illegalOrphanMessages != null)
                {
                    throw new IllegalOrphanException(illegalOrphanMessages);
                }
                em.remove(tipoDeTramite);
                em.getTransaction().commit();
                eliminado = true;
            }
            else
            {
                throw new ClassEliminatedException();
            }
        }
        catch (EntityNotFoundException enfe)
        {
            throw new NonexistentEntityException("The concepto with id " + id + " no longer exists.", enfe);
        }
        finally
        {
            if (em != null)
            {
                em.close();
            }
        }
        return eliminado;
    }

    public List<TipoDeTramite> findTipoDeTramiteEntities() {
        return findTipoDeTramiteEntities(true, -1, -1);
    }

    public List<TipoDeTramite> findTipoDeTramiteEntities(int maxResults, int firstResult) {
        return findTipoDeTramiteEntities(false, maxResults, firstResult);
    }

    private List<TipoDeTramite> findTipoDeTramiteEntities(boolean all, int maxResults, int firstResult) {
        EntityManager em = getEntityManager();
        try
        {
            Query q = em.createQuery("select object(o) from TipoDeTramite as o");
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

    public List<TipoDeTramite> findTipoDeTramite(Integer id) {
        EntityManager em = getEntityManager();

        List<TipoDeTramite> misTiposDeTramite = null;

        try
        {
            // Nombre del @NamedQuery definido en nuestra clase
            Query q = em.createNamedQuery("TipoDeTramite.findByIdTipoTramite");

            //Le paso el nombre del parametro del query, y el valor a buscar.
            q.setParameter("idTipoTramite", id);

            misTiposDeTramite = (List<TipoDeTramite>) q.getResultList();

            if (misTiposDeTramite != null)
            {

                misTiposDeTramite.get(0).setPlantillaPresupuestoList(new ArrayList<PlantillaPresupuesto>());
                misTiposDeTramite.get(0).setPlantillaTramiteList(new ArrayList<PlantillaTramite>());
                misTiposDeTramite.get(0).setTramiteList(new ArrayList<Tramite>());
            }

            return misTiposDeTramite;

        }
        finally
        {
            em.close();
        }
    }

    public List<TipoDeTramite> findTipoDeTramite(String nombre) {
        EntityManager em = getEntityManager();

        List<TipoDeTramite> misTiposDeTramite = null;

        try
        {
            // Nombre del @NamedQuery definido en nuestra clase
            Query q = em.createNamedQuery("TipoDeTramite.findByNombre");

            //Le paso el nombre del parametro del query, y el valor a buscar.
            q.setParameter("nombre", nombre);

            misTiposDeTramite = (List<TipoDeTramite>) q.getResultList();

            return misTiposDeTramite;

        }
        finally
        {
            em.close();
        }
    }

    public int getTipoDeTramiteCount() {
        EntityManager em = getEntityManager();
        try
        {
            Query q = em.createQuery("select count(o) from TipoDeTramite as o");
            return ((Long) q.getSingleResult()).intValue();
        }
        finally
        {
            em.close();
        }
    }

    @Override
    public String getNombreJpa() {
        return this.getClass().getName();
    }
}
