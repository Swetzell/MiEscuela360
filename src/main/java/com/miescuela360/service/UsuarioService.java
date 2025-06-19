package com.miescuela360.service;

import com.miescuela360.model.Usuario;
import com.miescuela360.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class UsuarioService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AuditoriaService auditoriaService;

    @Transactional(readOnly = true)
    public List<Usuario> findAll() {
        return usuarioRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Optional<Usuario> findById(Long id) {
        return usuarioRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public Optional<Usuario> findByUsername(String username) {
        return usuarioRepository.findByUsername(username);
    }

    @Transactional(readOnly = true)
    public Optional<Usuario> findByEmail(String email) {
        return usuarioRepository.findByEmail(email);
    }

    @Transactional
    public Usuario save(Usuario usuario) {
        Usuario usuarioAnterior = null;
        String accion = "CREAR";
        
        if (usuario.getId() != null) {
            usuarioAnterior = usuarioRepository.findById(usuario.getId()).orElse(null);
            accion = "ACTUALIZAR";
            
            // Si la contraseña no ha cambiado, mantener la existente
            if (usuario.getPassword() == null || usuario.getPassword().isEmpty()) {
                usuario.setPassword(usuarioAnterior.getPassword());
            } else {
                usuario.setPassword(passwordEncoder.encode(usuario.getPassword()));
            }
        } else {
            // Es un nuevo usuario
            usuario.setPassword(passwordEncoder.encode(usuario.getPassword()));
        }
        
        Usuario usuarioGuardado = usuarioRepository.save(usuario);
        
        // Registrar auditoría
        auditoriaService.registrarAccion(accion, usuarioGuardado, usuarioAnterior);
        
        return usuarioGuardado;
    }

    @Transactional
    public void deleteById(Long id) {
        Optional<Usuario> usuarioOpt = usuarioRepository.findById(id);
        if (usuarioOpt.isPresent()) {
            Usuario usuario = usuarioOpt.get();
            usuarioRepository.deleteById(id);
            
            auditoriaService.registrarAccion("ELIMINAR", usuario, usuario);
        }
    }

    @Transactional
    public boolean existsByUsername(String username) {
        return usuarioRepository.existsByUsername(username);
    }

    @Transactional
    public boolean existsByEmail(String email) {
        return usuarioRepository.existsByEmail(email);
    }

    @Transactional
    public void cambiarPassword(Long id, String nuevaPassword) {
        usuarioRepository.findById(id).ifPresent(usuario -> {
            Usuario usuarioAnterior = new Usuario();
            // Clonar solo los datos necesarios para la auditoría
            usuarioAnterior.setId(usuario.getId());
            usuarioAnterior.setUsername(usuario.getUsername());
            usuarioAnterior.setPassword(usuario.getPassword());
            
            usuario.setPassword(passwordEncoder.encode(nuevaPassword));
            usuarioRepository.save(usuario);
            
            auditoriaService.registrarAccion("CAMBIAR_PASSWORD", usuario, usuarioAnterior);
        });
    }

    @Transactional
    public void activarDesactivar(Long id) {
        usuarioRepository.findById(id).ifPresent(usuario -> {
            Usuario usuarioAnterior = new Usuario();
            // Clonar solo los datos necesarios para la auditoría
            usuarioAnterior.setId(usuario.getId());
            usuarioAnterior.setUsername(usuario.getUsername());
            usuarioAnterior.setActivo(usuario.isActivo());
            
            usuario.setActivo(!usuario.isActivo());
            usuarioRepository.save(usuario);
            
            auditoriaService.registrarAccion("CAMBIAR_ESTADO", usuario, usuarioAnterior);
        });
    }
} 