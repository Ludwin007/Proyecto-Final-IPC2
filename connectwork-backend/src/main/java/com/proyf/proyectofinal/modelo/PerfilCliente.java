/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.proyf.proyectofinal.modelo;

/**
 *
 * @author ludwi
 */

public class PerfilCliente {
    private int idPerfilCliente;
    private int idUsuario;
    private String descripcion;
    private String sector;
    private String sitioWeb;

    public PerfilCliente() {}

    public int getIdPerfilCliente() { return idPerfilCliente; } 
    public void setIdPerfilCliente(int idPerfilCliente) { this.idPerfilCliente = idPerfilCliente; }
    public int getIdUsuario() { return idUsuario; }
    public void setIdUsuario(int idUsuario) { this.idUsuario = idUsuario; }
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    public String getSector() { return sector; }
    public void setSector(String sector) { this.sector = sector; }
    public String getSitioWeb() { return sitioWeb; }
    public void setSitioWeb(String sitioWeb) { this.sitioWeb = sitioWeb; }
}