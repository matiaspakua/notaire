package com.licensis.notaire.api.client;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.licensis.notaire.dto.DtoUsuario;
import com.licensis.notaire.dto.GenericDto;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Cliente base para hacer peticiones REST a la API
 * Maneja serialización/deserialización con Jackson
 * Usa HttpURLConnection (disponible en Java estándar)
 */
public class RestClient {
    
    private static final ObjectMapper objectMapper = new ObjectMapper();
    
    /**
     * Obtiene una lista de objetos desde el endpoint.
     * Si responseType es GenericDto, deserializa cada elemento JSON como Map y lo envuelve en GenericDto.
     */
    @SuppressWarnings("unchecked")
    public static <T> List<T> getList(String endpoint, Class<T> responseType) throws IOException {
        String url = ApiConfig.getApiBaseUrl() + endpoint;
        String jsonResponse = makeGetRequest(url);

        if (responseType == GenericDto.class) {
            List<Map<String, Object>> list = objectMapper.readValue(jsonResponse,
                    new TypeReference<List<Map<String, Object>>>() {});
            List<GenericDto> result = new ArrayList<>();
            for (Map<String, Object> map : list) {
                GenericDto dto = new GenericDto();
                dto.setData(map != null ? map : new java.util.HashMap<>());
                result.add(dto);
            }
            return (List<T>) result;
        }

        T[] items = objectMapper.readValue(jsonResponse, objectMapper.getTypeFactory()
                .constructArrayType(responseType));
        return new ArrayList<>(Arrays.asList(items));
    }
    
    /**
     * Obtiene un objeto individual desde el endpoint
     */
    public static <T> T get(String endpoint, Class<T> responseType) throws IOException {
        String url = ApiConfig.getApiBaseUrl() + endpoint;
        String jsonResponse = makeGetRequest(url);
        return objectMapper.readValue(jsonResponse, responseType);
    }
    
    /**
     * Crea un nuevo objeto en el servidor
     */
    public static <T> T post(String endpoint, Object body, Class<T> responseType) throws IOException {
        String url = ApiConfig.getApiBaseUrl() + endpoint;
        String jsonBody = objectMapper.writeValueAsString(body);
        String jsonResponse = makePostRequest(url, jsonBody);
        return objectMapper.readValue(jsonResponse, responseType);
    }
    
    /**
     * Actualiza un objeto existente en el servidor
     */
    public static <T> T put(String endpoint, Object body, Class<T> responseType) throws IOException {
        String url = ApiConfig.getApiBaseUrl() + endpoint;
        String jsonBody = objectMapper.writeValueAsString(body);
        String jsonResponse = makePutRequest(url, jsonBody);
        
        if (jsonResponse.isEmpty()) {
            return null;
        }
        return objectMapper.readValue(jsonResponse, responseType);
    }
    
    /**
     * Autentica usuario (POST /usuarios/login).
     *
     * @param loginRequest DtoUsuario con nombre y contrasenia
     * @return DtoUsuario con valido=true y datos del usuario si es correcto; valido=false si no
     */
    public static DtoUsuario login(DtoUsuario loginRequest) throws IOException {
        String url = ApiConfig.getApiBaseUrl() + "/usuarios/login";
        String jsonBody = objectMapper.writeValueAsString(loginRequest);
        String jsonResponse = makePostRequest(url, jsonBody);
        return objectMapper.readValue(jsonResponse, DtoUsuario.class);
    }

    /**
     * Elimina un objeto del servidor
     */
    public static void delete(String endpoint) throws IOException {
        String url = ApiConfig.getApiBaseUrl() + endpoint;
        makeDeleteRequest(url);
    }
    
    /**
     * Realiza una petición GET
     */
    private static String makeGetRequest(String urlString) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) new URL(urlString).openConnection();
        connection.setRequestMethod("GET");
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setConnectTimeout(ApiConfig.getConnectionTimeout());
        connection.setReadTimeout(ApiConfig.getReadTimeout());
        
        try {
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
     * Realiza una petición POST
     */
    private static String makePostRequest(String urlString, String body) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) new URL(urlString).openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setConnectTimeout(ApiConfig.getConnectionTimeout());
        connection.setReadTimeout(ApiConfig.getReadTimeout());
        connection.setDoOutput(true);
        
        try {
            try (OutputStream os = connection.getOutputStream()) {
                os.write(body.getBytes(StandardCharsets.UTF_8));
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
     * Realiza una petición PUT
     */
    private static String makePutRequest(String urlString, String body) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) new URL(urlString).openConnection();
        connection.setRequestMethod("PUT");
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setConnectTimeout(ApiConfig.getConnectionTimeout());
        connection.setReadTimeout(ApiConfig.getReadTimeout());
        connection.setDoOutput(true);
        
        try {
            try (OutputStream os = connection.getOutputStream()) {
                os.write(body.getBytes(StandardCharsets.UTF_8));
                os.flush();
            }
            
            int responseCode = connection.getResponseCode();
            if (responseCode == 200 || responseCode == 204 || responseCode == 201) {
                InputStream is = responseCode == 204 ? null : connection.getInputStream();
                return is != null ? readResponse(is) : "";
            } else {
                String error = readResponse(connection.getErrorStream());
                throw new IOException("HTTP " + responseCode + ": " + error);
            }
        } finally {
            connection.disconnect();
        }
    }
    
    /**
     * Realiza una petición DELETE
     */
    private static void makeDeleteRequest(String urlString) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) new URL(urlString).openConnection();
        connection.setRequestMethod("DELETE");
        connection.setConnectTimeout(ApiConfig.getConnectionTimeout());
        connection.setReadTimeout(ApiConfig.getReadTimeout());
        
        try {
            int responseCode = connection.getResponseCode();
            if (responseCode != 200 && responseCode != 204) {
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
    private static String readResponse(InputStream is) throws IOException {
        if (is == null) {
            return "";
        }
        
        byte[] buffer = new byte[1024];
        StringBuilder response = new StringBuilder();
        int bytesRead;
        
        try (is) {
            while ((bytesRead = is.read(buffer)) != -1) {
                response.append(new String(buffer, 0, bytesRead, StandardCharsets.UTF_8));
            }
        }
        
        return response.toString();
    }
}

