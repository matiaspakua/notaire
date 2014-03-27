/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.licensis.notaire.jpa;

import java.io.Serializable;
import java.util.List;
import javax.persistence.*;
import javax.transaction.UserTransaction;
import com.licensis.notaire.jpa.exceptions.ClassEliminatedException;
import com.licensis.notaire.jpa.exceptions.NonexistentEntityException;
import com.licensis.notaire.jpa.exceptions.PreexistingEntityException;
import com.licensis.notaire.jpa.interfaz.IPersistenciaJpa;
import com.licensis.notaire.negocio.Concepto;
import com.licensis.notaire.negocio.PlantillaPresupuesto;
import com.licensis.notaire.negocio.PlantillaPresupuestoPK;
import com.licensis.notaire.negocio.TipoDeTramite;

/**
 *
 * @author User
 */
public class PlantillaPresupuestoJpaController implements Serializable, IPersistenciaJpa
{

    public PlantillaPresupuestoJpaController(UserTransaction utx, EntityManagerFactory emf) {
        this.utx = utx;
        this.emf = emf;
    }
    private UserTransaction utx = null;
    private EntityManagerFactory emf = null;

    public EntityManager getEntityManager() {
        return emf.createEntityManager();
    }

    public Boolean create(PlantillaPresupuesto plantillaPresupuesto) throws PreexistingEntityException {
        Boolean creada = false;

        if (plantillaPresupuesto.getPlantillaPresupuestoPK() == null)
        {
            plantillaPresupuesto.setPlantillaPresupuestoPK(new PlantillaPresupuestoPK());
        }
        plantillaPresupuesto.getPlantillaPresupuestoPK().setFkIdConcepto(plantillaPresupuesto.getConcepto().getIdConcepto());
        plantillaPresupuesto.getPlantillaPresupuestoPK().setFkIdTipoTramite(plantillaPresupuesto.getTipoDeTramite().getIdTipoTramite());
        EntityManager em = null;
        try
        {
            em = getEntityManager();
            em.getTransaction().begin();
            TipoDeTramite tipoDeTramite = plantillaPresupuesto.getTipoDeTramite();
            if (tipoDeTramite != null)
            {
                tipoDeTramite = em.getReference(tipoDeTramite.getClass(), tipoDeTramite.getIdTipoTramite());
                plantillaPresupuesto.setTipoDeTramite(tipoDeTramite);
            }
            Concepto concepto = plantillaPresupuesto.getConcepto();
            if (concepto != null)
            {
                concepto = em.getReference(concepto.getClass(), concepto.getIdConcepto());
                plantillaPresupuesto.setConcepto(concepto);
            }
            em.persist(plantillaPresupuesto);
            if (tipoDeTramite != null)
            {
                tipoDeTramite.getPlantillaPresupuestoList().add(plantillaPresupuesto);
                tipoDeTramite = em.merge(tipoDeTramite);
            }
            if (concepto != null)
            {
                concepto.getPlantillaPresupuestoList().add(plantillaPresupuesto);
                concepto = em.merge(concepto);
            }
            em.getTransaction().commit();
            creada = true;
        }
        catch (Exception ex)
        {
            if (findPlantillaPresupuesto(plantillaPresupuesto.getPlantillaPresupuestoPK()) != null)
            {
                throw new PreexistingEntityException("PlantillaPresupuesto " + plantillaPresupuesto + " already exists.", ex);
            }
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

    public void edit(PlantillaPresupuesto plantillaPresupuesto) throws NonexistentEntityException, Exception {
        plantillaPresupuesto.getPlantillaPresupuestoPK().setFkIdConcepto(plantillaPresupuesto.getConcepto().getIdConcepto());
        plantillaPresupuesto.getPlantillaPresupuestoPK().setFkIdTipoTramite(plantillaPresupuesto.getTipoDeTramite().getIdTipoTramite());
        EntityManager em = null;
        try
        {
            em = getEntityManager();
            em.getTransaction().begin();
            PlantillaPresupuesto persistentPlantillaPresupuesto = em.find(PlantillaPresupuesto.class, plantillaPresupuesto.getPlantillaPresupuestoPK());
            TipoDeTramite tipoDeTramiteOld = persistentPlantillaPresupuesto.getTipoDeTramite();
            TipoDeTramite tipoDeTramiteNew = plantillaPresupuesto.getTipoDeTramite();
            Concepto conceptoOld = persistentPlantillaPresupuesto.getConcepto();
            Concepto conceptoNew = plantillaPresupuesto.getConcepto();
            if (tipoDeTramiteNew != null)
            {
                tipoDeTramiteNew = em.getReference(tipoDeTramiteNew.getClass(), tipoDeTramiteNew.getIdTipoTramite());
                plantillaPresupuesto.setTipoDeTramite(tipoDeTramiteNew);
            }
            if (conceptoNew != null)
            {
                conceptoNew = em.getReference(conceptoNew.getClass(), conceptoNew.getIdConcepto());
                plantillaPresupuesto.setConcepto(conceptoNew);
            }
            plantillaPresupuesto = em.merge(plantillaPresupuesto);
            if (tipoDeTramiteOld != null && !tipoDeTramiteOld.equals(tipoDeTramiteNew))
            {
                tipoDeTramiteOld.getPlantillaPresupuestoList().remove(plantillaPresupuesto);
                tipoDeTramiteOld = em.merge(tipoDeTramiteOld);
            }
            if (tipoDeTramiteNew != null && !tipoDeTramiteNew.equals(tipoDeTramiteOld))
            {
                tipoDeTramiteNew.getPlantillaPresupuestoList().add(plantillaPresupuesto);
                tipoDeTramiteNew = em.merge(tipoDeTramiteNew);
            }
            if (conceptoOld != null && !conceptoOld.equals(conceptoNew))
            {
                conceptoOld.getPlantillaPresupuestoList().remove(plantillaPresupuesto);
                conceptoOld = em.merge(conceptoOld);
            }
            if (conceptoNew != null && !conceptoNew.equals(conceptoOld))
            {
                conceptoNew.getPlantillaPresupuestoList().add(plantillaPresupuesto);
                conceptoNew = em.merge(conceptoNew);
            }
            em.getTransaction().commit();
        }
        catch (Exception ex)
        {
            String msg = ex.getLocalizedMessage();
            if (msg == null || msg.length() == 0)
            {
                PlantillaPresupuestoPK id = plantillaPresupuesto.getPlantillaPresupuestoPK();
                if (findPlantillaPresupuesto(id) == null)
                {
                    throw new NonexistentEntityException("The plantillaPresupuesto with id " + id + " no longer exists.");
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

    public void destroy(PlantillaPresupuestoPK id) throws NonexistentEntityException {
        EntityManager em = null;
        try
        {
            em = getEntityManager();
            em.getTransaction().begin();
            PlantillaPresupuesto plantillaPresupuesto;
            try
            {
                plantillaPresupuesto = em.getReference(PlantillaPresupuesto.class, id);
                plantillaPresupuesto.getPlantillaPresupuestoPK();
            }
            catch (EntityNotFoundException enfe)
            {
                throw new NonexistentEntityException("The plantillaPresupuesto with id " + id + " no longer exists.", enfe);
            }
            TipoDeTramite tipoDeTramite = plantillaPresupuesto.getTipoDeTramite();
            if (tipoDeTramite != null)
            {
                tipoDeTramite.getPlantillaPresupuestoList().remove(plantillaPresupuesto);
                tipoDeTramite = em.merge(tipoDeTramite);
            }
            Concepto concepto = plantillaPresupuesto.getConcepto();
            if (concepto != null)
            {
                concepto.getPlantillaPresupuestoList().remove(plantillaPresupuesto);
                concepto = em.merge(concepto);
            }
            em.remove(plantillaPresupuesto);
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

    public List<PlantillaPresupuesto> findPlantillaPresupuestoEntities() {
        return findPlantillaPresupuestoEntities(true, -1, -1);
    }

    public List<PlantillaPresupuesto> findPlantillaPresupuestoEntities(int maxResults, int firstResult) {
        return findPlantillaPresupuestoEntities(false, maxResults, firstResult);
    }

    private List<PlantillaPresupuesto> findPlantillaPresupuestoEntities(boolean all, int maxResults, int firstResult) {
        EntityManager em = getEntityManager();
        try
        {
            Query q = em.createQuery("select object(o) from PlantillaPresupuesto as o");
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

    public PlantillaPresupuesto findPlantillaPresupuesto(PlantillaPresupuestoPK id) {
        EntityManager em = getEntityManager();
        try
        {
            return em.find(PlantillaPresupuesto.class, id);
        }
        finally
        {
            em.close();
        }
    }

    public int getPlantillaPresupuestoCount() {
        EntityManager em = getEntityManager();
        try
        {
            Query q = em.createQuery("select count(o) from PlantillaPresupuesto as o");
            return ((Long) q.getSingleResult()).intValue();
        }
        finally
        {
            em.close();
        }
    }

    public List<PlantillaPresupuesto> findPlantillasDePresupuesto(int idTipoTramite) {
        EntityManager em = getEntityManager();

        List<PlantillaPresupuesto> misPlantillas = null;

        try
        {
            // Nombre del @NamedQuery definido en nuestra clase
            Query q = em.createNamedQuery("PlantillaPresupuesto.findByFkIdTipoTramite");

            //Le paso el nombre del parametro del query, y el valor a buscar.
            q.setParameter("fkIdTipoTramite", idTipoTramite);

            misPlantillas = (List<PlantillaPresupuesto>) q.getResultList();

            return misPlantillas;

        }
        finally
        {
            em.close();
        }
    }

    public Boolean eliminarPlantillaPresupuesto(PlantillaPresupuesto miPlantilla) throws ClassEliminatedException {

        Boolean eliminada = false;
        int deleted = 0;

        EntityManager em = this.getEntityManager();
        EntityTransaction tx = null;

        // Abro una nueva transaccion
        tx = em.getTransaction();
        tx.begin();

        PlantillaPresupuesto miPlantillaPresupuesto = this.findPlantillaPresupuesto(miPlantilla.getPlantillaPresupuestoPK());

        if (miPlantillaPresupuesto != null)
        {

            // Creo el query
            Query query = em.createQuery("DELETE FROM PlantillaPresupuesto p WHERE p.plantillaPresupuestoPK.fkIdTipoTramite = ?1 AND  p.plantillaPresupuestoPK.fkIdConcepto = ?2");

            //Seteo el parametro 1 y 2
            query.setParameter(1, miPlantilla.getPlantillaPresupuestoPK().getFkIdTipoTramite());
            query.setParameter(2, miPlantilla.getPlantillaPresupuestoPK().getFkIdConcepto());

            // Ejecuto el query
            deleted = query.executeUpdate();

            // Hago commit
            tx.commit();

            if (deleted > 0)
            {
                eliminada = true;
            }
        }
        else
        {
            throw new ClassEliminatedException();
        }

        em.close();

        return eliminada;
    }

    @Override
    public String getNombreJpa() {
        return this.getClass().getName();
    }
}
