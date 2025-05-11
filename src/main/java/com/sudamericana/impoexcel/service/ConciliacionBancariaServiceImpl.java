package com.sudamericana.impoexcel.service;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.ByteArrayOutputStream;

import com.sudamericana.impoexcel.model.BancoCuenta;
import com.sudamericana.impoexcel.model.BancoEntidad;
import com.sudamericana.impoexcel.model.BancoEstado;
import com.sudamericana.impoexcel.model.BancoRegistro;
import com.sudamericana.impoexcel.model.ConciliacionDetalle;
import com.sudamericana.impoexcel.repository.ConciliacionBancariaRepository;

@Service
public class ConciliacionBancariaServiceImpl implements ConciliacionBancariaService {

    @Autowired
    private org.springframework.jdbc.core.JdbcTemplate jdbcTemplate;

    @Override
    public List<Map<String, Object>> procesarConciliacionAutomatica(String cuentaBanco, String periodo) {
        try {
            String sql = "EXEC Sp_estado_cuenta_bco_excel ?, ?";
            List<Map<String, Object>> resultados = jdbcTemplate.queryForList(sql, cuentaBanco, periodo);

            actualizarEstadosRegistros(cuentaBanco, periodo, resultados);

            List<Map<String, Object>> resultadosConciliados = conciliarConMst01bco(cuentaBanco, periodo, resultados);

            List<Map<String, Object>> resultadosFinales = aplicarAgrupaciones(resultadosConciliados);

            return resultadosFinales;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error al procesar conciliación: " + e.getMessage(), e);
        }
    }

    /**
     * Procesa los registros no conciliados comparándolos contra la tabla mst01bco
     * 
     * @param cuentaBanco Número de cuenta bancaria
     * @param periodo     Periodo en formato MMYYYY
     * @param resultados  Lista original de resultados de la conciliación
     * @return Lista actualizada de resultados
     */
    private List<Map<String, Object>> conciliarConMst01bco(String cuentaBanco, String periodo,
            List<Map<String, Object>> resultados) {
        try {
            System.out.println("Iniciando segundo paso de conciliación usando tabla mst01bco (solo memoria)...");

            // Determinar si es cuenta del Continental para aplicar lógica especial
            boolean esCuentaContinental = cuentaBanco.equals("7-0100012637-13")
                    || cuentaBanco.equals("7-0100012645-16");

            if (esCuentaContinental) {
                System.out.println("Cuenta Continental detectada: " + cuentaBanco
                        + ". Aplicando reglas específicas de conciliación.");
            }

            // Determinar el rango de fechas basado en el periodo (formato MMYYYY)
            String mes = periodo.substring(0, 2);
            String anio = periodo.substring(2);

            // Crear fechas de inicio y fin del periodo en formato para SQL Server
            String fechaInicioStr = anio + "-" + mes + "-01";
            String fechaFinStr = anio + "-" + mes + "-" +
                    java.time.YearMonth.of(Integer.parseInt(anio), Integer.parseInt(mes)).lengthOfMonth();

            System.out.println(
                    "Periodo: " + periodo + " - Filtrando registros desde " + fechaInicioStr + " hasta " + fechaFinStr);

            // Filtrar solo los registros no conciliados
            List<Map<String, Object>> noConciliados = resultados.stream()
                    .filter(r -> r.get("montopla") == null)
                    .collect(java.util.stream.Collectors.toList());

            System.out.println("Registros no conciliados para procesar: " + noConciliados.size());
            int conciliadosEnSegundoPaso = 0;

            for (Map<String, Object> registro : noConciliados) {
                // Extraer valores del registro bancario para comparación
                Object montoObj = registro.get("Monto");
                Object nroOpObj = registro.get("NroOperación");

                if (montoObj == null) {
                    continue;
                }

                double monto = Double.parseDouble(montoObj.toString());
                double montoAbs = Math.abs(monto);
                String nroOp = nroOpObj != null ? nroOpObj.toString().trim() : "";

                // Para el Continental, muchas veces la operación está en la descripción
                if (esCuentaContinental && (nroOp == null || nroOp.isEmpty() || nroOp.startsWith("AUTO-"))) {
                    Object descripcionObj = registro.get("Descripción");
                    if (descripcionObj != null) {
                        String descripcion = descripcionObj.toString().trim();
                        // Intentar extraer números de la descripción para Continental
                        String numerosEnDescripcion = descripcion.replaceAll("[^0-9]", "");
                        if (!numerosEnDescripcion.isEmpty()) {
                            nroOp = numerosEnDescripcion;
                            System.out
                                    .println("Continental: Extrayendo número de operación de la descripción: " + nroOp);
                        }
                    }
                }

                // Si en el banco es negativo, buscar en "haber"; si en el banco es positivo,
                // buscar en "debe"
                String campoMonto = monto < 0 ? "haber" : "debe";

                // Para Continental, podríamos necesitar ajustar el margen de tolerancia
                double tolerancia = esCuentaContinental ? 0.05 : 0.01;

                System.out.println("Buscando coincidencias para Monto: " + monto +
                        " (montoAbs: " + montoAbs +
                        ", buscando en campo: " + campoMonto +
                        ", tolerancia: " + tolerancia + ")");

                StringBuilder sqlBuilder = new StringBuilder();
                sqlBuilder.append("SELECT TOP 10 nrocta, fecha, ndocu, debe, haber, glosa ")
                        .append("FROM BdNava01.dbo.mst01bco ")
                        .append("WHERE ").append(campoMonto)
                        .append(" BETWEEN ? - " + tolerancia + " AND ? + " + tolerancia + " ")
                        .append("AND CONVERT(VARCHAR(10), fecha, 120) BETWEEN ? AND ? ");

                // Si tiene número de operación, verificar coincidencia
                if (nroOp != null && !nroOp.isEmpty() && !nroOp.startsWith("AUTO-")) {
                    // Para Continental, buscar coincidencia en glosa también
                    if (esCuentaContinental) {
                        sqlBuilder.append("AND (ndocu LIKE ? OR glosa LIKE ?) ");
                    } else {
                        sqlBuilder.append("AND ndocu LIKE ? ");
                    }
                }

                sqlBuilder.append("ORDER BY fecha DESC");

                String sqlConsulta = sqlBuilder.toString();
                System.out.println("SQL: " + sqlConsulta);

                List<Map<String, Object>> coincidencias;

                try {
                    // Ejecutar la consulta con los parámetros adecuados
                    if (nroOp != null && !nroOp.isEmpty() && !nroOp.startsWith("AUTO-")) {
                        String nroOpPattern = "%" + nroOp + "%";
                        if (esCuentaContinental) {
                            coincidencias = jdbcTemplate.queryForList(
                                    sqlConsulta, montoAbs, montoAbs, fechaInicioStr, fechaFinStr, nroOpPattern,
                                    nroOpPattern);
                        } else {
                            coincidencias = jdbcTemplate.queryForList(
                                    sqlConsulta, montoAbs, montoAbs, fechaInicioStr, fechaFinStr, nroOpPattern);
                        }
                    } else {
                        coincidencias = jdbcTemplate.queryForList(
                                sqlConsulta, montoAbs, montoAbs, fechaInicioStr, fechaFinStr);
                    }

                    System.out.println("Coincidencias encontradas: " + coincidencias.size());

                    // Procesamiento de coincidencias
                    if (!coincidencias.isEmpty()) {
                        // Ordenar las coincidencias por prioridad: coincidencia de ndocu exacta primero
                        Map<String, Object> mejorCoincidencia = null;

                        if (nroOp != null && !nroOp.isEmpty() && !nroOp.startsWith("AUTO-")) {
                            // Buscar coincidencia exacta primero
                            for (Map<String, Object> coincidencia : coincidencias) {
                                String ndocu = coincidencia.get("ndocu") != null
                                        ? coincidencia.get("ndocu").toString().trim()
                                        : "";
                                if (ndocu.equals(nroOp)) {
                                    mejorCoincidencia = coincidencia;
                                    System.out.println(
                                            "Encontrada coincidencia exacta de NroOp: " + nroOp + " = " + ndocu);
                                    break;
                                }
                            }

                            // Si no hay coincidencia exacta, buscar la más similar
                            if (mejorCoincidencia == null) {
                                for (Map<String, Object> coincidencia : coincidencias) {
                                    String ndocu = coincidencia.get("ndocu") != null
                                            ? coincidencia.get("ndocu").toString().trim()
                                            : "";
                                    if (ndocu.contains(nroOp) || nroOp.contains(ndocu)) {
                                        mejorCoincidencia = coincidencia;
                                        System.out.println("Encontrada coincidencia parcial. NroOp banco: " + nroOp
                                                + ", NDoc mst01bco: " + ndocu);
                                        break;
                                    }
                                }
                            }
                        }

                        // Si no encontramos coincidencia por número, usar la primera por monto
                        if (mejorCoincidencia == null) {
                            mejorCoincidencia = coincidencias.get(0);
                            System.out.println("Usando coincidencia por monto: " + monto);
                        }

                        Map<String, Object> coincidencia = mejorCoincidencia;

                        // Mostrar datos de la coincidencia para depuración
                        System.out.println("Datos de coincidencia encontrada: " +
                                " ndocu=" + coincidencia.get("ndocu") +
                                " debe=" + coincidencia.get("debe") +
                                " haber=" + coincidencia.get("haber") +
                                " fecha=" + coincidencia.get("fecha"));

                        for (Map<String, Object> original : resultados) {
                            if (original == registro) {
                                double montoPla = 0.0;
                                if (coincidencia.get("debe") != null
                                        && ((Number) coincidencia.get("debe")).doubleValue() > 0) {
                                    montoPla = -1 * ((Number) coincidencia.get("debe")).doubleValue();
                                } else if (coincidencia.get("haber") != null
                                        && ((Number) coincidencia.get("haber")).doubleValue() > 0) {
                                    montoPla = ((Number) coincidencia.get("haber")).doubleValue();
                                }

                                // Copiar datos de la coincidencia al registro original
                                original.put("montopla", montoPla);
                                original.put("Fechapla", coincidencia.get("fecha"));
                                original.put("nplan", coincidencia.get("ndocu"));
                                original.put("ndocu", coincidencia.get("ndocu"));
                                original.put("glosa", coincidencia.get("glosa"));
                                original.put("BDorigin", "mst01bco");

                                conciliadosEnSegundoPaso++;
                                System.out.println("Conciliado: Monto=" + monto + ", MontoPla=" + montoPla +
                                        ", NDoc=" + coincidencia.get("ndocu"));
                                break;
                            }
                        }
                    }
                } catch (Exception e) {
                    System.err.println(
                            "Error en consulta SQL para registro [" + nroOp + ", " + monto + "]: " + e.getMessage());
                }
            }

            try {
                System.out.println("Buscando transacciones sobrantes en MST01BCO...");

                // Recopilar todos los números de operación ya conciliados
                Set<String> operacionesConciliadas = new HashSet<>();
                Set<String> operacionesNormalizadas = new HashSet<>();

                for (Map<String, Object> registro : resultados) {
                    if (registro.get("NroOperación") != null) {
                        String nroOp = registro.get("NroOperación").toString().trim();
                        operacionesConciliadas.add(nroOp);

                        // Guardar versión normalizada (solo dígitos)
                        String nroOpNormalizado = nroOp.replaceAll("[^0-9]", "");
                        if (!nroOpNormalizado.isEmpty()) {
                            operacionesNormalizadas.add(nroOpNormalizado);
                            System.out.println(
                                    "Agregado número normalizado: " + nroOpNormalizado + " (original: " + nroOp + ")");
                        }

                        if (nroOp.contains("-")) {
                            // Extraemos la parte final después del último guión
                            String parteFinal = nroOp.substring(nroOp.lastIndexOf("-") + 1).trim();
                            if (!parteFinal.isEmpty()) {
                                operacionesNormalizadas.add(parteFinal);
                                System.out
                                        .println("Agregada parte final: " + parteFinal + " (original: " + nroOp + ")");
                            }

                            // Agregamos también todas las partes separadas por guiones
                            String[] partes = nroOp.split("-");
                            for (String parte : partes) {
                                if (!parte.trim().isEmpty()) {
                                    operacionesNormalizadas.add(parte.trim());
                                    System.out.println("Agregada parte individual: " + parte.trim() + " (original: "
                                            + nroOp + ")");
                                }
                            }
                        }
                    }
                }

                String cuentaNormalizada = cuentaBanco.replaceAll("[^0-9]", "");

                String sqlMst01bco = "SELECT ndocu, fecha, debe, haber, glosa " +
                        "FROM BdNava01.dbo.mst01bco " +
                        "WHERE REPLACE(REPLACE(REPLACE(nrocta, '-', ''), ' ', ''), '.', '') = ? " +
                        "AND CONVERT(VARCHAR(10), fecha, 120) BETWEEN ? AND ? " +
                        "ORDER BY fecha";

                List<Map<String, Object>> operacionesMst01bco = jdbcTemplate.queryForList(
                        sqlMst01bco, cuentaNormalizada, fechaInicioStr, fechaFinStr);
                System.out.println("Filtrando por cuenta bancaria: " + cuentaBanco);
                System.out.println("Rango de fechas: " + fechaInicioStr + " a " + fechaFinStr);

                System.out.println("Operaciones encontradas en MST01BCO: " + operacionesMst01bco.size());

                int sobrantesEncontrados = 0;

                // Identificar operaciones sobrantes en MST01BCO
                for (Map<String, Object> operacionMst01bco : operacionesMst01bco) {
                    String ndocu = operacionMst01bco.get("ndocu") != null
                            ? operacionMst01bco.get("ndocu").toString().trim()
                            : "";

                    if (ndocu.isEmpty()) {
                        continue; // Ignorar registros sin número de documento
                    }

                    // Verificar si esta operación ya está conciliada: coincidencia exacta
                    if (operacionesConciliadas.contains(ndocu)) {
                        System.out.println("Coincidencia exacta encontrada para: " + ndocu);
                        continue;
                    }

                    // Verificar coincidencia parcial: normalizar el número de documento
                    String ndocuNormalizadoOriginal = ndocu.replaceAll("[^0-9]", "");

                    // Verificar si el número normalizado ya existe en operaciones conciliadas
                    boolean yaEstaConciliada = false;

                    // Verificación 1: Normalización completa (solo dígitos)
                    if (!ndocuNormalizadoOriginal.isEmpty()
                            && operacionesNormalizadas.contains(ndocuNormalizadoOriginal)) {
                        System.out.println("Coincidencia por normalización completa: " + ndocu +
                                " (normalizado: " + ndocuNormalizadoOriginal + ")");
                        yaEstaConciliada = true;
                    }

                    // Verificación 2: Parte final después del último guión
                    if (!yaEstaConciliada && ndocu.contains("-")) {
                        String parteFinal = ndocu.substring(ndocu.lastIndexOf("-") + 1).trim();
                        if (!parteFinal.isEmpty() && operacionesNormalizadas.contains(parteFinal)) {
                            System.out.println("Coincidencia por parte final: " + ndocu +
                                    " (parte final: " + parteFinal + ")");
                            yaEstaConciliada = true;
                        }
                    }

                    // Verificación 3: Cualquier parte separada por guiones
                    if (!yaEstaConciliada && ndocu.contains("-")) {
                        String[] partes = ndocu.split("-");
                        for (String parte : partes) {
                            parte = parte.trim();
                            if (!parte.isEmpty() && operacionesNormalizadas.contains(parte)) {
                                System.out.println("Coincidencia por parte individual: " + ndocu +
                                        " (parte: " + parte + ")");
                                yaEstaConciliada = true;
                                break;
                            }
                        }
                    }

                    // Verificación 4: El documento completo es parte de alguna operación conciliada
                    if (!yaEstaConciliada) {
                        for (String opConciliada : operacionesConciliadas) {
                            if (opConciliada.contains(ndocu)) {
                                System.out.println("Coincidencia por inclusión: " + ndocu +
                                        " está contenido en " + opConciliada);
                                yaEstaConciliada = true;
                                break;
                            }
                        }
                    }

                    if (!yaEstaConciliada) {
                        String ndocuSinPrefijo = ndocu;
                        if (ndocu.contains("/")) {
                            ndocuSinPrefijo = ndocu.substring(ndocu.indexOf("/") + 1).trim();
                        }

                        for (Map<String, Object> registroConciliado : resultados) {
                            // Verificar si el registro ya tiene montopla
                            if (registroConciliado.get("montopla") != null) {
                                // Verificar en NroOperación
                                String nroOpConciliado = registroConciliado.get("NroOperación") != null
                                        ? registroConciliado.get("NroOperación").toString()
                                        : "";

                                // Verificar en Descripción
                                String descripcionConciliada = registroConciliado.get("Descripción") != null
                                        ? registroConciliado.get("Descripción").toString()
                                        : "";

                                String nroOpNormalizado = nroOpConciliado.replaceAll("[^0-9]", "");
                                String ndocuNormalizado2 = ndocu.replaceAll("[^0-9]", "");
                                String ndocuSinPrefijoNormalizado = ndocuSinPrefijo.replaceAll("[^0-9]", "");

                                // También verificar en el nplan y ndocu si existen
                                String nplanConciliado = registroConciliado.get("nplan") != null
                                        ? registroConciliado.get("nplan").toString()
                                        : "";
                                String ndocuConciliado = registroConciliado.get("ndocu") != null
                                        ? registroConciliado.get("ndocu").toString()
                                        : "";

                                // Verificar si el número está contenido en alguna parte
                                if ((!ndocuNormalizadoOriginal.isEmpty() &&
                                        (nroOpConciliado.contains(ndocu) ||
                                                descripcionConciliada.contains(ndocu) ||
                                                nplanConciliado.contains(ndocu) ||
                                                ndocuConciliado.contains(ndocu) ||
                                                nroOpNormalizado.equals(ndocuNormalizadoOriginal)))
                                        ||
                                        (!ndocuSinPrefijoNormalizado.isEmpty() &&
                                                (nroOpConciliado.contains(ndocuSinPrefijo) ||
                                                        descripcionConciliada.contains(ndocuSinPrefijo) ||
                                                        nplanConciliado.contains(ndocuSinPrefijo) ||
                                                        ndocuConciliado.contains(ndocuSinPrefijo) ||
                                                        nroOpNormalizado.equals(ndocuSinPrefijoNormalizado)))) {

                                    System.out.println("Coincidencia encontrada para ndocu: " + ndocu +
                                            " en registro: NroOp=" + nroOpConciliado +
                                            ", Desc=" + descripcionConciliada);
                                    yaEstaConciliada = true;
                                    break;
                                }
                            }
                        }
                    }
                    // Si no está conciliada por ningún método, agregarla como sobrante
                    if (!yaEstaConciliada) {

                        Map<String, Object> registroSobrante = new HashMap<>();

                        // Determinar el monto según si es debe o haber
                        double monto = 0.0;
                        String tipoOperacion = "";
                        if (operacionMst01bco.get("debe") != null &&
                                ((Number) operacionMst01bco.get("debe")).doubleValue() > 0) {
                            monto = ((Number) operacionMst01bco.get("debe")).doubleValue();
                            tipoOperacion = "CARGO";
                        } else if (operacionMst01bco.get("haber") != null &&
                                ((Number) operacionMst01bco.get("haber")).doubleValue() > 0) {
                            monto = -1 * ((Number) operacionMst01bco.get("haber")).doubleValue();
                            tipoOperacion = "ABONO";
                        }

                        String fechaFormateada = "";
                        if (operacionMst01bco.get("fecha") != null) {
                            java.sql.Timestamp timestamp = (java.sql.Timestamp) operacionMst01bco.get("fecha");
                            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd/MM/yyyy");
                            fechaFormateada = sdf.format(timestamp);
                        }

                        String glosa = operacionMst01bco.get("glosa") != null
                                ? operacionMst01bco.get("glosa").toString().trim()
                                : "";

                        String descripcionDetallada = String.format("[SOBRANTE NavaSoft] %s - %s %s - %s %.2f - %s",
                                ndocu,
                                fechaFormateada,
                                tipoOperacion,
                                tipoOperacion.equals("CARGO") ? "CARGO" : "ABONO",
                                Math.abs(monto),
                                glosa);
                        registroSobrante.put("NroOperación", ndocu);
                        registroSobrante.put("Fecha", operacionMst01bco.get("fecha"));
                        registroSobrante.put("Monto", monto);
                        registroSobrante.put("Descripción", descripcionDetallada);
                        registroSobrante.put("BDorigin", "sobrante_mst01bco");

                        registroSobrante.put("GlosaMST01BCO", glosa);
                        registroSobrante.put("FechaStr", fechaFormateada);
                        registroSobrante.put("TipoOperacion", tipoOperacion);
                        registroSobrante.put("MontoOriginal", Math.abs(monto));

                        System.out.println("Agregando registro sobrante: " + descripcionDetallada);

                        // Agregar a la lista de resultados
                        resultados.add(registroSobrante);
                        sobrantesEncontrados++;
                    } else {
                        System.out.println("Se excluyó como sobrante: " + ndocu + " porque ya está conciliado");
                    }
                }

                System.out.println("Se encontraron " + sobrantesEncontrados + " transacciones sobrantes en MST01BCO");

            } catch (Exception e) {
                System.err.println("Error al buscar transacciones sobrantes: " + e.getMessage());
                e.printStackTrace();
            }

            return resultados;

        } catch (Exception e) {
            System.err.println("Error en segundo paso de conciliación: " + e.getMessage());
            e.printStackTrace();
            return resultados;
        }
    }

    /**
     * Método para agrupar operaciones específicas como ITF y comisiones bancarias
     * 
     * @param resultados Lista de resultados de conciliación
     * @return Lista de resultados con agrupaciones aplicadas
     */
    private List<Map<String, Object>> aplicarAgrupaciones(List<Map<String, Object>> resultados) {
        try {
            System.out.println("Aplicando agrupaciones especiales...");

            List<Map<String, Object>> resultadoFinal = new ArrayList<>();

            Map<String, Object> acumuladoITF = null;
            Map<String, Object> acumuladoComisiones = null;

            List<Map<String, Object>> registrosITF = new ArrayList<>();
            List<Map<String, Object>> registrosComisiones = new ArrayList<>();

            for (Map<String, Object> registro : resultados) {
                if (registro.get("Descripción") == null) {
                    resultadoFinal.add(registro);
                    continue;
                }

                String descripcion = registro.get("Descripción").toString().toUpperCase().trim();

                if (descripcion.contains("[SOBRANTE NAVASOFT]") ||
                        descripcion.contains("[SOBRANTE MST01BCO]") ||
                        descripcion.contains("[SOBRANTE NavaSoft]") ||
                        descripcion.startsWith("AGRUPADO-") ||
                        descripcion.startsWith("AGRUPADO-COM-")) {
                    resultadoFinal.add(registro);
                    System.out.println("Registro excluido de agrupación: " + descripcion);
                    continue;
                }

                // Agrupar ITF y IT (Continental)
                if (descripcion.contains("ITF") ||
                        descripcion.contains("IMPUESTO A LA TRANSACCION") ||
                        descripcion.contains("IMPUESTO ITF") ||
                        descripcion.equals("IT") ||
                        descripcion.contains(" IT ")) {

                    registrosITF.add(registro);

                    if (acumuladoITF == null) {
                        acumuladoITF = new HashMap<>(registro);
                        acumuladoITF.put("Descripción", "ITF / IMPUESTO A LAS TRANSACCIONES");
                    } else {
                        double montoActual = Double.parseDouble(registro.get("Monto").toString());
                        double montoAcumulado = Double.parseDouble(acumuladoITF.get("Monto").toString());
                        acumuladoITF.put("Monto", montoActual + montoAcumulado);
                    }
                } else if (descripcion.contains("COM.OP.OTRA.LOCAL") ||
                        descripcion.contains("COM.OPE.VENT") ||
                        descripcion.contains("COM.MANTENIM") ||
                        descripcion.contains("ENVIO.EST.CTA") ||
                        descripcion.contains("COMISION") ||
                        descripcion.contains("ENVIO DE EXTRACTO DE MOVIMI") ||
                        descripcion.contains("COMIS. EXCESO OPERAC. VEN") ||
                        descripcion.contains("COMIS.EMISION TRANSF.CCE") ||
                        descripcion.contains("COMIS. TRASPASO O/")) {

                    registrosComisiones.add(registro);

                    if (acumuladoComisiones == null) {
                        acumuladoComisiones = new HashMap<>(registro);
                        acumuladoComisiones.put("Descripción", "COMISIONES BANCARIAS");
                    } else {
                        double montoActual = Double.parseDouble(registro.get("Monto").toString());
                        double montoAcumulado = Double.parseDouble(acumuladoComisiones.get("Monto").toString());
                        acumuladoComisiones.put("Monto", montoActual + montoAcumulado);
                    }
                } else {
                    resultadoFinal.add(registro);
                }
            }

            if (acumuladoITF != null) {
                acumuladoITF.put("NroOperación", "AGRUPADO-ITF-" + System.currentTimeMillis());

                StringBuilder detalleITF = new StringBuilder("Registros agrupados: ");
                for (Map<String, Object> reg : registrosITF) {
                    detalleITF.append(reg.get("NroOperación")).append(", ");
                }
                acumuladoITF.put("DetalleAgrupacion", detalleITF.toString());

                resultadoFinal.add(acumuladoITF);
                System.out.println("Agrupados " + registrosITF.size() + " registros ITF. Monto total: "
                        + acumuladoITF.get("Monto"));
            }

            if (acumuladoComisiones != null) {
                acumuladoComisiones.put("NroOperación", "AGRUPADO-COM-" + System.currentTimeMillis());

                StringBuilder detalleComisiones = new StringBuilder("Registros agrupados: ");
                for (Map<String, Object> reg : registrosComisiones) {
                    detalleComisiones.append(reg.get("NroOperación")).append(", ");
                }
                acumuladoComisiones.put("DetalleAgrupacion", detalleComisiones.toString());

                resultadoFinal.add(acumuladoComisiones);
                System.out.println("Agrupados " + registrosComisiones.size() + " registros de comisiones. Monto total: "
                        + acumuladoComisiones.get("Monto"));
            }

            System.out.println("Agrupación completada. Registros originales: " + resultados.size()
                    + ", Registros finales: " + resultadoFinal.size());
            return resultadoFinal;
        } catch (Exception e) {
            System.err.println("Error al aplicar agrupaciones: " + e.getMessage());
            e.printStackTrace();
            return resultados;
        }
    }

    @Autowired
    private ConciliacionBancariaRepository repository;

    @Override
    public List<BancoEntidad> obtenerEntidades() {
        return repository.obtenerBancos();
    }

    @Override
    public List<BancoCuenta> obtenerCuentasPorEntidad(String entidad) {
        System.out.println("Servicio: Obteniendo cuentas para entidad: " + entidad);
        List<BancoCuenta> cuentas = repository.obtenerCuentasPorEntidad(entidad);
        System.out.println("Servicio: Cuentas encontradas: " + cuentas.size());
        return cuentas;
    }

    @Override
    public BancoEstado obtenerEstadoConciliacion(String aniomes, String cuenta) {
        return repository.obtenerEstadoConciliacion(aniomes, cuenta);
    }

    @Override
    public List<BancoRegistro> obtenerRegistrosBanco(String aniomes, String cuenta) {
        return repository.obtenerRegistrosBanco(aniomes, cuenta);
    }

    @Override
    public boolean procesarArchivoExcel(MultipartFile file, String cuenta, String aniomes) {
        try {
            System.out.println("Iniciando procesamiento de archivo Excel para cuenta: " + cuenta);
            System.out.println("Tamaño del archivo: " + file.getSize() + " bytes");

            // eliminar registros existentes para evitar duplicados
            try {
                String deleteSql = "DELETE FROM BDSU_BP.FINA.REGISTRO_BANCO WHERE CUENTA = ? AND ANIOMES = ?";
                jdbcTemplate.update(deleteSql, cuenta, aniomes);
                System.out.println("Registros previos eliminados para cuenta: " + cuenta + ", periodo: " + aniomes);
            } catch (Exception e) {
                System.err.println("Advertencia: No se pudieron eliminar registros previos: " + e.getMessage());
            }

            try {
                Workbook workbook = null;
                try {
                    workbook = WorkbookFactory.create(file.getInputStream());
                } catch (Exception e) {
                    System.err.println("Error al abrir el archivo Excel: " + e.getMessage());
                    throw new RuntimeException("El archivo no es un Excel válido o está dañado");
                }

                Sheet sheet = workbook.getSheetAt(0);
                System.out.println("Archivo cargado correctamente. Número de filas: " + sheet.getLastRowNum());

                int filasInsertadas = 0;
                int filasConError = 0;

                int startRow;

                if (cuenta.equals("7-0100012637-13") || cuenta.equals("7-0100012645-16")) {
                    startRow = 11;
                    System.out.println("Cuenta Continental detectada. Iniciando lectura desde fila 12.");
                } else {
                    startRow = findHeaderRow(sheet);
                    if (startRow < 0) {
                        System.err.println("No se encontró la fila de cabeceras en el Excel");
                        startRow = 5;
                    } else {
                        startRow++;
                    }
                }

                System.out.println("Comenzando a procesar datos desde la fila: " + (startRow + 1));

                List<BancoRegistro> registrosAInsertar = new ArrayList<>();

                // Primera pasada: extraer todos los registros
                for (int i = startRow; i <= sheet.getLastRowNum(); i++) {
                    Row row = sheet.getRow(i);
                    if (row == null || isEmptyRow(row))
                        continue;

                    try {
                        BancoRegistro registro = extraerRegistroDeFila(row, cuenta, aniomes);
                        if (registro != null) {
                            registro.setEstado("1");
                            registrosAInsertar.add(registro);
                        }
                    } catch (Exception e) {
                        filasConError++;
                        System.err.println("Error en fila " + (i + 1) + ": " + e.getMessage());
                    }
                }

                // Segunda pasada: insertar los registros en la base de datos
                if (!registrosAInsertar.isEmpty()) {
                    for (BancoRegistro registro : registrosAInsertar) {
                        try {
                            repository.insertarRegistroBanco(registro);
                            filasInsertadas++;
                        } catch (Exception e) {
                            filasConError++;
                            System.err.println("Error al insertar registro: " + e.getMessage());
                        }
                    }
                }

                workbook.close();
                System.out.println("Procesamiento finalizado. Filas insertadas: " + filasInsertadas
                        + ", Filas con error: " + filasConError);

                return filasInsertadas > 0;
            } catch (Exception e) {
                System.err.println("Error al procesar el archivo Excel: " + e.getMessage());
                e.printStackTrace();
                return false;
            }
        } catch (Exception e) {
            System.err.println("Error crítico al procesar el archivo: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Error al procesar el archivo Excel: " + e.getMessage(), e);
        }
    }

    // Método para verificar si una fila está vacía
    private boolean isEmptyRow(Row row) {
        if (row == null)
            return true;
        for (int i = 0; i < row.getLastCellNum(); i++) {
            Cell cell = row.getCell(i);
            if (cell != null && cell.getCellType() != CellType.BLANK) {
                return false;
            }
        }
        return true;
    }

    // Método para encontrar la fila que contiene las cabeceras
    private int findHeaderRow(Sheet sheet) {
        int exactHeaderRow = findHeaderRowExacto(sheet);
        if (exactHeaderRow >= 0) {
            System.out.println("Se encontró la fila de cabeceras usando el método exacto: " + (exactHeaderRow + 1));
            return exactHeaderRow;
        }

        String[] headerKeywords = {
                "fecha", "valuta", "descripción operación", "descripcion operacion", "monto",
                "saldo", "sucursal", "agencia", "operación", "operacion", "número",
                "numero", "hora", "usuario", "utc", "referencia"
        };

        for (int i = 0; i <= Math.min(30, sheet.getLastRowNum()); i++) {
            Row row = sheet.getRow(i);
            if (row == null)
                continue;

            int matchCount = 0;
            int totalCells = 0;

            for (int j = 0; j < row.getLastCellNum(); j++) {
                Cell cell = row.getCell(j);
                if (cell == null)
                    continue;

                totalCells++;
                String cellValue = getCellValueAsString(cell, 255).toLowerCase();

                for (String keyword : headerKeywords) {
                    if (cellValue.contains(keyword)) {
                        matchCount++;
                        break;
                    }
                }
            }

            if (matchCount >= 3 && (double) matchCount / totalCells >= 0.3) {
                System.out.println("Se encontró la fila de cabeceras en la posición: " + (i + 1) +
                        " (coincidencias: " + matchCount + "/" + totalCells + ")");
                return i;
            }

            if (totalCells >= 5) {
                boolean hasFechaValuta = false;
                boolean hasDescripcionOp = false;
                boolean hasOperacionNumero = false;

                for (int j = 0; j < row.getLastCellNum(); j++) {
                    Cell cell = row.getCell(j);
                    if (cell == null)
                        continue;

                    String cellValue = getCellValueAsString(cell, 255).toLowerCase();

                    if (cellValue.contains("fecha valuta")) {
                        hasFechaValuta = true;
                    } else if (cellValue.contains("descripción operación")
                            || cellValue.contains("descripcion operacion")) {
                        hasDescripcionOp = true;
                    } else if (cellValue.contains("operación - número") || cellValue.contains("operacion - numero")) {
                        hasOperacionNumero = true;
                    }
                }

                if ((hasFechaValuta && hasDescripcionOp) || (hasDescripcionOp && hasOperacionNumero)) {
                    System.out.println("Se encontró la fila de cabeceras exacta en la posición: " + (i + 1));
                    return i;
                }
            }
        }

        int maxColumns = 0;
        int bestRow = -1;

        for (int i = 0; i <= Math.min(30, sheet.getLastRowNum()); i++) {
            Row row = sheet.getRow(i);
            if (row == null)
                continue;

            int textColumns = 0;
            for (int j = 0; j < row.getLastCellNum(); j++) {
                Cell cell = row.getCell(j);
                if (cell != null && cell.getCellType() == CellType.STRING &&
                        !cell.getStringCellValue().trim().isEmpty()) {
                    textColumns++;
                }
            }

            if (textColumns >= 5 && textColumns > maxColumns) {
                maxColumns = textColumns;
                bestRow = i;
            }
        }

        if (bestRow >= 0) {
            System.out.println("Se encontró una posible fila de cabeceras por número de columnas: " + (bestRow + 1));
            return bestRow;
        }

        return -1;
    }

    private BancoRegistro extraerRegistroDeFila(Row row, String cuenta, String aniomes) {
        if (isEmptyRow(row)) {
            return null;
        }

        if (cuenta.equals("7-0100012637-13") || cuenta.equals("7-0100012645-16")) {
            return extraerRegistroContinental(row, cuenta, aniomes);
        }

        BancoRegistro registro = new BancoRegistro();
        registro.setCuenta(cuenta);
        registro.setAniomes(aniomes);

        try {
            Map<String, Integer> columnMap = getColumnMap(row.getSheet());

            if (columnMap != null && !columnMap.isEmpty()) {
                System.out.println("Usando mapa de columnas: " + columnMap);

                if (columnMap.containsKey("fecha")) {
                    Cell fechaCell = row.getCell(columnMap.get("fecha"));
                    if (fechaCell != null) {
                        try {
                            if (fechaCell.getCellType() == CellType.NUMERIC
                                    && DateUtil.isCellDateFormatted(fechaCell)) {
                                registro.setFecha(LocalDate.ofInstant(
                                        fechaCell.getDateCellValue().toInstant(),
                                        java.time.ZoneId.systemDefault()));
                            } else {
                                registro.setFecha(parseDate(getCellValueAsString(fechaCell, 20).trim()));
                            }
                        } catch (Exception e) {
                            registro.setFecha(LocalDate.now());
                            System.err.println("Error al procesar fecha, usando actual: " + e.getMessage());
                        }
                    } else {
                        registro.setFecha(LocalDate.now());
                    }
                } else {
                    registro.setFecha(LocalDate.now());
                }

                if (columnMap.containsKey("descripcion") || columnMap.containsKey("descripción operación")) {
                    int descIdx = columnMap.containsKey("descripcion") ? columnMap.get("descripcion")
                            : columnMap.get("descripción operación");
                    Cell descCell = row.getCell(descIdx);
                    if (descCell != null) {
                        registro.setDescripcion(getCellValueAsString(descCell, 255));
                    } else {
                        registro.setDescripcion("Sin descripción");
                    }
                } else {
                    registro.setDescripcion("Sin descripción");
                }

                if (columnMap.containsKey("monto")) {
                    Cell montoCell = row.getCell(columnMap.get("monto"));
                    if (montoCell != null) {
                        registro.setMonto(extractAmount(montoCell));
                    } else {
                        registro.setMonto(BigDecimal.ZERO);
                    }
                } else {
                    registro.setMonto(BigDecimal.ZERO);
                }

                if (columnMap.containsKey("saldo")) {
                    Cell saldoCell = row.getCell(columnMap.get("saldo"));
                    if (saldoCell != null) {
                        registro.setSaldo(extractAmount(saldoCell));
                    } else {
                        registro.setSaldo(BigDecimal.ZERO);
                    }
                } else {
                    registro.setSaldo(BigDecimal.ZERO);
                }

                if (columnMap.containsKey("operación - número") || columnMap.containsKey("operacion - numero")
                        || columnMap.containsKey("operacion_nro")) {
                    int opIdx = -1;
                    if (columnMap.containsKey("operación - número"))
                        opIdx = columnMap.get("operación - número");
                    else if (columnMap.containsKey("operacion - numero"))
                        opIdx = columnMap.get("operacion - numero");
                    else
                        opIdx = columnMap.get("operacion_nro");

                    Cell opCell = row.getCell(opIdx);
                    if (opCell != null) {
                        registro.setOperacionNro(getCellValueAsString(opCell, 50));
                    } else {
                        registro.setOperacionNro("AUTO-" + System.currentTimeMillis() + "-" + row.getRowNum());
                    }
                } else {
                    registro.setOperacionNro("AUTO-" + System.currentTimeMillis() + "-" + row.getRowNum());
                }

                if (columnMap.containsKey("operación - hora") || columnMap.containsKey("operacion - hora")) {
                    int horaIdx = columnMap.containsKey("operación - hora") ? columnMap.get("operación - hora")
                            : columnMap.get("operacion - hora");
                    Cell horaCell = row.getCell(horaIdx);
                    if (horaCell != null) {
                        registro.setOperacionHora(getCellValueAsString(horaCell, 10));
                    } else {
                        registro.setOperacionHora("");
                    }
                } else {
                    registro.setOperacionHora("");
                }

                if (columnMap.containsKey("sucursal - agencia") || columnMap.containsKey("sucursal")) {
                    int sucIdx = columnMap.containsKey("sucursal - agencia") ? columnMap.get("sucursal - agencia")
                            : columnMap.get("sucursal");
                    Cell sucCell = row.getCell(sucIdx);
                    if (sucCell != null) {
                        registro.setSucursal(getCellValueAsString(sucCell, 50));
                    } else {
                        registro.setSucursal("");
                    }
                } else {
                    registro.setSucursal("");
                }

                if (columnMap.containsKey("referencia") || columnMap.containsKey("referencia2")) {
                    int refIdx = columnMap.containsKey("referencia") ? columnMap.get("referencia")
                            : columnMap.get("referencia2");
                    Cell refCell = row.getCell(refIdx);
                    if (refCell != null) {
                        registro.setReferencia(getCellValueAsString(refCell, 100));
                    } else {
                        registro.setReferencia("");
                    }
                } else {
                    registro.setReferencia("");
                }

            } else {
                System.out.println("No se encontró mapa de columnas. Usando búsqueda heurística.");

                Cell fechaCell = null;
                for (int i = 0; i <= 2; i++) {
                    Cell cell = row.getCell(i);
                    if (cell != null) {
                        if (cell.getCellType() == CellType.NUMERIC && DateUtil.isCellDateFormatted(cell)) {
                            fechaCell = cell;
                            break;
                        } else if (cell.getCellType() == CellType.STRING && isDateString(cell.getStringCellValue())) {
                            fechaCell = cell;
                            break;
                        }
                    }
                }

                if (fechaCell == null) {
                    for (int i = 0; i < row.getLastCellNum(); i++) {
                        Cell cell = row.getCell(i);
                        if (cell != null) {
                            if (cell.getCellType() == CellType.NUMERIC && DateUtil.isCellDateFormatted(cell)) {
                                fechaCell = cell;
                                break;
                            }

                            else if (cell.getCellType() == CellType.STRING && isDateString(cell.getStringCellValue())) {
                                fechaCell = cell;
                                break;
                            }
                        }
                    }
                }

                if (fechaCell == null) {
                    for (int i = 0; i < row.getLastCellNum(); i++) {
                        Cell cell = row.getCell(i);
                        if (cell != null && cell.getCellType() != CellType.BLANK) {
                            fechaCell = cell;
                            break;
                        }
                    }
                }

                if (fechaCell == null) {
                    registro.setFecha(LocalDate.now());
                } else {

                    try {
                        if (fechaCell.getCellType() == CellType.NUMERIC && DateUtil.isCellDateFormatted(fechaCell)) {
                            registro.setFecha(LocalDate.ofInstant(
                                    fechaCell.getDateCellValue().toInstant(),
                                    java.time.ZoneId.systemDefault()));
                        } else {

                            registro.setFecha(parseDate(getCellValueAsString(fechaCell, 20).trim()));
                        }
                    } catch (Exception e) {

                        registro.setFecha(LocalDate.now());
                        System.err.println("Error al procesar fecha, usando actual: " + e.getMessage());
                    }
                }

                Cell descCell = findDescriptionCell(row);
                if (descCell != null) {
                    registro.setDescripcion(getCellValueAsString(descCell, 255));
                } else {
                    Cell fallbackCell = row.getCell(1);
                    if (fallbackCell != null) {
                        registro.setDescripcion(getCellValueAsString(fallbackCell, 255));
                    } else {
                        registro.setDescripcion("Sin descripción");
                    }
                }

                Cell montoCell = findAmountCell(row);
                if (montoCell != null) {
                    BigDecimal monto = extractAmount(montoCell);
                    registro.setMonto(monto);
                } else {
                    registro.setMonto(BigDecimal.ZERO);
                    System.err.println(
                            "Advertencia: No se encontró columna de monto en la fila " + (row.getRowNum() + 1));
                }

                Cell operacionCell = findOperationNumberCell(row);
                if (operacionCell != null) {
                    registro.setOperacionNro(getCellValueAsString(operacionCell, 50));
                } else {
                    registro.setOperacionNro(
                            "AUTO-" + System.currentTimeMillis() + "-" + registro.getFecha() + "-" + row.getRowNum());
                }

                Cell saldoCell = findBalanceCell(row);
                if (saldoCell != null) {
                    try {
                        registro.setSaldo(extractAmount(saldoCell));
                    } catch (Exception e) {
                        registro.setSaldo(BigDecimal.ZERO);
                    }
                } else {
                    registro.setSaldo(BigDecimal.ZERO);
                }

                for (int i = 0; i < row.getLastCellNum(); i++) {
                    Cell cell = row.getCell(i);
                    if (cell == null)
                        continue;

                    String value = getCellValueAsString(cell, 100).trim();
                    if (value.isEmpty())
                        continue;

                    if (value.matches("\\d{1,2}:\\d{2}(:\\d{2})?")) {
                        registro.setOperacionHora(value);
                    } else if (value.matches(".*[Rr]ef.*") || value.length() > 5 && value.matches(".*\\d+.*")) {
                        registro.setReferencia(value);
                    }
                }
            }

            if (registro.getOperacionHora() == null)
                registro.setOperacionHora("");
            if (registro.getReferencia() == null)
                registro.setReferencia("");
            if (registro.getSucursal() == null)
                registro.setSucursal("");

            return registro;
        } catch (Exception e) {
            System.err.println("Error procesando fila " + (row.getRowNum() + 1) + ": " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    private BancoRegistro extraerRegistroContinental(Row row, String cuenta, String aniomes) {
        BancoRegistro registro = new BancoRegistro();
        registro.setCuenta(cuenta);
        registro.setAniomes(aniomes);

        try {

            // F. Operación -> FECHA (columna 0)
            Cell fechaOperacionCell = row.getCell(0);
            if (fechaOperacionCell != null) {
                try {
                    if (fechaOperacionCell.getCellType() == CellType.NUMERIC
                            && DateUtil.isCellDateFormatted(fechaOperacionCell)) {
                        registro.setFecha(LocalDate.ofInstant(
                                fechaOperacionCell.getDateCellValue().toInstant(),
                                java.time.ZoneId.systemDefault()));
                    } else {
                        registro.setFecha(parseDate(getCellValueAsString(fechaOperacionCell, 20).trim()));
                    }
                } catch (Exception e) {
                    registro.setFecha(LocalDate.now());
                    System.err.println("Error al procesar fecha de operación Continental: " + e.getMessage());
                }
            } else {
                registro.setFecha(LocalDate.now());
            }

            // Nº. Doc. -> OPERACION_NRO (columna 3)
            Cell nroDocCell = row.getCell(3);
            if (nroDocCell != null) {
                String operacionNro = getCellValueAsString(nroDocCell, 50);
                if (operacionNro.length() > 0) {
                    operacionNro += "4";
                }
                registro.setOperacionNro(operacionNro);
            } else {
                registro.setOperacionNro("AUTO-CONT-" + System.currentTimeMillis() + "-" + row.getRowNum());
            }

            // Concepto -> DESCRIPCION (columna 4)
            Cell conceptoCell = row.getCell(4);
            if (conceptoCell != null) {
                registro.setDescripcion(getCellValueAsString(conceptoCell, 255));
            } else {
                registro.setDescripcion("Sin descripción");
            }

            // Importe -> MONTO (columna 5)
            Cell importeCell = row.getCell(5);
            if (importeCell != null) {
                registro.setMonto(extractAmount(importeCell));
            } else {
                registro.setMonto(BigDecimal.ZERO);
            }

            // Oficina -> SUCURSAL (columna 6)
            Cell oficinaCell = row.getCell(6);
            if (oficinaCell != null) {
                registro.setSucursal(getCellValueAsString(oficinaCell, 50));
            } else {
                registro.setSucursal("");
            }

            // Valores por defecto para campos no presentes
            registro.setSaldo(BigDecimal.ZERO);
            registro.setOperacionHora("");
            registro.setReferencia("");
            registro.setEstado("1"); // No conciliado por defecto

            return registro;

        } catch (Exception e) {
            System.err.println("Error procesando fila Continental " + (row.getRowNum() + 1) + ": " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    private Map<String, Integer> getColumnMap(Sheet sheet) {
        // Primero buscar la fila con las cabeceras
        int headerRow = findHeaderRow(sheet);
        if (headerRow < 0)
            return null;

        Map<String, Integer> columnMap = new HashMap<>();
        Row header = sheet.getRow(headerRow);

        // Mapear cada columna por su nombre normalizado
        for (int i = 0; i < header.getLastCellNum(); i++) {
            Cell cell = header.getCell(i);
            if (cell != null && cell.getCellType() == CellType.STRING) {
                String headerName = cell.getStringCellValue().trim().toLowerCase();

                if (headerName.contains("fecha") && !headerName.contains("valuta") && !headerName.contains("pla")) {
                    columnMap.put("fecha", i);
                } else if (headerName.contains("fecha valuta")) {
                    columnMap.put("fecha_valuta", i);
                } else if (headerName.contains("descripción") || headerName.contains("descripcion")) {
                    columnMap.put("descripcion", i);
                } else if (headerName.contains("monto")) {
                    columnMap.put("monto", i);
                } else if (headerName.contains("saldo")) {
                    columnMap.put("saldo", i);
                } else if (headerName.contains("sucursal") || headerName.contains("agencia")) {
                    columnMap.put("sucursal", i);
                } else if ((headerName.contains("operación") || headerName.contains("operacion")) &&
                        (headerName.contains("número") || headerName.contains("numero"))) {
                    columnMap.put("operacion_nro", i);
                } else if ((headerName.contains("operación") || headerName.contains("operacion")) &&
                        headerName.contains("hora")) {
                    columnMap.put("operacion_hora", i);
                } else if (headerName.contains("usuario")) {
                    columnMap.put("usuario", i);
                } else if (headerName.contains("utc")) {
                    columnMap.put("utc", i);
                } else if (headerName.contains("referencia")) {
                    columnMap.put("referencia", i);
                }

                columnMap.put(headerName, i);
            }
        }

        System.out.println("Mapa de columnas creado: " + columnMap);

        return columnMap;
    }

    private int findHeaderRowExacto(Sheet sheet) {
        // Adición específica para buscar exactamente la cabecera mostrada
        String[] expectedHeaders = { "fecha", "fecha valuta", "descripción operación", "monto",
                "saldo", "sucursal - agencia", "operación - número",
                "operación - hora", "usuario", "utc", "referencia" };

        // Buscar en las primeras 30 filas
        for (int i = 0; i <= Math.min(30, sheet.getLastRowNum()); i++) {
            Row row = sheet.getRow(i);
            if (row == null)
                continue;

            // Verificar si esta fila tiene las cabeceras esperadas
            int matchCount = 0;
            for (int j = 0; j < row.getLastCellNum() && j < expectedHeaders.length; j++) {
                Cell cell = row.getCell(j);
                if (cell != null && cell.getCellType() == CellType.STRING) {
                    String cellValue = cell.getStringCellValue().trim().toLowerCase();
                    // Comprobar si coincide con el encabezado esperado
                    if (j < expectedHeaders.length && (cellValue.contains(expectedHeaders[j]) ||
                            expectedHeaders[j].contains(cellValue))) {
                        matchCount++;
                    }
                }
            }

            // Si encontramos suficientes coincidencias (más del 70%)
            if (matchCount > expectedHeaders.length * 0.5) {
                System.out.println("Encontrada fila de cabeceras exactas en posición: " + (i + 1) +
                        " (coincidencias: " + matchCount + "/" + expectedHeaders.length + ")");
                return i;
            }
        }

        return -1;
    }

    // Método para verificar si una cadena parece una fecha
    private boolean isDateString(String text) {
        if (text == null || text.trim().isEmpty())
            return false;

        return text.matches("\\d{1,2}[/.-]\\d{1,2}[/.-]\\d{2,4}") || // DD/MM/YYYY
                text.matches("\\d{2,4}[/.-]\\d{1,2}[/.-]\\d{1,2}") || // YYYY/MM/DD
                text.matches("\\d{1,2}[-]\\w{3}[-]\\d{2,4}") || // DD-MMM-YYYY
                text.matches("\\d{1,2}\\s+\\w{3,}\\s+\\d{2,4}"); // DD Month YYYY
    }

    private String getCellValueAsString(Cell cell, int maxLength) {
        String value = "";
        if (cell != null) {
            switch (cell.getCellType()) {
                case STRING:
                    value = cell.getStringCellValue();
                    break;
                case NUMERIC:
                    if (DateUtil.isCellDateFormatted(cell)) {
                        value = cell.getDateCellValue().toString();
                    } else {
                        value = String.valueOf(cell.getNumericCellValue());
                    }
                    break;
                case BOOLEAN:
                    value = String.valueOf(cell.getBooleanCellValue());
                    break;
                case FORMULA:
                    value = cell.getCellFormula();
                    break;
                default:
                    value = "";
            }
        }
        return value.length() > maxLength ? value.substring(0, maxLength) : value;
    }

    // Método para analizar una fecha de texto con varios formatos posibles
    private LocalDate parseDate(String dateStr) {
        String[] formats = {
                "dd/MM/yyyy", "dd-MM-yyyy", "yyyy/MM/dd", "yyyy-MM-dd",
                "dd/MM/yy", "dd-MM-yy", "MM/dd/yyyy", "MM-dd-yyyy"
        };

        for (String format : formats) {
            try {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern(format);
                return LocalDate.parse(dateStr, formatter);
            } catch (Exception e) {
                // Intentar con el siguiente formato
            }
        }

        throw new IllegalArgumentException("No se pudo parsear la fecha: " + dateStr);
    }

    // Encontrar la celda que probablemente contiene la descripción
    private Cell findDescriptionCell(Row row) {
        Cell longestTextCell = null;
        int maxLength = 0;

        for (int i = 0; i < row.getLastCellNum(); i++) {
            Cell cell = row.getCell(i);
            if (cell != null && cell.getCellType() == CellType.STRING) {
                String text = cell.getStringCellValue().trim();
                if (text.length() > maxLength) {
                    maxLength = text.length();
                    longestTextCell = cell;
                }
            }
        }

        return longestTextCell;
    }

    // Encontrar la celda que probablemente contiene el monto
    private Cell findAmountCell(Row row) {
        // Array de palabras clave que podrían indicar columnas de montos
        String[] montoKeywords = { "monto", "importe", "cantidad", "cargo", "abono", "debe", "haber", "s/", "$" };
        if (row.getRowNum() > 0) {
            Row headerRow = row.getSheet().getRow(row.getRowNum() - 1);
            if (headerRow != null) {
                for (int i = 0; i < headerRow.getLastCellNum(); i++) {
                    Cell headerCell = headerRow.getCell(i);
                    if (headerCell != null && headerCell.getCellType() == CellType.STRING) {
                        String headerText = headerCell.getStringCellValue().toLowerCase();
                        for (String keyword : montoKeywords) {
                            if (headerText.contains(keyword)) {
                                Cell valueCell = row.getCell(i);
                                if (valueCell != null) {
                                    return valueCell;
                                }
                            }
                        }
                    }
                }
            }
        }

        for (int i = 0; i < row.getLastCellNum(); i++) {
            Cell cell = row.getCell(i);
            if (cell != null) {
                if (cell.getCellType() == CellType.NUMERIC) {
                    return cell;
                }
            }
        }

        for (int i = 0; i < row.getLastCellNum(); i++) {
            Cell cell = row.getCell(i);
            if (cell != null && cell.getCellType() == CellType.STRING) {
                String cellValue = cell.getStringCellValue().trim().toLowerCase();

                if (cellValue.matches(".*[s/$]\\s*\\d+([.,]\\d+)?.*") ||
                        cellValue.matches("\\d+([.,]\\d+)?") ||
                        cellValue.matches("[\\-+]?\\d+([.,]\\d+)?")) {
                    return cell;
                }
            }
        }
        int[] commonAmountColumns = { 3, 4, 5, 6, 7 };
        for (int colIdx : commonAmountColumns) {
            if (colIdx < row.getLastCellNum()) {
                Cell cell = row.getCell(colIdx);
                if (cell != null) {
                    String cellValueStr = cell.toString().trim();
                    if (cellValueStr.matches(".*\\d.*")) {
                        return cell;
                    }
                }
            }
        }

        return null;
    }

    private BigDecimal extractAmount(Cell cell) {
        if (cell == null) {
            return BigDecimal.ZERO;
        }

        try {

            System.out.println("Extrayendo monto de celda tipo: " + cell.getCellType());

            if (cell.getCellType() == CellType.NUMERIC) {
                double valor = cell.getNumericCellValue();
                return BigDecimal.valueOf(valor);
            } else if (cell.getCellType() == CellType.STRING) {
                String stringValue = cell.getStringCellValue().trim();

                if (stringValue.isEmpty()) {
                    return BigDecimal.ZERO;
                }

                System.out.println("Procesando valor de celda como texto: '" + stringValue + "'");

                try {
                    // Preparar el valor para la conversión
                    String cleanValue = stringValue
                            .replace("S/", "") // Símbolo de soles
                            .replace("S/.", "") // Variante del símbolo de soles
                            .replace("$", "") // Símbolo de dólar
                            .replace("US$", "") // Dólar americano
                            .replace(" ", "") // Quitar espacios
                            .replace(",", "."); // Usar punto como separador decimal

                    if (cleanValue.matches("\\(.*\\)")) {
                        cleanValue = "-" + cleanValue.replaceAll("[\\(\\)]", "");
                    }

                    cleanValue = cleanValue.replaceAll("[^\\d.\\-]", "");

                    if (cleanValue.isEmpty()) {
                        return BigDecimal.ZERO;
                    }

                    // Manejar múltiples puntos decimales
                    int firstDotIndex = cleanValue.indexOf(".");
                    int lastDotIndex = cleanValue.lastIndexOf(".");
                    if (firstDotIndex != lastDotIndex && firstDotIndex >= 0) {
                        cleanValue = cleanValue.substring(0, lastDotIndex)
                                + cleanValue.substring(lastDotIndex).replace(".", "");
                    }

                    // Agregar cero delante de punto inicial
                    if (cleanValue.startsWith(".")) {
                        cleanValue = "0" + cleanValue;
                    }

                    System.out.println("Valor limpio para conversión: '" + cleanValue + "'");
                    return new BigDecimal(cleanValue);
                } catch (NumberFormatException e) {
                    System.err.println("Error al convertir a número: " + e.getMessage());
                    return BigDecimal.ZERO;
                }
            }
            // Manejar celdas de fórmula
            else if (cell.getCellType() == CellType.FORMULA) {
                // Intentar evaluar la fórmula según el tipo de resultado
                switch (cell.getCachedFormulaResultType()) {
                    case NUMERIC:
                        return BigDecimal.valueOf(cell.getNumericCellValue());
                    case STRING:
                        return convertStringToNumber(cell.getStringCellValue());
                    default:
                        return BigDecimal.ZERO;
                }
            }

            // Para otros tipos, devolver cero
            return BigDecimal.ZERO;
        } catch (Exception e) {
            System.err.println("Error al procesar monto: " + e.getMessage());
            return BigDecimal.ZERO;
        }
    }

    // Método auxiliar para convertir cadenas a números
    private BigDecimal convertStringToNumber(String text) {
        if (text == null || text.trim().isEmpty()) {
            return BigDecimal.ZERO;
        }

        try {
            // Limpiar y convertir
            String cleanValue = text.replaceAll("[^\\d.\\-,]", "")
                    .replace(",", ".");

            if (cleanValue.isEmpty()) {
                return BigDecimal.ZERO;
            }

            // Corregir múltiples puntos decimales
            int firstDot = cleanValue.indexOf(".");
            if (firstDot >= 0 && firstDot != cleanValue.lastIndexOf(".")) {
                cleanValue = cleanValue.substring(0, firstDot + 1)
                        + cleanValue.substring(firstDot + 1).replace(".", "");
            }

            return new BigDecimal(cleanValue);
        } catch (Exception e) {
            return BigDecimal.ZERO;
        }
    }

    // Métodos adicionales para encontrar otras celdas importantes
    private Cell findOperationNumberCell(Row row) {
        // Buscar cabeceras comunes para números de operación
        String[] operacionKeywords = { "operación", "operacion", "nro", "número", "referencia", "ref", "documento",
                "voucher" };

        // 1. Buscar primero por encabezado de columna
        if (row.getRowNum() > 0) {
            Row headerRow = row.getSheet().getRow(row.getRowNum() - 1);
            if (headerRow != null) {
                for (int i = 0; i < headerRow.getLastCellNum(); i++) {
                    Cell headerCell = headerRow.getCell(i);
                    if (headerCell != null && headerCell.getCellType() == CellType.STRING) {
                        String headerText = headerCell.getStringCellValue().toLowerCase();
                        for (String keyword : operacionKeywords) {
                            if (headerText.contains(keyword)) {
                                Cell valueCell = row.getCell(i);
                                if (valueCell != null) {
                                    return valueCell;
                                }
                            }
                        }
                    }
                }
            }
        }

        // 2. Buscar celdas con patrones que parecen números de operación
        for (int i = 0; i < row.getLastCellNum(); i++) {
            Cell cell = row.getCell(i);
            if (cell != null && cell.getCellType() == CellType.STRING) {
                String cellValue = cell.getStringCellValue().trim();
                // Buscar patrones de números de operación: números largos, formatos con
                // guiones, etc.
                if (cellValue.matches("\\d{5,}") ||
                        cellValue.matches("[A-Za-z]+-\\d+") ||
                        cellValue.matches("\\d+-\\d+") ||
                        (cellValue.length() >= 6 && cellValue.matches("[A-Za-z0-9-]+"))) {
                    return cell;
                }
            }
        }

        // 3. Si no encontramos nada, usar cualquier celda con un identificador
        for (int i = 2; i <= 4; i++) {
            if (i < row.getLastCellNum()) {
                Cell cell = row.getCell(i);
                if (cell != null) {
                    return cell;
                }
            }
        }

        return null;
    }

    private Cell findBalanceCell(Row row) {
        // Implementación para buscar el saldo
        return null;
    }

    @Override
    public List<ConciliacionDetalle> procesarConciliacionBancaria(String cuenta, String periodo) {
        // Llama al procedimiento almacenado para obtener los datos de conciliación
        return repository.obtenerEstadoCuentaBanco(cuenta, periodo);
    }

    @Override
    public boolean actualizarEstadoConciliacion(String cuenta, String periodo, Integer item, Integer estadoRegistro,
            Integer itemConciliacion) {
        try {
            repository.actualizarEstadoConciliacion(cuenta, periodo, item, estadoRegistro, itemConciliacion);
            return true;
        } catch (Exception e) {
            System.err.println("Error al actualizar estado de conciliación: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    private void actualizarEstadosRegistros(String cuentaBanco, String periodo, List<Map<String, Object>> resultados) {
        try {
            // 1. Primero, marcar todos los registros como no conciliados (estado 1)
            String resetSql = "UPDATE BDSU_BP.FINA.REGISTRO_BANCO " +
                    "SET ESTADO = '1' " +
                    "WHERE CUENTA = ? AND ANIOMES = ?";

            jdbcTemplate.update(resetSql, cuentaBanco, periodo);

            // 2. Actualizar registros conciliados basados en resultados del SP
            for (Map<String, Object> registro : resultados) {
                String nroOperacion = registro.get("NroOperación") != null ? registro.get("NroOperación").toString()
                        : null;
                Object montoObj = registro.get("Monto");
                Object montoPlaObj = registro.get("montopla");

                if (nroOperacion != null && !nroOperacion.isEmpty()) {
                    String estado = "1"; // Por defecto no conciliado

                    // Verificar si está conciliado (montopla presente)
                    if (montoPlaObj != null) {
                        double monto = montoObj != null ? Double.parseDouble(montoObj.toString()) : 0;
                        double montoPla = Double.parseDouble(montoPlaObj.toString());

                        if (Math.abs(monto - montoPla) < 0.01) {
                            estado = "3"; // Conciliado total
                        } else {
                            estado = "2"; // Conciliado parcial
                        }
                    }

                    // Actualizar estado del registro
                    String updateSql = "UPDATE BDSU_BP.FINA.REGISTRO_BANCO " +
                            "SET ESTADO = ? " +
                            "WHERE CUENTA = ? AND ANIOMES = ? AND OPERACION_NRO = ?";

                    jdbcTemplate.update(updateSql, estado, cuentaBanco, periodo, nroOperacion);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Error al actualizar estados de registros: " + e.getMessage());
        }
    }

    @Override
    public byte[] exportarConciliacionExcel(String cuentaBanco, String periodo) {
        try {
            String sql = "EXEC Sp_estado_cuenta_bco_excel ?, ?";
            List<Map<String, Object>> resultadosIniciales = jdbcTemplate.queryForList(sql, cuentaBanco, periodo);

            List<Map<String, Object>> resultadosConciliados = conciliarConMst01bco(cuentaBanco, periodo,
                    resultadosIniciales);

            List<Map<String, Object>> resultados = aplicarAgrupaciones(resultadosConciliados);

            System.out.println("Datos para Excel obtenidos. Registros totales: " + resultados.size());

            Map<String, Object> infoCuenta = new HashMap<>();

            if (cuentaBanco.equals("1-1136174-1-14") || cuentaBanco.equals("1-1159783-0-78") ||
                    cuentaBanco.equals("215-1795484-017") || cuentaBanco.equals("215-1796545-144") ||
                    cuentaBanco.equals("305-2570356-098") || cuentaBanco.equals("570-2333518-062") ||
                    cuentaBanco.equals("FEC")) {
                infoCuenta.put("banco", "BANCO DE CREDITO DEL PERU");

                if (cuentaBanco.equals("1-1136174-1-14") || cuentaBanco.equals("215-1796545-144")
                        || cuentaBanco.equals("FEC")) {
                    infoCuenta.put("cuenta", "CREDITO (D) - " + cuentaBanco);
                } else {
                    infoCuenta.put("cuenta", "CREDITO (S) - " + cuentaBanco);
                }
            } else if (cuentaBanco.equals("7-0100012637-13") || cuentaBanco.equals("7-0100012645-16")
                    || cuentaBanco.equals("153-10007977444")) {
                infoCuenta.put("banco", "BANCO CONTINENTAL");

                if (cuentaBanco.equals("7-0100012645-16")) {
                    infoCuenta.put("cuenta", "CONTINENTAL (D) - " + cuentaBanco);
                } else {
                    infoCuenta.put("cuenta", "CONTINENTAL (S) - " + cuentaBanco);
                }
            } else if (cuentaBanco.equals("021-098809") || cuentaBanco.equals("00-019-086585")) {
                infoCuenta.put("banco", "BANCO DE LA NACION");
                infoCuenta.put("cuenta", "NACION (S) - " + cuentaBanco);
            } else {
                infoCuenta.put("banco", "No disponible");
                infoCuenta.put("cuenta", cuentaBanco);
            }

            String nombreEmpresa = "SUDAMERICANA DE RODAMIENTOS S.A.C.";
            if (cuentaBanco.equals("153-10007977444") || cuentaBanco.equals("00-019-086585")) {
                nombreEmpresa = "SUDAMERICANA LATIN AMERICA S.A.C.";
            }

            // Contar registros por estado
            int totalRegistros = resultados.size();
            int conciliados = 0;
            int noConciliados = 0;
            int conciliadosPorMst01bco = 0;
            int sobrantesMst01bco = 0;
            double montoConciliado = 0.0;
            double montoNoConciliado = 0.0;
            double montoConciliadoMst01bco = 0.0;
            double montoSobranteMst01bco = 0.0;

            for (Map<String, Object> registro : resultados) {
                Object montoObj = registro.get("Monto");
                Object montoPlaObj = registro.get("montopla");
                String bdOrigin = registro.get("BDorigin") != null ? registro.get("BDorigin").toString() : "";

                if (montoObj == null)
                    continue;

                double monto = Double.parseDouble(montoObj.toString());

                if (montoPlaObj != null) {
                    conciliados++;
                    montoConciliado += monto;

                    if ("mst01bco".equals(bdOrigin)) {
                        conciliadosPorMst01bco++;
                        montoConciliadoMst01bco += monto;
                    }
                } else if ("sobrante_mst01bco".equals(bdOrigin)) {
                    sobrantesMst01bco++;
                    montoSobranteMst01bco += monto;
                } else {
                    noConciliados++;
                    montoNoConciliado += monto;
                }
            }

            String montoConciliadoStr = String.format("%,.2f", montoConciliado);
            String montoNoConciliadoStr = String.format("%,.2f", montoNoConciliado);
            String montoConciliadoMst01bcoStr = String.format("%,.2f", montoConciliadoMst01bco);
            String montoSobranteMst01bcoStr = String.format("%,.2f", montoSobranteMst01bco);

            // Crear Excel
            Workbook workbook = new XSSFWorkbook();
            Sheet sheet = workbook.createSheet("Conciliación Bancaria");

            // Estilos
            CellStyle titleStyle = workbook.createCellStyle();
            Font titleFont = workbook.createFont();
            titleFont.setBold(true);
            titleFont.setFontHeightInPoints((short) 16);
            titleStyle.setFont(titleFont);
            titleStyle.setAlignment(org.apache.poi.ss.usermodel.HorizontalAlignment.CENTER);

            CellStyle subtitleStyle = workbook.createCellStyle();
            Font subtitleFont = workbook.createFont();
            subtitleFont.setBold(true);
            subtitleFont.setFontHeightInPoints((short) 12);
            subtitleStyle.setFont(subtitleFont);

            CellStyle infoLabelStyle = workbook.createCellStyle();
            Font infoLabelFont = workbook.createFont();
            infoLabelFont.setBold(true);
            infoLabelStyle.setFont(infoLabelFont);
            infoLabelStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            infoLabelStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            infoLabelStyle.setBorderBottom(org.apache.poi.ss.usermodel.BorderStyle.THIN);
            infoLabelStyle.setBorderTop(org.apache.poi.ss.usermodel.BorderStyle.THIN);
            infoLabelStyle.setBorderLeft(org.apache.poi.ss.usermodel.BorderStyle.THIN);
            infoLabelStyle.setBorderRight(org.apache.poi.ss.usermodel.BorderStyle.THIN);

            CellStyle infoValueStyle = workbook.createCellStyle();
            infoValueStyle.setBorderBottom(org.apache.poi.ss.usermodel.BorderStyle.THIN);
            infoValueStyle.setBorderTop(org.apache.poi.ss.usermodel.BorderStyle.THIN);
            infoValueStyle.setBorderLeft(org.apache.poi.ss.usermodel.BorderStyle.THIN);
            infoValueStyle.setBorderRight(org.apache.poi.ss.usermodel.BorderStyle.THIN);

            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerFont.setColor(IndexedColors.WHITE.getIndex());
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headerStyle.setBorderBottom(org.apache.poi.ss.usermodel.BorderStyle.THIN);
            headerStyle.setBorderTop(org.apache.poi.ss.usermodel.BorderStyle.THIN);
            headerStyle.setBorderLeft(org.apache.poi.ss.usermodel.BorderStyle.THIN);
            headerStyle.setBorderRight(org.apache.poi.ss.usermodel.BorderStyle.THIN);
            headerStyle.setAlignment(org.apache.poi.ss.usermodel.HorizontalAlignment.CENTER);

            CellStyle conciliadoStyle = workbook.createCellStyle();
            conciliadoStyle.setFillForegroundColor(IndexedColors.LIGHT_GREEN.getIndex());
            conciliadoStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            conciliadoStyle.setBorderBottom(org.apache.poi.ss.usermodel.BorderStyle.THIN);
            conciliadoStyle.setBorderTop(org.apache.poi.ss.usermodel.BorderStyle.THIN);
            conciliadoStyle.setBorderLeft(org.apache.poi.ss.usermodel.BorderStyle.THIN);
            conciliadoStyle.setBorderRight(org.apache.poi.ss.usermodel.BorderStyle.THIN);

            CellStyle conciliadoMst01bcoStyle = workbook.createCellStyle();
            conciliadoMst01bcoStyle.setFillForegroundColor(IndexedColors.LIGHT_TURQUOISE.getIndex());
            conciliadoMst01bcoStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            conciliadoMst01bcoStyle.setBorderBottom(org.apache.poi.ss.usermodel.BorderStyle.THIN);
            conciliadoMst01bcoStyle.setBorderTop(org.apache.poi.ss.usermodel.BorderStyle.THIN);
            conciliadoMst01bcoStyle.setBorderLeft(org.apache.poi.ss.usermodel.BorderStyle.THIN);
            conciliadoMst01bcoStyle.setBorderRight(org.apache.poi.ss.usermodel.BorderStyle.THIN);

            CellStyle noConciliadoStyle = workbook.createCellStyle();
            noConciliadoStyle.setFillForegroundColor(IndexedColors.LIGHT_ORANGE.getIndex());
            noConciliadoStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            noConciliadoStyle.setBorderBottom(org.apache.poi.ss.usermodel.BorderStyle.THIN);
            noConciliadoStyle.setBorderTop(org.apache.poi.ss.usermodel.BorderStyle.THIN);
            noConciliadoStyle.setBorderLeft(org.apache.poi.ss.usermodel.BorderStyle.THIN);
            noConciliadoStyle.setBorderRight(org.apache.poi.ss.usermodel.BorderStyle.THIN);

            CellStyle sobranteMst01bcoStyle = workbook.createCellStyle();
            sobranteMst01bcoStyle.setFillForegroundColor(IndexedColors.ROSE.getIndex());
            sobranteMst01bcoStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            sobranteMst01bcoStyle.setBorderBottom(org.apache.poi.ss.usermodel.BorderStyle.THIN);
            sobranteMst01bcoStyle.setBorderTop(org.apache.poi.ss.usermodel.BorderStyle.THIN);
            sobranteMst01bcoStyle.setBorderLeft(org.apache.poi.ss.usermodel.BorderStyle.THIN);
            sobranteMst01bcoStyle.setBorderRight(org.apache.poi.ss.usermodel.BorderStyle.THIN);

            CellStyle numberSobranteMst01bcoStyle = workbook.createCellStyle();
            numberSobranteMst01bcoStyle.cloneStyleFrom(sobranteMst01bcoStyle);
            numberSobranteMst01bcoStyle.setDataFormat(workbook.createDataFormat().getFormat("#,##0.00"));

            CellStyle numberStyle = workbook.createCellStyle();
            numberStyle.setDataFormat(workbook.createDataFormat().getFormat("#,##0.00"));

            CellStyle numberConciliadoStyle = workbook.createCellStyle();
            numberConciliadoStyle.cloneStyleFrom(conciliadoStyle);
            numberConciliadoStyle.setDataFormat(workbook.createDataFormat().getFormat("#,##0.00"));

            CellStyle numberConciliadoMst01bcoStyle = workbook.createCellStyle();
            numberConciliadoMst01bcoStyle.cloneStyleFrom(conciliadoMst01bcoStyle);
            numberConciliadoMst01bcoStyle.setDataFormat(workbook.createDataFormat().getFormat("#,##0.00"));

            CellStyle numberNoConciliadoStyle = workbook.createCellStyle();
            numberNoConciliadoStyle.cloneStyleFrom(noConciliadoStyle);
            numberNoConciliadoStyle.setDataFormat(workbook.createDataFormat().getFormat("#,##0.00"));

            // -- Título del reporte (fila 0) --
            Row titleRow = sheet.createRow(0);
            Cell titleCell = titleRow.createCell(0);
            titleCell.setCellValue("REPORTE DE CONCILIACIÓN BANCARIA");
            titleCell.setCellStyle(titleStyle);
            sheet.addMergedRegion(new org.apache.poi.ss.util.CellRangeAddress(0, 0, 0, 12));

            // -- Subtítulo (fila 1) --
            Row subtitleRow = sheet.createRow(1);
            Cell subtitleCell = subtitleRow.createCell(0);
            subtitleCell.setCellValue(nombreEmpresa);
            subtitleCell.setCellStyle(subtitleStyle);
            sheet.addMergedRegion(new org.apache.poi.ss.util.CellRangeAddress(1, 1, 0, 12));

            // Dejar una fila vacía
            sheet.createRow(2);

            // -- Información del reporte en una sola celda (filas 3-6) --
            Row row3 = sheet.createRow(3);
            Cell cell3_0 = row3.createCell(0);
            cell3_0.setCellValue("INFORMACIÓN DEL REPORTE");
            cell3_0.setCellStyle(infoLabelStyle);
            sheet.addMergedRegion(new org.apache.poi.ss.util.CellRangeAddress(3, 3, 0, 1));

            // Crear celdas para cada dato
            Row rowFecha = sheet.createRow(4);
            Cell labelFecha = rowFecha.createCell(0);
            labelFecha.setCellValue("Fecha y Hora:");
            labelFecha.setCellStyle(infoLabelStyle);

            Cell valueFecha = rowFecha.createCell(1);
            valueFecha.setCellValue(java.time.LocalDateTime.now().format(
                    java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")));
            valueFecha.setCellStyle(infoValueStyle);

            // Cuenta bancaria
            Row rowCuenta = sheet.createRow(5);
            Cell labelCuenta = rowCuenta.createCell(0);
            labelCuenta.setCellValue("Cuenta Bancaria:");
            labelCuenta.setCellStyle(infoLabelStyle);

            Cell valueCuenta = rowCuenta.createCell(1);
            valueCuenta.setCellValue(infoCuenta.get("cuenta").toString());
            valueCuenta.setCellStyle(infoValueStyle);

            // Periodo
            Row rowPeriodo = sheet.createRow(6);
            Cell labelPeriodo = rowPeriodo.createCell(0);
            labelPeriodo.setCellValue("Periodo:");
            labelPeriodo.setCellStyle(infoLabelStyle);

            Cell valuePeriodo = rowPeriodo.createCell(1);
            String mes = periodo.substring(0, 2);
            String anio = periodo.substring(2);
            valuePeriodo.setCellValue(mes + "/" + anio);
            valuePeriodo.setCellStyle(infoValueStyle);

            // -- RESUMEN DE CONCILIACIÓN --
            // Título del resumen
            Cell cell3_5 = row3.createCell(3);
            cell3_5.setCellValue("RESUMEN DE CONCILIACIÓN");
            cell3_5.setCellStyle(infoLabelStyle);
            sheet.addMergedRegion(new org.apache.poi.ss.util.CellRangeAddress(3, 3, 3, 4));

            // Total de registros
            Cell labelTotal = rowFecha.createCell(3);
            labelTotal.setCellValue("Total de Registros:");
            labelTotal.setCellStyle(infoLabelStyle);

            Cell valueTotal = rowFecha.createCell(4);
            valueTotal.setCellValue(totalRegistros);
            valueTotal.setCellStyle(infoValueStyle);

            // Conciliados con montos
            Cell labelConciliados = rowCuenta.createCell(3);
            labelConciliados.setCellValue("Conciliados:");
            labelConciliados.setCellStyle(infoLabelStyle);

            Cell valueConciliados = rowCuenta.createCell(4);
            valueConciliados.setCellValue(conciliados + " (" + montoConciliadoStr + ")");
            CellStyle conciliadoTextStyle = workbook.createCellStyle();
            conciliadoTextStyle.cloneStyleFrom(conciliadoStyle);
            valueConciliados.setCellStyle(conciliadoTextStyle);

            // No conciliados con montos
            Cell labelNoConciliados = rowPeriodo.createCell(3);
            labelNoConciliados.setCellValue("No Conciliados:");
            labelNoConciliados.setCellStyle(infoLabelStyle);

            Cell valueNoConciliados = rowPeriodo.createCell(4);
            valueNoConciliados.setCellValue(noConciliados + " (" + montoNoConciliadoStr + ")");
            CellStyle noConciliadoTextStyle = workbook.createCellStyle();
            noConciliadoTextStyle.cloneStyleFrom(noConciliadoStyle);
            valueNoConciliados.setCellStyle(noConciliadoTextStyle);

            // conciliaciones por MST01BCO
            Row rowMst01bco = sheet.createRow(7);
            Cell labelMst01bco = rowMst01bco.createCell(3);
            labelMst01bco.setCellValue("Conciliados por Segunda Pasada:");
            labelMst01bco.setCellStyle(infoLabelStyle);

            Cell valueMst01bco = rowMst01bco.createCell(4);
            valueMst01bco.setCellValue(conciliadosPorMst01bco + " (" + montoConciliadoMst01bcoStr + ")");
            CellStyle mst01bcoTextStyle = workbook.createCellStyle();
            mst01bcoTextStyle.cloneStyleFrom(conciliadoMst01bcoStyle);
            valueMst01bco.setCellStyle(mst01bcoTextStyle);

            Row rowSobrantes = sheet.createRow(8);
            Cell labelSobrantes = rowSobrantes.createCell(3);
            labelSobrantes.setCellValue("Transacciones sobrantes en NavaSoft:");
            labelSobrantes.setCellStyle(infoLabelStyle);

            Cell valueSobrantes = rowSobrantes.createCell(4);
            valueSobrantes.setCellValue(sobrantesMst01bco + " (" + montoSobranteMst01bcoStr + ")");
            CellStyle sobrantesTextStyle = workbook.createCellStyle();
            sobrantesTextStyle.cloneStyleFrom(sobranteMst01bcoStyle);
            valueSobrantes.setCellStyle(sobrantesTextStyle);

            // Espacio antes de datos
            sheet.createRow(9);

            // Crear encabezado de tabla
            Row headerRow = sheet.createRow(9);
            String[] headers = {
                    "Fecha", "Descripción", "Nro. Operación", "Monto Banco",
                    "Fecha Planilla", "Nro. Planilla", "Documento", "Referencia",
                    "Monto Planilla", "Glosa", "Cliente", "Usuario", "Estado", "Origen"
            };

            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
                sheet.setColumnWidth(i, 4000); // Ancho inicial
            }

            // Llenar datos
            int rowNum = 10;
            for (Map<String, Object> registro : resultados) {
                Row row = sheet.createRow(rowNum++);

                String estado = "No conciliado";
                String origen = "";
                CellStyle rowStyle = noConciliadoStyle;
                CellStyle numberRowStyle = numberNoConciliadoStyle;

                Double montoBanco = registro.get("Monto") != null ? Double.parseDouble(registro.get("Monto").toString())
                        : 0;
                Double montoNava = registro.get("montopla") != null
                        ? Double.parseDouble(registro.get("montopla").toString())
                        : null;
                String bdOrigin = registro.get("BDorigin") != null ? registro.get("BDorigin").toString() : "";

                if (montoNava != null) {
                    estado = "Conciliado";

                    // Verificar si fue conciliado por MST01BCO
                    if ("mst01bco".equals(bdOrigin)) {
                        origen = "Segunda Pasada";
                        rowStyle = conciliadoMst01bcoStyle;
                        numberRowStyle = numberConciliadoMst01bcoStyle;
                    } else {
                        origen = "Primera Pasada";
                        rowStyle = conciliadoStyle;
                        numberRowStyle = numberConciliadoStyle;
                    }
                } else if ("sobrante_mst01bco".equals(bdOrigin)) {
                    estado = "Sobrante en Navasoft";
                    origen = "No encontrado en Banco";
                    rowStyle = sobranteMst01bcoStyle;
                    numberRowStyle = numberSobranteMst01bcoStyle;
                } else {
                    // No conciliado
                    rowStyle = noConciliadoStyle;
                    numberRowStyle = numberNoConciliadoStyle;
                }

                // Verificar si es un registro agrupado
                boolean esAgrupado = registro.get("NroOperación") != null &&
                        registro.get("NroOperación").toString().startsWith("AGRUPADO-");

                if (esAgrupado) {
                    origen = "Agrupado";

                    // Usar un estilo diferente para registros agrupados
                    CellStyle agrupadoStyle = workbook.createCellStyle();
                    agrupadoStyle.setFillForegroundColor(IndexedColors.LIGHT_YELLOW.getIndex());
                    agrupadoStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
                    agrupadoStyle.setBorderBottom(org.apache.poi.ss.usermodel.BorderStyle.THIN);
                    agrupadoStyle.setBorderTop(org.apache.poi.ss.usermodel.BorderStyle.THIN);
                    agrupadoStyle.setBorderLeft(org.apache.poi.ss.usermodel.BorderStyle.THIN);
                    agrupadoStyle.setBorderRight(org.apache.poi.ss.usermodel.BorderStyle.THIN);

                    CellStyle numberAgrupadoStyle = workbook.createCellStyle();
                    numberAgrupadoStyle.cloneStyleFrom(agrupadoStyle);
                    numberAgrupadoStyle.setDataFormat(workbook.createDataFormat().getFormat("#,##0.00"));

                    rowStyle = agrupadoStyle;
                    numberRowStyle = numberAgrupadoStyle;
                }

                // Datos con formato normal
                Cell cell0 = row.createCell(0);
                cell0.setCellValue(registro.get("Fecha") != null ? registro.get("Fecha").toString() : "");
                cell0.setCellStyle(rowStyle);

                Cell cell1 = row.createCell(1);
                cell1.setCellValue(registro.get("Descripción") != null ? registro.get("Descripción").toString() : "");
                cell1.setCellStyle(rowStyle);

                Cell cell2 = row.createCell(2);
                cell2.setCellValue(registro.get("NroOperación") != null ? registro.get("NroOperación").toString() : "");
                cell2.setCellStyle(rowStyle);

                // Datos numéricos
                Cell cell3 = row.createCell(3);
                cell3.setCellValue(montoBanco);
                cell3.setCellStyle(numberRowStyle);

                // Resto de datos
                Cell cell4 = row.createCell(4);
                cell4.setCellValue(registro.get("Fechapla") != null ? registro.get("Fechapla").toString() : "");
                cell4.setCellStyle(rowStyle);

                Cell cell5 = row.createCell(5);
                cell5.setCellValue(registro.get("nplan") != null ? registro.get("nplan").toString() : "");
                cell5.setCellStyle(rowStyle);

                Cell cell6 = row.createCell(6);
                cell6.setCellValue((registro.get("cdocu") != null ? registro.get("cdocu").toString() : "") +
                        (registro.get("cdocu") != null && registro.get("ndocu") != null ? "-" : "") +
                        (registro.get("ndocu") != null ? registro.get("ndocu").toString() : ""));
                cell6.setCellStyle(rowStyle);

                Cell cell7 = row.createCell(7);
                cell7.setCellValue((registro.get("nomdocref") != null ? registro.get("nomdocref").toString() : "") +
                        (registro.get("nomdocref") != null && registro.get("nrefe") != null ? "-" : "") +
                        (registro.get("nrefe") != null ? registro.get("nrefe").toString() : ""));
                cell7.setCellStyle(rowStyle);

                Cell cell8 = row.createCell(8);
                if (montoNava != null) {
                    cell8.setCellValue(montoNava);
                } else {
                    cell8.setCellValue("");
                }
                cell8.setCellStyle(numberRowStyle);

                Cell cell9 = row.createCell(9);
                String glosa = registro.get("glosa") != null ? registro.get("glosa").toString() : "";
                if (esAgrupado && registro.get("DetalleAgrupacion") != null) {
                    glosa += " | " + registro.get("DetalleAgrupacion").toString();
                }
                cell9.setCellValue(glosa);
                cell9.setCellStyle(rowStyle);

                Cell cell10 = row.createCell(10);
                cell10.setCellValue(registro.get("nomcli") != null ? registro.get("nomcli").toString() : "");
                cell10.setCellStyle(rowStyle);

                Cell cell11 = row.createCell(11);
                cell11.setCellValue(registro.get("nomusu") != null ? registro.get("nomusu").toString() : "");
                cell11.setCellStyle(rowStyle);

                Cell cell12 = row.createCell(12);
                cell12.setCellValue(estado);
                cell12.setCellStyle(rowStyle);

                Cell cell13 = row.createCell(13);
                cell13.setCellValue(origen);
                cell13.setCellStyle(rowStyle);
            }

            int lastRow = rowNum - 1;
            sheet.setAutoFilter(new org.apache.poi.ss.util.CellRangeAddress(9, lastRow, 0, headers.length - 1));

            // Auto-ajustar columnas para mejor visualización
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
                // Establecer un ancho mínimo
                if (sheet.getColumnWidth(i) < 3000) {
                    sheet.setColumnWidth(i, 3000);
                }
                // Limitar ancho máximo para columnas de texto largo
                if (i == 1 || i == 7 || i == 9 || i == 10) {
                    if (sheet.getColumnWidth(i) > 8000) {
                        sheet.setColumnWidth(i, 8000);
                    }
                }
            }

            // Congelar la fila de encabezado para mejor navegación
            sheet.createFreezePane(0, 10);

            // Convertir a bytes
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);
            workbook.close();

            System.out.println("Excel generado correctamente. Tamaño: " + outputStream.size() + " bytes");
            return outputStream.toByteArray();
        } catch (Exception e) {
            System.err.println("Error al generar Excel: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Error al exportar conciliación a Excel: " + e.getMessage());
        }
    }
}