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

public class ComisionConfig {
    private int idComision;
    private BigDecimal porcentaje;
    private LocalDateTime fechaInicio;
    private LocalDateTime fechaFin;

    public ComisionConfig() {}

    public int getIdComision() { return idComision; }
    public void setIdComision(int idComision) { this.idComision = idComision; }
    public BigDecimal getPorcentaje() { return porcentaje; }
    public void setPorcentaje(BigDecimal porcentaje) { this.porcentaje = porcentaje; }
    public LocalDateTime getFechaInicio() { return fechaInicio; }
    public void setFechaInicio(LocalDateTime fechaInicio) { this.fechaInicio = fechaInicio; }
    public LocalDateTime getFechaFin() { return fechaFin; }
    public void setFechaFin(LocalDateTime fechaFin) { this.fechaFin = fechaFin; }
}