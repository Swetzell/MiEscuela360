package com.sudamericana.impoexcel.controller;

import com.sudamericana.impoexcel.model.Grupo;
import com.sudamericana.impoexcel.model.KardexDetalle;
import com.sudamericana.impoexcel.model.Producto;
import com.sudamericana.impoexcel.model.SubLinea;
import com.sudamericana.impoexcel.service.AlmacenService;
import com.sudamericana.impoexcel.service.GrupoService;
import com.sudamericana.impoexcel.service.KardexService;
import com.sudamericana.impoexcel.service.LineaService;
import com.sudamericana.impoexcel.service.MarcaService;
import com.sudamericana.impoexcel.service.SubLineaService;

import org.springframework.beans.factory.annotation.Autowired;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/kardex")
public class KardexController {

    private static final Logger log = LoggerFactory.getLogger(KardexController.class);
    @Autowired
    private KardexService kardexService;

    @Autowired
    private LineaService lineaService;

    @Autowired
    private SubLineaService sublineaService;

    @Autowired
    private GrupoService grupoService;

    @Autowired
    private MarcaService marcaService;

    @Autowired
    private AlmacenService almacenService;

    @GetMapping
    @PreAuthorize("hasAnyRole('LOGISTICA', 'ADMIN')")
    public String mostrarFormulario(Model model) {
        model.addAttribute("lineas", lineaService.listarLineas());
        model.addAttribute("sublineas", sublineaService.listarSubLineas());
        model.addAttribute("grupos", grupoService.listarGrupos());
        model.addAttribute("marcas", marcaService.listarTodas());
        model.addAttribute("almacenes", almacenService.listarAlmacenes());
        return "kardex/formulario";
    }

    @GetMapping("/api/sublineas/{lineaId}")
    @ResponseBody
    public ResponseEntity<List<SubLinea>> getSubLineas(@PathVariable String lineaId) {
        try {
            List<SubLinea> sublineas = sublineaService.buscarSubLineas(lineaId, "");
            return ResponseEntity.ok(sublineas);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @GetMapping("/api/grupos/{sublineaId}")
    @ResponseBody
    public ResponseEntity<List<Grupo>> getGrupos(@PathVariable String sublineaId) {
        try {
            List<Grupo> grupos = grupoService.buscarGrupos(sublineaId, "");
            return ResponseEntity.ok(grupos);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @GetMapping("/buscar")
    @PreAuthorize("hasAnyRole('LOGISTICA', 'ADMIN')")
    public String buscarProductos(
            @RequestParam(required = false) String almacen,
            @RequestParam(required = false) String codigo,
            @RequestParam(required = false) String descripcion,
            @RequestParam(required = false) String linea,
            @RequestParam(required = false) String marca,
            @RequestParam(defaultValue = "1") int page,
            Model model) {
    
        try {
            log.info("Parámetros recibidos:");
            log.info("almacen: {}", almacen != null ? almacen : "%");
            log.info("codigo: {}", codigo != null ? codigo : "%");
            log.info("descripcion: {}", descripcion != null ? descripcion : "%");
            log.info("linea: {}", linea != null ? linea : "%");
            log.info("marca: {}", marca != null ? marca : "%");
            log.info("página: {}", page);
    
            // Obtener solo 30 registros por página
            List<Producto> productos = kardexService.buscarProductos(
                    almacen,
                    codigo,
                    descripcion,
                    linea,
                    marca,
                    page);
    
            model.addAttribute("productos", productos);
            model.addAttribute("almacen", almacen);
            model.addAttribute("codigo", codigo);
            model.addAttribute("descripcion", descripcion);
            model.addAttribute("linea", linea);
            model.addAttribute("marca", marca);
            model.addAttribute("currentPage", page);
    
            return "kardex/resultados";
        } catch (Exception e) {
            log.error("Error en búsqueda de productos", e);
            model.addAttribute("error", "Error al buscar productos: " + e.getMessage());
            return "error";
        }
    }
@GetMapping("/api/productos")
@ResponseBody
@PreAuthorize("hasAnyRole('LOGISTICA', 'ADMIN')")
public ResponseEntity<Map<String, Object>> getProductosPaginados(
        @RequestParam(defaultValue = "1") int page,
        @RequestParam(required = false) String almacen,
        @RequestParam(required = false) String codigo,
        @RequestParam(required = false) String descripcion,
        @RequestParam(required = false) String linea,
        @RequestParam(required = false) String marca) {
    
    try {
        List<Producto> productos = kardexService.buscarProductos(
                almacen,
                codigo,
                descripcion,
                linea,
                marca,
                page);

        Map<String, Object> response = new HashMap<>();
        response.put("data", productos);
        response.put("recordsTotal", productos.size());
        response.put("recordsFiltered", productos.size());
        
        return ResponseEntity.ok(response);
    } catch (Exception e) {
        log.error("Error al obtener productos paginados", e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
    }
}
    @GetMapping("/detalle/{codigoProducto}")
    @PreAuthorize("hasAnyRole('LOGISTICA', 'ADMIN')")
    public String mostrarDetalleKardex(
            @PathVariable String codigoProducto,
            @RequestParam(required = false) String fechaDesde,
            @RequestParam(required = false) String fechaHasta,
            Model model) {
        try {
            List<KardexDetalle> detalles = kardexService.buscarDetalleKardex(codigoProducto, fechaDesde, fechaHasta);
            model.addAttribute("detalles", detalles);
            model.addAttribute("codigoProducto", codigoProducto);
            model.addAttribute("fechaDesde", fechaDesde);
            model.addAttribute("fechaHasta", fechaHasta);

            // Agregar logging para debug
            System.out.println("Código Producto: " + codigoProducto);
            System.out.println("Cantidad de detalles: " + (detalles != null ? detalles.size() : "null"));

            return "kardex/detalle";
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("error", "Error al cargar los detalles: " + e.getMessage());
            return "kardex/error";
        }
    }

}
