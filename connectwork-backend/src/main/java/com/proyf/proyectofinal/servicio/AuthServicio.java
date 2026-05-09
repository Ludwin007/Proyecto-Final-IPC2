/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.proyf.proyectofinal.servicio;

import com.proyf.proyectofinal.daos.PerfilClienteDAO;
import com.proyf.proyectofinal.daos.PerfilFreelancerDAO;
import com.proyf.proyectofinal.daos.UsuarioDAO;
import com.proyf.proyectofinal.modelo.PerfilCliente;
import com.proyf.proyectofinal.modelo.PerfilFreelancer;
import com.proyf.proyectofinal.modelo.Usuario;
import com.proyf.proyectofinal.util.JwtUtil;
import org.mindrot.jbcrypt.BCrypt;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author ludwi
 */


public class AuthServicio {

    private final UsuarioDAO usuarioDAO = new UsuarioDAO();
    private final PerfilClienteDAO perfilClienteDAO = new PerfilClienteDAO();
    private final PerfilFreelancerDAO perfilFreelancerDAO = new PerfilFreelancerDAO();

    public Map<String, Object> login(String username, String contrasena) {
        Map<String, Object> resultado = new HashMap<>();

        if (username == null || username.isBlank() || contrasena == null || contrasena.isBlank()) {
            resultado.put("error", "Las credenciales son requeridas");
            return resultado;
        }

        Usuario u = usuarioDAO.buscarPorUsername(username.trim());
        
        if (u == null) {
            u = usuarioDAO.buscarPorCorreo(username.trim());
        }

        if (u == null) {
            resultado.put("error", "DEBUG: Usuario [" + username + "] no encontrado en la BD");
            return resultado;
        }

        if (!BCrypt.checkpw(contrasena, u.getContrasena())) {
            resultado.put("error", "DEBUG: Contrasena incorrecta para el usuario [" + u.getUsername() + "]");
            return resultado;
        }

        if (!u.isActivo()) {
            resultado.put("error", "Su cuenta ha sido suspendida. Contacte al administrador");
            return resultado;
        }

        String token = JwtUtil.generarToken(u.getIdUsuario(), u.getUsername(), u.getTipoRol(), u.isPerfilCompleto());

        Map<String, Object> datosUsuario = new HashMap<>();
        datosUsuario.put("idUsuario", u.getIdUsuario());
        datosUsuario.put("username", u.getUsername());
        datosUsuario.put("nombreCompleto", u.getNombreCompleto());
        datosUsuario.put("tipoRol", u.getTipoRol());
        datosUsuario.put("perfilCompleto", u.isPerfilCompleto());
        datosUsuario.put("saldo", u.getSaldo());

        resultado.put("token", token);
        resultado.put("usuario", datosUsuario);
        return resultado;
    }

    public Map<String, Object> registrar(Usuario u) {
        Map<String, Object> resultado = new HashMap<>();

        if (u.getUsername() == null || u.getUsername().isBlank()) {
            resultado.put("error", "El username es requerido");
            return resultado;
        }
        if (u.getContrasena() == null || u.getContrasena().length() < 8) {
            resultado.put("error", "La contrasena debe tener minimo 8 caracteres");
            return resultado;
        }
        if (u.getCorreo() == null || u.getCorreo().isBlank()) {
            resultado.put("error", "El correo es requerido");
            return resultado;
        }
        if (u.getNombreCompleto() == null || u.getNombreCompleto().isBlank()) {
            resultado.put("error", "El nombre completo es requerido");
            return resultado;
        }
        if (u.getCui() == null || u.getCui().isBlank()) {
            resultado.put("error", "El CUI es requerido");
            return resultado;
        }
        if (u.getFechaNacimiento() == null) {
            resultado.put("error", "La fecha de nacimiento es requerida");
            return resultado;
        }
        if (!u.getTipoRol().equals("CLIENTE") && !u.getTipoRol().equals("FREELANCER")) {
            resultado.put("error", "El tipo de rol no es valido");
            return resultado;
        }
        if (usuarioDAO.existeUsername(u.getUsername().trim())) {
            resultado.put("error", "El nombre de usuario ya esta en uso");
            return resultado;
        }
        if (usuarioDAO.existeCorreo(u.getCorreo().trim())) {
            resultado.put("error", "El correo electronico ya esta registrado");
            return resultado;
        }

        u.setUsername(u.getUsername().trim());
        u.setCorreo(u.getCorreo().trim());
        u.setContrasena(BCrypt.hashpw(u.getContrasena(), BCrypt.gensalt(12)));

        int idGenerado = usuarioDAO.insertar(u);
        if (idGenerado < 0) {
            resultado.put("error", "No se pudo registrar el usuario");
            return resultado;
        }

        resultado.put("mensaje", "Usuario registrado exitosamente");
        resultado.put("idUsuario", idGenerado);
        return resultado;
    }

    public Map<String, Object> completarPerfilCliente(int idUsuario, PerfilCliente perfil) {
        Map<String, Object> resultado = new HashMap<>();

        if (perfil.getDescripcion() == null || perfil.getDescripcion().isBlank()) {
            resultado.put("error", "La descripcion es requerida");
            return resultado;
        }
        if (perfil.getSector() == null || perfil.getSector().isBlank()) {
            resultado.put("error", "El sector es requerido");
            return resultado;
        }

        perfil.setIdUsuario(idUsuario);
        PerfilCliente existente = perfilClienteDAO.buscarPorUsuario(idUsuario);
        boolean ok = existente == null ? perfilClienteDAO.insertar(perfil) : perfilClienteDAO.actualizar(perfil);

        if (!ok) {
            resultado.put("error", "No se pudo guardar el perfil");
            return resultado;
        }

        usuarioDAO.actualizarPerfilCompleto(idUsuario);
        resultado.put("mensaje", "Perfil completado exitosamente");
        return resultado;
    }

    public Map<String, Object> completarPerfilFreelancer(int idUsuario, PerfilFreelancer perfil, List<Integer> idHabilidades) {
        Map<String, Object> resultado = new HashMap<>();

        if (perfil.getBiografia() == null || perfil.getBiografia().isBlank()) {
            resultado.put("error", "La biografia es requerida");
            return resultado;
        }
        if (perfil.getNivelExperiencia() == null) {
            resultado.put("error", "El nivel de experiencia es requerido");
            return resultado;
        }
        if (perfil.getTarifaHora() == null || perfil.getTarifaHora().compareTo(BigDecimal.ZERO) <= 0) {
            resultado.put("error", "La tarifa por hora debe ser mayor a cero");
            return resultado;
        }
        if (idHabilidades == null || idHabilidades.isEmpty()) {
            resultado.put("error", "Debe seleccionar al menos una habilidad");
            return resultado;
        }

        perfil.setIdUsuario(idUsuario);
        PerfilFreelancer existente = perfilFreelancerDAO.buscarPorUsuario(idUsuario);
        boolean ok = existente == null ? perfilFreelancerDAO.insertar(perfil) : perfilFreelancerDAO.actualizar(perfil);

        if (!ok) {
            resultado.put("error", "No se pudo guardar el perfil");
            return resultado;
        }

        perfilFreelancerDAO.eliminarHabilidades(idUsuario);
        for (int idH : idHabilidades) {
            perfilFreelancerDAO.agregarHabilidad(idUsuario, idH);
        }

        usuarioDAO.actualizarPerfilCompleto(idUsuario);
        resultado.put("mensaje", "Perfil completado exitosamente");
        return resultado;
    }

    public Map<String, Object> crearAdmin(Usuario u) {
        Map<String, Object> resultado = new HashMap<>();

        if (u.getUsername() == null || u.getUsername().isBlank()) {
            resultado.put("error", "El username es requerido");
            return resultado;
        }
        if (u.getContrasena() == null || u.getContrasena().length() < 8) {
            resultado.put("error", "La contrasena debe tener minimo 8 caracteres");
            return resultado;
        }
        if (usuarioDAO.existeUsername(u.getUsername().trim())) {
            resultado.put("error", "El nombre de usuario ya esta en uso");
            return resultado;
        }
        if (usuarioDAO.existeCorreo(u.getCorreo().trim())) {
            resultado.put("error", "El correo ya esta registrado");
            return resultado;
        }

        u.setTipoRol("ADMIN");
        u.setContrasena(BCrypt.hashpw(u.getContrasena(), BCrypt.gensalt(12)));

        int id = usuarioDAO.insertar(u);
        if (id < 0) {
            resultado.put("error", "No se pudo crear el administrador");
            return resultado;
        }

        usuarioDAO.actualizarPerfilCompleto(id);
        resultado.put("mensaje", "Administrador creado exitosamente");
        resultado.put("idUsuario", id);
        return resultado;
    }
}