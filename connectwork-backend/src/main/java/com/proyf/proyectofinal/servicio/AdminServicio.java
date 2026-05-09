/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.proyf.proyectofinal.servicio;

import com.proyf.proyectofinal.conexion.Conexion;
import com.proyf.proyectofinal.daos.*;
import com.proyf.proyectofinal.modelo.*;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.*;

/**
 *
 * @author ludwi
 */

public class AdminServicio {

    private final UsuarioDAO usuarioDAO = new UsuarioDAO();
    private final CategoriaDAO categoriaDAO = new CategoriaDAO();
    private final HabilidadDAO habilidadDAO = new HabilidadDAO();
    private final ComisionDAO comisionDAO = new ComisionDAO();
    private final SolicitudCatalogoDAO solicitudDAO = new SolicitudCatalogoDAO();


    public Map<String, Object> listarUsuarios() {
        Map<String, Object> resultado = new HashMap<>();
        resultado.put("usuarios", usuarioDAO.listarClientesYFreelancers());
        return resultado;
    }

    public Map<String, Object> cambiarEstadoUsuario(int idUsuario, boolean activo) {
        Map<String, Object> resultado = new HashMap<>();
        boolean ok = usuarioDAO.actualizarActivo(idUsuario, activo);
        if (!ok) {
            resultado.put("error", "No se pudo actualizar el estado del usuario");
            return resultado;
        }
        resultado.put("mensaje", activo ? "Usuario activado exitosamente" : "Usuario desactivado exitosamente");
        return resultado;
    }


    public Map<String, Object> listarCategorias() {
        Map<String, Object> resultado = new HashMap<>();
        resultado.put("categorias", categoriaDAO.listarTodas());
        return resultado;
    }

    public Map<String, Object> crearCategoria(Categoria c) {
        Map<String, Object> resultado = new HashMap<>();
        if (c.getNombre() == null || c.getNombre().isBlank()) {
            resultado.put("error", "El nombre de la categoria es requerido");
            return resultado;
        }
        if (categoriaDAO.existeNombre(c.getNombre().trim())) {
            resultado.put("error", "Ya existe una categoria con ese nombre");
            return resultado;
        }
        boolean ok = categoriaDAO.insertar(c);
        if (!ok) {
            resultado.put("error", "No se pudo crear la categoria");
            return resultado;
        }
        resultado.put("mensaje", "Categoria creada exitosamente");
        return resultado;
    }

    public Map<String, Object> editarCategoria(Categoria c) {
        Map<String, Object> resultado = new HashMap<>();
        if (c.getNombre() == null || c.getNombre().isBlank()) {
            resultado.put("error", "El nombre de la categoria es requerido");
            return resultado;
        }
        boolean ok = categoriaDAO.actualizar(c);
        if (!ok) {
            resultado.put("error", "No se pudo actualizar la categoria");
            return resultado;
        }
        resultado.put("mensaje", "Categoria actualizada exitosamente");
        return resultado;
    }

    public Map<String, Object> cambiarEstadoCategoria(int idCategoria, boolean activo) {
        Map<String, Object> resultado = new HashMap<>();
        boolean ok = categoriaDAO.cambiarEstado(idCategoria, activo);
        if (!ok) {
            resultado.put("error", "No se pudo cambiar el estado de la categoria");
            return resultado;
        }
        resultado.put("mensaje", activo ? "Categoria activada" : "Categoria desactivada");
        return resultado;
    }


    public Map<String, Object> listarHabilidades(int idCategoria) {
        Map<String, Object> resultado = new HashMap<>();
        resultado.put("habilidades", habilidadDAO.listarPorCategoria(idCategoria));
        return resultado;
    }

    public Map<String, Object> crearHabilidad(Habilidad h) {
        Map<String, Object> resultado = new HashMap<>();
        if (h.getNombre() == null || h.getNombre().isBlank()) {
            resultado.put("error", "El nombre de la habilidad es requerido");
            return resultado;
        }
        if (h.getIdCategoria() <= 0) {
            resultado.put("error", "Debe seleccionar una categoria para la habilidad");
            return resultado;
        }
        boolean ok = habilidadDAO.insertar(h);
        if (!ok) {
            resultado.put("error", "No se pudo crear la habilidad");
            return resultado;
        }
        resultado.put("mensaje", "Habilidad creada exitosamente");
        return resultado;
    }

    public Map<String, Object> editarHabilidad(Habilidad h) {
        Map<String, Object> resultado = new HashMap<>();
        if (h.getNombre() == null || h.getNombre().isBlank()) {
            resultado.put("error", "El nombre es requerido");
            return resultado;
        }
        boolean ok = habilidadDAO.actualizar(h);
        if (!ok) {
            resultado.put("error", "No se pudo actualizar la habilidad");
            return resultado;
        }
        resultado.put("mensaje", "Habilidad actualizada exitosamente");
        return resultado;
    }

    public Map<String, Object> cambiarEstadoHabilidad(int idHabilidad, boolean activo) {
        Map<String, Object> resultado = new HashMap<>();
        boolean ok = habilidadDAO.cambiarEstado(idHabilidad, activo);
        if (!ok) {
            resultado.put("error", "No se pudo cambiar el estado de la habilidad");
            return resultado;
        }
        resultado.put("mensaje", activo ? "Habilidad activada" : "Habilidad desactivada");
        return resultado;
    }


    public Map<String, Object> obtenerComision() {
        Map<String, Object> resultado = new HashMap<>();
        resultado.put("vigente", comisionDAO.obtenerVigente());
        resultado.put("historial", comisionDAO.listarHistorial());
        return resultado;
    }

    public Map<String, Object> actualizarComision(BigDecimal nuevoPorcentaje) {
        Map<String, Object> resultado = new HashMap<>();
        if (nuevoPorcentaje == null || nuevoPorcentaje.compareTo(BigDecimal.ZERO) < 0 || nuevoPorcentaje.compareTo(BigDecimal.valueOf(100)) > 0) {
            resultado.put("error", "El porcentaje debe estar entre 0 y 100");
            return resultado;
        }
        ComisionConfig actual = comisionDAO.obtenerVigente();
        if (actual != null && actual.getPorcentaje().compareTo(nuevoPorcentaje) == 0) {
            resultado.put("error", "El porcentaje ingresado es igual al actual");
            return resultado;
        }
        boolean ok = comisionDAO.actualizarComision(nuevoPorcentaje);
        if (!ok) {
            resultado.put("error", "No se pudo actualizar la comision");
            return resultado;
        }
        resultado.put("mensaje", "Porcentaje de comision actualizado a " + nuevoPorcentaje + "%");
        return resultado;
    }


    public Map<String, Object> listarSolicitudesPendientes() {
        Map<String, Object> resultado = new HashMap<>();
        resultado.put("solicitudes", solicitudDAO.listarPendientes());
        return resultado;
    }

    public Map<String, Object> resolverSolicitud(int idSolicitud, String decision, String respuesta, Integer idCategoriaPadre) {
        Map<String, Object> resultado = new HashMap<>();
        SolicitudCatalogo solicitud = solicitudDAO.buscarPorId(idSolicitud);
        
        if (solicitud == null || !solicitud.getEstado().equals("PENDIENTE")) {
            resultado.put("error", "La solicitud no existe o ya fue procesada");
            return resultado;
        }

        if (decision.equals("ACEPTADA")) {
            if (solicitud.getTipo().equals("CATEGORIA")) {
                Categoria nueva = new Categoria();
                nueva.setNombre(solicitud.getNombre());
                nueva.setDescripcion(solicitud.getDescripcion());
                categoriaDAO.insertar(nueva);
            } else if (solicitud.getTipo().equals("HABILIDAD")) {
                if (idCategoriaPadre == null || idCategoriaPadre <= 0) {
                    resultado.put("error", "Debe indicar la categoria a la que pertenece la habilidad");
                    return resultado;
                }
                Habilidad nueva = new Habilidad();
                nueva.setNombre(solicitud.getNombre());
                nueva.setDescripcion(solicitud.getDescripcion());
                nueva.setIdCategoria(idCategoriaPadre);
                habilidadDAO.insertar(nueva);
            }
        } else if (!decision.equals("RECHAZADA")) {
            resultado.put("error", "La decision debe ser ACEPTADA o RECHAZADA");
            return resultado;
        }

        if (decision.equals("RECHAZADA") && (respuesta == null || respuesta.isBlank())) {
            resultado.put("error", "El motivo de rechazo es obligatorio");
            return resultado;
        }

        solicitudDAO.resolver(idSolicitud, decision, respuesta);
        resultado.put("mensaje", "Solicitud procesada exitosamente");
        return resultado;
    }


    public Map<String, Object> reporteTopFreelancers(String desde, String hasta) {
        Map<String, Object> resultado = new HashMap<>();
        Connection conn = Conexion.obtenerConexion();
        if (conn == null) {
            resultado.put("error", "Error de conexion");
            return resultado;
        }
        try {
            PreparedStatement ps = conn.prepareStatement(
                "SELECT u.nombre_completo, COUNT(c.id_contrato) AS total_contratos, " +
                "SUM(c.monto) AS total_generado, " +
                "SUM(c.monto * c.porc_comision / 100) AS comision_plataforma " +
                "FROM contrato c JOIN usuario u ON c.id_freelancer = u.id_usuario " +
                "WHERE c.estado = 'COMPLETADO' AND c.fecha_fin BETWEEN ? AND ? " +
                "GROUP BY c.id_freelancer, u.nombre_completo " +
                "ORDER BY total_generado DESC LIMIT 5");
            ps.setString(1, desde + " 00:00:00");
            ps.setString(2, hasta + " 23:59:59");
            
            ResultSet rs = ps.executeQuery();
            List<Map<String, Object>> lista = new ArrayList<>();
            while (rs.next()) {
                Map<String, Object> fila = new LinkedHashMap<>();
                fila.put("nombre", rs.getString("nombre_completo"));
                fila.put("totalContratos", rs.getInt("total_contratos"));
                fila.put("totalGenerado", rs.getBigDecimal("total_generado"));
                fila.put("comisionPlataforma", rs.getBigDecimal("comision_plataforma"));
                lista.add(fila);
            }
            resultado.put("topFreelancers", lista);
        } catch (Exception e) {
            System.err.println("Error reporteTopFreelancers: " + e.getMessage());
            resultado.put("error", "Error al generar el reporte");
        } finally {
            new Conexion().desconectar(conn);
        }
        return resultado;
    }

    public Map<String, Object> reporteTopCategorias(String desde, String hasta) {
        Map<String, Object> resultado = new HashMap<>();
        Connection conn = Conexion.obtenerConexion();
        if (conn == null) {
            resultado.put("error", "Error de conexion");
            return resultado;
        }
        try {
            PreparedStatement ps = conn.prepareStatement(
                "SELECT ca.nombre, COUNT(co.id_contrato) AS total_contratos, " +
                "SUM(co.monto * co.porc_comision / 100) AS total_comisiones " +
                "FROM contrato co JOIN proyecto p ON co.id_proyecto = p.id_proyecto " +
                "JOIN categoria ca ON p.id_categoria = ca.id_categoria " +
                "WHERE co.estado = 'COMPLETADO' AND co.fecha_fin BETWEEN ? AND ? " +
                "GROUP BY p.id_categoria, ca.nombre " +
                "ORDER BY total_contratos DESC LIMIT 5");
            ps.setString(1, desde + " 00:00:00");
            ps.setString(2, hasta + " 23:59:59");
            
            ResultSet rs = ps.executeQuery();
            List<Map<String, Object>> lista = new ArrayList<>();
            while (rs.next()) {
                Map<String, Object> fila = new LinkedHashMap<>();
                fila.put("categoria", rs.getString("nombre"));
                fila.put("totalContratos", rs.getInt("total_contratos"));
                fila.put("totalComisiones", rs.getBigDecimal("total_comisiones"));
                lista.add(fila);
            }
            resultado.put("topCategorias", lista);
        } catch (Exception e) {
            System.err.println("Error reporteTopCategorias: " + e.getMessage());
            resultado.put("error", "Error al generar el reporte");
        } finally {
            new Conexion().desconectar(conn);
        }
        return resultado;
    }

    public Map<String, Object> reporteIngresos(String desde, String hasta) {
        Map<String, Object> resultado = new HashMap<>();
        Connection conn = Conexion.obtenerConexion();
        if (conn == null) {
            resultado.put("error", "Error de conexion");
            return resultado;
        }
        try {
            PreparedStatement ps = conn.prepareStatement(
                "SELECT COUNT(*) AS total_contratos, " +
                "IFNULL(SUM(monto * porc_comision / 100), 0) AS total_comisiones " +
                "FROM contrato WHERE estado = 'COMPLETADO' AND fecha_fin BETWEEN ? AND ?");
            ps.setString(1, desde + " 00:00:00");
            ps.setString(2, hasta + " 23:59:59");
            
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                resultado.put("totalContratos", rs.getInt("total_contratos"));
                resultado.put("totalComisiones", rs.getBigDecimal("total_comisiones"));
            }
        } catch (Exception e) {
            System.err.println("Error reporteIngresos: " + e.getMessage());
            resultado.put("error", "Error al generar el reporte");
        } finally {
            new Conexion().desconectar(conn);
        }
        return resultado;
    }
}
