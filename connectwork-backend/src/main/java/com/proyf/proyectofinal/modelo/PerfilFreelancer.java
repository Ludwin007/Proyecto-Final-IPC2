/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.proyf.proyectofinal.modelo;
import java.math.BigDecimal;

/**
 *
 * @author ludwi
 */

public class PerfilFreelancer {
    private int idPerfilFl;
    private int idUsuario;
    private String biografia;
    private String nivelExperiencia;
    private BigDecimal tarifaHora;
    private BigDecimal calificacionProm;
    private int totalContratos;

    public PerfilFreelancer() {}

    public int getIdPerfilFl() { return idPerfilFl; }
    public void setIdPerfilFl(int idPerfilFl) { this.idPerfilFl = idPerfilFl; }
    public int getIdUsuario() { return idUsuario; }
    public void setIdUsuario(int idUsuario) { this.idUsuario = idUsuario; }
    public String getBiografia() { return biografia; }
    public void setBiografia(String biografia) { this.biografia = biografia; }
    public String getNivelExperiencia() { return nivelExperiencia; }
    public void setNivelExperiencia(String nivelExperiencia) { this.nivelExperiencia = nivelExperiencia; }
    public BigDecimal getTarifaHora() { return tarifaHora; }
    public void setTarifaHora(BigDecimal tarifaHora) { this.tarifaHora = tarifaHora; }
    public BigDecimal getCalificacionProm() { return calificacionProm; }
    public void setCalificacionProm(BigDecimal calificacionProm) { this.calificacionProm = calificacionProm; }
    public int getTotalContratos() { return totalContratos; }
    public void setTotalContratos(int totalContratos) { this.totalContratos = totalContratos; }
}
