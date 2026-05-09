/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.proyf.proyectofinal.util;

/**
 *
 * @author ludwi
 */

public class Respuesta {
    private boolean exito;
    private String mensaje;
    private Object datos;

    public Respuesta(boolean exito, String mensaje, Object datos) {
        this.exito = exito;
        this.mensaje = mensaje;
        this.datos = datos;
    }

    public boolean isExito() { return exito; }
    public String getMensaje() { return mensaje; }
    public Object getDatos() { return datos; }
}