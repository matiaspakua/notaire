package com.licensis.notaire.unit;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.licensis.notaire.dto.DtoUser;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

@DisplayName("DtoUsuario JSON serialization")
class DtoUserSerializationTest {

    @Test
    @DisplayName("Should never include the password hash in serialized JSON (issue #564)")
    void shouldNotSerializePassword() throws Exception {
        DtoUser dto = new DtoUser();
        dto.setIdUser(1);
        dto.setName("admin");
        dto.setPassword("5f4dcc3b5aa765d61d8327deb882cf99");

        String json = new ObjectMapper().writeValueAsString(dto);

        assertFalse(json.contains("password"), "Serialized DtoUsuario must not contain the password field");
        assertFalse(json.contains("5f4dcc3b5aa765d61d8327deb882cf99"), "Serialized DtoUsuario must not leak the password hash");
    }

    @Test
    @DisplayName("Should still expose password to plain Java callers (getter not removed, only hidden from Jackson)")
    void shouldStillExposePasswordViaGetter() {
        DtoUser dto = new DtoUser();
        dto.setPassword("5f4dcc3b5aa765d61d8327deb882cf99");

        assertEquals("5f4dcc3b5aa765d61d8327deb882cf99", dto.getPassword());
    }

    @Test
    @DisplayName("Should still deserialize password from incoming JSON (login request body)")
    void shouldStillDeserializePasswordFromJson() throws Exception {
        String json = "{\"name\":\"admin\",\"password\":\"5f4dcc3b5aa765d61d8327deb882cf99\"}";

        DtoUser dto = new ObjectMapper().readValue(json, DtoUser.class);

        assertEquals("5f4dcc3b5aa765d61d8327deb882cf99", dto.getPassword());
    }
}
