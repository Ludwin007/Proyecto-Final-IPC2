/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.proyf.proyectofinal.daos;
import com.proyf.proyectofinal.conexion.Conexion;
import com.proyf.proyectofinal.modelo.RecargaSaldo;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author ludwi
 */

public class RecargaDAO {

    public List<RecargaSaldo> listarPorCliente(int idCliente) {
        Connection conn = Conexion.obtenerConexion();
        List<RecargaSaldo> lista = new ArrayList<>();
        if (conn == null) return lista;
        try {
            PreparedStatement ps = conn.prepareStatement(
                "SELECT * FROM recarga_saldo WHERE id_cliente = ? ORDER BY fecha DESC");
            ps.setInt(1, idCliente);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                RecargaSaldo r = new RecargaSaldo();
                r.setIdRecarga(rs.getInt("id_recarga"));
                r.setIdCliente(rs.getInt("id_cliente"));
                r.setMonto(rs.getBigDecimal("monto"));
                r.setDescripcion(rs.getString("descripcion"));
                if (rs.getTimestamp("fecha") != null) {
                    r.setFecha(rs.getTimestamp("fecha").toLocalDateTime());
                }
                lista.add(r);
            }
        } catch (SQLException e) {
            System.err.println("Error listarPorCliente recarga: " + e.getMessage());
        } finally {
            new Conexion().desconectar(conn);
        }
        return lista;
    }
}