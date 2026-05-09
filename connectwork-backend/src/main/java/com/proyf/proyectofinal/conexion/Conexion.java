/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.proyf.proyectofinal.conexion;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * @author ludwi
 */

public class Conexion {
    private String url = "jdbc:mariadb://localhost:3306/connectwork_db";
    private String usuario = "Gerard";
    private String password = "12345";
    public Connection conectar() {
        try {
            Class.forName("org.mariadb.jdbc.Driver");
            Connection conn = DriverManager.getConnection(url, usuario, password);
            System.out.println("Conexion exitosa");
            return conn;
        } catch (SQLException e) {
            System.err.println("Error al conectar: " + e.getMessage());
            return null;
        } catch (ClassNotFoundException e) {
            System.err.println("Error al cargar el driver: " + e.getMessage());
            return null;
        }
    }
    public static Connection obtenerConexion() {
        return new Conexion().conectar();
    }
    public void desconectar(Connection c) {
        if (c != null) {
            try {
                c.close();
                System.out.println("Conexion cerrada");
            } catch (SQLException e) {
                System.err.println("Error al cerrar conexion: " + e.getMessage());
            }
        }
    }
}