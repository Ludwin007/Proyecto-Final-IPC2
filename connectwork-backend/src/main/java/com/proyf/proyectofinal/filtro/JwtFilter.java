/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.proyf.proyectofinal.filtro;

import com.proyf.proyectofinal.util.GsonUtil;
import com.proyf.proyectofinal.util.JwtUtil;
import com.proyf.proyectofinal.util.Respuesta;
import io.jsonwebtoken.Claims;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 *
 * @author ludwi
 */

public class JwtFilter implements Filter {

    private static final List<String> RUTAS_PUBLICAS = Arrays.asList(
            "/api/auth/login",
            "/api/auth/registro"
    );

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest  request  = (HttpServletRequest)  req;
        HttpServletResponse response = (HttpServletResponse) res;

        String contextPath = request.getContextPath();
        String uri         = request.getRequestURI();
        String ruta        = uri.substring(contextPath.length());

        System.out.println(">>> JwtFilter procesando ruta: " + ruta + " | Metodo: " + request.getMethod());

        boolean esPublica = RUTAS_PUBLICAS.stream().anyMatch(ruta::startsWith);
        if (esPublica || "OPTIONS".equalsIgnoreCase(request.getMethod())) {
            System.out.println(">>> Ruta pública o OPTIONS, dejando pasar.");
            chain.doFilter(req, res);
            return;
        }

        String header = request.getHeader("Authorization");
        if (header == null || !header.startsWith("Bearer ")) {
            enviarError(response, HttpServletResponse.SC_UNAUTHORIZED, "Token de autenticacion requerido");
            return;
        }

        String token  = header.substring(7);
        Claims claims = JwtUtil.validarToken(token);
        if (claims == null) {
            enviarError(response, HttpServletResponse.SC_UNAUTHORIZED, "Token invalido o expirado");
            return;
        }

        try {
            int idUsuario = Integer.parseInt(claims.getSubject());
            request.setAttribute("idUsuario", idUsuario);
        } catch (NumberFormatException e) {
            enviarError(response, HttpServletResponse.SC_UNAUTHORIZED, "Token malformado");
            return;
        }

        request.setAttribute("username",        claims.get("username",        String.class));
        request.setAttribute("tipoRol",         claims.get("tipoRol",         String.class));
        request.setAttribute("perfilCompleto",  claims.get("perfilCompleto",  Boolean.class));

        chain.doFilter(req, res);
    }

    private void enviarError(HttpServletResponse response, int status, String mensaje) throws IOException {
        response.setStatus(status);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write(GsonUtil.toJson(new Respuesta(false, mensaje, null)));
    }
}
