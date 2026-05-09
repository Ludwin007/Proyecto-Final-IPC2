/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.proyf.proyectofinal.daos;
import com.proyf.proyectofinal.conexion.Conexion;
import com.proyf.proyectofinal.modelo.SolicitudCatalogo;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author ludwi
 */

public class SolicitudCatalogoDAO {

    private SolicitudCatalogo mapear(ResultSet rs) throws SQLException {
        SolicitudCatalogo s = new SolicitudCatalogo();
        s.setIdSolicitud(rs.getInt("id_solicitud"));
        s.setIdUsuario(rs.getInt("id_usuario"));
        s.setTipo(rs.getString("tipo"));
        s.setNombre(rs.getString("nombre"));
        s.setDescripcion(rs.getString("descripcion"));
        s.setEstado(rs.getString("estado"));
        s.setRespuestaAdmin(rs.getString("respuesta_admin"));
        if (rs.getTimestamp("fecha") != null) {
            s.setFecha(rs.getTimestamp("fecha").toLocalDateTime());
        }
        try { s.setNombreUsuario(rs.getString("nombre_usuario")); } catch (SQLException ignored) {}
        return s;
    }

    public boolean insertar(SolicitudCatalogo s) {
        Connection conn = Conexion.obtenerConexion();
        if (conn == null) return false;
        try {
            PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO solicitud_catalogo (id_usuario, tipo, nombre, descripcion) VALUES (?, ?, ?, ?)");
            ps.setInt(1, s.getIdUsuario());
            ps.setString(2, s.getTipo());
            ps.setString(3, s.getNombre());
            ps.setString(4, s.getDescripcion());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error insertar solicitud: " + e.getMessage());
        } finally {
            new Conexion().desconectar(conn);
        }
        return false;
    }

    public List<SolicitudCatalogo> listarPendientes() {
        Connection conn = Conexion.obtenerConexion();
        List<SolicitudCatalogo> lista = new ArrayList<>();
        if (conn == null) return lista;
        try {
            PreparedStatement ps = conn.prepareStatement(
                "SELECT s.*, u.nombre_completo AS nombre_usuario FROM solicitud_catalogo s " +
                "JOIN usuario u ON s.id_usuario = u.id_usuario WHERE s.estado = 'PENDIENTE' ORDER BY s.fecha");
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                lista.add(mapear(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error listarPendientes solicitud: " + e.getMessage());
        } finally {
            new Conexion().desconectar(conn);
        }
        return lista;
    }

    public List<SolicitudCatalogo> listarPorUsuario(int idUsuario) {
        Connection conn = Conexion.obtenerConexion();
        List<SolicitudCatalogo> lista = new ArrayList<>();
        if (conn == null) return lista;
        try {
            PreparedStatement ps = conn.prepareStatement(
                "SELECT s.*, u.nombre_completo AS nombre_usuario FROM solicitud_catalogo s " +
                "JOIN usuario u ON s.id_usuario = u.id_usuario WHERE s.id_usuario = ? ORDER BY s.fecha DESC");
            ps.setInt(1, idUsuario);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                lista.add(mapear(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error listarPorUsuario solicitud: " + e.getMessage());
        } finally {
            new Conexion().desconectar(conn);
        }
        return lista;
    }

    public boolean resolver(int idSolicitud, String estado, String respuesta) {
        Connection conn = Conexion.obtenerConexion();
        if (conn == null) return false;
        try {
            PreparedStatement ps = conn.prepareStatement(
                "UPDATE solicitud_catalogo SET estado = ?, respuesta_admin = ? WHERE id_solicitud = ?");
            ps.setString(1, estado);
            ps.setString(2, respuesta);
            ps.setInt(3, idSolicitud);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error resolver solicitud: " + e.getMessage());
        } finally {
            new Conexion().desconectar(conn);
        }
        return false;
    }

    public SolicitudCatalogo buscarPorId(int id) {
        Connection conn = Conexion.obtenerConexion();
        if (conn == null) return null;
        try {
            PreparedStatement ps = conn.prepareStatement(
                "SELECT s.*, u.nombre_completo AS nombre_usuario FROM solicitud_catalogo s " +
                "JOIN usuario u ON s.id_usuario = u.id_usuario WHERE s.id_solicitud = ?");
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapear(rs);
        } catch (SQLException e) {
            System.err.println("Error buscarPorId solicitud: " + e.getMessage());
        } finally {
            new Conexion().desconectar(conn);
        }
        return null;
    }
}