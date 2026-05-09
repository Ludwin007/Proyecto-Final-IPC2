/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.proyf.proyectofinal.daos;
import com.proyf.proyectofinal.conexion.Conexion;
import com.proyf.proyectofinal.modelo.Propuesta;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author ludwi
 */

public class PropuestaDAO {

    private Propuesta mapear(ResultSet rs) throws SQLException {
        Propuesta p = new Propuesta();
        p.setIdPropuesta(rs.getInt("id_propuesta"));
        p.setIdProyecto(rs.getInt("id_proyecto"));
        p.setIdFreelancer(rs.getInt("id_freelancer"));
        p.setMontoOfertado(rs.getBigDecimal("monto_ofertado"));
        p.setPlazoDias(rs.getInt("plazo_dias"));
        p.setCartaPresentacion(rs.getString("carta_presentacion"));
        p.setEstado(rs.getString("estado"));
        if (rs.getTimestamp("fecha_envio") != null) {
            p.setFechaEnvio(rs.getTimestamp("fecha_envio").toLocalDateTime());
        }
        try {
            p.setTituloProyecto(rs.getString("titulo_proyecto"));
        } catch (SQLException ignored) {}
        try {
            p.setNombreFreelancer(rs.getString("nombre_freelancer"));
        } catch (SQLException ignored) {}
        try {
            p.setCalificacionFreelancer(rs.getBigDecimal("calificacion_prom"));
        } catch (SQLException ignored) {}
        try {
            p.setNivelExperiencia(rs.getString("nivel_experiencia"));
        } catch (SQLException ignored) {}
        return p;
    }

    public int insertar(Propuesta p) {
        Connection conn = Conexion.obtenerConexion();
        if (conn == null) return -1;
        try {
            PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO propuesta (id_proyecto, id_freelancer, monto_ofertado, plazo_dias, carta_presentacion) " +
                "VALUES (?, ?, ?, ?, ?)", Statement.RETURN_GENERATED_KEYS);
            ps.setInt(1, p.getIdProyecto());
            ps.setInt(2, p.getIdFreelancer());
            ps.setBigDecimal(3, p.getMontoOfertado());
            ps.setInt(4, p.getPlazoDias());
            ps.setString(5, p.getCartaPresentacion());
            ps.executeUpdate();
            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            System.err.println("Error insertar propuesta: " + e.getMessage());
        } finally {
            new Conexion().desconectar(conn);
        }
        return -1;
    }

    public List<Propuesta> listarPorProyecto(int idProyecto) {
        Connection conn = Conexion.obtenerConexion();
        List<Propuesta> lista = new ArrayList<>();
        if (conn == null) return lista;
        try {
            PreparedStatement ps = conn.prepareStatement(
                "SELECT pr.*, u.nombre_completo AS nombre_freelancer, " +
                "pfl.calificacion_prom, pfl.nivel_experiencia, p.titulo AS titulo_proyecto " +
                "FROM propuesta pr JOIN usuario u ON pr.id_freelancer = u.id_usuario " +
                "LEFT JOIN perfil_freelancer pfl ON u.id_usuario = pfl.id_usuario " +
                "JOIN proyecto p ON pr.id_proyecto = p.id_proyecto " +
                "WHERE pr.id_proyecto = ? ORDER BY pr.fecha_envio DESC");
            ps.setInt(1, idProyecto);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                lista.add(mapear(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error listarPorProyecto propuesta: " + e.getMessage());
        } finally {
            new Conexion().desconectar(conn);
        }
        return lista;
    }

    public List<Propuesta> listarPorFreelancer(int idFreelancer) {
        Connection conn = Conexion.obtenerConexion();
        List<Propuesta> lista = new ArrayList<>();
        if (conn == null) return lista;
        try {
            PreparedStatement ps = conn.prepareStatement(
                "SELECT pr.*, u.nombre_completo AS nombre_freelancer, " +
                "pfl.calificacion_prom, pfl.nivel_experiencia, p.titulo AS titulo_proyecto " +
                "FROM propuesta pr JOIN usuario u ON pr.id_freelancer = u.id_usuario " +
                "LEFT JOIN perfil_freelancer pfl ON u.id_usuario = pfl.id_usuario " +
                "JOIN proyecto p ON pr.id_proyecto = p.id_proyecto " +
                "WHERE pr.id_freelancer = ? ORDER BY pr.fecha_envio DESC");
            ps.setInt(1, idFreelancer);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                lista.add(mapear(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error listarPorFreelancer propuesta: " + e.getMessage());
        } finally {
            new Conexion().desconectar(conn);
        }
        return lista;
    }

    public Propuesta buscarPorId(int id) {
        Connection conn = Conexion.obtenerConexion();
        if (conn == null) return null;
        try {
            PreparedStatement ps = conn.prepareStatement(
                "SELECT pr.*, u.nombre_completo AS nombre_freelancer, " +
                "pfl.calificacion_prom, pfl.nivel_experiencia, p.titulo AS titulo_proyecto " +
                "FROM propuesta pr JOIN usuario u ON pr.id_freelancer = u.id_usuario " +
                "LEFT JOIN perfil_freelancer pfl ON u.id_usuario = pfl.id_usuario " +
                "JOIN proyecto p ON pr.id_proyecto = p.id_proyecto WHERE pr.id_propuesta = ?");
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapear(rs);
        } catch (SQLException e) {
            System.err.println("Error buscarPorId propuesta: " + e.getMessage());
        } finally {
            new Conexion().desconectar(conn);
        }
        return null;
    }

    public boolean freelancerYaEnvio(int idFreelancer, int idProyecto) {
        Connection conn = Conexion.obtenerConexion();
        if (conn == null) return false;
        try {
            PreparedStatement ps = conn.prepareStatement(
                "SELECT COUNT(*) FROM propuesta WHERE id_freelancer = ? AND id_proyecto = ? AND estado != 'RETIRADA'");
            ps.setInt(1, idFreelancer);
            ps.setInt(2, idProyecto);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1) > 0;
        } catch (SQLException e) {
            System.err.println("Error freelancerYaEnvio: " + e.getMessage());
        } finally {
            new Conexion().desconectar(conn);
        }
        return false;
    }

    public boolean actualizarEstado(Connection conn, int idPropuesta, String estado) throws SQLException {
        PreparedStatement ps = conn.prepareStatement("UPDATE propuesta SET estado = ? WHERE id_propuesta = ?");
        ps.setString(1, estado);
        ps.setInt(2, idPropuesta);
        return ps.executeUpdate() > 0;
    }

    public boolean actualizarEstado(int idPropuesta, String estado) {
        Connection conn = Conexion.obtenerConexion();
        if (conn == null) return false;
        try {
            PreparedStatement ps = conn.prepareStatement("UPDATE propuesta SET estado = ? WHERE id_propuesta = ?");
            ps.setString(1, estado);
            ps.setInt(2, idPropuesta);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error actualizarEstado propuesta: " + e.getMessage());
        } finally {
            new Conexion().desconectar(conn);
        }
        return false;
    }

    public boolean rechazarDemas(Connection conn, int idProyecto, int idPropuestaAceptada) throws SQLException {
        PreparedStatement ps = conn.prepareStatement(
            "UPDATE propuesta SET estado = 'RECHAZADA' WHERE id_proyecto = ? AND id_propuesta != ? AND estado = 'PENDIENTE'");
        ps.setInt(1, idProyecto);
        ps.setInt(2, idPropuestaAceptada);
        ps.executeUpdate();
        return true;
    }
}
