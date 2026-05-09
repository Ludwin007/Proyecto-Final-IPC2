/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.proyf.proyectofinal.servicio;

import com.proyf.proyectofinal.conexion.Conexion;
import com.proyf.proyectofinal.daos.RecargaDAO;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.*;

/**
 *
 * @author ludwi
 */

public class ReporteServicio {

    private final RecargaDAO recargaDAO = new RecargaDAO();


    public Map<String, Object> reporteProyectosCliente(int idCliente, String desde, String hasta) {
        Map<String, Object> resultado = new HashMap<>();
        Connection conn = Conexion.obtenerConexion();
        if (conn == null) {
            resultado.put("error", "Error de conexion");
            return resultado;
        }
        try {
            PreparedStatement ps = conn.prepareStatement(
                "SELECT p.titulo, p.estado, c.monto, u.nombre_completo AS nombre_freelancer, p.fecha_publicacion " +
                "FROM proyecto p " +
                "LEFT JOIN contrato c ON p.id_proyecto = c.id_proyecto " +
                "LEFT JOIN usuario u ON c.id_freelancer = u.id_usuario " +
                "WHERE p.id_cliente = ? AND p.fecha_publicacion BETWEEN ? AND ? " +
                "ORDER BY p.fecha_publicacion DESC");
            ps.setInt(1, idCliente);
            ps.setString(2, desde + " 00:00:00");
            ps.setString(3, hasta + " 23:59:59");

            ResultSet rs = ps.executeQuery();
            List<Map<String, Object>> lista = new ArrayList<>();
            while (rs.next()) {
                Map<String, Object> fila = new LinkedHashMap<>();
                fila.put("titulo", rs.getString("titulo"));
                fila.put("estado", rs.getString("estado"));
                fila.put("monto", rs.getBigDecimal("monto"));
                fila.put("freelancer", rs.getString("nombre_freelancer"));
                fila.put("fechaPublicacion", rs.getTimestamp("fecha_publicacion"));
                lista.add(fila);
            }
            resultado.put("proyectos", lista);
        } catch (Exception e) {
            System.err.println("Error reporteProyectosCliente: " + e.getMessage());
            resultado.put("error", "Error al generar el reporte");
        } finally {
            new Conexion().desconectar(conn);
        }
        return resultado;
    }

    public Map<String, Object> reporteRecargasCliente(int idCliente) {
        Map<String, Object> resultado = new HashMap<>();
        resultado.put("recargas", recargaDAO.listarPorCliente(idCliente));
        return resultado;
    }

    public Map<String, Object> reporteGastoPorCategoria(int idCliente, String desde, String hasta) {
        Map<String, Object> resultado = new HashMap<>();
        Connection conn = Conexion.obtenerConexion();
        if (conn == null) {
            resultado.put("error", "Error de conexion");
            return resultado;
        }
        try {
            PreparedStatement ps = conn.prepareStatement(
                "SELECT ca.nombre, COUNT(co.id_contrato) AS total_contratos, SUM(co.monto) AS total_gastado " +
                "FROM contrato co JOIN proyecto p ON co.id_proyecto = p.id_proyecto " +
                "JOIN categoria ca ON p.id_categoria = ca.id_categoria " +
                "WHERE co.id_cliente = ? AND co.estado = 'COMPLETADO' AND co.fecha_fin BETWEEN ? AND ? " +
                "GROUP BY p.id_categoria, ca.nombre ORDER BY total_gastado DESC");
            ps.setInt(1, idCliente);
            ps.setString(2, desde + " 00:00:00");
            ps.setString(3, hasta + " 23:59:59");

            ResultSet rs = ps.executeQuery();
            List<Map<String, Object>> lista = new ArrayList<>();
            while (rs.next()) {
                Map<String, Object> fila = new LinkedHashMap<>();
                fila.put("categoria", rs.getString("nombre"));
                fila.put("totalContratos", rs.getInt("total_contratos"));
                fila.put("totalGastado", rs.getBigDecimal("total_gastado"));
                lista.add(fila);
            }
            resultado.put("gastoCategoria", lista);
        } catch (Exception e) {
            System.err.println("Error reporteGastoPorCategoria: " + e.getMessage());
            resultado.put("error", "Error al generar el reporte");
        } finally {
            new Conexion().desconectar(conn);
        }
        return resultado;
    }

    public Map<String, Object> reporteContratosFreelancer(int idFreelancer, String desde, String hasta) {
        Map<String, Object> resultado = new HashMap<>();
        Connection conn = Conexion.obtenerConexion();
        if (conn == null) {
            resultado.put("error", "Error de conexion");
            return resultado;
        }
        try {
            PreparedStatement ps = conn.prepareStatement(
                "SELECT u.nombre_completo AS cliente, p.titulo AS proyecto, " +
                "co.monto - (co.monto * co.porc_comision / 100) AS monto_neto, " +
                "cal.puntuacion, co.fecha_fin " +
                "FROM contrato co JOIN proyecto p ON co.id_proyecto = p.id_proyecto " +
                "JOIN usuario u ON co.id_cliente = u.id_usuario " +
                "LEFT JOIN calificacion cal ON co.id_contrato = cal.id_contrato " +
                "WHERE co.id_freelancer = ? AND co.estado = 'COMPLETADO' AND co.fecha_fin BETWEEN ? AND ? " +
                "ORDER BY co.fecha_fin DESC");
            ps.setInt(1, idFreelancer);
            ps.setString(2, desde + " 00:00:00");
            ps.setString(3, hasta + " 23:59:59");

            ResultSet rs = ps.executeQuery();
            List<Map<String, Object>> lista = new ArrayList<>();
            while (rs.next()) {
                Map<String, Object> fila = new LinkedHashMap<>();
                fila.put("cliente", rs.getString("cliente"));
                fila.put("proyecto", rs.getString("proyecto"));
                fila.put("montoNeto", rs.getBigDecimal("monto_neto"));
                fila.put("puntuacion", rs.getObject("puntuacion"));
                fila.put("fechaFin", rs.getTimestamp("fecha_fin"));
                lista.add(fila);
            }
            resultado.put("contratos", lista);
        } catch (Exception e) {
            System.err.println("Error reporteContratosFreelancer: " + e.getMessage());
            resultado.put("error", "Error al generar el reporte");
        } finally {
            new Conexion().desconectar(conn);
        }
        return resultado;
    }

    public Map<String, Object> reporteTopCategoriasFreelancer(int idFreelancer) {
        Map<String, Object> resultado = new HashMap<>();
        Connection conn = Conexion.obtenerConexion();
        if (conn == null) {
            resultado.put("error", "Error de conexion");
            return resultado;
        }
        try {
            PreparedStatement ps = conn.prepareStatement(
                "SELECT ca.nombre, COUNT(co.id_contrato) AS total_contratos, " +
                "SUM(co.monto - (co.monto * co.porc_comision / 100)) AS total_ingresos " +
                "FROM contrato co JOIN proyecto p ON co.id_proyecto = p.id_proyecto " +
                "JOIN categoria ca ON p.id_categoria = ca.id_categoria " +
                "WHERE co.id_freelancer = ? AND co.estado = 'COMPLETADO' " +
                "GROUP BY p.id_categoria, ca.nombre ORDER BY total_contratos DESC LIMIT 5");
            ps.setInt(1, idFreelancer);

            ResultSet rs = ps.executeQuery();
            List<Map<String, Object>> lista = new ArrayList<>();
            while (rs.next()) {
                Map<String, Object> fila = new LinkedHashMap<>();
                fila.put("categoria", rs.getString("nombre"));
                fila.put("totalContratos", rs.getInt("total_contratos"));
                fila.put("totalIngresos", rs.getBigDecimal("total_ingresos"));
                lista.add(fila);
            }
            resultado.put("topCategorias", lista);
        } catch (Exception e) {
            System.err.println("Error reporteTopCategoriasFreelancer: " + e.getMessage());
            resultado.put("error", "Error al generar el reporte");
        } finally {
            new Conexion().desconectar(conn);
        }
        return resultado;
    }

    public Map<String, Object> reportePropuestasFreelancer(int idFreelancer, String desde, String hasta) {
        Map<String, Object> resultado = new HashMap<>();
        Connection conn = Conexion.obtenerConexion();
        if (conn == null) {
            resultado.put("error", "Error de conexion");
            return resultado;
        }
        try {
            PreparedStatement ps = conn.prepareStatement(
                "SELECT p.titulo AS proyecto, pr.monto_ofertado, pr.estado, pr.fecha_envio " +
                "FROM propuesta pr JOIN proyecto p ON pr.id_proyecto = p.id_proyecto " +
                "WHERE pr.id_freelancer = ? AND pr.fecha_envio BETWEEN ? AND ? " +
                "ORDER BY pr.fecha_envio DESC");
            ps.setInt(1, idFreelancer);
            ps.setString(2, desde + " 00:00:00");
            ps.setString(3, hasta + " 23:59:59");

            ResultSet rs = ps.executeQuery();
            List<Map<String, Object>> lista = new ArrayList<>();
            while (rs.next()) {
                Map<String, Object> fila = new LinkedHashMap<>();
                fila.put("proyecto", rs.getString("proyecto"));
                fila.put("montoOfertado", rs.getBigDecimal("monto_ofertado"));
                fila.put("estado", rs.getString("estado"));
                fila.put("fechaEnvio", rs.getTimestamp("fecha_envio"));
                lista.add(fila);
            }
            resultado.put("propuestas", lista);
        } catch (Exception e) {
            System.err.println("Error reportePropuestasFreelancer: " + e.getMessage());
            resultado.put("error", "Error al generar el reporte");
        } finally {
            new Conexion().desconectar(conn);
        }
        return resultado;
    }
}