/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.proyf.proyectofinal.daos;

import com.proyf.proyectofinal.conexion.Conexion;
import com.proyf.proyectofinal.modelo.Calificacion;
import java.sql.*;

/**
 *
 * @author ludwi
 */

public class CalificacionDAO {

    public boolean insertar(Connection conn, Calificacion c) throws SQLException {
        PreparedStatement ps = conn.prepareStatement(
            "INSERT INTO calificacion (id_contrato, id_cliente, id_freelancer, puntuacion, comentario) VALUES (?, ?, ?, ?, ?)");
        ps.setInt(1, c.getIdContrato());
        ps.setInt(2, c.getIdCliente());
        ps.setInt(3, c.getIdFreelancer());
        ps.setInt(4, c.getPuntuacion());
        ps.setString(5, c.getComentario());
        return ps.executeUpdate() > 0;
    }

    public Calificacion buscarPorContrato(int idContrato) {
        Connection conn = Conexion.obtenerConexion();
        if (conn == null) return null;
        try {
            PreparedStatement ps = conn.prepareStatement(
                "SELECT ca.*, u.nombre_completo AS nombre_cliente " +
                "FROM calificacion ca JOIN usuario u ON ca.id_cliente = u.id_usuario " +
                "WHERE ca.id_contrato = ?");
            ps.setInt(1, idContrato);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                Calificacion c = new Calificacion();
                c.setIdCalificacion(rs.getInt("id_calificacion"));
                c.setIdContrato(rs.getInt("id_contrato"));
                c.setIdCliente(rs.getInt("id_cliente"));
                c.setNombreCliente(rs.getString("nombre_cliente"));
                c.setIdFreelancer(rs.getInt("id_freelancer"));
                c.setPuntuacion(rs.getInt("puntuacion"));
                c.setComentario(rs.getString("comentario"));
                if (rs.getTimestamp("fecha") != null) {
                    c.setFecha(rs.getTimestamp("fecha").toLocalDateTime());
                }
                return c;
            }
        } catch (SQLException e) {
            System.err.println("Error buscarPorContrato calificacion: " + e.getMessage());
        } finally {
            new Conexion().desconectar(conn);
        }
        return null;
    }

    public boolean eliminarPorContrato(Connection conn, int idContrato) throws SQLException {
        PreparedStatement ps = conn.prepareStatement("DELETE FROM calificacion WHERE id_contrato = ?");
        ps.setInt(1, idContrato);
        return ps.executeUpdate() > 0;
    }
}