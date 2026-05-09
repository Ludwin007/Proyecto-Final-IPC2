/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.proyf.proyectofinal.modelo;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 *
 * @author ludwi
 */

public class RecargaSaldo {
    private int idRecarga;
    private int idCliente;
    private BigDecimal monto;
    private String descripcion;
    private LocalDateTime fecha;

    public RecargaSaldo() {}

    public int getIdRecarga() { return idRecarga; }
    public void setIdRecarga(int idRecarga) { this.idRecarga = idRecarga; }
    public int getIdCliente() { return idCliente; }
    public void setIdCliente(int idCliente) { this.idCliente = idCliente; }
    public BigDecimal getMonto() { return monto; }
    public void setMonto(BigDecimal monto) { this.monto = monto; }
    public String getDescription() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    public LocalDateTime getFecha() { return fecha; }
    public void setFecha(LocalDateTime fecha) { this.fecha = fecha; }
}
