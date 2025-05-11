package com.sudamericana.impoexcel.controller;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.sudamericana.impoexcel.model.BancoCuenta;
import com.sudamericana.impoexcel.model.BancoEntidad;
import com.sudamericana.impoexcel.model.BancoEstado;
import com.sudamericana.impoexcel.model.BancoRegistro;
import com.sudamericana.impoexcel.model.ConciliacionDetalle;
import com.sudamericana.impoexcel.service.ConciliacionBancariaService;

@Controller
@RequestMapping("/conciliaciones")
public class ConciliacionBancariaController {

    @Autowired
    private ConciliacionBancariaService conciliacionService;

    @GetMapping
    public String mostrarFormulario(Model model) {
        // Inicializar el modelo con datos por defecto
        model.addAttribute("codigoEntidad", "1");
        model.addAttribute("lstEntidad", conciliacionService.obtenerEntidades());
        model.addAttribute("listaCta", conciliacionService.obtenerCuentasPorEntidad("1"));

        // Fecha actual en formato MM/yyyy
        String fechaActual = LocalDate.now().format(DateTimeFormatter.ofPattern("MM/yyyy"));
        model.addAttribute("fechaMesAnio", fechaActual);

        // Crear un objeto para el estado del banco
        BancoEstado estadoBanco = new BancoEstado(0, "Sin conciliar");
        model.addAttribute("estadoBanco", estadoBanco);

        // Listas vacías para los detalles
        model.addAttribute("grillaRegistro", new ArrayList<BancoRegistro>());
        model.addAttribute("grillaDetalle", new ArrayList<>());
        model.addAttribute("conciliacionDetalles", new ArrayList<ConciliacionDetalle>());

        return "conciliaciones/form";
    }

    @GetMapping("/actualizarCuentas")
    @ResponseBody
    public List<BancoCuenta> actualizarCuentas(@RequestParam("entidad") String entidad) {
        return conciliacionService.obtenerCuentasPorEntidad(entidad);
    }

    @GetMapping("/cuentas-por-entidad")
    @ResponseBody
    public ResponseEntity<List<BancoCuenta>> obtenerCuentasPorEntidad(@RequestParam("entidad") String entidad) {
        try {
            System.out.println("Solicitadas cuentas para entidad: " + entidad);

            // Obtener cuentas del servicio
            List<BancoCuenta> cuentas = conciliacionService.obtenerCuentasPorEntidad(entidad);

            System.out.println("Cuentas encontradas: " + cuentas.size());
            for (BancoCuenta cuenta : cuentas) {
                System.out.println("  - " + cuenta.getNrocta() + ": " + cuenta.getDescrip());
            }

            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(cuentas);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(null);
        }
    }
    @PostMapping("/cargar-archivo")
@ResponseBody
public Map<String, Object> cargarArchivo(
        @RequestParam("file") MultipartFile file,
        @RequestParam("entidad") String entidad,
        @RequestParam("cuenta") String cuenta,
        @RequestParam("periodo") String periodo) {
    
    Map<String, Object> response = new HashMap<>();
    
    try {
        System.out.println("Recibido archivo para cuenta: " + cuenta + ", periodo: " + periodo);
        System.out.println("Tipo de contenido del archivo: " + file.getContentType());
        System.out.println("Nombre original del archivo: " + file.getOriginalFilename());
        
        // Validación mejorada para archivos Excel
        String contentType = file.getContentType();
        String fileName = file.getOriginalFilename();
        boolean isExcel = (contentType != null && 
                (contentType.contains("excel") || 
                 contentType.contains("spreadsheetml") ||
                 contentType.contains("ms-excel"))) ||
                (fileName != null && 
                (fileName.toLowerCase().endsWith(".xlsx") || 
                 fileName.toLowerCase().endsWith(".xls")));
                
        if (!isExcel) {
            System.err.println("Tipo de archivo no permitido: " + contentType);
            response.put("success", false);
            response.put("message", "El archivo debe ser de tipo Excel (.xls o .xlsx)");
            return response;
        }
        
        // Formato del periodo a MMYYYY si viene como MM/YYYY
        if (periodo.contains("/")) {
            String[] parts = periodo.split("/");
            periodo = parts[0] + parts[1];
        }
        
        // Procesar el archivo
        boolean result = conciliacionService.procesarArchivoExcel(file, cuenta, periodo);
        
        if (result) {
            // Obtener conteo de registros procesados
            List<BancoRegistro> registros = conciliacionService.obtenerRegistrosBanco(periodo, cuenta);
            
            response.put("success", true);
            response.put("message", "Archivo procesado correctamente");
            response.put("registros", registros.size());
            return response;
        } else {
            response.put("success", false);
            response.put("message", "No se pudieron procesar registros del archivo");
            return response;
        }
        
    } catch (Exception e) {
        e.printStackTrace();
        response.put("success", false);
        response.put("message", "Error: " + e.getMessage());
        return response;
    }
}
    @PostMapping("/buscar")
    public String buscarConciliacion(
            @RequestParam("codigoEntidad") String entidad,
            @RequestParam("nroCuenta") String cuenta,
            @RequestParam("fechaMesAnio") String fechaMesAnio,
            Model model) {

        // Convertir el formato MM/yyyy a MMyyyy para la base de datos
        String aniomes = fechaMesAnio.replace("/", "");

        // Obtener datos para el formulario
        model.addAttribute("codigoEntidad", entidad);
        model.addAttribute("nroCuenta", cuenta);
        model.addAttribute("fechaMesAnio", fechaMesAnio);
        model.addAttribute("lstEntidad", conciliacionService.obtenerEntidades());
        model.addAttribute("listaCta", conciliacionService.obtenerCuentasPorEntidad(entidad));

        // Obtener el estado de la conciliación
        BancoEstado estadoBanco = conciliacionService.obtenerEstadoConciliacion(aniomes, cuenta);
        model.addAttribute("estadoBanco", estadoBanco);

        // Obtener los registros bancarios
        List<BancoRegistro> registros = conciliacionService.obtenerRegistrosBanco(aniomes, cuenta);
        model.addAttribute("grillaRegistro", registros);

        // Obtener los detalles de conciliación
        List<ConciliacionDetalle> detallesConciliacion = conciliacionService.procesarConciliacionBancaria(cuenta,
                aniomes);
        model.addAttribute("conciliacionDetalles", detallesConciliacion);

        // Calcular los totales para el detalle
        List<Map<String, Object>> detalles = calcularDetalles(registros);
        model.addAttribute("grillaDetalle", detalles);

        return "conciliaciones/form";
    }

    @PostMapping("/upload")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam("nroCuenta") String cuenta,
            @RequestParam("fechaMesAnio") String fechaMesAnio) {

        Map<String, Object> response = new HashMap<>();

        try {
            // Validar que el archivo sea de tipo Excel
            if (!file.getContentType().equals("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet") &&
                    !file.getContentType().equals("application/vnd.ms-excel")) {
                response.put("success", false);
                response.put("message", "El archivo debe ser de tipo Excel (.xls o .xlsx)");
                return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
            }

            // Convertir el formato MM/yyyy a MMyyyy para la base de datos
            String aniomes = fechaMesAnio.replace("/", "");

            boolean result = conciliacionService.procesarArchivoExcel(file, cuenta, aniomes);

            if (result) {
                response.put("success", true);
                response.put("message", "Archivo procesado correctamente");
                return new ResponseEntity<>(response, HttpStatus.OK);
            } else {
                response.put("success", false);
                response.put("message", "No se pudo procesar el archivo. Verifique el formato.");
                return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
            }

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error al procesar el archivo: " + e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/procesar")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> procesarConciliacion(
            @RequestParam("nroCuenta") String cuenta,
            @RequestParam("fechaMesAnio") String fechaMesAnio) {

        Map<String, Object> response = new HashMap<>();

        try {
            // Convertir el formato MM/yyyy a MMyyyy para la base de datos
            String aniomes = fechaMesAnio.replace("/", "");

            List<ConciliacionDetalle> detalles = conciliacionService.procesarConciliacionBancaria(cuenta, aniomes);

            response.put("success", true);
            response.put("message", "Conciliación procesada correctamente");
            response.put("detalles", detalles);
            return new ResponseEntity<>(response, HttpStatus.OK);

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error al procesar la conciliación: " + e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/actualizar-estado")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> actualizarEstado(
            @RequestParam("nroCuenta") String cuenta,
            @RequestParam("fechaMesAnio") String fechaMesAnio,
            @RequestParam("item") Integer item,
            @RequestParam("estado") Integer estado,
            @RequestParam("itemConciliacion") Integer itemConciliacion) {

        Map<String, Object> response = new HashMap<>();

        try {
            // Convertir el formato MM/yyyy a MMyyyy para la base de datos
            String aniomes = fechaMesAnio.replace("/", "");

            boolean result = conciliacionService.actualizarEstadoConciliacion(cuenta, aniomes, item, estado,
                    itemConciliacion);

            if (result) {
                response.put("success", true);
                response.put("message", "Estado actualizado correctamente");
                return new ResponseEntity<>(response, HttpStatus.OK);
            } else {
                response.put("success", false);
                response.put("message", "No se pudo actualizar el estado");
                return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
            }

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error al actualizar el estado: " + e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Método para calcular los totales para la vista detalle
    private List<Map<String, Object>> calcularDetalles(List<BancoRegistro> registros) {
        List<Map<String, Object>> detalles = new ArrayList<>();

        // Calcular los totales
        double totalCargos = 0;
        double totalAbonos = 0;

        for (BancoRegistro registro : registros) {
            double monto = registro.getMonto().doubleValue();
            if (monto < 0) {
                totalCargos += Math.abs(monto);
            } else {
                totalAbonos += monto;
            }
        }

        // Agregar los detalles
        Map<String, Object> cargos = new HashMap<>();
        cargos.put("concepto", "CARGOS");
        cargos.put("monto", totalCargos);
        detalles.add(cargos);

        Map<String, Object> abonos = new HashMap<>();
        abonos.put("concepto", "ABONOS");
        abonos.put("monto", totalAbonos);
        detalles.add(abonos);

        return detalles;
    }

    @GetMapping("/exportar")
    public ResponseEntity<?> exportarExcel(
            @RequestParam("cuenta") String cuenta,
            @RequestParam("periodo") String periodo) {

        try {
            System.out.println("Iniciando exportación a Excel - Cuenta: " + cuenta + ", Periodo: " + periodo);

            // Formato del periodo a MMYYYY si viene como MM/YYYY
            if (periodo.contains("/")) {
                String[] parts = periodo.split("/");
                periodo = parts[0] + parts[1];
            }

            byte[] excelBytes = conciliacionService.exportarConciliacionExcel(cuenta, periodo);

            // Validar que el resultado no sea nulo
            if (excelBytes == null || excelBytes.length == 0) {
                System.err.println("Error: Se generó un documento Excel vacío");
                return ResponseEntity
                        .status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body("Se generó un documento Excel vacío. Por favor, verifique los parámetros e intente de nuevo.");
            }

            // Nombre del archivo
            LocalDateTime now = LocalDateTime.now();
            String fileName = "Conciliacion_" +
                    cuenta.replaceAll("[^a-zA-Z0-9]", "-") + "_Periodo_" +
                    periodo + "-" +
                    now.format(DateTimeFormatter.ofPattern("ddMMyyyy-HHmm")) + ".xlsx";

            System.out.println(
                    "Excel generado correctamente. Tamaño: " + excelBytes.length + " bytes, Nombre: " + fileName);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(
                    MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
            headers.add("Content-Disposition", "attachment; filename=" + fileName);
            headers.add("Cache-Control", "no-cache, no-store, must-revalidate");
            headers.add("Pragma", "no-cache");
            headers.add("Expires", "0");
            headers.setContentLength(excelBytes.length);

            return ResponseEntity
                    .ok()
                    .headers(headers)
                    .body(excelBytes);
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Error al exportar Excel: " + e.getMessage());
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .contentType(MediaType.TEXT_PLAIN)
                    .body("Error al generar el archivo Excel: " + e.getMessage());
        }
    }

    @PostMapping("/procesar-automatico")
    @ResponseBody
    public Map<String, Object> procesarAutomatico(@RequestBody Map<String, String> params) {
        Map<String, Object> response = new HashMap<>();

        try {
            String cuentaBanco = params.get("cuentaBanco");
            String periodo = params.get("periodo");

            System.out.println("Parámetros recibidos: cuentaBanco=" + cuentaBanco + ", periodo=" + periodo);

            // Validar que los parámetros no sean nulos
            if (cuentaBanco == null || periodo == null) {
                response.put("success", false);
                response.put("message", "Parámetros incompletos");
                return response;
            }

            // Eliminar las comillas simples si ya están incluidas
            if (cuentaBanco.startsWith("'") && cuentaBanco.endsWith("'")) {
                cuentaBanco = cuentaBanco.substring(1, cuentaBanco.length() - 1);
            }
            if (periodo.startsWith("'") && periodo.endsWith("'")) {
                periodo = periodo.substring(1, periodo.length() - 1);
            }

            // Ejecutar la conciliación automática
            List<Map<String, Object>> resultados = conciliacionService.procesarConciliacionAutomatica(cuentaBanco,
                    periodo);

            // Contar registros por estado
            int totalRegistros = resultados.size();
            int conciliadosTotal = 0;
            int conciliadosParcial = 0;

            for (Map<String, Object> registro : resultados) {
                Double monto = registro.get("Monto") != null ? Double.parseDouble(registro.get("Monto").toString()) : 0;
                Double montoPla = registro.get("montopla") != null
                        ? Double.parseDouble(registro.get("montopla").toString())
                        : null;

                if (montoPla != null) {
                    if (Math.abs(monto - montoPla) < 0.01) {
                        conciliadosTotal++;
                    } else {
                        conciliadosParcial++;
                    }
                }
            }

            response.put("success", true);
            response.put("message", "Conciliación procesada correctamente");
            response.put("totalRegistros", totalRegistros);
            response.put("conciliadosTotal", conciliadosTotal);
            response.put("conciliadosParcial", conciliadosParcial);
            response.put("noConciliados", totalRegistros - conciliadosTotal - conciliadosParcial);
        } catch (Exception e) {
            e.printStackTrace();
            response.put("success", false);
            response.put("message", e.getMessage());
        }

        return response;
    }
}