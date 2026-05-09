/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.proyf.proyectofinal.servicio;

import com.proyf.proyectofinal.conexion.Conexion;
import com.proyf.proyectofinal.daos.*;
import com.proyf.proyectofinal.modelo.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author ludwi
 */

public class ContratoServicio {

    private final ContratoDAO contratoDAO = new ContratoDAO();
    private final EntregaDAO entregaDAO = new EntregaDAO();
    private final ProyectoDAO proyectoDAO = new ProyectoDAO();
    private final UsuarioDAO usuarioDAO = new UsuarioDAO();
    private final CalificacionDAO calificacionDAO = new CalificacionDAO();
    private final PerfilFreelancerDAO perfilFreelancerDAO = new PerfilFreelancerDAO();

    public Map<String, Object> listarPorFreelancer(int idFreelancer) {
        Map<String, Object> resultado = new HashMap<>();
        List<Contrato> contratos = contratoDAO.listarPorFreelancer(idFreelancer);
        resultado.put("contratos", contratos);
        return resultado;
    }

    public Map<String, Object> listarPorCliente(int idCliente) {
        Map<String, Object> resultado = new HashMap<>();
        List<Contrato> contratos = contratoDAO.listarPorCliente(idCliente);
        resultado.put("contratos", contratos);
        return resultado;
    }

    public Map<String, Object> obtenerDetalle(int idContrato, int idUsuarioToken, String rol) {
        Map<String, Object> resultado = new HashMap<>();
        Contrato contrato = contratoDAO.buscarPorId(idContrato);
        if (contrato == null) {
            resultado.put("error", "Contrato no encontrado");
            return resultado;
        }

        boolean esCliente = rol.equals("CLIENTE") && contrato.getIdCliente() == idUsuarioToken;
        boolean esFreelancer = rol.equals("FREELANCER") && contrato.getIdFreelancer() == idUsuarioToken;
        boolean esAdmin = rol.equals("ADMIN");

        if (!esCliente && !esFreelancer && !esAdmin) {
            resultado.put("error", "No tiene permiso para ver este contrato");
            return resultado;
        }

        List<Entrega> entregas = entregaDAO.listarPorContrato(idContrato);
        Calificacion calificacion = calificacionDAO.buscarPorContrato(idContrato);

        resultado.put("contrato", contrato);
        resultado.put("entregas", entregas);
        resultado.put("calificacion", calificacion);
        return resultado;
    }

    public Map<String, Object> subirEntrega(Entrega entrega, int idFreelancerToken) {
        Map<String, Object> resultado = new HashMap<>();
        Contrato contrato = contratoDAO.buscarPorId(entrega.getIdContrato());

        if (contrato == null || contrato.getIdFreelancer() != idFreelancerToken) {
            resultado.put("error", "No tiene permiso para entregar en este contrato");
            return resultado;
        }
        if (!contrato.getEstado().equals("EN_PROGRESO")) {
            resultado.put("error", "El contrato no esta en estado EN_PROGRESO");
            return resultado;
        }
        if (entregaDAO.hayEntregaPendiente(entrega.getIdContrato())) {
            resultado.put("error", "Ya existe una entrega pendiente de revision para este contrato");
            return resultado;
        }
        if (entrega.getDescripcion() == null || entrega.getDescripcion().length() < 10) {
            resultado.put("error", "La descripcion debe tener al menos 10 caracteres");
            return resultado;
        }

        int idEntrega = entregaDAO.insertar(entrega);
        if (idEntrega < 0) {
            resultado.put("error", "No se pudo registrar la entrega");
            return resultado;
        }

        proyectoDAO.actualizarEstado(contrato.getIdProyecto(), "ENTREGA_PENDIENTE");
        resultado.put("mensaje", "Entrega registrada. El cliente sera notificado");
        resultado.put("idEntrega", idEntrega);
        return resultado;
    }

    public Map<String, Object> retirarEntrega(int idEntrega, int idFreelancerToken) {
        Map<String, Object> resultado = new HashMap<>();
        Entrega entrega = entregaDAO.buscarPorId(idEntrega);
        if (entrega == null || !entrega.getEstado().equals("PENDIENTE")) {
            resultado.put("error", "La entrega no existe o ya fue procesada");
            return resultado;
        }

        Contrato contrato = contratoDAO.buscarPorId(entrega.getIdContrato());
        if (contrato == null || contrato.getIdFreelancer() != idFreelancerToken) {
            resultado.put("error", "No tiene permiso para retirar esta entrega");
            return resultado;
        }

        Connection conn = Conexion.obtenerConexion();
        if (conn == null) {
            resultado.put("error", "Error de conexion");
            return resultado;
        }

        try {
            conn.setAutoCommit(false);
            entregaDAO.eliminar(conn, idEntrega);
            contratoDAO.actualizarEstado(conn, contrato.getIdContrato(), "EN_PROGRESO", null);
            proyectoDAO.actualizarEstado(conn, contrato.getIdProyecto(), "EN_PROGRESO");
            conn.commit();
            resultado.put("mensaje", "Entrega retirada exitosamente");
        } catch (Exception e) {
            try { if (conn != null) conn.rollback(); } catch (Exception ex) { System.err.println(ex.getMessage()); }
            System.err.println("Error retirarEntrega: " + e.getMessage());
            resultado.put("error", "Error al retirar la entrega");
        } finally {
            try { if (conn != null) conn.setAutoCommit(true); } catch (Exception e) { System.err.println(e.getMessage()); }
            new Conexion().desconectar(conn);
        }
        return resultado;
    }

    public Map<String, Object> aprobarEntrega(int idEntrega, int idClienteToken) {
        Map<String, Object> resultado = new HashMap<>();
        Connection conn = Conexion.obtenerConexion();
        if (conn == null) {
            resultado.put("error", "Error de conexion");
            return resultado;
        }

        try {
            conn.setAutoCommit(false);
            Entrega entrega = entregaDAO.buscarPorId(idEntrega);
            if (entrega == null || !entrega.getEstado().equals("PENDIENTE")) {
                resultado.put("error", "La entrega no existe o ya fue procesada");
                conn.rollback();
                return resultado;
            }

            Contrato contrato = contratoDAO.buscarPorId(entrega.getIdContrato());
            if (contrato == null || contrato.getIdCliente() != idClienteToken) {
                resultado.put("error", "No tiene permiso para aprobar esta entrega");
                conn.rollback();
                return resultado;
            }

            BigDecimal comision = contrato.getPorcComision().divide(BigDecimal.valueOf(100), 10, RoundingMode.HALF_UP);
            BigDecimal montoComision = contrato.getMonto().multiply(comision).setScale(2, RoundingMode.HALF_UP);
            BigDecimal montoFreelancer = contrato.getMonto().subtract(montoComision);

            Usuario freelancer = usuarioDAO.buscarPorId(contrato.getIdFreelancer());
            BigDecimal nuevoSaldoFl = freelancer.getSaldo().add(montoFreelancer);
            usuarioDAO.actualizarSaldo(conn, freelancer.getIdUsuario(), nuevoSaldoFl, freelancer.getSaldoBloqueado());

            Usuario cliente = usuarioDAO.buscarPorId(idClienteToken);
            
            BigDecimal nuevoSaldoBloqueado = cliente.getSaldoBloqueado().subtract(contrato.getMonto());
            if (nuevoSaldoBloqueado.compareTo(BigDecimal.ZERO) < 0) nuevoSaldoBloqueado = BigDecimal.ZERO;
            BigDecimal nuevoSaldoCliente = cliente.getSaldo();
            
            usuarioDAO.actualizarSaldo(conn, idClienteToken, nuevoSaldoCliente, nuevoSaldoBloqueado);

            entregaDAO.actualizarEstado(conn, idEntrega, "APROBADA", null);
            contratoDAO.actualizarEstado(conn, contrato.getIdContrato(), "COMPLETADO", null);
            proyectoDAO.actualizarEstado(conn, contrato.getIdProyecto(), "COMPLETADO");
            
            perfilFreelancerDAO.actualizarCalificacion(conn, contrato.getIdFreelancer());

            resultado.put("nuevoSaldoCliente", nuevoSaldoCliente);

            conn.commit();
            resultado.put("mensaje", "Entrega aprobada. Pago liberado al freelancer");
            resultado.put("montoFreelancer", montoFreelancer);
            resultado.put("comisionPlataforma", montoComision);
            resultado.put("idContrato", contrato.getIdContrato());

        } catch (Exception e) {
            try { if (conn != null) conn.rollback(); } catch (Exception ex) { System.err.println(ex.getMessage()); }
            System.err.println("Error aprobarEntrega: " + e.getMessage());
            resultado.put("error", "Error al procesar la aprobacion");
        } finally {
            try { if (conn != null) conn.setAutoCommit(true); } catch (Exception e) { System.err.println(e.getMessage()); }
            new Conexion().desconectar(conn);
        }
        return resultado;
    }

    public Map<String, Object> rechazarEntrega(int idEntrega, int idClienteToken, String motivo) {
        Map<String, Object> resultado = new HashMap<>();
        if (motivo == null || motivo.isBlank()) {
            resultado.put("error", "El motivo de rechazo es obligatorio");
            return resultado;
        }

        Entrega entrega = entregaDAO.buscarPorId(idEntrega);
        if (entrega == null || !entrega.getEstado().equals("PENDIENTE")) {
            resultado.put("error", "La entrega no existe o ya fue procesada");
            return resultado;
        }

        Contrato contrato = contratoDAO.buscarPorId(entrega.getIdContrato());
        if (contrato == null || contrato.getIdCliente() != idClienteToken) {
            resultado.put("error", "No tiene permiso para rechazar esta entrega");
            return resultado;
        }

        Connection conn = Conexion.obtenerConexion();
        try {
            conn.setAutoCommit(false);
            entregaDAO.actualizarEstado(conn, idEntrega, "RECHAZADA", motivo);
            proyectoDAO.actualizarEstado(conn, contrato.getIdProyecto(), "EN_PROGRESO");
            conn.commit();
            resultado.put("mensaje", "Entrega rechazada. El freelancer puede subir una nueva entrega");
        } catch (Exception e) {
            try { conn.rollback(); } catch (Exception ex) { System.err.println(ex.getMessage()); }
            resultado.put("error", "Error al procesar el rechazo");
        } finally {
            new Conexion().desconectar(conn);
        }
        return resultado;
    }

    public Map<String, Object> cancelarContrato(int idContrato, int idClienteToken, String motivo) {
        Map<String, Object> resultado = new HashMap<>();
        if (motivo == null || motivo.isBlank()) {
            resultado.put("error", "El motivo de cancelacion es obligatorio");
            return resultado;
        }

        Contrato contrato = contratoDAO.buscarPorId(idContrato);
        if (contrato == null || contrato.getIdCliente() != idClienteToken) {
            resultado.put("error", "No tiene permiso para cancelar este contrato");
            return resultado;
        }
        if (contrato.getEstado().equals("COMPLETADO") || contrato.getEstado().equals("CANCELADO")) {
            resultado.put("error", "El contrato ya fue finalizado");
            return resultado;
        }

        Connection conn = Conexion.obtenerConexion();
        try {
            conn.setAutoCommit(false);
            Usuario cliente = usuarioDAO.buscarPorId(idClienteToken);
            
            BigDecimal montoADevolver = contrato.getMonto().min(cliente.getSaldoBloqueado());
            BigDecimal nuevoSaldo = cliente.getSaldo().add(montoADevolver);
            BigDecimal nuevoSaldoBloqueado = cliente.getSaldoBloqueado().subtract(montoADevolver);
            
            usuarioDAO.actualizarSaldo(conn, idClienteToken, nuevoSaldo, nuevoSaldoBloqueado);
            contratoDAO.actualizarEstado(conn, contrato.getIdContrato(), "CANCELADO", motivo);
            proyectoDAO.actualizarEstado(conn, contrato.getIdProyecto(), "CANCELADO");
            
            conn.commit();
            resultado.put("mensaje", "Contrato cancelado. El saldo bloqueado fue devuelto");
        } catch (Exception e) {
            try { conn.rollback(); } catch (Exception ex) { System.err.println(ex.getMessage()); }
            resultado.put("error", "Error al cancelar el contrato");
        } finally {
            new Conexion().desconectar(conn);
        }
        return resultado;
    }

    public Map<String, Object> calificar(Calificacion cal, int idClienteToken) {
        Map<String, Object> resultado = new HashMap<>();
        if (cal.getPuntuacion() < 1 || cal.getPuntuacion() > 5) {
            resultado.put("error", "La puntuacion debe estar entre 1 y 5");
            return resultado;
        }

        Contrato contrato = contratoDAO.buscarPorId(cal.getIdContrato());
        if (contrato == null || contrato.getIdCliente() != idClienteToken) {
            resultado.put("error", "No tiene permiso para calificar este contrato");
            return resultado;
        }
        if (!contrato.getEstado().equals("COMPLETADO")) {
            resultado.put("error", "Solo se pueden calificar contratos completados");
            return resultado;
        }

        Calificacion existente = calificacionDAO.buscarPorContrato(cal.getIdContrato());
        if (existente != null) {
            resultado.put("error", "Este contrato ya tiene una calificacion registrada");
            return resultado;
        }

        Connection conn = Conexion.obtenerConexion();
        try {
            conn.setAutoCommit(false);
            cal.setIdCliente(idClienteToken);
            cal.setIdFreelancer(contrato.getIdFreelancer());
            
            calificacionDAO.insertar(conn, cal);
            perfilFreelancerDAO.actualizarCalificacion(conn, contrato.getIdFreelancer());
            
            conn.commit();
            resultado.put("mensaje", "Calificacion registrada exitosamente");
        } catch (Exception e) {
            try { conn.rollback(); } catch (Exception ex) { System.err.println(ex.getMessage()); }
            resultado.put("error", "Error al registrar la calificacion");
        } finally {
            new Conexion().desconectar(conn);
        }
        return resultado;
    }

    public Map<String, Object> eliminarCalificacion(int idContrato, int idClienteToken) {
        Map<String, Object> resultado = new HashMap<>();
        Contrato contrato = contratoDAO.buscarPorId(idContrato);
        if (contrato == null || contrato.getIdCliente() != idClienteToken) {
            resultado.put("error", "No tiene permiso para eliminar esta calificacion");
            return resultado;
        }
        Calificacion existente = calificacionDAO.buscarPorContrato(idContrato);
        if (existente == null) {
            resultado.put("error", "Este contrato no tiene calificacion registrada");
            return resultado;
        }

        Connection conn = Conexion.obtenerConexion();
        try {
            conn.setAutoCommit(false);
            calificacionDAO.eliminarPorContrato(conn, idContrato);
            perfilFreelancerDAO.actualizarCalificacion(conn, contrato.getIdFreelancer());
            conn.commit();
            resultado.put("mensaje", "Calificacion eliminada exitosamente");
        } catch (Exception e) {
            try { conn.rollback(); } catch (Exception ex) { System.err.println(ex.getMessage()); }
            resultado.put("error", "Error al eliminar la calificacion");
        } finally {
            new Conexion().desconectar(conn);
        }
        return resultado;
    }
}