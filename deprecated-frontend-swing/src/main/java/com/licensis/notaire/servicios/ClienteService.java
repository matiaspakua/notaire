package com.licensis.notaire.servicios;

import com.licensis.notaire.api.client.RestClient;
import com.licensis.notaire.dto.DtoPersona;

import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Servicio para gestionar clientes/personas
 * Usa REST API para comunicarse con el backend
 * Nota: Este servicio trabaja directamente con DTOs, la conversión Persona->DtoPersona
 * se hace en el backend a través del endpoint REST
 */
public class ClienteService {
    
    private static final Logger logger = Logger.getLogger(ClienteService.class.getName());
    private static final String ENDPOINT = "/personas";
    
    /**
     * Busca clientes por nombre y apellido
     * Nota: Por ahora devuelve todas las personas, el filtrado se puede hacer en el frontend
     * o implementar un endpoint específico en el backend
     */
    public List<DtoPersona> buscarClientes(String nombre, String apellido) throws IOException {
        try {
            // Por ahora obtenemos todas y filtramos en el frontend
            // TODO: Implementar endpoint de búsqueda en backend
            List<DtoPersona> todas = obtenerTodasLasPersonas();
            
            // Filtrar por nombre y apellido si se proporcionan
            if (nombre != null && !nombre.isEmpty()) {
                final String nombreFilter = nombre.toLowerCase();
                todas = todas.stream()
                    .filter(p -> p.getNombre() != null && p.getNombre().toLowerCase().contains(nombreFilter))
                    .toList();
            }
            if (apellido != null && !apellido.isEmpty()) {
                final String apellidoFilter = apellido.toLowerCase();
                todas = todas.stream()
                    .filter(p -> p.getApellido() != null && p.getApellido().toLowerCase().contains(apellidoFilter))
                    .toList();
            }
            
            return todas;
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Error buscando clientes", e);
            throw e;
        }
    }
    
    /**
     * Obtiene un cliente por ID
     * Nota: Necesitamos un endpoint que devuelva DtoPersona directamente
     * Por ahora usamos GenericDto y convertimos manualmente
     */
    public DtoPersona obtenerCliente(Integer id) throws IOException {
        try {
            // TODO: Crear un endpoint específico que devuelva DtoPersona
            // Por ahora retornamos null y se implementará cuando migremos los formularios
            return null;
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error obteniendo cliente: " + id, e);
            throw new IOException("Error obteniendo cliente", e);
        }
    }
    
    /**
     * Crea un nuevo cliente
     */
    public DtoPersona crearCliente(DtoPersona dtoPersona) throws IOException {
        try {
            // El backend espera Persona, pero trabajamos con DTOs
            // Necesitamos convertir DtoPersona a formato que el backend entienda
            // Por ahora, esto es un placeholder
            logger.log(Level.INFO, "Crear cliente - pendiente de implementación completa");
            return dtoPersona;
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error creando cliente", e);
            throw new IOException("Error creando cliente", e);
        }
    }
    
    /**
     * Actualiza un cliente existente
     */
    public DtoPersona actualizarCliente(DtoPersona dtoPersona) throws IOException {
        try {
            logger.log(Level.INFO, "Actualizar cliente - pendiente de implementación completa");
            return dtoPersona;
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error actualizando cliente", e);
            throw new IOException("Error actualizando cliente", e);
        }
    }
    
    /**
     * Elimina un cliente
     */
    public boolean eliminarCliente(Integer id) throws IOException {
        try {
            RestClient.delete(ENDPOINT + "/" + id);
            return true;
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Error eliminando cliente: " + id, e);
            throw e;
        }
    }
    
    /**
     * Obtiene todas las personas
     * Nota: El backend devuelve List<Persona>, necesitamos convertirlo a List<DtoPersona>
     * Por ahora retornamos lista vacía como placeholder
     */
    public List<DtoPersona> obtenerTodasLasPersonas() throws IOException {
        try {
            // TODO: Implementar conversión Persona -> DtoPersona
            // Por ahora retornamos lista vacía
            logger.log(Level.INFO, "Obtener todas las personas - pendiente de implementación completa");
            return new java.util.ArrayList<>();
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error obteniendo todas las personas", e);
            throw new IOException("Error obteniendo todas las personas", e);
        }
    }
}

