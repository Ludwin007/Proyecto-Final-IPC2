/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.proyf.proyectofinal.daos;
import com.proyf.proyectofinal.conexion.Conexion;
import com.proyf.proyectofinal.modelo.PerfilFreelancer;
import java.sql.*;

/**
 *
 * @author ludwi
 */

public class PerfilFreelancerDAO {

    private PerfilFreelancer mapear(ResultSet rs) throws SQLException {
        PerfilFreelancer p = new PerfilFreelancer();
        p.setIdPerfilFl(rs.getInt("id_perfil_fl"));
        p.setIdUsuario(rs.getInt("id_usuario"));
        p.setBiografia(rs.getString("biografia"));
        p.setNivelExperiencia(rs.getString("nivel_experiencia"));
        p.setTarifaHora(rs.getBigDecimal("tarifa_hora"));
        p.setCalificacionProm(rs.getBigDecimal("calificacion_prom"));
        p.setTotalContratos(rs.getInt("total_contratos"));
        return p;
    }

    public boolean insertar(PerfilFreelancer p) {
        Connection conn = Conexion.obtenerConexion();
        if (conn == null) return false;
        try {
            PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO perfil_freelancer (id_usuario, biografia, nivel_experiencia, tarifa_hora) VALUES (?, ?, ?, ?)");
            ps.setInt(1, p.getIdUsuario());
            ps.setString(2, p.getBiografia());
            ps.setString(3, p.getNivelExperiencia());
            ps.setBigDecimal(4, p.getTarifaHora());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error insertar PerfilFreelancer: " + e.getMessage());
        } finally {
            new Conexion().desconectar(conn);
        }
        return false;
    }

    public PerfilFreelancer buscarPorUsuario(int idUsuario) {
        Connection conn = Conexion.obtenerConexion();
        if (conn == null) return null;
        try {
            PreparedStatement ps = conn.prepareStatement(
                "SELECT * FROM perfil_freelancer WHERE id_usuario = ?");
            ps.setInt(1, idUsuario);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapear(rs);
        } catch (SQLException e) {
            System.err.println("Error buscarPorUsuario PerfilFreelancer: " + e.getMessage());
        } finally {
            new Conexion().desconectar(conn);
        }
        return null;
    }

    public boolean actualizar(PerfilFreelancer p) {
        Connection conn = Conexion.obtenerConexion();
        if (conn == null) return false;
        try {
            PreparedStatement ps = conn.prepareStatement(
                "UPDATE perfil_freelancer SET biografia = ?, nivel_experiencia = ?, tarifa_hora = ? WHERE id_usuario = ?");
            ps.setString(1, p.getBiografia());
            ps.setString(2, p.getNivelExperiencia());
            ps.setBigDecimal(3, p.getTarifaHora());
            ps.setInt(4, p.getIdUsuario());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error actualizar PerfilFreelancer: " + e.getMessage());
        } finally {
            new Conexion().desconectar(conn);
        }
        return false;
    }

    public boolean actualizarCalificacion(Connection conn, int idUsuario) throws SQLException {
        PreparedStatement ps = conn.prepareStatement(
            "UPDATE perfil_freelancer SET calificacion_prom = " +
            "(SELECT IFNULL(AVG(puntuacion), 0) FROM calificacion WHERE id_freelancer = ?), " +
            "total_contratos = (SELECT COUNT(*) FROM contrato WHERE id_freelancer = ? AND estado = 'COMPLETADO') " +
            "WHERE id_usuario = ?");
        ps.setInt(1, idUsuario);
        ps.setInt(2, idUsuario);
        ps.setInt(3, idUsuario);
        return ps.executeUpdate() > 0;
    }

    public boolean agregarHabilidad(int idUsuario, int idHabilidad) {
        Connection conn = Conexion.obtenerConexion();
        if (conn == null) return false;
        try {
            PreparedStatement ps = conn.prepareStatement(
                "INSERT IGNORE INTO freelancer_habilidad (id_usuario, id_habilidad) VALUES (?, ?)");
            ps.setInt(1, idUsuario);
            ps.setInt(2, idHabilidad);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error agregarHabilidad: " + e.getMessage());
        } finally {
            new Conexion().desconectar(conn);
        }
        return false;
    }

    public boolean eliminarHabilidades(int idUsuario) {
        Connection conn = Conexion.obtenerConexion();
        if (conn == null) return false;
        try {
            PreparedStatement ps = conn.prepareStatement(
                "DELETE FROM freelancer_habilidad WHERE id_usuario = ?");
            ps.setInt(1, idUsuario);
            ps.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.err.println("Error eliminarHabilidades: " + e.getMessage());
        } finally {
            new Conexion().desconectar(conn);
        }
        return false;
    }
}
