/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.proyf.proyectofinal.servlet;

import com.proyf.proyectofinal.servicio.AdminServicio;
import com.proyf.proyectofinal.servicio.PdfServicio;
import com.proyf.proyectofinal.servicio.ReporteServicio;
import com.proyf.proyectofinal.util.GsonUtil;
import com.proyf.proyectofinal.util.Respuesta;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 *
 * @author ludwi
 */

public class PdfServlet extends HttpServlet {

    private final PdfServicio pdfServicio = new PdfServicio();
    private final AdminServicio adminServicio = new AdminServicio();
    private final ReporteServicio reporteServicio = new ReporteServicio();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String tipo = req.getParameter("tipo");
        String desde = req.getParameter("desde");
        String hasta = req.getParameter("hasta");
        String rol = (String) req.getAttribute("tipoRol");
        Object _rawId = req.getAttribute("idUsuario"); int idUsuario = _rawId != null ? (int) _rawId : -1;

        if (tipo == null || tipo.isBlank()) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.setContentType("application/json;charset=UTF-8");
            resp.getWriter().write(GsonUtil.toJson(new Respuesta(false, "El tipo de reporte es requerido", null)));
            return;
        }

        byte[] pdf = null;
        String nombreArchivo = "reporte.pdf";

        switch (tipo) {
            case "admin-freelancers" -> {
                if (!"ADMIN".equals(rol)) { denegarAcceso(resp); return; }
                Map<String, Object> datos = adminServicio.reporteTopFreelancers(desde, hasta);
                List<Map<String, Object>> lista = castLista(datos.get("topFreelancers"));
                List<String> enc = List.of("Freelancer", "Contratos", "Total generado (Q)", "Comision plataforma (Q)");
                List<List<String>> filas = new ArrayList<>();
                for (Map<String, Object> f : lista) {
                    filas.add(List.of(
                        str(f.get("nombre")), str(f.get("totalContratos")),
                        str(f.get("totalGenerado")), str(f.get("comisionPlataforma"))
                    ));
                }
                pdf = pdfServicio.generarReporte("Top 5 Freelancers", "Periodo: " + desde + " al " + hasta, enc, filas);
                nombreArchivo = "reporte-freelancers.pdf";
            }
            case "admin-categorias" -> {
                if (!"ADMIN".equals(rol)) { denegarAcceso(resp); return; }
                Map<String, Object> datos = adminServicio.reporteTopCategorias(desde, hasta);
                List<Map<String, Object>> lista = castLista(datos.get("topCategorias"));
                List<String> enc = List.of("Categoria", "Contratos", "Comisiones generadas (Q)");
                List<List<String>> filas = new ArrayList<>();
                for (Map<String, Object> f : lista) {
                    filas.add(List.of(str(f.get("categoria")), str(f.get("totalContratos")), str(f.get("totalComisiones"))));
                }
                pdf = pdfServicio.generarReporte("Top 5 Categorias", "Periodo: " + desde + " al " + hasta, enc, filas);
                nombreArchivo = "reporte-categorias.pdf";
            }
            case "admin-ingresos" -> {
                if (!"ADMIN".equals(rol)) { denegarAcceso(resp); return; }
                Map<String, Object> datos = adminServicio.reporteIngresos(desde, hasta);
                List<List<String>> filas = List.of(
                    List.of(str(datos.get("totalContratos")), str(datos.get("totalComisiones")))
                );
                pdf = pdfServicio.generarReporte("Ingresos de la plataforma", "Periodo: " + desde + " al " + hasta,
                    List.of("Contratos completados", "Total comisiones (Q)"), filas);
                nombreArchivo = "reporte-ingresos.pdf";
            }
            case "cliente-proyectos" -> {
                if (!"CLIENTE".equals(rol)) { denegarAcceso(resp); return; }
                Map<String, Object> datos = reporteServicio.reporteProyectosCliente(idUsuario, desde, hasta);
                List<Map<String, Object>> lista = castLista(datos.get("proyectos"));
                List<String> enc = List.of("Proyecto", "Estado", "Monto (Q)", "Freelancer", "Fecha");
                List<List<String>> filas = new ArrayList<>();
                for (Map<String, Object> f : lista) {
                    filas.add(List.of(str(f.get("titulo")), str(f.get("estado")), str(f.get("monto")), str(f.get("freelancer")), str(f.get("fechaPublicacion"))));
                }
                pdf = pdfServicio.generarReporte("Historial de proyectos", "Periodo: " + desde + " al " + hasta, enc, filas);
                nombreArchivo = "reporte-proyectos.pdf";
            }
            case "cliente-categorias" -> {
                if (!"CLIENTE".equals(rol)) { denegarAcceso(resp); return; }
                Map<String, Object> datos = reporteServicio.reporteGastoPorCategoria(idUsuario, desde, hasta);
                List<Map<String, Object>> lista = castLista(datos.get("gastoCategoria"));
                List<String> enc = List.of("Categoria", "Contratos", "Total gastado (Q)");
                List<List<String>> filas = new ArrayList<>();
                for (Map<String, Object> f : lista) {
                    filas.add(List.of(str(f.get("categoria")), str(f.get("totalContratos")), str(f.get("totalGastado"))));
                }
                pdf = pdfServicio.generarReporte("Gasto por categoria", "Periodo: " + desde + " al " + hasta, enc, filas);
                nombreArchivo = "reporte-gasto-categoria.pdf";
            }
            case "cliente-recargas" -> {
                if (!"CLIENTE".equals(rol)) { denegarAcceso(resp); return; }
                Map<String, Object> datos = reporteServicio.reporteRecargasCliente(idUsuario);
                List<Map<String, Object>> lista = castLista(datos.get("recargas"));
                List<String> enc = List.of("Monto (Q)", "Descripcion", "Fecha");
                List<List<String>> filas = new ArrayList<>();
                for (Map<String, Object> f : lista) {
                    filas.add(List.of(str(f.get("monto")), str(f.get("descripcion")), str(f.get("fecha"))));
                }
                pdf = pdfServicio.generarReporte("Historial de recargas", "Todas las recargas registradas", enc, filas);
                nombreArchivo = "reporte-recargas.pdf";
            }
            case "freelancer-contratos" -> {
                if (!"FREELANCER".equals(rol)) { denegarAcceso(resp); return; }
                Map<String, Object> datos = reporteServicio.reporteContratosFreelancer(idUsuario, desde, hasta);
                List<Map<String, Object>> lista = castLista(datos.get("contratos"));
                List<String> enc = List.of("Proyecto", "Cliente", "Monto neto (Q)", "Calificacion", "Fecha");
                List<List<String>> filas = new ArrayList<>();
                for (Map<String, Object> f : lista) {
                    filas.add(List.of(str(f.get("proyecto")), str(f.get("cliente")), str(f.get("montoNeto")), str(f.get("puntuacion")), str(f.get("fechaFin"))));
                }
                pdf = pdfServicio.generarReporte("Contratos completados", "Periodo: " + desde + " al " + hasta, enc, filas);
                nombreArchivo = "reporte-contratos.pdf";
            }
            case "freelancer-propuestas" -> {
                if (!"FREELANCER".equals(rol)) { denegarAcceso(resp); return; }
                Map<String, Object> datos = reporteServicio.reportePropuestasFreelancer(idUsuario, desde, hasta);
                List<Map<String, Object>> lista = castLista(datos.get("propuestas"));
                List<String> enc = List.of("Proyecto", "Monto ofertado (Q)", "Estado", "Fecha envio");
                List<List<String>> filas = new ArrayList<>();
                for (Map<String, Object> f : lista) {
                    filas.add(List.of(str(f.get("proyecto")), str(f.get("montoOfertado")), str(f.get("estado")), str(f.get("fechaEnvio"))));
                }
                pdf = pdfServicio.generarReporte("Propuestas enviadas", "Periodo: " + desde + " al " + hasta, enc, filas);
                nombreArchivo = "reporte-propuestas.pdf";
            }
            case "freelancer-categorias" -> {
                if (!"FREELANCER".equals(rol)) { denegarAcceso(resp); return; }
                Map<String, Object> datos = reporteServicio.reporteTopCategoriasFreelancer(idUsuario);
                List<Map<String, Object>> lista = castLista(datos.get("topCategorias"));
                List<String> enc = List.of("Categoria", "Contratos", "Total ingresos (Q)");
                List<List<String>> filas = new ArrayList<>();
                for (Map<String, Object> f : lista) {
                    filas.add(List.of(str(f.get("categoria")), str(f.get("totalContratos")), str(f.get("totalIngresos"))));
                }
                pdf = pdfServicio.generarReporte("Top categorias trabajadas", "Acumulado historico", enc, filas);
                nombreArchivo = "reporte-top-categorias.pdf";
            }
            default -> {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                resp.setContentType("application/json;charset=UTF-8");
                resp.getWriter().write(GsonUtil.toJson(new Respuesta(false, "Tipo de reporte no reconocido", null)));
                return;
            }
        }

        if (pdf != null && pdf.length > 0) {
            resp.setContentType("application/pdf");
            resp.setHeader("Content-Disposition", "attachment; filename=\"" + nombreArchivo + "\"");
            resp.setContentLength(pdf.length);
            resp.getOutputStream().write(pdf);
        } else {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.setContentType("application/json;charset=UTF-8");
            resp.getWriter().write(GsonUtil.toJson(new Respuesta(false, "Error al generar el PDF", null)));
        }
    }

    private void denegarAcceso(HttpServletResponse resp) throws IOException {
        resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
        resp.setContentType("application/json;charset=UTF-8");
        resp.getWriter().write(GsonUtil.toJson(new Respuesta(false, "Acceso denegado", null)));
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> castLista(Object obj) {
        if (obj instanceof List<?>) return (List<Map<String, Object>>) obj;
        return new ArrayList<>();
    }

    private String str(Object obj) {
        if (obj == null) return "—";
        return obj.toString();
    }
}