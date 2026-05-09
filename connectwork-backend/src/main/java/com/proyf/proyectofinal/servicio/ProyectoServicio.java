/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.proyf.proyectofinal.servicio;
import com.proyf.proyectofinal.daos.HabilidadDAO;
import com.proyf.proyectofinal.daos.ProyectoDAO;
import com.proyf.proyectofinal.modelo.Habilidad;
import com.proyf.proyectofinal.modelo.Proyecto;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author ludwi
 */

public class ProyectoServicio {

    private final ProyectoDAO proyectoDAO = new ProyectoDAO();
    private final HabilidadDAO habilidadDAO = new HabilidadDAO();

    public Map<String, Object> publicar(Proyecto p) {
        Map<String, Object> resultado = new HashMap<>();
        if (p.getTitulo() == null || p.getTitulo().length() < 5) {
            resultado.put("error", "El titulo debe tener al menos 5 caracteres");
            return resultado;
        }
        if (p.getDescripcion() == null || p.getDescripcion().isBlank()) {
            resultado.put("error", "La descripcion es requerida");
            return resultado;
        }
        if (p.getIdCategoria() <= 0) {
            resultado.put("error", "Debe seleccionar una categoria");
            return resultado;
        }
        if (p.getPresupuestoMax() == null || p.getPresupuestoMax().compareTo(BigDecimal.ZERO) <= 0) {
            resultado.put("error", "El presupuesto maximo debe ser mayor a cero");
            return resultado;
        }
        if (p.getFechaLimite() == null || !p.getFechaLimite().isAfter(LocalDate.now())) {
            resultado.put("error", "La fecha limite debe ser posterior a la fecha actual");
            return resultado;
        }
        if (p.getHabilidades() == null || p.getHabilidades().isEmpty()) {
            resultado.put("error", "Debe seleccionar al menos una habilidad requerida");
            return resultado;
        }

        int id = proyectoDAO.insertar(p);
        if (id < 0) {
            resultado.put("error", "No se pudo publicar el proyecto");
            return resultado;
        }

        resultado.put("mensaje", "Proyecto publicado exitosamente");
        resultado.put("idProyecto", id);
        return resultado;
    }

    public Map<String, Object> obtenerDetalle(int idProyecto) {
        Map<String, Object> resultado = new HashMap<>();
        Proyecto p = proyectoDAO.buscarPorId(idProyecto);
        if (p == null) {
            resultado.put("error", "Proyecto no encontrado");
            return resultado;
        }
        p.setHabilidades(habilidadDAO.listarDeProyecto(idProyecto));
        resultado.put("proyecto", p);
        return resultado;
    }

    public Map<String, Object> listarPorCliente(int idCliente) {
        Map<String, Object> resultado = new HashMap<>();
        List<Proyecto> proyectos = proyectoDAO.listarPorCliente(idCliente);
        for (Proyecto p : proyectos) {
            p.setHabilidades(habilidadDAO.listarDeProyecto(p.getIdProyecto()));
        }
        resultado.put("proyectos", proyectos);
        return resultado;
    }

    public Map<String, Object> listarAbiertos(Integer idCategoria, BigDecimal presMin, BigDecimal presMax) {
        Map<String, Object> resultado = new HashMap<>();
        List<Proyecto> proyectos = proyectoDAO.listarAbiertos(idCategoria, presMin, presMax);
        for (Proyecto p : proyectos) {
            p.setHabilidades(habilidadDAO.listarDeProyecto(p.getIdProyecto()));
        }
        resultado.put("proyectos", proyectos);
        return resultado;
    }

    public Map<String, Object> actualizar(Proyecto p, int idClienteToken) {
        Map<String, Object> resultado = new HashMap<>();
        Proyecto existente = proyectoDAO.buscarPorId(p.getIdProyecto());
        
        if (existente == null) {
            resultado.put("error", "Proyecto no encontrado");
            return resultado;
        }
        if (existente.getIdCliente() != idClienteToken) {
            resultado.put("error", "No tiene permiso para editar este proyecto");
            return resultado;
        }
        if (!existente.getEstado().equals("ABIERTO")) {
            resultado.put("error", "Solo se pueden editar proyectos en estado ABIERTO");
            return resultado;
        }
        if (p.getTitulo() == null || p.getTitulo().length() < 5) {
            resultado.put("error", "El titulo debe tener al menos 5 caracteres");
            return resultado;
        }
        if (p.getPresupuestoMax() == null || p.getPresupuestoMax().compareTo(BigDecimal.ZERO) <= 0) {
            resultado.put("error", "El presupuesto maximo debe ser mayor a cero");
            return resultado;
        }
        if (p.getFechaLimite() == null || !p.getFechaLimite().isAfter(LocalDate.now())) {
            resultado.put("error", "La fecha limite debe ser posterior a la fecha actual");
            return resultado;
        }

        p.setIdCliente(idClienteToken);
        boolean ok = proyectoDAO.actualizar(p);
        if (!ok) {
            resultado.put("error", "No se pudo actualizar el proyecto");
            return resultado;
        }

        resultado.put("mensaje", "Proyecto actualizado exitosamente");
        return resultado;
    }

    public Map<String, Object> cancelar(int idProyecto, int idClienteToken) {
        Map<String, Object> resultado = new HashMap<>();
        Proyecto existente = proyectoDAO.buscarPorId(idProyecto);
        
        if (existente == null) {
            resultado.put("error", "Proyecto no encontrado");
            return resultado;
        }
        if (existente.getIdCliente() != idClienteToken) {
            resultado.put("error", "No tiene permiso para cancelar este proyecto");
            return resultado;
        }
        if (!existente.getEstado().equals("ABIERTO")) {
            resultado.put("error", "Solo se pueden cancelar proyectos en estado ABIERTO");
            return resultado;
        }

        boolean ok = proyectoDAO.actualizarEstado(idProyecto, "CANCELADO");
        if (!ok) {
            resultado.put("error", "No se pudo cancelar el proyecto");
            return resultado;
        }

        resultado.put("mensaje", "Proyecto cancelado exitosamente");
        return resultado;
    }
}
