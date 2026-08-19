package com.licensis.notaire.util;

import com.licensis.notaire.dto.DtoUsuario;

/**
 * Gestor de sesión de usuario (Singleton)
 * Mantiene la referencia al usuario actualmente logueado
 */
public class SessionManager {
    
    private static SessionManager instancia = null;
    private DtoUsuario usuarioLogueado = null;
    
    private SessionManager() {
        // Constructor privado para singleton
    }
    
    public static SessionManager getInstancia() {
        if (instancia == null) {
            instancia = new SessionManager();
        }
        return instancia;
    }
    
    /**
     * Establece el usuario logueado
     */
    public void setUsuarioLogueado(DtoUsuario usuario) {
        this.usuarioLogueado = usuario;
    }
    
    /**
     * Obtiene el usuario logueado
     */
    public DtoUsuario getUsuarioLogueado() {
        return usuarioLogueado;
    }
    
    /**
     * Verifica si hay un usuario logueado
     */
    public boolean isUsuarioLogueado() {
        return usuarioLogueado != null && usuarioLogueado.isValido();
    }
    
    /**
     * Cierra la sesión
     */
    public void cerrarSesion() {
        this.usuarioLogueado = null;
    }
    
    /**
     * Obtiene el nombre completo del usuario logueado
     */
    public String getNombreCompletoUsuario() {
        if (usuarioLogueado != null && usuarioLogueado.getPersonas() != null) {
            return usuarioLogueado.getPersonas().getNombre() + " " + usuarioLogueado.getPersonas().getApellido();
        }
        return "";
    }
}

