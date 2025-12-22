package com.licensis.notaire.api.client;

import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Clase base para todos los clientes REST de entidades
 * Proporciona operaciones CRUD estándar usando Jackson para serialización
 */
public abstract class BaseRestClient<T> {
    
    protected static final Logger logger = Logger.getLogger(BaseRestClient.class.getName());
    protected String endpoint;
    protected Class<T> entityClass;
    
    protected BaseRestClient(String endpoint, Class<T> entityClass) {
        this.endpoint = endpoint;
        this.entityClass = entityClass;
    }
    
    /**
     * Obtiene todas las entidades
     */
    public List<T> findAll() throws IOException {
        try {
            List<T> items = RestClient.getList(endpoint, entityClass);
            logger.log(Level.FINE, "findAll() - Obtenidas " + items.size() + " entidades desde " + endpoint);
            return items;
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error en findAll() - " + endpoint, e);
            throw new IOException("Error obteniendo lista desde " + endpoint + ": " + e.getMessage(), e);
        }
    }
    
    /**
     * Obtiene una entidad por ID
     */
    public T find(Object id) throws IOException {
        try {
            String fullEndpoint = endpoint + "/" + id;
            T entity = RestClient.get(fullEndpoint, entityClass);
            logger.log(Level.FINE, "find() - Entidad encontrada: " + id);
            return entity;
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error en find(" + id + ") - " + endpoint, e);
            throw new IOException("Error obteniendo entidad desde " + endpoint + "/" + id + ": " + e.getMessage(), e);
        }
    }
    
    /**
     * Crea una nueva entidad
     */
    public T create(T entity) throws IOException {
        try {
            T createdEntity = RestClient.post(endpoint, entity, entityClass);
            logger.log(Level.FINE, "create() - Entidad creada exitosamente");
            return createdEntity;
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error en create()", e);
            throw new IOException("Error creando entidad en " + endpoint + ": " + e.getMessage(), e);
        }
    }
    
    /**
     * Actualiza una entidad existente
     */
    public T edit(T entity) throws IOException {
        try {
            // Intenta obtener el ID de la entidad
            Integer id = extractId(entity);
            String fullEndpoint = endpoint + "/" + id;
            
            T updatedEntity = RestClient.put(fullEndpoint, entity, entityClass);
            logger.log(Level.FINE, "edit() - Entidad actualizada: " + id);
            return updatedEntity != null ? updatedEntity : entity;
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error en edit()", e);
            throw new IOException("Error actualizando entidad en " + endpoint + ": " + e.getMessage(), e);
        }
    }
    
    /**
     * Elimina una entidad por ID
     */
    public void remove(Object id) throws IOException {
        try {
            String fullEndpoint = endpoint + "/" + id;
            RestClient.delete(fullEndpoint);
            logger.log(Level.FINE, "remove() - Entidad eliminada: " + id);
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error en remove(" + id + ")", e);
            throw new IOException("Error eliminando entidad desde " + endpoint + "/" + id + ": " + e.getMessage(), e);
        }
    }
    
    /**
     * Método abstracto que debe implementar cada cliente para extraer el ID de la entidad
     */
    protected abstract Integer extractId(T entity);
    
    /**
     * Método para obtener el nombre del JPA (para compatibilidad)
     */
    public abstract String getNombreJpa();
}
