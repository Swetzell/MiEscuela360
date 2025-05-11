package com.sudamericana.impoexcel.config;

import com.sudamericana.impoexcel.model.Servicio;
import com.sudamericana.impoexcel.model.Usuario;
import com.sudamericana.impoexcel.service.ServicioService;
import com.sudamericana.impoexcel.service.UsuarioService;

import jakarta.annotation.PostConstruct;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DataLoader {

    @Autowired
    private ServicioService servicioService;
    
    
    @Bean
    CommandLineRunner initDatabase() {
        return args -> {
            try {
                // Verificar si hay servicios registrados
                List<Servicio> serviciosExistentes = servicioService.listarTodos();
                
                if (serviciosExistentes.isEmpty()) {
                    System.out.println("Iniciando carga de servicios predefinidos...");
                    List<Servicio> serviciosCreados = new ArrayList<>();
                    
                    serviciosCreados.add(crearServicioSiNoExiste("Conciliaciones Bancarias",
                            "Módulo de conciliaciones bancarias",
                            "/conciliaciones"));
    
                    serviciosCreados.add(crearServicioSiNoExiste("Reposición de Productos",
                            "Gestión de reposición de productos",
                            "/reposicion-productos"));
    
                    serviciosCreados.add(crearServicioSiNoExiste("Reportes ABC",
                            "Generación de reportes ABC",
                            "/reportesabc"));
    
                    serviciosCreados.add(crearServicioSiNoExiste("Mercadería en Tránsito",
                            "Control de mercadería en tránsito",
                            "/listado-transito"));
    
                    serviciosCreados.add(crearServicioSiNoExiste("Gestión de Usuarios",
                            "Administración de usuarios del sistema",
                            "/usuarios"));
    
                    serviciosCreados.add(crearServicioSiNoExiste("Kardex",
                            "Gestión de kardex de productos",
                            "/kardex"));
    
                    serviciosCreados.add(crearServicioSiNoExiste("Cotizaciones Pendientes",
                            "Gestión de cotizaciones pendientes",
                            "/cotizaciones"));
    
                    serviciosCreados.add(crearServicioSiNoExiste("Retenedores",
                            "Gestión de retenedores con saldo",
                            "/retenedores"));
    
                    serviciosCreados.add(crearServicioSiNoExiste("Exportar Precios OC",
                            "Exportación de precios de órdenes de compra",
                            "/exportar-precios-oc"));
    
                    // Asignar todos los servicios a los administradores
                    asignarServiciosAAdministradores(serviciosCreados);
                    
                    System.out.println("Carga de servicios completada. Se crearon " + serviciosCreados.size() + " servicios.");
                } else {
                    System.out.println("Ya existen " + serviciosExistentes.size() + " servicios en la base de datos. No se cargarán nuevos servicios.");
                }
            } catch (Exception e) {
                System.err.println("Error al cargar los servicios: " + e.getMessage());
                e.printStackTrace();
            }
        };
    }

        private Servicio crearServicioSiNoExiste(String nombre, String descripcion, String ruta) {
            try {
                Optional<Servicio> servicioExistente = servicioService.buscarPorNombre(nombre);
                if (servicioExistente.isPresent()) {
                    System.out.println("Servicio ya existe: " + nombre);
                    return servicioExistente.get();
                }
        
                System.out.println("Creando nuevo servicio: " + nombre);
                Servicio servicio = new Servicio();
                servicio.setNombre(nombre);
                servicio.setDescripcion(descripcion);
                servicio.setRuta(ruta);
                Servicio servicioGuardado = servicioService.guardar(servicio);
                System.out.println("Servicio guardado con ID: " + servicioGuardado.getId());
                return servicioGuardado;
            } catch (Exception e) {
                System.err.println("Error al crear servicio " + nombre + ": " + e.getMessage());
                e.printStackTrace();
                return null;
            }
        }

    @Autowired
    private UsuarioService usuarioService;

    private void asignarServiciosAAdministradores(List<Servicio> servicios) {
        List<Usuario> administradores = usuarioService.obtenerTodos().stream()
                .filter(u -> u.getRoles().stream().anyMatch(
                        r -> r.getNombre().equals("ADMIN") || r.getNombre().equals("ROLE_ADMIN")))
                .collect(Collectors.toList());

        for (Usuario admin : administradores) {
            Set<Servicio> serviciosAdmin = admin.getServicios();
            serviciosAdmin.addAll(servicios);
            admin.setServicios(serviciosAdmin);
            usuarioService.actualizarUsuario(admin);
        }
    }

    @PostConstruct
    public void verificarServiciosEnBaseDeDatos() {
    try {
        List<Servicio> servicios = servicioService.listarTodos();
        System.out.println("==================================================");
        System.out.println("VERIFICACIÓN DE SERVICIOS EN BASE DE DATOS");
        System.out.println("Total de servicios encontrados: " + servicios.size());
        for (Servicio s : servicios) {
            System.out.println("ID: " + s.getId() + " - Nombre: " + s.getNombre() + " - Ruta: " + s.getRuta());
        }
        System.out.println("==================================================");
    } catch (Exception e) {
        System.err.println("Error al verificar servicios: " + e.getMessage());
        e.printStackTrace();
    }
}
}