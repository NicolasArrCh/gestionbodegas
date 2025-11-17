package com.c3.gestionbodegas.dto;

import com.c3.gestionbodegas.entities.Usuario.Rol;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegisterRequest {
    private String username;
    private String password;
    private String nombreCompleto;
    private Rol rol;
}
