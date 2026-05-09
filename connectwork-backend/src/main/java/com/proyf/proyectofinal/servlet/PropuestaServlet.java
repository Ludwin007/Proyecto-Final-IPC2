/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.proyf.proyectofinal.servlet;

import com.proyf.proyectofinal.modelo.Propuesta;
import com.proyf.proyectofinal.servicio.PropuestaServicio;
import com.proyf.proyectofinal.util.GsonUtil;
import com.proyf.proyectofinal.util.Respuesta;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;

/**
 *
 * @author ludwi
 */

@WebServlet("/api/propuestas/*")
public class PropuestaServlet extends HttpServlet {
    private final PropuestaServicio servicio = new PropuestaServicio();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json;charset=UTF-8");
        int idUsuario = (int) req.getAttribute("idUsuario");
        String rol = (String) req.getAttribute("tipoRol");
        String ruta = req.getPathInfo();

        if (ruta != null && ruta.startsWith("/proyecto/")) {
            int idProyecto = Integer.parseInt(ruta.replace("/proyecto/", ""));
            Map<String, Object> resultado = servicio.listarPorProyecto(idProyecto, idUsuario);
            if (resultado.containsKey("error")) {
                resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
                resp.getWriter().write(GsonUtil.toJson(new Respuesta(false, (String) resultado.get("error"), null)));
            } else {
                resp.getWriter().write(GsonUtil.toJson(new Respuesta(true, "OK", resultado)));
            }
        } else if ("FREELANCER".equals(rol)) {
            Map<String, Object> resultado = servicio.listarPorFreelancer(idUsuario);
            resp.getWriter().write(GsonUtil.toJson(new Respuesta(true, "OK", resultado)));
        } else {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write(GsonUtil.toJson(new Respuesta(false, "Parametros invalidos", null)));
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json;charset=UTF-8");
        int idUsuario = (int) req.getAttribute("idUsuario");
        String rol = (String) req.getAttribute("tipoRol");

        if (!"FREELANCER".equals(rol)) {
            resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
            resp.getWriter().write(GsonUtil.toJson(new Respuesta(false, "Solo los freelancers pueden enviar propuestas", null)));
            return;
        }

        String cuerpo = new String(req.getInputStream().readAllBytes());
        Propuesta p = GsonUtil.fromJson(cuerpo, Propuesta.class);
        p.setIdFreelancer(idUsuario);

        Map<String, Object> resultado = servicio.enviar(p);
        if (resultado.containsKey("error")) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write(GsonUtil.toJson(new Respuesta(false, (String) resultado.get("error"), null)));
        } else {
            resp.setStatus(HttpServletResponse.SC_CREATED);
            resp.getWriter().write(GsonUtil.toJson(new Respuesta(true, (String) resultado.get("mensaje"), resultado.get("idPropuesta"))));
        }
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json;charset=UTF-8");
        int idUsuario = (int) req.getAttribute("idUsuario");
        String ruta = req.getPathInfo();

        if (ruta == null) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write(GsonUtil.toJson(new Respuesta(false, "Ruta invalida", null)));
            return;
        }

        String[] partes = ruta.split("/"); 
        int idPropuesta = Integer.parseInt(partes[1]);
        String accion = partes.length > 2 ? partes[2] : "";

        Map<String, Object> resultado;
        switch (accion) {
            case "aceptar" -> resultado = servicio.aceptar(idPropuesta, idUsuario);
            case "rechazar" -> resultado = servicio.rechazar(idPropuesta, idUsuario);
            case "retirar" -> resultado = servicio.retirar(idPropuesta, idUsuario);
            default -> {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                resp.getWriter().write(GsonUtil.toJson(new Respuesta(false, "Accion no reconocida", null)));
                return;
            }
        }

        if (resultado.containsKey("error")) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write(GsonUtil.toJson(new Respuesta(false, (String) resultado.get("error"), null)));
        } else {
            resp.getWriter().write(GsonUtil.toJson(new Respuesta(true, (String) resultado.get("mensaje"), resultado)));
        }
    }
}