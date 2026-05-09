/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.proyf.proyectofinal.daos;
import com.proyf.proyectofinal.conexion.Conexion;
import com.proyf.proyectofinal.modelo.Categoria;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author ludwi
 */

public class CategoriaDAO {

    private Categoria mapear(ResultSet rs) throws SQLException {
        Categoria c = new Categoria();
        c.setIdCategoria(rs.getInt("id_categoria"));
        c.setNombre(rs.getString("nombre"));
        c.setDescripcion(rs.getString("descripcion"));
        c.setActivo(rs.getBoolean("activo"));
        return c;
    }

    public List<Categoria> listarTodas() {
        Connection conn = Conexion.obtenerConexion();
        List<Categoria> lista = new ArrayList<>();
        if (conn == null) return lista;
        try {
            PreparedStatement ps = conn.prepareStatement("SELECT * FROM categoria ORDER BY nombre");
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                lista.add(mapear(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error listarTodas categorias: " + e.getMessage());
        } finally {
            new Conexion().desconectar(conn);
        }
        return lista;
    }

    public List<Categoria> listarActivas() {
        Connection conn = Conexion.obtenerConexion();
        List<Categoria> lista = new ArrayList<>();
        if (conn == null) return lista;
        try {
            PreparedStatement ps = conn.prepareStatement("SELECT * FROM categoria WHERE activo = 1 ORDER BY nombre");
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                lista.add(mapear(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error listarActivas categorias: " + e.getMessage());
        } finally {
            new Conexion().desconectar(conn);
        }
        return lista;
    }

    public Categoria buscarPorId(int id) {
        Connection conn = Conexion.obtenerConexion();
        if (conn == null) return null;
        try {
            PreparedStatement ps = conn.prepareStatement("SELECT * FROM categoria WHERE id_categoria = ?");
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapear(rs);
        } catch (SQLException e) {
            System.err.println("Error buscarPorId categoria: " + e.getMessage());
        } finally {
            new Conexion().desconectar(conn);
        }
        return null;
    }

    public boolean existeNombre(String nombre) {
        Connection conn = Conexion.obtenerConexion();
        if (conn == null) return false;
        try {
            PreparedStatement ps = conn.prepareStatement("SELECT COUNT(*) FROM categoria WHERE LOWER(nombre) = LOWER(?)");
            ps.setString(1, nombre);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1) > 0;
        } catch (SQLException e) {
            System.err.println("Error existeNombre categoria: " + e.getMessage());
        } finally {
            new Conexion().desconectar(conn);
        }
        return false;
    }

    public boolean insertar(Categoria c) {
        Connection conn = Conexion.obtenerConexion();
        if (conn == null) return false;
        try {
            PreparedStatement ps = conn.prepareStatement("INSERT INTO categoria (nombre, descripcion) VALUES (?, ?)");
            ps.setString(1, c.getNombre());
            ps.setString(2, c.getDescripcion());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error insertar categoria: " + e.getMessage());
        } finally {
            new Conexion().desconectar(conn);
        }
        return false;
    }

    public boolean actualizar(Categoria c) {
        Connection conn = Conexion.obtenerConexion();
        if (conn == null) return false;
        try {
            PreparedStatement ps = conn.prepareStatement("UPDATE categoria SET nombre = ?, descripcion = ? WHERE id_categoria = ?");
            ps.setString(1, c.getNombre());
            ps.setString(2, c.getDescripcion());
            ps.setInt(3, c.getIdCategoria());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error actualizar categoria: " + e.getMessage());
        } finally {
            new Conexion().desconectar(conn);
        }
        return false;
    }

    public boolean cambiarEstado(int idCategoria, boolean activo) {
        Connection conn = Conexion.obtenerConexion();
        if (conn == null) return false;
        try {
            PreparedStatement ps = conn.prepareStatement("UPDATE categoria SET activo = ? WHERE id_categoria = ?");
            ps.setBoolean(1, activo);
            ps.setInt(2, idCategoria);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error cambiarEstado categoria: " + e.getMessage());
        } finally {
            new Conexion().desconectar(conn);
        }
        return false;
    }
}
