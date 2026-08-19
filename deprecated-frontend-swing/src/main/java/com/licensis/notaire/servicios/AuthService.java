package com.licensis.notaire.servicios;

import com.licensis.notaire.api.client.RestClient;
import com.licensis.notaire.api.client.ApiConfig;
import com.licensis.notaire.dto.DtoUsuario;
import com.licensis.notaire.util.SessionManager;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Servicio de autenticación
 * Maneja el login y logout de usuarios mediante REST API
 */
public class AuthService {
    
    private static final Logger logger = Logger.getLogger(AuthService.class.getName());
    private static final ObjectMapper objectMapper = new ObjectMapper();
    
    /**
     * Autentica un usuario con nombre y contraseña
     * 
     * @param nombre Nombre de usuario
     * @param contrasenia Contraseña del usuario
     * @return DtoUsuario con valido=true si la autenticación fue exitosa, valido=false en caso contrario
     */
    public DtoUsuario login(String nombre, String contrasenia) throws IOException {
        try {
            DtoUsuario loginRequest = new DtoUsuario();
            loginRequest.setNombre(nombre);
            loginRequest.setContrasenia(contrasenia);
            
            String url = ApiConfig.getApiBaseUrl() + "/usuarios/login";
            String jsonBody = objectMapper.writeValueAsString(loginRequest);
            
            String jsonResponse = makePostRequest(url, jsonBody);
            DtoUsuario usuario = objectMapper.readValue(jsonResponse, DtoUsuario.class);
            
            if (usuario != null && usuario.isValido()) {
                // Guardar usuario en sesión
                SessionManager.getInstancia().setUsuarioLogueado(usuario);
                logger.log(Level.INFO, "Usuario autenticado: " + nombre);
            } else {
                logger.log(Level.WARNING, "Autenticación fallida para usuario: " + nombre);
            }
            
            return usuario;
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Error en login para usuario: " + nombre, e);
            throw new IOException("Error de conexión con el servidor. Verifique que el backend esté corriendo.", e);
        }
    }
    
    /**
     * Cierra la sesión del usuario actual
     */
    public void logout() {
        SessionManager.getInstancia().cerrarSesion();
        logger.log(Level.INFO, "Sesión cerrada");
    }
    
    /**
     * Obtiene el usuario actualmente logueado
     */
    public DtoUsuario getCurrentUser() {
        return SessionManager.getInstancia().getUsuarioLogueado();
    }
    
    /**
     * Verifica si hay un usuario autenticado
     */
    public boolean isAuthenticated() {
        return SessionManager.getInstancia().isUsuarioLogueado();
    }
    
    /**
     * Realiza una petición POST
     */
    private String makePostRequest(String urlString, String body) throws IOException {
        java.net.HttpURLConnection connection = (java.net.HttpURLConnection) new java.net.URL(urlString).openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setConnectTimeout(ApiConfig.getConnectionTimeout());
        connection.setReadTimeout(ApiConfig.getReadTimeout());
        connection.setDoOutput(true);
        
        try {
            try (java.io.OutputStream os = connection.getOutputStream()) {
                os.write(body.getBytes(java.nio.charset.StandardCharsets.UTF_8));
                os.flush();
            }
            
            int responseCode = connection.getResponseCode();
            if (responseCode == 200 || responseCode == 201) {
                return readResponse(connection.getInputStream());
            } else {
                String error = readResponse(connection.getErrorStream());
                throw new IOException("HTTP " + responseCode + ": " + error);
            }
        } finally {
            connection.disconnect();
        }
    }
    
    /**
     * Lee la respuesta del stream
     */
    private String readResponse(java.io.InputStream is) throws IOException {
        if (is == null) {
            return "";
        }
        
        byte[] buffer = new byte[1024];
        StringBuilder response = new StringBuilder();
        int bytesRead;
        
        try (is) {
            while ((bytesRead = is.read(buffer)) != -1) {
                response.append(new String(buffer, 0, bytesRead, java.nio.charset.StandardCharsets.UTF_8));
            }
        }
        
        return response.toString();
    }
}

