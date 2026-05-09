/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.proyf.proyectofinal.daos;
import com.proyf.proyectofinal.conexion.Conexion;
import com.proyf.proyectofinal.modelo.Proyecto;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author ludwi
 */

public class ProyectoDAO {

    private Proyecto mapear(ResultSet rs) throws SQLException {
        Proyecto p = new Proyecto();
        p.setIdProyecto(rs.getInt("id_proyecto"));
        p.setIdCliente(rs.getInt("id_cliente"));
        p.setIdCategoria(rs.getInt("id_categoria"));
        p.setTitulo(rs.getString("titulo"));
        p.setDescripcion(rs.getString("descripcion"));
        p.setPresupuestoMax(rs.getBigDecimal("presupuesto_max"));
        if (rs.getDate("fecha_limite") != null) {
            p.setFechaLimite(rs.getDate("fecha_limite").toLocalDate());
        }
        p.setEstado(rs.getString("estado"));
        if (rs.getTimestamp("fecha_publicacion") != null) {
            p.setFechaPublicacion(rs.getTimestamp("fecha_publicacion").toLocalDateTime());
        }
        try {
            p.setNombreCliente(rs.getString("nombre_cliente"));
        } catch (SQLException ignored) {}
        try {
            p.setNombreCategoria(rs.getString("nombre_categoria"));
        } catch (SQLException ignored) {}
        return p;
    }

    public int insertar(Proyecto p) {
        Connection conn = Conexion.obtenerConexion();
        if (conn == null) return -1;
        try {
            conn.setAutoCommit(false);
            PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO proyecto (id_cliente, id_categoria, titulo, descripcion, presupuesto_max, fecha_limite) " +
                "VALUES (?, ?, ?, ?, ?, ?)", Statement.RETURN_GENERATED_KEYS);
            ps.setInt(1, p.getIdCliente());
            ps.setInt(2, p.getIdCategoria());
            ps.setString(3, p.getTitulo());
            ps.setString(4, p.getDescripcion());
            ps.setBigDecimal(5, p.getPresupuestoMax());
            ps.setDate(6, Date.valueOf(p.getFechaLimite()));
            ps.executeUpdate();

            ResultSet rs = ps.getGeneratedKeys();
            int idGenerado = -1;
            if (rs.next()) idGenerado = rs.getInt(1);

            if (idGenerado > 0 && p.getHabilidades() != null) {
                for (var h : p.getHabilidades()) {
                    PreparedStatement ph = conn.prepareStatement(
                        "INSERT INTO proyecto_habilidad (id_proyecto, id_habilidad) VALUES (?, ?)");
                    ph.setInt(1, idGenerado);
                    ph.setInt(2, h.getIdHabilidad());
                    ph.executeUpdate();
                }
            }
            conn.commit();
            return idGenerado;
        } catch (SQLException e) {
            try { if (conn != null) conn.rollback(); } catch (SQLException ex) { System.err.println(ex.getMessage()); }
            System.err.println("Error insertar proyecto: " + e.getMessage());
        } finally {
            try { if (conn != null) conn.setAutoCommit(true); } catch (SQLException e) { System.err.println(e.getMessage()); }
            new Conexion().desconectar(conn);
        }
        return -1;
    }

    public Proyecto buscarPorId(int id) {
        Connection conn = Conexion.obtenerConexion();
        if (conn == null) return null;
        try {
            PreparedStatement ps = conn.prepareStatement(
                "SELECT p.*, u.nombre_completo AS nombre_cliente, c.nombre AS nombre_categoria " +
                "FROM proyecto p JOIN usuario u ON p.id_cliente = u.id_usuario " +
                "JOIN categoria c ON p.id_categoria = c.id_categoria WHERE p.id_proyecto = ?");
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapear(rs);
        } catch (SQLException e) {
            System.err.println("Error buscarPorId proyecto: " + e.getMessage());
        } finally {
            new Conexion().desconectar(conn);
        }
        return null;
    }

    public List<Proyecto> listarPorCliente(int idCliente) {
        Connection conn = Conexion.obtenerConexion();
        List<Proyecto> lista = new ArrayList<>();
        if (conn == null) return lista;
        try {
            PreparedStatement ps = conn.prepareStatement(
                "SELECT p.*, u.nombre_completo AS nombre_cliente, c.nombre AS nombre_categoria " +
                "FROM proyecto p JOIN usuario u ON p.id_cliente = u.id_usuario " +
                "JOIN categoria c ON p.id_categoria = c.id_categoria " +
                "WHERE p.id_cliente = ? ORDER BY p.fecha_publicacion DESC");
            ps.setInt(1, idCliente);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                lista.add(mapear(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error listarPorCliente proyecto: " + e.getMessage());
        } finally {
            new Conexion().desconectar(conn);
        }
        return lista;
    }

    public List<Proyecto> listarAbiertos(Integer idCategoria, java.math.BigDecimal presMin, java.math.BigDecimal presMax) {
        Connection conn = Conexion.obtenerConexion();
        List<Proyecto> lista = new ArrayList<>();
        if (conn == null) return lista;
        try {
            StringBuilder sql = new StringBuilder(
                "SELECT p.*, u.nombre_completo AS nombre_cliente, c.nombre AS nombre_categoria " +
                "FROM proyecto p JOIN usuario u ON p.id_cliente = u.id_usuario " +
                "JOIN categoria c ON p.id_categoria = c.id_categoria WHERE p.estado = 'ABIERTO'");
            
            if (idCategoria != null) sql.append(" AND p.id_categoria = ?");
            if (presMin != null) sql.append(" AND p.presupuesto_max >= ?");
            if (presMax != null) sql.append(" AND p.presupuesto_max <= ?");
            
            sql.append(" ORDER BY p.fecha_publicacion DESC");
            
            PreparedStatement ps = conn.prepareStatement(sql.toString());
            int i = 1;
            if (idCategoria != null) ps.setInt(i++, idCategoria);
            if (presMin != null) ps.setBigDecimal(i++, presMin);
            if (presMax != null) ps.setBigDecimal(i, presMax);
            
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                lista.add(mapear(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error listarAbiertos proyecto: " + e.getMessage());
        } finally {
            new Conexion().desconectar(conn);
        }
        return lista;
    }

    public boolean actualizarEstado(Connection conn, int idProyecto, String estado) throws SQLException {
        PreparedStatement ps = conn.prepareStatement("UPDATE proyecto SET estado = ? WHERE id_proyecto = ?");
        ps.setString(1, estado);
        ps.setInt(2, idProyecto);
        return ps.executeUpdate() > 0;
    }

    public boolean actualizarEstado(int idProyecto, String estado) {
        Connection conn = Conexion.obtenerConexion();
        if (conn == null) return false;
        try {
            PreparedStatement ps = conn.prepareStatement("UPDATE proyecto SET estado = ? WHERE id_proyecto = ?");
            ps.setString(1, estado);
            ps.setInt(2, idProyecto);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error actualizarEstado proyecto: " + e.getMessage());
        } finally {
            new Conexion().desconectar(conn);
        }
        return false;
    }

    public boolean actualizar(Proyecto p) {
        Connection conn = Conexion.obtenerConexion();
        if (conn == null) return false;
        try {
            conn.setAutoCommit(false);
            PreparedStatement ps = conn.prepareStatement(
                "UPDATE proyecto SET id_categoria = ?, titulo = ?, descripcion = ?, " +
                "presupuesto_max = ?, fecha_limite = ? WHERE id_proyecto = ? AND id_cliente = ? AND estado = 'ABIERTO'");
            ps.setInt(1, p.getIdCategoria());
            ps.setString(2, p.getTitulo());
            ps.setString(3, p.getDescripcion());
            ps.setBigDecimal(4, p.getPresupuestoMax());
            ps.setDate(5, Date.valueOf(p.getFechaLimite()));
            ps.setInt(6, p.getIdProyecto());
            ps.setInt(7, p.getIdCliente());

            int filas = ps.executeUpdate();
            if (filas == 0) {
                conn.rollback();
                return false;
            }

            PreparedStatement psDel = conn.prepareStatement("DELETE FROM proyecto_habilidad WHERE id_proyecto = ?");
            psDel.setInt(1, p.getIdProyecto());
            psDel.executeUpdate();

            if (p.getHabilidades() != null) {
                for (var h : p.getHabilidades()) {
                    PreparedStatement ph = conn.prepareStatement(
                        "INSERT INTO proyecto_habilidad (id_proyecto, id_habilidad) VALUES (?, ?)");
                    ph.setInt(1, p.getIdProyecto());
                    ph.setInt(2, h.getIdHabilidad());
                    ph.executeUpdate();
                }
            }
            conn.commit();
            return true;
        } catch (SQLException e) {
            try { if (conn != null) conn.rollback(); } catch (SQLException ex) { System.err.println(ex.getMessage()); }
            System.err.println("Error actualizar proyecto: " + e.getMessage());
        } finally {
            try { if (conn != null) conn.setAutoCommit(true); } catch (SQLException e) { System.err.println(e.getMessage()); }
            new Conexion().desconectar(conn);
        }
        return false;
    }
}
