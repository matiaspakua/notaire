package com.licensis.notaire.api;

import com.licensis.notaire.dto.DtoUsuario;
import com.licensis.notaire.servicios.AdministradorSesion;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*") // Para desarrollo
public class AuthController {

    @PostMapping("/login")
    public ResponseEntity<DtoUsuario> login(@RequestBody DtoUsuario loginRequest) {
        DtoUsuario resultado = AdministradorSesion.getInstancia().validarUsuario(loginRequest);
        if (resultado.isValido()) {
            return ResponseEntity.ok(resultado);
        } else {
            return ResponseEntity.status(401).body(resultado);
        }
    }
}
