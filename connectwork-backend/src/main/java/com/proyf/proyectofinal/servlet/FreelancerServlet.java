/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.proyf.proyectofinal.servlet;

import com.proyf.proyectofinal.daos.HabilidadDAO;
import com.proyf.proyectofinal.daos.PerfilFreelancerDAO;
import com.proyf.proyectofinal.daos.SolicitudCatalogoDAO;
import com.proyf.proyectofinal.daos.UsuarioDAO;
import com.proyf.proyectofinal.modelo.SolicitudCatalogo;
import com.proyf.proyectofinal.servicio.ReporteServicio;
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

@WebServlet("/api/freelancer/*")
public class FreelancerServlet extends HttpServlet {
    private final UsuarioDAO usuarioDAO = new UsuarioDAO();
    private final PerfilFreelancerDAO perfilFreelancerDAO = new PerfilFreelancerDAO();
    private final HabilidadDAO habilidadDAO = new HabilidadDAO();
    private final SolicitudCatalogoDAO solicitudDAO = new SolicitudCatalogoDAO();
    private final ReporteServicio reporteServicio = new ReporteServicio();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json;charset=UTF-8");
        int idUsuario = (int) req.getAttribute("idUsuario");
        String rol = (String) req.getAttribute("tipoRol");
        String ruta = req.getPathInfo();

        if (!"FREELANCER".equals(rol)) {
            resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
            resp.getWriter().write(GsonUtil.toJson(new Respuesta(false, "Acceso denegado", null)));
            return;
        }

        if ("/perfil".equals(ruta)) {
            var usuario = usuarioDAO.buscarPorId(idUsuario);
            var perfil = perfilFreelancerDAO.buscarPorUsuario(idUsuario);
            var habilidades = habilidadDAO.listarDeFreelancer(idUsuario);
            Map<String, Object> datos = new HashMap<>();
            datos.put("usuario", usuario);
            datos.put("perfil", perfil);
            datos.put("habilidades", habilidades);
            resp.getWriter().write(GsonUtil.toJson(new Respuesta(true, "OK", datos)));
        } else if ("/saldo".equals(ruta)) {
            var usuario = usuarioDAO.buscarPorId(idUsuario);
            resp.getWriter().write(GsonUtil.toJson(new Respuesta(true, "OK", Map.of("saldo", usuario.getSaldo()))));
        } else if ("/solicitudes".equals(ruta)) {
            var solicitudes = solicitudDAO.listarPorUsuario(idUsuario);
            resp.getWriter().write(GsonUtil.toJson(new Respuesta(true, "OK", Map.of("solicitudes", solicitudes))));
        } else if (ruta != null && ruta.startsWith("/reportes/")) {
            String tipo = ruta.replace("/reportes/", "");
            String desde = req.getParameter("desde");
            String hasta = req.getParameter("hasta");
            Map<String, Object> resultado = switch (tipo) {
                case "contratos"   -> reporteServicio.reporteContratosFreelancer(idUsuario, desde, hasta);
                case "categorias"  -> reporteServicio.reporteTopCategoriasFreelancer(idUsuario);
                case "propuestas"  -> reporteServicio.reportePropuestasFreelancer(idUsuario, desde, hasta);
                default -> Map.of("error", "Tipo de reporte no reconocido");
            };
            if (resultado.containsKey("error")) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                resp.getWriter().write(GsonUtil.toJson(new Respuesta(false, (String) resultado.get("error"), null)));
            } else {
                resp.getWriter().write(GsonUtil.toJson(new Respuesta(true, "OK", resultado)));
            }
        } else {
            resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
            resp.getWriter().write(GsonUtil.toJson(new Respuesta(false, "Ruta no encontrada", null)));
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json;charset=UTF-8");
        int idUsuario = (int) req.getAttribute("idUsuario");
        String rol = (String) req.getAttribute("tipoRol");
        String ruta = req.getPathInfo();
        String cuerpo = new String(req.getInputStream().readAllBytes());

        if (!"FREELANCER".equals(rol)) {
            resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
            resp.getWriter().write(GsonUtil.toJson(new Respuesta(false, "Acceso denegado", null)));
            return;
        }

        if ("/solicitar-habilidad".equals(ruta)) {
            SolicitudCatalogo s = GsonUtil.fromJson(cuerpo, SolicitudCatalogo.class);
            s.setIdUsuario(idUsuario);
            s.setTipo("HABILIDAD");
            if (s.getNombre() == null || s.getNombre().isBlank()) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                resp.getWriter().write(GsonUtil.toJson(new Respuesta(false, "El nombre de la habilidad es requerido", null)));
                return;
            }
            boolean ok = solicitudDAO.insertar(s);
            if (!ok) {
                resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                resp.getWriter().write(GsonUtil.toJson(new Respuesta(false, "No se pudo registrar la solicitud", null)));
            } else {
                resp.setStatus(HttpServletResponse.SC_CREATED);
                resp.getWriter().write(GsonUtil.toJson(new Respuesta(true, "Solicitud enviada al administrador", null)));
            }
        } else {
            resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
            resp.getWriter().write(GsonUtil.toJson(new Respuesta(false, "Ruta no encontrada", null)));
        }
    }
}