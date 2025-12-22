package com.licensis.notaire.api.client;

/**
 * Configuración de la API REST
 * Centraliza la URL base y configuración de conexión
 */
public class ApiConfig {
    
    private static String apiBaseUrl = "http://localhost:8080/api/v1";
    private static int connectionTimeout = 10000; // 10 segundos
    private static int readTimeout = 10000; // 10 segundos
    
    public static String getApiBaseUrl() {
        return apiBaseUrl;
    }
    
    public static void setApiBaseUrl(String baseUrl) {
        apiBaseUrl = baseUrl;
    }
    
    public static int getConnectionTimeout() {
        return connectionTimeout;
    }
    
    public static void setConnectionTimeout(int timeout) {
        connectionTimeout = timeout;
    }
    
    public static int getReadTimeout() {
        return readTimeout;
    }
    
    public static void setReadTimeout(int timeout) {
        readTimeout = timeout;
    }
    
    /**
     * Permite configurar la API desde un archivo de propiedades
     * Ejemplo: http://localhost:8080/api/v1
     */
    public static void loadFromProperties() {
        try {
            String url = System.getProperty("api.base.url", apiBaseUrl);
            String timeout = System.getProperty("api.timeout", String.valueOf(connectionTimeout));
            setApiBaseUrl(url);
            setConnectionTimeout(Integer.parseInt(timeout));
            setReadTimeout(Integer.parseInt(timeout));
        } catch (Exception e) {
            System.err.println("Error loading API configuration: " + e.getMessage());
        }
    }
}
