package com.licensis.notaire.unit;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.licensis.notaire.negocio.Usuario;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

@DisplayName("Usuario entity JSON serialization")
class UsuarioSerializationTest {

    @Test
    @DisplayName("Should never include the password hash in serialized JSON (issue #631)")
    void shouldNotSerializeContrasenia() throws Exception {
        Usuario usuario = new Usuario(1, "admin", "5f4dcc3b5aa765d61d8327deb882cf99", true, "ADMIN");

        String json = new ObjectMapper().writeValueAsString(usuario);

        assertFalse(json.contains("contrasenia"), "Serialized Usuario must not contain the password field");
        assertFalse(json.contains("5f4dcc3b5aa765d61d8327deb882cf99"),
                "Serialized Usuario must not leak the password hash");
    }

    @Test
    @DisplayName("Should still expose contrasenia to plain Java callers, e.g. login (getter not removed, only hidden from Jackson)")
    void shouldStillExposeContraseniaViaGetter() {
        Usuario usuario = new Usuario();
        usuario.setContrasenia("5f4dcc3b5aa765d61d8327deb882cf99");

        assertEquals("5f4dcc3b5aa765d61d8327deb882cf99", usuario.getContrasenia());
    }
}
