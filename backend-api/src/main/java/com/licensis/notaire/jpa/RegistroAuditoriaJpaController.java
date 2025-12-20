/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.licensis.notaire.jpa;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.*;
import javax.transaction.UserTransaction;
import com.licensis.notaire.servicios.AdministradorJpa;
import com.licensis.notaire.jpa.exceptions.NonexistentEntityException;
import com.licensis.notaire.jpa.interfaz.IPersistenciaJpa;
import com.licensis.notaire.negocio.RegistroAuditoria;
import com.licensis.notaire.negocio.Usuario;

/**
 *
 * @author juanca
 */
public class RegistroAuditoriaJpaController implements Serializable, IPersistenciaJpa {

    private RegistroAuditoriaJpaController(UserTransaction utx, EntityManagerFactory emf) {
        this.utx = utx;
        this.emf = emf;
    }

    private UserTransaction utx = null;
    private EntityManagerFactory emf = null;
    private static RegistroAuditoriaJpaController instancia = null;

    public EntityManager getEntityManager() {
        return emf.createEntityManager();
    }

    public boolean create(RegistroAuditoria registroAuditoria) {
        boolean resultado = false;

        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            Usuario fkIdUsuario = registroAuditoria.getFkIdUsuario();
            if (fkIdUsuario != null) {
                fkIdUsuario = em.getReference(fkIdUsuario.getClass(), fkIdUsuario.getIdUsuario());
                registroAuditoria.setFkIdUsuario(fkIdUsuario);
            }
            em.persist(registroAuditoria);
            if (fkIdUsuario != null) {
                fkIdUsuario.getRegistroAuditoriaList().add(registroAuditoria);
                fkIdUsuario = em.merge(fkIdUsuario);
            }
            em.getTransaction().commit();
            resultado = true;
        } finally {
            if (em != null) {
                em.close();
            }
        }
        return resultado;
    }

    public static RegistroAuditoriaJpaController getInstancia() {

        EntityManagerFactory emf = AdministradorJpa.getEmf();

        if (instancia == null) {
            instancia = new RegistroAuditoriaJpaController(null, emf);
        }
        return instancia;
    }

    public void edit(RegistroAuditoria registroAuditoria) throws NonexistentEntityException, Exception {
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            RegistroAuditoria persistentRegistroAuditoria = em.find(RegistroAuditoria.class,
                    registroAuditoria.getIdRegistroAuditoria());
            Usuario fkIdUsuarioOld = persistentRegistroAuditoria.getFkIdUsuario();
            Usuario fkIdUsuarioNew = registroAuditoria.getFkIdUsuario();
            if (fkIdUsuarioNew != null) {
                fkIdUsuarioNew = em.getReference(fkIdUsuarioNew.getClass(), fkIdUsuarioNew.getIdUsuario());
                registroAuditoria.setFkIdUsuario(fkIdUsuarioNew);
            }
            registroAuditoria = em.merge(registroAuditoria);
            if (fkIdUsuarioOld != null && !fkIdUsuarioOld.equals(fkIdUsuarioNew)) {
                fkIdUsuarioOld.getRegistroAuditoriaList().remove(registroAuditoria);
                fkIdUsuarioOld = em.merge(fkIdUsuarioOld);
            }
            if (fkIdUsuarioNew != null && !fkIdUsuarioNew.equals(fkIdUsuarioOld)) {
                fkIdUsuarioNew.getRegistroAuditoriaList().add(registroAuditoria);
                fkIdUsuarioNew = em.merge(fkIdUsuarioNew);
            }
            em.getTransaction().commit();
        } catch (Exception ex) {
            String msg = ex.getLocalizedMessage();
            if (msg == null || msg.length() == 0) {
                Integer id = registroAuditoria.getIdRegistroAuditoria();
                if (findRegistroAuditoria(id) == null) {
                    throw new NonexistentEntityException("The registroAuditoria with id " + id + " no longer exists.");
                }
            }
            throw ex;
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public void destroy(Integer id) throws NonexistentEntityException {
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            RegistroAuditoria registroAuditoria;
            try {
                registroAuditoria = em.getReference(RegistroAuditoria.class, id);
                registroAuditoria.getIdRegistroAuditoria();
            } catch (EntityNotFoundException enfe) {
                throw new NonexistentEntityException("The registroAuditoria with id " + id + " no longer exists.",
                        enfe);
            }
            Usuario fkIdUsuario = registroAuditoria.getFkIdUsuario();
            if (fkIdUsuario != null) {
                fkIdUsuario.getRegistroAuditoriaList().remove(registroAuditoria);
                fkIdUsuario = em.merge(fkIdUsuario);
            }
            em.remove(registroAuditoria);
            em.getTransaction().commit();
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public List<RegistroAuditoria> findRegistroAuditoriaEntities() {
        return findRegistroAuditoriaEntities(true, -1, -1);
    }

    public List<RegistroAuditoria> findRegistroAuditoriaEntities(int maxResults, int firstResult) {
        return findRegistroAuditoriaEntities(false, maxResults, firstResult);
    }

    private List<RegistroAuditoria> findRegistroAuditoriaEntities(boolean all, int maxResults, int firstResult) {
        EntityManager em = getEntityManager();
        try {
            Query q = em.createQuery("select object(o) from RegistroAuditoria as o");
            if (!all) {
                q.setMaxResults(maxResults);
                q.setFirstResult(firstResult);
            }
            return q.getResultList();
        } finally {
            em.close();
        }
    }

    public RegistroAuditoria findRegistroAuditoria(Integer id) {
        EntityManager em = getEntityManager();
        try {
            return em.find(RegistroAuditoria.class, id);
        } finally {
            em.close();
        }
    }

    public int getRegistroAuditoriaCount() {
        EntityManager em = getEntityManager();
        try {
            Query q = em.createQuery("select count(o) from RegistroAuditoria as o");
            return ((Long) q.getSingleResult()).intValue();
        } finally {
            em.close();
        }
    }

    public ArrayList<RegistroAuditoria> buscarRegistroAuditoriasUsuario(Usuario miUsuario) {
        List<RegistroAuditoria> listaAuditoria = null;
        ArrayList<RegistroAuditoria> usuarioListaAuditoria = new ArrayList<RegistroAuditoria>();
        Boolean flag = false;
        try {

            EntityManager em = getEntityManager();

            Query query = em.createNamedQuery("RegistroAuditoria.findAll");

            listaAuditoria = query.getResultList();

            // Filtro el resultado de la busqueda con el Usuario deseado
            for (int i = 0; i < listaAuditoria.size(); i++) {
                if (miUsuario.getIdUsuario().equals(listaAuditoria.get(i).getFkIdUsuario().getIdUsuario())) {
                    flag = true;
                    usuarioListaAuditoria.add(listaAuditoria.get(i));
                }
            }

            if (!flag) {
                usuarioListaAuditoria = null;
            }

        } catch (Exception e) {
            System.out.println("Error Metodo : getDtoRegistroAuditoria");
        }
        return usuarioListaAuditoria;

    }

    @Override
    public String getNombreJpa() {
        return this.getClass().getName();
    }
}
