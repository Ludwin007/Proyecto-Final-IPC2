/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.proyf.proyectofinal.servlet;

import com.proyf.proyectofinal.modelo.PerfilCliente;
import com.proyf.proyectofinal.modelo.PerfilFreelancer;
import com.proyf.proyectofinal.modelo.Usuario;
import com.proyf.proyectofinal.servicio.AuthServicio;
import com.proyf.proyectofinal.util.GsonUtil;
import com.proyf.proyectofinal.util.Respuesta;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * 
 * @author ludwi
 */

public class AuthServlet extends HttpServlet {

    private final AuthServicio servicio = new AuthServicio();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json;charset=UTF-8");
        String ruta = req.getPathInfo();
        String cuerpo = new String(req.getInputStream().readAllBytes());

        if ("/login".equals(ruta)) {
            Map<?, ?> datos = GsonUtil.fromJson(cuerpo, Map.class);
            
            String username = (String) datos.get("username");
            if (username == null) {
                username = (String) datos.get("correo"); 
            }
            String contrasena = (String) datos.get("contrasena");
            
            System.out.println(">>> AuthServlet - Intento login - Usuario/Correo: " + username + " | Pass: " + (contrasena != null ? "***" : "null"));

            if (username == null || contrasena == null) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                resp.getWriter().write(GsonUtil.toJson(new Respuesta(false, "Correo y contraseña son requeridos", null)));
                return;
            }

            Map<String, Object> resultado = servicio.login(username, contrasena);
            if (resultado.containsKey("error")) {
                resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                resp.getWriter().write(GsonUtil.toJson(new Respuesta(false, (String) resultado.get("error"), null)));
            } else {
                resp.getWriter().write(GsonUtil.toJson(new Respuesta(true, "Login exitoso", resultado)));
            }

        } else if ("/registro".equals(ruta)) {
            Usuario u = GsonUtil.fromJson(cuerpo, Usuario.class);
            Map<String, Object> resultado = servicio.registrar(u);
            if (resultado.containsKey("error")) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                resp.getWriter().write(GsonUtil.toJson(new Respuesta(false, (String) resultado.get("error"), null)));
            } else {
                resp.setStatus(HttpServletResponse.SC_CREATED);
                resp.getWriter().write(GsonUtil.toJson(new Respuesta(true, (String) resultado.get("mensaje"), resultado.get("idUsuario"))));
            }

        } else if ("/perfil-cliente".equals(ruta)) {
            Object _rawId = req.getAttribute("idUsuario"); int idUsuario = _rawId != null ? (int) _rawId : -1;
            PerfilCliente perfil = GsonUtil.fromJson(cuerpo, PerfilCliente.class);
            Map<String, Object> resultado = servicio.completarPerfilCliente(idUsuario, perfil);
            if (resultado.containsKey("error")) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                resp.getWriter().write(GsonUtil.toJson(new Respuesta(false, (String) resultado.get("error"), null)));
            } else {
                resp.getWriter().write(GsonUtil.toJson(new Respuesta(true, (String) resultado.get("mensaje"), null)));
            }

        } else if ("/perfil-freelancer".equals(ruta)) {
            Object _rawId = req.getAttribute("idUsuario"); int idUsuario = _rawId != null ? (int) _rawId : -1;
            Map<?, ?> datos = GsonUtil.fromJson(cuerpo, Map.class);
            PerfilFreelancer perfil = GsonUtil.fromJson(GsonUtil.toJson(datos.get("perfil")), PerfilFreelancer.class);
            List<?> rawIds = (List<?>) datos.get("idHabilidades");
            List<Integer> idHabilidades = rawIds.stream()
                    .map(id -> ((Number) id).intValue()).toList();
            Map<String, Object> resultado = servicio.completarPerfilFreelancer(idUsuario, perfil, idHabilidades);
            if (resultado.containsKey("error")) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                resp.getWriter().write(GsonUtil.toJson(new Respuesta(false, (String) resultado.get("error"), null)));
            } else {
                resp.getWriter().write(GsonUtil.toJson(new Respuesta(true, (String) resultado.get("mensaje"), null)));
            }

        } else if ("/crear-admin".equals(ruta)) {
            String rol = (String) req.getAttribute("tipoRol");
            if (!"ADMIN".equals(rol)) {
                resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
                resp.getWriter().write(GsonUtil.toJson(new Respuesta(false, "Acceso denegado", null)));
                return;
            }
            Usuario u = GsonUtil.fromJson(cuerpo, Usuario.class);
            Map<String, Object> resultado = servicio.crearAdmin(u);
            if (resultado.containsKey("error")) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                resp.getWriter().write(GsonUtil.toJson(new Respuesta(false, (String) resultado.get("error"), null)));
            } else {
                resp.setStatus(HttpServletResponse.SC_CREATED);
                resp.getWriter().write(GsonUtil.toJson(new Respuesta(true, (String) resultado.get("mensaje"), resultado.get("idUsuario"))));
            }

        } else {
            resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
            resp.getWriter().write(GsonUtil.toJson(new Respuesta(false, "Ruta no encontrada", null)));
        }
    }
}
