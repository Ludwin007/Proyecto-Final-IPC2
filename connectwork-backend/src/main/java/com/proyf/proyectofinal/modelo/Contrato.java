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

public class Contrato {
    private int idContrato;
    private int idPropuesta;
    private int idProyecto;
    private String tituloProyecto;
    private int idCliente;
    private String nombreCliente;
    private int idFreelancer;
    private String nombreFreelancer;
    private BigDecimal monto;
    private BigDecimal porcComision;
    private LocalDateTime fechaInicio;
    private LocalDateTime fechaFin;
    private String estado;
    private String motivoCancelacion;

    public Contrato() {}

    public int getIdContrato() { return idContrato; }
    public void setIdContrato(int idContrato) { this.idContrato = idContrato; }
    public int getIdPropuesta() { return idPropuesta; }
    public void setIdPropuesta(int idPropuesta) { this.idPropuesta = idPropuesta; }
    public int getIdProyecto() { return idProyecto; }
    public void setIdProyecto(int idProyecto) { this.idProyecto = idProyecto; }
    public String getTituloProyecto() { return tituloProyecto; }
    public void setTituloProyecto(String tituloProyecto) { this.tituloProyecto = tituloProyecto; }
    public int getIdCliente() { return idCliente; }
    public void setIdCliente(int idCliente) { this.idCliente = idCliente; }
    public String getNombreCliente() { return nombreCliente; }
    public void setNombreCliente(String nombreCliente) { this.nombreCliente = nombreCliente; }
    public int getIdFreelancer() { return idFreelancer; }
    public void setIdFreelancer(int idFreelancer) { this.idFreelancer = idFreelancer; }
    public String getNombreFreelancer() { return nombreFreelancer; }
    public void setNombreFreelancer(String nombreFreelancer) { this.nombreFreelancer = nombreFreelancer; }
    public BigDecimal getMonto() { return monto; }
    public void setMonto(BigDecimal monto) { this.monto = monto; }
    public BigDecimal getPorcComision() { return porcComision; }
    public void setPorcComision(BigDecimal porcComision) { this.porcComision = porcComision; }
    public LocalDateTime getFechaInicio() { return fechaInicio; }
    public void setFechaInicio(LocalDateTime fechaInicio) { this.fechaInicio = fechaInicio; }
    public LocalDateTime getFechaFin() { return fechaFin; }
    public void setFechaFin(LocalDateTime fechaFin) { this.fechaFin = fechaFin; }
    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
    public String getMotivoCancelacion() { return motivoCancelacion; }
    public void setMotivoCancelacion(String motivoCancelacion) { this.motivoCancelacion = motivoCancelacion; }
}