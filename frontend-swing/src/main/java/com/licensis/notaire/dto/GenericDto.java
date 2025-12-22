package com.licensis.notaire.dto;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * DTO genérico que usa un Map para almacenar datos
 * Permite flexibilidad sin estar atado a tipos específicos
 */
public class GenericDto implements Serializable, Cloneable {
    
    private static final long serialVersionUID = 1L;
    protected Map<String, Object> data = new HashMap<>();
    
    public GenericDto() {
    }
    
    public Object get(String key) {
        return data.get(key);
    }
    
    public void put(String key, Object value) {
        data.put(key, value);
    }
    
    public Integer getInt(String key) {
        Object val = data.get(key);
        if (val instanceof Integer) {
            return (Integer) val;
        }
        if (val instanceof Number) {
            return ((Number) val).intValue();
        }
        return null;
    }
    
    public String getString(String key) {
        Object val = data.get(key);
        return val != null ? val.toString() : null;
    }
    
    public Boolean getBoolean(String key) {
        Object val = data.get(key);
        if (val instanceof Boolean) {
            return (Boolean) val;
        }
        return null;
    }
    
    public Float getFloat(String key) {
        Object val = data.get(key);
        if (val instanceof Float) {
            return (Float) val;
        }
        if (val instanceof Number) {
            return ((Number) val).floatValue();
        }
        return null;
    }
    
    public Map<String, Object> getData() {
        return data;
    }
    
    public void setData(Map<String, Object> data) {
        this.data = data;
    }
    
    @Override
    public String toString() {
        return "GenericDto{" + "data=" + data + '}';
    }
}
