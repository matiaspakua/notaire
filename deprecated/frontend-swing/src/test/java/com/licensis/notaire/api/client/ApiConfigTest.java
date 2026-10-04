package com.licensis.notaire.api.client;

import com.licensis.notaire.dto.DtoUsuario;
import com.licensis.notaire.dto.GenericDto;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

public class ApiConfigTest {

    @Test
    public void testDefaultConfiguration() {
        assertEquals("http://localhost:8080/api/v1", ApiConfig.getApiBaseUrl());
        assertEquals(10000, ApiConfig.getConnectionTimeout());
        assertEquals(10000, ApiConfig.getReadTimeout());
    }

    @Test
    public void testSetApiBaseUrl() {
        String originalUrl = ApiConfig.getApiBaseUrl();
        try {
            ApiConfig.setApiBaseUrl("http://test.server:9090/api/v1");
            assertEquals("http://test.server:9090/api/v1", ApiConfig.getApiBaseUrl());
        } finally {
            ApiConfig.setApiBaseUrl(originalUrl);
        }
    }

    @Test
    public void testSetConnectionTimeout() {
        int originalTimeout = ApiConfig.getConnectionTimeout();
        try {
            ApiConfig.setConnectionTimeout(5000);
            assertEquals(5000, ApiConfig.getConnectionTimeout());
        } finally {
            ApiConfig.setConnectionTimeout(originalTimeout);
        }
    }

    @Test
    public void testSetReadTimeout() {
        int originalTimeout = ApiConfig.getReadTimeout();
        try {
            ApiConfig.setReadTimeout(30000);
            assertEquals(30000, ApiConfig.getReadTimeout());
        } finally {
            ApiConfig.setReadTimeout(originalTimeout);
        }
    }

    @Test
    public void testLoadFromPropertiesWithDefaults() {
        String originalUrl = ApiConfig.getApiBaseUrl();
        int originalConnectTimeout = ApiConfig.getConnectionTimeout();
        int originalReadTimeout = ApiConfig.getReadTimeout();
        
        try {
            ApiConfig.loadFromProperties();
            
            assertEquals("http://localhost:8080/api/v1", ApiConfig.getApiBaseUrl());
            assertEquals(10000, ApiConfig.getConnectionTimeout());
            assertEquals(10000, ApiConfig.getReadTimeout());
        } finally {
            ApiConfig.setApiBaseUrl(originalUrl);
            ApiConfig.setConnectionTimeout(originalConnectTimeout);
            ApiConfig.setReadTimeout(originalReadTimeout);
        }
    }

    @Test
    public void testLoadFromPropertiesWithSystemProperties() {
        String originalUrl = ApiConfig.getApiBaseUrl();
        int originalConnectTimeout = ApiConfig.getConnectionTimeout();
        int originalReadTimeout = ApiConfig.getReadTimeout();
        
        try {
            System.setProperty("api.base.url", "http://custom:7777/api/v1");
            System.setProperty("api.timeout", "5000");
            
            ApiConfig.loadFromProperties();
            
            assertEquals("http://custom:7777/api/v1", ApiConfig.getApiBaseUrl());
            assertEquals(5000, ApiConfig.getConnectionTimeout());
            assertEquals(5000, ApiConfig.getReadTimeout());
        } finally {
            System.clearProperty("api.base.url");
            System.clearProperty("api.timeout");
            ApiConfig.setApiBaseUrl(originalUrl);
            ApiConfig.setConnectionTimeout(originalConnectTimeout);
            ApiConfig.setReadTimeout(originalReadTimeout);
        }
    }
}