package com.licensis.notaire.api.client;

import com.licensis.notaire.dto.DtoUsuario;
import com.licensis.notaire.dto.GenericDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

public class RestClientTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    public void testDtoUsuarioSerialization() throws Exception {
        DtoUsuario usuario = new DtoUsuario();
        usuario.setIdUsuario(1);
        usuario.setNombre("admin");
        usuario.setContrasenia("admin123");
        usuario.setEstado(true);
        usuario.setTipo("ADMINISTRADOR");
        usuario.setValido(true);

        String json = objectMapper.writeValueAsString(usuario);
        assertNotNull(json);
        assertTrue(json.contains("\"nombre\":\"admin\""));
        assertTrue(json.contains("\"valido\":true"));
    }

    @Test
    public void testDtoUsuarioDeserialization() throws Exception {
        String json = "{\"idUsuario\":1,\"nombre\":\"testuser\",\"contrasenia\":\"pass\",\"estado\":true,\"tipo\":\"USUARIO\",\"valido\":true}";

        DtoUsuario usuario = objectMapper.readValue(json, DtoUsuario.class);
        
        assertEquals(Integer.valueOf(1), usuario.getIdUsuario());
        assertEquals("testuser", usuario.getNombre());
        assertEquals("pass", usuario.getContrasenia());
        assertTrue(usuario.isEstado());
        assertEquals("USUARIO", usuario.getTipo());
        assertTrue(usuario.isValido());
    }

    @Test
    public void testDtoUsuarioWithNullValues() throws Exception {
        String json = "{\"idUsuario\":null,\"nombre\":null,\"contrasenia\":null,\"estado\":false,\"tipo\":null,\"valido\":false}";

        DtoUsuario usuario = objectMapper.readValue(json, DtoUsuario.class);
        
        assertNull(usuario.getIdUsuario());
        assertNull(usuario.getNombre());
        assertFalse(usuario.isValido());
    }

    @Test
    public void testGenericDtoSerialization() throws Exception {
        GenericDto genericDto = new GenericDto();
        java.util.HashMap<String, Object> data = new java.util.HashMap<>();
        data.put("id", 1);
        data.put("name", "Test");
        data.put("active", true);
        genericDto.setData(data);

        String json = objectMapper.writeValueAsString(genericDto);
        assertNotNull(json);
        assertTrue(json.contains("\"id\":1"));
        assertTrue(json.contains("\"name\":\"Test\""));
    }

    @Test
    public void testGenericDtoDeserialization() throws Exception {
        String json = "{\"data\":{\"id\":42,\"description\":\"Sample\",\"count\":100}}";

        GenericDto genericDto = objectMapper.readValue(json, GenericDto.class);
        
        assertNotNull(genericDto.getData());
        assertEquals(42, genericDto.getData().get("id"));
        assertEquals("Sample", genericDto.getData().get("description"));
        assertEquals(100, genericDto.getData().get("count"));
    }

    @Test
    public void testLoginRequestDto() throws Exception {
        DtoUsuario loginRequest = new DtoUsuario();
        loginRequest.setNombre("admin");
        loginRequest.setContrasenia("admin");

        String json = objectMapper.writeValueAsString(loginRequest);
        
        assertTrue(json.contains("\"nombre\":\"admin\""));
        assertTrue(json.contains("\"contrasenia\":\"admin\""));
    }

    @Test
    public void testApiEndpointConstruction() {
        String baseUrl = ApiConfig.getApiBaseUrl();
        
        assertEquals("http://localhost:8080/api/v1/usuarios", baseUrl + "/usuarios");
        assertEquals("http://localhost:8080/api/v1/personas", baseUrl + "/personas");
        assertEquals("http://localhost:8080/api/v1/conceptos", baseUrl + "/conceptos");
        assertEquals("http://localhost:8080/api/v1/presupuestos", baseUrl + "/presupuestos");
    }

    @Test
    public void testApiEndpointWithQueryParameters() {
        String baseUrl = ApiConfig.getApiBaseUrl();
        
        String searchEndpoint = baseUrl + "/personas/buscar?nombre=John";
        assertEquals("http://localhost:8080/api/v1/personas/buscar?nombre=John", searchEndpoint);
        
        String filterEndpoint = baseUrl + "/presupuestos?estado=pendiente";
        assertEquals("http://localhost:8080/api/v1/presupuestos?estado=pendiente", filterEndpoint);
    }
}