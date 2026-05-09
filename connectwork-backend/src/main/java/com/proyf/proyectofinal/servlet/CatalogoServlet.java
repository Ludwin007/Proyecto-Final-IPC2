/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.proyf.proyectofinal.servlet;

import com.proyf.proyectofinal.daos.CategoriaDAO;
import com.proyf.proyectofinal.daos.HabilidadDAO;
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

@WebServlet("/api/catalogo/*")
public class CatalogoServlet extends HttpServlet {
    private final CategoriaDAO categoriaDAO = new CategoriaDAO();
    private final HabilidadDAO habilidadDAO = new HabilidadDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json;charset=UTF-8");
        String ruta = req.getPathInfo();

        if ("/categorias".equals(ruta)) {
            resp.getWriter().write(GsonUtil.toJson(
                new Respuesta(true, "OK", Map.of("categorias", categoriaDAO.listarActivas()))));
        } else if ("/habilidades".equals(ruta)) {
            String catParam = req.getParameter("categoria");
            if (catParam != null) {
                int idCategoria = Integer.parseInt(catParam);
                resp.getWriter().write(GsonUtil.toJson(
                    new Respuesta(true, "OK", Map.of("habilidades", habilidadDAO.listarPorCategoria(idCategoria)))));
            } else {
                resp.getWriter().write(GsonUtil.toJson(
                    new Respuesta(true, "OK", Map.of("habilidades", habilidadDAO.listarActivas()))));
            }
        } else if ("/todo".equals(ruta)) {
            Map<String, Object> datos = new HashMap<>();
            datos.put("categorias", categoriaDAO.listarActivas());
            datos.put("habilidades", habilidadDAO.listarActivas());
            resp.getWriter().write(GsonUtil.toJson(new Respuesta(true, "OK", datos)));
        } else {
            resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
            resp.getWriter().write(GsonUtil.toJson(new Respuesta(false, "Ruta no encontrada", null)));
        }
    }
}