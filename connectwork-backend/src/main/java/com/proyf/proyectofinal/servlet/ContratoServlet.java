/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.proyf.proyectofinal.servlet;

import com.proyf.proyectofinal.modelo.Calificacion;
import com.proyf.proyectofinal.modelo.Entrega;
import com.proyf.proyectofinal.servicio.ContratoServicio;
import com.proyf.proyectofinal.util.GsonUtil;
import com.proyf.proyectofinal.util.Respuesta;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author ludwi
 */

@WebServlet("/api/contratos/*")
public class ContratoServlet extends HttpServlet {

    private final ContratoServicio servicio = new ContratoServicio();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json;charset=UTF-8");
        Object rawId = req.getAttribute("idUsuario");
        int idUsuario = rawId != null ? (int) rawId : -1;
        String rol = (String) req.getAttribute("tipoRol");
        String ruta = req.getPathInfo();

        if (ruta == null || "/".equals(ruta)) {
            Map<String, Object> resultado = "FREELANCER".equals(rol)
                    ? servicio.listarPorFreelancer(idUsuario)
                    : servicio.listarPorCliente(idUsuario);
            resp.getWriter().write(GsonUtil.toJson(new Respuesta(true, "OK", resultado)));
        } else {
            try {
                int idContrato = Integer.parseInt(ruta.replace("/", ""));
                Map<String, Object> resultado = servicio.obtenerDetalle(idContrato, idUsuario, rol);
                if (resultado.containsKey("error")) {
                    resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
                    resp.getWriter().write(GsonUtil.toJson(new Respuesta(false, (String) resultado.get("error"), null)));
                } else {
                    resp.getWriter().write(GsonUtil.toJson(new Respuesta(true, "OK", resultado)));
                }
            } catch (NumberFormatException e) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                resp.getWriter().write(GsonUtil.toJson(new Respuesta(false, "ID de contrato invalido", null)));
            }
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json;charset=UTF-8");
        Object rawId = req.getAttribute("idUsuario");
        int idUsuario = rawId != null ? (int) rawId : -1;
        String rol = (String) req.getAttribute("tipoRol");
        String ruta = req.getPathInfo();
        String cuerpo = new String(req.getInputStream().readAllBytes());

        if (ruta != null && ruta.equals("/entrega")) {
            if (!"FREELANCER".equals(rol)) {
                resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
                resp.getWriter().write(GsonUtil.toJson(new Respuesta(false, "Solo los freelancers pueden subir entregas", null)));
                return;
            }
            Entrega entrega = GsonUtil.fromJson(cuerpo, Entrega.class);
            Map<String, Object> resultado = servicio.subirEntrega(entrega, idUsuario);
            if (resultado.containsKey("error")) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                resp.getWriter().write(GsonUtil.toJson(new Respuesta(false, (String) resultado.get("error"), null)));
            } else {
                resp.setStatus(HttpServletResponse.SC_CREATED);
                resp.getWriter().write(GsonUtil.toJson(new Respuesta(true, (String) resultado.get("mensaje"), resultado.get("idEntrega"))));
            }

        } else if (ruta != null && ruta.equals("/calificar")) {
            if (!"CLIENTE".equals(rol)) {
                resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
                resp.getWriter().write(GsonUtil.toJson(new Respuesta(false, "Solo los clientes pueden calificar", null)));
                return;
            }
            Map<?, ?> datos = GsonUtil.fromJson(cuerpo, Map.class);
            Calificacion cal = new Calificacion();
            cal.setIdContrato(((Number) datos.get("idContrato")).intValue());
            cal.setPuntuacion(((Number) datos.get("puntuacion")).intValue());
            cal.setComentario(datos.get("comentario") != null ? (String) datos.get("comentario") : null);
            Map<String, Object> resultado = servicio.calificar(cal, idUsuario);
            if (resultado.containsKey("error")) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                resp.getWriter().write(GsonUtil.toJson(new Respuesta(false, (String) resultado.get("error"), null)));
            } else {
                resp.getWriter().write(GsonUtil.toJson(new Respuesta(true, (String) resultado.get("mensaje"), null)));
            }
        } else {
            resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
            resp.getWriter().write(GsonUtil.toJson(new Respuesta(false, "Ruta no encontrada", null)));
        }
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json;charset=UTF-8");
        Object rawId = req.getAttribute("idUsuario");
        int idUsuario = rawId != null ? (int) rawId : -1;
        String rol = (String) req.getAttribute("tipoRol");
        String ruta = req.getPathInfo();
        String cuerpo = new String(req.getInputStream().readAllBytes());

        if (ruta == null || "/".equals(ruta)) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write(GsonUtil.toJson(new Respuesta(false, "Ruta invalida", null)));
            return;
        }

        String[] partes = ruta.split("/");
        Map<String, Object> resultado = new HashMap<>();

        try {
            if (partes.length == 5 && "entregas".equals(partes[2])) {
                int idContrato = Integer.parseInt(partes[1]);
                int idEntrega = Integer.parseInt(partes[3]);
                String accion = partes[4];

                Map<?, ?> datos = cuerpo.isBlank() ? null : GsonUtil.fromJson(cuerpo, Map.class);
                String motivo = datos != null && datos.get("motivo") != null ? (String) datos.get("motivo") : null;

                switch (accion) {
                    case "retirar":
                        if (!"FREELANCER".equals(rol)) {
                            resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
                            resp.getWriter().write(GsonUtil.toJson(new Respuesta(false, "Solo los freelancers pueden retirar entregas", null)));
                            return;
                        }
                        resultado = servicio.retirarEntrega(idEntrega, idUsuario);
                        break;
                    case "aprobar":
                        if (!"CLIENTE".equals(rol)) {
                            resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
                            resp.getWriter().write(GsonUtil.toJson(new Respuesta(false, "Solo los clientes pueden aprobar entregas", null)));
                            return;
                        }
                        resultado = servicio.aprobarEntrega(idEntrega, idUsuario);
                        break;
                    case "rechazar":
                        if (!"CLIENTE".equals(rol)) {
                            resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
                            resp.getWriter().write(GsonUtil.toJson(new Respuesta(false, "Solo los clientes pueden rechazar entregas", null)));
                            return;
                        }
                        resultado = servicio.rechazarEntrega(idEntrega, idUsuario, motivo);
                        break;
                    default:
                        resultado.put("error", "Accion no reconocida: " + accion);
                }
            } else if (partes.length == 3 && "cancelar".equals(partes[2])) {
                if (!"CLIENTE".equals(rol)) {
                    resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
                    resp.getWriter().write(GsonUtil.toJson(new Respuesta(false, "Solo los clientes pueden cancelar contratos", null)));
                    return;
                }
                int idContrato = Integer.parseInt(partes[1]);
                Map<?, ?> datos = cuerpo.isBlank() ? null : GsonUtil.fromJson(cuerpo, Map.class);
                String motivo = datos != null && datos.get("motivo") != null ? (String) datos.get("motivo") : null;
                resultado = servicio.cancelarContrato(idContrato, idUsuario, motivo);
            } else if (partes.length == 3 && "eliminar-calificacion".equals(partes[2])) {
                if (!"CLIENTE".equals(rol)) {
                    resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
                    resp.getWriter().write(GsonUtil.toJson(new Respuesta(false, "Solo los clientes pueden eliminar calificaciones", null)));
                    return;
                }
                int idContrato = Integer.parseInt(partes[1]);
                resultado = servicio.eliminarCalificacion(idContrato, idUsuario);
            } else {
                resultado.put("error", "Ruta no encontrada");
            }
        } catch (NumberFormatException e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write(GsonUtil.toJson(new Respuesta(false, "ID invalido", null)));
            return;
        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write(GsonUtil.toJson(new Respuesta(false, "Error interno del servidor", null)));
            return;
        }

        if (resultado.containsKey("error")) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write(GsonUtil.toJson(new Respuesta(false, (String) resultado.get("error"), null)));
        } else {
            resp.getWriter().write(GsonUtil.toJson(new Respuesta(true, (String) resultado.get("mensaje"), resultado)));
        }
    }
}