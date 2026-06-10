package com.licensis.notaire.unit;

import com.licensis.notaire.api.UsuarioController;
import com.licensis.notaire.config.JwtTokenService;
import com.licensis.notaire.repository.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class UsuarioControllerHashTest {

    @Test
    void shouldGenerateMd5HashForPassword() throws Exception {
        UsuarioController controller = new UsuarioController(
                Mockito.mock(UsuarioRepository.class), Mockito.mock(JwtTokenService.class));
        Method method = UsuarioController.class.getDeclaredMethod("encriptaEnMD5", String.class);
        method.setAccessible(true);

        String hashed = (String) method.invoke(controller, "admin");

        assertNotNull(hashed);
        assertEquals("21232f297a57a5a743894a0e4a801fc3", hashed);
    }
}
