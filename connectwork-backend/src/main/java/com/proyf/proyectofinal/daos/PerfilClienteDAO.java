/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.proyf.proyectofinal.daos;
import com.proyf.proyectofinal.conexion.Conexion;
import com.proyf.proyectofinal.modelo.PerfilCliente;
import java.sql.*;

/**
 *
 * @author ludwi
 */

public class PerfilClienteDAO {

    public boolean insertar(PerfilCliente p) {
        Connection conn = Conexion.obtenerConexion();
        if (conn == null) return false;
        try {
            PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO perfil_cliente (id_usuario, descripcion, sector, sitio_web) VALUES (?, ?, ?, ?)");
            ps.setInt(1, p.getIdUsuario());
            ps.setString(2, p.getDescripcion());
            ps.setString(3, p.getSector());
            ps.setString(4, p.getSitioWeb());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error insertar PerfilCliente: " + e.getMessage());
        } finally {
            new Conexion().desconectar(conn);
        }
        return false;
    }

    public PerfilCliente buscarPorUsuario(int idUsuario) {
        Connection conn = Conexion.obtenerConexion();
        if (conn == null) return null;
        try {
            PreparedStatement ps = conn.prepareStatement(
                "SELECT * FROM perfil_cliente WHERE id_usuario = ?");
            ps.setInt(1, idUsuario);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                PerfilCliente p = new PerfilCliente();
                p.setIdPerfilCliente(rs.getInt("id_perfil_cliente"));
                p.setIdUsuario(rs.getInt("id_usuario"));
                p.setDescripcion(rs.getString("descripcion"));
                p.setSector(rs.getString("sector"));
                p.setSitioWeb(rs.getString("sitio_web"));
                return p;
            }
        } catch (SQLException e) {
            System.err.println("Error buscarPorUsuario PerfilCliente: " + e.getMessage());
        } finally {
            new Conexion().desconectar(conn);
        }
        return null;
    }

    public boolean actualizar(PerfilCliente p) {
        Connection conn = Conexion.obtenerConexion();
        if (conn == null) return false;
        try {
            PreparedStatement ps = conn.prepareStatement(
                "UPDATE perfil_cliente SET descripcion = ?, sector = ?, sitio_web = ? WHERE id_usuario = ?");
            ps.setString(1, p.getDescripcion());
            ps.setString(2, p.getSector());
            ps.setString(3, p.getSitioWeb());
            ps.setInt(4, p.getIdUsuario());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error actualizar PerfilCliente: " + e.getMessage());
        } finally {
            new Conexion().desconectar(conn);
        }
        return false;
    }
}
