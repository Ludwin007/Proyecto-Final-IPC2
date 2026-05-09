/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.proyf.proyectofinal.servicio;

import com.proyf.proyectofinal.conexion.Conexion;
import com.proyf.proyectofinal.daos.*;
import com.proyf.proyectofinal.modelo.*;
import java.math.BigDecimal;
import java.sql.Connection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author ludwi
 */

public class PropuestaServicio {

    private final PropuestaDAO propuestaDAO = new PropuestaDAO();
    private final ProyectoDAO proyectoDAO = new ProyectoDAO();
    private final ContratoDAO contratoDAO = new ContratoDAO();
    private final UsuarioDAO usuarioDAO = new UsuarioDAO();
    private final ComisionDAO comisionDAO = new ComisionDAO();
    private final HabilidadDAO habilidadDAO = new HabilidadDAO();

    public Map<String, Object> enviar(Propuesta p) {
        Map<String, Object> resultado = new HashMap<>();

        Proyecto proyecto = proyectoDAO.buscarPorId(p.getIdProyecto());
        if (proyecto == null || !proyecto.getEstado().equals("ABIERTO")) {
            resultado.put("error", "El proyecto no esta disponible para recibir propuestas");
            return resultado;
        }
        if (p.getMontoOfertado() == null || p.getMontoOfertado().compareTo(BigDecimal.ZERO) <= 0) {
            resultado.put("error", "El monto ofertado debe ser mayor a cero");
            return resultado;
        }
        if (p.getMontoOfertado().compareTo(proyecto.getPresupuestoMax()) > 0) {
            resultado.put("error", "El monto ofertado no puede superar el presupuesto maximo de Q " + proyecto.getPresupuestoMax());
            return resultado;
        }
        if (p.getPlazoDias() <= 0) {
            resultado.put("error", "El plazo en dias debe ser mayor a cero");
            return resultado;
        }
        if (p.getCartaPresentacion() == null || p.getCartaPresentacion().isBlank()) {
            resultado.put("error", "La carta de presentacion es requerida");
            return resultado;
        }
        if (!habilidadDAO.freelancerTieneHabilidadDeProyecto(p.getIdFreelancer(), p.getIdProyecto())) {
            resultado.put("error", "No posee ninguna de las habilidades requeridas para este proyecto");
            return resultado;
        }
        if (propuestaDAO.freelancerYaEnvio(p.getIdFreelancer(), p.getIdProyecto())) {
            resultado.put("error", "Ya enviaste una propuesta a este proyecto");
            return resultado;
        }

        int id = propuestaDAO.insertar(p);
        if (id < 0) {
            resultado.put("error", "No se pudo registrar la propuesta");
            return resultado;
        }

        resultado.put("mensaje", "Propuesta enviada exitosamente");
        resultado.put("idPropuesta", id);
        return resultado;
    }

    public Map<String, Object> listarPorProyecto(int idProyecto, int idClienteToken) {
        Map<String, Object> resultado = new HashMap<>();

        Proyecto proyecto = proyectoDAO.buscarPorId(idProyecto);
        if (proyecto == null || proyecto.getIdCliente() != idClienteToken) {
            resultado.put("error", "No tiene permiso para ver las propuestas de este proyecto");
            return resultado;
        }

        List<Propuesta> propuestas = propuestaDAO.listarPorProyecto(idProyecto);
        resultado.put("propuestas", propuestas);
        return resultado;
    }

    public Map<String, Object> listarPorFreelancer(int idFreelancer) {
        Map<String, Object> resultado = new HashMap<>();
        List<Propuesta> propuestas = propuestaDAO.listarPorFreelancer(idFreelancer);
        resultado.put("propuestas", propuestas);
        return resultado;
    }

    public Map<String, Object> aceptar(int idPropuesta, int idClienteToken) {
        Map<String, Object> resultado = new HashMap<>();
        Connection conn = Conexion.obtenerConexion();
        if (conn == null) {
            resultado.put("error", "Error de conexion a la base de datos");
            return resultado;
        }

        try {
            conn.setAutoCommit(false);

            Propuesta propuesta = propuestaDAO.buscarPorId(idPropuesta);
            if (propuesta == null || !propuesta.getEstado().equals("PENDIENTE")) {
                resultado.put("error", "La propuesta no existe o ya no esta pendiente");
                conn.rollback();
                return resultado;
            }

            Proyecto proyecto = proyectoDAO.buscarPorId(propuesta.getIdProyecto());
            if (proyecto == null || proyecto.getIdCliente() != idClienteToken) {
                resultado.put("error", "No tiene permiso para aceptar esta propuesta");
                conn.rollback();
                return resultado;
            }
            if (!proyecto.getEstado().equals("ABIERTO")) {
                resultado.put("error", "El proyecto ya no esta en estado ABIERTO");
                conn.rollback();
                return resultado;
            }

            Usuario cliente = usuarioDAO.buscarPorId(idClienteToken);
            if (cliente.getSaldo().compareTo(propuesta.getMontoOfertado()) < 0) {
                resultado.put("error", "Saldo insuficiente. Saldo disponible: Q " + cliente.getSaldo());
                conn.rollback();
                return resultado;
            }

            BigDecimal nuevoSaldo = cliente.getSaldo().subtract(propuesta.getMontoOfertado());
            BigDecimal nuevoSaldoBloqueado = cliente.getSaldoBloqueado().add(propuesta.getMontoOfertado());
            usuarioDAO.actualizarSaldo(conn, idClienteToken, nuevoSaldo, nuevoSaldoBloqueado);

            BigDecimal comision = comisionDAO.obtenerPorcentajeVigente();
            Contrato contrato = new Contrato();
            contrato.setIdPropuesta(idPropuesta);
            contrato.setIdProyecto(proyecto.getIdProyecto());
            contrato.setIdCliente(idClienteToken);
            contrato.setIdFreelancer(propuesta.getIdFreelancer());
            contrato.setMonto(propuesta.getMontoOfertado());
            contrato.setPorcComision(comision);

            int idContrato = contratoDAO.insertar(conn, contrato);
            if (idContrato < 0) {
                resultado.put("error", "No se pudo crear el contrato");
                conn.rollback();
                return resultado;
            }

            propuestaDAO.actualizarEstado(conn, idPropuesta, "ACEPTADA");
            propuestaDAO.rechazarDemas(conn, proyecto.getIdProyecto(), idPropuesta);
            proyectoDAO.actualizarEstado(conn, proyecto.getIdProyecto(), "EN_PROGRESO");

            conn.commit();
            resultado.put("mensaje", "Propuesta aceptada. Contrato creado exitosamente");
            resultado.put("idContrato", idContrato);
        } catch (Exception e) {
            try { conn.rollback(); } catch (Exception ex) { System.err.println(ex.getMessage()); }
            System.err.println("Error aceptar propuesta: " + e.getMessage());
            resultado.put("error", "Error al procesar la propuesta");
        } finally {
            try { conn.setAutoCommit(true); } catch (Exception e) { System.err.println(e.getMessage()); }
            new Conexion().desconectar(conn);
        }

        return resultado;
    }

    public Map<String, Object> rechazar(int idPropuesta, int idClienteToken) {
        Map<String, Object> resultado = new HashMap<>();

        Propuesta propuesta = propuestaDAO.buscarPorId(idPropuesta);
        if (propuesta == null || !propuesta.getEstado().equals("PENDIENTE")) {
            resultado.put("error", "La propuesta no existe o ya no esta pendiente");
            return resultado;
        }

        Proyecto proyecto = proyectoDAO.buscarPorId(propuesta.getIdProyecto());
        if (proyecto == null || proyecto.getIdCliente() != idClienteToken) {
            resultado.put("error", "No tiene permiso para rechazar esta propuesta");
            return resultado;
        }

        boolean ok = propuestaDAO.actualizarEstado(idPropuesta, "RECHAZADA");
        if (!ok) {
            resultado.put("error", "No se pudo rechazar la propuesta");
            return resultado;
        }

        resultado.put("mensaje", "Propuesta rechazada");
        return resultado;
    }

    public Map<String, Object> retirar(int idPropuesta, int idFreelancerToken) {
        Map<String, Object> resultado = new HashMap<>();

        Propuesta propuesta = propuestaDAO.buscarPorId(idPropuesta);
        if (propuesta == null || propuesta.getIdFreelancer() != idFreelancerToken) {
            resultado.put("error", "No tiene permiso para retirar esta propuesta");
            return resultado;
        }

        Proyecto proyecto = proyectoDAO.buscarPorId(propuesta.getIdProyecto());
        if (proyecto == null || !proyecto.getEstado().equals("ABIERTO")) {
            resultado.put("error", "Solo se puede retirar una propuesta cuando el proyecto esta ABIERTO");
            return resultado;
        }
        if (!propuesta.getEstado().equals("PENDIENTE")) {
            resultado.put("error", "La propuesta ya no se puede retirar");
            return resultado;
        }

        boolean ok = propuestaDAO.actualizarEstado(idPropuesta, "RETIRADA");
        if (!ok) {
            resultado.put("error", "No se pudo retirar la propuesta");
            return resultado;
        }

        resultado.put("mensaje", "Propuesta retirada exitosamente");
        return resultado;
    }
}