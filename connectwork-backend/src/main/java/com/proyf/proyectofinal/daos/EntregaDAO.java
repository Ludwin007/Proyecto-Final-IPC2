/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.proyf.proyectofinal.daos;

import com.proyf.proyectofinal.conexion.Conexion;
import com.proyf.proyectofinal.modelo.Entrega;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author ludwi
 */

public class EntregaDAO {

    private Entrega mapear(ResultSet rs) throws SQLException {
        Entrega e = new Entrega();
        e.setIdEntrega(rs.getInt("id_entrega"));
        e.setIdContrato(rs.getInt("id_contrato"));
        e.setDescripcion(rs.getString("descripcion"));
        e.setArchivosUrl(rs.getString("archivos_url"));
        if (rs.getTimestamp("fecha_subida") != null) {
            e.setFechaSubida(rs.getTimestamp("fecha_subida").toLocalDateTime());
        }
        e.setEstado(rs.getString("estado"));
        e.setMotivoRechazo(rs.getString("motivo_rechazo"));
        return e;
    }

    public int insertar(Entrega e) {
        Connection conn = Conexion.obtenerConexion();
        if (conn == null) return -1;
        try {
            PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO entrega (id_contrato, descripcion, archivos_url) VALUES (?, ?, ?)",
                Statement.RETURN_GENERATED_KEYS);
            ps.setInt(1, e.getIdContrato());
            ps.setString(2, e.getDescripcion());
            ps.setString(3, e.getArchivosUrl());
            ps.executeUpdate();
            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException ex) {
            System.err.println("Error insertar entrega: " + ex.getMessage());
        } finally {
            new Conexion().desconectar(conn);
        }
        return -1;
    }

    public List<Entrega> listarPorContrato(int idContrato) {
        Connection conn = Conexion.obtenerConexion();
        List<Entrega> lista = new ArrayList<>();
        if (conn == null) return lista;
        try {
            PreparedStatement ps = conn.prepareStatement(
                "SELECT * FROM entrega WHERE id_contrato = ? ORDER BY fecha_subida DESC");
            ps.setInt(1, idContrato);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                lista.add(mapear(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error listarPorContrato entrega: " + e.getMessage());
        } finally {
            new Conexion().desconectar(conn);
        }
        return lista;
    }

    public Entrega buscarPorId(int id) {
        Connection conn = Conexion.obtenerConexion();
        if (conn == null) return null;
        try {
            PreparedStatement ps = conn.prepareStatement(
                "SELECT * FROM entrega WHERE id_entrega = ?");
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapear(rs);
        } catch (SQLException e) {
            System.err.println("Error buscarPorId entrega: " + e.getMessage());
        } finally {
            new Conexion().desconectar(conn);
        }
        return null;
    }

    public boolean hayEntregaPendiente(int idContrato) {
        Connection conn = Conexion.obtenerConexion();
        if (conn == null) return false;
        try {
            PreparedStatement ps = conn.prepareStatement(
                "SELECT COUNT(*) FROM entrega WHERE id_contrato = ? AND estado = 'PENDIENTE'");
            ps.setInt(1, idContrato);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1) > 0;
        } catch (SQLException e) {
            System.err.println("Error hayEntregaPendiente: " + e.getMessage());
        } finally {
            new Conexion().desconectar(conn);
        }
        return false;
    }

    public boolean actualizarEstado(Connection conn, int idEntrega, String estado, String motivo) throws SQLException {
        PreparedStatement ps = conn.prepareStatement(
            "UPDATE entrega SET estado = ?, motivo_rechazo = ? WHERE id_entrega = ?");
        ps.setString(1, estado);
        ps.setString(2, motivo);
        ps.setInt(3, idEntrega);
        return ps.executeUpdate() > 0;
    }

    public boolean eliminar(Connection conn, int idEntrega) throws SQLException {
        PreparedStatement ps = conn.prepareStatement("DELETE FROM entrega WHERE id_entrega = ?");
        ps.setInt(1, idEntrega);
        return ps.executeUpdate() > 0;
    }
}