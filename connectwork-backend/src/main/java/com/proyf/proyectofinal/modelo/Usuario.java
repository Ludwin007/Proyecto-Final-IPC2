/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.proyf.proyectofinal.modelo;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 *
 * @author ludwi
 */

public class Usuario {
    private int idUsuario;
    private String tipoRol;
    private String nombreCompleto;
    private String username;
    private String contrasena;
    private String correo;
    private String telefono;
    private String direccion;
    private String cui;
    private LocalDate fechaNacimiento;
    private boolean activo;
    private BigDecimal saldo;
    private BigDecimal saldoBloqueado;
    private boolean perfilCompleto;
    private LocalDateTime fechaCreacion;

    public Usuario() {}

    public int getIdUsuario() { return idUsuario; }
    public void setIdUsuario(int idUsuario) { this.idUsuario = idUsuario; }
    public String getTipoRol() { return tipoRol; }
    public void setTipoRol(String tipoRol) { this.tipoRol = tipoRol; }
    public String getNombreCompleto() { return nombreCompleto; }
    public void setNombreCompleto(String nombreCompleto) { this.nombreCompleto = nombreCompleto; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getContrasena() { return contrasena; }
    public void setContrasena(String contrasena) { this.contrasena = contrasena; }
    public String getCorreo() { return correo; }
    public void setCorreo(String correo) { this.correo = correo; }
    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }
    public String getDireccion() { return direccion; }
    public void setDireccion(String direccion) { this.direccion = direccion; }
    public String getCui() { return cui; }
    public void setCui(String cui) { this.cui = cui; }
    public LocalDate getFechaNacimiento() { return fechaNacimiento; }
    public void setFechaNacimiento(LocalDate fechaNacimiento) { this.fechaNacimiento = fechaNacimiento; }
    public boolean isActivo() { return activo; }
    public void setActivo(boolean activo) { this.activo = activo; }
    public BigDecimal getSaldo() { return saldo; }
    public void setSaldo(BigDecimal saldo) { this.saldo = saldo; }
    public BigDecimal getSaldoBloqueado() { return saldoBloqueado; }
    public void setSaldoBloqueado(BigDecimal saldoBloqueado) { this.saldoBloqueado = saldoBloqueado; }
    public boolean isPerfilCompleto() { return perfilCompleto; }
    public void setPerfilCompleto(boolean perfilCompleto) { this.perfilCompleto = perfilCompleto; }
    public LocalDateTime getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(LocalDateTime fechaCreacion) { this.fechaCreacion = fechaCreacion; }
}