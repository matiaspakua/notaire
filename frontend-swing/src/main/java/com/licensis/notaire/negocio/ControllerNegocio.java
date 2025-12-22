package com.licensis.notaire.negocio;

import com.licensis.notaire.dto.DtoFlag;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Clase controller de la capa de negocio. Versión simplificada para el frontend.
 * Solo contiene los métodos necesarios para compilar.
 * 
 * @author User
 */
public class ControllerNegocio {

    private static ControllerNegocio instancia = null;

    private ControllerNegocio() {
    }

    public static ControllerNegocio getInstancia() {
        if (instancia == null) {
            instancia = new ControllerNegocio();
        }
        return instancia;
    }

    /**
     * Metodo que permite encriptar un string usando MD5
     *
     * @param stringAEncriptar El string a encriptar
     * @return El string encriptado en MD5
     */
    public String encriptaEnMD5(String stringAEncriptar) {
        char[] CONSTS_HEX = {
            '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'
        };
        try {
            MessageDigest msgd = MessageDigest.getInstance("MD5");
            byte[] bytes = msgd.digest(stringAEncriptar.getBytes());
            StringBuilder strbCadenaMD5 = new StringBuilder(2 * bytes.length);
            for (int i = 0; i < bytes.length; i++) {
                int bajo = (int) (bytes[i] & 0x0f);
                int alto = (int) ((bytes[i] & 0xf0) >> 4);
                strbCadenaMD5.append(CONSTS_HEX[alto]);
                strbCadenaMD5.append(CONSTS_HEX[bajo]);
            }
            return strbCadenaMD5.toString();
        } catch (NoSuchAlgorithmException e) {
            return null;
        }
    }

    /**
     * Metodo que permite verificar si dos contraseñas (char[]) son iguales
     *
     * @param j1 Primera contraseña
     * @param j2 Segunda contraseña
     * @return DtoFlag con flag=true si son iguales, false en caso contrario
     */
    public DtoFlag isPasswordCorrect(char[] j1, char[] j2) {
        com.licensis.notaire.dto.DtoFlag dtoFlag = new com.licensis.notaire.dto.DtoFlag();
        boolean valor = true;
        int puntero = 0;
        if (j1.length != j2.length) {
            valor = false;
        } else {
            while ((valor) && (puntero < j1.length)) {
                if (j1[puntero] != j2[puntero]) {
                    valor = false;
                }
                puntero++;
            }
        }
        dtoFlag.setFlag(valor);
        return dtoFlag;
    }
}
