package com.licensis.notaire.unit;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.licensis.notaire.dto.DtoUsuario;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

@DisplayName("DtoUsuario JSON serialization")
class DtoUsuarioSerializationTest {

    @Test
    @DisplayName("Should never include the password hash in serialized JSON (issue #564)")
    void shouldNotSerializeContrasenia() throws Exception {
        DtoUsuario dto = new DtoUsuario();
        dto.setIdUsuario(1);
        dto.setNombre("admin");
        dto.setContrasenia("5f4dcc3b5aa765d61d8327deb882cf99");

        String json = new ObjectMapper().writeValueAsString(dto);

        assertFalse(json.contains("contrasenia"), "Serialized DtoUsuario must not contain the password field");
        assertFalse(json.contains("5f4dcc3b5aa765d61d8327deb882cf99"), "Serialized DtoUsuario must not leak the password hash");
    }

    @Test
    @DisplayName("Should still expose contrasenia to plain Java callers (getter not removed, only hidden from Jackson)")
    void shouldStillExposeContraseniaViaGetter() {
        DtoUsuario dto = new DtoUsuario();
        dto.setContrasenia("5f4dcc3b5aa765d61d8327deb882cf99");

        assertEquals("5f4dcc3b5aa765d61d8327deb882cf99", dto.getContrasenia());
    }

    @Test
    @DisplayName("Should still deserialize contrasenia from incoming JSON (login request body)")
    void shouldStillDeserializeContraseniaFromJson() throws Exception {
        String json = "{\"nombre\":\"admin\",\"contrasenia\":\"5f4dcc3b5aa765d61d8327deb882cf99\"}";

        DtoUsuario dto = new ObjectMapper().readValue(json, DtoUsuario.class);

        assertEquals("5f4dcc3b5aa765d61d8327deb882cf99", dto.getContrasenia());
    }
}
