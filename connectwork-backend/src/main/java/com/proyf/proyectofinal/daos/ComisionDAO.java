/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.proyf.proyectofinal.daos;
import com.proyf.proyectofinal.conexion.Conexion;
import com.proyf.proyectofinal.modelo.ComisionConfig;
import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author ludwi
 */

public class ComisionDAO {

    private ComisionConfig mapear(ResultSet rs) throws SQLException {
        ComisionConfig c = new ComisionConfig();
        c.setIdComision(rs.getInt("id_comision"));
        c.setPorcentaje(rs.getBigDecimal("porcentaje"));
        if (rs.getTimestamp("fecha_inicio") != null) {
            c.setFechaInicio(rs.getTimestamp("fecha_inicio").toLocalDateTime());
        }
        if (rs.getTimestamp("fecha_fin") != null) {
            c.setFechaFin(rs.getTimestamp("fecha_fin").toLocalDateTime());
        }
        return c;
    }

    public BigDecimal obtenerPorcentajeVigente() {
        Connection conn = Conexion.obtenerConexion();
        if (conn == null) return BigDecimal.TEN;
        try {
            PreparedStatement ps = conn.prepareStatement(
                "SELECT porcentaje FROM comision_config WHERE fecha_fin IS NULL ORDER BY fecha_inicio DESC LIMIT 1");
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getBigDecimal("porcentaje");
        } catch (SQLException e) {
            System.err.println("Error obtenerPorcentajeVigente: " + e.getMessage());
        } finally {
            new Conexion().desconectar(conn);
        }
        return BigDecimal.TEN;
    }

    public ComisionConfig obtenerVigente() {
        Connection conn = Conexion.obtenerConexion();
        if (conn == null) return null;
        try {
            PreparedStatement ps = conn.prepareStatement(
                "SELECT * FROM comision_config WHERE fecha_fin IS NULL ORDER BY fecha_inicio DESC LIMIT 1");
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapear(rs);
        } catch (SQLException e) {
            System.err.println("Error obtenerVigente: " + e.getMessage());
        } finally {
            new Conexion().desconectar(conn);
        }
        return null;
    }

    public List<ComisionConfig> listarHistorial() {
        Connection conn = Conexion.obtenerConexion();
        List<ComisionConfig> lista = new ArrayList<>();
        if (conn == null) return lista;
        try {
            PreparedStatement ps = conn.prepareStatement(
                "SELECT * FROM comision_config ORDER BY fecha_inicio DESC");
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                lista.add(mapear(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error listarHistorial comision: " + e.getMessage());
        } finally {
            new Conexion().desconectar(conn);
        }
        return lista;
    }

    public boolean actualizarComision(BigDecimal nuevoPorcentaje) {
        Connection conn = Conexion.obtenerConexion();
        if (conn == null) return false;
        try {
            conn.setAutoCommit(false);
            PreparedStatement psCierre = conn.prepareStatement(
                "UPDATE comision_config SET fecha_fin = NOW() WHERE fecha_fin IS NULL");
            psCierre.executeUpdate();

            PreparedStatement psInsert = conn.prepareStatement(
                "INSERT INTO comision_config (porcentaje, fecha_inicio) VALUES (?, NOW())");
            psInsert.setBigDecimal(1, nuevoPorcentaje);
            psInsert.executeUpdate();

            conn.commit();
            return true;
        } catch (SQLException e) {
            try { if (conn != null) conn.rollback(); } catch (SQLException ex) { System.err.println(ex.getMessage()); }
            System.err.println("Error actualizarComision: " + e.getMessage());
        } finally {
            try { if (conn != null) conn.setAutoCommit(true); } catch (SQLException e) { System.err.println(e.getMessage()); }
            new Conexion().desconectar(conn);
        }
        return false;
    }
}
