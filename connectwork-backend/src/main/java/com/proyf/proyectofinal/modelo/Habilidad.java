/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.proyf.proyectofinal.modelo;

/**
 *
 * @author ludwi
 */

public class Habilidad {
    private int idHabilidad;
    private int idCategoria;
    private String nombreCategoria;
    private String nombre;
    private String descripcion;
    private boolean activo;

    public Habilidad() {}

    public int getIdHabilidad() { return idHabilidad; }
    public void setIdHabilidad(int idHabilidad) { this.idHabilidad = idHabilidad; }
    public int getIdCategoria() { return idCategoria; }
    public void setIdCategoria(int idCategoria) { this.idCategoria = idCategoria; }
    public String getNombreCategoria() { return nombreCategoria; }
    public void setNombreCategoria(String nombreCategoria) { this.nombreCategoria = nombreCategoria; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    public boolean isActivo() { return activo; }
    public void setActivo(boolean activo) { this.activo = activo; }
}
