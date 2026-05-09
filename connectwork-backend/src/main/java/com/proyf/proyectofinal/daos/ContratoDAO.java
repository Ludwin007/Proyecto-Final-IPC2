/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.proyf.proyectofinal.daos;
import com.proyf.proyectofinal.conexion.Conexion;
import com.proyf.proyectofinal.modelo.Contrato;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author ludwi
 */

public class ContratoDAO {

    private Contrato mapear(ResultSet rs) throws SQLException {
        Contrato c = new Contrato();
        c.setIdContrato(rs.getInt("id_contrato"));
        c.setIdPropuesta(rs.getInt("id_propuesta"));
        c.setIdProyecto(rs.getInt("id_proyecto"));
        c.setIdCliente(rs.getInt("id_cliente"));
        c.setIdFreelancer(rs.getInt("id_freelancer"));
        c.setMonto(rs.getBigDecimal("monto"));
        c.setPorcComision(rs.getBigDecimal("porc_comision"));
        if (rs.getTimestamp("fecha_inicio") != null) {
            c.setFechaInicio(rs.getTimestamp("fecha_inicio").toLocalDateTime());
        }
        if (rs.getTimestamp("fecha_fin") != null) {
            c.setFechaFin(rs.getTimestamp("fecha_fin").toLocalDateTime());
        }
        c.setEstado(rs.getString("estado"));
        c.setMotivoCancelacion(rs.getString("motivo_cancelacion"));
        try { c.setTituloProyecto(rs.getString("titulo_proyecto")); } catch (SQLException ignored) {}
        try { c.setNombreCliente(rs.getString("nombre_cliente")); } catch (SQLException ignored) {}
        try { c.setNombreFreelancer(rs.getString("nombre_freelancer")); } catch (SQLException ignored) {}
        return c;
    }

    public int insertar(Connection conn, Contrato c) throws SQLException {
        PreparedStatement ps = conn.prepareStatement(
            "INSERT INTO contrato (id_propuesta, id_proyecto, id_cliente, id_freelancer, monto, porc_comision) " +
            "VALUES (?, ?, ?, ?, ?, ?)", Statement.RETURN_GENERATED_KEYS);
        ps.setInt(1, c.getIdPropuesta());
        ps.setInt(2, c.getIdProyecto());
        ps.setInt(3, c.getIdCliente());
        ps.setInt(4, c.getIdFreelancer());
        ps.setBigDecimal(5, c.getMonto());
        ps.setBigDecimal(6, c.getPorcComision());
        ps.executeUpdate();
        ResultSet rs = ps.getGeneratedKeys();
        if (rs.next()) return rs.getInt(1);
        return -1;
    }

    public Contrato buscarPorId(int id) {
        Connection conn = Conexion.obtenerConexion();
        if (conn == null) return null;
        try {
            PreparedStatement ps = conn.prepareStatement(
                "SELECT co.*, p.titulo AS titulo_proyecto, " +
                "uc.nombre_completo AS nombre_cliente, uf.nombre_completo AS nombre_freelancer " +
                "FROM contrato co JOIN proyecto p ON co.id_proyecto = p.id_proyecto " +
                "JOIN usuario uc ON co.id_cliente = uc.id_usuario " +
                "JOIN usuario uf ON co.id_freelancer = uf.id_usuario " +
                "WHERE co.id_contrato = ?");
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapear(rs);
        } catch (SQLException e) {
            System.err.println("Error buscarPorId contrato: " + e.getMessage());
        } finally {
            new Conexion().desconectar(conn);
        }
        return null;
    }

    public List<Contrato> listarPorFreelancer(int idFreelancer) {
        Connection conn = Conexion.obtenerConexion();
        List<Contrato> lista = new ArrayList<>();
        if (conn == null) return lista;
        try {
            PreparedStatement ps = conn.prepareStatement(
                "SELECT co.*, p.titulo AS titulo_proyecto, " +
                "uc.nombre_completo AS nombre_cliente, uf.nombre_completo AS nombre_freelancer " +
                "FROM contrato co JOIN proyecto p ON co.id_proyecto = p.id_proyecto " +
                "JOIN usuario uc ON co.id_cliente = uc.id_usuario " +
                "JOIN usuario uf ON co.id_freelancer = uf.id_usuario " +
                "WHERE co.id_freelancer = ? ORDER BY co.fecha_inicio DESC");
            ps.setInt(1, idFreelancer);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                lista.add(mapear(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error listarPorFreelancer contrato: " + e.getMessage());
        } finally {
            new Conexion().desconectar(conn);
        }
        return lista;
    }

    public List<Contrato> listarPorCliente(int idCliente) {
        Connection conn = Conexion.obtenerConexion();
        List<Contrato> lista = new ArrayList<>();
        if (conn == null) return lista;
        try {
            PreparedStatement ps = conn.prepareStatement(
                "SELECT co.*, p.titulo AS titulo_proyecto, " +
                "uc.nombre_completo AS nombre_cliente, uf.nombre_completo AS nombre_freelancer " +
                "FROM contrato co JOIN proyecto p ON co.id_proyecto = p.id_proyecto " +
                "JOIN usuario uc ON co.id_cliente = uc.id_usuario " +
                "JOIN usuario uf ON co.id_freelancer = uf.id_usuario " +
                "WHERE co.id_cliente = ? ORDER BY co.fecha_inicio DESC");
            ps.setInt(1, idCliente);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                lista.add(mapear(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error listarPorCliente contrato: " + e.getMessage());
        } finally {
            new Conexion().desconectar(conn);
        }
        return lista;
    }

    public boolean actualizarEstado(Connection conn, int idContrato, String estado, String motivo) throws SQLException {
        PreparedStatement ps = conn.prepareStatement(
            "UPDATE contrato SET estado = ?, motivo_cancelacion = ?, fecha_fin = NOW() WHERE id_contrato = ?");
        ps.setString(1, estado);
        ps.setString(2, motivo);
        ps.setInt(3, idContrato);
        return ps.executeUpdate() > 0;
    }
}
