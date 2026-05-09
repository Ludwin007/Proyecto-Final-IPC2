/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.proyf.proyectofinal.daos;
import com.proyf.proyectofinal.conexion.Conexion;
import com.proyf.proyectofinal.modelo.Habilidad;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author ludwi
 */

public class HabilidadDAO {

    private Habilidad mapear(ResultSet rs) throws SQLException {
        Habilidad h = new Habilidad();
        h.setIdHabilidad(rs.getInt("id_habilidad"));
        h.setIdCategoria(rs.getInt("id_categoria"));
        h.setNombre(rs.getString("nombre"));
        h.setDescripcion(rs.getString("descripcion"));
        h.setActivo(rs.getBoolean("activo"));
        try {
            h.setNombreCategoria(rs.getString("nombre_categoria"));
        } catch (SQLException ignored) {}
        return h;
    }

    public List<Habilidad> listarActivas() {
        Connection conn = Conexion.obtenerConexion();
        List<Habilidad> lista = new ArrayList<>();
        if (conn == null) return lista;
        try {
            PreparedStatement ps = conn.prepareStatement(
                "SELECT h.*, c.nombre AS nombre_categoria FROM habilidad h " +
                "JOIN categoria c ON h.id_categoria = c.id_categoria " +
                "WHERE h.activo = 1 ORDER BY c.nombre, h.nombre");
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                lista.add(mapear(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error listarActivas habilidades: " + e.getMessage());
        } finally {
            new Conexion().desconectar(conn);
        }
        return lista;
    }

    public List<Habilidad> listarPorCategoria(int idCategoria) {
        Connection conn = Conexion.obtenerConexion();
        List<Habilidad> lista = new ArrayList<>();
        if (conn == null) return lista;
        try {
            PreparedStatement ps = conn.prepareStatement(
                "SELECT h.*, c.nombre AS nombre_categoria FROM habilidad h " +
                "JOIN categoria c ON h.id_categoria = c.id_categoria " +
                "WHERE h.id_categoria = ? ORDER BY h.nombre");
            ps.setInt(1, idCategoria);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                lista.add(mapear(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error listarPorCategoria habilidades: " + e.getMessage());
        } finally {
            new Conexion().desconectar(conn);
        }
        return lista;
    }

    public List<Habilidad> listarDeFreelancer(int idUsuario) {
        Connection conn = Conexion.obtenerConexion();
        List<Habilidad> lista = new ArrayList<>();
        if (conn == null) return lista;
        try {
            PreparedStatement ps = conn.prepareStatement(
                "SELECT h.*, c.nombre AS nombre_categoria FROM habilidad h " +
                "JOIN categoria c ON h.id_categoria = c.id_categoria " +
                "JOIN freelancer_habilidad fh ON fh.id_habilidad = h.id_habilidad " +
                "WHERE fh.id_usuario = ?");
            ps.setInt(1, idUsuario);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                lista.add(mapear(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error listarDeFreelancer habilidades: " + e.getMessage());
        } finally {
            new Conexion().desconectar(conn);
        }
        return lista;
    }

    public List<Habilidad> listarDeProyecto(int idProyecto) {
        Connection conn = Conexion.obtenerConexion();
        List<Habilidad> lista = new ArrayList<>();
        if (conn == null) return lista;
        try {
            PreparedStatement ps = conn.prepareStatement(
                "SELECT h.*, c.nombre AS nombre_categoria FROM habilidad h " +
                "JOIN categoria c ON h.id_categoria = c.id_categoria " +
                "JOIN proyecto_habilidad ph ON ph.id_habilidad = h.id_habilidad " +
                "WHERE ph.id_proyecto = ?");
            ps.setInt(1, idProyecto);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                lista.add(mapear(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error listarDeProyecto habilidades: " + e.getMessage());
        } finally {
            new Conexion().desconectar(conn);
        }
        return lista;
    }

    public boolean insertar(Habilidad h) {
        Connection conn = Conexion.obtenerConexion();
        if (conn == null) return false;
        try {
            PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO habilidad (id_categoria, nombre, descripcion) VALUES (?, ?, ?)");
            ps.setInt(1, h.getIdCategoria());
            ps.setString(2, h.getNombre());
            ps.setString(3, h.getDescripcion());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error insertar habilidad: " + e.getMessage());
        } finally {
            new Conexion().desconectar(conn);
        }
        return false;
    }

    public boolean actualizar(Habilidad h) {
        Connection conn = Conexion.obtenerConexion();
        if (conn == null) return false;
        try {
            PreparedStatement ps = conn.prepareStatement(
                "UPDATE habilidad SET nombre = ?, descripcion = ? WHERE id_habilidad = ?");
            ps.setString(1, h.getNombre());
            ps.setString(2, h.getDescripcion());
            ps.setInt(3, h.getIdHabilidad());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error actualizar habilidad: " + e.getMessage());
        } finally {
            new Conexion().desconectar(conn);
        }
        return false;
    }

    public boolean cambiarEstado(int idHabilidad, boolean activo) {
        Connection conn = Conexion.obtenerConexion();
        if (conn == null) return false;
        try {
            PreparedStatement ps = conn.prepareStatement("UPDATE habilidad SET activo = ? WHERE id_habilidad = ?");
            ps.setBoolean(1, activo);
            ps.setInt(2, idHabilidad);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error cambiarEstado habilidad: " + e.getMessage());
        } finally {
            new Conexion().desconectar(conn);
        }
        return false;
    }

    public boolean freelancerTieneHabilidadDeProyecto(int idFreelancer, int idProyecto) {
        Connection conn = Conexion.obtenerConexion();
        if (conn == null) return false;
        try {
            PreparedStatement ps = conn.prepareStatement(
                "SELECT COUNT(*) FROM freelancer_habilidad fh " +
                "JOIN proyecto_habilidad ph ON fh.id_habilidad = ph.id_habilidad " +
                "WHERE fh.id_usuario = ? AND ph.id_proyecto = ?");
            ps.setInt(1, idFreelancer);
            ps.setInt(2, idProyecto);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1) > 0;
        } catch (SQLException e) {
            System.err.println("Error freelancerTieneHabilidadDeProyecto: " + e.getMessage());
        } finally {
            new Conexion().desconectar(conn);
        }
        return false;
    }
}
