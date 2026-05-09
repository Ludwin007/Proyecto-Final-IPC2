/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.proyf.proyectofinal.daos;

import com.proyf.proyectofinal.conexion.Conexion;
import com.proyf.proyectofinal.modelo.Usuario;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author ludwi
 */

public class UsuarioDAO {

    private Usuario mapear(ResultSet rs) throws SQLException {
        Usuario u = new Usuario();
        u.setIdUsuario(rs.getInt("id_usuario"));
        u.setTipoRol(rs.getString("tipo_rol"));
        u.setNombreCompleto(rs.getString("nombre_completo"));
        u.setUsername(rs.getString("username"));
        u.setContrasena(rs.getString("contrasena"));
        u.setCorreo(rs.getString("correo"));
        u.setTelefono(rs.getString("telefono"));
        u.setDireccion(rs.getString("direccion"));
        u.setCui(rs.getString("cui"));
        if (rs.getDate("fecha_nacimiento") != null) {
            u.setFechaNacimiento(rs.getDate("fecha_nacimiento").toLocalDate());
        }
        u.setActivo(rs.getBoolean("activo"));
        u.setSaldo(rs.getBigDecimal("saldo"));
        u.setSaldoBloqueado(rs.getBigDecimal("saldo_bloqueado"));
        u.setPerfilCompleto(rs.getBoolean("perfil_completo"));
        if (rs.getTimestamp("fecha_creacion") != null) {
            u.setFechaCreacion(rs.getTimestamp("fecha_creacion").toLocalDateTime());
        }
        return u;
    }

    public Usuario buscarPorUsername(String username) {
        Connection conn = Conexion.obtenerConexion();
        if (conn == null) return null;
        try {
            PreparedStatement ps = conn.prepareStatement("SELECT * FROM usuario WHERE username = ?");
            ps.setString(1, username);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapear(rs);
        } catch (SQLException e) {
            System.err.println("Error buscarPorUsername: " + e.getMessage());
        } finally {
            new Conexion().desconectar(conn);
        }
        return null;
    }

    public Usuario buscarPorCorreo(String correo) {
        Connection conn = Conexion.obtenerConexion();
        if (conn == null) return null;
        try {
            PreparedStatement ps = conn.prepareStatement("SELECT * FROM usuario WHERE correo = ?");
            ps.setString(1, correo);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapear(rs);
        } catch (SQLException e) {
            System.err.println("Error buscarPorCorreo: " + e.getMessage());
        } finally {
            new Conexion().desconectar(conn);
        }
        return null;
    }

    public Usuario buscarPorId(int id) {
        Connection conn = Conexion.obtenerConexion();
        if (conn == null) return null;
        try {
            PreparedStatement ps = conn.prepareStatement("SELECT * FROM usuario WHERE id_usuario = ?");
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapear(rs);
        } catch (SQLException e) {
            System.err.println("Error buscarPorId: " + e.getMessage());
        } finally {
            new Conexion().desconectar(conn);
        }
        return null;
    }

    public boolean existeUsername(String username) {
        Connection conn = Conexion.obtenerConexion();
        if (conn == null) return false;
        try {
            PreparedStatement ps = conn.prepareStatement("SELECT COUNT(*) FROM usuario WHERE username = ?");
            ps.setString(1, username);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1) > 0;
        } catch (SQLException e) {
            System.err.println("Error existeUsername: " + e.getMessage());
        } finally {
            new Conexion().desconectar(conn);
        }
        return false;
    }

    public boolean existeCorreo(String correo) {
        Connection conn = Conexion.obtenerConexion();
        if (conn == null) return false;
        try {
            PreparedStatement ps = conn.prepareStatement("SELECT COUNT(*) FROM usuario WHERE correo = ?");
            ps.setString(1, correo);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1) > 0;
        } catch (SQLException e) {
            System.err.println("Error existeCorreo: " + e.getMessage());
        } finally {
            new Conexion().desconectar(conn);
        }
        return false;
    }

    public int insertar(Usuario u) {
        Connection conn = Conexion.obtenerConexion();
        if (conn == null) return -1;
        try {
            PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO usuario (tipo_rol, nombre_completo, username, contrasena, correo, " +
                "telefono, direccion, cui, fecha_nacimiento, activo, saldo, saldo_bloqueado, perfil_completo) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, 1, 0.00, 0.00, 0)",
                Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, u.getTipoRol());
            ps.setString(2, u.getNombreCompleto());
            ps.setString(3, u.getUsername());
            ps.setString(4, u.getContrasena());
            ps.setString(5, u.getCorreo());
            ps.setString(6, u.getTelefono());
            ps.setString(7, u.getDireccion());
            ps.setString(8, u.getCui());
            ps.setDate(9, Date.valueOf(u.getFechaNacimiento()));
            ps.executeUpdate();
            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            System.err.println("Error insertar usuario: " + e.getMessage());
        } finally {
            new Conexion().desconectar(conn);
        }
        return -1;
    }

    public boolean actualizarPerfilCompleto(int idUsuario) {
        Connection conn = Conexion.obtenerConexion();
        if (conn == null) return false;
        try {
            PreparedStatement ps = conn.prepareStatement("UPDATE usuario SET perfil_completo = 1 WHERE id_usuario = ?");
            ps.setInt(1, idUsuario);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error actualizarPerfilCompleto: " + e.getMessage());
        } finally {
            new Conexion().desconectar(conn);
        }
        return false;
    }

    public boolean actualizarActivo(int idUsuario, boolean activo) {
        Connection conn = Conexion.obtenerConexion();
        if (conn == null) return false;
        try {
            PreparedStatement ps = conn.prepareStatement("UPDATE usuario SET activo = ? WHERE id_usuario = ?");
            ps.setBoolean(1, activo);
            ps.setInt(2, idUsuario);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error actualizarActivo: " + e.getMessage());
        } finally {
            new Conexion().desconectar(conn);
        }
        return false;
    }

    public boolean actualizarSaldo(Connection conn, int idUsuario, java.math.BigDecimal saldo, java.math.BigDecimal saldoBloqueado) throws SQLException {
        PreparedStatement ps = conn.prepareStatement("UPDATE usuario SET saldo = ?, saldo_bloqueado = ? WHERE id_usuario = ?");
        ps.setBigDecimal(1, saldo);
        ps.setBigDecimal(2, saldoBloqueado);
        ps.setInt(3, idUsuario);
        return ps.executeUpdate() > 0;
    }

    public List<Usuario> listarPorRol(String rol) {
        Connection conn = Conexion.obtenerConexion();
        List<Usuario> lista = new ArrayList<>();
        if (conn == null) return lista;
        try {
            PreparedStatement ps = conn.prepareStatement("SELECT * FROM usuario WHERE tipo_rol = ? ORDER BY nombre_completo");
            ps.setString(1, rol);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) lista.add(mapear(rs));
        } catch (SQLException e) {
            System.err.println("Error listarPorRol: " + e.getMessage());
        } finally {
            new Conexion().desconectar(conn);
        }
        return lista;
    }

    public List<Usuario> listarClientesYFreelancers() {
        Connection conn = Conexion.obtenerConexion();
        List<Usuario> lista = new ArrayList<>();
        if (conn == null) return lista;
        try {
            PreparedStatement ps = conn.prepareStatement(
                "SELECT * FROM usuario WHERE tipo_rol IN ('CLIENTE','FREELANCER') ORDER BY nombre_completo");
            ResultSet rs = ps.executeQuery();
            while (rs.next()) lista.add(mapear(rs));
        } catch (SQLException e) {
            System.err.println("Error listarClientesYFreelancers: " + e.getMessage());
        } finally {
            new Conexion().desconectar(conn);
        }
        return lista;
    }

    public boolean recargarSaldo(int idCliente, java.math.BigDecimal monto, String descripcion) {
        Connection conn = Conexion.obtenerConexion();
        if (conn == null) return false;
        try {
            conn.setAutoCommit(false);
            PreparedStatement psUpdate = conn.prepareStatement("UPDATE usuario SET saldo = saldo + ? WHERE id_usuario = ?");
            psUpdate.setBigDecimal(1, monto);
            psUpdate.setInt(2, idCliente);
            psUpdate.executeUpdate();

            PreparedStatement psInsert = conn.prepareStatement("INSERT INTO recarga_saldo (id_cliente, monto, descripcion) VALUES (?, ?, ?)");
            psInsert.setInt(1, idCliente);
            psInsert.setBigDecimal(2, monto);
            psInsert.setString(3, descripcion);
            psInsert.executeUpdate();

            conn.commit();
            return true;
        } catch (SQLException e) {
            try { if (conn != null) conn.rollback(); } catch (SQLException ex) { System.err.println(ex.getMessage()); }
            System.err.println("Error recargarSaldo: " + e.getMessage());
        } finally {
            try { if (conn != null) conn.setAutoCommit(true); } catch (SQLException e) { System.err.println(e.getMessage()); }
            new Conexion().desconectar(conn);
        }
        return false;
    }
}