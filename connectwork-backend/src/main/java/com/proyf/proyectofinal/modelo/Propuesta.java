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

public class Propuesta {
    private int idPropuesta;
    private int idProyecto;
    private String tituloProyecto;
    private int idFreelancer;
    private String nombreFreelancer;
    private BigDecimal calificacionFreelancer;
    private String nivelExperiencia;
    private BigDecimal montoOfertado;
    private int plazoDias;
    private String cartaPresentacion;
    private String estado;
    private LocalDateTime fechaEnvio;

    public Propuesta() {}

    public int getIdPropuesta() { return idPropuesta; }
    public void setIdPropuesta(int idPropuesta) { this.idPropuesta = idPropuesta; }
    public int getIdProyecto() { return idProyecto; }
    public void setIdProyecto(int idProyecto) { this.idProyecto = idProyecto; }
    public String getTituloProyecto() { return tituloProyecto; }
    public void setTituloProyecto(String tituloProyecto) { this.tituloProyecto = tituloProyecto; }
    public int getIdFreelancer() { return idFreelancer; }
    public void setIdFreelancer(int idFreelancer) { this.idFreelancer = idFreelancer; }
    public String getNombreFreelancer() { return nombreFreelancer; }
    public void setNombreFreelancer(String nombreFreelancer) { this.nombreFreelancer = nombreFreelancer; }
    public BigDecimal getCalificacionFreelancer() { return calificacionFreelancer; }
    public void setCalificacionFreelancer(BigDecimal calificacionFreelancer) { this.calificacionFreelancer = calificacionFreelancer; }
    public String getNivelExperiencia() { return nivelExperiencia; }
    public void setNivelExperiencia(String nivelExperiencia) { this.nivelExperiencia = nivelExperiencia; }
    public BigDecimal getMontoOfertado() { return montoOfertado; }
    public void setMontoOfertado(BigDecimal montoOfertado) { this.montoOfertado = montoOfertado; }
    public int getPlazoDias() { return plazoDias; }
    public void setPlazoDias(int plazoDias) { this.plazoDias = plazoDias; }
    public String getCartaPresentacion() { return cartaPresentacion; }
    public void setCartaPresentacion(String cartaPresentacion) { this.cartaPresentacion = cartaPresentacion; }
    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
    public LocalDateTime getFechaEnvio() { return fechaEnvio; }
    public void setFechaEnvio(LocalDateTime fechaEnvio) { this.fechaEnvio = fechaEnvio; }

    public Object getCártaPresentacion() {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }
}