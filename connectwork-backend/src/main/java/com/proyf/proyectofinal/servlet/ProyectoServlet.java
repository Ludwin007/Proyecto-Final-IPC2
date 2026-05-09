/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.proyf.proyectofinal.servlet;

import com.proyf.proyectofinal.modelo.Proyecto;
import com.proyf.proyectofinal.servicio.ProyectoServicio;
import com.proyf.proyectofinal.util.GsonUtil;
import com.proyf.proyectofinal.util.Respuesta;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.Map;

/**
 *
 * @author ludwi
 */

@WebServlet("/api/proyectos/*")
public class ProyectoServlet extends HttpServlet {
    private final ProyectoServicio servicio = new ProyectoServicio();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json;charset=UTF-8");
        String ruta = req.getPathInfo();
        int idUsuario = (int) req.getAttribute("idUsuario");
        String rol = (String) req.getAttribute("tipoRol");

        if (ruta == null || "/".equals(ruta)) {
            if ("CLIENTE".equals(rol)) {
                Map<String, Object> resultado = servicio.listarPorCliente(idUsuario);
                resp.getWriter().write(GsonUtil.toJson(new Respuesta(true, "OK", resultado)));
            } else {
                String catParam = req.getParameter("categoria");
                String minParam = req.getParameter("presMin");
                String maxParam = req.getParameter("presMax");
                
                Integer idCategoria = catParam != null ? Integer.parseInt(catParam) : null;
                BigDecimal presMin = minParam != null ? new BigDecimal(minParam) : null;
                BigDecimal presMax = maxParam != null ? new BigDecimal(maxParam) : null;
                
                Map<String, Object> resultado = servicio.listarAbiertos(idCategoria, presMin, presMax);
                resp.getWriter().write(GsonUtil.toJson(new Respuesta(true, "OK", resultado)));
            }
        } else {
            int idProyecto = Integer.parseInt(ruta.replace("/", ""));
            Map<String, Object> resultado = servicio.obtenerDetalle(idProyecto);
            if (resultado.containsKey("error")) {
                resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                resp.getWriter().write(GsonUtil.toJson(new Respuesta(false, (String) resultado.get("error"), null)));
            } else {
                resp.getWriter().write(GsonUtil.toJson(new Respuesta(true, "OK", resultado)));
            }
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json;charset=UTF-8");
        int idUsuario = (int) req.getAttribute("idUsuario");
        String rol = (String) req.getAttribute("tipoRol");

        if (!"CLIENTE".equals(rol)) {
            resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
            resp.getWriter().write(GsonUtil.toJson(new Respuesta(false, "Solo los clientes pueden publicar proyectos", null)));
            return;
        }

        String cuerpo = new String(req.getInputStream().readAllBytes());
        Proyecto p = GsonUtil.fromJson(cuerpo, Proyecto.class);
        p.setIdCliente(idUsuario);

        Map<String, Object> resultado = servicio.publicar(p);
        if (resultado.containsKey("error")) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write(GsonUtil.toJson(new Respuesta(false, (String) resultado.get("error"), null)));
        } else {
            resp.setStatus(HttpServletResponse.SC_CREATED);
            resp.getWriter().write(GsonUtil.toJson(new Respuesta(true, (String) resultado.get("mensaje"), resultado.get("idProyecto"))));
        }
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json;charset=UTF-8");
        int idUsuario = (int) req.getAttribute("idUsuario");
        String rol = (String) req.getAttribute("tipoRol");
        String ruta = req.getPathInfo();

        if (!"CLIENTE".equals(rol)) {
            resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
            resp.getWriter().write(GsonUtil.toJson(new Respuesta(false, "Acceso denegado", null)));
            return;
        }

        String cuerpo = new String(req.getInputStream().readAllBytes());
        if (ruta != null && ruta.endsWith("/cancelar")) {
            int idProyecto = Integer.parseInt(ruta.replace("/", "").replace("cancelar", ""));
            Map<String, Object> resultado = servicio.cancelar(idProyecto, idUsuario);
            manejarRespuesta(resp, resultado);
        } else {
            Proyecto p = GsonUtil.fromJson(cuerpo, Proyecto.class);
            if (ruta != null) {
                try {
                    int idProyecto = Integer.parseInt(ruta.replace("/", ""));
                    p.setIdProyecto(idProyecto);
                } catch (NumberFormatException ignored) {}
            }
            Map<String, Object> resultado = servicio.actualizar(p, idUsuario);
            manejarRespuesta(resp, resultado);
        }
    }

    private void manejarRespuesta(HttpServletResponse resp, Map<String, Object> resultado) throws IOException {
        if (resultado.containsKey("error")) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write(GsonUtil.toJson(new Respuesta(false, (String) resultado.get("error"), null)));
        } else {
            resp.getWriter().write(GsonUtil.toJson(new Respuesta(true, (String) resultado.get("mensaje"), null)));
        }
    }
}