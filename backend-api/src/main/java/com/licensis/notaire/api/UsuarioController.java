package com.licensis.notaire.api;

import com.licensis.notaire.dto.DtoUsuario;
import com.licensis.notaire.jpa.exceptions.NonexistentJpaException;
import com.licensis.notaire.negocio.ControllerNegocio;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;

@RestController
@RequestMapping("/api/usuarios")
@CrossOrigin(origins = "*")
public class UsuarioController {

    @GetMapping
    public ResponseEntity<ArrayList<DtoUsuario>> list() {
        try {
            ArrayList<DtoUsuario> usuarios = ControllerNegocio.getInstancia().buscarUsuariosDisponibles();
            return ResponseEntity.ok(usuarios);
        } catch (NonexistentJpaException e) {
            return ResponseEntity.status(500).build();
        }
    }
}
