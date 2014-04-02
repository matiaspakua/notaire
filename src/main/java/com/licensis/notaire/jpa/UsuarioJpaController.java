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
import com.licensis.notaire.jpa.exceptions.ClassEliminatedException;
import com.licensis.notaire.jpa.exceptions.ClassModifiedException;
import com.licensis.notaire.jpa.exceptions.IllegalOrphanException;
import com.licensis.notaire.jpa.exceptions.NonexistentEntityException;
import com.licensis.notaire.jpa.interfaz.IPersistenciaJpa;
import com.licensis.notaire.negocio.Persona;
import com.licensis.notaire.negocio.RegistroAuditoria;
import com.licensis.notaire.negocio.Usuario;

/**
 *
 * @author juanca
 */
public class UsuarioJpaController implements Serializable, IPersistenciaJpa
{

    private UserTransaction utx = null;
    private EntityManagerFactory emf = Persistence.createEntityManagerFactory("notairePU");
    private static UsuarioJpaController instancia = null;

    private UsuarioJpaController(UserTransaction utx, EntityManagerFactory emf)
    {
        this.utx = utx;
        this.emf = emf;
    }

    public EntityManager getEntityManager()
    {
        return emf.createEntityManager();
    }

    public static UsuarioJpaController getInstancia()
    {

        EntityManagerFactory emf = Persistence.createEntityManagerFactory("notairePU");

        if (instancia == null)
        {
            instancia = new UsuarioJpaController(null, emf);
        }
        return instancia;

    }

    public void create(Usuario usuarios)
    {
        if (usuarios.getRegistroAuditoriaList() == null)
        {
            usuarios.setRegistroAuditoriaList(new ArrayList<RegistroAuditoria>());
        }
        EntityManager em = null;
        try
        {
            em = getEntityManager();
            em.getTransaction().begin();
            Persona fkIdPersona = usuarios.getFkIdPersona();
            if (fkIdPersona != null)
            {
                fkIdPersona = em.getReference(fkIdPersona.getClass(), fkIdPersona.getIdPersona());
                usuarios.setFkIdPersona(fkIdPersona);
            }
            List<RegistroAuditoria> attachedRegistroAuditoriaList = new ArrayList<RegistroAuditoria>();
            for (RegistroAuditoria registroAuditoriaListRegistroAuditoriaToAttach : usuarios.getRegistroAuditoriaList())
            {
                registroAuditoriaListRegistroAuditoriaToAttach = em.getReference(registroAuditoriaListRegistroAuditoriaToAttach.getClass(), registroAuditoriaListRegistroAuditoriaToAttach.getIdRegistroAuditoria());
                attachedRegistroAuditoriaList.add(registroAuditoriaListRegistroAuditoriaToAttach);
            }
            usuarios.setRegistroAuditoriaList(attachedRegistroAuditoriaList);
            em.persist(usuarios);
            if (fkIdPersona != null)
            {
                fkIdPersona.getUsuariosList().add(usuarios);
                fkIdPersona = em.merge(fkIdPersona);
            }
            for (RegistroAuditoria registroAuditoriaListRegistroAuditoria : usuarios.getRegistroAuditoriaList())
            {
                Usuario oldFkIdUsuarioOfRegistroAuditoriaListRegistroAuditoria = registroAuditoriaListRegistroAuditoria.getFkIdUsuario();
                registroAuditoriaListRegistroAuditoria.setFkIdUsuario(usuarios);
                registroAuditoriaListRegistroAuditoria = em.merge(registroAuditoriaListRegistroAuditoria);
                if (oldFkIdUsuarioOfRegistroAuditoriaListRegistroAuditoria != null)
                {
                    oldFkIdUsuarioOfRegistroAuditoriaListRegistroAuditoria.getRegistroAuditoriaList().remove(registroAuditoriaListRegistroAuditoria);
                    oldFkIdUsuarioOfRegistroAuditoriaListRegistroAuditoria = em.merge(oldFkIdUsuarioOfRegistroAuditoriaListRegistroAuditoria);
                }
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

    public void edit(Usuario usuarios) throws IllegalOrphanException, NonexistentEntityException, Exception
    {
        EntityManager em = null;
        try
        {
            em = getEntityManager();
            em.getTransaction().begin();
            Usuario persistentUsuarios = em.find(Usuario.class, usuarios.getIdUsuario());
            Persona fkIdPersonaOld = persistentUsuarios.getFkIdPersona();
            Persona fkIdPersonaNew = usuarios.getFkIdPersona();
            List<RegistroAuditoria> registroAuditoriaListOld = persistentUsuarios.getRegistroAuditoriaList();
            List<RegistroAuditoria> registroAuditoriaListNew = usuarios.getRegistroAuditoriaList();
            List<String> illegalOrphanMessages = null;
            for (RegistroAuditoria registroAuditoriaListOldRegistroAuditoria : registroAuditoriaListOld)
            {
                if (!registroAuditoriaListNew.contains(registroAuditoriaListOldRegistroAuditoria))
                {
                    if (illegalOrphanMessages == null)
                    {
                        illegalOrphanMessages = new ArrayList<String>();
                    }
                    illegalOrphanMessages.add("You must retain RegistroAuditoria " + registroAuditoriaListOldRegistroAuditoria + " since its fkIdUsuario field is not nullable.");
                }
            }
            if (illegalOrphanMessages != null)
            {
                throw new IllegalOrphanException(illegalOrphanMessages);
            }
            if (fkIdPersonaNew != null)
            {
                fkIdPersonaNew = em.getReference(fkIdPersonaNew.getClass(), fkIdPersonaNew.getIdPersona());
                usuarios.setFkIdPersona(fkIdPersonaNew);
            }
            List<RegistroAuditoria> attachedRegistroAuditoriaListNew = new ArrayList<RegistroAuditoria>();
            for (RegistroAuditoria registroAuditoriaListNewRegistroAuditoriaToAttach : registroAuditoriaListNew)
            {
                registroAuditoriaListNewRegistroAuditoriaToAttach = em.getReference(registroAuditoriaListNewRegistroAuditoriaToAttach.getClass(), registroAuditoriaListNewRegistroAuditoriaToAttach.getIdRegistroAuditoria());
                attachedRegistroAuditoriaListNew.add(registroAuditoriaListNewRegistroAuditoriaToAttach);
            }
            registroAuditoriaListNew = attachedRegistroAuditoriaListNew;
            usuarios.setRegistroAuditoriaList(registroAuditoriaListNew);
            usuarios = em.merge(usuarios);
            if (fkIdPersonaOld != null && !fkIdPersonaOld.equals(fkIdPersonaNew))
            {
                fkIdPersonaOld.getUsuariosList().remove(usuarios);
                fkIdPersonaOld = em.merge(fkIdPersonaOld);
            }
            if (fkIdPersonaNew != null && !fkIdPersonaNew.equals(fkIdPersonaOld))
            {
                fkIdPersonaNew.getUsuariosList().add(usuarios);
                fkIdPersonaNew = em.merge(fkIdPersonaNew);
            }
            for (RegistroAuditoria registroAuditoriaListNewRegistroAuditoria : registroAuditoriaListNew)
            {
                if (!registroAuditoriaListOld.contains(registroAuditoriaListNewRegistroAuditoria))
                {
                    Usuario oldFkIdUsuarioOfRegistroAuditoriaListNewRegistroAuditoria = registroAuditoriaListNewRegistroAuditoria.getFkIdUsuario();
                    registroAuditoriaListNewRegistroAuditoria.setFkIdUsuario(usuarios);
                    registroAuditoriaListNewRegistroAuditoria = em.merge(registroAuditoriaListNewRegistroAuditoria);
                    if (oldFkIdUsuarioOfRegistroAuditoriaListNewRegistroAuditoria != null && !oldFkIdUsuarioOfRegistroAuditoriaListNewRegistroAuditoria.equals(usuarios))
                    {
                        oldFkIdUsuarioOfRegistroAuditoriaListNewRegistroAuditoria.getRegistroAuditoriaList().remove(registroAuditoriaListNewRegistroAuditoria);
                        oldFkIdUsuarioOfRegistroAuditoriaListNewRegistroAuditoria = em.merge(oldFkIdUsuarioOfRegistroAuditoriaListNewRegistroAuditoria);
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
                Integer id = usuarios.getIdUsuario();
                if (findUsuarios(id) == null)
                {
                    throw new NonexistentEntityException("The usuarios with id " + id + " no longer exists.");
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
            Usuario usuarios;
            try
            {
                usuarios = em.getReference(Usuario.class, id);
                usuarios.getIdUsuario();
            }
            catch (EntityNotFoundException enfe)
            {
                throw new NonexistentEntityException("The usuarios with id " + id + " no longer exists.", enfe);
            }
            List<String> illegalOrphanMessages = null;
            List<RegistroAuditoria> registroAuditoriaListOrphanCheck = usuarios.getRegistroAuditoriaList();
            for (RegistroAuditoria registroAuditoriaListOrphanCheckRegistroAuditoria : registroAuditoriaListOrphanCheck)
            {
                if (illegalOrphanMessages == null)
                {
                    illegalOrphanMessages = new ArrayList<String>();
                }
                illegalOrphanMessages.add("This Usuarios (" + usuarios + ") cannot be destroyed since the RegistroAuditoria " + registroAuditoriaListOrphanCheckRegistroAuditoria + " in its registroAuditoriaList field has a non-nullable fkIdUsuario field.");
            }
            if (illegalOrphanMessages != null)
            {
                throw new IllegalOrphanException(illegalOrphanMessages);
            }
            Persona fkIdPersona = usuarios.getFkIdPersona();
            if (fkIdPersona != null)
            {
                fkIdPersona.getUsuariosList().remove(usuarios);
                fkIdPersona = em.merge(fkIdPersona);
            }
            em.remove(usuarios);
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

    public List<Usuario> findUsuariosEntities()
    {
        return findUsuariosEntities(true, -1, -1);
    }

    public List<Usuario> findUsuariosEntities(int maxResults, int firstResult)
    {
        return findUsuariosEntities(false, maxResults, firstResult);
    }

    private List<Usuario> findUsuariosEntities(boolean all, int maxResults, int firstResult)
    {
        EntityManager em = getEntityManager();
        try
        {
            Query q = em.createQuery("select object(o) from Usuarios as o");
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

    public Usuario findUsuarios(Integer id)
    {
        EntityManager em = getEntityManager();
        try
        {
            return em.find(Usuario.class, id);
        }
        finally
        {
            em.close();
        }
    }

    public int getUsuariosCount()
    {
        EntityManager em = getEntityManager();
        try
        {
            Query q = em.createQuery("select count(o) from Usuarios as o");
            return ((Long) q.getSingleResult()).intValue();
        }
        finally
        {
            em.close();
        }
    }

    public List<Usuario> buscarUsuarios()
    {
        List<Usuario> listaUsuarios = null;

        EntityManager em = getEntityManager();

        Query query = em.createNamedQuery("Usuario.findAll");

        listaUsuarios = query.getResultList();

        return listaUsuarios;
    }

    public Boolean modificarUsuario(Usuario pUsuario) throws ClassModifiedException, ClassEliminatedException
    {

        Boolean flag = false; //Variable para saber el resultado de la transaccion
        int oldVersion = 0; //Variable para Version en memoria del Objeto
        int version = 0;    //Variable para Version en bd del Objeto       

        EntityManager em = getEntityManager();

        Usuario persistenUsuario = em.find(Usuario.class, pUsuario.getIdUsuario()); // Cargo persona de db

        if (persistenUsuario != null)
        {
            version = persistenUsuario.getVersion(); // Version del Objeto en db
            oldVersion = pUsuario.getVersion(); // Version del Objeto en memoria

            if (version != oldVersion) //Si son distintas "Alguien modifico el objeto"
            {
                throw new ClassModifiedException("El usuario indicado ha sido modificado por otro usuario");

            } else
            {
                try
                {

                    em.getTransaction().begin(); //Comienzo Transaccion

                    persistenUsuario.setNombre(pUsuario.getNombre());
                    persistenUsuario.setContrasenia(pUsuario.getContrasenia());
                    persistenUsuario.setEstado(pUsuario.getEstado());
                    persistenUsuario.setTipo(pUsuario.getTipo());

                    //El valor de la version del objeto queda a cardo de Hibernate
                    em.getTransaction().commit();
                    flag = true;
                    em.close();

                }
                catch (Exception e)
                {
                    System.out.println("Error de Persistencia: Usuario JpaController metodo: modificarUsuario");
                }
            }
        } else//Si fue  eliminado se dispara una excepcion 
        {
            throw new ClassEliminatedException("El cliente indicado ya ha sido eliminado con anterioridad");
        }

        return flag;
    }

    @Override
    public String getNombreJpa()
    {
        return this.getClass().getName();
    }
}
