package com.sudamericana.impoexcel.controller;

import com.sudamericana.impoexcel.model.Product;
import com.sudamericana.impoexcel.repository.ProductRepository;
import com.sudamericana.impoexcel.service.ExcelService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Controller
@RequestMapping("/excel")
public class ExcelController {
    private static final Logger logger = LoggerFactory.getLogger(ExcelController.class);
    @Autowired
    private ExcelService excelService;

    @Autowired
    private ProductRepository productRepository;
    @GetMapping("/buscar")
    public String buscarProductos(
            @RequestParam(required = false) String codigo,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Model model) {
        try {
            logger.info("Buscando productos con código: {}", codigo);
            Page<Product> productos = excelService.buscarProductosFuzzy(codigo, PageRequest.of(page, size));
            
            model.addAttribute("productos", productos);
            model.addAttribute("codigo", codigo);
            
            // Para depuración
            logger.info("Resultados encontrados: {}", productos.getTotalElements());
            
            return "buscar";
        } catch (Exception e) {
            logger.error("Error al buscar productos: {}", e.getMessage(), e);
            model.addAttribute("error", "Error al buscar productos: " + e.getMessage());
            return "error";
        }
    }

    @GetMapping("/upload")
    public String mostrarFormularioUpload() {
        return "upload";
    }

@PostMapping("/upload")
@ResponseBody
public String uploadExcel(@RequestParam("file") MultipartFile file) {
    try {
        excelService.verificarEstructuraTabla();
        excelService.importarExcel(file.getInputStream());
        return "Excel importado correctamente.";
    } catch (Exception e) {
        return "Error al importar el archivo: " + e.getMessage();
    }
}
    @GetMapping("/exportar")
    public void exportarExcel(@RequestParam("fechaInicio") String fechaInicioStr,
            @RequestParam("fechaFin") String fechaFinStr,
            HttpServletResponse response) {
        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            Date fechaInicio = dateFormat.parse(fechaInicioStr);
            Date fechaFin = dateFormat.parse(fechaFinStr);

            byte[] excelData = excelService.exportarExcelPorFechas(fechaInicio, fechaFin);

            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setHeader("Content-Disposition", "attachment; filename=productos.xlsx");
            response.getOutputStream().write(excelData);
            response.getOutputStream().flush();
        } catch (ParseException | IOException e) {
            e.printStackTrace();
        }
    }

    @GetMapping("/sugerencias")
    @ResponseBody
    public List<String> obtenerSugerencias(@RequestParam String codigo) {
        try {
            return excelService.obtenerSugerenciasCodigoFuzzy(codigo);
        } catch (Exception e) {
            logger.error("Error al obtener sugerencias: {}", e.getMessage(), e);
            return Collections.emptyList();
        }
    }
    @PutMapping("/productos/{id}")
    public ResponseEntity<String> actualizarProducto(@PathVariable Long id, @RequestBody Product producto) {
        try {
            Optional<Product> productoExistente = productRepository.findById(id);
            if (productoExistente.isPresent()) {
                Product productoActualizado = productoExistente.get();
                productoActualizado.setCodigo(producto.getCodigo());
                productoActualizado.setMarca(producto.getMarca());
                productoActualizado.setStock(producto.getStock());
                productoActualizado.setPrecio(producto.getPrecio());
                productoActualizado.setProveedor(producto.getProveedor());
                productoActualizado.setPais(producto.getPais());
                
                productRepository.save(productoActualizado);
                return ResponseEntity.ok("Producto actualizado correctamente");
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al actualizar: " + e.getMessage());
        }
    }


    @DeleteMapping("/productos/{id}")
    public ResponseEntity<String> eliminarProducto(@PathVariable Long id) {
        try {
            if (productRepository.existsById(id)) {
                productRepository.deleteById(id);
                return ResponseEntity.ok("Producto eliminado correctamente");
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al eliminar: " + e.getMessage());
        }
    }
}