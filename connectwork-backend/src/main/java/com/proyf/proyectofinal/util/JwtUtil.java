/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.proyf.proyectofinal.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 *
 * @author ludwi
 */

public class JwtUtil {

    private static final String CLAVE_SECRETA = "ConnectWorkCUNOCIPC2Semestre2026ClaveSecreta!";
    private static final long EXPIRACION_MS = 86400000;

    private static SecretKey obtenerClave() {
        return Keys.hmacShaKeyFor(CLAVE_SECRETA.getBytes(StandardCharsets.UTF_8));
    }

    public static String generarToken(int idUsuario, String username, String tipoRol, boolean perfilCompleto) {
        return Jwts.builder()
                .subject(String.valueOf(idUsuario))
                .claim("username", username)
                .claim("tipoRol", tipoRol)
                .claim("perfilCompleto", perfilCompleto)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + EXPIRACION_MS))
                .signWith(obtenerClave())
                .compact();
    }

    public static Claims validarToken(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(obtenerClave())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (Exception e) {
            return null;
        }
    }

    public static int obtenerIdUsuario(String token) {
        Claims claims = validarToken(token);
        if (claims == null) return -1;
        return Integer.parseInt(claims.getSubject());
    }

    public static String obtenerRol(String token) {
        Claims claims = validarToken(token);
        if (claims == null) return null;
        return claims.get("tipoRol", String.class);
    }

    public static boolean obtenerPerfilCompleto(String token) {
        Claims claims = validarToken(token);
        if (claims == null) return false;
        return Boolean.TRUE.equals(claims.get("perfilCompleto", Boolean.class));
    }
}
