/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.proyf.proyectofinal.servlet;

import com.proyf.proyectofinal.modelo.Categoria;
import com.proyf.proyectofinal.modelo.Habilidad;
import com.proyf.proyectofinal.servicio.AdminServicio;
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

@WebServlet("/api/admin/*")
public class AdminServlet extends HttpServlet {
    private final AdminServicio servicio = new AdminServicio();

    private boolean esAdmin(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String rol = (String) req.getAttribute("tipoRol");
        if (!"ADMIN".equals(rol)) {
            resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
            resp.getWriter().write(GsonUtil.toJson(new Respuesta(false, "Acceso denegado", null)));
            return false;
        }
        return true;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json;charset=UTF-8");
        if (!esAdmin(req, resp)) return;

        String ruta = req.getPathInfo();

        if ("/usuarios".equals(ruta)) {
            Map<String, Object> resultado = servicio.listarUsuarios();
            resp.getWriter().write(GsonUtil.toJson(new Respuesta(true, "OK", resultado)));
        } else if ("/categorias".equals(ruta)) {
            Map<String, Object> resultado = servicio.listarCategorias();
            resp.getWriter().write(GsonUtil.toJson(new Respuesta(true, "OK", resultado)));
        } else if (ruta != null && ruta.startsWith("/habilidades/")) {
            int idCategoria = Integer.parseInt(ruta.replace("/habilidades/", ""));
            Map<String, Object> resultado = servicio.listarHabilidades(idCategoria);
            resp.getWriter().write(GsonUtil.toJson(new Respuesta(true, "OK", resultado)));
        } else if ("/comision".equals(ruta)) {
            Map<String, Object> resultado = servicio.obtenerComision();
            resp.getWriter().write(GsonUtil.toJson(new Respuesta(true, "OK", resultado)));
        } else if ("/solicitudes".equals(ruta)) {
            Map<String, Object> resultado = servicio.listarSolicitudesPendientes();
            resp.getWriter().write(GsonUtil.toJson(new Respuesta(true, "OK", resultado)));
        } else if (ruta != null && ruta.startsWith("/reportes/")) {
            manejarReportes(req, resp, ruta);
        } else {
            resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
            resp.getWriter().write(GsonUtil.toJson(new Respuesta(false, "Ruta no encontrada", null)));
        }
    }

    private void manejarReportes(HttpServletRequest req, HttpServletResponse resp, String ruta) throws IOException {
        String tipo = ruta.replace("/reportes/", "");
        String desde = req.getParameter("desde");
        String hasta = req.getParameter("hasta");
        
        Map<String, Object> resultado = switch (tipo) {
            case "freelancers" -> servicio.reporteTopFreelancers(desde, hasta);
            case "categorias"  -> servicio.reporteTopCategorias(desde, hasta);
            case "ingresos"    -> servicio.reporteIngresos(desde, hasta);
            default -> Map.of("error", "Tipo de reporte no reconocido");
        };

        if (resultado.containsKey("error")) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write(GsonUtil.toJson(new Respuesta(false, (String) resultado.get("error"), null)));
        } else {
            resp.getWriter().write(GsonUtil.toJson(new Respuesta(true, "OK", resultado)));
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json;charset=UTF-8");
        if (!esAdmin(req, resp)) return;

        String ruta = req.getPathInfo();
        String cuerpo = new String(req.getInputStream().readAllBytes());

        if ("/categorias".equals(ruta)) {
            Categoria c = GsonUtil.fromJson(cuerpo, Categoria.class);
            Map<String, Object> resultado = servicio.crearCategoria(c);
            enviarRespuesta(resp, resultado, HttpServletResponse.SC_CREATED);
        } else if ("/habilidades".equals(ruta)) {
            Habilidad h = GsonUtil.fromJson(cuerpo, Habilidad.class);
            Map<String, Object> resultado = servicio.crearHabilidad(h);
            enviarRespuesta(resp, resultado, HttpServletResponse.SC_CREATED);
        } else {
            resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
            resp.getWriter().write(GsonUtil.toJson(new Respuesta(false, "Ruta no encontrada", null)));
        }
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json;charset=UTF-8");
        if (!esAdmin(req, resp)) return;

        String ruta = req.getPathInfo();
        String cuerpo = new String(req.getInputStream().readAllBytes());
        if (ruta == null) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write(GsonUtil.toJson(new Respuesta(false, "Ruta invalida", null)));
            return;
        }

        Map<String, Object> resultado;
        
        if (ruta.contains("/activar") || ruta.contains("/desactivar")) {
            resultado = manejarCambiosEstado(ruta);
        } 
        else if (ruta.startsWith("/categorias/")) {
            Categoria c = GsonUtil.fromJson(cuerpo, Categoria.class);
            c.setIdCategoria(Integer.parseInt(ruta.replace("/categorias/", "")));
            resultado = servicio.editarCategoria(c);
        } else if (ruta.startsWith("/habilidades/")) {
            Habilidad h = GsonUtil.fromJson(cuerpo, Habilidad.class);
            h.setIdHabilidad(Integer.parseInt(ruta.replace("/habilidades/", "")));
            resultado = servicio.editarHabilidad(h);
        } else if ("/comision".equals(ruta)) {
            Map<?, ?> datos = GsonUtil.fromJson(cuerpo, Map.class);
            BigDecimal nuevoPct = new BigDecimal(datos.get("porcentaje").toString());
            resultado = servicio.actualizarComision(nuevoPct);
        } else if (ruta.startsWith("/solicitudes/")) {
            int idSolicitud = Integer.parseInt(ruta.replace("/solicitudes/", ""));
            Map<?, ?> datos = GsonUtil.fromJson(cuerpo, Map.class);
            resultado = servicio.resolverSolicitud(idSolicitud, 
                    (String) datos.get("decision"), 
                    (String) datos.get("respuesta"), 
                    datos.get("idCategoria") != null ? ((Number) datos.get("idCategoria")).intValue() : null);
        } else {
            resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
            resp.getWriter().write(GsonUtil.toJson(new Respuesta(false, "Ruta no encontrada", null)));
            return;
        }

        enviarRespuesta(resp, resultado, HttpServletResponse.SC_OK);
    }

    private Map<String, Object> manejarCambiosEstado(String ruta) {
        if (ruta.contains("/usuarios/")) {
            int id = Integer.parseInt(ruta.split("/")[2]);
            return servicio.cambiarEstadoUsuario(id, ruta.endsWith("/activar"));
        } else if (ruta.contains("/categorias/")) {
            int id = Integer.parseInt(ruta.split("/")[2]);
            return servicio.cambiarEstadoCategoria(id, ruta.endsWith("/activar"));
        } else if (ruta.contains("/habilidades/")) {
            int id = Integer.parseInt(ruta.split("/")[2]);
            return servicio.cambiarEstadoHabilidad(id, ruta.endsWith("/activar"));
        }
        return Map.of("error", "Entidad no reconocida");
    }

    private void enviarRespuesta(HttpServletResponse resp, Map<String, Object> resultado, int codigoExito) throws IOException {
        if (resultado.containsKey("error")) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write(GsonUtil.toJson(new Respuesta(false, (String) resultado.get("error"), null)));
        } else {
            resp.setStatus(codigoExito);
            resp.getWriter().write(GsonUtil.toJson(new Respuesta(true, (String) resultado.get("mensaje"), null)));
        }
    }
}
