/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.licensis.notaire.jpa;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Query;
import javax.persistence.EntityNotFoundException;
import javax.persistence.metamodel.ListAttribute;
import javax.transaction.UserTransaction;
import com.licensis.notaire.jpa.exceptions.ClassModifiedException;
import com.licensis.notaire.jpa.exceptions.NonexistentEntityException;
import com.licensis.notaire.jpa.interfaz.IPersistenciaJpa;
import com.licensis.notaire.negocio.DocumentoPresentado;
import com.licensis.notaire.negocio.Tramite;
import org.hibernate.StaleObjectStateException;

/**
 *
 * @author juanca
 */
public class DocumentoPresentadoJpaController implements Serializable, IPersistenciaJpa
{

    public DocumentoPresentadoJpaController(UserTransaction utx, EntityManagerFactory emf) {
        this.utx = utx;
        this.emf = emf;
    }
    private UserTransaction utx = null;
    private EntityManagerFactory emf = null;

    public EntityManager getEntityManager() {
        return emf.createEntityManager();
    }

    public boolean create(DocumentoPresentado documentoPresentado) throws NonexistentEntityException {

        boolean flag = false; //Variable para saber el resultado de la transaccion
        int version = 0;    //Variable para Version en bd del Objeto  

        Tramite fkIdTramite = documentoPresentado.getFkIdTramite();
        EntityManager em = null;

        try
        {
            em = getEntityManager();
            em.getTransaction().begin();

            flag = this.findDocumentoPresentado(documentoPresentado);

            if (flag)
            {
                throw new StaleObjectStateException(null, version);

            }
            else
            {
                if (fkIdTramite != null)
                {
                    fkIdTramite = em.getReference(fkIdTramite.getClass(), fkIdTramite.getIdTramite());
                    documentoPresentado.setFkIdTramite(fkIdTramite);
                }
                em.persist(documentoPresentado);
                if (fkIdTramite != null)
                {
                    fkIdTramite.getDocumentoPresentadoList().add(documentoPresentado);
                    fkIdTramite = em.merge(fkIdTramite);
                }
                em.getTransaction().commit();
                flag = true;
            }

        }
        finally
        {
            if (em != null)
            {
                em.close();
            }
        }
        return flag;



    }

    public boolean edit(DocumentoPresentado documentoPresentado) throws NonexistentEntityException, ClassModifiedException {

        Boolean flag = false; //Variable para saber el resultado de la transaccion
        int oldVersion = 0; //Variable para Version en memoria del Objeto
        int version = 0;    //Variable para Version en bd del Objeto       

        EntityManager em = getEntityManager();

        DocumentoPresentado persistDocumentoPresentado = em.find(DocumentoPresentado.class, documentoPresentado.getIdDocumentoPresentado());

        if (persistDocumentoPresentado != null)
        {
            version = persistDocumentoPresentado.getVersion(); // Version del Objeto en db
            oldVersion = documentoPresentado.getVersion(); // Version del Objeto en memoria

            if (version != oldVersion) //Si son distintas "Alguien modifico el objeto"
            {
                throw new ClassModifiedException("El documento indicado ha sido modificado por otro usuario");

            }
            else
            {
                try
                {
                    em.getTransaction().begin();
                    DocumentoPresentado persistentDocumentoPresentado = em.find(DocumentoPresentado.class, documentoPresentado.getIdDocumentoPresentado());
                    Tramite fkIdTramiteOld = persistentDocumentoPresentado.getFkIdTramite();
                    Tramite fkIdTramiteNew = documentoPresentado.getFkIdTramite();
                    if (fkIdTramiteNew != null)
                    {
                        fkIdTramiteNew = em.getReference(fkIdTramiteNew.getClass(), fkIdTramiteNew.getIdTramite());
                        documentoPresentado.setFkIdTramite(fkIdTramiteNew);
                    }
                    documentoPresentado = em.merge(documentoPresentado);
                    if (fkIdTramiteOld != null && !fkIdTramiteOld.equals(fkIdTramiteNew))
                    {
                        fkIdTramiteOld.getDocumentoPresentadoList().remove(documentoPresentado);
                        fkIdTramiteOld = em.merge(fkIdTramiteOld);
                    }
                    if (fkIdTramiteNew != null && !fkIdTramiteNew.equals(fkIdTramiteOld))
                    {
                        fkIdTramiteNew.getDocumentoPresentadoList().add(documentoPresentado);
                        fkIdTramiteNew = em.merge(fkIdTramiteNew);
                    }
                    em.getTransaction().commit();
                    flag = true;
                }
                catch (Exception ex)
                {
                    String msg = ex.getLocalizedMessage();
                    if (msg == null || msg.length() == 0)
                    {
                        Integer id = documentoPresentado.getIdDocumentoPresentado();
                        if (findDocumentoPresentado(id) == null)
                        {
                            throw new NonexistentEntityException("The documentoPresentado with id " + id + " no longer exists.");
                        }
                    }
                    throw ex;
                }

            }

        }
        return flag;
    }

    public void destroy(Integer id) throws NonexistentEntityException {
        EntityManager em = null;
        try
        {
            em = getEntityManager();
            em.getTransaction().begin();
            DocumentoPresentado documentoPresentado;
            try
            {
                documentoPresentado = em.getReference(DocumentoPresentado.class, id);
                documentoPresentado.getIdDocumentoPresentado();
            }
            catch (EntityNotFoundException enfe)
            {
                throw new NonexistentEntityException("The documentoPresentado with id " + id + " no longer exists.", enfe);
            }
            Tramite fkIdTramite = documentoPresentado.getFkIdTramite();
            if (fkIdTramite != null)
            {
                fkIdTramite.getDocumentoPresentadoList().remove(documentoPresentado);
                fkIdTramite = em.merge(fkIdTramite);
            }
            em.remove(documentoPresentado);
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

    public List<DocumentoPresentado> findDocumentoPresentadoEntities() {
        return findDocumentoPresentadoEntities(true, -1, -1);
    }

    public List<DocumentoPresentado> findDocumentoPresentadoEntities(int maxResults, int firstResult) {
        return findDocumentoPresentadoEntities(false, maxResults, firstResult);
    }

    private List<DocumentoPresentado> findDocumentoPresentadoEntities(boolean all, int maxResults, int firstResult) {
        EntityManager em = getEntityManager();
        try
        {
            Query q = em.createQuery("select object(o) from DocumentoPresentado as o");
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

    public DocumentoPresentado findDocumentoPresentado(Integer id) {
        EntityManager em = getEntityManager();
        try
        {
            return em.find(DocumentoPresentado.class, id);
        }
        finally
        {
            em.close();
        }
    }

    public int getDocumentoPresentadoCount() {
        EntityManager em = getEntityManager();
        try
        {
            Query q = em.createQuery("select count(o) from DocumentoPresentado as o");
            return ((Long) q.getSingleResult()).intValue();
        }
        finally
        {
            em.close();
        }
    }

    public List<DocumentoPresentado> findDocumentosPresentados() {
        EntityManager em = getEntityManager();

        List<DocumentoPresentado> listDocumentoPresentados = null;

        Query query = em.createNamedQuery("DocumentoPresentado.findAll");
        listDocumentoPresentados = query.getResultList();

        return listDocumentoPresentados;
    }

    public List<DocumentoPresentado> findDocumentosPorVencer() {
        EntityManager em = getEntityManager();
        List<DocumentoPresentado> listaDocumentoPorVencer = new ArrayList<>();

        Date fechaActual = Calendar.getInstance().getTime();

        Query query = em.createNamedQuery("DocumentoPresentado.findByFechaVencimiento");
        query.setParameter("fechaVencimiento", fechaActual);

        listaDocumentoPorVencer = query.getResultList();

        return listaDocumentoPorVencer;
    }

    private Boolean findDocumentoPresentado(DocumentoPresentado documentoPresentado) {

        boolean flag = false;
        int id_Tramite = documentoPresentado.getFkIdTramite().getIdTramite();
        String nombreDocumento = documentoPresentado.getNombre();
        
        EntityManager em = null;

        em = getEntityManager();
        em.getTransaction().begin();

        ArrayList<DocumentoPresentado> listaDocumentoPresentados = (ArrayList<DocumentoPresentado>) this.findDocumentosPresentados();

        for (int i = 0; i < listaDocumentoPresentados.size() && !flag; i++)
        {
            DocumentoPresentado documentoPresentadoPersist = listaDocumentoPresentados.get(i);
            int id_tramite_persist = documentoPresentadoPersist.getFkIdTramite().getIdTramite();
            String nombreDocumentoPersist = documentoPresentadoPersist.getNombre();

            if (id_Tramite == id_tramite_persist && nombreDocumento.equals(nombreDocumentoPersist))
            {
                flag = true;
            }

        }
        return flag;
    }

       public Boolean createDocumentoExterno(DocumentoPresentado documento) throws NonexistentEntityException, ClassModifiedException {
        EntityManager em = null;
        Boolean flag = false;
        em = getEntityManager();
        em.getTransaction().begin();
        try
        {
            DocumentoPresentado documentoPresentado = new DocumentoPresentado();

            documentoPresentado.setVersion(documento.getVersion());
            documentoPresentado.setNombre(documento.getNombre());
            documentoPresentado.setQuienEntrega(documento.getQuienEntrega());
       

            Tramite miTramite = documento.getFkIdTramite();
            documentoPresentado.setFkIdTramite(miTramite);

            em.persist(documentoPresentado);
            em.flush();
            em.getTransaction().commit();
            flag = true;
            
            this.edit(documentoPresentado);
        }
        finally
        {
            em.close();
        }
        return flag;
    }
    
    @Override
    public String getNombreJpa() {
        return this.getClass().getName();
    }
}
